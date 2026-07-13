package net.omni.sellwand.managers;

import net.omni.sellwand.SellWand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WandManager {

    private final SellWand plugin;
    private final NamespacedKey wandKey;
    private final NamespacedKey usesKey;
    private final NamespacedKey multiplierKey;
    private final NamespacedKey reloadKey;

    public WandManager(SellWand plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "wand");
        this.usesKey = new NamespacedKey(plugin, "uses");
        this.multiplierKey = new NamespacedKey(plugin, "multiplier");
        this.reloadKey = new NamespacedKey(plugin, "reload");
    }

    public ItemStack createWand(int uses, double multiplier) {
        Material material = Material.matchMaterial(plugin.getConfigUtil().getWandMaterial());
        if (material == null)
            material = Material.BLAZE_ROD;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return item;

        int customModelData = plugin.getConfigUtil().getWandCustomModelData();
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }

        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
        meta.getPersistentDataContainer().set(multiplierKey, PersistentDataType.DOUBLE, multiplier);
        meta.getPersistentDataContainer().set(reloadKey, PersistentDataType.INTEGER, plugin.getReloadCount());

        plugin.getChatRenderer().setDisplayName(meta, plugin.getConfigUtil().getWandName());

        List<String> loreLines = buildLore(uses, multiplier);
        plugin.getChatRenderer().setLore(meta, loreLines);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    private List<String> buildLore(int uses, double multiplier) {
        List<String> configLore = plugin.getConfigUtil().getWandLore();
        List<String> result = new ArrayList<>();

        for (String line : configLore) {
            result.add(line
                    .replace("%uses%", String.valueOf(uses))
                    .replace("%multiplier%", formatMultiplier(multiplier))
            );
        }

        return result;
    }

    public String formatMultiplier(double multiplier) {
        if (multiplier == (long) multiplier)
            return String.valueOf((long) multiplier);

        return String.format("%.2f", multiplier);
    }

    public int getUses(ItemStack item) {
        if (!isWand(item))
            return 0;

        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return 0;

        Integer uses = meta.getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);
        return uses != null ? uses : 0;
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;

        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return false;

        return meta.getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    public void setUses(ItemStack item, int uses) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
        meta.getPersistentDataContainer().set(multiplierKey, PersistentDataType.DOUBLE, getMultiplier(item));

        plugin.getChatRenderer().setLore(meta, buildLore(uses, getMultiplier(item)));
        item.setItemMeta(meta);
    }

    public double getMultiplier(ItemStack item) {
        if (!isWand(item))
            return 1.0;

        ItemMeta meta = item.getItemMeta();

        if (meta == null) return 1.0;

        Double multi = meta.getPersistentDataContainer().get(multiplierKey, PersistentDataType.DOUBLE);
        return multi != null ? multi : 1.0;
    }

    public boolean needsUpdate(ItemStack item) {
        if (!isWand(item)) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        Integer storedReload = meta.getPersistentDataContainer().get(reloadKey, PersistentDataType.INTEGER);
        return storedReload == null || storedReload != plugin.getReloadCount();
    }

    public ItemStack updateWand(ItemStack item) {
        int uses = getUses(item);
        double multiplier = getMultiplier(item);
        return createWand(uses, multiplier);
    }
}
