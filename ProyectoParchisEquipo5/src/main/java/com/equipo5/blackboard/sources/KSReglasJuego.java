package com.equipo5.blackboard.sources;

import com.equipo5.blackboard.Pizarra;
import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.net.Servidor;

public class KSReglasJuego {
    
    // Regla: Comer cuenta 20
    public void verificarColision(Pizarra pizarra, Servidor servidor, Ficha fichaMoviendo) {
        int[] seguros = {1, 5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68};
        boolean esSeguro = false;
        
        // 1. Verificar si está en seguro
        for(int s : seguros) if(s == fichaMoviendo.getPosicion()) esSeguro = true;
        
        if(!esSeguro) {
            for (Ficha otra : pizarra.getTablero().getTodasLasFichas()) {
                if (!otra.isEnBase() && 
                    otra.getPosicion() == fichaMoviendo.getPosicion() && 
                    !otra.getColor().equals(fichaMoviendo.getColor())) {
                    
                    // ¡COMIDA!
                    otra.regresarABase();
                    servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡COMIDA! " + fichaMoviendo.getColor() + " envía a casa a " + otra.getColor() + ".\" }");
                    
                    // REGLA: SI COMER 20 ESTÁ ACTIVA
                    if (pizarra.getInfoSala().isReglaComer20()) {
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Premio! Avanzas 20 casillas extra.\" }");
                        fichaMoviendo.avanzar(20);
                        // Verificar colisión recursivamente (opcional, simplificado aquí)
                    }
                    break; 
                }
            }
        }
    }

    // Regla: Tres Seises
    public void aplicarPenalizacionTresSeises(Pizarra pizarra, Servidor servidor) {
        // Solo si la regla está activa en la configuración de la Sala
        if (!pizarra.getInfoSala().isReglaTresSeises()) {
            return; // Si no está activa, no hacemos nada
        }

        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        int idCastigo = pizarra.getIdUltimaFichaMovida();
        
        boolean fichaCastigada = false;
        
        // Buscamos la ficha específica que se movió último
        if (idCastigo != -1) {
            for(Ficha f : pizarra.getTablero().getFichasDelColor(actual.getColor())) {
                if (f.getId() == idCastigo && !f.isEnBase() && !f.isEnMeta()) {
                    f.regresarABase();
                    fichaCastigada = true;
                    servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Castigo! La ficha " + f.getId() + " vuelve a casa.\" }");
                    break;
                }
            }
        }
        
        // Fallback: Si no encontramos la última (o ya estaba en casa), castigamos cualquiera que esté fuera
        if (!fichaCastigada) {
            for(Ficha f : pizarra.getTablero().getFichasDelColor(actual.getColor())) {
                if (!f.isEnBase() && !f.isEnMeta()) {
                    f.regresarABase();
                    servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Castigo! Una ficha vuelve a casa.\" }");
                    break;
                }
            }
        }
    }
    
    public int obtenerSalidaPorColor(String color) {
        switch(color) {
            case "AMARILLO": return 5;
            case "AZUL": return 22;
            case "ROJO": return 39;
            case "VERDE": return 56;
            default: return 1;
        }
    }
    
    public boolean verificarVictoria(Pizarra pizarra, String colorJugador) {
        // Obtenemos todas las fichas de ese jugador
        for (Ficha f : pizarra.getTablero().getFichasDelColor(colorJugador)) {
            // Si al menos UNA no está en meta, no ha ganado todavía
            if (!f.isEnMeta()) {
                return false;
            }
        }
        // Si el bucle termina, significa que TODAS están en meta
        return true;
    }
}