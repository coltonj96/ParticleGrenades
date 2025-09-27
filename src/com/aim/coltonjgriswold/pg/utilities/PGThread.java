package com.aim.coltonjgriswold.pg.utilities;

import org.bukkit.scheduler.BukkitRunnable;

public class PGThread {

    public PGThread() {
        start();
    }

    private void start() {
        new BukkitRunnable() {

            @Override
            public void run() {
                synchronized (this) {

                }
            }
        };
    }
}
