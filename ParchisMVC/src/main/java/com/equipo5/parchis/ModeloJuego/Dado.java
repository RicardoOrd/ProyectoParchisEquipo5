package com.equipo5.parchis.ModeloJuego;

import java.util.Random;

public class Dado {
    private final Random random = new Random();
    public int lanzar() {
        return random.nextInt(6) + 1;
    }
}
