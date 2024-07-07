(ns otus-02.homework.pangram
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(def alphabet-set (set "abcdefghijklmnopqrstuvwxyz"))

(defn is-pangram [^String input]
  (let [normalized-chars (-> input
                             string/lower-case
                             (string/replace #"[^a-z]" "")
                             set)]
    (set/subset? alphabet-set normalized-chars)))

