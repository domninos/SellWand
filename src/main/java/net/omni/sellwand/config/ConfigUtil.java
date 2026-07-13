package net.omni.sellwand.config;
import net.omni.sellwand.SellWand;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ConfigUtil {
    private final SellWand plugin;

    private String databaseFile;
    private int autoSaveInterval;
    private List<String> visibleKeys;
    private List<String> knownKeys;
    private Material fillerMaterial;

    private String guiTitle;
    private String keyNameFormat;
    private String fillerName;
    private List<String> hasKeyLore;
    private List<String> noKeyLore;
    private String openSound;
    private String claimSound;
    private String failSound;

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

        if (savedDefaults.get() > 0) {
            plugin.saveConfig();
            plugin.sendConsole("<green>Successfully loaded " + savedDefaults.get() + " default configuration(s)</green>");
        }

        plugin.sendConsole("<green>Successfully loaded config.yml</green>");
    }

    private String getAndDefaultString(String path, String defaultVal, IntConsumer consumer) {
        String temp = plugin.getConfig().getString(path);

        if (temp == null) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }

        return temp;
    }

    private int getAndDefaultInt(String path, int defaultVal, IntConsumer consumer) {
        int temp = plugin.getConfig().getInt(path);

        if (!plugin.getConfig().contains(path) || temp == 0) {
            plugin.getConfig().set(path, defaultVal);
            consumer.accept(1);
            return defaultVal;
        }

        return temp;
    }

}
