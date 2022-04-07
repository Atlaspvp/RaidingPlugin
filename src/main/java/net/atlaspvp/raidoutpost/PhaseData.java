package net.atlaspvp.raidoutpost;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PhaseData {

    private final List<ItemStack> itemStackList;
    private final ItemStack phase;

    public PhaseData(List<ItemStack> itemStackList, ItemStack phase) {
        this.itemStackList = itemStackList;
        this.phase = phase;
    }

    public List<ItemStack> getItemStackList() {
        return itemStackList;
    }

    public ItemStack getPhase() {
        return phase;
    }

    public void updateLore(int slot, ItemStack itemStack, Inventory inventory, boolean clear) {
        if (clear) {
            getItemStackList().clear();
            getPhase().setLore(null);
            inventory.setItem(slot, getPhase());
            return;
        }
        getItemStackList().add(itemStack);
        ItemMeta itemMeta = getPhase().getItemMeta();
        List<String> lore = getPhase().getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        ItemStack itemStack1 = itemStack.clone();
        if (!itemStack1.hasItemMeta()) {
            lore.add(ChatColor.GRAY + itemStack1.getI18NDisplayName() + " x " + itemStack1.getAmount());
        } else {
            ItemMeta itemMeta1 = itemStack1.getItemMeta();
            itemMeta1.setDisplayName(ChatColor.GRAY + itemMeta1.getDisplayName());
            itemStack1.setItemMeta(itemMeta1);
            lore.add(itemMeta1.getDisplayName() + " x " + itemStack1.getAmount());
        }
        itemMeta.setLore(lore);
        getPhase().setItemMeta(itemMeta);
        inventory.setItem(slot, getPhase());
    }
}
