package com.aim.coltonjgriswold.pg.utilities;

import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import com.aim.coltonjgriswold.pg.ParticleGrenades;
import com.aim.coltonjgriswold.pg.utilities.random.WeightedChance;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Grenade {

    private static Map<NamespacedKey, Grenade> grenades;

    static {
        grenades = new HashMap<>();
    }

    private String name;
    private List<String> description;
    private String usePerm;
    private String givePerm;
    private Color explosionColor;
    private Color explosionfadeColor;
    private Color smokeColor;
    private double radius;
    private boolean proximity;
    private boolean impact;
    private int fuse;
    private long cooldown;
    private double velocity;
    private double lifespan;
    private double damage;
    private double force;
    private double hitbox;
    private List<PotionEffect> effects;
    private boolean smokeEnabled;
    private WeightedChance<Particle> particles;
    private Sound sound;
    private float volume;
    private float pitch;
    private int fragments;
    private List<Material> breakOnHit;
    private List<Material> destroyOnHit;
    private List<Material> breakOnPenetrate;
    private List<Material> destroyOnPenetrate;
    private List<Material> ignore;
    private List<Material> throwOnHit;
    private List<Material> throwOnPenetrate;
    private Map<List<Material>, WeightedChance<Material>> convertOnHit;
    private Map<List<Material>, WeightedChance<Material>> convertOnPenetrate;
    private double distance;
    private NamespacedKey key;

    public Grenade(NamespacedKey key, ConfigurationSection section) {
        this.key = key;
        name = section.getString("name");
        description = section.getStringList("description");
        usePerm = "particlegrenades.use." + section.getName();
        givePerm = "particlegrenades.give." + section.getName();
        {
            int color = 0;
            try {
                color = Integer.parseInt(section.getString("particles.explosion.color.from"), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            explosionColor = Color.fromRGB(color);
        }
        {
            int color= 0;
            try {
                color = Integer.parseInt(section.getString("particles.explosion.color.to"), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            explosionfadeColor = Color.fromRGB(color);
        }
        {
            int color = 0;
            try {
                color = Integer.parseInt(section.getString("particles.smoke.color"), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            smokeColor = Color.fromRGB(color);
        }
        fuse = section.getInt("behavior.fuse");
        proximity = section.getBoolean("behavior.proximity");
        radius = section.getDouble("behavior.radius");
        impact = section.getBoolean("behavior.impact");
        cooldown = section.getLong("cooldown");
        velocity = section.getDouble("particles.explosion.velocity");
        lifespan = section.getDouble("particles.explosion.lifespan");
        damage = section.getDouble("particles.explosion.damage");
        force = section.getDouble("particles.explosion.force");
        hitbox = section.getDouble("particles.explosion.hitbox");
        effects = section.getStringList("effects").stream().filter(token -> {
            String[] tokens = token.split(":");
            NamespacedKey k = NamespacedKey.minecraft(tokens[0].toLowerCase());
            PotionEffectType effect;
            if (VersionUtils.isAtLeast("1.20.4"))
                effect = Registry.EFFECT.get(k);
            else
                effect = PotionEffectType.getByKey(k);
            if (effect == null)
                return false;
            return true;
        }).map(token -> {
            String[] tokens = token.split(":");
            PotionEffectType effect;
            NamespacedKey k = NamespacedKey.minecraft(tokens[0].toLowerCase());
            if (VersionUtils.isAtLeast("1.20.4"))
                effect = Registry.EFFECT.get(k);
            else
                effect = PotionEffectType.getByKey(k);
            int amplifier = 0;
            try {
                amplifier = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            int duration = 0;
            try {
                duration = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return new PotionEffect(effect, duration, amplifier);
        }).toList();
        smokeEnabled = section.getBoolean("particles.smoke.enabled");
        //q = section.getStringList("particles.explosion.particles").stream().map(token -> Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(token.toLowerCase()))).distinct().toList();
        {
            List<String> list = section.getStringList("particles.explosion.particles");
            WeightedChance<Particle> chance = new WeightedChance<>();
            list.stream().distinct().forEach(token -> {
                try {
                    String num = token.replaceAll("[^0-9]+\\.?[^0-9]+", "");
                    double weight = ((double) list.size()) / 100.0;
                    if (!num.isBlank())
                        weight = Double.parseDouble(num) / 100.0;
                    NamespacedKey k = NamespacedKey.minecraft(token.replaceAll("%[0-9*.?0-9+]*", "").toLowerCase());
                    Particle particle = null;
                    if (VersionUtils.isAtLeast("1.20.4"))
                        particle = Registry.PARTICLE_TYPE.get(k);
                    else
                        if (Arrays.stream(Particle.values()).anyMatch(part -> part.name().equalsIgnoreCase(k.getKey())))
                            particle = Particle.valueOf(k.getKey().toUpperCase(Locale.ROOT));
                    if (particle != null)
                        chance.add(particle, weight);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            });
            particles = chance;
        }
        {
            String[] tokens = section.getString("sound").split(":");
            NamespacedKey k = NamespacedKey.minecraft(tokens[0].toLowerCase());
            sound = Registry.SOUNDS.get(k);
            float volume = 0;
            try {
                volume = Float.parseFloat(tokens[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            this.volume = volume;
            float pitch = 0;
            try {
                pitch = Float.parseFloat(tokens[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            this.pitch = pitch;
        }
        fragments = section.getInt("particles.explosion.fragments");
        breakOnHit = filterMaterials(section.getStringList("particles.explosion.blocks.on_hit.break"));
        destroyOnHit = filterMaterials(section.getStringList("particles.explosion.blocks.on_hit.destroy"));
        breakOnPenetrate = filterMaterials(section.getStringList("particles.explosion.blocks.on_penetrate.break"));
        destroyOnPenetrate = filterMaterials(section.getStringList("particles.explosion.blocks.on_penetrate.destroy"));
        ignore = filterMaterials(section.getStringList("particles.explosion.blocks.on_penetrate.ignore"));
        throwOnHit = filterMaterials(section.getStringList("particles.explosion.blocks.on_hit.throw"));
        throwOnPenetrate = filterMaterials(section.getStringList("particles.explosion.blocks.on_penetrate.throw"));
        {
            Map<List<Material>, WeightedChance<Material>> map = new HashMap<>();
            section.getStringList("particles.explosion.blocks.on_hit.convert").forEach(token -> {
                String[] split = token.split(":");
                List<Material> from = new ArrayList<>(filterMaterials(Arrays.asList(split[0].split(","))));
                WeightedChance<Material> to = addWeight(Arrays.asList(split[1].split(",")));
                map.put(from, to);
            });
            convertOnHit = map;
        }
        {
            Map<List<Material>, WeightedChance<Material>> map = new HashMap<>();
            section.getStringList("particles.explosion.blocks.on_penetrate.convert").forEach(token -> {
                String[] split = token.split(":");
                List<Material> from = new ArrayList<>(filterMaterials(Arrays.asList(split[0].split(","))));
                WeightedChance<Material> to = addWeight(Arrays.asList(split[1].split(",")));
                map.put(from, to);
            });
            convertOnPenetrate = map;
        }
        distance = section.getDouble("distance");
        grenades.put(key, this);
    }

    private List<Material> filterMaterials(List<String> list) {
        Stream<Material> stream;
        if (VersionUtils.isAtLeast("1.20.4"))
            stream = Registry.MATERIAL.stream();
        else
            stream = Arrays.stream(Material.values());
        return stream.filter(material -> {
            boolean pass = false;
            for (String token : list) {
                if (matches(material, token))
                    pass = true;
                if (matches(material, token) && token.startsWith("-"))
                    return false;
            }
            return pass;
        }).distinct().toList();
    }

    private WeightedChance<Material> addWeight(List<String> list) {
        WeightedChance<Material> chance = new WeightedChance<>();
        filterMaterials(list).forEach(material -> {
            list.stream().filter(t -> !t.startsWith("-")).toList().forEach(token -> {
                if (matches(material, token)) {
                    try {
                        String num = token.replaceAll("[^0-9]+\\.?[^0-9]+", "");
                        double weight = ((double) list.size()) / 100.0;
                        if (!num.isBlank() && !num.equals("?"))
                            weight = Double.parseDouble(num) / 100.0;
                        chance.add(material, weight);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        return chance;
    }

    private boolean matches(Material material, String token) {
        String t = token.replace("?", ".*").replaceAll("[-%][0-9*.?0-9+]*", "");
        //boolean check = token.startsWith("-");
        return switch (t.toLowerCase()) {
            case "burnable" -> material.isBurnable();
            case "flammable" -> material.isFlammable();
            case "logs" -> Tag.LOGS.isTagged(material);
            case "leaves" -> Tag.LEAVES.isTagged(material);
            case "planks" -> Tag.PLANKS.isTagged(material);
            default -> VersionUtils.getKey(material).getKey().matches(t) && (material.isBlock() || material.isAir());
        };
        /*if (t.equalsIgnoreCase("burnable")) {
            if (token.startsWith("-") && material.isBurnable()) {
                return true;
            }
            return material.isBurnable();
        }
        if (t.equalsIgnoreCase("flammable")) {
            if (token.startsWith("-")) {
                return true;
            }
            return material.isFlammable();
        }
        return material.getKey().getKey().matches(t) && (material.isBlock() || material.isAir());

         */
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getUsePerm() {
        return usePerm;
    }

    public String getGivePerm() {
        return givePerm;
    }

    public Color getExplosionfadeColor() {
        return explosionColor;
    }

    public Color getExplosionFadeColor() {
        return explosionfadeColor;
    }

    public Color getSmokeColor() {
        return smokeColor;
    }

    public int getFuse() {
        return fuse;
    }

    public boolean getProximity() {
        return proximity;
    }

    public double getRadius() {
        return radius;
    }

    public boolean getImpact() {
        return impact;
    }

    public long getCooldown() {
        return cooldown;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getLifespan() {
        return lifespan;
    }

    public double getDamage() {
        return damage;
    }

    public double getForce() {
        return force;
    }

    public double getHitboxSize() {
        return hitbox;
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }

    public boolean isSmokeEnabled() {
        return smokeEnabled;
    }

    public WeightedChance<Particle> getParticles() {
        return particles;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public int getFragments() {
        return fragments;
    }

    public List<Material> getBreakOnHit() {
        return breakOnHit;
    }

    public List<Material> getDestroyOnHit() {
        return destroyOnHit;
    }

    public List<Material> getBreakOnPenetrate() {
        return breakOnPenetrate;
    }

    public List<Material> getDestroyOnPenetrate() {
        return destroyOnPenetrate;
    }

    public List<Material> getIgnoreList() {
        return ignore;
    }

    public List<Material> getThrowOnHitList() {
        return throwOnHit;
    }

    public List<Material> getThrowOnPenetrateList() {
        return throwOnPenetrate;
    }

    public Map<List<Material>, WeightedChance<Material>> getConvertOnHitList() {
        return convertOnHit;
    }

    public Map<List<Material>, WeightedChance<Material>> getConvertOnPenetrateList() {
        return convertOnPenetrate;
    }

    public double getDistance() {
        return distance;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public static void reload() {
        grenades.clear();
    }

    public static Grenade getByKey(NamespacedKey key) {
        if (grenades.containsKey(key))
            return grenades.get(key);
        return null;
    }

    public static Grenade getByKey(String key) {
        return getByKey(new NamespacedKey(ParticleGrenades.instance(), key));
    }

    public static Map<NamespacedKey, Grenade> values() {
        return grenades;
    }

    public static boolean contains(String key) {
        return grenades.containsKey(new NamespacedKey(ParticleGrenades.instance(), key));
    }

    public static NamespacedKey getKey(String name) {
        if (contains(name))
            return new NamespacedKey(ParticleGrenades.instance(), name);
        return null;
    }

    public static NamespacedKey getKey(ItemStack item) {
        if (GrenadeUtils.isGrenade(item))
            return GrenadeUtils.getGrenade(item).getKey();
        return null;
    }
}