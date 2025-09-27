package com.aim.coltonjgriswold.pg.commands.abstraction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class PGTabCommand extends PGCommand implements IPGTabCommand {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player player) {
            this.player = player;
            this.args = args;
            return onTab();
        }
        return null;
    }
}
