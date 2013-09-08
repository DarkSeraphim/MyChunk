package me.ellbristow.mychunk;

import com.massivecraft.factions.entity.Board;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.event.FactionsEventChunkChange;
import com.massivecraft.factions.event.FactionsEventChunkChangeType;
import com.massivecraft.mcore.ps.PS;
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

        Faction f = new Board().getFactionAt(PS.valueOf(location));
        
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
    public void onFactionClaim(FactionsEventChunkChange event) {
        
        if (event.isCancelled()) return;
        
        if (event.getType() != FactionsEventChunkChangeType.BUY) return;
        
        int x = (int)event.getChunk().getChunkX();
        int z = (int)event.getChunk().getChunkX();
        Chunk chunk = event.getChunk().asBukkitWorld().getChunkAt(event.getChunk().asBukkitLocation());
        
        if (MyChunkChunk.isClaimed(chunk)) {
            event.setCancelled(true);
            event.getUSender().getPlayer().sendMessage(ChatColor.RED + Lang.get("MyChunkClash"));
        }
        
    }
    
}
