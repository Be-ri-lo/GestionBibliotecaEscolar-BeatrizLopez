package cl.duoc.biblioteca.vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Botón con color propio, independiente del look and feel. */
public class BotonPlano extends JButton {

    private final Color fondo;
    private final Color fondoActivo;

    public BotonPlano(String texto, Color fondo) {
        super(texto);
        this.fondo = fondo;
        this.fondoActivo = fondo.darker();
        setUI(new BasicButtonUI());
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textoSobre(fondo));
        setFont(EstiloUI.fuente(Font.BOLD, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        Dimension tamano = getPreferredSize();
        setPreferredSize(new Dimension(Math.max(tamano.width, 96), Math.max(tamano.height, 36)));
        setMinimumSize(getPreferredSize());
    }

    @Override
    public void updateUI() {
        setUI(new BasicButtonUI());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!isEnabled()) {
            g2.setColor(new Color(180, 180, 180));
        } else if (getModel().isPressed() || getModel().isRollover()) {
            g2.setColor(fondoActivo);
        } else {
            g2.setColor(fondo);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        super.paintComponent(g);
    }

    /** El amarillo Duoc lleva texto negro; el negro y el rojo llevan texto blanco. */
    private static Color textoSobre(Color fondo) {
        int luminancia = (fondo.getRed() * 299 + fondo.getGreen() * 587 + fondo.getBlue() * 114) / 1000;
        return luminancia > 160 ? new Color(20, 20, 20) : Color.WHITE;
    }
}
