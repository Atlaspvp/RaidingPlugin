package net.atlaspvp.raidoutpost;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;

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
}
