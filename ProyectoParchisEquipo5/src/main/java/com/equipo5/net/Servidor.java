package com.equipo5.net;

import com.equipo5.blackboard.Control;
import com.equipo5.blackboard.Pizarra;
import com.equipo5.model.Jugador;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private int puerto;
    private Pizarra pizarra;
    private Control control; 
    private boolean ejecutando;
    private List<ClienteHilo> clientesConectados; 

    public Servidor(int puerto, int maxJugadores, boolean publica) {
        this.puerto = puerto;
        this.clientesConectados = new ArrayList<>();
        this.pizarra = new Pizarra(maxJugadores, publica);
        this.control = new Control(pizarra, this); 
    }
    
    public Servidor(int puerto) {
        this(puerto, 4, true);
    }

    public void iniciar() {
        ejecutando = true;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor iniciado en puerto " + puerto);
            control.iniciarCiclo();

            while (ejecutando) {
                Socket socketCliente = serverSocket.accept();
                ClienteHilo hilo = new ClienteHilo(socketCliente, pizarra, this);
                clientesConectados.add(hilo);
                hilo.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String mensaje) {
        for (ClienteHilo cliente : clientesConectados) {
            cliente.enviarMensaje(mensaje);
        }
    }
    
    public void broadcastLobbyStatus() {
        StringBuilder sb = new StringBuilder("Jugadores en sala:\n");
        for(Jugador j : pizarra.getJugadores()) {
            sb.append("- ").append(j.getNombre())
              .append(j.isListo() ? " (LISTO)" : " (ESPERANDO)").append("\n");
        }
        broadcast("{ \"type\": \"CHAT\", \"sender\": \"SISTEMA\", \"msg\": \"" + sb.toString().replace("\n", " | ") + "\" }");
    }

    public void iniciarJuego() {
        if (pizarra.getJugadores().size() < 2) {
            broadcast("{ \"type\": \"CHAT\", \"sender\": \"SISTEMA\", \"msg\": \"Faltan jugadores. Mínimo 2.\" }");
            return;
        }
        
        pizarra.setJuegoIniciado(true);
        broadcast("{ \"type\": \"START_GAME\" }");
        try { Thread.sleep(1000); } catch(Exception e){}
        control.iniciarPrimerTurno();
    }
}