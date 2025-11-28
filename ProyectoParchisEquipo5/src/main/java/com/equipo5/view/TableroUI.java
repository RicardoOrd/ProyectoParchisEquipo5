package com.equipo5.view;

import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.model.Tablero;
import com.equipo5.net.Cliente;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class TableroUI extends javax.swing.JFrame {

    private PanelTablero panelTablero;
    private JTextArea areaLog;
    private JButton btnLanzarDado;
    private JLabel lblDado;
    private JLabel lblTurno;
    
    private Cliente clienteRed;
    private Tablero modeloTablero; 
    
    // Coordenadas calculadas dinámicamente
    private Point[] coordenadasCasillas = new Point[69]; 

    public TableroUI() {
        initComponents();
        this.modeloTablero = new Tablero(); 
        
        // Inicializar dummies para ver algo antes de conectar
        List<Jugador> dummies = new ArrayList<>();
        Jugador j1 = new Jugador("P1"); j1.setColor("AMARILLO"); dummies.add(j1);
        Jugador j2 = new Jugador("P2"); j2.setColor("AZUL");     dummies.add(j2);
        Jugador j3 = new Jugador("P3"); j3.setColor("ROJO");     dummies.add(j3);
        Jugador j4 = new Jugador("P4"); j4.setColor("VERDE");    dummies.add(j4);
        
        this.modeloTablero.inicializarFichas(dummies);
        
        // Forzar cálculo inicial de coordenadas
        SwingUtilities.invokeLater(() -> panelTablero.calcularCoordenadas());
    }

    public void setClienteExistente(Cliente cliente) {
        this.clienteRed = cliente;
        this.clienteRed.setTableroView(this);
    }

    public void iniciarCliente(String host, String nickname) {
        this.clienteRed = new Cliente(); 
        this.clienteRed.setTableroView(this); 
        this.clienteRed.conectar(host, 5000, nickname);
        this.setTitle("Parchís - Jugador: " + nickname);
    }

    private void initComponents() {
        setTitle("Parchís - Equipo 5");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. Panel Central (Tablero) ---
        panelTablero = new PanelTablero();
        
        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                verificarClicFicha(e.getX(), e.getY());
            }
        });
        
        add(panelTablero, BorderLayout.CENTER);

        // --- 2. Panel Lateral ---
        JPanel panelLateral = new JPanel();
        panelLateral.setPreferredSize(new Dimension(300, 0));
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBackground(new Color(40, 40, 40)); 
        panelLateral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTurno = new JLabel("Esperando...");
        lblTurno.setForeground(Color.WHITE);
        lblTurno.setFont(new Font("Arial", Font.BOLD, 18));
        lblTurno.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblTurno);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        lblDado = new JLabel("🎲");
        lblDado.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        lblDado.setForeground(Color.ORANGE);
        lblDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 20)));

        btnLanzarDado = new JButton("LANZAR DADO");
        btnLanzarDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLanzarDado.setBackground(new Color(0, 153, 255));
        btnLanzarDado.setForeground(Color.WHITE);
        btnLanzarDado.setFont(new Font("Arial", Font.BOLD, 16));
        btnLanzarDado.setFocusPainted(false);
        btnLanzarDado.addActionListener(e -> {
            if (clienteRed != null) clienteRed.enviar("{ \"type\": \"ROLL\" }");
        });
        
        panelLateral.add(btnLanzarDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel lblLog = new JLabel("Historial:");
        lblLog.setForeground(Color.LIGHT_GRAY);
        lblLog.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblLog);
        
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setBackground(new Color(60, 60, 60));
        areaLog.setForeground(Color.WHITE);
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setPreferredSize(new Dimension(240, 300));
        panelLateral.add(scrollLog);

        add(panelLateral, BorderLayout.EAST);
    }
    
    public void procesarMensajeJuego(String json) {
        if (json.contains("DICE_RESULT")) {
             String valStr = json.split("\"value\": ")[1].split(" ")[0].replace("}", "").trim();
             mostrarResultadoDado(Integer.parseInt(valStr));
        }
        else if (json.contains("UPDATE")) {
             try {
                String data = json.split("\"board\": \"")[1].split("\"")[0];
                String[] fichasStr = data.split(",");
                for (String fStr : fichasStr) {
                    String[] partes = fStr.split(":");
                    String color = partes[0];
                    int id = Integer.parseInt(partes[1]);
                    int pos = Integer.parseInt(partes[2]);
                    boolean enBase = partes[3].equals("1");
                    actualizarFichaLocal(color, id, pos, enBase);
                }
                panelTablero.repaint();
            } catch (Exception e) {}
        }
        else if (json.contains("LOG")) {
            String msg = json.split("\"msg\": \"")[1].split("\"")[0];
            agregarLog(msg);
        }
        else if (json.contains("TURN")) {
             // Actualizar turno visualmente si se desea
        }
    }
    
    private void verificarClicFicha(int x, int y) {
        if (modeloTablero == null || clienteRed == null) return;
        
        // Buscar ficha bajo el clic
        for (Ficha f : modeloTablero.getTodasLasFichas()) {
            Point p = panelTablero.obtenerCoordenadasFicha(f);
            if (p.distance(x, y) < 25) { // Radio de clic
                clienteRed.enviar("{ \"type\": \"MOVE\", \"ficha\": " + f.getId() + " }");
                return; 
            }
        }
    }

    public void actualizarFichaLocal(String color, int id, int pos, boolean enBase) {
        if (modeloTablero == null) return;
        for (Ficha f : modeloTablero.getTodasLasFichas()) {
            if (f.getColor().equals(color) && f.getId() == id) {
                f.setPosicion(pos);
                f.setEnBase(enBase);
                return;
            }
        }
    }

    public void agregarLog(String mensaje) {
        areaLog.append("> " + mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void mostrarResultadoDado(int valor) {
        lblDado.setText("🎲 " + valor);
    }

    // =================================================================================
    // CLASE INTERNA: DIBUJO DEL TABLERO
    // =================================================================================
    private class PanelTablero extends JPanel {
        
        // Constantes de colores
        private final Color C_AMARILLO = new Color(255, 215, 0);
        private final Color C_ROJO = new Color(220, 20, 60);
        private final Color C_VERDE = new Color(34, 139, 34);
        private final Color C_AZUL = new Color(30, 144, 255);
        
        private int cx, cy; // Centro
        private int size;   // Tamaño celda base

        public PanelTablero() {
            setBackground(new Color(245, 230, 210)); // Color madera fondo
        }
        
        // Método crítico: Calcular dónde va cada casilla (1-68) geométricamente
        public void calcularCoordenadas() {
            int w = getWidth();
            int h = getHeight();
            cx = w / 2;
            cy = h / 2;
            
            // Tamaño relativo de una "casilla"
            size = Math.min(w, h) / 15; 
            
            // Lógica simple para mapear el recorrido rectangular del parchís
            // Este es un mapeo aproximado para dibujar las fichas sobre el dibujo
            // Se asume un recorrido antihorario empezando a la derecha
            
            // NOTA: Implementación completa de mapeo requeriría 68 "if" o lógica vectorial compleja
            // Usaremos una aproximación circular ajustada para que "aparezca" algo jugable
            
            int radio = (int)(size * 5.5);
            for (int i = 1; i <= 68; i++) {
                double angle = Math.toRadians((i - 1) * (360.0 / 68.0)); 
                coordenadasCasillas[i] = new Point(
                    cx + (int)(radio * Math.cos(angle)),
                    cy + (int)(radio * Math.sin(angle))
                );
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            calcularCoordenadas(); // Recalcular si redimensiona
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. DIBUJAR BASES (Cuadrados grandes)
            int baseSize = size * 4;
            // Amarillo (Abajo-Der)
            dibujarBase(g2, C_AMARILLO, getWidth() - baseSize - 20, getHeight() - baseSize - 20, baseSize);
            // Rojo (Arriba-Der) -> Ojo: En parchís clásico suele ser Rojo Abajo-Der, Amarillo Abajo-Izq, pero depende versión
            // Usaremos: Rojo (Der-Abajo), Verde (Izq-Abajo), Azul (Izq-Arriba), Amarillo (Der-Arriba)
            
            // Ajustamos posiciones estándar
            dibujarBase(g2, C_AMARILLO, getWidth() - baseSize - 20, 20, baseSize); // Arriba Der
            dibujarBase(g2, C_AZUL, 20, 20, baseSize); // Arriba Izq
            dibujarBase(g2, C_VERDE, 20, getHeight() - baseSize - 20, baseSize); // Abajo Izq
            dibujarBase(g2, C_ROJO, getWidth() - baseSize - 20, getHeight() - baseSize - 20, baseSize); // Abajo Der

            // 2. DIBUJAR CAMINOS (Cruces)
            g2.setColor(Color.LIGHT_GRAY);
            // Horizontal
            g2.fillRect(0, cy - size, getWidth(), size * 2);
            // Vertical
            g2.fillRect(cx - size, 0, size * 2, getHeight());
            
            // 3. DIBUJAR META (Triángulos centrales)
            g2.setColor(C_AMARILLO);
            g2.fillPolygon(new int[]{cx, cx+size*2, cx+size*2}, new int[]{cy, cy-size, cy+size}, 3);
            
            // Dibujar círculo central
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - size*2, cy - size*2, size*4, size*4);
            g2.setColor(Color.BLACK);
            g2.drawOval(cx - size*2, cy - size*2, size*4, size*4);

            // 4. DIBUJAR CASILLAS (Para visualización)
            g2.setColor(Color.GRAY);
            for(int i=1; i<=68; i++) {
                Point p = coordenadasCasillas[i];
                if(p != null) g2.drawRect(p.x-5, p.y-5, 10, 10);
            }

            // 5. DIBUJAR FICHAS
            if (modeloTablero != null) {
                for (Ficha f : modeloTablero.getTodasLasFichas()) {
                    dibujarFicha(g2, f);
                }
            }
        }

        private void dibujarBase(Graphics2D g, Color c, int x, int y, int s) {
            g.setColor(c);
            g.fillRect(x, y, s, s);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(3));
            g.drawRect(x, y, s, s);
            
            // Círculo blanco interno
            g.setColor(Color.WHITE);
            g.fillOval(x + s/4, y + s/4, s/2, s/2);
        }

        private void dibujarFicha(Graphics2D g, Ficha f) {
            Point p = obtenerCoordenadasFicha(f);
            Color c = obtenerColorReal(f.getColor());
            
            int fichaSize = 28;
            
            // Sombra
            g.setColor(new Color(0,0,0,80));
            g.fillOval(p.x + 3, p.y + 3, fichaSize, fichaSize);
            
            // Cuerpo
            g.setColor(c);
            g.fillOval(p.x, p.y, fichaSize, fichaSize);
            
            // Borde
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawOval(p.x, p.y, fichaSize, fichaSize);
            
            // Número ID
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(String.valueOf(f.getId()), p.x + 10, p.y + 19);
        }
        
        // Calcula posición visual final (Bases o Tablero)
        public Point obtenerCoordenadasFicha(Ficha f) {
            if (f.isEnBase()) {
                int baseSize = size * 4;
                int offsetX = (f.getId() % 2 == 0) ? 20 : -20;
                int offsetY = (f.getId() > 2) ? 20 : -20;
                
                // Centros aproximados de las bases
                switch (f.getColor()) {
                    case "AZUL": return new Point(20 + baseSize/2 + offsetX, 20 + baseSize/2 + offsetY);
                    case "AMARILLO": return new Point(getWidth() - 20 - baseSize/2 + offsetX, 20 + baseSize/2 + offsetY);
                    case "VERDE": return new Point(20 + baseSize/2 + offsetX, getHeight() - 20 - baseSize/2 + offsetY);
                    case "ROJO": return new Point(getWidth() - 20 - baseSize/2 + offsetX, getHeight() - 20 - baseSize/2 + offsetY);
                }
            } else {
                int pos = f.getPosicion();
                if (pos >= 1 && pos <= 68 && coordenadasCasillas[pos] != null) {
                    return coordenadasCasillas[pos];
                }
            }
            return new Point(cx, cy); // Fallback al centro
        }
        
        private Color obtenerColorReal(String c) {
            if (c == null) return Color.GRAY;
            switch (c) {
                case "ROJO": return C_ROJO;
                case "VERDE": return C_VERDE;
                case "AZUL": return C_AZUL;
                case "AMARILLO": return C_AMARILLO;
                default: return Color.GRAY;
            }
        }
    }
}