(define (domain conditional-test)
  (:requirements :strips :conditional-effects)

  (:predicates (p) (q))

  (:action enable-q
    :parameters ()
    :precondition ()
    :effect (q)
  )

  (:action toggle
    :parameters ()
    :precondition (p)
    :effect (and
      (p)
      (when (q) (not (p))))
  )
)
