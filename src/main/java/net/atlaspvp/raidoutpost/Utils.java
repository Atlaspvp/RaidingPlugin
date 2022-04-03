package net.atlaspvp.raidoutpost;

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

    public static Object[] generateIntegerArray(Inventory inventory) {
        List<Integer> integers = new ArrayList<>();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) continue;
            integers.add(Integer.parseInt(itemStack.getItemMeta().getDisplayName().replaceAll("\\D+", "")));
        }
        return integers.toArray(new Integer[0]);
    }

    public static Object[] generateStringArray(List<ItemStack> itemStackList) {
        List<String> stringList = new ArrayList<>();
        for (ItemStack itemStack : itemStackList) {
            stringList.add(Utils.writePersistentItemStack(itemStack));
        }
        return stringList.toArray(new Object[0]);
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
}
