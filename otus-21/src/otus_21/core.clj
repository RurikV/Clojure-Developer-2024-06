;; * Clojure Developer, урок 21
(ns otus-21.core
  (:require [clojure.zip :as z]
            [clojure.walk :as w]))

;; * Древовидные структуры

(def bin-tree
  [3 [1 nil [2 nil nil]]
   [7 [5 [4 nil nil] [6 nil nil]]
    [8 nil nil]]])

(def dict
  [:c [:a [:b
           :t]
       :olor]])

;; * Графы

;;   b - c
;;  /   /
;; a   e   g
;;  \ /   /
;;   d - f

;; ** Матрица смежности

;;   a b c d e f g
;; a x 1 0 1 0 0 0
;; b 1 x 1 0 0 0 0
;; c 0 1 x 0 1 0 1
;; d       x     .
;; e         x   .
;; f           x .
;; g . . . . . . x

;; ** Списки смежности

;; a: b, d
;; b: a, c
;; c: b, e
;; d: a, e, f
;; e: c, d
;; f: d, g
;; g: c

;; *** пример

(def gr
  {:a [:b :d]
   :b [:a :c]
   :c [:b :e]
   :d [:a :e :f]
   :e [:c :d]
   :f [:d :g]
   :g [:c]})

;; * clojure.walk

;; ** walk

(comment
  (w/walk (fn [x] (if (number? x) (str x) x))
          identity bin-tree)
  ;; ^ walk не рекурсивен сам по себе,
  ;; нужно на его основе писать свой обходчик:

  (letfn [(sum [t]
            (cond (coll? t)
                  (w/walk sum (partial reduce +) t)

                  (nil? t) 0

                  true t))]
    (sum bin-tree)))

;; ** prewalk/postwalk

(comment
  (w/prewalk str bin-tree)
  (w/postwalk
   (fn [x] (if (number? x) (str x) x))
   bin-tree))

;; * clojure.zip

(comment
  (-> bin-tree
      z/vector-zip
      z/down
      z/right
      z/down
      z/rightmost
      z/down
      (z/edit + 100)
      z/root)

  (-> bin-tree
      z/vector-zip
      z/down
      z/right
      z/down))

;; ** функция zipper и самодельные "застёжки"

(def gr-zipper
  (z/zipper
   (constantly true)
   #(get gr %)
   (fn [n _] n)
   :a))

(defn tap [z]
  (let [n (z/node z)]
    (println n)
    z))

(comment
  (-> gr-zipper
      z/down
      z/down
      z/right
      z/down
      z/right
      z/right
      tap
      z/children))

;; * Поиск

;; ** Depth-first search, поиск в глубину

(defn dfs [done? branch start]
  (loop [visited #{}
         queue (list [start '()])]
    (when (not-empty queue)
      (let [[[pos path] & other] queue]
        (cond (done? pos)
              (reverse (cons pos path))

              (visited pos)
              (recur visited other)

              :else
              (let [new-path (cons pos path)]
                (recur (conj visited pos)
                       (concat (for [v (branch pos)]
                                 [v new-path])
                               other))))))))

(comment
  (dfs #(= % :g)
       #(get gr %)
       :b)
  ;; => (:b :a :d :f :g)

  ;; если поменять порядок вариантов при ветвлении,
  ;; результат будет другой!
  (dfs #(= % :g)
       #(reverse (get gr %))
       :b)
  ;; => (:b :c :e :d :f :g)
  )

;; ** Breadth-first search, поиск в ширину

(defn bfs [done? branch start]
  (loop [visited #{}
         state {start '()}]
    (if-let [k (first (filter done? (keys state)))]
      (reverse (cons k (state k)))

      (when (not-empty state)
        (recur (into visited (keys state))
               (into {}
                     (for [[p ps] state
                           :let [path (cons p ps)]
                           v (branch p)
                           :when (not (visited v))]
                       [v path])))))))

(comment
  (bfs #(= % :g)
       #(get gr %)
       :b)
  ;; => (:b :a :d :f :g)

  ;; изменение порядка вариантов при ветвленни не влияет
  ;; на длину найденного пути (но может быть выбран другой
  ;; путь той же длины, если таковых несколько)
  (bfs #(= % :g)
       #(reverse (get gr %))
       :b)
  ;; => (:b :a :d :f :g)
  )
