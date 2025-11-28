package com.equipo5.model;

import java.util.UUID;

public class Jugador {
    private String playerId;
    private String displayName;
    private String color;
    private String avatar;
    private boolean listo; 

    public Jugador() {
        this.playerId = UUID.randomUUID().toString();
        this.listo = false;
    }

    public Jugador(String nombre) {
        this.playerId = UUID.randomUUID().toString();
        this.displayName = nombre;
        this.avatar = "default";
        this.listo = false;
    }

    public String getPlayerId() { return playerId; }
    public String getNombre() { return displayName; }
    public void setNombre(String nombre) { this.displayName = nombre; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public boolean isListo() { return listo; }
    public void setListo(boolean listo) { this.listo = listo; }

    @Override
    public String toString() {
        return displayName + (listo ? " [LISTO]" : " [ESPERANDO]");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Jugador jugador = (Jugador) obj;
        return playerId != null && playerId.equals(jugador.playerId);
    }
}