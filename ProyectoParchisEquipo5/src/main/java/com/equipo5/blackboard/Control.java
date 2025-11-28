package com.equipo5.blackboard;

import com.equipo5.model.Dado;
import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.net.Servidor;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Control implements Observer {
    
    private Pizarra pizarra;
    private Servidor servidor;
    private Dado dado;
    
    private int ultimoValorDado = 0; 
    private boolean dadoLanzadoEnEsteTurno = false;
    private int contadorSeisesTurno = 0;

    // Casillas seguras (Seguros) donde no se puede comer
    private final int[] SEGUROS = {1, 5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68};

    public Control(Pizarra pizarra, Servidor servidor) {
        this.pizarra = pizarra;
        this.servidor = servidor;
        this.dado = new Dado();
        this.pizarra.addObserver(this);
    }
    
    public void iniciarCiclo() {
        System.out.println("Control: Sistema listo esperando inicio de partida.");
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
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Ya lanzaste. Debes mover ficha.\" }");
            return;
        }

        int valor = dado.tirar();
        this.ultimoValorDado = valor;
        this.dadoLanzadoEnEsteTurno = true;
        
        servidor.broadcast("{ \"type\": \"DICE_RESULT\", \"value\": " + valor + " }");
        
        if (valor == 6) {
            contadorSeisesTurno++;
            if (contadorSeisesTurno == 3) {
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡TRES SEISES! Pierdes turno y ficha vuelve a casa.\" }");
                aplicarPenalizacionTresSeises();
                cambiarTurno();
                return;
            }
        } else {
            contadorSeisesTurno = 0;
        }
        
        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Resultado: " + valor + ".\" }");
    }

    private void handleMoverFicha(String idFichaStr) {
        if (!dadoLanzadoEnEsteTurno) {
             servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Debes lanzar el dado primero!\" }");
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
                boolean movio = false;
                
                if (ficha.isEnBase()) {
                    if (ultimoValorDado == 5) {
                        ficha.setEnBase(false);
                        // Calcular casilla de salida según color
                        ficha.setPosicion(obtenerSalidaPorColor(actual.getColor()));
                        movio = true;
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Ficha sale de casa!\" }");
                    } else {
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Necesitas un 5 para salir.\" }");
                    }
                } else {
                    ficha.avanzar(ultimoValorDado);
                    movio = true;
                }

                if (movio) {
                    // --- NUEVO: VERIFICAR SI COMEMOS ALGUIEN ---
                    verificarColision(ficha);
                    
                    enviarEstadoTablero();
                    
                    if (ultimoValorDado == 6) {
                        dadoLanzadoEnEsteTurno = false;
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Sacaste 6! Tiras de nuevo.\" }");
                    } else {
                        cambiarTurno();
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // --- LÓGICA DE COMER FICHAS ---
    private void verificarColision(Ficha fichaMoviendo) {
        if (esSeguro(fichaMoviendo.getPosicion())) return; // No se come en seguros

        for (Ficha otra : pizarra.getTablero().getTodasLasFichas()) {
            // Si está en la misma posición, no es la misma ficha y es de otro color
            if (!otra.isEnBase() && 
                otra.getPosicion() == fichaMoviendo.getPosicion() && 
                !otra.getColor().equals(fichaMoviendo.getColor())) {
                
                // COMER: La otra ficha vuelve a base
                otra.regresarABase();
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡" + fichaMoviendo.getColor() + " se comió a " + otra.getColor() + "!\" }");
                
                // Premio por comer: contar 20 (Simplificado: dar tiro extra o mover 20)
                // Por ahora solo notificamos
                break; // Solo comemos una
            }
        }
    }
    
    private boolean esSeguro(int pos) {
        for (int s : SEGUROS) if (s == pos) return true;
        return false;
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
        List<Ficha> misFichas = pizarra.getTablero().getFichasDelColor(actual.getColor());
        
        for(Ficha f : misFichas) {
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