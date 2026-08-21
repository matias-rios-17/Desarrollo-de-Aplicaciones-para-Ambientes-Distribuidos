import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) {

        String host = "localhost";
        int puerto = 5500;

        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el primer numero: ");
        int numero1 = scanner.nextInt();

        System.out.print("Ingrese la operacion (+, -, *, /): ");
        String operacion = scanner.next();

        System.out.print("Ingrese el segundo numero: ");
        int numero2 = scanner.nextInt();

        // Crear mensaje para enviar al servidor
        String mensaje = numero1 + ";" + operacion + ";" + numero2;

        try (
            Socket socket = new Socket(host, puerto);

            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            PrintWriter salida = new PrintWriter(
                socket.getOutputStream(), true
            )
        ) {

            // Enviar datos al servidor
            salida.println(mensaje);

            // Recibir resultado del servidor
            String resultado = entrada.readLine();

            // Mostrar resultado
            System.out.println("Resultado: " + resultado);

        } catch (IOException e) {

            System.out.println("No se pudo conectar con el servidor.");
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
    }
}