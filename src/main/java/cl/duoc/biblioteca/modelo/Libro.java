package cl.duoc.biblioteca.modelo;

public class Libro extends RecursoBibliografico {

    private String autor;
    private String isbn;
    private String editorial;
    private int idCategoria;
    private String nombreCategoria;

    public Libro() {
    }

    @Override
    public String getTipoRecurso() {
        return "Libro";
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return getTitulo() + " — disponibles: " + getStock();
    }
}
