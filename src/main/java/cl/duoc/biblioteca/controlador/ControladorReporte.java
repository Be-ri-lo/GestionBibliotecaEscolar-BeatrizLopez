package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.ReporteDao;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.reporte.GeneradorReporte;
import cl.duoc.biblioteca.reporte.ReporteHistorialEstudiante;
import cl.duoc.biblioteca.reporte.ReporteLibrosEnPrestamo;
import cl.duoc.biblioteca.reporte.ReporteLibrosMasPrestados;
import cl.duoc.biblioteca.util.MensajeSql;

import java.sql.SQLException;
import java.util.List;

public class ControladorReporte {

    private final PrestamoDao prestamoDao;
    private final ReporteDao reporteDao;
    private final EstudianteDao estudianteDao;
    private final List<GeneradorReporte> generadores;

    public ControladorReporte(PrestamoDao prestamoDao, ReporteDao reporteDao, EstudianteDao estudianteDao) {
        this.prestamoDao = prestamoDao;
        this.reporteDao = reporteDao;
        this.estudianteDao = estudianteDao;
        this.generadores = List.of(
                new ReporteLibrosMasPrestados(),
                new ReporteHistorialEstudiante(),
                new ReporteLibrosEnPrestamo()
        );
    }

    public List<GeneradorReporte> listarGeneradores() {
        return generadores;
    }

    public List<Estudiante> listarEstudiantes() {
        try {
            return estudianteDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los estudiantes"));
        }
    }

    public List<Object[]> ejecutar(GeneradorReporte generador, Integer idEstudiante) {
        try {
            return generador.generar(prestamoDao, reporteDao, idEstudiante);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "generar el reporte"));
        }
    }
}
