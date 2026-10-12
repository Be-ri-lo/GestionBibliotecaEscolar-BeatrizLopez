package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorPrestamo;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.RecursoBibliografico;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;
import cl.duoc.biblioteca.util.Sesion;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/** Consulta del catálogo y solicitud de préstamo para el rol estudiante. */
public class PanelCatalogo extends JPanel implements PanelActualizable {

    private final Sesion sesion;
    private final ControladorPrestamo controlador;
    private final NotificadorEstado estado;
    private final JTextField busqueda = new JTextField(24);
    private final JTable tabla = new JTable();
    private final BotonPlano prestar = new BotonPlano("Solicitar préstamo", EstiloUI.AMARILLO);
    private DefaultTableModel modelo;
    private List<Libro> libros = new ArrayList<>();

    public PanelCatalogo(Sesion sesion, ControladorPrestamo controlador, NotificadorEstado estado) {
        this.sesion = sesion;
        this.controlador = controlador;
        this.estado = estado;
        setLayout(new BorderLayout(0, 12));
        setBackground(EstiloUI.FONDO);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        construir();
        recargar();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(8, 8));
        norte.setOpaque(false);
        JPanel titulos = new JPanel(new java.awt.GridLayout(2, 1));
        titulos.setOpaque(false);
        titulos.add(EstiloUI.titulo("Catálogo"));
        titulos.add(EstiloUI.subtitulo("Consulta los libros y solicita un préstamo. El plazo es de "
                + controlador.getDiasPrestamo() + " días."));
        norte.add(titulos, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        EstiloUI.estilizarCampo(busqueda);
        busqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
        });
        acciones.add(EstiloUI.etiqueta("Buscar"));
        acciones.add(busqueda);
        prestar.addActionListener(e -> prestar());
        acciones.add(prestar);
        norte.add(acciones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        modelo = EstiloUI.modelo(new String[]{"ID", "Tipo", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock", "Disponibilidad"});
        tabla.setModel(modelo);
        JScrollPane scroll = EstiloUI.prepararTabla(tabla, -1);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void actualizarDatos() {
        recargar();
    }

    private void recargar() {
        try {
            libros = controlador.listarLibros();
            llenarTabla();
            int disponibles = 0;
            for (Libro libro : libros) {
                if (libro.hayDisponibilidad()) {
                    disponibles++;
                }
            }
            estado.mostrarEstado("Catálogo: " + libros.size() + " libros, " + disponibles + " con stock.");
        } catch (BibliotecaException e) {
            Mensajes.error(this, e.getMessage());
        }
    }

    private void llenarTabla() {
        String filtro = busqueda.getText().trim().toLowerCase();
        EstiloUI.volcar(tabla, modelo, () -> {
            for (Libro libro : libros) {
                RecursoBibliografico recurso = libro;
                String texto = (recurso.getTitulo() + " " + libro.getAutor() + " " + libro.getIsbn()
                        + " " + libro.getNombreCategoria()).toLowerCase();
                if (!filtro.isEmpty() && !texto.contains(filtro)) {
                    continue;
                }
                modelo.addRow(new Object[]{
                        libro.getId(),
                        recurso.getTipoRecurso(),
                        recurso.getTitulo(),
                        libro.getAutor(),
                        libro.getIsbn(),
                        libro.getEditorial(),
                        libro.getNombreCategoria(),
                        recurso.getStock(),
                        recurso.hayDisponibilidad() ? "Disponible" : "Sin stock"
                });
            }
        });
    }

    private void prestar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.aviso(this, "Selecciona un libro de la tabla.");
            return;
        }
        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        Libro libro = null;
        for (Libro candidato : libros) {
            if (candidato.getId() == id) {
                libro = candidato;
                break;
            }
        }
        if (libro == null) {
            return;
        }
        if (!libro.hayDisponibilidad()) {
            Mensajes.aviso(this, "Ese libro no tiene ejemplares disponibles.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Solicitar \"" + libro.getTitulo() + "\"?")) {
            return;
        }
        prestar.setEnabled(false);
        estado.mostrarEstado("Solicitud enviada al hilo de préstamos…");
        Libro elegido = libro;
        controlador.prestarEnSegundoPlano(sesion.getEstudiante().getId(), elegido.getId(), resultado -> {
            prestar.setEnabled(true);
            if (resultado.isExito()) {
                Mensajes.info(this, resultado.getMensaje());
                estado.mostrarEstado(resultado.getMensaje());
                recargar();
            } else {
                Mensajes.aviso(this, resultado.getMensaje());
            }
        });
    }
}
