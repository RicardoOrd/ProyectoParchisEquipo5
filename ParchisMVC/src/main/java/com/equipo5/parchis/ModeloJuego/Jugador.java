package com.equipo5.parchis.ModeloJuego;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Jugador {
    private final String id = UUID.randomUUID().toString();
    private final String nombre;
    private final String color;
    private final List<Integer> tiros = new ArrayList<>();

    public Jugador(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public void registrarTiro(int valor) {
        tiros.add(valor);
    }

    public List<Integer> getTiros() {
        return Collections.unmodifiableList(tiros);
    }

    public String getNombre() { return nombre; }
    public String getColor() { return color; }
    public String getId() { return id; }
}
