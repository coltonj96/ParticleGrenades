package com.aim.coltonjgriswold.pg.commands;

import com.aim.coltonjgriswold.pg.commands.abstraction.PGTabCommand;
import com.aim.coltonjgriswold.pg.utilities.Grenade;
import com.aim.coltonjgriswold.pg.utilities.GrenadeUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PGSpawnCommand extends PGTabCommand {

    @Override
    public boolean onCmd() {
        if (player != null) {
            if (args.length == 4) {
                if (Grenade.contains(args[0].toLowerCase())) {
                    Grenade g = Grenade.getByKey(args[0].toLowerCase());
                    if (!hasPermission(g.getGivePerm())) {
                        response("&cYou do not have permission!");
                    } else {
                        double x = 0;
                        double y = 0;
                        double z = 0;
                        try {
                            x = Double.parseDouble(args[1]);
                        } catch (NumberFormatException ex) {
                            response("&cInvalid X coordinate!");
                            return false;
                        }
                        try {
                            y = Double.parseDouble(args[2]);
                        } catch (NumberFormatException ex) {
                            response("&cInvalid Y coordinate!");
                            return false;
                        }
                        try {
                            z = Double.parseDouble(args[3]);
                        } catch (NumberFormatException ex) {
                            response("&cInvalid Z coordinate!");
                            return false;
                        }
                        GrenadeUtils.spawnGrenade(new Location(player.getWorld(), x, y, z), g.getKey(), player, new Vector(0, -10,0));
                        response("&aSuccessfully spawned &l%1$s &aat &l%3$s %2$s %4$s!", g.getName(), x, y, z);
                    }
                } else {
                    response("&cInvalid grenade!");
                }
            } else {
                response("&cUsage: /pgspawn <grenade> <x> <y> <z>");
            }
        }
        return true;
    }

    @Override
    public List<String> onTab() {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            if (player != null) {
                Grenade.values().forEach((k, v) -> {
                    if (hasPermission(v.getGivePerm())) {
                        list.add(k.getKey());
                    }
                });
            }
        }
        return list;
    }

}
