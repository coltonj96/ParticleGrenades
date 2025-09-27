package com.aim.coltonjgriswold.pg.commands;

import java.util.ArrayList;
import java.util.List;

import com.aim.coltonjgriswold.pg.commands.abstraction.PGCommand;
import com.aim.coltonjgriswold.pg.utilities.ChatUtils;

import com.aim.coltonjgriswold.pg.utilities.Grenade;

public class PGListGrenadesCommand extends PGCommand {

    @Override
    public boolean onCmd() {
        if (player != null) {
            if (hasPermission("particlegrenades.list")) {
                List<String> list = new ArrayList<>();
                list.add(ChatUtils.color("Available Grenades:"));
                Grenade.values().forEach((k, v) -> {
                    if (hasPermission(v.getGivePerm()))
                        list.add(ChatUtils.color(String.format("  %1$s &6&l(&6/pggive %2$s&6&l)", v.getName(), k.getKey())));
                });
                if (list.size() == 1)
                    list.add(ChatUtils.color("&aNONE"));
                response(list.toArray(new String[0]));
            } else {
                response("&cYou do not have permission!");
            }
        }
        return true;
    }
}
