package ejercicioudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EmisorUDP {
    private static final String SERVER_HOST = "localhost";
    private static final int PUERTO = 6000;

    public static void main(String[] args) {
        int contadorAlertas = 1;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress direccionServidor = InetAddress.getByName(SERVER_HOST);

            while (true) {
                String mensaje = "Alerta de Telemetria N° " + contadorAlertas + " | Nivel de Bateria: " + (int)(Math.random() * 100) + "%";
                byte[] buffer = mensaje.getBytes();

                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, direccionServidor, PUERTO);
                socket.send(paquete);

                System.out.println("Enviado: " + mensaje);
                contadorAlertas++;

                // Simular envio cada 7 segundos para comprobar la activacion del Timeout en el receptor
                Thread.sleep(7000); 
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error en el emisor UDP: " + e.getMessage());
        }
    }
}