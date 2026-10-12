package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.UsuarioDao;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;
import cl.duoc.biblioteca.util.MensajeSql;
import cl.duoc.biblioteca.util.Sesion;

import java.sql.SQLException;

public class ControladorAutenticacion {

    private final UsuarioDao usuarioDao;
    private final EstudianteDao estudianteDao;

    public ControladorAutenticacion(UsuarioDao usuarioDao, EstudianteDao estudianteDao) {
        this.usuarioDao = usuarioDao;
        this.estudianteDao = estudianteDao;
    }

    public Sesion iniciarSesion(String correo, String clave) {
        if (correo == null || correo.isBlank() || clave == null || clave.isBlank()) {
            throw new BibliotecaException("Ingresa el correo y la contraseña.");
        }
        try {
            Usuario usuario = usuarioDao.buscarPorCorreo(correo.trim());
            if (usuario == null || !usuario.getContrasena().equals(clave)) {
                throw new BibliotecaException("Correo o contraseña incorrectos.");
            }
            Estudiante estudiante = null;
            if (usuario.getRol() == Rol.ESTUDIANTE) {
                estudiante = estudianteDao.buscarPorRut(usuario.getRut());
                if (estudiante == null) {
                    throw new BibliotecaException(
                            "Este usuario no tiene una ficha de estudiante. Pide al bibliotecario que la registre."
                    );
                }
            }
            return new Sesion(usuario, estudiante);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "iniciar sesión"));
        }
    }
}
