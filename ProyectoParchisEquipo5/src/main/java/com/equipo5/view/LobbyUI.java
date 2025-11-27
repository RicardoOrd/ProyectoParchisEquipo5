package com.equipo5.view;

import com.equipo5.model.Jugador;
import com.equipo5.net.Cliente;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LobbyUI extends JFrame {

    private Cliente cliente;
    private JPanel panelJugadores;
    private JTextArea txtChatArea;
    private JTextField txtMensaje;
    private JButton btnAccion; // Botón "Listo" o "Iniciar"
    private JLabel lblCodigoSala;
    
    private boolean esAnfitrion;
    private boolean estoyListo = false;

    public LobbyUI(boolean esAnfitrion) {
        this.esAnfitrion = esAnfitrion;
        initComponents();
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    private void initComponents() {
        setTitle("Sala de Espera - Parchís");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- FONDO PRINCIPAL (Morado según Storyboard) ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(102, 0, 153));
        add(mainPanel, BorderLayout.CENTER);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));
        
        JLabel lblTitulo = new JLabel("Sala de Espera");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 36));
        lblTitulo.setForeground(Color.WHITE);
        
        lblCodigoSala = new JLabel("Sala: GLOBAL");
        lblCodigoSala.setFont(new Font("Monospaced", Font.BOLD, 18));
        lblCodigoSala.setForeground(new Color(255, 204, 0)); // Dorado
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(lblCodigoSala, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- CONTENIDO CENTRAL (Dividido en Jugadores y Chat) ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

        // 1. LISTA DE JUGADORES
        JPanel panelListaContainer = new JPanel(new BorderLayout());
        panelListaContainer.setBackground(new Color(255, 255, 255, 30)); // Blanco translúcido
        panelListaContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), "Jugadores Conectados"));
        ((javax.swing.border.TitledBorder) panelListaContainer.getBorder())
                .setTitleColor(Color.WHITE);

        panelJugadores = new JPanel();
        panelJugadores.setLayout(new BoxLayout(panelJugadores, BoxLayout.Y_AXIS));
        panelJugadores.setOpaque(false);
        
        JScrollPane scrollJugadores = new JScrollPane(panelJugadores);
        scrollJugadores.setOpaque(false);
        scrollJugadores.getViewport().setOpaque(false);
        scrollJugadores.setBorder(null);
        
        panelListaContainer.add(scrollJugadores, BorderLayout.CENTER);

        // 2. CHAT
        JPanel panelChatContainer = new JPanel(new BorderLayout());
        panelChatContainer.setBackground(new Color(255, 255, 255, 30));
        panelChatContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE), "Chat de Sala"));
        ((javax.swing.border.TitledBorder) panelChatContainer.getBorder())
                .setTitleColor(Color.WHITE);

        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtChatArea.setLineWrap(true);
        JScrollPane scrollChat = new JScrollPane(txtChatArea);
        
        JPanel panelInputChat = new JPanel(new BorderLayout(5, 0));
        panelInputChat.setOpaque(false);
        panelInputChat.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        txtMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setBackground(new Color(0, 204, 102));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje()); // Enter para enviar

        panelInputChat.add(txtMensaje, BorderLayout.CENTER);
        panelInputChat.add(btnEnviar, BorderLayout.EAST);

        panelChatContainer.add(scrollChat, BorderLayout.CENTER);
        panelChatContainer.add(panelInputChat, BorderLayout.SOUTH);

        centerPanel.add(panelListaContainer);
        centerPanel.add(panelChatContainer);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- FOOTER (Botón de Acción) ---
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 30, 0));

        btnAccion = new JButton("MARCAR COMO LISTO");
        btnAccion.setFont(new Font("Arial", Font.BOLD, 18));
        btnAccion.setPreferredSize(new Dimension(250, 50));
        btnAccion.setBackground(new Color(255, 204, 0)); // Amarillo
        btnAccion.setForeground(Color.BLACK);
        btnAccion.setFocusPainted(false);
        
        btnAccion.addActionListener(e -> toggleListo());

        footerPanel.add(btnAccion);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    // --- LÓGICA UI ---

    private void enviarMensaje() {
        String texto = txtMensaje.getText().trim();
        if (!texto.isEmpty() && cliente != null) {
            // Protocolo CHAT: { "type": "CHAT", "msg": "Hola mundo" }
            cliente.enviar("{ \"type\": \"CHAT\", \"msg\": \"" + texto + "\" }");
            txtMensaje.setText("");
        }
    }

    private void toggleListo() {
        if (cliente != null) {
            estoyListo = !estoyListo;
            // Protocolo READY: { "type": "TOGGLE_READY", "status": true }
            cliente.enviar("{ \"type\": \"TOGGLE_READY\", \"status\": " + estoyListo + " }");
            
            if (estoyListo) {
                btnAccion.setText("ESPERANDO... (Cancelar)");
                btnAccion.setBackground(new Color(200, 200, 200));
            } else {
                btnAccion.setText("MARCAR COMO LISTO");
                btnAccion.setBackground(new Color(255, 204, 0));
            }
        }
    }

    // --- MÉTODOS LLAMADOS POR EL CLIENTE (Red) ---

    public void agregarMensajeChat(String emisor, String mensaje) {
        txtChatArea.append(emisor + ": " + mensaje + "\n");
        txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
    }

    public void actualizarListaJugadores(List<Jugador> jugadores) {
        panelJugadores.removeAll();
        
        boolean todosListos = true;
        
        for (Jugador j : jugadores) {
            JPanel card = new JPanel(new BorderLayout());
            card.setMaximumSize(new Dimension(1000, 50));
            card.setBackground(new Color(255, 255, 255, 200));
            card.setBorder(new EmptyBorder(5, 10, 5, 10));
            
            JLabel lblNombre = new JLabel(j.getNombre());
            lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
            
            JLabel lblStatus = new JLabel(j.isListo() ? "✓ LISTO" : "⏳ ...");
            lblStatus.setForeground(j.isListo() ? new Color(0, 153, 0) : Color.GRAY);
            lblStatus.setFont(new Font("Arial", Font.BOLD, 14));

            if (!j.isListo()) todosListos = false;

            card.add(lblNombre, BorderLayout.WEST);
            card.add(lblStatus, BorderLayout.EAST);
            
            panelJugadores.add(card);
            panelJugadores.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        // Si soy anfitrión y todos están listos (min 2), cambiar botón a INICIAR
        if (esAnfitrion && todosListos && jugadores.size() >= 2) {
             btnAccion.setText("¡INICIAR PARTIDA!");
             btnAccion.setBackground(new Color(0, 204, 102)); // Verde
             // Reasignar acción para iniciar
             for(var l : btnAccion.getActionListeners()) btnAccion.removeActionListener(l);
             btnAccion.addActionListener(e -> iniciarJuego());
        }
        
        panelJugadores.revalidate();
        panelJugadores.repaint();
    }
    
    private void iniciarJuego() {
        if(cliente != null) {
            cliente.enviar("{ \"type\": \"START_GAME_REQUEST\" }");
        }
    }
}