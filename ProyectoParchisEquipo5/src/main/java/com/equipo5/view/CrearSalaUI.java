package com.equipo5.view;

import com.equipo5.net.Servidor;
import javax.swing.*;
import java.awt.*;

public class CrearSalaUI extends JFrame {

    // Referencia a la ventana anterior para poder volver si se cancela
    private final JFrame ventanaAnterior;
    
    // Componentes de la interfaz que necesitamos leer después
    private JTextField txtNickname;
    private JComboBox<String> cbNumJugadores;

    // Constructor que recibe la ventana anterior (MenuUI)
    public CrearSalaUI(JFrame ventanaAnterior) {
        this.ventanaAnterior = ventanaAnterior;
        initComponents();
    }

    private void initComponents() {
        setTitle("Configurar Partida - Parchís Equipo 5");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);

        // --- Panel Principal con Diseño Vertical ---
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10)); // 5 filas, 1 columna
        panel.setBackground(new Color(102, 0, 153)); // Fondo Morado Oscuro
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); // Margen interno

        // 1. Título
        JLabel lblTitulo = new JLabel("Configuración de Sala", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        // 2. Campo para el Nickname
        JPanel panelNombre = new JPanel(new BorderLayout());
        panelNombre.setOpaque(false); // Hacer transparente para ver el morado de fondo
        JLabel lblNick = new JLabel("Tu Nickname (Anfitrión):");
        lblNick.setForeground(Color.WHITE);
        lblNick.setFont(new Font("Arial", Font.BOLD, 14));
        txtNickname = new JTextField();
        panelNombre.add(lblNick, BorderLayout.NORTH);
        panelNombre.add(txtNickname, BorderLayout.CENTER);

        // 3. Selector de Cantidad de Jugadores
        JPanel panelJugadores = new JPanel(new BorderLayout());
        panelJugadores.setOpaque(false);
        JLabel lblJugadores = new JLabel("Número de Jugadores:");
        lblJugadores.setForeground(Color.WHITE);
        lblJugadores.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Opciones del ComboBox
        String[] opciones = {"2 Jugadores", "3 Jugadores", "4 Jugadores"};
        cbNumJugadores = new JComboBox<>(opciones);
        panelJugadores.add(lblJugadores, BorderLayout.NORTH);
        panelJugadores.add(cbNumJugadores, BorderLayout.CENTER);

        // 4. Botón Crear Sala (Verde)
        JButton btnIniciar = new JButton("CREAR SALA E INICIAR");
        btnIniciar.setBackground(new Color(0, 204, 102)); // Verde Brillante
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 14));
        btnIniciar.setFocusPainted(false);
        // Acción al pulsar el botón
        btnIniciar.addActionListener(e -> iniciarPartida());

        // 5. Botón Volver (Rojo)
        JButton btnVolver = new JButton("Cancelar");
        btnVolver.setBackground(new Color(204, 0, 0)); // Rojo Oscuro
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 14));
        btnVolver.setFocusPainted(false);
        // Acción al pulsar volver
        btnVolver.addActionListener(e -> {
            this.dispose(); // Cierra esta ventana
            ventanaAnterior.setVisible(true); // Muestra el menú de nuevo
        });

        // Agregar todo al panel
        panel.add(lblTitulo);
        panel.add(panelNombre);
        panel.add(panelJugadores);
        panel.add(btnIniciar);
        panel.add(btnVolver);

        // Agregar panel a la ventana
        add(panel);
    }

    // Lógica al presionar "Crear Sala"
    private void iniciarPartida() {
        // 1. Validar que haya escrito un nombre
        String nickname = txtNickname.getText().trim();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa un nombre para ser el anfitrión.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Obtener configuración (aunque por ahora el servidor aceptará hasta 4)
        String seleccion = (String) cbNumJugadores.getSelectedItem();
        System.out.println("Configuración seleccionada: " + seleccion);

        // 3. INICIAR EL SERVIDOR BLACKBOARD (En un hilo separado)
        // Usamos un Thread para no congelar la ventana visual mientras el servidor espera conexiones
        new Thread(() -> {
            try {
                // Instanciamos el Servidor en el puerto 5000
                Servidor servidor = new Servidor(5000);
                servidor.iniciar(); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        // 4. Feedback al usuario
        JOptionPane.showMessageDialog(this, 
            "¡Sala Creada Exitosamente!\n" +
            "Servidor escuchando en puerto 5000.\n" +
            "Tu eres el anfitrión: " + nickname,
            "Sala Lista", JOptionPane.INFORMATION_MESSAGE);

        // 5. Abrir la ventana del juego (Tablero)
        // Pasamos true al setVisible para mostrar el tablero
        TableroUI tablero = new TableroUI();
        tablero.setVisible(true);

        // 6. Cerrar esta ventana de configuración
        this.dispose();
    }
}