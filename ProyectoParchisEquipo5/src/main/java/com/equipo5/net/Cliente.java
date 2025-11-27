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
    
    // Referencias a las pantallas para poder actualizarlas
    private LobbyUI lobbyView;
    private TableroUI tableroView;
    
    private String miNombre; 

    // Constructor vacío, las vistas se asignan después según el flujo
    public Cliente() {}

    public void setLobbyView(LobbyUI lobby) { this.lobbyView = lobby; }

    public void conectar(String host, int puerto, String nickname) {
        this.miNombre = nickname;
        try {
            socket = new Socket(host, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Hilo dedicado a escuchar mensajes del servidor
            new Thread(this::escucharServidor).start();

            // Enviar mensaje de Login inicial
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

        // --- 1. LÓGICA DE CHAT (LOBBY) ---
        if (json.contains("\"type\": \"CHAT\"")) {
            // Parsing manual simple: { "type": "CHAT", "sender": "Ana", "msg": "Hola" }
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
        
        // --- 2. INICIO DE JUEGO (TRANSICIÓN LOBBY -> TABLERO) ---
        else if (json.contains("\"type\": \"START_GAME\"")) {
            SwingUtilities.invokeLater(() -> {
                // Cerrar la ventana del Lobby
                if (lobbyView != null) {
                    lobbyView.dispose(); 
                }
                
                // Abrir la ventana del Tablero
                tableroView = new TableroUI();
                // Pasamos la instancia actual de 'this' (Cliente) para no perder la conexión
                tableroView.setClienteExistente(this); 
                tableroView.setVisible(true);
            });
        }
        
        // --- 3. MENSAJES DENTRO DEL JUEGO (TABLERO) ---
        // Si ya estamos en la fase de tablero, delegamos el procesamiento a la UI del tablero
        else if (tableroView != null) {
            if (json.contains("UPDATE") || json.contains("DICE_RESULT") || json.contains("TURN") || json.contains("LOG")) {
                tableroView.procesarMensajeJuego(json);
            }
        }
        
        // --- 4. BIENVENIDA / ERROR ---
        else if (json.contains("\"type\": \"WELCOME\"")) {
            // Podrías guardar tu color aquí si el servidor lo manda
            if (lobbyView != null) {
                SwingUtilities.invokeLater(() -> lobbyView.agregarMensajeChat("SISTEMA", "Te has unido a la sala."));
            }
        }
    }
}