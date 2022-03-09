package com.gromit.antimine;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Antimine extends JavaPlugin {


    FileConfiguration config = getConfig();

    long period;

    public static long timeNoBoom;
    public static boolean preventMining;
    public static String startMSGtarget;
    public static String endMSGtarget;
    public static String startMSGraider;

    public static World raidOutpost;


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);


        //default config


        config.addDefault("period-raid-check", 200L);
        config.addDefault("time-no-explosion-raid-stop-ticks", 12000L);
        config.addDefault("prevent-mining-spawner", true);


        config.addDefault("start-raid-msg", "you are being raided!");
        config.addDefault("end-raid-msg", "you are no longer being raided");

        config.addDefault("you-started-raid", "your faction has started raiding!");

        config.addDefault("raiding-outpost-world", "raidoutpost");


        config.options().copyDefaults(true);
        saveConfig();


        //end default config

        period = config.getLong("period-raid-check");
        timeNoBoom = config.getLong("time-no-explosion-raid-stop-ticks");

        preventMining = config.getBoolean("prevent-mining-spawner");

        endMSGtarget = config.getString("end-raid-msg");

        startMSGtarget = config.getString("start-raid-msg");
        startMSGraider = config.getString("you-started-raiding");

        raidOutpost = Bukkit.getWorld(config.getString("raiding-outpost-world"));






        new Timer().runTaskTimerAsynchronously(this, 0, period);

        if(!Factions.getInstance().isTagTaken("RaidingOutpost")){
            getLogger().info("outpost faction does not exist yet");

            Faction outpost = Factions.getInstance().createFaction();
            outpost.setPermanent(true);
            outpost.setPermanentPower(1000);
            outpost.setTag("RaidingOutpost");
        } else{getLogger().info("outpost faction exists already");}
    }
}
