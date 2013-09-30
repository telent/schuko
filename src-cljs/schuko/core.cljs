(ns schuko.core
  (:require [schuko.events :as e]
            [schuko.dom :as dom]))

(set! (.-onload js/window) e/run-on-ready)
