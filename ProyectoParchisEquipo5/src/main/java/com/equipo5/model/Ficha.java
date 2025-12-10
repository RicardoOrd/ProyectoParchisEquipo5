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
        for (int i = 0; i < pasos; i++) {
            moverUnPaso();
        }
    }

private void moverUnPaso() {
    if (enMeta) return;

    // --- 1. YA DENTRO DEL PASILLO ---
    if (posicion >= 100) {
        if (posicion % 100 == 7) {
            enMeta = true; 
        } else {
            posicion++; 
        }
        return;
    }

    // --- 2. ENTRADA A PASILLOS ---
    // Verificamos si estamos EN la casilla de entrada y vamos a avanzar
    
    // ROJO (Entrada en 34)
    if (color.equals("ROJO") && posicion == 34) {
        posicion = 301; return;
    }
    
    // AMARILLO (Entrada en 68)
    if (color.equals("AMARILLO") && posicion == 68) {
        posicion = 101; return;
    }
    
    // AZUL (Entrada en 17)
    if (color.equals("AZUL") && posicion == 17) {
        posicion = 201; return;
    }
    
    // VERDE (Entrada en 51)
    if (color.equals("VERDE") && posicion == 51) {
        posicion = 401; return;
    }

    // --- 3. MOVIMIENTO NORMAL ---
    posicion++;
    if (posicion > 68) {
        posicion = 1;
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