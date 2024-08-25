(ns otus-04.homework.scramblies)

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
 (let [letter-freq (frequencies letters)
       word-freq (frequencies word)]
   (every? (fn [[char count]]
             (>= (get letter-freq char 0) count))
           word-freq)))
