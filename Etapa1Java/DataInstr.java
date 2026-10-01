import java.util.ArrayList;
import java.util.List;

public class DataInstr extends Instruccion {
    private final List<Integer> numeros;

    public DataInstr(List<Integer> numeros) {
        this.numeros = new ArrayList<Integer>(numeros);
    }

    @Override
    public String toIR() {
        StringBuilder resultado = new StringBuilder("DATA|");

        for (int i = 0; i < numeros.size(); i++) {
            if (i > 0) {
                resultado.append(",");
            }
            resultado.append(numeros.get(i));
        }

        return resultado.toString();
    }
}
