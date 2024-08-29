(ns simplified-tetris.core
  (:import [javax.swing JFrame JPanel Timer]
           [java.awt Color Dimension Graphics2D]
           [java.awt.event ActionListener KeyAdapter KeyEvent])
  (:gen-class))

(def board-width 10)
(def board-height 20)
(def cell-size 30)

(defn create-empty-board []
  (vec (repeat board-height (vec (repeat board-width 0)))))

(defn new-game []
  {:board (create-empty-board)
   :score 0})

(defn create-tetris-panel []
  (let [game-state (atom (new-game))]
    (proxy [JPanel ActionListener] []
      (paintComponent [^Graphics2D g]
        (proxy-super paintComponent g)
        (let [{:keys [board score]} @game-state]
          (.setColor g Color/BLACK)
          (.fillRect g 0 0 (* board-width cell-size) (* board-height cell-size))

          ; Draw grid
          (.setColor g Color/DARK_GRAY)
          (doseq [x (range board-width)
                  y (range board-height)]
            (.drawRect g (* x cell-size) (* y cell-size) cell-size cell-size))

          ; Draw some test cells
          (.setColor g Color/RED)
          (.fillRect g 0 0 cell-size cell-size)
          (.setColor g Color/BLUE)
          (.fillRect g (* 5 cell-size) (* 10 cell-size) cell-size cell-size)

          ; Draw score
          (.setColor g Color/WHITE)
          (.drawString g (str "Score: " score) 10 20))

        (println "Paint component called"))

      (getPreferredSize []
        (Dimension. (* board-width cell-size) (* board-height cell-size)))

      (actionPerformed [e]
        (println "Timer tick")
        (.repaint this)))))

(defn create-tetris-game []
  (let [frame (JFrame. "Simplified Tetris")
        panel (create-tetris-panel)
        timer (Timer. 1000 panel)]
    (doto frame
      (.add panel)
      (.pack)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setVisible true))
    (.start timer)
    (println "Game window created")))

(defn -main [& args]
  (javax.swing.SwingUtilities/invokeLater create-tetris-game))