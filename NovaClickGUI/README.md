# Nova ClickGUI — Fabric 26.1.2 (expanded UI preview)

Nova is a client-side Fabric mod project with a VAPE-inspired dark click-GUI layout and customizable RGB accent. This build expands the menu categories and adds practice/QOL module entries.

## Included in this source build

- Right Shift opens/closes the menu.
- Dark draggable menu with Combat, Player, Movement, Render, World, HUD, Visuals, Utility, and Settings categories.
- Search field and keyboard list scrolling.
- RGB sliders, random RGB, cyan/purple presets.
- Existing HUD readouts: coordinates, FPS, local clock, armor, health, direction, speed, biome, and world time.
- Existing utility options: compact HUD and HUD background.
- New practice/QOL entries with saved on/off preferences: Aimbot Trainer (dummy-only), Reach Trainer (visual training concept), Hit Timing Trainer, Target HUD Preview, Toggle Sprint/Sneak, Fullbright, Jump Trainer, dropped-item highlight option, entity-outline preview, and waypoints option.

## Important implementation status

The new Combat/Player/Movement/Render/World entries are currently **GUI/configuration scaffolding**: their toggles save preferences, but they do not yet implement the corresponding in-game behavior. In particular, there is no real-player aimbot, reach modification, packet manipulation, or anti-cheat bypass. The Aimbot Trainer entry is a saved practice-mode toggle only; it does not aim or target entities yet. The aim trainer/reach trainer entries are intended for future dummy-based private-world training tools only. The older HUD readouts are implemented.

No multiplayer combat automation is included. The Aimbot Trainer label is explicitly for future dummy-only training implementation.

## Target toolchain

- Minecraft Java Edition 26.1.2
- Fabric Loader 0.19.2
- Fabric API 0.155.3+26.1.2
- Fabric Loom 1.15.0
- Gradle 9.4.0
- Java 25

## Build on Windows

1. Install JDK 25 and verify `java -version` reports 25.
2. Install Gradle 9.4 and verify `gradle --version`.
3. Extract this ZIP.
4. Run `build.bat` or run `gradle build` in this directory.
5. The mod JAR should appear in `build/libs/nova-clickgui-1.1.0.jar`. Do not install the `-sources.jar`.

The first build needs internet access to download dependencies. This environment has Java 21 and no Gradle installation, so the JAR has not been compiled or runtime-tested here.

## Lunar Client

Import the built JAR into a Lunar Fabric 26.1.2 profile if that exact profile supports third-party Fabric mods. Compatibility must be verified in Lunar; if it rejects the mod, provide the full error and latest log.

## Configuration

Settings are saved to `.minecraft/config/nova.properties`.
