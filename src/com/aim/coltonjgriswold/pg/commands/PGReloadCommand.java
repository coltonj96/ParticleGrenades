package com.aim.coltonjgriswold.pg.commands;

import com.aim.coltonjgriswold.pg.commands.abstraction.PGCommand;
import com.aim.coltonjgriswold.pg.utilities.ChatUtils;

import com.aim.coltonjgriswold.pg.ParticleGrenades;

public class PGReloadCommand extends PGCommand {

    @Override
    public boolean onCmd() {
        if (player != null) {
            if (hasPermission("particlegrenades.reload")) {
                ParticleGrenades.instance().reloadConfig();
                ParticleGrenades.instance().parseConfig(true);
                response("&aConfig Reloaded!");
            } else {
                response("&cYou do not have permission!");
            }
        } else {
            ParticleGrenades.instance().reloadConfig();
            ParticleGrenades.instance().parseConfig(true);
            ParticleGrenades.instance().getLogger().info(ChatUtils.color("&8&l[&6ParticleGrenades&8&l] &aConfig Reloaded!"));
        }
        return true;
    }

}
