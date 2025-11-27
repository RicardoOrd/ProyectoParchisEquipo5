package com.equipo5.view;

import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.model.Tablero;
import com.equipo5.net.Cliente;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class TableroUI extends javax.swing.JFrame {

    private PanelTablero panelTablero;
    private JTextArea areaLog;
    private JButton btnLanzarDado;
    private JLabel lblDado;
    private JLabel lblTurno;

    // Referencia al Cliente de Red
    private Cliente clienteRed;
    @SuppressWarnings("unused")
    private String miNombre;

    // Modelo de datos local (replicado del servidor)
    private Tablero modeloTablero;
    @SuppressWarnings("unused")
    private List<Jugador> listaJugadores;

    public TableroUI() {
        initComponents();
        this.modeloTablero = new Tablero();
        this.listaJugadores = new ArrayList<>();

        // --- IMPORTANTE: Inicializar fichas visuales ---
        // Creamos 4 jugadores dummy para que el tablero pinte las 16 fichas en sus bases
        // desde el principio, aunque aún no estemos conectados.
        List<Jugador> dummies = new ArrayList<>();
        Jugador j1 = new Jugador("P1");
        j1.setColor("AMARILLO");
        dummies.add(j1);
        Jugador j2 = new Jugador("P2");
        j2.setColor("AZUL");
        dummies.add(j2);
        Jugador j3 = new Jugador("P3");
        j3.setColor("ROJO");
        dummies.add(j3);
        Jugador j4 = new Jugador("P4");
        j4.setColor("VERDE");
        dummies.add(j4);

        this.modeloTablero.inicializarFichas(dummies);
    }

    /**
     * Inicia la conexión de red.
     *
     * @param host Dirección IP del servidor ("localhost" o IP real).
     * @param nickname Nombre del jugador.
     */
    public void iniciarCliente(String host, String nickname) {
        this.miNombre = nickname;
        this.clienteRed = new Cliente(this);
        this.clienteRed.conectar(host, 5000, nickname);
        this.setTitle("Parchís - Equipo 5 - Jugador: " + nickname);
    }

    private void initComponents() {
        setTitle("Parchís - Equipo 5");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. Panel Central (Tablero) ---
        panelTablero = new PanelTablero();
        panelTablero.setBackground(new Color(240, 240, 240));

        // Listener para detectar clics en las fichas
        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                verificarClicFicha(e.getX(), e.getY());
            }
        });

        add(panelTablero, BorderLayout.CENTER);

        // --- 2. Panel Lateral (Controles) ---
        JPanel panelLateral = new JPanel();
        panelLateral.setPreferredSize(new Dimension(250, 0));
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBackground(new Color(50, 50, 50));
        panelLateral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Turno
        lblTurno = new JLabel("Conectando...");
        lblTurno.setForeground(Color.WHITE);
        lblTurno.setFont(new Font("Arial", Font.BOLD, 16));
        lblTurno.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblTurno);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // Dado Visual
        lblDado = new JLabel("?");
        lblDado.setFont(new Font("Arial", Font.BOLD, 60));
        lblDado.setForeground(Color.YELLOW);
        lblDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botón Lanzar
        btnLanzarDado = new JButton("LANZAR DADO");
        btnLanzarDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLanzarDado.setBackground(new Color(0, 153, 255));
        btnLanzarDado.setForeground(Color.WHITE);
        btnLanzarDado.setFont(new Font("Arial", Font.BOLD, 14));
        btnLanzarDado.setFocusPainted(false);
        btnLanzarDado.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Acción: Enviar solicitud ROLL al servidor
        btnLanzarDado.addActionListener(e -> {
            if (clienteRed != null) {
                clienteRed.enviar("{ \"type\": \"ROLL\" }");
            } else {
                agregarLog("Error: No hay conexión.");
            }
        });

        panelLateral.add(btnLanzarDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // Log de Texto
        JLabel lblLog = new JLabel("Registro del Juego:");
        lblLog.setForeground(Color.LIGHT_GRAY);
        lblLog.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblLog);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setPreferredSize(new Dimension(200, 300));
        panelLateral.add(scrollLog);

        add(panelLateral, BorderLayout.EAST);
    }

    public void setClienteExistente(Cliente cliente) {
        this.clienteRed = cliente;
        // Importante: Redirigir la salida del cliente a esta vista ahora
        // (Esto requiere lógica en Cliente.java para saber a quién mandar updates, 
        //  ya cubierto en el Cliente.java actualizado arriba)
    }

    // Método para procesar mensajes que llegan desde Cliente.java
    public void procesarMensajeJuego(String json) {
        if (json.contains("DICE_RESULT")) {
            String valStr = json.split("\"value\": ")[1].split(" ")[0].replace("}", "").trim();
            mostrarResultadoDado(Integer.parseInt(valStr));
        } else if (json.contains("UPDATE")) {
            // Lógica de update tablero...
        }
        // ... resto de lógica
    }

    // --- LÓGICA DE INPUT (Clics) ---
    private void verificarClicFicha(int x, int y) {
        if (modeloTablero == null || clienteRed == null) {
            return;
        }

        int w = panelTablero.getWidth();
        int h = panelTablero.getHeight();

        // Iterar sobre todas las fichas para ver si alguna fue clickeada
        for (Ficha f : modeloTablero.getTodasLasFichas()) {
            Point p = obtenerCoordenadasFicha(f, w, h);
            // Definimos un área sensible (hitbox) de 30x30 alrededor de la ficha
            Rectangle bounds = new Rectangle(p.x, p.y, 30, 30);

            if (bounds.contains(x, y)) {
                System.out.println("Clic en ficha ID: " + f.getId());
                // Enviar solicitud de movimiento al servidor
                clienteRed.enviar("{ \"type\": \"MOVE\", \"ficha\": " + f.getId() + " }");
                return; // Solo procesar un clic a la vez
            }
        }
    }

    // --- MÉTODOS DE ACTUALIZACIÓN (Output) ---
    // Método llamado por Cliente.java cuando recibe el JSON "UPDATE"
    public void actualizarFichaLocal(String color, int id, int pos, boolean enBase) {
        if (modeloTablero == null) {
            return;
        }

        for (Ficha f : modeloTablero.getTodasLasFichas()) {
            if (f.getColor().equals(color) && f.getId() == id) {
                f.setPosicion(pos);
                f.setEnBase(enBase);
                return;
            }
        }
    }

    public void actualizarVista(Tablero tablero, List<Jugador> jugadores) {
        this.modeloTablero = tablero;
        this.listaJugadores = jugadores;
        panelTablero.repaint();
    }

    public void actualizarTurnoInfo(String texto) {
        lblTurno.setText(texto);
    }

    public void agregarLog(String mensaje) {
        areaLog.append("> " + mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void mostrarResultadoDado(int valor) {
        lblDado.setText(String.valueOf(valor));
    }

    // Calcula dónde dibujar la ficha (usado para Pintar y para Clics)
    private Point obtenerCoordenadasFicha(Ficha ficha, int w, int h) {
        int x = 0, y = 0;
        if (ficha.isEnBase()) {
            switch (ficha.getColor()) {
                case "AZUL":
                    x = 60 + (ficha.getId() * 20);
                    y = 80;
                    break;
                case "AMARILLO":
                    x = w - 140 + (ficha.getId() * 20);
                    y = 80;
                    break;
                case "VERDE":
                    x = 60 + (ficha.getId() * 20);
                    y = h - 120;
                    break;
                case "ROJO":
                    x = w - 140 + (ficha.getId() * 20);
                    y = h - 120;
                    break;
            }
        } else {
            // Lógica visual circular simplificada
            // Mapeamos las 68 casillas a un ángulo en radianes
            int radio = 180;
            // Angulo 0 empieza a la derecha, ajustamos para que la casilla 1 esté donde corresponda
            double angulo = (ficha.getPosicion() * (2 * Math.PI)) / 68;

            x = (w / 2) + (int) (Math.cos(angulo) * radio);
            y = (h / 2) + (int) (Math.sin(angulo) * radio);
        }
        return new Point(x, y);
    }

    // --- PANEL DE DIBUJO ---
    private class PanelTablero extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cx = w / 2;
            int cy = h / 2;

            // Bases
            dibujarBase(g2, "AMARILLO", Color.YELLOW, w - 150, 50);
            dibujarBase(g2, "AZUL", Color.BLUE, 50, 50);
            dibujarBase(g2, "ROJO", Color.RED, w - 150, h - 150);
            dibujarBase(g2, "VERDE", Color.GREEN, 50, h - 150);

            // Centro / Meta
            g2.setColor(Color.GRAY);
            g2.fillRect(cx - 30, cy - 30, 60, 60);

            // Camino (Circular simple para visualización)
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(cx - 180, cy - 180, 360, 360);

            // Fichas
            if (modeloTablero != null) {
                for (Ficha f : modeloTablero.getTodasLasFichas()) {
                    dibujarFicha(g2, f, w, h);
                }
            }
        }

        private void dibujarBase(Graphics2D g, String txt, Color c, int x, int y) {
            g.setColor(c);
            g.fillRoundRect(x, y, 100, 100, 15, 15);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, 100, 100, 15, 15);
            g.drawString(txt, x + 20, y + 55);
        }

        private void dibujarFicha(Graphics2D g, Ficha f, int w, int h) {
            Point p = obtenerCoordenadasFicha(f, w, h);
            g.setColor(obtenerColorReal(f.getColor()));
            g.fillOval(p.x, p.y, 25, 25);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawOval(p.x, p.y, 25, 25);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(f.getId()), p.x + 8, p.y + 17);
        }

        private Color obtenerColorReal(String c) {
            if (c == null) {
                return Color.GRAY;
            }
            switch (c) {
                case "ROJO":
                    return Color.RED;
                case "VERDE":
                    return new Color(0, 180, 0);
                case "AZUL":
                    return Color.BLUE;
                case "AMARILLO":
                    return Color.YELLOW;
                default:
                    return Color.GRAY;
            }
        }
    }
}
