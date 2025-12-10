package com.equipo5.blackboard.sources;

import com.equipo5.blackboard.Pizarra;
import com.equipo5.model.Ficha;
import com.equipo5.net.Servidor;

public class KSNotificador {
    
    public static void cambiarTurno(Pizarra pizarra, Servidor servidor) {
        // Reseteamos variables de turno en la Pizarra
        pizarra.setDadoLanzadoEnEsteTurno(false);
        pizarra.setContadorSeisesTurno(0);
        
        // Calculamos siguiente jugador
        int nextIndex = (pizarra.getTurnoActualIndex() + 1) % pizarra.getJugadores().size();
        pizarra.setTurnoActualIndex(nextIndex);
        
        // Notificamos
        String nombre = pizarra.getJugadores().get(nextIndex).getNombre();
        servidor.broadcast("{ \"type\": \"TURN\", \"playerIndex\": " + nextIndex + " }");
        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Turno de " + nombre + "\" }");
    }

    public static void enviarEstadoTablero(Pizarra pizarra, Servidor servidor) {
        StringBuilder sb = new StringBuilder();
        // Construimos el string del tablero: COLOR:ID:POS:ENBASE,COLOR:ID...
        for (Ficha f : pizarra.getTablero().getTodasLasFichas()) {
            sb.append(f.getColor()).append(":")
              .append(f.getId()).append(":")
              .append(f.getPosicion()).append(":")
              .append(f.isEnBase() ? "1" : "0")
              .append(",");
        }
        // Eliminamos la última coma
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        
        servidor.broadcast("{ \"type\": \"UPDATE\", \"board\": \"" + sb.toString() + "\" }");
    }
}