package me.jetby.eventDelay.Commands;

import me.jetby.eventDelay.Configs.Config;
import me.jetby.eventDelay.Configs.DB;
import me.jetby.eventDelay.Configs.Messages;
import me.jetby.eventDelay.Manager.Assistants;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static me.jetby.eventDelay.Main.*;
import static me.jetby.eventDelay.Manager.Assistants.*;
import static me.jetby.eventDelay.Manager.Timer.Activate;
import static me.jetby.eventDelay.Manager.Triggers.startRandomEvent;
import static me.jetby.eventDelay.Manager.Triggers.stopEvent;
import static me.jetby.eventDelay.Utils.Color.replace;
import static me.jetby.eventDelay.Utils.FormatTimer.stringFormat;

public class EventCMD implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("event")) {

            if (sender instanceof Player) {

                Player p = (Player) sender;

                if (args.length == 0) {
                    for (String msg : Messages.get().usage()) {
                        sender.sendMessage(replace(msg, p));
                    }
                    return true;
                }

                String arg = args[0];
                switch (arg) {
                    case "delay": {
                        if (Bukkit.getOnlinePlayers().size() < Config.get().getMinPlayers()) {
                            for (String m : Messages.get().noPlayers()) {
                                m = replace(m, p);
                                sender.sendMessage(m
                                        .replace("{min_players}", String.valueOf(cfg.getInt("MinPlayers", 1)))
                                );
                            }
                            return true;
                        }
                        if (isEventActive()) {
                            for (String m : Messages.get().active()) {
                                m = m
                                        .replace("{time_to_start}", String.valueOf(getTime()))
                                        .replace("{time_to_start_string}", stringFormat(getTime()))
                                        .replace("{prefix}",getActiveEventPrefix())
                                ;
                                m = replace(m, p);
                                sender.sendMessage(m);
                            }
                            return true;
                        } else {
                            for (String m : Messages.get().time()) {
                                m = replace(m, p);
                                sender.sendMessage(m
                                        .replace("{time_to_start}", String.valueOf(getTime()))
                                        .replace("{time_to_start_string}", stringFormat(getTime()))
                                );
                            }

                        }
                        break;
                    } case "info": {
                        if (isEventActive()) {

                            List<String> msg = cfg.getStringList("Events." + getNowEvent() + ".activeInfo");

                            for (String m : msg) {
                                m = (m
                                        .replace("{prefix}", getActiveEventPrefix())
                                        .replace("{duration}", String.valueOf(getTimeToEnd()))
                                        .replace("{duration_string}", stringFormat(getTimeToEnd()))
                                        .replace("{active_status}", ActiveStatus())
                                        );
                                m = replace(m, p);
                                p.sendMessage(m);
                            }


                        } else {
                            for (String msg : Messages.get().info()) {
                                msg = replace(msg, p);
                                p.sendMessage(msg);
                            }
                        }
                        break;
                    } case "start": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(replace(Messages.get().NoPerm(), p));
                            return true;
                        }
                        db.set("OpeningTime", "false");
                        startRandomEvent();
                        TimerUntilNextEvent = Config.get().getTimer();
                        break;
                    } case "stop": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(replace(Messages.get().NoPerm(), p));
                            return true;
                        }
                        if (isEventActive()) {
                            TimeUntilDuration = 0;
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + Assistants.getActiveEventPrefix() + " остановлен."));
                            stopEvent(Assistants.getActiveEventPrefix());


                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                        break;
                    } case "timer": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(replace(Messages.get().NoPerm(), p));
                            return true;
                        }

                        if (args.length < 2) {

                            p.sendMessage(replace("&a[HELP] &f &c/event timer reset", p));
                            p.sendMessage(replace("&a[HELP] &f &c/event timer set <в секундах>", p));

                            return true;
                        }
                        if (args[1].equalsIgnoreCase("set")) {
                            if (args[2].equalsIgnoreCase("reset")) {
                                TimerUntilNextEvent = Config.get().getTimer();
                                p.sendMessage("Вы успешно сбросили время до начала ивента");

                            } else if (args[1].equalsIgnoreCase("set") && args.length == 3) {
                                TimerUntilNextEvent = Integer.parseInt(args[2]);
                                sender.sendMessage("&a[HELP] &fВы успешно поставили время до начала ивента на &a".replace('&', '§') + Integer.parseInt(args[2]));

                            }
                        }
                        break;
                    } case "next": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(replace(Messages.get().NoPerm(), p));
                            return true;
                        }
                        if (args.length < 2) {
                            sender.sendMessage("Используйте /event next <название ивента>");
                            return true;
                        }

                        if (cfg.contains("Events." + args[1])) {
                            setNextEvent(args[1]);
                            sender.sendMessage("Следующий ивент установлен на: " + ChatColor.GREEN + getNextEvent());
                        } else {
                            sender.sendMessage("Ивент с названием " + ChatColor.RED + args[1] + ChatColor.WHITE + " не найден.");
                        }
                        break;
                    } case "reload": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(replace(Messages.get().NoPerm(), p));
                            return true;
                        }
                        p.sendMessage(replace(Messages.get().Reload(), p));
                        getINSTANCE().cfgReload();
                        getINSTANCE().messagesReload();
                        TimeUntilDuration = Config.get().getTimer();

                        break;
                    } case "activate": {
                        if (!p.hasPermission("eventdelay.admin")) {
                            p.sendMessage(replace(Messages.get().NoPerm(), p));
                            return true;
                        }
                        if (Objects.requireNonNull(cfg.getConfigurationSection("Events")).getKeys(false).contains(getNowEvent())) {

                            if (isEventActive()) {
                                Activate();
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + getActiveEventPrefix() + " активирован."));
                            } else {
                                sender.sendMessage("Нету активных ивентов");
                            }

                        }
                        break;
                    } case "compass": {

                            if (isEventActive()) {
                                if (cfg.getBoolean("Events." + getNowEvent() + ".compass") && cfg.getString("Events." + getNowEvent() + ".compass")!=null) {

                                    World world = Bukkit.getWorld(cfg.getString("Events." + getNowEvent() + ".coordinates.world", "world"));
                                    // Замена плейсхолдеров до парсинга
                                    String xString = replace(cfg.getString("Events." + nowEvent + ".coordinates.x", "0"), p);
                                    String yString = replace(cfg.getString("Events." + nowEvent + ".coordinates.y", "0"), p);
                                    String zString = replace(cfg.getString("Events." + nowEvent + ".coordinates.z", "0"), p);

                                    // Парсинг в целые числа после замены плейсхолдеров
                                    int x = Integer.parseInt(xString);
                                    int y = Integer.parseInt(yString);
                                    int z = Integer.parseInt(zString);


                                    ItemStack itemInHand = p.getInventory().getItemInMainHand();

                                    if (itemInHand.getType()==Material.COMPASS) {

                                        Location targetLocation = new Location(world, x, y, z);
                                        p.setCompassTarget(targetLocation);
                                        p.sendMessage(replace(Messages.get().Success(), p));

                                    } else {
                                        p.sendMessage(replace(Messages.get().NoItem(), p));
                                    }

                                } else {
                                    p.sendMessage(replace(Messages.get().Disabled(), p));
                                }
                            } else {
                                for (String msg : Messages.get().info()) {
                                    msg = replace(msg, p);
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
                            sender.sendMessage(Messages.get().NoPerm());
                            return true;
                        }
                        db.set("OpeningTime", "false");
                        startRandomEvent();
                        TimerUntilNextEvent = Config.get().getTimer();
                        break;
                    }
                    case "stop": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(Messages.get().NoPerm());
                            return true;
                        }
                        if (isEventActive()) {
                            TimeUntilDuration = 0;
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + Assistants.getActiveEventPrefix() + " остановлен."));
                            stopEvent(Assistants.getActiveEventPrefix());


                        } else {
                            sender.sendMessage("Нету активных ивентов");
                        }
                        break;
                    }
                    case "timer": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(Messages.get().NoPerm());
                            return true;
                        }

                        if (args.length < 2) {

                            sender.sendMessage("&a[HELP] &f &c/event timer reset");
                            sender.sendMessage("&a[HELP] &f &c/event timer set <в секундах>");

                            return true;
                        }
                        if (args[1].equalsIgnoreCase("set")) {
                            if (args[2].equalsIgnoreCase("reset")) {
                                TimerUntilNextEvent = Config.get().getTimer();
                                sender.sendMessage("Вы успешно сбросили время до начала ивента");

                            } else if (args[1].equalsIgnoreCase("set") && args.length == 3) {
                                TimerUntilNextEvent = Integer.parseInt(args[2]);
                                sender.sendMessage("&a[HELP] &fВы успешно поставили время до начала ивента на &a".replace('&', '§') + Integer.parseInt(args[2]));

                            }
                        }
                        break;
                    }
                    case "next": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(Messages.get().NoPerm());
                            return true;
                        }
                        if (args.length < 2) {
                            sender.sendMessage("Используйте /event next <название ивента>");
                            return true;
                        }

                        if (cfg.contains("Events." + args[1])) {
                            setNextEvent(args[1]);
                            sender.sendMessage("Следующий ивент установлен на: " + ChatColor.GREEN + getNextEvent());
                        } else {
                            sender.sendMessage("Ивент с названием " + ChatColor.RED + args[1] + ChatColor.WHITE + " не найден.");
                        }
                        break;
                    }
                    case "reload": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(Messages.get().NoPerm());
                            return true;
                        }
                        sender.sendMessage(Messages.get().Reload());
                        getINSTANCE().cfgReload();
                        getINSTANCE().messagesReload();
                        TimeUntilDuration = Config.get().getTimer();

                        break;
                    }
                    case "activate": {
                        if (!sender.hasPermission("eventdelay.admin")) {
                            sender.sendMessage(Messages.get().NoPerm());
                            return true;
                        }
                        if (Objects.requireNonNull(cfg.getConfigurationSection("Events")).getKeys(false).contains(getNowEvent())) {

                            if (isEventActive()) {
                                Activate();
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Ивент " + getActiveEventPrefix() + " активирован."));
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

        private String ActiveStatus () {

            DB.get().load();
            String check = DB.get().getOpeningTime();

            assert check != null;
            if (check.equals("true")) {
                return Messages.get().Start().replace("{time_to_open}", String.valueOf(getTimeToOpen()));
            }

            if (check.equalsIgnoreCase("opened")) {
                return Messages.get().End();
            }

            return Messages.get().None();
        }
}