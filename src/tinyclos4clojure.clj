;Tiny Closのコピーライト↓

; Tiny CLOS copyright
;
; **********************************************************************
; Copyright (c) 1992 Xerox Corporation.  
; All Rights Reserved.  
;
; Use, reproduction, and preparation of derivative works are permitted.
; Any copy of this software or of any derivative work must include the
; above copyright notice of Xerox Corporation, this paragraph and the
; one after it.  Any distribution of this software or derivative works
; must comply with all applicable United States export control laws.
;
; This software is made available AS IS, and XEROX CORPORATION DISCLAIMS
; ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
; IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
; PURPOSE, AND NOTWITHSTANDING ANY OTHER PROVISION CONTAINED HEREIN, ANY
; LIABILITY FOR DAMAGES RESULTING FROM THE SOFTWARE OR ITS USE IS
; EXPRESSLY DISCLAIMED, WHETHER ARISING IN CONTRACT, TORT (INCLUDING
; NEGLIGENCE) OR STRICT LIABILITY, EVEN IF XEROX CORPORATION IS ADVISED
; OF THE POSSIBILITY OF SUCH DAMAGES.
; **********************************************************************

(ns niyarin.tiny-clos
   (:require clojure.pprint);for test
  )

(let* [instance-list (ref (hash-map))
       get-field ;refを返す
          (fn [closure] 
             (let [cell (@instance-list closure)]
                (and cell)))]

   (defn %allocate-instance-internal
      [class-data lock proc nfields]
      (let* [field 
                (ref
                  (apply 
                     vector 
                     (concat 
                        (list proc lock class-data)
                        (repeat nfields false))))
             res (fn [& args] (apply (@field 0) args))]
         (dosync
            (ref-set instance-list (assoc @instance-list res field))
            res)
         ))
      
      ;fieldのclass部(2番目)に自身をセット
      (defn %set-instance-class-to-self! [closure]
         (let [field (get-field closure)]
               (ref-set field (assoc @field 2 closure))))

      (defn %instance? [object] (get-field object))

      (defn %instance-class [closure] 
         (let [field (get-field closure)]
            (field 2)))

      (defn %instance-ref [closure index]
         (@(get-field closure) (+ index 3)))

      (defn %instance-set! [closure index new-value]
         (let [field (get-field closure)]
            (ref-set field (assoc @field (+ index 3) new-value)))))


(defn %allocate-instance [class-data nfields]
   (%allocate-instance-internal 
      class-data
      true
      (fn [& args] (throw (ex-info "ERROR" {})))
      nfields))


(def slots-of-class 
   '(direct-supers direct-slots class-priority-list slots nfields field-initializers getters-n-setters))

;<class>用のgetter/setterリスト
(def getters-n-setters-for-class
   (reduce
      (fn [left slot-name]
         (assoc left 
                slot-name
                (let [slot-index (.indexOf slots-of-class slot-name)]
                  (if (= slot-index -1)
                      (throw (ex-info "ERROR" {}))
                      (list 
                        (fn [object] (%instance-ref object slot-index));getter
                        (fn [object new-value] (%instance-set! object slot-index new-value)))))))
      {}
      slots-of-class))

(def <class> (%allocate-instance false (count slots-of-class)))
(dosync 
   (%set-instance-class-to-self! <class>))

(declare slot-ref)
(defn lookup-slot-info [class-data slot-name]
   (let [getter-n-setters
         (if (= class-data <class>)
            getters-n-setters-for-class
            (slot-ref class 'geters-n-setters)
            )]
      (let [entry (getter-n-setters slot-name)]
         (if entry
           entry
           (throw (ex-info (str "ERROR:not found slot " slot-name " .") {}))))))

(defn class-of [object]
  (cond 
    (%instance? object) (%instance-class object)
    :else 'ATODE));TODO

(defn slot-ref [object slot-name]
  (let* [info (lookup-slot-info (class-of object) slot-name)
         getter (first info)]
    (getter object)))

(defn slot-set! [object slot-name new-value]
  (let* [info (lookup-slot-info (class-of object) slot-name)
         setter (second info)]
    (setter object new-value)))

;<class> refs
(defn class-direct-slots [class-object]
  (slot-ref class-object 'direct-slots))

(defn class-direct-supers [class-object]
  (slot-ref class-object 'direct-supers))

(defn class-slots [class-object]
  (slot-ref class-object 'slots))
 
(defn class-class-priority-list [class-object]
  (slot-ref class-object 'class-priority-list))


(defn make-class-based-object [args]
   (let* [new-object (%allocate-instance <class> (count slots-of-class))
          direct-supers (args 'direct-supers)
          direct-slots (map list (args 'direct-slots))]

      (let* [class-priority-list (reverse direct-supers);TODO:あとで
             slots
               (apply
                 concat
                 (cons direct-slots
                       (map class-direct-slots (next class-priority-list))))
             nfields (ref 0)
             field-initializers (ref '())
             allocator
                (fn [init]
                  (let [f nfields]
                    (dosync
                      (ref-set nfields (+ @nfields 1))
                      (ref-set field-initializers (cons init @field-initializers)))
                    (list (fn [o] (%instance-ref o f))
                          (fn [o n] (%instance-set! o f n)))))
              getters-n-setters
                (reduce
                  (fn [left slot-name]
                    (assoc left
                           slot-name
                           (cons (first slot-name) (allocator (fn [] '())))))
                  {}
                  slots)]
         (dosync
           (slot-set! new-object 'direct-supers direct-supers)
           (slot-set! new-object 'direct-slots direct-slots)
           (slot-set! new-object 'class-priority-list class-priority-list)
           (slot-set! new-object 'slots slots)
           (slot-set! new-object 'nfields 0)
           (slot-set! new-object 'field-initializers (reverse @field-initializers))
           (slot-set! new-object 'getters-n-setters getters-n-setters))
         new-object)))

(def <top>
  (make-class-based-object
    {'direct-supres '()
     'direct-slots '()}))

(def <object>
  (make-class-based-object
    {'direct-supers (list <top>)
     'direct-slots '()}))

(dosync
    (slot-set! <class> 'direct-supers (list <object>))
    (slot-set! <class> 'direct-slots (map list slots-of-class))
    (slot-set! <class> 'class-priority-list (list <class> <object> <top>))
    (slot-set! <class> 'slots (map list slots-of-class))
    (slot-set! <class> 'nfields (count slots-of-class))
    (slot-set! <class> 'field-initializers
               (map
                    (fn [s] (fn [] '()))
                    slots-of-class))
    (slot-set! <class> 'getters-n-setters '()))
