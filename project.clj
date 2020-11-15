(defproject bored-again "0.1.0-SNAPSHOT"
  :description "Just another blog"
  :url "https://bored-again.com"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring "1.8.2"]
                 [compojure "1.6.2"]
                 [hiccup "2.0.0-alpha2"]
                 [com.layerware/hugsql "0.5.1"]
                 [org.postgresql/postgresql "42.2.18"]
                 [environ "1.2.0"]
                 [ring-basic-authentication "1.1.0"]]
  :plugins [[lein-environ "1.2.0"]]
  :main ^:skip-aot bored-again.core
  :target-path "target/%s"
  :profiles {:dev [:project/dev :profiles/dev]
             :test [:project/test :profiles/test]
             ;; only edit :profiles/* in profiles.clj
             :profiles/dev  {}
             :profiles/test {}})
