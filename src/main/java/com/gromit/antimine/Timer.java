package com.gromit.antimine;

import com.massivecraft.factions.Faction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Timer extends BukkitRunnable {


    String endMSGtargetMSGtarget = Antimine.endMSGtarget;
    long timeOut = Antimine.timeNoBoom;



    @Override
    public void run() {


        final HashMap<Faction, Long> raidMapNew = Listeners.raidMap;

        Iterator<Map.Entry<Faction, Long>> iterator = raidMapNew.entrySet().iterator();

        while (iterator.hasNext()){
            Map.Entry<Faction, Long> pair = iterator.next();
            iterator.remove(); // avoids a ConcurrentModificationException


            if ((System.currentTimeMillis() - pair.getValue()) > timeOut ){

                Listeners.raidMap.remove(pair.getKey());

                pair.getKey().sendMessage(endMSGtargetMSGtarget);
            }
        }
    }
}
