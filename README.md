# Do a Barrel Roll — Cleanroom 1.12.2 Port

Microsoft flight simulator for Minecraft elytras.

This is a port of [Do a Barrel Roll](https://github.com/enjarai/do-a-barrel-roll)
(originally by **enjarai**, GPL-3.0) to **Minecraft 1.12.2** running on the
[Cleanroom](https://cleanroommc.com/) mod loader (Forge fork).

## Features

- Camera roll while flying with an elytra — mouse Y controls pitch, mouse X controls roll
- Coordinated-turn banking, per-axis sensitivity/smoothing, automatic righting
- Keyboard flight controls (pitch/yaw/roll) that only activate while gliding
- Activation behaviours: vanilla, triple-jump, hybrid, hybrid-toggle
- Optional thrusting (server-controlled)
- Momentum-based mouse mode with HUD widget, artificial horizon widget
- Server config sync (thrusting, force-enabled, force-installed, kinetic damage)
- Roll sync: see other players' rolls in multiplayer
- Star Fox easter eggs

## Configuration

- Client: `config/do_a_barrel_roll-client.json`
- Server: `config/do_a_barrel_roll-server.json`

There is no in-game config GUI in this port; edit the JSON files and restart (or
reconnect for server values pushed by the server).

## Building

Based on the [CleanroomModTemplate](https://github.com/CleanroomMC/CleanroomModTemplate)
(custom Unimined fork, Gradle 9, Java 25 toolchain):

```
./gradlew build
```

The built jar lands in `build/libs`. Use the Gradle `runClient`/`runServer` tasks
(the IDEA run configurations `2. Run Client` / `3. Run Server`) to launch a dev
instance — do not use IntelliJ's own "Minecraft Client" configuration.

## License

GPL-3.0, same as the original mod. Original mod © enjarai.
