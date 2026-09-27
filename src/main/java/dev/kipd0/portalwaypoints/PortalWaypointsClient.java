package dev.kipd0.portalwaypoints;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public final class PortalWaypointsClient implements ClientModInitializer {
    public static final String MOD_ID = "portalwaypoints";

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        WaypointStore.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "general")
        );

        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.portalwaypoints.open",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_U,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                client.setScreen(new WaypointScreen());
            }
        });

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "waypoint_hud"),
                PortalWaypointsClient::renderHud
        );
    }

    private static void renderHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.screen != null) {
            return;
        }

        Waypoint waypoint = WaypointStore.getActive();
        if (waypoint == null) {
            return;
        }

        boolean nether = client.level.dimension().equals(Level.NETHER);
        boolean overworld = client.level.dimension().equals(Level.OVERWORLD);
        if (!nether && !overworld) {
            return;
        }

        int targetX = nether ? waypoint.netherX() : waypoint.overworldX;
        int targetZ = nether ? waypoint.netherZ() : waypoint.overworldZ;

        double dx = targetX - client.player.getX();
        double dz = targetZ - client.player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double relativeYaw = wrapDegrees(targetYaw - client.player.getYRot());
        String arrow = arrowFor(relativeYaw);

        String dimensionName = nether ? "Nether" : "Overworld";
        String line1 = arrow + "  " + waypoint.name;
        String line2 = dimensionName + "  X " + targetX + "  Z " + targetZ;
        String line3 = Math.round(distance) + " blocks";

        int centerX = graphics.guiWidth() / 2;
        int boxWidth = Math.max(170, Math.max(client.font.width(line1), client.font.width(line2)) + 20);
        int left = centerX - boxWidth / 2;
        int right = centerX + boxWidth / 2;

        graphics.fill(left, 6, right, distance <= 4 ? 51 : 42, 0x90000000);
        graphics.drawCenteredString(client.font, Component.literal(line1), centerX, 10, 0xFFFFFFFF);
        graphics.drawCenteredString(client.font, Component.literal(line2), centerX, 21, 0xFFE0E0E0);
        graphics.drawCenteredString(client.font, Component.literal(line3), centerX, 32, 0xFFFFD966);

        if (distance <= 4) {
            String reached = nether ? "BUILD PORTAL HERE" : "WAYPOINT REACHED";
            graphics.drawCenteredString(client.font, Component.literal(reached), centerX, 43, 0xFF55FF55);
        }
    }

    private static double wrapDegrees(double degrees) {
        degrees %= 360.0;
        if (degrees >= 180.0) {
            degrees -= 360.0;
        }
        if (degrees < -180.0) {
            degrees += 360.0;
        }
        return degrees;
    }

    private static String arrowFor(double angle) {
        if (angle >= -22.5 && angle < 22.5) return "↑";
        if (angle >= 22.5 && angle < 67.5) return "↗";
        if (angle >= 67.5 && angle < 112.5) return "→";
        if (angle >= 112.5 && angle < 157.5) return "↘";
        if (angle >= 157.5 || angle < -157.5) return "↓";
        if (angle >= -157.5 && angle < -112.5) return "↙";
        if (angle >= -112.5 && angle < -67.5) return "←";
        return "↖";
    }
}
