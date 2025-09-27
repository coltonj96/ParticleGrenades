package com.aim.coltonjgriswold.pg.utilities;

import java.util.List;
import java.util.Map;

import com.aim.coltonjgriswold.pg.utilities.random.WeightedChance;
import com.aim.coltonjgriswold.pg.utilities.worldguard.Utils;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import com.aim.coltonjgriswold.api.ParticleProjectile;

public class GrenadeEffect extends ParticleProjectile {

    private Grenade grenade;

    public GrenadeEffect(Grenade g) {
        super(g.getParticles().getValues().get(0), g.getHitboxSize(), g.getVelocity(), g.getLifespan());
        setColor(g.getExplosionfadeColor());
        setSecondaryColor(g.getExplosionFadeColor());
        grenade = g;
        grenade.getIgnoreList().forEach(this::ignoreMaterial);
        grenade.getBreakOnPenetrate().forEach(this::ignoreMaterial);
        grenade.getConvertOnPenetrateList().keySet().forEach(k -> k.forEach(this::ignoreMaterial));
        grenade.getDestroyOnPenetrate().forEach(this::ignoreMaterial);
        grenade.getThrowOnPenetrateList().forEach(this::ignoreMaterial);
        setTicks(2L);
    }

    @Override
    public void OnHitEntity(LivingEntity who, World world, Vector start, Vector end, Entity entity, double t) {
        if (entity instanceof LivingEntity e) {
            if (Utils.worldGuardLoaded())
                if (who instanceof Player p && !Utils.flag(p, e.getLocation(), Flags.DAMAGE_ANIMALS, Flags.PVP, Flags.POTION_SPLASH))
                    return;
                else
                    if (!Utils.flag(e.getLocation(), Flags.DAMAGE_ANIMALS, Flags.PVP, Flags.POTION_SPLASH))
                        return;
            if (e.equals(who))
                who.damage(grenade.getDamage());
            else
                e.damage(grenade.getDamage(), who);
            if (!grenade.getEffects().isEmpty())
                e.addPotionEffects(grenade.getEffects());
        }
        entity.setVelocity(end.subtract(start).normalize().multiply(grenade.getForce()));
    }

    @Override
    public void OnHitBlock(LivingEntity who, World world, Vector start, Vector end, Block block, double t) {
        if (Utils.worldGuardLoaded())
            if (who instanceof Player p && !Utils.flag(p, block.getLocation(), Flags.BUILD, Flags.TNT))
                return;
            else
                if (!Utils.flag(block.getLocation(), Flags.TNT))
                    return;
        if (grenade.getIgnoreList().contains(block.getType()))
            return;
        if (grenade.getThrowOnHitList().contains(block.getType())) {
            BlockData data = block.getBlockData();
            block.setType(Material.AIR);
            FallingBlock sand = world.spawnFallingBlock(block.getLocation(), data);
            sand.setVelocity(getDirection().normalize().multiply(grenade.getForce()));
        }
        if (grenade.getBreakOnHit().contains(block.getType()))
            block.breakNaturally();
        if (!grenade.getConvertOnHitList().isEmpty() && !block.getType().equals(Material.AIR)) {
            Map<List<Material>, WeightedChance<Material>> map = grenade.getConvertOnHitList();
            map.forEach((k, v) -> {
                if (k.contains(block.getType()))
                    block.setType(v.select());
            });
        }
        if (grenade.getDestroyOnHit().contains(block.getType()))
            block.setType(Material.AIR);
    }



    @Override
    public void OnPenetrateBlock(LivingEntity who, World world, Vector where, Block block, double t) {
        if (Utils.worldGuardLoaded())
            if (who instanceof Player p && !Utils.flag(p, block.getLocation(), Flags.BUILD, Flags.TNT))
                return;
            else
                if (!Utils.flag(block.getLocation(), Flags.TNT))
                    return;
        if (grenade.getIgnoreList().contains(block.getType()))
            return;
        if (grenade.getThrowOnPenetrateList().contains(block.getType())) {
            BlockData data = block.getBlockData();
            block.setType(Material.AIR);
            FallingBlock sand = world.spawnFallingBlock(block.getLocation(), data);
            sand.setVelocity(getDirection().normalize().multiply(grenade.getForce()));
        }
        if (grenade.getBreakOnPenetrate().contains(block.getType()))
            block.breakNaturally();
        if (!grenade.getConvertOnPenetrateList().isEmpty() && !block.getType().equals(Material.AIR)) {
            Map<List<Material>, WeightedChance<Material>> map = grenade.getConvertOnPenetrateList();
            map.forEach((k, v) -> {
                if (k.contains(block.getType())) {
                    block.setType(v.select());
                }
            });
        }
        if (grenade.getDestroyOnPenetrate().contains(block.getType()))
            block.setType(Material.AIR);
    }

    @Override
    public void OnMove(World world, Vector previous, Vector current, double t) {
        Particle p = VersionUtils.isAtLeast("1.20.5") ? Particle.valueOf("DUST") : Particle.valueOf("REDSTONE");
        if (!getParticleType().equals(p))
            return;
        Color a = grenade.getExplosionfadeColor();
        Color b = grenade.getExplosionFadeColor();
        int red = (int) (a.getRed() + ((b.getRed() - a.getRed()) * t));// % 256;
        int green = (int) (a.getGreen() + ((b.getGreen() - a.getGreen()) * t));// % 256;
        int blue = (int) (a.getBlue() + ((b.getBlue() - a.getBlue()) * t));// % 256;
        setColor(Color.fromRGB(red, green, blue));
    }
}
