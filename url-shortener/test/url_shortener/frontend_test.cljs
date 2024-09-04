(ns url-shortener.frontend-test
  (:require [cljs.test :refer-macros [deftest is testing async use-fixtures]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [reagent.dom.server :as rds]
            [url-shortener.frontend :as frontend]
            [cljs.core.async :refer [chan put! <!]]
            [cljs-http.client :as http]))

(def jsdom (js/require "jsdom"))
(def JSDOM (.-JSDOM jsdom))

(defn create-dom []
  (let [dom (JSDOM. "<html><body><div id='app'></div></body></html>" #js {:url "http://localhost"})]
    (set! js/window (.-window dom))
    (set! js/document (.-document js/window))
    (set! js/Node (.. js/window -Node))
    (set! js/NodeList (.. js/window -NodeList))
    (set! js/Element (.. js/window -Element))
    (set! js/HTMLElement (.. js/window -HTMLElement))
    (set! js/HTMLButtonElement (.. js/window -HTMLButtonElement))
    (set! js/MouseEvent (.. js/window -MouseEvent))
    dom))

(def dom-fixture
  {:before #(create-dom)})

(use-fixtures :once dom-fixture)

(println "Loading frontend-test namespace")

(defn render-to-string [component]
  (rds/render-to-string component))

(deftest test-header
  (testing "Header component renders correctly"
    (let [rendered (render-to-string [frontend/header "Test Header"])]
      (is (re-find #"Test Header" rendered)))))

(deftest test-short-page
  (testing "Short page component renders correctly"
    (reset! frontend/*app-state {:page :short :short-url "http://short.url/abc"})
    (let [rendered (render-to-string [frontend/short-page])]
      (is (re-find #"Your short link" rendered))
      (is (re-find #"http://short.url/abc" rendered))
      (is (re-find #"BACK" rendered)))))

(deftest test-main-page
  (testing "Main page component renders correctly"
    (let [rendered (render-to-string [frontend/main-page])]
      (is (re-find #"Shorten a long link" rendered))
      (is (re-find #"Paste a long URL" rendered))
      (is (re-find #"SHORTEN IT" rendered)))))

(deftest test-app
  (testing "App component renders main page by default"
    (reset! frontend/*app-state {:page :main})
    (let [rendered (render-to-string [frontend/app])]
      (is (re-find #"Shorten a long link" rendered))))

  (testing "App component renders short page when state is set"
    (reset! frontend/*app-state {:page :short :short-url "http://short.url/abc"})
    (let [rendered (render-to-string [frontend/app])]
      (is (re-find #"Your short link" rendered)))))


(deftest test-shorten-url
  (println "Running shorten URL test")
  (testing "Shortening URL updates app state"
    (async done
      (let [mock-response (chan)
            original-post http/post]
        (with-redefs [http/post (fn [_ _] mock-response)]
          (let [input-value (r/atom "https://example.com")
                component (r/create-class
                            {:reagent-render
                             (fn []
                               [frontend/main-page])
                             :component-did-mount
                             (fn [this]
                               (let [node (.getElementById js/document "app")
                                     _ (rdom/render [frontend/main-page] node)
                                     button (-> node (.getElementsByTagName "button") (aget 0))]
                                 (if button
                                   (do
                                     (.click button)
                                     (put! mock-response {:success true :body {:id "abc"}})
                                     (js/setTimeout
                                      (fn []
                                        (println "Shorten URL test callback executed")
                                        (is (= :short (:page @frontend/*app-state)))
                                        (is (= "http://short.url/abc" (:short-url @frontend/*app-state)))
                                        (done))
                                      200))
                                   (do
                                     (println "Button not found")
                                     (done)))))})]
            (rdom/render [component] (.getElementById js/document "app"))))))))
