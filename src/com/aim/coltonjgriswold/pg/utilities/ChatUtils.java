package com.aim.coltonjgriswold.pg.utilities;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class ChatUtils {

    public static void response(CommandSender player, String message) {
        response(player, message, "");
    }

    public static void response(CommandSender player, String message, Object... args) {
        player.sendMessage(color("&8&l[&6ParticleGrenades&8&l] %1$s".formatted(message).formatted(args)));
    }

    public static void response(CommandSender player, String message, String[] args) {
        response(player, message, (Object[]) args);
    }

    public static void response(CommandSender player, String[] messages) {
        messages[0] = color("&8&l[&6ParticleGrenades&8&l] %1$s".formatted(messages[0]));
        IntStream.range(1, messages.length).forEach(n -> messages[n] = color(messages[n]));
        player.sendMessage(messages);
    }

    public static String color(String string) {
        try {
            Class<?> chatColor;
            Class.forName("net.md_5.bungee.api.ChatColor");
            chatColor = Class.forName("net.md_5.bungee.api.ChatColor");
            Method of = chatColor.getMethod("of", String.class);
            Pattern p = Pattern.compile("(#[0-9a-fA-F]{6})");
            Matcher m = p.matcher(string);
            while (m.find()) {
                String a = m.group();
                string = string.replace(a, of.invoke(null, a).toString());
            }
            return ChatColor.translateAlternateColorCodes('&', string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
