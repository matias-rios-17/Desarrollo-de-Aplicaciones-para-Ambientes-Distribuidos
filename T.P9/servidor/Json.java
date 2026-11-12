package servidor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Utilidades mínimas para leer y armar JSON a mano (sin librerías externas).
public class Json {

    // Devuelve el valor de un campo de texto, ej: "usuario":"matias" -> matias
    public static String leerTexto(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        return m.find() ? m.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : null;
    }

    // Devuelve el valor de un campo numérico entero, ej: "exp":1700000000
    public static Long leerNumero(String json, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }

    // Escapa comillas y barras para poder meter un texto dentro de un JSON
    public static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Arma {"error":"mensaje"}
    public static String error(String mensaje) {
        return "{\"error\":\"" + escapar(mensaje) + "\"}";
    }
}
