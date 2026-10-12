package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorReporte;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Estudiante;
import cl.duoc.biblioteca.reporte.GeneradorReporte;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class PanelReportes extends JPanel implements PanelActualizable {

    private final ControladorReporte controlador;
    private final NotificadorEstado estado;
    private final JComboBox<GeneradorReporte> reportes = new JComboBox<>();
    private final JComboBox<Estudiante> estudiantes = new JComboBox<>();
    private final JLabel etiquetaEstudiante = EstiloUI.etiqueta("Estudiante");
    private final JTable tabla = new JTable();
    private DefaultTableModel modelo;
    private boolean cargando;

    public PanelReportes(ControladorReporte controlador, NotificadorEstado estado) {
        this.controlador = controlador;
        this.estado = estado;
        setLayout(new BorderLayout(0, 12));
        setBackground(EstiloUI.FONDO);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        construir();
        generar();
    }

    private void construir() {
        JPanel norte = new JPanel(new BorderLayout(0, 10));
        norte.setOpaque(false);
        JPanel titulos = new JPanel(new java.awt.GridLayout(2, 1));
        titulos.setOpaque(false);
        titulos.add(EstiloUI.titulo("Reportes"));
        titulos.add(EstiloUI.subtitulo("Libros más prestados, historial y préstamos vigentes"));
        norte.add(titulos, BorderLayout.NORTH);

        for (GeneradorReporte generador : controlador.listarGeneradores()) {
            reportes.addItem(generador);
        }
        recargarEstudiantes();
        EstiloUI.estilizarCombo(reportes);
        EstiloUI.estilizarCombo(estudiantes);
        reportes.setPreferredSize(new java.awt.Dimension(280, 32));
        estudiantes.setPreferredSize(new java.awt.Dimension(260, 32));
        reportes.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                                                                   int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GeneradorReporte generador) {
                    setText(generador.getTitulo());
                }
                if (index < 0) {
                    setOpaque(false);
                    setBackground(new java.awt.Color(0, 0, 0, 0));
                    setForeground(EstiloUI.TEXTO);
                } else if (isSelected) {
                    setOpaque(true);
                    setBackground(EstiloUI.AMARILLO_SUAVE);
                    setForeground(EstiloUI.TEXTO);
                } else {
                    setOpaque(true);
                    setBackground(java.awt.Color.WHITE);
                    setForeground(EstiloUI.TEXTO);
                }
                return this;
            }
        });
        reportes.addActionListener(e -> {
            actualizarEstudianteVisible();
            generar();
        });
        estudiantes.addActionListener(e -> {
            if (!cargando && estudiantes.isVisible()) {
                generar();
            }
        });

        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 6, 4, 6);
        gc.gridy = 0;
        gc.gridx = 0;
        acciones.add(EstiloUI.etiqueta("Informe"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        acciones.add(reportes, gc);
        BotonPlano generar = new BotonPlano("Generar", EstiloUI.AMARILLO);
        generar.addActionListener(e -> generar());
        gc.gridx = 2;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        acciones.add(generar, gc);
        gc.gridy = 1;
        gc.gridx = 0;
        acciones.add(etiquetaEstudiante, gc);
        gc.gridx = 1;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        acciones.add(estudiantes, gc);
        norte.add(acciones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        modelo = EstiloUI.modelo(new String[]{"Resultado"});
        tabla.setModel(modelo);
        JScrollPane scroll = EstiloUI.prepararTabla(tabla, -1);
        add(scroll, BorderLayout.CENTER);
        actualizarEstudianteVisible();
    }

    @Override
    public void actualizarDatos() {
        recargarEstudiantes();
        generar();
    }

    private void recargarEstudiantes() {
        Estudiante seleccionado = (Estudiante) estudiantes.getSelectedItem();
        cargando = true;
        try {
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
        } catch (BibliotecaException e) {
            Mensajes.error(this, e.getMessage());
        } finally {
            cargando = false;
        }
    }

    private void actualizarEstudianteVisible() {
        GeneradorReporte generador = (GeneradorReporte) reportes.getSelectedItem();
        boolean requiere = generador != null && generador.requiereEstudiante();
        etiquetaEstudiante.setVisible(requiere);
        estudiantes.setVisible(requiere);
        revalidate();
        repaint();
    }

    private void generar() {
        GeneradorReporte generador = (GeneradorReporte) reportes.getSelectedItem();
        if (generador == null) {
            return;
        }
        Integer idEstudiante = null;
        if (generador.requiereEstudiante()) {
            Estudiante estudiante = (Estudiante) estudiantes.getSelectedItem();
            if (estudiante == null) {
                Mensajes.aviso(this, "Selecciona un estudiante para ver su historial.");
                return;
            }
            idEstudiante = estudiante.getId();
        }
        try {
            List<Object[]> filas = controlador.ejecutar(generador, idEstudiante);
            modelo = EstiloUI.modelo(generador.getColumnas());
            for (Object[] fila : filas) {
                modelo.addRow(fila);
            }
            tabla.setModel(modelo);
            int columnaEstado = columnaEstado(generador.getColumnas());
            EstiloUI.prepararTabla(tabla, columnaEstado);
            estado.mostrarEstado(generador.getTitulo() + ": " + filas.size() + " fila(s).");
            if (filas.isEmpty()) {
                Mensajes.info(this, "El reporte no tiene registros para mostrar.");
            }
        } catch (BibliotecaException e) {
            Mensajes.error(this, e.getMessage());
        }
    }

    private int columnaEstado(String[] columnas) {
        for (int i = 0; i < columnas.length; i++) {
            if ("Estado".equals(columnas[i])) {
                return i;
            }
        }
        return -1;
    }
}
