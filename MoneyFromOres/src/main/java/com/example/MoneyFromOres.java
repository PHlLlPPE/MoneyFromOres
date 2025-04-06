package com.example.moneyfromores;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MoneyFromOres extends JavaPlugin implements Listener {

    private Economy econ;
    private FileConfiguration statsConfig;
    private File statsFile;

    private final HashSet<String> placedBlocks = new HashSet<>();

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
        saveDefaultConfig();
        loadStatsFile();

        if (!setupEconomy()) {
            getLogger().severe("Vault ou un plugin d'économie est manquant !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("mfo").setExecutor(new MFOCommand(this));
        getLogger().info("✅ MoneyFromOres activé !");
    }

    @Override
    public void onDisable() {
        saveStatsFile();
        getLogger().info("❌ MoneyFromOres désactivé.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public void loadStatsFile() {
        statsFile = new File(getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    public void saveStatsFile() {
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            getLogger().severe("Erreur lors de la sauvegarde de stats.yml !");
            e.printStackTrace();
        }
    }

    public FileConfiguration getStatsConfig() {
        return statsConfig;
    }

    public double getMultiplierForPlayer(Player player) {
        double multiplier = 1.0;
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("moneyfromores.multiplier." + i)) {
                multiplier = i;
                break;
            }
        }
        return multiplier;
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + ":"
             + location.getBlockX() + ","
             + location.getBlockY() + ","
             + location.getBlockZ();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material blockType = event.getBlockPlaced().getType();
        if (ores.contains(blockType)) {
            String loc = serializeLocation(event.getBlockPlaced().getLocation());
            placedBlocks.add(loc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.getGameMode().toString().equals("SURVIVAL")) return;

        Material blockType = event.getBlock().getType();
        if (!ores.contains(blockType)) return;

        String loc = serializeLocation(event.getBlock().getLocation());

        // Système anti-farm
        if (placedBlocks.contains(loc)) {
            placedBlocks.remove(loc);
            return;
        }

        double baseReward = getConfig().getDouble("rewards." + blockType.name(), 10.0);
        double multiplier = getMultiplierForPlayer(player);
        double reward = baseReward * multiplier;

        econ.depositPlayer(player, reward);

        String message = getConfig().getString("messages.reward",
            "§aTu as gagné {amount} money pour avoir miné un {ore} !");
        message = message.replace("{amount}", String.format("%.2f", reward))
                         .replace("{ore}", blockType.name())
                         .replace("{multiplier}", String.valueOf(multiplier));

        player.sendMessage(message);

        String uuid = player.getUniqueId().toString();
        int blocks = statsConfig.getInt(uuid + ".blocks", 0);
        double total = statsConfig.getDouble(uuid + ".earned", 0.0);

        statsConfig.set(uuid + ".blocks", blocks + 1);
        statsConfig.set(uuid + ".earned", total + reward);
        saveStatsFile();
    }
}
