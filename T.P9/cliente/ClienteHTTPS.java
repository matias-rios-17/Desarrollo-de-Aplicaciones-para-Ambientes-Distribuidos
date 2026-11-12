package cliente;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

// Cliente de prueba: ejecuta los pasos del TP9 contra el servidor HTTPS.
// Confía en el certificado autofirmado cargando un truststore (NO desactiva la validación).
public class ClienteHTTPS {

    private static final String BASE = "https://localhost:8443";
    private static final String TRUSTSTORE = "certificados/cliente-truststore.p12";
    private static final String CLAVE_TRUSTSTORE = "cambiar123";

    private static HttpClient cliente;

    public static void main(String[] args) {
        try {
            cliente = HttpClient.newBuilder()
                    .sslContext(crearContextoSSL())
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // ---------- PASO 1: login correcto ----------
            titulo("PASO 1 - Login correcto (usuario matias) -> emisión de JWT  [Captura 1]");
            HttpResponse<String> r = post("/login", "{\"usuario\":\"matias\",\"clave\":\"1234\"}");
            String tokenMatias = leerToken(r.body());
            System.out.println("Token recibido : " + tokenMatias);
            System.out.println("Header         : " + decodificar(tokenMatias.split("\\.")[0]));
            System.out.println("Payload        : " + decodificar(tokenMatias.split("\\.")[1]));

            // ---------- PASO 2: acceso autorizado ----------
            titulo("PASO 2 - GET /api/perfil con Authorization: Bearer por HTTPS  [Captura 2]");
            get("/api/perfil", tokenMatias);

            // ---------- PASO 3: rechazos 401 ----------
            titulo("PASO 3 - Rechazos 401 Unauthorized  [Captura 3]");
            System.out.println("3.a) Sin token:");
            get("/api/perfil", null);
            System.out.println("\n3.b) Token inválido (cualquier texto):");
            get("/api/perfil", "esto-no-es-un-token");
            System.out.println("\n3.c) Token expirado (se pide un token de 1 segundo y se esperan 2):");
            HttpResponse<String> rCorto = post("/login", "{\"usuario\":\"matias\",\"clave\":\"1234\",\"duracion\":1}");
            String tokenCorto = leerToken(rCorto.body());
            Thread.sleep(2000);
            get("/api/perfil", tokenCorto);

            // ---------- PASO 4: tampering ----------
            titulo("PASO 4 - Tampering: se cambia el rol USER -> ADMIN sin volver a firmar  [Captura 4]");
            String[] partes = tokenMatias.split("\\.");
            String payloadOriginal = decodificar(partes[1]);
            String payloadAlterado = payloadOriginal.replace("\"rol\":\"USER\"", "\"rol\":\"ADMIN\"");
            String tokenAlterado = partes[0] + "." + codificar(payloadAlterado) + "." + partes[2];
            System.out.println("Payload original : " + payloadOriginal);
            System.out.println("Payload alterado : " + payloadAlterado);
            System.out.println("Token alterado   : " + tokenAlterado);
            System.out.println("(la firma es la misma de antes, no se recalculó)\n");
            get("/api/admin", tokenAlterado);

            // ---------- PASO 5: control de roles ----------
            titulo("PASO 5 - Control de acceso por rol en /api/admin");
            System.out.println("5.a) Token válido de matias (rol USER):");
            get("/api/admin", tokenMatias);
            System.out.println("\n5.b) Token válido de admin (rol ADMIN):");
            HttpResponse<String> rAdmin = post("/login", "{\"usuario\":\"admin\",\"clave\":\"admin123\"}");
            get("/api/admin", leerToken(rAdmin.body()));

            // ---------- PASO 6: credenciales incorrectas y HTTP plano ----------
            titulo("PASO 6 - Otros casos");
            System.out.println("6.a) Login con clave incorrecta:");
            post("/login", "{\"usuario\":\"matias\",\"clave\":\"mala\"}");
            System.out.println("\n6.b) Intento por HTTP plano (sin TLS) a http://localhost:8443:");
            probarHttpPlano();

            System.out.println("\nPruebas finalizadas.");

        } catch (ConnectException e) {
            System.out.println("No se pudo conectar a " + BASE + ". ¿Está levantado el servidor?");
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró " + TRUSTSTORE + ". Ejecutá primero generar-certificado.bat");
        } catch (IOException e) {
            System.out.println("Error de red: " + e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Ejecución interrumpida");
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e);
        }
    }

    // Truststore con el certificado del servidor, para que el cliente confíe en él
    private static SSLContext crearContextoSSL() throws Exception {
        KeyStore ts = KeyStore.getInstance("PKCS12");
        try (InputStream in = new FileInputStream(TRUSTSTORE)) {
            ts.load(in, CLAVE_TRUSTSTORE.toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(null, tmf.getTrustManagers(), null);
        return ssl;
    }

    private static HttpResponse<String> post(String ruta, String json) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + ruta))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        System.out.println(">> POST " + BASE + ruta);
        System.out.println("   Body: " + json);
        return enviar(req);
    }

    private static HttpResponse<String> get(String ruta, String token) throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(BASE + ruta))
                .timeout(Duration.ofSeconds(10))
                .GET();
        System.out.println(">> GET " + BASE + ruta);
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
            System.out.println("   Authorization: Bearer " + recortar(token));
        } else {
            System.out.println("   (sin encabezado Authorization)");
        }
        return enviar(b.build());
    }

    private static HttpResponse<String> enviar(HttpRequest req) throws IOException, InterruptedException {
        HttpResponse<String> r = cliente.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("<< Estado: " + r.statusCode() + " " + textoEstado(r.statusCode()));
        System.out.println("   Respuesta: " + r.body());
        return r;
    }

    // El servidor solo habla TLS: una petición HTTP sin cifrar tiene que fallar
    private static void probarHttpPlano() throws InterruptedException {
        HttpClient plano = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:8443/api/perfil"))
                .timeout(Duration.ofSeconds(5)).GET().build();
        try {
            HttpResponse<String> r = plano.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("<< Estado inesperado: " + r.statusCode());
        } catch (IOException e) {
            System.out.println("<< Rechazado: el servidor no acepta HTTP sin cifrar (" + e.getClass().getSimpleName() + ")");
        }
    }

    private static String leerToken(String json) {
        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (!m.find()) {
            throw new IllegalStateException("La respuesta no trae token: " + json);
        }
        return m.group(1);
    }

    private static String decodificar(String base64url) {
        return new String(Base64.getUrlDecoder().decode(base64url), StandardCharsets.UTF_8);
    }

    private static String codificar(String texto) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String recortar(String token) {
        return token.length() > 40 ? token.substring(0, 40) + "..." : token;
    }

    private static String textoEstado(int codigo) {
        switch (codigo) {
            case 200: return "OK";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            default: return "";
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println(texto);
        System.out.println("==================================================================");
    }
}
