package com.aim.coltonjgriswold.pg.utilities;

import com.aim.coltonjgriswold.api.LaunchedProjectileData;
import com.aim.coltonjgriswold.api.ParticleProjectiles;
import com.aim.coltonjgriswold.pg.ParticleGrenades;
import com.aim.coltonjgriswold.pg.utilities.data.PGDataUtil;
import com.aim.coltonjgriswold.pg.utilities.data.PGKeys;
import com.aim.coltonjgriswold.pg.utilities.random.WeightedChance;
import com.aim.coltonjgriswold.pg.utilities.worldguard.Utils;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.IntStream;

public class GrenadeUtils {

    public static void giveRecipes(Player player) {
        Grenade.values().forEach((k, v) -> {
            player.undiscoverRecipe(k);
            if (player.hasPermission(v.getUsePerm()) || player.isOp())
                player.discoverRecipe(k);
        });
    }

    public static void giveRecipes() {
        Bukkit.getOnlinePlayers().forEach(GrenadeUtils::giveRecipes);
    }

    public static void spawnGrenade(Location location, NamespacedKey key, LivingEntity entity, Vector velocity) {

        final Item i = location.getWorld().dropItem(location, new ItemStack(Material.TNT));
        i.setVelocity(velocity);

        final Grenade g = Grenade.getByKey(key);

        if (g == null)
            return;

        location.getWorld().playSound(i.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
        i.setPickupDelay(g.getFuse() + 20);
        i.setMetadata("ParticleGrenade", new FixedMetadataValue(ParticleGrenades.instance(), true));
        if (g.isSmokeEnabled()) {
            new BukkitRunnable() {
                Color color = g.getSmokeColor();

                @Override
                public void run() {
                    if (!i.isValid()) {
                        cancel();
                        return;
                    }
                    Particle p = VersionUtils.isAtLeast("1.20.5") ? Particle.valueOf("DUST") : Particle.valueOf("REDSTONE");
                    i.getWorld().spawnParticle(p, i.getLocation().add(0, 0.5, 0), 0, 0, 0, 0, new Particle.DustOptions(color, 0.75f));
                }

            }.runTaskTimer(ParticleGrenades.instance(), 0, 2);
        }
        new BukkitRunnable() {

            private long time = g.getFuse();
            private double dist = g.getDistance();
            private double square = dist * dist;

            @Override
            public void run() {
                if (!i.isValid()) {
                    cancel();
                    i.remove();
                    return;
                }
                if (Utils.worldGuardLoaded()) {
                    boolean flag = !Utils.flag(entity, entity.getLocation(), Flags.POTION_SPLASH, Flags.TNT);
                    if (flag) {
                        i.remove();
                        cancel();
                        if (entity instanceof Player player)
                            ChatUtils.response(player, "&cThat area is protected!");
                        return;
                    }
                }
                if (g.getProximity()) {
                    List<Entity> near = i.getNearbyEntities(square, square, square);
                    if (!near.isEmpty()) {
                        if (near.stream().filter(e -> e instanceof LivingEntity && e.getUniqueId() != i.getThrower()).anyMatch(e -> i.getWorld() == e.getWorld() && e.getLocation().toVector().isInSphere(i.getLocation().toVector(), dist))) {
                            explode();
                            return;
                        }
                    }
                }
                if (g.getImpact()) {
                    Vector v = i.getVelocity();
                    if (!i.isOnGround() && v.getX() == 0 || v.getY() == 0 || v.getZ() == 0) {
                        explode();
                        return;
                    }
                }
                if (time-- <= 0)
                    explode();
            }

            private void explode() {
                GrenadeUtils.explode(g, i.getLocation(), entity);
                i.remove();
                cancel();
            }

        }.runTaskTimer(ParticleGrenades.instance(), 0, 1);
    }

    public static void spawnGrenade(Location location, NamespacedKey key, LivingEntity entity) {
        spawnGrenade(location, key, entity, new Vector());
    }

    public static void spawnGrenade(Location location, NamespacedKey key, Vector velocity) {
        spawnGrenade(location, key, null, velocity);
    }

    public static void spawnGrenade(Location location, NamespacedKey key) {
        spawnGrenade(location, key, null, new Vector());
    }

    private static double G = 9.8 / 3.0;

    public static void explode(Grenade g, Location location, LivingEntity entity) {
        Location where = location.clone();
        where.getWorld().playSound(where, g.getSound(), g.getVolume(), g.getPitch());
        WeightedChance<Particle> particle = g.getParticles();
        List<LaunchedProjectileData> data = new ArrayList<>();
        IntStream.range(0, g.getFragments()).forEach(n -> {
            GrenadeEffect grenade = new GrenadeEffect(g);
            grenade.setParticleType(particle.select());
            Material material = where.clone().add(0.5, 0.5, 0.5).getBlock().getType();
            if (material.equals(Material.AIR))
                grenade.setData(material);
            Location loc = where.clone().setDirection(Vector.getRandom().subtract(Vector.getRandom()).multiply(2.0));
            grenade.setVelocity(((g.getVelocity() / 5.0) + (g.getVelocity() - (g.getVelocity() / 5.0))) * new Random().nextDouble());
            grenade.setLifespan(((g.getLifespan() / 5.0) + (g.getLifespan() - (g.getLifespan() / 5.0))) * new Random().nextDouble());
            data.add(new LaunchedProjectileData(grenade, entity, where.getWorld(), loc, Vector.getRandom().subtract(Vector.getRandom()).multiply(2.0), Vector.getRandom().subtract(Vector.getRandom()).multiply(2.0), true));
        });
        /*for (int n = 0; n < g.getFragments(); n++) {
            GrenadeEffect grenade = new WorldGuardGrenadeEffect(g);
            grenade.setParticleType(particle.select());
            Material material = where.clone().subtract(0, 0.5, 0).getBlock().getType();
            if (material.equals(Material.AIR))
                grenade.setData(material);
            Location loc = where.clone().setDirection(Vector.getRandom().subtract(Vector.getRandom()).multiply(2.0));
            grenade.setVelocity(((g.getVelocity() / 5.0) + (g.getVelocity() - (g.getVelocity() / 5.0))) * new Random().nextDouble());
            grenade.setLifespan(((g.getLifespan() / 5.0) + (g.getLifespan() - (g.getLifespan() / 5.0))) * new Random().nextDouble());
            data.add(new LaunchedProjectileData(grenade, entity, where.getWorld(), loc, Vector.getRandom().subtract(Vector.getRandom()).multiply(2.0), Vector.getRandom().subtract(Vector.getRandom()).multiply(2.0), true));
        }*/
        ParticleProjectiles.launchMultiple(data);
    }

    public static void explode(Grenade g, Location location) {
        explode(g, location, null);
    }

    public static boolean isGrenade(ItemStack item) {
        return isGrenade(item.getItemMeta());
    }

    public static <T extends PersistentDataHolder> boolean isGrenade(T t) {
        PersistentDataContainer data = t.getPersistentDataContainer();
        return data.has(new NamespacedKey(ParticleGrenades.instance(), "pgdata"), PersistentDataType.STRING);
    }

    public static Grenade getGrenade(ItemStack item) {
        return getGrenade(item.getItemMeta());
    }

    public static <T extends PersistentDataHolder> Grenade getGrenade(T t) {
        if (isGrenade(t)) {
            String name = getName(t);
            if (!Grenade.contains(name))
                return null;
            return Grenade.getByKey(name);
        }
        return null;
    }

    public static String getName(ItemStack item) {
        return PGDataUtil.getData(item);
    }

    private static <T extends PersistentDataHolder> String getName(T t) {
        PersistentDataContainer data = t.getPersistentDataContainer();

        if (!data.has(PGKeys.DATA, PersistentDataType.STRING))
            return "";
        return data.get(PGKeys.DATA, PersistentDataType.STRING);
    }

    public static ItemStack createGrenade(Grenade grenade) {
        ItemStack item = new ItemStack(Material.TNT);
        ItemMeta meta = item.getItemMeta();
        PGDataUtil.addData(meta, grenade.getKey().getKey().toLowerCase());
        meta.setDisplayName(ChatUtils.color(grenade.getName()));
        List<String> lore = new ArrayList<>();
        grenade.getDescription().forEach(s -> lore.add(ChatUtils.color(s)));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createGrenade(String key) {
        return createGrenade(Grenade.getByKey(key));
    }
}
