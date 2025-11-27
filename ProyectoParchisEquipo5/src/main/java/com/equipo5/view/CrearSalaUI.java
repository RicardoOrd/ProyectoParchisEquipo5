package com.equipo5.view;

import com.equipo5.net.Cliente;
import com.equipo5.net.Servidor;
import javax.swing.*;
import java.awt.*;

public class CrearSalaUI extends JFrame {

    private JTextField txtNickname;

    public CrearSalaUI(JFrame ventanaAnterior) {
        initComponents();
    }

    private void initComponents() {
        setTitle("Crear Sala");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1));

        add(new JLabel("Nombre del Anfitrión:"));
        txtNickname = new JTextField();
        add(txtNickname);

        JButton btnIniciar = new JButton("CREAR SALA");
        btnIniciar.addActionListener(e -> crearSala());
        add(btnIniciar);
    }

    private void crearSala() {
        String nick = txtNickname.getText().trim();
        if (nick.isEmpty()) return;

        // 1. Iniciar Servidor
        new Thread(() -> new Servidor(5000).iniciar()).start();

        // 2. Abrir Lobby como Host
        LobbyUI lobby = new LobbyUI(true); // true = es anfitrión
        lobby.setVisible(true);
        
        // 3. Conectar Cliente
        Cliente cliente = new Cliente();
        cliente.setLobbyView(lobby);
        cliente.conectar("localhost", 5000, nick);
        lobby.setCliente(cliente);

        this.dispose();
    }
}