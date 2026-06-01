package org.nooll.nullip;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class NullIp extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NullIp enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("NullIp disabled");
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!getConfig().getBoolean("settings.enabled", true)) {
            return;
        }

        String ip = event.getAddress().getHostAddress();
        String name = event.getName();

        if (isAllowed(ip, name)) {
            return;
        }

        String message = color(getConfig().getString(
                "settings.kick-message",
                "&cВход запрещён. Этот ник не привязан к вашему IP."
        ));

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, message);

        getLogger().warning("Blocked login: " + name + " from " + ip);
    }

    private boolean isAllowed(String ip, String name) {
        ConfigurationSection section = getConfig().getConfigurationSection("allowed");

        if (section == null) {
            return false;
        }

        if (!section.contains(ip)) {
            return false;
        }

        List<String> names = section.getStringList(ip);
        boolean ignoreCase = getConfig().getBoolean("settings.ignore-name-case", true);

        for (String allowedName : names) {
            if (allowedName == null) {
                continue;
            }

            if (ignoreCase) {
                if (allowedName.equalsIgnoreCase(name)) {
                    return true;
                }
            } else {
                if (allowedName.equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(color("&aNullIp перезагружен."));
            return true;
        }

        sender.sendMessage(color("&7Использование: &f/" + label + " reload"));
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}