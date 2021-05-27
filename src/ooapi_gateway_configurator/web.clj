(ns ooapi-gateway-configurator.web
  (:require [compojure.core :refer [GET routes wrap-routes]]
            [compojure.route :refer [not-found resources]]
            [ooapi-gateway-configurator.applications :as applications]
            [ooapi-gateway-configurator.auth :as auth]
            [ooapi-gateway-configurator.auth-pages :as auth-pages]
            [ooapi-gateway-configurator.html :refer [layout]]
            [ooapi-gateway-configurator.institutions :as institutions]
            [ooapi-gateway-configurator.state :as state]
            [ooapi-gateway-configurator.store :as store]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn main-page
  []
  [:ul
   [:li [:a {:href (applications/path)}
         "Applications"]]
   [:li [:a {:href (institutions/path)}
         "Institutions"]]])

(defn mk-handler
  [config]
  (routes
   (wrap-routes
    (routes (GET "/" req (layout (main-page) req))
            applications/handler
            institutions/handler)
    auth/wrap-member-of (get-in config [:auth :group-ids]))
   auth/logout-handler
   (resources "/" {:root "public"})
   (not-found "nothing here..")))

(defn mk-app
  [config]
  (-> config
      (mk-handler)

      (state/wrap)
      (store/wrap (:store config))

      (auth-pages/wrap-auth-pages)
      (auth/wrap-authentication (:auth config))

      (wrap-defaults (-> config
                         (get :site-defaults site-defaults)
                         (assoc-in [:session :cookie-attrs :same-site] :lax)))))
