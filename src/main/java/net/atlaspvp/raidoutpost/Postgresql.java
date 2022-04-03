package net.atlaspvp.raidoutpost;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Postgresql {

    public static boolean isDisabled = false;

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
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS raidoutpost (faction VARCHAR PRIMARY KEY, rewardlist INTEGER[], captures INTEGER, currentphase INTEGER, time BIGINT)");
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

    public static void saveFaction(Object2ObjectOpenHashMap<Faction, FactionInventory> inventoryMap) {
        if (isDisabled) return;
        if (inventoryMap.isEmpty()) return;
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        ObjectIterator<Object2ObjectMap.Entry<Faction, FactionInventory>> iterator = inventoryMap.object2ObjectEntrySet().fastIterator();
        ObjectSet<Faction> objectSet = inventoryMap.keySet();

        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO raidoutpost (faction, rewardlist, captures, currentphase, time) VALUES (?, ?, ?, ?, ?) ON CONFLICT (faction) DO UPDATE SET rewardlist=?, captures=?, currentphase=?, time=?");
            while (iterator.hasNext()) {
                Object2ObjectMap.Entry<Faction, FactionInventory> pair = iterator.next();
                FactionInventory factionInventory = pair.getValue();
                ps.setString(1, pair.getKey().getTag());
                ps.setArray(2, connection.createArrayOf("INTEGER", Utils.generateIntegerArray(factionInventory.getInventory())));
                ps.setInt(3, factionInventory.getCaptures());
                ps.setInt(4, factionInventory.getCurrentPhase());
                ps.setLong(5, factionInventory.getTime());
                ps.setArray(6, connection.createArrayOf("INTEGER", Utils.generateIntegerArray(factionInventory.getInventory())));
                ps.setInt(7, factionInventory.getCaptures());
                ps.setInt(8, factionInventory.getCurrentPhase());
                ps.setLong(9, factionInventory.getTime());
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

    public static void saveItemStacks(Int2ObjectOpenHashMap<PhaseData> phaseDataMap) {
        if (isDisabled) return;
        if (phaseDataMap.isEmpty()) return;
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        ObjectIterator<Int2ObjectMap.Entry<PhaseData>> iterator = phaseDataMap.int2ObjectEntrySet().fastIterator();

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

    public static void readItemStacks(Int2ObjectOpenHashMap<PhaseData> phaseDataMap) {
        if (isDisabled) return;
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
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
                    phaseDataMap.put(i, new PhaseData(new ArrayList<>(), emerald));
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
                phaseDataMap.put(phase, new PhaseData(itemStackList, emerald));
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {e.printStackTrace();}
        closeConnection(connection);
    }

    public static void readFaction(Plugin plugin, Object2ObjectOpenHashMap<Faction, FactionInventory> factionMap, Int2ObjectOpenHashMap<PhaseData> phaseDataMap) {
        Connection connection;

        try {
            connection = Dbapi.getHikariConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(plugin);
            return;
        }

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM raidoutpost");
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                Faction faction = Factions.getInstance().getByTag(resultSet.getString(1));
                Integer[] integers = (Integer[]) resultSet.getArray(2).getArray();
                FactionInventory factionInventory = new FactionInventory(resultSet.getInt(3), resultSet.getInt(4), resultSet.getLong(5), faction);
                Inventory inventory = factionInventory.getInventory();
                for (int phase : integers) {
                    inventory.addItem(phaseDataMap.get(phase).getPhase());
                }
                factionMap.put(faction, factionInventory);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            setDisabled(plugin);
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