(ns user
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [ooapi-gateway-configurator.core :as core])
  (:import java.util.logging.LogManager))

(def config (core/mk-config env))

(defn start!
  []
  (core/start! config))

(defn stop!
  []
  (core/stop!))
