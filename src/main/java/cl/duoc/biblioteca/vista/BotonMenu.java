package cl.duoc.biblioteca.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Opción del menú lateral. */
public class BotonMenu extends JButton {

    private boolean seleccionado;

    public BotonMenu(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(EstiloUI.fuente(Font.PLAIN, 14));
        setForeground(EstiloUI.TEXTO);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 12));
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        setForeground(EstiloUI.TEXTO);
        setFont(EstiloUI.fuente(seleccionado ? Font.BOLD : Font.PLAIN, 14));
        repaint();
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (seleccionado) {
            g2.setColor(EstiloUI.AMARILLO);
        } else if (getModel().isRollover()) {
            g2.setColor(EstiloUI.AMARILLO_SUAVE);
        } else {
            g2.setColor(EstiloUI.FONDO);
        }
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
