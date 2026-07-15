package net.omni.sellwand;

import net.omni.sellwand.chat.ChatRenderer;
import net.omni.sellwand.chat.PaperChatRenderer;
import net.omni.sellwand.chat.SpigotChatRenderer;
import net.omni.sellwand.commands.SellWandCommand;
import net.omni.sellwand.config.ConfigUtil;
import net.omni.sellwand.config.SellWandConfig;
import net.omni.sellwand.listeners.SellWandListener;
import net.omni.sellwand.managers.MessagesManager;
import net.omni.sellwand.managers.WandManager;
import net.omni.sellwand.messages.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SellWand extends JavaPlugin {

    private ChatRenderer chatRenderer;

    private SellWandConfig messagesConfig;
    private MessagesManager messagesManager;
    private ConfigUtil configUtil;
    private WandManager wandManager;

    private int reloadCount = 0;

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

        this.wandManager = new WandManager(this);

        registerHooks();
        registerCommands();
        registerListeners();
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

    private void registerHooks() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null)
            sendConsole("<yellow>Vault not found. Economy features will not work.</yellow>");

        if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") == null)
            sendConsole("<yellow>ShopGUIPlus not found. Economy features will not work.</yellow>");
    }

    private void registerCommands() {
        new SellWandCommand(this).register();
    }

    private void registerListeners() {
        new SellWandListener(this).register();
    }

    public void sendConsole(String message) {
        chatRenderer.sendMessage(Bukkit.getConsoleSender(), chatRenderer.color(message));
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

    public WandManager getWandManager() {
        return wandManager;
    }

    public int getReloadCount() {
        return reloadCount;
    }

    public void incrementReloadCount() {
        reloadCount++;
    }
}
