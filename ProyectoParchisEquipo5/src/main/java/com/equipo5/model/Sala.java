package com.equipo5.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sala {
    private String codigo;
    private boolean publica;
    private int maxJugadores;
    private List<Jugador> jugadores;
    private boolean enJuego;

    // Constructor para UC-04 Crear/Configurar partida
    public Sala(boolean publica, int maxJugadores) {
        this.codigo = generarCodigoUnico();
        this.publica = publica;
        this.maxJugadores = maxJugadores;
        this.jugadores = new ArrayList<>();
        this.enJuego = false;
    }

    private String generarCodigoUnico() {
        // Genera un código corto, ej: "A1B2"
        return UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // Métodos del diagrama UC-02 [cite: 109]
    public boolean hayEspacio() {
        return jugadores.size() < maxJugadores;
    }

    public boolean agregarJugador(Jugador j) {
        if (!hayEspacio() || enJuego) {
            return false;
        }
        jugadores.add(j);
        return true;
    }
    
    public void eliminarJugador(Jugador j) {
        jugadores.remove(j);
    }

    // Getters y Setters según diagrama [cite: 107]
    public String getCodigo() { return codigo; }
    public boolean isPublica() { return publica; }
    public List<Jugador> getJugadores() { return jugadores; }
    public boolean isEnJuego() { return enJuego; }
    public void setEnJuego(boolean enJuego) { this.enJuego = enJuego; }
}