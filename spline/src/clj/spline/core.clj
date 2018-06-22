(ns spline.core
  (:import
    (org.apache.spark.sql SparkSession)
    (org.formcept.spline lineageInitializer))
  (:gen-class))

(def spark-session
  (-> (SparkSession/builder)
      (.master "local[*]")
      (.appName "Spline")
      (.config "spline.mongodb.url" "mongodb://localhost")
      (.config "spline.mongodb.name" "spline")
      (.config "spline.mode" "BEST_EFFORT")
      (.config "spline.persistence.factory"
               "za.co.absa.spline.persistence.mongo.MongoPersistenceFactory")
      (.getOrCreate)))

;; lineageInitializer
(.initLineage (lineageInitializer.) spark-session)

;; DataFrame-1
(def df-1
  (-> (.read spark-session)
      (.option "header" "true")
      (.option "inferSchema" "true")
      (.csv "resources/data1.csv")
      (.as "source")))

;; DataFrame-1
(def df-2
  (-> (.read spark-session)
      (.option "header" "true")
      (.option "inferSchema" "true")
      (.csv "resources/data2.csv")
      (.as "source")))

;; Join both the DataFrames
(def joined-df
  (.join df-1 df-2 (.gt (.col df-1 "A") (.col df-2 "G")) "inner"))

;; Write joined DataFrame into a file
(-> (.write joined-df)
    (.mode "overwrite")
    (.csv "/home/abhi/spline.csv"))
