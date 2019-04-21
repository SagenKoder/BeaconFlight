package app.sagen.beaconflight;

import java.util.List;

public abstract class BeaconFlightConfiguration {

    public abstract List<BeaconFlightPath> loadAll();

    public abstract void saveAll(List<BeaconFlightPath> paths);

}
