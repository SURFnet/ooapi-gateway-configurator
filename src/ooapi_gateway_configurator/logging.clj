;; Copyright (C) 2021, 2022 SURFnet B.V.
;;
;; This program is free software: you can redistribute it and/or modify it
;; under the terms of the GNU General Public License as published by the Free
;; Software Foundation, either version 3 of the License, or (at your option)
;; any later version.
;;
;; This program is distributed in the hope that it will be useful, but WITHOUT
;; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
;; FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
;; more details.
;;
;; You should have received a copy of the GNU General Public License along
;; with this program. If not, see http://www.gnu.org/licenses/.

(ns ooapi-gateway-configurator.logging
  "Ring middleware for catching and logging exceptions."
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [ooapi-gateway-configurator.bare-layout :as bare-layout])
  (:import java.util.UUID
           org.slf4j.MDC))

(def redact-key-re
  "Regex matching keys for entries that should be redacted from logs."
  #"(?i:)pass|secret|client.id|proxy.options|auth") ;; proxy options can contain authentication info

(def redact-placeholder
  "String that will replace a redacted entry.

  Also see [[redact-key-re]] and [[with-mdc]]."
  "XXX-REDACTED")

(defn ->mdc-key
  "Convert x to key to use in the Mapped Diagnostic Context.

  MDC keys must be strings. For qualified keywords includes the
  namespace in the MDC key; \"my.foo/bar\", otherwise
  uses [[clojure.core/str]] to coerce to string."
  [x]
  (if (keyword? x)
    (subs (str x) 1) ;; convert with ns if fully qualified key, but
    ;; leave out colon: :foo/bar => "foo/bar"

    (str x)))

(defn ->mdc-val
  "Convert x to value to use in the Mapped Diagnostic Context.

  MDC values must be strings -- the MDC is a flat string/string
  map. Assumes x is a keyword, a sequential collection of 'simple'
  values (converted to a comma-separated string) or has a reasonable
  [[clojure.core/str]] coercion."
  [x]
  (cond
    (keyword? x)
    (subs (str x) 1)

    (sequential? x)
    (string/join ", " (map #(str "'" (->mdc-val %) "'") x))

    :else
    (str x)))

(defn ->mdc-entry
  "Convert a `[key value]` pair to a Mapped Diagnostic Context entry.

  Uses [[->mdc-key]] and [[->mdc-val]] to convert. In entries with
  key matching [[redact-key-re]], the value will be replaced with
  [[redact-placeholder]]"
  [[k v]]
  (let [k (->mdc-key k)]
    [k (if (re-find redact-key-re k)
         redact-placeholder
         (->mdc-val v))]))

(defmacro with-mdc
  "Adds the map `m` to the Mapped Diagnostic Context (MDC) and executes `body`.

  Enties in `m` are processed using [[->mdc-entry]].

  Removes the added keys from the MDC after `body` is executed.

  See also [[redact-key-re]] and https://logback.qos.ch/manual/mdc.html"
  [m & body]
  `(let [m# ~m]
     (try
       (doseq [[k# v#] (map ->mdc-entry m#)]
         (MDC/put k# v#))
       ~@body
       (finally
         (doseq [k# (keys m#)]
           (MDC/remove (->mdc-key k#)))))))

(defn wrap-request-logging
  [f]
  (fn [{:keys                        [request-method uri]
        {:keys [trace-id parent-id]} :traceparent
        :as                          request}]
    (let [method (string/upper-case (name request-method))]
      (with-mdc {:request_method method
                 :url            uri
                 :trace-id       trace-id
                 :parent-id      parent-id}
        (when-let [{:keys [status :oauth2/user-info] :as response} (f request)]
          (with-mdc {:http_status status
                     :user-id     (get-in user-info [:conext :sub])}
            (log/info status method uri)
            response))))))

(defn log-exception
  [e id]
  ;; Request info is provided in MDC, see wrap-request-logging
  (with-mdc {:error_id id}
    (log/error e (str "Error " id))))

(defn wrap-exception-logging
  [f]
  (fn [request]
    (try
      (f request)
      (catch Throwable e
        (let [id (str (UUID/randomUUID))]
          (log-exception e id)
          (bare-layout/exception id request))))))

(defn wrap-logging
  [f]
  (-> f
      wrap-exception-logging
      wrap-request-logging))
