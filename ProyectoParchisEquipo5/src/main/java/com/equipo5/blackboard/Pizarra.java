package com.equipo5.blackboard;

import com.equipo5.model.Jugador;
import com.equipo5.model.Sala;
import com.equipo5.model.Tablero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

public class Pizarra extends Observable {
    
    // --- ESTADO DEL MODELO (Datos estructurales) ---
    private List<Jugador> jugadores;
    private Tablero tablero;
    private Sala infoSala; 

    // --- ESTADO DEL JUEGO (Datos de flujo) ---
    private boolean juegoIniciado;
    private int turnoActualIndex;
    private String ultimoMensaje;

    // --- VARIABLES DE ESTADO DE TURNO (Patrón Blackboard) ---
    // Estas variables permiten a las KS comunicarse sin acoplarse
    private int ultimoValorDado = 0; 
    private boolean dadoLanzadoEnEsteTurno = false;
    private int contadorSeisesTurno = 0;
    
    // Variable crítica para la regla de los "Tres Seises":
    // Guarda el ID de la ficha que se movió en el último paso para saber cuál castigar.
    private int idUltimaFichaMovida = -1; // -1 indica ninguna

    // Constructor completo con configuración de reglas
    public Pizarra(int maxJugadores, boolean publica, boolean r3Seises, boolean rComer20, boolean rSalida5) {
        this.jugadores = Collections.synchronizedList(new ArrayList<>());
        this.tablero = new Tablero();
        
        // Estado inicial del flujo
        this.juegoIniciado = false;
        this.turnoActualIndex = 0;
        this.ultimoMensaje = "Pizarra Inicializada";
        
        // Inicialización de variables de turno
        this.ultimoValorDado = 0;
        this.dadoLanzadoEnEsteTurno = false;
        this.contadorSeisesTurno = 0;
        this.idUltimaFichaMovida = -1;
        
        // Creamos la Sala pasando las reglas configuradas por el usuario
        this.infoSala = new Sala(publica, maxJugadores, r3Seises, rComer20, rSalida5);
    }
    
    // Constructor por defecto (útil para compatibilidad o pruebas rápidas)
    public Pizarra() {
        // Por defecto: 4 jugadores, pública, todas las reglas activas excepto salida obligatoria con 5
        this(4, true, true, true, false); 
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

    // Método central de notificación para el patrón Observer
    public void notificarCambio(String tipo, Object dato) {
        setChanged();
        // Enviamos un array [TIPO_EVENTO, DATO_STRING]
        notifyObservers(new String[]{tipo, dato.toString()});
    }
    
    // Helper semántico para las Fuentes de Conocimiento (KS)
    public void notificarEvento(String tipo, String dato) {
        notificarCambio(tipo, dato);
    }

    // --- GETTERS Y SETTERS ---

    public List<Jugador> getJugadores() { return new ArrayList<>(jugadores); }
    public Tablero getTablero() { return tablero; }
    public boolean isJuegoIniciado() { return juegoIniciado; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public Sala getInfoSala() { return infoSala; }
    
    public int getTurnoActualIndex() { return turnoActualIndex; }
    public void setTurnoActualIndex(int turnoActualIndex) { this.turnoActualIndex = turnoActualIndex; }

    // --- GETTERS Y SETTERS DE ESTADO (BLACKBOARD) ---

    public int getUltimoValorDado() { return ultimoValorDado; }
    public void setUltimoValorDado(int valor) { this.ultimoValorDado = valor; }

    public boolean isDadoLanzadoEnEsteTurno() { return dadoLanzadoEnEsteTurno; }
    public void setDadoLanzadoEnEsteTurno(boolean lanzado) { this.dadoLanzadoEnEsteTurno = lanzado; }

    public int getContadorSeisesTurno() { return contadorSeisesTurno; }
    public void setContadorSeisesTurno(int contador) { this.contadorSeisesTurno = contador; }
    
    public int getIdUltimaFichaMovida() { return idUltimaFichaMovida; }
    public void setIdUltimaFichaMovida(int id) { this.idUltimaFichaMovida = id; }
}