public class FilterInstr extends Instruccion {
    private final String comparador;
    private final int numero;

    public FilterInstr(String comparador, int numero) {
        this.comparador = comparador;
        this.numero = numero;
    }

    @Override
    public String toIR() {
        return "FILTER|" + comparador + "|" + numero;
    }
}
