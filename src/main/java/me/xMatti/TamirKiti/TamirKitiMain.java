package me.xMatti.TamirKiti;

import me.xMatti.TamirKiti.commands.GiveKitCommand;
import me.xMatti.TamirKiti.commands.ReloadCommand;
import me.xMatti.TamirKiti.commands.TamirKitiCommand;
import me.xMatti.TamirKiti.database.DatabaseManager;
import me.xMatti.TamirKiti.listeners.KitListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TamirKitiMain extends JavaPlugin {

    public final Map<UUID, Long> cooldowns = new HashMap<>();
    private DatabaseManager databaseManager;
    private static TamirKitiMain instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
            databaseManager.createTable();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, ChatColor.RED + "Veritabanı başlatılamadı! TamirKiti eklentisi devre dışı bırakılıyor.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        cooldowns.putAll(databaseManager.loadAllCooldowns());
        getLogger().info(ChatColor.AQUA + "" + cooldowns.size() + " oyuncunun bekleme süresi verisi veritabanından yüklendi.");

        getCommand("tamirkiti").setExecutor(new TamirKitiCommand(this));
        getCommand("tamirkitireload").setExecutor(new ReloadCommand(this));
        getCommand("tamirkitiver").setExecutor(new GiveKitCommand(this));
        getServer().getPluginManager().registerEvents(new KitListener(this), this);

        getLogger().info(ChatColor.GREEN + "" + ChatColor.BOLD + "TamirKiti eklentisi başarıyla etkinleştirildi!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info(ChatColor.RED + "TamirKiti eklentisi devre dışı bırakıldı.");
    }

    public void updatePlayerCooldown(UUID uuid, long timestamp) {
        cooldowns.put(uuid, timestamp);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (databaseManager != null) {
                    databaseManager.setPlayerCooldown(uuid, timestamp);
                }
            }
        }.runTaskAsynchronously(this);
    }

    private Material getConfiguredMaterial() {
        String materialName = getConfig().getString("repair_kit.material", "PAPER").toUpperCase();
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            getLogger().warning(ChatColor.YELLOW + "config.yml dosyasındaki 'repair_kit.material' değeri ('" + materialName + "') geçersiz! Varsayılan olarak KAĞIT (PAPER) kullanılıyor.");
            return Material.PAPER;
        }
        return material;
    }

    public ItemStack createRepairKitItem() {
        FileConfiguration config = getConfig();
        Material kitMaterial = getConfiguredMaterial();
        ItemStack kit = new ItemStack(kitMaterial, 1);
        ItemMeta meta = kit.getItemMeta();

        String displayName = ChatColor.translateAlternateColorCodes('&', config.getString("repair_kit.name", "&b&lTamir Kiti"));
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        for (String line : config.getStringList("repair_kit.lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);

        if (config.getBoolean("repair_kit.glow", true)) {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        kit.setItemMeta(meta);
        return kit;
    }

    public boolean isRepairKit(ItemStack item) {
        Material configuredMaterial = getConfiguredMaterial();
        if (item == null || item.getType() != configuredMaterial || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }

        FileConfiguration config = getConfig();
        String configItemName = ChatColor.translateAlternateColorCodes('&', config.getString("repair_kit.name"));
        List<String> configItemLore = config.getStringList("repair_kit.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());

        return meta.getDisplayName().equals(configItemName) && meta.getLore().equals(configItemLore);
    }

    public static TamirKitiMain getInstance() {
        return instance;
    }

    public String getMessage(String path, String... replacements) {
        FileConfiguration config = getConfig();
        String messagePath = "messages." + path;
        String message = config.getString(messagePath);

        if (message == null) {
            getLogger().warning(ChatColor.YELLOW + "config.yml dosyasında '" + messagePath + "' mesaj yolu bulunamadı! Lütfen config dosyanızı kontrol edin.");
            return "";
        }

        String prefix = config.getString("messages.prefix", "&8[&bTamirKiti&8] &r");

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        String finalMessage = prefix + message;
        return ChatColor.translateAlternateColorCodes('&', finalMessage);
    }

    public long getCooldownForPlayer(Player player) {
        FileConfiguration config = getConfig();
        long lowestCooldown = config.getLong("cooldowns.default", 604800L) * 1000;
        boolean foundRankCooldown = false;

        if (config.isConfigurationSection("cooldowns")) {
            for (String rankKey : config.getConfigurationSection("cooldowns").getKeys(false)) {
                if (rankKey.equalsIgnoreCase("default")) continue;

                if (player.hasPermission("tamirkiti.rank." + rankKey.toLowerCase())) {
                    long rankCooldown = config.getLong("cooldowns." + rankKey, lowestCooldown / 1000) * 1000;
                    if (!foundRankCooldown || rankCooldown < lowestCooldown) {
                        lowestCooldown = rankCooldown;
                        foundRankCooldown = true;
                    }
                }
            }
        }
        return lowestCooldown;
    }

    public String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long days = millis / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" gün ");
        if (hours > 0) sb.append(hours).append(" saat ");
        if (minutes > 0) sb.append(minutes).append(" dakika ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append(" saniye");

        return sb.toString().trim();
    }
}