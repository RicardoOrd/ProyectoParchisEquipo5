package com.equipo5.parchis.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Buttons {
    public static JButton primary(String text) {
        return styled(text, UITheme.BTN_PRIMARY_BG, UITheme.BTN_PRIMARY_FG);
    }
    public static JButton secondary(String text) {
        return styled(text, UITheme.BTN_SECONDARY_BG, UITheme.BTN_PRIMARY_FG);
    }
    public static JButton warn(String text) {
        return styled(text, UITheme.BTN_WARN_BG, UITheme.BTN_PRIMARY_FG);
    }
    private static JButton styled(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); int h = getHeight();
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0,0,w,h,18,18));
                g2.setColor(fg);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBackground(bg);
        return b;
    }
}
