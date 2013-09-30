; -*- clojure -*- mode
(ns schuko.events
  (:require [clojure.string :as string]
            [schuko.dom :as d]
            ))

;;; the *real* initial value for all-slides has to be done when
;;; document has loaded, because we get it by grovelling the DOM
(def all-slides (atom []))
(defn get-all-slides []
  (let [el (.createElement js/document "div")]
    (set! (.-innerHTML el) (.-innerHTML (d/by-id :everything)))
    (seq (.querySelectorAll el "div.slide"))))

(defmulti swap-effect (fn [front back effect] effect))

(defmethod swap-effect :fade [front back effect & [duration]]
  (let [duration (or duration 1000)
        delay (int (* duration 0.3))]
    (set! (-> front .-style .-transition)
          (format "opacity %dms linear %dms" duration delay))
    (set! (-> back .-style .-transition)
          (format "opacity %dms linear" duration))
    (d/reflow)
    (set! (-> back .-style .-opacity) "0")
    (set! (-> front .-style .-opacity) "1")))

(defmethod swap-effect :fliph [now prev effect]
  (set! (-> now .-style .-transform) "rotateY(-180deg)")
  (set! (-> prev .-style .-transform) "rotateY(0deg)")
  (d/reflow)
  (d/add-class prev "flip")
  (d/add-class now "flip")
  (set! (-> now .-style .-transform) "rotateY(0deg)")
  (set! (-> prev .-style .-transform) "rotateY(180deg)")
  (set! (-> now .-style .-opacity) "1"))

(defmethod swap-effect :flipv [now prev effect]
  (set! (-> now .-style .-transform) "rotateX(-180deg)")
  (set! (-> prev .-style .-transform) "rotateX(0deg)")
  (d/reflow)
  (d/add-class prev "flip")
  (d/add-class now "flip")
  (set! (-> now .-style .-transform) "rotateX(0deg)")
  (set! (-> prev .-style .-transform) "rotateX(180deg)")
  (set! (-> now .-style .-opacity) "1"))

(defmethod swap-effect :spin [now prev effect]
  ;; this effect was discovered accidentally
  (set! (-> now .-style .-transform) "rotateX(-180deg) rotateY(-180deg")
  (set! (-> prev .-style .-transform) "rotateX(0deg) rotateY(0deg)")
  (d/reflow)
  (d/add-class prev "flip")
  (d/add-class now "flip")
  (set! (-> now .-style .-transform) "rotateY(0deg) rotateX(0deg)")
  (set! (-> prev .-style .-transform) "rotateY(180deg) rotateX(180deg)")
  (set! (-> now .-style .-opacity) "1"))

(defmethod swap-effect :wipe [front back effect]
  ;; put a solid div in the top left corner in front of picture,
  ;; grow it to cover the whole div, swap front and back, shrink
  (let [curtain (.createElement js/document "div")
        parent (.-parentNode front)]
    (.appendChild parent curtain)
    (set! (-> curtain .-style .-width) "0px")
    (set! (-> curtain .-style .-height) "0px")
    (set! (-> curtain .-style .-zIndex) "99")
    (set! (-> back .-style .-zIndex) "1")
    (set! (-> front .-style .-zIndex) "0")
    (set! (-> front .-style .-opacity) "1")
    (d/add-class curtain "wipe")
    (.addEventListener
     curtain "transitionend"
     (fn [e]
       ;; this gets called four times: when the height reaches max,
       ;; when the width reaches max, when the height reaches 0,
       ;; when the width reaches 0
       (when (and (= (.-propertyName e) "height")
                  (> (.-clientHeight curtain) 0))
         (set! (-> front .-style .-zIndex) "2")
         (d/reflow)
         (set! (-> curtain .-style .-height) "0px"))
       (when (and (= (.-propertyName e) "width")
                  (> (.-clientWidth curtain) 0))
         (set! (-> curtain .-style .-width) "0px")
         (d/reflow))))

    (d/reflow)
    (set! (-> curtain .-style .-width)
          (max (.-clientWidth front) (.-clientWidth back)))
    (set! (-> curtain .-style .-height)
          (max (.-clientHeight front) (.-clientHeight back)))
    ))

(defn swap-to [front back effect]
  (d/remove-class back "current")
  (d/add-class front "current")
  (swap-effect front back effect))

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
