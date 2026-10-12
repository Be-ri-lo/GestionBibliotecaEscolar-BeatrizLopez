package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorPrestamo;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Libro;
import cl.duoc.biblioteca.modelo.Prestamo;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;
import cl.duoc.biblioteca.util.Fechas;
import cl.duoc.biblioteca.util.Sesion;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PanelPrestamos extends JPanel implements PanelActualizable {

    private final Sesion sesion;
    private final ControladorPrestamo controlador;
    private final NotificadorEstado estado;
    private final JComboBox<Estudiante> estudiantes = new JComboBox<>();
    private final JComboBox<Libro> libros = new JComboBox<>();
    private final JTextField busquedaAlumno = new JTextField();
    private final JComboBox<String> filtro = new JComboBox<>(new String[]{
            "Todos", "En préstamo", "Atrasados", "Devueltos"
    });
    private final JLabel plazo = new JLabel();
    private final JLabel proceso = new JLabel(" ");
    private final JTable tabla = new JTable();
    private final BotonPlano prestar = new BotonPlano("Registrar préstamo", EstiloUI.AMARILLO);
    private final BotonPlano devolver = new BotonPlano("Registrar devolución", EstiloUI.NEGRO);
    private DefaultTableModel modelo;
    private List<Prestamo> prestamos = new ArrayList<>();

    public PanelPrestamos(Sesion sesion, ControladorPrestamo controlador, NotificadorEstado estado) {
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
        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        String titulo = sesion.esBibliotecario() ? "Préstamos y devoluciones" : "Mis préstamos";
        String detalle = sesion.esBibliotecario()
                ? "El registro corre en segundo plano y actualiza stock e historial"
                : "Consulta tus libros, detecta atrasos y registra la devolución";
        JPanel titulos = new JPanel(new java.awt.GridLayout(2, 1));
        titulos.setOpaque(false);
        titulos.add(EstiloUI.titulo(titulo));
        titulos.add(EstiloUI.subtitulo(detalle));
        norte.add(titulos, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new java.awt.GridBagLayout());
        acciones.setOpaque(false);
        EstiloUI.estilizarCombo(estudiantes);
        EstiloUI.estilizarCombo(libros);
        EstiloUI.estilizarCombo(filtro);
        estudiantes.setPreferredSize(new java.awt.Dimension(220, 32));
        libros.setPreferredSize(new java.awt.Dimension(260, 32));
        filtro.setPreferredSize(new java.awt.Dimension(140, 32));
        busquedaAlumno.setPreferredSize(new java.awt.Dimension(220, 32));
        EstiloUI.estilizarCampo(busquedaAlumno);
        prestar.addActionListener(e -> prestar());
        devolver.addActionListener(e -> devolver());
        filtro.addActionListener(e -> llenarTabla());
        busquedaAlumno.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { llenarTabla(); }
            public void removeUpdate(DocumentEvent e) { llenarTabla(); }
            public void changedUpdate(DocumentEvent e) { llenarTabla(); }
        });

        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.anchor = java.awt.GridBagConstraints.WEST;
        gc.insets = new java.awt.Insets(4, 6, 4, 6);
        gc.gridy = 0;
        int columna = 0;
        if (sesion.esBibliotecario()) {
            gc.gridx = columna++;
            acciones.add(EstiloUI.etiqueta("Estudiante"), gc);
            gc.gridx = columna++;
            acciones.add(estudiantes, gc);
        }
        gc.gridx = columna++;
        acciones.add(EstiloUI.etiqueta("Libro"), gc);
        gc.gridx = columna;
        acciones.add(libros, gc);

        JPanel botones = EstiloUI.filaBotones(prestar, devolver);
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 4;
        acciones.add(botones, gc);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(EstiloUI.etiqueta("Buscar alumno"));
        filtros.add(busquedaAlumno);
        filtros.add(EstiloUI.etiqueta("Ver"));
        filtros.add(filtro);
        gc.gridy = 2;
        acciones.add(filtros, gc);
        norte.add(acciones, BorderLayout.CENTER);

        LocalDate vencimiento = LocalDate.now().plusDays(controlador.getDiasPrestamo());
        plazo.setText("Plazo: " + controlador.getDiasPrestamo() + " días (vencería el "
                + Fechas.formatear(vencimiento) + "). Máximo " + controlador.getMaximoActivos()
                + " préstamos activos por estudiante.");
        plazo.setFont(EstiloUI.fuente(java.awt.Font.PLAIN, 13));
        plazo.setForeground(EstiloUI.TEXTO_SUAVE);
        proceso.setFont(EstiloUI.fuente(java.awt.Font.BOLD, 13));
        proceso.setForeground(EstiloUI.NEGRO);
        JPanel avisos = new JPanel(new java.awt.GridLayout(2, 1, 0, 4));
        avisos.setOpaque(false);
        avisos.add(plazo);
        avisos.add(proceso);
        norte.add(avisos, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        modelo = EstiloUI.modelo(new String[]{
                "ID", "Estudiante", "Curso", "Libro", "Préstamo", "Vencimiento", "Estado"
        });
        tabla.setModel(modelo);
        JScrollPane scroll = EstiloUI.prepararTabla(tabla, 6);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void actualizarDatos() {
        recargar();
    }

    private void recargar() {
        try {
            if (sesion.esBibliotecario()) {
                prestamos = controlador.listar();
                cargarEstudiantes();
            } else {
                prestamos = controlador.listarPorEstudiante(sesion.getEstudiante().getId());
            }
            cargarLibros();
            llenarTabla();
        } catch (BibliotecaException e) {
            Mensajes.error(this, e.getMessage());
        }
    }

    private void cargarEstudiantes() {
        Estudiante seleccionado = (Estudiante) estudiantes.getSelectedItem();
        estudiantes.removeAllItems();
        for (Estudiante estudiante : controlador.listarEstudiantes()) {
            estudiantes.addItem(estudiante);
        }
        if (seleccionado != null) {
            for (int i = 0; i < estudiantes.getItemCount(); i++) {
                if (estudiantes.getItemAt(i).getId() == seleccionado.getId()) {
                    estudiantes.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void cargarLibros() {
        libros.removeAllItems();
        for (Libro libro : controlador.listarLibrosDisponibles()) {
            libros.addItem(libro);
        }
    }

    private void llenarTabla() {
        if (modelo == null) {
            return;
        }
        String seleccionado = (String) filtro.getSelectedItem();
        String alumno = busquedaAlumno.getText();
        EstiloUI.volcar(tabla, modelo, () -> {
            for (Prestamo prestamo : prestamos) {
                if (!coincideFiltro(prestamo, seleccionado) || !coincideAlumno(prestamo, alumno)) {
                    continue;
                }
                modelo.addRow(new Object[]{
                        prestamo.getId(),
                        prestamo.getNombreEstudiante(),
                        prestamo.getCursoEstudiante(),
                        prestamo.getTituloLibro(),
                        Fechas.formatear(prestamo.getFechaPrestamo()),
                        Fechas.formatear(prestamo.getFechaDevolucion()),
                        prestamo.getEstado()
                });
            }
        });
        String texto = alumno == null ? "" : alumno.trim();
        if (texto.isEmpty()) {
            estado.mostrarEstado("Préstamos en pantalla: " + modelo.getRowCount());
        } else {
            estado.mostrarEstado("Libros de \"" + texto + "\": " + modelo.getRowCount());
        }
    }

    private boolean coincideAlumno(Prestamo prestamo, String texto) {
        if (texto == null || texto.isBlank()) {
            return true;
        }
        String buscado = texto.trim().toLowerCase();
        String nombre = prestamo.getNombreEstudiante() == null ? "" : prestamo.getNombreEstudiante().toLowerCase();
        String curso = prestamo.getCursoEstudiante() == null ? "" : prestamo.getCursoEstudiante().toLowerCase();
        return nombre.contains(buscado) || curso.contains(buscado);
    }

    private boolean coincideFiltro(Prestamo prestamo, String filtroSeleccionado) {
        if (filtroSeleccionado == null || "Todos".equals(filtroSeleccionado)) {
            return true;
        }
        if ("En préstamo".equals(filtroSeleccionado)) {
            return !prestamo.isDevuelto() && !prestamo.estaVencido();
        }
        if ("Atrasados".equals(filtroSeleccionado)) {
            return prestamo.estaVencido();
        }
        return prestamo.isDevuelto();
    }

    private void prestar() {
        Estudiante estudiante = estudianteSeleccionado();
        Libro libro = (Libro) libros.getSelectedItem();
        if (estudiante == null) {
            Mensajes.aviso(this, "Selecciona un estudiante.");
            return;
        }
        if (libro == null) {
            Mensajes.aviso(this, "No hay libros con stock disponible.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Prestar \"" + libro.getTitulo() + "\" a " + estudiante.getNombre() + "?")) {
            return;
        }
        prestar.setEnabled(false);
        devolver.setEnabled(false);
        proceso.setText("Registrando el préstamo en segundo plano. Puedes seguir usando la ventana.");
        estado.mostrarEstado("Hilo de préstamo en ejecución…");
        controlador.prestarEnSegundoPlano(estudiante.getId(), libro.getId(), this::terminarOperacion);
    }

    private void devolver() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.aviso(this, "Selecciona un préstamo de la tabla.");
            return;
        }
        int id = Integer.parseInt(tabla.getValueAt(fila, 0).toString());
        String libro = tabla.getValueAt(fila, 3).toString();
        String situacion = tabla.getValueAt(fila, 6).toString();
        if ("Devuelto".equals(situacion)) {
            Mensajes.aviso(this, "Ese préstamo ya fue devuelto.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Registrar la devolución de \"" + libro + "\"?")) {
            return;
        }
        prestar.setEnabled(false);
        devolver.setEnabled(false);
        proceso.setText("Registrando la devolución en segundo plano…");
        estado.mostrarEstado("Hilo de devolución en ejecución…");
        controlador.devolverEnSegundoPlano(id, this::terminarOperacion);
    }

    private void terminarOperacion(ResultadoOperacion resultado) {
        prestar.setEnabled(true);
        devolver.setEnabled(true);
        proceso.setText(resultado.getMensaje());
        estado.mostrarEstado(resultado.getMensaje());
        if (resultado.isExito()) {
            Mensajes.info(this, resultado.getMensaje());
            recargar();
        } else {
            Mensajes.aviso(this, resultado.getMensaje());
        }
    }

    private Estudiante estudianteSeleccionado() {
        if (!sesion.esBibliotecario()) {
            return sesion.getEstudiante();
        }
        return (Estudiante) estudiantes.getSelectedItem();
    }
}
