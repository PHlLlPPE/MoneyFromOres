package com.example.moneyfromores;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MoneyFromOres extends JavaPlugin implements Listener {

    private Economy econ;
    private FileConfiguration statsConfig;
    private File statsFile;

    private FileConfiguration toggleConfig;
    private File toggleFile;

    private final HashSet<String> placedBlocks = new HashSet<>();

    private final List<Material> ores = Arrays.asList(
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE
    );

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadStatsFile();
        loadToggleFile();

        if (!setupEconomy()) {
            getLogger().severe("Vault ou un plugin d'économie est manquant !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("mfo").setExecutor(new MFOCommand(this));

        String[] startupArt = {
            "§9━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "§b🟢 [MFO] Plugin MoneyFromOres v1.0 activé !",
            "§7Auteur : §fQuantumCraft-Studio",
            "§7Description : §eRécompense les joueurs pour l'extraction de minerais.",
            "§7Commandes : §a/mfo stats, /mfo top, /mfo toggle, /mfo reload",
            "§7Site : §nhttps://quantumcraft.dev",
            "§9━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        };
        for (String line : startupArt) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    @Override
    public void onDisable() {
        saveStatsFile();
        saveToggleFile();

        String[] shutdownArt = {
            "§4━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            "§c🔴 [MFO] Plugin MoneyFromOres désactivé.",
            "§7Auteur : §fQuantumCraft-Studio",
            "§7Merci d’avoir utilisé le plugin 💎",
            "§4━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        };
        for (String line : shutdownArt) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
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
        return location.getWorld().getName() + ":" +
               location.getBlockX() + "," +
               location.getBlockY() + "," +
               location.getBlockZ();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlockPlaced().getType();
        if (ores.contains(type)) {
            placedBlocks.add(serializeLocation(event.getBlockPlaced().getLocation()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        Material type = event.getBlock().getType();
        if (!ores.contains(type)) return;

        String loc = serializeLocation(event.getBlock().getLocation());

        if (placedBlocks.contains(loc)) {
            placedBlocks.remove(loc);
            String antiFarmMsg = getConfig().getString("messages.anti_farm");
            if (antiFarmMsg != null) player.sendMessage(antiFarmMsg);
            return;
        }

        // 🎲 Système de chance
        double chance = getConfig().getDouble("chance", 1.0);
        if (Math.random() > chance) return;

        double baseReward = getConfig().getDouble("rewards." + type.name(), 0.0);
        if (baseReward <= 0) {
            String noRewardMsg = getConfig().getString("messages.no_reward");
            if (noRewardMsg != null) player.sendMessage(noRewardMsg);
            return;
        }

        double multiplier = getMultiplierForPlayer(player);
        double reward = baseReward * multiplier;

        econ.depositPlayer(player, reward);

        String msg = getConfig().getString("messages.reward",
                "§aTu as gagné {amount} money pour avoir miné un {ore} !");
        msg = msg.replace("{amount}", String.format("%.2f", reward))
                 .replace("{ore}", type.name())
                 .replace("{multiplier}", String.valueOf(multiplier));

        if (isToggleEnabled(player)) {
            player.sendMessage(msg);
        }

        if (getConfig().getBoolean("effects.enabled", true)) {
            String soundName = getConfig().getString("effects.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            String particleName = getConfig().getString("effects.particle", "VILLAGER_HAPPY");

            try {
                if (!soundName.equalsIgnoreCase("none")) {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    player.playSound(player.getLocation(), sound, 1f, 1f);
                }
            } catch (Exception e) {
                getLogger().warning("Son invalide : " + soundName);
            }

            try {
                if (!particleName.equalsIgnoreCase("none")) {
                    Particle particle = Particle.valueOf(particleName.toUpperCase());
                    player.spawnParticle(particle, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5);
                }
            } catch (Exception e) {
                getLogger().warning("Particule invalide : " + particleName);
            }
        }

        String uuid = player.getUniqueId().toString();
        statsConfig.set(uuid + ".blocks", statsConfig.getInt(uuid + ".blocks", 0) + 1);
        statsConfig.set(uuid + ".earned", statsConfig.getDouble(uuid + ".earned", 0) + reward);
        saveStatsFile();
    }

    public void loadToggleFile() {
        toggleFile = new File(getDataFolder(), "toggle.yml");
        if (!toggleFile.exists()) {
            try {
                toggleFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        toggleConfig = YamlConfiguration.loadConfiguration(toggleFile);
    }

    public void saveToggleFile() {
        try {
            toggleConfig.save(toggleFile);
        } catch (IOException e) {
            getLogger().severe("Erreur lors de la sauvegarde de toggle.yml !");
            e.printStackTrace();
        }
    }

    public boolean isToggleEnabled(Player player) {
        return toggleConfig.getBoolean(player.getUniqueId().toString(), true);
    }

    public void toggleMessages(Player player) {
        String uuid = player.getUniqueId().toString();
        boolean current = toggleConfig.getBoolean(uuid, true);
        toggleConfig.set(uuid, !current);
        saveToggleFile();
    }
}
