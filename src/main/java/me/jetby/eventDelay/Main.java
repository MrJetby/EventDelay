package me.jetby.eventDelay;

import lombok.Getter;
import lombok.Setter;
import me.jetby.eventDelay.commands.EventCMD;
import me.jetby.eventDelay.configurations.Config;
import me.jetby.eventDelay.configurations.Messages;
import me.jetby.eventDelay.configurations.WebhookConfig;
import me.jetby.eventDelay.manager.Assistants;
import me.jetby.eventDelay.manager.Timer;
import me.jetby.eventDelay.manager.Triggers;
import me.jetby.eventDelay.tools.EventDelayAPI;
import me.jetby.eventDelay.tools.EventDelayExpansion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


@Getter
public final class Main extends JavaPlugin {

    private EventDelayAPI eventDelayAPI;
    private Timer timer;
    private Triggers triggers;
    @Setter
    private Config cfg;
    private Assistants assistants;
    private final WebhookConfig webhookConfig = new WebhookConfig(this);
    private final Messages messages = new Messages(this);

    @Override
    public void onEnable() {
        cfg = new Config(this);
        cfg.load();

        final FileConfiguration messagesFile = cfg.getFileConfiguration(getDataFolder().getAbsolutePath(), "messages.yml");
        final FileConfiguration webhookFile = cfg.getFileConfiguration(getDataFolder().getAbsolutePath(), "webhook.yml");


        messages.load(messagesFile);
        webhookConfig.load(webhookFile);

        loadPlugin();


    }
    public void loadPlugin(Player player) {
        eventDelayAPI = new EventDelayAPI(cfg.isFreeze(),
                cfg.getTimer(),
                cfg.getMinPlayers(),
                "none",
                "none",
                "none");

        assistants = new Assistants(this, eventDelayAPI);
        triggers = new Triggers(this);
        timer = new Timer(this);
        timer.initialize();
        timer.startTimer();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EventDelayExpansion(this).register();
        }
        getCommand("event").setExecutor(new EventCMD(this));

        Bukkit.getScheduler().runTaskLater(this, ()-> {assistants.createNextRandomEvent();}, 5L);


        new Metrics(this, 23730);

        player.sendMessage("§aПлагин успешно запущен и готов к использованию!");
    }
    public void loadPlugin() {
        eventDelayAPI = new EventDelayAPI(cfg.isFreeze(),
                cfg.getTimer(),
                cfg.getMinPlayers(),
                "none",
                "none",
                "none");

        assistants = new Assistants(this, eventDelayAPI);
        triggers = new Triggers(this);
        timer = new Timer(this);
        timer.initialize();
        timer.startTimer();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EventDelayExpansion(this).register();
        }
        getCommand("event").setExecutor(new EventCMD(this));

        Bukkit.getScheduler().runTaskLater(this, ()-> {assistants.createNextRandomEvent();}, 5L);
        new Metrics(this, 23730);
    }

}
