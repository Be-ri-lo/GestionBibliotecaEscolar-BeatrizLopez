package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.ReporteDao;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.util.Fechas;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteHistorialEstudiante implements GeneradorReporte {

    @Override
    public String getTitulo() {
        return "Historial por estudiante";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Libro", "Fecha de préstamo", "Fecha de vencimiento", "Estado", "Días de atraso"};
    }

    @Override
    public boolean requiereEstudiante() {
        return true;
    }

    @Override
    public List<Object[]> generar(PrestamoDao prestamos, ReporteDao reportes, Integer idEstudiante) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        if (idEstudiante == null) {
            return filas;
        }
        for (Prestamo prestamo : prestamos.listarPorEstudiante(idEstudiante)) {
            filas.add(fila(prestamo));
        }
        return filas;
    }

    static Object[] fila(Prestamo prestamo) {
        long atraso = prestamo.diasDeAtraso();
        return new Object[]{
                prestamo.getTituloLibro(),
                Fechas.formatear(prestamo.getFechaPrestamo()),
                Fechas.formatear(prestamo.getFechaDevolucion()),
                prestamo.getEstado(),
                atraso == 0 ? "" : atraso
        };
    }
}
