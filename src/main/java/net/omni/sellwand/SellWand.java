package net.omni.sellwand;

import net.omni.sellwand.chat.ChatRenderer;
import net.omni.sellwand.chat.PaperChatRenderer;
import net.omni.sellwand.chat.SpigotChatRenderer;
import net.omni.sellwand.commands.SellWandCommand;
import net.omni.sellwand.config.ConfigUtil;
import net.omni.sellwand.config.SellWandConfig;
import net.omni.sellwand.managers.MessagesManager;
import net.omni.sellwand.messages.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SellWand extends JavaPlugin {

    private ChatRenderer chatRenderer;

    private SellWandConfig messagesConfig;
    private MessagesManager messagesManager;
    private ConfigUtil configUtil;

    @Override
    public void onDisable() {
        configUtil.flush();
        messagesManager.flush();
        sendConsole("<red>Successfully disabled.</red>");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initChatRenderer();

        this.messagesConfig = new SellWandConfig(this, "messages.yml");
        this.messagesManager = new MessagesManager(this);
        messagesManager.loadMessages();

        this.configUtil = new ConfigUtil(this);
        configUtil.load();


    }

    private void initChatRenderer() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            this.chatRenderer = new PaperChatRenderer();
            sendConsole("<green>PaperMC detected. Using PaperChatRenderer.</green>");
        } catch (ClassNotFoundException e) {
            this.chatRenderer = new SpigotChatRenderer();
            sendConsole("<gray>Spigot detected. Using SpigotChatRenderer.</gray>");
        }

        MessageUtil.init(chatRenderer);
    }

    public void sendConsole(String message) {
        chatRenderer.sendMessage(Bukkit.getConsoleSender(), chatRenderer.color(message));
    }

    private void registerHooks() {
    }

    private void registerCommands() {
        new SellWandCommand(this).register();
    }

    private void registerListeners() {
    }

    public void sendMessage(CommandSender sender, String message) {
        chatRenderer.sendMessage(sender, chatRenderer.color(message));
    }

    public SellWandConfig getMessagesConfig() {
        return messagesConfig;
    }

    public ConfigUtil getConfigUtil() {
        return configUtil;
    }

    public ChatRenderer getChatRenderer() {
        return chatRenderer;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
}
