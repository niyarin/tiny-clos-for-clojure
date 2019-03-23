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

(ns niyarin.tiny-clos)

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
   '(direct-supers direct-slots cpl slots nfields field-initializers getters-n-setters))

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

