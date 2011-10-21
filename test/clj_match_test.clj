(ns clj-match-test
  (:use [clj-match] :reload-all)
  (:use [clojure.test]))

(deftest test-int
  (is (= 'ok
	 (match 0
	   0	'ok)))
  (is (= 'ok
	 (match 1
	   0	'ng
	   1	'ok))))

(deftest test-str
  (is (= 'ok
	 (match "foo"
	   "foo"	'ok)))
  (is (= 'ok
	 (match "bar"
	   "foo"	'ng
	   "bar"	'ok))))

(deftest test-char
  (is (= 'ok
	 (match \a
	   \a	'ok)))
  (is (= 'ok
	 (match \b
	   \a	'ng
	   \b	'ok))))

(deftest test-symbol
  (is (= 'ok
	 (match 'hello
	   'hello	'ok)))
  (is (= 'ok
	 (match 'world
	   'hello	'ng
	   'world	'ok))))

(deftest test-empty
  (is (= 'ok
	 (match []
	   []	'ok)))
  (is (= 'ok
	 (match ()
	   []	'ok))))

(deftest test-list
  (is (= 'ok
	 (match [1]
	   [1]	'ok)))
  (is (= 'ok
	 (match [2]
	   [1]	'ng
	   [2]	'ok)))

  (is (= 'ok
	 (match '(1)
	   [1]	'ok)))
  (is (= 'ok
	 (match [1]
	   (1)	'ok)))

  (is (= 1
	 (match [1]
	   [x]	x)))
  (is (= 1
	 (match '(1)
	   (x)	x)))
  (is (= 1
	 (match [1]
	   (x)	x)))
  (is (= 1
	 (match '(1)
	   [x]	x)))

  (is (= 'OK
         (match ()
           (x)	'NG
           ()	'OK)))

  (is (= 'OK
         (match [1]
           (x y)	'NG
           (x)	'OK)))
)


(deftest test-with-nil-result
  (is (= nil
         (match ["hello" "world"]
           ("hello" "world" & rest) nil
           _ 'not_nil))))
