package com.equipo5.view;

import com.equipo5.net.Cliente;
import javax.swing.*;
import java.awt.*;

public class UnirseSalaUI extends JFrame {

    private final JFrame ventanaAnterior;
    private JTextField txtNickname;
    private JTextField txtIp;
    private JTextField txtCodigo; // NUEVO: Campo para el código
    private JComboBox<String> cbAvatar;

    public UnirseSalaUI(JFrame ventanaAnterior) {
        this.ventanaAnterior = ventanaAnterior;
        initComponents();
    }

    private void initComponents() {
        setTitle("Unirse a Partida");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 480); // Aumentamos altura
        setLocationRelativeTo(null);
        setResizable(false);

        // Aumentamos a 7 filas para incluir el campo de Código
        JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10));
        panel.setBackground(new Color(102, 0, 153)); 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 1. Título
        JLabel lblTitulo = new JLabel("Unirse a Sala Existente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        // 2. Nickname
        JPanel panelNick = crearPanelCampo("Tu Nickname:", txtNickname = new JTextField());
        
        // 3. Avatar
        String[] avatares = {"🐶 Perro", "🐱 Gato", "🤖 Robot", "👽 Alien"};
        cbAvatar = new JComboBox<>(avatares);
        JPanel panelAvatar = crearPanelCampo("Elige tu Avatar:", cbAvatar);

        // 4. IP del Servidor
        JPanel panelIp = crearPanelCampo("IP del Servidor:", txtIp = new JTextField("localhost"));
        
        // 5. Código de Sala (NUEVO)
        txtCodigo = new JTextField();
        JPanel panelCodigo = crearPanelCampo("Código de Sala (Si es privada):", txtCodigo);

        // 6. Botón Unirse
        JButton btnUnirse = new JButton("CONECTARSE");
        btnUnirse.setBackground(new Color(0, 204, 102));
        btnUnirse.setForeground(Color.WHITE);
        btnUnirse.setFont(new Font("Arial", Font.BOLD, 14));
        btnUnirse.addActionListener(e -> unirsePartida());

        // 7. Botón Cancelar
        JButton btnVolver = new JButton("Cancelar");
        btnVolver.setBackground(new Color(204, 0, 0));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.addActionListener(e -> {
            dispose();
            if (ventanaAnterior != null) ventanaAnterior.setVisible(true);
        });

        // Añadir en orden
        panel.add(lblTitulo);
        panel.add(panelNick);
        panel.add(panelAvatar);
        panel.add(panelIp);
        panel.add(panelCodigo); // Agregamos el panel de código
        panel.add(btnUnirse);
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

    private void unirsePartida() {
        String nickname = txtNickname.getText().trim();
        String ip = txtIp.getText().trim();
        String codigo = txtCodigo.getText().trim().toUpperCase(); // Capturamos código
        
        String avatarRaw = (String) cbAvatar.getSelectedItem();
        String avatarClean = avatarRaw.split(" ")[1]; 

        if (nickname.isEmpty() || ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa nombre e IP.");
            return;
        }

        LobbyUI lobby = new LobbyUI(false); 
        lobby.setVisible(true);
        
        Cliente cliente = new Cliente();
        cliente.setLobbyView(lobby);
        // Enviamos el código también
        cliente.conectar(ip, 5000, nickname, avatarClean, codigo); 
        lobby.setCliente(cliente);

        this.dispose();
    }
}