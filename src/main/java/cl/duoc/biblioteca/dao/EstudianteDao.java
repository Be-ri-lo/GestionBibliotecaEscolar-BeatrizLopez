package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.Estudiante;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDao extends DaoBase implements IDao<Estudiante> {

    @Override
    public void crear(Estudiante estudiante) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCurso());
                ps.setString(4, estudiante.getCorreo());
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        estudiante.setId(claves.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Estudiante buscarPorId(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE id = ?";
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
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

    public Estudiante buscarPorRut(String rut) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE rut = ?";
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setString(1, rut);
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
    public List<Estudiante> listarTodos() throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes ORDER BY nombre";
            List<Estudiante> estudiantes = new ArrayList<>();
            try (PreparedStatement ps = conectar().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    estudiantes.add(mapear(rs));
                }
            }
            return estudiantes;
        }
    }

    @Override
    public void actualizar(Estudiante estudiante) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getRut());
                ps.setString(3, estudiante.getCurso());
                ps.setString(4, estudiante.getCorreo());
                ps.setInt(5, estudiante.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement("DELETE FROM estudiantes WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(rs.getInt("id"));
        estudiante.setNombre(rs.getString("nombre"));
        estudiante.setRut(rs.getString("rut"));
        estudiante.setCurso(rs.getString("curso"));
        estudiante.setCorreo(rs.getString("correo"));
        return estudiante;
    }
}
