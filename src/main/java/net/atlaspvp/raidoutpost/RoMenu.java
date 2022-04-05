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
    private final ItemStack map = new ItemStack(Material.MAP);
    private final ItemStack chest = new ItemStack(Material.CHEST);
    private final ItemStack barrier = new ItemStack(Material.BARRIER);
    private final List<String> possibleArg = List.of("1", "2", "3", "4", "5", "6", "7");

    public RoMenu(RaidOutpost raidOutpost) {
        this.raidOutpost = raidOutpost;
        prepareItems();
    }

    @Override
    public boolean onCommand(@org.jetbrains.annotations.NotNull CommandSender commandSender, @org.jetbrains.annotations.NotNull Command command, @org.jetbrains.annotations.NotNull String string, @org.jetbrains.annotations.NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to execute this command!");
        } else if (strings.length == 0) {
            player.openInventory(inventory);
        } else if (strings.length == 1 && player.hasPermission("raidoutpost.admin") && possibleArg.contains(strings[0])) {
            int phase = Integer.parseInt(strings[0]);
            raidOutpost.getPhaseDataMap().get(phase).updateLore(getSlot(phase), player.getItemInHand(), getInventory(), false);
        } else if (strings.length == 2 && strings[1].equalsIgnoreCase("clear") && possibleArg.contains(strings[0]) && player.hasPermission("raidoutpost.admin")) {
            int phase = Integer.parseInt(strings[0]);
            raidOutpost.getPhaseDataMap().get(phase).updateLore(getSlot(phase), null, getInventory(), true);
        } else {
            Utils.sendRoMessage(player, "Wrong usage");
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

        ItemMeta chestMeta = chest.getItemMeta();
        chestMeta.setDisplayName(ChatColor.WHITE + "Raid outpost rewards");
        chest.setItemMeta(chestMeta);
        inventory.setItem(13, chest);

        ItemMeta mapMeta = map.getItemMeta();
        mapMeta.setDisplayName(ChatColor.RESET + "Capture leaderboard");
        map.setItemMeta(mapMeta);
        inventory.setItem(15, map);

        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(ChatColor.WHITE + "Raid outpost captured");
        barrier.setItemMeta(barrierMeta);

        byte slot = 28;
        for (int i = 1; i < 8; i++) {
            getInventory().setItem(slot, raidOutpost.getPhaseDataMap().get(i).getPhase());
            slot++;
        }
    }

    private int getSlot(int phase) {
        switch (phase) {
            case 1 -> {return 28;}
            case 2 -> {return 29;}
            case 3 -> {return 30;}
            case 4 -> {return 31;}
            case 5 -> {return 32;}
            case 6 -> {return 33;}
            case 7 -> {return 34;}
            default -> {return 0;}
        }
    }

    public void closeRo(int noWildTeleport) {
        inventory.setItem(11, barrier);
        new BukkitRunnable() {
            @Override
            public void run() {
                inventory.setItem(11, tnt);
                raidOutpost.setRoLockdown(false);
            }
        }.runTaskLater(raidOutpost, noWildTeleport);
    }

    public ItemStack getTnt() {
        return tnt;
    }

    public ItemStack getMap() {
        return map;
    }
}
