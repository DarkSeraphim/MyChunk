package me.ellbristow.mychunk.listeners;

import me.ellbristow.mychunk.MyChunkChunk;
import me.ellbristow.mychunk.lang.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
    
    public CommandListener() {
    }
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        
        String message[] = event.getMessage().split(" ");
        
        Player player = event.getPlayer();
        
        if (message[0].equalsIgnoreCase("/town") || message[0].equalsIgnoreCase("/t")) {
            
            if(message.length > 1 && (message[1].equalsIgnoreCase("new") || message[1].equalsIgnoreCase("claim"))) {

                Chunk loc = player.getLocation().getChunk();

                if (MyChunkChunk.isClaimed(loc)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + Lang.get("MyChunkClash"));
                }
                
            }
            
        }
        
    }

}
