package cl.duoc.biblioteca.util;

import java.sql.SQLException;

public final class MensajeSql {

    private MensajeSql() {
    }

    public static String traducir(SQLException error, String accion) {
        String crudo = error.getMessage() == null ? "" : error.getMessage().toLowerCase();
        if (crudo.contains("duplicate")) {
            if (crudo.contains("isbn")) {
                return "Ya existe un libro con ese ISBN.";
            }
            if (crudo.contains("correo")) {
                return "Ya existe un registro con ese correo.";
            }
            if (crudo.contains("rut")) {
                return "Ya existe un registro con ese RUT.";
            }
            if (crudo.contains("nombre")) {
                return "Ya existe una categoría con ese nombre.";
            }
            return "Ya existe un registro con esos datos.";
        }
        if (crudo.contains("unknown database")) {
            return "La base biblioteca no existe. Ejecuta los scripts de la carpeta sql.";
        }
        if (crudo.contains("communications link") || crudo.contains("connection refused")) {
            return "No hay conexión con MySQL. Verifica que el servidor esté encendido.";
        }
        return "No se pudo " + accion + ".";
    }
}
