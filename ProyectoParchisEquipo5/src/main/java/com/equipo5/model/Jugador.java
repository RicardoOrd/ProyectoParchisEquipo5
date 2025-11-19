package com.equipo5.model;

public class Jugador {
    private String nombre;
    private String color; // "ROJO", "VERDE", etc.
    
    // Constructor vacío
    public Jugador() {
    }

    // Constructor con nombre
    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    
    @Override
    public String toString() {
        return nombre + " (" + (color != null ? color : "Sin color") + ")";
    }
}