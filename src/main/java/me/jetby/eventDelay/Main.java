package me.jetby.eventDelay;

import me.jetby.eventDelay.configurations.Config;
import me.jetby.eventDelay.configurations.Messages;
import me.jetby.eventDelay.configurations.WebhookConfig;
import me.jetby.eventDelay.tools.EventDelayAPI;
import me.jetby.eventDelay.tools.License;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


import org.bstats.bukkit.Metrics;

public final class Main extends JavaPlugin {
    public static YamlConfiguration messages;
    private static Main INSTANCE;

    public static Main getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        Config config = new Config();
        config.loadYamlFile(this);

        Messages messages = new Messages();
        messages.loadYamlFile(this);

        WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.loadYamlFile(this);

        new Metrics(this, 23730);

        Bukkit.getServer().getConsoleSender().sendMessage("-------> Login <-------");
        License license = new License(getConfig().getString("license"), getConfig().getString("licenseLink", "http://treexstudio.site"), this);
        license.request();
        Bukkit.getServer().getConsoleSender().sendMessage(" |- License checking: "+license.getLicense());
        if (license.isValid()) {

            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Login accepted");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Welcome: "+license.getLicensedTo());
            Bukkit.getServer().getConsoleSender().sendMessage(" |- I am enabling all");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- the features for you");
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");

        } else {
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Login denied");
            Bukkit.getServer().getConsoleSender().sendMessage(" |- Return error "+license.getReturn());
            Bukkit.getServer().getConsoleSender().sendMessage("------------------------");

            Bukkit.getPluginManager().disablePlugin(this);
        }

    }


    public EventDelayAPI getAPI() {
        return new EventDelayAPI();
    }

}
