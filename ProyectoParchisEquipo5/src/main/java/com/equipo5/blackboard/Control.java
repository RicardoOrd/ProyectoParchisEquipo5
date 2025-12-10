package com.equipo5.blackboard;

import com.equipo5.blackboard.sources.FuenteConocimineto;
import com.equipo5.blackboard.sources.KSLanzarDado;
import com.equipo5.blackboard.sources.KSMoverFicha;
import com.equipo5.blackboard.sources.KSNotificador;
import com.equipo5.net.Servidor;
import java.util.Observable;
import java.util.Observer;

public class Control implements Observer {
    
    private Pizarra pizarra;
    private Servidor servidor;
    
    // Instancias de las Fuentes de Conocimiento (KS)
    private FuenteConocimineto ksLanzarDado;
    private FuenteConocimineto ksMoverFicha;

    public Control(Pizarra pizarra, Servidor servidor) {
        this.pizarra = pizarra;
        this.servidor = servidor;
        this.pizarra.addObserver(this); // Suscribirse a cambios en la Pizarra
        
        // Inicializar las KS
        this.ksLanzarDado = new KSLanzarDado();
        this.ksMoverFicha = new KSMoverFicha();
    }
    
    public void iniciarCiclo() {
        System.out.println("Control: Blackboard escuchando eventos...");
    }
    
    public void iniciarPrimerTurno() {
        if (!pizarra.getJugadores().isEmpty()) {
            pizarra.setTurnoActualIndex(0); 
            // Usamos el notificador para iniciar visualmente
            KSNotificador.enviarEstadoTablero(pizarra, servidor);
            // Truco para forzar el anuncio del turno 0
            pizarra.setTurnoActualIndex(-1); 
            KSNotificador.cambiarTurno(pizarra, servidor);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof String[])) return;
        
        String[] evento = (String[]) arg;
        String tipo = evento[0];
        String dato = evento[1];
        
        // El Control decide QUÉ Fuente de Conocimiento activar según el evento
        switch (tipo) {
            case "SOLICITUD_DADO": 
                ksLanzarDado.ejecutar(pizarra, servidor, dato);
                break;
                
            case "SOLICITUD_MOVIMIENTO": 
                ksMoverFicha.ejecutar(pizarra, servidor, dato);
                break;
                
            case "ESTADO_JUEGO":
                // Aquí podrías delegar a una KSInicioJuego si la tuvieras
                break;
        }
    }
}