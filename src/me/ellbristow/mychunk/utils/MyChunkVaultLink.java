package me.ellbristow.mychunk.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MyChunkVaultLink {
    
    public static Economy economy;
    public static boolean foundEconomy;
    public static String economyName;
    
    public static void initEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        if (economy != null) {
            foundEconomy = true;
            economyName = economy.getName();
        }
    }

}
