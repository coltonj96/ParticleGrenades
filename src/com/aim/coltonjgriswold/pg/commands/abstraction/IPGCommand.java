package com.aim.coltonjgriswold.pg.commands.abstraction;

import org.bukkit.command.CommandExecutor;

public interface IPGCommand extends CommandExecutor {

    boolean onCmd();
}
