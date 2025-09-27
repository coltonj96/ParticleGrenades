package com.aim.coltonjgriswold.pg.commands.abstraction;

import org.bukkit.command.TabCompleter;

import java.util.List;

public interface IPGTabCommand extends TabCompleter {

    List<String> onTab();
}