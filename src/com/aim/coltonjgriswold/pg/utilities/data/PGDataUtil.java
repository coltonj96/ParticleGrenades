package com.aim.coltonjgriswold.pg.utilities.data;

import com.aim.coltonjgriswold.pg.utilities.Grenade;
import com.aim.coltonjgriswold.pg.utilities.VersionUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PGDataUtil {

    public static boolean has(PersistentDataContainer data, NamespacedKey namespace) {
        if (VersionUtils.isAtLeast("1.20.4"))
            return data.has(namespace);
        return data.getKeys().contains(namespace);
    }

    public static <T extends PersistentDataHolder, P, C> boolean hasCustom(T t, NamespacedKey namespace, PersistentDataType<P, C> type) {
        return t != null && t.getPersistentDataContainer().has(namespace, type);
    }

    public static <T extends PersistentDataHolder, P, C> boolean hasCustom(T t, NamespacedKey namespace) {
        return t != null && has(t.getPersistentDataContainer(), namespace);
    }

    public static boolean hasData(ItemStack item) {
        return hasCustom(item.getItemMeta(), PGKeys.DATA);
    }

    public static boolean hasData(World world) {
        return hasCustom(world, PGKeys.LOCATION);
    }

    public static boolean hasData(World world, NamespacedKey key) {
        return hasData(world) && getData(world).has(key, new VectorListData());
    }

    public static boolean hasData(World world, Vector vector) {
        return hasData(world) && getData(world).getKeys().stream().anyMatch(key -> getData(world, key).contains(vector));
    }

    public static boolean hasData(Location location) {
        World world = location.getWorld();
        return hasData(world) && getData(world).getKeys().stream().anyMatch(key -> getData(world, key).contains(location.toVector().toBlockVector()));
    }

    public static boolean hasData(Block block) {
        return hasData(block.getWorld(), block.getLocation().toVector());
    }

    public static <T extends PersistentDataHolder, P, C> C getCustom(T t, NamespacedKey namespace, PersistentDataType<P, C> type) {
        return hasCustom(t, namespace, type) ? t.getPersistentDataContainer().get(namespace, type) : null;
    }

    public static <T extends PersistentDataHolder> PersistentDataContainer getTags(T t, NamespacedKey namespace) {
        return getCustom(t, namespace, PGDataType.TAG_CONTAINER);
    }

    public static String getData(ItemStack item) {
        return getCustom(item.getItemMeta(), PGKeys.DATA, PGDataType.STRING);
    }

    public static PersistentDataContainer getData(World world) {
        return getTags(world, PGKeys.LOCATION);
    }

    public static List<Vector> getData(World world, NamespacedKey key) {
        return getData(world).get(key, new VectorListData());
    }

    public static NamespacedKey getData(Location location) {
        return hasData(location) ? getData(location.getWorld()).getKeys().stream().filter(key -> contains(location, key)).findFirst().orElse(null) : null;
    }

    public static NamespacedKey getData(Block block) {
        return hasData(block) ? getData(block.getWorld()).getKeys().stream().filter(key -> contains(block, key)).findFirst().orElse(null) : null;
    }

    public static <T extends PersistentDataHolder, P, C> void setCustom(T t, NamespacedKey key, PersistentDataType<P,C> type, C data) {
        if (type instanceof PersistentDataContainer tag)
            tag.set(key, type, data);
        else
            t.getPersistentDataContainer().set(key, type, data);
    }

    public static void addData(ItemStack item, String data) {
        setCustom(item.getItemMeta(), PGKeys.DATA, PGDataType.STRING, data);
    }

    public static void addData(ItemMeta meta, String data) {
        setCustom(meta, PGKeys.DATA, PGDataType.STRING, data);
    }

    public static void addData(World world, Vector vector, NamespacedKey key) {
        PersistentDataContainer data;
        if (!hasData(world))
            setCustom(world, PGKeys.LOCATION, PGDataType.TAG_CONTAINER, world.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer());
        data = getData(world);
        if (data == null)
            return;
        List<Vector> list = new ArrayList<>();
        if (has(data, key))
            list = new ArrayList<>(data.get(key, new VectorListData()));
        list.add(vector);
        list = list.stream().distinct().toList();
        data.set(key, PGDataType.VECTORLIST, list);
        setCustom(world, PGKeys.LOCATION, PGDataType.TAG_CONTAINER, data);
        //world.getPersistentDataContainer().set(pgloc, PersistentDataType.TAG_CONTAINER, data);
    }

    public static boolean contains(World world, Vector vector, NamespacedKey key) {
        if (hasData(world, key))
            return getData(world, key).contains(vector);
        return false;
    }

    public static boolean contains(Location location, NamespacedKey key) {
        return contains(location.getWorld(), location.toVector(), key);
    }

    public static boolean contains(Block block, NamespacedKey key) {
        return contains(block.getLocation(), key);
    }

    public static void removeCustom(World world, NamespacedKey key) {
        if (hasData(world)) {
            PersistentDataContainer data = getData(world);
            if (data == null)
                return;
            if (has(data, key)) {
                data.remove(key);
                world.getPersistentDataContainer().set(PGKeys.LOCATION, PGDataType.TAG_CONTAINER, data);
                if (data.isEmpty())
                    world.getPersistentDataContainer().remove(PGKeys.LOCATION);
            }
        }
    }

    public static void removeData(ItemStack item) {
        if (hasData(item))
            item.getItemMeta().getPersistentDataContainer().remove(Grenade.getKey(getData(item)));
    }

    public static void removeData(World world, Vector vector, NamespacedKey key) {
        PersistentDataContainer data;
        if (hasData(world)) {
            data = getData(world);
            if (data == null)
                return;
            List<Vector> list = new ArrayList<>();
            if (data.has(key))
                list = new ArrayList<>(data.get(key, new VectorListData()));
            list.remove(vector);
            data.set(key, new VectorListData(), list);
            setCustom(world, PGKeys.LOCATION, PGDataType.TAG_CONTAINER, data);
            //world.getPersistentDataContainer().set(PGKeys.LOCATION, PersistentDataType.TAG_CONTAINER, data);
        }
    }

    public static void removeData(Block block, NamespacedKey key) {
        removeData(block. getWorld(), block.getLocation().toVector(), key);
    }

    public static void removeData(Location location, NamespacedKey key) {
        removeData(location.getWorld(), location.toVector(), key);
    }
}
