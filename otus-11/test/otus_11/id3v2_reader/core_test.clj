(ns otus-11.id3v2-reader.core-test
  (:require [clojure.test :refer :all]
            [otus-11.id3v2-reader.core :as core :refer [read-synchsafe-integer find-id3v2-tag create-decoder decode-frame extract-frames read-id3v2-tags]])
  (:import [otus_11.id3v2_reader.core ISO88591Decoder UTF16Decoder UTF16BEDecoder UTF8Decoder]
           [java.io ByteArrayInputStream]))

(deftest test-read-synchsafe-integer
  (testing "Read synchsafe integer"
    (is (= 128 (read-synchsafe-integer [0 0 1 0])))
    (is (= 127 (read-synchsafe-integer [0 0 0 127])))
    (is (= 268435455 (read-synchsafe-integer [127 127 127 127])))))

(deftest test-find-id3v2-tag
  (testing "Find valid ID3v2 tag"
    (let [valid-tag (byte-array (concat (.getBytes "ID3") [4 0 64 0 0 0 16]))]
      (is (= {:version 4 :revision 0 :flags 64 :size 16} (find-id3v2-tag valid-tag)))))

  (testing "Invalid ID3v2 tag"
    (let [invalid-tag (byte-array (.getBytes "NOT"))]
      (is (nil? (find-id3v2-tag invalid-tag))))))

(deftest test-create-decoder
  (testing "Create decoders for different encodings"
    (is (instance? ISO88591Decoder (create-decoder 0)))
    (is (instance? UTF16Decoder (create-decoder 1)))
    (is (instance? UTF16BEDecoder (create-decoder 2)))
    (is (instance? UTF8Decoder (create-decoder 3)))
    (is (instance? UTF8Decoder (create-decoder 4)))))  ; Неизвестная кодировка

(deftest test-decode-text
  (testing "Decode text with different encodings"
    (let [test-string "Test String"]
      (is (= test-string (core/decode (create-decoder 0) (.getBytes test-string "ISO-8859-1"))))
      (is (= test-string (core/decode (create-decoder 1) (.getBytes test-string "UTF-16"))))
      (is (= test-string (core/decode (create-decoder 2) (.getBytes test-string "UTF-16BE"))))
      (is (= test-string (core/decode (create-decoder 3) (.getBytes test-string "UTF-8")))))))

(deftest test-decode-frame
  (testing "Decode different frame types"
    (let [test-data (byte-array (cons 0 (.getBytes "Test Data" "ISO-8859-1")))]
      (is (= {:album "Test Data"} (decode-frame "TALB" test-data)))
      (is (= {:artist "Test Data"} (decode-frame "TPE1" test-data)))
      (is (= {:title "Test Data"} (decode-frame "TIT2" test-data)))
      (is (= {:year "Test Data"} (decode-frame "TYER" test-data)))
      (is (= {:genre "Test Data"} (decode-frame "TCON" test-data)))
      (is (= {:unknown "Test Data"} (decode-frame "UNKN" test-data))))))

(deftest test-extract-frames
  (testing "Extract frames from tag data"
    (let [tag-data (byte-array (concat
                                (.getBytes "TALB") [0 0 0 11] [0 0] (.getBytes "\u0000Test Album")
                                (.getBytes "TPE1") [0 0 0 12] [0 0] (.getBytes "\u0000Test Artist")
                                (.getBytes "TIT2") [0 0 0 11] [0 0] (.getBytes "\u0000Test Title")))]
      (is (= {:album "Test Album" :artist "Test Artist" :title "Test Title"}
             (extract-frames tag-data))))))

(deftest test-read-id3v2-tags
  (testing "Read ID3v2 tags from file"
    (let [mock-data (byte-array (concat
                                 (.getBytes "ID3") [4 0 0 0 0 0 46]  ; Header
                                 (.getBytes "TALB") [0 0 0 11] [0 0] (.getBytes "\u0000Test Album")
                                 (.getBytes "TPE1") [0 0 0 12] [0 0] (.getBytes "\u0000Test Artist")))
          mock-input-stream (ByteArrayInputStream. mock-data)]
      (with-redefs [clojure.java.io/input-stream (constantly mock-input-stream)]
        (is (= {:album "Test Album" :artist "Test Artist"}
               (read-id3v2-tags "test.mp3")))))))

(run-tests)