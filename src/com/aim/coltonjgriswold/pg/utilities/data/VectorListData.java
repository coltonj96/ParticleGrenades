package com.aim.coltonjgriswold.pg.utilities.data;

import com.google.common.collect.Lists;

import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;

public class VectorListData implements ListPersistentDataType<int[], Vector> {

    private final VectorData data;

    public VectorListData() {
        data = new VectorData();
    }

    @Override
    public Class<List<int[]>> getPrimitiveType() {
        return (Class<List<int[]>>) (Object) List.class;
    }

    @Override
    public Class<List<Vector>> getComplexType() {
        return (Class<List<Vector>>) (Object) List.class;
    }

    @Override
    public List<int[]> toPrimitive(List<Vector> complex, PersistentDataAdapterContext context) {
        return Lists.transform(complex, s -> data.toPrimitive(s, context));
        //return complex.stream().map(vector -> new int[] {(int) vector.getX(), (int) vector.getY(), (int) vector.getZ()}).toList();
    }

    @Override
    public List<Vector> fromPrimitive(List<int[]> primitive, PersistentDataAdapterContext context) {
        return Lists.transform(primitive, s -> data.fromPrimitive(s, context));
        //return primitive.stream().map(integer -> new Vector(integer[0], integer[1], integer[2])).toList();
    }

    @Override
    public PersistentDataType<int[], Vector> elementType() {
        return data;
    }
}
