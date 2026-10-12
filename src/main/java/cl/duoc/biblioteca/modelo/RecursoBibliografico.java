package cl.duoc.biblioteca.modelo;

/**
 * Material que la biblioteca puede prestar. Libro es la implementación actual;
 * la disponibilidad se resuelve aquí para que el préstamo no dependa de un tipo concreto.
 */
public abstract class RecursoBibliografico {

    private int id;
    private String titulo;
    private int stock;

    protected RecursoBibliografico() {
    }

    protected RecursoBibliografico(int id, String titulo, int stock) {
        this.id = id;
        this.titulo = titulo;
        this.stock = stock;
    }

    public abstract String getTipoRecurso();

    public boolean hayDisponibilidad() {
        return stock > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
