(ns schuko.dom
  (:require [clojure.string :as string]))

(defn by-id [id]
  (.getElementById js/document (name id)))

;;; may not work on opera? http://dev.clojure.org/jira/browse/CLJS-120
(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

(defn by-selector [selector]
  (seq (.querySelectorAll js/document selector)))

(defn add-class [el class]
  (let [c (.-className el)
        classes (set (string/split c #" +"))]
    (if (contains? classes class)
      c
      (set! (.-className el) (string/join " " (conj classes class))))))

(defn remove-class [el class]
  (let [c (.-className el)
        classes (set (string/split c #" +"))]
    (if (contains? classes class)
      (set! (.-className el) (string/join " " (disj classes class)))
      c)))

(defn get-style [el name]
  (let [styles (.getComputedStyle js/window el)]
    (aget styles name)))

(defn reflow []
  (aget (.getComputedStyle js/window (first (by-selector "body")))
        "height"))

(defn remove-node [node]
  (let [parent (.-parentNode node)]
    (and parent (.removeChild parent node))))

