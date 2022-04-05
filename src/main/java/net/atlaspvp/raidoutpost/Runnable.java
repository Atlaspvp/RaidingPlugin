package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class Runnable {

    public static void startAllDatabaseRunnables(RaidOutpost raidOutpost, ItemStack map) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Postgresql.saveFaction(raidOutpost);
                Postgresql.saveItemStacks(raidOutpost);
                Postgresql.readLeaderboard(raidOutpost, map);
            }
        }.runTaskTimerAsynchronously(raidOutpost, raidOutpost.getConfigRo().getSaveRoFactionTimer(), raidOutpost.getConfigRo().getSaveRoFactionTimer());
    }

    public static void regenRo(RaidOutpost raidOutpost, @Nullable Faction capturedRo) {
        raidOutpost.setRoLockdown(true);
        raidOutpost.getRoMenu().closeRo(raidOutpost.getConfigRo().getLockWildTeleport());
        World raidWorld = raidOutpost.getConfigRo().getRaidWorld();
        World spawnWorld = raidOutpost.getConfigRo().getSpawnWorld();

        long time = raidOutpost.getConfigRo().getRoRegenInterval() / 1200;
        for (Player player : raidOutpost.getConfigRo().getRaidWorld().getPlayers()) {
            if (capturedRo != null) {
                Utils.sendRoMessage(player, "Raid outpost has been captured by " + capturedRo.getTag());
            }
            if (time == 1) {
                Utils.sendRoMessage(player, "Raid outpost will reset in " + time + " minute");
            } else Utils.sendRoMessage(player, "Raid outpost will reset in " + time + " minutes");
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : raidWorld.getPlayers()) {
                    player.teleport(spawnWorld.getSpawnLocation());
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Faction faction : Factions.getInstance().getAllFactions()) {
                            if (faction.getTag().equalsIgnoreCase("RaidOutpost")) continue;
                            Board.getInstance().unclaimAllInWorld(faction.getId(), raidWorld);
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.unloadWorld(raidWorld, false);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            FileUtils.deleteDirectory(new File(raidOutpost.getConfigRo().getTargetFolder()));
                                            FileUtils.copyDirectoryToDirectory(new File(raidOutpost.getConfigRo().getSourceFolder()), new File(raidOutpost.getConfigRo().getServerFolder()));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                raidOutpost.getConfigRo().setRaidWorld(Bukkit.createWorld(new WorldCreator(raidOutpost.getConfigRo().getRaidWorldName())));
                                                raidOutpost.getTeleportCooldown().clear();
                                            }
                                        }.runTask(raidOutpost);
                                    }
                                }.runTaskAsynchronously(raidOutpost);
                            }
                        }.runTask(raidOutpost);
                    }
                }.runTaskAsynchronously(raidOutpost);
            }
        }.runTaskLater(raidOutpost, raidOutpost.getConfigRo().getRoRegenInterval());
    }


    public static void factionRaidTimer(RaidOutpost raidOutpost) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ObjectIterator<Object2LongMap.Entry<Faction>> iterator = raidOutpost.getRaidMap().object2LongEntrySet().fastIterator();

                while (iterator.hasNext()) {
                    Object2LongMap.Entry<Faction> pair = iterator.next();
                    iterator.remove(); // avoids a ConcurrentModificationException

                    if (Utils.isCooldown(System.currentTimeMillis(), pair.getLongValue() + raidOutpost.getConfigRo().getTimeout())) {
                        Faction defendFaction = pair.getKey();
                        raidOutpost.getRaidMap().remove(defendFaction);
                        defendFaction.sendMessage(raidOutpost.getConfigRo().getEndMSGTarget());
                    }
                }
            }
        }.runTaskTimer(raidOutpost, 0, raidOutpost.getConfigRo().getFactionLockdownTimer());
    }
}
