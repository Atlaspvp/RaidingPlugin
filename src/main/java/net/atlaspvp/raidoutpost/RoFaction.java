package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoFaction implements InventoryHolder {

    private final RaidOutpost raidOutpost;
    private final Faction faction;
    private final Inventory inventory = Bukkit.createInventory(this, 27, "Raid Outpost rewards");
    private int captures;
    private int currentPhase;
    private long time;
    private boolean refreshPhase;
    private CaptureTimer captureTimer;

    public RoFaction(RaidOutpost raidOutpost, Faction faction, int captures, int currentPhase, long time, boolean refreshPhase) {
        this.raidOutpost = raidOutpost;
        this.faction = faction;
        this.captures = captures;
        this.currentPhase = currentPhase;
        this.time = time;
        this.refreshPhase = refreshPhase;
    }

    public void startCaptureTimer(long delay) {
        new CaptureTimer(raidOutpost, this, delay);
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

    public boolean isRefreshPhase() {
        return refreshPhase;
    }

    public void setRefreshPhase(boolean refreshPhase) {
        this.refreshPhase = refreshPhase;
    }
}

class CaptureTimer extends RealTimeRunnable {

    private final RaidOutpost raidOutpost;
    private final RoFaction roFaction;

    public CaptureTimer(RaidOutpost raidOutpost, RoFaction roFaction, long delay) {
        super(raidOutpost, delay, RealTimeRunnable.SYNC);
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
        new CaptureTimer(raidOutpost, roFaction, raidOutpost.getConfigRo().getPhaseInterval() * 50L);
    }
}
