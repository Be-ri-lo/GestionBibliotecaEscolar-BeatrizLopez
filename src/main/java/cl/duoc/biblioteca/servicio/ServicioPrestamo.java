package cl.duoc.biblioteca.servicio;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.LibroDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.modelo.RecursoBibliografico;
import cl.duoc.biblioteca.util.Fechas;
import cl.duoc.biblioteca.util.MensajeSql;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Registra préstamos y devoluciones protegiendo el stock.
 * Los métodos synchronized impiden que dos hilos descuenten el mismo ejemplar a la vez.
 */
public class ServicioPrestamo {

    public static final int DIAS_PRESTAMO = 14;
    public static final int MAX_PRESTAMOS_ACTIVOS = 3;

    private final PrestamoDao prestamoDao;
    private final LibroDao libroDao;
    private final EstudianteDao estudianteDao;

    public ServicioPrestamo(PrestamoDao prestamoDao, LibroDao libroDao, EstudianteDao estudianteDao) {
        this.prestamoDao = prestamoDao;
        this.libroDao = libroDao;
        this.estudianteDao = estudianteDao;
    }

    public synchronized ResultadoOperacion registrarPrestamo(int idEstudiante, int idLibro) {
        try {
            return DatabaseConnection.getInstance().enTransaccion(() -> {
                Estudiante estudiante = estudianteDao.buscarPorId(idEstudiante);
                if (estudiante == null) {
                    return ResultadoOperacion.error("El estudiante no existe.");
                }

                RecursoBibliografico recurso = libroDao.buscarPorIdParaActualizar(idLibro);
                if (!(recurso instanceof Libro libro)) {
                    return ResultadoOperacion.error("El libro no existe.");
                }
                if (!recurso.hayDisponibilidad()) {
                    return ResultadoOperacion.error("No hay ejemplares disponibles de \"" + libro.getTitulo() + "\".");
                }
                if (prestamoDao.existePrestamoActivo(idEstudiante, idLibro)) {
                    return ResultadoOperacion.error("El estudiante ya tiene un préstamo vigente de este libro.");
                }
                if (prestamoDao.contarActivos(idEstudiante) >= MAX_PRESTAMOS_ACTIVOS) {
                    return ResultadoOperacion.error(
                            "El estudiante ya tiene " + MAX_PRESTAMOS_ACTIVOS
                                    + " préstamos activos. Debe devolver un libro antes de pedir otro."
                    );
                }

                LocalDate hoy = LocalDate.now();
                Prestamo prestamo = new Prestamo();
                prestamo.setIdEstudiante(idEstudiante);
                prestamo.setIdLibro(idLibro);
                prestamo.setFechaPrestamo(hoy);
                prestamo.setFechaDevolucion(hoy.plusDays(DIAS_PRESTAMO));
                prestamo.setDevuelto(false);

                // 1. Registrar el préstamo (queda en el historial del estudiante).
                prestamoDao.crear(prestamo);
                // 2. Actualizar el stock dentro de la misma sección crítica.
                descontarStock(libro);
                // 3. Confirmar que el movimiento figura en el historial.
                int movimientos = prestamoDao.listarPorEstudiante(idEstudiante).size();

                return ResultadoOperacion.ok(
                        "Préstamo registrado a " + estudiante.getNombre()
                                + ". La devolución vence el " + Fechas.formatear(prestamo.getFechaDevolucion())
                                + ". El historial del estudiante queda con " + movimientos + " movimiento(s)."
                );
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "registrar el préstamo"));
        }
    }

    public synchronized ResultadoOperacion registrarDevolucion(int idPrestamo) {
        try {
            return DatabaseConnection.getInstance().enTransaccion(() -> {
                Prestamo prestamo = prestamoDao.buscarPorId(idPrestamo);
                if (prestamo == null) {
                    return ResultadoOperacion.error("El préstamo no existe.");
                }
                if (prestamo.isDevuelto()) {
                    return ResultadoOperacion.error("Este préstamo ya fue devuelto.");
                }

                boolean conAtraso = prestamo.estaVencido();
                long dias = prestamo.diasDeAtraso();
                prestamo.setDevuelto(true);

                prestamoDao.marcarDevuelto(prestamo);
                reponerStock(prestamo.getIdLibro());

                if (conAtraso) {
                    return ResultadoOperacion.ok(
                            "Devolución registrada con atraso de " + dias
                                    + " día(s). Se deja constancia en el historial de "
                                    + prestamo.getNombreEstudiante() + "."
                    );
                }
                return ResultadoOperacion.ok(
                        "Devolución registrada a tiempo. El ejemplar de \""
                                + prestamo.getTituloLibro() + "\" volvió al inventario."
                );
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "registrar la devolución"));
        }
    }

    /** Descuenta una unidad. Solo debe usarse dentro del préstamo sincronizado. */
    public synchronized void descontarStock(Libro libro) throws SQLException {
        if (libro == null || !libro.hayDisponibilidad()) {
            throw new SQLException("No se puede descontar stock de un libro sin ejemplares.");
        }
        int nuevoStock = libro.getStock() - 1;
        libroDao.actualizarStock(libro.getId(), nuevoStock);
        libro.setStock(nuevoStock);
    }

    /** Devuelve una unidad al inventario. Solo debe usarse dentro de la devolución sincronizada. */
    public synchronized void reponerStock(int idLibro) throws SQLException {
        Libro libro = libroDao.buscarPorIdParaActualizar(idLibro);
        if (libro == null) {
            throw new SQLException("Libro no encontrado al reponer el stock.");
        }
        libroDao.actualizarStock(idLibro, libro.getStock() + 1);
    }
}
