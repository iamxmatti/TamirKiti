package me.xMatti.TamirKiti.commands;

import me.xMatti.TamirKiti.TamirKitiMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TamirKitiCommand implements CommandExecutor {

    private final TamirKitiMain plugin;

    public TamirKitiCommand(TamirKitiMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("must_be_player"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tamirkiti.command.use")) {
            player.sendMessage(plugin.getMessage("no_permission_command"));
            return true;
        }

        if (!player.hasPermission("tamirkiti.bypass.cooldown")) {
            long playerRankCooldownMillis = plugin.getCooldownForPlayer(player);

            long lastUsedTimestamp = plugin.cooldowns.getOrDefault(player.getUniqueId(), 0L);
            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis - lastUsedTimestamp < playerRankCooldownMillis) {
                long timeLeftMillis = playerRankCooldownMillis - (currentTimeMillis - lastUsedTimestamp);
                player.sendMessage(plugin.getMessage("cooldown_active", "%time%", plugin.formatTime(timeLeftMillis)));
                return true;
            }
            plugin.updatePlayerCooldown(player.getUniqueId(), currentTimeMillis);
        }

        ItemStack repairKit = plugin.createRepairKitItem();
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), repairKit);
            player.sendMessage(plugin.getMessage("kit_received") + " (Envanterin dolu olduğu için yere bırakıldı)");
        } else {
            player.getInventory().addItem(repairKit);
            player.sendMessage(plugin.getMessage("kit_received"));
        }

        return true;
    }
}