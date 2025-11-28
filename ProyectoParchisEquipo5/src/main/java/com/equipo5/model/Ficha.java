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
        this.enBase = false; 
        this.posicion += pasos;
        
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