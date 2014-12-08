package me.ellbristow.mychunk.listeners;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import me.ellbristow.mychunk.MyChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import me.ellbristow.mychunk.ganglands.GangLands;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.utils.FactionsHook;
import me.ellbristow.mychunk.utils.MyChunkVaultLink;
import me.ellbristow.mychunk.utils.TownyHook;
import me.ellbristow.mychunk.utils.WorldGuardHook;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerListener implements Listener {
    
    private HashMap<String, Block> leaseConfirm = new HashMap<String, Block>();
    
    public PlayerListener() {
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerDamage (EntityDamageByEntityEvent event) {
        
        if (event.isCancelled()) return;
        if (!MyChunk.isWorldEnabled(event.getEntity().getWorld().getName()) && !MyChunkChunk.getOwner(event.getEntity().getLocation().getChunk()).equalsIgnoreCase("Server")) return;

        Entity entity = event.getEntity();
        
        if (entity instanceof Player) {
            
            Player hurtPlayer = (Player)entity;
            MyChunkChunk chunk = new MyChunkChunk(hurtPlayer.getLocation().getChunk());
            MyChunkChunk fromChunk = new MyChunkChunk(event.getDamager().getLocation().getChunk());
            
            if (chunk.isClaimed() || fromChunk.isClaimed()) {
                
                Entity damager = event.getDamager();
                
                if (damager instanceof Player && (!chunk.getAllowPVP() || !fromChunk.getAllowPVP())) {
                    
                    event.setCancelled(true);
                    Player naughty = (Player)damager;
                    naughty.sendMessage(ChatColor.RED + Lang.get("NoPermsPVP"));
                    
                } else if ((damager instanceof Monster || damager instanceof Slime) && (!chunk.getAllowMobs() || !fromChunk.getAllowMobs())) {
                    
                    event.setCancelled(true);
                    
                } else if (damager instanceof Projectile) {
                    
                    ProjectileSource shooter = ((Projectile)event.getDamager()).getShooter();
                    
                    if (shooter instanceof Player && (!chunk.getAllowPVP() || !fromChunk.getAllowPVP())) {
                        event.setCancelled(true);
                    } else if (shooter instanceof Monster && !chunk.getAllowMobs()) {
                        event.setCancelled(true);
                    }
                    
                }
                
            } else if (MyChunk.getToggle("preventPVP")) {
                
                Entity damager = event.getDamager();
                
                if (damager instanceof Player && !chunk.getAllowPVP()) {
                    
                    event.setCancelled(true);
                    Player naughty = (Player)damager;
                    naughty.sendMessage(ChatColor.RED + Lang.get("NoPermsPVP"));
                    
                } else if (damager instanceof Projectile) {
                    
                	ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                    
                    if (shooter instanceof Player && !chunk.getAllowPVP()) {
                        event.setCancelled(true);
                    }
                    
                }
                
            }
            
        } else if (entity instanceof Animals) {
            MyChunkChunk chunk = new MyChunkChunk(entity.getLocation().getChunk());
            
            if (chunk.isClaimed()) {
                
                Entity damager = event.getDamager();
                
                if (damager instanceof Player && !chunk.getOwner().equalsIgnoreCase(((Player)damager).getName()) && !chunk.isAllowed(((Player)damager).getName(), "A") && !((Player)damager).hasPermission("mychunk.bypass")) {
                    
                    event.setCancelled(true);
                    Player naughty = (Player)damager;
                    naughty.sendMessage(ChatColor.RED + Lang.get("NoPermsAnimals"));
                    
                } else if (damager instanceof Projectile) {
                    
                	ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
                    
                    if (shooter instanceof Player && !chunk.getOwner().equalsIgnoreCase(((Player)shooter).getName()) && !chunk.isAllowed(((Player)shooter).getName(), "A")) {
                        event.setCancelled(true);
                    }
                    
                }
                
            }
        } else if (entity instanceof Monster || entity instanceof Slime) {
             MyChunkChunk chunk = new MyChunkChunk(entity.getLocation().getChunk());
             MyChunkChunk fromChunk = new MyChunkChunk(event.getDamager().getLocation().getChunk());
            
            if (!chunk.getAllowMobs() || !fromChunk.getAllowMobs()) {
                if (event.getDamager() instanceof Player) {
                    Player damager = (Player)event.getDamager();
                    if (!damager.hasPermission("mychunk.override")) {
                        event.setCancelled(true);
                    }
                }
            }
        } else if (entity instanceof ItemFrame) {
            
            Entity damager = event.getDamager();
            Chunk chunk = entity.getLocation().getChunk();
            
            if (damager instanceof Player) {

                if (!MyChunkChunk.isAllowed(chunk, (Player)damager, "D") && !WorldGuardHook.isRegion(event.getEntity().getLocation()) && !FactionsHook.isClaimed(event.getEntity().getLocation()) && !TownyHook.isClaimed(event.getEntity().getLocation())) {

                    ((Player)damager).sendMessage(ChatColor.RED + Lang.get("NoPermsBreak"));
                    event.setCancelled(true);

                }

            } else {
                if (MyChunkChunk.isClaimed(chunk)) {

                    event.setCancelled(true);

                }
            }
            
        }
        
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerEmptyBucket (PlayerBucketEmptyEvent event) {
        
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Material bucket = event.getBucket();
        Block block = event.getBlockClicked();
        BlockFace face = event.getBlockFace();
        Block targetBlock;
        String flag;
        String lang;
        
        if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN)|| face.equals(BlockFace.SELF)) {
            targetBlock = block;
        } else {
            targetBlock = block.getRelative(face);
        }
        
        Chunk chunk = targetBlock.getChunk();
        
        if (!MyChunk.isWorldEnabled(event.getPlayer().getWorld().getName()) && !MyChunkChunk.getOwner(targetBlock.getChunk()).equalsIgnoreCase("Server")) return;
        
        if (bucket == Material.LAVA_BUCKET) {
            flag = "L";
            lang = "NoPermsLava";
        } else {
            flag = "W";
            lang = "NoPermsWater";
        }
        
        if (!MyChunkChunk.isAllowed(chunk, player, flag) && !WorldGuardHook.isRegion(block.getLocation()) && !FactionsHook.isClaimed(block.getLocation()) && !TownyHook.isClaimed(block.getLocation())) {
            
            player.sendMessage(ChatColor.RED + Lang.get(lang));
            event.setCancelled(true);
            player.setItemInHand(new ItemStack(bucket, 1));
            
        }
        
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerInteract (PlayerInteractEvent event) {
        
        if (event.isCancelled()) return;
        if (!MyChunk.isWorldEnabled(event.getPlayer().getWorld().getName()) && (event.getClickedBlock() != null && !MyChunkChunk.getOwner(event.getClickedBlock().getLocation().getChunk()).equalsIgnoreCase("Server"))) return;
        
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            
            Block block = event.getClickedBlock();
            Chunk chunk = block.getChunk();
            
            if (!MyChunkChunk.isClaimed(chunk) && !MyChunk.getToggle("protectUncliamed")) return;
            
            Player player = event.getPlayer();
            if (block.getType().equals(Material.NETHERRACK) && block.getRelative(BlockFace.UP) != null && block.getRelative(BlockFace.UP).getType().equals(Material.FIRE)) {
                
                // Extinguish Fire
                
                if (!MyChunkChunk.isAllowed(chunk, player, "I")) {
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsFire"));
                    event.setCancelled(true);
                        
                }
                
            } else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.TRAP_DOOR || block.getType() == Material.FENCE_GATE) {
                
                // Door
                
                if (!MyChunkChunk.isAllowed(chunk, player, "O") && getLeaseOwner(event.getClickedBlock()).equals("")) {
                    
                    // ANY door
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsDoor"));
                    OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(MyChunkChunk.getOwner(chunk));
                    
                    if (owner.isOnline() && MyChunk.getToggle("ownerNotifications")) {
                        owner.getPlayer().sendMessage(ChatColor.GOLD + Lang.get("NoPermsDoorOwner"));
                    }
                    
                    event.setCancelled(true);
                
                } else {
                    
                    // Leased Door
                    
                    String owner = getLeaseOwner(event.getClickedBlock());
                    
                    if (!owner.equals("") && !owner.equalsIgnoreCase(player.getName())) {
                        
                        if (!player.hasPermission("mychunk.bypass")) {
                            player.sendMessage(ChatColor.RED + Lang.get("NoPermsDoor"));
                            event.setCancelled(true);
                        }
                        
                    }
                    
                }
                
            } else if (block.getType() == Material.STONE_BUTTON || block.getType() == Material.WOOD_BUTTON) {
                
                // Button
                
                if (!MyChunkChunk.isAllowed(chunk, player, "U")) {
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsButton"));
                    OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(MyChunkChunk.getOwner(chunk));
                    
                    if (owner.isOnline() && MyChunk.getToggle("ownerNotifications")) {
                        owner.getPlayer().sendMessage(ChatColor.GOLD + Lang.get("NoPermsButtonOwner"));
                    }
                    
                    event.setCancelled(true);
                    
                }
                
            } else if (block.getType() == Material.LEVER) {
                
                // Lever
                
                if (!MyChunkChunk.isAllowed(chunk, player, "U")) {
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsLever"));
                    OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(MyChunkChunk.getOwner(chunk));
                    
                    if (owner.isOnline() && MyChunk.getToggle("ownerNotifications")) {
                        owner.getPlayer().sendMessage(ChatColor.GOLD + Lang.get("NoPermsLeverOwner"));
                    }
                    
                    event.setCancelled(true);
                    
                }
                
            } else if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                
                // Chest
                
                if (!MyChunkChunk.isAllowed(chunk, player, "C")) {
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsChest"));
                    OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(MyChunkChunk.getOwner(chunk));
                    
                    if (owner.isOnline() && MyChunk.getToggle("ownerNotifications")) {
                        owner.getPlayer().sendMessage(ChatColor.GOLD + Lang.get("NoPermsChestOwner"));
                    }
                    
                    event.setCancelled(true);
                    
                }
                
            } else if (block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE || block.getType() == Material.DISPENSER || block.getType() == Material.ENCHANTMENT_TABLE || block.getType() == Material.BREWING_STAND || block.getType() == Material.CAULDRON || block.getType() == Material.ENDER_PORTAL_FRAME || block.getType() == Material.COMMAND || block.getType() == Material.BEACON || block.getType() == Material.FLOWER_POT || block.getType() == Material.ANVIL || block.getType() == Material.HOPPER || block.getType() == Material.DROPPER || block.getType() == Material.CAKE_BLOCK) {
                
                // Special Block
                
                if (!MyChunkChunk.isAllowed(chunk, player, "S")) {
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsSpecial"));
                    OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(MyChunkChunk.getOwner(chunk));
                    
                    if (owner.isOnline() && MyChunk.getToggle("ownerNotifications")) {
                        owner.getPlayer().sendMessage(ChatColor.GOLD + Lang.get("NoPermsSpecialOwner"));
                    }
                    
                    event.setCancelled(true);
                    
                }
                
            } else if (block.getType() == Material.DIODE_BLOCK_OFF || block.getType() == Material.DIODE_BLOCK_ON || block.getType() == Material.REDSTONE_COMPARATOR_OFF || block.getType() == Material.REDSTONE_COMPARATOR_ON) {
                
                // Redstone Repeaters/Comparators
                
                if (!MyChunkChunk.isAllowed(chunk, player, "B")) {
                    
                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsBuild"));
                    event.setCancelled(true);
                    
                }
                
            } else if (block.getType().equals(Material.WALL_SIGN)) {
                
                // Lease Signs
                
                Sign sign = (Sign)block.getState();
                
                if (sign.getLine(0).equalsIgnoreCase("[Lease]")) {
                    
                    if (!getLeaseOwner(getAttachedBlock(block).getRelative(BlockFace.DOWN)).equals("")) {
                        
                        player.sendMessage(ChatColor.GOLD + "Door is leased by " + ChatColor.WHITE + getLeaseOwner(block));
                        event.setCancelled(true);
                        
                    } else {
                        
                        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                            
                            if(!MyChunk.getToggle("foundEconomy")) {
                                player.sendMessage(ChatColor.RED + "[Lease] signs require an economy plugin!");
                                event.setCancelled(true);
                                return;
                            }
                        
                            double price = Double.parseDouble(sign.getLine(1));
                            
                            if (leaseConfirm.containsKey(player.getName()) && leaseConfirm.get(player.getName()).equals(block)) {

                                // Confirm Purchase
                                
                                leaseConfirm.remove(player.getName());
                                
                                if (!(MyChunkVaultLink.economy.getBalance(player.getName()) >= price)) {
                                    player.sendMessage(ChatColor.RED + "You cannot afford this Lease!");
                                    event.setCancelled(true);
                                    return;
                                }
                                
                                MyChunkVaultLink.economy.withdrawPlayer(player.getName(), price);
                                
                                sign.setLine(2, ChatColor.GREEN + player.getName());
                                Date date = new Date();
                                Calendar c = Calendar.getInstance();
                                c.setTime(date);
                                c.add(Calendar.DATE, 7);
                                String newDate = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
                                sign.setLine(3, newDate);
                                sign.update();
                                
                                player.sendMessage(ChatColor.GOLD + "You have leased this door for 7 days for "+ChatColor.WHITE + MyChunkVaultLink.economy.format(price) + ChatColor.GOLD+"!");

                            } else {

                                // Unconfirmed, Add

                                player.sendMessage(ChatColor.GOLD + "Right-Click again to confirm Lease Purchase for " + ChatColor.WHITE + MyChunkVaultLink.economy.format(price));
                                leaseConfirm.put(player.getName(), block);

                            }
                        }
                        
                    }
                    
                }
                
            }
            
        } else if (event.getAction().equals(Action.PHYSICAL)) {
            
            // Pressure Plates and Crop Trample
            
            Player player = event.getPlayer();
            Block block = player.getLocation().getBlock();
            Chunk chunk = block.getChunk();
            
            if (!MyChunkChunk.isClaimed(chunk) && !MyChunk.getToggle("protectUnclaimed")) return;
            
            if ((block.getType() == Material.CROPS || block.getType() ==  Material.SOIL || block.getType() ==  Material.CARROT || block.getType() ==  Material.POTATO || (block.getType() ==  Material.AIR && block.getRelative(BlockFace.DOWN).getType().equals(Material.SOIL)))) {
                event.setCancelled(true);
            } else if (!MyChunkChunk.isAllowed(chunk, player, "U")) {
                
                event.setCancelled(true);
                
            }
        }
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
            
        if (MyChunk.getToggle("useClaimExpiry")) {
            MyChunkChunk.refreshOwnership(event.getPlayer().getName());
        }
        
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerMove (PlayerMoveEvent event) {
        
        if (event.isCancelled()) return;
        if (!MyChunk.isWorldEnabled(event.getPlayer().getWorld().getName()) && !MyChunkChunk.getOwner(event.getTo().getChunk()).equalsIgnoreCase("Server") && !MyChunkChunk.getOwner(event.getFrom().getChunk()).equalsIgnoreCase("Server")) return;
        
        Location fromLoc = event.getFrom();
        Location toLoc = event.getTo();
        
        if (TownyHook.foundTowny() && TownyHook.isClaimed(toLoc)) {
            return;
        }
        
        if (fromLoc.getChunk() != toLoc.getChunk()) {
            
            String message = "";
            MyChunkChunk fromChunk = new MyChunkChunk(fromLoc.getChunk());
            MyChunkChunk toChunk = new MyChunkChunk(toLoc.getChunk());
            Player player = event.getPlayer();
            String fromChunkOwner = fromChunk.getOwner();
            String toChunkOwner = toChunk.getOwner();
            
            done:
            if (!fromChunkOwner.equalsIgnoreCase(toChunkOwner)) {
                
                if (MyChunk.getToggle("preventEntry")) {
                    if (toChunk.isClaimed() && !toChunk.getOwner().equalsIgnoreCase("Public") && !toChunk.getOwner().equalsIgnoreCase(player.getName()) && !toChunk.isAllowed(player.getName(), "E") && !toChunk.isForSale() && !player.hasPermission("mychunk.override")) {
                        if (!toChunk.getOwner().equalsIgnoreCase("Server") ^ !player.hasPermission("mychunk.server.entry")) {
                            message += toChunk.getOwner() + " " + ChatColor.RED + Lang.get("NoEntry");
                            event.setCancelled(true);
                            break done;
                        }
                    }
                }
                
                if (toChunkOwner.equals("")) {
                    message += ChatColor.GRAY + "~"+Lang.get("Unowned");
                    break done;
                }
                
                String forSale = "";
                
                if (MyChunk.getToggle("foundEconomy")) {
                    
                    if (toChunk.isForSale()) {
                        
                        double claimPrice = toChunk.getClaimPrice();
                        int playerClaimed = MyChunkChunk.getOwnedChunkCount(player.getName());
                        
                        if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                            int ramp = playerClaimed;
                            if (MyChunk.getToggle("firstChunkFree") && playerClaimed > 0) {
                                ramp--;
                            }
                            claimPrice += MyChunk.getDoubleSetting("priceRampRate") * ramp;
                        }
                        
                        claimPrice = toChunk.getClaimPrice();
                        
                        if (claimPrice != 0) {

                            forSale = ChatColor.YELLOW + " ["+Lang.get("ChunkForSale")+ ": " + MyChunkVaultLink.economy.format(claimPrice) + "]";

                        }
                    }
                }
                
                if (toChunkOwner.equalsIgnoreCase("server")) {
                    message += ChatColor.LIGHT_PURPLE + "~"+Lang.get("Server") + forSale;
                } else if (toChunkOwner.equalsIgnoreCase("public")) {
                    message += ChatColor.GREEN + "~"+Lang.get("Public") + forSale;
                } else {
                    
                    ChatColor color = ChatColor.GOLD;
                    
                    if (toChunk.isGangland()) {
                        
                        if (GangLands.isGangMemberOf(player, toChunk.getGangName())) {
                            color = ChatColor.DARK_GREEN;
                        } else {
                            color = ChatColor.DARK_RED;
                        }
                        
                    }
                    
                    message += color + "~" + toChunkOwner + forSale;
                }
                
            } else if (!toChunkOwner.equals("")) {
                
                if (!toChunk.isGangland() && MyChunk.getToggle("preventEntry")) {
                    if (toChunk.isClaimed() && !toChunk.getOwner().equalsIgnoreCase("Public") && !toChunk.getOwner().equalsIgnoreCase(player.getName()) && !toChunk.isAllowed(player.getName(), "E") && !toChunk.isForSale() && !player.hasPermission("mychunk.override")) {
                        if (!toChunk.getOwner().equalsIgnoreCase("Server") ^ !player.hasPermission("mychunk.server.entry")) {
                            message += toChunk.getOwner() + " " + ChatColor.RED + Lang.get("NoEntry");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                
                if (MyChunk.getToggle("foundEconomy")) {
                    if (toChunk.isForSale()) {
                        
                        double claimPrice = toChunk.getClaimPrice();
                        int playerClaimed = MyChunkChunk.getOwnedChunkCount(player.getName());
                        
                        if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                            int ramp = playerClaimed;
                            if (MyChunk.getToggle("firstChunkFree") && playerClaimed > 0) {
                                ramp--;
                            }
                            claimPrice += MyChunk.getDoubleSetting("priceRampRate") * ramp;
                        }

                        if (claimPrice == 0) {
                            return;
                        }

                        String forSale = ChatColor.YELLOW + "["+Lang.get("ChunkForSale") + ": " + MyChunkVaultLink.economy.format(claimPrice) + "]";

                        message += forSale;
                    }
                }
                
            }
            
            if (!MyChunk.getToggle("preventPVP") && fromChunk.getAllowPVP() != toChunk.getAllowPVP()) {
                if (toChunk.getAllowPVP()) {
                    message += ChatColor.DARK_RED + " " + Lang.get("PVP");
                } else {
                    message += ChatColor.GREEN + " " + Lang.get("NoPVP");
                }
            }
            
            if (fromChunk.getAllowMobs() != toChunk.getAllowMobs()) {
                if (toChunk.getAllowMobs()) {
                    message += ChatColor.DARK_RED + " "+Lang.get("Mobs");
                } else {
                    message += ChatColor.GREEN + " "+Lang.get("NoMobs");
                }
            }
            
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
            
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        
        if (MyChunk.getToggle("useClaimExpiry")) {
            MyChunkChunk.refreshOwnership(event.getPlayer().getName());
        }
        
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        
        if (event.isCancelled()) return;
        if (!MyChunk.isWorldEnabled(event.getPlayer().getWorld().getName()) && !MyChunkChunk.getOwner(event.getTo().getChunk()).equalsIgnoreCase("Server") && !MyChunkChunk.getOwner(event.getFrom().getChunk()).equalsIgnoreCase("Server")) return;
        
        Location fromLoc = event.getFrom();
        Location toLoc = event.getTo();
        
        if (fromLoc.getChunk() != toLoc.getChunk()) {
            
            MyChunkChunk fromChunk = new MyChunkChunk(fromLoc.getChunk());
            MyChunkChunk toChunk = new MyChunkChunk(toLoc.getChunk());
            Player player = event.getPlayer();
            String fromChunkOwner = fromChunk.getOwner();
            String toChunkOwner = toChunk.getOwner();
            
            if (!fromChunkOwner.equalsIgnoreCase(toChunkOwner)) {
                
                if (toChunkOwner.equals("")) {
                    player.sendMessage(ChatColor.GRAY + "~"+Lang.get("Unowned"));
                    return;
                }
                
                String forSale = "";
                
                if (MyChunk.getToggle("foundEconomy")) {
                    
                    double claimPrice = MyChunkChunk.getClaimPrice(toLoc.getChunk(), player);
                    
                    if (claimPrice != 0) {
                        
                        forSale = ChatColor.YELLOW + " ["+Lang.get("ChunkForSale")+ ": " + MyChunkVaultLink.economy.format(claimPrice) + "]";

                    }
                }
                
                if (toChunkOwner.equalsIgnoreCase("server")) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "~"+Lang.get("Server") + forSale);
                } else if (toChunkOwner.equalsIgnoreCase("public")) {
                    player.sendMessage(ChatColor.GREEN + "~"+Lang.get("Public") + forSale);
                } else {
                    
                    ChatColor color = ChatColor.GOLD;
                    
                    if (toChunk.isGangland()) {
                        if (GangLands.isGangMemberOf(player, toChunk.getGangName())) {
                            color = ChatColor.DARK_GREEN;
                        } else {
                            color = ChatColor.DARK_RED;
                        }
                    }
                    
                    player.sendMessage(color + "~" + toChunkOwner + forSale);
                }
                
            } else if (!toChunkOwner.equals("")) {
                
                if (MyChunk.getToggle("foundEconomy")) {

                    double claimPrice = MyChunkChunk.getClaimPrice(toLoc.getChunk(), player);

                    if (claimPrice == 0) {
                        return;
                    }

                    String forSale = ChatColor.YELLOW + "["+Lang.get("ChunkForSale") + ": " + MyChunkVaultLink.economy.format(claimPrice) + "]";

                    player.sendMessage(forSale);
                }
                
            }
            
        }
        
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
        
        if (event.isCancelled()) return;
        if (!MyChunk.isWorldEnabled(event.getPlayer().getWorld().getName()) && !MyChunkChunk.getOwner(event.getRightClicked().getLocation().getChunk()).equalsIgnoreCase("Server")) return;
        
        Entity target = event.getRightClicked();
        
        if (target instanceof Animals) {
            Player player = event.getPlayer();
            Chunk chunk = target.getLocation().getChunk();
            String chunkOwner = MyChunkChunk.getOwner(chunk);
            if (!player.getName().equalsIgnoreCase(chunkOwner) && !MyChunkChunk.isAllowed(chunk, player, "A")) {
                player.sendMessage(ChatColor.RED + Lang.get("NoPermsAnimals"));
                event.setCancelled(true);
            }
            
        }
        
        if (target instanceof ItemFrame) {
            Player player = event.getPlayer();
            Chunk chunk = target.getLocation().getChunk();
            String chunkOwner = MyChunkChunk.getOwner(chunk);
            if (!player.getName().equalsIgnoreCase(chunkOwner) && !MyChunkChunk.isAllowed(chunk, player, "U")) {
                event.setCancelled(true);
            }
        }
        
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerMount(VehicleEnterEvent event) {
        
        if (event.isCancelled()) return;
        if (!MyChunk.isWorldEnabled(event.getVehicle().getWorld().getName()) && !MyChunkChunk.getOwner(event.getVehicle().getLocation().getChunk()).equalsIgnoreCase("Server")) return;
        
        Entity target = event.getVehicle();

        if (target instanceof Animals) {
            Player player = (Player)event.getEntered();
            Chunk chunk = target.getLocation().getChunk();
            String chunkOwner = MyChunkChunk.getOwner(chunk);
            if (!player.getName().equalsIgnoreCase(chunkOwner) && !MyChunkChunk.isAllowed(chunk, player, "A")) {
                player.sendMessage(ChatColor.RED + Lang.get("NoPermsAnimals"));
                event.setCancelled(true);
            }

        }
        
    }
    
    private static String getLeaseOwner(Block door) {
        
        if (door.getType() != Material.WOODEN_DOOR && door.getType() != Material.TRAP_DOOR && door.getType() != Material.FENCE_GATE) {
            return "";
        }
        
        Block aboveDoor = door.getRelative(BlockFace.UP);
                    
        while (aboveDoor.getType() == Material.WOODEN_DOOR || aboveDoor.getType() == Material.TRAP_DOOR || aboveDoor.getType() == Material.FENCE_GATE) {
            aboveDoor = aboveDoor.getRelative(BlockFace.UP);
        }
        
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face: faces) {
            
            Block block = aboveDoor.getRelative(face);
            BlockState state = block.getState();

            if (state instanceof Sign) {
                Sign sign = (Sign)state;
                if (sign.getLine(0).equalsIgnoreCase("[Lease]")) {
                    String[] line2 = sign.getLine(3).split("-");
                    int day = Integer.parseInt(line2[0]);
                    int month = Integer.parseInt(line2[1]);
                    int year = Integer.parseInt(line2[2]);
                    if (year >= Calendar.getInstance().get(Calendar.YEAR) || month >= Calendar.getInstance().get(Calendar.MONTH) || day >= Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                        return ChatColor.stripColor(sign.getLine(2));
                    } else {
                        sign.setLine(2, "");
                        sign.update();
                    }
                }
            }
            
        }
        
        return "";
    }
    
    private static Block getAttachedBlock(Block b) {
        MaterialData m = b.getState().getData();
        BlockFace face = BlockFace.DOWN;
        if (m instanceof Attachable) {
            face = ((Attachable) m).getAttachedFace();
        }
        return b.getRelative(face);
    }
    
    
}
