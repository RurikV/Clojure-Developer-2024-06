(ns url-shortener.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljs.test :refer-macros [run-tests] :refer [report]]
            [url-shortener.frontend-test]))

(println "Starting test runner")

(defmethod report [::cljs.test/default :end-run-tests] [m]
  (println "Test results:")
  (println (pr-str m)))

(defn ^:export run []
  (run-tests 'url-shortener.frontend-test))

(doo-tests 'url-shortener.frontend-test)

;; Wait longer for async tests to complete
(js/setTimeout #(println "Test runner finished") 5000)