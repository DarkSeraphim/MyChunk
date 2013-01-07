package me.ellbristow.mychunk;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.LandClaimEvent;
import me.ellbristow.mychunk.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class FactionsHook implements Listener {

    public static boolean isClaimed(Location location) {
        if (!foundFactions()) {
            return false;
        }

        Faction f = Board.getFactionAt(location);
        
        if (f.isNone()) {
            return false;
        }

        return true;
    }

    public static boolean foundFactions() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Factions");

        // Factions may not be loaded
        if (plugin == null) {
            return false;
        }
        return true;
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onFactionClaim(LandClaimEvent event) {
        
        if (event.isCancelled()) return;
        
        int x = (int)event.getLocation().getX();
        int z = (int)event.getLocation().getZ();
        Chunk chunk = event.getLocation().getWorld().getChunkAt(x, z);
        
        if (MyChunkChunk.isClaimed(chunk)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + Lang.get("MyChunkClash"));
        }
        
    }
    
}
