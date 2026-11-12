package servidor;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

// Middleware de autorización: se ejecuta ANTES del handler de cada ruta protegida.
// - Sin token / token inválido / expirado -> 401 Unauthorized
// - Token válido pero el rol no alcanza   -> 403 Forbidden
// - Si está todo bien, guarda los claims en el exchange y deja pasar la petición.
public class FiltroJwt extends Filter {

    public static final String ATRIBUTO_CLAIMS = "claims";

    private final String rolRequerido; // null = cualquier rol válido

    public FiltroJwt(String rolRequerido) {
        this.rolRequerido = rolRequerido;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        String header = exchange.getRequestHeaders().getFirst("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("[401] " + ruta + " -> falta el token");
            exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
            Respuesta.enviarJson(exchange, 401, Json.error("Falta el encabezado Authorization: Bearer <token>"));
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        Claims claims;
        try {
            claims = JwtUtil.validar(token);
        } catch (TokenInvalidoException e) {
            if (e.getMotivo() == TokenInvalidoException.Motivo.FIRMA) {
                System.out.println("[TAMPERING] " + ruta + " -> " + e.getMessage());
            } else {
                System.out.println("[401] " + ruta + " -> " + e.getMessage());
            }
            exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer error=\"invalid_token\"");
            Respuesta.enviarJson(exchange, 401, Json.error(e.getMessage()));
            return;
        }

        if (rolRequerido != null && !rolRequerido.equals(claims.getRol())) {
            System.out.println("[403] " + ruta + " -> usuario=" + claims.getSub()
                    + " rol=" + claims.getRol() + " (se requiere " + rolRequerido + ")");
            Respuesta.enviarJson(exchange, 403, Json.error("Acceso denegado: se requiere rol " + rolRequerido));
            return;
        }

        System.out.println("[ACCESO OK] " + ruta + " -> usuario=" + claims.getSub() + " rol=" + claims.getRol());
        exchange.setAttribute(ATRIBUTO_CLAIMS, claims);
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Valida el JWT del encabezado Authorization";
    }
}
