(ns kafka-streams.core
  (:import
    (org.apache.kafka.clients.producer KafkaProducer ProducerRecord)
    (org.apache.kafka.common.serialization Serdes)
    (org.apache.kafka.streams KafkaStreams StreamsConfig)
    (org.apache.kafka.streams.kstream KStream KStreamBuilder Transformer
                                      TransformerSupplier)
    (org.apache.kafka.streams.processor ProcessorContext))
  (:gen-class))

;; -----------------------------------------------------------------------------
;; Clojure transducers interface to kafka-streams
;; -----------------------------------------------------------------------------
(deftype TransducerTransformer [step-fn ^{:volatile-mutable true} context]
  Transformer
  (init [_ c]
    (set! context c))
  (transform [_ k v]
    (try
      (step-fn context [k v])
      (catch Exception e
        (.printStackTrace e)))
    nil)
  (punctuate [^Transformer this ^long t])
  (close [_]))

(defn- kafka-streams-step
  ([context] context)
  ([^ProcessorContext context [k v]]
   (.forward context k v)
   (.commit context)
   context))

(defn transformer
  "Creates a transducing transformer for use in Kafka Streams topologies."
  [xform]
  (TransducerTransformer. (xform kafka-streams-step) nil))

(defn transformer-supplier
  [xform]
  (reify
    TransformerSupplier
    (get [_] (transformer xform))))

;; Transduce Kafka Stream
(defn transduce-kstream
  ^KStream [^KStream kstream xform]
  (.transform kstream (transformer-supplier xform) (into-array String [])))
;; -----------------------------------------------------------------------------

(def xform
  (comp (filter (fn [[k v]] (string? v)))
        (map (fn [[k v]] [v k]))
        (filter (fn [[k v]] (= "foo" v)))))

(def builder (KStreamBuilder.))

(def kstream
  (-> builder
      (.stream (into-array String ["tset"]))
      (transduce-kstream xform)
      (.to "test")))

;; Properties
(def props
  {StreamsConfig/APPLICATION_ID_CONFIG    "my-app"
   StreamsConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:9092"
   StreamsConfig/KEY_SERDE_CLASS_CONFIG   (.getName (.getClass (Serdes/String)))
   StreamsConfig/VALUE_SERDE_CLASS_CONFIG (.getName (.getClass (Serdes/String)))})

(def config
  (StreamsConfig. props))

(def kafka-streams
  (KafkaStreams. builder config))

;; Start kafka streaming
(.start kafka-streams)

;; Producer
(def producer
  (KafkaProducer. {"bootstrap.servers" "localhost:9092"
                   "acks"              "all"
                   "retries"           "0"
                   "key.serializer"    "org.apache.kafka.common.serialization.StringSerializer"
                   "value.serializer"  "org.apache.kafka.common.serialization.StringSerializer"}))

(prn (str "Sending a message to topic 'tset', on which actions will be performed"
          " and if conditions are met then sent to output-topic which is 'test' in this case..."))
;; Transformation will happen based on the transform function `(transduce-kstream input xform)`
(.send producer (ProducerRecord. "tset" "foo" "AB")) ;; tset = topic-name


(prn (str "Sending a message to topic 'tset', on which actions will be performed"
          " and if conditions are met then sent to output-topic which is 'test' in this case..."))
(.send producer (ProducerRecord. "tset" "baz" "quux"))

(prn "Closing kafka-producer...")
(.close producer)

(prn "Closing kafka-streams...")
(.close kafka-streams)
