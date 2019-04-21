package app.sagen.beaconflight;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BeaconFlightCommand extends AutoCommand {

    HashMap<UUID, PathCreator> pathCreators = new HashMap<>();

    public BeaconFlightCommand() {
        super("beaconflight");
        options.add(REQUIRE_PLAYER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        if (args.length == 0) {
            sendHelp(p);
        }

        // list paths
        else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            List<BeaconFlightPath> pathsInWorld = BeaconFlightManager.get().getBeaconFlightsInWorld(p.getWorld().getName());
            if (pathsInWorld.size() == 0) {
                p.sendMessage("No beaconflights available");
                return true;
            }
            p.sendMessage("** BeaconFlights **");
            for (BeaconFlightPath path : pathsInWorld) {
                p.sendMessage(path.getName() + " " + path.getPath().getPoints().size());
            }
            p.sendMessage("-");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            BeaconFlightPath path = BeaconFlightManager.get().getBeaconFlightPath(p.getWorld().getName(), args[1]);
            if (path == null) {
                p.sendMessage("command.beaconflight.not_exists");
                return true;
            }
            p.sendMessage("command.beaconflight.list_points.title");
            for (Vector vector : path.getPath().getPoints()) {
                p.sendMessage("command.beaconflight.list_points.node");
            }
            p.sendMessage("command.beaconflight.list_points.foot");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            BeaconFlightPath path = BeaconFlightManager.get().getBeaconFlightPath(p.getWorld().getName(), args[1]);
            if (path == null) {
                p.sendMessage("command.beaconflight.not_exists");
                return true;
            }
            path.destroy();
            BeaconFlightManager.get().remove(path);
            p.sendMessage("command.beaconflight.removed");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("ride")) {
            BeaconFlightPath path = BeaconFlightManager.get().getBeaconFlightPath(p.getWorld().getName(), args[1]);
            if (path == null) {
                p.sendMessage("command.beaconflight.not_exists");
                return true;
            }
            path.addPlayer(p, true);
            p.sendMessage("command.beaconflight.started_flight");
            p.sendMessage("§3Started the BeaconFlight <3");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            PathCreator pathCreator = new PathCreator(args[1], p.getWorld().getName());
            pathCreators.put(p.getUniqueId(), pathCreator);
            p.sendMessage("command.beaconflight.created_path");
            p.performCommand("beaconflight addpoint");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("addpoint")) {
            PathCreator pathCreator = pathCreators.get(p.getUniqueId());
            if (pathCreator == null) {
                p.sendMessage("§cYou do not have any pathcreator! Make a new one with /beaconflight create <name>");
                return true;
            }
            if (!p.getWorld().getName().equalsIgnoreCase(pathCreator.getWorld())) {
                p.sendMessage("command.beaconflight.addpoint.not_same_world");
                return true;
            }

            pathCreator.addFirst(p.getLocation().toVector());
            p.sendMessage("command.beaconflight.addpoint.added_point");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("finish")) {
            PathCreator pathCreator = pathCreators.get(p.getUniqueId());
            if (pathCreator == null) {
                p.sendMessage("§cYou do not have any pathcreator! Make a new one with /beaconflight create <name>");
                return true;
            }

            if (pathCreator.getNumberOfPoints() < 3) {
                p.sendMessage("§cYou need to add at least 3 points to create a BeaconFlight!");
                return true;
            }

            BeaconFlightPath path = pathCreator.build();
            BeaconFlightManager.get().addBeaconFlightToWorld(pathCreator.getWorld(), path);
            pathCreators.remove(p.getUniqueId());

            p.sendMessage("§3Added the new BeaconFlight <3<3");
            p.sendMessage("§3The travel duration for the flight is currently " + path.getTravelDurationInTicks());
        } else if (args.length > 3 && args[0].equalsIgnoreCase("title") &&
                (args[1].equalsIgnoreCase("start") || args[1].equalsIgnoreCase("end"))) {
            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            PathCreator pathCreator = pathCreators.get(p.getUniqueId());
            if (pathCreator == null) {
                p.sendMessage("§cYou do not have any pathcreator! Make a new one with /beaconflight create <name>");
                return true;
            }

            if (args[1].equalsIgnoreCase("start")) {
                pathCreator.setTitleStart(message);
                p.sendMessage("§3Set the title on start to '" + message + "'!");
            } else {
                pathCreator.setTitleEnd(message);
                p.sendMessage("§3Set the title on start to '" + message + "'!");
            }
        } else {
            p.sendMessage("§cWrong usage!");
            sendHelp(p);
        }

        return true;
    }

    public void sendHelp(CommandSender p) {
        p.sendMessage("§3-------< BeaconFlight >-------");
        p.sendMessage("§e/beaconflight §7- Show this help");
        p.sendMessage("§e/beaconflight list §7- List every BeaconFlight in this world");
        p.sendMessage("§e/beaconflight list <name> §7- List every point on a BeaconFlight");
        p.sendMessage("§e/beaconflight remove <name> §7- Removes a BeaconPath");
        p.sendMessage("§e/beaconflight ride <name> §7- Starts the BeaconFlight for you");
        p.sendMessage("§e/beaconflight create <name> §7- Creates a BeaconFlight");
        p.sendMessage("§e/beaconflight title start §7- Sets the title of the start point");
        p.sendMessage("§e/beaconflight title end §7- Sets the title of the end point");
        p.sendMessage("§e/beaconflight addpoint §7- Sets a point");
        p.sendMessage("§e/beaconflight finish §7- Builds the new BeaconFlight");
        p.sendMessage("§3---");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String startOfAction = args[0].toLowerCase().trim();
            return Arrays.asList("list", "remove", "ride", "create", "title", "title", "addpoint", "finish").stream()
                    .filter(s -> s.toLowerCase().startsWith(startOfAction))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && Arrays.asList("list", "remove", "ride").contains(args[0].toLowerCase())) {
            String startOfAction = args[1].toLowerCase().trim();
            return BeaconFlightManager.get().getBeaconFlightsInWorld(((Player) sender).getWorld().getName()).stream()
                    .map(BeaconFlightPath::getName)
                    .filter(s -> s.startsWith(startOfAction))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("title")) {
            String startOfAction = args[1].toLowerCase().trim();
            return Arrays.asList("start", "end").stream()
                    .filter(s -> s.toLowerCase().startsWith(startOfAction))
                    .collect(Collectors.toList());
        } else {
            return Arrays.asList();
        }
    }
}
