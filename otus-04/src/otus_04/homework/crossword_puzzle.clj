(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

(defn parse-input
  "Parses the input string into a grid and a list of words."
  [input]
  (let [lines (str/split-lines input)
        grid (mapv vec (butlast lines))
        words (str/split (last lines) #";")]
    [grid words]))

(defn find-word-slot
  "Finds a word slot (a sequence of - characters) in a crossword grid 
   where a word can potentially be placed. 
   It works by starting at a specific position in the grid 
   and checking in a specified direction (:across or :down) 
   to see how many consecutive - characters it can find, 
   which would indicate the length of the word slot."
  [grid [y x] direction]
  (let [length (count (take-while #(= % \-)
                                  (map #(get-in grid [(if (= direction :down) (+ y %) y)
                                                      (if (= direction :across) (+ x %) x)])
                                       (range))))]
    (when (> length 1)
      {:y y, :x x, :length length, :direction direction})))

(defn find-all-slots
  "Finds all valid word slots in the grid."
  [grid]
  (let [height (count grid)
        width (count (first grid))]
    (for [y (range height)
          x (range width)
          direction [:across :down]
          :let [slot (find-word-slot grid [y x] direction)]
          :when slot]
      slot)))

(defn can-place-word?
  "Checks if a word can be placed in the given slot without conflicts."
  [grid {:keys [y x length direction]} word]
  (every? true?
          (map-indexed
           (fn [idx letter]
             (let [cy (if (= direction :across) y (+ y idx))
                   cx (if (= direction :across) (+ x idx) x)
                   current (get-in grid [cy cx])]
               (or (= current \-) (= current letter))))
           word)))

(defn place-word
  "Places a word in the given slot on the grid."
  [grid {:keys [y x direction]} word]
  (reduce (fn [g idx]
            (let [cy (if (= direction :across) y (+ y idx))
                  cx (if (= direction :across) (+ x idx) x)]
              (assoc-in g [cy cx] (nth word idx))))
          grid
          (range (count word))))

(defn solve-crossword
  "Recursively solves the crossword puzzle using backtracking."
  [grid words slots]
  (if (empty? words)
    grid  ; All words placed, puzzle solved
    (if (empty? slots)
      nil  ; No more slots but words remaining, backtrack
      (let [slot (first slots)
            possible-words (filter #(= (count %) (:length slot)) words)]
        (loop [remaining-words possible-words]
          (if (empty? remaining-words)
            (solve-crossword grid words (rest slots))  ; Try next slot
            (let [word (first remaining-words)]
              (if (can-place-word? grid slot word)
                (let [new-grid (place-word grid slot word)
                      new-words (remove #{word} words)
                      new-slots (remove #(= % slot) slots)
                      result (solve-crossword new-grid new-words new-slots)]
                  (if result
                    result
                    (recur (rest remaining-words))))  ; Try next word
                (recur (rest remaining-words))))))))))  ; Word doesn't fit, try next

(defn solve
  "Возвращает решённый кроссворд. Аргумент является строкой вида

  +-++++++++
  +-++++++++
  +-++++++++
  +-----++++
  +-+++-++++
  +-+++-++++
  +++++-++++
  ++------++
  +++++-++++
  +++++-++++
  LONDON;DELHI;ICELAND;ANKARA

  Все строки вплоть до предпоследней описывают лист бумаги, а символами
  '-' отмечены клетки для вписывания букв. В последней строке перечислены
  слова, которые нужно 'вписать' в 'клетки'. Слова могут быть вписаны
  сверху-вниз или слева-направо."
  [input]
  (let [[grid words] (parse-input input)
        slots (find-all-slots grid)
        solution (solve-crossword grid words slots)]
    (if solution
      (str/join "\n" (map str/join solution))
      "No solution found")))