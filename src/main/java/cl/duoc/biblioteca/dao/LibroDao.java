package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.Libro;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LibroDao extends DaoBase implements IDao<Libro> {

    private static final String SELECT_BASE = """
            SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria
            FROM libros l
            INNER JOIN categorias c ON c.id = l.id_categoria
            """;

    @Override
    public void crear(Libro libro) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, libro.getTitulo());
                ps.setString(2, libro.getAutor());
                ps.setString(3, libro.getIsbn());
                ps.setString(4, libro.getEditorial());
                ps.setInt(5, libro.getStock());
                ps.setInt(6, libro.getIdCategoria());
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        libro.setId(claves.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Libro buscarPorId(int id) throws SQLException {
        return buscar(SELECT_BASE + " WHERE l.id = ?", id, false);
    }

    /** Bloquea la fila del libro hasta que termine la transacción de préstamo. */
    public Libro buscarPorIdParaActualizar(int id) throws SQLException {
        return buscar(SELECT_BASE + " WHERE l.id = ? FOR UPDATE", id, true);
    }

    @Override
    public List<Libro> listarTodos() throws SQLException {
        synchronized (DatabaseConnection.class) {
            List<Libro> libros = new ArrayList<>();
            try (PreparedStatement ps = conectar().prepareStatement(SELECT_BASE + " ORDER BY l.titulo");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    libros.add(mapear(rs));
                }
            }
            return libros;
        }
    }

    @Override
    public void actualizar(Libro libro) throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    UPDATE libros
                    SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ?
                    WHERE id = ?
                    """;
            try (PreparedStatement ps = conectar().prepareStatement(sql)) {
                ps.setString(1, libro.getTitulo());
                ps.setString(2, libro.getAutor());
                ps.setString(3, libro.getIsbn());
                ps.setString(4, libro.getEditorial());
                ps.setInt(5, libro.getStock());
                ps.setInt(6, libro.getIdCategoria());
                ps.setInt(7, libro.getId());
                ps.executeUpdate();
            }
        }
    }

    public void actualizarStock(int idLibro, int stock) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement(
                    "UPDATE libros SET stock = ? WHERE id = ?")) {
                ps.setInt(1, stock);
                ps.setInt(2, idLibro);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        synchronized (DatabaseConnection.class) {
            try (PreparedStatement ps = conectar().prepareStatement("DELETE FROM libros WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    private Libro buscar(String sql, int id, boolean yaEnTransaccion) throws SQLException {
        if (yaEnTransaccion) {
            return ejecutarBusqueda(sql, id);
        }
        synchronized (DatabaseConnection.class) {
            return ejecutarBusqueda(sql, id);
        }
    }

    private Libro ejecutarBusqueda(String sql, int id) throws SQLException {
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

    private Libro mapear(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setId(rs.getInt("id"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setAutor(rs.getString("autor"));
        libro.setIsbn(rs.getString("isbn"));
        libro.setEditorial(rs.getString("editorial"));
        libro.setStock(rs.getInt("stock"));
        libro.setIdCategoria(rs.getInt("id_categoria"));
        libro.setNombreCategoria(rs.getString("categoria"));
        return libro;
    }
}
