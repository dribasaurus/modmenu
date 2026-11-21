package com.nova.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistent client-only settings for Nova. */
public final class NovaConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nova.properties");
    private static final Properties DATA = new Properties();

    public static int red = 65, green = 220, blue = 190;
    public static boolean coords = true;
    public static boolean fps = true;
    public static boolean clock = false;
    public static boolean armor = false;
    public static boolean health = false;
    public static boolean direction = false;
    public static boolean speed = false;
    public static boolean biome = false;
    public static boolean worldTime = false;
    public static boolean compactHud = true;
    public static boolean showHudBackground = true;
    // Practice/QOL module preferences. These are persisted GUI settings; modules marked preview
    // are UI options until their dedicated client-side renderer/logic is implemented.
    public static boolean practiceAim = false, reachTrainer = false, hitTiming = false, targetPreview = false;
    public static boolean toggleSprint = false, toggleSneak = false, fullbright = false;
    public static boolean jumpTrainer = false, itemHighlights = false, entityOutline = false;
    public static boolean waypoints = false, worldInfo = false;

    private NovaConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (InputStream in = Files.newInputStream(FILE)) {
            DATA.load(in);
            red = number("red", 65); green = number("green", 220); blue = number("blue", 190);
            coords = bool("coords", true); fps = bool("fps", true); clock = bool("clock", false);
            armor = bool("armor", false); health = bool("health", false); direction = bool("direction", false);
            speed = bool("speed", false); biome = bool("biome", false); worldTime = bool("worldTime", false);
            compactHud = bool("compactHud", true); showHudBackground = bool("showHudBackground", true);
            practiceAim = bool("practiceAim", false); reachTrainer = bool("reachTrainer", false);
            hitTiming = bool("hitTiming", false); targetPreview = bool("targetPreview", false);
            toggleSprint = bool("toggleSprint", false); toggleSneak = bool("toggleSneak", false);
            fullbright = bool("fullbright", false); jumpTrainer = bool("jumpTrainer", false);
            itemHighlights = bool("itemHighlights", false); entityOutline = bool("entityOutline", false);
            waypoints = bool("waypoints", false); worldInfo = bool("worldInfo", false);
        } catch (Exception ignored) { /* Keep defaults if the config is malformed. */ }
    }

    private static int number(String key, int fallback) {
        try { return clamp(Integer.parseInt(DATA.getProperty(key, Integer.toString(fallback)))); }
        catch (NumberFormatException ignored) { return fallback; }
    }
    private static boolean bool(String key, boolean fallback) {
        return Boolean.parseBoolean(DATA.getProperty(key, Boolean.toString(fallback)));
    }

    public static void save() {
        DATA.setProperty("red", Integer.toString(red)); DATA.setProperty("green", Integer.toString(green));
        DATA.setProperty("blue", Integer.toString(blue)); DATA.setProperty("coords", Boolean.toString(coords));
        DATA.setProperty("fps", Boolean.toString(fps)); DATA.setProperty("clock", Boolean.toString(clock));
        DATA.setProperty("armor", Boolean.toString(armor)); DATA.setProperty("health", Boolean.toString(health));
        DATA.setProperty("direction", Boolean.toString(direction)); DATA.setProperty("speed", Boolean.toString(speed));
        DATA.setProperty("biome", Boolean.toString(biome)); DATA.setProperty("worldTime", Boolean.toString(worldTime));
        DATA.setProperty("compactHud", Boolean.toString(compactHud));
        DATA.setProperty("showHudBackground", Boolean.toString(showHudBackground));
        DATA.setProperty("practiceAim", Boolean.toString(practiceAim)); DATA.setProperty("reachTrainer", Boolean.toString(reachTrainer));
        DATA.setProperty("hitTiming", Boolean.toString(hitTiming)); DATA.setProperty("targetPreview", Boolean.toString(targetPreview));
        DATA.setProperty("toggleSprint", Boolean.toString(toggleSprint)); DATA.setProperty("toggleSneak", Boolean.toString(toggleSneak));
        DATA.setProperty("fullbright", Boolean.toString(fullbright)); DATA.setProperty("jumpTrainer", Boolean.toString(jumpTrainer));
        DATA.setProperty("itemHighlights", Boolean.toString(itemHighlights)); DATA.setProperty("entityOutline", Boolean.toString(entityOutline));
        DATA.setProperty("waypoints", Boolean.toString(waypoints)); DATA.setProperty("worldInfo", Boolean.toString(worldInfo));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { DATA.store(out, "Nova ClickGUI settings"); }
        } catch (Exception ignored) { /* A read-only config directory should not crash the client. */ }
    }

    public static int accent() { return 0xFF000000 | (red << 16) | (green << 8) | blue; }
    public static int clamp(int value) { return Math.max(0, Math.min(255, value)); }
    public static void reset() {
        red = 65; green = 220; blue = 190;
        coords = true; fps = true; clock = false; armor = false; health = false;
        direction = false; speed = false; biome = false; worldTime = false;
        compactHud = true; showHudBackground = true;
        practiceAim = reachTrainer = hitTiming = targetPreview = false;
        toggleSprint = toggleSneak = fullbright = jumpTrainer = false;
        itemHighlights = entityOutline = waypoints = worldInfo = false;
        save();
    }
}
