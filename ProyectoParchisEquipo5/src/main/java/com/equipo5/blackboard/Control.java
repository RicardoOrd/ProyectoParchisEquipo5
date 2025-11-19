package com.equipo5.blackboard;

import java.util.Observable;
import java.util.Observer;

public class Control implements Observer {
    private Pizarra pizarra;

    public Control(Pizarra pizarra) {
        this.pizarra = pizarra;
        // El control se suscribe a la pizarra
        this.pizarra.addObserver(this);
    }
    
    public void iniciarCiclo() {
        System.out.println("Control: Ciclo de monitoreo iniciado.");
    }

    @Override
    public void update(Observable o, Object arg) {
        // Este método se ejecuta AUTOMÁTICAMENTE cuando la pizarra cambia
        if (arg instanceof String[]) {
            String[] evento = (String[]) arg;
            String tipo = evento[0];
            String dato = evento[1];
            
            System.out.println("[CONTROL] Cambio detectado en Pizarra -> " + tipo + ": " + dato);
            
            // Aquí es donde la arquitectura Blackboard brilla:
            // Si tipo es "LANZAR_DADO", llamamos a KSLanzarDado
            // Si tipo es "NUEVO_JUGADOR", llamamos a KSNotificador, etc.
        }
    }
}
