package cl.duoc.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato CRUD de un DAO. Cada entidad implementa estas operaciones
 * contra su propia tabla.
 */
public interface IDao<T> {

    void crear(T entidad) throws SQLException;

    T buscarPorId(int id) throws SQLException;

    List<T> listarTodos() throws SQLException;

    void actualizar(T entidad) throws SQLException;

    void eliminar(int id) throws SQLException;
}
