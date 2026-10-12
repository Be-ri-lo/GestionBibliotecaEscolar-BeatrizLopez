package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorEstudiante;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.modelo.Persona;
import cl.duoc.biblioteca.servicio.ResultadoOperacion;

import cl.duoc.biblioteca.util.Validador;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

public class PanelEstudiantes extends JPanel implements PanelActualizable {

    private final ControladorEstudiante controlador;
    private final NotificadorEstado estado;
    private final JTextField busqueda = new JTextField(24);
    private final JTextField nombre = new JTextField();
    private final JTextField rut = new JTextField();
    private final JComboBox<String> curso = new JComboBox<>();
    private final JTextField correo = new JTextField();
    private final JPasswordField clave = new JPasswordField();
    private final JLabel modo = EstiloUI.subtitulo("Nuevo estudiante");
    private final JLabel ficha = EstiloUI.etiqueta(" ");
    private final JTable tabla = new JTable();
    private DefaultTableModel modelo;
    private List<Estudiante> estudiantes = new ArrayList<>();
    private int idEditando;
    private String rutOriginal = "";

    public PanelEstudiantes(ControladorEstudiante controlador, NotificadorEstado estado) {
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
        titulos.add(EstiloUI.titulo("Estudiantes"), gc);
        gc.gridy = 1;
        titulos.add(EstiloUI.subtitulo("Ficha personal y acceso al sistema"), gc);
        norte.add(titulos, BorderLayout.WEST);

        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buscar.setOpaque(false);
        EstiloUI.estilizarCampo(busqueda);
        busqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { llenarTabla(); }
        });
        buscar.add(EstiloUI.etiqueta("Buscar"));
        buscar.add(busqueda);
        norte.add(buscar, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        modelo = EstiloUI.modelo(new String[]{"ID", "Nombre", "RUT", "Curso", "Correo", "Perfil"});
        tabla.setModel(modelo);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        JScrollPane scroll = EstiloUI.prepararTabla(tabla, -1);
        add(scroll, BorderLayout.CENTER);

        JPanel formulario = EstiloUI.tarjeta("Datos del estudiante");
        formulario.setLayout(new GridBagLayout());
        formulario.setPreferredSize(new Dimension(360, 0));
        EstiloUI.estilizarCampo(nombre);
        EstiloUI.estilizarCampo(rut);
        EstiloUI.estilizarCampo(correo);
        EstiloUI.estilizarCampo(clave);
        EstiloUI.estilizarCombo(curso);
        for (String opcion : Validador.cursos()) {
            curso.addItem(opcion);
        }

        GridBagConstraints fila = new GridBagConstraints();
        fila.gridx = 0;
        fila.gridy = 0;
        fila.gridwidth = 2;
        fila.anchor = GridBagConstraints.WEST;
        formulario.add(modo, fila);
        EstiloUI.agregarFila(formulario, fila, 1, "Nombre", nombre);
        EstiloUI.agregarFila(formulario, fila, 2, "RUT", rut);
        EstiloUI.agregarFila(formulario, fila, 3, "Curso", curso);
        EstiloUI.agregarFila(formulario, fila, 4, "Correo", correo);
        EstiloUI.agregarFila(formulario, fila, 5, "Clave de acceso", clave);

        fila.gridy = 6;
        fila.gridx = 0;
        fila.gridwidth = 2;
        fila.insets = new java.awt.Insets(4, 4, 4, 4);
        formulario.add(ficha, fila);

        JLabel ayudaClave = EstiloUI.subtitulo("<html>Curso: 1ro, 2do, 3ro o 4to Medio, letra A, B o C.<br>Al editar, deja la clave vacía si no quieres cambiarla.</html>");
        fila.gridy = 7;
        formulario.add(ayudaClave, fila);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        BotonPlano guardar = new BotonPlano("Guardar", EstiloUI.AMARILLO);
        BotonPlano limpiar = new BotonPlano("Limpiar", EstiloUI.NEGRO);
        BotonPlano eliminar = new BotonPlano("Eliminar", EstiloUI.PELIGRO);
        guardar.addActionListener(e -> guardar());
        limpiar.addActionListener(e -> limpiar());
        eliminar.addActionListener(e -> eliminar());
        acciones.add(guardar);
        acciones.add(limpiar);
        acciones.add(eliminar);

        fila.gridy = 8;
        fila.weighty = 1;
        fila.anchor = GridBagConstraints.NORTHWEST;
        formulario.add(acciones, fila);
        add(formulario, BorderLayout.EAST);
    }

    @Override
    public void actualizarDatos() {
        recargar();
    }

    private void recargar() {
        try {
            estudiantes = controlador.listar();
            llenarTabla();
            estado.mostrarEstado("Estudiantes cargados: " + estudiantes.size());
        } catch (BibliotecaException e) {
            Mensajes.error(this, e.getMessage());
        }
    }

    private void llenarTabla() {
        String filtro = busqueda.getText().trim().toLowerCase();
        EstiloUI.volcar(tabla, modelo, () -> {
            for (Estudiante estudiante : estudiantes) {
                Persona persona = estudiante;
                String texto = (estudiante.getNombre() + " " + estudiante.getRut() + " "
                        + estudiante.getCurso() + " " + estudiante.getCorreo()).toLowerCase();
                if (!filtro.isEmpty() && !texto.contains(filtro)) {
                    continue;
                }
                modelo.addRow(new Object[]{
                        estudiante.getId(),
                        estudiante.getNombre(),
                        estudiante.getRut(),
                        estudiante.getCurso(),
                        estudiante.getCorreo(),
                        persona.obtenerRolDescriptivo()
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
        for (Estudiante estudiante : estudiantes) {
            if (estudiante.getId() == id) {
                idEditando = estudiante.getId();
                rutOriginal = estudiante.getRut();
                nombre.setText(estudiante.getNombre());
                rut.setText(estudiante.getRut());
                curso.setSelectedItem(estudiante.getCurso());
                correo.setText(estudiante.getCorreo());
                clave.setText("");
                modo.setText("Editando estudiante #" + estudiante.getId());
                ficha.setText(estudiante.obtenerRolDescriptivo());
                break;
            }
        }
    }

    private void guardar() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(idEditando);
        estudiante.setNombre(nombre.getText());
        estudiante.setRut(rut.getText());
        estudiante.setCurso(curso.getSelectedItem() == null ? "" : curso.getSelectedItem().toString());
        estudiante.setCorreo(correo.getText());
        ResultadoOperacion resultado = controlador.guardar(estudiante, new String(clave.getPassword()), rutOriginal);
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
            Mensajes.aviso(this, "Selecciona un estudiante de la tabla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Eliminar al estudiante y su acceso al sistema?")) {
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
        rutOriginal = "";
        nombre.setText("");
        rut.setText("");
        curso.setSelectedIndex(0);
        correo.setText("");
        clave.setText("");
        modo.setText("Nuevo estudiante");
        ficha.setText(" ");
        tabla.clearSelection();
    }
}
