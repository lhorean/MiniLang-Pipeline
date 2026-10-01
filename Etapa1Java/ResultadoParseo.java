import java.util.ArrayList;
import java.util.List;

public class ResultadoParseo {
    private final List<Instruccion> instrucciones;
    private final List<String> errores;

    public ResultadoParseo(List<Instruccion> instrucciones, List<String> errores) {
        this.instrucciones = new ArrayList<Instruccion>(instrucciones);
        this.errores = new ArrayList<String>(errores);
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    public List<Instruccion> getInstrucciones() {
        return new ArrayList<Instruccion>(instrucciones);
    }

    public List<String> getErrores() {
        return new ArrayList<String>(errores);
    }
}
