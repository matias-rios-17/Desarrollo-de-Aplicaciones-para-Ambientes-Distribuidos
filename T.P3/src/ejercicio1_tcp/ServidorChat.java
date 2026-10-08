import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TP3 - Ejercicio 1: Servidor de Chat Multihilo (Sockets TCP)
 *
 * - Escucha en un puerto (por defecto 5000) con ServerSocket.
 * - Por cada conexion aceptada (accept) crea un hilo independiente
 *   (Thread + Runnable) que atiende a ese cliente.
 * - Cada mensaje recibido se retransmite (broadcast) a todos los
 *   demas clientes conectados.
 * - Si un cliente se desconecta (con /salir o cerrando la ventana)
 *   se cierra su socket y el servidor sigue funcionando.
 *
 * Uso: java ServidorChat [puerto]
 */
public class ServidorChat {

    private static final int PUERTO_POR_DEFECTO = 5000;

    // Lista compartida por todos los hilos. CopyOnWriteArrayList es segura
    // para recorrerla mientras otros hilos agregan o quitan clientes.
    private static final List<ManejadorCliente> clientes = new CopyOnWriteArrayList<>();

    // Contador para numerar a los clientes (seguro entre hilos)
    private static final AtomicInteger contadorClientes = new AtomicInteger(0);

    public static void main(String[] args) {

        int puerto = PUERTO_POR_DEFECTO;

        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }

        try (ServerSocket servidor = new ServerSocket(puerto)) {

            log("Servidor de chat iniciado en el puerto " + puerto);
            log("Esperando conexiones de clientes...");

            // Bucle principal: el hilo main SOLO acepta conexiones.
            while (true) {

                // accept() bloquea hasta que llega un cliente nuevo
                Socket socket = servidor.accept();

                int numero = contadorClientes.incrementAndGet();

                log("Nueva conexion desde " + socket.getInetAddress().getHostAddress()
                        + ":" + socket.getPort() + " (cliente #" + numero + ")");

                // Se delega la atencion del cliente a un hilo independiente
                ManejadorCliente manejador = new ManejadorCliente(socket, numero);
                Thread hilo = new Thread(manejador, "Hilo-Cliente-" + numero);
                hilo.start();
            }

        } catch (IOException e) {
            log("Error en el servidor: " + e.getMessage());
        }
    }

    /**
     * Envia un mensaje a todos los clientes conectados, excepto al emisor.
     * Si emisor es null, el mensaje va a todos (avisos del servidor).
     */
    static void difundir(String mensaje, ManejadorCliente emisor) {
        for (ManejadorCliente cliente : clientes) {
            if (cliente != emisor) {
                cliente.enviar(mensaje);
            }
        }
    }

    static void log(String texto) {
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[" + hora + "] [" + Thread.currentThread().getName() + "] " + texto);
    }

    /**
     * Runnable que atiende a UN cliente. Cada instancia corre en su propio hilo.
     */
    static class ManejadorCliente implements Runnable {

        private final Socket socket;
        private final int numero;
        private PrintWriter salida;
        private String nombre;

        ManejadorCliente(Socket socket, int numero) {
            this.socket = socket;
            this.numero = numero;
            this.nombre = "Cliente" + numero;
        }

        @Override
        public void run() {

            try (
                BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                )
            ) {

                salida = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true
                );

                // 1) Pedir el nombre de usuario
                salida.println("[Servidor] Bienvenido al chat! Ingresa tu nombre:");
                String nombreRecibido = entrada.readLine();

                if (nombreRecibido == null) {
                    // Se desconecto antes de decir su nombre
                    return;
                }

                if (!nombreRecibido.trim().isEmpty()) {
                    nombre = nombreRecibido.trim();
                }

                // 2) Registrarlo en la lista compartida y avisar a todos
                clientes.add(this);
                log(nombre + " se unio al chat. Clientes conectados: " + clientes.size());

                salida.println("[Servidor] Hola " + nombre + "! Hay " + clientes.size()
                        + " usuario(s) conectado(s). Comandos: /usuarios, /salir");
                difundir("[Servidor] " + nombre + " se unio al chat", this);

                // 3) Leer mensajes hasta que el cliente se vaya
                String mensaje;
                boolean salioConComando = false;

                while ((mensaje = entrada.readLine()) != null) {

                    mensaje = mensaje.trim();

                    if (mensaje.isEmpty()) {
                        continue;
                    }

                    if (mensaje.equalsIgnoreCase("/salir")) {
                        salida.println("[Servidor] Hasta luego, " + nombre + "!");
                        salioConComando = true;
                        break;
                    }

                    if (mensaje.equalsIgnoreCase("/usuarios")) {
                        salida.println("[Servidor] Conectados (" + clientes.size() + "): " + listarNombres());
                        continue;
                    }

                    log("Mensaje de " + nombre + ": " + mensaje);

                    // Broadcast: se reenvia a todos los demas
                    difundir(nombre + ": " + mensaje, this);
                }

                // readLine() devolvio null: el cliente cerro la conexion sin avisar
                if (!salioConComando) {
                    log(nombre + " cerro la conexion sin usar /salir (desconexion abrupta)");
                }

            } catch (IOException e) {
                // Desconexion abrupta (se cerro la ventana, se corto la red, etc.)
                log("Conexion perdida con " + nombre + " (" + e.getMessage() + ")");

            } finally {
                desconectar();
            }
        }

        /** Envia un mensaje a este cliente. synchronized: varios hilos pueden llamarlo a la vez. */
        synchronized void enviar(String mensaje) {
            if (salida != null) {
                salida.println(mensaje);
            }
        }

        /** Quita al cliente de la lista y cierra su socket, sin detener el servidor. */
        private void desconectar() {

            boolean estaba = clientes.remove(this);

            try {
                socket.close();
            } catch (IOException e) {
                log("Error al cerrar el socket de " + nombre + ": " + e.getMessage());
            }

            if (estaba) {
                log(nombre + " se desconecto. Clientes conectados: " + clientes.size());
                difundir("[Servidor] " + nombre + " salio del chat", null);
            } else {
                log("Cliente #" + numero + " se desconecto antes de registrarse");
            }
        }

        private static String listarNombres() {
            StringBuilder sb = new StringBuilder();
            for (ManejadorCliente c : clientes) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(c.nombre);
            }
            return sb.toString();
        }
    }
}
