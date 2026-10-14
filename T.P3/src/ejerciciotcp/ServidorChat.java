package ejerciciotcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ServidorChat {
    private static final int PUERTO = 5000;
    // Set concurrente para gestionar la lista de clientes activos
    private static Set<ManejadorCliente> clientesConectados = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Servidor TCP iniciado en el puerto " + PUERTO);

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente conectado desde: " + socketCliente.getInetAddress().getHostAddress());

                ManejadorCliente cliente = new ManejadorCliente(socketCliente);
                clientesConectados.add(cliente);

                // Asignar un hilo independiente por cliente
                new Thread(cliente).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    // Método para retransmitir mensajes a todos los clientes (Broadcast)
    public static void retransmitirMensaje(String mensaje, ManejadorCliente emisor) {
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente != emisor) { // Evitamos reenviar el mensaje al emisor
                    cliente.enviarMensaje(mensaje);
                }
            }
        }
    }

    // Eliminar un cliente cuando se desconecta
    public static void eliminarCliente(ManejadorCliente cliente) {
        clientesConectados.remove(cliente);
        System.out.println("Cliente desconectado. Clientes restantes: " + clientesConectados.size());
    }
}