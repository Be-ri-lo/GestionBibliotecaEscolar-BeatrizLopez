package cl.duoc.biblioteca.conexion;

import java.sql.SQLException;

@FunctionalInterface
public interface ProveedorSql<T> {
    T obtener() throws SQLException;
}
