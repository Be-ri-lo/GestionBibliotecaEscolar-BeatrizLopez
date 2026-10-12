package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.controlador.ControladorAutenticacion;
import cl.duoc.biblioteca.excepcion.BibliotecaException;
import cl.duoc.biblioteca.modelo.Persona;
import cl.duoc.biblioteca.util.Sesion;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class VentanaLogin extends JFrame {

    private final ControladorAutenticacion controlador;
    private final JTextField campoCorreo = new JTextField(20);
    private final JPasswordField campoClave = new JPasswordField(20);
    private final JLabel mensaje = new JLabel(" ");

    public VentanaLogin() {
        this(new ControladorAutenticacion(
                new cl.duoc.biblioteca.dao.UsuarioDao(),
                new cl.duoc.biblioteca.dao.EstudianteDao()
        ));
    }

    public VentanaLogin(ControladorAutenticacion controlador) {
        this.controlador = controlador;
        setTitle("Biblioteca Escolar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(null);
        construir();
    }

    private void construir() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstiloUI.FONDO);

        JPanel cabecera = new JPanel(new GridBagLayout());
        cabecera.setBackground(EstiloUI.NEGRO);
        cabecera.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 6, 0, EstiloUI.AMARILLO));
        cabecera.setPreferredSize(new Dimension(460, 150));
        JLabel titulo = new JLabel("Biblioteca Escolar");
        titulo.setFont(EstiloUI.fuente(Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        JLabel detalle = new JLabel("Registro, préstamo y devolución de libros");
        detalle.setFont(EstiloUI.fuente(Font.PLAIN, 14));
        detalle.setForeground(EstiloUI.AMARILLO);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        cabecera.add(titulo, gc);
        gc.gridy = 1;
        gc.insets = new java.awt.Insets(8, 0, 0, 0);
        cabecera.add(detalle, gc);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(EstiloUI.FONDO);
        formulario.setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 40, 20, 40));
        EstiloUI.estilizarCampo(campoCorreo);
        EstiloUI.estilizarCampo(campoClave);
        campoCorreo.setText("antonia@correo.cl");

        GridBagConstraints fila = new GridBagConstraints();
        fila.gridx = 0;
        fila.gridy = 0;
        fila.weightx = 1;
        fila.fill = GridBagConstraints.HORIZONTAL;
        fila.anchor = GridBagConstraints.WEST;
        fila.insets = new java.awt.Insets(0, 0, 4, 0);
        formulario.add(EstiloUI.etiqueta("Correo"), fila);
        fila.gridy = 1;
        fila.insets = new java.awt.Insets(0, 0, 14, 0);
        formulario.add(campoCorreo, fila);
        fila.gridy = 2;
        fila.insets = new java.awt.Insets(0, 0, 4, 0);
        formulario.add(EstiloUI.etiqueta("Contraseña"), fila);
        fila.gridy = 3;
        fila.insets = new java.awt.Insets(0, 0, 16, 0);
        formulario.add(campoClave, fila);

        BotonPlano ingresar = new BotonPlano("Ingresar", EstiloUI.AMARILLO);
        ingresar.addActionListener(e -> ingresar());
        fila.gridy = 4;
        fila.insets = new java.awt.Insets(0, 0, 10, 0);
        formulario.add(ingresar, fila);

        mensaje.setFont(EstiloUI.fuente(Font.PLAIN, 13));
        mensaje.setForeground(EstiloUI.PELIGRO);
        fila.gridy = 5;
        formulario.add(mensaje, fila);

        JLabel ayuda = new JLabel("<html>Bibliotecario: antonia@correo.cl / clave123<br>"
                + "Estudiante: carlos@correo.cl / clave123</html>");
        ayuda.setFont(EstiloUI.fuente(Font.PLAIN, 12));
        ayuda.setForeground(EstiloUI.TEXTO_SUAVE);
        fila.gridy = 6;
        fila.insets = new java.awt.Insets(18, 0, 0, 0);
        formulario.add(ayuda, fila);

        JLabel pie = new JLabel("Evaluación Final Transversal · Desarrollo Orientado a Objetos II", JLabel.CENTER);
        pie.setFont(EstiloUI.fuente(Font.PLAIN, 11));
        pie.setForeground(EstiloUI.TEXTO_SUAVE);
        pie.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 16, 0));

        getRootPane().setDefaultButton(ingresar);
        campoClave.addActionListener(e -> ingresar());

        raiz.add(cabecera, BorderLayout.NORTH);
        raiz.add(formulario, BorderLayout.CENTER);
        raiz.add(pie, BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private void ingresar() {
        mensaje.setText(" ");
        try {
            String clave = new String(campoClave.getPassword());
            Sesion sesion = controlador.iniciarSesion(campoCorreo.getText(), clave);
            Persona persona = sesion.getUsuario();
            VentanaPrincipal principal = new VentanaPrincipal(sesion);
            principal.setVisible(true);
            dispose();
            principal.mostrarEstado("Sesión iniciada como " + persona.obtenerRolDescriptivo() + ".");
        } catch (BibliotecaException e) {
            mensaje.setText(e.getMessage());
        }
    }
}
