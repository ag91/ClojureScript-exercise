(ns applicant-test.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(defonce app-state (atom {:entry {:url nil
                                  :description nil}
                          :images []}))

;; NOTE: I left all the definitions in this file to make evaluation
;; easier (no navigation from file to file)
;; Otherwise I would have moved some of the functions in a utils.cljs
;; file and the soblono functions in a soblono-utils.cljs

(defn update! [cursor event target]
  (om/update! cursor target (.-value (.-target event))))

; I could refactor more, but for now it seems enough
(defn add-insertion-dom [type label value update-fn]
  [[:label label]
  [type {:type "text"
           :class "form-control"
           :value (str value)
           :on-change (fn [event]
                        (update-fn event))}]])

(defn add-image-url-input [get-fn update-fn]
  (let [target [:entry :url]]
    (add-insertion-dom :input "Image URL" (get-fn target)  #(update-fn % target))))

(defn add-image-description-input [get-fn update-fn]
  (let [target [:entry :description]]
    (add-insertion-dom :textarea "Image Description" (get-fn target) #(update-fn % target))))

(defn image-already-in-list? [url get-fn]
  (some #(= (:url %) url) (get-fn [:images])))

(defn is-invalid-url? [url]
  (try (js/URL. url) false (catch js/Error e true)))

(defn cond-fn [get-fn transact-fn]
  (let [url (get-fn [:entry :url])
        entry (get-fn [:entry])]
    (cond
      (image-already-in-list? url get-fn) (js/alert "This image is already in your list.") ; with more time I would have used the following https://stackoverflow.com/questions/14361517/mark-error-in-form-using-bootstrap
      (is-invalid-url? url) (js/alert "The image URL is invalid.")
      true (transact-fn [:images] #(conj % entry)))))


; NOTE: I could have refactored the submit and delete button similarly
; to the input forms.
(defn add-submit-button [cond-fn]
  [:input {:type "submit"
           :value "Add Image"
           :class "btn btn-primary"
           :on-click (fn [_] (cond-fn))}])

(defn delete-image [img images]
  (vec (filter (fn [e] (not (= (:url img) (:url e)))) images)))

(defn add-delete-button [img transact-fn]
  [:input {:type "submit" ; show a button for each image using Om cursors
            :value "Delete Image"
            :class "btn btn-primary"
            :on-click (fn [_]
                        (transact-fn [:images] #(delete-image img %)))}])

(defn add-image [img transact-fn]
  [:div {:class "card"}
   [:img {:class "card-img-top" :src (:url img)}]
   [:div {:class "card-body"}
    [:p {:class "card-text"} (:description img)]]
   (add-delete-button img transact-fn)])

(defn add-image-list [get-fn transact-fn]
  [:div {:id "image-list" :class "card mt-2"}
   [:div {:class "card-body"}
    [:h4 "The list of images"]
    ;; TODO: do something cool with (:images cursor) here to show them all
    [:div {:class "card-deck"} ; show image and comment after submission in image list
     (for [img (get-fn [:images])]
       (add-image img transact-fn))]]])

;; NOTE: I could have taken out the soblono part in a function as I
;; have done for the tasks, but I left the original parts of the
;; assignment to highlight my work.
(defn root-component [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        [:h1 "My Favourite Images"]
        [:p {:class "lead"} "This is where I keep track of all my favourite images. I sure hope I never accidentally refresh this page!"]
        [:div {:id "image-entry" :class "card"}
         [:div {:class "card-body"}
          [:h4 "New image"]
          [:div {:class "form-group"}
           ; I made add-image-url-input a higher order function to
           ; make it testable (here I assume the om library to be well
           ; tested)
           (add-image-url-input (partial get-in cursor) (partial update! cursor))
           ;; DONE: textarea for image description
           (add-image-description-input (partial get-in cursor) (partial update! cursor))
           ;; DONE: button that :on-click takes the current :entry and adds it to the list of images
           (add-submit-button (fn [] (cond-fn (partial get-in cursor) (partial om/transact! cursor))))]]]
        (add-image-list (partial get-in cursor) (partial om/transact! cursor))
        ]))))

(defn render []
  (om/root
   root-component
   app-state
   {:target (js/document.getElementById "app")}))
