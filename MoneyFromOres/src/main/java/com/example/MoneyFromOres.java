package com.example.moneyfromores;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

import java.util.Arrays;
import java.util.List;

public class MoneyFromOres extends JavaPlugin implements Listener {

    private Economy econ;

    // Liste des minerais à récompenser
    private final List<Material> ores = Arrays.asList(
        Material.DIAMOND_ORE,
        Material.GOLD_ORE,
        Material.IRON_ORE,
        Material.EMERALD_ORE,
        Material.COPPER_ORE,
        Material.REDSTONE_ORE,
        Material.LAPIS_ORE
    );

    @Override
    public void onEnable() {
        // Chargement de la config par défaut si elle n'existe pas encore
        saveDefaultConfig();

        // Vérifie que Vault et un plugin d'économie sont installés
        if (!setupEconomy()) {
            getLogger().severe("Vault ou un plugin d'économie n'a pas été trouvé !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Enregistre l'événement de casse de blocs
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("✅ MoneyFromOres activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ MoneyFromOres désactivé.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().getGameMode().toString().equals("SURVIVAL")) {
            return;
        }

        Material blockType = event.getBlock().getType();
        if (ores.contains(blockType)) {
            // Lis la récompense dans config.yml, ou utilise 10.0 par défaut
            double reward = getConfig().getDouble("rewards." + blockType.name(), 10.0);

            econ.depositPlayer(event.getPlayer(), reward);

            // Message personnalisable dans config.yml
            String message = getConfig().getString("messages.reward", "§aTu viens de gagner {amount} money pour avoir miné un {ore} !");
            message = message.replace("{amount}", String.valueOf(reward))
                             .replace("{ore}", blockType.name());

            event.getPlayer().sendMessage(message);
        }
    }
}
