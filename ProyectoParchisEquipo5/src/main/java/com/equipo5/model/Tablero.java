package com.equipo5.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tablero {
    // Guardamos las fichas agrupadas por color para acceso rápido
    private Map<String, List<Ficha>> fichasPorJugador;

    public Tablero() {
        this.fichasPorJugador = new HashMap<>();
    }

    // Inicializa el tablero creando 4 fichas para cada jugador registrado
    public void inicializarFichas(List<Jugador> jugadores) {
        fichasPorJugador.clear();
        
        // Colores predefinidos en orden de turno habitual
        String[] coloresDisponibles = {"AMARILLO", "AZUL", "ROJO", "VERDE"};
        
        int i = 0;
        for (Jugador jugador : jugadores) {
            // Asignar color al jugador si no tiene
            if (jugador.getColor() == null && i < coloresDisponibles.length) {
                jugador.setColor(coloresDisponibles[i]);
            }
            
            String color = jugador.getColor();
            List<Ficha> misFichas = new ArrayList<>();
            
            // Crear las 4 fichas
            for (int f = 1; f <= 4; f++) {
                misFichas.add(new Ficha(color, f));
            }
            
            fichasPorJugador.put(color, misFichas);
            i++;
        }
        System.out.println("Tablero inicializado con " + jugadores.size() + " equipos.");
    }

    // Obtener todas las fichas de un color específico
    public List<Ficha> getFichasDelColor(String color) {
        return fichasPorJugador.getOrDefault(color, new ArrayList<>());
    }
    
    // Obtener todas las fichas del tablero (útil para pintar la UI o verificar choques)
    public List<Ficha> getTodasLasFichas() {
        List<Ficha> todas = new ArrayList<>();
        for (List<Ficha> lista : fichasPorJugador.values()) {
            todas.addAll(lista);
        }
        return todas;
    }
    
    // Verifica si hay alguna ficha en una posición específica (para lógica de comer/bloqueo)
    public Ficha getFichaEnPosicion(int casillero) {
        if (casillero <= 0) return null; // Ignorar bases
        
        for (List<Ficha> lista : fichasPorJugador.values()) {
            for (Ficha f : lista) {
                if (f.getPosicion() == casillero && !f.isEnBase() && !f.isEnMeta()) {
                    return f;
                }
            }
        }
        return null;
    }
}