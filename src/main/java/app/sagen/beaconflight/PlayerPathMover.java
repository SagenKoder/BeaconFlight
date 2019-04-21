package app.sagen.beaconflight;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

@Getter(AccessLevel.PACKAGE)
public class PlayerPathMover {

    private Player player;
    private Entity entity;
    private String world;
    private CubicSpline3D<Vector> path;
    private float travelDurationInTicks;
    private boolean direction;
    private float time;

    PlayerPathMover(Player player, String world, CubicSpline3D<Vector> path, float travelDurationInTicks, boolean direction) {
        this.player = player;
        this.world = world;
        this.path = path;
        this.travelDurationInTicks = travelDurationInTicks;
        this.direction = direction;
        this.time = direction ? 0 : travelDurationInTicks;

        Vector start = direction ? path.getPoints().lastElement() : path.getPoints().firstElement();
        Location location = new Location(
                Bukkit.getWorld(world),
                start.getX(),
                start.getY(),
                start.getZ(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch());
        player.teleport(location);
        entity = createArmourStand(location, player);
    }

    void destroy() {
        Vector end = direction ? path.getPoints().firstElement() : path.getPoints().lastElement();
        if(entity != null) {
            entity.remove();
            entity = null;
        }

        if(player != null) {
            Bukkit.getScheduler().runTaskLater(BeaconFlightManager.get().getPlugin(), () -> {
                player.teleport(new Location(
                        Bukkit.getWorld(world),
                        end.getX(),
                        end.getY(),
                        end.getZ(),
                        player.getLocation().getYaw(),
                        player.getLocation().getPitch()));
            },1);
            player.setGravity(true);
        }
        path = null;
        world = null;
    }

    boolean update() {
        Vector end = direction ? path.getPoints().lastElement() : path.getPoints().firstElement();
        time = direction ? time + 1 : time - 1;
        if (player == null || !player.getWorld().getName().equalsIgnoreCase(world) || time >= travelDurationInTicks || time < 0) {
            return false;
        }
        Vector point = (time == travelDurationInTicks) ? end : path.getPoint(1 - (time / travelDurationInTicks));
        if (point == null) return true;
        if (player.getVehicle() == null) {
            entity.addPassenger(player);
        }
        moveEntity(entity, point.getX(), point.getY() + 0.3f, point.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        return true;
    }

    /* static */

    @Getter(AccessLevel.NONE)
    private static Method[] moveEntityReflection = null;

    private static Method[] getMoveEntityReflection() {
        if (moveEntityReflection == null) {
            try {
                Method getHandle = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
                moveEntityReflection = new Method[]{
                        getHandle,
                        getHandle.getReturnType().getDeclaredMethod("setPositionRotation",
                                double.class,
                                double.class,
                                double.class,
                                float.class,
                                float.class)};
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return moveEntityReflection;
    }

    private static void moveEntity(Entity entity, double x, double y, double z) {
        moveEntity(entity, x, y, z, entity.getLocation().getYaw(), entity.getLocation().getPitch());
    }

    private static void moveEntity(Entity entity, double x, double y, double z, float yaw, float pitch) {
        try {
            getMoveEntityReflection()[1].invoke(moveEntityReflection[0].invoke(entity), x, y, z, yaw, pitch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Entity createArmourStand(Location location, Player player) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        entity.addPassenger(player);
        entity.setInvulnerable(true);
        entity.setAI(false);
        entity.setGliding(false);
        entity.setGravity(false);

        ArmorStand as = (ArmorStand) entity;
        as.setVisible(false);
        as.setSmall(true);
        as.setMarker(true);

        return entity;
    }
}
