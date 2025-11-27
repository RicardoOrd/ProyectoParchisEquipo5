package com.equipo5.net;

import com.equipo5.blackboard.Pizarra;
import com.equipo5.model.Jugador;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteHilo extends Thread {
    private Socket socket;
    private Pizarra pizarra;
    private Servidor servidor;
    private PrintWriter out;
    private BufferedReader in;
    private Jugador miJugador;

    public ClienteHilo(Socket socket, Pizarra pizarra, Servidor servidor) {
        this.socket = socket;
        this.pizarra = pizarra;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("CONECTADO");

            String linea;
            while ((linea = in.readLine()) != null) {
                procesarMensaje(linea);
            }
        } catch (IOException e) {
            // Desconexión
        }
    }

    private void procesarMensaje(String json) {
        if (json.contains("\"type\": \"LOGIN\"")) {
            String nombre = json.split("\"name\": \"")[1].split("\"")[0];
            miJugador = new Jugador(nombre);
            if (pizarra.registrarJugador(miJugador)) {
                // Enviar confirmación y actualizar lobby a todos
                enviarMensaje("{ \"type\": \"WELCOME\", \"color\": \"AUTO\" }");
                servidor.broadcastLobbyStatus(); 
            }
        } 
        else if (json.contains("\"type\": \"CHAT\"")) {
            String msg = json.split("\"msg\": \"")[1].split("\"")[0];
            // Reenviar a todos con el nombre del emisor
            servidor.broadcast("{ \"type\": \"CHAT\", \"sender\": \"" + miJugador.getNombre() + "\", \"msg\": \"" + msg + "\" }");
        }
        else if (json.contains("\"type\": \"TOGGLE_READY\"")) {
            boolean status = json.contains("\"status\": true");
            miJugador.setListo(status);
            // Avisar a todos del cambio de estado
            servidor.broadcastLobbyStatus();
        }
        else if (json.contains("\"type\": \"START_GAME_REQUEST\"")) {
            // Solo el host (primer jugador) debería poder iniciar, o simplificamos
            servidor.iniciarJuego();
        }
        // Mensajes de juego (dado, mover)
        else if (json.contains("ROLL") || json.contains("MOVE")) {
            pizarra.notificarCambio(json.contains("ROLL") ? "SOLICITUD_DADO" : "SOLICITUD_MOVIMIENTO", 
                                    json.contains("ROLL") ? miJugador.getNombre() : json.split("\"ficha\": ")[1].split(" ")[0].replace("}", "").trim());
        }
    }

    public void enviarMensaje(String msg) {
        if (out != null) out.println(msg);
    }
}