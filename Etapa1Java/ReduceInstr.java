public class ReduceInstr extends Instruccion {
    private final String operacion;

    public ReduceInstr(String operacion) {
        this.operacion = operacion;
    }

    @Override
    public String toIR() {
        return "REDUCE|" + operacion;
    }
}
