(define (domain movie-strips)
  (:requirements :strips :typing)
  (:types item)
  (:predicates (movie-rewound)
               (counter-at-two-hours)
               (counter-at-other-than-two-hours)
               (counter-at-zero)
               (have-chips)
               (have-dip)
               (have-pop)
               (have-cheese)
               (have-crackers)
               (chips ?x - item)
               (dip ?x - item)
               (pop ?x - item)
               (cheese ?x - item)
               (crackers ?x - item))

  (:action rewind-movie-2
           :parameters ()
           :precondition (counter-at-two-hours)
           :effect (movie-rewound))

  (:action rewind-movie
           :parameters ()
           :precondition (counter-at-other-than-two-hours)
           :effect (and (movie-rewound)
                        (not (counter-at-zero))))

  (:action reset-counter
           :parameters ()
           :precondition ()
           :effect (counter-at-zero))

  (:action get-chips
           :parameters (?x - item)
           :precondition (chips ?x)
           :effect (have-chips))

  (:action get-dip
           :parameters (?x - item)
           :precondition (dip ?x)
           :effect (have-dip))

  (:action get-pop
           :parameters (?x - item)
           :precondition (pop ?x)
           :effect (have-pop))

  (:action get-cheese
           :parameters (?x - item)
           :precondition (cheese ?x)
           :effect (have-cheese))

  (:action get-crackers
           :parameters (?x - item)
           :precondition (crackers ?x)
           :effect (have-crackers)))