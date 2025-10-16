package com.equipo5.parchis.controlador;

import com.equipo5.parchis.ModeloJuego.Juego;
import com.equipo5.parchis.ModeloJuego.Jugador;

public class ControlJuego {
    private final Juego juego;

    public ControlJuego(Juego juego) {
        this.juego = juego;
    }

    public void agregarJugador(String nombre, String color) {
        juego.agregarJugador(nombre, color);
    }

    public void iniciarPartida() {
        juego.iniciar();
    }

    public String lanzarDadoYObtenerMensaje() {
        int valor = juego.lanzarDado();
        Jugador actual = juego.getJugadorEnTurno();
        return "Jugador " + actual.getNombre() + " lanza: " + valor;
    }

    public void siguienteTurno() {
        juego.siguienteTurno();
    }

    public int getNumeroJugadores() {
        return juego.getJugadores().size();
    }

    public boolean puedeIniciar() {
        return getNumeroJugadores() >= 2;
    }

    public Juego getJuego() {
        return juego;
    }
}
