(ns schuko.core
  (:require [schuko.events :as e]))

(set! (.-onload js/window) e/run-on-ready)