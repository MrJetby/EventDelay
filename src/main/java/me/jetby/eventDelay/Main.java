package me.jetby.eventDelay;

import lombok.Getter;
import lombok.Setter;
import me.jetby.eventDelay.commands.EventCMD;
import me.jetby.eventDelay.commands.TabCompleter;
import me.jetby.eventDelay.configurations.Config;
import me.jetby.eventDelay.configurations.Messages;
import me.jetby.eventDelay.configurations.WebhookConfig;
import me.jetby.eventDelay.manager.Timer;
import me.jetby.eventDelay.manager.Triggers;
import me.jetby.eventDelay.tools.EventDelayAPI;
import me.jetby.eventDelay.tools.License;
import me.jetby.eventDelay.tools.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


import org.bstats.bukkit.Metrics;

import java.util.logging.Level;

import static me.jetby.eventDelay.configurations.Config.CFG;

@Getter
public final class Main extends JavaPlugin {
    public static Main INSTANCE;
    @Setter
    private EventDelayAPI eventDelayAPI;
    @Setter
    private License license;
    @Setter
    private EventCMD eventCMD;
    @Setter
    private Timer timer;
    @Setter
    private Triggers triggers;

    @Override
    public void onEnable() {
        INSTANCE = this;



        new Metrics(this, 23730);

        Bukkit.getServer().getConsoleSender().sendMessage("-------> Login <-------");
        license = new License(getConfig().getString("license.key"), getConfig().getString("license.url", "http://treexstudio.site"), this);
        license.request();
        Bukkit.getServer().getConsoleSender().sendMessage(" |- License checking: "+license.getLicense());
        if (license.isValid()) {


            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new PlaceholderAPI().register();
            }



            getLogger().log(Level.INFO, "------------------------");
            getLogger().log(Level.INFO, " (✔) Лицензия действительна");
            getLogger().log(Level.INFO, "------------------------");
            getLogger().log(Level.INFO, " |- Добро пожаловать: "+license.getLicensedTo());
            getLogger().log(Level.INFO, "------------------------");

        } else {
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Login denied");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Return error "+license.getReturn());
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");

            Bukkit.getPluginManager().disablePlugin(this);
        }

    }


    @Override
    public void onLoad() {
        Config config = new Config();
        config.loadYamlFile(this);

        Messages messages = new Messages();
        messages.loadYamlFile(this);

        WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.loadYamlFile(this);

        EventDelayAPI eventDelayAPI = new EventDelayAPI(CFG().getBoolean("Freeze", true),
                CFG().getInt("Timer", 1800),
                CFG().getInt("MinPlayers", 3),
                "none",
                "none",
                "none");

        setEventDelayAPI(eventDelayAPI);

        Triggers triggers = new Triggers(eventDelayAPI);
        setTriggers(triggers);

        Timer timer = new Timer(eventDelayAPI, triggers);
        setTimer(timer);


        getTimer().initialize();
        getTimer().startTimer();

        EventCMD eventCMD = new EventCMD(eventDelayAPI, timer, triggers);
        setEventCMD(eventCMD);
        getCommand("event").setExecutor(getEventCMD());
        getCommand("event").setTabCompleter(new TabCompleter());

        triggers.nextRandomEvent();
    }





}
