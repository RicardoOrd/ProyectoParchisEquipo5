package com.equipo5.blackboard.sources;

import com.equipo5.blackboard.Pizarra;
import com.equipo5.net.Servidor;

public interface FuenteConocimineto {
    // Definimos un método genérico para ejecutar la lógica
    void ejecutar(Pizarra pizarra, Servidor servidor, String datoAsociado);
}