; -*- clojure -*- mode
(ns schuko.events)
  
(defn by-id [id]
  (.getElementById js/document (name id)))

(defn by-tag [tag]
  (seq (.getElementsByTagName js/document (name tag))))

(defn by-selector [selector]
  (seq (.querySelectorAll js/document selector)))

;;; may not work on opera? http://dev.clojure.org/jira/browse/CLJS-120
(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

;;; the *real* initial value for all-slides has to be done when 
;;; document has loaded, because we get it by grovelling the DOM
(def all-slides (atom []))
(defn get-all-slides []
  (let [el (.createElement js/document "div")]
    (set! (.-innerHTML el) (.-innerHTML (by-id :everything)))
    (seq (.querySelectorAll el "div.slide"))))

(def current-slide (atom 0))
(add-watch
 current-slide :key 
 (fn [key ref old-state state]
   (let [portal (by-id :content)]
     (.log js/console (pr-str "showing slide " state))
     (set! (.-innerHTML portal) (.-innerHTML (nth @all-slides state)))
     )))
 
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
