package servidor;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Servidor REST del TP7, adaptado para el TP10 (pruebas de carga).
 *
 * Uso:
 *   java -cp out servidor.ServidorREST                      (versión sin optimizar)
 *   java -cp out servidor.ServidorREST --hilos 32           (versión optimizada)
 *   java -cp out servidor.ServidorREST --hilos 32 --demora 20
 *
 * --hilos N   N <= 1 o sin indicar: setExecutor(null) -> el JDK atiende todos
 *             los pedidos con UN solo hilo despachador (como en el TP7).
 *             N > 1: pool fijo de N hilos (Executors.newFixedThreadPool).
 * --demora MS demora simulada por pedido (acceso a BD/disco). Por defecto 20 ms.
 */
public class ServidorREST {

    private static final int PUERTO = 8080;

    public static void main(String[] args) throws IOException {
        int hilos = 1;
        int demoraMs = 20;
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--hilos" -> hilos = Integer.parseInt(args[++i]);
                    case "--demora" -> demoraMs = Integer.parseInt(args[++i]);
                    default -> throw new IllegalArgumentException("Argumento desconocido: " + args[i]);
                }
            }
            if (demoraMs < 0) throw new IllegalArgumentException("La demora no puede ser negativa");
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            System.out.println("Error en los argumentos: " + e.getMessage());
            System.out.println("Uso: java -cp out servidor.ServidorREST [--hilos N] [--demora MS]");
            return;
        }

        HttpServer server;
        try {
            // backlog 1024: cola de conexiones pendientes del socket
            server = HttpServer.create(new InetSocketAddress(PUERTO), 1024);
        } catch (BindException e) {
            System.out.println("El puerto " + PUERTO + " ya está en uso. ¿Hay otro servidor levantado?");
            return;
        }

        RepositorioProductos repo = new RepositorioProductos();
        server.createContext("/productos", new ProductoHandler(repo, demoraMs));

        String modo;
        if (hilos <= 1) {
            server.setExecutor(null);   // hilo despachador único del JDK
            modo = "SIN OPTIMIZAR (1 hilo despachador)";
            hilos = 1;
        } else {
            server.setExecutor(Executors.newFixedThreadPool(hilos));
            modo = "OPTIMIZADA (pool de " + hilos + " hilos)";
        }

        server.start();
        System.out.println("=== Servidor REST TP10 escuchando en http://localhost:" + PUERTO + "/productos ===");
        System.out.println("Modo: " + modo + " | hilos=" + hilos + " | demora simulada=" + demoraMs + " ms/pedido");
        System.out.println("Productos iniciales: " + repo.cantidad() + " | PID: " + ProcessHandle.current().pid());
        if (demoraMs > 0) {
            System.out.printf("Capacidad teórica por hilo: ~%.0f req/s (1000 / %d ms)%n", 1000.0 / demoraMs, demoraMs);
        }
        System.out.println("Ctrl + C para detener.");
    }
}
