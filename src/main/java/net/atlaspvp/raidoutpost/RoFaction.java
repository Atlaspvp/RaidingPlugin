package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoFaction implements InventoryHolder {

    private final RaidOutpost raidOutpost;
    private final Faction faction;
    private final Inventory inventory = Bukkit.createInventory(this, 27, "Raid Outpost rewards");
    private int captures;
    private int currentPhase;
    private long time;
    private CaptureTimer captureTimer;

    public RoFaction(RaidOutpost raidOutpost, Faction faction, int captures, int currentPhase, long time) {
        this.raidOutpost = raidOutpost;
        this.faction = faction;
        this.captures = captures;
        this.currentPhase = currentPhase;
        this.time = time;
    }

    public void startCaptureTimer(long ticks) {
        new CaptureTimer(raidOutpost, this).runTaskLater(raidOutpost, ticks);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public int getCaptures() {
        return captures;
    }

    public void setCaptures(int captures) {
        this.captures = captures;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(int currentPhase) {
        this.currentPhase = currentPhase;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public @Nullable CaptureTimer getCaptureTimer() {
        return captureTimer;
    }

    public void setCaptureTimer(CaptureTimer captureTimer) {
        this.captureTimer = captureTimer;
    }

    public Faction getFaction() {
        return faction;
    }

    public void removeCaptureTimer() {captureTimer = null;};
}

class CaptureTimer extends BukkitRunnable {

    private final RaidOutpost raidOutpost;
    private final RoFaction roFaction;

    public CaptureTimer(RaidOutpost raidOutpost, RoFaction roFaction) {
        this.raidOutpost = raidOutpost;
        this.roFaction = roFaction;
        this.roFaction.setCaptureTimer(this);
    }

    @Override
    public void run() {
        Utils.refreshCapturePhase(raidOutpost, roFaction);
        if (roFaction.getCurrentPhase() == 7) {
            Utils.autoStopCapture(raidOutpost, roFaction, this);
            return;
        }
        new CaptureTimer(raidOutpost, roFaction).runTaskLater(raidOutpost, raidOutpost.getConfigRo().getPhaseInterval());
    }
}
