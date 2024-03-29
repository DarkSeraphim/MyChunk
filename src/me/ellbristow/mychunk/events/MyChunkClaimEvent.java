package me.ellbristow.mychunk.events;

import me.ellbristow.mychunk.LiteChunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class MyChunkClaimEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private LiteChunk chunk;
    private String oldOwner;
    public MyChunkClaimEvent(String worldName, int x, int z, String oldOwner, String newOwner, boolean chunkForSale) {
        chunk = new LiteChunk(worldName, x, z, newOwner, chunkForSale);
        this.oldOwner = oldOwner;
    }
    
    public LiteChunk getLiteChunk() {
        return chunk;
    }
    
    public String getOldOwner() {
        return oldOwner;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
