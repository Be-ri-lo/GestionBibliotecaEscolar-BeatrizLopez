package cl.duoc.biblioteca.util;

import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;

/**
 * Usuario que inició sesión y, si corresponde, su ficha de estudiante.
 */
public class Sesion {

    private final Usuario usuario;
    private final Estudiante estudiante;

    public Sesion(Usuario usuario, Estudiante estudiante) {
        this.usuario = usuario;
        this.estudiante = estudiante;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public boolean esBibliotecario() {
        return usuario.getRol() == Rol.BIBLIOTECARIO;
    }
}
