/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/28/18 4:54 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.*;

public class BeaconFlightPath {

    private static Color[] cleanColors = new Color[]{
            Color.fromBGR(255, 255, 255),
            Color.fromBGR(1, 1, 255),
            Color.fromBGR(255, 1, 1),
            Color.fromBGR(1, 255, 1),
            Color.fromBGR(255, 255, 1),
            Color.fromBGR(255, 1, 255),
            Color.fromBGR(1, 255, 255),
            Color.fromBGR(1, 1, 1)
    };

    private static Random random = new Random(System.currentTimeMillis());
    private String name;
    private String worldName;
    private CubicSpline3D<Vector> path;
    private float travelDurationInTicks;
    private Vector p_end;
    private Vector p_start;
    private HashMap<UUID, PlayerPathMover> playerMovers = new HashMap<>();
    private String title_start;
    private String title_end;
    private int ticksPassed = 0;

    BeaconFlightPath(String name, String worldName, List<Vector> path, float speedMultiplier, String title_start, String title_end) {
        this.name = name.toLowerCase();
        this.path = new CubicSpline3D<>(Vector.class, Double.TYPE);
        for (Vector v : path) {
            this.path.addPoint(v);
        }
        this.path.calcSpline();
        this.p_end = path.get(0);
        this.p_start = path.get(path.size() - 1);
        this.worldName = worldName;
        this.travelDurationInTicks = (int) (this.path.getHeuristicDistance() * speedMultiplier);

        this.title_start = title_start;
        this.title_end = title_end;
    }

    static String serialize(BeaconFlightPath path) {
        StringBuilder sb = new StringBuilder();

        sb.append(path.name).append("##")
                .append(path.worldName).append("##")
                .append((int) path.travelDurationInTicks).append("##")
                .append(path.title_start).append("##")
                .append(path.title_end).append("##");

        boolean first = true;
        for (Vector v : path.getPath().getPoints()) {
            if (!first) sb.append("@");
            sb.append(v.getX()).append(":").append(v.getY()).append(":").append(v.getZ());
            first = false;
        }
        return sb.toString();
    }

    static BeaconFlightPath deserialize(String path) {
        try {
            String[] split = path.split("##");
            String name = split[0];
            String worldname = split[1];
            float travelDurationInTicks = Integer.parseInt(split[2]);
            String title_start = split[3];
            String title_end = split[4];

            String pointsString = split[5];
            String[] pointsStringSplit = pointsString.split("@");
            LinkedList<Vector> points = new LinkedList<>();
            for (String point : pointsStringSplit) {
                String[] pointSegments = point.split(":");
                points.addLast(new Vector(
                        Double.parseDouble(pointSegments[0]),
                        Double.parseDouble(pointSegments[1]),
                        Double.parseDouble(pointSegments[2])
                ));
            }
            BeaconFlightPath beaconFlightPath = new BeaconFlightPath(name, worldname, points, 1.0f, title_start, title_end);
            beaconFlightPath.setTravelDurationInTicks(travelDurationInTicks);
            return beaconFlightPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void addPlayer(Player player, boolean direction) {
        if (Bukkit.getWorld(worldName) == null) {
            System.out.println("Cannot find the world!");
            player.sendMessage("§cError occured. Could not find the world " + worldName + "!");
            return;
        }

        // ignore players already added
        if (playerMovers.containsKey(player.getUniqueId())) return;

        player.setSneaking(false);
        playerMovers.put(player.getUniqueId(), new PlayerPathMover(player, worldName, path, travelDurationInTicks, direction));
    }

    public void playerQuit(Player player) {
        removePlayer(player.getUniqueId());
    }

    public void destroy() {
        for (Player o : Bukkit.getOnlinePlayers()) {
            playerQuit(o);
        }
    }

    private void removePlayer(UUID... uuids) {
        for (UUID uuid : uuids) {
            if (!playerMovers.containsKey(uuid)) continue;
            playerMovers.get(uuid).destroy();
            playerMovers.remove(uuid);
        }
    }

    boolean handleClick(Player player, Vector vector) {
        System.out.println("handleClick - " + name);
        if(p_start.distanceSquared(vector) < Math.pow(1.5, 2)) {
            System.out.println("p_start");
            addPlayer(player, true);
            return true;
        }
        if(p_end.distanceSquared(vector) < Math.pow(1.5, 2)) {
            System.out.println("p_end");
            addPlayer(player, false);
            return true;
        }
        return false;
    }

    void update() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            System.out.println("Cannot find world " + worldName + "!!");
            return;
        }
        ArrayList<UUID> remove = new ArrayList<>();
        for (Map.Entry<UUID, PlayerPathMover> e : playerMovers.entrySet()) {
            if(!e.getValue().update()) remove.add(e.getKey());
        }
        for (UUID u : remove) removePlayer(u);

        ticksPassed++;

        if (ticksPassed % 2 == 0)
            renderParticles();
    }

    private void renderParticles() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        for (float i = 0.00f; i <= 1; i += (1.0f / getTravelDurationInTicks())) {
            if (random.nextFloat() > 0.95f) {
                Vector point = path.getPoint(i);
                world.spawnParticle(Particle.REDSTONE,
                        point.getX() - 0.25f + Math.random() * 0.5f,
                        point.getY() - 0.25f + Math.random() * 0.5f,
                        point.getZ() - 0.25f + Math.random() * 0.5f,
                        0, new Particle.DustOptions(cleanColors[0], 1));
            }
        }
    }

    public String getName() {
        return name;
    }

    String getWorldName() {
        return worldName;
    }

    public CubicSpline3D<Vector> getPath() {
        return path;
    }

    public float getTravelDurationInTicks() {
        return travelDurationInTicks;
    }

    private void setTravelDurationInTicks(float travelDurationInTicks) {
        this.travelDurationInTicks = travelDurationInTicks;
    }

    public Vector getEndPoint() {
        return p_end;
    }

    public Vector getStartPoint() {
        return p_start;
    }

    HashMap<UUID, PlayerPathMover> getPlayersOnPath() {
        return playerMovers;
    }
}
