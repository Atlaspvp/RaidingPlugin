package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;

public final class RaidOutpost extends JavaPlugin {

    private final Object2LongOpenHashMap<Faction> raidMap = new Object2LongOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Faction, RoFaction> factionMap = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<PhaseData> phaseDataMap = new Int2ObjectOpenHashMap<>();
    private final Object2LongOpenHashMap<UUID> teleportCooldown = new Object2LongOpenHashMap<>();
    private GlobalTimer globalTimer;
    private RoFaction currentRoFaction;
    private Config config;
    private RoMenu roMenu;
    private Faction ro;
    private Faction wilderness;
    private Faction warzone;
    private boolean roLockdown = false;

    @Override
    public void onEnable() {
        globalTimer = new GlobalTimer(this);
        Postgresql.createTable(this);
        Postgresql.readItemStacks(this);
        roMenu = new RoMenu(this);
        config = new Config(this, getConfig());
        if (!Factions.getInstance().isTagTaken("RaidOutpost")) {
            Faction outpost = Factions.getInstance().createFaction();
            outpost.setPermanent(true);
            outpost.setPermanentPower(1000);
            outpost.setTag("RaidOutpost");
        }
        ro = Factions.getInstance().getByTag("RaidOutpost");
        Postgresql.readFaction(this);
        Runnable.factionRaidTimer(this);
        Postgresql.readLeaderboard(this, this.getRoMenu().getInventory().getItem(15));
        Runnable.startAllDatabaseRunnables(this, this.getRoMenu().getInventory().getItem(15));

        wilderness = Factions.getInstance().getWilderness();
        warzone = Factions.getInstance().getWarZone();
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        Objects.requireNonNull(getCommand("RaidOutpost")).setExecutor(roMenu);
    }

    @Override
    public void onDisable() {
        Postgresql.saveItemStacks(this);
        Postgresql.saveFaction(this);
    }

    public Object2LongOpenHashMap<Faction> getRaidMap() {
        return raidMap;
    }

    public Object2ObjectOpenHashMap<Faction, RoFaction> getFactionMap() {
        return factionMap;
    }

    public Int2ObjectOpenHashMap<PhaseData> getPhaseDataMap() {
        return phaseDataMap;
    }

    public Object2LongOpenHashMap<UUID> getTeleportCooldown() {
        return teleportCooldown;
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

    public boolean isRoLockdown() {
        return roLockdown;
    }

    public void setRoLockdown(boolean roLockdown) {
        this.roLockdown = roLockdown;
    }

    public GlobalTimer getGlobalTimer() {
        return globalTimer;
    }

    public RoFaction getCurrentRoFaction() {
        return currentRoFaction;
    }

    public void setCurrentRoFaction(RoFaction currentRoFaction) {
        this.currentRoFaction = currentRoFaction;
    }
}
