package ejerciciotcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nombreCliente;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Leer el nombre/alias del cliente
            out.println("Bienvenido. Ingrese su nombre o alias:");
            this.nombreCliente = in.readLine();
            if (this.nombreCliente == null || this.nombreCliente.trim().isEmpty()) {
                this.nombreCliente = "Anonimo-" + socket.getPort();
            }

            ServidorChat.retransmitirMensaje("--> " + nombreCliente + " se ha unido al chat.", this);

            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                if ("exit".equalsIgnoreCase(mensaje.trim())) {
                    break;
                }
                String mensajeFormateado = nombreCliente + ": " + mensaje;
                System.out.println(mensajeFormateado);
                ServidorChat.retransmitirMensaje(mensajeFormateado, this);
            }
        } catch (IOException e) {
            System.out.println("Error con el cliente " + nombreCliente + ": " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    public void enviarMensaje(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }

    private void desconectar() {
        ServidorChat.eliminarCliente(this);
        ServidorChat.retransmitirMensaje("<-- " + nombreCliente + " ha salido del chat.", this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando socket: " + e.getMessage());
        }
    }
}