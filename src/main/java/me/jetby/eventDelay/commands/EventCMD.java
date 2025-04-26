package me.jetby.eventDelay.commands;

import me.jetby.eventDelay.Main;
import me.jetby.eventDelay.configurations.Config;
import me.jetby.eventDelay.configurations.Messages;
import me.jetby.eventDelay.configurations.WebhookConfig;
import me.jetby.eventDelay.manager.Assistants;
import me.jetby.eventDelay.manager.Timer;
import me.jetby.eventDelay.manager.Triggers;
import me.jetby.eventDelay.tools.EventDelayAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.configurations.Messages.MSG;
import static me.jetby.eventDelay.manager.Assistants.*;
import static me.jetby.eventDelay.tools.Actions.teleportButton;
import static me.jetby.eventDelay.tools.Color.hex;
import static me.jetby.eventDelay.tools.FormatTimer.stringFormat;

public class EventCMD implements CommandExecutor {
    private final EventDelayAPI eventDelayAPI;
    private final Timer timer;
    private final Triggers triggers;

    public EventCMD(EventDelayAPI eventDelayAPI, Timer timer, Triggers triggers){
        this.eventDelayAPI = eventDelayAPI;
        this.timer = timer;
        this.triggers = triggers;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("event")) {

            if (sender instanceof Player p) {

                if (args.length == 0) {
                    for (String msg : MSG().getStringList("messages.usage")) {
                        sender.sendMessage(hex(msg, p));
                    }
                    return true;
                }

                String arg = args[0];
                switch (arg) {
                    case "delay": {
                        if (Bukkit.getOnlinePlayers().size() < CFG().getInt("MinPlayers", 3)) {
                            for (String m : MSG().getStringList("delay.noPlayers")) {
                                m = hex(m, p);
                                sender.sendMessage(m
                                        .replace("{min_players}", String.valueOf(CFG().getInt("MinPlayers", 1)))
                                );
                            }
                            return true;
                        }
                        if (isEventActive()) {
                            for (String m : MSG().getStringList("delay.active")) {
                                m = m
                                        .replace("{time_to_start}", String.valueOf(eventDelayAPI.getTimerUntilNextEvent()))
                                        .replace("{time_to_start_string}", stringFormat(eventDelayAPI.getTimerUntilNextEvent()))
                                        .replace("{prefix}", getNowEventPrefix())
                                ;
                                m = hex(m, p);
                                sender.sendMessage(m);
                            }
                            return true;
                        } else {
                            for (String m : MSG().getStringList("delay.time")) {
                                m = hex(m, p);
                                sender.sendMessage(m
                                        .replace("{time_to_start}", String.valueOf(eventDelayAPI.getTimerUntilNextEvent()))
                                        .replace("{time_to_start_string}", stringFormat(eventDelayAPI.getTimerUntilNextEvent()))
                                );
                            }

                        }
                        break;
                    } case "info": {
                        if (isEventActive()) {

                            List<String> msg = CFG().getStringList("Events." + eventDelayAPI.getNowEvent() + ".activeInfo");

                            for (String m : msg) {
                                m = (m
                                        .replace("{prefix}", getNowEventPrefix())
                                        .replace("{duration}", String.valueOf(eventDelayAPI.getDuration()))
                                        .replace("{duration_string}", stringFormat(eventDelayAPI.getDuration()))
                                        .replace("{active_status}", ActiveStatus())
                                        );
                                m = hex(m, p);
                                p.sendMessage(m);
                            }


                        } else {
                            for (String msg : MSG().getStringList("delay.info")) {
                                msg = hex(msg, p);
                                p.sendMessage(msg);
                            }
                        }
                        break;
                    } case "start": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(hex(MSG().getString("messages.noPerm")));
                            return true;
                        }
                        eventDelayAPI.setActivationStatus("false");
                        triggers.startRandomEvent();
                        eventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));
                        break;
                    } case "teleport": {
                        if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                            break;
                        }

                        teleportButton(p, eventDelayAPI);
                        break;
                    }
                    case "stop": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(hex(MSG().getString("messages.noPerm")));
                            return true;
                        }
                        if (isEventActive()) {
                            eventDelayAPI.setOpeningTimer(0);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + Assistants.getNowEventPrefix() + " остановлен."));
                            triggers.stopEvent(eventDelayAPI.getNowEvent());


                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                        break;
                    } case "timer": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(hex(MSG().getString("messages.noPerm")));
                            return true;
                        }

                        if (args.length < 2) {

                            p.sendMessage(hex("&a[HELP] &f &c/event timer reset", p));
                            p.sendMessage(hex("&a[HELP] &f &c/event timer set <в секундах>", p));

                            return true;
                        }
                        if (args[1].equalsIgnoreCase("set")) {
                            if (args[2].equalsIgnoreCase("reset")) {
                                eventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));
                                p.sendMessage("Вы успешно сбросили время до начала ивента");

                            } else if (args[1].equalsIgnoreCase("set") && args.length == 3) {
                                eventDelayAPI.setTimerUntilNextEvent(Integer.parseInt(args[2]));
                                sender.sendMessage("&a[HELP] &fВы успешно поставили время до начала ивента на &a".replace('&', '§') + Integer.parseInt(args[2]));

                            }
                        }
                        break;
                    } case "setNext": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(hex(MSG().getString("messages.noPerm")));
                            return true;
                        }
                        if (args.length < 2) {
                            sender.sendMessage("Используйте /event setNext <название ивента>");
                            return true;
                        }

                        if (CFG().contains("Events." + args[1])) {
                            eventDelayAPI.setNextEvent(args[1]);
                            sender.sendMessage("Следующий ивент установлен на: " + ChatColor.GREEN + eventDelayAPI.getNextEvent());
                        } else {
                            sender.sendMessage("Ивент с названием " + ChatColor.RED + args[1] + ChatColor.WHITE + " не найден.");
                        }
                        break;
                    } case "reload": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(hex(MSG().getString("messages.noPerm")));
                            return true;
                        }
                        p.sendMessage(hex(MSG().getString("messages.reload"), p));

                        Config config = new Config();
                        config.reloadCfg(Main.INSTANCE);

                        Messages messages = new Messages();
                        messages.reloadCfg(Main.INSTANCE);

                        WebhookConfig webhookConfig = new WebhookConfig();
                        webhookConfig.reloadCfg(Main.INSTANCE);

                        eventDelayAPI.setOpeningTimer(CFG().getInt("Timer", 1800));

                        eventDelayAPI.setTimer(CFG().getInt("Timer", 1800));
                        eventDelayAPI.setFreeze(CFG().getBoolean("Freeze", true));
                        eventDelayAPI.setMinPlayers(CFG().getInt("MinPlayers", 3));

                        eventDelayAPI.setPreviousEvent("none");
                        eventDelayAPI.setNowEvent("none");
                        eventDelayAPI.setNextEvent("none");

                        timer.initialize();
                        timer.startTimer();
                        triggers.nextRandomEvent();

                        break;
                    } case "activate": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(hex(MSG().getString("messages.noPerm")));
                            return true;
                        }
                        if (Objects.requireNonNull(CFG().getConfigurationSection("Events")).getKeys(false).contains(eventDelayAPI.getNowEvent())) {

                            if (isEventActive()) {
                                timer.Activate();
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + getNowEventPrefix() + " активирован."));
                            } else {
                                sender.sendMessage("Нету активных ивентов");
                            }

                        }
                        break;
                    } case "compass": {

                            if (isEventActive()) {
                                if (CFG().getBoolean("Events." + eventDelayAPI.getNowEvent() + ".compass") && CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".compass")!=null) {

                                    World world = Bukkit.getWorld(CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".coordinates.world", "world"));

                                    String xString = hex(CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".coordinates.x", "0"), p);
                                    String yString = hex(CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".coordinates.y", "0"), p);
                                    String zString = hex(CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".coordinates.z", "0"), p);

                                    int x = Integer.parseInt(xString);
                                    int y = Integer.parseInt(yString);
                                    int z = Integer.parseInt(zString);


                                    ItemStack itemInHand = p.getInventory().getItemInMainHand();

                                    if (itemInHand.getType()==Material.COMPASS) {

                                        Location targetLocation = new Location(world, x, y, z);
                                        p.setCompassTarget(targetLocation);
                                        p.sendMessage(hex(MSG().getString("compass.success"), p));

                                    } else {
                                        p.sendMessage(hex(MSG().getString("compass.noItem"), p));
                                    }

                                } else {
                                    p.sendMessage(hex(MSG().getString("compass.disabled"), p));
                                }
                            } else {
                                for (String msg : MSG().getStringList("delay.info")) {
                                    msg = hex(msg, p);
                                    p.sendMessage(msg);
                                }
                            }
                        }

                }
            } else {
                String arg = args[0];
                switch (arg) {
                    case "start": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(MSG().getString("messages.noPerm"));
                            return true;
                        }
                        eventDelayAPI.setActivationStatus("false");
                        triggers.startRandomEvent();
                        eventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));
                        break;
                    }
                    case "stop": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(MSG().getString("messages.noPerm"));
                            return true;
                        }
                        if (isEventActive()) {
                            eventDelayAPI.setOpeningTimer(0);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + Assistants.getNowEventPrefix() + " остановлен."));
                            triggers.stopEvent(eventDelayAPI.getNowEvent());


                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                        break;
                    }
                    case "timer": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(MSG().getString("messages.noPerm"));
                            return true;
                        }

                        if (args.length < 2) {

                            sender.sendMessage("&a[HELP] &f &c/event timer reset");
                            sender.sendMessage("&a[HELP] &f &c/event timer set <в секундах>");

                            return true;
                        }
                        if (args[1].equalsIgnoreCase("set")) {
                            if (args[2].equalsIgnoreCase("reset")) {
                                eventDelayAPI.setTimerUntilNextEvent(CFG().getInt("Timer", 1800));
                                sender.sendMessage("Вы успешно сбросили время до начала ивента");

                            } else if (args[1].equalsIgnoreCase("set") && args.length == 3) {
                                eventDelayAPI.setTimerUntilNextEvent(Integer.parseInt(args[2]));
                                sender.sendMessage("&a[HELP] &fВы успешно поставили время до начала ивента на &a".replace('&', '§') + Integer.parseInt(args[2]));

                            }
                        }
                        break;
                    }
                    case "next": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(MSG().getString("messages.noPerm"));
                            return true;
                        }
                        if (args.length < 2) {
                            sender.sendMessage("Используйте /event next <название ивента>");
                            return true;
                        }

                        if (CFG().contains("Events." + args[1])) {
                            eventDelayAPI.setNextEvent(args[1]);
                            sender.sendMessage("Следующий ивент установлен на: " + ChatColor.GREEN + eventDelayAPI.getNextEvent());
                        } else {
                            sender.sendMessage("Ивент с названием " + ChatColor.RED + args[1] + ChatColor.WHITE + " не найден.");
                        }
                        break;
                    }
                    case "reload": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(MSG().getString("messages.noPerm"));
                            return true;
                        }
                        sender.sendMessage(MSG().getString("messages.reload"));
                        Config config = new Config();
                        config.reloadCfg(Main.INSTANCE);

                        Messages messages = new Messages();
                        messages.reloadCfg(Main.INSTANCE);

                        WebhookConfig webhookConfig = new WebhookConfig();
                        webhookConfig.reloadCfg(Main.INSTANCE);

                        eventDelayAPI.setOpeningTimer(CFG().getInt("Timer", 1800));

                        eventDelayAPI.setTimer(CFG().getInt("Timer", 1800));
                        eventDelayAPI.setFreeze(CFG().getBoolean("Freeze", true));
                        eventDelayAPI.setMinPlayers(CFG().getInt("MinPlayers", 3));

                        eventDelayAPI.setPreviousEvent("none");
                        eventDelayAPI.setNowEvent("none");
                        eventDelayAPI.setNextEvent("none");

                        timer.initialize();
                        timer.startTimer();
                        triggers.nextRandomEvent();

                        break;
                    }
                    case "activate": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(MSG().getString("messages.noPerm"));
                            return true;
                        }
                        if (Objects.requireNonNull(CFG().getConfigurationSection("Events")).getKeys(false).contains(eventDelayAPI.getNowEvent())) {

                            if (isEventActive()) {
                                timer.Activate();
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + getNowEventPrefix() + " активирован."));
                            } else {
                                sender.sendMessage("Нету активных ивентов");
                            }

                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    public String ActiveStatus () {

        String check = eventDelayAPI.getActivationStatus();

        assert check != null;
        if (check.equals("true")) {
            return MSG().getString("OpeningTime.start").replace("{time_to_open}", String.valueOf(eventDelayAPI.getOpeningTimer()));
        }

        if (check.equalsIgnoreCase("opened")) {
            return MSG().getString("OpeningTime.end");
        }

        return MSG().getString("OpeningTime.none");
    }
}