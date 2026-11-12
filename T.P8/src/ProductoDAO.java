import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public int insertar(String nombre, double precio) throws SQLException {
        String sql = "INSERT INTO productos (nombre, precio) VALUES (?, ?) RETURNING id";
        try (Connection c = Conexiones.escritura();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }

    public List<String> listar() throws SQLException {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, precio FROM productos ORDER BY id";
        try (Connection c = Conexiones.lectura();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rs.getInt("id") + " - " + rs.getString("nombre") + " - $" + rs.getBigDecimal("precio"));
            }
        }
        return lista;
    }
}