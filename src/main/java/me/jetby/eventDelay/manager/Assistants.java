package me.jetby.eventDelay.manager;


import me.jetby.eventDelay.tools.EventDelayAPI;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.configurations.Messages.MSG;

public class Assistants {

    // Делаем это единожды для оптимизации
    private static final Random random = new Random();

    public static String getRandomEvent() {
        List<String> events = CFG().getConfigurationSection("Events").getKeys(false).stream().toList();
        Map<String, Integer> eventChances = new HashMap<>();
        int totalWeight = 0;

        for (String event : events) {
            int chance = CFG().getInt("Events." + event + ".Chance", 100);
            eventChances.put(event, chance);
            totalWeight += chance;
        }

        int randomValue = random.nextInt(totalWeight) + 1;

        int currentWeight = 0;
        for (Map.Entry<String, Integer> entry : eventChances.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }

        return events.get(random.nextInt(events.size()));
    }


    public static boolean isEventActive(EventDelayAPI eventDelayAPI) {
        return !eventDelayAPI.getNowEvent().equalsIgnoreCase("none");
    }

    public static String getPreviousEventPrefix(EventDelayAPI eventDelayAPI) {
        if (!eventDelayAPI.getPreviousEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + eventDelayAPI.getPreviousEvent() + ".Prefix");
        }
        return "none";
    }

    public static String getNowEventPrefix(EventDelayAPI eventDelayAPI) {
        if (!eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".Prefix");
        }
        return "none";
    }

    public static String getNextEventPrefix(EventDelayAPI eventDelayAPI) {
        if (!eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            return CFG().getString("Events." + eventDelayAPI.getNextEvent() + ".Prefix");
        }
        return "none";
    }


    public static boolean isCompass(EventDelayAPI eventDelayAPI) {
        if (!eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
            return CFG().getBoolean("Events." + eventDelayAPI.getNowEvent() + ".compass", false);
        }
        return false;
    }

    public static String activeStatus(EventDelayAPI eventDelayAPI) {
        String check = eventDelayAPI.getActivationStatus();
        ConfigurationSection openingTime = MSG().getConfigurationSection("OpeningTime");
        if (check.equals("true")) {
            return openingTime.getString("start").replace("{time_to_open}", Integer.toString(eventDelayAPI.getOpeningTimer()));
        }
        if (check.equalsIgnoreCase("opened")) {
            return openingTime.getString("end");
        }
        return openingTime.getString("none");
    }
}
