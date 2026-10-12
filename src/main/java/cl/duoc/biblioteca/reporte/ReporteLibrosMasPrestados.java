package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.ReporteDao;
import cl.duoc.biblioteca.modelo.ResumenPrestamo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteLibrosMasPrestados implements GeneradorReporte {

    @Override
    public String getTitulo() {
        return "Libros más prestados";
    }

    @Override
    public String[] getColumnas() {
        return new String[]{"Título", "Autor", "Total de préstamos"};
    }

    @Override
    public boolean requiereEstudiante() {
        return false;
    }

    @Override
    public List<Object[]> generar(PrestamoDao prestamos, ReporteDao reportes, Integer idEstudiante) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        for (ResumenPrestamo resumen : reportes.librosMasPrestados()) {
            filas.add(new Object[]{resumen.getTitulo(), resumen.getAutor(), resumen.getTotal()});
        }
        return filas;
    }
}
