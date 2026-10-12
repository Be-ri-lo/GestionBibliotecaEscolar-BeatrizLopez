package cl.duoc.biblioteca.util;

import java.util.ArrayList;
import java.util.List;

public final class Validador {

    private Validador() {
    }

    public static boolean rutValido(String rut) {
        String formateado = formatearRut(rut);
        if (!formateado.matches("\\d{7,8}-[0-9K]")) {
            return false;
        }
        String cuerpo = formateado.substring(0, formateado.length() - 2);
        String dv = formateado.substring(formateado.length() - 1);
        return dv.equals(calcularDv(cuerpo));
    }

    /** Deja el RUT como en el script de la guía: 12345678-9, sin puntos. */
    public static String formatearRut(String rut) {
        if (rut == null) {
            return "";
        }
        String limpio = rut.replace(".", "").replace(" ", "").trim().toUpperCase();
        if (limpio.matches("\\d{7,8}[0-9K]")) {
            int corte = limpio.length() - 1;
            return limpio.substring(0, corte) + "-" + limpio.substring(corte);
        }
        return limpio;
    }

    public static boolean cursoValido(String curso) {
        if (curso == null) {
            return false;
        }
        return curso.trim().matches("^(1ro|2do|3ro|4to) Medio [A-C]$");
    }

    public static List<String> cursos() {
        List<String> lista = new ArrayList<>();
        String[] niveles = {"1ro", "2do", "3ro", "4to"};
        String[] letras = {"A", "B", "C"};
        for (String nivel : niveles) {
            for (String letra : letras) {
                lista.add(nivel + " Medio " + letra);
            }
        }
        return lista;
    }

    public static boolean correoValido(String correo) {
        if (correo == null) {
            return false;
        }
        return correo.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static boolean isbnValido(String isbn) {
        if (isbn == null) {
            return false;
        }
        String limpio = isbn.replace("-", "").trim();
        return limpio.matches("\\d{10}|\\d{13}");
    }

    public static String normalizarIsbn(String isbn) {
        return isbn.replace("-", "").trim();
    }

    private static String calcularDv(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return "0";
        }
        if (resto == 10) {
            return "K";
        }
        return String.valueOf(resto);
    }
}
