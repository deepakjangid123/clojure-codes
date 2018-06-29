(ns macros.core
  (:gen-class))

(defmacro <try-1>
  "Macro for try-catch block.
  Here we are quoting each symbol we want to use as a symbol"
  [body]
  (list 'try body
        (list 'catch 'Exception 'e 0)))

(<try> (/ 1 10))
(<try> (/ 1 0))

(defmacro <try-2>
  "Macro for try-catch block.
  Here we are using '`' syntax quoting and `~` unquote form.
  Here 'e#' is auto-gensym'd symbol, which is different from other user defined 'e' variable.
  Ex. (gensym 'e)"
  [body]
  `(try ~body
     (catch Exception e# 0)))

(<try-2> (/ 1 10))
(<try-2> (/ 1 0))

(defmacro <add>
  "Macro for addition.
  Here we are using '~@' unquote splicing, which unwraps a seqable data structure,
  placing its contents directly within the enclosing syntax-quoted data structure
  Ex:
  With unquote
  `(+ ~(list 1 2 3))
   => (clojure.core/+ (1 2 3))

  With unquote splicing
  `(+ ~@(list 1 2 3))
   => (clojure.core/+ 1 2 3)
  "
  [coll]
  `(+ ~@coll))

(<add> [3 4 5])
(<add> (1 2 3))
