(ns applicant-test.core-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [cljs.test]
            [applicant-test.core :as c]))

;; NOTE: proper testing would need an external library like https://github.com/bensu/cljs-react-test
;; for lack of time I am testing with what I have

(deftest add-image-url-input-test
  (let [state (atom 0)
        dom (apply hash-map (flatten (c/add-image-url-input (fn [target] "test") (fn [event target] (swap! state inc)))))]        
    ((:on-change (:input dom)) "someEvent")
    (is (= @state 1))))

(deftest add-image-description-input-test
  (let [state (atom 0)
        dom (apply hash-map (flatten (c/add-image-description-input (fn [target] "test") (fn [event target] (swap! state inc)))))]        
    ((:on-change (:textarea dom)) "someEvent")
    (is (= @state 1))))

(deftest add-submit-button-test
  (let [state (atom 0)
        dom (apply hash-map (c/add-submit-button (fn [] (swap! state inc))))]
    ((:on-click (:input dom)) "someEvent")
    (is (= @state 1))))

(deftest add-image-list-test
  (let [transact-fn (fn [_ _] nil)
        get-fn (fn [_] [{:url "someUrl", :description "someDescription"} {:url "someOtherUrl", :description "someOtherDescription"}])
        ; the following binding would need refactoring
        dom-strings (filter string?
                            (flatten
                              (map #(if (map? %) (vals %) %)
                                   (flatten (c/add-image-list get-fn transact-fn)))))
        ]
    (is (some #(= "someUrl" %) dom-strings))
    (is (some #(= "someDescription" %) dom-strings))
    (is (some #(= "someOtherUrl" %) dom-strings))
    (is (some #(= "someOtherDescription" %) dom-strings))))

(deftest image-already-in-list?-test
  (let [get-fn (fn [_] [{:url "someUrl"}])]
    (is (c/image-already-in-list? "someUrl" get-fn))
    (is (not (c/image-already-in-list? "notExistingUrl" get-fn)))))

(deftest image-already-in-list?-test
  (is (c/is-invalid-url? ""))
  (is (c/is-invalid-url? "someUrl"))
  (is (not (c/is-invalid-url? "https://duckduckgo.com/"))))

(deftest delete-image-test
  (let [url {:url "someUrl"}
        other-url {:url "someOtherUrl"}]
  (is (= (c/delete-image url []) []))
  (is (= (c/delete-image url [url url other-url]) [other-url]))))

(deftest add-delete-button-test
  (let [state (atom 0)
        dom (apply hash-map (c/add-delete-button "someUrl" (fn [_ _] (swap! state inc))))]
    ((:on-click (:input dom)) "someEvent")
    (is (= @state 1))))
