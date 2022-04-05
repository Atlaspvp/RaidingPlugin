package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Listeners implements Listener {

    private final RaidOutpost raidOutpost;

    private final Random random = new Random();
    private RoFaction roFaction;
    private final List<String> lore = new ArrayList<>(1);

    private long lastBreachTime;

    public Listeners(RaidOutpost raidOutpost) {
        this.raidOutpost = raidOutpost;
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

        if (eventFaction.equals(raidOutpost.getWilderness())) return;
        if (eventFaction.equals(raidOutpost.getWarzone())) return;

        Faction spawnFaction = Board.getInstance().getFactionAt(new FLocation(tntPrimed.getSpawnLocation()));

        long currentTime = System.currentTimeMillis();
        if (tntPrimed.getWorld().equals(raidOutpost.getConfigRo().getRaidWorld())) {
            if (!eventFaction.equals(raidOutpost.getRo())) return;

            if (!Utils.isCooldown(currentTime, lastBreachTime + raidOutpost.getConfigRo().getLockWildTeleport())) {
                event.setCancelled(true);
                return;
            } else if (Utils.isInsideXZ(eventLoc, raidOutpost.getConfigRo(), 2)) {
                for (Block block : blockList) {
                    Location location = block.getLocation();
                    if (Utils.isInsideXYZ(location, raidOutpost.getConfigRo(), 0)) {
                        RoFaction roFaction = raidOutpost.getFactionMap().get(spawnFaction);
                        if (roFaction != null && roFaction.getCaptureTimer() != null) {
                            return;
                        } else if (this.roFaction != null) {
                            CaptureTimer captureTimer = this.roFaction.getCaptureTimer();
                            if (captureTimer != null) {
                                Utils.stopCapture(raidOutpost, this.roFaction, captureTimer);
                            } else {
                                this.roFaction = null;
                            }
                        }
                        raidOutpost.getFactionMap().computeIfAbsent(spawnFaction, k -> new RoFaction(raidOutpost, spawnFaction, 0, 0, 0));
                        this.roFaction = raidOutpost.getFactionMap().get(spawnFaction);
                        Utils.startCapture(raidOutpost, this.roFaction, spawnFaction, lore);
                        lastBreachTime = currentTime;
                        return;
                    }
                }
            }
            return;
        }

        if (eventFaction.equals(spawnFaction)) return;

        if (raidOutpost.getRaidMap().containsKey(eventFaction)) {
            if (Utils.isCooldown(currentTime, raidOutpost.getRaidMap().get(eventFaction) + 100)) return;
            if (blockList.contains(eventLoc.getBlock())){
                raidOutpost.getRaidMap().put(eventFaction, currentTime);
            }
        } else {
            raidOutpost.getRaidMap().put(eventFaction, currentTime);
            spawnFaction.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED +  "Raid Manager" + ChatColor.WHITE + "] " + ChatColor.RESET + raidOutpost.getConfigRo().getStartMSGRaider() + " " + eventFaction.getTag());
            eventFaction.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED +  "Raid Manager" + ChatColor.WHITE + "] " + ChatColor.RESET + spawnFaction.getTag() + " " + raidOutpost.getConfigRo().getStartMSGTarget());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (raidOutpost.getConfigRo().isPreventMining() && event.getBlock().getType().equals(Material.SPAWNER) && raidOutpost.getRaidMap().containsKey(FPlayers.getInstance().getByPlayer(event.getPlayer()).getFaction())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine spawners while being raided!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().getWorld().equals(raidOutpost.getConfigRo().getRaidWorld())) return;
        if (!raidOutpost.isRoLockdown()) return;
        event.getPlayer().teleport(raidOutpost.getConfigRo().getSpawnWorld().getSpawnLocation());
    }

    @EventHandler
    public void onDisbandFaction(FactionDisbandEvent event) {
        RoFaction roFaction = raidOutpost.getFactionMap().get(event.getFaction());
        if (roFaction == null) return;
        ItemStack map = raidOutpost.getRoMenu().getMap();
        List<String> lore = map.getLore();
        if (lore != null) {
            lore.remove(ChatColor.GRAY + "Controlled by: " + roFaction.getFaction().getTag());
            map.setLore(lore);
            raidOutpost.getRoMenu().getInventory().setItem(15, map);
        }
        raidOutpost.getFactionMap().remove(event.getFaction());
    }

    @EventHandler
    public void onAutoDisbandFaction(FactionAutoDisbandEvent event) {
        RoFaction roFaction = raidOutpost.getFactionMap().get(event.getFaction());
        if (roFaction == null) return;
        ItemStack map = raidOutpost.getRoMenu().getMap();
        List<String> lore = map.getLore();
        if (lore != null) {
            lore.remove(ChatColor.GRAY + "Controlled by: " + roFaction.getFaction().getTag());
            map.setLore(lore);
            raidOutpost.getRoMenu().getInventory().setItem(15, map);
        }
        raidOutpost.getFactionMap().remove(event.getFaction());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack itemStack = event.getCurrentItem();
        String title = event.getView().getTitle();
        if (inventory == null || itemStack == null || !title.equalsIgnoreCase("Raid Outpost")) return;
        event.setCancelled(true);

        HumanEntity player = event.getWhoClicked();
        Faction faction = FPlayers.getInstance().getByPlayer((Player) player).getFaction();
        RoFaction roFaction = raidOutpost.getFactionMap().get(faction);
        Inventory inventory1 = null;
        if (roFaction != null) {
            inventory1 = roFaction.getInventory();
        }

        Material material = itemStack.getType();
        if (material == Material.TNT && title.equalsIgnoreCase("Raid Outpost")) {
            boolean foundLocation = false;
            UUID uuid = player.getUniqueId();
            if (raidOutpost.getTeleportCooldown().containsKey(uuid) && !Utils.isCooldown(System.currentTimeMillis(), raidOutpost.getTeleportCooldown().get(uuid) + raidOutpost.getConfigRo().getTeleportCooldown())) {
                Utils.sendRoMessage(player, "You are on teleport cooldown");
                return;
            }
            for (int i = 0; i < 10; i++) {
                int x = random.nextInt(1599) - 799;
                int y = 40;
                int z = random.nextInt(1599) - 799;
                Location location = new Location(raidOutpost.getConfigRo().getRaidWorld(), x, y, z);

                if (!location.getBlock().isSolid()) {
                    player.teleport(location);
                    raidOutpost.getTeleportCooldown().put(uuid, System.currentTimeMillis());
                    foundLocation = true;
                    break;
                }
            }
            if (!foundLocation) {
                Utils.sendRoMessage(player, "Could not find a location to teleport to");
            }
        }
        if (material == Material.CHEST && title.equalsIgnoreCase("Raid Outpost")) {
            if (inventory1 == null) {
                Utils.sendRoMessage(player, "Your faction needs to capture Raid Outpost at least once");
                return;
            }
            inventory.close();
            player.openInventory(inventory1);
        }
    }
}
