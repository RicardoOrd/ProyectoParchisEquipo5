package com.equipo5.blackboard;

import com.equipo5.model.Jugador;
import com.equipo5.model.Tablero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * La Pizarra (Blackboard) es la memoria central del servidor.
 * Contiene todo el estado del juego y notifica cambios a los observadores (Control).
 */
public class Pizarra extends Observable {
    
    // --- ESTADO DEL JUEGO (Datos) ---
    private List<Jugador> jugadores;
    private Tablero tablero;          // El tablero con las fichas
    private boolean juegoIniciado;
    private String ultimoMensaje;     // Para logs o chat simple
    private int turnoActualIndex;     // Índice del jugador que tiene el turno (0 a 3)

    public Pizarra() {
        // Usamos lista sincronizada para seguridad en servidor multihilo
        this.jugadores = Collections.synchronizedList(new ArrayList<>());
        this.tablero = new Tablero(); // Inicializamos el tablero vacío
        this.juegoIniciado = false;
        this.turnoActualIndex = 0;
        this.ultimoMensaje = "Pizarra Inicializada";
    }

    // --- MÉTODOS DE ESCRITURA (Modifican el estado) ---
    
    /**
     * Intenta registrar un nuevo jugador en la partida.
     * @param j El jugador a agregar
     * @return true si se agregó, false si la sala está llena o el juego ya empezó
     */
    public synchronized boolean registrarJugador(Jugador j) {
        if (jugadores.size() < 4 && !juegoIniciado) {
            jugadores.add(j);
            setUltimoMensaje("Jugador conectado: " + j.getNombre());
            notificarCambio("NUEVO_JUGADOR", j);
            return true;
        }
        return false;
    }
    
    /**
     * Cambia el estado del juego. Si inicia, prepara el tablero.
     */
    public void setJuegoIniciado(boolean iniciado) {
        this.juegoIniciado = iniciado;
        if (iniciado) {
            // Paso crucial: Crear las fichas para los jugadores conectados
            this.tablero.inicializarFichas(this.jugadores);
            setUltimoMensaje("¡La partida ha comenzado!");
        }
        notificarCambio("ESTADO_JUEGO", iniciado ? "INICIADO" : "ESPERA");
    }

    /**
     * Actualiza un mensaje global (ej. chat o logs del sistema)
     */
    public void setUltimoMensaje(String msg) {
        this.ultimoMensaje = msg;
        // Notificamos solo como evento de texto
        notificarCambio("LOG", msg); 
    }

    /**
     * Método auxiliar para avisar a los observadores (Control y Clientes)
     * Envía un array de String: [TIPO_EVENTO, DATO]
     */
    public void notificarCambio(String tipo, Object dato) {
        setChanged(); // Marca que hubo un cambio importante
        notifyObservers(new String[]{tipo, dato.toString()}); // Avisa a Control.java
    }

    // --- MÉTODOS DE LECTURA (Getters) ---

    public List<Jugador> getJugadores() {
        return new ArrayList<>(jugadores); // Retorna copia para proteger la lista original
    }

    public Tablero getTablero() {
        return tablero;
    }

    public boolean isJuegoIniciado() {
        return juegoIniciado;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }
}