import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActorProcesador implements Runnable {
    // Mensaje especial de fin (poison pill): avisa al actor que debe terminar
    public static final MensajeLectura FIN = new MensajeLectura("FIN", 0);

    // Buzón (Mailbox) FIFO para recibir mensajes asincrónicos
    private final BlockingQueue<MensajeLectura> mailbox = new LinkedBlockingQueue<>();

    // Estado interno privado (aislado): solo lo toca el hilo de este actor
    private int totalLecturas = 0;
    private double sumaMuestras = 0.0;

    // Operación Send: deja el mensaje en el buzón y vuelve sin esperar
    public void send(MensajeLectura mensaje) {
        mailbox.offer(mensaje);
    }

    // Operación Designar (State Change): procesa el mensaje y actualiza el estado
    private void procesarMensaje(MensajeLectura m) {
        totalLecturas++;
        sumaMuestras += m.getValor();
        double promedio = sumaMuestras / totalLecturas;

        System.out.printf("[%03d] Procesado mensaje de %s | Valor: %.2f | Promedio Histórico: %.2f%n",
                totalLecturas, m.getIdSensor(), m.getValor(), promedio);
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Toma el siguiente mensaje de la cola FIFO (espera si está vacía)
                MensajeLectura mensaje = mailbox.take();
                if (mensaje == FIN) {
                    break;
                }
                procesarMensaje(mensaje);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        mostrarResultado();
    }

    // El propio actor informa su resultado final; nadie de afuera lee su estado
    private void mostrarResultado() {
        System.out.println("\n==================================================");
        System.out.println("            RESULTADO DEL PROCESAMIENTO           ");
        System.out.println("==================================================");
        System.out.println("Total de mensajes recibidos/procesados: " + totalLecturas);
        System.out.printf("Promedio final acumulado: %.2f%n", totalLecturas == 0 ? 0 : sumaMuestras / totalLecturas);
        System.out.println("Procesamiento finalizado sin condiciones de carrera.");
    }
}