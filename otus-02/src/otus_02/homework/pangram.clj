(ns otus-02.homework.pangram)

(defn is-pangram [^String input]
  (loop [chars input
         found-letters #{}]
    (if (or (empty? chars) (= 26 (count found-letters)))
      (= 26 (count found-letters))
      (let [current-char (Character/toLowerCase (first chars))]
        (if (and (>= (int current-char) (int \a))
                 (<= (int current-char) (int \z)))
          (recur (rest chars) (conj found-letters current-char))
          (recur (rest chars) found-letters))))))