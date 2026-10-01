import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParserMini {
    public ResultadoParseo parsear(String archivoEntrada) throws IOException {
        List<String> lineas = Files.readAllLines(Paths.get(archivoEntrada));
        List<Instruccion> instrucciones = new ArrayList<Instruccion>();
        List<String> errores = new ArrayList<String>();

        boolean encontroData = false;
        boolean encontroPrint = false;
        int cantidadData = 0;
        int cantidadOperacionesValidas = 0;
        int lineaData = -1;
        int instruccionesNoVacias = 0;

        for (int i = 0; i < lineas.size(); i++) {
            int numeroLinea = i + 1;
            String linea = lineas.get(i).trim();

            if (linea.length() == 0) {
                continue;
            }

            instruccionesNoVacias++;
            String[] partes = linea.split("\\s+");
            String palabraClave = partes[0];

            if (encontroPrint) {
                errores.add("Linea " + numeroLinea + ": no puede haber instrucciones despues de PRINT.");
                continue;
            }

            if (palabraClave.equals("DATA")) {
                cantidadData++;
                encontroData = true;

                if (cantidadData == 1) {
                    lineaData = numeroLinea;
                } else {
                    errores.add("Linea " + numeroLinea + ": solo puede existir un DATA.");
                }

                if (instruccionesNoVacias != 1) {
                    errores.add("Linea " + numeroLinea + ": DATA debe ser la primera instruccion.");
                }

                DataInstr data = parsearData(partes, numeroLinea, errores);
                if (data != null) {
                    instrucciones.add(data);
                }
            } else if (palabraClave.equals("FILTER")) {
                validarDataAntesOperacion(encontroData, numeroLinea, errores);
                FilterInstr filter = parsearFilter(partes, numeroLinea, errores);
                if (filter != null) {
                    instrucciones.add(filter);
                    cantidadOperacionesValidas++;
                }
            } else if (palabraClave.equals("MAP")) {
                validarDataAntesOperacion(encontroData, numeroLinea, errores);
                MapInstr map = parsearMap(partes, numeroLinea, errores);
                if (map != null) {
                    instrucciones.add(map);
                    cantidadOperacionesValidas++;
                }
            } else if (palabraClave.equals("REDUCE")) {
                validarDataAntesOperacion(encontroData, numeroLinea, errores);
                ReduceInstr reduce = parsearReduce(partes, numeroLinea, errores);
                if (reduce != null) {
                    instrucciones.add(reduce);
                    cantidadOperacionesValidas++;
                }
            } else if (palabraClave.equals("PRINT")) {
                if (partes.length != 1) {
                    errores.add("Linea " + numeroLinea + ": PRINT no acepta argumentos.");
                }

                encontroPrint = true;
                instrucciones.add(new PrintInstr());
            } else {
                errores.add("Linea " + numeroLinea + ": instruccion no permitida: " + palabraClave + ".");
            }
        }

        validarEstructuraFinal(
                encontroData,
                encontroPrint,
                cantidadData,
                cantidadOperacionesValidas,
                lineaData,
                errores);

        return new ResultadoParseo(instrucciones, errores);
    }

    private DataInstr parsearData(String[] partes, int numeroLinea, List<String> errores) {
        if (partes.length < 2) {
            errores.add("Linea " + numeroLinea + ": DATA debe contener al menos un numero.");
            return null;
        }

        List<Integer> numeros = new ArrayList<Integer>();
        boolean esValido = true;

        for (int i = 1; i < partes.length; i++) {
            Integer numero = parsearNumero(partes[i], numeroLinea, errores);
            if (numero == null) {
                esValido = false;
            } else {
                numeros.add(numero);
            }
        }

        if (!esValido) {
            return null;
        }

        return new DataInstr(numeros);
    }

    private FilterInstr parsearFilter(String[] partes, int numeroLinea, List<String> errores) {
        if (partes.length != 3) {
            errores.add("Linea " + numeroLinea + ": FILTER debe tener exactamente un comparador y un numero.");
            return null;
        }

        if (!esComparadorValido(partes[1])) {
            errores.add("Linea " + numeroLinea + ": FILTER solo acepta >, <, >=, <= o ==.");
            return null;
        }

        Integer numero = parsearNumero(partes[2], numeroLinea, errores);
        if (numero == null) {
            return null;
        }

        return new FilterInstr(partes[1], numero);
    }

    private MapInstr parsearMap(String[] partes, int numeroLinea, List<String> errores) {
        if (partes.length != 3) {
            errores.add("Linea " + numeroLinea + ": MAP debe tener exactamente un operador y un numero.");
            return null;
        }

        if (!esAritmeticoValido(partes[1])) {
            errores.add("Linea " + numeroLinea + ": MAP solo acepta +, - o *.");
            return null;
        }

        Integer numero = parsearNumero(partes[2], numeroLinea, errores);
        if (numero == null) {
            return null;
        }

        return new MapInstr(partes[1], numero);
    }

    private ReduceInstr parsearReduce(String[] partes, int numeroLinea, List<String> errores) {
        if (partes.length != 2) {
            errores.add("Linea " + numeroLinea + ": REDUCE debe tener exactamente un argumento.");
            return null;
        }

        if (!esReduceValido(partes[1])) {
            errores.add("Linea " + numeroLinea + ": REDUCE solo acepta SUM, MAX o MIN.");
            return null;
        }

        return new ReduceInstr(partes[1]);
    }

    private Integer parsearNumero(String texto, int numeroLinea, List<String> errores) {
        if (!texto.matches("[0-9]+")) {
            errores.add("Linea " + numeroLinea + ": numero invalido: " + texto + ".");
            return null;
        }

        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException error) {
            errores.add("Linea " + numeroLinea + ": numero demasiado grande: " + texto + ".");
            return null;
        }
    }

    private void validarDataAntesOperacion(boolean encontroData, int numeroLinea, List<String> errores) {
        if (!encontroData) {
            errores.add("Linea " + numeroLinea + ": DATA debe ser la primera instruccion.");
        }
    }

    private void validarEstructuraFinal(
            boolean encontroData,
            boolean encontroPrint,
            int cantidadData,
            int cantidadOperacionesValidas,
            int lineaData,
            List<String> errores) {
        if (!encontroData || cantidadData != 1) {
            errores.add("Error: debe existir exactamente un DATA.");
        }

        if (lineaData != -1 && cantidadOperacionesValidas == 0) {
            errores.add("Linea " + lineaData + ": despues de DATA debe existir al menos una operacion.");
        }

        if (!encontroPrint) {
            errores.add("Error: debe existir PRINT.");
        }
    }

    private boolean esComparadorValido(String texto) {
        return texto.equals(">") || texto.equals("<") || texto.equals(">=")
                || texto.equals("<=") || texto.equals("==");
    }

    private boolean esAritmeticoValido(String texto) {
        return texto.equals("+") || texto.equals("-") || texto.equals("*");
    }

    private boolean esReduceValido(String texto) {
        return texto.equals("SUM") || texto.equals("MAX") || texto.equals("MIN");
    }
}
