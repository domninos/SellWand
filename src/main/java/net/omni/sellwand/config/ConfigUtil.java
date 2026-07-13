package net.omni.sellwand.config;

import net.omni.sellwand.SellWand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigUtil {
    private final SellWand plugin;

    private String wandMaterial;
    private String wandName;
    private List<String> wandLore;
    private int wandCustomModelData;
    private int wandDefaultUses;
    private double wandDefaultMultiplier;
    private boolean checkContainerPermissions;
    private boolean removeOnUseUp;

    public ConfigUtil(SellWand plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        flush();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        load();
    }

    public void flush() {
    }

    public void load() {
        AtomicInteger savedDefaults = new AtomicInteger();

        this.wandMaterial = getAndDefaultString("wand.material", "BLAZE_ROD", savedDefaults);
        this.wandName = getAndDefaultString("wand.name", "<gradient:#FFAA00:#FFFF55>Sell Wand</gradient>", savedDefaults);
        this.wandCustomModelData = getAndDefaultInt("wand.custom-model-data", 0, savedDefaults);
        this.wandDefaultUses = getAndDefaultInt("wand.default-uses", 100, savedDefaults);
        this.wandDefaultMultiplier = getAndDefaultDouble("wand.default-multiplier", 1.0, savedDefaults);
        this.checkContainerPermissions = getAndDefaultBoolean("settings.check-container-permissions", true, savedDefaults);
        this.removeOnUseUp = getAndDefaultBoolean("settings.remove-on-use-up", true, savedDefaults);

        List<String> defaultLore = new ArrayList<>();
        defaultLore.add("");
        defaultLore.add("<gray>Right-click a container to sell items.</gray>");
        defaultLore.add("");
        defaultLore.add("<yellow>Uses:</yellow> <white>%uses%</white>");
        defaultLore.add("<yellow>Multiplier:</yellow> <white>%multiplier%x</white>");

        this.wandLore = getAndDefaultStringList("wand.lore", defaultLore, savedDefaults);

        if (savedDefaults.get() > 0) {
            plugin.saveConfig();
            plugin.sendConsole("<green>Successfully loaded " + savedDefaults.get() + " default configuration(s)</green>");
        }

        plugin.sendConsole("<green>Successfully loaded config.yml</green>");
    }

    private String getAndDefaultString(String path, String defaultVal, AtomicInteger counter) {
        String temp = plugin.getConfig().getString(path);
        if (temp == null) {
            plugin.getConfig().set(path, defaultVal);
            counter.incrementAndGet();
            return defaultVal;
        }
        return temp;
    }

    private int getAndDefaultInt(String path, int defaultVal, AtomicInteger counter) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            counter.incrementAndGet();
            return defaultVal;
        }
        return plugin.getConfig().getInt(path);
    }

    private double getAndDefaultDouble(String path, double defaultVal, AtomicInteger counter) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            counter.incrementAndGet();
            return defaultVal;
        }
        return plugin.getConfig().getDouble(path);
    }

    private boolean getAndDefaultBoolean(String path, boolean defaultVal, AtomicInteger counter) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            counter.incrementAndGet();
            return defaultVal;
        }
        return plugin.getConfig().getBoolean(path);
    }

    private List<String> getAndDefaultStringList(String path, List<String> defaultVal, AtomicInteger counter) {
        if (!plugin.getConfig().contains(path)) {
            plugin.getConfig().set(path, defaultVal);
            counter.incrementAndGet();
            return defaultVal;
        }
        return plugin.getConfig().getStringList(path);
    }

    public String getWandMaterial() {
        return wandMaterial;
    }

    public String getWandName() {
        return wandName;
    }

    public List<String> getWandLore() {
        return wandLore;
    }

    public int getWandCustomModelData() {
        return wandCustomModelData;
    }

    public int getWandDefaultUses() {
        return wandDefaultUses;
    }

    public double getWandDefaultMultiplier() {
        return wandDefaultMultiplier;
    }

    public boolean isCheckContainerPermissions() {
        return checkContainerPermissions;
    }

    public boolean isRemoveOnUseUp() {
        return removeOnUseUp;
    }
}
