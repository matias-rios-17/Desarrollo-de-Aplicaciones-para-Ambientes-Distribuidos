package servidor;

import java.util.HashMap;
import java.util.Map;

// "Base de datos" de usuarios en memoria (solo para el TP).
public class Usuarios {

    private static class Usuario {
        final String clave;
        final String rol;

        Usuario(String clave, String rol) {
            this.clave = clave;
            this.rol = rol;
        }
    }

    private final Map<String, Usuario> usuarios = new HashMap<>();

    public Usuarios() {
        usuarios.put("admin", new Usuario("admin123", "ADMIN"));
        usuarios.put("matias", new Usuario("1234", "USER"));
    }

    // Devuelve el rol si usuario y clave son correctos, o null si no
    public String autenticar(String usuario, String clave) {
        Usuario u = usuarios.get(usuario);
        if (u == null || !u.clave.equals(clave)) {
            return null;
        }
        return u.rol;
    }
}
