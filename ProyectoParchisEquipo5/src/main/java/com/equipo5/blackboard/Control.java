package com.equipo5.blackboard;

import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.net.Servidor;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ThreadLocalRandom; 

public class Control implements Observer {
    
    private Pizarra pizarra;
    private Servidor servidor;
    
    private int ultimoValorDado = 0; 
    private boolean dadoLanzadoEnEsteTurno = false;
    private int contadorSeisesTurno = 0;

    public Control(Pizarra pizarra, Servidor servidor) {
        this.pizarra = pizarra;
        this.servidor = servidor;
        this.pizarra.addObserver(this);
    }
    
    public void iniciarCiclo() {
        System.out.println("Control: Esperando inicio...");
    }
    
    public void iniciarPrimerTurno() {
        if (!pizarra.getJugadores().isEmpty()) {
            pizarra.setTurnoActualIndex(0);
            enviarEstadoTablero();
            notificarTurno(0);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof String[])) return;
        String[] evento = (String[]) arg;
        String tipo = evento[0];
        String dato = evento[1];
        
        switch (tipo) {
            case "ESTADO_JUEGO": break;
            case "SOLICITUD_DADO": handleLanzarDado(dato); break;
            case "SOLICITUD_MOVIMIENTO": handleMoverFicha(dato); break;
        }
    }

    private void handleLanzarDado(String nombreJugador) {
        if (!esTurnoDe(nombreJugador)) return;
        
        if (dadoLanzadoEnEsteTurno) {
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Ya lanzaste! Debes mover ficha.\" }");
            return;
        }

        int valor = ThreadLocalRandom.current().nextInt(1, 7); 
        this.ultimoValorDado = valor;
        this.dadoLanzadoEnEsteTurno = true;
        
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        servidor.broadcast("{ \"type\": \"DICE_RESULT\", \"value\": " + valor + ", \"color\": \"" + actual.getColor() + "\" }");
        
        if (valor == 6) {
            contadorSeisesTurno++;
            if (contadorSeisesTurno == 3) {
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Tres 6 seguidos! Pierdes turno y ficha a casa.\" }");
                aplicarPenalizacionTresSeises();
                cambiarTurno();
                return;
            }
        } else {
            contadorSeisesTurno = 0;
        }
        
        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Resultado: " + valor + ".\" }");

        boolean puedeMover = pizarra.getTablero().tieneMovimientosPosibles(actual.getColor(), valor);

        if (!puedeMover) {
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"No tienes movimientos posibles con un " + valor + ".\" }");
            try { Thread.sleep(1500); } catch (Exception e) {}

            if (valor == 6) {
                dadoLanzadoEnEsteTurno = false;
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Pero sacaste 6! Tiras de nuevo.\" }");
            } else {
                cambiarTurno();
            }
        }
    }

    private void handleMoverFicha(String idFichaStr) {
        if (!dadoLanzadoEnEsteTurno) {
             servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Tira el dado primero!\" }");
             return;
        }

        try {
            int idFicha = Integer.parseInt(idFichaStr);
            Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
            Ficha ficha = null;
            
            for (Ficha f : pizarra.getTablero().getFichasDelColor(actual.getColor())) {
                if (f.getId() == idFicha) { ficha = f; break; }
            }
            
            if (ficha != null) {
                if (pizarra.getTablero().esMovimientoValido(ficha, ultimoValorDado)) {
                    
                    if (ficha.isEnBase()) {
                        ficha.setEnBase(false);
                        ficha.setPosicion(obtenerSalidaPorColor(actual.getColor()));
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Ficha sale a la casilla de salida!\" }");
                    } else {
                        ficha.avanzar(ultimoValorDado);
                        verificarColision(ficha);
                    }

                    enviarEstadoTablero();
                    
                    if (ultimoValorDado == 6) {
                        dadoLanzadoEnEsteTurno = false; 
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡6! Repites turno.\" }");
                    } else {
                        cambiarTurno();
                    }
                    
                } else {
                    if (ficha.isEnBase()) {
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"❌ Necesitas un 5 para sacar esta ficha.\" }");
                    } else {
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"❌ Movimiento no válido.\" }");
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void verificarColision(Ficha fichaMoviendo) {
        int[] seguros = {1, 5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68};
        boolean esSeguro = false;
        for(int s : seguros) if(s == fichaMoviendo.getPosicion()) esSeguro = true;
        
        if(!esSeguro) {
            for (Ficha otra : pizarra.getTablero().getTodasLasFichas()) {
                if (!otra.isEnBase() && 
                    otra.getPosicion() == fichaMoviendo.getPosicion() && 
                    !otra.getColor().equals(fichaMoviendo.getColor())) {
                    
                    otra.regresarABase();
                    servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡COMIDA! " + fichaMoviendo.getColor() + " envía a casa a " + otra.getColor() + ".\" }");
                    break; 
                }
            }
        }
    }
    
    private int obtenerSalidaPorColor(String color) {
        switch(color) {
            case "AMARILLO": return 5;
            case "AZUL": return 22;
            case "ROJO": return 39;
            case "VERDE": return 56;
            default: return 1;
        }
    }

    private void aplicarPenalizacionTresSeises() {
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        for(Ficha f : pizarra.getTablero().getFichasDelColor(actual.getColor())) {
             if (!f.isEnBase() && !f.isEnMeta()) {
                 f.regresarABase();
                 break; 
             }
        }
        enviarEstadoTablero();
    }

    private boolean esTurnoDe(String nombre) {
        if (!pizarra.isJuegoIniciado()) return false;
        if (pizarra.getJugadores().isEmpty()) return false;
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        return actual.getNombre().equals(nombre);
    }

    private void cambiarTurno() {
        dadoLanzadoEnEsteTurno = false;
        contadorSeisesTurno = 0;
        int nextIndex = (pizarra.getTurnoActualIndex() + 1) % pizarra.getJugadores().size();
        pizarra.setTurnoActualIndex(nextIndex);
        notificarTurno(nextIndex);
    }
    
    private void notificarTurno(int index) {
        String nombre = pizarra.getJugadores().get(index).getNombre();
        servidor.broadcast("{ \"type\": \"TURN\", \"playerIndex\": " + index + " }");
        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Turno de " + nombre + "\" }");
    }

    private void enviarEstadoTablero() {
        StringBuilder sb = new StringBuilder();
        for (Ficha f : pizarra.getTablero().getTodasLasFichas()) {
            sb.append(f.getColor()).append(":")
              .append(f.getId()).append(":")
              .append(f.getPosicion()).append(":")
              .append(f.isEnBase() ? "1" : "0")
              .append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        servidor.broadcast("{ \"type\": \"UPDATE\", \"board\": \"" + sb.toString() + "\" }");
    }
}