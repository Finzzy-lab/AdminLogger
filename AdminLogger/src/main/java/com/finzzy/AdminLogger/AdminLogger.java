package com.finzzy.AdminLogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AdminLogger extends JavaPlugin implements Listener {

    private FileConfiguration messages;
    private String language;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        language = getConfig().getString("language", "tr");
        loadMessages(language);
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info(getConsoleMessage("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(getConsoleMessage("plugin.disabled"));
    }

    private void loadMessages(String lang) {
        String resourcePath = "lang/" + lang + ".yml";
        try (InputStream is = getResource(resourcePath)) {
            if (is == null) {
                getLogger().warning("Dil dosyası bulunamadı: " + resourcePath);
                messages = new YamlConfiguration();
                return;
            }
            messages = YamlConfiguration.loadConfiguration(new InputStreamReader(is));
        } catch (IOException e) {
            e.printStackTrace();
            messages = new YamlConfiguration();
        }
    }

    // Placeholder sınıfı
    private record Placeholder(String key, String value) {}

    // Mesaj renklerini işle
    // playerName: Altın
    // ara cümleler: Yeşil
    // komutlar, item, target, effect, entity, gamemode: Kırmızı
    // [AdminLogger] prefix: Sarı
    private String formatChatMessage(String rawMessage, Placeholder... placeholders) {
        String prefix = ChatColor.YELLOW + "[AdminLogger] " + ChatColor.RESET;
        String msg = rawMessage;

        for (Placeholder p : placeholders) {
            String replacement = p.value;
            switch (p.key) {
                case "player" -> replacement = ChatColor.GOLD + p.value + ChatColor.GREEN;
                case "gamemode", "oldmode", "newmode",
                     "target", "effect", "entity", "details", "command", "args", "item", "amount" ->
                        replacement = ChatColor.RED + p.value + ChatColor.GREEN;
            }
            msg = msg.replace("{" + p.key + "}", replacement);
        }
        return prefix + ChatColor.GREEN + msg;
    }

    private String getMessage(String path, Placeholder... placeholders) {
        String rawMessage = messages.getString(path, "");
        if (rawMessage.isEmpty()) return "";
        return formatChatMessage(rawMessage, placeholders);
    }

    private String getConsoleMessage(String path) {
        return "[AdminLogger] " + messages.getString(path, "");
    }

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode oldMode = player.getGameMode();
        GameMode newMode = event.getNewGameMode();

        // Dil dosyasından mesaj al
        String rawMessage = messages.getString("events.gamemode-change", "");
        if (rawMessage.isEmpty()) return;

        // Mesajdaki placeholderları renklendirerek değiştir
        // Player adı altın, modlar kırmızı, diğer metin yeşil ve prefix sarı
        String message = formatChatMessage(rawMessage,
                new Placeholder("player", player.getName()),
                new Placeholder("oldmode", oldMode.name().toUpperCase()),
                new Placeholder("newmode", newMode.name().toUpperCase()));

        Bukkit.broadcastMessage(message);
    }

    @EventHandler
    public void onCreativeItemSpawn(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() != GameMode.CREATIVE) return;

        ItemStack item = event.getCursor();
        if (item == null || item.getType().isAir()) return;

        String msg = getMessage("events.creative-item-take",
                new Placeholder("player", player.getName()),
                new Placeholder("amount", String.valueOf(item.getAmount())),
                new Placeholder("item", item.getType().name()));
        Bukkit.broadcastMessage(msg);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = "";
        String[] args = event.getMessage().split(" ");

        String label = args[0].toLowerCase();

        switch (label) {

            case "/give" -> {
                if (args.length >= 3)
                    msg = getMessage("commands.give",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]),
                            new Placeholder("item", args[2]));
            }
            case "/tp", "/teleport" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.tp",
                            new Placeholder("player", player.getName()),
                            new Placeholder("details", event.getMessage().substring(label.length()).trim()));
            }
            case "/kill" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.kill",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]));
            }
            case "/ban" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.ban",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]));
            }
            case "/kick" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.kick",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]));
            }
            case "/op" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.op",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]));
            }
            case "/deop" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.deop",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]));
            }
            case "/effect" -> {
                if (args.length >= 3)
                    msg = getMessage("commands.effect",
                            new Placeholder("player", player.getName()),
                            new Placeholder("target", args[1]),
                            new Placeholder("effect", args[2]));
            }
            case "/summon" -> {
                if (args.length >= 2)
                    msg = getMessage("commands.summon",
                            new Placeholder("player", player.getName()),
                            new Placeholder("entity", args[1]));
            }
            case "/spawnpoint" -> {
                msg = getMessage("commands.spawnpoint",
                        new Placeholder("player", player.getName()));
            }
            case "/setblock", "/fill", "/clone" -> {
                msg = getMessage("commands.build-command",
                        new Placeholder("player", player.getName()),
                        new Placeholder("command", label),
                        new Placeholder("args", String.join(" ", args)));
            }
            case "/setworldspawn" -> {
                msg = getMessage("commands.setworldspawn",
                        new Placeholder("player", player.getName()));
            }
            case "/reload" -> {
                msg = getMessage("commands.reload",
                        new Placeholder("player", player.getName()));
            }
        }

        if (!msg.isEmpty()) {
            Bukkit.broadcastMessage(msg);
            getLogger().info("[Komut Logu] " + player.getName() + ": " + event.getMessage());
        }
    }
}
