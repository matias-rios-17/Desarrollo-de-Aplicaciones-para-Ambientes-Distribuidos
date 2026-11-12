package servidor;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

// Levanta el servidor HTTP en el puerto 8080
public class ServidorREST {
    public static final int PUERTO = 8080;

    public static void main(String[] args) throws Exception {
        RepositorioProductos repo = new RepositorioProductos();
        HttpServer server = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        server.createContext("/productos", new ProductoHandler(repo));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("[SERVIDOR] API REST escuchando en http://localhost:" + PUERTO + "/productos");
    }
}