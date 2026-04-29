# Seleccion final provisional de ejemplos

Este archivo resume **solo** los ejemplos que hoy forman parte de la lista activa de
[PreliminaryResultsCli.java](D:/Usuario/Desktop/uni/tesis/lkh-model-checker/src/main/java/lkh/cli/PreliminaryResultsCli.java:1).

Para el detalle completo de origen y de las normalizaciones locales, ver
[SOURCES.md](D:/Usuario/Desktop/uni/tesis/lkh-model-checker/src/main/resources/pddl-examples/SOURCES.md:1).

## Casos locales

- `tire / tire-problem`
  - Archivos locales del repositorio.

- `rsc / easy`
  - Archivos locales del repositorio.

- `logistics / local-problem`
  - Archivos locales del repositorio.

## Casos IPC / oficiales sin cambios semanticos relevantes

- `ipc-2000 blocks / instance-1`
  - Fuente oficial `potassco/pddl-instances`.

- `ipc-2002 zenotravel / instance-1`
  - Fuente oficial `potassco/pddl-instances`.

- `ipc-2002 zenotravel / instance-2`
  - Fuente oficial `potassco/pddl-instances`.

- `ipc-2002 depots / instance-1`
  - Fuente oficial `potassco/pddl-instances`.

- `ipc-2002 satellite-strips-automatic / instance-1`
  - Fuente oficial `potassco/pddl-instances`.

## Casos con ajuste por compatibilidad PDDL4J

- `ipc-1998 gripper / instance-1`
  - Dominio e instancia reexpresados con `:typing` para evitar la mutilacion de acciones en PDDL4J.

- `ipc-1998 gripper / instance-2`
  - Igual criterio que `instance-1`.

- `ai-planning ferry / 5 locations, 6 cars (seed 1)`
  - Dominio reexpresado con `:typing` para evitar la mutilacion de acciones en PDDL4J.

- `ai-planning ferry / 6 locations, 6 cars (seed 1)`
  - Igual criterio que `ferry 5x6`.

## Casos normalizados sin costos

- `ipc-2008 parc-printer / instance-1 (no-costs)`
  - Version sin `:action-costs` ni `:metric`.

- `ipc-2008 parc-printer / instance-2 (no-costs)`
  - Version sin `:action-costs` ni `:metric`.

- `ipc-2008 parc-printer / instance-3 (no-costs)`
  - Version sin `:action-costs` ni `:metric`.

- `ipc-2008 woodworking / instance-1 (no-costs)`
  - Version sin `:action-costs`, funciones de costo y `:metric`.

- `ipc-2008 woodworking / instance-2 (no-costs)`
  - Igual criterio que `instance-1`.

## Variantes locales ajustadas

- `ipc-2000 logistics / instance-1 (5pkg)`
  - Variante local de la oficial `instance-1`, removiendo un paquete extra no usado en el goal.

- `ipc-2002 satellite-strips-automatic / instance-2-lite`
  - Variante local de la oficial `instance-2`, reducida a un instrumento y dos modos.

- `ipc-2008 woodworking / instance-12-trimmed (no-costs)`
  - Variante local de la oficial `instance-12`, ademas normalizada sin costos y recortada en una pieza.
