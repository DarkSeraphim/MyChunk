package me.ellbristow.mychunk;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class MyChunkListener implements Listener {
    
    public static MyChunk plugin;
    
    public MyChunkListener (MyChunk instance) {
        plugin = instance;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onExplosion (EntityExplodeEvent event) {
        if (!event.isCancelled()) {
            List<Block> blocks = event.blockList();
            if (blocks != null) {
                int index = 0;
                Collection<Block> saveBanks = new HashSet<Block>();
                for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
                    Block block = it.next();
                    MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                    if (chunk.isClaimed() || plugin.protectUnclaimed) {
                        saveBanks.add(block);
                    }
                    index++;
                }
                if (!saveBanks.isEmpty()) {
                    event.blockList().removeAll(saveBanks);
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onBlockPlace (BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block, plugin);
            if ((chunk.isClaimed() && block.getTypeId() != 51) || plugin.protectUnclaimed) {
                String owner = chunk.getOwner();
                Player player = event.getPlayer();
                if (!owner.equalsIgnoreCase(player.getName()) && !chunk.isAllowed(player.getName(), "B") && !WorldGuardHook.isRegion(event.getBlock().getLocation())) {
                    if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.build"))) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to build here!");
                        if (block.getTypeId() != 63 && block.getTypeId() != 68) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onBlockBreak (BlockBreakEvent event) {
        if (!event.isCancelled()) {
            MyChunkChunk chunk = new MyChunkChunk(event.getBlock(), plugin);
            if (chunk.isClaimed() || plugin.protectUnclaimed) {
                String owner = chunk.getOwner();
                Player player = event.getPlayer();
                if (!owner.equalsIgnoreCase(player.getName())&& !chunk.isAllowed(player.getName(), "D") && !WorldGuardHook.isRegion(event.getBlock().getLocation())) {
                    if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.destroy"))) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to break blocks here!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onBlockIgnite (BlockIgniteEvent event) {
        if (!event.isCancelled()) {
            MyChunkChunk chunk = new MyChunkChunk(event.getBlock(), plugin);
            if (chunk.isClaimed() || plugin.protectUnclaimed) {
                String owner = chunk.getOwner();
                if (event.getCause() == IgniteCause.FLINT_AND_STEEL) {
                    Player player = event.getPlayer();
                    if (!owner.equalsIgnoreCase(player.getName()) && !chunk.isAllowed(player.getName(), "I")) {
                        if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.ignite"))) {
                            player.sendMessage(ChatColor.RED + "FIRE! Oh phew... you're not allowed!");
                            event.setCancelled(true);
                        }
                    }
                } else if (event.getCause() == IgniteCause.LAVA || event.getCause() == IgniteCause.SPREAD) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onZombieDoorEvent (EntityBreakDoorEvent event) {
        if (event.getBlock().getTypeId() == 64 && event.getEntityType().equals(EntityType.ZOMBIE)) {
            MyChunkChunk chunk = new MyChunkChunk(event.getBlock(), plugin);
            if (chunk.isClaimed() || plugin.protectUnclaimed) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerEmptyBucket (PlayerBucketEmptyEvent event) {
        if (!event.isCancelled()) {
            Block block = event.getBlockClicked();
            BlockFace face = event.getBlockFace();
            Block targetBlock;
            if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN)|| face.equals(BlockFace.SELF)) {
                targetBlock = block;
            } else {
                targetBlock = block.getRelative(face);
            }
            MyChunkChunk chunk = new MyChunkChunk(targetBlock, plugin);
            if (chunk.isClaimed() || plugin.protectUnclaimed) {
                String owner = chunk.getOwner();
                Player player = event.getPlayer();
                if ((!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.build"))) {
                    int bucket = event.getBucket().getId();
                    if (bucket == 327 && !chunk.isAllowed(player.getName(), "L")) {
                        player.sendMessage(ChatColor.RED + "Are you crazy!? You can't drop lava there!");
                        event.setCancelled(true);
                        player.setItemInHand(new ItemStack(327,1));
                    } else if (bucket == 326 && !chunk.isAllowed(player.getName(), "W")) {
                        player.sendMessage(ChatColor.RED + "Are you crazy!? You can't drop water there!");
                        event.setCancelled(true);
                        player.setItemInHand(new ItemStack(326,1));
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInteract (PlayerInteractEvent event) {
        if (!event.isCancelled()) {
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = event.getClickedBlock();
                if (block.getTypeId() == 64 || block.getTypeId() == 96 || block.getTypeId() == 107) {
                    MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                    Player player = event.getPlayer();
                    String owner = chunk.getOwner();
                    if (chunk.isClaimed() && !owner.equals(player.getName()) && !chunk.isAllowed(player.getName(), "O")) {
                        if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.doors"))) {
                            player.sendMessage(ChatColor.RED + ">KNOCK< >KNOCK< This door is locked!");
                            OfflinePlayer ownerPlayer = plugin.getServer().getOfflinePlayer(owner);
                            if (ownerPlayer.isOnline()) {
                                ownerPlayer.getPlayer().sendMessage(ChatColor.GOLD + ">KNOCK< >KNOCK< Someone is visiting your chunk!");
                            }
                            event.setCancelled(true);
                        }
                    }
                } else if (block.getTypeId() == 77) {
                    MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                    Player player = event.getPlayer();
                    String owner = chunk.getOwner();
                    if (chunk.isClaimed() && !owner.equals(player.getName()) && !chunk.isAllowed(player.getName(), "U")) {
                        if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.use"))) {
                            player.sendMessage(ChatColor.RED + ">BUZZZ< The button tripped a silent alarm!");
                            OfflinePlayer ownerPlayer = plugin.getServer().getOfflinePlayer(owner);
                            if (ownerPlayer.isOnline()) {
                                ownerPlayer.getPlayer().sendMessage(ChatColor.GOLD + ">BUZZ< Someone pressed a button in your chunk!");
                            }
                            event.setCancelled(true);
                        }
                    }
                } else if (block.getTypeId() == 69) {
                    MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                    Player player = event.getPlayer();
                    String owner = chunk.getOwner();
                    if (chunk.isClaimed() && !owner.equals(player.getName()) && !chunk.isAllowed(player.getName(), "U")) {
                        if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.use"))) {
                            player.sendMessage(ChatColor.RED + ">CLICK< The lever tripped a silent alarm!");
                            OfflinePlayer ownerPlayer = plugin.getServer().getOfflinePlayer(owner);
                            if (ownerPlayer.isOnline()) {
                                ownerPlayer.getPlayer().sendMessage(ChatColor.GOLD + ">CLICK< Someone touched a lever in your chunk!");
                            }
                            event.setCancelled(true);
                        }
                    }
                } else if (block.getTypeId() == 54) {
                    MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                    Player player = event.getPlayer();
                    String owner = chunk.getOwner();
                    if (chunk.isClaimed() && !owner.equals(player.getName()) && !chunk.isAllowed(player.getName(), "C")) {
                        if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.chests"))) {
                            player.sendMessage(ChatColor.RED + ">CLUNK< That chest isn't yours!");
                            OfflinePlayer ownerPlayer = plugin.getServer().getOfflinePlayer(owner);
                            if (ownerPlayer.isOnline()) {
                                ownerPlayer.getPlayer().sendMessage(ChatColor.GOLD + ">CLUNK< Someone tryed to open a chest on your chunk!");
                            }
                            event.setCancelled(true);
                        }
                    }
                } else if (block.getTypeId() == 61 || block.getTypeId() == 62 || block.getTypeId() == 23 || block.getTypeId() == 117) {
                    MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                    Player player = event.getPlayer();
                    String owner = chunk.getOwner();
                    if (chunk.isClaimed() && !owner.equals(player.getName()) && !chunk.isAllowed(player.getName(), "S")) {
                        if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.special"))) {
                            player.sendMessage(ChatColor.RED + ">BUZZZ< Hands off! That's a special block!");
                            OfflinePlayer ownerPlayer = plugin.getServer().getOfflinePlayer(owner);
                            if (ownerPlayer.isOnline()) {
                                ownerPlayer.getPlayer().sendMessage(ChatColor.GOLD + ">BUZZZ< Someone touched a special block in your chunk!");
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            } else if (event.getAction().equals(Action.PHYSICAL)) {
                Player player = event.getPlayer();
                Block block = player.getLocation().getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                String owner = chunk.getOwner();
                if (chunk.isClaimed() && !owner.equals(player.getName()) && !chunk.isAllowed(player.getName(), "U")) {
                    if ((!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.override")) || (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.server.use"))) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerMove (PlayerMoveEvent event) {
        if (!event.isCancelled()) {
            Location fromLoc = event.getFrom();
            Location toLoc = event.getTo();
            if (fromLoc.getChunk() != toLoc.getChunk()) {
                MyChunkChunk fromChunk = new MyChunkChunk(fromLoc.getBlock(), plugin);
                MyChunkChunk toChunk = new MyChunkChunk(toLoc.getBlock(), plugin);
                Player player = event.getPlayer();
                if (!fromChunk.getOwner().equals(toChunk.getOwner())) {
                    String forSale = "";
                    if (toChunk.isForSale()) {
                        forSale = ChatColor.YELLOW + " [Chunk For Sale";
                        if (plugin.foundEconomy && toChunk.getClaimPrice() != 0) {
                            if (plugin.ownedChunkCount(player.getName()) < plugin.maxChunks || !plugin.allowOverbuy) {
                                forSale += ": " + plugin.vault.economy.format(toChunk.getClaimPrice());
                            } else if (plugin.allowOverbuy && plugin.ownedChunkCount(player.getName()) >= plugin.maxChunks) {
                                forSale += ": " + plugin.vault.economy.format(toChunk.getOverbuyPrice());
                            }
                        }
                        forSale += "]";
                    }
                    if (toChunk.getOwner().equalsIgnoreCase("server")) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "~Server" + forSale);
                    } else {
                        player.sendMessage(ChatColor.GOLD + "~" + toChunk.getOwner() + forSale);
                    }
                } else if (toChunk.isForSale()) {
                    String forSale = ChatColor.YELLOW + "[Chunk For Sale";
                    if (plugin.foundEconomy && toChunk.getClaimPrice() != 0) {
                        if (plugin.ownedChunkCount(player.getName()) < plugin.maxChunks || !plugin.allowOverbuy || (plugin.allowOverbuy && player.hasPermission("mychunk.free"))) {
                            forSale += ": " + plugin.vault.economy.format(toChunk.getClaimPrice());
                        } else if (plugin.allowOverbuy && plugin.ownedChunkCount(player.getName()) >= plugin.maxChunks) {
                            forSale += ": " + plugin.vault.economy.format(toChunk.getOverbuyPrice());
                        }
                    }
                    forSale += "]";
                    player.sendMessage(forSale);
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onMonsterSpawn (CreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            LivingEntity mob = event.getEntity();
            if (mob instanceof Monster) {
                MyChunkChunk chunk = new MyChunkChunk(event.getLocation().getBlock(), plugin);
                if (chunk.isClaimed() && !chunk.getAllowMobs()) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerPVP (EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            MyChunkChunk chunk = new MyChunkChunk(player.getLocation().getBlock(),plugin);
            if (chunk.isClaimed()) {
                Entity damager = event.getDamager();
                if (damager instanceof Player) {
                    event.setCancelled(true);
                    Player naughty = (Player)damager;
                    naughty.sendMessage(ChatColor.RED + "That player is protected by a magic shield!");
                } else if (damager instanceof Monster) {
                    if (!chunk.getAllowMobs()) {
                        event.setCancelled(true);
                    }
                }
            }
        } else if (event.getDamager() instanceof Arrow) {

            MyChunkChunk chunk = new MyChunkChunk(entity.getLocation().getBlock(), plugin);
            Entity shooter = ((Arrow) event.getDamager()).getShooter();
            
            // Stop arrow damage in claimed chunks
            if (shooter instanceof Player && entity instanceof Player) {
                event.setCancelled(true);
            } else if (shooter instanceof Monster) {
                if (chunk.isClaimed() && !chunk.getAllowMobs()) {
                    event.setCancelled(true);
                }
            }

        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onSignChange (SignChangeEvent event) {
        if (!event.isCancelled()) {
            String line0 = event.getLine(0);
            String line1 = event.getLine(1);
            if (line0.equalsIgnoreCase("[claim]")) {
            // Player attempted to claim a chunk
                Player player = event.getPlayer();
                Block block = event.getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                boolean allowed = true;
                if (!player.hasPermission("mychunk.claim") && !player.hasPermission("mychunk.claim.server")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to claim chunks!");
                    allowed = false;
                } else if (chunk.isClaimed()) {
                    String owner = chunk.getOwner();
                    if (owner.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ChatColor.RED + "You already own this chunk!");
                        allowed = false;
                    } else if (!chunk.isForSale()) {
                        player.sendMessage(ChatColor.RED + "This Chunk is already owned by " + ChatColor.WHITE + owner + ChatColor.RED + "!");
                        allowed = false;
                    } else if (chunk.isForSale() && !player.hasPermission("mychunk.buy")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to buy owned chunks!");
                        allowed = false;
                    }
                } else if (plugin.allowNeighbours == false && chunk.hasNeighbours() && !chunk.isForSale()) {
                    String[] neighbours = chunk.getNeighbours();
                    for (int i = 0; i<neighbours.length; i++) {
                        if (!neighbours[i].equalsIgnoreCase("") && !neighbours[i].equalsIgnoreCase("server") && !neighbours[i].equalsIgnoreCase("unowned")) {
                            if (!neighbours[i].equalsIgnoreCase(player.getName()) && line1.equalsIgnoreCase("")) {
                                player.sendMessage(ChatColor.RED + "You cannot claim a chunk next to someone else's chunk!");
                                allowed = false;
                            } else if (!line1.equals("") && !neighbours[i].equalsIgnoreCase(line1) && !line1.equalsIgnoreCase("server")) {
                                player.sendMessage(ChatColor.RED + "You cannot claim a chunk next to someone else's chunk!");
                                allowed = false;
                            }
                        }
                    }
                }
                int playerMax = plugin.getMaxChunks(player);
                if (plugin.foundEconomy && chunk.getClaimPrice() != 0 && !player.hasPermission("mychunk.free") && (playerMax == 0 || plugin.ownedChunkCount(player.getName()) < playerMax) && plugin.vault.economy.getBalance(player.getName()) < chunk.getClaimPrice()) {
                    player.sendMessage(ChatColor.RED + "You cannot afford to claim that chunk! (Price: " + ChatColor.WHITE + plugin.vault.economy.format(plugin.chunkPrice) + ChatColor.RED + ")!");
                    allowed = false;
                } else if (plugin.foundEconomy && playerMax != 0 && plugin.ownedChunkCount(player.getName()) >= playerMax && !player.hasPermission("mychunk.free")) {
                    if (plugin.allowOverbuy && player.hasPermission("mychunk.claim.overbuy") && plugin.vault.economy.getBalance(player.getName()) < chunk.getOverbuyPrice()) {
                        player.sendMessage(ChatColor.RED + "You cannot afford to claim that chunk! (Price: " + ChatColor.WHITE + plugin.vault.economy.format(chunk.getOverbuyPrice()) + ChatColor.RED + ")!");
                        allowed = false;
                    }
                }
                if (allowed) {
                    if (line1.equals("") || line1.equalsIgnoreCase(player.getName())) {
                        int ownedChunks = plugin.ownedChunkCount(player.getName());
                        if ((ownedChunks < playerMax || (plugin.allowOverbuy && player.hasPermission("mychunk.claim.overbuy"))) || player.hasPermission("mychunk.claim.unlimited") || playerMax == 0) {
                            if (plugin.foundEconomy && chunk.getClaimPrice() != 0 && !player.hasPermission("mychunk.free") && (playerMax == 0 || plugin.ownedChunkCount(player.getName()) < playerMax)) {
                                plugin.vault.economy.withdrawPlayer(player.getName(), chunk.getClaimPrice());
                                player.sendMessage(plugin.vault.economy.format(chunk.getClaimPrice()) + ChatColor.GOLD + " was deducted from your account");
                            } else if (plugin.foundEconomy && plugin.allowOverbuy && plugin.ownedChunkCount(player.getName()) >= playerMax && !player.hasPermission("mychunk.free")) {
                                double price;
                                if (plugin.overbuyP2P) {
                                    price = chunk.getOverbuyPrice();
                                } else {
                                    price = chunk.getClaimPrice();
                                }
                                plugin.vault.economy.withdrawPlayer(player.getName(), price);
                                player.sendMessage(plugin.vault.economy.format(price) + ChatColor.GOLD + " was deducted from your account");
                            }
                            if (plugin.foundEconomy && chunk.isForSale()) {
                                plugin.vault.economy.depositPlayer(chunk.getOwner(), chunk.getClaimPrice());
                                OfflinePlayer oldOwner = plugin.getServer().getOfflinePlayer(chunk.getOwner());
                                if (oldOwner.isOnline()) {
                                    oldOwner.getPlayer().sendMessage(player.getName() + ChatColor.GOLD + " bought one of your chunks for " + ChatColor.WHITE + plugin.vault.economy.format(chunk.getClaimPrice()) + ChatColor.GOLD + "!");
                                }
                            }
                            chunk.claim(player.getName());
                            player.sendMessage(ChatColor.GOLD + "Chunk claimed!");
                        } else {
                            player.sendMessage(ChatColor.RED + "You already own " + ownedChunks + " chunks! (Max " + playerMax + ")");
                        }
                    } else {
                        String correctName = "";
                        allowed = true;
                        if (line1.equalsIgnoreCase("server")) {
                            if (!player.hasPermission("mychunk.claim.server")) {
                                player.sendMessage(ChatColor.RED + "You do not have permission to claim chunks for the server!");
                                allowed = false;
                            } else {
                                correctName = "Server";
                            }
                        } else {
                            if (player.hasPermission("mychunk.claim.others")) {
                                OfflinePlayer target = plugin.getServer().getOfflinePlayer(line1);
                                if (!target.hasPlayedBefore()) {
                                    player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + line1 + ChatColor.RED + " not found!");
                                    allowed = false;
                                } else {
                                    correctName = target.getName();
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to claim chunks for other players!");
                                allowed = false;
                            }
                        }
                        if (allowed) {
                            int ownedChunks = plugin.ownedChunkCount(player.getName());
                            if ((ownedChunks < plugin.maxChunks) || player.hasPermission("mychunk.claim.others.unlimited") || plugin.maxChunks == 0 || (correctName.equalsIgnoreCase("server") && player.hasPermission("mychunk.server.claim"))) {
                                chunk.claim(correctName);
                                player.sendMessage(ChatColor.GOLD + "Chunk claimed for " + ChatColor.WHITE + correctName + ChatColor.GOLD + "!");
                                if (plugin.foundEconomy && plugin.chunkPrice != 0 && !correctName.equalsIgnoreCase("server")) {
                                    plugin.vault.economy.withdrawPlayer(player.getName(), plugin.chunkPrice);
                                    player.sendMessage(plugin.vault.economy.format(plugin.chunkPrice) + ChatColor.GOLD + " was deducted from your account");
                                }
                            }
                        }
                    }
                }
                event.setCancelled(true);
                breakSign(block);
            } else if (line0.equalsIgnoreCase("[unclaim]")) {
            // Player attempted to unclaim a chunk
                Player player = event.getPlayer();
                Block block = event.getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                String owner = chunk.getOwner();
                boolean allowed =true;
                if (!chunk.isClaimed()) {
                    player.sendMessage(ChatColor.RED + "This chunk is not owned!");
                    allowed = false;
                } else if (!owner.equalsIgnoreCase(player.getName())) {
                    if (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.unclaim.server")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to unclaim chunks for the server!");
                        allowed = false;
                    } else if (!owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.unclaim.others")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to unclaim chunks for " + ChatColor.WHITE + owner + ChatColor.RED + "!");
                        allowed = false;
                    }
                }
                if (allowed) {
                    chunk.unclaim();
                    if (owner.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ChatColor.GOLD + "Chunk unclaimed!");
                    } else {
                        player.sendMessage(ChatColor.GOLD + "Chunk unclaimed for " + ChatColor.WHITE + owner + ChatColor.RED + "!");
                    }
                    if (plugin.unclaimRefund && !player.hasPermission("mychunk.free")) {
                        plugin.vault.economy.depositPlayer(player.getName(), plugin.chunkPrice);
                    }
                }
                event.setCancelled(true);
                breakSign(block);
            } else if (line0.equalsIgnoreCase("[owner]")) {
            // Player requested chunk's Owner info
                Player player = event.getPlayer();
                Block block = event.getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                if (chunk.isClaimed()) {
                    String owner = chunk.getOwner();
                    if (owner.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ChatColor.GOLD + "You own this chunk!");
                        player.sendMessage(ChatColor.GREEN + "Allowed Players : " + chunk.getAllowed());
                    } else {
                        player.sendMessage(ChatColor.GOLD + "This Chunk is owned by " + ChatColor.WHITE + owner + ChatColor.GOLD + "!");
                        player.sendMessage(ChatColor.GREEN + "Allowed Players : " + chunk.getAllowed());
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "This chunk is " + ChatColor.WHITE + "Unowned" + ChatColor.GOLD + "!");
                }
                event.setCancelled(true);
                breakSign(block);
            } else if (line0.equalsIgnoreCase("[allow]")) {
                // Player attempted to add a player allowance
                Player player = event.getPlayer();
                Block block = event.getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                if ("EVERYONE".equals(line1.toUpperCase())) {
                    line1 = "*";
                }
                String line2 = event.getLine(2).toUpperCase();
                if (!chunk.getOwner().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You do not own this chunk!");
                } else if ("".equals(line1) || line1.contains(" ")) {
                    player.sendMessage(ChatColor.RED + "Line 2 must contain a player name (or * for all)!");
                } else if (line1.equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You dont need to allow yourself!");
                } else {
                    if ("".equals(line2)) {
                        line2 = "*";
                    }
                    boolean found = true;
                    String targetName = "*";
                    if (!"*".equalsIgnoreCase(line1)) {
                        Player target = plugin.getServer().getPlayer(line1);
                        if (target == null) {
                            OfflinePlayer offTarget = plugin.getServer().getOfflinePlayer(line1);
                            targetName = offTarget.getName();
                            if (!offTarget.hasPlayedBefore()) {
                                player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + line1 + ChatColor.RED + " not found!");
                                found = false;
                            }
                        } else {
                            targetName = target.getName();
                        }
                    }
                    String displayName = targetName;
                    if (displayName.equals("*")) {
                        displayName = "EVERYONE";
                    }
                    if (found && !"*".equalsIgnoreCase(line2)) {
                        String errors = "";
                        for (int i = 0; i < line2.length(); i++) {
                            String thisChar = line2.substring(i, i+1).replaceAll(" ","");
                            if (chunk.isFlag(thisChar.toUpperCase())) {
                                chunk.allow(targetName, thisChar);
                            } else {
                                errors += thisChar;
                            }
                        }
                        player.sendMessage(ChatColor.GOLD + "Permission updated!");
                        if (!"".equals(errors)) {
                            player.sendMessage(ChatColor.RED + "Flags not found: " + errors);
                        }
                        player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " has had the following flags added: " + ChatColor.GREEN + line2.replaceAll(" ",""));
                        if (!"*".equals(targetName)) {
                            player.sendMessage(ChatColor.GREEN + "Allowed: " + chunk.getAllowedFlags(targetName));
                        }
                        player.sendMessage(ChatColor.GOLD + "Use an [owner] sign to see all permission flags");
                    } else if (found && "*".equalsIgnoreCase(line2)) {
                        chunk.allow(targetName, "*");
                        player.sendMessage(ChatColor.GOLD + "Permission updated!");
                        player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " has had the following flags added: " + ChatColor.GREEN + line2.replaceAll(" ",""));
                        if (!"*".equals(targetName)) {
                            player.sendMessage(ChatColor.GREEN + "New Flags: " + chunk.getAllowedFlags(targetName));
                        }
                        player.sendMessage(ChatColor.GOLD + "Use an [owner] sign to see all permission flags");
                    }
                } 
                event.setCancelled(true);
                breakSign(block);
            } else if (line0.equalsIgnoreCase("[disallow]")) {
                // Player attempted to add a player allowance
                Player player = event.getPlayer();
                Block block = event.getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                if ("EVERYONE".equals(line1.toUpperCase())) {
                    line1 = "*";
                }
                String line2 = event.getLine(2).toUpperCase();
                if (!chunk.getOwner().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You do not own this chunk!");
                } else if ("".equals(line1) || line1.contains(" ")) {
                    player.sendMessage(ChatColor.RED + "Line 2 must contain a player name (or * for all)!");
                } else if (line1.equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You cannot disallow yourself!");
                } else if (!"*".equals(line1) && chunk.isAllowed("*",line2)) {
                    player.sendMessage(ChatColor.RED + "You cannot disallow flags allowed to EVERYONE!");
                }else {
                    if ("".equals(line2)) {
                        line2 = "*";
                    }
                    boolean found = true;
                    String targetName = "*";
                    if (!"*".equalsIgnoreCase(line1)) {
                        Player target = plugin.getServer().getPlayer(line1);
                        if (target == null) {
                            OfflinePlayer offTarget = plugin.getServer().getOfflinePlayer(line1);
                            targetName = offTarget.getName();
                            if (!offTarget.hasPlayedBefore()) {
                                player.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + line1 + ChatColor.RED + " not found!");
                                found = false;
                            }
                        } else {
                            targetName = target.getName();
                        }
                    }
                    String displayName = targetName;
                    if (displayName.equals("*")) {
                        displayName = "EVERYONE";
                    }
                    if (found && !"*".equalsIgnoreCase(line2)) {
                        String errors = "";
                        for (int i = 0; i < line2.length(); i++) {
                            String thisChar = line2.substring(i, i+1).replaceAll(" ","");
                            if (chunk.isFlag(thisChar.toUpperCase())) {
                                chunk.disallow(targetName, thisChar);
                            } else {
                                errors += thisChar;
                            }
                        }
                        player.sendMessage(ChatColor.GOLD + "Permission updated!");
                        if (!"".equals(errors)) {
                            player.sendMessage(ChatColor.RED + "Flags not found: " + errors);
                        }
                        player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " has had the following flags removed: " + ChatColor.GREEN + line2.replaceAll(" ",""));
                        if (!"*".equals(targetName)) {
                            player.sendMessage(ChatColor.GREEN + "New Flags: " + chunk.getAllowedFlags(targetName));
                        }
                        player.sendMessage(ChatColor.GOLD + "Use an [owner] sign to see all permission flags");
                    } else if (found && "*".equalsIgnoreCase(line2)) {
                        chunk.disallow(targetName, "*");
                        player.sendMessage(ChatColor.GOLD + "Permission updated!");
                        player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " has had the following flags removed: " + ChatColor.GREEN + line2.replaceAll(" ",""));
                        if (!"*".equals(targetName)) {
                            player.sendMessage(ChatColor.GREEN + "New Flags: " + chunk.getAllowedFlags(targetName));
                        }
                        player.sendMessage(ChatColor.GOLD + "Use an [owner] sign to see all permission flags");
                    }
                } 
                event.setCancelled(true);
                breakSign(block);
            } else if (line0.equalsIgnoreCase("[for sale]")) {
                Player player = event.getPlayer();
                MyChunkChunk chunk = new MyChunkChunk(event.getBlock(), plugin);
                boolean allowed = true;
                Double price = 0.00;
                if (!player.hasPermission("mychunk.sell")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use [For Sale] signs!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                } else if (player.hasPermission("mychunk.free")) {
                    player.sendMessage(ChatColor.RED + "You can claim chunks for free! You're not allowed to sell them!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                } else if (!chunk.getOwner().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't sell this chunk, you don't own it!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                } else if (plugin.foundEconomy) {
                    if (line1.isEmpty() || line1.equals("")) {
                        player.sendMessage(ChatColor.RED + "Line 2 must contain your sale price!");
                        event.setCancelled(true);
                        breakSign(event.getBlock());
                        allowed = false;
                    } else {
                        try {
                            price = Double.parseDouble(line1);
                        } catch (NumberFormatException nfe) {
                            player.sendMessage(ChatColor.RED + "Line 2 must contain your sale price (in #.## format)!");
                            event.setCancelled(true);
                            breakSign(event.getBlock());
                            allowed = false;
                        }
                        if (price == 0) {
                            player.sendMessage(ChatColor.RED + "Sale price cannot be 0!");
                            event.setCancelled(true);
                            breakSign(event.getBlock());
                            allowed = false;
                        }
                    }
                    
                }
                if (allowed) {
                    if (plugin.foundEconomy) {
                        player.sendMessage(ChatColor.GOLD + "Chunk on sale for " + plugin.vault.economy.format(price) + "!");
                        chunk.setForSale(price);
                    } else {
                        player.sendMessage(ChatColor.GOLD + "Chunk on sale!");
                        chunk.setForSale(plugin.chunkPrice);
                    }
                    breakSign(event.getBlock());
                }
            } else if (line0.equalsIgnoreCase("[not for sale]")) {
                Player player = event.getPlayer();
                MyChunkChunk chunk = new MyChunkChunk(event.getBlock(), plugin);
                boolean allowed = true;
                if (!chunk.getOwner().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You don't own this chunk!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                } else if (!chunk.isForSale()) {
                    player.sendMessage(ChatColor.RED + "This chunk is not for sale!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                }
                if (allowed) {
                    player.sendMessage(ChatColor.GOLD + "Chunk taken off sale!");
                    chunk.setNotForSale();
                    breakSign(event.getBlock());
                }
            } else if (line0.equalsIgnoreCase("[AllowMobs]")) {
                Player player = event.getPlayer();
                MyChunkChunk chunk = new MyChunkChunk(event.getBlock(), plugin);
                boolean allowed = true;
                if (!chunk.getOwner().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You don't own this chunk!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                }
                if (!player.hasPermission("mychunk.allowmobs")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use [AllowMobs] signs!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                }
                if (!line1.equalsIgnoreCase("on") && !line1.equalsIgnoreCase("off")) {
                    player.sendMessage(ChatColor.RED + "Line 2 must say either " + ChatColor.GOLD + "on" + ChatColor.RED + " or " + ChatColor.GOLD + "off" + ChatColor.RED + "!");
                    event.setCancelled(true);
                    breakSign(event.getBlock());
                    allowed = false;
                }
                if (allowed) {
                    if (line1.equalsIgnoreCase("on")) {
                        chunk.setAllowMobs(true);
                        player.sendMessage(ChatColor.GOLD + "Mobs now " + ChatColor.GREEN + "CAN" + ChatColor.GOLD + " spawn in this chunk!");
                    } else {
                        chunk.setAllowMobs(false);
                        player.sendMessage(ChatColor.GOLD + "Mobs now " + ChatColor.RED + "CAN NOT" + ChatColor.GOLD + " spawn in this chunk!");
                    }
                    breakSign(event.getBlock());
                }
            }
            if (!event.isCancelled()) {
                Block block = event.getBlock();
                MyChunkChunk chunk = new MyChunkChunk(block, plugin);
                if (chunk.isClaimed()) {
                    String owner = chunk.getOwner();
                    Player player = event.getPlayer();
                    if (!owner.equalsIgnoreCase(player.getName()) && !chunk.isAllowed(player.getName(), "B") && !player.hasPermission("mychunk.override")) {
                        if (!owner.equalsIgnoreCase("server") ^ !player.hasPermission("mychunk.server.build")) {
                            event.setCancelled(true);
                            breakSign(block);
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPaintingBreak(PaintingBreakByEntityEvent event) {
        if (event.isCancelled())
            return;
        Entity remover = event.getRemover();
        if (remover instanceof Player) {
            MyChunkChunk chunk = new MyChunkChunk(event.getPainting().getLocation().getBlock(), plugin);
            if (!((Player)remover).hasPermission("mychunk.override") && (chunk.isClaimed() && !((Player)remover).getName().equals(chunk.getOwner()) && !chunk.isAllowed(((Player)remover).getName(), "B")) || plugin.protectUnclaimed) {
                ((Player)remover).sendMessage(ChatColor.RED + "You do not have permission to break blocks here!");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.isCancelled()) {
            MyChunkChunk fromChunk = new MyChunkChunk(event.getFrom().getBlock(), plugin);
            MyChunkChunk toChunk = new MyChunkChunk(event.getTo().getBlock(), plugin);
            if (!fromChunk.getOwner().equals(toChunk.getOwner())) {
                if (toChunk.getOwner().equalsIgnoreCase("server")) {
                    event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "~" + toChunk.getOwner());
                } else {
                    event.getPlayer().sendMessage(ChatColor.GOLD + "~" + toChunk.getOwner());
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        if (event.getBlock().getChunk() != event.getBlock().getRelative(event.getDirection()).getChunk()) {
            MyChunkChunk chunk1 = new MyChunkChunk(event.getBlock(), plugin);
            MyChunkChunk chunk2 = new MyChunkChunk(event.getBlock().getRelative(event.getDirection()), plugin);
            if (chunk2.isClaimed() && !chunk1.getOwner().equals(chunk2.getOwner())) {
                // Pushing into an owned chunk with a different owner
                event.setCancelled(true);
                return;
            }
        }
        if (!event.getDirection().equals(BlockFace.UP) && !event.getDirection().equals(BlockFace.DOWN)) {
            // Pushing Sideways
            List<Block> blocks = event.getBlocks();
            for (Block block : blocks) {
                if (block.getChunk() != block.getRelative(event.getDirection()).getChunk()) {
                    MyChunkChunk chunk1 = new MyChunkChunk(block, plugin);
                    MyChunkChunk chunk2 = new MyChunkChunk(block.getRelative(event.getDirection()), plugin);
                    if (chunk2.isClaimed() && !chunk1.getOwner().equals(chunk2.getOwner())) {
                        // Pushing into an owned chunk with a different owner
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        if (!event.getDirection().equals(BlockFace.UP) && !event.getDirection().equals(BlockFace.DOWN)) {
            if (event.isSticky()) {
                if (event.getBlock().getChunk() != event.getRetractLocation().getBlock().getChunk()) {
                    MyChunkChunk chunk1 = new MyChunkChunk(event.getBlock(), plugin);
                    MyChunkChunk chunk2 = new MyChunkChunk(event.getRetractLocation().getBlock(), plugin);
                    if (chunk2.isClaimed() && !chunk1.getOwner().equals(chunk2.getOwner())) {
                        // Pulling out of an owned chunk with a different owner
                        event.setCancelled(true);
                        event.getBlock().setType(Material.PISTON_STICKY_BASE);
                        switch (event.getDirection()) {
                            case NORTH:
                                event.getBlock().setData((byte)4);
                                break;
                            case SOUTH:
                                event.getBlock().setData((byte)5);
                                break;
                            case EAST:
                                event.getBlock().setData((byte)2);
                                break;
                            case WEST:
                                event.getBlock().setData((byte)3);
                                break;
                        }
                        event.getBlock().getRelative(event.getDirection()).setType(Material.AIR);
                        event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.PISTON_RETRACT, 1, 1);
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        refreshOwnership(event.getPlayer().getName());
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        refreshOwnership(event.getPlayer().getName());
    }
    
    private void breakSign(Block block) {
        if (block.getTypeId() == 63 || block.getTypeId() == 68) {
            block.setTypeId(0);
            block.getWorld().dropItem(block.getLocation(), new ItemStack(323,1));
        }
    }
    
    private void refreshOwnership(String playerName) {
        Object[] allChunks = plugin.chunkStore.getKeys(true).toArray();
        for (int i = 1; i < allChunks.length; i++) {
            String thisOwner = plugin.chunkStore.getString(allChunks[i] + ".owner");
            if (playerName.equals(thisOwner)) {
                plugin.chunkStore.set(allChunks[i] + ".lastActive", new Date().getTime() / 1000);
            }
        }
        plugin.saveChunkStore();
    }
}
