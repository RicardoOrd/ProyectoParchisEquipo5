package com.equipo5.parchis.PresentacionJuego;

import com.equipo5.parchis.ModeloJuego.Juego;
import com.equipo5.parchis.controlador.ControlJuego;
import com.equipo5.parchis.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.stream.Collectors;

public class VentanaLobby extends JFrame {
    private final JTextField txtNombre1 = new JTextField(14);
    private final JTextField txtColor1 = new JTextField(10);
    private final JTextField txtNombre2 = new JTextField(14);
    private final JTextField txtColor2 = new JTextField(10);
    private final JTextArea areaJugadores = new JTextArea(12, 24);
    private final JLabel lblContador = new JLabel("Jugadores registrados: 0/4");
    private final JLabel lblAviso = new JLabel(" ");
    private final JButton btnAgregar1 = Buttons.primary("Agregar J1");
    private final JButton btnAgregar2 = Buttons.primary("Agregar J2");
    private final JButton btnIniciar = Buttons.secondary("Iniciar partida");

    private final ControlJuego control = new ControlJuego(new Juego());

    public VentanaLobby() {
        super("Lobby - Parchis (Simulacion UC)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 520);
        setLocationRelativeTo(null);

        GradientPanel bg = new GradientPanel();
        bg.setLayout(new BorderLayout(0, 16));
        bg.setBorder(new EmptyBorder(18,18,18,18));
        setContentPane(bg);

        // Header
        JLabel title = new JLabel("PARCHIS", SwingConstants.CENTER);
        title.setForeground(Color.YELLOW);
        title.setFont(UITheme.titleFont(title));
        bg.add(title, BorderLayout.NORTH);

        // Card (center)
        CardPanel card = new CardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        bg.add(card, BorderLayout.CENTER);

        // Subheader inside card
        JLabel subt = new JLabel("Registro rápido");
        subt.setFont(UITheme.subtitleFont(subt));
        subt.setForeground(UITheme.TEXT_DARK);
        gc.gridx=0; gc.gridy=0; gc.gridwidth=4; gc.weightx=1;
        card.add(subt, gc);

        // Left form
        gc.gridwidth=1; gc.weightx=0;
        gc.gridx=0; gc.gridy=1; card.add(new JLabel("Nombre J1:"), gc);
        gc.gridx=1; gc.gridy=1; gc.weightx=1; card.add(txtNombre1, gc);
        gc.gridx=2; gc.gridy=1; gc.weightx=0; card.add(new JLabel("Color:"), gc);
        gc.gridx=3; gc.gridy=1; gc.weightx=0.5; card.add(txtColor1, gc);
        gc.gridx=4; gc.gridy=1; gc.weightx=0; card.add(btnAgregar1, gc);

        gc.gridx=0; gc.gridy=2; gc.weightx=0; card.add(new JLabel("Nombre J2:"), gc);
        gc.gridx=1; gc.gridy=2; gc.weightx=1; card.add(txtNombre2, gc);
        gc.gridx=2; gc.gridy=2; gc.weightx=0; card.add(new JLabel("Color:"), gc);
        gc.gridx=3; gc.gridy=2; gc.weightx=0.5; card.add(txtColor2, gc);
        gc.gridx=4; gc.gridy=2; gc.weightx=0; card.add(btnAgregar2, gc);

        // Right list inside card
        JPanel right = new JPanel(new BorderLayout(8,8));
        right.setOpaque(false);
        lblContador.setFont(lblContador.getFont().deriveFont(Font.BOLD));
        right.add(lblContador, BorderLayout.NORTH);
        areaJugadores.setEditable(false);
        areaJugadores.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        right.add(new JScrollPane(areaJugadores), BorderLayout.CENTER);

        gc.gridx=5; gc.gridy=1; gc.gridheight=2; gc.weightx=1; gc.fill=GridBagConstraints.BOTH;
        card.add(right, gc);

        // Footer inside card
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        lblAviso.setForeground(new Color(0,128,0));
        footer.add(lblAviso, BorderLayout.WEST);
        btnIniciar.setEnabled(false);
        footer.add(btnIniciar, BorderLayout.EAST);

        gc.gridx=0; gc.gridy=3; gc.gridwidth=6; gc.gridheight=1; gc.weightx=1; gc.fill=GridBagConstraints.HORIZONTAL;
        card.add(footer, gc);

        // Listeners
        btnAgregar1.addActionListener(e -> agregar(txtNombre1.getText(), txtColor1.getText(), 1));
        btnAgregar2.addActionListener(e -> agregar(txtNombre2.getText(), txtColor2.getText(), 2));
        btnIniciar.addActionListener(e -> iniciar());
        txtColor1.addActionListener(e -> btnAgregar1.doClick());
        txtColor2.addActionListener(e -> btnAgregar2.doClick());

        SwingUtilities.invokeLater(() -> txtNombre1.requestFocusInWindow());
        renderJugadores();
    }

    private void agregar(String nombre, String color, int cual) {
        try {
            String n = (nombre == null || nombre.isBlank()) ? "Jugador" + (control.getNumeroJugadores()+1) : nombre.trim();
            String c = (color == null || color.isBlank()) ? "sin-color" : color.trim();
            control.agregarJugador(n, c);
            lblAviso.setText("\u2713 " + n + " registrado");
            if (cual == 1) { txtNombre1.setText(""); txtColor1.setText(""); txtNombre2.requestFocusInWindow(); }
            else { txtNombre2.setText(""); txtColor2.setText(""); txtNombre1.requestFocusInWindow(); }
            renderJugadores();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void iniciar() {
        try {
            control.iniciarPartida();
            VentanaPartida vp = new VentanaPartida(control);
            vp.setVisible(true);
            this.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede iniciar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderJugadores() {
        var lista = control.getJuego().getJugadores().stream()
                .map(j -> "- " + j.getNombre() + " (" + j.getColor() + ")")
                .collect(Collectors.joining("\n"));
        areaJugadores.setText(lista);
        lblContador.setText("Jugadores registrados: " + control.getNumeroJugadores() + "/4");
        btnIniciar.setEnabled(control.puedeIniciar());
    }
}
