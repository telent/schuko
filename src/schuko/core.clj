(ns schuko.core
  (:require [hiccup.core :as hiccup]
            [endophile.core :as ecore]
            [endophile.hiccup :as ehic]
            ))

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
       [:script "console.log('hello literal')"]
       [:div {:id :content}]]
      (apply vector :script {:id :everything :type "text/html"}
             h)])
  ([h] (top-and-tail h false)))

(defn energize [fn]
  (spit "out.html"
        (hiccup/html (top-and-tail (parse-hiccup-file fn)))))

(energize "example/sample.md")
