(ns twitter.core
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [http.async.client :as http]
            [http.async.client.request :refer [execute-request
                                               prepare-request]]
            [twitter.oauth :refer [auth-header]]))

(defn- map-kv
  "transforms the k/v pairs of a map using a supplied transformation function"
  [m key-fn val-fn]
  (into {} (for [[k v] m] [(key-fn k) (val-fn v)])))

(defn- hyphen->underscore
  "Replaces each - with a _"
  [s]
  (string/replace s \- \_))

(defn- flatten-to-csv
  "Turns collections into comma-separated strings; preserves other values"
  [val]
  (if (coll? val) (string/join "," val) val))

(defn- subs-uri
  "substitutes parameters for tokens in the uri"
  [uri params]
  (string/replace uri #"\{\:(\w+)\}"
                  (fn [[_ kw]]
                    (let [value (get params (keyword kw))]
                      (assert value (format "%s needs :%s param to be supplied" uri kw))
                      (str value)))))

(defn format-twitter-error-message
  "read an error response into a string error message"
  [response]
  (let [status-code (:code (http/status response))
        body (json/read-str (http/string response) :key-fn keyword)
        desc (or (:message (first (:errors body))) (:error body))
        code (or (:code (first (:errors body))) status-code)
        req (:request body)]
    (cond
      (= 429 status-code) (format "Twitter responded with error 88: Rate limit exceeded. Next reset at %s (UTC epoch seconds)" (-> response http/headers :x-rate-limit-reset))
      (and req code desc) (format "Twitter responded '%s' with error %d: %s" req code desc)
      (and code desc) (format "Twitter responded with error %d: %s" code desc)
      desc (format "Twitter responded with error: %s" desc)
      :default "Twitter responded with an unknown error")))

(def ^:private default-client (delay (http/create-client :follow-redirects false :request-timeout -1)))

(defn- await-response
  "this takes a response and returns a map of the headers, status, and body (as a string)"
  [response]
  (http/await response)
  {:status (http/status response)
   :headers (http/headers response)
   :body (http/string response)})

(defn- transform-sync-response
  [response]
  (if (< (:code (http/status response)) 400)
    (update (await-response response) :body #(json/read-str % :key-fn keyword))
    (throw (Exception. (format-twitter-error-message response)))))

(defn execute-api-request
  "Creates an HTTP request, signing with OAuth as directed by the :oauth-creds option.
  Note that the params are transformed (from lispy -'s to x-header-style _'s) and added to the query.
  So :params could be {:screen-name 'twitterapi'} and it be merged into :query as {:screen_name 'twitterapi'}.
  The uri has the params substituted in, so '/{:id}' in the uri will result in, e.g., '/123'"
  [http-method uri {:keys [params body query oauth-creds headers client callbacks sync]
                    :or {client @default-client}
                    :as arg-map}]
  (let [params (map-kv params (comp keyword hyphen->underscore name) flatten-to-csv)
        uri (subs-uri uri params)
        query (merge query params)
        headers (merge headers
                       {:Authorization (auth-header oauth-creds http-method uri query)}
                       (when (vector? body) {:Content-Type "multipart/form-data"}))
        request (prepare-request http-method uri :query query :headers headers :body body)
        ; other-args (merge (dissoc arg-map :query :headers :body :params :oauth-creds :client :api :callbacks)
        response (apply execute-request client request (apply concat callbacks))]
    (if sync (transform-sync-response response) response)))

(defmacro def-twitter-method
  "Declares a twitter method with the supplied name, HTTP method and relative resource path.
  As part of the specification, it must have an :api member of the 'rest' list.
  From these it creates a uri, the api context and relative resource path."
  [fn-name default-http-method resource-path & rest]
  (let [rest-map (apply sorted-map rest)]
    `(defn ~fn-name
       [& {:as args#}]
       (let [arg-map# (merge ~rest-map args#)
             api-prefix# (or (:api arg-map#) (throw (Exception. "must include an :api entry in the params")))
             http-method# (or (:http-method args#) ~default-http-method)
             ; makes a uri from a supplied protocol, site, version and resource-path
             uri# (str api-prefix# "/" ~resource-path)]
         (execute-api-request http-method# uri# arg-map#)))))
