package servidor;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

// Servidor que responde ÚNICAMENTE por HTTPS (TLS) en el puerto 8443.
public class ServidorHTTPS {

    private static final int PUERTO = 8443;
    private static final String KEYSTORE = "certificados/servidor.p12";
    private static final String CLAVE_KEYSTORE = "cambiar123";

    public static void main(String[] args) {
        try {
            SSLContext ssl = crearContextoSSL();

            HttpsServer servidor = HttpsServer.create(new InetSocketAddress(PUERTO), 0);
            servidor.setHttpsConfigurator(new HttpsConfigurator(ssl));

            Usuarios usuarios = new Usuarios();

            // Ruta pública
            servidor.createContext("/login", new LoginHandler(usuarios));

            // Rutas protegidas: pasan primero por el middleware FiltroJwt
            HttpContext perfil = servidor.createContext("/api/perfil", new PerfilHandler());
            perfil.getFilters().add(new FiltroJwt(null));

            HttpContext admin = servidor.createContext("/api/admin", new AdminHandler());
            admin.getFilters().add(new FiltroJwt("ADMIN"));

            // Cualquier otra ruta -> 404
            servidor.createContext("/", exchange ->
                    Respuesta.enviarJson(exchange, 404, Json.error("Ruta no encontrada")));

            servidor.setExecutor(Executors.newFixedThreadPool(4));
            servidor.start();

            System.out.println("Servidor HTTPS escuchando en https://localhost:" + PUERTO);
            System.out.println("Protocolo: " + ssl.getProtocol() + " | Certificado: " + KEYSTORE);
            System.out.println("Rutas: POST /login | GET /api/perfil (token) | GET /api/admin (rol ADMIN)");
        } catch (FileNotFoundException e) {
            System.out.println("No se encontró " + KEYSTORE + ". Ejecutá primero generar-certificado.bat");
        } catch (java.net.BindException e) {
            System.out.println("El puerto " + PUERTO + " ya está en uso. ¿Hay otro servidor levantado?");
        } catch (Exception e) {
            System.out.println("Error al iniciar el servidor: " + e);
        }
    }

    // Carga el certificado autofirmado (keystore PKCS12) y arma el contexto TLS
    private static SSLContext crearContextoSSL() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream in = new FileInputStream(KEYSTORE)) {
            ks.load(in, CLAVE_KEYSTORE.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, CLAVE_KEYSTORE.toCharArray());

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), null, null);
        return ssl;
    }
}
