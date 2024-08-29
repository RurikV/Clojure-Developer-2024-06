(ns parallel-tetris
  (:import [javax.swing JFrame JPanel JButton JLabel JOptionPane]
           [java.awt Font BorderLayout]
           [java.awt.event ActionListener]))

(defn create-tetris-game []
  (let [state (atom 100)
        view (doto (JLabel. (str @state))
               (.setFont (Font. "Monospaced" Font/BOLD 46)))
        button (JButton. "Start Game")
        frame (doto (JFrame. "Tetris")
                (.add (doto (JPanel. (BorderLayout.))
                        (.add button BorderLayout/NORTH)
                        (.add view)))
                (.setSize 400 200)
                (.setLocationByPlatform true)
                (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE))]

    (letfn [(set-view [] (.setText view (str @state)))
            (game-loop []
              (while (pos? @state)
                (swap! state dec)
                (Thread/sleep 1000)
                (set-view))
              (JOptionPane/showMessageDialog frame "Game Over!"))]

      (.addActionListener
       button
       (proxy [ActionListener] []
         (actionPerformed [evt]
           (.setEnabled button false)
           (future (game-loop)))))

      (.setVisible frame true))))

(defn create-multiple-games [n]
  (dotimes [_ n]
    (future (create-tetris-game))))

(defn -main [& args]
  (let [num-games (if (seq args)
                    (Integer/parseInt (first args))
                    3)]
    (create-multiple-games num-games)))

(parallel-tetris/-main)