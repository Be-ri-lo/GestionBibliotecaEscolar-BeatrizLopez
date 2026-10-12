package cl.duoc.biblioteca;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.vista.EstiloUI;
import cl.duoc.biblioteca.vista.VentanaLogin;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/**
 * Punto de entrada del sistema de biblioteca escolar.
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Biblioteca Escolar");
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            EstiloUI.aplicarNimbus();
        } catch (Exception e) {
            System.out.println("Se usará la apariencia por defecto: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.getInstance();
                VentanaLogin login = new VentanaLogin();
                login.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo conectar a MySQL.\n\n"
                                + e.getMessage()
                                + "\n\nEjecuta los scripts de la carpeta sql y revisa "
                                + "src/main/resources/database.properties.",
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
