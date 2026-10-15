import java.io.Serializable;

public class Transaccion implements Serializable {
    private int idTransaccion;
    private String origen;
    private double monto;
    private long timestamp;

    public Transaccion(int idTransaccion, String origen, double monto, long timestamp) {
        this.idTransaccion = idTransaccion;
        this.origen = origen;
        this.monto = monto;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public int getIdTransaccion() { return idTransaccion; }
    public String getOrigen() { return origen; }
    public double getMonto() { return monto; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id=" + idTransaccion +
                ", origen='" + origen + '\'' +
                ", monto=" + monto +
                ", timestamp=" + timestamp +
                '}';
    }
}