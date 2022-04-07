package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.MojangsonParser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static <T extends HumanEntity> void sendRoMessage(T player, String message) {
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.LIGHT_PURPLE +  "RaidOutpost" + ChatColor.WHITE + "] " + ChatColor.RESET + message);
    }

    public static boolean isCooldown(long currentTime, long cooldown) {
        return currentTime > cooldown;
    }

    public static boolean isInsideXZ(Location location, Config config, int margin) {
        return location.getX() > config.getMinX() - margin && location.getX() < config.getMaxX() + margin && location.getZ() > config.getMinZ() - margin && location.getZ() < config.getMaxZ() + margin;
    }

    public static boolean isInsideXYZ(Location location, Config config, int margin) {
        return location.getY() <= config.getMaxY() + margin && location.getY() >= config.getMinY() - margin && location.getX() <= config.getMaxX() + margin && location.getX() >= config.getMinX() - margin && location.getZ() <= config.getMaxZ() + margin && location.getZ() >= config.getMinZ() - margin;
    }

    public static Object[] generateStringArray(List<ItemStack> itemStackList) {
        List<String> stringList = new ArrayList<>();
        for (ItemStack itemStack : itemStackList) {
            stringList.add(Utils.writePersistentItemStack(itemStack));
        }
        return stringList.toArray(new Object[0]);
    }

    public static Object[] generateStringArray1(Inventory inventory) {
        List<String> stringList = new ArrayList<>();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) continue;
            stringList.add(Utils.writePersistentItemStack(itemStack));
        }
        return stringList.toArray(new String[0]);
    }

    public static String writePersistentItemStack(ItemStack itemStack) {
        return itemStack.getType() + "__" + itemStack.getAmount() + "__" + CraftItemStack.asNMSCopy(itemStack).getTag();
    }

    public static ItemStack readPersistentString(String string) {
        String[] strings = string.split("__", 3);
        ItemStack itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(strings[0])));
        itemStack.setAmount(Integer.parseInt(strings[1]));
        if (!strings[2].equalsIgnoreCase("null")) {
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            try {
                nmsItem.setTag(MojangsonParser.parse(strings[2]));
            } catch (CommandSyntaxException e) {e.printStackTrace();}
            return CraftItemStack.asBukkitCopy(nmsItem);
        } else return itemStack;
    }

    public static void giveRewards(RaidOutpost raidOutpost, Inventory inventory, int phase) {
        List<ItemStack> itemStackList = raidOutpost.getPhaseDataMap().get(phase).getItemStackList();
        for (ItemStack itemStack : itemStackList) {
            inventory.addItem(itemStack);
        }
    }

    public static void startCapture(RaidOutpost raidOutpost, RoFaction roFaction, Faction spawnFaction) {
        roFaction.setRefreshPhase(true);
        raidOutpost.setCurrentRoFaction(roFaction);
        setTntLore(raidOutpost, roFaction, raidOutpost.getLore());
        setMapLore(raidOutpost, roFaction);
        roFaction.setCaptures(roFaction.getCaptures() + 1);
        roFaction.setCurrentPhase(1);
        if (roFaction.getTime() == 0) {
            roFaction.setTime(raidOutpost.getConfigRo().getPhaseInterval() * 50L);
        }
        giveRewards(raidOutpost, roFaction.getInventory(), 1);
        roFaction.startCaptureTimer(raidOutpost.getConfigRo().getPhaseInterval() * 50L);
        Runnable.regenRo(raidOutpost, spawnFaction);
    }

    public static void stopCapture(RaidOutpost raidOutpost, RoFaction roFaction, CaptureTimer captureTimer) {
        captureTimer.stop();
        roFaction.setCurrentPhase(0);
        roFaction.setTime(0);
        raidOutpost.getRoMenu().getTnt().setLore(null);
        raidOutpost.setCurrentRoFaction(null);
        roFaction.setRefreshPhase(false);
    }

    public static void autoStopCapture(RaidOutpost raidOutpost, RoFaction roFaction, CaptureTimer captureTimer) {
        roFaction.setCurrentPhase(0);
        roFaction.setTime(0);
        raidOutpost.getRoMenu().getTnt().setLore(null);
        captureTimer.stop();
        roFaction.removeCaptureTimer();
        Runnable.regenRo(raidOutpost, null);
        raidOutpost.setCurrentRoFaction(null);
        roFaction.setRefreshPhase(false);
    }

    public static void refreshCapturePhase(RaidOutpost raidOutpost, RoFaction roFaction) {
        roFaction.setTime(raidOutpost.getConfigRo().getPhaseInterval() * 50L);
        roFaction.setCurrentPhase(roFaction.getCurrentPhase() + 1);
        giveRewards(raidOutpost, roFaction.getInventory(), roFaction.getCurrentPhase());
    }

    public static void refreshCapturePhaseDatabase(RaidOutpost raidOutpost, RoFaction roFaction) {
        setTntLore(raidOutpost, roFaction, raidOutpost.getLore());
        raidOutpost.setCurrentRoFaction(roFaction);
        if (raidOutpost.getConfigRo().getPhaseInterval() - roFaction.getTime() / 50 <= raidOutpost.getConfigRo().getRoRegenInterval() && roFaction.getCurrentPhase() == 1) {
            Runnable.regenRo(raidOutpost, null);
        }
        roFaction.startCaptureTimer(roFaction.getTime());
    }

    public static void setTntLore(RaidOutpost raidOutpost, RoFaction roFaction, List<String> lore) {
        ItemStack tnt = raidOutpost.getRoMenu().getTnt();
        lore.clear();
        lore.add(ChatColor.GRAY + "Controlled by: " + roFaction.getFaction().getTag());
        tnt.setLore(lore);
        ItemStack barrier = raidOutpost.getRoMenu().getInventory().getItem(11);
        if (barrier != null && tnt.getType() != Material.BARRIER) {
            raidOutpost.getRoMenu().getInventory().setItem(11, tnt);
        }
    }

    public static void setMapLore(RaidOutpost raidOutpost, RoFaction roFaction) {
        ItemStack map = raidOutpost.getRoMenu().getMap();
        List<String> lore1 = map.getLore();
        if (lore1 == null) {
            lore1 = new ArrayList<>();
        }
        lore1.add(ChatColor.GRAY + roFaction.getFaction().getTag() + ": " + roFaction.getCaptures());
        map.setLore(lore1);
        raidOutpost.getRoMenu().getInventory().setItem(15, map);
    }
}
