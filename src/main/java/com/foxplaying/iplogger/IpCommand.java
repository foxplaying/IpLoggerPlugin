// IpCommand.java
package com.foxplaying.iplogger;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.*;

public class IpCommand implements CommandExecutor, TabCompleter {
    private final IpLoggerPlugin plugin;

    public IpCommand(IpLoggerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("iplogger.admin")) {
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§6" + plugin.getLangMessage("usage"));
            sender.sendMessage("§7/getip <" + plugin.getLangMessage("label-playername") + "> §f- " + plugin.getLangMessage("help-query-ip"));
            sender.sendMessage("§7/getip reload §f- " + plugin.getLangMessage("help-reload"));
            sender.sendMessage("§7/getip version §f- " + plugin.getLangMessage("help-version"));
            sender.sendMessage("§7/getip language <zh|en> §f- " + plugin.getLangMessage("help-language"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                plugin.reloadPluginConfig();
                plugin.loadLanguageConfig();
                sender.sendMessage("§a" + plugin.getLangMessage("reload"));
                return true;

            case "version":
                sender.sendMessage("§7" + plugin.getLangMessage("version-checking"));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    if (!plugin.isEnabled()) return;
                    String latest = VersionChecker.fetchLatestVersion();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!plugin.isEnabled()) return;

                        if (latest == null) {
                            sender.sendMessage("§c" + plugin.getLangMessage("version-check-failed"));
                        } else {
                            String clean = latest.replaceFirst("^v", "");
                            if (!clean.equals(plugin.getDescription().getVersion())) {
                                sender.sendMessage(String.format("§a" + plugin.getLangMessage("version-update"),
                                        plugin.getDescription().getVersion(), latest));
                                sender.sendMessage("§b" + plugin.getLangMessage("project-home") + ": §9https://github.com/foxplaying/IpLoggerPlugin");
                            } else {
                                sender.sendMessage(String.format("§a" + plugin.getLangMessage("version-latest"),
                                        plugin.getDescription().getVersion()));
                            }
                        }
                    });
                });
                return true;

            case "language":
                if (args.length < 2) {
                    sender.sendMessage("§e" + plugin.getLangMessage("language-usage"));
                    return true;
                }
                String newLang = args[1].toLowerCase();
                if (!newLang.equals("zh") && !newLang.equals("en")) {
                    sender.sendMessage("§c" + plugin.getLangMessage("language-invalid"));
                    return true;
                }
                plugin.setLanguage(newLang);
                plugin.loadLanguageConfig();
                sender.sendMessage(String.format("§a" + plugin.getLangMessage("language-success"), newLang));
                return true;

            default:
                final String target = sub;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    if (!plugin.isEnabled()) return;
                    IpDatabase.IpRecord record = plugin.getDatabase().getLatestIpInfo(target);
                    if (record != null) {
                        sender.sendMessage("§b" + plugin.getLangMessage("ip-record-header"));

                        sendCopyable(sender, plugin.getLangMessage("label-playername"), record.name);
                        sendCopyable(sender, plugin.getLangMessage("label-uuid"), record.uuid);
                        sendCopyable(sender, plugin.getLangMessage("label-ipv4"), record.ipv4 != null ? record.ipv4 : plugin.getLangMessage("none"));
                        sendCopyable(sender, plugin.getLangMessage("label-ipv6"), record.ipv6 != null ? record.ipv6 : plugin.getLangMessage("none"));

                        sender.sendMessage("§7" + plugin.getLangMessage("last-recorded-time") + ": §f" + record.time);
                    } else {
                        sender.sendMessage(String.format("§c" + plugin.getLangMessage("record-not-found"), target));
                    }
                });
                return true;
        }
    }

    private void sendCopyable(CommandSender sender, String label, String value) {
        TextComponent line = new TextComponent("§7" + label + ": §f" + value);
        line.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value));
        line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.getLangMessage("copy-hover") + label)));
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(line);
        } else {
            sender.sendMessage("§7" + label + ": §f" + value);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();

            String[] baseSubs = {"help", "reload", "version", "language"};
            for (String sub : baseSubs) {
                if (sub.startsWith(prefix)) {
                    matches.add(sub);
                }
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    matches.add(p.getName());
                }
            }

            Collections.sort(matches);
            return matches;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("language")) {
            List<String> langs = new ArrayList<>();
            if ("zh".startsWith(args[1].toLowerCase())) langs.add("zh");
            if ("en".startsWith(args[1].toLowerCase())) langs.add("en");
            return langs;
        }

        return Collections.emptyList();
    }
}