package cl.duoc.biblioteca.excepcion;

/**
 * Error de negocio o de persistencia que la vista puede mostrar al usuario.
 */
public class BibliotecaException extends RuntimeException {

    public BibliotecaException(String mensaje) {
        super(mensaje);
    }

    public BibliotecaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
