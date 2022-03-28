package net.atlaspvp.raidoutpost;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RoMenu implements CommandExecutor, InventoryHolder {

    private final RaidOutpost raidOutpost;
    private final Inventory inventory = Bukkit.createInventory(this, 45, "Raid Outpost");
    private final ItemStack tnt = new ItemStack(Material.TNT);
    private final ItemStack emerald = new ItemStack(Material.EMERALD);
    private final ItemStack map = new ItemStack(Material.MAP);
    private final ItemStack barrier = new ItemStack(Material.BARRIER);
    private final List<Integer> indexes = List.of(28, 29, 30, 31, 32, 33, 34);

    public RoMenu(RaidOutpost raidOutpost) {
        this.raidOutpost = raidOutpost;
        this.prepareItems();
    }

    @Override
    public boolean onCommand(@org.jetbrains.annotations.NotNull CommandSender commandSender, @org.jetbrains.annotations.NotNull Command command, @org.jetbrains.annotations.NotNull String string, @org.jetbrains.annotations.NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to execute this command!");
        } else {
            player.openInventory(inventory);
        }
        return true;
    }

    @Override
    public @org.jetbrains.annotations.NotNull Inventory getInventory() {
        return inventory;
    }

    private void prepareItems() {
        ItemMeta tntMeta = tnt.getItemMeta();
        tntMeta.setDisplayName(ChatColor.RESET + "Teleport to raid outpost");
        tnt.setItemMeta(tntMeta);
        inventory.setItem(11, tnt);

        ItemMeta mapMeta = map.getItemMeta();
        mapMeta.setDisplayName(ChatColor.RESET + "Capture leaderboard");
        map.setItemMeta(mapMeta);
        inventory.setItem(15, map);

        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(ChatColor.WHITE + "Raid outpost captured");
        barrier.setItemMeta(barrierMeta);

        for (int index : this.indexes) {
            inventory.setItem(index, emerald);
        }
    }

    public void closeRo(int noWildTeleport) {
        inventory.setItem(11, barrier);
        new BukkitRunnable() {
            @Override
            public void run() {
                inventory.setItem(11, tnt);
            }
        }.runTaskLater(raidOutpost, noWildTeleport);
    }
}
