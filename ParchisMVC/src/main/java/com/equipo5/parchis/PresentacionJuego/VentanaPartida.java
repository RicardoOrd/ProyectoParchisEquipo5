package com.equipo5.parchis.PresentacionJuego;

import com.equipo5.parchis.ModeloJuego.Jugador;
import com.equipo5.parchis.controlador.ControlJuego;
import com.equipo5.parchis.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaPartida extends JFrame {
    private final ControlJuego control;
    private final JLabel lblTurno = new JLabel();
    private final JTextArea areaLog = new JTextArea(12, 36);
    private final JButton btnLanzar = Buttons.primary("LANZAR DADO");
    private final JButton btnSiguiente = Buttons.secondary("Siguiente turno");

    public VentanaPartida(ControlJuego control) {
        super("Partida - Lanzar dado (UC-05)");
        this.control = control;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        GradientPanel bg = new GradientPanel();
        bg.setLayout(new BorderLayout(0, 16));
        bg.setBorder(new EmptyBorder(18,18,18,18));
        setContentPane(bg);

        JLabel title = new JLabel("PARCHIS", SwingConstants.CENTER);
        title.setForeground(Color.YELLOW);
        title.setFont(UITheme.titleFont(title));
        bg.add(title, BorderLayout.NORTH);

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(10,10));
        bg.add(card, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        top.setOpaque(false);
        JLabel lbl = new JLabel("Turno de:");
        lbl.setFont(UITheme.subtitleFont(lbl));
        top.add(lbl);
        top.add(lblTurno);
        card.add(top, BorderLayout.NORTH);

        areaLog.setEditable(false);
        areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        card.add(new JScrollPane(areaLog), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnLanzar);
        actions.add(btnSiguiente);
        card.add(actions, BorderLayout.SOUTH);

        btnLanzar.addActionListener(e -> lanzar());
        btnSiguiente.addActionListener(e -> siguiente());

        actualizarTurno();
    }

    private void actualizarTurno() {
        Jugador j = control.getJuego().getJugadorEnTurno();
        lblTurno.setText(j != null ? j.getNombre() + " (" + j.getColor() + ")" : "-");
    }

    private void lanzar() {
        try {
            String msg = control.lanzarDadoYObtenerMensaje();
            areaLog.append(msg + "\n");
            // Auto-avanza turno
            control.siguienteTurno();
            actualizarTurno();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void siguiente() {
        control.siguienteTurno();
        actualizarTurno();
    }
}
