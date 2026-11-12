package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Atiende todas las peticiones a /productos
public class ProductoHandler implements HttpHandler {
    private final RepositorioProductos repo;

    public ProductoHandler(RepositorioProductos repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        System.out.println("[SERVIDOR] " + metodo + " " + ruta);
        try {
            String resto = ruta.replaceFirst("^/productos/?", "");
            if (metodo.equals("GET") && resto.isEmpty()) {
                listar(ex);
            } else if (metodo.equals("GET")) {
                obtener(ex, resto);
            } else if (metodo.equals("POST") && resto.isEmpty()) {
                crear(ex);
            } else {
                responder(ex, 405, error("Metodo no permitido"));
            }
        } catch (Exception e) {
            responder(ex, 500, error("Error interno: " + e.getMessage()));
        }
    }

    private void listar(HttpExchange ex) throws IOException {
        List<Producto> lista = repo.listar();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(lista.get(i).toJson());
        }
        sb.append("]");
        responder(ex, 200, sb.toString());
    }

    private void obtener(HttpExchange ex, String idTxt) throws IOException {
        int id;
        try {
            id = Integer.parseInt(idTxt);
        } catch (NumberFormatException e) {
            responder(ex, 400, error("El id debe ser un numero"));
            return;
        }
        Producto p = repo.buscar(id);
        if (p == null) {
            responder(ex, 404, error("Producto " + id + " no encontrado"));
        } else {
            responder(ex, 200, p.toJson());
        }
    }

    private void crear(HttpExchange ex) throws IOException {
        String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Producto datos;
        try {
            datos = Producto.fromJson(0, cuerpo);
        } catch (IllegalArgumentException e) {
            responder(ex, 400, error(e.getMessage()));
            return;
        }
        Producto nuevo = repo.agregar(datos.getNombre(), datos.getPrecio());
        responder(ex, 201, nuevo.toJson());
    }

    private String error(String msg) {
        return "{\"error\":\"" + Producto.escapar(msg) + "\"}";
    }

    private void responder(HttpExchange ex, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}