package cl.duoc.biblioteca.servicio;

import java.util.function.Consumer;

/**
 * Hilo de fondo para registrar un préstamo sin congelar la ventana.
 * Delega en ServicioPrestamo, que actualiza el préstamo, el stock y el historial.
 */
public class HiloRegistroPrestamo extends Thread {

    private final ServicioPrestamo servicio;
    private final int idEstudiante;
    private final int idLibro;
    private final Consumer<ResultadoOperacion> alTerminar;

    public HiloRegistroPrestamo(ServicioPrestamo servicio, int idEstudiante, int idLibro,
                                Consumer<ResultadoOperacion> alTerminar) {
        super("Hilo-prestamo-libro-" + idLibro);
        this.servicio = servicio;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.alTerminar = alTerminar;
    }

    @Override
    public void run() {
        ResultadoOperacion resultado;
        try {
            resultado = servicio.registrarPrestamo(idEstudiante, idLibro);
        } catch (RuntimeException e) {
            e.printStackTrace();
            resultado = ResultadoOperacion.error("No se pudo registrar el préstamo.");
        }
        alTerminar.accept(resultado);
    }
}
