import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GeneradorIR {
    public void generar(List<Instruccion> instrucciones, String archivoSalida) throws IOException {
        StringBuilder contenido = new StringBuilder();

        for (Instruccion instruccion : instrucciones) {
            contenido.append(instruccion.toIR()).append(System.lineSeparator());
        }

        Files.write(Paths.get(archivoSalida), contenido.toString().getBytes(StandardCharsets.UTF_8));
    }
}
