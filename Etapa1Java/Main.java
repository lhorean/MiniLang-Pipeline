import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String archivoEntrada = "programa.mini";
        String archivoSalida = "programa.ir";

        ParserMini parser = new ParserMini();
        GeneradorIR generador = new GeneradorIR();

        try {
            ResultadoParseo resultado = parser.parsear(archivoEntrada);

            if (resultado.tieneErrores()) {
                Files.deleteIfExists(Paths.get(archivoSalida));

                for (String error : resultado.getErrores()) {
                    System.out.println(error);
                }

                System.out.println("No se genero " + archivoSalida + " porque el programa contiene errores.");
                return;
            }

            generador.generar(resultado.getInstrucciones(), archivoSalida);
            System.out.println("Archivo " + archivoSalida + " generado correctamente.");
        } catch (IOException error) {
            System.out.println("Error al leer o escribir archivos: " + error.getMessage());
        }
    }
}
