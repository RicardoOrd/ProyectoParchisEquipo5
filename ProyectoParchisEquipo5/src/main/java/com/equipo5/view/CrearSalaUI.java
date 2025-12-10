package com.equipo5.view;

import com.equipo5.net.Cliente;
import com.equipo5.net.Servidor;
import javax.swing.*;
import java.awt.*;

public class CrearSalaUI extends JFrame {

    private final JFrame ventanaAnterior;
    private JTextField txtNickname;
    private JComboBox<String> cbNumJugadores;
    private JCheckBox chkPrivada;

    // --- NUEVOS COMPONENTES (UC-01 y UC-04) ---
    private JComboBox<String> cbAvatar; // Para elegir avatar
    private JCheckBox chkTresSeises;    // Regla: 3 seises penalizan
    private JCheckBox chkComer20;       // Regla: Comer da 20 pasos
    private JCheckBox chkSalida5;       // Regla: Salir obligatoriamente con 5

    public CrearSalaUI(JFrame ventanaAnterior) {
        this.ventanaAnterior = ventanaAnterior;
        initComponents();
    }

    private void initComponents() {
        setTitle("Configurar Partida");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 620); // Altura ajustada para que quepan las reglas
        setLocationRelativeTo(null);
        setResizable(false);

        // Usamos 8 filas para acomodar: Título, Nick, Avatar, Jugadores, Reglas, Privada, Crear, Volver
        JPanel panel = new JPanel(new GridLayout(8, 1, 10, 10));
        panel.setBackground(new Color(102, 0, 153)); // Morado
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 1. Título
        JLabel lblTitulo = new JLabel("Configuración de Sala", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        // 2. Nickname
        JPanel panelNombre = crearPanelCampo("Tu Nickname:", txtNickname = new JTextField());

        // 3. Avatar (UC-01)
        String[] avatares = {"🐶 Perro", "🐱 Gato", "🤖 Robot", "👽 Alien"};
        cbAvatar = new JComboBox<>(avatares);
        JPanel panelAvatar = crearPanelCampo("Elige tu Avatar:", cbAvatar);

        // 4. Número de Jugadores
        JPanel panelJugadores = new JPanel(new BorderLayout());
        panelJugadores.setOpaque(false);
        JLabel lblJugadores = new JLabel("Máximo de Jugadores:");
        lblJugadores.setForeground(Color.WHITE);
        lblJugadores.setFont(new Font("Arial", Font.BOLD, 14));

        String[] opciones = {"2 Jugadores", "3 Jugadores", "4 Jugadores"};
        cbNumJugadores = new JComboBox<>(opciones);
        cbNumJugadores.setSelectedIndex(2); // Default 4

        panelJugadores.add(lblJugadores, BorderLayout.NORTH);
        panelJugadores.add(cbNumJugadores, BorderLayout.CENTER);

        // 5. Panel de Reglas Especiales (UC-04)
        JPanel panelReglas = new JPanel(new GridLayout(3, 1));
        panelReglas.setOpaque(false);
        // Borde con título para agrupar las reglas visualmente
        panelReglas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), "Reglas Especiales",
                0, 0, new Font("Arial", Font.BOLD, 12), Color.WHITE));

        chkTresSeises = crearCheck("Castigo por tres 6 seguidos", true);
        chkComer20 = crearCheck("Avanzar 20 al comer", true);
        chkSalida5 = crearCheck("Salir solo con 5", false);

        panelReglas.add(chkTresSeises);
        panelReglas.add(chkComer20);
        panelReglas.add(chkSalida5);

        // 6. Sala Privada
        chkPrivada = crearCheck("Sala Privada (Requiere Código)", false);
        chkPrivada.setFont(new Font("Arial", Font.BOLD, 14)); // Destacar un poco más

        // 7. Botón Iniciar
        JButton btnIniciar = new JButton("CREAR SALA");
        btnIniciar.setBackground(new Color(0, 204, 102));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 14));
        btnIniciar.addActionListener(e -> iniciarPartida());

        // 8. Botón Cancelar
        JButton btnVolver = new JButton("Cancelar");
        btnVolver.setBackground(new Color(204, 0, 0));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.addActionListener(e -> {
            dispose();
            if (ventanaAnterior != null) {
                ventanaAnterior.setVisible(true);
            }
        });

        // Agregar todo al panel principal en orden
        panel.add(lblTitulo);
        panel.add(panelNombre);
        panel.add(panelAvatar);
        panel.add(panelJugadores);
        panel.add(panelReglas);
        panel.add(chkPrivada);
        panel.add(btnIniciar);
        panel.add(btnVolver);

        add(panel);
    }

    // Método helper para crear checkboxes estilizados
    private JCheckBox crearCheck(String texto, boolean selected) {
        JCheckBox chk = new JCheckBox(texto);
        chk.setOpaque(false);
        chk.setForeground(Color.WHITE);
        chk.setFont(new Font("Arial", Font.PLAIN, 12));
        chk.setSelected(selected);
        return chk;
    }

    private JPanel crearPanelCampo(String titulo, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(titulo);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(l, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private void iniciarPartida() {
        String nickname = txtNickname.getText().trim();

        // Obtener avatar limpio (sin el emoji)
        String avatarRaw = (String) cbAvatar.getSelectedItem();
        String avatarClean = avatarRaw.split(" ")[1];

        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre.");
            return;
        }

        int maxJugadores = Integer.parseInt(((String) cbNumJugadores.getSelectedItem()).split(" ")[0]);
        boolean esPublica = !chkPrivada.isSelected();

        // --- CAPTURAR LAS REGLAS SELECCIONADAS ---
        boolean r3 = chkTresSeises.isSelected();
        boolean r20 = chkComer20.isSelected();
        boolean r5 = chkSalida5.isSelected();

        // 1. Iniciar Servidor (En un hilo separado para no congelar la UI)
        // Pasamos los 6 parámetros requeridos por el nuevo constructor de Servidor
        new Thread(() -> {
            new Servidor(5000, maxJugadores, esPublica, r3, r20, r5).iniciar();
        }).start();

        // Pequeña espera para asegurar que el servidor arranque antes de conectar
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }

        // 2. Abrir Lobby (Como anfitrión = true)
        LobbyUI lobby = new LobbyUI(true);
        lobby.setVisible(true);

        // 3. Conectar Cliente (El anfitrión también es un cliente)
        Cliente cliente = new Cliente();
        cliente.setLobbyView(lobby);
        // Conectamos pasando también el avatar
        cliente.conectar("localhost", 5000, nickname, avatarClean, "");
        lobby.setCliente(cliente);

        this.dispose();
    }
}
