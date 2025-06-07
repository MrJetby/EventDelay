package me.jetby.eventDelay.manager;

import me.jetby.eventDelay.Main;
import me.jetby.eventDelay.tools.Actions;
import me.jetby.eventDelay.tools.EventDelayAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    private static long nextEventTime = 0;

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
        if (startingTimer != null) {
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
                    eventDelayAPI.setDelay(timer);
                }
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
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
        calculateNextEventTime();
        int secondsUntilNextEvent = (int) (nextEventTime - System.currentTimeMillis() / 1000);
        eventDelayAPI.setDelay(secondsUntilNextEvent);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis() / 1000;
                long secondsUntil = nextEventTime - currentTime;

                if (secondsUntil == 1) {
                    if (Bukkit.getOnlinePlayers().size() >= eventDelayAPI.getMinPlayers()) {
                        String currentTimeStr = LocalTime.now(zoneId).format(timeFormatter);
                        String forcedEvent = timezoneEvents.get(currentTimeStr);
                        if (forcedEvent != null && !forcedEvent.isEmpty()) {
                            eventDelayAPI.setNextEvent(forcedEvent);
                        }
                        triggers.triggerNextEvent();
                        calculateNextEventTime();
                    }
                }

                eventDelayAPI.setDelay((int) secondsUntil);
                checkWarnings();
            }
        };
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
        startingTimer = task;
    }

    private void calculateNextEventTime() {
        long now = System.currentTimeMillis() / 1000;
        nextEventTime = Long.MAX_VALUE;

        for (String timeStr : timezoneEvents.keySet()) {
            long eventTime = parseTimeString(timeStr);
            if (eventTime > now && eventTime < nextEventTime) {
                nextEventTime = eventTime;
            }
        }

        if (nextEventTime == Long.MAX_VALUE && !timezoneEvents.isEmpty()) {
            String firstTime = timezoneEvents.keySet().iterator().next();
            nextEventTime = parseTimeString(firstTime) + 86400;
        }
    }

    private long parseTimeString(String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length < 2) {
            return Long.MAX_VALUE;
        }
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next = now.withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .truncatedTo(ChronoUnit.SECONDS);

        if (next.isBefore(now)) {
            next = next.plusDays(1);
        }

        return next.toEpochSecond();
    }

    public void startDuration(String eventName, int duration) {
        if (durationTimer != null) {
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
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
        durationTimer = task;
    }

    public void Activate() {
        if (activationTimer != null) {
            activationTimer.cancel();
        }
        eventDelayAPI.setOpeningTimer(CFG().getInt("Events." + eventDelayAPI.getNowEvent() + ".ActivationTime", 120));
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
        task.runTaskTimerAsynchronously(plugin, 0, 20L);
        activationTimer = task;
    }
}