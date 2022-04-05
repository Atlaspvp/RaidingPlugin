package net.atlaspvp.raidoutpost;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.Configuration;

import java.util.Objects;

public class Config {

    private World raidWorld;
    private final World spawnWorld;
    private final String raidWorldName;

    private final int teleportCooldown;
    private final int timeout;
    private final int factionLockdownTimer;
    private final int lockWildTeleport;
    private final int roRegenInterval;
    private final int phaseInterval;
    private final int saveRoFactionTimerAndItemStacks;

    private final String startMSGTarget;
    private final String startMSGRaider;
    private final String endMSGTarget;

    private final boolean preventMining;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    private final String sourceFolder;
    private final String targetFolder;
    private final String serverFolder;

    public Config(RaidOutpost raidOutpost, Configuration config) {
        config.addDefault("ro-world", "ro");
        config.addDefault("spawn-world", "world");

        config.addDefault("faction-lockdown-check-timer (ticks)", 200);
        config.addDefault("faction-lockdown (ticks)", 12000);
        config.addDefault("ro-gui-teleport-lock-time (ticks)", 2400);
        config.addDefault("ro-regen-interval (ticks)", 1200);
        config.addDefault("teleport-cooldown (milliseconds)", 300000);
        config.addDefault("phase-interval (ticks)", 72000);
        config.addDefault("save-ro-faction-and-itemstacks-timer (ticks)", 72000);

        config.addDefault("prevent-mining-spawner", true);

        config.addDefault("start-defend-msg", "has started a raid on you!");
        config.addDefault("end-defend-msg", "is no longer raiding you");
        config.addDefault("start-raid-msg", "Your faction started a raid on");

        config.addDefault("raiding-outpost-minX", -20);
        config.addDefault("raiding-outpost-maxX", 20);
        config.addDefault("raiding-outpost-minY", 100);
        config.addDefault("raiding-outpost-maxY", 200);
        config.addDefault("raiding-outpost-minZ", -20);
        config.addDefault("raiding-outpost-maxZ", 20);

        config.addDefault("source-folder", "");
        config.addDefault("target-folder", "");
        config.addDefault("server-folder", "");
        config.options().copyDefaults(true);
        raidOutpost.saveConfig();

        raidWorld = Bukkit.createWorld(new WorldCreator(Objects.requireNonNull(config.getString("ro-world"))));
        spawnWorld = Bukkit.createWorld(new WorldCreator(Objects.requireNonNull(config.getString("spawn-world"))));
        raidWorldName = config.getString("ro-world");

        timeout = config.getInt("faction-lockdown (ticks)");
        factionLockdownTimer = config.getInt("faction-lockdown-check-timer (ticks)");
        lockWildTeleport = config.getInt("ro-gui-teleport-lock-time (ticks)");
        teleportCooldown = config.getInt("teleport-cooldown (milliseconds)");
        roRegenInterval = config.getInt("ro-regen-interval (ticks)");
        phaseInterval = config.getInt("phase-interval (ticks)");
        saveRoFactionTimerAndItemStacks = config.getInt("save-ro-faction-and-itemstacks-timer (ticks)");

        startMSGTarget = config.getString("start-defend-msg");
        startMSGRaider = config.getString("start-raid-msg");
        endMSGTarget = config.getString("end-defend-msg");

        preventMining = config.getBoolean("prevent-mining-spawner");

        minX = config.getInt("raiding-outpost-minX");
        maxX = config.getInt("raiding-outpost-maxX");
        minY = config.getInt("raiding-outpost-minY");
        maxY = config.getInt("raiding-outpost-maxY");
        minZ = config.getInt("raiding-outpost-minZ");
        maxZ = config.getInt("raiding-outpost-maxZ");

        sourceFolder = config.getString("source-folder");
        targetFolder = config.getString("target-folder");
        serverFolder = config.getString("server-folder");
    }

    public World getRaidWorld() {
        return raidWorld;
    }

    public void setRaidWorld(World raidWorld) {
        this.raidWorld = raidWorld;
    }

    public World getSpawnWorld() {
        return spawnWorld;
    }

    public String getRaidWorldName() {
        return raidWorldName;
    }

    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getFactionLockdownTimer() {
        return factionLockdownTimer;
    }

    public int getLockWildTeleport() {
        return lockWildTeleport;
    }

    public int getRoRegenInterval() {
        return roRegenInterval;
    }

    public int getPhaseInterval() {
        return phaseInterval;
    }

    public int getSaveRoFactionTimer() {
        return saveRoFactionTimerAndItemStacks;
    }

    public String getStartMSGTarget() {
        return startMSGTarget;
    }

    public String getStartMSGRaider() {
        return startMSGRaider;
    }

    public String getEndMSGTarget() {
        return endMSGTarget;
    }

    public boolean isPreventMining() {
        return preventMining;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getServerFolder() {
        return serverFolder;
    }
}
