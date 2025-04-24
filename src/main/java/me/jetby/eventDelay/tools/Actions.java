package me.jetby.eventDelay.tools;

import me.jetby.eventDelay.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static me.jetby.eventDelay.commands.EventCMD.ActiveStatus;
import static me.jetby.eventDelay.configurations.Messages.MSG;
import static me.jetby.eventDelay.configurations.WebhookConfig.WH;
import static me.jetby.eventDelay.manager.Assistants.getNowEventPrefix;
import static me.jetby.eventDelay.tools.Color.hex;
import static me.jetby.eventDelay.tools.Color.setPlaceholders;
import static me.jetby.eventDelay.tools.FormatTimer.stringFormat;
import static me.jetby.eventDelay.tools.Webhook.sendToDiscord;


public class Actions {


    private static final HashMap<UUID, Long> teleportCooldowns = new HashMap<>();


    public static void execute(Player sender, List<String> commands) {
        executeWithDelay(sender, commands, 0);
    }

    private static void executeWithDelay(Player player, List<String> commands, int index) {
        if (index >= commands.size()) return;

        String command = commands.get(index);
        String[] args = command.split(" ");
        String withoutCMD = command.replace(args[0] + " ", "");

        if (args[0].equalsIgnoreCase("[DELAY]")) {
            int delayTicks = Integer.parseInt(args[1]);

            Bukkit.getScheduler().runTaskLater(
                    Main.getInstance(),
                    () -> executeWithDelay(player, commands, index + 1),
                    delayTicks
            );
            return;
        }

        if (args[0].startsWith("[TELEPORT_BUTTON=")) {
            int radius;
            try {
                String radiusStr = args[0].substring(args[0].indexOf("=") + 1, args[0].indexOf("]"));
                radius = Integer.parseInt(radiusStr);
            } catch (Exception e) {
                radius = 10;
            }

            int cooldownSeconds = 0;
            if (command.contains("--cooldown:")) {
                try {
                    cooldownSeconds = Integer.parseInt(command.split("--cooldown:")[1].split(" ")[0]);
                } catch (Exception ignored) {}
            }
            String[] buttonParams = withoutCMD.split(";", 2);
            if (buttonParams.length < 2) {
                player.sendMessage(hex("&cОшибка формата: используйте [TELEPORT_BUTTON=радиус] текст;подсказка"));
                return;
            }

            TextComponent msg = new TextComponent(hex(buttonParams[0].replace("--cooldown:"+cooldownSeconds, "").trim()));
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event 127hajSkjnfa,asd12sa "+radius+" "+cooldownSeconds));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hex(buttonParams[1].replace("--cooldown:"+cooldownSeconds, "").trim()))));

            player.spigot().sendMessage(ChatMessageType.CHAT, msg);
            executeWithDelay(player, commands, index + 1);
            return;
        }

        if (args[0].startsWith("[TELEPORT_NEAR=")) {
            int radius = 10;
            try {
                radius = Integer.parseInt(args[0].substring(args[0].indexOf("=") + 1, args[0].indexOf("]")));
            } catch (Exception ignored) {}

            String[] params = withoutCMD.split(";");
            if (params.length < 4) return;

            World world = Bukkit.getWorld(params[0].trim());
            if (world == null) return;
            try {
                Location center = new Location(
                        world,
                        Double.parseDouble(params[1].trim()),
                        Double.parseDouble(params[2].trim()),
                        Double.parseDouble(params[3].trim())
                );
                teleportRandom(player, center, radius, 0);
            } catch (NumberFormatException ignored) {}
            executeWithDelay(player, commands, index + 1);
            return;
        }

        switch (args[0].toUpperCase()) {


            case "[MESSAGE]", "[MSG]", "[MESSAGE_ALL]": {
                player.sendMessage(hex(withoutCMD));
                break;
            }
            case "[TELEPORT]", "[TP]": {
                String[] parts = withoutCMD.split(" ");
                if (parts.length == 4) {
                    try {
                        String worldName = parts[0];
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);

                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            Bukkit.getLogger().warning("Мир " + worldName + " не найден");
                            break;
                        }

                        Location location = new Location(world, x, y, z);
                        player.teleport(location);

                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("Ошибка парсинга координат");
                        break;
                    }
                }if (parts.length >= 6) {
                    try {
                        String worldName = parts[0];
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        float yaw = Float.parseFloat(parts[4]);
                        float pitch = Float.parseFloat(parts[5]);

                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            Bukkit.getLogger().warning("Мир " + worldName + " не найден");
                            break;
                        }

                        Location location = new Location(world, x, y, z, yaw, pitch);
                        player.teleport(location);

                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("Ошибка парсинга координат");
                        break;
                    }
                } else {
                    Bukkit.getLogger().warning("Некорректные данные для телепорта" );
                    break;
                }
                break;
            }
            case "[PLAYER]": {
                Bukkit.dispatchCommand(player, hex(withoutCMD.replace("%player%", player.getName())));
                break;
            }
            case "[CONSOLE]": {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hex(withoutCMD.replace("%player%", player.getName())));
                break;
            }
            case "[SEND_WEBHOOK]": {

                List<String> lore = new ArrayList<>();
                for (String l : WH().getStringList("webhooks."+withoutCMD+".lore")) {
                    l = l.replace("{prefix}", getNowEventPrefix())
                            .replace("{duration}", String.valueOf(EventDelayAPI.getDuration()))
                            .replace("{duration_string}", stringFormat(EventDelayAPI.getDuration()))
                            .replace("{active_status}", ActiveStatus());
                    l = setPlaceholders(l, null);
                    lore.add(l);
                }

                if (WH().contains("webhooks."+withoutCMD)) {
                    sendToDiscord(WH().getString("webhooks."+withoutCMD+".Url"),
                            WH().getString("webhooks."+withoutCMD+".Username"),
                            WH().getString("webhooks."+withoutCMD+".Avatar"),
                            WH().getString("webhooks."+withoutCMD+".color"),
                            WH().getString("webhooks."+withoutCMD+".title")
                                    .replace("{prefix}", getNowEventPrefix())
                                    .replace("{duration}", String.valueOf(EventDelayAPI.getDuration()))
                                    .replace("{duration_string}", stringFormat(EventDelayAPI.getDuration()))
                                    .replace("{active_status}", ActiveStatus()),
                            lore
                    );
                }
                System.out.println("sex");
                break;
            }

            case "[ACTIONBAR]": {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(hex(withoutCMD
                        .replace("%player%", player.getName()))));
                break;
            }
            case "[SOUND]": {
                float volume = 1.0f;
                float pitch = 1.0f;
                for (String arg : args) {
                    if (arg.startsWith("-volume:")) {
                        volume = Float.parseFloat(arg.replace("-volume:", ""));
                        continue;
                    }
                    if (!arg.startsWith("-pitch:")) continue;
                    pitch = Float.parseFloat(arg.replace("-pitch:", ""));
                }
                player.playSound(player.getLocation(), Sound.valueOf((String) args[1]), volume, pitch);
                break;
            }
            case "[EFFECT]": {
                int strength = 0;
                int duration = 1;
                for (String arg : args) {
                    if (arg.startsWith("-strength:")) {
                        strength = Integer.parseInt(arg.replace("-strength:", ""));
                        continue;
                    }
                    if (!arg.startsWith("-duration:")) continue;
                    duration = Integer.parseInt(arg.replace("-duration:", ""));
                }
                PotionEffectType effectType = PotionEffectType.getByName((String) args[1]);
                if (effectType == null) {
                    return;
                }
                if (player.hasPotionEffect(effectType)) {
                    return;
                }
                player.addPotionEffect(new PotionEffect(effectType, duration * 20, strength));
                break;
            }
            case "[TITLE]": {
                String title = "";
                String subTitle = "";
                int fadeIn = 1;
                int stay = 3;
                int fadeOut = 1;
                for (String arg : args) {
                    if (arg.startsWith("-fadeIn:")) {
                        fadeIn = Integer.parseInt(arg.replace("-fadeIn:", ""));
                        withoutCMD = withoutCMD.replace(arg, "");
                        continue;
                    }
                    if (arg.startsWith("-stay:")) {
                        stay = Integer.parseInt(arg.replace("-stay:", ""));
                        withoutCMD = withoutCMD.replace(arg, "");
                        continue;
                    }
                    if (!arg.startsWith("-fadeOut:")) continue;
                    fadeOut = Integer.parseInt(arg.replace("-fadeOut:", ""));
                    withoutCMD = withoutCMD.replace(arg, "");
                }
                String[] message = hex(withoutCMD).split(";");
                if (message.length >= 1) {
                    title = message[0];
                    if (message.length >= 2) {
                        subTitle = message[1];
                    }
                }
                player.sendTitle(title, subTitle, fadeIn * 20, stay * 20, fadeOut * 20);
            }
        }
        executeWithDelay(player, commands, index + 1);
    }

    public static void teleportRandom(Player player, Location center, int radius, int cooldownSeconds) {
        if (cooldownSeconds > 0) {
            Long lastTeleport = teleportCooldowns.get(player.getUniqueId());
            if (lastTeleport != null && (System.currentTimeMillis() - lastTeleport) < cooldownSeconds * 1000L) {
                int remaining = (int) (cooldownSeconds - (System.currentTimeMillis() - lastTeleport) / 1000);
                player.sendMessage(hex(MSG().getString("messages.tp_cooldown").replace("{time}", String.valueOf(remaining)), player));
                return;
            }
        }

        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double newX = center.getX() + radius * Math.cos(angle);
        double newZ = center.getZ() + radius * Math.sin(angle);

        World world = center.getWorld();
        double newY = world.getHighestBlockYAt((int) newX, (int) newZ) + 1;

        Location newLocation = new Location(world, newX, newY, newZ);
        player.teleport(newLocation);

        if (cooldownSeconds > 0) {
            teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }



}
