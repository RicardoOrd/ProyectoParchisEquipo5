package com.equipo5.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dado {
    private List<Integer> historialTiros;
    private Random random;

    public Dado() {
        this.historialTiros = new ArrayList<>();
        this.random = new Random();
    }

    public int tirar() {
        int valor = random.nextInt(6) + 1;
        registrarTiro(valor);
        return valor;
    }

    private void registrarTiro(int valor) {
        historialTiros.add(valor);
        // Mantenemos solo los últimos 3 tiros por eficiencia
        if (historialTiros.size() > 3) {
            historialTiros.remove(0);
        }
    }

    // Regla especificada en el documento 
    public boolean tresSeisesSeguidos() {
        if (historialTiros.size() < 3) return false;
        return historialTiros.get(historialTiros.size() - 1) == 6 &&
               historialTiros.get(historialTiros.size() - 2) == 6 &&
               historialTiros.get(historialTiros.size() - 3) == 6;
    }
    
    public void limpiarHistorial() {
        historialTiros.clear();
    }
}