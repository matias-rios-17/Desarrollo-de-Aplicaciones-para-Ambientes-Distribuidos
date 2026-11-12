import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
        ProductoDAO dao = new ProductoDAO();
        String[] nombres = {"Teclado", "Mouse", "Monitor", "Auriculares", "Webcam"};
        double[] precios = {15000.50, 8500.00, 210000.99, 32500.75, 45000.00};

        // 1) Escritura en el PRIMARIO
        System.out.println("=== ESCRITURA en nodo PRIMARIO (puerto 5432) ===");
        for (int i = 0; i < nombres.length; i++) {
            try {
                int id = dao.insertar(nombres[i], precios[i]);
                System.out.println("[PRIMARIO] Insertado id " + id + ": " + nombres[i]);
            } catch (SQLException e) {
                System.out.println("[ERROR PRIMARIO] No se pudo escribir: " + e.getMessage());
                System.out.println("[FAILOVER] El primario no responde, se pasa a modo solo lectura.");
                break;
            }
        }

        // Pequena espera para que la replica reciba los cambios
        Thread.sleep(1000);

        // 2) Lectura desde la REPLICA
        System.out.println();
        System.out.println("=== LECTURA desde nodo REPLICA (puerto 5434) ===");
        try {
            for (String p : dao.listar()) {
                System.out.println("[REPLICA] " + p);
            }
        } catch (SQLException e) {
            System.out.println("[ERROR REPLICA] No se pudo leer: " + e.getMessage());
        }
    }
}