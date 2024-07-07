(ns otus-02.homework.palindrome
  (:require [clojure.string :as string]))

(def non-alphanumeric-pattern #"\W+")

(defn normalize-string [^String input]
  (-> input
      string/lower-case
      (string/replace non-alphanumeric-pattern "")))

(defn is-palindrome [^String input]
  (let [normalized-input (normalize-string input)]
    (= normalized-input (string/reverse normalized-input))))
