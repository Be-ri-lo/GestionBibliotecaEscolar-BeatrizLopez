package cl.duoc.biblioteca.conexion;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Única conexión JDBC de la aplicación (patrón Singleton).
 * Todo el acceso a MySQL pasa por el mismo monitor para que los hilos
 * no usen la conexión al mismo tiempo.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private String url;
    private String usuario;
    private String clave;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        cargarConfiguracion();
        conectar();
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        synchronized (DatabaseConnection.class) {
            if (!conexionViva()) {
                conectar();
            }
            return connection;
        }
    }

    /**
     * Ejecuta trabajo JDBC en una transacción. Si el trabajo lanza una excepción,
     * se revierte. El monitor de la clase serializa este acceso con el resto de los DAO.
     */
    public <T> T enTransaccion(ProveedorSql<T> trabajo) throws SQLException {
        synchronized (DatabaseConnection.class) {
            Connection conn = getConnection();
            boolean autocommitPrevio = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                T resultado = trabajo.obtener();
                conn.commit();
                return resultado;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autocommitPrevio);
            }
        }
    }

    public synchronized void cerrar() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection = null;
        }
    }

    private void cargarConfiguracion() throws SQLException {
        Properties propiedades = new Properties();
        try (InputStream entrada = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (entrada == null) {
                throw new SQLException("No se encontró database.properties en los recursos del proyecto.");
            }
            propiedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("No se pudo leer database.properties.", e);
        }

        url = propiedades.getProperty("db.url", "").trim();
        usuario = propiedades.getProperty("db.user", "").trim();
        clave = propiedades.getProperty("db.password", "");
        if (url.isEmpty() || usuario.isEmpty()) {
            throw new SQLException("database.properties debe definir db.url y db.user.");
        }
    }

    private void conectar() throws SQLException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                connection = null;
            }
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver MySQL Connector/J.", e);
        }
        connection = DriverManager.getConnection(url, usuario, clave);
    }

    private boolean conexionViva() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
