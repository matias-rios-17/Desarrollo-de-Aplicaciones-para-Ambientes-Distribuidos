import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteTCP {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;
    private static final int CANTIDAD_TRANSACCIONES = 1000;

    public static void main(String[] args) {
        // Generar 1000 transacciones simuladas
        List<Transaccion> transacciones = generarTransacciones(CANTIDAD_TRANSACCIONES);

        try (Socket socket = new Socket(HOST, PUERTO);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Conectado al servidor en " + HOST + ":" + PUERTO);

            // ==========================================
            // PRUEBA 1: FORMATO JSON
            // ==========================================
            long inicioJson = System.nanoTime();
            long bytesTotalesJson = 0;

            dos.writeInt(transacciones.size()); // Enviar cantidad a procesar

            for (Transaccion t : transacciones) {
                String json = ParserMensajes.aJson(t);
                byte[] jsonBytes = json.getBytes();
                bytesTotalesJson += jsonBytes.length;
                dos.writeUTF(json);
            }
            dos.flush();
            long finJson = System.nanoTime();
            double tiempoJsonMs = (finJson - inicioJson) / 1e6;

            // ==========================================
            // PRUEBA 2: FORMATO BINARIO DIRECTO
            // ==========================================
            long inicioBinario = System.nanoTime();
            long bytesTotalesBinario = 0;

            dos.writeInt(transacciones.size()); // Enviar cantidad a procesar

            for (Transaccion t : transacciones) {
                byte[] bytesBinarios = ParserMensajes.aBinario(t);
                bytesTotalesBinario += bytesBinarios.length;
                dos.writeInt(bytesBinarios.length); // Enviar tamaño del arreglo
                dos.write(bytesBinarios);
            }
            dos.flush();
            long finBinario = System.nanoTime();
            double tiempoBinarioMs = (finBinario - inicioBinario) / 1e6;

            // ==========================================
            // RESULTADOS
            // ==========================================
            System.out.println("\n--- RESULTADOS DE EVALUACIÓN DE DESEMPEÑO (OVERHEAD) ---");
            System.out.println("1. Carga útil transmitida:");
            System.out.println("   - JSON:    " + bytesTotalesJson + " bytes");
            System.out.println("   - Binario: " + bytesTotalesBinario + " bytes");
            System.out.printf("   - Reducción de tamaño: %.2f%%\n",
                    (1 - ((double) bytesTotalesBinario / bytesTotalesJson)) * 100);

            System.out.println("\n2. Tiempo total (Serialización + Envío):");
            System.out.printf("   - JSON:    %.2f ms\n", tiempoJsonMs);
            System.out.printf("   - Binario: %.2f ms\n", tiempoBinarioMs);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Transaccion> generarTransacciones(int cantidad) {
        List<Transaccion> lista = new ArrayList<>();
        long timestampActual = System.currentTimeMillis();

        for (int i = 1; i <= cantidad; i++) {
            lista.add(new Transaccion(
                    100 + i,
                    "Nodo" + (char) ('A' + (i % 5)),
                    1000.50 + i,
                    timestampActual + i
            ));
        }
        return lista;
    }
}