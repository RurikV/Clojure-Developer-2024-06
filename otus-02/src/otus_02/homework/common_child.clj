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

(ns otus-02.homework.common-child
  (:require [criterium.core :as c]))

;; (defn assoc-in!
;;   "Associates a value in a nested structure, where ks is a
;;    sequence of keys and v is the new value and returns a new
;;    nested structure. If any levels do not exist, hash-maps
;;    will be created."
;;   [m [k & ks] v]
;;   (if ks
;;     (assoc! m k (assoc-in! (get m k) ks v))
;;     (assoc! m k v)))

;; Corrected Space-optimized DP solution
(defn longest-common-subsequence-length-space-optimized [^String s1 ^String s2]
  (let [m (count s1)
        n (count s2)
        current-row (int-array (inc n))]
    (doseq [i (range 1 (inc m))]
      (let [previous-row (aclone current-row)]
        (doseq [j (range 1 (inc n))]
          (aset current-row j
                (if (= (.charAt s1 (dec i)) (.charAt s2 (dec j)))
                  (inc (aget previous-row (dec j)))
                  (max (aget current-row (dec j))
                       (aget previous-row j)))))))
    (aget current-row n)))

;; Corrected Iterative DP solution
(defn longest-common-subsequence-length-iterative [^String s1 ^String s2]
  (let [m (count s1)
        n (count s2)]
    (loop [i 0
           prev-row (vec (repeat (inc n) 0))]
      (if (= i m)
        (peek prev-row)
        (recur (inc i)
               (reduce
                (fn [curr-row j]
                  (assoc curr-row j
                         (if (= (.charAt s1 i) (.charAt s2 (dec j)))
                           (inc (get prev-row (dec j)))
                           (max (get curr-row (dec j))
                                (get prev-row j)))))
                (assoc prev-row 0 0)
                (range 1 (inc n))))))))

;; Optimized vector-based solution 
(defn longest-common-subsequence-length-optimized [^String s1 ^String s2]
  (let [m (count s1)
       n (count s2)
       dp (atom (vec (repeat (inc m) (vec (repeat (inc n) 0)))))]
   (doseq [i (range 1 (inc m))
           j (range 1 (inc n))]
     (let [char1 (.charAt s1 (dec i))
           char2 (.charAt s2 (dec j))
           new-val (if (= char1 char2)
                     (inc (get-in @dp [(dec i) (dec j)]))
                     (max (get-in @dp [(dec i) j]) (get-in @dp [i (dec j)])))]
       (swap! dp assoc-in [i j] new-val)))
   (get-in @dp [m n])))

;; Common child length function (wrapper for the chosen implementation)
(defn common-child-length [lcs-fn ^String s1 ^String s2]
  (lcs-fn s1 s2))

;; Test function to verify correctness
(defn run-tests [lcs-fn]
  (println "Running tests:")
  (println "Test 1:" (= (common-child-length lcs-fn "SHINCHAN" "NOHARAAA") 3))
  (println "Test 2:" (= (common-child-length lcs-fn "HARRY" "SALLY") 2))
  (println "Test 3:" (= (common-child-length lcs-fn "AA" "BB") 0))
  (println "Test 4:" (= (common-child-length lcs-fn "ABCDEF" "FBDAMN") 2)))

;; Run the tests with space-optimized version
(println "Testing with space-optimized version:")
(run-tests longest-common-subsequence-length-space-optimized)

;; Run the tests with iterative version
(println "\nTesting with iterative version:")
(run-tests longest-common-subsequence-length-iterative)

;; Run the tests with iterative version
(println "\nTesting with length-optimized version:")
(run-tests longest-common-subsequence-length-optimized)

;; Generate test data (unchanged)
(defn generate-random-string [length]
  (apply str (repeatedly length #(char (+ (rand-int 26) 65)))))

(def small-s1 (generate-random-string 100))
(def small-s2 (generate-random-string 100))
(def medium-s1 (generate-random-string 1000))
(def medium-s2 (generate-random-string 1000))
(def large-s1 (generate-random-string 5000))
(def large-s2 (generate-random-string 5000))

;; New function to measure memory usage
(defn measure-memory-usage []
  (let [runtime (Runtime/getRuntime)]
    (.gc runtime)
    (Thread/sleep 100)
    (.gc runtime)
    (- (.totalMemory runtime) (.freeMemory runtime))))

;; Updated performance testing function with memory usage measurement
(defn run-performance-test [f s1 s2 description]
  (println description)
  (let [start-memory (measure-memory-usage)
        _ (c/quick-bench (f s1 s2))
        end-memory (measure-memory-usage)
        memory-used (- end-memory start-memory)]
    (println "Memory used:" (/ memory-used 1024 1024.0) "MB")))

;; Updated run-all-tests function
(defn run-all-performance-tests []
  (run-performance-test longest-common-subsequence-length-space-optimized small-s1 small-s2 "Space-Optimized DP (Small)")
  (run-performance-test longest-common-subsequence-length-iterative small-s1 small-s2 "Iterative DP (Small)")
  (run-performance-test longest-common-subsequence-length-optimized small-s1 small-s2 "Optimized Vector (Small)")
  
  (run-performance-test longest-common-subsequence-length-space-optimized medium-s1 medium-s2 "Space-Optimized DP (Medium)")
  (run-performance-test longest-common-subsequence-length-iterative medium-s1 medium-s2 "Iterative DP (Medium)")
  (run-performance-test longest-common-subsequence-length-optimized medium-s1 medium-s2 "Optimized Vector (Medium)")
  
  (run-performance-test longest-common-subsequence-length-space-optimized large-s1 large-s2 "Space-Optimized DP (Large)")
  (run-performance-test longest-common-subsequence-length-iterative large-s1 large-s2 "Iterative DP (Large)")
  (run-performance-test longest-common-subsequence-length-optimized large-s1 large-s2 "Optimized Vector (Large)"))

;; Run the tests
(run-all-performance-tests)
