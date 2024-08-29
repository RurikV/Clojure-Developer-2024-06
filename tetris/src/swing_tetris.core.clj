(ns swing-tetris.core
  (:import [javax.swing JFrame JPanel Timer JButton]
           [java.awt Color Dimension BorderLayout])
  (:gen-class))

(def board-width 10)
(def board-height 20)
(def cell-size 30)

(def tetrominos
  {:I0 [[1 1 1 1]]
   :I1 [[1]
        [1]
        [1]
        [1]]
   :J0 [[1 0 0]
        [1 1 1]]
   :J1 [[1 1]
        [1 0]
        [1 0]]
   :J2 [[1 1 1]
        [0 0 1]]
   :J3 [[0 1]
        [0 1]
        [1 1]]
   :L0 [[0 0 1]
        [1 1 1]]
   :L1 [[1 0]
        [1 0]
        [1 1]]
   :L2 [[1 1 1]
        [1 0 0]]
   :L3 [[1 1]
        [0 1]
        [0 1]]
   :O [[1 1]
       [1 1]]
   :S0 [[0 1 1]
        [1 1 0]]
   :S1 [[1 0]
        [1 1]
        [0 1]]
   :T0 [[0 1 0]
        [1 1 1]]
   :T1 [[1 0]
        [1 1]
        [1 0]]
   :T2 [[1 1 1]
        [0 1 0]]
   :T3 [[0 1]
        [1 1]
        [0 1]]
   :Z0 [[1 1 0]
        [0 1 1]]
   :Z1 [[0 1]
        [1 1]
        [1 0]]})

;; Define the rotation sequence for each piece
(def rotations
  {:I0 [:I1], :I1 [:I0]
   :O [:O]
   :J0 [:J1], :J1 [:J2], :J2 [:J3], :J3 [:J0]
   :L0 [:L1], :L1 [:L2], :L2 [:L3], :L3 [:L0]
   :S0 [:S1], :S1 [:S0]
   :T0 [:T1], :T1 [:T2], :T2 [:T3], :T3 [:T0]
   :Z0 [:Z1], :Z1 [:Z0]})


;; Define colors for each tetromino type
(def tetromino-colors
  {:I0 Color/CYAN, :I1 Color/CYAN
   :J0 Color/BLUE, :J1 Color/BLUE, :J2 Color/BLUE, :J3 Color/BLUE
   :L0 Color/ORANGE, :L1 Color/ORANGE, :L2 Color/ORANGE, :L3 Color/ORANGE
   :O Color/YELLOW
   :S0 Color/GREEN, :S1 Color/GREEN
   :T0 Color/MAGENTA, :T1 Color/MAGENTA, :T2 Color/MAGENTA, :T3 Color/MAGENTA
   :Z0 Color/RED, :Z1 Color/RED})

(defn create-empty-board []
  (vec (repeat board-height (vec (repeat board-width nil)))))

(defn new-game []
  {:board (create-empty-board)
   :piece (rand-nth (keys tetrominos))
   :position [(int (/ board-width 2)) 0]
   :game-over false
   :score 0})

(defn get-next-rotation [piece]
    (get rotations piece))

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
          (map-indexed vector (tetrominos piece))))

(defn remove-complete-rows [board]
  (let [completed-rows (count (filter #(every? some? %) board))]
    [(into (vec (repeat completed-rows (vec (repeat board-width nil))))
           (remove #(every? some? %) board))
     completed-rows]))

(defn update-game [game]
  (let [{:keys [board piece position]} game
        [x y] position
        new-position [x (inc y)]]
    (if (valid-position? board (tetrominos piece) new-position)
      (assoc game :position new-position)
      (let [new-board (place-piece board piece position)
            [cleared-board completed-rows] (remove-complete-rows new-board)
            new-piece (rand-nth (keys tetrominos))
            new-position [(int (/ board-width 2)) 0]]
        (if (valid-position? cleared-board (tetrominos new-piece) new-position)
          (-> game
              (assoc :board cleared-board
                     :piece new-piece
                     :position new-position)
              (update :score + (* completed-rows completed-rows 100)))
          (assoc game :game-over true))))))

(defn move-left [game]
  (let [new-x (dec (first (:position game)))
        new-position [new-x (second (:position game))]]
    (if (valid-position? (:board game) (tetrominos (:piece game)) new-position)
      (assoc game :position new-position)
      game)))

(defn move-right [game]
  (let [new-x (inc (first (:position game)))
        new-position [new-x (second (:position game))]]
    (if (valid-position? (:board game) (tetrominos (:piece game)) new-position)
      (assoc game :position new-position)
      game)))

(defn rotate [game]
  (let [current-piece (:piece game)
        current-position (:position game)
        next-rotation (first (get-next-rotation current-piece))
        rotated-piece (tetrominos next-rotation)]
        ;; (println current-piece next-rotation)
    (if (valid-position? (:board game) rotated-piece current-position)
      (assoc game :piece next-rotation)
      game)))

(defn create-tetris-panel [game-state]
  (let [panel (proxy [JPanel ActionListener] []
                (paintComponent [g]
                  (proxy-super paintComponent g)
                  (let [{:keys [board piece position score game-over]} @game-state
                        [piece-x piece-y] position]
                    (.setColor g Color/BLACK)
                    (.fillRect g 0 0 (* board-width cell-size) (* board-height cell-size))
                    (doseq [y (range board-height)
                            x (range board-width)]
                      (when-let [cell (get-in board [y x])]
                        (.setColor g (get tetromino-colors cell Color/GRAY))
                        (.fillRect g (* x cell-size) (* y cell-size) cell-size cell-size)))
                    (when piece
                      (let [piece-cells (tetrominos piece)]
                        (.setColor g (get tetromino-colors piece Color/GRAY))
                        (doseq [y (range (count piece-cells))
                                x (range (count (first piece-cells)))]
                          (when (pos? (get-in piece-cells [y x]))
                            (.fillRect g (* (+ piece-x x) cell-size) (* (+ piece-y y) cell-size) cell-size cell-size)))))
                    (.setColor g Color/WHITE)
                    (.drawString g (str "Score: " score) 10 20)
                    (when game-over
                      (let [font (java.awt.Font. "Arial" java.awt.Font/BOLD 48)  ;; Set a larger font size (e.g., 48)
                            metrics (.getFontMetrics g font)
                            text "Game Over!"
                            text-width (.stringWidth metrics text)
                            text-height (.getHeight metrics)
                            x (/ (- (* board-width cell-size) text-width) 2)
                            y (/ (+ (* board-height cell-size) text-height) 2)]
                        (.setFont g font)
                        (.setColor g Color/RED)
                        (.drawString g text x y)))))
                    (getPreferredSize []
                                      (Dimension. (* board-width cell-size) (* board-height cell-size)))
                    (actionPerformed [e]
                                     (swap! game-state update-game)
                                     (.repaint this)))
        key-listener (proxy [KeyAdapter] []
                       (keyPressed [e]
                         (when-not (:game-over @game-state)
                           (case (.getKeyCode e)
                             37 (swap! game-state move-left)
                             39 (swap! game-state move-right)
                             38 (swap! game-state rotate)
                             40 (swap! game-state update-game)
                             nil))
                         (.repaint panel)))]
    (.addKeyListener panel key-listener)
    (.setFocusable panel true)
    panel))

(defn create-tetris-game []
  (let [frame (JFrame. "Tetris")
        game-state (atom (new-game))
        panel (create-tetris-panel game-state)
        timer (Timer. 500 panel)
        new-game-button (JButton. "New Game")]
    (.addActionListener
     new-game-button
     (proxy [ActionListener] []
       (actionPerformed [_]
         (reset! game-state (new-game))
         (.stop timer)
         (.start timer)
         (.requestFocusInWindow panel))))
    (doto frame
      (.setLayout (BorderLayout.))
      (.add panel BorderLayout/CENTER)
      (.add new-game-button BorderLayout/SOUTH)
      (.pack)
      (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
      (.addWindowListener
       (proxy [WindowAdapter] []
         (windowClosing [_]
           (.stop timer)
           (println "Tetris game closed. REPL is still running.")))))
    (.setVisible frame true)
    (.requestFocusInWindow panel)
    (.start timer)))

(defn -main []
  (javax.swing.SwingUtilities/invokeLater create-tetris-game)
  (println "Tetris game started. Close the game window to return to the REPL.")
  (println "The REPL will remain active after closing the game."))

(swing-tetris.core/-main)
(swing-tetris.core/-main)