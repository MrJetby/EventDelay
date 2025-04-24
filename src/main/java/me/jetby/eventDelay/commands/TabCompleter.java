package me.jetby.eventDelay.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static me.jetby.eventDelay.configurations.Config.CFG;
import static me.jetby.eventDelay.manager.Assistants.isCompass;


public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (sender instanceof Player p) {




            if (args.length == 1) {

                if (p.hasPermission("eventdelay.admin")) {
                    completions.add("delay");
                    completions.add("info");
                    if (isCompass()) {
                        completions.add("compass");
                    }
                    completions.add("activate");
                    completions.add("setNext");
                    completions.add("timer");
                    completions.add("start");
                    completions.add("stop");
                    completions.add("reload");
                } else {
                    if (isCompass()) {
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
        }

        return List.of();
    }
}
