package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class RaidOutpost extends JavaPlugin {

    private final Object2LongOpenHashMap<Faction> raidMap = new Object2LongOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Faction, FactionInventory> factionMap = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<PhaseData> phaseDataMap = new Int2ObjectOpenHashMap<>();
    private Config config;
    private RoMenu roMenu;
    private Faction ro;
    private Faction wilderness;
    private Faction warzone;

    @Override
    public void onEnable() {
        Postgresql.createTable(this);
        Postgresql.readItemStacks(phaseDataMap);
        Postgresql.readFaction(this, factionMap, phaseDataMap);
        config = new Config(this, getConfig());
        new Timer(this).runTaskTimer(this, 0, config.getFactionLockdownTimer());

        if (!Factions.getInstance().isTagTaken("RaidOutpost")) {
            Faction outpost = Factions.getInstance().createFaction();
            outpost.setPermanent(true);
            outpost.setPermanentPower(1000);
            outpost.setTag("RaidOutpost");
        }
        ro = Factions.getInstance().getByTag("RaidOutpost");
        wilderness = Factions.getInstance().getWilderness();
        warzone = Factions.getInstance().getWarZone();
        roMenu = new RoMenu(this);
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        Objects.requireNonNull(getCommand("RaidOutpost")).setExecutor(roMenu);
    }

    @Override
    public void onDisable() {
        Postgresql.saveItemStacks(phaseDataMap);
        Postgresql.saveFaction(factionMap);
    }

    public Object2LongOpenHashMap<Faction> getRaidMap() {
        return raidMap;
    }

    public Object2ObjectOpenHashMap<Faction, FactionInventory> getFactionMap() {
        return factionMap;
    }

    public Int2ObjectOpenHashMap<PhaseData> getPhaseDataMap() {
        return phaseDataMap;
    }

    public Faction getRo() {
        return ro;
    }

    public Faction getWilderness() {
        return wilderness;
    }

    public Faction getWarzone() {
        return warzone;
    }

    public Config getConfigRo() {
        return config;
    }

    public RoMenu getRoMenu() {
        return roMenu;
    }
}
