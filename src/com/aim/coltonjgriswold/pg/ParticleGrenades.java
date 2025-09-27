package com.aim.coltonjgriswold.pg;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.StreamSupport;

import com.aim.coltonjgriswold.pg.commands.PGGiveCommand;
import com.aim.coltonjgriswold.pg.commands.PGListGrenadesCommand;
import com.aim.coltonjgriswold.pg.commands.PGReloadCommand;
import com.aim.coltonjgriswold.pg.commands.PGSpawnCommand;
import com.aim.coltonjgriswold.pg.utilities.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.WorldGuard;

public class ParticleGrenades extends JavaPlugin {

    private static ParticleGrenades plugin;
    private Set<Recipe> recipes;
    //private List<Material> materials;

    private WorldGuard worldGuard;

    public void onEnable() {
        //Debugging.enable();
        plugin = this;
        recipes = new HashSet<>();
        worldGuardLoaded();
        //materials = Lists.newArrayList(Material.values());
        saveDefaultConfig();
        parseConfig(false);
        generateText();
        getServer().getPluginManager().registerEvents(new GrenadeHandler(), this);
        getCommand("pgreload").setExecutor(new PGReloadCommand());
        getCommand("pglist").setExecutor(new PGListGrenadesCommand());
        PGGiveCommand give = new PGGiveCommand();
        getCommand("pggive").setTabCompleter(give);
        getCommand("pggive").setExecutor(give);
        //PGEditCommand edit = new PGEditCommand();
        PGSpawnCommand spawn = new PGSpawnCommand();
        getCommand("pgspawn").setTabCompleter(spawn);
        getCommand("pgspawn").setExecutor(spawn);
        //getCommand("pgedit").setExecutor(edit);
        //getCommand("pgedit").setTabCompleter(edit);
    }

    public void onDisable() {
        recipes.forEach(r -> getServer().removeRecipe(((CraftingRecipe) r).getKey()));
    }

    public static ParticleGrenades instance() {
        return plugin;
    }

    private void generateText() {
        try {
            if (!getDataFolder().exists())
                getDataFolder().mkdir();

            List<Material> materials;
            List<PotionEffectType> effects;
            List<Particle> particles;
            List<Sound> sounds;

            if (VersionUtils.isAtLeast("1.20.4")) {
                materials = new ArrayList<>(Registry.MATERIAL.stream().toList());
                effects = new ArrayList<>(Registry.EFFECT.stream().toList());
                particles = new ArrayList<>(Registry.PARTICLE_TYPE.stream().toList());
                sounds = new ArrayList<>(Registry.SOUNDS.stream().toList());
            } else {
                materials = new ArrayList<>(List.of(Material.values()));
                effects = new ArrayList<>(List.of(PotionEffectType.values()));
                particles = new ArrayList<>(List.of(Particle.values()));
                sounds = new ArrayList<>(StreamSupport.stream(Registry.SOUNDS.spliterator(), false).toList());
            }

            File file = new File(getDataFolder(), "materials.txt");
            FileWriter writer = new FileWriter(file);
            materials.sort(Comparator.comparing(Enum::name));
            for (Material m : materials) {
                NamespacedKey key = VersionUtils.getKey(m);
                if (!key.getKey().startsWith("LEGACY"))
                    writer.write(key.getKey() + "\r\n");
            }
            writer.close();

            file = new File(getDataFolder(), "effects.txt");
            writer = new FileWriter(file);
            effects.sort(Comparator.comparing(e -> VersionUtils.getKey(e).getKey()));
            for (PotionEffectType e : effects)
                if (e != null)
                    writer.write(VersionUtils.getKey(e).getKey() + "\r\n");
            writer.close();

            file = new File(getDataFolder(), "particles.txt");
            writer = new FileWriter(file);
            particles.sort(Comparator.comparing(Enum::name));
            for (Particle p : particles) {
                String name = p.name();
                if (VersionUtils.isAtLeast("1.20.2"))
                    name = VersionUtils.getKey(p).getKey();
                if (!name.startsWith("LEGACY"))
                    writer.write(name + "\r\n");
            }
            writer.close();

            file = new File(getDataFolder(), "sounds.txt");
            writer = new FileWriter(file);
            sounds.sort(Comparator.comparing(s -> VersionUtils.getKey(s).getKey()));
            for (Sound s : sounds) {
                writer.write(VersionUtils.getKey(s).getKey() + "\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void remove() {
        Set<NamespacedKey> temp = new HashSet<>();
        Iterable<Recipe> iter = () -> getServer().recipeIterator();
        StreamSupport.stream(iter.spliterator(), false).filter(r ->
                r instanceof ShapedRecipe recipe && VersionUtils.getKey(recipe).getNamespace().equalsIgnoreCase(getName().toLowerCase(Locale.ROOT))
        ).forEach(k -> getServer().removeRecipe(VersionUtils.getKey((ShapedRecipe) k)));
    }

    public void parseConfig(boolean reload) {
        remove();
        char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        FileConfiguration c = getConfig();
        /*try {
            FileConfiguration test = new YamlConfiguration();
            test.loadFromString(c.saveToString());
            test.getKeys(true).forEach(System.out::println);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }*/
        Set<Recipe> temp = new HashSet<>();
        c.getKeys(false).forEach(g -> {
            /*String name = c.getString(g + ".name");
            List<String> description = c.getStringList(g + ".description");
            ItemStack item = new ItemStack(Material.TNT, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> lore = new ArrayList<>();
            description.forEach(s -> lore.add(ChatColor.translateAlternateColorCodes('&', s)));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(this, "pgdata"), PersistentDataType.STRING, g);

            item.setItemMeta(meta);
            */

            NamespacedKey key = new NamespacedKey(this, g);

            ShapedRecipe recipe = new ShapedRecipe(key, createGrenade(g));

            List<String> list = c.getStringList((g + ".recipe.ingredients"));

            List<String> grid = c.getStringList(g + ".recipe.grid");
            grid = grid.stream().map(s -> s.replace(chars[list.size()], ' ')).toList();
            recipe.shape(grid.toArray(new String[0]));

            for (int n = 0; n < list.size(); n++) {
                if (c.getKeys(false).contains(list.get(n).toLowerCase())) {
                    recipe.setIngredient(chars[n], new RecipeChoice.ExactChoice(createGrenade(list.get(n).toLowerCase())));
                } else {
                    Material mat;
                    if (VersionUtils.isAtLeast("1.20.4"))
                        mat = Registry.MATERIAL.get(NamespacedKey.minecraft(list.get(n)));//Material.getMaterial(list.get(n));
                    else
                        mat = Material.valueOf(list.get(n).toUpperCase(Locale.ROOT));
                    if (mat == null || mat == Material.AIR)
                        continue;
                    recipe.setIngredient(chars[n], mat);
                }
            }
            recipe.setGroup(recipe.getKey().toString());
            recipe.setCategory(CraftingBookCategory.REDSTONE);
            temp.add(recipe);
            new Grenade(key, c.getConfigurationSection(g));
        });
        //if (reload) {
            recipes.forEach(r -> {
                getServer().removeRecipe(VersionUtils.getKey((ShapedRecipe) r));
            });
            getServer().getOnlinePlayers().forEach(GrenadeUtils::giveRecipes);
        //}
        temp.forEach(recipe -> getServer().addRecipe(recipe));
        recipes = temp;
    }

    private ItemStack createGrenade(String g) {
        FileConfiguration c = getConfig();
        String name = c.getString(g + ".name");
        List<String> description = c.getStringList(g + ".description");
        ItemStack item = new ItemStack(Material.TNT, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lore = new ArrayList<>();
        description.forEach(s -> lore.add(ChatColor.translateAlternateColorCodes('&', s)));
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(new NamespacedKey(this, "pgdata"), PersistentDataType.STRING, g);

        item.setItemMeta(meta);

        return item;
    }

    private boolean worldGuardLoaded() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null)
            return false;
        worldGuard = WorldGuard.getInstance();
        return worldGuard != null;
    }

    public WorldGuard getWorldGuard() {
        return worldGuard;
    }
}
