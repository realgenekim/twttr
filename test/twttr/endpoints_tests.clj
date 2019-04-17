(ns twttr.endpoints-tests
  (:require [clojure.test :refer [deftest is]]
            [twttr.api :as api]))

(def api-reference-index
  "This listing is drawn from https://developer.twitter.com/en/docs/api-reference-index"
  {:get    ["/account/settings"
            "/account/verify_credentials"
            "/application/rate_limit_status"
            "/blocks/ids"
            "/blocks/list"
            "/collections/entries"
            "/collections/list"
            "/collections/show"
            "/direct_messages/events/list"
            "/direct_messages/events/show"
            "/direct_messages/welcome_messages/list"
            "/direct_messages/welcome_messages/rules/list"
            "/direct_messages/welcome_messages/rules/show"
            "/direct_messages/welcome_messages/show"
            "/favorites/list"
            "/followers/ids"
            "/followers/list"
            "/friends/ids"
            "/friends/list"
            "/friendships/incoming"
            "/friendships/lookup"
            "/friendships/no_retweets/ids"
            "/friendships/outgoing"
            "/friendships/show"
            "/geo/id/:place_id"
            "/geo/reverse_geocode"
            "/geo/search"
            "/help/configuration"
            "/help/languages"
            "/help/privacy"
            "/help/tos"
            "/lists/list"
            "/lists/members"
            "/lists/members/show"
            "/lists/memberships"
            "/lists/ownerships"
            "/lists/show"
            "/lists/statuses"
            "/lists/subscribers"
            "/lists/subscribers/show"
            "/lists/subscriptions"
            "/media/upload"
            "/mutes/users/ids"
            "/mutes/users/list"
            "/saved_searches/list"
            "/saved_searches/show/:id"
            "/search/tweets"
            "/statuses/home_timeline"
            "/statuses/lookup"
            "/statuses/mentions_timeline"
            "/statuses/oembed"
            "/statuses/retweeters/ids"
            "/statuses/retweets/:id"
            "/statuses/retweets_of_me"
            "/statuses/show/:id"
            "/statuses/user_timeline"
            "/trends/available"
            "/trends/closest"
            "/trends/place"
            "/users/lookup"
            "/users/profile_banner"
            "/users/search"
            "/users/show"
            "/users/suggestions"
            "/users/suggestions/:slug"
            "/users/suggestions/:slug/members"]
   :post   ["/account/remove_profile_banner"
            "/account/settings"
            "/account/update_profile"
            "/account/update_profile_background_image"
            "/account/update_profile_banner"
            "/account/update_profile_image"
            "/blocks/create"
            "/blocks/destroy"
            "/collections/create"
            "/collections/destroy"
            "/collections/entries/add"
            "/collections/entries/curate"
            "/collections/entries/move"
            "/collections/entries/remove"
            "/collections/update"
            "/direct_messages/events/new"
            "/direct_messages/welcome_messages/new"
            "/direct_messages/welcome_messages/rules/new"
            "/direct_messages/mark_read"
            "/direct_messages/indicate_typing"
            "/favorites/create"
            "/favorites/destroy"
            "/friendships/create"
            "/friendships/destroy"
            "/friendships/update"
            "/lists/create"
            "/lists/destroy"
            "/lists/members/create"
            "/lists/members/create_all"
            "/lists/members/destroy"
            "/lists/members/destroy_all"
            "/lists/subscribers/create"
            "/lists/subscribers/destroy"
            "/lists/update"
            "/media/metadata/create"
            "/media/upload"
            "/mutes/users/create"
            "/mutes/users/destroy"
            "/saved_searches/create"
            "/saved_searches/destroy/:id"
            "/statuses/destroy/:id"
            "/statuses/retweet/:id"
            "/statuses/unretweet/:id"
            "/statuses/update"
            "/users/report_spam"]
   :delete ["/direct_messages/events/destroy"
            "/direct_messages/welcome_messages/destroy"
            "/direct_messages/welcome_messages/rules/destroy"]})

(def api-extra
  {:get    ["/account_activity/webhooks"
            "/account_activity/webhooks/:webhook_id/subscriptions"
            "/account_activity/webhooks/:webhook_id/subscriptions/list"
            "/statuses/sample"]
   :post   ["/account_activity/webhooks/:webhook_id/subscriptions"
            "/account_activity/webhooks"
            "/statuses/filter"]
   :put    ["/account_activity/webhooks/:webhook_id"]
   :delete ["/account_activity/webhooks/:webhook_id/subscriptions"
            "/account_activity/webhooks/:webhook_id"]})

(def api-oauth
  {:get  ["/oauth/authenticate"
          "/oauth/authorize"]
   :post ["/oauth/access_token"
          "/oauth/request_token"
          "/oauth2/token"
          "/oauth2/invalidate_token"]})

(deftest test-endpoints-comprehensiveness
  (is (= (reduce into #{} (mapcat vals [api-reference-index api-extra api-oauth]))
         (set (map :path api/endpoints)))))
