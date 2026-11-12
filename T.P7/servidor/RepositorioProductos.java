package servidor;

import java.util.ArrayList;
import java.util.List;

// Guarda los productos en memoria (thread-safe con synchronized)
public class RepositorioProductos {
    private final List<Producto> productos = new ArrayList<>();
    private int siguienteId = 1;

    public RepositorioProductos() {
        agregar("Teclado", 15000.50);
        agregar("Mouse", 8500.00);
        agregar("Monitor", 210000.99);
    }

    public synchronized List<Producto> listar() {
        return new ArrayList<>(productos);
    }

    public synchronized Producto buscar(int id) {
        for (Producto p : productos) if (p.getId() == id) return p;
        return null;
    }

    public synchronized Producto agregar(String nombre, double precio) {
        Producto p = new Producto(siguienteId++, nombre, precio);
        productos.add(p);
        return p;
    }

    public synchronized int proximoId() { return siguienteId; }
}