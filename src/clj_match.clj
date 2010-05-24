(ns clj-match
  (:use [clojure core set]
        [clojure.contrib str-utils pprint]))

(defn find-symbols [pat]
  (defn find-symbols-inner [pat]
    (cond
     (symbol? pat)	(if (= '_ pat) {} {pat true})
     ;;
     (and (coll? pat)
	  (> (count pat) 0)
	  (or (= :and (first pat))
	      (= :or (first pat))))
     (apply
      union
      (map find-symbols-inner (rest pat)))
     ;;
     (and (coll? pat)
	  (> (count pat) 1)
	  (= :? (first pat)))
     (apply
      union
      (map find-symbols-inner (nthnext pat 2)))
     ;;
     (coll? pat)	(apply union (map find-symbols-inner pat))
     true {}))
  (keys (find-symbols-inner pat)))

(defn compile-clause [val pat]
  (cond
   (true? pat)	pat
   ;;
   (symbol? pat)
   (condp = pat
     '_		true
     pat	`(do (dosync (ref-set ~pat ~val)) true))
   ;;
   (coll? pat)
   (cond
    (empty? pat)	`(empty? ~val)
    ;;
    true
    (cond
     (and (= 2 (count pat))
	  (= '& (first pat))
	  (symbol? (second pat)))
     `(do (dosync (ref-set ~(second pat) ~val)))
     ;;
     (= (first pat) 'quote)	`(= ~val ~pat)
     ;;
     (= (first pat) :and)	`(and ~@(map (partial compile-clause val) (rest pat)))
     ;;
     (= (first pat) :or)	`(or ~@(map (partial compile-clause val) (rest pat)))
     ;;
     (= (first pat) :not)	`(not (or ~@(map (partial compile-clause val) (rest pat))))
     ;;
     (and (= (first pat) :?)
	  (not (empty?
		(rest pat))))
     `(every? ~(second pat) ~(nthnext pat 2))
     ;;
     true
     (let [car-sym (gensym), cdr-sym (gensym)]
       `(and (coll? ~val)
	     (let [~car-sym (first ~val),
		   ~cdr-sym (rest ~val)]
	       (and ~(compile-clause car-sym (first pat))
		    ~(compile-clause cdr-sym (rest pat))))))))
   true `(= ~pat ~val)))

(defmacro if-match [val pat match-body & mismatch-body]
  (let [syms (find-symbols pat),
	val-sym (gensym),
	pred (compile-clause val-sym pat)
	ref-bindings (apply vector
			    (apply concat (map (fn [sym] `[~sym (ref nil)])
					       syms)))
	deref-bindings (apply vector
			      (apply concat (map (fn [sym] `[~sym @~sym])
						 syms)))]
    `(let [~val-sym ~val]
       (let ~ref-bindings
	 (if ~pred
	   (let ~deref-bindings
	     ~match-body)
	   ~@mismatch-body)))))
