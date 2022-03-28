package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class RegenRo extends BukkitRunnable {

    private final RaidOutpost raidOutpost;
    private final Config config;

    private final Faction ro;
    private final Faction capturedRo;

    public RegenRo(RaidOutpost raidOutpost, Faction ro, Faction capturedRo, Config config) {
        this.raidOutpost = raidOutpost;
        this.config = config;
        this.ro = ro;
        this.capturedRo = capturedRo;
        this.messagePlayers(config.getRoRegenInterval() / 1200);
    }

    private void messagePlayers(int time) {
        for (Player player : config.getRaidWorld().getPlayers()) {
            Utils.sendRoMessage(player, "Raid outpost has been captured by " + capturedRo.getTag());
            if (time == 1) {
                Utils.sendRoMessage(player, "Raid outpost will reset in " + time + " minute");
            } else Utils.sendRoMessage(player, "Raid outpost will reset in " + time + " minutes");
        }
    }

    @Override
    public void run() {
        for (Player player : config.getRaidWorld().getPlayers()) {
            player.teleport(config.getSpawnWorld().getSpawnLocation());
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Faction faction : Factions.getInstance().getAllFactions()) {
                    if (faction.getTag().equalsIgnoreCase("RaidOutpost")) continue;
                    Board.getInstance().unclaimAllInWorld(faction.getId(), config.getRaidWorld());
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.unloadWorld(config.getRaidWorld(), false);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    FileUtils.copyDirectoryToDirectory(new File(config.getSourceFolder()), new File(config.getTargetFolder()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        config.setRaidWorld(Bukkit.createWorld(new WorldCreator(config.getRaidWorldName())));
                                    }
                                }.runTask(raidOutpost);
                            }
                        }.runTaskAsynchronously(raidOutpost);
                    }
                }.runTask(raidOutpost);
            }
        }.runTaskAsynchronously(this.raidOutpost);
    }
}
