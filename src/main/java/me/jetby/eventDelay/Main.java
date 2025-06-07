package me.jetby.eventDelay;

import lombok.Getter;
import me.jetby.eventDelay.commands.EventCMD;
import me.jetby.eventDelay.configurations.Config;
import me.jetby.eventDelay.configurations.Messages;
import me.jetby.eventDelay.configurations.WebhookConfig;
import me.jetby.eventDelay.manager.Timer;
import me.jetby.eventDelay.manager.Triggers;
import me.jetby.eventDelay.tools.EventDelayAPI;
import me.jetby.eventDelay.tools.EventDelayExpansion;
import me.jetby.eventDelay.tools.License;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static me.jetby.eventDelay.configurations.Config.CFG;

@Getter
public final class Main extends JavaPlugin {

    private EventDelayAPI eventDelayAPI;
    private License license;
    private Timer timer;
    private Triggers triggers;

    @Override
    public void onEnable() {

        Config config = new Config();
        config.loadYamlFile(this);

        Messages messages = new Messages();
        messages.loadYamlFile(this);

        WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.loadYamlFile(this);


        getLogger().info("-------> Login <-------");
        license = new License(getConfig().getString("license.key"), this);
        license.request();
        Bukkit.getConsoleSender().sendMessage("["+getName()+"]"+" §b♻ Проверка лицензии... ");
        if (license.isValid()) {
            getLogger().info("");
            Bukkit.getConsoleSender().sendMessage("["+getName()+"]"+" §a✔ Лицензия действительна ");
            getLogger().info("");
            getLogger().info("-----------------------");


            eventDelayAPI = new EventDelayAPI(CFG().getBoolean("Freeze", true),
                    CFG().getInt("Timer", 1800),
                    CFG().getInt("MinPlayers", 3),
                    "none",
                    "none",
                    "none");
            triggers = new Triggers(this);
            timer = new Timer(this);
            timer.initialize();
            timer.startTimer();
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new EventDelayExpansion(this).register();
            }
            getCommand("event").setExecutor(new EventCMD(this));
            triggers.nextRandomEvent();
            new Metrics(this, 23730);
            return;

        } else if (!license.isValid()){
            getLogger().info("");
            Bukkit.getConsoleSender().sendMessage("["+getName()+"]"+" §c"+license.getReturn());
            getLogger().info("");
            Bukkit.getConsoleSender().sendMessage("["+getName()+"]"+" §eЕсли вы не смогли решить проблему самостоятельно,");
            Bukkit.getConsoleSender().sendMessage("["+getName()+"]"+" §eтогда обратитесь к нам: §bhttps://dsc.gg/treexstudio");
            getLogger().info("------------------------");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().disablePlugin(this);

    }

}
