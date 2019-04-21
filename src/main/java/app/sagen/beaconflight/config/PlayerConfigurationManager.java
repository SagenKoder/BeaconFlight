/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/26/18 2:40 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight.config;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class PlayerConfigurationManager {

    protected final Map<UUID, PlayerConfiguration> playerConfigurations = Collections.synchronizedMap(new HashMap<>());

    protected JavaPlugin plugin;

    public PlayerConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerConfiguration pc : PlayerConfigurationManager.this.getAllConfigurations()) {
                    if (Bukkit.getPlayer(pc.getManagerPlayerUUID()) == null) { // Player is offline
                        pc.discard(true); // save to disk and discard from memory
                    }
                }
            }
        }.runTaskTimer(plugin, 20 * 60 * 5, 20 * 60 * 5); // 5m, 5m
    }

    public int configurationsCreated() {
        synchronized (this.playerConfigurations) {
            return this.playerConfigurations.size();
        }
    }

    public void discard(final PlayerConfiguration pc) {
        synchronized (this.playerConfigurations) {
            this.playerConfigurations.remove(pc.getManagerPlayerUUID());
        }
    }

    public synchronized Collection<PlayerConfiguration> getAllConfigurations() {
        synchronized (this.playerConfigurations) {
            return Collections.synchronizedCollection(this.playerConfigurations.values());
        }
    }

    public PlayerConfiguration getConfiguration(final UUID u) {
        synchronized (this.playerConfigurations) {
            if (this.playerConfigurations.containsKey(u)) return this.playerConfigurations.get(u);
            final PlayerConfiguration pcm = new FilePlayerConfiguration(this, u);
            this.playerConfigurations.put(u, pcm);
            return pcm;
        }
    }

    public PlayerConfiguration getConfiguration(final OfflinePlayer p) {
        return this.getConfiguration(p.getUniqueId());
    }

    public boolean isConfigurationCreated(final UUID u) {
        synchronized (this.playerConfigurations) {
            return this.playerConfigurations.containsKey(u);
        }
    }

    public boolean isConfigurationCreated(final OfflinePlayer p) {
        return this.isConfigurationCreated(p.getUniqueId());
    }

    public void removeAllConfigurations() {
        final Collection<PlayerConfiguration> oldConfs = new ArrayList<>();
        synchronized (this.playerConfigurations) {
            oldConfs.addAll(this.playerConfigurations.values());
            for (final PlayerConfiguration pcm : oldConfs)
                pcm.discard(false);
        }
    }

    public void saveAllConfigurations() {
        System.out.println("Saving all player configurations!");
        synchronized (this.playerConfigurations) {
            for (final PlayerConfiguration pcm : this.playerConfigurations.values())
                pcm.forceSave();
        }
    }

    public int getTotalPlayerDataFiles() {
        File f = new File(plugin.getDataFolder() + File.separator + "userdata" + File.separator);
        try {
            return (int) (Files.list(Paths.get(f.getAbsolutePath())).count() + getAllConfigurations().stream().filter(o -> !o.exists()).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String[] getPlayerDataFilenames() {
        saveAllConfigurations();
        File f = new File(plugin.getDataFolder() + File.separator + "userdata" + File.separator);
        return f.list();
    }

}
