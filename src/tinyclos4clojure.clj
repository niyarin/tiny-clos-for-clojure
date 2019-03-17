

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
;
; EDIT HISTORY:
;
;      10/**/92  Gregor  Originally Written
; 1.0  11/10/92  Gregor  Changed names of generic invocation generics.
;                        Changed compute-getters-and-setters protocol.
;                        Made comments match the code.
;                        Changed maximum line width to 72.
; 1.1  11/24/92  Gregor  Fixed bug in compute-method-more-specific?,
;                        wrt the use of for-each.
;                        Both methods on allocate instance failed to
;                        initialize fields properly.
;                        The specializers and procedure initargs are
;                        now required when creating a method, that is,
;                        they no longer default.  No working program
;                        should notice this change.
; 1.2  12/02/92  Gregor  Fix minor things that improve portability:
;                         - DEFINE needs 2 args in R4Rs
;                         - Conditionalize printer hooks.
;                         - () doesn't evaluate to ()
;
; 1.3  12/08/92  Gregor  More minor things:
;                         - () really doesn't evaluate to () damnit!
;                         - It turns out DEFINE-MACRO is never used.
;                         - Confusion over the "failure" return value
;                           of ASSQ -- ASSQ returns #f if the key is
;                           not found.
;                         - SEQUENCE   --> BEGIN
;                         - LAST-PAIR  --> last now in support
;                        Change instance rep to protect Schemes that
;                        don't detect circular structures when
;                        printing.
;                        A more reasonable error message when there
;                        are no applicable methods or next methods.
; 1.4  12/10/92  Gregor  Flush filter-in for collect-if.  Add news
;                        classes <input-port> and <output-port>.
;                        Also add 
;
; 1.5  12/17/92  Gregor  Minor changes to class of and primitive
;                        classes to try and deal with '() and #f
;                        better.
;
; 1.6   9/9/93   Gregor  Fix a monstrous bug in the bootstrap of
;                        compute-apply-generic which sometimes ran
;                        user methods on this generic function when
;                        it shouldn't.
;
; 1.7   8/9/94   Gregor  Add Scheme 48 to support.scm.




(ns niyarin.tiny-clos)
