/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/26/18 2:40 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ConfigurationManager {

    private final Map<String, Configuration> confs = new HashMap<String, Configuration>();
    JavaPlugin plugin;

    public ConfigurationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int configurationsCreated() {
        synchronized (this.confs) {
            return this.confs.size();
        }
    }

    public Collection<Configuration> getAllConfigurations() {
        synchronized (this.confs) {
            return Collections.synchronizedCollection(this.confs.values());
        }
    }

    public Configuration getConfiguration(String s) {
        synchronized (this.confs) {
            if (this.confs.containsKey(s)) return this.confs.get(s);
            final Configuration cm = new Configuration(this, s);
            this.confs.put(s, cm);
            return cm;
        }
    }

    public boolean isConfigurationCreated(String s) {
        synchronized (this.confs) {
            return this.confs.containsKey(s);
        }
    }

    public void removeAllConfigurations() {
        final Collection<Configuration> oldConfs = new ArrayList<>();
        oldConfs.addAll(this.confs.values());
        synchronized (this.confs) {
            for (final Configuration cm : oldConfs)
                discardConfiguration(cm, false);
        }
    }

    public void saveAllConfigurations() {
        System.out.println("Saving all configurations!");
        synchronized (this.confs) {
            for (final Configuration cm : this.confs.values())
                cm.forceSave();
        }
    }

    public void discardConfiguration(Configuration configuration, boolean save) {
        synchronized (this.confs) {
            if (save) configuration.forceSave();
            this.confs.remove(configuration.name);
        }
    }
}
