public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== INICIANDO TRABAJO PRACTICO N° 5: MODELO DE ACTORES ===\n");

        // 1. OPERACIÓN: CREAR (SPAWN)
        // Instanciar el Actor Procesador (con su buzón) y su hilo
        ActorProcesador procesador = new ActorProcesador();
        Thread hiloProcesador = new Thread(procesador, "Hilo-Procesador");
        hiloProcesador.start();

        // Crear 5 Actores Sensores que enviarán 100 mensajes cada uno (Total: 500 mensajes)
        int numSensores = 5;
        int mensajesPorSensor = 100;
        Thread[] hilosSensores = new Thread[numSensores];

        // 2. OPERACIÓN: ENVIAR (SEND)
        // Los sensores envían mensajes concurrentes hacia el procesador
        for (int i = 0; i < numSensores; i++) {
            ActorSensor sensor = new ActorSensor("Sensor-" + (i + 1), procesador, mensajesPorSensor);
            hilosSensores[i] = new Thread(sensor, "Hilo-Sensor-" + (i + 1));
            hilosSensores[i].start();
        }

        // Esperar a que todos los sensores terminen de enviar su ráfaga
        for (Thread h : hilosSensores) {
            h.join();
        }

        // Avisar al procesador que termine, también con un mensaje (poison pill).
        // Como el buzón es FIFO, llega después de los 500 mensajes.
        procesador.send(ActorProcesador.FIN);
        hiloProcesador.join();
    }
}