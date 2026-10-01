from functools import reduce
from pathlib import Path


class ErrorIR(Exception):
    pass


def formatear_lista(datos):
    return str(datos)


def aplicar_filter(datos, comparador, numero):
    comparadores = {
        ">": lambda valor: valor > numero,
        "<": lambda valor: valor < numero,
        ">=": lambda valor: valor >= numero,
        "<=": lambda valor: valor <= numero,
        "==": lambda valor: valor == numero,
    }

    if comparador not in comparadores:
        raise ErrorIR("Comparador no reconocido: " + comparador)

    return list(filter(comparadores[comparador], datos))


def aplicar_map(datos, operador, numero):
    operadores = {
        "+": lambda valor: valor + numero,
        "-": lambda valor: valor - numero,
        "*": lambda valor: valor * numero,
    }

    if operador not in operadores:
        raise ErrorIR("Operador no reconocido: " + operador)

    return list(map(operadores[operador], datos))


def aplicar_reduce(datos, operacion):
    if operacion == "SUM":
        return reduce(lambda acumulado, valor: acumulado + valor, datos, 0)

    if len(datos) == 0:
        raise ErrorIR("No se puede aplicar REDUCE " + operacion + " a una lista vacia.")

    if operacion == "MAX":
        return reduce(lambda mayor, valor: mayor if mayor > valor else valor, datos)

    if operacion == "MIN":
        return reduce(lambda menor, valor: menor if menor < valor else valor, datos)

    raise ErrorIR("Operacion REDUCE no reconocida: " + operacion)


def leer_numero(texto):
    try:
        return int(texto)
    except ValueError:
        raise ErrorIR("Numero invalido en IR: " + texto)


def leer_data(partes):
    if len(partes) != 2 or partes[1] == "":
        raise ErrorIR("DATA mal formado en IR.")

    return list(map(leer_numero, partes[1].split(",")))


def generar_mips_input(ruta_mips_input, resultado_final, operaciones, status):
    estado_ok = status == "OK" and type(resultado_final) is int

    if estado_ok:
        lineas = [str(resultado_final), str(operaciones), "1"]
    else:
        lineas = ["0", str(operaciones), "0"]

    Path(ruta_mips_input).write_text("\n".join(lineas) + "\n", encoding="utf-8")


def ejecutar_programa(ruta_ir, ruta_resultado, ruta_mips_input=None):
    datos = []
    resultado_final = None
    operaciones = 0
    trazas = []
    status = "OK"
    error = ""

    try:
        lineas = Path(ruta_ir).read_text(encoding="utf-8").splitlines()

        for numero_linea, linea in enumerate(lineas, start=1):
            if linea.strip() == "":
                continue

            partes = linea.strip().split("|")
            instruccion = partes[0]

            if instruccion == "DATA":
                datos = leer_data(partes)
                resultado_final = datos
            elif instruccion == "FILTER":
                if len(partes) != 3:
                    raise ErrorIR("Linea " + str(numero_linea) + ": FILTER mal formado en IR.")
                numero = leer_numero(partes[2])
                datos = aplicar_filter(datos, partes[1], numero)
                resultado_final = datos
                operaciones += 1
                trazas.append("FILTER " + partes[1] + " " + str(numero) + " => " + formatear_lista(datos))
            elif instruccion == "MAP":
                if len(partes) != 3:
                    raise ErrorIR("Linea " + str(numero_linea) + ": MAP mal formado en IR.")
                numero = leer_numero(partes[2])
                datos = aplicar_map(datos, partes[1], numero)
                resultado_final = datos
                operaciones += 1
                trazas.append("MAP " + partes[1] + " " + str(numero) + " => " + formatear_lista(datos))
            elif instruccion == "REDUCE":
                if len(partes) != 2:
                    raise ErrorIR("Linea " + str(numero_linea) + ": REDUCE mal formado en IR.")
                try:
                    resultado_final = aplicar_reduce(datos, partes[1])
                    operaciones += 1
                    trazas.append("REDUCE " + partes[1] + " => " + str(resultado_final))
                except ErrorIR as detalle:
                    trazas.append("REDUCE " + partes[1] + " => ERROR")
                    raise detalle
            elif instruccion == "PRINT":
                if len(partes) != 1:
                    raise ErrorIR("Linea " + str(numero_linea) + ": PRINT mal formado en IR.")
            else:
                raise ErrorIR("Linea " + str(numero_linea) + ": instruccion desconocida en IR: " + instruccion)
    except ErrorIR as detalle:
        status = "ERROR"
        error = str(detalle)

    contenido = list(trazas)
    contenido.append("RESULT=" + ("" if status == "ERROR" else str(resultado_final)))
    contenido.append("OPERATIONS=" + str(operaciones))
    contenido.append("STATUS=" + status)

    if status == "ERROR":
        contenido.append("ERROR=" + error)

    Path(ruta_resultado).write_text("\n".join(contenido) + "\n", encoding="utf-8")

    if ruta_mips_input is not None:
        generar_mips_input(ruta_mips_input, resultado_final, operaciones, status)


def main():
    carpeta_actual = Path(__file__).resolve().parent
    raiz_proyecto = carpeta_actual.parent
    ruta_ir = raiz_proyecto / "programa.ir"
    ruta_resultado = raiz_proyecto / "resultado.txt"
    ruta_mips_input = raiz_proyecto / "mips_input.txt"
    ejecutar_programa(ruta_ir, ruta_resultado, ruta_mips_input)


if __name__ == "__main__":
    main()

