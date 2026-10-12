package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.UsuarioDao;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;
import cl.duoc.biblioteca.util.MensajeSql;
import cl.duoc.biblioteca.util.Validador;

import java.sql.SQLException;
import java.util.List;

public class ControladorEstudiante {

    private final EstudianteDao estudianteDao;
    private final UsuarioDao usuarioDao;
    private final PrestamoDao prestamoDao;

    public ControladorEstudiante(EstudianteDao estudianteDao, UsuarioDao usuarioDao, PrestamoDao prestamoDao) {
        this.estudianteDao = estudianteDao;
        this.usuarioDao = usuarioDao;
        this.prestamoDao = prestamoDao;
    }

    public List<Estudiante> listar() {
        try {
            return estudianteDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los estudiantes"));
        }
    }

    public ResultadoOperacion guardar(Estudiante estudiante, String clave, String rutOriginal) {
        String error = validar(estudiante, clave, rutOriginal);
        if (error != null) {
            return ResultadoOperacion.error(error);
        }
        estudiante.setNombre(estudiante.getNombre().trim());
        estudiante.setRut(Validador.formatearRut(estudiante.getRut()));
        estudiante.setCurso(estudiante.getCurso().trim());
        estudiante.setCorreo(estudiante.getCorreo().trim().toLowerCase());
        String claveLimpia = clave == null ? "" : clave.trim();

        try {
            return DatabaseConnection.getInstance().enTransaccion(() -> {
                if (estudiante.getId() == 0) {
                    if (claveLimpia.length() < 4) {
                        return ResultadoOperacion.error("La contraseña de acceso debe tener al menos 4 caracteres.");
                    }
                    estudianteDao.crear(estudiante);
                    crearUsuario(estudiante, claveLimpia);
                    return ResultadoOperacion.ok("Estudiante registrado. Ya puede ingresar con su correo.");
                }

                if (estudianteDao.buscarPorId(estudiante.getId()) == null) {
                    return ResultadoOperacion.error("El estudiante ya no existe.");
                }
                estudianteDao.actualizar(estudiante);
                sincronizarUsuario(estudiante, rutOriginal, claveLimpia);
                return ResultadoOperacion.ok("Estudiante actualizado.");
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "guardar el estudiante"));
        }
    }

    public ResultadoOperacion eliminar(int id) {
        try {
            Estudiante estudiante = estudianteDao.buscarPorId(id);
            if (estudiante == null) {
                return ResultadoOperacion.error("El estudiante ya no existe.");
            }
            if (prestamoDao.existePorEstudiante(id)) {
                return ResultadoOperacion.error("No se puede eliminar al estudiante porque tiene préstamos asociados.");
            }
            return DatabaseConnection.getInstance().enTransaccion(() -> {
                Usuario usuario = usuarioDao.buscarPorRut(estudiante.getRut());
                if (usuario != null && usuario.getRol() == Rol.ESTUDIANTE) {
                    usuarioDao.eliminar(usuario.getId());
                }
                estudianteDao.eliminar(id);
                return ResultadoOperacion.ok("Estudiante eliminado.");
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "eliminar el estudiante"));
        }
    }

    private void crearUsuario(Estudiante estudiante, String clave) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setNombre(estudiante.getNombre());
        usuario.setRut(estudiante.getRut());
        usuario.setCorreo(estudiante.getCorreo());
        usuario.setContrasena(clave);
        usuario.setRol(Rol.ESTUDIANTE);
        usuarioDao.crear(usuario);
    }

    private void sincronizarUsuario(Estudiante estudiante, String rutOriginal, String clave) throws SQLException {
        String rutBusqueda = rutOriginal == null || rutOriginal.isBlank() ? estudiante.getRut() : rutOriginal;
        Usuario usuario = usuarioDao.buscarPorRut(rutBusqueda);
        if (usuario == null) {
            if (clave.length() >= 4) {
                crearUsuario(estudiante, clave);
            }
            return;
        }
        usuario.setNombre(estudiante.getNombre());
        usuario.setRut(estudiante.getRut());
        usuario.setCorreo(estudiante.getCorreo());
        usuario.setRol(Rol.ESTUDIANTE);
        if (clave.length() >= 4) {
            usuario.setContrasena(clave);
        }
        usuarioDao.actualizar(usuario);
    }

    private String validar(Estudiante estudiante, String clave, String rutOriginal) {
        if (estudiante.getNombre() == null || estudiante.getNombre().isBlank()) {
            return "El nombre es obligatorio.";
        }
        String rutIngresado = Validador.formatearRut(estudiante.getRut());
        String rutPrevio = Validador.formatearRut(rutOriginal);
        boolean conservaRutCargado = estudiante.getId() != 0 && !rutPrevio.isBlank() && rutIngresado.equals(rutPrevio);
        if (!conservaRutCargado && !Validador.rutValido(estudiante.getRut())) {
            return "El RUT no es válido. Revisa el dígito verificador, por ejemplo 20.111.222-2.";
        }
        if (!Validador.cursoValido(estudiante.getCurso())) {
            return "El curso debe usar el formato 3ro Medio A (1ro, 2do, 3ro o 4to, letra A, B o C).";
        }
        if (!Validador.correoValido(estudiante.getCorreo())) {
            return "El correo no es válido.";
        }
        if (estudiante.getNombre().trim().length() > 100 || estudiante.getCurso().trim().length() > 20) {
            return "Uno de los textos supera el largo permitido.";
        }
        if (clave != null && !clave.isBlank() && clave.trim().length() < 4) {
            return "La contraseña de acceso debe tener al menos 4 caracteres.";
        }
        return null;
    }
}
