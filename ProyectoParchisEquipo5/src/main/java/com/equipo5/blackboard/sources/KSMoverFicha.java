package com.equipo5.blackboard.sources;

import com.equipo5.blackboard.Pizarra;
import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.net.Servidor;

public class KSMoverFicha implements FuenteConocimineto {

    private KSReglasJuego reglas = new KSReglasJuego();

    @Override
    public void ejecutar(Pizarra pizarra, Servidor servidor, String idFichaStr) {
        // 1. Validar que ya haya lanzado dado
        if (!pizarra.isDadoLanzadoEnEsteTurno()) {
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Tira el dado primero!\" }");
            return;
        }

        try {
            int idFicha = Integer.parseInt(idFichaStr);
            Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
            Ficha ficha = null;

            // Buscar la ficha solicitada
            for (Ficha f : pizarra.getTablero().getFichasDelColor(actual.getColor())) {
                if (f.getId() == idFicha) {
                    ficha = f;
                    break;
                }
            }

            if (ficha != null) {
                int valorDado = pizarra.getUltimoValorDado();
                boolean regla5Activa = pizarra.getInfoSala().isReglaSalida5(); // Obtener regla

                // 2. Validar movimiento pasando la regla
                if (pizarra.getTablero().esMovimientoValido(ficha, valorDado, regla5Activa)) {

                    if (ficha.isEnBase()) {
                        // YA NO NECESITAS EL BLOQUE IF MANUAL AQUÍ
                        // Porque esMovimientoValido ya devolvió false si no era 5.
                        // Pero puedes dejar el mensaje de log específico si quieres, 
                        // aunque técnicamente nunca entrará aquí si no es válido.

                        // Salir de base
                        ficha.setEnBase(false);                   // Verificamos si la regla está activa en la configuración de la Sala.
                        // Si está activa Y el valor NO es 5, impedimos salir.
                        if (pizarra.getInfoSala().isReglaSalida5() && valorDado != 5) {
                            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"❌ Regla activa: Necesitas un 5 para salir de casa.\" }");
                            return;
                        }

                        // Salir de base
                        ficha.setEnBase(false);
                        ficha.setPosicion(reglas.obtenerSalidaPorColor(actual.getColor()));
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Ficha sale a la salida!\" }");
                    } else {
                        // Movimiento normal
                        ficha.avanzar(valorDado);
                        // Verificar colisiones (Comer)
                        reglas.verificarColision(pizarra, servidor, ficha);
                    }

                    // --- IMPORTANTE: Actualizar Pizarra para regla de 3 seises ---
                    pizarra.setIdUltimaFichaMovida(ficha.getId());
                    // -------------------------------------------------------------

                    // Actualizar clientes
                    KSNotificador.enviarEstadoTablero(pizarra, servidor);

                    // --- NUEVO: VERIFICAR VICTORIA ---
                    if (reglas.verificarVictoria(pizarra, actual.getColor())) {
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"🏆 ¡EL JUGADOR " + actual.getNombre() + " HA GANADO! 🏆\" }");
                        servidor.broadcast("{ \"type\": \"GAME_OVER\", \"winner\": \"" + actual.getNombre() + "\" }");
                        pizarra.setJuegoIniciado(false); // Detener el juego
                        return; // Salir para no cambiar turno
                    }
                    // ---------------------------------

                    // 3. Gestión de Turno (Solo si nadie ha ganado aún)
                    if (valorDado == 6) {
                        pizarra.setDadoLanzadoEnEsteTurno(false);
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Sacaste 6! Repites turno.\" }");
                    } else {
                        KSNotificador.cambiarTurno(pizarra, servidor);
                    }

                } else {
                    servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"❌ Movimiento no válido.\" }");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
