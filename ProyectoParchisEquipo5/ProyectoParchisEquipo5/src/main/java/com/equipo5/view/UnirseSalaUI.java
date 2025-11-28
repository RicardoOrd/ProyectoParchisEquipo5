package com.equipo5.view;

import com.equipo5.net.Cliente;
import javax.swing.*;
import java.awt.*;

public class UnirseSalaUI extends JFrame {

    private final JFrame ventanaAnterior;
    private JTextField txtNickname;
    private JTextField txtIp;

    public UnirseSalaUI(JFrame ventanaAnterior) {
        this.ventanaAnterior = ventanaAnterior;
        initComponents();
    }

    private void initComponents() {
        setTitle("Unirse a Partida");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBackground(new Color(102, 0, 153)); // Morado
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // 1. Título
        JLabel lblTitulo = new JLabel("Unirse a Sala Existente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        // 2. Nickname
        JPanel panelNick = crearPanelCampo("Tu Nickname:", txtNickname = new JTextField());

        // 3. IP del Servidor
        JPanel panelIp = crearPanelCampo("IP del Servidor:", txtIp = new JTextField("localhost"));

        // 4. Botón Unirse
        JButton btnUnirse = new JButton("CONECTARSE");
        btnUnirse.setBackground(new Color(0, 204, 102));
        btnUnirse.setForeground(Color.WHITE);
        btnUnirse.setFont(new Font("Arial", Font.BOLD, 14));
        btnUnirse.addActionListener(e -> unirsePartida());

        // 5. Botón Cancelar
        JButton btnVolver = new JButton("Cancelar");
        btnVolver.setBackground(new Color(204, 0, 0));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.addActionListener(e -> {
            dispose();
            if(ventanaAnterior != null) ventanaAnterior.setVisible(true);
        });

        panel.add(lblTitulo);
        panel.add(panelNick);
        panel.add(panelIp);
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

        if (nickname.isEmpty() || ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa tu nombre y la IP.");
            return;
        }

        // 1. Abrir Lobby como Cliente (NO anfitrión)
        // Pasamos 'false' porque es un cliente invitado
        LobbyUI lobby = new LobbyUI(false); 
        lobby.setVisible(true);
        
        // 2. Conectar Cliente
        Cliente cliente = new Cliente();
        cliente.setLobbyView(lobby);
        cliente.conectar(ip, 5000, nickname);
        lobby.setCliente(cliente);

        this.dispose();
    }
}