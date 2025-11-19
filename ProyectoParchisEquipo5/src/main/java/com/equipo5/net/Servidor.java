package com.equipo5.net;

import com.equipo5.blackboard.Control;
import com.equipo5.blackboard.Pizarra;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private int puerto;
    private Pizarra pizarra;
    private Control control; // El cerebro del Blackboard
    private boolean ejecutando;

    public Servidor(int puerto) {
        this.puerto = puerto;
        this.pizarra = new Pizarra();
        // Inicializamos el Control pasándole la pizarra
        this.control = new Control(pizarra); 
    }

    public void iniciar() {
        ejecutando = true;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("--- SERVIDOR BLACKBOARD INICIADO EN PUERTO " + puerto + " ---");
            
            // Arrancar el ciclo de control
            control.iniciarCiclo();

            while (ejecutando) {
                Socket socketCliente = serverSocket.accept();
                // Crear un hilo para atender al nuevo cliente
                ClienteHilo hilo = new ClienteHilo(socketCliente, pizarra);
                hilo.start();
            }
        } catch (IOException e) {
            System.err.println("Error iniciando servidor: " + e.getMessage());
        }
    }
}