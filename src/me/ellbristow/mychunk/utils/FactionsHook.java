package me.ellbristow.mychunk.utils;

import me.ellbristow.mychunk.MyChunkChunk;
import me.ellbristow.mychunk.lang.Lang;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.massivecraft.factions.entity.Board;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.event.EventFactionsChunkChangeType;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.massivecore.ps.PS;

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
		Plugin plugin = Bukkit.getServer().getPluginManager()
				.getPlugin("Factions");

		// Factions may not be loaded
		if (plugin == null) {
			return false;
		}
		return true;
	}

	@EventHandler (priority = EventPriority.NORMAL)
    public void onFactionClaim(EventFactionsChunksChange event) {
        
        if (event.isCancelled())
        	return;
        
        for(EventFactionsChunkChangeType type : event.getChunkType().values()) {
        	if(!type.equals(EventFactionsChunkChangeType.BUY))
        		return;
        }
        
        for(PS chunk : event.getChunks()) {
        	Chunk bukkitChunk = chunk.asBukkitWorld().getChunkAt(chunk.asBukkitLocation());
            if (MyChunkChunk.isClaimed(bukkitChunk)) {
                event.setCancelled(true);
                event.getMSender().getPlayer().sendMessage(ChatColor.RED + Lang.get("MyChunkClash"));
            }
        }
        
    }
}
