package me.jetby.eventDelay;

import me.jetby.eventDelay.Commands.TabCompleter;
import me.jetby.eventDelay.Commands.EventCMD;
import me.jetby.eventDelay.Configs.Config;
import me.jetby.eventDelay.Configs.DB;
import me.jetby.eventDelay.Utils.License;
import me.jetby.eventDelay.Utils.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

import static me.jetby.eventDelay.Manager.Timer.startTimer;
import static me.jetby.eventDelay.Manager.Triggers.nextRandomEvent;

import org.bstats.bukkit.Metrics;
public final class Main extends JavaPlugin {
    public static YamlConfiguration cfg;
    public static YamlConfiguration db;
    public static YamlConfiguration messages;
    public static Main INSTANCE;
    public static boolean freeze;
    public static int timer;
    public static int minPlayers;
    public static String nowEvent;
    public static String nextEvent;
    public static int TimerUntilNextEvent;
    public static int duration;
    public static int TimeUntilDuration;
    public static int OpeningTimer;

    public static Main getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        cfgReload();
        messagesReload();
        dbReload();

        Bukkit.getServer().getConsoleSender().sendMessage("-------> Login <-------");
        License license = new License(getConfig().getString("license"), getConfig().getString("licenseLink", "https://treexstudio.site"), this);
        license.request();
        Bukkit.getServer().getConsoleSender().sendMessage(" |- License checking: "+license.getLicense());
        if (license.isValid()) {
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Login accepted");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Welcome: "+license.getLicensedTo());
            Bukkit.getServer().getConsoleSender().sendMessage(" |- I am enabling all");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- the features for you");
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");

            INSTANCE = this;

            // МЕТРИКА
            int pluginId = 23730;
            Metrics metrics = new Metrics(this, pluginId);


            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceholderAPI().register();
            }

            timer = Config.get().getTimer();
            freeze = Config.get().getFreeze();
            minPlayers = Config.get().getMinPlayers();
            nextEvent = DB.get().getNextEvent();
            nowEvent = DB.get().getNowEvent();

            getCommand("event").setExecutor(new EventCMD());
            getCommand("event").setTabCompleter(new TabCompleter());

            startTimer();
            nextRandomEvent();
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Login denied");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Return error "+license.getReturn());
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");

            Bukkit.getPluginManager().disablePlugin(this);
        }

    }



    public void cfgReload() {
        File file = new File(getDataFolder().getAbsolutePath() + "/config.yml");
        if (file.exists()) {
            getLogger().log(Level.INFO, "Конфиг успешно загружен. (config.yml)");
            cfg = YamlConfiguration.loadConfiguration(file);
        } else {
            saveResource("config.yml", false);
            cfg = YamlConfiguration.loadConfiguration(file);
        }
    }

    public void dbReload() {
        File file = new File(getDataFolder().getAbsolutePath() + "/db.yml");
        if (file.exists()) {
            getLogger().log(Level.INFO, "Конфиг успешно загружен. (db.yml)");
            db = YamlConfiguration.loadConfiguration(file);
        } else {
            saveResource("db.yml", false);
            db = YamlConfiguration.loadConfiguration(file);
        }
    }

    public void dbSave() {
        try {
            File file = new File(getDataFolder(), "db.yml");
            db.save(file);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Не удалось сохранить файл db.yml", e);
        }
    }

    public void messagesReload() {
        File file = new File(getDataFolder(), "messages.yml");
        if (file.exists()) {
            getLogger().log(Level.INFO, "Конфиг успешно загружен. (messages.yml)");
            messages = YamlConfiguration.loadConfiguration(file);
        } else {
            saveResource("messages.yml", false);
            messages = YamlConfiguration.loadConfiguration(file);
        }
    }

    public void cfgSave() {
        try {
            File file = new File(getDataFolder(), "config.yml");
            cfg.save(file);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Не удалось сохранить файл config.yml", e);
        }
    }
}
