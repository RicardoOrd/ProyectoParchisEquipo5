# ParchisMVC (Simulacion UC-03 / UC-05)
Proyecto Java (Maven) con arquitectura MVC para demostrar **Iniciar Partida** y **Lanzar Dado** con dos jugadores.

## Requisitos
- Java 17+
- Maven 3.8+
- NetBeans 15+ (o cualquier IDE con soporte Maven)

## Ejecucion rapida
```bash
mvn -q clean package
mvn -q exec:java
# O bien:
java -jar target/ParchisMVC-1.0-SNAPSHOT.jar
```

## Flujo demostrado
1. Lobby: registrar 2 jugadores (nombre + color).
2. Iniciar partida: se fija orden aleatorio de turnos.
3. Partida: boton **Lanzar dado** para el jugador en turno y **Siguiente turno** para rotar.

## Estructura de paquetes
- `com.equipo5.parchis.ModeloJuego` (Juego, Jugador, Dado)
- `com.equipo5.parchis.controlador` (ControlJuego)
- `com.equipo5.parchis.PresentacionJuego` (VentanaLobby, VentanaPartida)

## Notas
- No hay reglas completas de Parchis; es una **simulacion minima** para el caso de uso principal.
- Puedes extender facilmente con reglas, tablero y mas casos.
