package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Publica el servicio en el registro RMI.
 */
public class ServidorRMI {
    public static void main(String[] args) {
        try {
            int puerto = 1099;
            Registry registry = LocateRegistry.createRegistry(puerto);

            ServicioProcesamiento servicio = new ServicioProcesamientoImpl();
            registry.rebind("ServicioProcesamiento", servicio);

            System.out.println("==================================================");
            System.out.println(" Servidor RMI activo escuchando en el puerto " + puerto);
            System.out.println(" Servicio 'ServicioProcesamiento' publicado.");
            System.out.println(" Esperando solicitudes de clientes...");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("Error al iniciar ServidorRMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}