package cl.duoc.biblioteca.modelo;

/** Fila del reporte de libros más prestados. */
public class ResumenPrestamo {

    private final String titulo;
    private final String autor;
    private final int total;

    public ResumenPrestamo(String titulo, String autor, int total) {
        this.titulo = titulo;
        this.autor = autor;
        this.total = total;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public int getTotal() {
        return total;
    }
}
