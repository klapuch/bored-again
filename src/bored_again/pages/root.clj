(ns bored-again.pages.root
  (:require [bored-again.pages.layout :as layout]))

(defn content
  []
  (str
    (layout/template
      (let [img-attrs {:width 42 :height 42}
            link-attrs {:target "_blank" :rel "nofollow noopener"}]
        [:div.flex.justify-center.mt-10
         [:a.mr-4 (merge {:href "https://github.com/klapuch"} link-attrs)
          [:img (merge {:src "/images/web/github.svg" :title "GitHub"} img-attrs)]
          ]
         [:a.mr-4 (merge {:href "https://twitter.com/klapuchdominik"} link-attrs)
          [:img (merge {:src "/images/web/twitter.svg" :title "Twitter"} img-attrs)]
          ]
         [:a.mr-4 (merge {:href "https://linkedin.com/klapuchdominik"} link-attrs)
          [:img (merge {:src "/images/web/linkedin.svg" :title "LinkeIn"} img-attrs)]
          ]
         [:a.mr-4 (merge {:href "https://last.fm/user/RecklessFace"} link-attrs)
          [:img (merge {:src "/images/web/last.fm.svg" :title "Last.fm"} img-attrs)]
          ]
         [:a.mr-4 (merge {:href "https://t.me/klareek"} link-attrs)
          [:img (merge {:src "/images/web/telegram.svg" :title "Telegram"} img-attrs)]
          ]
         [:a.mr-4 (merge {:href "mailto:klapuchdominik@gmail.com"} link-attrs)
          [:img (merge {:src "/images/web/email.svg" :title "Email"} img-attrs)]
          ]
         ])
      "Contacts"
      "Contacts on GitHub, Twitter, LinkedIn, Email, Telegram, etc..")))
