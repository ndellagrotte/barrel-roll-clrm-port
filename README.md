# Do a Barrel Roll — 1.12.2 Port

Microsoft flight simulator for Minecraft elytras.

This is a port of [Do a Barrel Roll](https://github.com/enjarai/do-a-barrel-roll)
(originally by **enjarai**, GPL-3.0) to **Minecraft 1.12.2**. A single jar runs on both
**MinecraftForge 1.12.2** and the [Cleanroom](https://cleanroommc.com/) loader.

## Requirements

- Minecraft 1.12.2 with **MinecraftForge** (14.23.5.2860 or newer) **or Cleanroom**
- **[MixinBooter](https://github.com/CleanroomMC/MixinBooter)** — required on MinecraftForge, where it
  provides Mixin. Cleanroom already bundles it, so no separate download is needed there.

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

The built jar lands in `build/libs`. The build compiles against **vanilla MinecraftForge** and emits
**Java 8 bytecode**, which is what lets one artifact load on both loaders: Forge 1.12.2 runs on a
Java 8 JVM, and Cleanroom (Java 21+) accepts Java 8 bytecode. Keep `src/main/java` free of records,
`var`, switch expressions, and Java 9+ APIs.

Use the Gradle `runClient`/`runServer` tasks (the IDEA run configurations `2. Run Client` /
`3. Run Server`) to launch a dev instance — do not use IntelliJ's own "Minecraft Client"
configuration. The dev run uses Forge; to test the Cleanroom path, drop the built jar into a real
Cleanroom instance.

## License

GPL-3.0, same as the original mod. Original mod © enjarai.
