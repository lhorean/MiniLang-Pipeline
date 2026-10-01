public class MapInstr extends Instruccion {
    private final String operador;
    private final int numero;

    public MapInstr(String operador, int numero) {
        this.operador = operador;
        this.numero = numero;
    }

    @Override
    public String toIR() {
        return "MAP|" + operador + "|" + numero;
    }
}
