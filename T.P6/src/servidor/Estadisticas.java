package servidor;

import java.io.Serializable;

/**
 * Objeto de transferencia de datos (DTO) para los resultados estadísticos.
 * Debe implementar Serializable para poder viajar por la red mediante RMI.
 */
public class Estadisticas implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double promedio;
    private final double maximo;
    private final double minimo;
    private final double desviacionEstandar;

    public Estadisticas(double promedio, double maximo, double minimo, double desviacionEstandar) {
        this.promedio = promedio;
        this.maximo = maximo;
        this.minimo = minimo;
        this.desviacionEstandar = desviacionEstandar;
    }

    public double getPromedio() { return promedio; }
    public double getMaximo() { return maximo; }
    public double getMinimo() { return minimo; }
    public double getDesviacionEstandar() { return desviacionEstandar; }

    @Override
    public String toString() {
        return String.format("Promedio: %.2f | Máximo: %.2f | Mínimo: %.2f | Desviación Estándar: %.2f",
                promedio, maximo, minimo, desviacionEstandar);
    }
}