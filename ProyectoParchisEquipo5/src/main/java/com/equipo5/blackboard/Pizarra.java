package com.equipo5.blackboard;

import com.equipo5.model.Jugador;
import com.equipo5.model.Sala;
import com.equipo5.model.Tablero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

public class Pizarra extends Observable {
    
    private List<Jugador> jugadores;
    private Tablero tablero;
    private boolean juegoIniciado;
    private String ultimoMensaje;
    private int turnoActualIndex;
    private Sala infoSala; 

    public Pizarra() {
        this(4, true); 
    }

    // Constructor que recibe la configuración real
    public Pizarra(int maxJugadores, boolean publica) {
        this.jugadores = Collections.synchronizedList(new ArrayList<>());
        this.tablero = new Tablero();
        this.juegoIniciado = false;
        this.turnoActualIndex = 0;
        this.ultimoMensaje = "Pizarra Inicializada";
        
        // Crear la sala con la configuracion del usuario
        this.infoSala = new Sala(publica, maxJugadores);
    }

    public synchronized boolean registrarJugador(Jugador j) {
        if (infoSala.hayEspacio() && !juegoIniciado) {
            jugadores.add(j);
            infoSala.agregarJugador(j);
            
            // Asignar colores en orden de llegada
            String[] cols = {"AMARILLO", "AZUL", "ROJO", "VERDE"};
            if (jugadores.size() <= 4) {
                j.setColor(cols[jugadores.size()-1]);
            }
            
            setUltimoMensaje("Jugador conectado: " + j.getNombre());
            notificarCambio("NUEVO_JUGADOR", j);
            return true;
        }
        return false;
    }
    
    public void setJuegoIniciado(boolean iniciado) {
        this.juegoIniciado = iniciado;
        this.infoSala.setEnJuego(iniciado);
        
        if (iniciado) {
            this.tablero.inicializarFichas(this.jugadores);
            setUltimoMensaje("¡La partida ha comenzado!");
        }
        notificarCambio("ESTADO_JUEGO", iniciado ? "INICIADO" : "ESPERA");
    }

    public void setUltimoMensaje(String msg) {
        this.ultimoMensaje = msg;
        notificarCambio("LOG", msg); 
    }

    public void notificarCambio(String tipo, Object dato) {
        setChanged();
        notifyObservers(new String[]{tipo, dato.toString()});
    }

    public List<Jugador> getJugadores() { return new ArrayList<>(jugadores); }
    public Tablero getTablero() { return tablero; }
    public boolean isJuegoIniciado() { return juegoIniciado; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public int getTurnoActualIndex() { return turnoActualIndex; }
    public void setTurnoActualIndex(int turnoActualIndex) { this.turnoActualIndex = turnoActualIndex; }
    public Sala getInfoSala() { return infoSala; }
}