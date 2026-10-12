package cl.duoc.biblioteca.vista;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;

public final class EstiloUI {

    /** Amarillo institucional Duoc UC (#FFD100) y negro de la marca. */
    public static final Color AMARILLO = new Color(255, 209, 0);
    public static final Color AMARILLO_SUAVE = new Color(255, 243, 196);
    public static final Color NEGRO = new Color(20, 20, 20);
    public static final Color FONDO = new Color(245, 245, 245);
    public static final Color TARJETA = Color.WHITE;
    public static final Color TEXTO = new Color(26, 26, 26);
    public static final Color TEXTO_SUAVE = new Color(90, 90, 90);
    public static final Color BORDE = new Color(220, 220, 220);
    public static final Color PELIGRO = new Color(153, 40, 40);
    public static final Color ATRASO = new Color(255, 232, 230);

    private EstiloUI() {
    }

    public static void aplicarNimbus() {
        UIManager.put("control", FONDO);
        UIManager.put("nimbusBase", NEGRO);
        UIManager.put("nimbusBlueGrey", new Color(210, 210, 210));
        UIManager.put("text", TEXTO);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.alternateRowColor", new Color(255, 250, 230));
    }

    public static Font fuente(int estilo, int tamano) {
        return new Font("SansSerif", estilo, tamano);
    }

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.BOLD, 22));
        etiqueta.setForeground(NEGRO);
        return etiqueta;
    }

    public static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.PLAIN, 13));
        etiqueta.setForeground(TEXTO_SUAVE);
        return etiqueta;
    }

    public static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.PLAIN, 13));
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    public static void estilizarCampo(JTextField campo) {
        campo.setFont(fuente(Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    /** Quita el fondo gris de Nimbus. El combo queda transparente y solo muestra el borde. */
    public static void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(fuente(Font.PLAIN, 14));
        combo.setOpaque(false);
        combo.setBackground(new Color(0, 0, 0, 0));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)
        ));
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                comboBox.setOpaque(false);
                comboBox.setBackground(new Color(0, 0, 0, 0));
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            }

            @Override
            protected JButton createArrowButton() {
                JButton flecha = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(TEXTO);
                        int ancho = 8;
                        int x = (getWidth() - ancho) / 2;
                        int y = getHeight() / 2 - 2;
                        g2.fillPolygon(
                                new int[]{x, x + ancho, x + ancho / 2},
                                new int[]{y, y, y + 5},
                                3
                        );
                        g2.dispose();
                    }
                };
                flecha.setOpaque(false);
                flecha.setContentAreaFilled(false);
                flecha.setBorderPainted(false);
                flecha.setFocusable(false);
                return flecha;
            }
        });
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index < 0) {
                    setOpaque(false);
                    setBackground(new Color(0, 0, 0, 0));
                    setForeground(TEXTO);
                } else if (isSelected) {
                    setOpaque(true);
                    setBackground(AMARILLO_SUAVE);
                    setForeground(TEXTO);
                } else {
                    setOpaque(true);
                    setBackground(Color.WHITE);
                    setForeground(TEXTO);
                }
                return this;
            }
        });
    }

    public static JPanel tarjeta(String titulo) {
        JPanel panel = new JPanel();
        panel.setBackground(TARJETA);
        TitledBorder borde = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDE), titulo);
        borde.setTitleFont(fuente(Font.BOLD, 13));
        borde.setTitleColor(NEGRO);
        panel.setBorder(BorderFactory.createCompoundBorder(borde, BorderFactory.createEmptyBorder(8, 10, 10, 10)));
        return panel;
    }

    public static void agregarFila(JPanel panel, GridBagConstraints gc, int fila, String texto, JComponent campo) {
        gc.gridx = 0;
        gc.gridy = fila;
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(5, 4, 5, 8);
        panel.add(etiqueta(texto), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 4);
        panel.add(campo, gc);
    }

    public static DefaultTableModel modelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JScrollPane prepararTabla(JTable tabla, int columnaEstado) {
        estilizarTabla(tabla, columnaEstado);
        Container padre = tabla.getParent();
        if (padre instanceof JViewport vista && vista.getParent() instanceof JScrollPane existente) {
            return existente;
        }
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** Recarga las filas sin romper el ordenador de la tabla. */
    public static void volcar(JTable tabla, DefaultTableModel modelo, Runnable carga) {
        tabla.clearSelection();
        tabla.setRowSorter(null);
        modelo.setRowCount(0);
        carga.run();
        tabla.setRowSorter(new TableRowSorter<>(modelo));
        tabla.revalidate();
        tabla.repaint();
    }

    public static JPanel filaBotones(JComponent... componentes) {
        JPanel fila = new JPanel(new java.awt.GridLayout(1, Math.max(componentes.length, 1), 8, 0));
        fila.setOpaque(false);
        for (JComponent componente : componentes) {
            fila.add(componente);
        }
        return fila;
    }

    private static void estilizarTabla(JTable tabla, int columnaEstado) {
        tabla.setFont(fuente(Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(AMARILLO_SUAVE);
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                celda.setBackground(NEGRO);
                celda.setForeground(Color.WHITE);
                celda.setFont(fuente(Font.BOLD, 13));
                celda.setHorizontalAlignment(SwingConstants.LEFT);
                celda.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
                celda.setOpaque(true);
                return celda;
            }
        };
        for (int i = 0; i < tabla.getColumnModel().getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setHeaderRenderer(encabezado);
        }

        if (columnaEstado >= 0) {
            DefaultTableCellRenderer cuerpo = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                               boolean hasFocus, int row, int column) {
                    Component celda = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    int filaModelo = table.convertRowIndexToModel(row);
                    Object estado = table.getModel().getValueAt(filaModelo, columnaEstado);
                    String texto = estado == null ? "" : estado.toString();
                    if (!isSelected) {
                        if ("Atrasado".equals(texto)) {
                            celda.setBackground(ATRASO);
                        } else {
                            celda.setBackground(Color.WHITE);
                        }
                    }
                    celda.setForeground(TEXTO);
                    if (celda instanceof JLabel etiqueta) {
                        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    }
                    return celda;
                }
            };
            for (int i = 0; i < tabla.getColumnModel().getColumnCount(); i++) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(cuerpo);
            }
        }
    }

    public static void cursorMano(JButton boton) {
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
