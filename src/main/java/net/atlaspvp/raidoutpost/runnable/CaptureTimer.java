package net.atlaspvp.raidoutpost.runnable;

import com.massivecraft.factions.perms.Relation;
import net.atlaspvp.raidoutpost.FactionInventory;
import net.atlaspvp.raidoutpost.RaidOutpost;
import org.bukkit.scheduler.BukkitRunnable;

public class CaptureTimer extends BukkitRunnable {

    private final RaidOutpost raidOutpost;
    private final FactionInventory factionInventory;

    public CaptureTimer(FactionInventory factionInventory, RaidOutpost raidOutpost) {
        this.raidOutpost = raidOutpost;
        this.factionInventory = factionInventory;
        factionInventory.setCaptureTimer(this);

        factionInventory.setCurrentPhase(factionInventory.getCurrentPhase() + 1);
        factionInventory.getInventory().addItem(raidOutpost.getPhaseDataMap().get(factionInventory.getCurrentPhase()).getPhase());
        raidOutpost.getRo().setRelationWish(factionInventory.getFaction(), Relation.MEMBER);
    }

    @Override
    public void run() {
        if (factionInventory.getCurrentPhase() == 7) {
            new RegenRo(raidOutpost, raidOutpost.getRo(), null, raidOutpost.getConfigRo());
            factionInventory.setCurrentPhase(0);
            raidOutpost.getRo().setRelationWish(factionInventory.getFaction(), Relation.ENEMY);
            cancel();
            return;
        }
        new CaptureTimer(factionInventory, raidOutpost).runTaskLater(raidOutpost, raidOutpost.getConfigRo().getPhaseInterval());
    }

    public FactionInventory getFactionInventory() {
        return factionInventory;
    }
}
