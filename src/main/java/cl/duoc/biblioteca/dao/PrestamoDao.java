package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.Prestamo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDao extends DaoBase implements IDao<Prestamo> {

    private static final String SELECT_BASE = """
            SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion,
                   p.devuelto,
                   e.nombre AS nombre_estudiante, e.curso AS curso_estudiante,
                   l.titulo AS titulo_libro
            FROM prestamos p
            INNER JOIN estudiantes e ON e.id = p.id_estudiante
            INNER JOIN libros l ON l.id = p.id_libro
            """;

    @Override
    public void crear(Prestamo prestamo) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    INSERT INTO prestamos
                        (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, prestamo.getIdEstudiante());
                ps.setInt(2, prestamo.getIdLibro());
                ps.setDate(3, aSql(prestamo.getFechaPrestamo()));
                ps.setDate(4, aSql(prestamo.getFechaDevolucion()));
                ps.setBoolean(5, prestamo.isDevuelto());
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        prestamo.setId(claves.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Prestamo buscarPorId(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement(SELECT_BASE + " WHERE p.id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapear(rs);
                    }
                    return null;
                }
            }
        }
    }

    @Override
    public List<Prestamo> listarTodos() throws SQLException {
        return listar(SELECT_BASE + " ORDER BY p.fecha_prestamo DESC, p.id DESC");
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        synchronized (DatabaseConnection.class) {
            List<Prestamo> prestamos = new ArrayList<>();
            String sql = SELECT_BASE + " WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC";
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        prestamos.add(mapear(rs));
                    }
                }
            }
            return prestamos;
        }
    }

    public List<Prestamo> listarActivos() throws SQLException {
        return listar(SELECT_BASE + " WHERE p.devuelto = 0 ORDER BY p.fecha_devolucion, p.id");
    }

    public boolean existePrestamoActivo(int idEstudiante, int idLibro) throws SQLException {
        return contar("""
                SELECT COUNT(*) FROM prestamos
                WHERE id_estudiante = ? AND id_libro = ? AND devuelto = 0
                """, idEstudiante, idLibro) > 0;
    }

    public int contarActivos(int idEstudiante) throws SQLException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND devuelto = 0", idEstudiante);
    }

    public boolean existePorEstudiante(int idEstudiante) throws SQLException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ?", idEstudiante) > 0;
    }

    public boolean existePorLibro(int idLibro) throws SQLException {
        return contar("SELECT COUNT(*) FROM prestamos WHERE id_libro = ?", idLibro) > 0;
    }

    public void marcarDevuelto(Prestamo prestamo) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "UPDATE prestamos SET devuelto = ? WHERE id = ?";
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setBoolean(1, prestamo.isDevuelto());
                ps.setInt(2, prestamo.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void actualizar(Prestamo prestamo) throws SQLException {
        marcarDevuelto(prestamo);
    }

    @Override
    public void eliminar(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    private List<Prestamo> listar(String sql) throws SQLException {
        synchronized (DatabaseConnection.class) {
            List<Prestamo> prestamos = new ArrayList<>();
            try (PreparedStatement ps = conectar().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapear(rs));
                }
            }
            return prestamos;
        }
    }

    private int contar(String sql, int... parametros) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                for (int i = 0; i < parametros.length; i++) {
                    ps.setInt(i + 1, parametros[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setId(rs.getInt("id"));
        prestamo.setIdEstudiante(rs.getInt("id_estudiante"));
        prestamo.setIdLibro(rs.getInt("id_libro"));
        prestamo.setFechaPrestamo(aLocal(rs.getDate("fecha_prestamo")));
        prestamo.setFechaDevolucion(aLocal(rs.getDate("fecha_devolucion")));
        prestamo.setDevuelto(rs.getBoolean("devuelto"));
        prestamo.setNombreEstudiante(rs.getString("nombre_estudiante"));
        prestamo.setCursoEstudiante(rs.getString("curso_estudiante"));
        prestamo.setTituloLibro(rs.getString("titulo_libro"));
        return prestamo;
    }
}
