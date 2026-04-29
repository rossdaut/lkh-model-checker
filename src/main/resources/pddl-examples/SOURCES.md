# Fuentes de ejemplos PDDL externos

Los ejemplos bajo `external/potassco` fueron descargados desde el repositorio
[`potassco/pddl-instances`](https://github.com/potassco/pddl-instances), que recopila instancias
de las International Planning Competitions en una estructura uniforme.

Tambien se agregaron dominios bajo `external/ai-planning` a partir del repositorio
[`AI-Planning/pddl-generators`](https://github.com/AI-Planning/pddl-generators), que recopila
generadores clasicos de benchmarks PDDL.

Las familias bajo `external/potassco/ipc-1998` **no** provienen de `potassco/pddl-instances`:
quedaron ubicadas ahi solo para unificar la organizacion de benchmarks externos. Su fuente real es
el material del **1st International Planning Competition (1998)** conservado en el archivo
revisado de benchmarks de esa competencia.

## Ajustes locales para compatibilidad con PDDL4J

Durante la exploracion de benchmarks aparecieron dos clases de problemas practicos con PDDL4J:

- en algunos dominios **no tipados**, la fase de inferencia de tipos de PDDL4J mutilaba
  precondiciones y efectos negativos de las acciones instanciadas;
- en algunas familias con `:action-costs` y `(:metric minimize (total-cost))`, la ruta de
  parseo/instanciacion que usa este proyecto no aceptaba bien los archivos originales aunque la
  parte proposicional del dominio si fuera util para el LTS.

Para evitar dejar esa logica de saneamiento dentro del codigo Java del proyecto, los cambios de
compatibilidad quedaron reflejados directamente en los PDDL y se documentan aca.

### Familias ajustadas por `:typing`

- `external/potassco/ipc-1998/domains/gripper-round-1-strips/`
  - Se reemplazo el dominio no tipado por una version **tipada equivalente**.
  - Tambien se reescribieron las instancias usadas (`instance-1`, `instance-2`) para que los tipos
    quedaran expresados via `:typing` y no como predicados unarios en `:init`.
  - Motivo: PDDL4J simplificaba mal acciones como `pick`, `drop` y `move` en la version no tipada,
    eliminando efectos negativos relevantes.

- `external/ai-planning/ferry/`
  - Se reemplazo el dominio no tipado por una version **tipada equivalente**.
  - Las instancias usadas de la familia quedaron alineadas con ese dominio tipado.
  - Motivo: en la version no tipada, PDDL4J mutilaba acciones como `board` y `debark`,
    preservando add effects pero perdiendo delete effects esenciales.

### Familias ajustadas por eliminacion de costos

- `external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/`
  - Derivada localmente de `parc-printer-sequential-optimal-strips`.
  - Se eliminaron `:action-costs`, la funcion `total-cost`, las asignaciones numericas iniciales y
    `(:metric minimize (total-cost))`.
  - Motivo: la parte de costos/metrica chocaba con PDDL4J, pero la parte proposicional si era
    util para construir y comparar LTS.

- `external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/`
  - Derivada localmente de `woodworking-sequential-optimal-strips`.
  - Se eliminaron `:action-costs`, la funcion `total-cost`, los efectos `increase` y
    `(:metric minimize (total-cost))`.
  - Motivo: mismo criterio que en `parc-printer`: se preservo la dinamica proposicional usada por
    el constructor de LTS y el checker, descartando solo la capa de costos que PDDL4J no bancaba
    bien en esta integracion.

### Variantes locales usadas para ajustar tamaño experimental

- `external/potassco/ipc-2002/domains/satellite-strips-automatic/instances/instance-2-lite.pddl`
  - Variante local basada en la `instance-2` oficial.
  - Se simplifico el problema conservando un solo instrumento y dos modos para obtener una segunda
    instancia de la familia que siguiera siendo soluble y entrara en el presupuesto experimental.

- `external/potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1-5pkg.pddl`
  - Variante local basada en la `instance-1` oficial.
  - Se removio un paquete extra que no participaba del goal para obtener un escalon intermedio mas
    util en la tabla de rendimiento.

- `external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-12-trimmed.pddl`
  - Variante local basada en la `instance-12` oficial.
  - Ademas de la normalizacion sin costos, se elimino una pieza completa con sus hechos iniciales y
    metas asociadas para obtener un caso grande pero todavia usable dentro del presupuesto temporal.

Archivos incorporados:

- `external/potassco/ipc-2000/domains/blocks-strips-typed/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/domain.pddl
- `external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/instances/instance-1.pddl
- `external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-2.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/instances/instance-2.pddl
- `external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-3.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/instances/instance-3.pddl
- `external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-10.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/instances/instance-10.pddl
- `external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-24.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/instances/instance-24.pddl
- `external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-100.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/instances/instance-100.pddl
- `external/potassco/ipc-2002/domains/driverlog-strips-automatic/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/driverlog-strips-automatic/domain.pddl
- `external/potassco/ipc-2002/domains/driverlog-strips-automatic/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/driverlog-strips-automatic/instances/instance-1.pddl
- `external/potassco/ipc-2002/domains/driverlog-strips-automatic/instances/instance-2.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/driverlog-strips-automatic/instances/instance-2.pddl
- `external/potassco/ipc-2002/domains/depots-strips-automatic/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/depots-strips-automatic/domain.pddl
- `external/potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/depots-strips-automatic/instances/instance-1.pddl
- `external/potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-2.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/depots-strips-automatic/instances/instance-2.pddl
- `external/potassco/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl
- `external/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-1.pddl
- `external/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-2.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-2.pddl
- `external/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-3.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-3.pddl
- `external/potassco/ipc-2002/domains/rovers-strips-automatic/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/rovers-strips-automatic/domain.pddl
- `external/potassco/ipc-2002/domains/rovers-strips-automatic/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/rovers-strips-automatic/instances/instance-1.pddl
- `external/potassco/ipc-2002/domains/satellite-strips-automatic/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/satellite-strips-automatic/domain.pddl
- `external/potassco/ipc-2002/domains/satellite-strips-automatic/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/satellite-strips-automatic/instances/instance-1.pddl
- `external/potassco/ipc-2002/domains/satellite-strips-automatic/instances/instance-2-lite.pddl`
  - Variante local basada en https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/satellite-strips-automatic/instances/instance-2.pddl
  - Ajuste aplicado: se redujo el problema a un solo instrumento y dos modos para obtener un caso intermedio soluble y manejable
- `external/potassco/ipc-2000/domains/logistics-strips-typed/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/logistics-strips-typed/domain.pddl
- `external/potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/logistics-strips-typed/instances/instance-1.pddl
- `external/potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1-5pkg.pddl`
  - Variante local basada en https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/logistics-strips-typed/instances/instance-1.pddl
  - Ajuste aplicado: se removio un paquete extra no usado en el goal para obtener una variante intermedia
- `external/potassco/ipc-2004/domains/satellite-strips/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2004/domains/satellite-strips/domain.pddl
- `external/potassco/ipc-2004/domains/satellite-strips/instances/instance-1.pddl`
  - Fuente: https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2004/domains/satellite-strips/instances/instance-1.pddl
- `external/potassco/ipc-1998/domains/gripper-round-1-strips/domain.pddl`
  - Derivado localmente del benchmark IPC 1998 disponible en el archivo revisado de esa competencia
  - Ajuste aplicado: dominio reexpresado con `:typing` para evitar la mutilacion de acciones de PDDL4J
- `external/potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-1.pddl`
  - Derivado localmente del benchmark IPC 1998 correspondiente
  - Ajuste aplicado: instancia reescrita para expresar tipos via `:typing`
- `external/potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-2.pddl`
  - Derivado localmente del benchmark IPC 1998 correspondiente
  - Ajuste aplicado: instancia reescrita para expresar tipos via `:typing`
- `external/potassco/ipc-1998/domains/logistics-round-2-strips/domain.pddl`
  - Fuente: benchmark IPC 1998 tomado del archivo revisado de esa competencia
- `external/potassco/ipc-1998/domains/logistics-round-2-strips/instances/instance-1.pddl`
  - Fuente: benchmark IPC 1998 tomado del archivo revisado de esa competencia
- `external/potassco/ipc-1998/domains/logistics-round-2-strips/instances/instance-2.pddl`
  - Fuente: benchmark IPC 1998 tomado del archivo revisado de esa competencia
- `external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl`
  - Derivado localmente de `potassco/pddl-instances/ipc-2008/domains/parc-printer-sequential-optimal-strips/domain-1.pddl`
  - Normalizacion aplicada: se eliminaron `:action-costs`, la funcion `total-cost`, las asignaciones numericas iniciales y la metrica de optimizacion, sin tocar la parte proposicional usada para construir el LTS
- `external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-1.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/parc-printer-sequential-optimal-strips/instance-1.pddl
  - Normalizacion aplicada: se elimino `(:metric minimize (total-cost))`
- `external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-2.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/parc-printer-sequential-optimal-strips/instance-2.pddl
  - Normalizacion aplicada: se elimino `(:metric minimize (total-cost))`
- `external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-3.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/parc-printer-sequential-optimal-strips/instance-3.pddl
  - Normalizacion aplicada: se elimino `(:metric minimize (total-cost))`
- `external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/woodworking-sequential-optimal-strips/domain.pddl
  - Normalizacion aplicada: se eliminaron `:action-costs`, la funcion `total-cost`, los efectos `increase` y la metrica de optimizacion, sin tocar la parte proposicional usada para construir el LTS
- `external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-1.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/woodworking-sequential-optimal-strips/instance-1.pddl
  - Normalizacion aplicada: se elimino `(:metric minimize (total-cost))`
- `external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-2.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/woodworking-sequential-optimal-strips/instance-2.pddl
  - Normalizacion aplicada: se eliminaron `(:metric minimize (total-cost))` y los costos numericos por pieza
- `external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-12-trimmed.pddl`
  - Derivado localmente de https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/woodworking-sequential-optimal-strips/instance-12.pddl
  - Normalizacion aplicada: se elimino `(:metric minimize (total-cost))` y se recorto una pieza (`p3`) con sus hechos iniciales y metas asociadas para obtener una instancia intermedia util para la escalera experimental

- `external/ai-planning/ferry/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/ferry/domain.pddl
- `external/ai-planning/ferry/instances/ferry-l4-c5-s1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `ferry` de `AI-Planning/pddl-generators` con `4` locations, `5` cars y `seed 1`
- `external/ai-planning/ferry/instances/ferry-l5-c5-s1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `ferry` de `AI-Planning/pddl-generators` con `5` locations, `5` cars y `seed 1`
- `external/ai-planning/ferry/instances/ferry-l5-c6-s1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `ferry` de `AI-Planning/pddl-generators` con `5` locations, `6` cars y `seed 1`
- `external/ai-planning/ferry/instances/ferry-l6-c6-s1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `ferry` de `AI-Planning/pddl-generators` con `6` locations, `6` cars y `seed 1`
- `external/ai-planning/ferry/instances/ferry-l5-c7-s1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `ferry` de `AI-Planning/pddl-generators` con `5` locations, `7` cars y `seed 1`
- `external/ai-planning/gripper/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/gripper/domain.pddl
- `external/ai-planning/gripper/instances/gripper-6.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `gripper` de `AI-Planning/pddl-generators` con `6` balls
- `external/ai-planning/gripper/instances/gripper-7.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `gripper` de `AI-Planning/pddl-generators` con `7` balls
- `external/ai-planning/gripper/instances/gripper-8.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `gripper` de `AI-Planning/pddl-generators` con `8` balls
- `external/ai-planning/gripper/instances/gripper-10.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `gripper` de `AI-Planning/pddl-generators` con `10` balls
- `external/ai-planning/storage/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/storage/domain.pddl
- `external/ai-planning/storage/instances/storage-08.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `storage` de `AI-Planning/pddl-generators` con los parametros IPC5 del problema 08
- `external/ai-planning/storage/instances/storage-09.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `storage` de `AI-Planning/pddl-generators` con los parametros IPC5 del problema 09
- `external/ai-planning/storage/instances/storage-10.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `storage` de `AI-Planning/pddl-generators` con los parametros IPC5 del problema 10
- `external/ai-planning/storage/instances/storage-11.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `storage` de `AI-Planning/pddl-generators` con los parametros IPC5 del problema 11
- `external/ai-planning/satellite/domain.pddl`
  - Fuente: https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/satellite/domain.pddl
- `external/ai-planning/satellite/instances/satellite-s1-i1-m2-t5-o3-seed1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `satellite` de `AI-Planning/pddl-generators` con `1` satellite, `1` instrument, `2` modes, `5` targets, `3` observations y `seed 1`
- `external/ai-planning/satellite/instances/satellite-s1-i1-m2-t5-o3-seed2.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `satellite` de `AI-Planning/pddl-generators` con `1` satellite, `1` instrument, `2` modes, `5` targets, `3` observations y `seed 2`
- `external/ai-planning/satellite/instances/satellite-s1-i1-m2-t5-o3-seed3.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `satellite` de `AI-Planning/pddl-generators` con `1` satellite, `1` instrument, `2` modes, `5` targets, `3` observations y `seed 3`
- `external/ai-planning/satellite/instances/satellite-s1-i1-m2-t6-o2-seed1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `satellite` de `AI-Planning/pddl-generators` con `1` satellite, `1` instrument, `2` modes, `6` targets, `2` observations y `seed 1`
- `external/ai-planning/satellite/instances/satellite-s1-i1-m2-t7-o2-seed1.pddl`
  - Generado localmente para esta coleccion siguiendo la familia `satellite` de `AI-Planning/pddl-generators` con `1` satellite, `1` instrument, `2` modes, `7` targets, `2` observations y `seed 1`
