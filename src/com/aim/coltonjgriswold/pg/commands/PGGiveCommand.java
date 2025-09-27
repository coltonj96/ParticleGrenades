package com.aim.coltonjgriswold.pg.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aim.coltonjgriswold.pg.commands.abstraction.PGTabCommand;
import com.aim.coltonjgriswold.pg.utilities.GrenadeUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.aim.coltonjgriswold.pg.utilities.Grenade;

public class PGGiveCommand extends PGTabCommand {

    @Override
    public boolean onCmd() {
        if (player != null) {
            if (args.length == 1 || args.length == 2) {
                if (Grenade.contains(args[0].toLowerCase())) {
                    Grenade g = Grenade.getByKey(args[0].toLowerCase());
                    if (!hasPermission(g.getGivePerm())) {
                        response("&cYou do not have permission!");
                    } else {
                        int amount = Material.TNT.getMaxStackSize();
                        if (args.length == 2) {
                            try {
                                amount = Integer.parseInt(args[1]);
                                if (amount < 1) {
                                    response("&cSecond argument must be a positive number!");
                                    return true;
                                }
                            } catch (NumberFormatException ex) {
                                response("&cSecond argument must be a number!");
                            }
                        }
                        ItemStack item = GrenadeUtils.createGrenade(g);
                        item.setAmount(amount);
                        HashMap<Integer, ItemStack> map = player.getInventory().addItem(item);
                        if (!map.isEmpty())
                            response("&aSuccesfully gave &l%1$s &aof &l%2$s!", amount - map.values().iterator().next().getAmount(), g.getName());
                        else
                            response("&aSuccesfully gave &l%1$s &aof &l%2$s!", amount, g.getName());
                    }
                } else {
                    response("&cInvalid grenade!");
                }
            } else {
                response("&cUsage: /pggive <grenade> [amount]");
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
