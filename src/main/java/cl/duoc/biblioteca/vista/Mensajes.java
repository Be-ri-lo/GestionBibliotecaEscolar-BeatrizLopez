package cl.duoc.biblioteca.vista;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class Mensajes {

    private Mensajes() {
    }

    public static void info(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Biblioteca escolar", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Biblioteca escolar", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Biblioteca escolar", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String texto) {
        int respuesta = JOptionPane.showConfirmDialog(
                padre, texto, "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        return respuesta == JOptionPane.YES_OPTION;
    }
}
