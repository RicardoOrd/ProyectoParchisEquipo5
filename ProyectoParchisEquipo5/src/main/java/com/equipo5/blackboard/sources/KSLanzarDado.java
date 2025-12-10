package com.equipo5.blackboard.sources;

import com.equipo5.blackboard.Pizarra;
import com.equipo5.model.Jugador;
import com.equipo5.net.Servidor;
import java.util.concurrent.ThreadLocalRandom;

public class KSLanzarDado implements FuenteConocimineto {

    private KSReglasJuego reglas = new KSReglasJuego(); 

    @Override
    public void ejecutar(Pizarra pizarra, Servidor servidor, String dato) {
        // dato es el nombre del jugador que solicitó lanzar
        String nombreJugador = dato;
        
        // 1. Validaciones
        if (!esTurnoDe(pizarra, nombreJugador)) return;
        
        if (pizarra.isDadoLanzadoEnEsteTurno()) {
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Ya lanzaste! Debes mover ficha.\" }");
            return;
        }

        // 2. Lógica del dado
        int valor = ThreadLocalRandom.current().nextInt(1, 7);
        pizarra.setUltimoValorDado(valor);
        pizarra.setDadoLanzadoEnEsteTurno(true);
        
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        
        // Notificar resultado visual (esto dispara la animación en el cliente)
        servidor.broadcast("{ \"type\": \"DICE_RESULT\", \"value\": " + valor + ", \"color\": \"" + actual.getColor() + "\" }");
        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Resultado: " + valor + ".\" }");

        // 3. Regla de los tres 6
        if (valor == 6) {
            int seises = pizarra.getContadorSeisesTurno() + 1;
            pizarra.setContadorSeisesTurno(seises);
            
            if (seises == 3) {
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Tres 6 seguidos! Pierdes turno y ficha a casa.\" }");
                
                // --- AQUÍ ESTABA EL ERROR: Agregamos 'servidor' ---
                reglas.aplicarPenalizacionTresSeises(pizarra, servidor);
                // --------------------------------------------------
                
                KSNotificador.enviarEstadoTablero(pizarra, servidor);
                KSNotificador.cambiarTurno(pizarra, servidor);
                return;
            }
        } else {
            pizarra.setContadorSeisesTurno(0);
        }

// 4. Verificar si puede mover
        // Obtenemos si la regla está activa desde la Pizarra
        boolean regla5Activa = pizarra.getInfoSala().isReglaSalida5();
        
        // Pasamos el booleano al método actualizado
        boolean puedeMover = pizarra.getTablero().tieneMovimientosPosibles(actual.getColor(), valor, regla5Activa);
        
        if (!puedeMover) {
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"No tienes movimientos con un " + valor + ".\" }");
            try { Thread.sleep(1500); } catch (Exception e) {} // Pausa para leer

            if (valor == 6) {
                // Si saca 6 pero no puede mover, tira de nuevo (según reglas estándar, o pasa turno según variante)
                // Asumimos regla amigable: tira de nuevo para intentar desbloquear o sacar otra
                pizarra.setDadoLanzadoEnEsteTurno(false);
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Sacaste 6! Tiras de nuevo.\" }");
            } else {
                KSNotificador.cambiarTurno(pizarra, servidor);
            }
        }
    }
    
    private boolean esTurnoDe(Pizarra pizarra, String nombre) {
        if (!pizarra.isJuegoIniciado()) return false;
        if (pizarra.getJugadores().isEmpty()) return false;
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        return actual.getNombre().equals(nombre);
    }
}