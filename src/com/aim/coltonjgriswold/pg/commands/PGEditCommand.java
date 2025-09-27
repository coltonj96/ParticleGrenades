package com.aim.coltonjgriswold.pg.commands;

import java.util.List;
import java.util.Set;

import com.aim.coltonjgriswold.pg.commands.abstraction.PGTabCommand;
import com.aim.coltonjgriswold.pg.utilities.Grenade;
import com.aim.coltonjgriswold.pg.utilities.GrenadeUtils;
import org.bukkit.configuration.file.FileConfiguration;

import com.aim.coltonjgriswold.pg.ParticleGrenades;

public class PGEditCommand extends PGTabCommand {

    @Override
    public boolean onCmd() {
        GrenadeUtils.explode(Grenade.getByKey("tactical_nuke"), player.getLocation(), player);
        return true;
    }

    @Override
    public List<String> onTab() {
        List<String> list;
        FileConfiguration config = ParticleGrenades.instance().getConfig();
        Set<String> grenades = config.getKeys(true);
        if (args[0].isBlank())
            list = grenades.stream().toList();
        else
            list = grenades.stream().filter(s -> s.contains(args[0])).toList();
        return list;
    }
}

