(ns otus-02.homework.square-code
  (:require [clojure.string :as str]))

;; Реализовать классический метод составления секретных сообщений, называемый `square code`.
;; Выведите закодированную версию полученного текста.

;; Во-первых, текст нормализуется: из текста удаляются пробелы и знаки препинания,
;; также текст переводится в нижний регистр.
;; Затем нормализованные символы разбиваются на строки.
;; Эти строки можно рассматривать как образующие прямоугольник при печати их друг под другом.

;; Например,
"If man was meant to stay on the ground, god would have given us roots."
;; нормализуется в строку:
"ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots"

;; Разбиваем текст в виде прямоугольника.
;; Размер прямоугольника (rows, cols) должен определяться длиной сообщения,
;; так что c >= r и c - r <= 1, где c — количество столбцов, а r — количество строк.
;; Наш нормализованный текст имеет длину 54 символа
;; и представляет собой прямоугольник с c = 8 и r = 7:
"ifmanwas"
"meanttos"
"tayonthe"
"groundgo"
"dwouldha"
"vegivenu"
"sroots  "

;; Закодированное сообщение получается путем чтения столбцов слева направо.
;; Сообщение выше закодировано как:
"imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau"

;; Полученный закодированный текст разбиваем кусками, которые заполняют идеальные прямоугольники (r X c),
;; с кусочками c длины r, разделенными пробелами.
;; Для фраз, которые на n символов меньше идеального прямоугольника,
;; дополните каждый из последних n фрагментов одним пробелом в конце.
"imtgdvs fearwer mayoogo anouuio ntnnlvt wttddes aohghn  sseoau "

;; Обратите внимание, что если бы мы сложили их,
;; мы могли бы визуально декодировать зашифрованный текст обратно в исходное сообщение:

"imtgdvs"
"fearwer"
"mayoogo"
"anouuio"
"ntnnlvt"
"wttddes"
"aohghn "
"sseoau "

(defn- normalize-string [^String input]
  (-> input
      str/lower-case
      (str/replace #"[^\w]" "")))

(defn- calculate-dimensions [^String input]
  (let [length (count input)
        cols (int (Math/ceil (Math/sqrt length)))
        rows (int (Math/ceil (/ length cols)))]
    [rows cols]))

(defn encode-string [^String input]
  (let [normalized (normalize-string input)
        [_ cols] (calculate-dimensions normalized)
        chunks (partition-all cols normalized)
        padded-chunks (map #(take cols (concat % (repeat \space))) chunks)]
    (->> (apply map vector padded-chunks)
         (map #(apply str %))
         (str/join " "))))

(defn decode-string [^String ciphertext]
  (let [words (str/split ciphertext #"\s+")
        cols (count words)
        rows (apply max (map count words))
        matrix (mapv #(vec (take rows (concat % (repeat \space)))) words)]
    (->> (for [row (range rows)
               col (range cols)
               :let [char (get-in matrix [col row])]
               :when (not= char \space)]
           char)
         (apply str))))