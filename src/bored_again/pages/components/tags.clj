(ns bored-again.pages.components.tags)

(defn- h-tag
  ([href-fn tag]
    [:a.text-xs.inline-block.bg-gray-200.rounded-full.px-2.py-1.font-semibold.text-black.mr-2.hover:text-gray-700.tag {:href  (href-fn (:id tag) (:slug tag))
                                                                                                                       :title (:name tag)}
    (:name tag)])
  ([tag] (h-tag (fn [id slug] (format "/tags/%s-%s" id slug)) tag)))


(defn h-tags
  ([tags href-fn] (map (partial h-tag href-fn) tags))
  ([tags] (map h-tag tags)))
