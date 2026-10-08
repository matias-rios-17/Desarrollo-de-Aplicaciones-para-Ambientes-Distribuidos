import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * TP3 - Ejercicio 1: Cliente del Chat Multihilo (Sockets TCP)
 *
 * Usa dos hilos:
 *  - Un hilo "Receptor" que escucha todo lo que manda el servidor
 *    y lo muestra en pantalla en tiempo real.
 *  - El hilo main, que lee lo que escribe el usuario y lo envia.
 *
 * Uso: java ClienteChat [host] [puerto]
 */
public class ClienteChat {

    private static final String HOST_POR_DEFECTO = "localhost";
    private static final int PUERTO_POR_DEFECTO = 5000;

    public static void main(String[] args) {

        String host = HOST_POR_DEFECTO;
        int puerto = PUERTO_POR_DEFECTO;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            puerto = Integer.parseInt(args[1]);
        }

        System.out.println("===== CLIENTE DE CHAT TCP =====");
        System.out.println("Conectando a " + host + ":" + puerto + "...");

        try (
            Socket socket = new Socket(host, puerto);

            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );

            PrintWriter salida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true
            );

            Scanner teclado = new Scanner(System.in)
        ) {

            System.out.println("Conectado! (escribi /salir para terminar)");

            // Hilo que recibe los mensajes del servidor en tiempo real
            Thread receptor = new Thread(() -> {
                try {
                    String linea;
                    while ((linea = entrada.readLine()) != null) {
                        System.out.println(linea);
                    }
                    System.out.println("[Cliente] El servidor cerro la conexion.");
                } catch (IOException e) {
                    // El socket se cerro (por /salir o porque se cayo el servidor)
                    System.out.println("[Cliente] Conexion cerrada.");
                }
            }, "Receptor");

            receptor.setDaemon(true);
            receptor.start();

            // Hilo main: leer del teclado y enviar al servidor
            while (teclado.hasNextLine()) {

                String mensaje = teclado.nextLine();
                salida.println(mensaje);

                if (mensaje.trim().equalsIgnoreCase("/salir")) {
                    break;
                }
            }

            // Esperar un momento la despedida del servidor
            receptor.join(1000);

        } catch (ConnectException e) {
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de comunicacion: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Cliente finalizado.");
    }
}
