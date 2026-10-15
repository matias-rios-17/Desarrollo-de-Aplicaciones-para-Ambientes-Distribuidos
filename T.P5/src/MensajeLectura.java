// Mensaje inmutable para garantizar el aislamiento de estado
public final class MensajeLectura {
    private final String idSensor;
    private final double valor;
    private final long timestamp;

    public MensajeLectura(String idSensor, double valor) {
        this.idSensor = idSensor;
        this.valor = valor;
        this.timestamp = System.currentTimeMillis();
    }

    public String getIdSensor() {
        return idSensor;
    }

    public double getValor() {
        return valor;
    }

    public long getTimestamp() {
        return timestamp;
    }
}