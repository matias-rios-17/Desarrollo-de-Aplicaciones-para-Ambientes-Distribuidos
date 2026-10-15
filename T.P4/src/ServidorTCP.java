import java.io.*;
import java.net.*;

public class ServidorTCP {
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado. Esperando conexiones en el puerto " + PUERTO + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\nCliente conectado desde: " + socket.getInetAddress());
                procesarCliente(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void procesarCliente(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            
            // 1. Recepción en Formato JSON
            int cantidadJson = dis.readInt();
            long bytesJsonRecibidos = 0;

            for (int i = 0; i < cantidadJson; i++) {
                String json = dis.readUTF();
                bytesJsonRecibidos += json.getBytes().length;
                Transaccion t = ParserMensajes.desdeJson(json);
            }
            System.out.println("[JSON] Ráfaga procesada exitosamente.");
            System.out.println("[JSON] Bytes totales de carga útil recibida: " + bytesJsonRecibidos + " bytes");

            // 2. Recepción en Formato Binario
            int cantidadBinario = dis.readInt();
            long bytesBinarioRecibidos = 0;

            for (int i = 0; i < cantidadBinario; i++) {
                int tamano = dis.readInt();
                byte[] buffer = new byte[tamano];
                dis.readFully(buffer);
                bytesBinarioRecibidos += tamano;
                Transaccion t = ParserMensajes.desdeBinario(buffer);
            }
            System.out.println("[BINARIO] Ráfaga procesada exitosamente.");
            System.out.println("[BINARIO] Bytes totales de carga útil recibida: " + bytesBinarioRecibidos + " bytes");

        } catch (IOException e) {
            System.err.println("Error procesando cliente: " + e.getMessage());
        }
    }
}