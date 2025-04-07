package com.example.moneyfromores;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MFOCommand implements CommandExecutor {

    private final MoneyFromOres plugin;

    public MFOCommand(MoneyFromOres plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("moneyfromores.command")) {
            sender.sendMessage("§cTu n’as pas la permission d’utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§e===== MoneyFromOres =====");
            sender.sendMessage("§e/mfo stats §7- Voir vos statistiques");
            sender.sendMessage("§e/mfo top §7- Classement par minerais minés");
            sender.sendMessage("§e/mfo top earned §7- Classement par argent gagné");
            sender.sendMessage("§e/mfo toggle §7- Activer/désactiver les messages de gain");
            sender.sendMessage("§e/mfo reload §7- Recharger la configuration");
            return true;
        }

        if (args.length == 1) {
            String sub = args[0].toLowerCase();

            switch (sub) {
                case "reload":
                    plugin.reloadConfig();
                    plugin.loadStatsFile();
                    plugin.loadToggleFile();
                    sender.sendMessage("§aConfiguration rechargée !");
                    return true;

                case "stats":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cCette commande est réservée aux joueurs.");
                        return true;
                    }
                    Player player = (Player) sender;
                    FileConfiguration stats = plugin.getStatsConfig();
                    String uuid = player.getUniqueId().toString();
                    int blocks = stats.getInt(uuid + ".blocks", 0);
                    double earned = stats.getDouble(uuid + ".earned", 0.0);
                    player.sendMessage("§6== Vos statistiques ==");
                    player.sendMessage("§eMinerais minés : §f" + blocks);
                    player.sendMessage("§eArgent gagné : §f" + String.format("%.2f", earned));
                    return true;

                case "toggle":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
                        return true;
                    }
                    Player p = (Player) sender;
                    plugin.toggleMessages(p);
                    boolean enabled = plugin.isToggleEnabled(p);
                    p.sendMessage(enabled
                        ? "§aMessages de récompense activés."
                        : "§cMessages de récompense désactivés.");
                    return true;

                case "top":
                case "topmined":
                    sendTop(sender, "blocks", "Minerais minés");
                    return true;

                case "topearned":
                case "earned":
                    sendTop(sender, "earned", "Argent gagné");
                    return true;

                default:
                    sender.sendMessage("§cCommande inconnue. Tape /mfo pour voir les options.");
                    return true;
            }
        }

        return false;
    }

    private void sendTop(CommandSender sender, String path, String title) {
        FileConfiguration stats = plugin.getStatsConfig();
        Map<String, Double> values = new HashMap<>();

        for (String uuid : stats.getKeys(false)) {
            double value = stats.getDouble(uuid + "." + path, 0.0);
            values.put(uuid, value);
        }

        List<Map.Entry<String, Double>> sorted = values.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        sender.sendMessage("§6== Classement : " + title + " ==");
        int i = 1;
        for (Map.Entry<String, Double> entry : sorted) {
            String name = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey())).getName();
            String value = path.equals("blocks") ? String.valueOf(entry.getValue().intValue())
                                                 : String.format("%.2f", entry.getValue());
            sender.sendMessage("§e" + i + ". §f" + name + " : §b" + value);
            i++;
        }
    }
}
