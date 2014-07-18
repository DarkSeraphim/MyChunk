package me.ellbristow.mychunk.listeners;

import me.ellbristow.mychunk.MyChunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    
    public ChatListener() {        
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        
        if (!MyChunk.getToggle("useChatFormat"))
            return;
        
        Player player = event.getPlayer();
        
        if (event.getMessage() != null) {
            event.setFormat(MyChunk.formatChat(event.getMessage(), player));
        }
        
    }
    
}
