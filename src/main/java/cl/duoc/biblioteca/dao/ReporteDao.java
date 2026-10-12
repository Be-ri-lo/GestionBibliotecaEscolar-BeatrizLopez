package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.modelo.ResumenPrestamo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDao extends DaoBase {

    public List<ResumenPrestamo> librosMasPrestados() throws SQLException {
        synchronized (DatabaseConnection.class) {
            String sql = """
                    SELECT l.titulo, l.autor, COUNT(p.id) AS total
                    FROM prestamos p
                    INNER JOIN libros l ON l.id = p.id_libro
                    GROUP BY l.id, l.titulo, l.autor
                    ORDER BY total DESC, l.titulo
                    """;
            List<ResumenPrestamo> filas = new ArrayList<>();
            try (PreparedStatement ps = conectar().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new ResumenPrestamo(
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getInt("total")
                    ));
                }
            }
            return filas;
        }
    }
}
