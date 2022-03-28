package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class RaidOutpost extends JavaPlugin {

    private final Object2LongOpenHashMap<Faction> raidMap = new Object2LongOpenHashMap<>();

    @Override
    public void onEnable() {
        Config config = new Config(this, getConfig());
        new Timer(raidMap, config).runTaskTimer(this, 0, config.getFactionLockdownTimer());

        if (!Factions.getInstance().isTagTaken("RaidOutpost")) {
            Faction outpost = Factions.getInstance().createFaction();
            outpost.setPermanent(true);
            outpost.setPermanentPower(1000);
            outpost.setTag("RaidOutpost");
        }
        RoMenu roMenu = new RoMenu(this);
        getServer().getPluginManager().registerEvents(new Listeners(this, roMenu, raidMap, config), this);
        Objects.requireNonNull(getCommand("RaidOutpost")).setExecutor(roMenu);
    }
}
