package net.omni.sellwand.managers;
import net.omni.sellwand.SellWand;
import net.omni.sellwand.messages.Messages;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MessagesManager {
    private final SellWand plugin;

    public MessagesManager(SellWand plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        plugin.getMessagesConfig().reload();

        FileConfiguration config = plugin.getMessagesConfig().getConfig();

        int savedDefaults = 0;

        for (Messages message : Messages.values()) {
            if (message.getDefaultVal() instanceof List<?>) {
                if (!config.contains(message.getPath())) {
                    config.set(message.getPath(), message.getDefaultVal());
                    savedDefaults++;
                }

                message.setCachedVal(config.getStringList(message.getPath()));
            } else {
                if (!config.contains(message.getPath())) {
                    config.set(message.getPath(), message.getDefaultVal());
                    savedDefaults++;
                }

                message.setCachedVal(config.getString(message.getPath()));
            }
        }

        if (savedDefaults > 0) {
            plugin.getMessagesConfig().save();

            plugin.sendConsole("<green>Successfully loaded " + savedDefaults + " default message(s).</green>");
        }
    }

    public void flush() {
        for (Messages message : Messages.values())
            message.flush();
    }
}
