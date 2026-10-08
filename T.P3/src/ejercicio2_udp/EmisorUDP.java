import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * TP3 - Ejercicio 2: Emisor de Alertas y Telemetria (Sockets UDP)
 *
 * Envia un datagrama de alerta cada cierto intervalo a un receptor UDP.
 * Cada mensaje lleva un numero de secuencia para que el receptor pueda
 * detectar si algun datagrama se perdio en el camino.
 *
 * Opcionalmente puede SIMULAR perdida de paquetes (no envia algunos
 * datagramas a proposito) para probar la deteccion del receptor.
 *
 * Uso: java EmisorUDP [host] [puerto] [intervaloMs] [cantidad] [probPerdida] [idSensor]
 *   cantidad = 0      -> envia sin parar (Ctrl+C para cortar)
 *   probPerdida = 0.3 -> no envia ~30% de los datagramas (simulacion)
 */
public class EmisorUDP {

    public static void main(String[] args) {

        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : 6000;
        long intervalo = args.length > 2 ? Long.parseLong(args[2]) : 2000;
        int cantidad = args.length > 3 ? Integer.parseInt(args[3]) : 0;
        double probPerdida = args.length > 4 ? Double.parseDouble(args[4]) : 0.0;
        String idSensor = args.length > 5 ? args[5] : "SENSOR-01";

        Random random = new Random();
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");

        System.out.println("===== EMISOR UDP DE ALERTAS =====");
        System.out.println("Destino: " + host + ":" + puerto
                + " | Intervalo: " + intervalo + " ms"
                + " | Cantidad: " + (cantidad == 0 ? "infinita" : cantidad)
                + " | Perdida simulada: " + (int) (probPerdida * 100) + "%");

        // UDP no necesita conexion: se crea el socket y se manda directamente
        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress direccion = InetAddress.getByName(host);
            int enviados = 0;
            int descartados = 0;

            for (int secuencia = 1; cantidad == 0 || secuencia <= cantidad; secuencia++) {

                // Telemetria simulada
                double temperatura = 55 + random.nextDouble() * 35;   // 55 a 90 C
                int cpu = 40 + random.nextInt(61);                   // 40 a 100 %
                String nivel = (temperatura > 80 || cpu > 90) ? "CRITICA"
                             : (temperatura > 70 || cpu > 75) ? "ADVERTENCIA"
                             : "INFO";

                String hora = formatoHora.format(new Date());

                // Formato: ALERTA|sensor|secuencia|nivel|temp|cpu|hora
                String mensaje = String.format(Locale.US, "ALERTA|%s|%d|%s|temp=%.1fC|cpu=%d%%|%s",
                        idSensor, secuencia, nivel, temperatura, cpu, hora);

                if (random.nextDouble() < probPerdida) {
                    // Simulacion: el paquete "se pierde en la red" (no se envia)
                    descartados++;
                    System.out.println("[" + hora + "] (simulado) Datagrama #" + secuencia
                            + " PERDIDO en la red, no se envia");
                } else {
                    byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket paquete = new DatagramPacket(datos, datos.length, direccion, puerto);
                    socket.send(paquete);
                    enviados++;
                    System.out.println("[" + hora + "] Enviado #" + secuencia + " (" + datos.length
                            + " bytes): " + mensaje);
                }

                Thread.sleep(intervalo);
            }

            System.out.println("Fin del envio. Enviados: " + enviados + " | Perdidos (simulados): " + descartados);

        } catch (IOException e) {
            System.out.println("Error de E/S: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Emisor interrumpido.");
        }
    }
}
