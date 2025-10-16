package com.equipo5.parchis.ModeloJuego;

import java.util.*;

public class Juego {
    private final List<Jugador> jugadores = new ArrayList<>();
    private int indiceTurno = 0;
    private Estado estado = Estado.LOBBY;
    private final Dado dado = new Dado();

    public enum Estado { LOBBY, EN_CURSO, FINALIZADA }

    public void agregarJugador(String nombre, String color) {
        if (estado != Estado.LOBBY) throw new IllegalStateException("La partida ya inicio");
        if (jugadores.size() >= 4) throw new IllegalStateException("Maximo 4 jugadores");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        jugadores.add(new Jugador(nombre, color == null || color.isBlank() ? "sin-color" : color));
    }

    public List<Jugador> getJugadores() { return Collections.unmodifiableList(jugadores); }

    public void iniciar() {
        if (jugadores.size() < 2) throw new IllegalStateException("Se requieren al menos 2 jugadores");
        this.estado = Estado.EN_CURSO;
        Collections.shuffle(jugadores, new Random());
        this.indiceTurno = 0;
    }

    public Jugador getJugadorEnTurno() {
        if (estado != Estado.EN_CURSO) return null;
        return jugadores.get(indiceTurno);
    }

    public int lanzarDado() {
        if (estado != Estado.EN_CURSO) throw new IllegalStateException("La partida no esta en curso");
        Jugador j = getJugadorEnTurno();
        int resultado = dado.lanzar();
        j.registrarTiro(resultado);
        return resultado;
    }

    public void siguienteTurno() {
        if (estado != Estado.EN_CURSO) return;
        indiceTurno = (indiceTurno + 1) % jugadores.size();
    }

    public void finalizar() {
        this.estado = Estado.FINALIZADA;
    }

    public Estado getEstado() { return estado; }
}
