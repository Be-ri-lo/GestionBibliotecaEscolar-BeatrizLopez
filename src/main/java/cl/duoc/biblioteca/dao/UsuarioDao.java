package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.Rol;
import cl.duoc.biblioteca.modelo.Usuario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao extends DaoBase implements IDao<Usuario> {

    @Override
    public void crear(Usuario usuario) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    INSERT INTO usuarios (nombre, rut, correo, contraseña, rol)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getRut());
                ps.setString(3, usuario.getCorreo());
                ps.setString(4, usuario.getContrasena());
                ps.setString(5, usuario.getRol().name().toLowerCase());
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        usuario.setId(claves.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios WHERE id = ?";
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

    public Usuario buscarPorCorreo(String correo) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    SELECT id, nombre, rut, correo, contraseña, rol
                    FROM usuarios
                    WHERE LOWER(correo) = LOWER(?)
                    """;
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapear(rs);
                    }
                    return null;
                }
            }
        }
    }

    public Usuario buscarPorRut(String rut) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios WHERE rut = ?";
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
    public List<Usuario> listarTodos() throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "SELECT id, nombre, rut, correo, contraseña, rol FROM usuarios ORDER BY nombre";
            List<Usuario> usuarios = new ArrayList<>();
            try (PreparedStatement ps = conectar().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapear(rs));
                }
            }
            return usuarios;
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    UPDATE usuarios
                    SET nombre = ?, rut = ?, correo = ?, contraseña = ?, rol = ?
                    WHERE id = ?
                    """;
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getRut());
                ps.setString(3, usuario.getCorreo());
                ps.setString(4, usuario.getContrasena());
                ps.setString(5, usuario.getRol().name().toLowerCase());
                ps.setInt(6, usuario.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setRut(rs.getString("rut"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contraseña"));
        usuario.setRol(Rol.desdeTexto(rs.getString("rol")));
        return usuario;
    }
}
