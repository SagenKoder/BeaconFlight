/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/26/18 2:40 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PathCreator {

    private String name;
    private String title_start;
    private String title_end;
    private String world;
    private LinkedList<Vector> points;

    public PathCreator(String name, String world) {
        this.name = name.toLowerCase();
        this.world = world.toLowerCase();
        this.points = new LinkedList<>();
    }

    public String getTitleStart() {
        return title_start;
    }

    public void setTitleStart(String titleStart) {
        this.title_start = titleStart;
    }

    public String getTitleEnd() {
        return title_end;
    }

    public void setTitleEnd(String titleEnd) {
        this.title_end = titleEnd;
    }

    public int getNumberOfPoints() {
        return points.size();
    }

    public LinkedList<Vector> getPoints() {
        return points;
    }

    public void addFirst(Vector point) {
        points.addFirst(point.clone());
    }

    public void addLast(Vector point) {
        points.addLast(point.clone());
    }

    public void removeFirst() {
        points.removeFirst();
    }

    public void removeLast() {
        points.removeLast();
    }

    public BeaconFlightPath build() {
        if (getNumberOfPoints() < 3) throw new IllegalStateException("Cannot create a spline with less than 3 points!");

        BeaconFlightPath mover = new BeaconFlightPath(name, world, points, 0.1f,
                (title_start == null ? name + " start" : title_start),
                (title_end == null ? name + " end" : title_end));
        return mover;
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }
}
