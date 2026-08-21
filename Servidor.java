import java.io.*;
import java.net.*;

public class Servidor {

    public static void main(String[] args) {

        int puerto = 5500;

        try (ServerSocket servidor = new ServerSocket(puerto)) {

            System.out.println("Servidor iniciado en el puerto " + puerto);
            System.out.println("Esperando conexion de un cliente...");

            try (
                Socket socket = servidor.accept();

                BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );

                PrintWriter salida = new PrintWriter(
                    socket.getOutputStream(), true
                )
            ) {

                // Recibir los datos enviados por el cliente
                String mensaje = entrada.readLine();

                System.out.println("Datos recibidos: " + mensaje);

                // Separar los datos: numero1;operacion;numero2
                String[] datos = mensaje.split(";");

                int numero1 = Integer.parseInt(datos[0]);
                String operacion = datos[1];
                int numero2 = Integer.parseInt(datos[2]);

                String resultado;

                // Realizar la operación
                switch (operacion) {

                    case "+":
                        resultado = String.valueOf(numero1 + numero2);
                        break;

                    case "-":
                        resultado = String.valueOf(numero1 - numero2);
                        break;

                    case "*":
                        resultado = String.valueOf(numero1 * numero2);
                        break;

                    case "/":
                        // Validar división por cero
                        if (numero2 == 0) {
                            resultado = "ERROR: Division por cero";
                        } else {
                            resultado = String.valueOf(
                                (double) numero1 / numero2
                            );
                        }
                        break;

                    default:
                        resultado = "ERROR: Operacion no valida";
                        break;
                }

                // Enviar el resultado al cliente
                salida.println(resultado);

                System.out.println("Resultado enviado: " + resultado);
            }

        } catch (IOException | NumberFormatException e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
}