(ns schuko.core
  (:require [hiccup.core :as hiccup]
            [endophile.core :as ecore]
            [endophile.hiccup :as ehic]
            ))

(defn parse-hiccup-file [fn]
  (ehic/clj-contents (ecore/mp (slurp fn))))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn top-and-tail [h]
  [:html
   [:head
    [:title "Schuko"]]
   [:body
    [:div {:id :content}]]
   [:script {:id :everything :type "text/html"}
    h]])
    

(defn energize [fn]
  (hiccup/html (top-and-tail (parse-hiccup-file fn))))

; (parse-hiccup-file "example/sample.md")
