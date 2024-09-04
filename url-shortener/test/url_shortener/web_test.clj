(ns url-shortener.web-test
  (:require [clojure.test :refer :all]
            [url-shortener.web :as web]
            [url-shortener.core :as core]
            [ring.mock.request :as mock]
            [cheshire.core :as json]))

(defn parse-json-body [response]
  (if (string? (:body response))
    (json/parse-string (:body response) true)
    (:body response)))

(use-fixtures :each (fn [f]
                      (core/clear-all-mappings!)
                      (f)))

(deftest test-retain
  (testing "retain with new URL"
    (let [response (web/retain "https://example.com")
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (:id body))))

  (testing "retain with custom ID"
    (let [response (web/retain "https://clojure.org" "clj")
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (= "clj" (:id body)))))

  (testing "retain with existing ID"
    (core/shorten! "https://existing.com" "existing")
    (let [response (web/retain "https://new.com" "existing")
          body (parse-json-body response)]
      (is (= 409 (:status response)))
      (is (= "Short URL existing is already taken" (:error body))))))

(deftest test-router
  (testing "POST / - create new short URL (JSON)"
    (let [response (web/handler (-> (mock/request :post "/")
                                    (mock/content-type "application/json")
                                    (mock/body (json/generate-string {:url "https://example.com"}))))
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (:id body))))

  (testing "POST / - create new short URL (form-encoded)"
    (let [response (web/handler (-> (mock/request :post "/")
                                    (mock/content-type "application/x-www-form-urlencoded")
                                    (mock/body "url=https://example.com")))
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (:id body))))

  (testing "POST / - missing URL parameter"
    (let [response (web/handler (mock/request :post "/"))
          body (parse-json-body response)]
      (is (= 400 (:status response)))
      (is (= "No `url` parameter provided" (:error body)))))

  (testing "PUT /:id - create custom short URL (JSON)"
    (let [response (web/handler (-> (mock/request :put "/custom")
                                    (mock/content-type "application/json")
                                    (mock/body (json/generate-string {:url "https://example.com"}))))
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (= "custom" (:id body)))))

  (testing "PUT /:id - create custom short URL (form-encoded)"
    (let [response (web/handler (-> (mock/request :put "/custom2")
                                    (mock/content-type "application/x-www-form-urlencoded")
                                    (mock/body "url=https://example.com")))
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (= "custom2" (:id body)))))

  (testing "GET /:id - redirect to long URL"
    (core/shorten! "https://example.com" "ex")
    (let [response (web/handler (mock/request :get "/ex"))]
      (is (= 302 (:status response)))
      (is (= "https://example.com" (get-in response [:headers "Location"])))))

  (testing "GET /:id - non-existing short URL"
    (let [response (web/handler (mock/request :get "/non-existing"))
          body (parse-json-body response)]
      (is (= 404 (:status response)))
      (is (= "Requested URL not found." (:error body)))))

  (testing "GET /list/ - list all URLs"
    (core/shorten! "https://example1.com" "ex1")
    (core/shorten! "https://example2.com" "ex2")
    (let [response (web/handler (mock/request :get "/list/"))
          body (parse-json-body response)]
      (is (= 200 (:status response)))
      (is (vector? (:urls body)))
      (is (>= (count (:urls body)) 2))
      (is (some #(= % {:id "ex1" :url "https://example1.com"}) (:urls body)))
      (is (some #(= % {:id "ex2" :url "https://example2.com"}) (:urls body))))))

(deftest test-handler
  (testing "handler serves static resources"
    (let [response (web/handler (mock/request :get "/index.html"))]
      (is (= 200 (:status response)))
      (is (= "text/html" (get-in response [:headers "Content-Type"])))))

  (testing "handler processes JSON requests"
    (let [response (web/handler (-> (mock/request :post "/")
                                    (mock/content-type "application/json")
                                    (mock/body (json/generate-string {:url "https://example.com"}))))
          body (parse-json-body response)]
      (is (= 201 (:status response)))
      (is (:id body)))))