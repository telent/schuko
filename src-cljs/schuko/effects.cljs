(ns schuko.effects
  (:require [schuko.dom :as d]))

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
