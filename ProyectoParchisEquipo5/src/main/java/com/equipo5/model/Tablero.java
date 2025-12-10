package com.equipo5.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tablero {

    private Map<String, List<Ficha>> fichasPorJugador;

    public Tablero() {
        this.fichasPorJugador = new HashMap<>();
    }

    public void inicializarFichas(List<Jugador> jugadores) {
        fichasPorJugador.clear();
        String[] coloresDisponibles = {"AMARILLO", "AZUL", "ROJO", "VERDE"};

        int i = 0;
        for (Jugador jugador : jugadores) {
            if (jugador.getColor() == null && i < coloresDisponibles.length) {
                jugador.setColor(coloresDisponibles[i]);
            }
            String color = jugador.getColor();
            List<Ficha> misFichas = new ArrayList<>();
            for (int f = 1; f <= 4; f++) {
                misFichas.add(new Ficha(color, f));
            }
            fichasPorJugador.put(color, misFichas);
            i++;
        }
        System.out.println("Tablero inicializado.");
    }

    public List<Ficha> getFichasDelColor(String color) {
        return fichasPorJugador.getOrDefault(color, new ArrayList<>());
    }

    public List<Ficha> getTodasLasFichas() {
        List<Ficha> todas = new ArrayList<>();
        for (List<Ficha> lista : fichasPorJugador.values()) {
            todas.addAll(lista);
        }
        return todas;
    }

// Modificamos la firma para recibir la regla
    public boolean tieneMovimientosPosibles(String color, int valorDado, boolean obligarSalida5) {
        List<Ficha> fichas = getFichasDelColor(color);
        for (Ficha f : fichas) {
            // Pasamos la regla a la validación individual
            if (esMovimientoValido(f, valorDado, obligarSalida5)) {
                return true;
            }
        }
        return false;
    }

    public boolean esMovimientoValido(Ficha f, int valorDado, boolean obligarSalida5) {
        if (f.isEnMeta()) {
            return false;
        }

        if (f.isEnBase()) {
            // Si la regla está activa (true), SOLO es válido si el dado es 5.
            if (obligarSalida5) {
                return (valorDado == 5);
            }
            // Si la regla NO está activa, permitimos salir con cualquier número (lógica anterior)
            return true;
        } else {
            return true;
        }
    }
}
