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

    public CrearSalaUI(JFrame ventanaAnterior) {
        this.ventanaAnterior = ventanaAnterior;
        initComponents();
    }

    private void initComponents() {
        setTitle("Configurar Partida");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBackground(new Color(102, 0, 153)); // Morado
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel("Configuración de Sala", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JPanel panelNombre = crearPanelCampo("Tu Nickname:", txtNickname = new JTextField());

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

        chkPrivada = new JCheckBox("Sala Privada (Requiere Código)");
        chkPrivada.setOpaque(false);
        chkPrivada.setForeground(Color.WHITE);
        chkPrivada.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnIniciar = new JButton("CREAR SALA");
        btnIniciar.setBackground(new Color(0, 204, 102));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFont(new Font("Arial", Font.BOLD, 14));
        btnIniciar.addActionListener(e -> iniciarPartida());

        JButton btnVolver = new JButton("Cancelar");
        btnVolver.setBackground(new Color(204, 0, 0));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.addActionListener(e -> {
            dispose();
            if(ventanaAnterior != null) ventanaAnterior.setVisible(true);
        });

        panel.add(lblTitulo);
        panel.add(panelNombre);
        panel.add(panelJugadores);
        panel.add(chkPrivada);
        panel.add(btnIniciar);
        panel.add(btnVolver);

        add(panel);
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
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre.");
            return;
        }

        int maxJugadores = Integer.parseInt(((String)cbNumJugadores.getSelectedItem()).split(" ")[0]);
        boolean esPublica = !chkPrivada.isSelected();

        // 1. Iniciar Servidor
        new Thread(() -> {
            new Servidor(5000, maxJugadores, esPublica).iniciar();
        }).start();

        // --- CORRECCIÓN CRÍTICA: ESPERAR A QUE EL SERVIDOR ARRANQUE ---
        try {
            Thread.sleep(500); // Esperar 0.5 segundos para asegurar que el puerto 5000 esté abierto
        } catch (InterruptedException ex) {}

        // 2. Abrir Lobby
        LobbyUI lobby = new LobbyUI(true); 
        lobby.setVisible(true);
        
        // 3. Conectar Cliente
        Cliente cliente = new Cliente();
        cliente.setLobbyView(lobby);
        cliente.conectar("localhost", 5000, nickname);
        lobby.setCliente(cliente);

        this.dispose();
    }
}