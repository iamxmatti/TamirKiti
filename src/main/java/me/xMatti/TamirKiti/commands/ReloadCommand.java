package me.xMatti.TamirKiti.commands;

import me.xMatti.TamirKiti.TamirKitiMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.logging.Level;

public class ReloadCommand implements CommandExecutor {

    private final TamirKitiMain plugin;

    public ReloadCommand(TamirKitiMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tamirkiti.command.reload")) {
            sender.sendMessage(plugin.getMessage("no_permission_command"));
            return true;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(plugin.getMessage("reload_success"));
        } catch (Exception e) {
            sender.sendMessage(plugin.getMessage("reload_fail", "%error%", e.getMessage()));
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "Config yeniden yüklenirken bir hata oluştu!", e);
        }
        return true;
    }
}