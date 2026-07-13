package net.omni.sellwand.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.omni.sellwand.SellWand;
import net.omni.sellwand.managers.WandManager;
import net.omni.sellwand.messages.MessageUtil;
import net.omni.sellwand.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SellWandCommand implements CommandExecutor, TabCompleter {

    private final SellWand plugin;

    public SellWandCommand(SellWand plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "about" -> {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(getAboutText()));
                return true;
            }

            case "give" -> {
                if (!sender.hasPermission("sellwand.admin")) {
                    plugin.sendMessage(sender, Messages.NO_PERMS.toString());
                    return true;
                }

                if (args.length > 4) {
                    plugin.sendMessage(sender, Messages.USAGE.toString());
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    plugin.sendMessage(sender, Messages.PLAYER_NOT_FOUND.replace(
                            "player", args[1]));
                    return true;
                }

                int uses = plugin.getConfigUtil().getWandDefaultUses();

                if (args.length >= 3) {
                    try {
                        uses = Integer.parseInt(args[2]);
                        if (uses <= 0)
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        plugin.sendMessage(sender, Messages.NOT_INTEGER.toString());
                        return true;
                    }
                }

                double multiplier = plugin.getConfigUtil().getWandDefaultMultiplier();
                if (args.length == 4) {
                    try {
                        multiplier = Double.parseDouble(args[3]);
                        if (multiplier <= 0)
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        plugin.sendMessage(sender, Messages.NOT_INTEGER.toString());
                        return true;
                    }
                }

                WandManager wandManager = plugin.getWandManager();

                target.getInventory().addItem(wandManager.createWand(uses, multiplier)).values().forEach(
                        item -> target.getWorld().dropItemNaturally(target.getLocation(), item)
                );

                plugin.sendMessage(sender, Messages.WAND_GIVEN.replace(
                        "player", target.getName(),
                        "uses", String.valueOf(uses),
                        "multiplier", wandManager.formatMultiplier(multiplier)));

                if (!sender.equals(target))
                    plugin.sendMessage(target, Messages.WAND_RECEIVED.toString());

                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("sellwand.admin")) {
                    plugin.sendMessage(sender, Messages.NO_PERMS.toString());
                    return true;
                }

                plugin.incrementReloadCount();
                plugin.getConfigUtil().reloadConfig();
                plugin.getMessagesManager().loadMessages();

                plugin.sendMessage(sender, Messages.RELOADED.toString());
                return true;
            }

            case "help" -> {
                sendHelp(sender);
                return true;
            }

            default -> {
                plugin.sendMessage(sender, Messages.UNKNOWN_COMMAND.toString());
                return true;
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        StringBuilder helpBuilder = new StringBuilder();

        helpBuilder.append("\n<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>\n");
        helpBuilder.append("  <gradient:#FFAA00:#FFFF55><bold>SellWand</bold></gradient>\n\n");

        if (sender.hasPermission("sellwand.admin")) {
            MessageUtil.append("sellwand <#FFFF55>give</#FFFF55> <player> <uses> [multiplier]", "Give a sell wand to a player.", helpBuilder);
            MessageUtil.append("sellwand <#FFFF55>about</#FFFF55>", "Shows plugin information.", helpBuilder);
            MessageUtil.append("sellwand <#FFFF55>reload</#FFFF55>", "Reload configs and messages.", helpBuilder);
        }

        MessageUtil.append("sellwand <#FFFF55>help</#FFFF55>", "Shows this help menu.", helpBuilder);

        helpBuilder.append("\n<dark_gray>▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪▪</dark_gray>");

        sender.sendMessage(MessageUtil.parse(helpBuilder.toString()));
    }

    private String getAboutText() {
        String pluginName = plugin.getDescription().getName();
        String version = plugin.getDescription().getVersion();
        String author = plugin.getDescription().getAuthors().getFirst();
        String githubUrl = "https://github.com/domninos/SelLWand";
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
        if (!sender.hasPermission("sellwand.admin"))
            return List.of();

        if (args.length == 1)
            return filterStartsWith(Arrays.asList("give", "reload", "help"), args[0]);

        if (args.length == 2 && args[0].equalsIgnoreCase("give"))
            return null;

        if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            return List.of(String.valueOf(plugin.getConfigUtil().getWandDefaultUses()));

        if (args.length == 4 && args[0].equalsIgnoreCase("give"))
            return List.of(String.valueOf(plugin.getConfigUtil().getWandDefaultMultiplier()));

        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }

    public void register() {
        PluginCommand sellWandCommand = plugin.getCommand("sellwand");

        if (sellWandCommand == null) {
            plugin.sendConsole("<red>/sellwand is not found in plugin.yml</red>");
            return;
        }

        sellWandCommand.setExecutor(this);
        sellWandCommand.setTabCompleter(this);
    }
}
