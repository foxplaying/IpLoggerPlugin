// IpLoggerPlugin.java
package com.foxplaying.iplogger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class IpLoggerPlugin extends JavaPlugin implements Listener {
    private static IpLoggerPlugin instance;
    private IpDatabase database;
    private boolean joinLogEnabled = false;
    private String language = "zh";
    private FileConfiguration langConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadPluginConfig();

        database = new IpDatabase(this);
        database.init();

        getCommand("getip").setExecutor(new IpCommand(this));
        getCommand("getip").setTabCompleter(new IpCommand(this));
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info(String.format(
            getLangMessage("plugin-enabled"),
            joinLogEnabled ? getLangMessage("enabled") : getLangMessage("disabled"),
            getLangMessage("language")
));
    }

    @Override
    public void onDisable() {
        if (database != null) database.close();
        getLogger().info(getLangMessage("plugin-disabled"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String name = player.getName();

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String[] ips = GetIp.fetchBoth(); // [0] IPv4, [1] IPv6
            database.savePlayerIp(uuid, name, ips[0], ips[1]);

            if (joinLogEnabled) {
                String displayIp = (ips[0] != null && !ips[0].isEmpty()) ? ips[0] :
                        (ips[1] != null && !ips[1].isEmpty()) ? ips[1] : getLangMessage("none");
                Bukkit.getScheduler().runTask(this, () -> {
                    getLogger().info(String.format(getLangMessage("player-join-log"), name, displayIp));
                });
            }
        });
    }

    public IpDatabase getDatabase() {
        return database;
    }

    public static IpLoggerPlugin getInstance() {
        return instance;
    }

    public boolean isJoinLogEnabled() {
        return joinLogEnabled;
    }

    public void setJoinLogEnabled(boolean val) {
        this.joinLogEnabled = val;
        getConfig().set("join-log-enabled", val);
        saveConfig();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        this.language = lang;
        getConfig().set("language", lang);
        saveConfig();
        loadLanguageConfig();
    }

    public void reloadPluginConfig() {
        reloadConfig();
        joinLogEnabled = getConfig().getBoolean("join-log-enabled", true);
        language = getConfig().getString("language", "zh");
        loadLanguageConfig();
    }

    public void loadLanguageConfig() {
        File langFile = new File(getDataFolder(), "language_" + language + ".yml");
        if (!langFile.exists()) {
            saveResource("language_" + language + ".yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getLangMessage(String key) {
        if (langConfig == null) loadLanguageConfig();
        return langConfig.getString(key, key);
    }
}
