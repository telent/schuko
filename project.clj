(defproject schuko "0.1.0-SNAPSHOT"
  :plugins [[lein-cljsbuild "0.3.3"]]
  :description "Powerpoint, Euro style"
  :url "http://ww.telent.net"
  :hooks [leiningen.cljsbuild]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :cljsbuild {
              :builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/main.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :aot [schuko.core]
  :main schuko.core
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [endophile "0.1.1"]
                 [org.clojure/clojurescript "0.0-1859"
                  :exclusions [org.apache.ant/ant]]
                 [hiccup "1.0.1"]
                 ])
