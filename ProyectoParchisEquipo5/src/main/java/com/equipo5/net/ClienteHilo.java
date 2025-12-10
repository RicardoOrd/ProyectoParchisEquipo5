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
                // --- CAMBIO INICIO ---
                // Solo eliminamos al jugador si el juego NO ha iniciado.
                // Si ya inició, lo dejamos en la lista (aunque esté desconectado) para no romper el orden de turnos.
                if (!pizarra.isJuegoIniciado()) {
                    pizarra.getInfoSala().eliminarJugador(miJugador);
                    servidor.broadcastLobbyStatus();
                } else {
                    // Opcional: Avisar a los demás que se fue
                    servidor.broadcast("{ \"type\": \"LOG\", \"msg\": \"⚠️ " + miJugador.getNombre() + " se ha desconectado.\" }");
                }
                // --- CAMBIO FIN ---
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void procesarMensaje(String json) {
        if (json.contains("\"type\": \"LOGIN\"")) {
            try {
                String nombre = json.split("\"name\": \"")[1].split("\"")[0];

                String avatar = "default";
                if (json.contains("\"avatar\": \"")) {
                    avatar = json.split("\"avatar\": \"")[1].split("\"")[0];
                }

                // --- VALIDACIÓN DE CÓDIGO (UC-02) ---
                // Leemos el código enviado por el cliente
                String codeCliente = "";
                if (json.contains("\"code\": \"")) {
                    codeCliente = json.split("\"code\": \"")[1].split("\"")[0];
                }

                // Verificamos si la sala es privada
                if (!pizarra.getInfoSala().isPublica()) {
                    // CORRECCIÓN: Si la lista de jugadores NO está vacía, validamos.
                    // Si está vacía, es el Anfitrión (Host) entrando, lo dejamos pasar sin código.
                    if (!pizarra.getJugadores().isEmpty()) {
                        String codigoReal = pizarra.getInfoSala().getCodigo();
                        if (!codeCliente.equalsIgnoreCase(codigoReal)) { // Ignorar mayúsculas/minúsculas
                            enviarMensaje("{ \"type\": \"ERROR\", \"msg\": \"Sala Privada. Codigo incorrecto.\" }");
                            return; // Rechazamos la conexión
                        }
                    }
                }
                // ------------------------------------

                miJugador = new Jugador(nombre);
                miJugador.setAvatar(avatar);

                if (pizarra.registrarJugador(miJugador)) {
                    enviarMensaje("{ \"type\": \"WELCOME\", \"color\": \"AUTO\" }");

                    // Si es privada, le recordamos el código al usuario (útil para el anfitrión)
                    if (!pizarra.getInfoSala().isPublica()) {
                        enviarMensaje("{ \"type\": \"CHAT\", \"sender\": \"SISTEMA\", \"msg\": \"Código de Sala: " + pizarra.getInfoSala().getCodigo() + "\" }");
                    }

                    servidor.broadcastLobbyStatus();
                } else {
                    enviarMensaje("{ \"type\": \"ERROR\", \"msg\": \"Sala llena o en juego\" }");
                }
            } catch (Exception e) {
                System.out.println("Error en Login: " + json);
            }
        } else if (json.contains("\"type\": \"CHAT\"")) {
            try {
                String msg = json.split("\"msg\": \"")[1].split("\"")[0];
                servidor.broadcast("{ \"type\": \"CHAT\", \"sender\": \"" + miJugador.getNombre() + "\", \"msg\": \"" + msg + "\" }");
            } catch (Exception e) {
            }
        } else if (json.contains("\"type\": \"TOGGLE_READY\"")) {
            boolean status = json.contains("\"status\": true");
            miJugador.setListo(status);
            servidor.broadcastLobbyStatus(); // Actualizar checks
        } else if (json.contains("\"type\": \"START_GAME_REQUEST\"")) {
            // NUEVO: Auto-listo para el Host
            // Si el que pulsa iniciar (Host) no estaba listo, lo ponemos listo a la fuerza.
            if (!miJugador.isListo()) {
                miJugador.setListo(true);
                // Avisamos a todos del cambio de estado visualmente antes de iniciar
                servidor.broadcastLobbyStatus();
            }

            // Intentamos iniciar (ahora la validación del Servidor pasará si el resto también está listo)
            servidor.iniciarJuego();
        } else if (json.contains("ROLL") || json.contains("MOVE")) {
            try {
                String param = json.contains("ROLL") ? miJugador.getNombre() : json.split("\"ficha\": ")[1].split(" ")[0].replace("}", "").trim();
                pizarra.notificarCambio(json.contains("ROLL") ? "SOLICITUD_DADO" : "SOLICITUD_MOVIMIENTO", param);
            } catch (Exception e) {
            }
        }
    }

    public void enviarMensaje(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}
