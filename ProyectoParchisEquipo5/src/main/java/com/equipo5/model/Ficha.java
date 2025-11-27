package com.equipo5.model;

public class Ficha {
    private String color;
    private int id; // Identificador (1, 2, 3 o 4)
    private int posicion; // 0 = Base, 1-68 = Tablero
    private boolean enBase;
    private boolean enMeta;

    public Ficha(String color, int id) {
        this.color = color;
        this.id = id;
        this.posicion = 0; // Inicialmente en base
        this.enBase = true;
        this.enMeta = false;
    }

    // --- Métodos de Lógica ---
    
    public void avanzar(int pasos) {
        // Al avanzar, asumimos que ya no está en base
        this.enBase = false; 
        this.posicion += pasos;
        
        // Lógica circular simple (Tablero de 68 casillas)
        // Si pasa del 68, vuelve a empezar (1, 2, 3...)
        if (this.posicion > 68) {
            this.posicion -= 68;
        }
    }

    public void regresarABase() {
        this.posicion = 0;
        this.enBase = true;
    }

    // --- Getters y Setters ---

    public String getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public boolean isEnBase() {
        return enBase;
    }

    // IMPORTANTE: Este setter lo necesita el Control para sacarla de casa manualmente
    public void setEnBase(boolean enBase) {
        this.enBase = enBase;
    }
    
    public boolean isEnMeta() {
        return enMeta;
    }
    
    public void setEnMeta(boolean enMeta) {
        this.enMeta = enMeta;
    }

    @Override
    public String toString() {
        return "Ficha " + color + "-" + id + " en casillero " + posicion;
    }
}