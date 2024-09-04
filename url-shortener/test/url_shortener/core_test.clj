(ns url-shortener.core-test
  (:require [clojure.test :refer :all]
            [url-shortener.core :as core]))

(deftest test-int->id
  (testing "int->id conversion"
    (are [input expected] (= expected (core/int->id input))
      0 "0"
      61 "z"
      62 "10"
      9999999999999 "2q3Rktod"
      9223372036854775807 "AzL8n0Y58W7"))

  (testing "int->id with negative numbers"
    (is (thrown? IllegalArgumentException (core/int->id -1)))))

(deftest test-id->int
  (testing "id->int conversion"
    (are [input expected] (= expected (core/id->int input))
      "0" 0
      "z" 61
      "10" 62
      "clj" 149031
      "Clojure" 725410830262))

  (testing "id->int with invalid characters"
    (is (thrown? NullPointerException (core/id->int "invalid!")))))

(deftest test-shorten!
  (testing "shorten! with automatic ID generation"
    (let [url "https://example.com"
          id (core/shorten! url)]
      (is (string? id))
      (is (= url (core/url-for id)))))

  (testing "shorten! with custom ID"
    (let [url "https://clojure.org"
          custom-id "clj"]
      (is (= custom-id (core/shorten! url custom-id)))
      (is (= url (core/url-for custom-id)))))

  (testing "shorten! with existing ID"
    (let [url1 "https://example.com"
          url2 "https://another-example.com"
          id (core/shorten! url1 "existing")]
      (is (= id "existing"))
      (is (nil? (core/shorten! url2 "existing")))
      (is (= url1 (core/url-for "existing"))))))

(deftest test-url-for
  (testing "url-for with existing ID"
    (let [url "https://example.com"
          id (core/shorten! url)]
      (is (= url (core/url-for id)))))

  (testing "url-for with non-existing ID"
    (is (nil? (core/url-for "non-existing")))))

(deftest test-list-all
  (testing "list-all returns all shortened URLs"
    (core/shorten! "https://example1.com" "ex1")
    (core/shorten! "https://example2.com" "ex2")
    (let [all-urls (core/list-all)]
      (is (vector? all-urls))
      (is (>= (count all-urls) 2))
      (is (every? #(and (:id %) (:url %)) all-urls))
      (is (some #(= % {:id "ex1" :url "https://example1.com"}) all-urls))
      (is (some #(= % {:id "ex2" :url "https://example2.com"}) all-urls)))))