package me.jetby.eventDelay.manager;

import me.jetby.eventDelay.tools.Actions;
import me.jetby.eventDelay.tools.EventDelayAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.manager.Assistants.getRandomEvent;
import static me.jetby.eventDelay.manager.Timer.startDuration;

public class Triggers {
    public static void nextRandomEvent() {
        if (EventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            EventDelayAPI.setNextEvent(getRandomEvent());
        }
    }
    public static void startRandomEvent() {
        if (EventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            EventDelayAPI.setNowEvent(EventDelayAPI.getNextEvent());
        } else {
            stopEvent(EventDelayAPI.getNowEvent());
            EventDelayAPI.setNowEvent(EventDelayAPI.getNextEvent());
        }

        EventDelayAPI.setNextEvent(getRandomEvent());
        triggerEvent(EventDelayAPI.getNowEvent());
    }



    public static void stopEvent(String eventName) {
        if (EventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return;
        }

        EventDelayAPI.setTimeToEnd(CFG().getInt("Events." + eventName + ".Duration"));
        List<String> commands = CFG().getStringList("Events." + eventName + ".onEnd");

        for (Player player : Bukkit.getOnlinePlayers()) {
            Actions.execute(player, commands);
        }
        EventDelayAPI.setPreviousEvent(EventDelayAPI.getNowEvent());
        EventDelayAPI.setNowEvent("none");
        EventDelayAPI.setNextEvent(getRandomEvent());

    }

    public static void triggerNextEvent() {
        if (EventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            EventDelayAPI.setNextEvent(getRandomEvent());
        }

        EventDelayAPI.setNowEvent(EventDelayAPI.getNextEvent());
        EventDelayAPI.setNextEvent("none");
        EventDelayAPI.setActivationStatus("false");

        triggerEvent(EventDelayAPI.getNowEvent());

        // Only reset timer if in DEFAULT mode
        if (CFG().getString("TimerType", "DEFAULT").equalsIgnoreCase("DEFAULT")) {
            EventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));
        }
    }
    public static void triggerEvent(String eventName) {
        EventDelayAPI.setTimeToEnd(CFG().getInt("Events." + eventName + ".Duration"));

        List<String> defaultCommands = CFG().getStringList("Events." + eventName + ".onStart.default");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Actions.execute(player, defaultCommands);
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
                        Actions.execute(player, randomCommands);
                    }
                }
            }
        }

        startDuration(EventDelayAPI.getNowEvent(), EventDelayAPI.getTimeToEnd());
    }

}
