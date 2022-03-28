package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {

    private final Object2LongOpenHashMap<Faction> raidMap;
    private final Config config;

    public Timer(Object2LongOpenHashMap<Faction> raidMap, Config config) {
        this.raidMap = raidMap;
        this.config = config;
    }

    @Override
    public void run() {
        ObjectIterator<Object2LongMap.Entry<Faction>> iterator = raidMap.object2LongEntrySet().fastIterator();

        while (iterator.hasNext()){
            Object2LongMap.Entry<Faction> pair = iterator.next();
            iterator.remove(); // avoids a ConcurrentModificationException

            if (Utils.isCooldown(System.currentTimeMillis(), pair.getLongValue() + config.getTimeout())) {
                Faction defendFaction = pair.getKey();
                raidMap.remove(defendFaction);
                defendFaction.sendMessage(config.getEndMSGTarget());
            }
        }
    }
}
