package com.equipo5.parchis.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CardPanel extends JPanel {
    public CardPanel() {
        setOpaque(false);
        setBorder(new EmptyBorder(16,16,16,16));
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(UITheme.CARD_BG);
        g2.fillRoundRect(0, 0, w-1, h-1, 24, 24);
        g2.setColor(UITheme.CARD_BORDER);
        g2.drawRoundRect(0, 0, w-1, h-1, 24, 24);
        g2.dispose();
        super.paintComponent(g);
    }
}
