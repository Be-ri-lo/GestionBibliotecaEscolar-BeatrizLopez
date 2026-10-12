package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.ReporteDao;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.util.Fechas;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteLibrosEnPrestamo implements GeneradorReporte {

    @Override
    public String getTitulo() {
        return "Libros actualmente en préstamo";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Estudiante", "Curso", "Libro", "Fecha de préstamo", "Fecha de vencimiento", "Estado", "Días de atraso"};
    }

    @Override
    public boolean requiereEstudiante() {
        return false;
    }

    @Override
    public List<Object[]> generar(PrestamoDao prestamos, ReporteDao reportes, Integer idEstudiante) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo prestamo : prestamos.listarActivos()) {
            long atraso = prestamo.diasDeAtraso();
            filas.add(new Object[]{
                    prestamo.getNombreEstudiante(),
                    prestamo.getCursoEstudiante(),
                    prestamo.getTituloLibro(),
                    Fechas.formatear(prestamo.getFechaPrestamo()),
                    Fechas.formatear(prestamo.getFechaDevolucion()),
                    prestamo.getEstado(),
                    atraso == 0 ? "" : atraso
            });
        }
        return filas;
    }
}
