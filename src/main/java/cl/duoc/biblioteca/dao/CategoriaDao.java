package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.Categoria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao extends DaoBase implements IDao<Categoria> {

    @Override
    public void crear(Categoria categoria) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = "INSERT INTO categorias (nombre) VALUES (?)";
            try (PreparedStatement ps = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, categoria.getNombre());
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        categoria.setId(claves.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement(
                    "SELECT id, nombre FROM categorias WHERE id = ?")) {
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
    public List<Categoria> listarTodos() throws SQLException {
        synchronized (DatabaseConnection.class) {
            List<Categoria> categorias = new ArrayList<>();
            try (PreparedStatement ps = conectar().prepareStatement(
                    "SELECT id, nombre FROM categorias ORDER BY nombre");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(mapear(rs));
                }
            }
            return categorias;
        }
    }

    @Override
    public void actualizar(Categoria categoria) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement(
                    "UPDATE categorias SET nombre = ? WHERE id = ?")) {
                ps.setString(1, categoria.getNombre());
                ps.setInt(2, categoria.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement("DELETE FROM categorias WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    public boolean tieneLibros(int idCategoria) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement(
                    "SELECT COUNT(*) FROM libros WHERE id_categoria = ?")) {
                ps.setInt(1, idCategoria);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id"), rs.getString("nombre"));
    }
}
