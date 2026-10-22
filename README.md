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

Built with [CleanroomGradle](https://github.com/CleanroomMC/CleanroomGradle) and
[Conventions](https://github.com/CleanroomMC/Conventions), following the
[CleanroomModTemplate](https://github.com/CleanroomMC/CleanroomModTemplate). Targets
Cleanroom 0.7.12 and needs a Java 25 JDK.

```
./gradlew build
```

Two jars land in `build/libs`: `do_a_barrel_roll-<version>.jar` (SRG-named, the one to ship)
and `do_a_barrel_roll-<version>-mcp.jar` (MCP-named, for dev environments).

The version is computed from git tags by the Versioning plugin; `./gradlew -q printVersion`
shows it. Tag `1.2.0` on `main` to release that version.

Use the Gradle `runClient` / `runServer` tasks to launch a dev instance. The Mixin config is
passed to dev runs through the `crl.dev.mixin` system property and shipped in the jar via the
`MixinConfigs` manifest attribute.

## License

GPL-3.0, same as the original mod. Original mod © enjarai.
