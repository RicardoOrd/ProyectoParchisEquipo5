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
    
    public boolean tieneMovimientosPosibles(String color, int valorDado) {
        // Optimización: Si el valor es 5, siempre se puede salir de casa (a menos que la salida esté bloqueada, que no implementamos aún)
        // Si no es 5, solo se pueden mover fichas que NO estén en casa y NO estén en meta.
        
        List<Ficha> fichas = getFichasDelColor(color);
        for (Ficha f : fichas) {
            if (esMovimientoValido(f, valorDado)) {
                return true; 
            }
        }
        return false;
    }

    public boolean esMovimientoValido(Ficha f, int valorDado) {
    if (f.isEnMeta()) return false; 

    if (f.isEnBase()) {
        // CAMBIO: Permitir salir con cualquier número (o al menos con >= 1)
        // Antes era: return (valorDado == 5);
        return true; 
    } else {
        return true;
    }
}
}