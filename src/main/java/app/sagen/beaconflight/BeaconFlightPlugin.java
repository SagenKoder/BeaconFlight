package app.sagen.beaconflight;

import org.bukkit.plugin.java.JavaPlugin;

public class BeaconFlightPlugin extends JavaPlugin {

    @Override
    public void onDisable() {
        BeaconFlightManager.destroy();
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        BeaconFlightFileConfiguration.setup(getDataFolder());
        BeaconFlightManager.setup(this);

        new BeaconFlightCommand();
    }
}
