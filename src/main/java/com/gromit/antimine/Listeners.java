package com.gromit.antimine;

import com.massivecraft.factions.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.gromit.antimine.Antimine.*;

public class Listeners implements Listener {


    public static HashMap<Faction, Long> raidMap = new HashMap<>();
    private final Plugin plugin;
    private final World raidWorld = raidOutpost;


    private final Faction fWild = Factions.getInstance().getFactionById("0");


    public Listeners(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        if (!(event.getEntity() instanceof TNTPrimed)) return;

        if(event.getEntity().getWorld().equals(raidWorld))

        if (event.blockList().isEmpty()) return;

        Location eventLoc = event.getLocation();
        FLocation eventLocation = new FLocation(eventLoc);
        Faction eventFaction = Board.getInstance().getFactionAt(eventLocation);

        if (eventFaction.equals(fWild)) return;


        TNTPrimed tntPrimed = (TNTPrimed) event.getEntity();

        Faction spawnFaction = Board.getInstance().getFactionAt(new FLocation(tntPrimed.getSpawnLocation()));

        if (eventFaction.equals(spawnFaction)) return;

        long currentTime = System.currentTimeMillis();


        if (raidMap.containsKey(eventFaction)){
            long lastEntry = raidMap.get(eventFaction);

            if(currentTime - lastEntry < 100) return;

            List<Block> blockList = event.blockList();

            if (blockList.contains(eventLoc.getBlock())){
                raidMap.put(eventFaction, currentTime);
            }

        } else{

            raidMap.put(eventFaction, currentTime);

            spawnFaction.sendMessage(startMSGraider);

            eventFaction.sendMessage(startMSGtarget);

        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (preventMining && event.getBlock().getType().equals(Material.SPAWNER) && raidMap.containsKey(FPlayers.getInstance().getByPlayer(event.getPlayer()).getFaction())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine spawners while being raided!");
        }
    }
}
