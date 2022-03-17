package com.gromit.antimine;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Antimine extends JavaPlugin {


    FileConfiguration config = getConfig();

    long period;

    public static long timeNoBoom;
    public static boolean preventMining;
    public static String startMSGtarget;
    public static String endMSGtarget;
    public static String startMSGraider;

    public static World raidOutpost;
    public static List<Chunk> outpostCore;
    public static int minY;
    public static int maxY;
    public static int minX;
    public static int maxX;
    public static int minZ;
    public static int maxZ;


    @Override
    public void onEnable() {



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

        minY = config.getInt("raiding-outpost-miny", 200);
        maxY = config.getInt("raiding-outpost-maxy", 100);
        minX = config.getInt("raiding-outpost-minx", -20);
        maxX = config.getInt("raiding-outpost-maxx", 20);
        minZ = config.getInt("raiding-outpost-minz", -20);
        maxZ = config.getInt("raiding-outpost-maxz", 20);






        new Timer().runTaskTimerAsynchronously(this, 0, period);

        if(!Factions.getInstance().isTagTaken("RaidingOutpost")){
            getLogger().info("outpost faction does not exist yet");

            Faction outpost = Factions.getInstance().createFaction();
            outpost.setPermanent(true);
            outpost.setPermanentPower(1000);
            outpost.setTag("RaidingOutpost");
        } else{getLogger().info("outpost faction exists already");}




        getServer().getPluginManager().registerEvents(new Listeners(this , minY, maxY,minX, maxX, minZ, maxZ), this);




    }
}
