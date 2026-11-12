package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

// GET /api/perfil -> cualquier usuario con token válido (protegido por FiltroJwt)
public class PerfilHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                Respuesta.enviarJson(exchange, 405, Json.error("Método no permitido, usar GET"));
                return;
            }
            Claims c = (Claims) exchange.getAttribute(FiltroJwt.ATRIBUTO_CLAIMS);
            String json = "{\"usuario\":\"" + Json.escapar(c.getSub()) + "\","
                    + "\"rol\":\"" + Json.escapar(c.getRol()) + "\","
                    + "\"expiraEn\":" + c.getExp() + ","
                    + "\"mensaje\":\"Acceso autorizado por HTTPS\"}";
            Respuesta.enviarJson(exchange, 200, json);
        } catch (Exception e) {
            System.out.println("[500] Error en /api/perfil: " + e.getMessage());
            Respuesta.enviarJson(exchange, 500, Json.error("Error interno del servidor"));
        }
    }
}
