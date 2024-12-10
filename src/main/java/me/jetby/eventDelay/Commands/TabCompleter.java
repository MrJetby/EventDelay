package me.jetby.eventDelay.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static me.jetby.eventDelay.Main.cfg;
import static me.jetby.eventDelay.Manager.Assistants.getNowEvent;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (sender instanceof Player) {
            Player p = (Player) sender;

            List<String> adminCommands = List.of(
                    "delay",
                    "info",
                    "compass",
                    "activate",
                    "next",
                    "timer",
                    "start",
                    "stop",
                    "reload"
            );

            List<String> userCommands = List.of(
                    "delay",
                    "compass",
                    "info"
            );

            List<String> availableCommands = p.hasPermission("eventdelay.admin") ? adminCommands : userCommands;

            if (args.length == 1) {
                String input = args[0].toLowerCase();
                return availableCommands.stream()
                        .filter(cmd -> cmd.startsWith(input))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
