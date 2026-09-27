package dev.kipd0.portalwaypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class WaypointStore {
    private static final Logger LOGGER = LoggerFactory.getLogger("portalwaypoints");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("portalwaypoints.json");

    private static Data data = new Data();

    private WaypointStore() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            data = new Data();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            data = loaded == null ? new Data() : loaded;
            if (data.waypoints == null) {
                data.waypoints = new ArrayList<>();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load {}", CONFIG_PATH, e);
            data = new Data();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save {}", CONFIG_PATH, e);
        }
    }

    public static List<Waypoint> getWaypoints() {
        return Collections.unmodifiableList(data.waypoints);
    }

    public static Waypoint add(String name, int overworldX, int overworldZ) {
        Waypoint waypoint = new Waypoint(UUID.randomUUID().toString(), name, overworldX, overworldZ);
        data.waypoints.add(waypoint);
        save();
        return waypoint;
    }

    public static void delete(String id) {
        data.waypoints.removeIf(waypoint -> waypoint.id.equals(id));
        if (id != null && id.equals(data.activeId)) {
            data.activeId = null;
        }
        save();
    }

    public static void setActive(String id) {
        data.activeId = id;
        save();
    }

    public static void clearActive() {
        data.activeId = null;
        save();
    }

    public static Waypoint getActive() {
        if (data.activeId == null) {
            return null;
        }

        for (Waypoint waypoint : data.waypoints) {
            if (data.activeId.equals(waypoint.id)) {
                return waypoint;
            }
        }

        return null;
    }

    public static boolean isActive(String id) {
        return id != null && id.equals(data.activeId);
    }

    private static final class Data {
        private List<Waypoint> waypoints = new ArrayList<>();
        private String activeId;
    }
}
