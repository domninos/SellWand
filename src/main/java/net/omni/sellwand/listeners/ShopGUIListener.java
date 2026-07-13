package net.omni.sellwand.listeners;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.event.ShopGUIPlusPostEnableEvent;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.omni.sellwand.SellWand;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ShopGUIListener implements Listener {

    private final SellWand plugin;

    public ShopGUIListener(SellWand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopGUILoad(ShopGUIPlusPostEnableEvent event) {
        plugin.sendConsole("<yellow>ShopGUI+ detected. Waiting for economy provider...</yellow>");

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            try {
                EconomyProvider provider = ShopGuiPlusApi.getPlugin()
                        .getEconomyManager().getDefaultEconomyProvider();

                if (provider != null) {
                    plugin.setEconomyProvider(provider);
                    plugin.sendConsole("<green>Successfully hooked into ShopGUI+ economy.</green>");
                    task.cancel();
                }
            } catch (Exception ignored) {
            }
        }, 20L, 20L);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
