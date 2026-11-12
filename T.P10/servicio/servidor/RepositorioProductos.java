package servidor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Almacenamiento en memoria de los productos.
 *
 * Cambio respecto al TP7: ahora es thread-safe, porque en la versión
 * optimizada (--hilos N) varios hilos atienden pedidos al mismo tiempo.
 * - ConcurrentSkipListMap: mapa concurrente que mantiene los ids ordenados.
 * - AtomicInteger: genera ids únicos sin condiciones de carrera.
 */
public class RepositorioProductos {

    private final Map<Integer, Producto> productos = new ConcurrentSkipListMap<>();
    private final AtomicInteger proximoId = new AtomicInteger(1);

    public RepositorioProductos() {
        // Datos iniciales (los ids 1..5 siempre existen; el locustfile los usa)
        agregar("Teclado mecánico", 45999.90);
        agregar("Mouse inalámbrico", 18500.00);
        agregar("Monitor 24 pulgadas", 210000.00);
        agregar("Auriculares", 32500.75);
        agregar("Webcam HD", 27999.99);
    }

    public List<Producto> listar() {
        return new ArrayList<>(productos.values());
    }

    public Producto buscar(int id) {
        return productos.get(id);
    }

    public Producto agregar(String nombre, double precio) {
        int id = proximoId.getAndIncrement();
        Producto p = new Producto(id, nombre, precio);
        productos.put(id, p);
        return p;
    }

    public int cantidad() {
        return productos.size();
    }
}
