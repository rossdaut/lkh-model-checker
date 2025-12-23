(define (domain tire)
    (:requirements :adl)

    (:constants flat spare ground axle)

    (:predicates (at ?obj ?place)
                 (tire ?obj)
    )

    (:action remove
        :parameters (?obj ?place)
        :precondition (at ?obj ?place)
        :effect (and (not (at ?obj ?place)) (at ?obj ground))
    )

    (:action put-on
        :parameters (?obj)
        :precondition ( and (tire ?obj)
                            (at ?obj ground)
                            (not (at flat axle))
                            (not (at spare axle))
                      )
        :effect (and (not (at ?obj ground)) (at ?obj axle))
    )
)