package net.omni.sellwand.messages;

import java.util.ArrayList;
import java.util.List;

public enum Messages {
    NO_PERMS("no_perms", "<red>You do not have permission to use this command.</red>"),
    ONLY_PLAYERS("only_players", "<red>Only players can use this command.</red>"),

    PLAYER_NOT_FOUND("player_not_found", "<red>Player %player% not found.</red>"),
    USAGE("usage", "<red>Invalid arguments. Usage: /sellwand give {player} {uses} [multiplier]</red>"),

    UNKNOWN_COMMAND("unknown_cmd", "<red>Unknown command.</red>"),
    RELOADED("reloaded", "<green>config.yml and messages.yml have been reloaded.</green>"),

    SOLD_ITEMS("sold_items", "<green>Sold <white>%amount%</white> item(s) for <gold>$%price%</gold>."),
    SOLD_ITEMS_WITH_BOOST("sold_items_with_boost", "<green>Sold <white>%amount%</white> item(s) for <gold>$%price%</gold> (<white>%multiplier%x</white> boost)."),
    SOLD_ITEMS_WITH_USAGE_BOOST("sold_items_with_usage_boost", "<green>Sold <white>%amount%</white> item(s) for <gold>$%price%</gold> (<white>%multiplier%x</white> boost). Uses left: <white>%uses%</white>"),

    SWITCH_MODES("switch_mode", "<green>Successfully switched from %before% to %after%.</green>"),

    LOG("console_log", "<gray>%player% sold %items% to %shop% for $%price% (%multiplier%x boost) | New Balance: $%new%</gray>"),

    NO_SELLABLE_ITEMS("no_sellable_items", "<red>No sellable items found in this container.</red>"),

    NOT_INTEGER("not_integer", "<red>Please provide an number.</red>"),

    NO_USES_LEFT("no_uses_left", "<red>This Sell Wand has no uses left!</red>"),

    WAND_GIVEN("wand_given", "<green>Given <white>%player%</white> a Sell Wand (%uses% uses, %multiplier%x multiplier).</green>"),
    WAND_RECEIVED("wand_received", "<green>You received a Sell Wand!</green>"),

    CONTAINER_NO_PERM("container_no_perm", "<red>You don't have permission to access this container.</red>"),

    CLAIM_NO_PERM("claim_no_perm", "<red>You don't have permission to access this container in this claim.</red>"),

    ECON_NOT_FOUND("econ_not_found", "<red>Economy plugin not found. Cannot process sale.</red>"),

    WAND_REMOVED("wand_removed", "<red>Your Sell Wand broke!</red>");

    private final String path;
    private final Object defaultVal;
    private Object cachedVal;

    Messages(String path, Object defaultVal) {
        this.path = path;
        this.defaultVal = defaultVal;
    }

    public String getPath() {
        return path;
    }

    public Object getDefaultVal() {
        return defaultVal;
    }

    public void setCachedVal(Object val) {
        this.cachedVal = val;
    }

    public String replace(String... pairs) {
        String result = this.toString();
        return replace(result, pairs);
    }

    @Override
    public String toString() {
        if (cachedVal instanceof List<?>)
            return "";

        return cachedVal instanceof String ? (String) cachedVal : (String) defaultVal;
    }

    private String replace(String result, String... pairs) {
        if (result.isEmpty())
            return "";

        for (int i = 0; i < pairs.length - 1; i += 2) {
            String key = pairs[i];
            String val = pairs[i + 1];
            if (key != null && val != null) {
                result = result.replace("%" + key + "%", val);
            }
        }

        return result;
    }

    public String replaceList(String... pairs) {
        List<String> originalList = this.asList();
        if (originalList.isEmpty())
            return "";

        List<String> modifiedList = new ArrayList<>();
        for (String line : originalList) {
            if (line != null)
                modifiedList.add(replace(line, pairs));
        }

        return String.join("\n", modifiedList);
    }

    @SuppressWarnings("unchecked")
    public List<String> asList() {
        return cachedVal instanceof List<?> ? (List<String>) cachedVal : (List<String>) defaultVal;
    }

    public void flush() {
        if (cachedVal instanceof List<?> cachedList)
            cachedList.clear();

        if (defaultVal instanceof List<?> defaultList)
            defaultList.clear();

        this.cachedVal = null;
    }
}
