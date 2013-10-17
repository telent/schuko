; -*- clojure -*- mode
(ns schuko.core
  (:require [clojure.string :as string]
            [schuko.dom :as d]
            [schuko.effects :as effects]
            ))

;;; the *real* initial value for all-slides has to be done when
;;; document has loaded, because we get it by grovelling the DOM
(def all-slides (atom []))
(defn get-all-slides []
  (let [el (.createElement js/document "div")]
    (set! (.-innerHTML el) (.-innerHTML (d/by-id :everything)))
    (seq (.querySelectorAll el "div.slide"))))

(defn swap-to [front back effect]
  (d/remove-class back "current")
  (d/add-class front "current")
  (effects/swap-effect front back effect))

(defn show-nth-slide [n]
  (let [old-slide (first (d/by-selector "div#content div.current"))
        older-slides (d/by-selector "div#content div:not(.current)")
        new-slide (.createElement js/document "div")
        parent (.-parentNode old-slide)]
    (doall (map d/remove-node older-slides))
    (set! (.-innerHTML new-slide) (.-innerHTML (nth @all-slides n)))
    (.appendChild parent new-slide)
    (swap-to new-slide old-slide :flipv)))

(def current-slide (atom 0))
(add-watch
 current-slide :key (fn [key ref old-state state] (show-nth-slide state)))

(defn key-handler [e]
  (let [code (.-keyCode e)]
    (case code
      32 (swap! current-slide inc)
      8 (swap! current-slide dec)
      (.log js/console (pr-str ["unrecognised key" code @current-slide])))))

(defn ^:export run-on-ready []
  (let [slides (get-all-slides)]
    (swap! all-slides concat slides)
    (swap! current-slide identity 1)
    )
  (.addEventListener js/document "keyup" key-handler))

(set! (.-onload js/window) run-on-ready)
