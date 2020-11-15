(ns bored-again.handlers)


(def ^{:private true, :const true} SECURITY_HEADERS {"X-Frame-Options" "DENY"
                                                     "X-Content-Type-Options" "nosniff"
                                                     "X-XSS-Protection" "1; mode=block"
                                                     "Referrer-Policy" "no-referrer"})


(defn- merge-headers
  [name-values]
    (->> name-values
         (map (fn [[name value]] {:headers {name value}}))
         (apply merge-with into)))


(defn- merge-security-headers
  []
  (partial (merge-headers SECURITY_HEADERS)))


(defn wrap-security-headers
  [handler]
  (fn [request]
    (let [response (handler request)]
      (merge-with into response (merge-security-headers)))))
