package ejercicioudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class ReceptorUDP {
    private static final int PUERTO = 6000;
    private static final int TIMEOUT_MS = 5000; // 5 segundos

    public static void main(String[] args) {
        byte[] buffer = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(PUERTO)) {
            // Establecer tiempo de espera de 5 segundos
            socket.setSoTimeout(TIMEOUT_MS);
            System.out.println("Receptor UDP escuchando en el puerto " + PUERTO + " (Timeout: " + TIMEOUT_MS + " ms)...");

            while (true) {
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(paquete); // Bloqueante hasta recibir o agotar timeout
                    String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                    System.out.println("[ALERTA RECIBIDA] Desde " + paquete.getAddress() + ":" + paquete.getPort() + " -> " + mensaje);
                } catch (SocketTimeoutException e) {
                    // Manejo del timeout
                    System.out.println("[ADVERTENCIA] No se recibieron datagramas en los ultimos " + (TIMEOUT_MS / 1000) + " segundos. Reintentando...");
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el receptor UDP: " + e.getMessage());
        }
    }
}