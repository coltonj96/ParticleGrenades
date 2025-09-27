package com.aim.coltonjgriswold.pg.utilities;

import java.util.*;

import com.aim.coltonjgriswold.pg.utilities.data.PGDataUtil;
import com.aim.coltonjgriswold.pg.utilities.worldguard.Utils;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.data.Directional;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.aim.coltonjgriswold.pg.ParticleGrenades;

public class GrenadeHandler implements Listener {

    @EventHandler
    public void onGrenade(final PlayerDropItemEvent event) {
        dropGrenade(event);
    }

    @EventHandler
    public void onReload(ServerLoadEvent event) {
        if (event.getType().equals(ServerLoadEvent.LoadType.RELOAD))
            Bukkit.getOnlinePlayers().forEach(GrenadeUtils::giveRecipes);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        GrenadeUtils.giveRecipes(event.getPlayer());

        /*Location loc = new Location(player.getWorld(), 0.5, 74.5, 0.5);

        ItemDisplay dis = player.getWorld().spawn(loc.clone().subtract(0, 2, 0), ItemDisplay.class);
        dis.setItemStack(new ItemStack(Material.TNT));
        dis.setDisplayWidth(0.25f);
        dis.setDisplayHeight(0.25f);

        new BukkitRunnable() {

            @Override
            public void run() {
                if (Test.isInRadius(player, dis.getLocation(), 0.5)) {
                    GrenadeUtils.explode(Grenade.getByKey("tactical_nuke"), dis.getLocation(), player);
                    if (dis != null)
                        dis.remove();
                    cancel();
                }

            }
        }.runTaskTimer(ParticleGrenades.instance(), 0, 5);
         */
    }

    private void dropGrenade(PlayerDropItemEvent event) {
        final Item i = event.getItemDrop();

        if (!i.getItemStack().getType().equals(Material.TNT))
            return;

        if (!GrenadeUtils.isGrenade(i.getItemStack()))
            return;

        String name = GrenadeUtils.getName(i.getItemStack());

        final Grenade g = Grenade.getByKey(name);

        if (g == null)
            return;

        final Player p = event.getPlayer();
        Date date = new Date();
        long time = date.getTime() + (g.getCooldown() * 50);
        if (!p.hasMetadata("pg_" + name) || p.getMetadata("pg_" + name).isEmpty())
            p.setMetadata("pg_" + name, new FixedMetadataValue(ParticleGrenades.instance(), 0));
        if (p.isSneaking()) {
            if (!p.hasPermission(g.getUsePerm())) {
                ChatUtils.response(p, "&cYou do not have permission to use that!");
                event.setCancelled(true);
                return;
            }
            event.setCancelled(date.getTime() <= p.getMetadata("pg_" + name).get(0).asLong());
            if (event.isCancelled())
                return;
            p.setMetadata("pg_" + name, new FixedMetadataValue(ParticleGrenades.instance(), time));
            if (Utils.worldGuardLoaded())
                if (!Utils.flag(p, p.getLocation(), Flags.ITEM_DROP)) {
                    ChatUtils.response(p, "&cThis area is protected!");
                    event.setCancelled(true);
                    return;
                }
            p.getWorld().playSound(i.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
            i.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(g.getDistance()));
            i.setPickupDelay(g.getFuse() + 20);
            i.setMetadata("ParticleGrenade", new FixedMetadataValue(ParticleGrenades.instance(), true));
            if (g.isSmokeEnabled()) {
                new BukkitRunnable() {
                    Color color = g.getSmokeColor();

                    @Override
                    public void run() {
                        if (!event.getItemDrop().isValid()) {
                            cancel();
                            return;
                        }
                        Particle p = VersionUtils.isAtLeast("1.20.5") ? Particle.valueOf("DUST") : Particle.valueOf("REDSTONE");
                        i.getWorld().spawnParticle(p, i.getLocation().add(0, 0.5, 0), 0, 0, 0, 0, new DustOptions(color, 0.75f));
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

                    if (Utils.worldGuardLoaded())
                        if (!Utils.flag(p, i.getLocation(), Flags.POTION_SPLASH, Flags.BLOCK_BREAK)) {
                            i.remove();
                            cancel();
                            ChatUtils.response(p, "&cThat area is protected!");
                            return;
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
                    GrenadeUtils.explode(g, i.getLocation(), p);
                    i.remove();
                    cancel();
                }

            }.runTaskTimer(ParticleGrenades.instance(), 0, 1);
        }
    }

    /*private Pew pew = new Pew();

    @EventHandler
    public void onLand(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (event.getProjectile().getType().equals(EntityType.ARROW)) {
                Entity arrow = event.getProjectile();
                Vector start = arrow.getLocation().add(arrow.getVelocity().normalize()).toVector();
                pew.launchSingle(p, p.getWorld(), start, arrow.getVelocity(), new Vector(), new Vector(), true);
                arrow.remove();
            }
        }
    }

    protected class Pew extends ParticleProjectile {

        public Pew() {
            super(Particle.FLAME, 0.1, 5000.0, 5.0);
            setTicks(2L);
        }

        @Override
        public void OnHitBlock(LivingEntity who, World world, Vector start, Vector end, Block block, double t) {
            Grenade g = Grenade.getByKey("tactical_nuke");
            new BukkitRunnable() {

                @Override
                public void run() {
                    explode(g, end.toLocation(world), who);
                }

            }.runTaskLater(ParticleGrenades.instance(), 0);
        }
    }*/

    @EventHandler
    public void OnMerge(ItemMergeEvent event) {
        Item i = event.getEntity();
        Item j = event.getTarget();
        event.setCancelled(
                (i.getItemStack().getType().equals(Material.TNT) && i.hasMetadata("ParticleGrenade")) ||
                (j.getItemStack().getType().equals(Material.TNT) && j.hasMetadata("ParticleGrenade"))
        );
    }

    @EventHandler
    public void OnPlace(BlockPlaceEvent event) {
        ItemStack i = event.getHand().equals(EquipmentSlot.HAND) ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand();
        if (GrenadeUtils.isGrenade(i)) {
            if (!VersionUtils.isAtLeast("1.20.5")) {
                event.setCancelled(true);
                return;
            }
            if (Utils.worldGuardLoaded()) {
                Player p = event.getPlayer();
                if (!Utils.flag(p, p.getLocation(), Flags.BUILD)) {
                    ChatUtils.response(p, "&cThis area is protected!");
                    event.setCancelled(true);
                    return;
                }
            }
            Location loc = event.getBlockPlaced().getLocation();
            PGDataUtil.addData(loc.getWorld(), loc.toVector(), Grenade.getKey(GrenadeUtils.getName(i)));
        }

        //event.setCancelled(isGrenade(i));
    }

    @EventHandler
    public void OnIgnite(TNTPrimeEvent event) {
        if (!VersionUtils.isAtLeast("1.20.5"))
            return;
        Block block = event.getBlock();

        if (PGDataUtil.hasData(block)) {
            if (Utils.worldGuardLoaded()) {
                if (event.getCause().equals(TNTPrimeEvent.PrimeCause.PLAYER)) {
                    Player player = (Player) event.getPrimingEntity();
                    if (!Utils.flag(player, block.getLocation(), Flags.POTION_SPLASH, Flags.TNT, Flags.BUILD)) {
                        ChatUtils.response(player, "&cThis area is protected!");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    if (!Utils.flag(block.getLocation(), Flags.POTION_SPLASH, Flags.TNT)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            NamespacedKey key = PGDataUtil.getData(block);
            GrenadeUtils.spawnGrenade(block.getLocation().add(0.5, 0.5, 0.5), key, event.getPrimingEntity() instanceof LivingEntity e ? e : null);
            PGDataUtil.removeData(block, key);
            event.setCancelled(true);
            block.setType(Material.AIR);
        }
    }

    @EventHandler
    public void OnDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem().clone();
        if (GrenadeUtils.isGrenade(item)) {
            Dispenser block = (Dispenser) event.getBlock().getState();
            Grenade g = GrenadeUtils.getGrenade(item);
            Vector facing = ((Directional) block.getBlockData()).getFacing().getDirection();
            GrenadeUtils.spawnGrenade(event.getBlock().getLocation().add(0.5, 0.5, 0.5).add(facing.clone().normalize().multiply(0.6)), g.getKey(), facing.normalize().multiply(g.getDistance()));
            Inventory inventory = block.getInventory();

            event.setItem(new ItemStack(Material.AIR));

            new BukkitRunnable() {

                @Override
                public void run() {
                    inventory.removeItem(item);
                }

            }.runTask(ParticleGrenades.instance());
        }
    }

    @EventHandler
    public void OnBreak(BlockBreakEvent event) {
        if (!VersionUtils.isAtLeast("1.20.5"))
            return;
        Block block = event.getBlock();
        if (PGDataUtil.hasData(block)) {
            NamespacedKey key = PGDataUtil.getData(block);
            PGDataUtil.removeData(block, key);
        }
    }
}
