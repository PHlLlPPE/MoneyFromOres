package com.example.moneyfromores;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MFOCommand implements CommandExecutor {

    private final MoneyFromOres plugin;

    public MFOCommand(MoneyFromOres plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {

            // --- RELOAD ---
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("moneyfromores.command")) {
                    sender.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                    return true;
                }

                plugin.reloadConfig();
                sender.sendMessage("§aConfiguration rechargée !");
                return true;
            }

            // --- STATS ---
            if (args[0].equalsIgnoreCase("stats")) {
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
        }

        // --- Aide par défaut ---
        sender.sendMessage("§6Utilisation :");
        sender.sendMessage("§e/mfo reload §7- Recharge la configuration");
        sender.sendMessage("§e/mfo stats §7- Affiche tes statistiques personnelles");
        return true;
    }
}
