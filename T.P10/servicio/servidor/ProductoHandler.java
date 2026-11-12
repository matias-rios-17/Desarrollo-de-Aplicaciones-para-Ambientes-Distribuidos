package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Atiende la ruta /productos (igual que en el TP7):
 *   GET  /productos       -> 200 lista
 *   GET  /productos/{id}  -> 200 producto | 404 no existe | 400 id inválido
 *   POST /productos       -> 201 creado   | 400 JSON inválido
 *   otro método           -> 405
 *   error inesperado      -> 500
 *
 * Agregado para el TP10: una demora simulada (--demora MS) que representa
 * el costo de un acceso a base de datos / disco en cada pedido.
 */
public class ProductoHandler implements HttpHandler {

    private final RepositorioProductos repo;
    private final int demoraMs;

    // Límite de productos guardados: la prueba de carga hace muchos POST y
    // no queremos que el GET /productos crezca sin fin (sesgaría la medición).
    private static final int MAX_PRODUCTOS = 200;

    public ProductoHandler(RepositorioProductos repo, int demoraMs) {
        this.repo = repo;
        this.demoraMs = demoraMs;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            simularAccesoADatos();
            String metodo = ex.getRequestMethod();
            String ruta = ex.getRequestURI().getPath();

            if ("GET".equals(metodo) && (ruta.equals("/productos") || ruta.equals("/productos/"))) {
                listar(ex);
            } else if ("GET".equals(metodo) && ruta.startsWith("/productos/")) {
                buscar(ex, ruta.substring("/productos/".length()));
            } else if ("POST".equals(metodo) && (ruta.equals("/productos") || ruta.equals("/productos/"))) {
                crear(ex);
            } else if (!ruta.startsWith("/productos")) {
                responder(ex, 404, "{\"error\":\"Ruta no encontrada\"}");
            } else {
                responder(ex, 405, "{\"error\":\"Método no permitido\"}");
            }
        } catch (Exception e) {
            responder(ex, 500, "{\"error\":\"Error interno del servidor\"}");
        } finally {
            ex.close();
        }
    }

    /**
     * Simula la latencia de E/S de un acceso a una base de datos o disco.
     * Mientras el hilo duerme NO usa CPU, pero queda ocupado y no puede
     * atender otro pedido: es justamente lo que pasa con E/S bloqueante.
     */
    private void simularAccesoADatos() throws InterruptedException {
        if (demoraMs > 0) Thread.sleep(demoraMs);
    }

    private void listar(HttpExchange ex) throws IOException {
        List<Producto> lista = repo.listar();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(lista.get(i).toJson());
        }
        sb.append(']');
        responder(ex, 200, sb.toString());
    }

    private void buscar(HttpExchange ex, String idTxt) throws IOException {
        int id;
        try {
            id = Integer.parseInt(idTxt);
        } catch (NumberFormatException e) {
            responder(ex, 400, "{\"error\":\"El id debe ser un número\"}");
            return;
        }
        Producto p = repo.buscar(id);
        if (p == null) {
            responder(ex, 404, "{\"error\":\"Producto " + id + " no encontrado\"}");
        } else {
            responder(ex, 200, p.toJson());
        }
    }

    private void crear(HttpExchange ex) throws IOException {
        String cuerpo;
        try (InputStream in = ex.getRequestBody()) {
            cuerpo = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Producto datos;
        try {
            datos = Producto.fromJson(cuerpo);
        } catch (IllegalArgumentException e) {
            responder(ex, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }
        Producto creado;
        if (repo.cantidad() < MAX_PRODUCTOS) {
            creado = repo.agregar(datos.getNombre(), datos.getPrecio());
        } else {
            // Catálogo lleno: se valida y se responde igual, sin guardar.
            creado = new Producto(0, datos.getNombre(), datos.getPrecio());
        }
        responder(ex, 201, creado.toJson());
    }

    private void responder(HttpExchange ex, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
