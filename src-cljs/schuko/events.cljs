; -*- clojure -*- mode
(ns schuko.events
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

(defn remove-node [node]
  (let [parent (.-parentNode node)]
    (and parent (.removeChild parent node))))

;;; the *real* initial value for all-slides has to be done when
;;; document has loaded, because we get it by grovelling the DOM
(def all-slides (atom []))
(defn get-all-slides []
  (let [el (.createElement js/document "div")]
    (set! (.-innerHTML el) (.-innerHTML (by-id :everything)))
    (seq (.querySelectorAll el "div.slide"))))

(defmulti swap-effect (fn [front back effect] effect))

(defmethod swap-effect :fade [front back effect & [duration]]
  (let [duration (or duration 1000)
        delay (int (* duration 0.3))]
    (set! (-> front .-style .-transition)
          (format "opacity %dms linear %dms" duration delay))
    (set! (-> back .-style .-transition)
          (format "opacity %dms linear" duration))
    (set! (-> back .-style .-opacity) "0")
    (set! (-> front .-style .-opacity) "1")))

(defmethod swap-effect :wipe [front back effect]
  ;; put a solid div in the top left corner in front of picture,
  ;; grow it to cover the whole div, swap front and back, shrink
  (let [curtain (.createElement js/document "div")
        parent (.-parentNode front)]
    (set! (.-className curtain) "wipe")
    (.appendChild parent curtain)
    (set! (-> curtain .-style .-width) "0px")
    (set! (-> curtain .-style .-height) "0px")
    (set! (-> curtain .-style .-zIndex) "99")
    (set! (-> curtain .-style .-width)
          (max (.-clientWidth front) (.-clientWidth back)))
    (set! (-> curtain .-style .-height)
          (max (.-clientHeight front) (.-clientHeight back)))
    (.addEventListener
     curtain "transitionend"
     (fn [e]
       ;; this gets called four times: when the height reaches max,
       ;; when the width reaches max, when the height reaches 0,
       ;; when the width reaches 0
       (.log js/console  (pr-str [(.-propertyName e)
                                  (.-clientWidth curtain)
                                  (.-clientHeight curtain)]))

       (cond
        (and (= (.-propertyName e) "height") (> (.-clientHeight curtain) 0))
        (do
          (set! (-> front .-style .-opacity) "1")
          (set! (-> back .-style .-opacity) "0")
          (set! (-> curtain .-style .-height) "0px"))

        (and (= (.-propertyName e) "width") (> (.-clientWidth curtain) 0))
        (set! (-> curtain .-style .-width) "0px")

        (and (= (.-clientHeight curtain) 0) (= (.-clientWidth curtain) 0))
        (remove-node curtain)
        )))))

(defn swap-to [front back effect]
  (remove-class back "current")
  (add-class back "old")
  (add-class front "current")
  (swap-effect front back effect))

(defn show-nth-slide [n]
  (let [old-slide (first (by-selector "div#content div.current"))
        older-slides (by-selector "div#content div.old")
        new-slide (.createElement js/document "div")
        parent (.-parentNode old-slide)]
    (doall (map remove-node older-slides))
    (set! (.-innerHTML new-slide) (.-innerHTML (nth @all-slides n)))
    (.appendChild parent new-slide)
    ;; XXX removing this call to get-style, when the effect is fade,
    ;; in FF22.0, causes the new slide to be displayed instantly instead of
    ;; having the transition applied.  I do not understand why.
    (get-style old-slide "height")
    (swap-to new-slide old-slide :fade)))

(def current-slide (atom 0))
(add-watch
 current-slide :key (fn [key ref old-state state] (show-nth-slide state)))

(defn key-handler [e]
  (let [code (.-keyCode e)]
    (case code
      32 (swap! current-slide inc)
      8 (swap! current-slide dec)
      (.log js/console (pr-str ["unrecognised key" code @current-slide])))))

(defn ^:export ready []
  (let [slides (get-all-slides)]
    (swap! all-slides concat slides)
    (swap! current-slide identity 1)
    )
  (.addEventListener js/document "keyup" key-handler))

(set! (.-onload js/window) ready)
