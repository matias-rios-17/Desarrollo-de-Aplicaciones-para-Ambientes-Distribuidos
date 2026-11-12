package servidor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Crea y valida tokens JWT firmados con HMAC-SHA256 (HS256).
// Formato: base64url(header) + "." + base64url(payload) + "." + base64url(firma)
public class JwtUtil {

    // Clave secreta del servidor. Se puede cambiar con la variable de entorno JWT_SECRETO.
    private static final String SECRETO = System.getenv().getOrDefault(
            "JWT_SECRETO", "clave-secreta-tp9-ambientes-distribuidos-2026");

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    // Genera un token para el usuario con el rol indicado, válido por "segundos"
    public static String crear(String sub, String rol, long segundos) {
        long ahora = System.currentTimeMillis() / 1000;
        String payload = "{\"sub\":\"" + Json.escapar(sub) + "\","
                + "\"rol\":\"" + Json.escapar(rol) + "\","
                + "\"iat\":" + ahora + ","
                + "\"exp\":" + (ahora + segundos) + "}";

        String header64 = codificar(HEADER);
        String payload64 = codificar(payload);
        String firma = firmar(header64 + "." + payload64);
        return header64 + "." + payload64 + "." + firma;
    }

    // Valida formato, firma y expiración. Si está todo bien devuelve los claims.
    public static Claims validar(String token) throws TokenInvalidoException {
        String[] partes = token.split("\\.", -1);
        if (partes.length != 3) {
            throw new TokenInvalidoException(TokenInvalidoException.Motivo.FORMATO,
                    "Token con formato inválido");
        }

        // 1) Se recalcula la firma y se compara con la recibida (comparación en tiempo constante)
        String firmaEsperada = firmar(partes[0] + "." + partes[1]);
        boolean firmaOk = MessageDigest.isEqual(
                firmaEsperada.getBytes(StandardCharsets.US_ASCII),
                partes[2].getBytes(StandardCharsets.US_ASCII));
        if (!firmaOk) {
            throw new TokenInvalidoException(TokenInvalidoException.Motivo.FIRMA,
                    "Firma inválida: el token fue alterado o no lo emitió este servidor");
        }

        // 2) Se lee el payload
        String payload;
        try {
            payload = new String(DECODER.decode(partes[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new TokenInvalidoException(TokenInvalidoException.Motivo.FORMATO,
                    "Payload mal codificado");
        }
        String sub = Json.leerTexto(payload, "sub");
        String rol = Json.leerTexto(payload, "rol");
        Long iat = Json.leerNumero(payload, "iat");
        Long exp = Json.leerNumero(payload, "exp");
        if (sub == null || rol == null || exp == null) {
            throw new TokenInvalidoException(TokenInvalidoException.Motivo.FORMATO,
                    "Faltan claims obligatorios (sub, rol, exp)");
        }

        // 3) Se controla la expiración
        long ahora = System.currentTimeMillis() / 1000;
        if (ahora >= exp) {
            throw new TokenInvalidoException(TokenInvalidoException.Motivo.EXPIRADO,
                    "Token expirado");
        }

        return new Claims(sub, rol, iat == null ? 0 : iat, exp);
    }

    private static String codificar(String texto) {
        return ENCODER.encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    // Calcula HMAC-SHA256(datos, SECRETO) y lo devuelve en base64url
    private static String firmar(String datos) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(datos.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
    }
}
