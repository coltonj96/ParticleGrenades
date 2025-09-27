package com.aim.coltonjgriswold.pg.utilities.random;

import java.util.Random;

public class WeightedRandom extends Random {

    private long seed;

    public WeightedRandom() {
        super();
        seed = nextLong();
        setSeed(seed);
    }

    public WeightedRandom(long seed) {
        super(seed);
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }
}

