package cl.duoc.biblioteca;

import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.util.Validador;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasBibliotecaTest {

    @Test
    void validaElDigitoVerificadorDelRut() {
        assertTrue(Validador.rutValido("20.111.222-2"));
        assertTrue(Validador.rutValido("16.543.210-K"));
        assertEquals("16543210-K", Validador.formatearRut("16543210k"));
        assertFalse(Validador.rutValido("12345678-9"));
        assertFalse(Validador.rutValido("123"));
    }

    @Test
    void elCursoSoloAceptaElFormatoEscolar() {
        assertTrue(Validador.cursoValido("3ro Medio A"));
        assertTrue(Validador.cursoValido("4to Medio C"));
        assertFalse(Validador.cursoValido("tercero medio"));
        assertFalse(Validador.isbnValido("123"));
        assertTrue(Validador.isbnValido("9780441013593"));
    }

    @Test
    void marcaAtrasoCuandoLaDevolucionSuperaElVencimiento() {
        Prestamo prestamo = new Prestamo();
        prestamo.setDevuelto(false);
        prestamo.setFechaDevolucion(LocalDate.now().minusDays(3));
        assertTrue(prestamo.estaVencido());
        assertEquals(3, prestamo.diasDeAtraso());
        assertEquals("Atrasado", prestamo.getEstado());
    }

    @Test
    void unPrestamoVigenteNoFiguraComoAtrasado() {
        Prestamo prestamo = new Prestamo();
        prestamo.setDevuelto(false);
        prestamo.setFechaDevolucion(LocalDate.now().plusDays(4));
        assertFalse(prestamo.estaVencido());
        assertEquals("En préstamo", prestamo.getEstado());
    }
}
