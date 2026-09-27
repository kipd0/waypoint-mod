package dev.kipd0.portalwaypoints;

public final class Waypoint {
    public String id;
    public String name;
    public int overworldX;
    public int overworldZ;

    public Waypoint() {
    }

    public Waypoint(String id, String name, int overworldX, int overworldZ) {
        this.id = id;
        this.name = name;
        this.overworldX = overworldX;
        this.overworldZ = overworldZ;
    }

    public int netherX() {
        return Math.floorDiv(overworldX, 8);
    }

    public int netherZ() {
        return Math.floorDiv(overworldZ, 8);
    }
}
