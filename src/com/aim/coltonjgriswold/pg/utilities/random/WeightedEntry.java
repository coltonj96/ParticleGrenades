package com.aim.coltonjgriswold.pg.utilities.random;

public class WeightedEntry<E> implements Weighted<E> {

    protected double weight;
    protected E value;

    public WeightedEntry(E value, double weight) {
        this.weight = weight;
        this.value = value;
    }

    public E value() {
        return value;
    }

    public double weight() {
        return weight;
    }
}
