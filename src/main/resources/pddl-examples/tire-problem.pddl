(define (problem pb_tire)
    (:domain dm-tire)

    (:objects trunk)

    (:init (tire flat)
           (tire spare)
           (at flat axle)
           (at spare trunk)
    )

    (:goal (at spare axle))
)