package com.equipo5.view;

import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.model.Tablero;
import com.equipo5.net.Cliente;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class TableroUI extends javax.swing.JFrame {

    private PanelTablero panelTablero;
    private JTextArea areaLog;
    private JButton btnLanzarDado;
    private JLabel lblDado;
    private JLabel lblTurno;
    
    private Cliente clienteRed;
    private Tablero modeloTablero; 
    
    public TableroUI() {
        initComponents();
        this.modeloTablero = new Tablero(); 
        
        // Inicialización visual dummy para pruebas
        List<Jugador> dummies = new ArrayList<>();
        Jugador j1 = new Jugador("P1"); j1.setColor("AMARILLO"); dummies.add(j1);
        Jugador j2 = new Jugador("P2"); j2.setColor("AZUL");     dummies.add(j2);
        Jugador j3 = new Jugador("P3"); j3.setColor("ROJO");     dummies.add(j3);
        Jugador j4 = new Jugador("P4"); j4.setColor("VERDE");    dummies.add(j4);
        
        this.modeloTablero.inicializarFichas(dummies);
        // Forzamos el repintado inicial
        SwingUtilities.invokeLater(() -> panelTablero.repaint());
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
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        panelTablero = new PanelTablero();
        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                verificarClicFicha(e.getX(), e.getY());
            }
        });
        add(panelTablero, BorderLayout.CENTER);

        // Panel Lateral
        JPanel panelLateral = new JPanel();
        panelLateral.setPreferredSize(new Dimension(300, 0));
        panelLateral.setLayout(new BoxLayout(panelLateral, BoxLayout.Y_AXIS));
        panelLateral.setBackground(new Color(30, 30, 30)); 
        panelLateral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblTurno = new JLabel("Esperando...");
        lblTurno.setForeground(Color.WHITE);
        lblTurno.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTurno.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblTurno);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 40)));

        lblDado = new JLabel("🎲");
        lblDado.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        lblDado.setForeground(Color.WHITE); 
        lblDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        btnLanzarDado = new JButton("LANZAR DADO");
        btnLanzarDado.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLanzarDado.setBackground(new Color(0, 120, 215));
        btnLanzarDado.setForeground(Color.WHITE);
        btnLanzarDado.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLanzarDado.setFocusPainted(false);
        btnLanzarDado.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLanzarDado.addActionListener(e -> {
            if (clienteRed != null) clienteRed.enviar("{ \"type\": \"ROLL\" }");
        });
        
        panelLateral.add(btnLanzarDado);
        panelLateral.add(Box.createRigidArea(new Dimension(0, 40)));

        JLabel lblLog = new JLabel("Historial:");
        lblLog.setForeground(Color.LIGHT_GRAY);
        lblLog.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLog.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateral.add(lblLog);
        
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        areaLog.setBackground(new Color(50, 50, 50));
        areaLog.setForeground(Color.WHITE);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createLineBorder(new Color(70,70,70)));
        scrollLog.setPreferredSize(new Dimension(240, 400));
        panelLateral.add(scrollLog);

        add(panelLateral, BorderLayout.EAST);
    }
    
    public void procesarMensajeJuego(String json) {
        if (json.contains("DICE_RESULT")) {
             String valStr = json.split("\"value\": ")[1].split(",")[0].replace("}", "").trim();
             int valor = Integer.parseInt(valStr);
             Color cDado = Color.WHITE;
             if (json.contains("\"color\": \"")) {
                 String colStr = json.split("\"color\": \"")[1].split("\"")[0];
                 cDado = panelTablero.obtenerColorReal(colStr);
             }
             mostrarResultadoDado(valor, cDado);
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
    }
    
    private void verificarClicFicha(int x, int y) {
        if (modeloTablero == null || clienteRed == null) return;
        for (Ficha f : modeloTablero.getTodasLasFichas()) {
            Point p = panelTablero.obtenerCoordenadasFicha(f);
            if (p != null && p.distance(x, y) < panelTablero.getCellSize() * 0.6) { 
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

    public void mostrarResultadoDado(int valor, Color color) {
        lblDado.setText("🎲 " + valor);
        lblDado.setForeground(color);
    }

    // =================================================================================
    // CLASE INTERNA: DIBUJO GEOMÉTRICO 19x19 (GRID SYSTEM) - ORIGINAL Y RECTO
    // =================================================================================
    private class PanelTablero extends JPanel {
        
        private final Color C_AMARILLO = new Color(255, 215, 0);
        private final Color C_ROJO = new Color(220, 20, 60);
        private final Color C_VERDE = new Color(34, 139, 34);
        private final Color C_AZUL = new Color(30, 144, 255);
        private final Color C_FONDO = new Color(245, 230, 210); 
        private final Color C_CAMINO = Color.WHITE;
        private final Color C_BORDE = new Color(50, 50, 50);
        private final Color C_SEGURO = new Color(200, 200, 200);

        private int cx, cy; 
        private int cellSize;
        
        // Mapeo EXACTO: Índice 1-68 -> Point(x,y)
        private Map<Integer, Point> mapaCasillas = new HashMap<>();
        private Map<String, Point> mapaBases = new HashMap<>();
        
        public PanelTablero() {
            setBackground(C_FONDO);
        }
        
        public int getCellSize() { return cellSize; }
        
        // Convierte coordenadas de grid (0-18) a pixeles
        private Point gridToPixel(int gx, int gy) {
            // El tablero es 19x19. gx, gy van de 0 a 18.
            // Centramos el tablero en el panel
            int boardSize = cellSize * 19;
            int startX = cx - boardSize / 2;
            int startY = cy - boardSize / 2;
            
            return new Point(
                startX + gx * cellSize,
                startY + gy * cellSize
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.5f));

            int w = getWidth();
            int h = getHeight();
            int minDim = Math.min(w, h);
            
            // Calculamos centro y tamaño de celda
            cx = w / 2;
            cy = h / 2;
            cellSize = (minDim - 40) / 19; 
            
            // Limpiar mapas para recalcular posiciones
            mapaCasillas.clear();
            mapaBases.clear();

            // --- 1. DIBUJAR BRAZOS (GRID LOGIC) ---
            // Rojo (Izquierda): Filas 8-10, Cols 0-7
            drawHorizontalArm(g2, 0, 7, C_ROJO, 35, 42, 27, 34); // Indices aproximados
            
            // Amarillo (Derecha): Filas 8-10, Cols 11-18
            drawHorizontalArm(g2, 11, 18, C_AMARILLO, 1, 8, 61, 68);
            
            // Azul (Arriba): Cols 8-10, Filas 0-7
            drawVerticalArm(g2, 0, 7, C_AZUL, 18, 25, 10, 17);
            
            // Verde (Abajo): Cols 8-10, Filas 11-18
            drawVerticalArm(g2, 11, 18, C_VERDE, 52, 59, 44, 51);
            
            // --- 2. DIBUJAR CENTRO ---
            drawCenter(g2);
            
            // --- 3. DIBUJAR BASES ---
            drawBase(g2, 0, 0, C_ROJO);        // Top-Left
            drawBase(g2, 13, 0, C_AZUL);       // Top-Right
            drawBase(g2, 0, 13, C_VERDE);      // Bot-Left
            drawBase(g2, 13, 13, C_AMARILLO);  // Bot-Right
            
            // --- 4. MAPEO MANUAL DE ESQUINAS ---
            // Las esquinas no entran en los loops de brazos, hay que añadirlas
            registerCell(9, 11, 7);  // Esquina Der-Sup (Antes de Azul)
            registerCell(26, 7, 7);  // Esquina Izq-Sup (Antes de Rojo)
            registerCell(43, 7, 11); // Esquina Izq-Inf (Antes de Verde)
            registerCell(60, 11, 11); // Esquina Der-Inf (Antes de Amarillo)
            // Pintar las esquinas manualmente para que se vean
            drawSingleCell(g2, 11, 7, Color.WHITE);
            drawSingleCell(g2, 7, 7, Color.WHITE);
            drawSingleCell(g2, 7, 11, Color.WHITE);
            drawSingleCell(g2, 11, 11, Color.WHITE);

            // --- 5. DIBUJAR FICHAS ---
            if (modeloTablero != null) {
                for (Ficha f : modeloTablero.getTodasLasFichas()) {
                    dibujarFicha(g2, f);
                }
            }
        }
        
        private void drawHorizontalArm(Graphics2D g, int startCol, int endCol, Color teamColor, 
                                      int idxStartOut, int idxEndOut, int idxStartIn, int idxEndIn) {
            // Fila Superior (8): Entrada o Salida según lado
            // Fila Media (9): Pasillo Color
            // Fila Inferior (10): Salida o Entrada según lado
            
            // ROJO (Izquierda 0-7): 
            // Fila 8 (Arriba): Entrada (34..27) -> Col 7..0
            // Fila 9: Pasillo
            // Fila 10 (Abajo): Salida (35..42) -> Col 7..0
            
            // AMARILLO (Derecha 11-18):
            // Fila 8 (Arriba): Entrada (68..61) -> Col 11..18
            // Fila 9: Pasillo
            // Fila 10 (Abajo): Salida (1..8) -> Col 11..18
            
            boolean isLeft = (startCol == 0);
            
            for (int col = startCol; col <= endCol; col++) {
                // Fila 8
                int idxTop = isLeft ? (idxEndIn - (7-col)) : (idxEndIn - (col-11)); 
                // Fix indices Rojo: 27..34. Col 0->34? No. Col 7->34 (cerca centro). Col 0->27.
                // Rojo Fila 8 (In): Col 0(27) -> Col 7(34)
                if (isLeft) idxTop = idxStartIn + col; 
                
                Color cTop = C_CAMINO;
                if(isLeft && col==2) cTop = C_SEGURO; // Seguro Rojo
                if(!isLeft && col==16) cTop = C_SEGURO; // Seguro Amarillo Entrada (66)? No 63.
                // 61(18)..68(11). 63 is at col 16? 68-11=57. 63 is 68-5. 
                // Let's rely on standard positions: 5th cell from end.
                
                drawSingleCell(g, col, 8, cTop);
                if(isLeft) registerCell(idxTop, col, 8); 
                else registerCell(idxTop, col, 8); // Need Logic mapping
                
                // Fila 9 (Pasillo)
                drawSingleCell(g, col, 9, (isLeft && col==0) || (!isLeft && col==18) ? C_CAMINO : teamColor);
                
                // Fila 10
                int idxBot = isLeft ? (idxStartOut + (7-col)) : (idxStartOut + (col-11));
                // Rojo Out: 35(Col 7)..42(Col 0).
                Color cBot = C_CAMINO;
                if (isLeft && col==7) cBot = C_SEGURO; // Rojo Salida
                if (!isLeft && col==11) cBot = C_SEGURO; // Amarillo Salida (5? no 1)
                // Amarillo Salida es 5. 1 es col 11. 5 es col 15.
                if(!isLeft && col==15) cBot = C_SEGURO; 
                
                drawSingleCell(g, col, 10, cBot);
                registerCell(idxBot, col, 10);
            }
            
            // RE-MAPEO MANUAL PARA PRECISIÓN (Sobreescribe loop anterior)
            if (isLeft) { // ROJO
                // Out (35-42): Fila 10, Col 7->0
                for(int i=0; i<8; i++) registerCell(35+i, 7-i, 10);
                // In (27-34): Fila 8, Col 0->7
                for(int i=0; i<8; i++) registerCell(27+i, i, 8);
                
            } else { // AMARILLO
                // Out (1-8): Fila 10, Col 11->18
                for(int i=0; i<8; i++) registerCell(1+i, 11+i, 10);
                // In (61-68): Fila 8, Col 18->11
                for(int i=0; i<8; i++) registerCell(61+i, 18-i, 8);
            }
        }
        
        private void drawVerticalArm(Graphics2D g, int startRow, int endRow, Color teamColor,
                                    int idxStartOut, int idxEndOut, int idxStartIn, int idxEndIn) {
            // AZUL (Arriba 0-7):
            // Col 8: Salida (18..25) -> Row 7..0
            // Col 9: Pasillo
            // Col 10: Entrada (10..17) -> Row 0..7
            
            // VERDE (Abajo 11-18):
            // Col 8: Entrada (44..51) -> Row 18..11
            // Col 9: Pasillo
            // Col 10: Salida (52..59) -> Row 11..18
            
            boolean isTop = (startRow == 0);
            
            for(int row = startRow; row <= endRow; row++) {
                // Col 8
                drawSingleCell(g, 8, row, C_CAMINO);
                // Col 9
                drawSingleCell(g, 9, row, (isTop && row==0) || (!isTop && row==18) ? C_CAMINO : teamColor);
                // Col 10
                drawSingleCell(g, 10, row, C_CAMINO);
            }
            
            // SEGUROS MANUALES
            if(isTop) {
                drawSingleCell(g, 10, 4, C_SEGURO); // Azul Salida (22)
                drawSingleCell(g, 8, 4, C_SEGURO); // 12
            } else {
                drawSingleCell(g, 10, 14, C_SEGURO); // Verde Salida (56)
                drawSingleCell(g, 8, 14, C_SEGURO); // 46
            }
            
            // MAPEO
            if (isTop) { // AZUL
                // Out (18-25): Col 8, Row 7->0
                for(int i=0; i<8; i++) registerCell(18+i, 8, 7-i);
                // In (10-17): Col 10, Row 0->7
                for(int i=0; i<8; i++) registerCell(10+i, 10, i);
            } else { // VERDE
                // Out (52-59): Col 10, Row 11->18
                for(int i=0; i<8; i++) registerCell(52+i, 10, 11+i);
                // In (44-51): Col 8, Row 18->11
                for(int i=0; i<8; i++) registerCell(44+i, 8, 18-i);
            }
        }
        
        private void drawSingleCell(Graphics2D g, int gx, int gy, Color c) {
            Point p = gridToPixel(gx, gy);
            g.setColor(c);
            g.fillRect(p.x, p.y, cellSize, cellSize);
            g.setColor(C_BORDE);
            g.drawRect(p.x, p.y, cellSize, cellSize);
        }
        
        private void registerCell(int idx, int gx, int gy) {
            Point p = gridToPixel(gx, gy);
            // Guardar centro
            mapaCasillas.put(idx, new Point(p.x + cellSize/2, p.y + cellSize/2));
        }
        
        private void drawBase(Graphics2D g, int gx, int gy, Color c) {
            // Base ocupa 6x6 celdas
            Point p = gridToPixel(gx, gy);
            int size = cellSize * 6;
            
            g.setColor(c);
            g.fillRoundRect(p.x, p.y, size, size, 20, 20);
            g.setColor(C_BORDE);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(p.x, p.y, size, size, 20, 20);
            
            // Círculo blanco
            int innerSize = (int)(size * 0.7);
            int offset = (size - innerSize) / 2;
            g.setColor(Color.WHITE);
            g.fillOval(p.x + offset, p.y + offset, innerSize, innerSize);
            g.setColor(C_BORDE);
            g.setStroke(new BasicStroke(1));
            g.drawOval(p.x + offset, p.y + offset, innerSize, innerSize);
            
            // Guardar centro base
            String k = "";
            if(c==C_ROJO) k="ROJO"; else if(c==C_AZUL) k="AZUL"; 
            else if(c==C_VERDE) k="VERDE"; else k="AMARILLO";
            mapaBases.put(k, new Point(p.x + size/2, p.y + size/2));
        }
        
        private void drawCenter(Graphics2D g) {
            Point pCenter = gridToPixel(9, 9);
            int cx = pCenter.x + cellSize/2; // True center pixel
            int cy = pCenter.y + cellSize/2;
            int half = (int)(cellSize * 1.5);
            
            // Area central es 3x3 celdas -> 8,8 a 10,10
            // Triangulos
            drawTri(g, cx, cy, half, 0, C_AMARILLO);
            drawTri(g, cx, cy, half, 90, C_VERDE);
            drawTri(g, cx, cy, half, 180, C_ROJO);
            drawTri(g, cx, cy, half, 270, C_AZUL);
        }
        
        private void drawTri(Graphics2D g, int cx, int cy, int size, int angle, Color c) {
            AffineTransform old = g.getTransform();
            g.rotate(Math.toRadians(angle), cx, cy);
            g.setColor(c);
            // Triangulo apuntando derecha
            int[] x = {cx, cx + size, cx + size};
            int[] y = {cy, cy - size, cy + size};
            g.fillPolygon(x, y, 3);
            g.setColor(C_BORDE);
            g.drawPolygon(x, y, 3);
            g.setTransform(old);
        }

        private void dibujarFicha(Graphics2D g, Ficha f) {
            Point p = obtenerCoordenadasFicha(f);
            if (p == null) return; 
            
            Color c = obtenerColorReal(f.getColor());
            int fSize = (int)(cellSize * 0.7);
            
            g.setColor(new Color(0,0,0,60));
            g.fillOval(p.x - fSize/2 + 2, p.y - fSize/2 + 2, fSize, fSize);
            g.setColor(c);
            g.fillOval(p.x - fSize/2, p.y - fSize/2, fSize, fSize);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(p.x - fSize/2, p.y - fSize/2, fSize, fSize);
            
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String txt = String.valueOf(f.getId());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(txt, p.x - fm.stringWidth(txt)/2, p.y + fm.getAscent()/2 - 2);
        }
        
        public Point obtenerCoordenadasFicha(Ficha f) {
            if (f.isEnBase()) {
                Point baseCenter = mapaBases.get(f.getColor());
                if (baseCenter == null) return new Point(cx, cy);
                int gap = (int)(cellSize * 1.2);
                int dx = (f.getId() % 2 == 0) ? 1 : -1;
                int dy = (f.getId() > 2) ? 1 : -1;
                return new Point(baseCenter.x + (dx * gap/2), baseCenter.y + (dy * gap/2));
            } else {
                if (mapaCasillas.containsKey(f.getPosicion())) {
                    return mapaCasillas.get(f.getPosicion());
                }
            }
            return new Point(cx, cy); 
        }
        
        public Color obtenerColorReal(String c) {
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