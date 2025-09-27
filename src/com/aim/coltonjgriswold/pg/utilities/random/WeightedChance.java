package com.aim.coltonjgriswold.pg.utilities.random;

import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Predicate;

public class WeightedChance<E> {

    private NavigableMap<Double, Weighted<E>> map = new TreeMap<>();
    private WeightedRandom random;
    private double total = 0;

    public WeightedChance() {
        random = new WeightedRandom();
    }

    public WeightedChance(WeightedRandom random) {
        this.random = random;
    }

    public WeightedChance(Collection<? extends Weighted<E>> collection) {
        addAll(collection);
    }

    public static <E> WeightedChance<E> fromCollection(Collection<? extends Weighted<E>> collection) {
        return new WeightedChance<>(collection);
    }

    public static <E> E select(Collection<? extends Weighted<E>> collection) {
        return select(new WeightedRandom(), collection);
    }

    public static <E> E select(WeightedRandom random, Collection<? extends Weighted<E>> collection) {
        return WeightedChance.fromCollection(collection).select(random);
    }

    public boolean add(E value, double weight) {
        Weighted<E> entry = new WeightedEntry<>(value, weight);
        if (weight <= 0 || contains(value))
            return false;
        total += weight;
        map.put(total, entry);
        return true;
    }

    public boolean add(Weighted<E> entry) {
        if (entry.weight() <= 0 || contains(entry))
            return false;
        total += entry.weight();
        map.put(total, entry);
        return true;
    }

    public void addAll(Collection<? extends Weighted<E>> collection) {
        collection.forEach(this::add);
    }

    public boolean remove(E value) {
        NavigableMap<Double, Weighted<E>> tempMap = new TreeMap<>();
        total = 0;
        map.values().stream().filter(entry -> !entry.value().equals(value)).forEach(entry -> {
            total += entry.weight();
            tempMap.put(total, entry);
        });
        boolean change = map.size() != tempMap.size();
        map = tempMap;
        return change;
    }

    public boolean remove(Predicate<Weighted<E>> predicate) {
        NavigableMap<Double, Weighted<E>> tempMap = new TreeMap<>();
        total = 0;
        map.values().stream().filter(entry -> !predicate.test(entry)).forEach(entry -> {
            total += entry.weight();
            tempMap.put(total, entry);
        });
        boolean change = map.size() != tempMap.size();
        map = tempMap;
        return change;
    }

    public E select() {
        return select(random);
    }

    public E select(WeightedRandom random) {
        double next = random.nextDouble() * total;
        return map.ceilingEntry(next).getValue().value();
    }

    public double getWeight(E value) {
        Weighted<E> e = map.values().stream().filter(entry -> entry.value().equals(value)).findFirst().orElse(null);
        if (e != null)
            return e.weight();
        return 0;
    }

    public double getTotal() {
        return total;
    }

    public List<E> getValues() {
        return map.values().stream().map(Weighted::value).toList();
    }

    public boolean contains(E value) {
        return map.values().stream().anyMatch(entry -> entry.value().equals(value));
    }

    public boolean contains(Weighted<E> weighted) {
        return map.values().stream().anyMatch(entry -> entry.value().equals(weighted));
    }
}