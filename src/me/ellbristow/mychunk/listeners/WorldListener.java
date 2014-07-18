package me.ellbristow.mychunk.listeners;

import me.ellbristow.mychunk.MyChunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;


public class WorldListener implements Listener {
    
    public WorldListener() {
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onWorldLoad (WorldLoadEvent event) {
        
        if (!MyChunk.isWorldEnabled(event.getWorld().getName()) && !MyChunk.isWorldDisabled(event.getWorld().getName())) {
            MyChunk.enableWorld(event.getWorld().getName());
        }
        
    }
}