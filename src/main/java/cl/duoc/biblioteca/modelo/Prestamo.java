package cl.duoc.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;
    private String nombreEstudiante;
    private String cursoEstudiante;
    private String tituloLibro;

    public boolean estaVencido() {
        return !devuelto && fechaDevolucion != null && LocalDate.now().isAfter(fechaDevolucion);
    }

    public long diasDeAtraso() {
        if (!estaVencido() || fechaDevolucion == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaDevolucion, LocalDate.now());
    }

    public String getEstado() {
        if (devuelto) {
            return "Devuelto";
        }
        if (estaVencido()) {
            return "Atrasado";
        }
        return "En préstamo";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public boolean isDevuelto() {
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public String getCursoEstudiante() {
        return cursoEstudiante;
    }

    public void setCursoEstudiante(String cursoEstudiante) {
        this.cursoEstudiante = cursoEstudiante;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }
}
