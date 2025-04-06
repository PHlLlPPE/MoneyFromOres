package com.example.moneyfromores;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class MFOCommand implements CommandExecutor {

    private final MoneyFromOres plugin;

    public MFOCommand(MoneyFromOres plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /mfo reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("moneyfromores.command")) {
                sender.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                return true;
            }

            plugin.reloadConfig();
            sender.sendMessage("§aConfiguration rechargée !");
            return true;
        }

        // /mfo stats
        if (args.length == 1 && args[0].equalsIgnoreCase("stats")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;
            String uuid = player.getUniqueId().toString();

            int blocks = plugin.getStatsConfig().getInt(uuid + ".blocks", 0);
            double earned = plugin.getStatsConfig().getDouble(uuid + ".earned", 0.0);

            player.sendMessage("§e--- Statistiques MoneyFromOres ---");
            player.sendMessage("§7Minerais minés : §a" + blocks);
            player.sendMessage("§7Argent gagné : §a" + String.format("%.2f", earned) + " $");
            return true;
        }

        // /mfo top [earned]
        if (args.length >= 1 && args[0].equalsIgnoreCase("top")) {
            boolean sortByMoney = args.length > 1 && args[1].equalsIgnoreCase("earned");

            FileConfiguration stats = plugin.getStatsConfig();
            Map<String, Double> sorted = new HashMap<>();

            for (String key : stats.getKeys(false)) {
                double value = sortByMoney
                        ? stats.getDouble(key + ".earned", 0.0)
                        : stats.getInt(key + ".blocks", 0);
                sorted.put(key, value);
            }

            List<Map.Entry<String, Double>> top = sorted.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .toList();

            sender.sendMessage("§e--- TOP " + (sortByMoney ? "Riches (Argent gagné)" : "Mineurs (Minerais minés)") + " ---");

            int i = 1;
            for (Map.Entry<String, Double> entry : top) {
                String name = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey())).getName();
                String formatted = sortByMoney
                        ? String.format("§a%.2f $", entry.getValue())
                        : String.format("§a%d minerais", entry.getValue().intValue());

                sender.sendMessage("§6#" + i + " §f" + (name != null ? name : "Inconnu") + " §7- " + formatted);
                i++;
            }

            return true;
        }

        // Aide par défaut
        sender.sendMessage("§6Utilisation des commandes :");
        sender.sendMessage("§e/mfo reload §7- Recharge la configuration");
        sender.sendMessage("§e/mfo stats §7- Affiche tes statistiques personnelles");
        sender.sendMessage("§e/mfo top §7- Classement par minerais minés");
        sender.sendMessage("§e/mfo top earned §7- Classement par argent gagné");
        return true;
    }
}
