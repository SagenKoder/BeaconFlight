/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/26/18 2:40 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight.config;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Date;

public interface GeneralConfiguration extends ConfigurationSection {

    float getFloat(String path);

    Location getLocation(String path, String worldName);

    Location getLocation(String path);

    void setLocation(String path, Location value);

    void setDate(String path, Date date);

    Date getDate(String path);
}
