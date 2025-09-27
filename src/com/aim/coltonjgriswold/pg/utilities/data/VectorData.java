package com.aim.coltonjgriswold.pg.utilities.data;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class VectorData implements PersistentDataType<int[], Vector> {

    @Override
    public Class<int[]> getPrimitiveType() {
        return int[].class;
    }

    @Override
    public Class<Vector> getComplexType() {
        return Vector.class;
    }

    @Override
    public int[] toPrimitive(Vector complex, PersistentDataAdapterContext context) {;
        return new int[] {(int) complex.getX(), (int) complex.getY(), (int) complex.getZ()};
    }

    @Override
    public Vector fromPrimitive(int[] primitive, PersistentDataAdapterContext context) {
        return new Vector(primitive[0], primitive[1], primitive[2]);
    }
}
