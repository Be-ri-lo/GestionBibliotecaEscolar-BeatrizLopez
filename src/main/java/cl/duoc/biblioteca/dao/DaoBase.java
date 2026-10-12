package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.conexion.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

/** Operaciones JDBC compartidas por los DAO. */
public abstract class DaoBase {

    protected Connection conectar() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected Date aSql(LocalDate fecha) {
        if (fecha == null) {
            return null;
        }
        return Date.valueOf(fecha);
    }

    protected LocalDate aLocal(Date fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.toLocalDate();
    }
}
