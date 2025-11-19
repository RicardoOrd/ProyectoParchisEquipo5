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
    private PrintWriter out;
    private BufferedReader in;
    private Jugador miJugador;

    public ClienteHilo(Socket socket, Pizarra pizarra) {
        this.socket = socket;
        this.pizarra = pizarra;
    }

    @Override
    public void run() {
        try {
            // Configurar streams de entrada/salida
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1. Protocolo de Bienvenida
            out.println("CONECTADO: Por favor envía tu nombre");
            
            // 2. Esperar nombre del jugador
            String nombreRecibido = in.readLine();
            
            if (nombreRecibido != null && !nombreRecibido.isEmpty()) {
                miJugador = new Jugador(nombreRecibido);
                boolean exito = pizarra.registrarJugador(miJugador);
                
                if (exito) {
                    out.println("OK: Bienvenido " + nombreRecibido);
                    System.out.println("Red: Jugador registrado -> " + nombreRecibido);
                } else {
                    out.println("ERROR: Sala llena");
                    cerrarConexion();
                    return;
                }
            }

            // 3. Bucle principal: Escuchar mensajes del cliente
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println("Mensaje de " + miJugador.getNombre() + ": " + mensaje);
                // Aquí luego conectaremos con el Control para procesar "LANZAR_DADO", etc.
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + (miJugador != null ? miJugador.getNombre() : "Desconocido"));
        } finally {
            cerrarConexion();
        }
    }
    
    public void enviarMensaje(String msg) {
        if (out != null) out.println(msg);
    }
    
    private void cerrarConexion() {
        try { socket.close(); } catch (IOException e) {}
    }
}