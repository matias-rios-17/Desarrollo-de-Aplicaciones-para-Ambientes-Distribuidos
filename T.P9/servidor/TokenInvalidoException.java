package servidor;

// Excepción que se lanza cuando un JWT no es válido.
// El "motivo" sirve para saber por qué se rechazó (formato, firma o expiración).
public class TokenInvalidoException extends Exception {

    public enum Motivo { FORMATO, FIRMA, EXPIRADO }

    private final Motivo motivo;

    public TokenInvalidoException(Motivo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
