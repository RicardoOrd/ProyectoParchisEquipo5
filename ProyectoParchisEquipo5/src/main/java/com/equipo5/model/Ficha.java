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

        // --- 1. LÓGICA DENTRO DEL PASILLO (IDs > 100) ---
        if (posicion >= 100) {
            // El último dígito 7 indica que está ante la meta (107, 207, etc.)
            if (posicion % 100 == 7) {
                enMeta = true; 
            } else {
                posicion++; // Avanza dentro del pasillo
            }
            return;
        }

        // --- 2. ENTRADA A LOS PASILLOS (CORREGIDO) ---
        // El punto de entrada es la ÚLTIMA casilla del brazo de su color, justo antes del centro.
        
        // ROJO: Brazo Izq. Entra al final de la fila de arriba (34) -> Pasillo 301
        if (color.equals("ROJO") && posicion == 34) {
            posicion = 301; return;
        }
        
        // AMARILLO: Brazo Der. Entra al final de la fila de arriba (68) -> Pasillo 101
        // Nota: En tu dibujo el amarillo es el derecho (ids 61-68)
        if (color.equals("AMARILLO") && posicion == 68) {
            posicion = 101; return;
        }
        
        // AZUL: Brazo Arriba. Entra al final de la columna izq (17) -> Pasillo 201
        // (ids 10-17 bajan hacia el centro)
        if (color.equals("AZUL") && posicion == 17) {
            posicion = 201; return;
        }
        
        // VERDE: Brazo Abajo. Entra al final de la columna der (51) -> Pasillo 401
        // (ids 44-51 suben hacia el centro)
        if (color.equals("VERDE") && posicion == 51) {
            posicion = 401; return;
        }

        // --- 3. MOVIMIENTO NORMAL (CÍRCULO) ---
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