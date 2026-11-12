package servidor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

// POST /login  -> recibe {"usuario":"...","clave":"..."} y devuelve un JWT.
// Campo opcional "duracion" (segundos, de 1 a 3600) para pedir un token más corto.
public class LoginHandler implements HttpHandler {

    private static final long DURACION_POR_DEFECTO = 60;  // segundos
    private static final long DURACION_MAXIMA = 3600;

    private final Usuarios usuarios;

    public LoginHandler(Usuarios usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Allow", "POST");
                Respuesta.enviarJson(exchange, 405, Json.error("Método no permitido, usar POST"));
                return;
            }

            String cuerpo = Respuesta.leerCuerpo(exchange);
            String usuario = Json.leerTexto(cuerpo, "usuario");
            String clave = Json.leerTexto(cuerpo, "clave");
            if (usuario == null || clave == null) {
                Respuesta.enviarJson(exchange, 400, Json.error("JSON inválido: se esperan los campos usuario y clave"));
                return;
            }

            // Duración del token (opcional, limitada entre 1 y 3600 segundos)
            Long pedida = Json.leerNumero(cuerpo, "duracion");
            long duracion = DURACION_POR_DEFECTO;
            if (pedida != null) {
                duracion = Math.max(1, Math.min(DURACION_MAXIMA, pedida));
            }

            String rol = usuarios.autenticar(usuario, clave);
            if (rol == null) {
                System.out.println("[LOGIN FALLIDO] usuario=" + usuario);
                Respuesta.enviarJson(exchange, 401, Json.error("Usuario o clave incorrectos"));
                return;
            }

            String token = JwtUtil.crear(usuario, rol, duracion);
            System.out.println("[LOGIN] usuario=" + usuario + " rol=" + rol + " expira en " + duracion + " s");
            String json = "{\"token\":\"" + token + "\",\"tipo\":\"Bearer\",\"expiraEn\":" + duracion + "}";
            Respuesta.enviarJson(exchange, 200, json);

        } catch (Exception e) {
            System.out.println("[500] Error en /login: " + e.getMessage());
            Respuesta.enviarJson(exchange, 500, Json.error("Error interno del servidor"));
        }
    }
}
