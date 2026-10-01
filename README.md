# MiniLang Pipeline

Proyecto práctico de **EIF400 – Paradigmas de Programación**.

## Flujo general

```text
programa.mini
  -> Java
  -> programa.ir
  -> Python
  -> resultado.txt / mips_input.txt
  -> MIPS
  -> firma.txt
```

## Objetivo

Leer un programa escrito en un mini lenguaje, validarlo, convertirlo a una representación intermedia, ejecutar sus transformaciones con estilo funcional y generar una firma de verificación en MIPS.

## Estructura

```text
MiniLang-Pipeline/
├── Etapa1Java/
│   ├── DataInstr.java
│   ├── FilterInstr.java
│   ├── GeneradorIR.java
│   ├── Instruccion.java
│   ├── Main.java
│   ├── MapInstr.java
│   ├── ParserMini.java
│   ├── PrintInstr.java
│   ├── ReduceInstr.java
│   └── ResultadoParseo.java
├── Etapa2Python/
│   └── ejecutor.py
├── Etapa3MIPS/
│   └── firma.asm
├── pruebas/
├── programa.mini
├── programa.ir
├── resultado.txt
├── mips_input.txt
└── firma.txt
```

## Gramática

```text
<programa> ::= <data> <operacion> { <operacion> } "PRINT"
<data> ::= "DATA" <numero> { <numero> }
<operacion> ::= <filter> | <map> | <reduce>
<filter> ::= "FILTER" <comparador> <numero>
<map> ::= "MAP" <aritmetico> <numero>
<reduce> ::= "REDUCE" ("SUM" | "MAX" | "MIN")
<comparador> ::= ">" | "<" | ">=" | "<=" | "=="
<aritmetico> ::= "+" | "-" | "*"
<numero> ::= entero no negativo
```

Ejemplo:

```text
DATA 3 8 5 10 12
FILTER > 5
MAP * 2
REDUCE SUM
PRINT
```

## Etapa 1 – Java

Java lee `programa.mini`, valida la gramática, reporta errores con número de línea y genera `programa.ir` únicamente si todo el programa es válido.

La solución demuestra orientación a objetos con una clase abstracta `Instruccion` y las clases `DataInstr`, `FilterInstr`, `MapInstr`, `ReduceInstr` y `PrintInstr`.

El polimorfismo se observa cuando `GeneradorIR` recorre una `List<Instruccion>` y ejecuta `toIR()` sin preguntar por la clase concreta.

Ejemplo de `programa.ir`:

```text
DATA|3,8,5,10,12
FILTER|>|5
MAP|*|2
REDUCE|SUM
PRINT
```

## Etapa 2 – Python

Python lee `programa.ir` y ejecuta:

- `FILTER` con `filter()`.
- `MAP` con `map()`.
- `REDUCE` con `functools.reduce()`.

Ejemplo de `resultado.txt`:

```text
FILTER > 5 => [8, 10, 12]
MAP * 2 => [16, 20, 24]
REDUCE SUM => 60
RESULT=60
OPERATIONS=3
STATUS=OK
```

También genera `mips_input.txt`:

```text
60
3
1
```

La primera línea representa el resultado, la segunda la cantidad de operaciones y la tercera el estado (`1` = OK, `0` = ERROR).

## Etapa 3 – MIPS

MIPS lee `mips_input.txt` y calcula:

```text
checksum = resultado
checksum = checksum XOR operaciones
checksum = checksum + 17
```

Para `RESULT=60` y `OPERATIONS=3`:

```text
60 XOR 3 = 63
63 + 17 = 80
```

La implementación usa registros, memoria, ciclo, operación lógica, operación aritmética y salto condicional.

La ejecución fue verificada en **MARS 4.5** y produjo:

```text
RESULT=60
OPERATIONS=3
CHECKSUM=80
STATUS=OK
```

El código incluye soporte para resultados negativos y usa `.align 2` para mantener alineadas a 4 bytes las áreas utilizadas por `lw` y `sw`.

## Ejecución

### Java

Desde la raíz del proyecto:

```powershell
javac -d build Etapa1Java\*.java
java -cp build Main
```

Debe generarse `programa.ir`.

Guardar `programa.mini` en UTF-8 sin BOM.

### Python

```powershell
python Etapa2Python\ejecutor.py
```

Debe generar `resultado.txt` y `mips_input.txt`.

### MIPS

Abrir `Etapa3MIPS/firma.asm` con **MARS 4.5**.

En MARS:

```text
Run -> Assemble
Run -> Go
```

`mips_input.txt` debe estar disponible en el directorio de trabajo usado por MARS.

Debe generarse `firma.txt`.

## Caso válido

```text
DATA 3 8 5 10 12
FILTER > 5
MAP * 2
REDUCE SUM
PRINT
```

Resultado final:

```text
RESULT=60
OPERATIONS=3
CHECKSUM=80
STATUS=OK
```

## Caso de error

```text
DATA 1 2 3
FILTER != 2
PRINT
```

Java rechaza el programa, no genera un nuevo `programa.ir` y el pipeline se detiene antes de Python.

## Casos de prueba

Se probaron programas válidos, operador inválido, ausencia de DATA, REDUCE MAX, REDUCE MIN, lista vacía después de FILTER, operaciones consecutivas, números inválidos, instrucciones después de PRINT, dos DATA, argumentos extra, resultados negativos y corte correcto del pipeline.

## Contratos

### Java -> Python
`programa.ir`

### Python -> salida legible
`resultado.txt`

### Python -> MIPS
`mips_input.txt`

### MIPS -> salida final
`firma.txt`

## Tecnologías

- Java
- Python 3
- MIPS
- MARS 4.5
- Visual Studio Code

## Integrante

Gaudy Montero