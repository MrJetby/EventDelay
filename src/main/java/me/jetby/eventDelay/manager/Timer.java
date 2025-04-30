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
import static me.jetby.eventDelay.manager.Assistants.getNextEventPrefix;

public class Timer {

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;
    private final Triggers triggers;

    private BukkitRunnable startingTimer;
    private BukkitRunnable activationTimer;
    private BukkitRunnable durationTimer;

    public Timer(Main plugin) {
        this.plugin = plugin;
        this.eventDelayAPI = plugin.getEventDelayAPI();
        this.triggers = plugin.getTriggers();
    }

    private static final Map<String, String> timezoneEvents = new HashMap<>();
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
        int timer = CFG().getInt("Timer", 1800);
        if (timerType.equalsIgnoreCase("TIMEZONE")) {
            startTimezoneTimer();
        } else {
            startDefaultTimer(timer);
        }
    }

    private void startDefaultTimer(int timer) {
        if (startingTimer!=null) {
            startingTimer.cancel();
        }
        eventDelayAPI.setDelay(timer);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getDelay() > 0) {
                    if (eventDelayAPI.isFreeze()) {
                        if (Bukkit.getOnlinePlayers().size() >= eventDelayAPI.getMinPlayers()) {
                            eventDelayAPI.setDelay(eventDelayAPI.getDelay() - 1);
                        }
                    } else {
                        eventDelayAPI.setDelay(eventDelayAPI.getDelay() - 1);
                    }
                    checkWarnings();
                } else {
                    triggers.triggerNextEvent();
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20);
        startingTimer = task;
    }

    private void checkWarnings() {
        if (!eventDelayAPI.getNextEvent().equalsIgnoreCase("none")) {
            List<Integer> warnTimes = CFG().getIntegerList("Events." + eventDelayAPI.getNextEvent() + ".warns.time");
            List<String> warnActions = CFG().getStringList("Events." + eventDelayAPI.getNextEvent() + ".warns.warnActions");
            warnActions.replaceAll(s -> s
                    .replace("{prefix}", getNextEventPrefix(eventDelayAPI))
                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getDelay()))
            );
            if (warnTimes.contains(eventDelayAPI.getDelay())) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Actions.execute(plugin, player, warnActions);
                }
            }
        }
    }
    private void startTimezoneTimer() {
        int secondsUntilNextEvent = getSecondsUntilNextTimezoneEvent();
        eventDelayAPI.setDelay(secondsUntilNextEvent);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getDelay() > 0) {
                    eventDelayAPI.setDelay(eventDelayAPI.getDelay() - 1);
                    checkWarnings();
                } else {
                    triggers.triggerNextEvent();
                    cancel();
                }

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
        task.runTaskTimerAsynchronously(plugin, 0, 20);
    }


    private int getSecondsUntilNextTimezoneEvent() {
        LocalTime now = LocalTime.now(zoneId);
        int minSeconds = Integer.MAX_VALUE;

        for (String timeString : timezoneEvents.keySet()) {
            LocalTime eventTime = LocalTime.parse(timeString, timeFormatter);
            int seconds = (int) java.time.Duration.between(now, eventTime).getSeconds();

            if (seconds < 0) {
                seconds += 24 * 60 * 60; // Если уже прошло, то переносим на следующий день
            }

            if (seconds < minSeconds) {
                minSeconds = seconds;
            }
        }

        return minSeconds == Integer.MAX_VALUE ? 0 : minSeconds;
    }

    public void startDuration(String eventName, int duration) {
        if (durationTimer!=null) {
            durationTimer.cancel();
        }
        eventDelayAPI.setDuration(duration);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getDuration() > 0) {
                    eventDelayAPI.setDuration(eventDelayAPI.getDuration() - 1);
                } else if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                    cancel();
                } else {
                    List<String> commands = CFG().getStringList("Events." + eventName + ".onEnd");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(plugin, player, commands);
                    }
                    eventDelayAPI.setPreviousEvent(eventDelayAPI.getNowEvent());
                    eventDelayAPI.setNowEvent("none");
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20);
        durationTimer = task;
    }

    public void Activate() {
        if (activationTimer!=null) {
            activationTimer.cancel();
        }
        eventDelayAPI.setOpeningTimer(CFG().getInt("Events." + eventDelayAPI.getNowEvent() + ".ActivationTime", 120));  // Устанавливаем начальное время для ивента
        eventDelayAPI.setActivationStatus("true");
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (eventDelayAPI.getOpeningTimer() > 0) {
                    eventDelayAPI.setOpeningTimer(eventDelayAPI.getOpeningTimer() - 1);
                } else {
                    List<String> onActivated = CFG().getStringList("Events." + eventDelayAPI.getNowEvent() + ".onActivated");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Actions.execute(plugin, player, onActivated);
                    }
                    eventDelayAPI.setActivationStatus("opened");
                    cancel();
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20);
        activationTimer = task;
    }
}
