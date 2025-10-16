package com.equipo5.parchis;

import com.equipo5.parchis.PresentacionJuego.VentanaLobby;
import javax.swing.SwingUtilities;

public class ParchisMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaLobby lobby = new VentanaLobby();
            lobby.setVisible(true);
        });
    }
}
