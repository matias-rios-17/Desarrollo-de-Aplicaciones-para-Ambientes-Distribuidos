import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexiones {
    private static final String URL_PRIMARIO = "jdbc:postgresql://localhost:5432/tienda";
    private static final String URL_REPLICA  = "jdbc:postgresql://localhost:5434/tienda";
    private static final String USUARIO = "admin";
    private static final String CLAVE   = "admin123";

    // INSERT / UPDATE / DELETE van al nodo primario
    public static Connection escritura() throws SQLException {
        return DriverManager.getConnection(URL_PRIMARIO, USUARIO, CLAVE);
    }

    // SELECT van a la replica (solo lectura)
    public static Connection lectura() throws SQLException {
        Connection c = DriverManager.getConnection(URL_REPLICA, USUARIO, CLAVE);
        c.setReadOnly(true);
        return c;
    }
}