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
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.configurations.Messages.MSG;
import static me.jetby.eventDelay.manager.Assistants.*;
import static me.jetby.eventDelay.tools.Actions.teleportButton;
import static me.jetby.eventDelay.tools.Color.hex;
import static me.jetby.eventDelay.tools.FormatTimer.stringFormat;

public class EventCMD implements TabExecutor {

    private final Main plugin;
    private final EventDelayAPI eventDelayAPI;
    private final Timer timer;
    private final Triggers triggers;

    public EventCMD(Main plugin) {
        this.plugin = plugin;
        this.eventDelayAPI = plugin.getEventDelayAPI();
        this.timer = plugin.getTimer();
        this.triggers = plugin.getTriggers();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
                                    .replace("{min_players}", Integer.toString(CFG().getInt("MinPlayers", 3)))
                            );
                        }
                        return true;
                    }
                    if (isEventActive(eventDelayAPI)) {
                        for (String m : MSG().getStringList("delay.active")) {
                            m = m
                                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getDelay()))
                                    .replace("{time_to_start_string}", stringFormat(eventDelayAPI.getDelay()))
                                    .replace("{prefix}", getNowEventPrefix(eventDelayAPI))
                            ;
                            m = hex(m, p);
                            sender.sendMessage(m);
                        }
                        return true;
                    } else {
                        for (String m : MSG().getStringList("delay.time")) {
                            m = hex(m, p);
                            sender.sendMessage(m
                                    .replace("{time_to_start}", String.valueOf(eventDelayAPI.getDelay()))
                                    .replace("{time_to_start_string}", stringFormat(eventDelayAPI.getDelay()))
                            );
                        }
                    }
                    break;
                }
                case "info": {
                    if (isEventActive(eventDelayAPI)) {

                        List<String> msg = CFG().getStringList("Events." + eventDelayAPI.getNowEvent() + ".activeInfo");

                        for (String m : msg) {
                            m = (m
                                    .replace("{prefix}", getNowEventPrefix(eventDelayAPI))
                                    .replace("{duration}", String.valueOf(eventDelayAPI.getDuration()))
                                    .replace("{duration_string}", stringFormat(eventDelayAPI.getDuration()))
                                    .replace("{active_status}", activeStatus(eventDelayAPI))
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
                }
                case "start": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(hex(MSG().getString("messages.noPerm")));
                        return true;
                    }

                    if (args.length==1) {
                        eventDelayAPI.setActivationStatus("false");
                        triggers.startRandomEvent();
                        eventDelayAPI.setDelay(CFG().getInt("Timer", 1800));
                        break;
                    } else if (args.length==2) {
                        eventDelayAPI.setActivationStatus("false");
                        triggers.startEvent(args[1]);
                        eventDelayAPI.setDelay(CFG().getInt("Timer", 1800));
                    }

                }
                case "teleport": {
                    if (eventDelayAPI.getNowEvent().equalsIgnoreCase("none")) {
                        break;
                    }

                    teleportButton(plugin, p, eventDelayAPI);
                    break;
                }
                case "stop": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(hex(MSG().getString("messages.noPerm")));
                        return true;
                    }
                    if (isEventActive(eventDelayAPI)) {
                        eventDelayAPI.setOpeningTimer(0);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + Assistants.getNowEventPrefix(eventDelayAPI) + " остановлен."));
                        triggers.stopEvent(eventDelayAPI.getNowEvent());


                    } else {
                        sender.sendMessage("Нету активных ивентов");
                    }
                    break;
                }
                case "timer": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(hex(MSG().getString("messages.noPerm")));
                        return true;
                    }

                    if (args.length < 3) {

                        p.sendMessage(hex("&a[HELP] &fСбросить таймер &c/event timer reset &7<duration/activation/delay>", p));
                        p.sendMessage(hex("&a[HELP] &fПоставить значение &c/event timer set &7<duration/activation/delay> <в секундах>", p));

                        return true;
                    }
                    if (args[1].equalsIgnoreCase("reset")) {
                        if (args[2].equalsIgnoreCase("duration")) {

                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(CFG().getInt("Events."+eventDelayAPI.getNowEvent()+".Duration", 300));
                                sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                            } else {
                                sender.sendMessage(hex("&cНету активного ивента."));
                            }


                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(CFG().getInt("Events."+eventDelayAPI.getNowEvent()+".ActivationTime", 60));
                                sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                            } else {
                                sender.sendMessage(hex("&cНету активного ивента."));
                            }

                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(CFG().getInt("Timer", 1800));
                            sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }
                        p.sendMessage("Вы успешно сбросили время до начала ивента");
                        break;
                    }
                    if (args[1].equalsIgnoreCase("set") && args.length == 4) {
                        if (args[2].equalsIgnoreCase("duration")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("&a[HELP] &fУ таймера &e" + args[2] + " &fуспешно установлено значение &a" + args[3] + "&f."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }
                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("&a[HELP] &fУ таймера &e"+args[2]+" &fуспешно установлено значение &a"+args[3]+"&f."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }

                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(Integer.parseInt(args[3]));
                            sender.sendMessage(hex("&a[HELP] &fУ таймера &e"+args[2]+" &fуспешно установлено значение &a"+args[3]+"&f."));
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }

                    }

                    break;
                }
                case "setNext": {
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
                }
                case "reload": {
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(hex(MSG().getString("messages.noPerm")));
                        return true;
                    }
                    p.sendMessage(hex(MSG().getString("messages.reload"), p));

                    Config config = new Config();
                    config.reloadCfg(plugin);

                    Messages messages = new Messages();
                    messages.reloadCfg(plugin);

                    WebhookConfig webhookConfig = new WebhookConfig();
                    webhookConfig.reloadCfg(plugin);

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
                    if (!p.hasPermission("eventdelay.admin")) {
                        p.sendMessage(hex(MSG().getString("messages.noPerm")));
                        return true;
                    }
                    if (CFG().getConfigurationSection("Events").getKeys(false).contains(eventDelayAPI.getNowEvent())) {
                        if (isEventActive(eventDelayAPI)) {
                            timer.Activate();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + getNowEventPrefix(eventDelayAPI) + " активирован."));
                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                    }
                    break;
                }
                case "compass": {
                    if (isEventActive(eventDelayAPI)) {
                        if (CFG().getBoolean("Events." + eventDelayAPI.getNowEvent() + ".compass") && CFG().getString("Events." + eventDelayAPI.getNowEvent() + ".compass") != null) {

                            ConfigurationSection coordinateSection = CFG().getConfigurationSection("Events." + eventDelayAPI.getNowEvent() + ".coordinates");

                            World world = Bukkit.getWorld(coordinateSection.getString("world", "world"));

                            String xString = hex(coordinateSection.getString("x", "0"), p);
                            String yString = hex(coordinateSection.getString("y", "0"), p);
                            String zString = hex(coordinateSection.getString("z", "0"), p);

                            int x = Integer.parseInt(xString);
                            int y = Integer.parseInt(yString);
                            int z = Integer.parseInt(zString);

                            ItemStack itemInHand = p.getInventory().getItemInMainHand();

                            if (itemInHand.getType() == Material.COMPASS) {

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
                    eventDelayAPI.setDelay(CFG().getInt("Timer", 1800));
                    break;
                }
                case "stop": {
                    if (!sender.hasPermission("eventdelay.admin")) {
                        sender.sendMessage(MSG().getString("messages.noPerm"));
                        return true;
                    }
                    if (isEventActive(eventDelayAPI)) {
                        eventDelayAPI.setOpeningTimer(0);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + Assistants.getNowEventPrefix(eventDelayAPI) + " остановлен."));
                        triggers.stopEvent(eventDelayAPI.getNowEvent());
                    } else {
                        sender.sendMessage("Нету активных ивентов");
                    }
                    break;
                }
                case "timer": {

                    if (args.length < 3) {

                        sender.sendMessage(hex("[HELP] Сбросить таймер /event timer reset <duration/activation/delay>"));
                        sender.sendMessage(hex("[HELP] Поставить значение /event timer set <duration/activation/delay> <в секундах>"));

                        return true;
                    }
                    if (args[1].equalsIgnoreCase("reset")) {
                        if (args[2].equalsIgnoreCase("duration")) {

                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(CFG().getInt("Events."+eventDelayAPI.getNowEvent()+".Duration", 300));
                                sender.sendMessage("Вы успешно сбросили время для таймера &a"+args[2]);
                            } else {
                                sender.sendMessage(hex("Нету активного ивента."));
                            }


                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(CFG().getInt("Events."+eventDelayAPI.getNowEvent()+".ActivationTime", 60));
                                sender.sendMessage("Вы успешно сбросили время для таймера "+args[2]);
                            } else {
                                sender.sendMessage(hex("Нету активного ивента."));
                            }

                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(CFG().getInt("Timer", 1800));
                            sender.sendMessage("Вы успешно сбросили время для таймера "+args[2]);
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
                        }
                        sender.sendMessage("Вы успешно сбросили время до начала ивента");
                        break;
                    }
                    if (args[1].equalsIgnoreCase("set") && args.length == 4) {
                        if (args[2].equalsIgnoreCase("duration")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setDuration(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("[HELP] У таймера " + args[2] + " успешно установлено значение " + args[3] + "."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }
                        } else if (args[2].equalsIgnoreCase("activation")) {
                            if (eventDelayAPI.getNowEvent()!=null) {
                                eventDelayAPI.setOpeningTimer(Integer.parseInt(args[3]));
                                sender.sendMessage(hex("[HELP] У таймера "+args[2]+" успешно установлено значение "+args[3]+"."));
                            } else {
                                sender.sendMessage("Таймер "+args[2]+" не был найден.");
                            }
                        } else if (args[2].equalsIgnoreCase("delay")) {
                            eventDelayAPI.setDelay(Integer.parseInt(args[3]));
                            sender.sendMessage(hex("[HELP] У таймера "+args[2]+" успешно установлено значение "+args[3]+"."));
                        } else {
                            sender.sendMessage("Таймер "+args[2]+" не был найден.");
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
                    config.reloadCfg(plugin);

                    Messages messages = new Messages();
                    messages.reloadCfg(plugin);

                    WebhookConfig webhookConfig = new WebhookConfig();
                    webhookConfig.reloadCfg(plugin);

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
                    if (CFG().getConfigurationSection("Events").getKeys(false).contains(eventDelayAPI.getNowEvent())) {
                        if (isEventActive(eventDelayAPI)) {
                            timer.Activate();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + getNowEventPrefix(eventDelayAPI) + " активирован."));
                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                    }
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return List.of();
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (p.hasPermission("eventdelay.admin")) {
                completions.add("delay");
                completions.add("info");
                if (isCompass(eventDelayAPI)) {
                    completions.add("compass");
                }
                completions.add("activate");
                completions.add("setNext");
                completions.add("timer");
                completions.add("start");
                completions.add("stop");
                completions.add("reload");
            } else {
                if (isCompass(eventDelayAPI)) {
                    completions.add("compass");
                }
                completions.add("delay");
                completions.add("info");
            }

            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args[0].equalsIgnoreCase("timer")) {
            if (args.length==2) {
                completions.add("set");
                completions.add("reset");
                String input = args[1].toLowerCase();
                return completions.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
            if (args.length==3) {
                completions.add("duration");
                completions.add("activation");
                completions.add("delay");

                String input = args[2].toLowerCase();
                return completions.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setNext")
                    || args[0].equalsIgnoreCase("start")) {

                ConfigurationSection eventsSection = CFG().getConfigurationSection("Events");
                if (eventsSection != null) {
                    completions.addAll(eventsSection.getKeys(false));
                }

                String input = args[1].toLowerCase();
                return completions.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}