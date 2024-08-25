(ns otus-04.homework.magic-square)

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method

(ns otus-04.homework.magic-square)

(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  (let [square (vec (repeat n (vec (repeat n 0))))
        total-cells (* n n)]
    (loop [square square
           x (quot n 2)
           y 0
           num 1]
      ;; (println "Iteration" num)
      ;; (println "Square so far:" square)
      ;; (println "Current position: (" y ", " x ")")
      (if (> num total-cells)
        square
        (let [next-x (mod (inc x) n)
              next-y (mod (dec y) n)]
          ;; (println "Next position: (" next-y ", " next-x ")")
          (if (zero? (get-in square [next-y next-x]))
            (recur (assoc-in square [y x] num)
                   next-x
                   next-y
                   (inc num))
            (recur (assoc-in square [y x] num)
                   x
                   (mod (inc y) n)
                   (inc num))))))))

