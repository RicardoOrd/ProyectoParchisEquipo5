package com.equipo5.view;

import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.model.Tablero;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 */
public class TableroUI extends javax.swing.JFrame {

    private PanelTablero panelTablero;
    private JTextArea areaLog;
    private JButton btnLanzarDado;
    private JLabel lblDado;
    private JLabel lblTurno;
    
    // Referencia local a los datos para poder pintarlos
    private Tablero modeloTablero; 
    private List<Jugador> listaJugadores;

    public TableroUI() {
        initComponents();
        this.modeloTablero = new Tablero(); // Tablero vacío inicial
        this.listaJugadores = new ArrayList<>();
    }

    private void initComponents() {
        setTitle("Parchís - Equipo 5");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Panel Central (El Tablero pintado)
        panelTablero = new PanelTablero();
        panelTablero.setBackground(new Color(240, 240, 240));
        add(panelTablero, BorderLayout.CENTER);

        // 2. Panel Lateral (Controles e Información)
        JPanel panelLateral = new JPanel();
        panelLateral.setPreferredSize(new Dimension(250, 0));
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBackground(new Color(50, 50, 50)); // Gris oscuro
        panelLateral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Indicador de Turno ---
        lblTurno = new JLabel("Esperando jugadores...");
        lblTurno.setForeground(Color.WHITE);
        lblTurno.setFont(new Font("Arial", Font.BOLD, 16));
        lblTurno.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblTurno);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- Visualizador del Dado ---
        lblDado = new JLabel("?");
        lblDado.setFont(new Font("Arial", Font.BOLD, 60));
        lblDado.setForeground(Color.YELLOW);
        lblDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Botón Lanzar ---
        btnLanzarDado = new JButton("LANZAR DADO");
        btnLanzarDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLanzarDado.setBackground(new Color(0, 153, 255));
        btnLanzarDado.setForeground(Color.WHITE);
        btnLanzarDado.setFont(new Font("Arial", Font.BOLD, 14));
        btnLanzarDado.setFocusPainted(false);
        btnLanzarDado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Acción del botón (Aquí conectarás con el Cliente/Red más adelante)
        btnLanzarDado.addActionListener(e -> {
            // Ejemplo de acción local temporal
            int valor = (int) (Math.random() * 6) + 1;
            mostrarResultadoDado(valor);
            agregarLog("Has lanzado un " + valor);
        });
        
        panelLateral.add(btnLanzarDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- Área de Logs/Chat ---
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

    // --- MÉTODOS PÚBLICOS PARA ACTUALIZAR LA VISTA (Blackboard) ---

    /**
     * Recibe el tablero actualizado (con nuevas posiciones) y repinta.
     */
    public void actualizarVista(Tablero tablero, List<Jugador> jugadores) {
        this.modeloTablero = tablero;
        this.listaJugadores = jugadores;
        
        // Actualizar lista de jugadores en el log si es necesario
        lblTurno.setText("Jugadores: " + jugadores.size());
        
        // Forzar repintado del panel del tablero
        panelTablero.repaint();
    }

    public void agregarLog(String mensaje) {
        areaLog.append("> " + mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength()); // Auto-scroll
    }

    public void mostrarResultadoDado(int valor) {
        lblDado.setText(String.valueOf(valor));
    }

    // --- CLASE INTERNA PARA DIBUJAR EL TABLERO ---
    private class PanelTablero extends JPanel {
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int centroX = w / 2;
            int centroY = h / 2;
            int tamanoCasilla = 30;

            // 1. Dibujar Estructura Base (Cruz del Parchís)
            // Fondo blanco general ya está puesto
            
            // Dibujar bases (Esquinas)
            dibujarBase(g2, "AMARILLO", Color.YELLOW, w - 150, 50);
            dibujarBase(g2, "AZUL", Color.BLUE, 50, 50);
            dibujarBase(g2, "ROJO", Color.RED, w - 150, h - 150);
            dibujarBase(g2, "VERDE", Color.GREEN, 50, h - 150);

            // Dibujar Meta Central
            g2.setColor(Color.GRAY);
            g2.fillPolygon(
                new int[]{centroX - 40, centroX + 40, centroX + 40, centroX - 40}, 
                new int[]{centroY - 40, centroY - 40, centroY + 40, centroY + 40}, 4
            );
            
            // Dibujar caminos (Representación simplificada visual)
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(centroX - 15, 50, 30, centroY - 90); // Camino vertical superior
            g2.drawRect(centroX - 15, centroY + 40, 30, centroY - 90); // Camino vertical inferior
            g2.drawRect(50, centroY - 15, centroX - 90, 30); // Camino horiz izq
            g2.drawRect(centroX + 40, centroY - 15, centroX - 90, 30); // Camino horiz der

            // 2. DIBUJAR FICHAS (Lo más importante)
            if (modeloTablero != null) {
                for (Ficha f : modeloTablero.getTodasLasFichas()) {
                    dibujarFicha(g2, f, w, h);
                }
            }
        }

        private void dibujarBase(Graphics2D g2, String nombre, Color c, int x, int y) {
            g2.setColor(c);
            g2.fillRoundRect(x, y, 100, 100, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(x, y, 100, 100, 20, 20);
            g2.drawString(nombre, x + 20, y + 55);
        }

        private void dibujarFicha(Graphics2D g2, Ficha ficha, int w, int h) {
            // Lógica simple de coordenadas basada en el estado
            // Si está en base:
            int x = 0, y = 0;
            
            if (ficha.isEnBase()) {
                // Coordenadas fijas en las bases según color
                switch (ficha.getColor()) {
                    case "AZUL": x = 60 + (ficha.getId() * 20); y = 80; break;
                    case "AMARILLO": x = w - 140 + (ficha.getId() * 20); y = 80; break;
                    case "VERDE": x = 60 + (ficha.getId() * 20); y = h - 120; break;
                    case "ROJO": x = w - 140 + (ficha.getId() * 20); y = h - 120; break;
                    default: return;
                }
            } else {
                // Si está en tablero (cálculo aproximado para visualización rápida)
                int offset = ficha.getPosicion() * 5; 
                x = (w / 2) + (int) (Math.cos(offset) * 100);
                y = (h / 2) + (int) (Math.sin(offset) * 100);
            }

            // Dibujar el circulo de la ficha
            g2.setColor(obtenerColorReal(ficha.getColor()));
            g2.fillOval(x, y, 20, 20);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x, y, 20, 20);
            
            // Dibujar numero de ficha
            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(ficha.getId()), x + 6, y + 15);
        }
        
        private Color obtenerColorReal(String nombreColor) {
            switch (nombreColor) {
                case "ROJO": return Color.RED;
                case "VERDE": return new Color(0, 153, 0); 
                case "AZUL": return Color.BLUE;
                case "AMARILLO": return Color.YELLOW;
                default: return Color.GRAY;
            }
        }
    }
    
}