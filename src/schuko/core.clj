(ns schuko.core
  (:require [hiccup.core :as hiccup]
            [endophile.core :as ecore]
            [endophile.hiccup :as ehic]
            )
  (:gen-class))

(defn butlast-v [vec]
  "A vector containing all elements of VEC except the rightmost"
  (subvec vec 0 (dec (count vec))))

(defn group-h1
  ([els] (group-h1 els []))
  ([els result]
     (let [new-result
           (if (= (first (first els)) :h1)
             (conj result [(first els)])
             (conj (butlast-v result) (conj (last result) (first els))))]
       (if (first (rest els))
         (group-h1 (rest els) new-result)
         new-result))))

(defn parse-hiccup-file [fn]
  (let [h (ehic/clj-contents (ecore/mp (slurp fn)))]
    (map #(apply vector :div {:class :slide} %)
         (group-h1 h))))

;; for ease of testing without reloading absolutely frigging
;; everything every time, you can call this with second argument True
;; to make it generate references to external js and css.  This is of
;; course a bad idea for actual presentations because the paths to those
;; external files are only correct in very limited circumstances

(defn top-and-tail
  ([h inline]
     [:html
      [:head
       [:title "Schuko"]
       (if inline
         [:style (slurp "example/default.css")]
         [:link {:rel :stylesheet
                 :type "text/css"
                 :href "example/default.css"}])
       (if inline
         [:script
          {:type "text/javascript" }
          (slurp "resources/public/js/main.js")]
         [:script
          {:type "text/javascript"
           :src "resources/public/js/main.js"}])]
      [:body
       [:div {:id :content}
        [:div {:class :front}]
        [:div {:class :back}]]]
      (apply vector :script {:id :everything :type "text/html"}
             h)])
  ([h] (top-and-tail h true)))

;; for test purposes
(defn energize []
  (spit "out.html"
        (hiccup/html
         (top-and-tail (parse-hiccup-file "example/sample.md") false))))

; (energize "example/sample.md" false)

;; would be a win if we could override the css filename too
(defn -main [& args]
  (let [[infile outfile] args]
    (pr (format "processing %s " infile))
    (spit (second args)
          (hiccup/html (top-and-tail (parse-hiccup-file (first args)))))
    (prn (format "-> %s" outfile))))
