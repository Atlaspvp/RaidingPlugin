package com.gromit.antimine;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Antimine extends JavaPlugin {

    private final Object2LongOpenHashMap<Faction> raidMap = new Object2LongOpenHashMap<>();
    private final FileConfiguration config = this.getConfig();

    public static World raidOutpost;
    public static List<Chunk> outpostCore;

    @Override
    public void onEnable() {
        this.prepareConfig();
        new Timer(config, raidMap).runTaskTimerAsynchronously(this, 0, config.getLong("period-raid-check"));

        if (!Factions.getInstance().isTagTaken("RaidingOutpost")) {
            getLogger().info("Outpost faction does not exist yet");

            Faction outpost = Factions.getInstance().createFaction();
            outpost.setPermanent(true);
            outpost.setPermanentPower(1000);
            outpost.setTag("RaidingOutpost");
        } else getLogger().info("outpost faction exists already");

        getServer().getPluginManager().registerEvents(new Listeners(this, config, raidMap), this);
    }

    private void prepareConfig() {
        config.addDefault("period-raid-check", 200L);
        config.addDefault("time-no-explosion-raid-stop-ticks", 12000L);
        config.addDefault("prevent-mining-spawner", true);
        config.addDefault("start-raid-msg", "you are being raided!");
        config.addDefault("end-raid-msg", "you are no longer being raided");
        config.addDefault("you-started-raid", "your faction has started raiding!");
        config.addDefault("raiding-outpost-world", "raidoutpost");
        config.options().copyDefaults(true);
        saveConfig();

        raidOutpost = Bukkit.getWorld(config.getString("raiding-outpost-world"));
    }
}
