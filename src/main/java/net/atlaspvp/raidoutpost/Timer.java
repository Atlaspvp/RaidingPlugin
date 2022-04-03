package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {

    private final RaidOutpost raidOutpost;

    public Timer(RaidOutpost raidOutpost) {
        this.raidOutpost = raidOutpost;
    }

    @Override
    public void run() {
        ObjectIterator<Object2LongMap.Entry<Faction>> iterator = raidOutpost.getRaidMap().object2LongEntrySet().fastIterator();

        while (iterator.hasNext()){
            Object2LongMap.Entry<Faction> pair = iterator.next();
            iterator.remove(); // avoids a ConcurrentModificationException

            if (Utils.isCooldown(System.currentTimeMillis(), pair.getLongValue() + raidOutpost.getConfigRo().getTimeout())) {
                Faction defendFaction = pair.getKey();
                raidOutpost.getRaidMap().remove(defendFaction);
                defendFaction.sendMessage(raidOutpost.getConfigRo().getEndMSGTarget());
            }
        }
    }
}
