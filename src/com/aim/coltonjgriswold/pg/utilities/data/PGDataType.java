package com.aim.coltonjgriswold.pg.utilities.data;

import com.aim.coltonjgriswold.pg.utilities.VersionUtils;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class PGDataType {

    public static PersistentDataType<String, String> STRING = PersistentDataType.STRING;
    public static PersistentDataType<PersistentDataContainer, PersistentDataContainer> TAG_CONTAINER = PersistentDataType.TAG_CONTAINER;
    public static PersistentDataType<int[], Vector> VECTOR = new VectorData();
    public static ListPersistentDataType<int[], Vector> VECTORLIST = null;

    static {
        if (VersionUtils.isAtLeast("1.20.5"))
            VECTORLIST = new VectorListData();
    }
}
