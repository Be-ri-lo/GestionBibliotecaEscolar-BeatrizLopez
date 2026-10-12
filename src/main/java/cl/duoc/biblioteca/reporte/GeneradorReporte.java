package cl.duoc.biblioteca.reporte;

import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.ReporteDao;

import java.sql.SQLException;
import java.util.List;

/** Cada reporte concreto arma sus filas sin que la vista conozca el SQL. */
public interface GeneradorReporte {

    String getTitulo();

    String[] getColumnas();

    boolean requiereEstudiante();

    List<Object[]> generar(PrestamoDao prestamos, ReporteDao reportes, Integer idEstudiante) throws SQLException;
}
