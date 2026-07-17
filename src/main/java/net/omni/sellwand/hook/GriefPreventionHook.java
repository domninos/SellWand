package net.omni.sellwand.hook;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.omni.sellwand.SellWand;
import net.omni.sellwand.messages.Messages;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class GriefPreventionHook {
    private final SellWand plugin;
    private boolean enabled = false;

    public GriefPreventionHook(SellWand plugin) {
        this.plugin = plugin;
    }

    public boolean hasClaimPerms(Player player, Block block) {
        if (!isEnabled())
            return true;

        GriefPrevention gp = GriefPrevention.instance;
        Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);

        if (claim != null) {
            Supplier<String> denial = claim.checkPermission(player, ClaimPermission.Inventory, null);

            if (denial != null && denial.get() != null) {
                plugin.sendMessage(player, Messages.CLAIM_NO_PERM.toString());
                return false;
            }

            return true;
        }

        return false;
    }

    public void init() {
        this.enabled = true;

        plugin.sendConsole("<green>Successfully hooked into GriefPrevention</green>");
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
