package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.LibroDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.servicio.HiloRegistroDevolucion;
import cl.duoc.biblioteca.servicio.HiloRegistroPrestamo;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;
import cl.duoc.biblioteca.servicio.ServicioPrestamo;
import cl.duoc.biblioteca.util.MensajeSql;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ControladorPrestamo {

    private final ServicioPrestamo servicio;
    private final PrestamoDao prestamoDao;
    private final EstudianteDao estudianteDao;
    private final LibroDao libroDao;

    public ControladorPrestamo(ServicioPrestamo servicio, PrestamoDao prestamoDao,
                               EstudianteDao estudianteDao, LibroDao libroDao) {
        this.servicio = servicio;
        this.prestamoDao = prestamoDao;
        this.estudianteDao = estudianteDao;
        this.libroDao = libroDao;
    }

    public void prestarEnSegundoPlano(int idEstudiante, int idLibro, Consumer<ResultadoOperacion> alTerminar) {
        HiloRegistroPrestamo hilo = new HiloRegistroPrestamo(servicio, idEstudiante, idLibro,
                resultado -> SwingUtilities.invokeLater(() -> alTerminar.accept(resultado)));
        hilo.start();
    }

    public void devolverEnSegundoPlano(int idPrestamo, Consumer<ResultadoOperacion> alTerminar) {
        HiloRegistroDevolucion hilo = new HiloRegistroDevolucion(servicio, idPrestamo,
                resultado -> SwingUtilities.invokeLater(() -> alTerminar.accept(resultado)));
        hilo.start();
    }

    public List<Prestamo> listar() {
        try {
            return prestamoDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los préstamos"));
        }
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) {
        try {
            return prestamoDao.listarPorEstudiante(idEstudiante);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar el historial"));
        }
    }

    public List<Estudiante> listarEstudiantes() {
        try {
            return estudianteDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los estudiantes"));
        }
    }

    public List<Libro> listarLibros() {
        try {
            return libroDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los libros"));
        }
    }

    public List<Libro> listarLibrosDisponibles() {
        try {
            List<Libro> disponibles = new ArrayList<>();
            for (Libro libro : libroDao.listarTodos()) {
                if (libro.hayDisponibilidad()) {
                    disponibles.add(libro);
                }
            }
            return disponibles;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los libros"));
        }
    }

    public int getDiasPrestamo() {
        return ServicioPrestamo.DIAS_PRESTAMO;
    }

    public int getMaximoActivos() {
        return ServicioPrestamo.MAX_PRESTAMOS_ACTIVOS;
    }
}
