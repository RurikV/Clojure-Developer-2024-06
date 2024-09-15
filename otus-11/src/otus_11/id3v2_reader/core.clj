(ns otus-11.id3v2-reader.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io RandomAccessFile))
  (:gen-class))

;; Протокол для декодирования текста
(defprotocol TextDecoder
  (decode [this data]))

;; Реализации декодеров для разных кодировок
(defrecord ISO88591Decoder []
  TextDecoder
  (decode [_ data] (String. data "ISO-8859-1")))

(defrecord UTF16Decoder []
  TextDecoder
  (decode [_ data] (String. data "UTF-16")))

(defrecord UTF16BEDecoder []
  TextDecoder
  (decode [_ data] (String. data "UTF-16BE")))

(defrecord UTF8Decoder []
  TextDecoder
  (decode [_ data] (String. data "UTF-8")))

;; Фабричный метод для создания декодера
(defn create-decoder [encoding]
  (case (int encoding)
    0 (->ISO88591Decoder)
    1 (->UTF16Decoder)
    2 (->UTF16BEDecoder)
    3 (->UTF8Decoder)
    (->UTF8Decoder)))  ; По умолчанию используем UTF-8

;; Мультиметод для декодирования фреймов
(defmulti decode-frame (fn [frame-id _] frame-id))

(defmethod decode-frame "TALB" [_ data] {:album (decode (create-decoder (first data)) (byte-array (rest data)))})
(defmethod decode-frame "TPE1" [_ data] {:artist (decode (create-decoder (first data)) (byte-array (rest data)))})
(defmethod decode-frame "TIT2" [_ data] {:title (decode (create-decoder (first data)) (byte-array (rest data)))})
(defmethod decode-frame "TYER" [_ data] {:year (decode (create-decoder (first data)) (byte-array (rest data)))})
(defmethod decode-frame "TCON" [_ data] {:genre (decode (create-decoder (first data)) (byte-array (rest data)))})
(defmethod decode-frame :default [frame-id data]
  (let [key (keyword (if (= (str/lower-case frame-id) "unkn")
                       "unknown"
                       (str/lower-case frame-id)))]
    {key (decode (create-decoder (first data)) (byte-array (rest data)))}))

(defn read-synchsafe-integer [bytes]
  (reduce (fn [result byte]
            (-> result
                (bit-shift-left 7)
                (bit-or (bit-and byte 0x7F))))
          0
          bytes))

(defn find-id3v2-tag [data]
  (let [id (String. (byte-array (take 3 data)) "ASCII")]
    (println "Tag ID:" id)
    (when (= id "ID3")
      (let [major-version (nth data 3)
            revision (nth data 4)
            flags (nth data 5)
            size (read-synchsafe-integer (drop 6 data))]
        (println "Version:" major-version)
        (println "Revision:" revision)
        (println "Flags:" flags)
        (println "Size:" size)
        {:version major-version
         :revision revision
         :flags flags
         :size size}))))

(defn extract-frames [data]
  (loop [remaining data
         frames {}]
    (if (< (count remaining) 10)
      frames
      (let [frame-id (String. (byte-array (take 4 remaining)) "ASCII")
            frame-size (read-synchsafe-integer (take 4 (drop 4 remaining)))
            frame-data (take frame-size (drop 10 remaining))]
        (if (and (re-matches #"[A-Z0-9]{4}" frame-id) (pos? frame-size) (<= frame-size (- (count remaining) 10)))
          (let [decoded-frame (decode-frame frame-id (byte-array frame-data))]
            (recur (drop (+ 10 frame-size) remaining)
                   (merge frames decoded-frame)))
          (recur (drop 1 remaining) frames))))))

(defn read-id3v2-tags [file-path]
  (println "Attempting to read file:" file-path)
  (with-open [input-stream (clojure.java.io/input-stream file-path)]
    (let [header (byte-array 10)]
      (.read input-stream header)
      (when-let [tag (find-id3v2-tag header)]
        (let [tag-size (:size tag)
              tag-data (byte-array tag-size)]
          (.read input-stream tag-data)
          (println "Found ID3v2 tag, reading frames...")
          (extract-frames tag-data))))))

(defn -main [& args]
  (if-let [file-path (first args)]
    (try
      (let [tags (read-id3v2-tags file-path)]
        (if tags
          (do
            (println "\nExtracted tag information:")
            (doseq [[k v] tags]
              (println (format "%s: %s" k v))))
          (println "Failed to extract tags from the file.")))
      (catch Exception e
        (println "Error reading file:" (.getMessage e))
        (.printStackTrace e)))
    (println "Please provide a file path as an argument")))