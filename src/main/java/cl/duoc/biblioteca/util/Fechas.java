package cl.duoc.biblioteca.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class Fechas {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Fechas() {
    }

    public static String formatear(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO);
    }
}
