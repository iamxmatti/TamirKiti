package me.xMatti.TamirKiti.listeners;

import me.xMatti.TamirKiti.TamirKitiMain;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;

public class KitListener implements Listener {

    private final TamirKitiMain plugin;

    public KitListener(TamirKitiMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.isRepairKit(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("cannot_place_kit"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();

        if (event.getInventory() instanceof AnvilInventory) {
            if (event.getSlot() == 2 && plugin.isRepairKit(event.getInventory().getItem(0))) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(plugin.getMessage("cannot_rename_kit"));
                return;
            }
        }

        if (cursorItem != null && plugin.isRepairKit(cursorItem)) {
            Inventory clickedInventory = event.getClickedInventory();

            if (clickedInventory != null && clickedInventory.getType() != InventoryType.PLAYER && clickedInventory.getType() != InventoryType.CRAFTING) {
                return;
            }

            if (currentItem != null && currentItem.getType() != Material.AIR) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();

                boolean isRepairableType = currentItem.getType().getMaxDurability() > 0;
                boolean isDamaged = isRepairableType && currentItem.getDurability() > 0;

                if (isDamaged) {
                    currentItem.setDurability((short) 0);
                    player.sendMessage(plugin.getMessage("item_repaired"));
                    playSuccessSound(player);

                    ItemStack remainingKit = cursorItem.clone();
                    if (remainingKit.getAmount() > 1) {
                        remainingKit.setAmount(remainingKit.getAmount() - 1);
                    } else {
                        remainingKit = null;
                    }
                    returnKitToInventory(player, remainingKit);

                } else if (isRepairableType) {
                    player.sendMessage(plugin.getMessage("item_already_repaired"));
                    playFailureSound(player);
                    returnKitToInventory(player, cursorItem);
                } else {
                    event.setCancelled(false);
                }
            }
        }
    }

    private void returnKitToInventory(Player player, ItemStack kit) {
        if (kit == null) {
            player.setItemOnCursor(null);
            return;
        }
        HashMap<Integer, ItemStack> couldNotFit = player.getInventory().addItem(kit);
        if (!couldNotFit.isEmpty()) {
            player.setItemOnCursor(couldNotFit.get(0));
        } else {
            player.setItemOnCursor(null);
        }
    }

    private void playSuccessSound(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String soundName = plugin.getConfig().getString("sounds.success.name", "ANVIL_USE").toUpperCase();
                    float volume = (float) plugin.getConfig().getDouble("sounds.success.volume", 1.0);
                    float pitch = (float) plugin.getConfig().getDouble("sounds.success.pitch", 1.2);
                    Sound sound = Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("config.yml'deki 'sounds.success.name' değeri geçersiz!");
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private void playFailureSound(Player player) {
        try {
            String soundName = plugin.getConfig().getString("sounds.failure.name", "NOTE_BASS").toUpperCase();
            float volume = (float) plugin.getConfig().getDouble("sounds.failure.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("sounds.failure.pitch", 0.8);
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("config.yml'deki 'sounds.failure.name' değeri geçersiz!");
        }
    }
}