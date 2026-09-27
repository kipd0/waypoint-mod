package dev.kipd0.portalwaypoints;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class WaypointScreen extends Screen {
    private static final int ROWS_PER_PAGE = 4;

    private boolean editorOpen;
    private boolean syncingFields;
    private int page;
    private String errorMessage = "";

    private EditBox nameBox;
    private EditBox overworldXBox;
    private EditBox overworldZBox;
    private EditBox netherXBox;
    private EditBox netherZBox;

    public WaypointScreen() {
        super(Component.literal("Portal Waypoints"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        addRenderableWidget(Button.builder(Component.literal("Add Waypoint"), button -> {
            editorOpen = !editorOpen;
            errorMessage = "";
            rebuildWidgets();
        }).bounds(centerX - 75, 32, 150, 20).build());

        if (editorOpen) {
            createEditor(centerX);
        } else {
            createSavedWaypointRows(centerX, 68);
        }
    }

    private void createEditor(int centerX) {
        int fieldWidth = 92;
        int gap = 8;
        int left = centerX - fieldWidth - gap / 2;
        int right = centerX + gap / 2;

        nameBox = new EditBox(this.font, centerX - 96, 61, 192, 20, Component.literal("Waypoint name"));
        nameBox.setMaxLength(40);
        addRenderableWidget(nameBox);

        overworldXBox = coordinateBox(left, 98, fieldWidth, "Overworld X");
        overworldZBox = coordinateBox(right, 98, fieldWidth, "Overworld Z");
        netherXBox = coordinateBox(left, 132, fieldWidth, "Nether X");
        netherZBox = coordinateBox(right, 132, fieldWidth, "Nether Z");

        overworldXBox.setResponder(value -> syncFromOverworld());
        overworldZBox.setResponder(value -> syncFromOverworld());
        netherXBox.setResponder(value -> syncFromNether());
        netherZBox.setResponder(value -> syncFromNether());

        addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveNewWaypoint())
                .bounds(centerX + 102, 61, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            editorOpen = false;
            errorMessage = "";
            rebuildWidgets();
        }).bounds(centerX + 102, 86, 58, 20).build());
    }

    private EditBox coordinateBox(int x, int y, int width, String label) {
        EditBox box = new EditBox(this.font, x, y, width, 20, Component.literal(label));
        box.setMaxLength(12);
        box.setFilter(value -> value.matches("-?\\d*"));
        addRenderableWidget(box);
        return box;
    }

    private void syncFromOverworld() {
        if (syncingFields) {
            return;
        }

        Integer x = parseCoordinate(overworldXBox.getValue());
        Integer z = parseCoordinate(overworldZBox.getValue());
        if (x == null || z == null) {
            return;
        }

        syncingFields = true;
        netherXBox.setValue(Integer.toString(Math.floorDiv(x, 8)));
        netherZBox.setValue(Integer.toString(Math.floorDiv(z, 8)));
        syncingFields = false;
    }

    private void syncFromNether() {
        if (syncingFields) {
            return;
        }

        Integer x = parseCoordinate(netherXBox.getValue());
        Integer z = parseCoordinate(netherZBox.getValue());
        if (x == null || z == null) {
            return;
        }

        long overworldX = (long) x * 8L;
        long overworldZ = (long) z * 8L;
        if (overworldX < Integer.MIN_VALUE || overworldX > Integer.MAX_VALUE
                || overworldZ < Integer.MIN_VALUE || overworldZ > Integer.MAX_VALUE) {
            return;
        }

        syncingFields = true;
        overworldXBox.setValue(Long.toString(overworldX));
        overworldZBox.setValue(Long.toString(overworldZ));
        syncingFields = false;
    }

    private void saveNewWaypoint() {
        String name = nameBox.getValue().trim();
        Integer x = parseCoordinate(overworldXBox.getValue());
        Integer z = parseCoordinate(overworldZBox.getValue());

        if (name.isEmpty()) {
            errorMessage = "Enter a waypoint name.";
            return;
        }
        if (x == null || z == null) {
            errorMessage = "Enter valid X and Z coordinates.";
            return;
        }

        Waypoint waypoint = WaypointStore.add(name, x, z);
        WaypointStore.setActive(waypoint.id);
        editorOpen = false;
        errorMessage = "";

        int lastPage = Math.max(0, (WaypointStore.getWaypoints().size() - 1) / ROWS_PER_PAGE);
        page = lastPage;
        rebuildWidgets();
    }

    private void createSavedWaypointRows(int centerX, int listTop) {
        List<Waypoint> waypoints = WaypointStore.getWaypoints();
        int maxPage = Math.max(0, (waypoints.size() - 1) / ROWS_PER_PAGE);
        page = Math.max(0, Math.min(page, maxPage));

        int start = page * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, waypoints.size());

        for (int i = start; i < end; i++) {
            Waypoint waypoint = waypoints.get(i);
            int rowY = listTop + (i - start) * 27;

            addRenderableWidget(Button.builder(
                    Component.literal(WaypointStore.isActive(waypoint.id) ? "Loaded" : "Load"),
                    button -> {
                        WaypointStore.setActive(waypoint.id);
                        rebuildWidgets();
                    }
            ).bounds(centerX + 50, rowY, 52, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
                WaypointStore.delete(waypoint.id);
                rebuildWidgets();
            }).bounds(centerX + 106, rowY, 54, 20).build());
        }

        if (maxPage > 0) {
            int navY = listTop + ROWS_PER_PAGE * 27 + 2;
            Button previous = Button.builder(Component.literal("<"), button -> {
                page--;
                rebuildWidgets();
            }).bounds(centerX - 60, navY, 40, 20).build();
            previous.active = page > 0;
            addRenderableWidget(previous);

            Button next = Button.builder(Component.literal(">"), button -> {
                page++;
                rebuildWidgets();
            }).bounds(centerX + 20, navY, 40, 20).build();
            next.active = page < maxPage;
            addRenderableWidget(next);
        }

        if (WaypointStore.getActive() != null) {
            int clearY = Math.min(this.height - 28, listTop + ROWS_PER_PAGE * 27 + 28);
            addRenderableWidget(Button.builder(Component.literal("Clear Active"), button -> {
                WaypointStore.clearActive();
                rebuildWidgets();
            }).bounds(centerX - 60, clearY, 120, 20).build());
        }
    }

    private static Integer parseCoordinate(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        graphics.drawCenteredString(this.font, this.title, centerX, 12, 0xFFFFFFFF);

        if (editorOpen) {
            graphics.drawString(this.font, "Name", centerX - 96, 52, 0xFFBFBFBF);
            graphics.drawString(this.font, "Overworld X", centerX - 96, 88, 0xFFFFFFFF);
            graphics.drawString(this.font, "Overworld Z", centerX + 4, 88, 0xFFFFFFFF);
            graphics.drawString(this.font, "Nether X", centerX - 96, 122, 0xFFAA77FF);
            graphics.drawString(this.font, "Nether Z", centerX + 4, 122, 0xFFAA77FF);

            if (!errorMessage.isEmpty()) {
                graphics.drawString(this.font, errorMessage, centerX - 96, 156, 0xFFFF5555);
            }
        }

        if (!editorOpen) {
            drawSavedWaypointText(graphics, centerX, 68);
        }
    }

    private void drawSavedWaypointText(GuiGraphics graphics, int centerX, int listTop) {
        List<Waypoint> waypoints = WaypointStore.getWaypoints();
        if (waypoints.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.literal("No saved waypoints"), centerX, listTop + 6, 0xFFAAAAAA);
            return;
        }

        int start = page * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, waypoints.size());

        for (int i = start; i < end; i++) {
            Waypoint waypoint = waypoints.get(i);
            int rowY = listTop + (i - start) * 27;
            int nameColor = WaypointStore.isActive(waypoint.id) ? 0xFF55FF55 : 0xFFFFFFFF;

            String name = waypoint.name.length() > 24 ? waypoint.name.substring(0, 23) + "…" : waypoint.name;
            String coords = "OW " + waypoint.overworldX + ", " + waypoint.overworldZ
                    + "  |  N " + waypoint.netherX() + ", " + waypoint.netherZ();

            graphics.drawString(this.font, name, centerX - 150, rowY + 1, nameColor);
            graphics.drawString(this.font, coords, centerX - 150, rowY + 11, 0xFFAAAAAA);
        }

        int maxPage = Math.max(0, (waypoints.size() - 1) / ROWS_PER_PAGE);
        if (maxPage > 0) {
            graphics.drawCenteredString(this.font,
                    Component.literal((page + 1) + " / " + (maxPage + 1)),
                    centerX,
                    listTop + ROWS_PER_PAGE * 27 + 8,
                    0xFFCCCCCC);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
