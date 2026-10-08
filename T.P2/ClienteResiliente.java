import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class ClienteResiliente {

    private static final int PUERTO = 5500;
    private static final String HOST = "localhost";

    // Tiempo base del backoff
    private static final long BASE = 1000;

    // Maximo de intentos
    private static final int MAX_INTENTOS = 5;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        System.out.println("===== CLIENTE RESILIENTE =====");

        System.out.print("Ingrese el mensaje a enviar: ");
        String mensaje = scanner.nextLine();


        int intentosRealizados = 0;
        boolean exito = false;


        long tiempoInicio = System.currentTimeMillis();


        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {

            intentosRealizados = intento;

            System.out.println("\nIntento " + intento +
                    " de " + MAX_INTENTOS);

            try (
                Socket socket = new Socket(HOST, PUERTO);

                BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );

                PrintWriter salida = new PrintWriter(
                    socket.getOutputStream(),
                    true
                )
            ) {

                // Enviar mensaje
                salida.println(mensaje);

                // Recibir respuesta
                String respuesta = entrada.readLine();

                System.out.println(
                        "Respuesta del servidor: " + respuesta
                );

  
                exito = true;

                break;

            } catch (IOException e) {

                System.out.println(
                        "Error en el intento " + intento
                );

                System.out.println(
                        "Mensaje: " + e.getMessage()
                );

            
                if (intento < MAX_INTENTOS) {


                    long backoff =
                            BASE * (long) Math.pow(2, intento - 1);


                    long jitter = random.nextInt(501);

                    // Tiempo total de espera
                    long tiempoEsperado = backoff + jitter;

                    System.out.println(
                            "Backoff: " + backoff + " ms"
                    );

                    System.out.println(
                            "Jitter: " + jitter + " ms"
                    );

                    System.out.println(
                            "Esperando " + tiempoEsperado +
                            " ms antes del proximo intento..."
                    );

                    try {

                        Thread.sleep(tiempoEsperado);

                    } catch (InterruptedException ie) {

                        Thread.currentThread().interrupt();

                        System.out.println(
                                "La espera fue interrumpida."
                        );

                        break;
                    }
                }
            }
        }



        long tiempoFin = System.currentTimeMillis();

        // Tiempo total transcurrido
        long tiempoTotal = tiempoFin - tiempoInicio;



        System.out.println("\n");
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "       METRICAS DE RESILIENCIA"
        );
        System.out.println(
                "=========================================="
        );

        // Estado final
        if (exito) {

            System.out.println(
                    "Estado final: EXITO"
            );

        } else {

            System.out.println(
                    "Estado final: FALLO DEFINITIVO"
            );
        }

        // Cantidad de intentos
        System.out.println(
                "Intentos realizados: " + intentosRealizados
        );

        // Tiempo total
        System.out.println(
                "Tiempo total transcurrido: "
                        + tiempoTotal + " ms"
        );

        System.out.println(
                "=========================================="
        );

        scanner.close();
    }
}