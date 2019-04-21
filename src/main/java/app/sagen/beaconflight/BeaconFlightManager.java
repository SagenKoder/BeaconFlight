/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/28/18 4:46 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BeaconFlightManager {

    private static BeaconFlightManager beaconFlightManager;

    public static void setup(JavaPlugin plugin) {
        if(beaconFlightManager != null)
            throw new IllegalStateException("BeaconFlightManager has already been initialized!");
        beaconFlightManager = new BeaconFlightManager(plugin);
    }

    public static BeaconFlightManager get() {
        if(beaconFlightManager == null)
            throw new IllegalStateException("BeaconFlightManager has not yet been initialized!");
        return beaconFlightManager;
    }

    public static void destroy() {
        for(BeaconFlightPath beaconFlightPath : beaconFlightManager.getAllBeaconFlightPaths()) {
            beaconFlightPath.destroy();
        }
        beaconFlightManager = null;
    }

    public HashMap<String, List<BeaconFlightPath>> pathsInWorld = new HashMap<>();
    private JavaPlugin plugin;

    private BeaconFlightManager(JavaPlugin plugin) {
        this.plugin = plugin;
        List<BeaconFlightPath> paths = BeaconFlightFileConfiguration.get().loadAll();
        for (BeaconFlightPath path : paths) {
            addBeaconFlightToWorldAndDontSave(path.getWorldName(), path);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (List<BeaconFlightPath> paths : pathsInWorld.values()) {
                    for (BeaconFlightPath path : paths) {
                        path.update();
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 1, 1);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                for(BeaconFlightPath beaconFlightPath : getBeaconFlightsInWorld(e.getPlayer().getWorld().getName())) {
                    beaconFlightPath.playerQuit(e.getPlayer());
                }
            }
            @EventHandler
            public void onClick(PlayerInteractEvent e) {
                if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
                if(!e.getClickedBlock().getType().equals(Material.BEACON)) return;
                for(BeaconFlightPath beaconFlightPath : getBeaconFlightsInWorld(e.getPlayer().getWorld().getName())) {
                    if(beaconFlightPath.handleClick(e.getPlayer(), e.getClickedBlock().getLocation().toVector())) {
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        }, plugin);
    }

    JavaPlugin getPlugin() {
        return plugin;
    }

    public List<BeaconFlightPath> getBeaconFlightsInWorld(String world) {
        return pathsInWorld.getOrDefault(world.toLowerCase(), new ArrayList<>());
    }

    public BeaconFlightPath getBeaconFlightPath(String world, String name) {
        for (BeaconFlightPath path : getBeaconFlightsInWorld(world)) {
            if (path.getName().equalsIgnoreCase(name)) return path;
        }
        return null;
    }

    public void addBeaconFlightToWorld(String world, BeaconFlightPath path) {
        if (!pathsInWorld.containsKey(world.toLowerCase()))
            pathsInWorld.put(world.toLowerCase(), new ArrayList<>());

        pathsInWorld.get(world.toLowerCase()).add(path);

        BeaconFlightFileConfiguration.get().saveAll(getAllBeaconFlightPaths());
    }

    private void addBeaconFlightToWorldAndDontSave(String world, BeaconFlightPath path) {
        if (!pathsInWorld.containsKey(world.toLowerCase()))
            pathsInWorld.put(world.toLowerCase(), new ArrayList<>());

        pathsInWorld.get(world.toLowerCase()).add(path);
    }

    public ArrayList<BeaconFlightPath> getAllBeaconFlightPaths() {
        ArrayList<BeaconFlightPath> allFlightPaths = new ArrayList<>();

        for (List<BeaconFlightPath> paths : pathsInWorld.values()) {
            allFlightPaths.addAll(paths);
        }

        return allFlightPaths;
    }

    public void remove(BeaconFlightPath path) {
        getBeaconFlightsInWorld(path.getWorldName()).remove(path);
        BeaconFlightFileConfiguration.get().saveAll(getAllBeaconFlightPaths());
    }

}
