package cliente;

import servidor.Estadisticas;
import servidor.ServicioProcesamiento;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente con menú interactivo por consola.
 */
public class ClienteRMI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Localiza el registro RMI e invoca la referencia remota
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServicioProcesamiento servicio = (ServicioProcesamiento) registry.lookup("ServicioProcesamiento");

            System.out.println("Conexión exitosa con el Servidor RMI.\n");

            boolean salir = false;
            while (!salir) {
                System.out.println("--- MENÚ SERVICIOS DISTRIBUIDOS ---");
                System.out.println("1. Calcular Estadísticas");
                System.out.println("2. Validar CUIT");
                System.out.println("3. Filtrar Cadenas de Texto");
                System.out.println("4. Salir");
                System.out.print("Seleccione una opción: ");

                String opcion = scanner.nextLine();

                switch (opcion) {
                    case "1":
                        System.out.print("Ingrese números separados por coma (ej: 12, 45, 8, 23.5): ");
                        String[] numsInput = scanner.nextLine().split(",");
                        List<Double> numeros = new ArrayList<>();
                        for (String n : numsInput) {
                            numeros.add(Double.parseDouble(n.trim()));
                        }
                        Estadisticas stats = servicio.calcularEstadisticas(numeros);
                        System.out.println("\n[Resultado Servidor]");
                        System.out.println("  - " + stats + "\n");
                        break;

                    case "2":
                        System.out.print("Ingrese CUIT a validar (11 dígitos, sin guiones): ");
                        String cuit = scanner.nextLine().trim();
                        boolean esValido = servicio.validarCuit(cuit);
                        System.out.println("\n[Resultado Servidor] El CUIT " + cuit + (esValido ? " ES VÁLIDO." : " NO ES VÁLIDO.") + "\n");
                        break;

                    case "3":
                        System.out.print("Ingrese textos separados por coma: ");
                        String[] textosInput = scanner.nextLine().split(",");
                        List<String> cadenas = new ArrayList<>();
                        for (String t : textosInput) {
                            cadenas.add(t.trim());
                        }
                        System.out.print("Ingrese patrón/subcadena a buscar: ");
                        String patron = scanner.nextLine().trim();
                        List<String> filtradas = servicio.filtrarCadenas(cadenas, patron);
                        System.out.println("\n[Resultado Servidor] Coincidencias: " + filtradas + "\n");
                        break;

                    case "4":
                        salir = true;
                        System.out.println("Cerrando sesión del cliente...");
                        break;

                    default:
                        System.out.println("Opción inválida, intente de nuevo.\n");
                }
            }
        } catch (java.rmi.ConnectException e) {
            System.err.println("Error de conexión: No se pudo contactar al servidor RMI.");
        } catch (Exception e) {
            System.err.println("Excepción en ClienteRMI: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}