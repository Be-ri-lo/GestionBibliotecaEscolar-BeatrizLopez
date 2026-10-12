package cl.duoc.biblioteca;

import cl.duoc.biblioteca.controlador.ControladorAutenticacion;
import cl.duoc.biblioteca.dao.CategoriaDao;
import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.LibroDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.UsuarioDao;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;
import cl.duoc.biblioteca.servicio.ServicioPrestamo;
import cl.duoc.biblioteca.util.Sesion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el stock con varios hilos a la vez. Requiere MySQL y la base biblioteca.
 */
class ConcurrenciaPrestamoTest {

    @Test
    void bibliotecarioYEstudiantePuedenIniciarSesion() {
        ControladorAutenticacion autenticacion = new ControladorAutenticacion(new UsuarioDao(), new EstudianteDao());
        Sesion bibliotecario = autenticacion.iniciarSesion("antonia@correo.cl", "clave123");
        Sesion estudiante = autenticacion.iniciarSesion("carlos@correo.cl", "clave123");
        assertTrue(bibliotecario.esBibliotecario());
        assertFalse(estudiante.esBibliotecario());
        assertEquals("Carlos Ruiz", estudiante.getEstudiante().getNombre());
    }

    @Test
    void variosPrestamosSimultaneosNoDejanElStockNegativo() throws Exception {
        CategoriaDao categoriaDao = new CategoriaDao();
        EstudianteDao estudianteDao = new EstudianteDao();
        LibroDao libroDao = new LibroDao();
        PrestamoDao prestamoDao = new PrestamoDao();
        ServicioPrestamo servicio = new ServicioPrestamo(prestamoDao, libroDao, estudianteDao);

        String marca = String.valueOf(System.nanoTime());
        Categoria categoria = new Categoria(0, ("Prueba " + marca).substring(0, 18));
        categoriaDao.crear(categoria);

        Libro libro = new Libro();
        libro.setTitulo("Libro de prueba " + marca);
        libro.setAutor("Prueba");
        libro.setIsbn(("978" + marca).substring(0, 13));
        libro.setEditorial("Test");
        libro.setStock(3);
        libro.setIdCategoria(categoria.getId());
        libroDao.crear(libro);

        List<Estudiante> estudiantes = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                Estudiante estudiante = new Estudiante();
                estudiante.setNombre("Prueba " + i);
                estudiante.setRut(String.format("%02d%s", i, marca).substring(0, 12));
                estudiante.setCurso("4° Medio A");
                estudiante.setCorreo("p" + i + marca + "@test.local");
                estudianteDao.crear(estudiante);
                estudiantes.add(estudiante);
            }

            ExecutorService pool = Executors.newFixedThreadPool(8);
            CountDownLatch inicio = new CountDownLatch(1);
            List<Future<ResultadoOperacion>> futuros = new ArrayList<>();
            for (Estudiante estudiante : estudiantes) {
                futuros.add(pool.submit(() -> {
                    inicio.await(5, TimeUnit.SECONDS);
                    return servicio.registrarPrestamo(estudiante.getId(), libro.getId());
                }));
            }
            inicio.countDown();

            int exitos = 0;
            for (Future<ResultadoOperacion> futuro : futuros) {
                ResultadoOperacion resultado = futuro.get(20, TimeUnit.SECONDS);
                if (resultado.isExito()) {
                    exitos++;
                }
            }
            pool.shutdown();

            Libro actualizado = libroDao.buscarPorId(libro.getId());
            assertEquals(3, exitos);
            assertEquals(0, actualizado.getStock());
            assertTrue(actualizado.getStock() >= 0);
        } finally {
            for (Prestamo prestamo : prestamoDao.listarTodos()) {
                if (prestamo.getIdLibro() == libro.getId()) {
                    prestamoDao.eliminar(prestamo.getId());
                }
            }
            libroDao.eliminar(libro.getId());
            for (Estudiante estudiante : estudiantes) {
                estudianteDao.eliminar(estudiante.getId());
            }
            categoriaDao.eliminar(categoria.getId());
        }
    }

    @Test
    void laDevolucionAtrasadaQuedaRegistradaYReponeElStock() throws Exception {
        CategoriaDao categoriaDao = new CategoriaDao();
        EstudianteDao estudianteDao = new EstudianteDao();
        LibroDao libroDao = new LibroDao();
        PrestamoDao prestamoDao = new PrestamoDao();
        ServicioPrestamo servicio = new ServicioPrestamo(prestamoDao, libroDao, estudianteDao);

        String marca = String.valueOf(System.nanoTime());
        Categoria categoria = new Categoria(0, ("Atraso " + marca).substring(0, 16));
        categoriaDao.crear(categoria);

        Libro libro = new Libro();
        libro.setTitulo("Libro atrasado " + marca);
        libro.setAutor("Prueba");
        libro.setIsbn(("979" + marca).substring(0, 13));
        libro.setEditorial("Test");
        libro.setStock(2);
        libro.setIdCategoria(categoria.getId());
        libroDao.crear(libro);

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre("Estudiante atraso");
        estudiante.setRut(("8" + marca).substring(0, 12));
        estudiante.setCurso("1° Medio A");
        estudiante.setCorreo("atraso" + marca + "@test.local");
        estudianteDao.crear(estudiante);

        Prestamo prestamo = new Prestamo();
        prestamo.setIdEstudiante(estudiante.getId());
        prestamo.setIdLibro(libro.getId());
        prestamo.setFechaPrestamo(LocalDate.now().minusDays(20));
        prestamo.setFechaDevolucion(LocalDate.now().minusDays(6));
        prestamo.setDevuelto(false);
        prestamoDao.crear(prestamo);
        libroDao.actualizarStock(libro.getId(), 1);

        try {
            ResultadoOperacion resultado = servicio.registrarDevolucion(prestamo.getId());
            Prestamo guardado = prestamoDao.buscarPorId(prestamo.getId());
            assertTrue(resultado.isExito());
            assertTrue(resultado.getMensaje().contains("atraso"));
            assertTrue(guardado.isDevuelto());
            assertEquals(2, libroDao.buscarPorId(libro.getId()).getStock());
        } finally {
            prestamoDao.eliminar(prestamo.getId());
            libroDao.eliminar(libro.getId());
            estudianteDao.eliminar(estudiante.getId());
            categoriaDao.eliminar(categoria.getId());
        }
    }
}
