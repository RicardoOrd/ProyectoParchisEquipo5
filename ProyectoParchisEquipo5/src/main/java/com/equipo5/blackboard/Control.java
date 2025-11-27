package com.equipo5.blackboard;

import com.equipo5.model.Dado;
import com.equipo5.model.Ficha;
import com.equipo5.model.Jugador;
import com.equipo5.net.Servidor;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * El Controlador (Brain) del patrón Blackboard.
 * Escucha cambios en la Pizarra y ejecuta la lógica del juego.
 */
public class Control implements Observer {
    
    private Pizarra pizarra;
    private Servidor servidor;
    private Dado dado; // Instancia del Dado según diagrama de clases
    
    private int ultimoValorDado = 0; 
    private boolean dadoLanzadoEnEsteTurno = false;
    private int contadorSeisesTurno = 0; // Para la regla de penalización

    public Control(Pizarra pizarra, Servidor servidor) {
        this.pizarra = pizarra;
        this.servidor = servidor;
        this.dado = new Dado(); // Inicializamos el dado
        this.pizarra.addObserver(this);
    }
    
    public void iniciarCiclo() {
        System.out.println("Control: Sistema listo esperando inicio de partida.");
    }
    
    // Método llamado por el Servidor cuando el Host da clic en "Iniciar Partida" en el Lobby
    public void iniciarPrimerTurno() {
        if (!pizarra.getJugadores().isEmpty()) {
            pizarra.setTurnoActualIndex(0);
            enviarEstadoTablero(); // Sincronizar fichas iniciales
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
            case "ESTADO_JUEGO":
                // Lógica manejada en iniciarPrimerTurno
                break;
            case "SOLICITUD_DADO": 
                handleLanzarDado(dato);
                break;
            case "SOLICITUD_MOVIMIENTO":
                handleMoverFicha(dato);
                break;
        }
    }

    private void handleLanzarDado(String nombreJugador) {
        // 1. Validar turno
        if (!esTurnoDe(nombreJugador)) return;
        
        // 2. Validar si ya lanzó
        if (dadoLanzadoEnEsteTurno) {
            servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Ya lanzaste. Debes mover ficha.\" }");
            return;
        }

        // 3. Usar la clase Dado para generar valor
        int valor = dado.tirar();
        this.ultimoValorDado = valor;
        this.dadoLanzadoEnEsteTurno = true;
        
        // Enviar resultado visual
        servidor.broadcast("{ \"type\": \"DICE_RESULT\", \"value\": " + valor + " }");
        
        // --- REGLA: Tres 6 seguidos (Caso Alterno 5 en UC-05) ---
        if (valor == 6) {
            contadorSeisesTurno++;
            if (contadorSeisesTurno == 3) {
                servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡TRES SEISES! Pierdes turno y ficha vuelve a casa.\" }");
                aplicarPenalizacionTresSeises();
                cambiarTurno(); // Pasa turno inmediatamente
                return;
            }
        } else {
            contadorSeisesTurno = 0; // Reset si no es 6
        }
        
        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Resultado: " + valor + ".\" }");
        
        // Validar si tiene movimientos posibles (Opcional: pasar turno si no puede mover)
        // Por simplicidad, dejamos que el jugador lo intente o el sistema valide al mover.
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
            
            // Buscar la ficha que pertenece al jugador actual
            for (Ficha f : pizarra.getTablero().getFichasDelColor(actual.getColor())) {
                if (f.getId() == idFicha) { ficha = f; break; }
            }
            
            if (ficha != null) {
                boolean movio = false;
                
                // --- REGLAS DE MOVIMIENTO ---
                if (ficha.isEnBase()) {
                    // Regla: Solo sale con 5
                    if (ultimoValorDado == 5) {
                        ficha.setEnBase(false);
                        ficha.setPosicion(1); // Sale a la casilla 1 (simplificado)
                        movio = true;
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Ficha sale de casa!\" }");
                    } else {
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"Necesitas un 5 para salir.\" }");
                    }
                } else {
                    // Movimiento normal
                    ficha.avanzar(ultimoValorDado);
                    movio = true;
                }

                if (movio) {
                    enviarEstadoTablero(); // Actualizar clientes
                    
                    // Regla: Si sacó 6, repite turno (si no fue el tercero)
                    if (ultimoValorDado == 6) {
                        dadoLanzadoEnEsteTurno = false; // Habilitar dado de nuevo
                        servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"¡Sacaste 6! Tiras de nuevo.\" }");
                    } else {
                        cambiarTurno();
                    }
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
    
    // Implementación de la penalización del documento: Regresar ficha más avanzada
    private void aplicarPenalizacionTresSeises() {
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        List<Ficha> misFichas = pizarra.getTablero().getFichasDelColor(actual.getColor());
        
        Ficha candidata = null;
        // Buscar la primera ficha que esté fuera de base para regresarla
        for(Ficha f : misFichas) {
             if (!f.isEnBase() && !f.isEnMeta()) {
                 candidata = f;
                 break; 
             }
        }
        
        if (candidata != null) {
            candidata.regresarABase();
            enviarEstadoTablero();
        }
    }

    private boolean esTurnoDe(String nombre) {
        if (!pizarra.isJuegoIniciado()) return false;
        if (pizarra.getJugadores().isEmpty()) return false;
        
        Jugador actual = pizarra.getJugadores().get(pizarra.getTurnoActualIndex());
        return actual.getNombre().equals(nombre);
    }

    private void cambiarTurno() {
        dadoLanzadoEnEsteTurno = false;
        contadorSeisesTurno = 0; // Reiniciar contador de seises al cambiar turno
        
        // Turno circular
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
        // Serializamos el tablero: COLOR:ID:POS:ENBASE,...
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