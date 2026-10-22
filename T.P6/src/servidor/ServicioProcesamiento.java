package servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz remota que define los servicios del sistema distribuido.
 */
public interface ServicioProcesamiento extends Remote {

    // 1. Procesamiento Matemático que retorna el DTO Estadisticas
    Estadisticas calcularEstadisticas(List<Double> numeros) throws RemoteException;

    // 2. Validación de Criterios (CUIT de 11 dígitos)
    boolean validarCuit(String cuit) throws RemoteException;

    // 3. Filtro de Contenido
    List<String> filtrarCadenas(List<String> cadenas, String patron) throws RemoteException;
}