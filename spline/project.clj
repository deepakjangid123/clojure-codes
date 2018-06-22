(defproject spline "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.slf4s/slf4s-api_2.11 "1.7.25"]
                 [za.co.absa.spline/spline-core "0.3.1"]
                 [za.co.absa.spline/spline-core-spark-adapter-2.2 "0.3.1"]
                 [za.co.absa.spline/spline-persistence-mongo "0.3.1"]
                 [org.mongodb/casbah-core_2.11 "3.1.1"]
                 [org.apache.spark/spark-core_2.11 "2.2.0"]
                 [org.apache.spark/spark-sql_2.11 "2.2.0"]]
  :main spline.core
  :source-paths ["src/clj"]
  :resource-paths ~(concat ["resources"])
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :compile-path "src/scala/target/scala-2.11/classes"
  :aot :all)
