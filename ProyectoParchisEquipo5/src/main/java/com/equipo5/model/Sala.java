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
    
    // --- VARIABLES DE REGLAS (Configuración de la Partida) ---
    private boolean reglaTresSeises; // Penalización si sacas tres 6
    private boolean reglaComer20;    // Avanzar 20 si comes
    private boolean reglaSalida5;    // Obligatorio salir con 5

    // Constructor completo con reglas
    public Sala(boolean publica, int maxJugadores, boolean r3Seises, boolean rComer20, boolean rSalida5) {
        this.codigo = generarCodigoUnico();
        this.publica = publica;
        this.maxJugadores = maxJugadores;
        
        // Guardamos la configuración de reglas elegida por el anfitrión
        this.reglaTresSeises = r3Seises;
        this.reglaComer20 = rComer20;
        this.reglaSalida5 = rSalida5;
        
        this.jugadores = new ArrayList<>();
        this.enJuego = false;
    }

    // Constructor simplificado (por si lo usas en tests antiguos)
    public Sala(boolean publica, int maxJugadores) {
        this(publica, maxJugadores, true, true, false); // Valores por defecto
    }

    private String generarCodigoUnico() {
        // Genera un código corto de 4 caracteres (ej. "A1B2")
        return UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

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

    // --- GETTERS ESTÁNDAR ---
    public String getCodigo() { return codigo; }
    public boolean isPublica() { return publica; }
    public List<Jugador> getJugadores() { return jugadores; }
    public boolean isEnJuego() { return enJuego; }
    public void setEnJuego(boolean enJuego) { this.enJuego = enJuego; }
    
    // --- GETTERS DE REGLAS (Para que las KS las consulten) ---
    public boolean isReglaTresSeises() { return reglaTresSeises; }
    public boolean isReglaComer20() { return reglaComer20; }
    public boolean isReglaSalida5() { return reglaSalida5; }
}