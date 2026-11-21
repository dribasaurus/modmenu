package com.nova.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class NovaClient implements ClientModInitializer {
    private static KeyMapping openMenu;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("nova", "general"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.nova.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        NovaConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) {
                Minecraft.getInstance().setScreen(Minecraft.getInstance().screen instanceof NovaScreen ? null : new NovaScreen());
            }
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("nova", "qol_hud"), NovaClient::renderHud);
    }

    private static void renderHud(net.minecraft.client.gui.GuiGraphicsExtractor graphics,
                                  net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;
        var player = client.player;
        List<String> lines = new ArrayList<>();
        if (NovaConfig.coords) {
            var pos = player.blockPosition();
            lines.add("XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ());
        }
        if (NovaConfig.fps) lines.add("FPS  " + client.getFps());
        if (NovaConfig.clock) lines.add("TIME  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        if (NovaConfig.armor) lines.add("ARMOR  " + player.getArmorValue());
        if (NovaConfig.health) lines.add(String.format(java.util.Locale.ROOT, "HEALTH  %.1f / %.1f", player.getHealth(), player.getMaxHealth()));
        if (NovaConfig.direction) {
            String[] directions = {"SOUTH", "WEST", "NORTH", "EAST"};
            int facing = Math.floorMod((int)Math.floor(player.getYRot() / 90.0f + 0.5), 4);
            lines.add("FACING  " + directions[facing]);
        }
        if (NovaConfig.speed) {
            double blocksPerSecond = player.getDeltaMovement().horizontalDistance() * 20.0;
            lines.add(String.format(java.util.Locale.ROOT, "SPEED  %.2f m/s", blocksPerSecond));
        }
        if (NovaConfig.biome) {
            String biome = player.level().getBiome(player.blockPosition()).unwrapKey()
                    .map(key -> key.identifier().toString()).orElse("unknown");
            lines.add("BIOME  " + biome);
        }
        if (NovaConfig.worldTime) {
            long time = Math.floorMod(player.level().getDayTime(), 24000L);
            long hours = (time / 1000 + 6) % 24;
            long minutes = (time % 1000) * 60 / 1000;
            lines.add(String.format(java.util.Locale.ROOT, "WORLD  %02d:%02d", hours, minutes));
        }

        int y = 8;
        int accent = NovaConfig.accent();
        int lineHeight = NovaConfig.compactHud ? 12 : 16;
        for (String text : lines) {
            int textWidth = client.font.width(text);
            if (NovaConfig.showHudBackground) {
                graphics.fill(6, y - 2, textWidth + 16, y + 10, 0x990D1117);
                graphics.fill(6, y - 2, 8, y + 10, accent);
            }
            graphics.text(client.font, text, NovaConfig.showHudBackground ? 12 : 6, y, 0xFFF1F5F9, true);
            y += lineHeight;
        }
    }
}
