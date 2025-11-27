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

    public Servidor(int puerto) {
        this.puerto = puerto;
        this.clientesConectados = new ArrayList<>();
        this.pizarra = new Pizarra();
        this.control = new Control(pizarra, this); 
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
        // Construimos un JSON manual con la lista de jugadores
        // { "type": "LOBBY_UPDATE", "players": [ { "name": "Ana", "ready": true }, ... ] }
        // Nota: Por simplicidad, aquí enviamos un mensaje de CHAT especial que el cliente interpretará
        // En un sistema real, usaríamos GSON.
        // Simularemos enviando los nombres por chat de sistema por ahora para no romper el parser manual
        
        StringBuilder sb = new StringBuilder("Jugadores en sala:\n");
        for(Jugador j : pizarra.getJugadores()) {
            sb.append("- ").append(j.getNombre())
              .append(j.isListo() ? " (LISTO)" : " (ESPERANDO)").append("\n");
        }
        broadcast("{ \"type\": \"CHAT\", \"sender\": \"SISTEMA\", \"msg\": \"" + sb.toString().replace("\n", " | ") + "\" }");
        
        // Para que la UI de Lobby se actualice gráficamente, necesitaríamos parsear JSON de objetos.
        // Dado el límite de parsing manual, usar el chat de sistema es lo más robusto sin librerías.
        // Si quieres la lista gráfica, implementa el parsing de JSON arrays en Cliente.java
    }

    public void iniciarJuego() {
        pizarra.setJuegoIniciado(true);
        broadcast("{ \"type\": \"START_GAME\" }");
        // Dar tiempo a clientes para abrir ventana
        try { Thread.sleep(1000); } catch(Exception e){}
        control.iniciarPrimerTurno();
    }
}