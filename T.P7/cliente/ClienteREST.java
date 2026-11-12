package cliente;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Cliente independiente que consume la API REST de productos
public class ClienteREST {
    private static final String BASE = "http://localhost:8080/productos";
    private static final HttpClient http = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try {
            titulo("1) Listar productos (GET /productos)");
            get(BASE);

            titulo("2) Crear producto valido (POST /productos)");
            HttpResponse<String> creado = post("{\"nombre\":\"Auriculares\",\"precio\":32500.75}");
            int id = extraerId(creado.body());

            titulo("3) Buscar el producto creado (GET /productos/" + id + ")");
            get(BASE + "/" + id);

            titulo("4) Buscar id inexistente (GET /productos/999) -> 404");
            get(BASE + "/999");

            titulo("5) Buscar con id no numerico (GET /productos/abc) -> 400");
            get(BASE + "/abc");

            titulo("6) Enviar JSON roto (POST /productos) -> 400");
            post("{\"nombre\":\"Roto\",\"precio\":");
        } catch (ConnectException e) {
            System.out.println("No se pudo conectar. Esta corriendo el servidor?");
        } catch (Exception e) {
            System.out.println("Error en el cliente: " + e);
        }
    }

    private static HttpResponse<String> get(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        return mostrar(http.send(req, HttpResponse.BodyHandlers.ofString()));
    }

    private static HttpResponse<String> post(String json) throws Exception {
        System.out.println("Enviando: " + json);
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return mostrar(http.send(req, HttpResponse.BodyHandlers.ofString()));
    }

    private static HttpResponse<String> mostrar(HttpResponse<String> r) {
        System.out.println("Status: " + r.statusCode());
        System.out.println("Body:   " + r.body());
        return r;
    }

    private static int extraerId(String json) {
        int i = json.indexOf("\"id\":") + 5;
        int f = i;
        while (f < json.length() && Character.isDigit(json.charAt(f))) f++;
        return Integer.parseInt(json.substring(i, f));
    }

    private static void titulo(String t) {
        System.out.println();
        System.out.println("=== " + t + " ===");
    }
}