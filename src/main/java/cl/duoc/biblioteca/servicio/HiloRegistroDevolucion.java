package cl.duoc.biblioteca.servicio;

import java.util.function.Consumer;

/** Hilo de fondo para devolver un libro y reponer su stock sin bloquear la interfaz. */
public class HiloRegistroDevolucion extends Thread {

    private final ServicioPrestamo servicio;
    private final int idPrestamo;
    private final Consumer<ResultadoOperacion> alTerminar;

    public HiloRegistroDevolucion(ServicioPrestamo servicio, int idPrestamo,
                                  Consumer<ResultadoOperacion> alTerminar) {
        super("Hilo-devolucion-" + idPrestamo);
        this.servicio = servicio;
        this.idPrestamo = idPrestamo;
        this.alTerminar = alTerminar;
    }

    @Override
    public void run() {
        ResultadoOperacion resultado;
        try {
            resultado = servicio.registrarDevolucion(idPrestamo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            resultado = ResultadoOperacion.error("No se pudo registrar la devolución.");
        }
        alTerminar.accept(resultado);
    }
}
