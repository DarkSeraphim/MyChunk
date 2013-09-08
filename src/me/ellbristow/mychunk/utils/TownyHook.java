package me.ellbristow.mychunk.utils;

import com.palmergames.bukkit.towny.object.TownyUniverse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TownyHook implements Listener {

    public static boolean isClaimed(Location location) {
        if (!foundTowny()) {
            return false;
        }
        
        String town = TownyUniverse.getTownName(location);
        
        if (town == null || "".equals(town)) {
            return false;
        }

        return true;
    }

    public static boolean foundTowny() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Towny");

        // Towny may not be loaded
        if (plugin == null) {
            return false;
        }
        return true;
    }
    
}
