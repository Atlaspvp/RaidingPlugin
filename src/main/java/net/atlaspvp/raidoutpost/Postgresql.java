package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.atlaspvp.dbapi.Dbapi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Postgresql {

    private static boolean isDisabled = false;
    private static final List<String> lore = new ArrayList<>(5);

    private static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void createTable(Plugin plugin) {
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(plugin);
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS raidoutpost (faction VARCHAR PRIMARY KEY, rewardlist VARCHAR[], captures INTEGER, currentphase INTEGER, time BIGINT, refreshphase BOOLEAN)");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(plugin);
        }

        try {
            PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS raidoutpostrewards (phase INTEGER PRIMARY KEY, rewards VARCHAR[])");
            ps1.executeUpdate();
            ps1.close();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(plugin);
        }
        closeConnection(connection);
    }

    public static void saveFaction(RaidOutpost raidOutpost) {
        if (isDisabled) return;
        if (raidOutpost.getFactionMap().isEmpty()) return;
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        ObjectIterator<Object2ObjectMap.Entry<Faction, RoFaction>> iterator = raidOutpost.getFactionMap().object2ObjectEntrySet().fastIterator();
        ObjectSet<Faction> objectSet = raidOutpost.getFactionMap().keySet();

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO raidoutpost (faction, rewardlist, captures, currentphase, time, refreshphase) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (faction) DO UPDATE SET rewardlist=?, captures=?, currentphase=?, time=?, refreshphase=?");
            while (iterator.hasNext()) {
                Object2ObjectMap.Entry<Faction, RoFaction> pair = iterator.next();
                RoFaction roFaction = pair.getValue();
                ps.setString(1, pair.getKey().getTag());
                ps.setArray(2, connection.createArrayOf("VARCHAR", Utils.generateStringArray1(roFaction.getInventory())));
                ps.setInt(3, roFaction.getCaptures());
                ps.setInt(4, roFaction.getCurrentPhase());
                if (roFaction.getTime() > 0) {
                    ps.setLong(5, roFaction.getTime());
                } else ps.setLong(5, 0);
                ps.setBoolean(6, roFaction.isRefreshPhase());
                ps.setArray(7, connection.createArrayOf("VARCHAR", Utils.generateStringArray1(roFaction.getInventory())));
                ps.setInt(8, roFaction.getCaptures());
                ps.setInt(9, roFaction.getCurrentPhase());
                if (roFaction.getTime() > 0) {
                    ps.setLong(10, roFaction.getTime());
                } else ps.setLong(10, 0);
                ps.setBoolean(11, roFaction.isRefreshPhase());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement ps = connection.prepareStatement("delete from raidoutpost where not faction = any (?)");
            List<String> factionTags = new ArrayList<>();
            for (Faction faction : objectSet) {
                factionTags.add(faction.getTag());
            }
            String[] keys = factionTags.toArray(new String[0]);
            Array array = connection.createArrayOf("VARCHAR", keys);
            ps.setArray(1, array);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException s) {
            s.printStackTrace();
        }
        closeConnection(connection);
    }

    public static void readFaction(RaidOutpost raidOutpost) {
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(raidOutpost);
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM raidoutpost");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Faction faction = Factions.getInstance().getByTag(resultSet.getString(1));
                String[] strings = (String[]) resultSet.getArray(2).getArray();
                RoFaction roFaction = new RoFaction(raidOutpost, faction, resultSet.getInt(3), resultSet.getInt(4), resultSet.getLong(5), resultSet.getBoolean(6));
                if (roFaction.isRefreshPhase()) {
                    Utils.refreshCapturePhaseDatabase(raidOutpost, roFaction);
                }
                Inventory inventory = roFaction.getInventory();
                for (String string : strings) {
                    inventory.addItem(Utils.readPersistentString(string));
                }
                raidOutpost.getFactionMap().put(faction, roFaction);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(raidOutpost);
        }
        closeConnection(connection);
    }

    public static void readLeaderboard(RaidOutpost raidOutpost, ItemStack map) {
        Connection connection;
        if (map == null) return;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM raidoutpost ORDER BY captures DESC LIMIT 5");
            ResultSet resultSet = ps.executeQuery();

            lore.clear();
            while (resultSet.next()) {
                lore.add(ChatColor.GRAY + resultSet.getString(1) + ": " + resultSet.getInt(3));
            }
            resultSet.close();
            ps.close();
            new BukkitRunnable() {
                @Override
                public void run() {
                    map.setLore(lore);
                }
            }.runTask(raidOutpost);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeConnection(connection);
    }

    public static void saveItemStacks(RaidOutpost raidOutpost) {
        if (isDisabled) return;
        if (raidOutpost.getPhaseDataMap().isEmpty()) return;
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        ObjectIterator<Int2ObjectMap.Entry<PhaseData>> iterator = raidOutpost.getPhaseDataMap().int2ObjectEntrySet().fastIterator();

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO raidoutpostrewards (phase, rewards) VALUES (?, ?) ON CONFLICT (phase) DO UPDATE SET rewards=?");
            while (iterator.hasNext()) {
                Int2ObjectMap.Entry<PhaseData> pair = iterator.next();
                ps.setInt(1, pair.getIntKey());
                ps.setArray(2, connection.createArrayOf("VARCHAR", Utils.generateStringArray(pair.getValue().getItemStackList())));
                ps.setArray(3, connection.createArrayOf("VARCHAR", Utils.generateStringArray(pair.getValue().getItemStackList())));
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        closeConnection(connection);
    }

    public static void readItemStacks(RaidOutpost raidOutpost) {
        if (isDisabled) return;
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(raidOutpost);
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM raidoutpostrewards", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = ps.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                for (int i = 1; i < 8; i++) {
                    ItemStack emerald = new ItemStack(Material.EMERALD);
                    ItemMeta emeraldMeta = emerald.getItemMeta();
                    emeraldMeta.setDisplayName(ChatColor.RESET + "Phase " + i);
                    emerald.setItemMeta(emeraldMeta);
                    raidOutpost.getPhaseDataMap().put(i, new PhaseData(new ArrayList<>(), emerald));
                }
            }

            while (resultSet.next()) {
                int phase = resultSet.getInt(1);
                String[] strings = (String[]) resultSet.getArray(2).getArray();
                List<ItemStack> itemStackList = new ArrayList<>();
                for (String string : strings) {
                    itemStackList.add(Utils.readPersistentString(string));
                }
                List<String> lore = new ArrayList<>();
                ItemStack emerald = new ItemStack(Material.EMERALD);
                ItemMeta emeraldMeta = emerald.getItemMeta();
                emeraldMeta.setDisplayName(ChatColor.RESET + "Phase " + phase);
                for (ItemStack itemStack1 : itemStackList) {
                    ItemStack itemStack = itemStack1.clone();
                    if (!itemStack.hasItemMeta()) {
                        lore.add(ChatColor.GRAY + itemStack.getI18NDisplayName() + " x " + itemStack.getAmount());
                    } else {
                        ItemMeta itemMeta1 = itemStack.getItemMeta();
                        itemMeta1.setDisplayName(ChatColor.GRAY + itemMeta1.getDisplayName());
                        itemStack.setItemMeta(itemMeta1);
                        lore.add(itemMeta1.getDisplayName() + " x " + itemStack.getAmount());
                    }
                }
                emeraldMeta.setLore(lore);
                emerald.setItemMeta(emeraldMeta);
                raidOutpost.getPhaseDataMap().put(phase, new PhaseData(itemStackList, emerald));
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(raidOutpost);
        }
        closeConnection(connection);
    }

    private static void setDisabled(Plugin plugin) {
        if (!isDisabled) {
            HandlerList.unregisterAll(plugin);
            plugin.getServer().getScheduler().cancelTasks(plugin);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            isDisabled = true;
        }
    }

}