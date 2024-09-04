(ns url-shortener.web
  (:require [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as response]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [url-shortener.core :as shortener]))

(defn retain
  ([url]
   (if-let [id (shortener/shorten! url)]
     (response/created (str "/" id) {:id id})
     (response/bad-request {:error "Failed to create short URL"})))
  ([url id]
   (if-let [created-id (shortener/shorten! url id)]
     (response/created (str "/" created-id) {:id created-id})
     (-> (response/response {:error (format "Short URL %s is already taken" id)})
         (response/status 409)))))

(defn extract-url [request]
  (let [params (:params request)
        body-params (:body-params request)
        json-params (:json-params request)
        url (or (get params :url)
                (get params "url")
                (get body-params :url)
                (get body-params "url")
                (get json-params :url)
                (get json-params "url"))]
    (println "Extracted URL:" url)
    (println "Request params:" params)
    (println "Body params:" body-params)
    (println "JSON params:" json-params)
    url))

(compojure/defroutes router
  (compojure/GET "/" []
    (response/resource-response "index.html" {:root "public"}))

  (compojure/POST "/" request
    (println "Received POST request:" request)
    (if-let [url (extract-url request)]
      (retain url)
      (response/bad-request {:error "No `url` parameter provided"})))

  (compojure/PUT "/:id" [id :as request]
    (println "Received PUT request:" request)
    (if-let [url (extract-url request)]
      (retain url id)
      (response/bad-request {:error "No `url` parameter provided"})))

  (compojure/GET "/:id" [id]
    (if-let [url (shortener/url-for id)]
      (response/redirect url)
      (response/not-found {:error "Requested URL not found."})))

  (compojure/GET "/list/" []
    (response/response {:urls (shortener/list-all)}))

  (route/not-found "Not Found"))

(def handler
  (-> router
      (wrap-json-params)
      (wrap-json-response)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)))

(comment
  (handler {:uri "/"
            :request-method :post
            :params {:url "https://github.com/Clojure-Developer/Clojure-Developer-2023-10"}})

  (handler {:uri "/clj"
            :request-method :put
            :params {:url "https://clojure.org"}})

  (router {:uri "/list/"
           :request-method :get})

  (handler {:uri "/clj"
            :request-method :get})
  )
