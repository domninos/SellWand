package net.omni.sellwand.listeners;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.player.PlayerDataNotLoadedException;
import net.brcdev.shopgui.modifier.PriceModifier;
import net.brcdev.shopgui.modifier.PriceModifierActionType;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.omni.sellwand.SellWand;
import net.omni.sellwand.config.ConfigUtil;
import net.omni.sellwand.managers.WandManager;
import net.omni.sellwand.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SellWandListener implements Listener {

    private final SellWand plugin;

    public SellWandListener(SellWand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        WandManager wandManager = plugin.getWandManager();
        if (!wandManager.isWand(itemInHand))
            return;

        event.setCancelled(true);

        if (wandManager.needsUpdate(itemInHand)) {
            itemInHand = wandManager.updateWand(itemInHand);
            player.getInventory().setItemInMainHand(itemInHand);
        }

        if (!player.hasPermission("sellwand.use")) {
            plugin.sendMessage(player, Messages.NO_PERMS.toString());
            return;
        }

        int uses = wandManager.getUses(itemInHand);
        if (uses <= 0) {
            plugin.sendMessage(player, Messages.NO_USES_LEFT.toString());
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (!plugin.getConfigUtil().getContainers().contains(block.getType()))
            return;

        Container container = (Container) block.getState();

        if (plugin.getConfigUtil().isCheckContainerPermissions()) {
            if (!player.hasPermission("sellwand.open")) {
                plugin.sendMessage(player, Messages.CONTAINER_NO_PERM.toString());
                return;
            }
        }

        if (!plugin.getGriefPreventionHook().hasClaimPerms(player, block)) {
            plugin.sendMessage(player, Messages.CLAIM_NO_PERM.toString());
            return;
        }

        Inventory inventory = container.getInventory();

        Map<Shop, Double> shopPrices = new HashMap<>();
        List<LogEntry> logEntries = new ArrayList<>();

        int totalAmount = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType().isAir())
                continue;

            ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(player, slot);
            if (shopItem == null)
                continue;

            int amount = slot.getAmount();
            double price = shopItem.getSellPriceForAmount(player, amount);
            if (price <= 0)
                continue;

            Shop shop = shopItem.getShop();
            shopPrices.merge(shop, price, Double::sum);
            logEntries.add(new LogEntry(shop, formatItemName(slot), amount, price));

            totalAmount += amount;
            inventory.clear(i);
        }

        if (totalAmount == 0) {
            plugin.sendMessage(player, Messages.NO_SELLABLE_ITEMS.toString());
            return;
        }

        double wandMultiplier = wandManager.getMultiplier(itemInHand);
        double combinedMultiplier;
        double modifier = 1;

        try {
            PriceModifier priceModifier = ShopGuiPlusApi.getPriceModifier(player, PriceModifierActionType.SELL);
            modifier = priceModifier.getModifier();

            if (plugin.getConfigUtil().getMultiplierMode() == ConfigUtil.MultiplierMode.MULTIPLY)
                combinedMultiplier = wandMultiplier * modifier;
            else
                combinedMultiplier = wandMultiplier + modifier - 1.0;

        } catch (PlayerDataNotLoadedException e) {
            combinedMultiplier = wandMultiplier;
            plugin.sendConsole("<yellow>Could not load player price sell modifier. Using wand's multiplier only.</yellow>");
        }

        double finalPayout = 0.0;

        for (Map.Entry<Shop, Double> entry : shopPrices.entrySet()) {
            double price = entry.getValue() / modifier; // price (already with multiplier)
            double shopTotal = price * combinedMultiplier;
            EconomyProvider shopEconomy = entry.getKey().getEconomyProvider();

            if (shopEconomy == null) {
                plugin.sendConsole("<yellow>Shop '" + entry.getKey().getName() + "' has no economy provider. Skipping.</yellow>");
                continue;
            }

            shopEconomy.deposit(player, shopTotal);
            finalPayout += shopTotal;
        }

        if (plugin.getConfigUtil().isConsoleLogging()) {
            Map<String, LogEntry> consolidated = new LinkedHashMap<>();

            for (LogEntry entry : logEntries) {
                String key = entry.shop().getName() + "|" + entry.itemName();
                consolidated.merge(key, entry, (a, b) ->
                        new LogEntry(a.shop(), a.itemName(),
                                a.amount() + b.amount(),
                                a.price() + b.price()));
            }

            for (LogEntry entry : consolidated.values()) {
                double price = entry.price() / modifier; // price (already with multiplier)
                double shopTotal = price * combinedMultiplier;

                String formattedLog = Messages.LOG.replace(
                        "player", player.getName(),
                        "items", String.format("%,d", entry.amount()) + "x " + entry.itemName(),
                        "price", String.format("%,.2f", shopTotal),
                        "shop", entry.shop().getId(),
                        "multiplier", wandManager.formatMultiplier(combinedMultiplier),
                        "new", String.format("%,.2f", entry.shop().getEconomyProvider().getBalance(player))
                );

                plugin.sendMessage(Bukkit.getConsoleSender(), formattedLog);
            }

            consolidated.clear(); // garbage
        }

        int newUses = uses - 1;

        if (newUses <= 0)
            plugin.sendMessage(player, Messages.SOLD_ITEMS_WITH_BOOST.replace(
                    "amount", String.format("%,d", totalAmount),
                    "price", String.format("%,.2f", finalPayout),
                    "multiplier", wandManager.formatMultiplier(combinedMultiplier)));
        else
            plugin.sendMessage(player, Messages.SOLD_ITEMS_WITH_USAGE_BOOST.replace(
                    "amount", String.format("%,d", totalAmount),
                    "price", String.format("%,.2f", finalPayout),
                    "multiplier", wandManager.formatMultiplier(combinedMultiplier),
                    "uses", String.valueOf(newUses)));

        wandManager.setUses(itemInHand, newUses);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        if (newUses == 0 && plugin.getConfigUtil().isRemoveOnUseUp()) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ARMOR_STAND_BREAK, 1f, 1f);
            plugin.sendMessage(player, Messages.WAND_REMOVED.toString());
        }

        shopPrices.clear(); // garbage
        logEntries.clear(); // garbage
    }

    private String formatItemName(ItemStack item) {
        Component nameComp = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().displayName()
                : Component.translatable(item.getType().translationKey());

        assert nameComp != null;
        return PlainTextComponentSerializer.plainText().serialize(nameComp);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private record LogEntry(Shop shop, String itemName, int amount, double price) {
    }
}
