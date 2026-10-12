package cl.duoc.biblioteca.controlador;

import cl.duoc.biblioteca.dao.CategoriaDao;
import cl.duoc.biblioteca.dao.LibroDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;
import cl.duoc.biblioteca.util.MensajeSql;
import cl.duoc.biblioteca.util.Validador;

import java.sql.SQLException;
import java.util.List;

public class ControladorLibro {

    private final LibroDao libroDao;
    private final CategoriaDao categoriaDao;
    private final PrestamoDao prestamoDao;

    public ControladorLibro(LibroDao libroDao, CategoriaDao categoriaDao, PrestamoDao prestamoDao) {
        this.libroDao = libroDao;
        this.categoriaDao = categoriaDao;
        this.prestamoDao = prestamoDao;
    }

    public List<Libro> listar() {
        try {
            return libroDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar los libros"));
        }
    }

    public List<Categoria> listarCategorias() {
        try {
            return categoriaDao.listarTodos();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BibliotecaException(MensajeSql.traducir(e, "cargar las categorías"));
        }
    }

    public ResultadoOperacion guardar(Libro libro) {
        String error = validar(libro);
        if (error != null) {
            return ResultadoOperacion.error(error);
        }
        libro.setTitulo(libro.getTitulo().trim());
        libro.setAutor(libro.getAutor().trim());
        libro.setIsbn(Validador.normalizarIsbn(libro.getIsbn()));
        libro.setEditorial(libro.getEditorial().trim());
        try {
            if (libro.getId() == 0) {
                libroDao.crear(libro);
                return ResultadoOperacion.ok("Libro registrado.");
            }
            if (libroDao.buscarPorId(libro.getId()) == null) {
                return ResultadoOperacion.error("El libro ya no existe.");
            }
            libroDao.actualizar(libro);
            return ResultadoOperacion.ok("Libro actualizado.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "guardar el libro"));
        }
    }

    public ResultadoOperacion eliminar(int id) {
        try {
            if (libroDao.buscarPorId(id) == null) {
                return ResultadoOperacion.error("El libro ya no existe.");
            }
            if (prestamoDao.existePorLibro(id)) {
                return ResultadoOperacion.error("No se puede eliminar el libro porque tiene préstamos asociados.");
            }
            libroDao.eliminar(id);
            return ResultadoOperacion.ok("Libro eliminado.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "eliminar el libro"));
        }
    }

    public ResultadoOperacion crearCategoria(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return ResultadoOperacion.error("Escribe el nombre de la categoría.");
        }
        try {
            Categoria categoria = new Categoria(0, nombre.trim());
            categoriaDao.crear(categoria);
            return ResultadoOperacion.ok("Categoría registrada.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "crear la categoría"));
        }
    }

    public ResultadoOperacion renombrarCategoria(Categoria categoria, String nombre) {
        if (categoria == null) {
            return ResultadoOperacion.error("Selecciona una categoría.");
        }
        if (nombre == null || nombre.isBlank()) {
            return ResultadoOperacion.error("Escribe el nombre de la categoría.");
        }
        try {
            categoria.setNombre(nombre.trim());
            categoriaDao.actualizar(categoria);
            return ResultadoOperacion.ok("Categoría actualizada.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "renombrar la categoría"));
        }
    }

    public ResultadoOperacion eliminarCategoria(Categoria categoria) {
        if (categoria == null) {
            return ResultadoOperacion.error("Selecciona una categoría.");
        }
        try {
            if (categoriaDao.tieneLibros(categoria.getId())) {
                return ResultadoOperacion.error("No se puede eliminar la categoría porque tiene libros asociados.");
            }
            categoriaDao.eliminar(categoria.getId());
            return ResultadoOperacion.ok("Categoría eliminada.");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResultadoOperacion.error(MensajeSql.traducir(e, "eliminar la categoría"));
        }
    }

    private String validar(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank()) {
            return "El título es obligatorio.";
        }
        if (libro.getAutor() == null || libro.getAutor().isBlank()) {
            return "El autor es obligatorio.";
        }
        if (!Validador.isbnValido(libro.getIsbn())) {
            return "El ISBN debe tener 10 o 13 dígitos.";
        }
        if (libro.getEditorial() == null || libro.getEditorial().isBlank()) {
            return "La editorial es obligatoria.";
        }
        if (libro.getStock() < 0) {
            return "El stock no puede ser negativo.";
        }
        if (libro.getIdCategoria() <= 0) {
            return "Selecciona una categoría.";
        }
        if (libro.getTitulo().trim().length() > 200 || libro.getAutor().trim().length() > 100
                || libro.getEditorial().trim().length() > 100) {
            return "Uno de los textos supera el largo permitido.";
        }
        return null;
    }
}
