package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.*;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Listeners implements Listener {

    private final RaidOutpost raidOutpost;
    private final RoMenu roMenu;
    private final Config config;

    private final Object2LongOpenHashMap<Faction> raidMap;
    private final Object2LongOpenHashMap<UUID> teleportCooldown = new Object2LongOpenHashMap<>();
    private final Random random = new Random();

    private final Faction ro = Factions.getInstance().getByTag("RaidOutpost");
    private final Faction wilderness = Factions.getInstance().getWilderness();
    private final Faction warzone = Factions.getInstance().getWarZone();

    private long lastBreachTime;

    public Listeners(RaidOutpost raidOutpost, RoMenu roMenu, Object2LongOpenHashMap<Faction> raidMap, Config config) {
        this.raidOutpost = raidOutpost;
        this.roMenu = roMenu;
        this.config = config;
        this.raidMap = raidMap;
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) return;
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof TNTPrimed tntPrimed)) return;

        List<Block> blockList = event.blockList();

        Location eventLoc = event.getLocation();
        FLocation eventLocation = new FLocation(eventLoc);
        Faction eventFaction = Board.getInstance().getFactionAt(eventLocation);

        //if (eventFaction.equals(wilderness)) return;
        if (eventFaction.equals(warzone)) return;

        Faction spawnFaction = Board.getInstance().getFactionAt(new FLocation(tntPrimed.getSpawnLocation()));

        long currentTime = System.currentTimeMillis();
        if (tntPrimed.getWorld().equals(config.getRaidWorld())) {
            //if (!eventFaction.equals(raidingOutpostFaction)) return;

            if (!Utils.isCooldown(currentTime, lastBreachTime + config.getLockWildTeleport())) {
                event.setCancelled(true);
                return;
            } else if (Utils.isInsideXZ(eventLoc, config, 2)) {
                for (Block block : blockList) {
                    Location location = block.getLocation();
                    if (Utils.isInsideXYZ(location, config, 0)) {
                        this.lastBreachTime = currentTime;
                        this.roMenu.closeRo(config.getLockWildTeleport());
                        teleportCooldown.clear();
                        new RegenRo(raidOutpost, ro, spawnFaction, config).runTaskLater(raidOutpost, config.getRoRegenInterval());
                        break;
                    }
                }
            }
            return;
        }

        if (eventFaction.equals(spawnFaction)) return;

        if (raidMap.containsKey(eventFaction)) {
            if (currentTime - raidMap.get(eventFaction) < 100) return;
            if (blockList.contains(eventLoc.getBlock())){
                raidMap.put(eventFaction, currentTime);
            }
        } else {
            raidMap.put(eventFaction, currentTime);
            spawnFaction.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED +  "Raid Manager" + ChatColor.WHITE + "] " + ChatColor.RESET + config.getStartMSGRaider() + " " + eventFaction.getTag());
            eventFaction.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED +  "Raid Manager" + ChatColor.WHITE + "] " + ChatColor.RESET + spawnFaction.getTag() + " " + config.getStartMSGTarget());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (config.isPreventMining() && event.getBlock().getType().equals(Material.SPAWNER) && raidMap.containsKey(FPlayers.getInstance().getByPlayer(event.getPlayer()).getFaction())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine spawners while being raided!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack itemStack = event.getCurrentItem();
        if (inventory == null || itemStack == null || !event.getView().getTitle().equalsIgnoreCase("Raid Outpost")) return;
        event.setCancelled(true);

        HumanEntity player = event.getWhoClicked();

        if (itemStack.getType() == Material.TNT) {
            boolean foundLocation = false;
            UUID uuid = player.getUniqueId();
            if (teleportCooldown.containsKey(uuid) && System.currentTimeMillis() - config.getTeleportCooldown() < teleportCooldown.get(uuid)) {
                Utils.sendRoMessage(player, "You are on teleport cooldown");
                return;
            }
            for (int i = 0; i < 10; i++) {
                int x = random.nextInt(1599) - 799;
                int y = 40;
                int z = random.nextInt(1599) - 799;
                Location location = new Location(config.getRaidWorld(), x, y, z);

                if (!location.getBlock().isSolid()) {
                    player.teleport(location);
                    teleportCooldown.put(uuid, System.currentTimeMillis());
                    foundLocation = true;
                    break;
                }
            }
            if (!foundLocation) {
                Utils.sendRoMessage(player, "Could not find a location to teleport to");
            }
        }
    }
}
