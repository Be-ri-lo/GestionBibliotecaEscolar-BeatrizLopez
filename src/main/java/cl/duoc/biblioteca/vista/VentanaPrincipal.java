package cl.duoc.biblioteca.vista;

import cl.duoc.biblioteca.conexion.DatabaseConnection;
import cl.duoc.biblioteca.controlador.ControladorEstudiante;
import cl.duoc.biblioteca.controlador.ControladorLibro;
import cl.duoc.biblioteca.controlador.ControladorPrestamo;
import cl.duoc.biblioteca.controlador.ControladorReporte;
import cl.duoc.biblioteca.dao.CategoriaDao;
import cl.duoc.biblioteca.dao.EstudianteDao;
import cl.duoc.biblioteca.dao.LibroDao;
import cl.duoc.biblioteca.dao.PrestamoDao;
import cl.duoc.biblioteca.dao.ReporteDao;
import cl.duoc.biblioteca.dao.UsuarioDao;
import cl.duoc.biblioteca.modelo.Persona;
import cl.duoc.biblioteca.servicio.ServicioPrestamo;
import cl.duoc.biblioteca.util.Sesion;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class VentanaPrincipal extends JFrame implements NotificadorEstado {

    private final Sesion sesion;
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final JLabel estado = new JLabel("Listo");
    private final Map<String, JPanel> paneles = new LinkedHashMap<>();
    private final Map<String, BotonMenu> botones = new LinkedHashMap<>();

    public VentanaPrincipal(Sesion sesion) {
        this.sesion = sesion;
        setTitle("Biblioteca Escolar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cerrarConexion();
            }
        });
        construir();
    }

    private void construir() {
        UsuarioDao usuarioDao = new UsuarioDao();
        EstudianteDao estudianteDao = new EstudianteDao();
        LibroDao libroDao = new LibroDao();
        CategoriaDao categoriaDao = new CategoriaDao();
        PrestamoDao prestamoDao = new PrestamoDao();
        ReporteDao reporteDao = new ReporteDao();
        ServicioPrestamo servicio = new ServicioPrestamo(prestamoDao, libroDao, estudianteDao);

        ControladorLibro controladorLibro = new ControladorLibro(libroDao, categoriaDao, prestamoDao);
        ControladorEstudiante controladorEstudiante = new ControladorEstudiante(estudianteDao, usuarioDao, prestamoDao);
        ControladorPrestamo controladorPrestamo = new ControladorPrestamo(servicio, prestamoDao, estudianteDao, libroDao);
        ControladorReporte controladorReporte = new ControladorReporte(prestamoDao, reporteDao, estudianteDao);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstiloUI.FONDO);
        raiz.add(cabecera(), BorderLayout.NORTH);

        contenido.setBackground(EstiloUI.FONDO);
        if (sesion.esBibliotecario()) {
            agregar("Libros", new PanelLibros(controladorLibro, this));
            agregar("Estudiantes", new PanelEstudiantes(controladorEstudiante, this));
            agregar("Préstamos", new PanelPrestamos(sesion, controladorPrestamo, this));
            agregar("Reportes", new PanelReportes(controladorReporte, this));
        } else {
            agregar("Catálogo", new PanelCatalogo(sesion, controladorPrestamo, this));
            agregar("Mis préstamos", new PanelPrestamos(sesion, controladorPrestamo, this));
        }
        raiz.add(contenido, BorderLayout.CENTER);

        estado.setFont(EstiloUI.fuente(Font.PLAIN, 12));
        estado.setForeground(EstiloUI.TEXTO_SUAVE);
        estado.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, EstiloUI.BORDE),
                javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        estado.setOpaque(true);
        estado.setBackground(EstiloUI.TARJETA);
        raiz.add(estado, BorderLayout.SOUTH);

        setJMenuBar(barraMenu());
        setContentPane(raiz);
        mostrarPrimera();
    }

    private JPanel cabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EstiloUI.NEGRO);
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 4, 0, EstiloUI.AMARILLO),
                javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        JLabel titulo = new JLabel("Biblioteca Escolar");
        titulo.setFont(EstiloUI.fuente(Font.BOLD, 20));
        titulo.setForeground(java.awt.Color.WHITE);
        Persona persona = sesion.getUsuario();
        JLabel usuario = new JLabel(persona.getNombre() + "  ·  " + persona.obtenerRolDescriptivo());
        usuario.setFont(EstiloUI.fuente(Font.PLAIN, 14));
        usuario.setForeground(EstiloUI.AMARILLO);
        panel.add(titulo, BorderLayout.WEST);
        panel.add(usuario, BorderLayout.EAST);
        return panel;
    }

    private void agregar(String nombre, JPanel panel) {
        contenido.add(panel, nombre);
        paneles.put(nombre, panel);
        BotonMenu boton = new BotonMenu(nombre);
        boton.addActionListener(e -> mostrar(nombre));
        botones.put(nombre, boton);
    }

    private JMenuBar barraMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        JMenuItem salir = new JMenuItem("Cerrar sesión");
        salir.addActionListener(e -> cerrarSesion());
        JMenuItem cerrar = new JMenuItem("Salir");
        cerrar.addActionListener(e -> {
            cerrarConexion();
            System.exit(0);
        });
        archivo.add(salir);
        archivo.add(cerrar);

        JMenu gestion = new JMenu(sesion.esBibliotecario() ? "Gestión" : "Mi biblioteca");
        for (String nombre : nombresModulos()) {
            JMenuItem item = new JMenuItem(nombre);
            item.addActionListener(e -> mostrar(nombre));
            gestion.add(item);
        }

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acerca = new JMenuItem("Decisiones de diseño");
        acerca.addActionListener(e -> mostrarAyuda());
        ayuda.add(acerca);

        barra.add(archivo);
        barra.add(gestion);
        barra.add(ayuda);
        return barra;
    }

    private java.util.List<String> nombresModulos() {
        if (sesion.esBibliotecario()) {
            return java.util.List.of("Libros", "Estudiantes", "Préstamos", "Reportes");
        }
        return java.util.List.of("Catálogo", "Mis préstamos");
    }

    private void mostrarPrimera() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new javax.swing.BoxLayout(lateral, javax.swing.BoxLayout.Y_AXIS));
        lateral.setBackground(EstiloUI.FONDO);
        lateral.setPreferredSize(new Dimension(210, 0));
        lateral.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, EstiloUI.BORDE));
        for (BotonMenu boton : botones.values()) {
            boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            boton.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            lateral.add(boton);
        }
        getContentPane().add(lateral, BorderLayout.WEST);
        mostrar(nombresModulos().get(0));
    }

    private void mostrar(String nombre) {
        cartas.show(contenido, nombre);
        JPanel panel = paneles.get(nombre);
        if (panel instanceof PanelActualizable actualizable) {
            actualizable.actualizarDatos();
        }
        for (Map.Entry<String, BotonMenu> entrada : botones.entrySet()) {
            entrada.getValue().setSeleccionado(entrada.getKey().equals(nombre));
        }
        mostrarEstado("Módulo: " + nombre);
    }

    @Override
    public void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
    }

    private void cerrarSesion() {
        VentanaLogin login = new VentanaLogin();
        login.setVisible(true);
        dispose();
    }

    private void cerrarConexion() {
        try {
            DatabaseConnection.getInstance().cerrar();
        } catch (SQLException ignored) {
            // La conexión ya estaba cerrada.
        }
    }

    private void mostrarAyuda() {
        JTextArea area = new JTextArea(textoAyuda());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(EstiloUI.fuente(Font.PLAIN, 13));
        area.setBackground(EstiloUI.FONDO);
        area.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 320));
        JOptionPane.showMessageDialog(this, scroll, "Cómo está construido el sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    private String textoAyuda() {
        return """
                El sistema separa la interfaz, la lógica y los datos.

                Modelo: Persona, Libro, Prestamo y el resto de las entidades.
                Usuario y Estudiante heredan de Persona. Libro hereda de RecursoBibliografico.
                Vista: ventanas JFrame y paneles. No ejecutan SQL.
                Controlador: recibe la acción de la vista y llama al modelo.

                DAO: cada clase se encarga del SQL de una tabla e implementa la interfaz IDao.
                Singleton: DatabaseConnection.getInstance() abre una sola conexión con DriverManager.
                Hilos: el préstamo y la devolución corren en un Thread propio. La ventana sigue respondiendo
                mientras se graba el préstamo, se actualiza el stock y se confirma el historial.
                Sincronización: descontarStock y reponerStock son métodos synchronized. El préstamo completo
                además va en una transacción, así dos préstamos simultáneos no dejan el stock negativo.

                Plazo de préstamo: 14 días. Máximo: 3 libros activos por estudiante.
                """;
    }
}
