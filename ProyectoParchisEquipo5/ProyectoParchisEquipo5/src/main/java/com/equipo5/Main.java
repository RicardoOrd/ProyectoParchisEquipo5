package com.equipo5;

import com.equipo5.view.MenuUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Iniciar la interfaz gráfica en el hilo de despacho de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new MenuUI().setVisible(true);
        });
    }
}
    

