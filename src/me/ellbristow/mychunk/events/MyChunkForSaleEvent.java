package me.ellbristow.mychunk.events;

import me.ellbristow.mychunk.LiteChunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class MyChunkForSaleEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private LiteChunk chunk;
    private String owner;
    
    public MyChunkForSaleEvent(String worldName, int x, int z, String owner, boolean chunkForSale) {
        chunk = new LiteChunk(worldName, x, z, owner, chunkForSale);
        this.owner = owner;
    }
    
    public LiteChunk getLiteChunk() {
        return chunk;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public String getOwner() {
        return owner;
    }

}
