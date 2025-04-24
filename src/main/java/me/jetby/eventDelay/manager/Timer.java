package me.jetby.eventDelay.manager;

import me.jetby.eventDelay.tools.Actions;
import me.jetby.eventDelay.tools.EventDelayAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.Main.*;
import static me.jetby.eventDelay.manager.Assistants.*;
import static me.jetby.eventDelay.manager.Triggers.triggerEvent;
import static me.jetby.eventDelay.manager.Triggers.triggerNextEvent;

public class Timer {

    private static Map<String, String> timezoneEvents = new HashMap<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static ZoneId zoneId;
    private static String lastTriggeredTime = "";
    public static void initialize() {
        String timezone = CFG().getString("TimeZone", "GMT+3").replace("UTC", "GMT");
        zoneId = ZoneId.of(timezone);

        if (CFG().getString("TimerType", "DEFAULT").equalsIgnoreCase("TIMEZONE")) {
            for (String timeEntry : CFG().getStringList("TimeZones")) {
                String[] parts = timeEntry.split(";");
                String time = parts[0].trim();
                String event = parts.length > 1 ? parts[1].trim() : null;
                timezoneEvents.put(time, event);
            }
        }
    }

    public static void startTimer() {
        String timerType = CFG().getString("TimerType", "DEFAULT");

        if (timerType.equalsIgnoreCase("TIMEZONE")) {
            startTimezoneTimer();
        } else {
            startDefaultTimer();
        }
    }

    private static void startDefaultTimer() {
        EventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (EventDelayAPI.getTimerUntilNextEvent() > 0) {
                    if (EventDelayAPI.isFreeze()) {
                        if (Bukkit.getOnlinePlayers().size() >= EventDelayAPI.getMinPlayers()) {
                            EventDelayAPI.setTimerUntilNextEvent(EventDelayAPI.getTimerUntilNextEvent()-1);
                        }
                    } else {
                        EventDelayAPI.setTimerUntilNextEvent(EventDelayAPI.getTimerUntilNextEvent()-1);
                    }

                    checkWarnings();
                } else {
                    triggerNextEvent();
                }
            }
        };
        task.runTaskTimerAsynchronously(getInstance(), 0, 20);
    }

    private static void checkWarnings() {
        if (!EventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            List<Integer> warnTimes = CFG().getIntegerList("Events." + EventDelayAPI.getNextEvent() + ".warns.time");
            List<String> warnActions = CFG().getStringList("Events." + EventDelayAPI.getNextEvent() + ".warns.warnActions");

            warnActions.replaceAll(s -> s
                    .replace("{prefix}", getNextEventPrefix())
                    .replace("{time_to_start}", String.valueOf(EventDelayAPI.getTimerUntilNextEvent()))
            );

            if (warnTimes.contains(EventDelayAPI.getTimerUntilNextEvent())) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Actions.execute(player, warnActions);
                }
            }
        }
    }
    private static void startTimezoneTimer() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now(zoneId);
                String currentTime = now.format(timeFormatter);

                if (!currentTime.equals(lastTriggeredTime) && timezoneEvents.containsKey(currentTime)) {
                    if (Bukkit.getOnlinePlayers().size() >= EventDelayAPI.getMinPlayers()) {
                        String forcedEvent = timezoneEvents.get(currentTime);
                        if (forcedEvent != null && !forcedEvent.isEmpty()) {
                            EventDelayAPI.setNextEvent(forcedEvent);
                        }
                        triggerNextEvent();
                        lastTriggeredTime = currentTime;
                    }
                }

                if (!currentTime.equals(lastTriggeredTime)) {
                    lastTriggeredTime = "";
                }
            }
        };
        task.runTaskTimerAsynchronously(getInstance(), 0, 20);
    }

    public static void startDuration(String eventName, int duration) {
        EventDelayAPI.setDuration(duration);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                if (EventDelayAPI.getDuration() > 0) {

                    EventDelayAPI.setDuration(EventDelayAPI.getDuration()-1);
                } else if (EventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                    cancel();
                }
                else {
                    List<String> commands = CFG().getStringList("Events." + eventName + ".onEnd");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(player, commands);
                    }

                    EventDelayAPI.setPreviousEvent(EventDelayAPI.getNowEvent());



                    EventDelayAPI.setNowEvent("none");
                    cancel();
                    }

                }
        };task.runTaskTimer(getInstance(), 0, 20);  // Таймер на каждую секунду
    }


    public static void Activate() {
        EventDelayAPI.setTimeToOpen(CFG().getInt("Events." + EventDelayAPI.getNowEvent() + ".ActivationTime", 120));  // Устанавливаем начальное время для ивента
        EventDelayAPI.setActivationStatus("true");

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (EventDelayAPI.getTimeToEnd() > 0) {

                    EventDelayAPI.setTimeToOpen(EventDelayAPI.getTimeToOpen()-1);
                } else {

                    List<String> onActivated = CFG().getStringList("Events."+EventDelayAPI.getNowEvent()+".onActivated");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(player, onActivated);
                    }

                    EventDelayAPI.setActivationStatus("opened");
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(getInstance(), 0, 20);  // Таймер на каждую секунду


    }


}
