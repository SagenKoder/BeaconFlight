/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/28/18 1:16 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeaconFlightFileConfiguration extends BeaconFlightConfiguration {

    private static BeaconFlightFileConfiguration beaconFlightFileConfiguration;

    public static void setup(File dataFolder) {
        if(beaconFlightFileConfiguration != null)
            throw new IllegalStateException("BeaconFlightManager has already been initialized!");
        beaconFlightFileConfiguration = new BeaconFlightFileConfiguration(dataFolder);
    }

    public static BeaconFlightFileConfiguration get() {
        if (beaconFlightFileConfiguration == null)
            throw new IllegalStateException("BeaconFlightFileConfiguration has not yet been initialized!");
        return beaconFlightFileConfiguration;
    }

    private File dataFolder;

    private BeaconFlightFileConfiguration(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public List<BeaconFlightPath> loadAll() {
        File beaconFlightFile = new File(dataFolder, "beaconFlights.txt");
        Path path = beaconFlightFile.toPath();
        try {
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            List<BeaconFlightPath> beaconFlightPaths = new ArrayList<>();
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                BeaconFlightPath beaconPath = BeaconFlightPath.deserialize(line);
                beaconFlightPaths.add(beaconPath);
            }
            return beaconFlightPaths;
        } catch (IOException e) {
            e.printStackTrace();
        }
        // empty list
        return new ArrayList<>();
    }

    public void saveAll(List<BeaconFlightPath> paths) {
        List<String> lines = paths.stream().map(BeaconFlightPath::serialize).collect(Collectors.toList());
        File beaconFlightFile = new File(dataFolder, "beaconFlights.txt");
        Path path = beaconFlightFile.toPath();
        try {
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
