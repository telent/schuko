# What?

None of these are a definition, but all are common descriptions

* programming with functions

* programming without side-effects

* everything is an expression

* programming without state (no variable assignments)

* "what" not "how"

* weird (but less weird than it used to be)

# Why?

* It's easier to reason about a computation if there is no state

* It's easier to test a computation if it always give the same answer for the same input

* Moore's Law is over, the future is multicore.  We can do more in parallel if each computation can only see its own inputs

 * We can do less in total (lazy evaluation)


# How

* We're going to use Clojure, because it places a heavy emphasis on a
  non-mutating style

* Many techniques are applicable to Ruby (otherwise I wouldn't be
  doing this in work time) but the finer points of proc/block/lambda obscure the
  point


# Some syntax

* Numbers
* Strings
* Symbols
* Keywords
* Maps
* Functions
* Arrays

````
[1 2 3 4.5 "six" 'seven :eight {:nine 10} 
 (fn [x] (+ x 11))]

=> [1 2 3 4.5 "six" seven :eight {:nine 10} 
   #<server$eval1380$fn__1381 schuko.server$eval1380$fn__1381@3429dbb8>]
````
 

# Naming things

````

(def twelve 12)

(def double (fn [x] (* 2 x)))

(defn double [x] (* 2 x))    ; shorthand

(defn fun-with-locals [x]
  (let [a 1 b 2] 
    (+ a b x)))
````


# Functions

````
odd? => #<core$odd_QMARK_ clojure.core$odd_QMARK_@4f9c205b>

(odd? 3)            ; invoking a function
=> true

(max 1 2 3 2 -9)    ; ... with several parameters
=> 3

(map odd? [1 2 3])  ; ... on ALL THE THINGS
=> (true false true)
````

# Order of evaluation

Consider

````
(defn first-arg [x y] x)
(defn square [x] (* x x))
(first-arg (square 4) (square 2)) => ?
````

We could calculate from the inside out ("innermost reduction", "eager
evaluation")

````
(first-arg (square 4) (square 2))
(first-arg (* 4 4) (square 2))
(first-arg 16 (square 2))
(first-arg 16 (* 2 2))
(first-arg 16 4)
16
````

# Order of evaluation

Still considering 

````
(defn first-arg [x y] x)
(defn square [x] (* x x))
(first-arg (square 4) (square 2)) => ?
````

what if we evaluate from the outside in ("outermost reduction", "normal order reduction")

````
(first-arg (square 4) (square 2))
(square 4)
(* 4 4)
16
````

# Order of evaluation ...

* Both methods give the same answer, but inside-out might not
  always terminate.  What about 

````
(defn forever [] (forever))
(first-arg 10 (forever))
````

# Order of evaluation ...

An imperative language has to calculate inside-out, because
side-effects.  But if there are no side effects?

````
(defn infinite-integers [j]
  (cons j (infinite-integers (+ j 1))))

;; in real life, this blows the stack
(nth (infinite-integers 13) 8) => KABOOM
````

# Order of evaluation ...

An imperative language has to calculate inside-out, because
side-effects.  But if there are no side effects?

````
(defn infinite-integers [j] 
  (cons j (lazy-seq (infinite-integers (+ j 1)))))

;; 'lazy-seq' tells Clojure we want to do it lazily
(nth (infinite-integers 13) 8) => 21
````

* But who'd ever have a need for infinite sequences? Nobody uses GUIs
  or long-lived servers, do they?


# Higher-order functions

These look pretty similar

````
(defn add-two-to-everything [items] 
  (if (empty? items) 
      nil 
      (cons (+ 2 (first items)) 
            (add-two-to-everything (rest items)))))


(defn triple-everything [items] 
  (if (empty? items) 
      nil 
      (cons (* 3 (first items)) 
            (triple-everything (rest items)))))
```` 

How can we DRY it up and abstract the common parts?


# Higher-order functions

A higher order function (also: "functor") is a function that accepts
another function as argument, or that produces a function as result.

````
(def foo (fn [x] (fn [y] (* x y))))

;; applying the function gives ... a function!
(foo 2) => #<user$foo$fn__1389 user$foo$fn__1389@794f8b9a>

;; apply the function then apply the 
;; function it returned
((foo 2) 5) => 10
````

# Higher-order functions

So let's look at those do-to-everything functions again

````
(defn apply-fun-to-everything [fun items] 
  (if (empty? items) 
      nil 
      (cons (fun (first items))
            (apply-fun-to-everything func (rest items)))))

(defn add-two [x] (+ 2 x))
(apply-fun-to-everything add-two '(5 6 7 8))

(apply-fun-to-everything (fn [x] (* 3 x)) '(5 6 7 8))
                         
````

So we've separated traversing the data structure from the processing
that happens on each node.

# Fewer data structures, more functions

> "It is better to have 100 functions operate on one data structure than 10 functions on 10 data structures." -- Alan Perlis

This is a pretty common story.  That was the Visitor pattern, but many
of the traditional "Gang of Four" design patterns turn out to be
hardly worth naming.


# "Live"-coding demonstration

> Construct a simulator for a lunar lander, and use it to create a
flight plan that could be used to control a real one.  It should output
a line of text every second or whenever the thruster state changes,
showing time elapsed, height, velocity, and thruster state.

* Mass: 10149kg
* Engine thrust (full power): 44,400N
* Acceleration due to gravity: 1.6 m.s^-2
* Initial height: 210m
* Initial velocity: 0


Solution at http://www.github.com/telent/schuko/example/lander.clj

Oh look, it's the Policy pattern :-)
