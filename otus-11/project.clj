(defproject otus-11 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :test-paths ["test"]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/test.check "1.1.0"]]

  :repl-options {:init-ns otus-11.core}

  :main ^:skip-aot otus-11.id3v2-reader.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
