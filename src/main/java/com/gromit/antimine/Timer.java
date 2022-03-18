package com.gromit.antimine;

import com.massivecraft.factions.Faction;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.configuration.Configuration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;

public class Timer extends BukkitRunnable {

    private final long timeout;
    private final String endMSGTarget;
    private final Object2LongOpenHashMap<Faction> raidMap;

    public Timer(Configuration config, Object2LongOpenHashMap<Faction> raidMap) {
        this.timeout = config.getLong("time-no-explosion-raid-stop-ticks");
        this.endMSGTarget = config.getString("end-raid-msg");
        this.raidMap = raidMap;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<Faction, Long>> iterator = raidMap.entrySet().iterator();

        while (iterator.hasNext()){
            Map.Entry<Faction, Long> pair = iterator.next();
            iterator.remove(); // avoids a ConcurrentModificationException

            if ((System.currentTimeMillis() - pair.getValue()) > timeout){

                raidMap.remove(pair.getKey());

                pair.getKey().sendMessage(endMSGTarget);
            }
        }
    }
}
