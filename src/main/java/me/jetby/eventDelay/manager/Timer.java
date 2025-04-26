package me.jetby.eventDelay.manager;

import me.jetby.eventDelay.Main;
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

public class Timer {

    private final EventDelayAPI eventDelayAPI;
    private final Triggers triggers;

    public Timer(EventDelayAPI eventDelayAPI, Triggers triggers){
        this.eventDelayAPI = eventDelayAPI;
        this.triggers = triggers;
    }

    private static Map<String, String> timezoneEvents = new HashMap<>();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static ZoneId zoneId;
    private static String lastTriggeredTime = "";

    public void initialize() {
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

    public void startTimer() {
        String timerType = CFG().getString("TimerType", "DEFAULT");

        if (timerType.equalsIgnoreCase("TIMEZONE")) {
            startTimezoneTimer();
        } else {
            startDefaultTimer();
        }
    }

    private void startDefaultTimer() {
        eventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getTimerUntilNextEvent() > 0) {
                    if (eventDelayAPI.isFreeze()) {
                        if (Bukkit.getOnlinePlayers().size() >= eventDelayAPI.getMinPlayers()) {
                            eventDelayAPI.setTimerUntilNextEvent(eventDelayAPI.getTimerUntilNextEvent()-1);
                        }
                    } else {
                        eventDelayAPI.setTimerUntilNextEvent(eventDelayAPI.getTimerUntilNextEvent()-1);
                    }

                    checkWarnings();
                } else {
                    triggers.triggerNextEvent();
                }
            }
        };
        task.runTaskTimerAsynchronously(INSTANCE, 0, 20);
    }

    private void checkWarnings() {
        if (!eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            List<Integer> warnTimes = CFG().getIntegerList("Events." + eventDelayAPI.getNextEvent() + ".warns.time");
            List<String> warnActions = CFG().getStringList("Events." + eventDelayAPI.getNextEvent() + ".warns.warnActions");

            warnActions.replaceAll(s -> s
                    .replace("{prefix}", getNextEventPrefix())
                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getTimerUntilNextEvent()))
            );

            if (warnTimes.contains(eventDelayAPI.getTimerUntilNextEvent())) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Actions.execute(player, warnActions);
                }
            }
        }
    }
    private void startTimezoneTimer() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now(zoneId);
                String currentTime = now.format(timeFormatter);

                if (!currentTime.equals(lastTriggeredTime) && timezoneEvents.containsKey(currentTime)) {
                    if (Bukkit.getOnlinePlayers().size() >= eventDelayAPI.getMinPlayers()) {
                        String forcedEvent = timezoneEvents.get(currentTime);
                        if (forcedEvent != null && !forcedEvent.isEmpty()) {
                            eventDelayAPI.setNextEvent(forcedEvent);
                        }
                        triggers.triggerNextEvent();
                        lastTriggeredTime = currentTime;
                    }
                }

                if (!currentTime.equals(lastTriggeredTime)) {
                    lastTriggeredTime = "";
                }
            }
        };
        task.runTaskTimerAsynchronously(INSTANCE, 0, 20);
    }

    public void startDuration(String eventName, int duration) {
        eventDelayAPI.setDuration(duration);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getDuration() > 0) {
                    eventDelayAPI.setDuration(eventDelayAPI.getDuration()-1);
                } else if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                    cancel();
                }
                else {
                    List<String> commands = CFG().getStringList("Events." + eventName + ".onEnd");

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(player, commands);
                    }
                    eventDelayAPI.setPreviousEvent(eventDelayAPI.getNowEvent());
                    eventDelayAPI.setNowEvent("none");
                    cancel();
                    }
                }
        };task.runTaskTimer(INSTANCE, 0, 20);  // Таймер на каждую секунду
    }


    public void Activate() {
        eventDelayAPI.setOpeningTimer(CFG().getInt("Events." + eventDelayAPI.getNowEvent() + ".ActivationTime", 120));  // Устанавливаем начальное время для ивента
        eventDelayAPI.setActivationStatus("true");

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getOpeningTimer() > 0) {

                    eventDelayAPI.setOpeningTimer(eventDelayAPI.getOpeningTimer()-1);
                } else {

                    List<String> onActivated = CFG().getStringList("Events."+eventDelayAPI.getNowEvent()+".onActivated");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(player, onActivated);
                    }

                    eventDelayAPI.setActivationStatus("opened");
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(INSTANCE, 0, 20);  // Таймер на каждую секунду


    }


}
