package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FactionAutoDisbandEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.atlaspvp.raidoutpost.runnable.CaptureTimer;
import net.atlaspvp.raidoutpost.runnable.RegenRo;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Listeners implements Listener {

    private final RaidOutpost raidOutpost;
    private CaptureTimer captureTimer;

    private final Object2LongOpenHashMap<UUID> teleportCooldown = new Object2LongOpenHashMap<>();
    private final Random random = new Random();

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
                        if (captureTimer != null) {
                            if (captureTimer.getFactionInventory().equals(raidOutpost.getFactionMap().get(spawnFaction))) {
                                return;
                            } else {
                                captureTimer.cancel();
                                captureTimer.getFactionInventory().setCurrentPhase(0);
                            }
                        }
                        FactionInventory factionInventory = raidOutpost.getFactionMap().get(spawnFaction);
                        factionInventory.setCaptures(factionInventory.getCaptures() + 1);
                        captureTimer = new CaptureTimer(factionInventory, raidOutpost);
                        captureTimer.runTaskLater(raidOutpost, raidOutpost.getConfigRo().getPhaseInterval());
                        lastBreachTime = currentTime;
                        raidOutpost.getRoMenu().closeRo(raidOutpost.getConfigRo().getLockWildTeleport());
                        teleportCooldown.clear();
                        new RegenRo(raidOutpost, raidOutpost.getRo(), spawnFaction, raidOutpost.getConfigRo()).runTaskLater(raidOutpost, raidOutpost.getConfigRo().getRoRegenInterval());
                        break;
                    }
                }
            }
            return;
        }

        if (eventFaction.equals(spawnFaction)) return;

        if (raidOutpost.getRaidMap().containsKey(eventFaction)) {
            if (currentTime - raidOutpost.getRaidMap().get(eventFaction) < 100) return;
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
    public void onCreateFaction(FactionCreateEvent event) {
        Faction faction = event.getFaction();
        raidOutpost.getFactionMap().put(faction, new FactionInventory(0, 0, 0, faction));
    }

    @EventHandler
    public void onDisbandFaction(FactionDisbandEvent event) {
        raidOutpost.getFactionMap().remove(event.getFaction());
    }

    @EventHandler
    public void onAutoDisbandFaction(FactionAutoDisbandEvent event) {
        raidOutpost.getFactionMap().remove(event.getFaction());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack itemStack = event.getCurrentItem();
        String title = event.getView().getTitle();
        if (inventory == null || itemStack == null || !title.equalsIgnoreCase("Raid Outpost") && !title.equalsIgnoreCase("Raid Outpost rewards")) return;
        event.setCancelled(true);

        HumanEntity player = event.getWhoClicked();
        Faction faction = FPlayers.getInstance().getByPlayer((Player) player).getFaction();
        FactionInventory factionInventory = raidOutpost.getFactionMap().get(faction);
        Inventory inventory1 = null;
        if (factionInventory != null) {
            inventory1 = factionInventory.getInventory();
        }

        Material material = itemStack.getType();
        if (material == Material.TNT && title.equalsIgnoreCase("Raid Outpost")) {
            boolean foundLocation = false;
            UUID uuid = player.getUniqueId();
            if (teleportCooldown.containsKey(uuid) && !Utils.isCooldown(System.currentTimeMillis(), teleportCooldown.get(uuid) + raidOutpost.getConfigRo().getTeleportCooldown())) {
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
                    teleportCooldown.put(uuid, System.currentTimeMillis());
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
                Utils.sendRoMessage(player, "You are not part of a faction");
                return;
            }
            player.openInventory(inventory1);
        }
        if (title.equalsIgnoreCase("Raid Outpost rewards") && material == Material.EMERALD && inventory1 != null) {
            int phase = Integer.parseInt(itemStack.getItemMeta().getDisplayName().replaceAll("\\D+", ""));
            List<ItemStack> itemStackList = raidOutpost.getPhaseDataMap().get(phase).getItemStackList();
            for (ItemStack itemStack1 : itemStackList) {
                player.getInventory().addItem(itemStack1);
            }
            inventory1.remove(itemStack);
        }
    }
}
