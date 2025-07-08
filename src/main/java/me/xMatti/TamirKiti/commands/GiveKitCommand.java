package me.xMatti.TamirKiti.commands;

import me.xMatti.TamirKiti.TamirKitiMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveKitCommand implements CommandExecutor {

    private final TamirKitiMain plugin;

    public GiveKitCommand(TamirKitiMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tamirkiti.command.give")) {
            sender.sendMessage(plugin.getMessage("no_permission_command"));
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(plugin.getMessage("invalid_usage_give"));
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sender.sendMessage(plugin.getMessage("player_not_online"));
            return true;
        }

        int amount = 1;
        if (args.length == 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) {
                    amount = 1;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("not_a_number"));
                return true;
            }
        }

        ItemStack repairKit = plugin.createRepairKitItem();
        repairKit.setAmount(amount);

        if (targetPlayer.getInventory().firstEmpty() == -1) {
            targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), repairKit);
            targetPlayer.sendMessage(plugin.getMessage("kit_received_from_admin", "%amount%", String.valueOf(amount)) + " (Envanterin doluydu, yere bırakıldı!)");
        } else {
            targetPlayer.getInventory().addItem(repairKit);
            targetPlayer.sendMessage(plugin.getMessage("kit_received_from_admin", "%amount%", String.valueOf(amount)));
        }

        sender.sendMessage(plugin.getMessage("kit_given_to_player", "%player%", targetPlayer.getName(), "%amount%", String.valueOf(amount)));
        return true;
    }
}