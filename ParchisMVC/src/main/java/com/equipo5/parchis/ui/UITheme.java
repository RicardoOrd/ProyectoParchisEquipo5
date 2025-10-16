package com.equipo5.parchis.ui;

import java.awt.*;

public class UITheme {
    // Colors inspired by the storyboard
    public static final Color GRADIENT_TOP = new Color(132, 94, 247);   // violeta
    public static final Color GRADIENT_BOTTOM = new Color(67, 97, 238); // azul
    public static final Color CARD_BG = new Color(255, 255, 255, 235);
    public static final Color CARD_BORDER = new Color(230, 230, 250);
    public static final Color TEXT_DARK = new Color(40, 40, 50);

    public static final Color BTN_PRIMARY_BG = new Color(46, 204, 113);  // verde
    public static final Color BTN_PRIMARY_FG = Color.WHITE;
    public static final Color BTN_SECONDARY_BG = new Color(52, 152, 219); // azul
    public static final Color BTN_WARN_BG = new Color(231, 76, 60); // rojo

    public static Font titleFont(Component c) {
        return c.getFont().deriveFont(Font.BOLD, 28f);
    }

    public static Font subtitleFont(Component c) {
        return c.getFont().deriveFont(Font.BOLD, 14f);
    }
}
