(ns otus-02.homework.common-child
  (:require [clojure.string :as str]))

;; Строка называется потомком другой строки,
;; если она может быть образована путем удаления 0 или более символов из другой строки.
;; Буквы нельзя переставлять.
;; Имея две строки одинаковой длины, какую самую длинную строку можно построить так,
;; чтобы она была потомком обеих строк?

;; Например 'ABCD' и 'ABDC'

;; Эти строки имеют два дочерних элемента с максимальной длиной 3, ABC и ABD.
;; Их можно образовать, исключив D или C из обеих строк.
;; Ответ в данном случае - 3

;; Еще пример HARRY и SALLY. Ответ будет - 2, так как общий элемент у них AY

(defn- create-dp-matrix [rows cols]
  (vec (repeat (inc rows) (vec (repeat (inc cols) 0)))))

(defn longest-common-subsequence-length [^String first-string ^String second-string]
  (let [first-length (count first-string)
        second-length (count second-string)
        initial-matrix (create-dp-matrix first-length second-length)
        final-matrix
        (reduce
         (fn [matrix [first-idx second-idx]]
           (assoc-in matrix [first-idx second-idx]
                     (if (= (.charAt first-string (dec first-idx))
                            (.charAt second-string (dec second-idx)))
                       (inc (get-in matrix [(dec first-idx) (dec second-idx)]))
                       (max (get-in matrix [(dec first-idx) second-idx])
                            (get-in matrix [first-idx (dec second-idx)])))))
         initial-matrix
         (for [first-idx (range 1 (inc first-length))
               second-idx (range 1 (inc second-length))]
           [first-idx second-idx]))]
    (get-in final-matrix [first-length second-length])))

(defn common-child-length [^String first-string ^String second-string]
  (longest-common-subsequence-length first-string second-string))