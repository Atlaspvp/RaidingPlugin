package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import net.atlaspvp.raidoutpost.runnable.CaptureTimer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FactionInventory implements InventoryHolder {

    private final Inventory inventory = Bukkit.createInventory(this, 27, "Raid Outpost rewards");
    private int captures;
    private int currentPhase;
    private long time;
    private CaptureTimer captureTimer;
    private final Faction faction;

    public FactionInventory(int captures, int currentPhase, long time, Faction faction) {
        this.captures = captures;
        this.currentPhase = currentPhase;
        this.time = time;
        this.faction = faction;
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
}
