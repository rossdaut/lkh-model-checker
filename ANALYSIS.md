# Análisis por familia: comportamiento de SSS vs NONE

Este documento registra el análisis estructural de por qué SSS (Strong Stubborn Sets) comprime
o no comprime el LTS en cada familia de dominios PDDL testeada.

---

## Convenciones

- **SSS nodes**: nodos del LTS explorados usando la poda SSS durante la generación
- **NONE nodes**: nodos explorados sin poda
- **Ratio**: NONE / SSS (>1 significa que SSS redujo la exploración)
- `passed=true` confirma que la instancia es resoluble (se encontró estrategia universal)
- Todas las instancias usan el mismo dominio; lo que varía es la instancia concreta

---

## Resumen de ratios SSS/NONE (todas las familias testeadas)

| Familia | SSS nodes | NONE nodes | Ratio | SSS ayuda? |
|---|---:|---:|---:|---|
| parking (ipc-2011) | 9,086\* | 691,234\* | **76×** | sí |
| woodworking (ipc-2008) | 852 | 16,875 | **20×** | sí |
| movie (ipc-1998) | 10 | 128 | **13×** | sí |
| freecell (ipc-2000) | 2,091\* | 5,794 | **2.8×** | sí |
| parc-printer (ipc-2008) | 21 | 42 | **2×** | sí |
| barman (ipc-2011) | 484,603\* | 963,473\* | **2×** | sí |
| floor-tile (ipc-2011) | 907,164\* | 1,470,286\* | **1.6×** | sí |
| rovers (ipc-2002) | 784,927\* | 792,305\* | ~1× | no |
| todos los demás | — | — | **1×** | no |

\* = timeout (60s), se compara la exploración alcanzada antes del corte.

El resultado más importante: la mayoría de familias da ratio exactamente 1×, lo que significa
que el stubborn set calculado en cada estado es igual al conjunto de acciones aplicables completo.

---

## Hipótesis general sobre cuándo SSS comprime el LTS

SSS (Strong Stubborn Sets) encuentra compresión cuando el dominio tiene alguna de estas propiedades
estructurales:

### 1. Elecciones simétricas / intercambiables ("symmetry collapse")
Cuando hay múltiples objetos que satisfacen el mismo subgoal, SSS colapsa en uno solo la
exploración de todas las elecciones equivalentes. Ejemplo claro: **movie** (5 chips
intercambiables para el goal `have-chips`) y **parc-printer** (slots de impresión equivalentes).

### 2. Sub-tareas estructuralmente independientes ("structural independence")
Cuando el dominio tiene objetos cuyas cadenas de acciones no comparten predicados afectados.
SSS evita explorar todas las interfoliaciones de esas cadenas.
Ejemplo: **woodworking** (procesar la placa p0 no afecta procesar p1; las máquinas son
independientes por placa) y **parking** a gran escala.

### 3. Sub-objetivos ya satisfechos en el init ("goal decomposition")
Cuando parte de los goals están satisfechos desde el inicio, SSS identifica acciones que
no deben perturbar esos sub-goals ya logrados y las excluye del stubborn set.
Esto crea conjuntos independientes: las acciones del "sub-problema pendiente" vs. las que
preservan el "sub-problema ya resuelto".

### Cuándo SSS NO ayuda (ratio = 1×)

Cuando todas las acciones comparten predicados entre sí, el grafo de dependencias de SSS
queda completamente conectado. El algoritmo cierra el stubborn set hasta incluir todas las
acciones aplicables → sin compresión. Esto ocurre en:

- **Dominios con recurso global único**: gripper (la posición del robot acopla todas las
  acciones de traslado), blocks (la única mesa y el predicado `on-table` son compartidos),
  sokoban (el tablero compartido), logistics.
- **Instancias pequeñas**: incluso en dominios que sí se comprimen a gran escala (barman,
  parking), instancias menores no muestran compresión porque no hay suficiente ramificación
  independiente.

---

## Parking (`ipc-2011 parking`)

### Dominio
Autos apilados en cordones (curbs); solo el auto de arriba (`car-clear`) puede moverse.
Cuatro tipos de acción: curb→curb, curb→detrás-de-auto, auto→curb, auto→detrás-de-auto.

### Resultados por instancia

| Instancia | Config | SSS nodes | NONE nodes | Ratio | Status |
|---|---|---:|---:|---:|---|
| `instance-1` (potassco) | 12 autos, 7 curbs | 9,086\* | 691,234\* | **76×** | TIMEOUT/TIMEOUT |
| `gen-4c6a` (generada) | 6 autos, 4 curbs | 27,805 | 27,805 | 1× | OK/OK |
| `gen-7c6a` (generada) | 6 autos, 7 curbs | 118,293\* | 117,881\* | ~1× | TIMEOUT/TIMEOUT |

\* exploración al momento del timeout.

### Análisis del ratio 76× en instance-1

La `instance-1` tiene 12 autos y 7 curbs. Al inspeccionar init vs. goal:

```
Sub-objetivos ya satisfechos en init:
  (at-curb-num car_03 curb_3)   ← car_03 ya en posición correcta
  (behind-car car_10 car_03)    ← car_10 ya en posición correcta
  (behind-car car_07 car_00)    ← car_07 ya en posición correcta (respecto a car_00)
```

3 de 11 sub-objetivos del goal están satisfechos desde el inicio. Esto crea independencia
real: las acciones que afectan a car_03, car_10, o car_06 (ya en su posición goal) pueden
excluirse del stubborn set de los estados donde esos sub-goals siguen satisfechos.

Combinado con la escala grande (12 autos × 7 curbs = enorme espacio de interfoliaciones),
SSS evita explorar la vast mayoría de interfoliaciones equivalentes.

### Por qué las instancias generadas no muestran compresión

#### Experimento 1: gen-4c6a (4 curbs, 6 autos)
```
INIT:  curb_0: car_2 car_5 | curb_1: car_0 car_4 | curb_2: car_3 | curb_3: car_1
       (todos los curbs ocupados; cero curbs libres)
GOAL:  curb_0: car_0 | curb_1: car_1 car_5 | curb_2: car_2 | curb_3: car_3
       Sub-objetivos ya satisfechos en init: NINGUNO
```
Con todos los curbs ocupados y sin sub-goals pre-satisfechos, el grafo de dependencias de SSS
conecta todas las acciones. Resultado: ratio 1×.

#### Experimento 2: gen-7c6a (7 curbs, 6 autos) — hipótesis "curbs vacíos"
Se intentó aumentar el espacio de maniobra añadiendo curbs libres (curb_5 y curb_6 vacíos
en init). Sin embargo, el ratio siguió siendo ~1×.

**Conclusión del experimento**: los curbs vacíos no son suficientes. Lo que importa es que
algún sub-objetivo esté pre-satisfecho en el init (que algún auto ya esté en su posición goal).
En ambas instancias generadas, el generador asigna un goal donde NINGÚN auto está en su
posición inicial.

**Verificación pendiente**: generar una instancia con el mismo generador pero usando una
semilla que produzca algunos autos ya en posición goal, y comprobar si SSS entonces comprime.

---

## Barman (`ipc-2011 barman`)

### Dominio
Robot barman que prepara cócteles: vierte ingredientes en el shaker, agita, y sirve en shots.
Recurso compartido crítico: un único shaker que debe limpiarse entre usos.

### Resultados por instancia

| Instancia | Config | SSS nodes | NONE nodes | Ratio | Status |
|---|---|---:|---:|---:|---|
| `instance-1` (potassco) | 3 cócteles, 3 ing, 4 shots | 484,603\* | 963,473\* | **2×** | TIMEOUT/TIMEOUT |
| `gen-1c2i2s` (generada) | 1 cóctel, 2 ing, 2 shots | 5,148 | 5,148 | 1× | OK/OK |
| `gen-2c2i3s` (generada) | 2 cócteles, 2 ing, 3 shots | 108,591 | 108,591 | 1× | OK/OK |

### Análisis del ratio 2× en instance-1

Con 3 cócteles, 3 ingredientes, y 4 shots disponibles, existen múltiples formas de
interfoliar la preparación: mientras un shot ya servido espera, la secuencia de preparación
del siguiente cóctel puede comenzar. SSS identifica que ciertos bloques de acciones
(preparar-cocktail-1 y preparar-cocktail-2) conmutan en determinados sub-estados, y evita
explorar ambas ordenaciones.

El ratio 2× (modesto comparado con parking o woodworking) refleja que el shaker compartido
serializa gran parte de la preparación; la independencia solo emerge en sub-pasos específicos.

### Por qué las instancias generadas no muestran compresión

#### Experimento 1: gen-1c2i2s (hipótesis: "1 track secuencial")
Con 1 solo cóctel, existe una única cadena lineal de acciones sin ramificaciones independientes.
SSS incluye todo el espacio en el stubborn set. Ratio 1×. ✓ Hipótesis confirmada.

#### Experimento 2: gen-2c2i3s (hipótesis: "2 cócteles → 2 tracks independientes")
Con 2 cócteles y los mismos 2 ingredientes, se esperaba que la preparación de cocktail_1 y
cocktail_2 resultara parcialmente independiente. Sin embargo, el resultado fue SSS=108,591 = NONE.

**Diagnóstico**: los 2 cócteles comparten exactamente los mismos 2 dispensers e ingredientes.
Todo paso de preparación de cocktail_1 compite con el equivalente de cocktail_2 por los mismos
recursos (shaker, dispensers, ingredient1, ingredient2). No existe independencia estructural real.

**Contraste con instance-1**: con 3 cócteles, 3 ingredients y 4 shots, hay combinaciones donde
algunos shots y dispensers no se solapan entre dos cadenas de preparación, creando los sub-estados
donde SSS puede encontrar acciones que conmutan.

**Conclusión**: la condición no es solo "múltiples cócteles" sino "múltiples cócteles con
suficiente diversidad de recursos" y a escala suficientemente grande para que el espacio de
interfoliaciones sea significativo. Las instancias generadas son demasiado pequeñas y uniformes.

---

## Woodworking (`ipc-2008 woodworking`)

### Dominio
Fábrica de muebles: cortar, lijar, barnizar y tratar tablones de madera en distintas máquinas.
Múltiples piezas (`part`) procesables de forma independiente.

### Resultados

| Instancia | Config | SSS nodes | NONE nodes | Ratio | Status |
|---|---|---:|---:|---:|---|
| `instance-1` (potassco) | 3 piezas, 2 tablones | 852 | 16,875 | **20×** | OK/OK |

### Análisis

Cada pieza de madera (p0, p1, p2) pasa por una cadena de operaciones de maquinado independiente.
Procesar p0 en el spray-varnisher no afecta las precondiciones ni efectos de procesar p1 en el
glazer. SSS identifica estas cadenas como sub-tareas independientes y explora una única
interfoliación representativa de las 3!, eliminando el 94% del espacio de estados NONE.

**Nota**: este es el dominio con el ratio más alto entre los que completan dentro del timeout.
La combinación de independencia estructural clara (piezas sin recursos compartidos entre sí)
y tamaño moderado produce la mayor compresión observable.

---

## Movie (`ipc-1998 movie`)

### Dominio
Preparar snacks para una película: conseguir chips, dip, pop, cheese, crackers; rebobinar cassette.

### Resultados

| Instancia | Config | SSS nodes | NONE nodes | Ratio | Status |
|---|---|---:|---:|---:|---|
| `instance-1` | 5 items × 5 categorías | 10 | 128 | **13×** | OK |

### Análisis

El goal requiere `have-chips` (cualquier chip), `have-dip` (cualquier dip), etc. Hay 5 chips
intercambiables (c1–c5): el goal no distingue cuál se elige. SSS reconoce que `get(c1)`,
`get(c2)`, ..., `get(c5)` son simétricas respecto al goal y explora solo una. Análogamente
para cada categoría.

El resultado extremo (10 vs 128) refleja que casi todo el espacio NONE corresponde a ordenaciones
equivalentes de elecciones simétricas, mientras SSS selecciona una única traza representativa.

---

_Documento en construcción — se irán agregando más familias según avancen los experimentos._
