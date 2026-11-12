package servidor;

import java.util.Locale;

// Representa un producto del catalogo
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

    // Escapa comillas y barras para armar JSON valido
    public static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public String toJson() {
        return String.format(Locale.US, "{\"id\":%d,\"nombre\":\"%s\",\"precio\":%.2f}",
                id, escapar(nombre), precio);
    }

    // Parsea {"nombre":"...","precio":12.5}. Lanza IllegalArgumentException si esta mal.
    public static Producto fromJson(int id, String json) {
        if (json == null) throw new IllegalArgumentException("Cuerpo vacio");
        String j = json.trim();
        if (!j.startsWith("{") || !j.endsWith("}"))
            throw new IllegalArgumentException("JSON mal formado");
        String nombre = leerString(j, "nombre");
        String precioTxt = leerValor(j, "precio");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Falta el campo 'nombre'");
        if (precioTxt == null)
            throw new IllegalArgumentException("Falta el campo 'precio'");
        double precio;
        try {
            precio = Double.parseDouble(precioTxt);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'precio' debe ser numerico");
        }
        if (precio < 0) throw new IllegalArgumentException("'precio' no puede ser negativo");
        return new Producto(id, nombre, precio);
    }

    private static int posValor(String j, String campo) {
        int k = j.indexOf("\"" + campo + "\"");
        if (k < 0) return -1;
        int dp = j.indexOf(':', k);
        if (dp < 0) return -1;
        int i = dp + 1;
        while (i < j.length() && Character.isWhitespace(j.charAt(i))) i++;
        return i;
    }

    private static String leerString(String j, String campo) {
        int i = posValor(j, campo);
        if (i < 0 || i >= j.length() || j.charAt(i) != '"') return null;
        StringBuilder sb = new StringBuilder();
        for (int p = i + 1; p < j.length(); p++) {
            char c = j.charAt(p);
            if (c == '\\' && p + 1 < j.length()) { sb.append(j.charAt(++p)); continue; }
            if (c == '"') return sb.toString();
            sb.append(c);
        }
        throw new IllegalArgumentException("String sin cerrar en '" + campo + "'");
    }

    private static String leerValor(String j, String campo) {
        int i = posValor(j, campo);
        if (i < 0) return null;
        int f = i;
        while (f < j.length() && ",}".indexOf(j.charAt(f)) < 0) f++;
        return j.substring(i, f).trim();
    }
}