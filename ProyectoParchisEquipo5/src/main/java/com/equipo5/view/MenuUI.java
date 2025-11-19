package com.equipo5.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MenuUI extends JFrame {

    public MenuUI() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Parchís - Equipo 5");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);

        // Panel principal con fondo morado (según storyboard)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(102, 0, 153)); // Morado oscuro
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Título
        JLabel titleLabel = new JLabel("PARCHÍS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Espaciado
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Botón Crear Partida
        JButton btnCrear = crearBotonEstilizado("Crear Partida");
        btnCrear.addActionListener((ActionEvent e) -> {
            abrirCrearSala();
        });

        // Botón Unirse a Sala
        JButton btnUnirse = crearBotonEstilizado("Unirse a Sala");
        btnUnirse.addActionListener((ActionEvent e) -> {
            // Aquí iría la lógica para abrir la ventana de Unirse (UC-02)
            JOptionPane.showMessageDialog(this, "Funcionalidad pendiente: Unirse a Sala");
        });

        // Botón Salir
        JButton btnSalir = crearBotonEstilizado("Salir");
        btnSalir.addActionListener(e -> System.exit(0));

        // Añadir botones al panel
        mainPanel.add(btnCrear);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(btnUnirse);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(btnSalir);

        add(mainPanel);
    }

    // Método auxiliar para estilos de botones consistentes
    private JButton crearBotonEstilizado(String texto) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(new Color(255, 204, 0)); // Amarillo/Dorado
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        return btn;
    }

    private void abrirCrearSala() {
        // Oculta este menú y abre la configuración
        new CrearSalaUI(this).setVisible(true);
        this.setVisible(false);
    }
}