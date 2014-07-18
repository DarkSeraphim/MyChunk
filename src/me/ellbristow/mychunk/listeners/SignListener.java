package me.ellbristow.mychunk.listeners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import me.ellbristow.mychunk.MyChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.utils.FactionsHook;
import me.ellbristow.mychunk.utils.MyChunkVaultLink;
import me.ellbristow.mychunk.utils.TownyHook;
import me.ellbristow.mychunk.utils.WorldGuardHook;
import me.ellbristow.mychunk.utils.db.SQLBridge;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

public class SignListener implements Listener {

    public static HashMap<String, Block> pendingAreas = new HashMap<String, Block>();
    public static HashMap<String, Block> pendingUnclaims = new HashMap<String, Block>();

    public SignListener() {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {

        if (event.isCancelled()) {
            return;
        }
        if (!MyChunk.isWorldEnabled(event.getBlock().getWorld().getName()) && !MyChunkChunk.getOwner(event.getBlock().getChunk()).equalsIgnoreCase("Server")) return;

        String line0 = event.getLine(0);
        String line1 = event.getLine(1);

        if (line0.equalsIgnoreCase("[claim]")) {

            // Player attempted to claim a chunk
            Player player = event.getPlayer();
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block);

            if (!player.hasPermission("mychunk.claim") && !player.hasPermission("mychunk.claim.server") && !player.hasPermission("mychunk.claim.public")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaim"));
                breakSign(block);
                return;

            } else if (chunk.isClaimed()) {

                String owner = chunk.getOwner();

                if (owner.equalsIgnoreCase(player.getName())) {

                    player.sendMessage(ChatColor.RED + Lang.get("AlreadyOwner"));
                    breakSign(block);
                    return;

                } else if (!chunk.isForSale()) {

                    player.sendMessage(ChatColor.RED + Lang.get("AlreadyOwned") + " " + ChatColor.WHITE + owner + ChatColor.RED + "!");
                    breakSign(block);
                    return;

                } else if (chunk.isForSale() && !player.hasPermission("mychunk.buy")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsBuyOwned"));
                    breakSign(block);
                    return;

                }

            } else if (!MyChunk.getToggle("allowNether") && player.getWorld().getEnvironment().equals(Environment.NETHER)) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsNether"));
                breakSign(block);
                return;

            } else if (!MyChunk.getToggle("allowEnd") && player.getWorld().getEnvironment().equals(Environment.THE_END)) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsEnd"));
                breakSign(block);
                return;

            } else if (FactionsHook.isClaimed(block.getLocation())) {
                
                player.sendMessage(ChatColor.RED + Lang.get("FactionsClash"));
                breakSign(block);
                return;
                
            } else if (TownyHook.isClaimed(block.getLocation())) {
                
                player.sendMessage(ChatColor.RED + Lang.get("TownyClash"));
                breakSign(block);
                return;
                
            }

            int playerMax = MyChunk.getMaxChunks(player);
            int playerClaimed = MyChunkChunk.getOwnedChunkCount(player.getName());
            boolean isOverbuy = false;
            
            if (playerMax != 0 && playerClaimed >= playerMax) {
                isOverbuy = true;
            }

            if (isOverbuy && (!MyChunk.getToggle("allowOverbuy") || !player.hasPermission("mychunk.claim.overbuy"))) {

                player.sendMessage(ChatColor.RED + Lang.get("MaxChunksReached") + " (" + playerMax + ")!");
                breakSign(block);
                return;

            }
            
            double claimPrice = 0;
            boolean isFreeChunk = false;
            
            if (MyChunk.getToggle("foundEconomy")) {
                
                if (!line1.equalsIgnoreCase("server") && !line1.equalsIgnoreCase("public") && !player.hasPermission("mychunk.free") && !(playerClaimed == 0 && MyChunk.getToggle("firstChunkFree"))) {
                    if (!isOverbuy) {

                        claimPrice = chunk.getClaimPrice();

                    } else {

                        claimPrice = chunk.getOverbuyPrice();
                        
                    }
                    
                    if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                        int ramp = playerClaimed;
                        if (MyChunk.getToggle("firstChunkFree") && playerClaimed > 0) {
                            ramp--;
                        }
                        claimPrice += MyChunk.getDoubleSetting("priceRampRate") * ramp;
                    }
                } else if (MyChunk.getToggle("firstChunkFree") && playerClaimed == 0)  {
                    isFreeChunk = true;
                }
            }
            
            if (claimPrice != 0 && MyChunkVaultLink.economy.getBalance(player.getName()) < claimPrice) {

                player.sendMessage(ChatColor.RED + Lang.get("CantAfford") + " (" + Lang.get("Price") + ": " + ChatColor.WHITE + MyChunkVaultLink.economy.format(claimPrice) + ChatColor.RED + ")!");
                breakSign(block);
                return;

            }

            if (line1.equals("") || line1.equalsIgnoreCase(player.getName())) {

                if (!MyChunk.getToggle("allowNeighbours") && chunk.hasNeighbours() && !chunk.isForSale()) {
                    
                    HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("owner", "MyChunks", "world = '" + chunk.getWorldName() + "' AND ((x = " + chunk.getX() + "+1 AND z = " + chunk.getZ() + ") OR (x = " + chunk.getX() + "-1 AND z = " + chunk.getZ() + ") OR (x = " + chunk.getX() + " AND z = " + chunk.getZ() + "+1) OR (x = " + chunk.getX() + " AND z = " + chunk.getZ() + "-1))", "", "");
                    
                    if (results != null && !results.isEmpty()) {
                        
                        for (int i : results.keySet()) {
                            
                            if (results.get(i).get("owner").equalsIgnoreCase(player.getName()) || results.get(i).get("owner").equalsIgnoreCase("Server") || results.get(i).get("owner").equalsIgnoreCase("Public")) {
                                continue;
                            }
                            
                            player.sendMessage(ChatColor.RED + Lang.get("NoNeighbours"));
                            breakSign(block);

                            return;
                            
                        }
                            
                    }
                    
                }

                if (claimPrice != 0 && !isFreeChunk) {

                    if (!(MyChunk.getToggle("firstChunkFree") && playerClaimed == 0) || chunk.isForSale()) {
                        MyChunkVaultLink.economy.withdrawPlayer(player.getName(), claimPrice);
                        player.sendMessage(MyChunkVaultLink.economy.format(claimPrice) + ChatColor.GOLD + " " + Lang.get("AmountDeducted"));
                    }

                } else if (isFreeChunk) {
                    player.sendMessage(ChatColor.GOLD + " " + Lang.get("FirstChunkFree"));
                }

                if (chunk.isForSale()) {
                    
                    if (claimPrice != 0) {
                        MyChunkVaultLink.economy.depositPlayer(chunk.getOwner(), claimPrice);
                    }
                    OfflinePlayer oldOwner = Bukkit.getServer().getOfflinePlayer(chunk.getOwner());

                    if (oldOwner.isOnline()) {
                        if (claimPrice != 0) {
                            oldOwner.getPlayer().sendMessage(player.getName() + ChatColor.GOLD + " " + Lang.get("BoughtFor") + " " + ChatColor.WHITE + MyChunkVaultLink.economy.format(claimPrice) + ChatColor.GOLD + "!");
                        } else {
                            oldOwner.getPlayer().sendMessage(player.getName() + ChatColor.GOLD + " " + Lang.get("ClaimedYourChunk") + "!");
                        }
                    }

                }

                chunk.claim(player.getName(), "");
                player.sendMessage(ChatColor.GOLD + Lang.get("ChunkClaimed"));


                breakSign(block);

            } else {

                String correctName;

                if (line1.equalsIgnoreCase("server")) {

                    if (!player.hasPermission("mychunk.claim.server")) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimServer"));
                        breakSign(block);
                        return;

                    } else {
                        correctName = "Server";
                    }

                } else if (line1.equalsIgnoreCase("public")) {

                    if (!player.hasPermission("mychunk.claim.public")) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimPublic"));
                        breakSign(block);
                        return;

                    } else {
                        correctName = "Public";
                    }

                } else if (player.hasPermission("mychunk.claim.others")) {

                    if (!MyChunk.getToggle("allowNeighbours") && chunk.hasNeighbours() && !chunk.isForSale()) {

                        MyChunkChunk[] neighbours = chunk.getNeighbours();

                        if ((neighbours[0].isClaimed() && !neighbours[0].getOwner().equalsIgnoreCase(line1) && !neighbours[0].getOwner().equalsIgnoreCase(player.getName())) || (neighbours[1].isClaimed() && !neighbours[1].getOwner().equalsIgnoreCase(line1) && !neighbours[1].getOwner().equalsIgnoreCase(player.getName())) || (neighbours[2].isClaimed() && !neighbours[2].getOwner().equalsIgnoreCase(line1) && !neighbours[2].getOwner().equalsIgnoreCase(player.getName())) || (neighbours[3].isClaimed() && !neighbours[3].getOwner().equalsIgnoreCase(line1) && !neighbours[3].getOwner().equalsIgnoreCase(player.getName()))) {

                            player.sendMessage(ChatColor.RED + Lang.get("NoNeighbours"));
                            breakSign(block);
                            return;

                        }

                    }

                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);

                    if (!target.hasPlayedBefore() && !target.isOnline()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        breakSign(block);
                        return;

                    } else {
                        correctName = target.getName();
                    }

                } else {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimOther"));
                    breakSign(block);
                    return;

                }

                chunk.claim(correctName, "");
                player.sendMessage(ChatColor.GOLD + Lang.get("ChunkClaimedFor") + " " + ChatColor.WHITE + correctName + ChatColor.GOLD + "!");

                if (claimPrice != 0 && !correctName.equalsIgnoreCase("server") && !correctName.equalsIgnoreCase("public")) {
                    
                    if (!isFreeChunk) {
                        MyChunkVaultLink.economy.withdrawPlayer(player.getName(), claimPrice);
                        player.sendMessage(MyChunkVaultLink.economy.format(claimPrice) + ChatColor.GOLD + " " + Lang.get("AmountDeducted"));
                    } else {
                        player.sendMessage(ChatColor.GOLD + " " + Lang.get("FirstChunkFree"));
                    }

                }

                breakSign(block);

            }

        } else if (line0.equalsIgnoreCase("[ClaimArea]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            if (!player.hasPermission("mychunk.claim")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaim"));
                breakSign(block);
                return;

            } else if (!player.hasPermission("mychunk.claim.area")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimArea"));
                breakSign(block);
                return;

            } else if (!MyChunk.getToggle("allowNether") && block.getWorld().getEnvironment().equals(Environment.NETHER)) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsNether"));
                breakSign(block);
                return;

            } else if (!MyChunk.getToggle("allowEnd") && block.getWorld().getEnvironment().equals(Environment.THE_END)) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsEnd"));
                breakSign(block);
                return;

            }

            String correctName;

            if (line1.isEmpty() || line1.equalsIgnoreCase(player.getName())) {

                correctName = player.getName();

            } else {

                if (line1.equalsIgnoreCase("Server")) {

                    if (!player.hasPermission("mychunk.claim.server")) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimServer"));
                        breakSign(block);
                        return;

                    } else {
                        correctName = "Server";
                    }

                } else if (line1.equalsIgnoreCase("Public")) {

                    if (!player.hasPermission("mychunk.claim.public")) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimPublic"));
                        breakSign(block);
                        return;

                    } else {
                        correctName = "Public";
                    }

                } else {

                    if (player.hasPermission("mychunk.claim.others")) {

                        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);
                        if (!target.hasPlayedBefore()) {

                            player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                            breakSign(block);
                            return;

                        } else {
                            correctName = target.getName();
                        }

                    } else {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimOther"));
                        breakSign(block);
                        return;

                    }
                }
            }

            if (event.getLine(2).equalsIgnoreCase("cancel")) {

                pendingAreas.remove(correctName);
                player.sendMessage(ChatColor.RED + Lang.get("ClaimAreaCancelled"));
                breakSign(block);
                return;

            }

            if (!pendingAreas.containsKey(correctName)) {

                pendingAreas.put(correctName, event.getBlock());
                player.sendMessage(ChatColor.GOLD + Lang.get("StartClaimArea1"));
                player.sendMessage(ChatColor.GOLD + Lang.get("StartClaimArea2"));

            } else {

                Block startBlock = pendingAreas.get(correctName);

                if (startBlock.getWorld() != block.getWorld()) {
                    player.sendMessage(ChatColor.RED + Lang.get("ClaimAreaWorldError"));
                    breakSign(block);
                    return;
                }

                Chunk startChunk = startBlock.getChunk();
                pendingAreas.remove(correctName);
                Chunk endChunk = block.getChunk();
                int startX;
                int startZ;
                int endX;
                int endZ;

                if (startChunk.getX() <= endChunk.getX()) {

                    startX = startChunk.getX();
                    endX = endChunk.getX();

                } else {

                    startX = endChunk.getX();
                    endX = startChunk.getX();

                }

                if (startChunk.getZ() <= endChunk.getZ()) {

                    startZ = startChunk.getZ();
                    endZ = endChunk.getZ();

                } else {

                    startZ = endChunk.getZ();
                    endZ = startChunk.getZ();

                }

                boolean foundClaimed = false;
                boolean foundNeighbour = false;
                List<MyChunkChunk> foundChunks = new ArrayList<MyChunkChunk>();
                int chunkCount = 0;
                xloop:
                for (int x = startX; x <= endX; x++) {

                    for (int z = startZ; z <= endZ; z++) {

                        if (chunkCount < 64) {

                            MyChunkChunk myChunk = new MyChunkChunk(block.getWorld().getName(), x, z);

                            if ((myChunk.isClaimed() && !myChunk.getOwner().equalsIgnoreCase(correctName) && !myChunk.isForSale()) || FactionsHook.isClaimed(block.getLocation()) || TownyHook.isClaimed(block.getLocation())) {

                                foundClaimed = true;
                                break xloop;

                            } else if (myChunk.hasNeighbours()) {

                                MyChunkChunk[] neighbours = myChunk.getNeighbours();

                                for (MyChunkChunk neighbour : neighbours) {

                                    if (neighbour.isClaimed() && !neighbour.getOwner().equalsIgnoreCase(correctName) && !neighbour.getOwner().equalsIgnoreCase(player.getName()) && !neighbour.getOwner().equalsIgnoreCase("Server") && !neighbour.getOwner().equalsIgnoreCase("Public") && !myChunk.isForSale()) {

                                        foundNeighbour = true;
                                        if (!MyChunk.getToggle("allowNeighbours")) {
                                            break xloop;
                                        }

                                    }

                                }
                            }

                            foundChunks.add(myChunk);
                            chunkCount++;

                        } else {

                            player.sendMessage(ChatColor.RED + Lang.get("AreaTooBig"));
                            breakSign(block);
                            return;

                        }

                    }

                }

                if (foundClaimed) {

                    player.sendMessage(ChatColor.RED + Lang.get("FoundClaimedInArea"));
                    breakSign(block);
                    return;

                }
                if (foundNeighbour && !MyChunk.getToggle("allowNeighbours")) {

                    player.sendMessage(ChatColor.RED + Lang.get("FoundNeighboursInArea"));
                    breakSign(block);
                    return;

                }

                int claimed = MyChunkChunk.getOwnedChunkCount(correctName);
                int max = MyChunk.getMaxChunks(player);

                if (max != 0 && (!MyChunk.getToggle("allowOverbuy") || !player.hasPermission("mychunk.claim.overbuy")) && max - claimed < foundChunks.size()) {

                    player.sendMessage(ChatColor.RED + (correctName.equalsIgnoreCase(player.getName()) ? "You" : correctName) + Lang.get("ClaimAreaTooLarge"));
                    player.sendMessage(ChatColor.RED + Lang.get("ChunksOwned") + ": " + ChatColor.WHITE + claimed);
                    player.sendMessage(ChatColor.RED + Lang.get("ChunkMax") + ": " + ChatColor.WHITE + max);
                    player.sendMessage(ChatColor.RED + Lang.get("ChunksInArea") + ": " + chunkCount);
                    breakSign(block);
                    return;

                }

                int allowance = max - claimed;
                if (allowance < 0) {
                    allowance = 0;
                }

                if (MyChunk.getToggle("foundEconomy") && !player.hasPermission("mychunk.free") && !correctName.equalsIgnoreCase("Server")&& !correctName.equalsIgnoreCase("Public")) {

                    double areaPrice = 0;

                    for (MyChunkChunk myChunk : foundChunks) {
                        
                        if (allowance > 0) {
                            if (!(MyChunk.getToggle("firstChunkFree") && MyChunkChunk.getOwnedChunkCount(correctName) == 0)) {
                                areaPrice += myChunk.getClaimPrice();
                                if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                                    for (int i = 0; i < chunkCount; i++) {
                                        areaPrice += MyChunk.getDoubleSetting("priceRampRate");
                                    }
                                }
                            }
                            allowance--;

                        } else {
                            if (!(MyChunk.getToggle("firstChunkFree") && MyChunkChunk.getOwnedChunkCount(correctName) == 0)) {
                                areaPrice += myChunk.getOverbuyPrice();
                                if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                                    for (int i = 0; i < chunkCount; i++) {
                                        areaPrice += MyChunk.getDoubleSetting("priceRampRate");
                                    }
                                }
                            }
                        }

                    }

                    if (MyChunkVaultLink.economy.getBalance(player.getName()) < areaPrice) {

                        player.sendMessage(ChatColor.RED + Lang.get("CantAffordClaimArea"));
                        player.sendMessage(ChatColor.RED + Lang.get("Price") + ": " + ChatColor.WHITE + MyChunkVaultLink.economy.format(areaPrice));
                        breakSign(block);
                        return;

                    }

                    MyChunkVaultLink.economy.withdrawPlayer(player.getName(), areaPrice);
                    player.sendMessage(ChatColor.GOLD + Lang.get("YouWereCharged") + " " + ChatColor.WHITE + MyChunkVaultLink.economy.format(areaPrice));

                }

                for (MyChunkChunk myChunk : foundChunks) {
                    myChunk.claim(correctName, "");
                }

                player.sendMessage(ChatColor.GOLD + Lang.get("ChunksClaimed") + ": " + ChatColor.WHITE + foundChunks.size());

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[unclaim]")) {

            // Player attempted to unclaim a chunk

            Player player = event.getPlayer();
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block);

            if (!chunk.isClaimed()) {

                player.sendMessage(ChatColor.RED + Lang.get("ChunkNotOwned"));
                breakSign(block);
                return;

            }

            String owner = chunk.getOwner();

            if (!owner.equalsIgnoreCase(player.getName())) {

                if (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.unclaim.server")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimServer"));
                    breakSign(block);
                    return;

                } else if (owner.equalsIgnoreCase("public") && !player.hasPermission("mychunk.unclaim.public")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimPublic"));
                    breakSign(block);
                    return;

                } else if (!owner.equalsIgnoreCase("server") && !owner.equalsIgnoreCase("public") && !player.hasPermission("mychunk.unclaim.others")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimOther"));
                    breakSign(block);
                    return;

                }

            }

            chunk.unclaim();

            if (owner.equalsIgnoreCase(player.getName())) {

                player.sendMessage(ChatColor.GOLD + Lang.get("ChunkUnclaimed"));

            } else {

                player.sendMessage(ChatColor.GOLD + Lang.get("ChunkUnclaimedFor") + " " + ChatColor.WHITE + owner + ChatColor.RED + "!");

            }

            if (!owner.equalsIgnoreCase("Server") && !owner.equalsIgnoreCase("Public") && MyChunk.getToggle("unclaimRefund") && !player.hasPermission("mychunk.free")) {
                
                if (!(MyChunk.getToggle("firstChunkFree") && MyChunkChunk.getOwnedChunkCount(player.getName()) == 0)) {
                    double price = MyChunk.getDoubleSetting("chunkPrice");
                    if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                        int claimed = MyChunkChunk.getOwnedChunkCount(player.getName());
                        price += MyChunk.getDoubleSetting("priceRampRate") * claimed;
                    }
                    MyChunkVaultLink.economy.depositPlayer(player.getName(), price / 100 * MyChunk.getDoubleSetting("refundPercent"));
                }

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[unclaimarea]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            String correctName;

            if (line1.isEmpty() || line1.equalsIgnoreCase(player.getName())) {

                correctName = player.getName();

            } else {

                if (line1.equalsIgnoreCase("Server")) {

                    if (!player.hasPermission("mychunk.unclaim.server")) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimServer"));
                        breakSign(block);
                        return;

                    } else {
                        correctName = "Server";
                    }

                } else if (line1.equalsIgnoreCase("Public")) {

                    if (!player.hasPermission("mychunk.unclaim.public")) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimPublic"));
                        breakSign(block);
                        return;

                    } else {
                        correctName = "Public";
                    }

                } else {

                    if (player.hasPermission("mychunk.unclaim.others")) {

                        OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);
                        if (!target.hasPlayedBefore()) {

                            player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                            breakSign(block);
                            return;

                        } else {
                            correctName = target.getName();
                        }

                    } else {

                        player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimOther"));
                        breakSign(block);
                        return;

                    }
                }

            }

            if (event.getLine(2).equalsIgnoreCase("cancel")) {

                pendingUnclaims.remove(correctName);
                player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaCancelled"));
                breakSign(block);
                return;

            }

            if (!pendingUnclaims.containsKey(correctName)) {

                pendingUnclaims.put(correctName, event.getBlock());
                player.sendMessage(ChatColor.GOLD + Lang.get("StartUnclaimArea1"));
                player.sendMessage(ChatColor.GOLD + Lang.get("StartUnclaimArea2"));

            } else {

                Block startBlock = pendingUnclaims.get(correctName);

                if (startBlock.getWorld() != block.getWorld()) {
                    player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaWorldError"));
                    breakSign(block);
                    return;
                }

                Chunk startChunk = startBlock.getChunk();
                pendingUnclaims.remove(correctName);
                Chunk endChunk = block.getChunk();
                int startX;
                int startZ;
                int endX;
                int endZ;

                if (startChunk.getX() <= endChunk.getX()) {

                    startX = startChunk.getX();
                    endX = endChunk.getX();

                } else {

                    startX = endChunk.getX();
                    endX = startChunk.getX();

                }

                if (startChunk.getZ() <= endChunk.getZ()) {

                    startZ = startChunk.getZ();
                    endZ = endChunk.getZ();

                } else {

                    startZ = endChunk.getZ();
                    endZ = startChunk.getZ();

                }

                List<MyChunkChunk> foundChunks = new ArrayList<MyChunkChunk>();
                int chunkCount = 0;
                xloop:
                for (int x = startX; x <= endX; x++) {

                    for (int z = startZ; z <= endZ; z++) {

                        if (chunkCount < 64) {

                            MyChunkChunk myChunk = new MyChunkChunk(block.getWorld().getName(), x, z);

                            if (myChunk.isClaimed() && myChunk.getOwner().equalsIgnoreCase(correctName)) {

                                foundChunks.add(myChunk);
                                chunkCount++;

                            }

                        } else {

                            player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaTooBig"));
                            breakSign(block);
                            return;

                        }

                    }

                }

                if (foundChunks.isEmpty()) {
                    player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaNoneFound"));
                    breakSign(block);
                    return;
                }

                if (MyChunk.getToggle("foundEconomy") && !player.hasPermission("mychunk.free") && !correctName.equalsIgnoreCase("Server") && !correctName.equalsIgnoreCase("Public") && MyChunk.getToggle("unclaimRefund")) {
                    
                    if (!(MyChunk.getToggle("firstChunkFree") && MyChunkChunk.getOwnedChunkCount(player.getName()) == 0)) {
                        chunkCount--;
                    }
                    double price = MyChunk.getDoubleSetting("chunkPrice") * chunkCount;
                    if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                        for (int i = 0; i < chunkCount; i++) {
                            price += MyChunk.getDoubleSetting("priceRampRate");
                        }
                    }
                    MyChunkVaultLink.economy.depositPlayer(player.getName(), price / 100 * MyChunk.getDoubleSetting("refundPrecent"));

                }

                for (MyChunkChunk myChunk : foundChunks) {
                    myChunk.unclaim();
                }

                player.sendMessage(ChatColor.GOLD + Lang.get("ChunksUnclaimed") + ": " + ChatColor.WHITE + foundChunks.size());

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[owner]")) {

            // Player requested chunk's Owner info
            Player player = event.getPlayer();
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block);

            if (chunk.isClaimed()) {

                String owner = chunk.getOwner();

                if (owner.equalsIgnoreCase(player.getName())) {

                    player.sendMessage(ChatColor.GOLD + Lang.get("YouOwn"));
                    player.sendMessage(ChatColor.GREEN + Lang.get("AllowedPlayers") + ": " + chunk.getAllowed());

                } else {

                    player.sendMessage(ChatColor.GOLD + Lang.get("OwnedBy") + " " + ChatColor.WHITE + owner + ChatColor.GOLD + "!");
                    player.sendMessage(ChatColor.GREEN + Lang.get("AllowedPlayers") + ": " + chunk.getAllowed());

                }

            } else {
                player.sendMessage(ChatColor.GOLD + Lang.get("ChunkIs") + " " + ChatColor.WHITE + Lang.get("Unowned") + ChatColor.GOLD + "!");
            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[allow]")) {

            // Player attempted to add a player allowance
            Player player = event.getPlayer();
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block);

            if (Lang.get("Everyone").equalsIgnoreCase(line1.toUpperCase())) {
                line1 = "*";
            }

            String line2 = event.getLine(2).toUpperCase();
            String owner = chunk.getOwner();

            if (!owner.equalsIgnoreCase(player.getName()) && !(owner.equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs"))) {
                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            } else if (!owner.equalsIgnoreCase(player.getName()) && !owner.equalsIgnoreCase("server") && !(owner.equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {
                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            } else if ("".equals(line1) || line1.contains(" ")) {
                player.sendMessage(ChatColor.RED + Lang.get("Line2Player"));
            } else if (line1.equalsIgnoreCase(player.getName()) && !chunk.getOwner().equalsIgnoreCase("Server") && !chunk.getOwner().equalsIgnoreCase("Public")) {
                player.sendMessage(ChatColor.RED + Lang.get("AllowSelf"));
            } else {

                if ("".equals(line2)) {
                    line2 = "*";
                }

                String targetName = "*";

                if (!"*".equalsIgnoreCase(line1)) {
                    
                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);

                    if (!target.hasPlayedBefore()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        breakSign(block);
                        return;

                    } else {
                        targetName = target.getName();
                    }

                }

                String displayName = targetName;
                if (displayName.equals("*")) {
                    displayName = Lang.get("Everyone");
                }

                if (!"*".equalsIgnoreCase(line2)) {

                    String errors = "";

                    for (int i = 0; i < line2.length(); i++) {

                        String thisChar = line2.substring(i, i + 1).replaceAll(" ", "");

                        if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                            chunk.allow(targetName, thisChar);
                        } else {
                            errors += thisChar;
                            line2.replaceAll(thisChar, "");
                        }

                    }

                    player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                    
                    if (!"".equals(errors)) {
                        player.sendMessage(ChatColor.RED + Lang.get("FlagsNotFound") + ": " + errors);
                    }

                    player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("ReceivedFlags") + ": " + ChatColor.GREEN + line2.replaceAll(" ", ""));

                    if (!"*".equals(targetName)) {
                        player.sendMessage(ChatColor.GREEN + Lang.get("Allowed") + ": " + chunk.getAllowedFlags(targetName));
                    }

                } else {

                    chunk.allow(targetName, line2.replaceAll(" ", ""));
                    player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                    player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("ReceivedFlags") + ": " + ChatColor.GREEN + line2.replaceAll(" ", ""));

                    if (!"*".equals(line2)) {
                        player.sendMessage(ChatColor.GREEN + Lang.get("NewFlags") +": " + chunk.getAllowedFlags(targetName));
                    }

                }

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[allow*]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            if (Lang.get("Everyone").equalsIgnoreCase(line1.toUpperCase())) {
                line1 = "*";
            }

            String line2 = event.getLine(2).toUpperCase();

            if ("".equals(line1) || line1.contains(" ")) {
                player.sendMessage(ChatColor.RED + Lang.get("Line2Player"));
            } else if (line1.equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.RED + Lang.get("AllowSelf"));
            } else {

                if ("".equals(line2)) {
                    line2 = "*";
                }

                String targetName = "*";

                if (!"*".equalsIgnoreCase(line1)) {

                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);

                    if (!target.hasPlayedBefore()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        breakSign(block);
                        return;

                    } else {
                        targetName = target.getName();
                    }

                }

                String displayName = targetName;

                if (displayName.equals("*")) {
                    displayName = Lang.get("Everyone");
                }
                
                HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("world,x,z", "MyChunks", "owner = '" + player.getName() + "'", "", "");

                if (results == null || results.isEmpty()) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                    breakSign(block);
                    return;

                }

                String errors = "";

                for (int i = 0; i < line2.length(); i++) {

                    String thisChar = line2.substring(i, i + 1).replaceAll(" ", "");

                    if (!MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                        errors += thisChar;
                    }

                }

                if (!"".equals(errors)) {
                    player.sendMessage(ChatColor.RED + Lang.get("FlagsNotFound") + ": " + errors);
                }
                
                int counter = 0;
                
                for (int i : results.keySet()) {

                    MyChunkChunk myChunk = new MyChunkChunk(results.get(i).get("world"), Integer.parseInt(results.get(i).get("x")), Integer.parseInt(results.get(i).get("z")));

                    if (!"*".equalsIgnoreCase(line2)) {

                        for (int j = 0; j < line2.length(); j++) {

                            String thisChar = line2.substring(j, j + 1).replaceAll(" ", "");

                            if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                                myChunk.allow(targetName, thisChar);
                            }

                        }

                    } else {

                        myChunk.allow(targetName, line2.replaceAll(" ", ""));

                    }

                    counter ++;

                }
                
                if (counter != 0) {
                    player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                    player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("ReceivedFlagsAll") +": " + ChatColor.GREEN + line2);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                }

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[disallow]")) {

            // Player attempted to add a player allowance
            Player player = event.getPlayer();
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block);

            if (Lang.get("Everyone").equalsIgnoreCase(line1.toUpperCase())) {
                line1 = "*";
            }

            String line2 = event.getLine(2).toUpperCase();
            String owner = chunk.getOwner();

            if (!owner.equalsIgnoreCase(player.getName()) && !(owner.equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs"))) {
                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            } else if (!owner.equalsIgnoreCase(player.getName()) && !owner.equalsIgnoreCase("server") && !(owner.equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {
                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            } else if ("".equals(line1) || line1.contains(" ")) {
                player.sendMessage(ChatColor.RED + Lang.get("Line2Player"));
            } else if (line1.equalsIgnoreCase(player.getName()) && !chunk.getOwner().equalsIgnoreCase("Server") && !chunk.getOwner().equalsIgnoreCase("Public")) {
                player.sendMessage(ChatColor.RED + "You cannot disallow yourself!");
            } else if (!"*".equals(line1) && chunk.isAllowed("*", line2)) {
                player.sendMessage(ChatColor.RED + "You cannot disallow flags allowed to EVERYONE!");
            } else {

                if ("".equals(line2)) {
                    line2 = "*";
                }

                String targetName = "*";

                if (!"*".equalsIgnoreCase(line1)) {

                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);

                    if (!target.hasPlayedBefore()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        breakSign(block);
                        return;

                    } else {
                        targetName = target.getName();
                    }

                }

                String displayName = targetName;

                if (displayName.equals("*")) {
                    displayName = Lang.get("Everyone");
                }

                if (!"*".equalsIgnoreCase(line2)) {

                    String errors = "";

                    for (int i = 0; i < line2.length(); i++) {

                        String thisChar = line2.substring(i, i + 1).replaceAll(" ", "");
                        if (thisChar.equalsIgnoreCase("E") && chunk.getOwner().equalsIgnoreCase("public")) {
                            player.sendMessage(ChatColor.RED + "You cannot deny entry access (E) on public chunks!");
                            line2.replaceAll("E", "");
                        } else if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                            chunk.disallow(targetName, thisChar);
                        } else {
                            errors += thisChar;
                            line2.replaceAll(thisChar, "");
                        }

                    }

                    player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));

                    if (!"".equals(errors)) {
                        player.sendMessage(ChatColor.RED + Lang.get("FlagsNotFound") + ": " + errors);
                    }

                    player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("LostFlags") +": " + ChatColor.GREEN + line2.replaceAll(" ", ""));

                    if (!"*".equals(targetName)) {
                        player.sendMessage(ChatColor.GREEN + Lang.get("NewFlags") + ": " + chunk.getAllowedFlags(targetName));
                    }

                } else {

                    chunk.disallow(targetName, line2.replaceAll(" ", ""));
                    player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                    player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("LostFlags") +": " + ChatColor.GREEN + line2.replaceAll(" ", ""));

                    if (!"*".equals(line2)) {
                        player.sendMessage(ChatColor.GREEN + Lang.get("NewFlags") + ": " + chunk.getAllowedFlags(targetName));
                    }

                }

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[disallow*]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            if (Lang.get("Everyone").equalsIgnoreCase(line1.toUpperCase())) {
                line1 = "*";
            }

            String line2 = event.getLine(2).toUpperCase();

            if ("".equals(line1) || line1.contains(" ")) {
                player.sendMessage(ChatColor.RED + Lang.get("Line2Player"));
            } else if (line1.equalsIgnoreCase(player.getName())) {
                player.sendMessage(ChatColor.RED + Lang.get("DisallowSelf"));
            } else {

                if ("".equals(line2)) {
                    line2 = "*";
                }

                String targetName = "*";

                if (!"*".equalsIgnoreCase(line1)) {

                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(line1);

                    if (!target.hasPlayedBefore()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + line1 + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        breakSign(block);
                        return;

                    } else {
                        targetName = target.getName();
                    }

                }

                String displayName = targetName;

                if (displayName.equals("*")) {
                    displayName = Lang.get("Everyone");
                }
                
                HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("world,x,z", "MyChunks", "owner = '" + player.getName() + "'", "", "");

                if (results == null || results.isEmpty()) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                    breakSign(block);
                    return;

                }

                String errors = "";

                for (int i = 0; i < line2.length(); i++) {

                    String thisChar = line2.substring(i, i + 1).replaceAll(" ", "");
                    if (!MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                        errors += thisChar;
                        line2.replaceAll(thisChar, "");
                    }

                }

                if (!"".equals(errors)) {
                    player.sendMessage(ChatColor.RED + "Flags not found: " + errors);
                }
                
                int counter = 0;
                
                for (int i : results.keySet()) {

                    MyChunkChunk chunk = new MyChunkChunk(results.get(i).get("world"), Integer.parseInt(results.get(i).get("x")), Integer.parseInt(results.get(i).get("z")));

                    if (!"*".equalsIgnoreCase(line2)) {

                        for (int j = 0; j < line2.length(); j++) {

                            String thisChar = line2.substring(j, j + 1).replaceAll(" ", "");

                            if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                                chunk.disallow(targetName, thisChar);
                            }

                        }

                    } else {

                        chunk.disallow(targetName, line2.replaceAll(" ", ""));

                    }

                    counter++;

                }
                
                if (counter != 0) {
                    player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                    player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("LostFlagsAll") + ": " + ChatColor.GREEN + line2.replaceAll(" ", ""));
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                }

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[for sale]") || line0.equalsIgnoreCase("[forsale]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            MyChunkChunk chunk = new MyChunkChunk(block);
            Double price = 0.00;

            if (!player.hasPermission("mychunk.sell")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsSell"));
                breakSign(block);
                return;

            } else if (player.hasPermission("mychunk.free")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsSellFree"));
                breakSign(block);
                return;

            } else if (!player.hasPermission("mychunk.override") && !chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
                breakSign(block);
                return;

            } else if (MyChunk.getToggle("foundEconomy")) {

                if (line1.isEmpty() || line1.equals("")) {

                    player.sendMessage(ChatColor.RED + Lang.get("Line2SellPrice"));
                    breakSign(block);
                    return;

                } else {

                    try {
                        price = Double.parseDouble(line1);
                    } catch (NumberFormatException nfe) {

                        player.sendMessage(ChatColor.RED + Lang.get("SellPriceNumber"));
                        breakSign(block);
                        return;

                    }

                    if (price == 0) {

                        player.sendMessage(ChatColor.RED + Lang.get("SellPriceZero"));
                        breakSign(block);
                        return;

                    }

                }

            }

            if (MyChunk.getToggle("foundEconomy")) {

                player.sendMessage(ChatColor.GOLD + "Chunk on sale for " + MyChunkVaultLink.economy.format(price) + "!");
                chunk.setForSale(price);

            } else {

                player.sendMessage(ChatColor.GOLD + "Chunk on sale!");
                chunk.setForSale(MyChunk.getDoubleSetting("chunkPrice"));

            }

            breakSign(block);

        } else if (line0.equalsIgnoreCase("[not for sale]") || line0.equalsIgnoreCase("[notforsale]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            MyChunkChunk chunk = new MyChunkChunk(block);

            if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
                breakSign(block);
                return;

            } else if (!chunk.isForSale()) {

                player.sendMessage(ChatColor.RED + Lang.get("ChunkNotForSale"));
                breakSign(block);
                return;

            }

            player.sendMessage(ChatColor.GOLD + Lang.get("ChunkOffSale"));
            chunk.setNotForSale();
            breakSign(block);

        } else if (line0.equalsIgnoreCase("[AllowMobs]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            MyChunkChunk chunk = new MyChunkChunk(block);

            if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
                breakSign(block);
                return;

            }
            
            if (chunk.getOwner().equalsIgnoreCase("public")) {
                
                player.sendMessage(ChatColor.RED + Lang.get("NotPublicSign"));
                breakSign(block);
                return;
                
            }

            if (!player.hasPermission("mychunk.allowmobs")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsMobSign"));
                breakSign(block);
                return;

            }

            if (!line1.equalsIgnoreCase("on") && !line1.equalsIgnoreCase("off")) {

                player.sendMessage(ChatColor.RED + Lang.get("SpecifyOnOff"));
                breakSign(block);
                return;

            }

            if (line1.equalsIgnoreCase("on")) {

                chunk.setAllowMobs(true);
                player.sendMessage(ChatColor.GOLD + Lang.get("MobsCanSpawn"));

            } else {

                chunk.setAllowMobs(false);
                player.sendMessage(ChatColor.GOLD + Lang.get("MobsCannotSpawn"));

            }

            breakSign(event.getBlock());

        } else if (line0.equalsIgnoreCase("[AllowPVP]")) {

            Player player = event.getPlayer();
            Block block = event.getBlock();

            MyChunkChunk chunk = new MyChunkChunk(block);

            if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
                breakSign(block);
                return;

            }
            
            if (chunk.getOwner().equalsIgnoreCase("public")) {
                
                player.sendMessage(ChatColor.RED + Lang.get("NotPublicSign"));
                breakSign(block);
                return;
                
            }

            if (!player.hasPermission("mychunk.allowpvp")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsPVPSign"));
                breakSign(block);
                return;

            }

            if (!line1.equalsIgnoreCase("on") && !line1.equalsIgnoreCase("off")) {

                player.sendMessage(ChatColor.RED + Lang.get("SpecifyOnOff"));
                breakSign(block);
                return;

            }

            if (line1.equalsIgnoreCase("on")) {

                chunk.setAllowPVP(true);
                player.sendMessage(ChatColor.GOLD + Lang.get("PVPAllowed"));

            } else {

                chunk.setAllowPVP(false);
                player.sendMessage(ChatColor.GOLD + Lang.get("PVPDisallowed"));

            }

            breakSign(event.getBlock());
        
        } else if (line0.equalsIgnoreCase("[Lease]")) {
            
            Player player = event.getPlayer();
            Block block = event.getBlock();
            
            if (!MyChunk.getToggle("foundEconomy")) {
                
                player.sendMessage(ChatColor.RED + Lang.get("LeaseRequiresEco"));
                breakSign(block);
                return;
                
            }
            
            MyChunkChunk chunk = new MyChunkChunk(block);
            
            if (!player.hasPermission("mychunk.lease")) {
                
                player.sendMessage(ChatColor.RED + Lang.get("NoPermsLease"));
                breakSign(block);
                return;
                
            }
            
            if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

                player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
                breakSign(block);
                return;

            }
            
            if (chunk.getOwner().equalsIgnoreCase("public")) {
                
                player.sendMessage(ChatColor.RED + Lang.get("NotPublicSign"));
                breakSign(block);
                return;
                
            }
            
            if (!block.getType().equals(Material.WALL_SIGN)) {
                
                player.sendMessage(ChatColor.RED + Lang.get("LeaseOnWall"));
                breakSign(block);
                return;
                
            }
            
            Block attached = getAttachedBlock(block);
            Block door = attached.getRelative(BlockFace.DOWN);
            
            if ( door.getTypeId() != 64 && door.getTypeId() != 96 && door.getTypeId() != 107) {
                
                player.sendMessage(ChatColor.RED + Lang.get("LeaseAboveDoor"));
                breakSign(block);
                return;
                
            }
            
            if (alreadyLeased(attached, block)) {
                
                player.sendMessage(ChatColor.RED + Lang.get("DoorAlreadyLeased"));
                breakSign(block);
                return;
                
            }
            
            double price;
            
            try {
                price = Double.parseDouble(event.getLine(1));
            } catch(NumberFormatException nfe) {
                player.sendMessage(ChatColor.RED + Lang.get("Line2LeasePrice"));
                breakSign(block);
                return;
            }
            
            if (price < 0) {
                price = 0;
                event.setLine(1, price+"");
            }
            
            event.setLine(0, "[Lease]");
            event.setLine(2, "");
            event.setLine(3, new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

            player.sendMessage(ChatColor.GOLD + Lang.get("LeaseCreated"));
            
        } else {
            
            Block block = event.getBlock();
            MyChunkChunk chunk = new MyChunkChunk(block);
            Player player = event.getPlayer();
            
            if(!WorldGuardHook.isRegion(block.getLocation()) && !FactionsHook.isClaimed(block.getLocation()) && !TownyHook.isClaimed(block.getLocation())){

                if (chunk.isClaimed()) {
    
                    String owner = chunk.getOwner();
                    if (!owner.equalsIgnoreCase(player.getName()) && !chunk.isAllowed(player.getName(), "B") && !player.hasPermission("mychunk.override")) {
                        event.setCancelled(true);
                        breakSign(block);
                    }
    
                } else if (MyChunk.getToggle("protectUnclaimed") && !player.hasPermission("mychunk.override")) {
    
                    event.setCancelled(true);
                    breakSign(block);
    
                    if (!WorldGuardHook.isRegion(block.getLocation()) && !FactionsHook.isClaimed(block.getLocation()) && !TownyHook.isClaimed(block.getLocation())) {
                        if (chunk.isClaimed()) {
                            String owner = chunk.getOwner();
                            if (!owner.equalsIgnoreCase(player.getName()) && !chunk.isAllowed(player.getName(), "B") && !player.hasPermission("mychunk.override")) {
                                event.setCancelled(true);
                                breakSign(block);
                            }
                        } else if (MyChunk.getToggle("protectUnclaimed") && !player.hasPermission("mychunk.override")) {
                            event.setCancelled(true);
                            breakSign(block);
                        }
                    }
    
                }
            }
        }

    }

    private void breakSign(Block block) {

        if (block.getTypeId() == 63 || block.getTypeId() == 68) {

            block.setTypeId(0);
            block.getWorld().dropItem(block.getLocation(), new ItemStack(323, 1));

        }

    }
    
    private static Block getAttachedBlock(Block b) {
        MaterialData m = b.getState().getData();
        BlockFace face = BlockFace.DOWN;
        if (m instanceof Attachable) {
            face = ((Attachable) m).getAttachedFace();
        }
        return b.getRelative(face);
    }
    
    private static boolean alreadyLeased(Block attached, Block sign) {
        
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face: faces) {
            
            Block block = attached.getRelative(face);
            
            if (block != sign) {
                BlockState state = block.getState();

                if (state instanceof Sign) {
                    if (((Sign)state).getLine(0).equalsIgnoreCase("[Lease]"))
                        return true;
                }
            }
            
        }
        
        return false;
    }
    
}
