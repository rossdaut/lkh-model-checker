# Fuentes de ejemplos PDDL externos

## Estructura de carpetas

- `potassco/` — instancias descargadas o derivadas del repositorio [`potassco/pddl-instances`](https://github.com/potassco/pddl-instances), que recopila instancias de las International Planning Competitions en una estructura uniforme.
- `ai-planning/` — dominios e instancias generados a partir del repositorio [`AI-Planning/pddl-generators`](https://github.com/AI-Planning/pddl-generators), que provee generadores clasicos de benchmarks PDDL.
- `ia-modern-aproach/` — dominios modelados a partir de ejemplos del libro *Artificial Intelligence: A Modern Approach* (Russell & Norvig). Incluye las familias `tire`, `rsc` y `logistics`.

Las familias bajo `potassco/ipc-1998` **no** provienen de `potassco/pddl-instances`: quedaron ubicadas ahi solo para unificar la organizacion de benchmarks externos. Su fuente real es el material del **1st International Planning Competition (1998)** conservado en el archivo revisado de benchmarks de esa competencia.

## Ajustes locales para compatibilidad con PDDL4J

Durante la exploracion de benchmarks aparecieron dos clases de problemas practicos con PDDL4J:

- en algunos dominios **no tipados**, la fase de inferencia de tipos de PDDL4J mutilaba precondiciones y efectos negativos de las acciones instanciadas;
- en algunas familias con `:action-costs` y `(:metric minimize (total-cost))`, la ruta de parseo/instanciacion que usa este proyecto no aceptaba bien los archivos originales aunque la parte proposicional del dominio si fuera util para el LTS.

Los cambios de compatibilidad quedaron reflejados directamente en los PDDL y se documentan aquí.

### Familias ajustadas por `:typing`

- `potassco/ipc-1998/domains/gripper-round-1-strips/`
  - Se reemplazo el dominio no tipado por una version **tipada equivalente**.
  - Tambien se reescribieron las instancias usadas (`instance-1`, `instance-2`) para que los tipos quedaran expresados via `:typing` y no como predicados unarios en `:init`.
  - Motivo: PDDL4J simplificaba mal acciones como `pick`, `drop` y `move` en la version no tipada, eliminando efectos negativos relevantes.

- `ai-planning/ferry/`
  - Se reemplazo el dominio no tipado por una version **tipada equivalente**.
  - Las instancias usadas de la familia quedaron alineadas con ese dominio tipado.
  - Motivo: en la version no tipada, PDDL4J simplificaba mal acciones como `board` y `debark`, preservando add effects pero perdiendo delete effects esenciales.

- `potassco/ipc-1998/domains/movie-strips/`
  - Se agrego `:requirements :strips :typing`, `:types item` y se tiparon los parametros de las acciones.
  - Se agrego `:precondition ()` en la accion `reset-counter` para compatibilidad con el parser de PDDL4J.

### Familias ajustadas por eliminacion de costos

- `potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/`
  - Derivada localmente de `parc-printer-sequential-optimal-strips`.
  - Se eliminaron `:action-costs`, la funcion `total-cost`, las asignaciones numericas iniciales y `(:metric minimize (total-cost))`.

- `potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/`
  - Derivada localmente de `woodworking-sequential-optimal-strips`.
  - Se eliminaron `:action-costs`, la funcion `total-cost`, los efectos `increase` y `(:metric minimize (total-cost))`.

### Variantes locales usadas para ajustar tamaño experimental

- `potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1-5pkg.pddl`
  - Variante local basada en la `instance-1` oficial.
  - Se removio un paquete extra que no participaba del goal para obtener un lts intermedio mas util en la tabla de resultados.

- `potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-12-trimmed.pddl`
  - Variante local basada en la `instance-12` oficial.
  - Ademas de la normalizacion sin costos, se elimino una pieza completa con sus hechos iniciales y metas asociadas para obtener un caso grande.

## Archivos incorporados

### potassco/ipc-1998

- `potassco/ipc-1998/domains/gripper-round-1-strips/domain.pddl` — derivado localmente del benchmark IPC 1998, reexpresado con `:typing`
- `potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-1.pddl` — reescrita para expresar tipos via `:typing`
- `potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-2.pddl` — reescrita para expresar tipos via `:typing`
- `potassco/ipc-1998/domains/logistics-round-2-strips/domain.pddl` — fuente: archivo revisado IPC 1998
- `potassco/ipc-1998/domains/logistics-round-2-strips/instances/instance-1.pddl` — fuente: archivo revisado IPC 1998
- `potassco/ipc-1998/domains/movie-strips/domain.pddl` — ajustado para compatibilidad con PDDL4J (typing, precondicion vacia)
- `potassco/ipc-1998/domains/movie-strips/instances/instance-1.pddl`

### potassco/ipc-2000

- `potassco/ipc-2000/domains/blocks-strips-typed/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/blocks-strips-typed/domain.pddl
- `potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-1.pddl`
- `potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-10.pddl`
- `potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-24.pddl`
- `potassco/ipc-2000/domains/logistics-strips-typed/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2000/domains/logistics-strips-typed/domain.pddl
- `potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1.pddl`
- `potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1-5pkg.pddl` — variante local (ver arriba)

### potassco/ipc-2002

- `potassco/ipc-2002/domains/depots-strips-automatic/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/depots-strips-automatic/domain.pddl
- `potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-1.pddl`
- `potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-2.pddl`
- `potassco/ipc-2002/domains/driverlog-strips-automatic/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/driverlog-strips-automatic/domain.pddl
- `potassco/ipc-2002/domains/driverlog-strips-automatic/instances/instance-1.pddl`
- `potassco/ipc-2002/domains/freecell-strips-automatic/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/freecell-strips-automatic/domain.pddl
- `potassco/ipc-2002/domains/freecell-strips-automatic/instances/instance-1.pddl`
- `potassco/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl
- `potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-1.pddl`
- `potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-3.pddl`

### potassco/ipc-2004

- `potassco/ipc-2004/domains/psr-small-strips/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2004/domains/psr-small-strips/domain.pddl
- `potassco/ipc-2004/domains/psr-small-strips/instances/instance-1.pddl`

### potassco/ipc-2006

- `potassco/ipc-2006/domains/pipesworld-propositional-strips/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2006/domains/pipesworld-propositional-strips/domain.pddl
- `potassco/ipc-2006/domains/pipesworld-propositional-strips/instances/instance-1.pddl`
- `potassco/ipc-2006/domains/storage-propositional/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2006/domains/storage-propositional/domain.pddl
- `potassco/ipc-2006/domains/storage-propositional/instances/instance-1.pddl`
- `potassco/ipc-2006/domains/tpp-propositional-strips/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2006/domains/tpp-propositional-strips/domain.pddl
- `potassco/ipc-2006/domains/tpp-propositional-strips/instances/instance-1.pddl`

### potassco/ipc-2008

- `potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl` — derivado de `parc-printer-sequential-optimal-strips`, sin costos
- `potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-1.pddl`
- `potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-2.pddl`
- `potassco/ipc-2008/domains/scanalyzer-3d-sequential-optimal-strips-no-costs/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/scanalyzer-3d-sequential-optimal-strips-no-costs/domain.pddl
- `potassco/ipc-2008/domains/scanalyzer-3d-sequential-optimal-strips-no-costs/instances/instance-1.pddl`
- `potassco/ipc-2008/domains/sokoban-sequential-optimal-strips-no-costs/domain.pddl` — https://raw.githubusercontent.com/potassco/pddl-instances/master/ipc-2008/domains/sokoban-sequential-optimal-strips-no-costs/domain.pddl
- `potassco/ipc-2008/domains/sokoban-sequential-optimal-strips-no-costs/instances/instance-1.pddl`
- `potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl` — derivado de `woodworking-sequential-optimal-strips`, sin costos
- `potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-1.pddl`
- `potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-2.pddl`
- `potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-12-trimmed.pddl` — variante local recortada (ver arriba)

### ai-planning/ferry

- `ai-planning/ferry/domain.pddl` — https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/ferry/domain.pddl (version tipada)
- `ai-planning/ferry/instances/instance-1.pddl` — generado con 4 locations, 5 cars, seed 1
- `ai-planning/ferry/instances/ferry-l5-c7-s1.pddl` — generado con 5 locations, 7 cars, seed 1
- `ai-planning/ferry/instances/ferry-l6-c6-s1.pddl` — generado con 6 locations, 6 cars, seed 1

### ai-planning/gripper

- `ai-planning/gripper/domain.pddl` — https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/gripper/domain.pddl
- `ai-planning/gripper/instances/gripper-6.pddl` — generado con 6 balls
- `ai-planning/gripper/instances/gripper-7.pddl` — generado con 7 balls
- `ai-planning/gripper/instances/gripper-8.pddl` — generado con 8 balls
- `ai-planning/gripper/instances/gripper-10.pddl` — generado con 10 balls

### ai-planning/storage

- `ai-planning/storage/domain.pddl` — https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/storage/domain.pddl
- `ai-planning/storage/instances/storage-08.pddl` — generado con parametros IPC5 problema 08
- `ai-planning/storage/instances/storage-09.pddl` — generado con parametros IPC5 problema 09
- `ai-planning/storage/instances/storage-10.pddl` — generado con parametros IPC5 problema 10

### ai-planning/woodworking

- `ai-planning/woodworking/domain.pddl` — https://raw.githubusercontent.com/AI-Planning/pddl-generators/main/woodworking/domain.pddl
- `ai-planning/woodworking/instances/instance-1.pddl`

### ia-modern-aproach

- `ia-modern-aproach/tire/` — dominio y problema tire-world, modelados a partir de AIMA
- `ia-modern-aproach/rsc/` — dominio RSC con instancias easy y hard, modelados a partir de AIMA
- `ia-modern-aproach/logistics/` — dominio logistics con instancia local, modelados a partir de AIMA
