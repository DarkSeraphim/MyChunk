package me.ellbristow.mychunk.listeners;

import me.ellbristow.mychunk.MyChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public class MobListener implements Listener {
    
    public MobListener() {
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onEntityInteract(EntityInteractEvent event) {
        
        if (event.isCancelled()) return;
        
        Block block = event.getBlock();
        
        if ((block.getType() == Material.CROPS || block.getType() ==  Material.SOIL || block.getType() ==  Material.CARROT || block.getType() ==  Material.POTATO || (block.getType() ==  Material.AIR && block.getRelative(BlockFace.DOWN).getType().equals(Material.SOIL))) && MyChunkChunk.isClaimed(block.getChunk())) {
            event.setCancelled(true);
        }
        
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onMonsterSpawn (CreatureSpawnEvent event) {
        
        if (event.isCancelled()) return;
        
        if (!event.getSpawnReason().equals(SpawnReason.EGG)) {
            LivingEntity mob = event.getEntity();

            if (mob instanceof Monster || mob instanceof Slime) {

                if (!MyChunkChunk.getAllowMobs(event.getLocation().getBlock().getChunk())) {
                    event.setCancelled(true);
                }

            }
        }
        
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onEndermanPickupEvent (EntityChangeBlockEvent event) {
        
        if (event.isCancelled()) return;
        
        if (event.getEntityType().equals(EntityType.ENDERMAN)) {
            
            if ((MyChunkChunk.isClaimed(event.getBlock().getChunk()) && !MyChunkChunk.getOwner(event.getBlock().getChunk()).equalsIgnoreCase("Public")) || MyChunk.getToggle("protectUnclaimed") || MyChunk.getToggle("allowMobGrief")) {
                event.setCancelled(true);
            }
            
        }
        
    }
    
}
