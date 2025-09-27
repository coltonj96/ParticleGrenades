package com.aim.coltonjgriswold.pg.commands.abstraction;

import com.aim.coltonjgriswold.pg.utilities.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class PGCommand implements IPGCommand {

    protected CommandSender sender;

    protected Player player;

    protected Command command;

    protected String[] args;

    protected boolean console = false;

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.command = command;
        this.sender = sender;
        if (sender instanceof Player player) {
            this.player = player;
            if (!hasPermission()) {
                response("&c", "Insufficient Permissions!");
                return true;
            }
            this.args = args;
            return onCmd();
        }
        if (sender instanceof ConsoleCommandSender c) {
            this.args = args;
            console = true;
            return onCmd();
        }
        return false;
    }

    public final boolean hasPermission(String node) {
        return player != null ? player.hasPermission(node) : sender.hasPermission(node);
    }

    public final boolean hasPermission(String node, Object... args) {
        return player != null ? player.hasPermission(node.formatted(args)) : sender.hasPermission(node.formatted(args));
    }

    public final boolean hasPermission(String node, String[] args) {
        return player != null ? player.hasPermission(node.formatted((Object[]) args)) : sender.hasPermission(node.formatted((Object[]) args));
    }

    public final boolean hasPermission() {
        return player != null ? player.hasPermission(command.getPermission()) : sender.hasPermission(command.getPermission());
    }

    public final void response(String message) {
        ChatUtils.response(sender, message);
    }

    public final void response(String message, Object... args) {
        ChatUtils.response(sender, message, args);
    }

    public final void response(String message, String[] args) {
        ChatUtils.response(sender, message, args);
    }

    public final void response(String[] messages) {
        ChatUtils.response(sender, messages);
    }
}
