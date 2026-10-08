import java.io.*;
import java.net.*;

public class Servidor {

    private static final int PUERTO = 5500;
    private static int cantidadPeticiones = 0;

    public static void main(String[] args) {

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println("===== SERVIDOR RESILIENTE =====");
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            System.out.println("Esperando conexiones...");

            while (true) {

                try (Socket socket = servidor.accept();
                     BufferedReader entrada = new BufferedReader(
                             new InputStreamReader(socket.getInputStream()));
                     PrintWriter salida = new PrintWriter(
                             socket.getOutputStream(), true)) {

                    cantidadPeticiones++;

                    String mensaje = entrada.readLine();

                    System.out.println("\nPetición recibida #" + cantidadPeticiones);
                    System.out.println("Mensaje: " + mensaje);

                    // Simular error en las primeras 2 peticiones
                    if (cantidadPeticiones <= 2) {

                        System.out.println("ERROR SIMULADO DEL SERVIDOR");

                        // Cerramos la conexión sin enviar respuesta
                        socket.close();

                    } else {

                        System.out.println("Petición procesada correctamente.");

                        salida.println("OK - Petición procesada correctamente");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}