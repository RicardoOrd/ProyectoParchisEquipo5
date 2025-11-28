package com.equipo5.view;

import com.equipo5.net.Cliente;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class LobbyUI extends JFrame {

    private Cliente cliente;
    private JPanel panelJugadores;
    private JTextArea txtChatArea;
    private JTextField txtMensaje;
    private JButton btnListo; 
    private JButton btnIniciar;
    private JLabel lblCodigoSala;
    
    private boolean esAnfitrion;
    private boolean estoyListo = false;

    // Colores del Diseño
    private final Color COLOR_FONDO = new Color(88, 0, 149); // Morado similar a la imagen
    private final Color COLOR_BOTON_INICIAR = new Color(255, 69, 0); // Rojo/Naranja
    private final Color COLOR_BOTON_LISTO = new Color(255, 204, 0); // Amarillo
    private final Color COLOR_TEXTO_LISTO = new Color(50, 50, 50); // Gris oscuro para texto en amarillo
    private final Color COLOR_PANEL_BLANCO = Color.WHITE;

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
        setSize(950, 650);
        setLocationRelativeTo(null);
        setResizable(false); // Mantener diseño fijo
        setLayout(new BorderLayout());

        // --- PANEL PRINCIPAL (Fondo Morado) ---
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20)); // Gaps horizontales y verticales
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30)); // Margen externo
        add(mainPanel, BorderLayout.CENTER);

        // =================================================================================
        // 1. HEADER (Título y Botón Iniciar)
        // =================================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("Sala de Espera");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblTitulo.setForeground(Color.WHITE);
        
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        headerRight.setOpaque(false);
        
        // Etiqueta Código
        lblCodigoSala = new JLabel("Cód: GLOBAL");
        lblCodigoSala.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblCodigoSala.setForeground(new Color(255, 220, 0)); // Amarillo claro
        headerRight.add(lblCodigoSala);

        // Botón Iniciar (Solo Anfitrión)
        if (esAnfitrion) {
            btnIniciar = new JButton("INICIAR PARTIDA");
            estilizarBoton(btnIniciar, COLOR_BOTON_INICIAR, Color.WHITE, 16);
            btnIniciar.setPreferredSize(new Dimension(180, 40));
            btnIniciar.addActionListener(e -> iniciarJuego());
            headerRight.add(btnIniciar);
        }
        
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // =================================================================================
        // 2. CONTENIDO CENTRAL (Dos columnas: Jugadores y Chat)
        // =================================================================================
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0)); // 30px de espacio entre columnas
        centerPanel.setOpaque(false);

        // --- COLUMNA IZQUIERDA: LISTA JUGADORES ---
        JPanel panelListaContainer = crearPanelContenedor("Jugadores");
        
        panelJugadores = new JPanel();
        // CAMBIO IMPORTANTE: Usamos un layout vertical más robusto o simplemente Box
        panelJugadores.setLayout(new BoxLayout(panelJugadores, BoxLayout.Y_AXIS));
        panelJugadores.setBackground(Color.WHITE); 
        
        // IMPORTANTE: Panel "Glue" para empujar los elementos hacia arriba
        // Esto evita que se expandan y ocupen todo el espacio vertical de forma extraña
        
        JScrollPane scrollJugadores = new JScrollPane(panelJugadores);
        scrollJugadores.setBorder(null); 
        scrollJugadores.getViewport().setBackground(Color.WHITE);
        scrollJugadores.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Evitar scroll horizontal
        
        panelListaContainer.add(scrollJugadores, BorderLayout.CENTER);

        // --- COLUMNA DERECHA: CHAT ---
        JPanel panelChatContainer = crearPanelContenedor("Chat");
        
        // Área de texto del chat
        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtChatArea.setMargin(new Insets(10, 10, 10, 10)); 
        txtChatArea.setLineWrap(true); // Ajuste de línea para que no se salga
        txtChatArea.setWrapStyleWord(true);

        JScrollPane scrollChat = new JScrollPane(txtChatArea);
        scrollChat.setBorder(null); 
        
        // Panel inferior del chat (Input + Botón)
        JPanel panelInput = new JPanel(new BorderLayout(10, 0));
        panelInput.setOpaque(false);
        panelInput.setBorder(new EmptyBorder(10, 0, 0, 0)); 
        
        txtMensaje = new JTextField();
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JButton btnEnviar = new JButton("Enviar");
        estilizarBoton(btnEnviar, new Color(240, 240, 240), Color.BLACK, 12);
        btnEnviar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnEnviar.setPreferredSize(new Dimension(80, 30));
        
        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje());
        
        panelInput.add(txtMensaje, BorderLayout.CENTER);
        panelInput.add(btnEnviar, BorderLayout.EAST);

        panelChatContainer.add(scrollChat, BorderLayout.CENTER);
        panelChatContainer.add(panelInput, BorderLayout.SOUTH);

        // Agregar paneles al centro
        centerPanel.add(panelListaContainer);
        centerPanel.add(panelChatContainer);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // =================================================================================
        // 3. FOOTER (Botón Listo Grande)
        // =================================================================================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnListo = new JButton("MARCAR COMO LISTO");
        estilizarBoton(btnListo, COLOR_BOTON_LISTO, COLOR_TEXTO_LISTO, 20);
        btnListo.setPreferredSize(new Dimension(300, 60)); 
        btnListo.addActionListener(e -> toggleListo());

        footerPanel.add(btnListo);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    // --- MÉTODOS DE AYUDA VISUAL ---

    private JPanel crearPanelContenedor(String titulo) {
        // Wrapper transparente para el borde
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        // Borde blanco con título
        Border lineBorder = BorderFactory.createLineBorder(Color.WHITE, 2);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(lineBorder, titulo);
        titledBorder.setTitleColor(Color.WHITE);
        titledBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        wrapper.setBorder(titledBorder);
        
        // Contenido blanco interno con padding
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        // Margen interno para separar el contenido del borde blanco exterior
        content.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }
    
    private void estilizarBoton(JButton btn, Color bg, Color fg, int fontSize) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- LÓGICA ---

    private void enviarMensaje() {
        String t = txtMensaje.getText().trim();
        if(!t.isEmpty() && cliente != null) {
            cliente.enviar("{ \"type\": \"CHAT\", \"msg\": \"" + t + "\" }");
            txtMensaje.setText("");
        }
    }

    private void toggleListo() {
        if(cliente != null) {
            estoyListo = !estoyListo;
            cliente.enviar("{ \"type\": \"TOGGLE_READY\", \"status\": " + estoyListo + " }");
            
            if (estoyListo) {
                btnListo.setText("ESPERANDO... (Cancelar)");
                btnListo.setBackground(new Color(200, 200, 200)); 
            } else {
                btnListo.setText("MARCAR COMO LISTO");
                btnListo.setBackground(COLOR_BOTON_LISTO);
            }
        }
    }
    
    private void iniciarJuego() {
        if(cliente != null) {
            cliente.enviar("{ \"type\": \"START_GAME_REQUEST\" }");
        }
    }

    public void agregarMensajeChat(String emisor, String mensaje) {
        if (emisor.equals("SISTEMA") && mensaje.startsWith("Jugadores en sala:")) {
            actualizarListaVisual(mensaje);
        } else {
            txtChatArea.append(emisor + ": " + mensaje + "\n");
            txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
        }
    }
    
    private void actualizarListaVisual(String mensajeRaw) {
        panelJugadores.removeAll();
        String[] lineas = mensajeRaw.split("\\|");
        
        for (String linea : lineas) {
            String texto = linea.trim();
            if (texto.isEmpty() || texto.equals("Jugadores en sala:")) continue;
            
            JPanel card = new JPanel(new BorderLayout());
            
            card.setMaximumSize(new Dimension(2000, 50)); 
            card.setPreferredSize(new Dimension(300, 50)); // Ancho flexible, alto fijo
            card.setMinimumSize(new Dimension(300, 50));
            
            card.setBorder(new CompoundBorder(
                new EmptyBorder(0, 0, 8, 0), // Margen inferior entre tarjetas (espacio blanco)
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1) // Borde gris suave
            ));
            
            boolean isListo = texto.contains("(LISTO)");
            card.setBackground(isListo ? new Color(255, 250, 205) : new Color(248, 248, 248));
            
            JLabel lblTexto = new JLabel(texto.replace("- ", ""));
            lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTexto.setForeground(Color.DARK_GRAY);
            lblTexto.setBorder(new EmptyBorder(0, 15, 0, 0)); // Padding izquierdo del texto
            
            JLabel lblIcono = new JLabel(isListo ? "✔" : "⏳");
            lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            lblIcono.setForeground(isListo ? new Color(0, 150, 0) : Color.GRAY);
            lblIcono.setBorder(new EmptyBorder(0, 0, 0, 15)); // Padding derecho
            
            card.add(lblTexto, BorderLayout.CENTER);
            card.add(lblIcono, BorderLayout.EAST);
            
            panelJugadores.add(card);
        }
        
        panelJugadores.revalidate();
        panelJugadores.repaint();
    }
    
    public void actualizarListaJugadores(java.util.List<com.equipo5.model.Jugador> jugadores) {}
}