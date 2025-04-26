package me.jetby.eventDelay.manager;

import me.jetby.eventDelay.Main;
import me.jetby.eventDelay.tools.Actions;
import me.jetby.eventDelay.tools.EventDelayAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.manager.Assistants.getRandomEvent;

public class Triggers {

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;

    public Triggers(Main plugin) {
        this.plugin = plugin;
        this.eventDelayAPI = plugin.getEventDelayAPI();
    }

    public void nextRandomEvent() {
        if (eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            eventDelayAPI.setNextEvent(getRandomEvent());
        }
    }

    public void startRandomEvent() {
        if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            eventDelayAPI.setNowEvent(eventDelayAPI.getNextEvent());
        } else {
            stopEvent(eventDelayAPI.getNowEvent());
            eventDelayAPI.setNowEvent(eventDelayAPI.getNextEvent());
        }

        eventDelayAPI.setNextEvent(getRandomEvent());
        triggerEvent(eventDelayAPI.getNowEvent());
    }


    public void stopEvent(String eventName) {
        if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return;
        }

        eventDelayAPI.setOpeningTimer(CFG().getInt("Events." + eventName + ".Duration"));
        List<String> commands = CFG().getStringList("Events." + eventName + ".onEnd");

        for (Player player : Bukkit.getOnlinePlayers()) {
            Actions.execute(plugin, player, commands);
        }
        eventDelayAPI.setPreviousEvent(eventDelayAPI.getNowEvent());
        eventDelayAPI.setNowEvent("none");
        eventDelayAPI.setNextEvent(getRandomEvent());

    }

    public void triggerNextEvent() {
        if (eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            eventDelayAPI.setNextEvent(getRandomEvent());
        }

        eventDelayAPI.setNowEvent(eventDelayAPI.getNextEvent());
        eventDelayAPI.setNextEvent("none");
        eventDelayAPI.setActivationStatus("false");

        triggerEvent(eventDelayAPI.getNowEvent());

        // Only reset timer if in DEFAULT mode
        if (CFG().getString("TimerType", "DEFAULT").equalsIgnoreCase("DEFAULT")) {
            eventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));
        }
    }

    public void triggerEvent(String eventName) {
        eventDelayAPI.setOpeningTimer(CFG().getInt("Events." + eventName + ".Duration"));

        List<String> defaultCommands = CFG().getStringList("Events." + eventName + ".onStart.default");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Actions.execute(plugin, player, defaultCommands);
        }
        if (CFG().contains("Events." + eventName + ".onStart.random")) {
            Set<String> sections = CFG().getConfigurationSection("Events." + eventName + ".onStart.random").getKeys(false);
            if (!sections.isEmpty()) {
                String randomSection = sections.stream()
                        .skip((int) (Math.random() * sections.size()))
                        .findFirst()
                        .orElse(null);

                if (randomSection != null) {
                    List<String> randomCommands = CFG().getStringList("Events." + eventName + ".onStart.random." + randomSection);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(plugin, player, randomCommands);
                    }
                }
            }
        }


        plugin.getTimer().startDuration(eventDelayAPI.getNowEvent(), eventDelayAPI.getOpeningTimer());
    }

}
