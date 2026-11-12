package servidor;

import java.util.Locale;

/**
 * Modelo de dominio: un producto del catálogo.
 * Incluye el marshalling (toJson) y unmarshalling (fromJson) hechos a mano,
 * igual que en el TP7 (sin librerías externas).
 */
public class Producto {

    private final int id;
    private final String nombre;
    private final double precio;

    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }

    /** Convierte el producto a JSON. Locale.US para usar punto decimal. */
    public String toJson() {
        return String.format(Locale.US,
                "{\"id\":%d,\"nombre\":\"%s\",\"precio\":%.2f}",
                id, escapar(nombre), precio);
    }

    /**
     * Lee un JSON del tipo {"nombre":"...","precio":123.45} y devuelve un
     * producto sin id (id = 0). Lanza IllegalArgumentException si el JSON
     * está mal formado o faltan campos.
     */
    public static Producto fromJson(String json) {
        if (json == null) throw new IllegalArgumentException("Cuerpo vacío");
        String s = json.trim();
        if (!s.startsWith("{") || !s.endsWith("}")) {
            throw new IllegalArgumentException("JSON mal formado");
        }
        String nombre = extraerTexto(s, "nombre");
        String precioTxt = extraerNumero(s, "precio");
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Falta el campo 'nombre'");
        }
        if (precioTxt == null) {
            throw new IllegalArgumentException("Falta el campo 'precio'");
        }
        double precio;
        try {
            precio = Double.parseDouble(precioTxt);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo 'precio' no es un número");
        }
        if (precio < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        return new Producto(0, nombre, precio);
    }

    private static String extraerTexto(String json, String campo) {
        int i = json.indexOf("\"" + campo + "\"");
        if (i < 0) return null;
        int dosPuntos = json.indexOf(':', i);
        if (dosPuntos < 0) return null;
        int ini = json.indexOf('"', dosPuntos + 1);
        if (ini < 0) return null;
        int fin = json.indexOf('"', ini + 1);
        if (fin < 0) throw new IllegalArgumentException("JSON mal formado");
        return json.substring(ini + 1, fin);
    }

    private static String extraerNumero(String json, String campo) {
        int i = json.indexOf("\"" + campo + "\"");
        if (i < 0) return null;
        int dosPuntos = json.indexOf(':', i);
        if (dosPuntos < 0) return null;
        int j = dosPuntos + 1;
        while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
        int ini = j;
        while (j < json.length() && "0123456789.-+eE".indexOf(json.charAt(j)) >= 0) j++;
        if (ini == j) throw new IllegalArgumentException("El campo 'precio' no es un número");
        return json.substring(ini, j);
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
