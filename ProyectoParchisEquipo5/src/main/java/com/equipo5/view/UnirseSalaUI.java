package com.equipo5.view;

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
        setTitle("Unirse a Partida - Parchís Equipo 5");
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
        JPanel panelNick = new JPanel(new BorderLayout());
        panelNick.setOpaque(false);
        JLabel lblNick = new JLabel("Tu Nickname:");
        lblNick.setForeground(Color.WHITE);
        txtNickname = new JTextField();
        panelNick.add(lblNick, BorderLayout.NORTH);
        panelNick.add(txtNickname, BorderLayout.CENTER);

        // 3. IP del Servidor
        JPanel panelIp = new JPanel(new BorderLayout());
        panelIp.setOpaque(false);
        JLabel lblIp = new JLabel("IP del Servidor (Ej. 192.168.1.50):");
        lblIp.setForeground(Color.WHITE);
        txtIp = new JTextField("localhost"); // Por defecto localhost para pruebas
        panelIp.add(lblIp, BorderLayout.NORTH);
        panelIp.add(txtIp, BorderLayout.CENTER);

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
            ventanaAnterior.setVisible(true);
        });

        panel.add(lblTitulo);
        panel.add(panelNick);
        panel.add(panelIp);
        panel.add(btnUnirse);
        panel.add(btnVolver);

        add(panel);
    }

    private void unirsePartida() {
        String nickname = txtNickname.getText().trim();
        String ip = txtIp.getText().trim();

        if (nickname.isEmpty() || ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa tu nombre y la IP.");
            return;
        }

        // Crear el tablero y conectar al cliente a la IP especificada
        TableroUI tablero = new TableroUI();
        tablero.setVisible(true);
        
        // Llamamos al método actualizado que acepta IP (ver punto 2)
        tablero.iniciarCliente(ip, nickname);

        this.dispose();
    }
}