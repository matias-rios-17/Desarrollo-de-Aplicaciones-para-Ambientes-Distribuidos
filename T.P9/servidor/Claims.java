package servidor;

// Datos (claims) que viajan dentro del payload del JWT.
public class Claims {

    private final String sub;  // usuario
    private final String rol;  // rol del usuario (ADMIN / USER)
    private final long iat;    // emitido en (segundos desde 1970)
    private final long exp;    // expira en (segundos desde 1970)

    public Claims(String sub, String rol, long iat, long exp) {
        this.sub = sub;
        this.rol = rol;
        this.iat = iat;
        this.exp = exp;
    }

    public String getSub() { return sub; }
    public String getRol() { return rol; }
    public long getIat() { return iat; }
    public long getExp() { return exp; }
}
