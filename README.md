# Portal Waypoints

Client-side Fabric 1.21.11 waypoint helper for Nether travel.

## Features

- Press **U** to open the waypoint menu (changeable in Minecraft Controls).
- Save multiple named waypoints.
- Only one waypoint can be loaded/active at a time.
- Enter Overworld X/Z and the Nether X/Z updates automatically.
- Enter Nether X/Z and the Overworld X/Z updates automatically.
- Uses the normal 8:1 Overworld/Nether coordinate conversion.
- Shows an on-screen direction arrow, waypoint name, target coordinates, and distance.
- Automatically shows the Overworld target while in the Overworld and the converted Nether target while in the Nether.
- Shows **BUILD PORTAL HERE** when within 4 blocks of the Nether target.
- Saved waypoints are stored in `config/portalwaypoints.json`.
- Client-side only; the server does not need the mod.

## Build on GitHub

Create the files in this repository using the same paths as this project. The workflow must be located at:

`.github/workflows/build.yml`

Then push/commit the files. Open **Actions > Build**, run the workflow if needed, and download the `portal-waypoints` artifact from the completed run.

The compiled mod JAR will be inside the artifact. Put it in your Fabric 1.21.11 `mods` folder together with Fabric API.

## Toolchain

- Minecraft 1.21.11
- Fabric Loader 0.18.4
- Fabric API 0.141.4+1.21.11
- Java 21
