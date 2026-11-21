package com.nova.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A lightweight, fully custom click GUI. All features are client-side information/HUD helpers. */
public final class NovaScreen extends Screen {
    private static final String[] CATEGORIES = {"Combat", "Player", "Movement", "Render", "World", "HUD", "Visuals", "Utility", "Settings"};
    private static final int PANEL_W = 650, PANEL_H = 440;
    private String category = "HUD";
    private String search = "";
    private boolean searchFocused;
    private boolean dragging;
    private boolean draggingSlider;
    private int dragX, dragY;
    private double lastMouseX, lastMouseY;
    private int panelLeft, panelTop;
    private int scroll;

    public NovaScreen() { super(Component.literal("Nova ClickGUI")); }

    @Override
    protected void init() {
        panelLeft = Math.max(8, Math.min(panelLeft == 0 ? (width - PANEL_W) / 2 : panelLeft, width - Math.min(PANEL_W, width - 16) - 8));
        panelTop = Math.max(8, Math.min(panelTop == 0 ? (height - PANEL_H) / 2 : panelTop, height - Math.min(PANEL_H, height - 16) - 8));
    }

    private int panelWidth() { return Math.min(PANEL_W, width - 16); }
    private int panelHeight() { return Math.min(PANEL_H, height - 16); }
    private int accent() { return NovaConfig.accent(); }
    private int muted() { return 0xFF8993A2; }
    private int white() { return 0xFFF0F3F7; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int w = panelWidth(), h = panelHeight();
        int x = panelLeft, y = panelTop;
        g.fill(0, 0, width, height, 0x55000000);
        g.fill(x + 3, y + 4, x + w + 3, y + h + 4, 0x55000000);
        g.fill(x, y, x + w, y + h, 0xF20D1118);
        g.fill(x, y, x + w, y + 2, accent());
        g.fill(x, y, x + 150, y + h, 0xFF111720);
        g.fill(x + 150, y + 48, x + 151, y + h, 0xFF252D38);

        // Header / draggable title bar
        g.text(font, "NOVA", x + 18, y + 15, white(), false);
        g.text(font, "CLIENT  /  26.1.2", x + 68, y + 16, muted(), false);
        g.text(font, "RIGHT SHIFT", x + w - 112, y + 16, muted(), false);
        g.fill(x + w - 25, y + 10, x + w - 13, y + 22, 0xFF27313D);
        g.text(font, "x", x + w - 22, y + 11, white(), false);
        g.fill(x + 14, y + 37, x + 136, y + 38, 0xFF29323E);

        // Sidebar
        int navY = y + 48;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String item = CATEGORIES[i];
            int iy = navY + i * 29;
            boolean selected = category.equals(item);
            if (selected) {
                g.fill(x + 9, iy - 6, x + 140, iy + 22, 0xFF202B35);
                g.fill(x + 9, iy - 6, x + 12, iy + 22, accent());
            }
            g.text(font, item, x + 22, iy + 2, selected ? white() : muted(), false);
        }
        g.text(font, "NOVA CLIENT", x + 18, y + h - 37, 0xFF647080, false);
        g.text(font, "PRACTICE / QOL", x + 18, y + h - 23, 0xFF647080, false);

        // Main content header
        int cx = x + 170;
        g.text(font, category.toUpperCase(Locale.ROOT), cx, y + 56, white(), false);
        String subtitle = switch (category) {
            case "HUD" -> "Choose the information shown on your screen.";
            case "Visuals" -> "Personalize Nova's interface appearance.";
            case "Combat" -> "Private-world practice tools; no multiplayer combat automation.";
            case "Player" -> "Local player conveniences.";
            case "Movement" -> "Movement information and practice helpers.";
            case "Render" -> "Visual helpers for local practice.";
            case "World" -> "World information and waypoint tools.";
            case "Utility" -> "Small client-side helpers and display options.";
            default -> "Configure the menu and restore defaults.";
        };
        g.text(font, subtitle, cx, y + 72, muted(), false);

        if (category.equals("Visuals")) drawVisuals(g, cx, y);
        else if (category.equals("Settings")) drawSettings(g, cx, y);
        else drawModuleList(g, cx, y);

        g.text(font, "CONFIG SAVED AUTOMATICALLY", x + w - 190, y + h - 19, 0xFF687585, false);
    }

    private void drawModuleList(GuiGraphicsExtractor g, int cx, int y) {
        int searchY = y + 96;
        g.fill(cx, searchY, panelLeft + panelWidth() - 20, searchY + 25, 0xFF171E27);
        g.fill(cx, searchY, cx + 2, searchY + 25, searchFocused ? accent() : 0xFF303B48);
        g.text(font, search.isEmpty() ? "Search modules..." : search, cx + 10, searchY + 8,
                search.isEmpty() ? 0xFF687585 : white(), false);

        List<ModuleEntry> entries = entriesForCategory();
        List<ModuleEntry> filtered = new ArrayList<>();
        for (ModuleEntry e : entries) if (e.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) filtered.add(e);
        if (filtered.isEmpty()) {
            g.text(font, "No modules found", cx + 8, y + 145, muted(), false);
            return;
        }
        int rowY = y + 133;
        int rowH = 37;
        for (int i = scroll; i < Math.min(filtered.size(), scroll + 6); i++) {
            ModuleEntry e = filtered.get(i);
            int ry = rowY + (i - scroll) * 42;
            g.fill(cx, ry, panelLeft + panelWidth() - 20, ry + rowH, 0xFF151C25);
            g.text(font, e.name, cx + 10, ry + 6, white(), false);
            g.text(font, e.description, cx + 10, ry + 20, muted(), false);
            int sx = panelLeft + panelWidth() - 65;
            int sy = ry + 10;
            g.fill(sx, sy, sx + 34, sy + 17, e.enabled.get() ? accent() : 0xFF303A46);
            int knobX = e.enabled.get() ? sx + 19 : sx + 2;
            g.fill(knobX, sy + 2, knobX + 13, sy + 15, 0xFFF4F6F8);
        }
        g.text(font, "CLICK A SWITCH TO TOGGLE  /  " + filtered.size() + " MODULES", cx, y + panelHeight() - 45, muted(), false);
    }

    private void drawVisuals(GuiGraphicsExtractor g, int cx, int y) {
        int sw = panelLeft + panelWidth() - 20;
        g.text(font, "ACCENT COLOR", cx, y + 105, white(), false);
        g.fill(cx, y + 123, sw, y + 160, 0xFF151C25);
        g.fill(cx + 12, y + 134, cx + 38, y + 149, accent());
        g.text(font, String.format(Locale.ROOT, "#%02X%02X%02X", NovaConfig.red, NovaConfig.green, NovaConfig.blue), cx + 48, y + 137, white(), false);
        g.text(font, "RED", cx, y + 181, 0xFFFF8A8A, false);
        drawSlider(g, cx + 50, y + 180, sw - cx - 66, NovaConfig.red);
        g.text(font, "GREEN", cx, y + 214, 0xFF8BE0A4, false);
        drawSlider(g, cx + 50, y + 213, sw - cx - 66, NovaConfig.green);
        g.text(font, "BLUE", cx, y + 247, 0xFF8AB8FF, false);
        drawSlider(g, cx + 50, y + 246, sw - cx - 66, NovaConfig.blue);
        drawButton(g, cx, y + 278, 112, 26, "RANDOM RGB", false);
        drawButton(g, cx + 120, y + 278, 112, 26, "CYAN PRESET", false);
        drawButton(g, cx + 240, y + 278, 112, 26, "PURPLE PRESET", false);
        g.text(font, "Drag a slider or click its track to adjust the accent.", cx, y + 322, muted(), false);
    }

    private void drawSlider(GuiGraphicsExtractor g, int x, int y, int w, int value) {
        g.fill(x, y + 5, x + w, y + 10, 0xFF303A46);
        int knob = x + (w * value / 255);
        g.fill(x, y + 5, knob, y + 10, accent());
        g.fill(knob - 3, y + 1, knob + 4, y + 14, 0xFFF1F4F8);
        g.text(font, Integer.toString(value), x + w - 26, y - 10, muted(), false);
    }

    private void drawSettings(GuiGraphicsExtractor g, int cx, int y) {
        g.text(font, "MENU", cx, y + 108, white(), false);
        g.text(font, "Open / close: Right Shift", cx, y + 130, muted(), false);
        g.text(font, "The menu can be dragged by its top title bar.", cx, y + 150, muted(), false);
        g.text(font, "DATA", cx, y + 190, white(), false);
        g.text(font, "Settings file: .minecraft/config/nova.properties", cx, y + 212, muted(), false);
        drawButton(g, cx, y + 248, 150, 28, "RESET ALL SETTINGS", false);
        g.text(font, "Practice modules are training helpers, not multiplayer cheats.", cx, y + 300, muted(), false);
    }

    private void drawButton(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, boolean active) {
        g.fill(x, y, x + w, y + h, active ? accent() : 0xFF222C37);
        g.text(font, label, x + 9, y + (h - 9) / 2, white(), false);
    }

    private record ModuleEntry(String name, String description, java.util.function.BooleanSupplier enabled, Runnable toggle) {}
    private List<ModuleEntry> entriesForCategory() {
        return switch (category) {
            case "Combat" -> List.of(
                entry("Aimbot Trainer", "Dummy-only aim training; never targets real players", () -> NovaConfig.practiceAim, () -> NovaConfig.practiceAim = !NovaConfig.practiceAim),
                entry("Reach Trainer", "Visual distance ruler for private-world practice", () -> NovaConfig.reachTrainer, () -> NovaConfig.reachTrainer = !NovaConfig.reachTrainer),
                entry("Hit Timing Trainer", "Practice timing prompts for training dummies", () -> NovaConfig.hitTiming, () -> NovaConfig.hitTiming = !NovaConfig.hitTiming),
                entry("Target HUD Preview", "Preview a configurable dummy target panel", () -> NovaConfig.targetPreview, () -> NovaConfig.targetPreview = !NovaConfig.targetPreview));
            case "Player" -> List.of(
                entry("Toggle Sprint", "Client-side sprint convenience", () -> NovaConfig.toggleSprint, () -> NovaConfig.toggleSprint = !NovaConfig.toggleSprint),
                entry("Toggle Sneak", "Client-side sneak convenience", () -> NovaConfig.toggleSneak, () -> NovaConfig.toggleSneak = !NovaConfig.toggleSneak),
                entry("Fullbright", "Brighten the local game view", () -> NovaConfig.fullbright, () -> NovaConfig.fullbright = !NovaConfig.fullbright));
            case "Movement" -> List.of(
                entry("Jump Trainer", "Local jump timing practice indicator", () -> NovaConfig.jumpTrainer, () -> NovaConfig.jumpTrainer = !NovaConfig.jumpTrainer),
                entry("Speedometer", "Display horizontal movement speed", () -> NovaConfig.speed, () -> NovaConfig.speed = !NovaConfig.speed));
            case "Render" -> List.of(
                entry("Dropped Item Highlights", "Highlight dropped-item locations in supported practice worlds", () -> NovaConfig.itemHighlights, () -> NovaConfig.itemHighlights = !NovaConfig.itemHighlights),
                entry("Entity Outline Preview", "Toggle the local entity-outline preview option", () -> NovaConfig.entityOutline, () -> NovaConfig.entityOutline = !NovaConfig.entityOutline));
            case "World" -> List.of(
                entry("Waypoints", "Enable waypoint management panel", () -> NovaConfig.waypoints, () -> NovaConfig.waypoints = !NovaConfig.waypoints),
                entry("World Info", "Show biome and in-game time in HUD", () -> NovaConfig.worldInfo, () -> NovaConfig.worldInfo = !NovaConfig.worldInfo));
            case "HUD" -> hudEntries();
            case "Utility" -> utilityEntries();
            default -> List.of();
        };
    }

    private List<ModuleEntry> hudEntries() {
        return List.of(
                entry("Coordinates", "Shows your XYZ block position", () -> NovaConfig.coords, () -> NovaConfig.coords = !NovaConfig.coords),
                entry("FPS Counter", "Displays the current frame rate", () -> NovaConfig.fps, () -> NovaConfig.fps = !NovaConfig.fps),
                entry("Local Clock", "Displays your computer's local time", () -> NovaConfig.clock, () -> NovaConfig.clock = !NovaConfig.clock),
                entry("Armor Value", "Displays your current armor points", () -> NovaConfig.armor, () -> NovaConfig.armor = !NovaConfig.armor),
                entry("Health", "Displays your current health", () -> NovaConfig.health, () -> NovaConfig.health = !NovaConfig.health),
                entry("Direction", "Displays your facing direction", () -> NovaConfig.direction, () -> NovaConfig.direction = !NovaConfig.direction),
                entry("Movement Speed", "Displays horizontal movement speed", () -> NovaConfig.speed, () -> NovaConfig.speed = !NovaConfig.speed),
                entry("Biome", "Displays the current biome identifier", () -> NovaConfig.biome, () -> NovaConfig.biome = !NovaConfig.biome),
                entry("World Time", "Displays the in-game day time", () -> NovaConfig.worldTime, () -> NovaConfig.worldTime = !NovaConfig.worldTime)
        );
    }
    private List<ModuleEntry> utilityEntries() {
        return List.of(
                entry("Compact HUD", "Use tighter spacing for HUD lines", () -> NovaConfig.compactHud, () -> NovaConfig.compactHud = !NovaConfig.compactHud),
                entry("HUD Background", "Draw a subtle background behind HUD text", () -> NovaConfig.showHudBackground, () -> NovaConfig.showHudBackground = !NovaConfig.showHudBackground)
        );
    }
    private ModuleEntry entry(String n, String d, java.util.function.BooleanSupplier state, Runnable toggle) { return new ModuleEntry(n, d, state, toggle); }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mouseX = click.x(), mouseY = click.y();
        int button = click.button();
        int x = panelLeft, y = panelTop, w = panelWidth();
        if (button == 0 && inside(mouseX, mouseY, x, y, w, 32)) {
            if (mouseX >= x + w - 32) { onClose(); return true; }
            dragging = true; dragX = (int) mouseX - panelLeft; dragY = (int) mouseY - panelTop; lastMouseX = mouseX; lastMouseY = mouseY; return true;
        }
        if (button == 0 && inside(mouseX, mouseY, x + 8, y + 47, 136, 150)) {
            int index = (int) ((mouseY - (y + 47)) / 34);
            if (index >= 0 && index < CATEGORIES.length) { category = CATEGORIES[index]; scroll = 0; search = ""; searchFocused = false; }
            return true;
        }
        int cx = x + 170, sw = x + w - 20;
        if (category.equals("Visuals")) {
            if (mouseY >= y + 178 && mouseY <= y + 194) { setChannel(0, mouseX, cx + 50, sw - cx - 66); draggingSlider = true; lastMouseX = mouseX; lastMouseY = mouseY; return true; }
            if (mouseY >= y + 211 && mouseY <= y + 227) { setChannel(1, mouseX, cx + 50, sw - cx - 66); draggingSlider = true; lastMouseX = mouseX; lastMouseY = mouseY; return true; }
            if (mouseY >= y + 244 && mouseY <= y + 260) { setChannel(2, mouseX, cx + 50, sw - cx - 66); draggingSlider = true; lastMouseX = mouseX; lastMouseY = mouseY; return true; }
            if (inside(mouseX, mouseY, cx, y + 278, 112, 26)) { NovaConfig.red = (int)(Math.random()*256); NovaConfig.green = (int)(Math.random()*256); NovaConfig.blue = (int)(Math.random()*256); NovaConfig.save(); return true; }
            if (inside(mouseX, mouseY, cx + 120, y + 278, 112, 26)) { NovaConfig.red=65; NovaConfig.green=220; NovaConfig.blue=190; NovaConfig.save(); return true; }
            if (inside(mouseX, mouseY, cx + 240, y + 278, 112, 26)) { NovaConfig.red=166; NovaConfig.green=110; NovaConfig.blue=255; NovaConfig.save(); return true; }
        } else if (category.equals("Settings")) {
            if (inside(mouseX, mouseY, cx, y + 248, 150, 28)) { NovaConfig.reset(); return true; }
        } else {
            if (inside(mouseX, mouseY, cx, y + 96, w - 190, 25)) { searchFocused = true; return true; }
            searchFocused = false;
            List<ModuleEntry> entries = entriesForCategory();
            List<ModuleEntry> filtered = entries.stream().filter(e -> e.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))).toList();
            int rowY = y + 133;
            for (int i = scroll; i < Math.min(filtered.size(), scroll + 6); i++) {
                int ry = rowY + (i - scroll) * 42;
                if (inside(mouseX, mouseY, cx, ry, w - 190, 37)) { filtered.get(i).toggle.run(); NovaConfig.save(); return true; }
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging && click.button() == 0) {
            panelLeft = Math.max(8, Math.min(width - panelWidth() - 8, panelLeft + (int)Math.round(deltaX)));
            panelTop = Math.max(8, Math.min(height - panelHeight() - 8, panelTop + (int)Math.round(deltaY)));
            return true;
        }
        if (draggingSlider && click.button() == 0 && category.equals("Visuals")) {
            lastMouseX += deltaX; lastMouseY += deltaY;
            int cx = panelLeft + 170, sw = panelLeft + panelWidth() - 20;
            int channel = lastMouseY < panelTop + 200 ? 0 : lastMouseY < panelTop + 233 ? 1 : 2;
            setChannel(channel, lastMouseX, cx + 50, sw - cx - 66); return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) { dragging = false; draggingSlider = false; }
        return super.mouseReleased(click);
    }

    private void setChannel(int channel, double mouseX, int sx, int sw) {
        int value = NovaConfig.clamp((int)Math.round((mouseX - sx) / Math.max(1.0, sw) * 255));
        if (channel == 0) NovaConfig.red = value; else if (channel == 1) NovaConfig.green = value; else NovaConfig.blue = value;
        NovaConfig.save();
    }
    private boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x+w && my >= y && my <= y+h; }

    @Override
    public boolean charTyped(CharInput input) {
        int codePoint = input.codepoint();
        if (searchFocused && !Character.isISOControl(codePoint) && search.length() < 32) { search += new String(Character.toChars(codePoint)); scroll = 0; return true; }
        return super.charTyped(input);
    }
    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length()-1); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
        }
        if (keyCode == GLFW.GLFW_KEY_UP) { scroll = Math.max(0, scroll - 1); return true; }
        if (keyCode == GLFW.GLFW_KEY_DOWN) { scroll = Math.min(8, scroll + 1); return true; }
        return super.keyPressed(input);
    }
    @Override public void onClose() { NovaConfig.save(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
