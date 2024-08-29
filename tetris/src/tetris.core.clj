(ns tetris.core
  (:import [javax.swing JFrame JPanel Timer]
           [java.awt Color Dimension]
           [java.awt.event ActionListener KeyAdapter KeyEvent]))

(def board-width 10)
(def board-height 20)
(def cell-size 30)

(def tetrominoes
  {'I [[1 1 1 1]]
   'J [[1 0 0]
       [1 1 1]]
   'L [[0 0 1]
       [1 1 1]]
   'O [[1 1]
       [1 1]]
   'S [[0 1 1]
       [1 1 0]]
   'T [[0 1 0]
       [1 1 1]]
   'Z [[1 1 0]
       [0 1 1]]})

(defn create-empty-board []
  (vec (repeat board-height (vec (repeat board-width nil)))))

(def game-state (atom {:board (create-empty-board)
                       :piece (rand-nth (keys tetrominoes))
                       :position [(quot board-width 2) 0]
                       :score 0
                       :game-over false}))

(defn rotate-piece [piece]
  (apply mapv vector (reverse piece)))

(defn valid-position? [board piece [x y]]
  (every? (fn [[dy row]]
            (every? (fn [[dx cell]]
                      (let [new-x (+ x dx)
                            new-y (+ y dy)]
                        (or (zero? cell)
                            (and (< -1 new-x board-width)
                                 (< -1 new-y board-height)
                                 (nil? (get-in board [new-y new-x]))))))
                    (map-indexed vector row)))
          (map-indexed vector piece)))

(defn place-piece [board piece [x y]]
  (reduce (fn [new-board [dy row]]
            (reduce (fn [nb [dx cell]]
                      (if (pos? cell)
                        (assoc-in nb [(+ y dy) (+ x dx)] piece)
                        nb))
                    new-board
                    (map-indexed vector row)))
          board
          (map-indexed vector (tetrominoes piece))))

(defn remove-complete-rows [board]
  (let [completed-rows (count (filter #(every? some? %) board))]
    [(remove #(every? some? %) board) completed-rows]))

(defn update-game [game]
  (let [{:keys [board piece position]} game
        [x y] position
        new-position [x (inc y)]]
    (if (valid-position? board (tetrominoes piece) new-position)
      (assoc game :position new-position)
      (let [new-board (place-piece board piece position)
            [cleared-board completed-rows] (remove-complete-rows new-board)
            rows-added (- board-height (count cleared-board))
            final-board (into (vec (repeat rows-added (vec (repeat board-width nil)))) cleared-board)
            new-piece (rand-nth (keys tetrominoes))
            new-position [(quot board-width 2) 0]]
        (if (valid-position? final-board (tetrominoes new-piece) new-position)
          (-> game
              (assoc :board final-board
                     :piece new-piece
                     :position new-position)
              (update :score + (* completed-rows completed-rows 100)))
          (assoc game :game-over true))))))

(defn move-left [game]
  (let [new-x (dec (first (:position game)))
        new-position [new-x (second (:position game))]]
    (if (valid-position? (:board game) (tetrominoes (:piece game)) new-position)
      (assoc game :position new-position)
      game)))

(defn move-right [game]
  (let [new-x (inc (first (:position game)))
        new-position [new-x (second (:position game))]]
    (if (valid-position? (:board game) (tetrominoes (:piece game)) new-position)
      (assoc game :position new-position)
      game)))

(defn rotate [game]
  (let [current-piece (:piece game)
        current-position (:position game)
        rotated-piece (rotate-piece (tetrominoes current-piece))]
    (if (valid-position? (:board game) rotated-piece current-position)
      (assoc game :piece (first (filter #(= rotated-piece (tetrominoes %)) (keys tetrominoes))))
      game)))

(defn game-panel []
  (let [panel (proxy [JPanel ActionListener] []
                (paintComponent [g]
                  (proxy-super paintComponent g)
                  (let [{:keys [board piece position score]} @game-state
                        [piece-x piece-y] position]
                    (.setColor g Color/BLACK)
                    (.fillRect g 0 0 (* board-width cell-size) (* board-height cell-size))
                    (doseq [y (range board-height)
                            x (range board-width)]
                      (when-let [cell (get-in board [y x])]
                        (.setColor g (get {'I Color/CYAN
                                           'J Color/BLUE
                                           'L Color/ORANGE
                                           'O Color/YELLOW
                                           'S Color/GREEN
                                           'T Color/MAGENTA
                                           'Z Color/RED}
                                          cell
                                          Color/GRAY))
                        (.fillRect g (* x cell-size) (* y cell-size) cell-size cell-size)))
                    (when piece
                      (let [piece-cells (tetrominoes piece)]
                        (.setColor g (get {'I Color/CYAN
                                           'J Color/BLUE
                                           'L Color/ORANGE
                                           'O Color/YELLOW
                                           'S Color/GREEN
                                           'T Color/MAGENTA
                                           'Z Color/RED}
                                          piece
                                          Color/GRAY))
                        (doseq [y (range (count piece-cells))
                                x (range (count (first piece-cells)))]
                          (when (pos? (get-in piece-cells [y x]))
                            (.fillRect g (* (+ piece-x x) cell-size) (* (+ piece-y y) cell-size) cell-size cell-size)))))
                    (.setColor g Color/WHITE)
                    (.drawString g (str "Score: " score) 10 20)))
                (getPreferredSize []
                  (Dimension. (* board-width cell-size) (* board-height cell-size)))
                (actionPerformed [e]
                  (swap! game-state update-game)
                  (.repaint this)))
        key-adapter (proxy [KeyAdapter] []
                      (keyPressed [e]
                        (case (.getKeyCode e)
                          KeyEvent/VK_LEFT  (swap! game-state move-left)
                          KeyEvent/VK_RIGHT (swap! game-state move-right)
                          KeyEvent/VK_UP    (swap! game-state rotate)
                          KeyEvent/VK_DOWN  (swap! game-state update-game)
                          nil)
                        (.repaint panel)))]
    (doto panel
      (.setFocusable true)
      (.addKeyListener key-adapter))))

(defn game-frame []
  (doto (JFrame. "Clojure Tetris")
    (.add (game-panel))
    (.pack)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))

(defn start-game []
  (let [frame (game-frame)
        panel (.getComponent frame 0)
        timer (Timer. 500 panel)]
    (.start timer)))

(defn -main [& args]
  (start-game))


(tetris.core/-main)