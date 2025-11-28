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
            
            // Mensaje inicial de conexión exitosa
            out.println("CONECTADO");

            String linea;
            while ((linea = in.readLine()) != null) {
                procesarMensaje(linea);
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + (miJugador != null ? miJugador.getNombre() : "Desconocido"));
        } finally {
            // Limpieza al desconectar
            if (miJugador != null) {
                pizarra.getInfoSala().eliminarJugador(miJugador);
                servidor.broadcastLobbyStatus(); // Actualizar a los demás que se fue
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private void procesarMensaje(String json) {
        if (json.contains("\"type\": \"LOGIN\"")) {
            try {
                String nombre = json.split("\"name\": \"")[1].split("\"")[0];
                miJugador = new Jugador(nombre);
                
                if (pizarra.registrarJugador(miJugador)) {
                    enviarMensaje("{ \"type\": \"WELCOME\", \"color\": \"AUTO\" }");
                    
                    servidor.broadcastLobbyStatus(); 
                } else {
                    enviarMensaje("{ \"type\": \"ERROR\", \"msg\": \"Sala llena o en juego\" }");
                }
            } catch (Exception e) {
                System.out.println("Error en Login: " + json);
            }
        } 
        else if (json.contains("\"type\": \"CHAT\"")) {
            try {
                String msg = json.split("\"msg\": \"")[1].split("\"")[0];
                servidor.broadcast("{ \"type\": \"CHAT\", \"sender\": \"" + miJugador.getNombre() + "\", \"msg\": \"" + msg + "\" }");
            } catch (Exception e) {}
        }
        else if (json.contains("\"type\": \"TOGGLE_READY\"")) {
            boolean status = json.contains("\"status\": true");
            miJugador.setListo(status);
            servidor.broadcastLobbyStatus(); // Actualizar checks
        }
        else if (json.contains("\"type\": \"START_GAME_REQUEST\"")) {
            servidor.iniciarJuego();
        }
        else if (json.contains("ROLL") || json.contains("MOVE")) {
            try {
                String param = json.contains("ROLL") ? miJugador.getNombre() : json.split("\"ficha\": ")[1].split(" ")[0].replace("}", "").trim();
                pizarra.notificarCambio(json.contains("ROLL") ? "SOLICITUD_DADO" : "SOLICITUD_MOVIMIENTO", param);
            } catch (Exception e) {}
        }
    }

    public void enviarMensaje(String msg) {
        if (out != null) out.println(msg);
    }
}