(define (domain test)
  (:requirements :strips)

  (:constants x y)

  (:predicates (p ?v))

  (:action a
    :parameters ()
    :precondition (p x)
    :effect (and (p x) (not (p y)))
  )

  (:action b
    :parameters ()
    :precondition (p y)
    :effect (and (p y) (not (p x)))
  )

  (:action c
    :parameters ()
     :precondition (p x)
     :effect (and (p x) (p y))
  )

  (:action d
    :parameters ()
    :precondition (p y)
    :effect (and (p y) (p x))
  )
)