import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TP3 - Ejercicio 2: Receptor de Alertas y Telemetria (Sockets UDP con Timeout)
 *
 * - Espera datagramas en el puerto 6000 (configurable).
 * - socket.setSoTimeout(5000): si en 5 segundos no llega nada, receive()
 *   lanza SocketTimeoutException; se registra una advertencia y se sigue
 *   escuchando (el receptor NO se cae).
 * - Usa el numero de secuencia de cada alerta para detectar datagramas
 *   perdidos (huecos en la secuencia).
 *
 * Uso: java ReceptorUDP [puerto]
 */
public class ReceptorUDP {

    private static final int TIMEOUT_MS = 5000;
    private static final int TAM_BUFFER = 1024;

    // Ultima secuencia recibida por cada sensor (para detectar perdidas)
    private static final Map<String, Integer> ultimaSecuencia = new ConcurrentHashMap<>();

    private static volatile int recibidos = 0;
    private static volatile int timeouts = 0;
    private static volatile int perdidosDetectados = 0;

    public static void main(String[] args) {

        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 6000;

        // Al cerrar con Ctrl+C se muestra un resumen
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("========== RESUMEN DEL RECEPTOR ==========");
            System.out.println("Datagramas recibidos:        " + recibidos);
            System.out.println("Timeouts (5 s sin datos):    " + timeouts);
            System.out.println("Datagramas perdidos (detect): " + perdidosDetectados);
            System.out.println("==========================================");
        }));

        try (DatagramSocket socket = new DatagramSocket(puerto)) {

            // Tiempo limite de espera para receive()
            socket.setSoTimeout(TIMEOUT_MS);

            log("Receptor UDP escuchando en el puerto " + puerto
                    + " (timeout = " + TIMEOUT_MS + " ms)");

            byte[] buffer = new byte[TAM_BUFFER];

            while (true) {

                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

                try {
                    // Bloquea hasta que llegue un datagrama o pasen 5 segundos
                    socket.receive(paquete);

                    recibidos++;
                    String mensaje = new String(paquete.getData(), 0, paquete.getLength(),
                            StandardCharsets.UTF_8);

                    log("Recibido de " + paquete.getAddress().getHostAddress() + ":" + paquete.getPort()
                            + " (" + paquete.getLength() + " bytes) -> " + mensaje);

                    verificarSecuencia(mensaje);

                } catch (SocketTimeoutException e) {
                    // No llego nada en 5 s: se avisa y se vuelve a escuchar
                    timeouts++;
                    log("[ADVERTENCIA] No se recibieron datagramas en los ultimos "
                            + (TIMEOUT_MS / 1000) + " s. El emisor puede estar caido o los paquetes"
                            + " se perdieron. Sigo escuchando...");
                }
            }

        } catch (SocketException e) {
            log("No se pudo abrir el socket UDP: " + e.getMessage());
        } catch (IOException e) {
            log("Error de E/S: " + e.getMessage());
        }
    }

    /** Compara la secuencia recibida con la esperada para detectar huecos. */
    private static void verificarSecuencia(String mensaje) {

        String[] partes = mensaje.split("\\|");

        if (partes.length < 3 || !partes[0].equals("ALERTA")) {
            return; // mensaje con otro formato: no se controla
        }

        try {
            String sensor = partes[1];
            int secuencia = Integer.parseInt(partes[2]);
            // Las secuencias empiezan en 1: si es el primer mensaje del sensor
            // se toma 0 como anterior (asi tambien se detecta si se perdio el #1)
            Integer anterior = ultimaSecuencia.getOrDefault(sensor, 0);

            if (secuencia > anterior + 1) {
                int perdidos = secuencia - anterior - 1;
                perdidosDetectados += perdidos;
                log("[PERDIDA] " + sensor + ": se esperaba #" + (anterior + 1) + " y llego #" + secuencia
                        + " -> " + perdidos + " datagrama(s) perdido(s)");
            }

            if (secuencia > anterior) {
                ultimaSecuencia.put(sensor, secuencia);
            }

        } catch (NumberFormatException e) {
            log("Secuencia invalida en el mensaje: " + mensaje);
        }
    }

    private static void log(String texto) {
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + hora + "] " + texto);
    }
}
