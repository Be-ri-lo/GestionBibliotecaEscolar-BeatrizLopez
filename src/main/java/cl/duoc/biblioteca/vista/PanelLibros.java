package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorLibro;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Categoria;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

public class PanelLibros extends JPanel implements PanelActualizable {

    private final ControladorLibro controlador;
    private final NotificadorEstado estado;
    private final JTextField busqueda = new JTextField(24);
    private final JTextField titulo = new JTextField();
    private final JTextField autor = new JTextField();
    private final JTextField isbn = new JTextField();
    private final JTextField editorial = new JTextField();
    private final JSpinner stock = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
    private final JComboBox<Categoria> categorias = new JComboBox<>();
    private final JTable tabla = new JTable();
    private final JLabel modo = EstiloUI.subtitulo("Nuevo libro");
    private DefaultTableModel modelo;
    private List<Libro> libros = new ArrayList<>();
    private int idEditando;

    public PanelLibros(ControladorLibro controlador, NotificadorEstado estado) {
        this.controlador = controlador;
        this.estado = estado;
        setLayout(new BorderLayout(0, 12));
        setBackground(EstiloUI.FONDO);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        construir();
        recargar();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(12, 8));
        norte.setOpaque(false);
        JPanel titulos = new JPanel(new GridBagLayout());
        titulos.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        titulos.add(EstiloUI.titulo("Libros"), gc);
        gc.gridy = 1;
        titulos.add(EstiloUI.subtitulo("Inventario por categoría, autor, ISBN y stock disponible"), gc);
        norte.add(titulos, BorderLayout.WEST);

        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buscar.setOpaque(false);
        EstiloUI.estilizarCampo(busqueda);
        busqueda.setToolTipText("Título, autor, ISBN, editorial o categoría");
        busqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
        });
        buscar.add(EstiloUI.etiqueta("Buscar"));
        buscar.add(busqueda);
        norte.add(buscar, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        modelo = EstiloUI.modelo(new String[]{"ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Tipo", "Stock"});
        tabla.setModel(modelo);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        JScrollPane scroll = EstiloUI.prepararTabla(tabla, -1);
        add(scroll, BorderLayout.CENTER);

        JPanel formulario = EstiloUI.tarjeta("Datos del libro");
        formulario.setLayout(new GridBagLayout());
        formulario.setPreferredSize(new Dimension(400, 0));
        EstiloUI.estilizarCampo(titulo);
        EstiloUI.estilizarCampo(autor);
        EstiloUI.estilizarCampo(isbn);
        EstiloUI.estilizarCampo(editorial);
        EstiloUI.estilizarCombo(categorias);

        GridBagConstraints fila = new GridBagConstraints();
        fila.gridx = 0;
        fila.gridy = 0;
        fila.gridwidth = 2;
        fila.anchor = GridBagConstraints.WEST;
        formulario.add(modo, fila);
        EstiloUI.agregarFila(formulario, fila, 1, "Título", titulo);
        EstiloUI.agregarFila(formulario, fila, 2, "Autor", autor);
        EstiloUI.agregarFila(formulario, fila, 3, "ISBN", isbn);
        EstiloUI.agregarFila(formulario, fila, 4, "Editorial", editorial);
        EstiloUI.agregarFila(formulario, fila, 5, "Stock", stock);
        EstiloUI.agregarFila(formulario, fila, 6, "Categoría", categorias);

        BotonPlano guardar = new BotonPlano("Guardar", EstiloUI.AMARILLO);
        BotonPlano limpiar = new BotonPlano("Limpiar", EstiloUI.NEGRO);
        BotonPlano eliminar = new BotonPlano("Eliminar", EstiloUI.PELIGRO);
        guardar.addActionListener(e -> guardar());
        limpiar.addActionListener(e -> limpiar());
        eliminar.addActionListener(e -> eliminar());
        BotonPlano nueva = new BotonPlano("Nueva categoría", EstiloUI.NEGRO);
        BotonPlano renombrar = new BotonPlano("Renombrar", EstiloUI.NEGRO);
        BotonPlano borrarCategoria = new BotonPlano("Quitar categoría", EstiloUI.PELIGRO);
        nueva.addActionListener(e -> nuevaCategoria());
        renombrar.addActionListener(e -> renombrarCategoria());
        borrarCategoria.addActionListener(e -> eliminarCategoria());

        JPanel acciones = EstiloUI.filaBotones(guardar, limpiar, eliminar);
        JPanel categoriasAcciones = EstiloUI.filaBotones(nueva, renombrar, borrarCategoria);

        fila.gridx = 0;
        fila.gridy = 7;
        fila.gridwidth = 2;
        fila.weighty = 1;
        fila.fill = GridBagConstraints.NONE;
        fila.anchor = GridBagConstraints.NORTHWEST;
        fila.insets = new java.awt.Insets(12, 0, 4, 0);
        formulario.add(acciones, fila);
        fila.gridy = 8;
        fila.weighty = 0;
        formulario.add(categoriasAcciones, fila);
        add(formulario, BorderLayout.EAST);
    }

    @Override
    public void actualizarDatos() {
        recargar();
    }

    private void recargar() {
        try {
            libros = controlador.listar();
            cargarCategorias();
            llenarTabla();
            estado.mostrarEstado("Libros cargados: " + libros.size());
        } catch (BibliotecaException e) {
            Mensajes.error(this, e.getMessage());
        }
    }

    private void cargarCategorias() {
        Categoria seleccion = (Categoria) categorias.getSelectedItem();
        categorias.removeAllItems();
        for (Categoria categoria : controlador.listarCategorias()) {
            categorias.addItem(categoria);
        }
        if (seleccion != null) {
            for (int i = 0; i < categorias.getItemCount(); i++) {
                if (categorias.getItemAt(i).getId() == seleccion.getId()) {
                    categorias.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void llenarTabla() {
        String filtro = busqueda.getText().trim().toLowerCase();
        EstiloUI.volcar(tabla, modelo, () -> {
            for (Libro libro : libros) {
                String texto = (libro.getTitulo() + " " + libro.getAutor() + " " + libro.getIsbn()
                        + " " + libro.getEditorial() + " " + libro.getNombreCategoria()).toLowerCase();
                if (!filtro.isEmpty() && !texto.contains(filtro)) {
                    continue;
                }
                modelo.addRow(new Object[]{
                        libro.getId(),
                        libro.getTitulo(),
                        libro.getAutor(),
                        libro.getIsbn(),
                        libro.getEditorial(),
                        libro.getNombreCategoria(),
                        libro.getTipoRecurso(),
                        libro.getStock()
                });
            }
        });
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        for (Libro libro : libros) {
            if (libro.getId() == id) {
                idEditando = libro.getId();
                titulo.setText(libro.getTitulo());
                autor.setText(libro.getAutor());
                isbn.setText(libro.getIsbn());
                editorial.setText(libro.getEditorial());
                stock.setValue(libro.getStock());
                seleccionarCategoria(libro.getIdCategoria());
                modo.setText("Editando libro #" + libro.getId());
                break;
            }
        }
    }

    private void seleccionarCategoria(int idCategoria) {
        for (int i = 0; i < categorias.getItemCount(); i++) {
            if (categorias.getItemAt(i).getId() == idCategoria) {
                categorias.setSelectedIndex(i);
                return;
            }
        }
    }

    private void guardar() {
        Categoria categoria = (Categoria) categorias.getSelectedItem();
        Libro libro = new Libro();
        libro.setId(idEditando);
        libro.setTitulo(titulo.getText());
        libro.setAutor(autor.getText());
        libro.setIsbn(isbn.getText());
        libro.setEditorial(editorial.getText());
        libro.setStock((Integer) stock.getValue());
        libro.setIdCategoria(categoria == null ? 0 : categoria.getId());
        ResultadoOperacion resultado = controlador.guardar(libro);
        if (resultado.isExito()) {
            Mensajes.info(this, resultado.getMensaje());
            limpiar();
            recargar();
        } else {
            Mensajes.aviso(this, resultado.getMensaje());
        }
    }

    private void eliminar() {
        if (idEditando == 0) {
            Mensajes.aviso(this, "Selecciona un libro de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Eliminar el libro seleccionado?")) {
            return;
        }
        ResultadoOperacion resultado = controlador.eliminar(idEditando);
        if (resultado.isExito()) {
            Mensajes.info(this, resultado.getMensaje());
            limpiar();
            recargar();
        } else {
            Mensajes.aviso(this, resultado.getMensaje());
        }
    }

    private void limpiar() {
        idEditando = 0;
        titulo.setText("");
        autor.setText("");
        isbn.setText("");
        editorial.setText("");
        stock.setValue(1);
        modo.setText("Nuevo libro");
        tabla.clearSelection();
    }

    private void nuevaCategoria() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la categoría:");
        if (nombre == null) {
            return;
        }
        ResultadoOperacion resultado = controlador.crearCategoria(nombre);
        mostrarYRecargarCategorias(resultado);
    }

    private void renombrarCategoria() {
        Categoria categoria = (Categoria) categorias.getSelectedItem();
        if (categoria == null) {
            Mensajes.aviso(this, "No hay categorías cargadas.");
            return;
        }
        String nombre = JOptionPane.showInputDialog(this, "Nuevo nombre:", categoria.getNombre());
        if (nombre == null) {
            return;
        }
        ResultadoOperacion resultado = controlador.renombrarCategoria(categoria, nombre);
        mostrarYRecargarCategorias(resultado);
    }

    private void eliminarCategoria() {
        Categoria categoria = (Categoria) categorias.getSelectedItem();
        if (categoria == null) {
            Mensajes.aviso(this, "No hay categorías cargadas.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Eliminar la categoría \"" + categoria.getNombre() + "\"?")) {
            return;
        }
        ResultadoOperacion resultado = controlador.eliminarCategoria(categoria);
        mostrarYRecargarCategorias(resultado);
    }

    private void mostrarYRecargarCategorias(ResultadoOperacion resultado) {
        if (resultado.isExito()) {
            Mensajes.info(this, resultado.getMensaje());
            recargar();
        } else {
            Mensajes.aviso(this, resultado.getMensaje());
        }
    }
}
