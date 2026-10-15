import java.util.Random;

public class ActorSensor implements Runnable {
    private final String idSensor;
    private final ActorProcesador procesadorTarget;
    private final int cantidadMensajes;
    private final Random random = new Random();

    public ActorSensor(String idSensor, ActorProcesador procesadorTarget, int cantidadMensajes) {
        this.idSensor = idSensor;
        this.procesadorTarget = procesadorTarget;
        this.cantidadMensajes = cantidadMensajes;
    }

    @Override
    public void run() {
        for (int i = 0; i < cantidadMensajes; i++) {
            // Genera una lectura simulada (ejemplo: Temperatura entre 15.0 y 35.0)
            double temperatura = 15.0 + (20.0 * random.nextDouble());
            MensajeLectura mensaje = new MensajeLectura(idSensor, temperatura);

            // Operacion Send (Envio de mensaje asincronico)
            procesadorTarget.send(mensaje);

            try {
                Thread.sleep(1); // Breve pausa para simular intervalo de sensor
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}