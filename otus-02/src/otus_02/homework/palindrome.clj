(ns otus-02.homework.palindrome
  (:require [clojure.string :as string]))

(def alphanumeric-pattern #"[a-zA-Z0-9\p{IsIdeographic}]")

(defn alphanumeric? [c]
  (boolean (re-matches alphanumeric-pattern (str c))))

(defn normalize-char [c]
  (string/lower-case (str c)))

(defn is-palindrome [^String input]
  (let [length (.length input)]
    (loop [left 0
           right (dec length)]
      (cond
        (>= left right) true

        (not (alphanumeric? (.charAt input left)))
        (recur (inc left) right)

        (not (alphanumeric? (.charAt input right)))
        (recur left (dec right))

        :else
        (let [left-char (normalize-char (.charAt input left))
              right-char (normalize-char (.charAt input right))]
          (if (= left-char right-char)
            (recur (inc left) (dec right))
            false))))))