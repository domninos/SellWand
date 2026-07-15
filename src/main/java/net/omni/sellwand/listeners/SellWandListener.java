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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (block == null) return;

        if (!plugin.getConfigUtil().getContainers().contains(block.getType()))
            return;

        Container container = (Container) block.getState();

        if (plugin.getConfigUtil().isCheckContainerPermissions()) {
            if (!player.hasPermission("sellwand.open")) {
                plugin.sendMessage(player, Messages.CONTAINER_NO_PERM.toString());
                return;
            }
        }

        EconomyProvider economy = plugin.getEconomyProvider();
        if (economy == null) {
            plugin.sendMessage(player, Messages.ECON_NOT_FOUND.toString());
            return;
        }

        Inventory inventory = container.getInventory();

        ItemStack[] items = inventory.getContents().clone();
        Set<Shop> shops = new HashSet<>();

        double totalPrice = 0.0;
        int totalAmount = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot == null || slot.getType().isAir())
                continue;

            int amount = slot.getAmount();

            ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(player, slot);

            double price = shopItem.getSellPriceForAmount(player, amount);
            if (price <= 0)
                continue;

            Shop shop = shopItem.getShop();
            shops.add(shop);

            totalPrice += price * amount;
            totalAmount += amount;

            inventory.clear(i);
        }

        if (totalAmount == 0) {
            plugin.sendMessage(player, Messages.NO_SELLABLE_ITEMS.toString());
            return;
        }

        double wandMultiplier = wandManager.getMultiplier(itemInHand);
        double combinedMultiplier;

        try {
            PriceModifier priceModifier = ShopGuiPlusApi.getPriceModifier(player, PriceModifierActionType.SELL);

            if (plugin.getConfigUtil().getMultiplierMode() == ConfigUtil.MultiplierMode.MULTIPLY)
                combinedMultiplier = wandMultiplier * priceModifier.getModifier();
            else
                combinedMultiplier = wandMultiplier + priceModifier.getModifier() - 1.0; // remove base 1x

        } catch (PlayerDataNotLoadedException e) {
            combinedMultiplier = wandMultiplier;
            plugin.sendConsole("<yellow>Could not load player price sell modifier. Using wand's multiplier only.</yellow>");
        }

        double finalPayout = totalPrice * combinedMultiplier;

        economy.deposit(player, finalPayout);

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

        // log
        // %player% sold all %amount% x %items% for $%price% to %shops% shop (%multiplier%x boost)
        if (plugin.getConfigUtil().isConsoleLogging()) {
            plugin.getLogger().info(Messages.LOG.replace(
                    "player", player.getName(),
                    "amount", String.format("%,d", totalAmount),
                    "items", Arrays.stream(items)
                            .filter(Objects::nonNull)
                            .map(item -> {
                                Component nameComp = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                                        ? item.getItemMeta().displayName()
                                        : Component.translatable(item.getType().translationKey());

                                assert nameComp != null;
                                String plainName = PlainTextComponentSerializer.plainText().serialize(nameComp);

                                return item.getAmount() + "x " + plainName;
                            })
                            .collect(Collectors.joining(", ")),
                    "price", String.format("%,.2f", finalPayout),
                    "shops", shops.stream().map(Shop::getName).collect(Collectors.joining(", ")),
                    "multiplier", wandManager.formatMultiplier(combinedMultiplier)
            ));
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
