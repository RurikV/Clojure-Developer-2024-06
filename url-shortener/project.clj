(defproject url-shortener "0.1.0-SNAPSHOT"
  :description "URL shortener app"

  :source-paths ["src" "resources"]
  :resource-paths ["resources"]

  :dependencies [;; Backend
                 [org.clojure/clojure "1.11.1"]
                 [ring/ring-jetty-adapter "1.11.0"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.1"]
                 [org.slf4j/slf4j-simple "2.0.10"]
                 [ring/ring-mock "0.3.2"]

                 ;; Frontend
                 [reagent "1.1.1"]
                 [org.clojure/core.async "1.6.681"]
                 [cljs-http "0.1.48"
                  :exclusions [org.clojure/core.async
                               com.cognitect/transit-cljs
                               com.cognitect/transit-js]]
                 [thheller/shadow-cljs "2.27.2"] ; Keep it synced with npm version!
                 [doo "0.1.11"]
                 [org.clojure/clojurescript "1.11.60"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]]

  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-figwheel "0.5.20"]
            [lein-doo "0.1.11"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "url-shortener.frontend/on-js-reload"}
                :compiler {:main url-shortener.frontend
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/url_shortener.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/url_shortener.min.js"
                           :main url-shortener.frontend
                           :optimizations :advanced
                           :pretty-print false}}
               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:main url-shortener.test-runner
                           :output-to "target/test/test.js"
                           :output-dir "target/test"
                           :target :nodejs
                           :optimizations :none
                           :source-map true}}]}

  :doo {:build "test"
      :alias {:default [:node]}
      :paths {:node "node"}
      :verbosity 3
      :debug true}

  :aliases {"testjs" ["doo" "node" "test" "once"]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :main ^:skip-aot url-shortener.core

  :uberjar-name "url-shortener.jar"

  :profiles {:dev {:source-paths ["src" "dev"]
                   :dependencies [[ring/ring-devel "1.11.0"]
                                  [binaryage/devtools "1.0.6"]
                                  [figwheel-sidecar "0.5.20"]
                                  [cider/piggieback "0.5.3"]]}

             :repl {:repl-options {:init-ns user}}

             :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

             :uberjar {:aot :all}})