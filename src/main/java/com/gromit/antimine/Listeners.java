package com.gromit.antimine;

import com.massivecraft.factions.*;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
    public static long lastBreachTimeMS = 0;


    private final Plugin plugin;
    private final World raidWorld = raidOutpost;

    private final Faction raidingOutpostFaction = Factions.getInstance().getByTag("RaidingOutpost");


    private final Faction fWild = Factions.getInstance().getFactionById("0");

    private final int minY;
    private final int maxY;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;


    public Listeners(Plugin plugin, int minY, int maxY, int minX, int maxX, int minZ, int maxZ ) {
        this.plugin = plugin;
        this.minY = minY;
        this.maxY = maxY;
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;


    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        List<Block> blockList = event.blockList();
        if (blockList.isEmpty()) return;

        Entity entity = event.getEntity();

        if (!(entity instanceof TNTPrimed)) return;

        Location eventLoc = event.getLocation();
        FLocation eventLocation = new FLocation(eventLoc);
        Faction eventFaction = Board.getInstance().getFactionAt(eventLocation);

        if (eventFaction.equals(fWild)) return;

        if(entity.getWorld().equals(raidWorld)){
            //check if in claims of the raiding outpost

            if(eventFaction.equals(raidingOutpostFaction)){

                if(System.currentTimeMillis()>(lastBreachTimeMS + 100000)){
                    event.setCancelled(true);
                    return;
                }else{
                    if(eventLoc.getX()>(minX-2) && eventLoc.getX()<(maxX+2) && eventLoc.getZ()>(minZ+2)&& eventLoc.getZ()<(maxZ+2)){
                        boolean blockBroken = false;
                        for(Block block : blockList){
                            Location location = block.getLocation();
                            if(location.getY()<maxY && location.getY()>minY && location.getX()<maxX && location.getX()>minX && location.getZ()<maxZ && location.getZ()>minZ){
                                blockBroken=true;
                                lastBreachTimeMS = System.currentTimeMillis();
                                break;
                            }
                        }
                    }
                }
            }
            //regenerate outpost

        }



        TNTPrimed tntPrimed = (TNTPrimed) entity;

        Faction spawnFaction = Board.getInstance().getFactionAt(new FLocation(tntPrimed.getSpawnLocation()));

        if (eventFaction.equals(spawnFaction)) return;

        long currentTime = System.currentTimeMillis();


        if (raidMap.containsKey(eventFaction)){
            long lastEntry = raidMap.get(eventFaction);

            if(currentTime - lastEntry < 100) return;



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
