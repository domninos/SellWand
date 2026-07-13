package net.omni.sellwand.commands;

import net.omni.sellwand.SellWand;
import net.omni.sellwand.messages.MessageUtil;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SellWandCommand implements CommandExecutor, TabCompleter {

    private final SellWand plugin;

    public SellWandCommand(SellWand plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return false;
    }

    private void sendHelp(CommandSender sender) {
        StringBuilder helpBuilder = new StringBuilder();

        helpBuilder.append("\n<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>\n");
        helpBuilder.append("  <gradient:#00AAFF:#55FFFF><bold>SellWand</bold></gradient> <gray>\n\n");

        if (sender.hasPermission("crateybackpack.admin")) {
            MessageUtil.append("crateybackpack <#55FFFF>help</#55FFFF>", "Shows this help menu.", helpBuilder);
            MessageUtil.append("crateybackpack <#55FFFF>about</#55FFFF>", "Shows plugin information.", helpBuilder);
            MessageUtil.append("crateybackpack <#55FFFF>reload</#55FFFF>", "Reloads configs, messages, and Cratey cache.", helpBuilder, "cbp");
        }

        helpBuilder.append("<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>");

        sender.sendMessage(MessageUtil.parse(helpBuilder.toString()));
    }

    private String getAboutText() {
        String pluginName = plugin.getDescription().getName();
        String version = plugin.getDescription().getVersion();
        String author = plugin.getDescription().getAuthors().getFirst();
        String githubUrl = "https://github.com/domninos/SellWand";
        String discordUrl = "https://discord.gg/7CuCtDHmQ3";

        return "<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>\n" +
                "  <gradient:#00AAFF:#55FFFF><bold>" + pluginName + "</bold></gradient>\n\n" +
                "  <yellow>Version:</yellow> <white>" + version + "</white>\n" +
                "  <yellow>Author:</yellow> <aqua>" + author + "</aqua>\n\n" +
                "  <white>Links: </white>" +
                "<click:open_url:'" + githubUrl + "'><hover:show_text:'<gray>View source code on GitHub</gray>'><dark_purple>[GitHub]</dark_purple></hover></click> " +
                "<click:open_url:'" + discordUrl + "'><hover:show_text:'<gray>Join the support Discord</gray>'><blue>[Discord]</blue></hover></click>\n" +
                "<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }

    public void register() {
        PluginCommand sellWandCommand = plugin.getCommand("sellwand");

        if (sellWandCommand == null) {
            plugin.sendConsole("<red>/sellwand is not found in plugin.yml");
            return;
        }

        sellWandCommand.setExecutor(this);
        sellWandCommand.setTabCompleter(this);
    }
}
