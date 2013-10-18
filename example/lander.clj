(ns lander.core)

(def mass 10149)
(def gravity -1.6)
(def thrust 44400)

(defn advance [delta-t state]
  (let [accel (if (:thruster? state)
                (/ thrust mass) 
                gravity)
        new-v (+ (:v state) (* accel delta-t))
        distance (+ (* (:v state) delta-t) 
                    (* 0.5 accel delta-t delta-t))
        new-height (+ (:height state) distance)]
    {:thruster? (:thruster? state) 
     :v new-v
     :time (+ 1 (:time state))
     :height new-height}))











(defn simulate [thrust-decider state]
  (prn state)
  (if (< (:height state) 0) 
    state
    (simulate thrust-decider
              (advance 1 (thrust-decider state)))))



(defn rock [state]
  state)

(defn slow-lander [state]
  (if (< (:v state) -2) 
    (assoc state :thruster? true)
    (assoc state :thruster? false)))

(defn gung-ho [state]
  (if (< (:height state) 75)
    (slow-lander state)
    state))


      
