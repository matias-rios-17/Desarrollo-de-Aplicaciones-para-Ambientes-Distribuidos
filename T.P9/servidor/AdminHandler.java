package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

// GET /api/admin -> solo rol ADMIN (el FiltroJwt ya controló el rol)
public class AdminHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                Respuesta.enviarJson(exchange, 405, Json.error("Método no permitido, usar GET"));
                return;
            }
            Claims c = (Claims) exchange.getAttribute(FiltroJwt.ATRIBUTO_CLAIMS);
            String json = "{\"usuario\":\"" + Json.escapar(c.getSub()) + "\","
                    + "\"mensaje\":\"Bienvenido al panel de administración\","
                    + "\"usuariosRegistrados\":2}";
            Respuesta.enviarJson(exchange, 200, json);
        } catch (Exception e) {
            System.out.println("[500] Error en /api/admin: " + e.getMessage());
            Respuesta.enviarJson(exchange, 500, Json.error("Error interno del servidor"));
        }
    }
}
