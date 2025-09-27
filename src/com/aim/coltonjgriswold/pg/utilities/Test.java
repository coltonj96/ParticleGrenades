package com.aim.coltonjgriswold.pg.utilities;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Test {

    public static boolean hasLineOfSight(Player player, Location location) {
        Location eye = player.getEyeLocation();
        Vector a = eye.toVector();
        Vector b = location.toVector();
        Vector dir = eye.getDirection().normalize();
        double dist = a.distance(b);

        Vector blockToEye = a.clone().add(dir.clone().multiply(dist));
        double distBlockToEye = blockToEye.distance(b);
        double d = dist >= player.getClientViewDistance() ? player.getClientViewDistance() : dist;
        RayTraceResult trace = player.getWorld().rayTrace(eye, eye.getDirection(), d, FluidCollisionMode.ALWAYS, true, 1.0, ent -> ent instanceof LivingEntity);
        return distBlockToEye <= 0.5;
    }

    public static boolean isLookingAt(Player player, Location location) {
        return isLookingAt(player, location, 0.5);
    }

    public static boolean isLookingAt(Player player, Location location, double radius) {
        Location eye = player.getEyeLocation();
        Vector a = eye.toVector();
        Vector b = location.toVector();
        Vector dir = eye.getDirection().normalize();
        double dist = a.distance(b);

        Vector blockToEye = a.clone().add(dir.clone().multiply(dist));
        double distBlockToEye = blockToEye.distance(b);
        return distBlockToEye <= radius;
    }

    public static boolean isInRadius(Player player, Location location, double distance) {
        return player.getLocation().distance(location) <= distance;
    }
}
