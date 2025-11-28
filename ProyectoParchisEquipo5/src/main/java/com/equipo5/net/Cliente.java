package com.equipo5.net;

import com.equipo5.view.LobbyUI;
import com.equipo5.view.TableroUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Cliente {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    private LobbyUI lobbyView;
    private TableroUI tableroView;
    
    @SuppressWarnings("unused")
    private String miNombre; 

    public Cliente() {}

    public void setLobbyView(LobbyUI lobby) { 
        this.lobbyView = lobby; 
    }
    
    public void setTableroView(TableroUI tablero) { 
        this.tableroView = tablero; 
    }

    public void conectar(String host, int puerto, String nickname) {
        this.miNombre = nickname;
        try {
            socket = new Socket(host, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this::escucharServidor).start();

            enviar("{ \"type\": \"LOGIN\", \"name\": \"" + nickname + "\" }");

        } catch (IOException e) {
            System.err.println("Error conectando al servidor: " + e.getMessage());
        }
    }

    public void enviar(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }

    private void escucharServidor() {
        try {
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                procesarMensaje(mensaje);
            }
        } catch (IOException e) {
            System.out.println("Desconectado del servidor.");
        }
    }

    private void procesarMensaje(String json) {
        System.out.println("Cliente recibe: " + json);

        // chat
        if (json.contains("\"type\": \"CHAT\"")) {
            try {
                String sender = json.split("\"sender\": \"")[1].split("\"")[0];
                String msg = json.split("\"msg\": \"")[1].split("\"")[0];
                
                if (lobbyView != null) {
                    SwingUtilities.invokeLater(() -> lobbyView.agregarMensajeChat(sender, msg));
                }
            } catch (Exception e) {
                System.out.println("Error parseando chat: " + json);
            }
        }
        
        // inicio juego
        else if (json.contains("\"type\": \"START_GAME\"")) {
            SwingUtilities.invokeLater(() -> {
                // Cerrar la ventana del Lobby si existe
                if (lobbyView != null) {
                    lobbyView.dispose(); 
                }
                
                if (tableroView == null) {
                    tableroView = new TableroUI();
                }
                
                tableroView.setClienteExistente(this); 
                tableroView.setVisible(true);
            });
        }
        
        // --- mensajes dentro del juego
        else if (tableroView != null && 
                (json.contains("UPDATE") || json.contains("DICE_RESULT") || 
                 json.contains("TURN") || json.contains("LOG"))) {
            
            tableroView.procesarMensajeJuego(json);
        }
        
        else if (json.contains("\"type\": \"WELCOME\"")) {
            if (lobbyView != null) {
                SwingUtilities.invokeLater(() -> lobbyView.agregarMensajeChat("SISTEMA", "Te has unido a la sala."));
            }
        }
    }
}