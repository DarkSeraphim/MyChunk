package me.ellbristow.mychunk.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.ellbristow.mychunk.MyChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.listeners.SignListener;
import me.ellbristow.mychunk.utils.FactionsHook;
import me.ellbristow.mychunk.utils.MyChunkVaultLink;
import me.ellbristow.mychunk.utils.TownyHook;
import me.ellbristow.mychunk.utils.db.SQLBridge;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class MyChunkCommand implements CommandExecutor {

    private MyChunk plugin;
    
    public MyChunkCommand(MyChunk instance) {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        
        if (!command.getName().equalsIgnoreCase("mychunk")) {
            
            sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /" + cmd);
        
            return false;
            
        }
            
        if (args.length == 0) {
            return commandMyChunk(sender, 0);
        }

        try {
            int page = Integer.parseInt(args[0]);
            return commandMyChunk(sender, page);
        } catch (NumberFormatException e) {}

        if (args[0].equalsIgnoreCase("allow")) {
            return commandAllow(sender, args);
        }

        if (args[0].equalsIgnoreCase("allow*") || args[0].equalsIgnoreCase("allowall")) {
            return commandAllowall(sender, args);
        }

        if (args[0].equalsIgnoreCase("allowmobs")) {
            return commandAllowmobs(sender, args);
        }

        if (args[0].equalsIgnoreCase("allowpvp")) {
            return commandAllowpvp(sender, args);
        }

        if (args[0].equalsIgnoreCase("claim")) {
            return commandClaim(sender, args);
        }

        if (args[0].equalsIgnoreCase("claimarea")) {
            return commandClaimarea(sender, args);
        }

        if (args[0].equalsIgnoreCase("disallow")) {
            return commandDisallow(sender, args);
        }

        if (args[0].equalsIgnoreCase("disallow*") || args[0].equalsIgnoreCase("disallowall")) {
            return commandDisallowall(sender, args);
        }

        if (args[0].equalsIgnoreCase("expirydays")) {
            return commandExpiryDays(sender, args);
        }

        if (args[0].equalsIgnoreCase("flags")) {
            return commandFlags(sender);
        }

        if (args[0].equalsIgnoreCase("forsale") || args[0].equalsIgnoreCase("fs")) {
            return commandForsale(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("gangnamelength")) {
            return commandGangnamelength(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("gangchunkmultiplier")) {
            return commandGangMultiplier(sender, args);
        }

        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            return commandHelp(sender, args);
        }

        if (args[0].equalsIgnoreCase("info")) {
            return commandInfo(sender, args);
        }

        if (args[0].equalsIgnoreCase("max")) {
            return commandMax(sender, args);
        }

        if (args[0].equalsIgnoreCase("notforsale") || args[0].equalsIgnoreCase("nfs")) {
            return commandNotforsale(sender);
        }

        if (args[0].equalsIgnoreCase("obprice")) {
            return commandObprice(sender, args);
        }

        if (args[0].equalsIgnoreCase("owner")) {
            return commandOwner(sender);
        }

        if (args[0].equalsIgnoreCase("price")) {
            return commandPrice(sender, args);
        }

        if (args[0].equalsIgnoreCase("purgep")) {
            return commandPurgep(sender, args);
        }

        if (args[0].equalsIgnoreCase("purgew")) {
            return commandPurgew(sender, args);
        }

        if (args[0].equalsIgnoreCase("refund")) {
            return commandRefund(sender, args);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return commandReload(sender);
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            return commandToggle(sender, args);
        }

        if (args[0].equalsIgnoreCase("unclaim")) {
            return commandUnclaim(sender, args);
        }

        if (args[0].equalsIgnoreCase("unclaimarea")) {
            return commandUnclaimarea(sender, args);
        }

        if (args[0].equalsIgnoreCase("world")) {
            return commandWorld(sender, args);
        }

        // Command Not Found
        sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /mychunk " + args[0]);
        sender.sendMessage(ChatColor.RED + Lang.get("Try") + " /mychunk help");

        return false;
        
    }

/*
                      _                 _    
                     | |               | |   
 _ __ ___  _   _  ___| |__  _   _ _ __ | | __
| '_ ` _ \| | | |/ __| '_ \| | | | '_ \| |/ /
| | | | | | |_| | (__| | | | |_| | | | |   < 
|_| |_| |_|\__, |\___|_| |_|\__,_|_| |_|_|\_\
            __/ |                            
           |___/                             

*/
    
    private boolean commandMyChunk(CommandSender sender, int page) {

        if (!sender.hasPermission("mychunk.commands.stats")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }

        PluginDescriptionFile pdfFile = plugin.getDescription();
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "MyChunk v" + ChatColor.WHITE + pdfFile.getVersion() + ChatColor.GOLD + " " + Lang.get("By") + " " + ChatColor.WHITE + "ellbristow");
        sender.sendMessage(ChatColor.GRAY + "====");
        
        if (page != 2) {
            /*
             * Show First Page (Player Specific Info)
             */
            
            HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("COUNT(*) AS counter", "MyChunks", "", "", "");
            
            int count = Integer.parseInt(results.get(0).get("counter"));
            
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.GOLD + Lang.get("ChunksOwned") + ": " + ChatColor.WHITE + plugin.ownedChunkCount(sender.getName()) + "  " + ChatColor.GOLD + Lang.get("TotalClaimedChunks") + ": " + ChatColor.WHITE + count);
            } else {
                sender.sendMessage(ChatColor.GOLD + Lang.get("TotalClaimedChunks") + ": " + ChatColor.WHITE + count);
            }
            
            String yourMax;
            int playerMax = MyChunk.getMaxChunks(sender);
            
            if (playerMax != 0) {
                yourMax = playerMax + "";
            } else {
                yourMax = Lang.get("Unlimited");
            }
            
            sender.sendMessage(ChatColor.GOLD + Lang.get("MaxChunkClaim") + ": " + ChatColor.WHITE + yourMax);
            
            if (MyChunk.getToggle("foundeconomy")) {
                
                double thisPrice = MyChunk.getDoubleSetting("chunkPrice");
                String rampMessage = ChatColor.GOLD + Lang.get("PriceRamping") + ": " + ChatColor.WHITE + (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0 ? "Yes" : "No");
                
                if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                    
                    rampMessage += " " + ChatColor.GOLD + Lang.get("RampRate") + ": " + ChatColor.WHITE + MyChunk.getDoubleSetting("priceRampRate");
                    
                    if (sender instanceof Player) {
                        
                        int claimed = MyChunkChunk.getOwnedChunkCount(sender.getName());
                        
                        if (MyChunk.getToggle("firstchunkfree") && claimed > 0) {
                            claimed--;
                        }
                        
                        thisPrice += MyChunk.getDoubleSetting("priceRampRate") * claimed;
                        
                    }
                    
                }
                
                sender.sendMessage(rampMessage);
                
                sender.sendMessage(ChatColor.GOLD + Lang.get("ChunkPrice") + ": " + ChatColor.WHITE + MyChunkVaultLink.economy.format(thisPrice) + " " + 
                        ChatColor.GOLD + Lang.get("UnclaimRefunds") + ": " + 
                        ChatColor.WHITE + (MyChunk.getToggle("unclaimrefund") && MyChunk.getDoubleSetting("refundPercent") != 0 ? MyChunk.getDoubleSetting("refundPercent") + "%" : "No"));

                String overFee = "";

                if (MyChunk.getToggle("allowoverbuy")) {
                    String resales = "exc.";
                    if (MyChunk.getToggle("overbuyp2p")) {
                        resales = "inc.";
                    }
                    overFee = " " + ChatColor.GOLD + Lang.get("OverbuyFee") + ": " + ChatColor.WHITE + MyChunkVaultLink.economy.format(MyChunk.getDoubleSetting("overbuyprice")) + "(" + resales + " " + Lang.get("Resales") + ")";
                }

                sender.sendMessage(ChatColor.GOLD + Lang.get("AllowOverbuy") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allowoverbuy")) + overFee);
                
            }

            sender.sendMessage(ChatColor.GRAY + Lang.get("NextPage") + ": /mychunk 2");
            
            return true;
            
        } else {
            
            /*
             * Show Second Page (Plugin Settings)
             */
            
            String claimExpiry;
            
            if (!MyChunk.getToggle("useclaimexpiry")) {
                claimExpiry = Lang.get("Disabled");
            } else {
                claimExpiry = MyChunk.getIntSetting("claimexpirydays") + " " + Lang.get("DaysWithoutLogin");
            }
            
            sender.sendMessage(ChatColor.GOLD + Lang.get("ClaimExpiry") + ": " + ChatColor.WHITE + claimExpiry);
            sender.sendMessage(ChatColor.GOLD + Lang.get("AllowMobGrief") + ": " + ChatColor.WHITE + (MyChunk.getToggle("allowmobgrief") ? Lang.get("Yes") : Lang.get("No")) + "  " + ChatColor.GOLD + Lang.get("PreventPVP") + ": " + ChatColor.WHITE + (MyChunk.getToggle("preventpvp") ? Lang.get("Yes") : Lang.get("No")));
            sender.sendMessage(ChatColor.GOLD + Lang.get("OwnerNotifications") + ": " + ChatColor.WHITE + (MyChunk.getToggle("ownerNotifications") ? Lang.get("Yes") : Lang.get("No")) + " " + ChatColor.GOLD + Lang.get("PreventEntry") + ": " + ChatColor.WHITE + (MyChunk.getToggle("prevententry") ? Lang.get("Yes") : Lang.get("No")));
            sender.sendMessage(ChatColor.GOLD + Lang.get("AllowNeighbours") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allowneighbours")) + ChatColor.GOLD + "  " + Lang.get("ProtectUnclaimed") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("protectunclaimed")));
            sender.sendMessage(ChatColor.GOLD + Lang.get("AllowNether") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allownether")) + "  " + ChatColor.GOLD + Lang.get("AllowEnd") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allowend")));
            
            return true;
            
        }

    }
    
/*
       _ _               
      | | |              
  __ _| | | _____      __
 / _` | | |/ _ \ \ /\ / /
| (_| | | | (_) \ V  V / 
 \__,_|_|_|\___/ \_/\_/  

*/
    
    private boolean commandAllow(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.allow")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);
        
        boolean isEveryone = false;
        
        String targetName;
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("Everyone")) {
                isEveryone = true;
                targetName = "*";
            } else {
                targetName = args[1];
            }
        } else {
            player.sendMessage(ChatColor.RED + Lang.get("SpecifyPlayer"));
            player.sendMessage(ChatColor.GRAY + "/mychunk allow [Player|'*'] {Flags|'*'}");
            return false;
        }
        
        String flags;
        
        if (args.length > 2) {
            flags = args[2];
        } else {
            flags = "*";
        }
        
        String owner = chunk.getOwner();

        if (!owner.equalsIgnoreCase(player.getName()) && !(owner.equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) & !player.hasPermission("mychunk.override")) {
            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
        } else if (!owner.equalsIgnoreCase(player.getName()) && !(owner.equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))  & !player.hasPermission("mychunk.override")) {
            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
        } else if (targetName.equalsIgnoreCase(player.getName()) && !chunk.getOwner().equalsIgnoreCase("Server") && !chunk.getOwner().equalsIgnoreCase("Public")) {
            player.sendMessage(ChatColor.RED + Lang.get("AllowSelf"));
        } else {

            if (!isEveryone) {

                OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);

                if (!target.hasPlayedBefore()) {

                    player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                    return false;

                } else {
                    targetName = target.getName();
                }

            }

            String displayName = targetName;
            if (isEveryone) {
                displayName = Lang.get("Everyone");
            }

            if (!"*".equalsIgnoreCase(flags)) {

                String errors = "";

                for (int i = 0; i < flags.length(); i++) {

                    String thisChar = flags.substring(i, i + 1);

                    if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                        chunk.allow(targetName, thisChar);
                    } else {
                        errors += thisChar;
                        flags.replaceAll(thisChar, "");
                    }

                }

                player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                
                if (!"".equals(errors)) {
                    player.sendMessage(ChatColor.RED + Lang.get("FlagsNotFound") + ": " + errors);
                }

                player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("ReceivedFlags") + ": " + ChatColor.GREEN + flags);

                if (!"*".equals(targetName)) {
                    player.sendMessage(ChatColor.GREEN + Lang.get("Allowed") + ": " + chunk.getAllowedFlags(targetName));
                }

            } else {

                chunk.allow(targetName, flags);
                player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("ReceivedFlags") +": " + ChatColor.GREEN + flags);

                if (!"*".equals(flags)) {
                    player.sendMessage(ChatColor.GREEN + Lang.get("NewFlags") + ": " + chunk.getAllowedFlags(targetName));
                }

            }

        }
        
        return true;
        
    }
    
/*
       _ _                    _ _ 
      | | |                  | | |
  __ _| | | _____      ____ _| | |
 / _` | | |/ _ \ \ /\ / / _` | | |
| (_| | | | (_) \ V  V / (_| | | |
 \__,_|_|_|\___/ \_/\_/ \__,_|_|_|

*/
    
    private boolean commandAllowall(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.allow")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        
        boolean isEveryone = false;
        
        String targetName;
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("Everyone")) {
                isEveryone = true;
                targetName = "*";
            } else {
                targetName = args[1];
            }
        } else {
            player.sendMessage(ChatColor.RED + Lang.get("SpecifyPlayer"));
            player.sendMessage(ChatColor.GRAY + "/mychunk allow [Player|'*'] {Flags|'*'}");
            return false;
        }
        
        String flags;
        
        if (args.length > 2) {
            flags = args[2];
        } else {
            flags = "*";
        }
        
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + Lang.get("AllowSelf"));
        } else {

            if (!isEveryone) {

                OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);

                if (!target.hasPlayedBefore()) {

                    player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                    return false;

                } else {
                    targetName = target.getName();
                }

            }

            String displayName = targetName;

            if (isEveryone) {
                displayName = Lang.get("Everyone");
            }
            
            HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("world,x,z", "MyChunks", "owner = '" + player.getName() + "'", "", "");

            if (results == null || results.isEmpty()) {

                player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                
                return false;

            }

            String errors = "";

            for (int i = 0; i < flags.length(); i++) {

                String thisChar = flags.substring(i, i + 1);

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

                if (!"*".equalsIgnoreCase(flags)) {

                    for (int j = 0; j < flags.length(); j++) {

                        String thisChar = flags.substring(j, j + 1);

                        if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                            myChunk.allow(targetName, thisChar);
                        }

                    }

                } else {

                    myChunk.allow(targetName, flags);

                }

                counter++;

            }
            
            if (counter != 0) {
                player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("ReceivedFlagsAll") + ": " + ChatColor.GREEN + flags);
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                return false;
            }
            
        }
        
        return true;
        
    }
    
/*
       _ _                               _         
      | | |                             | |        
  __ _| | | _____      ___ __ ___   ___ | |__  ___ 
 / _` | | |/ _ \ \ /\ / / '_ ` _ \ / _ \| '_ \/ __|
| (_| | | | (_) \ V  V /| | | | | | (_) | |_) \__ \
 \__,_|_|_|\___/ \_/\_/ |_| |_| |_|\___/|_.__/|___/

*/
    
    private boolean commandAllowmobs(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.allowmobs")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);
        
        if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            return false;

        }

        if (chunk.getOwner().equalsIgnoreCase("public")) {

            player.sendMessage(ChatColor.RED + Lang.get("NotPublicCommand"));
            return false;

        }
        
        if (!(args.length > 1) || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {

            player.sendMessage(ChatColor.RED + Lang.get("SpecifyOnOff"));
            player.sendMessage(ChatColor.GRAY + "/mychunk allowmobs ['on'|'off']");
            return false;

        }

        if (args[1].equalsIgnoreCase("on")) {

            chunk.setAllowMobs(true);
            player.sendMessage(ChatColor.GOLD + Lang.get("MobsCanSpawn"));

        } else {

            chunk.setAllowMobs(false);
            player.sendMessage(ChatColor.GOLD + Lang.get("MobsCannotSpawn"));

        }
        
        return true;
        
    }
    
/*
       _ _                                
      | | |                               
  __ _| | | _____      ___ ____   ___ __  
 / _` | | |/ _ \ \ /\ / / '_ \ \ / / '_ \ 
| (_| | | | (_) \ V  V /| |_) \ V /| |_) |
 \__,_|_|_|\___/ \_/\_/ | .__/ \_/ | .__/ 
                        | |        | |    
                        |_|        |_|    
*/
    
    private boolean commandAllowpvp(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.allowpvp")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);
        
        if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            return false;

        }

        if (chunk.getOwner().equalsIgnoreCase("public")) {

            player.sendMessage(ChatColor.RED + Lang.get("NotPublicSign"));
            return false;

        }

        if (!(args.length > 1) || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {

            player.sendMessage(ChatColor.RED + Lang.get("SpecifyOnOff"));
            player.sendMessage(ChatColor.GRAY + "/mychunk allowpvp ['on'|'off']");
            return false;

        }

        if (args[1].equalsIgnoreCase("on")) {

            chunk.setAllowPVP(true);
            player.sendMessage(ChatColor.GOLD + Lang.get("PVPAllowed"));

        } else {

            chunk.setAllowPVP(false);
            player.sendMessage(ChatColor.GOLD + Lang.get("PVPDisallowed"));

        }
        
        return true;
        
    }
    
/*
      _       _           
     | |     (_)          
  ___| | __ _ _ _ __ ___  
 / __| |/ _` | | '_ ` _ \ 
| (__| | (_| | | | | | | |
 \___|_|\__,_|_|_| |_| |_|

*/
    
    private boolean commandClaim(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.claim")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        // Player attempted to claim a chunk
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);
        
        if (!MyChunk.isWorldEnabled(player.getWorld().getName()) && !MyChunk.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + Lang.get("ClaimWorldDisabled"));
            return false;
        }
        
        if (!player.hasPermission("mychunk.claim") && !player.hasPermission("mychunk.claim.server") && !player.hasPermission("mychunk.claim.public")) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaim"));
            return false;

        } else if (chunk.isClaimed()) {

            String owner = chunk.getOwner();

            if (owner.equalsIgnoreCase(player.getName())) {

                player.sendMessage(ChatColor.RED + Lang.get("AlreadyOwner"));
                return false;

            } else if (!chunk.isForSale()) {

                player.sendMessage(ChatColor.RED + Lang.get("AlreadyOwned") + " " + ChatColor.WHITE + owner + ChatColor.RED + "!");
                return false;

            } else if (chunk.isForSale() && !player.hasPermission("mychunk.buy")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsBuyOwned"));
                return false;

            }

        } else if (!MyChunk.getToggle("allowNether") && player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsNether"));
            return false;

        } else if (!MyChunk.getToggle("allowEnd") && player.getWorld().getEnvironment().equals(World.Environment.THE_END)) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsEnd"));
            return false;

        } else if (FactionsHook.isClaimed(block.getLocation())) {

            player.sendMessage(ChatColor.RED + Lang.get("FactionsClash"));
            return false;

        } else if (TownyHook.isClaimed(block.getLocation())) {

            player.sendMessage(ChatColor.RED + Lang.get("TownyClash"));
            return false;

        }

        int playerMax = MyChunk.getMaxChunks(player);
        int playerClaimed = MyChunkChunk.getOwnedChunkCount(player.getName());
        boolean isOverbuy = false;

        if (playerMax != 0 && playerClaimed >= playerMax) {
            isOverbuy = true;
        }

        if (isOverbuy && (!MyChunk.getToggle("allowOverbuy") || !player.hasPermission("mychunk.claim.overbuy"))) {

            player.sendMessage(ChatColor.RED + Lang.get("MaxChunksReached") + " (" + playerMax + ")!");
            return false;

        }

        double claimPrice = 0;
        boolean isFreeChunk = false;
        
        boolean isServer = false;
        boolean isPublic = false;
        boolean isOther = false;
        
        String targetName = player.getName();
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("server")) {
                isServer = true;
                targetName = "Server";
            } else if (args[1].equalsIgnoreCase("public")) {
                isPublic = true;
                targetName = "Public";
            } else if (!args[1].equalsIgnoreCase(player.getName())) {
                isOther = true;
                targetName = args[1];
            }
        }

        if (MyChunk.getToggle("foundEconomy")) {

            if ( !isServer && !isPublic && !player.hasPermission("mychunk.free") && !(playerClaimed == 0 && MyChunk.getToggle("firstChunkFree"))) {
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
            return false;

        }

        if (!isServer && !isPublic && !isOther) {

            if (!MyChunk.getToggle("allowNeighbours") && chunk.hasNeighbours() && !chunk.isForSale()) {
                
                HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("owner", "MyChunks", "world = '" + chunk.getWorldName() + "' AND ((x = " + chunk.getX() + "+1 AND z = " + chunk.getZ() + ") OR (x = " + chunk.getX() + "-1 AND z = " + chunk.getZ() + ") OR (x = " + chunk.getX() + " AND z = " + chunk.getZ() + "+1) OR (x = " + chunk.getX() + " AND z = " + chunk.getZ() + "-1))", "", "");

                if (results != null && !results.isEmpty()) {
                    
                        for (int i : results.keySet()) {
                            
                            if (results.get(i).get("owner").equalsIgnoreCase(player.getName()) || results.get(i).get("owner").equalsIgnoreCase("Server") || results.get(i).get("owner").equalsIgnoreCase("Public")) {
                                continue;
                            }
                            player.sendMessage(ChatColor.RED + Lang.get("NoNeighbours"));
                            
                            return true;
                            
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

        } else {

            String correctName;

            if (isServer) {

                if (!player.hasPermission("mychunk.claim.server")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimServer"));
                    return false;

                } else {
                    correctName = "Server";
                }

            } else if (isPublic) {

                if (!player.hasPermission("mychunk.claim.public")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimPublic"));
                    return false;

                } else {
                    correctName = "Public";
                }

            } else if (player.hasPermission("mychunk.claim.others")) {

                if (!MyChunk.getToggle("allowNeighbours") && chunk.hasNeighbours() && !chunk.isForSale()) {

                    MyChunkChunk[] neighbours = chunk.getNeighbours();

                    if ((neighbours[0].isClaimed() && !neighbours[0].getOwner().equalsIgnoreCase(targetName) && !neighbours[0].getOwner().equalsIgnoreCase(player.getName())) || (neighbours[1].isClaimed() && !neighbours[1].getOwner().equalsIgnoreCase(targetName) && !neighbours[1].getOwner().equalsIgnoreCase(player.getName())) || (neighbours[2].isClaimed() && !neighbours[2].getOwner().equalsIgnoreCase(targetName) && !neighbours[2].getOwner().equalsIgnoreCase(player.getName())) || (neighbours[3].isClaimed() && !neighbours[3].getOwner().equalsIgnoreCase(targetName) && !neighbours[3].getOwner().equalsIgnoreCase(player.getName()))) {

                        player.sendMessage(ChatColor.RED + Lang.get("NoNeighbours"));
                        return false;

                    }

                }

                OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);

                if (!target.hasPlayedBefore() && !target.isOnline()) {

                    player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                    return false;

                } else {
                    correctName = target.getName();
                }

            } else {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimOther"));
                return false;

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

        }
        
        return true;
    }
    
/*
      _       _                                
     | |     (_)                               
  ___| | __ _ _ _ __ ___   __ _ _ __ ___  __ _ 
 / __| |/ _` | | '_ ` _ \ / _` | '__/ _ \/ _` |
| (__| | (_| | | | | | | | (_| | | |  __/ (_| |
 \___|_|\__,_|_|_| |_| |_|\__,_|_|  \___|\__,_|

*/
    
    private boolean commandClaimarea (CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.claimarea")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();

        if (!player.hasPermission("mychunk.claim")) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaim"));
            return false;

        } else if (!player.hasPermission("mychunk.claim.area")) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimArea"));
            return false;

        } else if (!MyChunk.getToggle("allowNether") && block.getWorld().getEnvironment().equals(World.Environment.NETHER)) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsNether"));
            return false;

        } else if (!MyChunk.getToggle("allowEnd") && block.getWorld().getEnvironment().equals(World.Environment.THE_END)) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsEnd"));
            return false;

        }

        String correctName;
        
        boolean isServer = false;
        boolean isPublic = false;
        boolean isOther = false;
        
        String targetName = player.getName();
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("server")) {
                isServer = true;
                targetName = "Server";
            } else if (args[1].equalsIgnoreCase("public")) {
                isPublic = true;
                targetName = "Public";
            } else if (!args[1].equalsIgnoreCase(player.getName())) {
                isOther = true;
                targetName = args[1];
            }
        }

        if (!isServer && !isPublic && !isOther) {

            correctName = player.getName();

        } else {

            if (isServer) {

                if (!player.hasPermission("mychunk.claim.server")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimServer"));
                    return false;

                } else {
                    correctName = "Server";
                }

            } else if (isPublic) {

                if (!player.hasPermission("mychunk.claim.public")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimPublic"));
                    return false;

                } else {
                    correctName = "Public";
                }

            } else {

                if (player.hasPermission("mychunk.claim.others")) {

                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);
                    if (!target.hasPlayedBefore()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        return false;

                    } else {
                        correctName = target.getName();
                    }

                } else {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsClaimOther"));
                    return false;

                }
            }
        }

        if (args.length > 2 && args[2].equalsIgnoreCase("cancel")) {

            SignListener.pendingAreas.remove(correctName);
            player.sendMessage(ChatColor.RED + Lang.get("ClaimAreaCancelled"));
            return false;

        }

        if (!SignListener.pendingAreas.containsKey(correctName)) {

            SignListener.pendingAreas.put(correctName, block);
            player.sendMessage(ChatColor.GOLD + Lang.get("StartClaimArea1"));
            player.sendMessage(ChatColor.GOLD + Lang.get("StartClaimArea2"));

        } else {

            Block startBlock = SignListener.pendingAreas.get(correctName);

            if (startBlock.getWorld() != block.getWorld()) {
                player.sendMessage(ChatColor.RED + Lang.get("ClaimAreaWorldError"));
                return false;
            }

            Chunk startChunk = startBlock.getChunk();
            SignListener.pendingAreas.remove(correctName);
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
                        return false;

                    }

                }

            }

            if (foundClaimed) {

                player.sendMessage(ChatColor.RED + Lang.get("FoundClaimedInArea"));
                return false;

            }
            if (foundNeighbour && !MyChunk.getToggle("allowNeighbours")) {

                player.sendMessage(ChatColor.RED + Lang.get("FoundNeighboursInArea"));
                return false;

            }

            int claimed = MyChunkChunk.getOwnedChunkCount(correctName);
            int max = MyChunk.getMaxChunks(player);

            if (max != 0 && (!MyChunk.getToggle("allowOverbuy") || !player.hasPermission("mychunk.claim.overbuy")) && max - claimed < foundChunks.size()) {

                player.sendMessage(ChatColor.RED + (correctName.equalsIgnoreCase(player.getName()) ? "You" : correctName) + Lang.get("ClaimAreaTooLarge"));
                player.sendMessage(ChatColor.RED + Lang.get("ChunksOwned") + ": " + ChatColor.WHITE + claimed);
                player.sendMessage(ChatColor.RED + Lang.get("ChunkMax") + ": " + ChatColor.WHITE + max);
                player.sendMessage(ChatColor.RED + Lang.get("ChunksInArea") + ": " + chunkCount);
                return false;

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
                    return false;

                }

                MyChunkVaultLink.economy.withdrawPlayer(player.getName(), areaPrice);
                player.sendMessage(ChatColor.GOLD + Lang.get("YouWereCharged") + " " + ChatColor.WHITE + MyChunkVaultLink.economy.format(areaPrice));

            }

            for (MyChunkChunk myChunk : foundChunks) {
                myChunk.claim(correctName, "");
            }

            player.sendMessage(ChatColor.GOLD + Lang.get("ChunksClaimed") + ": " + ChatColor.WHITE + foundChunks.size());

        }
        
        return true;
    }
    
/*
     _ _           _ _               
    | (_)         | | |              
  __| |_ ___  __ _| | | _____      __
 / _` | / __|/ _` | | |/ _ \ \ /\ / /
| (_| | \__ \ (_| | | | (_) \ V  V / 
 \__,_|_|___/\__,_|_|_|\___/ \_/\_/  

*/
    
    private boolean commandDisallow(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.disallow")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        // Player attempted to claim a chunk
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);
        
        boolean isEveryone = false;
        
        String targetName;
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("Everyone")) {
                isEveryone = true;
                targetName = "*";
            } else {
                targetName = args[1];
            }
        } else {
            player.sendMessage(ChatColor.RED + Lang.get("SpecifyPlayer"));
            player.sendMessage(ChatColor.GRAY + "/mychunk allow [Player|'*'] {Flags|'*'}");
            return false;
        }
        
        String flags;
        
        if (args.length > 2) {
            flags = args[2];
        } else {
            flags = "*";
        }
        
        String owner = chunk.getOwner();
        
        if (!owner.equalsIgnoreCase(player.getName()) && !(owner.equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs"))) {
            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
        } else if (!owner.equalsIgnoreCase(player.getName()) && !owner.equalsIgnoreCase("server") && !(owner.equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {
            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
        } else if (targetName.equalsIgnoreCase(player.getName()) && !chunk.getOwner().equalsIgnoreCase("Server") && !chunk.getOwner().equalsIgnoreCase("Public")) {
            player.sendMessage(ChatColor.RED + "You cannot disallow yourself!");
        } else if (!isEveryone && chunk.isAllowed("*", targetName)) {
            player.sendMessage(ChatColor.RED + Lang.get("DisallowEveryone"));
        } else {

            if (!isEveryone) {

                OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);

                if (!target.hasPlayedBefore()) {

                    player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                    return false;

                } else {
                    targetName = target.getName();
                }

            }

            String displayName = targetName;

            if (displayName.equals("*")) {
                displayName = Lang.get("Everyone");
            }

            if (!"*".equalsIgnoreCase(flags)) {

                String errors = "";

                for (int i = 0; i < flags.length(); i++) {

                    String thisChar = flags.substring(i, i + 1);
                    if (thisChar.equalsIgnoreCase("E") && chunk.getOwner().equalsIgnoreCase("public")) {
                        player.sendMessage(ChatColor.RED + Lang.get("DenyPublicEntry"));
                        flags.replaceAll("E", "");
                    } else if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                        chunk.disallow(targetName, thisChar);
                    } else {
                        errors += thisChar;
                        flags.replaceAll(thisChar, "");
                    }

                }

                player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));

                if (!"".equals(errors)) {
                    player.sendMessage(ChatColor.RED + Lang.get("FlagsNotFound") + ": " + errors);
                }

                player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("LostFlags") + ": " + ChatColor.GREEN + flags);

                if (!"*".equals(targetName)) {
                    player.sendMessage(ChatColor.GREEN + Lang.get("NewFlags") + ": " + chunk.getAllowedFlags(targetName));
                }

            } else {

                chunk.disallow(targetName, flags);
                player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("LostFlags") +": " + ChatColor.GREEN + flags);

                if (!"*".equals(flags)) {
                    player.sendMessage(ChatColor.GREEN + Lang.get("NewFlags") + ": " + chunk.getAllowedFlags(targetName));
                }

            }

        }
        
        return true;
        
    }
    
/*
     _ _           _ _                    _ _ 
    | (_)         | | |                  | | |
  __| |_ ___  __ _| | | _____      ____ _| | |
 / _` | / __|/ _` | | |/ _ \ \ /\ / / _` | | |
| (_| | \__ \ (_| | | | (_) \ V  V / (_| | | |
 \__,_|_|___/\__,_|_|_|\___/ \_/\_/ \__,_|_|_|

*/
    
    private boolean commandDisallowall(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.disallow")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        // Player attempted to claim a chunk
        Player player = (Player)sender;
        
        boolean isEveryone = false;
        
        String targetName;
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("Everyone")) {
                isEveryone = true;
                targetName = "*";
            } else {
                targetName = args[1];
            }
        } else {
            player.sendMessage(ChatColor.RED + Lang.get("SpecifyPlayer"));
            player.sendMessage(ChatColor.GRAY + "/mychunk allow [Player|'*'] {Flags|'*'}");
            return false;
        }
        
        String flags;
        
        if (args.length > 2) {
            flags = args[2];
        } else {
            flags = "*";
        }
        
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + Lang.get("DisallowSelf"));
        } else {

            if (!isEveryone) {

                OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);

                if (!target.hasPlayedBefore()) {

                    player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                    return false;

                } else {
                    targetName = target.getName();
                }

            }

            String displayName = targetName;

            if (isEveryone) {
                displayName = Lang.get("Everyone");
            }
            
            HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("world,x,z", "MyChunks", "owner = '" + player.getName() + "'", "", "");

            if (results == null || results.isEmpty()) {

                player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                
                return false;

            }

            String errors = "";

            for (int i = 0; i < flags.length(); i++) {

                String thisChar = flags.substring(i, i + 1);
                if (!MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                    errors += thisChar;
                    flags.replaceAll(thisChar, "");
                }

            }

            if (!"".equals(errors)) {
                player.sendMessage(ChatColor.RED + Lang.get("FlagsNotFound") + ": " + errors);
            }
            
            int counter = 0;
            
            for (int i : results.keySet()) {

                MyChunkChunk chunk = new MyChunkChunk(results.get(i).get("world"), Integer.parseInt(results.get(i).get("x")), Integer.parseInt(results.get(i).get("z")));

                if (!"*".equalsIgnoreCase(flags)) {

                    for (int j = 0; j < flags.length(); j++) {

                        String thisChar = flags.substring(j, j + 1);

                        if (MyChunkChunk.isFlag(thisChar.toUpperCase())) {
                            chunk.disallow(targetName, thisChar);
                        }

                    }

                } else {

                    chunk.disallow(targetName, flags);

                }

                counter ++;

            }
            
            if (counter != 0) {
                player.sendMessage(ChatColor.GOLD + Lang.get("PermissionsUpdated"));
                player.sendMessage(ChatColor.WHITE + displayName + ChatColor.GOLD + " " + Lang.get("LostFlagsAll") + ": " + ChatColor.GREEN + flags);
            } else {
                player.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned"));
                return false;
            }

        }
        
        return true;
        
    }
    
/*
                 _                _                 
                (_)              | |                
  _____  ___ __  _ _ __ _   _  __| | __ _ _   _ ___ 
 / _ \ \/ / '_ \| | '__| | | |/ _` |/ _` | | | / __|
|  __/>  <| |_) | | |  | |_| | (_| | (_| | |_| \__ \
 \___/_/\_\ .__/|_|_|   \__, |\__,_|\__,_|\__, |___/
          | |            __/ |             __/ |    
          |_|           |___/             |___/     

*/
    
    private boolean commandExpiryDays(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.expirydays")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (!MyChunk.getToggle("useClaimExpiry")) {
            sender.sendMessage(ChatColor.RED + Lang.get("ClaimExpiryDisabled"));
            return true;
        }
        
        if (args.length == 1) {
            
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyClaimExpiry"));
            sender.sendMessage(ChatColor.RED + "/mychunk expirydays ["+Lang.get("Days")+"]");
            
            return false;
            
        } else if (args.length == 2) {

            int newDays;
            
            try {
                
                newDays = Integer.parseInt(args[1]);
                
            } catch (NumberFormatException e) {
                
                sender.sendMessage(ChatColor.RED + Lang.get("Days") + " " + Lang.get("NotInteger") + "!");
                sender.sendMessage(ChatColor.RED + "/mychunk expirydays [" + Lang.get("Days") + "]");
                return false;
                
            }
            
            if (newDays <= 0) {
                
                sender.sendMessage(ChatColor.RED + Lang.get("Days") + " " + Lang.get("LessThanOne") + "!");
                sender.sendMessage(ChatColor.RED + "/mychunk expirydays ["+Lang.get("Days")+"]");
                return false;
                
            }
            
            plugin.setExpiryDays(newDays);
            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageClaimedChunksExpire") + ChatColor.GREEN + newDays + ChatColor.GOLD + " " + Lang.get("CommandMessageDaysofInactivity"));
            
        }
        
        return true;
    }
    
/*
  __ _                 
 / _| |                
| |_| | __ _  __ _ ___ 
|  _| |/ _` |/ _` / __|
| | | | (_| | (_| \__ \
|_| |_|\__,_|\__, |___/
              __/ |    
             |___/     
*/
    
    private boolean commandFlags(CommandSender sender) {

        if (!sender.hasPermission("mychunk.commands.flags")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "MyChunk "+Lang.get("PermissionFlags"));
        sender.sendMessage(ChatColor.GRAY + "====");
        sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GOLD + " = "+Lang.get("All")+" | " + ChatColor.GREEN + "A" + ChatColor.GOLD + " = "+Lang.get("Animals"));
        sender.sendMessage(ChatColor.GREEN + "B" + ChatColor.GOLD + " = "+Lang.get("Build") + " | " + ChatColor.GREEN + "C" + ChatColor.GOLD + " = "+Lang.get("AccessChests"));
        sender.sendMessage(ChatColor.GREEN + "D"  + ChatColor.GOLD + " = "+Lang.get("Destroy") + " | " + ChatColor.GREEN + "E" + ChatColor.GOLD + " = "+Lang.get("Enter"));
        sender.sendMessage(ChatColor.GREEN + "I"  + ChatColor.GOLD + " = "+Lang.get("IgniteBlocks") + " | " + ChatColor.GREEN + "L"  + ChatColor.GOLD + " = "+Lang.get("DropLava"));
        sender.sendMessage(ChatColor.GREEN + "O" + ChatColor.GOLD + " = "+Lang.get("OpenWoodenDoors") + " | " + ChatColor.GREEN + "U"  + ChatColor.GOLD + " = "+Lang.get("UseButtonsLevers"));
        sender.sendMessage(ChatColor.GREEN + "W"  + ChatColor.GOLD + " = "+Lang.get("DropWater"));
        return true;
        
    }
    
/*
  __                     _      
 / _|                   | |     
| |_ ___  _ __ ___  __ _| | ___ 
|  _/ _ \| '__/ __|/ _` | |/ _ \
| || (_) | |  \__ \ (_| | |  __/
|_| \___/|_|  |___/\__,_|_|\___|

*/
    
    private boolean commandForsale(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.forsale")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();

        MyChunkChunk chunk = new MyChunkChunk(block);
        Double price = 0.00;

        if (!player.hasPermission("mychunk.sell")) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsSell"));
            return false;

        } else if (player.hasPermission("mychunk.free")) {

            player.sendMessage(ChatColor.RED + Lang.get("NoPermsSellFree"));
            return false;

        } else if (!player.hasPermission("mychunk.override") && !chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            return false;

        } else if (MyChunk.getToggle("foundEconomy")) {

            if (!(args.length > 1)) {

                player.sendMessage(ChatColor.RED + Lang.get("SpecifySellPrice"));
                player.sendMessage(ChatColor.GRAY + "/mychunk forsale [price]");
                return false;

            } else {

                try {
                    price = Double.parseDouble(args[1]);
                } catch (NumberFormatException nfe) {

                    player.sendMessage(ChatColor.RED + Lang.get("SellPriceNumber"));
                    player.sendMessage(ChatColor.GRAY + "/mychunk forsale [price]");
                    return false;

                }

                if (price == 0) {

                    player.sendMessage(ChatColor.RED + Lang.get("SellPriceZero"));
                    return false;

                }

            }

        }

        if (MyChunk.getToggle("foundEconomy")) {

            player.sendMessage(ChatColor.GOLD + Lang.get("ChunkForSale") + ": " + MyChunkVaultLink.economy.format(price) + "!");
            chunk.setForSale(price);

        } else {

            player.sendMessage(ChatColor.GOLD + Lang.get("ChunkForSale") + "!");
            chunk.setForSale(MyChunk.getDoubleSetting("chunkPrice"));

        }
        
        return true;
        
    }
    
/*
                                         _ _   _       _ _           
                                        | | | (_)     | (_)          
  __ _  __ _ _ __   __ _ _ __ ___  _   _| | |_ _ _ __ | |_  ___ _ __ 
 / _` |/ _` | '_ \ / _` | '_ ` _ \| | | | | __| | '_ \| | |/ _ \ '__|
| (_| | (_| | | | | (_| | | | | | | |_| | | |_| | |_) | | |  __/ |   
 \__, |\__,_|_| |_|\__, |_| |_| |_|\__,_|_|\__|_| .__/|_|_|\___|_|   
  __/ |             __/ |                       | |                  
 |___/             |___/                        |_|                  

*/
    
    private boolean commandGangMultiplier(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.gangmultiplier")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyNewGangMultiplier"));
            sender.sendMessage(ChatColor.RED + "/mychunk gangmultiplier ["+Lang.get("NewLimit")+"]");
            return false;
        } else if (args.length == 2) {
            
            int newMax;
            try {
                newMax = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("AmountNotInteger") + " (e.g. 5)");
                sender.sendMessage(ChatColor.RED + "/mychunk gangmultiplier ["+Lang.get("NewLimit")+"]");
                return false;
            }

            plugin.setGangMultiplier(newMax);

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageNewGangMultiplier") + " " + newMax);
            return true;
            
        }
        return true;
    }
    
/*
                                                    _                  _   _     
                                                   | |                | | | |    
  __ _  __ _ _ __   __ _ _ __   __ _ _ __ ___   ___| | ___ _ __   __ _| |_| |__  
 / _` |/ _` | '_ \ / _` | '_ \ / _` | '_ ` _ \ / _ \ |/ _ \ '_ \ / _` | __| '_ \ 
| (_| | (_| | | | | (_| | | | | (_| | | | | | |  __/ |  __/ | | | (_| | |_| | | |
 \__, |\__,_|_| |_|\__, |_| |_|\__,_|_| |_| |_|\___|_|\___|_| |_|\__, |\__|_| |_|
  __/ |             __/ |                                         __/ |          
 |___/             |___/                                         |___/           

*/
    
    private boolean commandGangnamelength(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.gangnamelength")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyNewGangnamelength"));
            sender.sendMessage(ChatColor.RED + "/mychunk gangNameLength ["+Lang.get("NewLimit")+"]");
            return false;
        } else if (args.length == 2) {
            
            int newMax;
            try {
                newMax = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("AmountNotInteger") + " (e.g. 5)");
                sender.sendMessage(ChatColor.RED + "/mychunk gangNameLength ["+Lang.get("NewLimit")+"]");
                return false;
            }

            plugin.setGangnamelength(newMax);

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageGangnamelength") + " " + newMax);
            return true;
            
        }
        return true;
    }

/*
 _          _       
| |        | |      
| |__   ___| |_ __  
| '_ \ / _ \ | '_ \ 
| | | |  __/ | |_) |
|_| |_|\___|_| .__/ 
             | |    
             |_|    
*/
    
    private boolean commandHelp(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.help")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        int page = 1;
        
        if (args.length != 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("PageNotNumber"));
                sender.sendMessage(ChatColor.RED + "/mychunk ? {"+Lang.get("Page")+"}");
                return false;
            }
        }
        
        List<String> helpLines = new ArrayList<String>();
        
        if (sender.hasPermission("mychunk.commands.gang.help"))
            helpLines.add(ChatColor.GOLD + "["+Lang.get("GangCommands")+"] /gang help {"+Lang.get("Page")+"}");
        
        if (sender.hasPermission("mychunk.commands.stats"))
            helpLines.add(ChatColor.GOLD + "/mychunk {"+Lang.get("Page")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageStats"));
        
        if (sender.hasPermission("mychunk.commands.help"))
            helpLines.add(ChatColor.GOLD + "/mychunk help {"+Lang.get("Page")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageHelp"));
        
        if (sender.hasPermission("mychunk.commands.info"))
            helpLines.add(ChatColor.GOLD + "/mychunk info {"+Lang.get("Player")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageInfo"));
        
        if (sender.hasPermission("mychunk.commands.claim"))
            helpLines.add(ChatColor.GOLD + "/mychunk claim {"+Lang.get("Player")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageClaim"));
        
        if (sender.hasPermission("mychunk.commands.claimarea"))
            helpLines.add(ChatColor.GOLD + "/mychunk claimarea {"+Lang.get("Player")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageClaimArea"));
        
        if (sender.hasPermission("mychunk.commands.unclaim"))
            helpLines.add(ChatColor.GOLD + "/mychunk unclaim: " + ChatColor.GRAY + Lang.get("CommandMessageUnclaim"));
        
        if (sender.hasPermission("mychunk.commands.unclaimarea"))
            helpLines.add(ChatColor.GOLD + "/mychunk unclaimarea {"+Lang.get("Player")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageUnclaimArea"));
        
        if (sender.hasPermission("mychunk.commands.allow"))
            helpLines.add(ChatColor.GOLD + "/mychunk allow ["+Lang.get("Player")+"] {"+Lang.get("Flags")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageAllow"));
        
        if (sender.hasPermission("mychunk.commands.allow"))
            helpLines.add(ChatColor.GOLD + "/mychunk allow* ["+Lang.get("Player")+"] {"+Lang.get("Flags")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageAllowAll"));
        
        if (sender.hasPermission("mychunk.commands.allowmobs"))
            helpLines.add(ChatColor.GOLD + "/mychunk allowmobs ["+Lang.get("On")+"|"+Lang.get("Off")+"]: " + "\n" + ChatColor.GRAY + "  " + Lang.get("CommandMessageAllowmobs"));
        
        if (sender.hasPermission("mychunk.commands.allowpvp"))
            helpLines.add(ChatColor.GOLD + "/mychunk allowpvp ["+Lang.get("On")+"|"+Lang.get("Off")+"]: " + "\n" + ChatColor.GRAY + "  " + Lang.get("CommandMessageAllowpvp"));
        
        if (sender.hasPermission("mychunk.commands.disallow"))
            helpLines.add(ChatColor.GOLD + "/mychunk disallow ["+Lang.get("Player")+"] {"+Lang.get("Flags")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageDisallow"));
        
        if (sender.hasPermission("mychunk.commands.disallow"))
            helpLines.add(ChatColor.GOLD + "/mychunk allow* ["+Lang.get("Player")+"] {"+Lang.get("Flags")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageDisallowAll"));
        
        if (sender.hasPermission("mychunk.commands.flags"))
            helpLines.add(ChatColor.GOLD + "/mychunk flags: " + ChatColor.GRAY + Lang.get("CommandMessageFlags"));
        
        if (sender.hasPermission("mychunk.commands.forsale"))
            helpLines.add(ChatColor.GOLD + "/mychunk forsale ["+Lang.get("Price")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageForsale"));
        
        if (sender.hasPermission("mychunk.commands.gangmultiplier"))
            helpLines.add(ChatColor.GOLD + "/mychunk gangmultiplier: " + ChatColor.GRAY + Lang.get("CommandMessageGangMultiplier"));
        
        if (sender.hasPermission("mychunk.commands.gangnamelength"))
            helpLines.add(ChatColor.GOLD + "/mychunk gangnamelength: " + ChatColor.GRAY + Lang.get("CommandMessageGangnamelength"));
        
        if (sender.hasPermission("mychunk.commands.reload"))
            helpLines.add(ChatColor.GOLD + "/mychunk reload: " + ChatColor.GRAY + Lang.get("CommandMessageReload"));
        
        if (sender.hasPermission("mychunk.commands.max"))
            helpLines.add(ChatColor.GOLD + "/mychunk max ["+Lang.get("Limit")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageMax"));
        
        if (sender.hasPermission("mychunk.commands.notforsale"))
            helpLines.add(ChatColor.GOLD + "/mychunk notforsale: " + ChatColor.GRAY + Lang.get("CommandMessageNotforsale"));
        
        if (MyChunk.getToggle("foundEconomy") && sender.hasPermission("mychunk.commands.obprice"))
            helpLines.add(ChatColor.GOLD + "/mychunk obprice ["+Lang.get("Price")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageObprice"));
        
        if (sender.hasPermission("mychunk.commands.owner"))
            helpLines.add(ChatColor.GOLD + "/mychunk owner: " + ChatColor.GRAY + Lang.get("CommandMessageOwner"));
        
        if (MyChunk.getToggle("foundEconomy") && sender.hasPermission("mychunk.commands.price"))
            helpLines.add(ChatColor.GOLD + "/mychunk price ["+Lang.get("Price")+"]: " + ChatColor.GRAY + Lang.get("CommandMessagePrice"));
        
        if (sender.hasPermission("mychunk.commands.ramprate"))
            helpLines.add(ChatColor.GOLD + "/mychunk ramprate ["+Lang.get("Rate")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageRamprate"));
        
        if (sender.hasPermission("mychunk.commands.expirydays"))
            helpLines.add(ChatColor.GOLD + "/mychunk expirydays ["+Lang.get("Days")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageExpirydays"));
        
        if (sender.hasPermission("mychunk.commands.refund"))
            helpLines.add(ChatColor.GOLD + "/mychunk refund ["+Lang.get("Percentage")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageRefund"));
        
        if (sender.hasPermission("mychunk.commands.purgep"))
            helpLines.add(ChatColor.GOLD + "/mychunk purgep ["+Lang.get("Player")+"]: " + "\n" + "  " + ChatColor.GRAY + Lang.get("CommandMessagePurgep"));
        
        if (sender.hasPermission("mychunk.commands.purgew"))
            helpLines.add(ChatColor.GOLD + "/mychunk purgew ["+Lang.get("World")+"]: " + "\n" + ChatColor.GRAY + Lang.get("CommandMessagePurgep"));
        
        if (sender.hasPermission("mychunk.commands.toggle.refund")
                || sender.hasPermission("mychunk.commands.toggle.overbuy")
                || sender.hasPermission("mychunk.commands.toggle.resales")
                || sender.hasPermission("mychunk.commands.toggle.neighbours")
                || sender.hasPermission("mychunk.commands.toggle.unclaimed")
                || sender.hasPermission("mychunk.commands.toggle.tnt")
                || sender.hasPermission("mychunk.commands.toggle.expiry")
                || sender.hasPermission("mychunk.commands.toggle.allownether")
                || sender.hasPermission("mychunk.commands.toggle.allowend")
                || sender.hasPermission("mychunk.commands.toggle.notify")
                || sender.hasPermission("mychunk.commands.toggle.firstChunkFree")
                || sender.hasPermission("mychunk.commands.toggle.preventEntry")
                || sender.hasPermission("mychunk.commands.toggle.preventPVP")
                || sender.hasPermission("mychunk.commands.toggle.mobGrief")
                || sender.hasPermission("mychunk.commands.toggle.rampchunkprice")
                || sender.hasPermission("mychunk.commands.toggle.usechatformat"))
            helpLines.add(ChatColor.GOLD + "/mychunk toggle ["+Lang.get("Setting")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageToggle") + "\n" + ChatColor.GRAY + "  "
                    + Lang.get("CommandMessageAvailable") + ":\n   refund overbuy resales neighbours unclaimed tnt expiry\n   allownether allowend notify firstChunkFree preventEntry\n   preventPVP mobGrief rampChunkPrice useChatFormat");
        
        int lines = 0;
        List<Integer> pageLines = new ArrayList<Integer>();
        int pages = 0;
        
        int lineCounter = 0;
        int helpLine = 0;
        
        for (String line : helpLines) {
            helpLine++;
            if (!line.contains("\n")) {
                lines++;
                lineCounter++;
            } else {
                String[] lineSplit = line.split("\n");
                lines++;
                lineCounter+=lineSplit.length;
            }
            if (lineCounter > 7 || helpLine == helpLines.size()) {
                pageLines.add(lines);
                lines = 0;
                lineCounter = 0;
                pages++;
            }
        }
        
        if (page < 1 || page > pages ) {
            page = 1;
        }
        
        PluginDescriptionFile pdfFile = plugin.getDescription();
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "MyChunk v" + ChatColor.WHITE + pdfFile.getVersion() + ChatColor.GOLD + " " + Lang.get("By") + " " + ChatColor.WHITE + "ellbristow");
        sender.sendMessage(ChatColor.GRAY + "==== " + Lang.get("Page") + " " + page + "/" + pages + ChatColor.WHITE + " {"+Lang.get("Optional")+"} ["+Lang.get("Required")+"]");
        
        int firstLine = 0;
        
        for (int i = 1; i < page; i++) {
            firstLine += pageLines.get(i-1);
        }
        
        for (int i = 0; i < pageLines.get(page-1); i++) {
            String line = helpLines.get(firstLine + i);
            if (!line.contains("\n")) {
                sender.sendMessage(line);
            } else {
                String[] lineSplit = line.split(line);
                sender.sendMessage(lineSplit);
            }
        }
        
        if (pages > page) {
            sender.sendMessage(ChatColor.GOLD + Lang.get("NextPage") + ": " + ChatColor.WHITE + "/mychunk help " + (page + 1));
        }

        return true;
        
    }

/*
 _        __      
(_)      / _|     
 _ _ __ | |_ ___  
| | '_ \|  _/ _ \ 
| | | | | || (_) |
|_|_| |_|_| \___/ 

*/
    
    private boolean commandInfo(CommandSender sender, String[] args) {

        if (!sender.hasPermission("mychunk.commands.info")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        int page = 1;
        OfflinePlayer target = Bukkit.getOfflinePlayer(sender.getName());
        
        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("ServerNoChunks"));
            return false;
        }
        
        if (args.length > 1) {
            
            try {
                page = Integer.parseInt(args[1]);
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + Lang.get("ServerNoChunks"));
                    return false;
                }
                
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                
                if (!sender.hasPermission("mychunk.commands.info.others")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                target = Bukkit.getOfflinePlayer(args[1]);
                
                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + Lang.get("Player") + ChatColor.WHITE + " " + args[1] + " " + ChatColor.RED + Lang.get("NotFound") + "!");
                    sender.sendMessage(ChatColor.RED + "/mychunk info {"+Lang.get("Player")+"} {"+Lang.get("Page")+"}");
                    return false;
                }
                
                if (args.length > 2) {
                    
                    try {
                        page = Integer.parseInt(args[2]);

                        if (page < 1) {
                            page = 1;
                        }
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.RED + Lang.get("PageNotNumber"));
                        sender.sendMessage(ChatColor.RED + "/mychunk info {"+Lang.get("Player")+"} {"+Lang.get("Page")+"}");
                        return false;
                    }
                    
                }
                
            }
            
        }
        
        MyChunkChunk[] chunks = MyChunkChunk.getOwnedChunks(target.getName());
        
        if (chunks == null || chunks.length == 0) {
            if (target.getName().equalsIgnoreCase(sender.getName()))
                sender.sendMessage(ChatColor.RED + Lang.get("NoChunksOwned") + "!");
            else
                sender.sendMessage(target.getName() + ChatColor.RED + " " + Lang.get("Doesn'tOwnChunks") + "!");
        } else {
            
            int pages = (int)Math.ceil((double)chunks.length /8);
            if (page > pages)
                page = 1;
            
            if (target.getName().equalsIgnoreCase(sender.getName()))
                sender.sendMessage(ChatColor.GOLD + Lang.get("Your") + " " + Lang.get("OwnedChunks") + ": (" + chunks.length + ") " + Lang.get("Page") + " " + page + "/" + pages);
            else
                sender.sendMessage(target.getName() + Lang.get("'s") + ChatColor.GOLD + " " + Lang.get("OwnedChunks") + ": (" + chunks.length + ") " + Lang.get("Page") + " " + page + "/" + pages);
            
            loop:
            for (int i = 0; i < 8 && i < chunks.length; i++) {
                int get = ((page-1) * 8)+i;
                Chunk thisChunk = Bukkit.getWorld(chunks[get].getWorldName()).getChunkAt(chunks[get].getX(), chunks[get].getZ());
                sender.sendMessage(ChatColor.GOLD + " World: " + ChatColor.GRAY + thisChunk.getWorld().getName() + ChatColor.GOLD + " X: " + ChatColor.GRAY + thisChunk.getBlock(7, 0, 7).getX() + ChatColor.GOLD + " Z: " + ChatColor.GRAY + thisChunk.getBlock(7, 0, 7).getZ());
                if (i >= 8 && get < chunks.length) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("NextPage") + ": " + ChatColor.WHITE + "/mychunk info 2");
                    break loop;
                }
            }

        }
        
        return true;
        
    }

/*
 _ __ ___   __ ___  __
| '_ ` _ \ / _` \ \/ /
| | | | | | (_| |>  < 
|_| |_| |_|\__,_/_/\_\

*/
    
    private boolean commandMax(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.max")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyNewMaxChunks"));
            sender.sendMessage(ChatColor.RED + "/mychunk max ["+Lang.get("NewLimit")+"]");
            return false;
        } else if (args.length == 2) {
            
            int newMax;
            try {
                newMax = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("AmountNotInteger") + " (e.g. 5)");
                sender.sendMessage(ChatColor.RED + "/mychunk max ["+Lang.get("NewLimit")+"]");
                return false;
            }

            plugin.setMaxChunks(newMax);

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageMax") + " " + newMax);
            return true;
            
        }
        return true;
    }
    
/*
             _    __                     _      
            | |  / _|                   | |     
 _ __   ___ | |_| |_ ___  _ __ ___  __ _| | ___ 
| '_ \ / _ \| __|  _/ _ \| '__/ __|/ _` | |/ _ \
| | | | (_) | |_| || (_) | |  \__ \ (_| | |  __/
|_| |_|\___/ \__|_| \___/|_|  |___/\__,_|_|\___|

*/
    
    private boolean commandNotforsale(CommandSender sender) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.notforsale")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();

        MyChunkChunk chunk = new MyChunkChunk(block);

        if (!chunk.getOwner().equalsIgnoreCase(player.getName()) && !(chunk.getOwner().equalsIgnoreCase("server") && player.hasPermission("mychunk.server.signs")) && !(chunk.getOwner().equalsIgnoreCase("public") && player.hasPermission("mychunk.public.signs"))) {

            player.sendMessage(ChatColor.RED + Lang.get("DoNotOwn"));
            return false;

        } else if (!chunk.isForSale()) {

            player.sendMessage(ChatColor.RED + Lang.get("ChunkNotForSale"));
            return false;

        }

        chunk.setNotForSale();
        player.sendMessage(ChatColor.GOLD + Lang.get("ChunkOffSale"));
        
        return true;
        
    }
    
/*
       _                _          
      | |              (_)         
  ___ | |__  _ __  _ __ _  ___ ___ 
 / _ \| '_ \| '_ \| '__| |/ __/ _ \
| (_) | |_) | |_) | |  | | (_|  __/
 \___/|_.__/| .__/|_|  |_|\___\___|
            | |                    
            |_|                    

*/
    
    private boolean commandObprice(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.obprice")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (!MyChunk.getToggle("foundEconomy")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoEcoPlugin"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyNewOverbuyPrice"));
            sender.sendMessage(ChatColor.RED + "/mychunk obprice ["+Lang.get("NewPrice")+"]");
            return false;
        } else if (args.length == 2) {
            
            double newPrice;
            try {
                newPrice = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("PriceNotNumber") + " (e.g. 5.00)");
                sender.sendMessage(ChatColor.RED + "/mychunk obprice ["+Lang.get("NewPrice")+"]");
                return false;
            }

            plugin.setOverbuyPrice(newPrice);

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageOverbuyPrice") + " " + MyChunkVaultLink.economy.format(newPrice));
            return true;
            
        }
        return true;
    }
    
/*
  _____      ___ __   ___ _ __ 
 / _ \ \ /\ / / '_ \ / _ \ '__|
| (_) \ V  V /| | | |  __/ |   
 \___/ \_/\_/ |_| |_|\___|_|   

*/
    
    private boolean commandOwner(CommandSender sender) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.claim")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        // Player attempted to claim a chunk
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
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
        
        return true;
        
    }
    
/*
            _          
           (_)         
 _ __  _ __ _  ___ ___ 
| '_ \| '__| |/ __/ _ \
| |_) | |  | | (_|  __/
| .__/|_|  |_|\___\___|
| |                    
|_|                    


*/

    private boolean commandPrice(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.price")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (!MyChunk.getToggle("foundEconomy")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoEcoPlugin"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyNewChunkPrice"));
            sender.sendMessage(ChatColor.RED + "/mychunk price ["+Lang.get("NewPrice")+"]");
            return false;
        } else if (args.length == 2) {
            
            double newPrice;
            try {
                newPrice = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("PriceNotNumber") + " (e.g. 5.00)");
                sender.sendMessage(ChatColor.RED + "/mychunk price ["+Lang.get("NewPrice")+"]");
                return false;
            }

            plugin.setChunkPrice(newPrice);

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageChunkPrice")+ " " + MyChunkVaultLink.economy.format(newPrice));
            return true;
            
        }
        return true;
    }
    
/*
 _ __  _   _ _ __ __ _  ___ _ __  
| '_ \| | | | '__/ _` |/ _ \ '_ \ 
| |_) | |_| | | | (_| |  __/ |_) |
| .__/ \__,_|_|  \__, |\___| .__/ 
| |               __/ |    | |    
|_|              |___/     |_|    

*/
    
    private boolean commandPurgep(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.purgep")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 1) {
            
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyPurgePlayer"));
            sender.sendMessage(ChatColor.RED + "/mychunk purgep ["+Lang.get("PlayerName")+"]");
            
            return false;
            
        } else if (args.length == 2) {
            
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            
            if (!player.hasPlayedBefore() && !player.isOnline()) {
                
                sender.sendMessage(ChatColor.RED + Lang.get("Player") + ChatColor.WHITE + " " + args[1] + " " + ChatColor.RED + Lang.get("NotFound") + "!");
                return false;
                
            }
            
            HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("world, x, z", "MyChunks", "owner = '"+player.getName()+"'", "", "");
            
            List<Chunk> chunks = new ArrayList<Chunk>();
            
            if (results != null && !results.isEmpty()) {
                
                for (int i  : results.keySet()) {

                    String world = results.get(i).get("world");
                    int x = Integer.parseInt(results.get(i).get("x"));
                    int z = Integer.parseInt(results.get(i).get("z"));
                    chunks.add(Bukkit.getWorld(world).getChunkAt(x, z));

                }

            }
            
            for (Chunk thisChunk: chunks) {
                MyChunkChunk.unclaim(thisChunk);
            }
            
            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageAllChunksFor") + " " + ChatColor.WHITE + player.getName() + " " + ChatColor.GOLD + Lang.get("CommandMessageAreNow") + " " + Lang.get("Unowned"));
            
        }
        
        return true;
    }
    
/*
 _ __  _   _ _ __ __ _  _____      __
| '_ \| | | | '__/ _` |/ _ \ \ /\ / /
| |_) | |_| | | | (_| |  __/\ V  V / 
| .__/ \__,_|_|  \__, |\___| \_/\_/  
| |               __/ |              
|_|              |___/               

*/
    
    private boolean commandPurgew(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.purgew")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 1) {
            
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyPurgeWorld"));
            sender.sendMessage(ChatColor.RED + "/mychunk purgew ["+Lang.get("WorldName")+"]");
            
            return false;
            
        } else if (args.length == 2) {
            
            World world = Bukkit.getWorld(args[1]);
            
            if (world == null) {
                
                sender.sendMessage(ChatColor.RED + Lang.get("World") + " " + ChatColor.WHITE + args[1] + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                return false;
                
            }
            
            String worldName = world.getName();
            
            SQLBridge.query("DELETE FROM MyChunks WHERE world = '"+worldName+"'");
            
            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageAllChunksIn") + " " + ChatColor.WHITE + world.getName() + " " + ChatColor.GOLD + Lang.get("CommandMessageAreNow") + " " + Lang.get("Unowned"));
            
        }
        
        return true;
    }
    
/*
                                     _       
                                    | |      
 _ __ __ _ _ __ ___  _ __  _ __ __ _| |_ ___ 
| '__/ _` | '_ ` _ \| '_ \| '__/ _` | __/ _ \
| | | (_| | | | | | | |_) | | | (_| | ||  __/
|_|  \__,_|_| |_| |_| .__/|_|  \__,_|\__\___|
                    | |                      
                    |_|                      

*/
    
    private boolean commandRampRate(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.ramprate")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 1) {
            
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyRefund"));
            sender.sendMessage(ChatColor.RED + "/mychunk refund ["+Lang.get("Percentage")+"]");
            
            return false;
            
        } else if (args.length == 2) {
            
            double percent;
            try {
                percent = Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + Lang.get("RefundNotNumber"));
                return false;
            }
            plugin.setRefundPercent(percent);
            sender.sendMessage(ChatColor.GOLD + Lang.get("RefundSet") + ChatColor.WHITE + percent + "%");
            
        }
        
        return true;
    }
    
/*
           __                 _ 
          / _|               | |
 _ __ ___| |_ _   _ _ __   __| |
| '__/ _ \  _| | | | '_ \ / _` |
| | |  __/ | | |_| | | | | (_| |
|_|  \___|_|  \__,_|_| |_|\__,_|


*/
    
    private boolean commandRefund(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.refund")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (!MyChunk.getToggle("foundEconomy")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoEcoPlugin"));
            return false;
        } else {
            double newRate;
            try {
                newRate = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("RateNotNumber") + " (e.g. 5.00)");
                sender.sendMessage(ChatColor.RED + "/mychunk rampRate {"+Lang.get("NewRampRate")+"}");
                return false;
            }
            
            plugin.setRampRate(newRate);
            sender.sendMessage(ChatColor.GOLD + Lang.get("RampRateSet") + " " + newRate);
        }
        
        return true;
    }
    
/*
          _                 _ 
         | |               | |
 _ __ ___| | ___   __ _  __| |
| '__/ _ \ |/ _ \ / _` |/ _` |
| | |  __/ | (_) | (_| | (_| |
|_|  \___|_|\___/ \__,_|\__,_|

*/
    
    private boolean commandReload(CommandSender sender) {
        
        if (!sender.hasPermission("mychunk.commands.reload")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }

        plugin.reload();
        sender.sendMessage(ChatColor.GOLD + Lang.get("Reloaded"));
        
        return true;
    }
    
/*
 _                    _      
| |                  | |     
| |_ ___   __ _  __ _| | ___ 
| __/ _ \ / _` |/ _` | |/ _ \
| || (_) | (_| | (_| | |  __/
 \__\___/ \__, |\__, |_|\___|
           __/ | __/ |       
          |___/ |___/        

*/
    
    private boolean commandToggle(CommandSender sender, String[] args) {
        
        if (args.length == 1) {
            
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyToggle"));
            sender.sendMessage(ChatColor.RED + "/mychunk toggle [refund | overbuy | neighbours | resales | unclaimed | expiry | allownether | allowend | notify | firstChunkFree | preventEntry | preventPVP | mobGrief | useChatFormat ]");
            
            return false;
            
        } else if (args.length >= 2) {
            
            if (args[1].equalsIgnoreCase("refund") || args[1].equalsIgnoreCase("unclaimRefund")) {
                
                /* refund || unclaimRefund */
                
                if (!sender.hasPermission("mychunk.commands.toggle.refund")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                if (!MyChunk.getToggle("foundEconomy")) {
                    
                    sender.sendMessage(ChatColor.RED + Lang.get("NoEcoPlugin"));
                    return false;
                    
                }

                plugin.toggleSetting("unclaimRefund");
                
                if (MyChunk.getToggle("unclaimRefund")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleRefundOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleRefundOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("overbuy") || args[1].equalsIgnoreCase("allowOverbuy")) {
                
                /* overbuy || allowOverbuy */
                
                if (!sender.hasPermission("mychunk.commands.toggle.overbuy")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                if (!MyChunk.getToggle("foundEconomy")) {
                    
                    sender.sendMessage(ChatColor.RED + Lang.get("NoEcoPlugin"));
                    return false;
                    
                }
                    
                plugin.toggleSetting("allowOverbuy");
                
                if (MyChunk.getToggle("allowOverbuy")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleOverbuyOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleOverbuyOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("resales")) {
                
                /* resales */
                
                if (!sender.hasPermission("mychunk.commands.toggle.resales")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                if (!MyChunk.getToggle("foundEconomy")) {
                    
                    sender.sendMessage(ChatColor.RED + Lang.get("NoEcoPlugin"));
                    return false;
                    
                }
                
                if (!MyChunk.getToggle("allowOverbuy")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoOverbuy"));
                    return false;
                }
                    
                plugin.toggleSetting("overbuyP2P");
                
                if (MyChunk.getToggle("overbuyP2P")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleResalesOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleResalesOff"));
                }
                return true;

            } else if (args[1].equalsIgnoreCase("neighbours") || args[1].equalsIgnoreCase("allowNeighbours") || args[1].equalsIgnoreCase("neighbors") || args[1].equalsIgnoreCase("allowNeighbors")) {
                
                /* neighbours || allowNeighbours || neighbors || allowNeighbors */
                
                if (!sender.hasPermission("mychunk.commands.toggle.neighbours") || !sender.hasPermission("mychunk.commands.toggle.neighbors")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                    
                plugin.toggleSetting("allowNeighbours");
                
                if (MyChunk.getToggle("allowNeighbours")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleNeighboursOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleNeighboursOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("unclaimed") || args[1].equalsIgnoreCase("protectUnclaimed")) {
                
                /* unclaimed || protectUnclaimed */
                
                if (!sender.hasPermission("mychunk.commands.toggle.unclaimed")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                    
                plugin.toggleSetting("protectUnclaimed");
                
                if (MyChunk.getToggle("protectUnclaimed")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleUnclaimedOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleUnclaimedOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("tnt") || args[1].equalsIgnoreCase("unclaimedTNT")) {
                
                /* tnt || unclaimedTNT */
                
                if (!sender.hasPermission("mychunk.commands.toggle.tnt")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                if (!MyChunk.getToggle("protectUnclaimed")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("NoUnclaimed"));
                    return false;
                }
                
                plugin.toggleSetting("unclaimedTNT");
                
                if (MyChunk.getToggle("unclaimedTNT")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleTNTOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleTNTOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("expiry") || args[1].equalsIgnoreCase("useClaimExpiry")) {
                
                /* expiry || useClaimExpiry */
                
                if (!sender.hasPermission("mychunk.commands.toggle.expiry")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("useClaimExpiry");
                
                if (MyChunk.getToggle("useClaimExpiry")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleExpiryOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleExpiryOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("allownether")) {
                
                /* allowNether */
                
                if (!sender.hasPermission("mychunk.commands.toggle.allownether")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("allowNether");
                
                if (MyChunk.getToggle("allowNether")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleNetherCan"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleNetherCannot"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("allowend")) {
                
                /* allowEnd */
                
                if (!sender.hasPermission("mychunk.commands.toggle.allowend")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("allowEnd");
                
                if (MyChunk.getToggle("allowEnd")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleEndCan"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleEndCannot"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("notify")) {
                
                /* notify */
                
                if (!sender.hasPermission("mychunk.commands.toggle.notify")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("ownerNotifications");
                
                if (MyChunk.getToggle("ownerNotifications")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleNotifyOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleNotifyOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("firstChunkFree")) {
                
                /* firstChunkFree */
                
                if (!sender.hasPermission("mychunk.commands.toggle.firstchunkfree")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("firstChunkFree");
                
                if (MyChunk.getToggle("firstChunkFree")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleFirstChunkFreeOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleFirstChunkFreeOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("rampChunkPrice")) {
                
                /* rampChunkPrice */
                
                if (!sender.hasPermission("mychunk.commands.toggle.rampchunkprice")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("rampChunkPrice");
                
                if (MyChunk.getToggle("rampChunkPrice")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleRampChunkPriceOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleRampChunkPriceOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("preventEntry")) {
                
                /* preventEntry */
                
                if (!sender.hasPermission("mychunk.commands.toggle.preventEntry")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("preventEntry");
                
                if (MyChunk.getToggle("preventEntry")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleEntryOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleEntryOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("preventPVP")) {
                
                /* preventPVP */
                
                if (!sender.hasPermission("mychunk.commands.toggle.preventPVP")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("preventPVP");
                
                if (MyChunk.getToggle("preventPVP")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("TogglePVPOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("TogglePVPOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("mobGrief")) {
                
                /* mobGrief */
                
                if (!sender.hasPermission("mychunk.commands.toggle.mobGrief")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("allowMobGrief");
                
                if (MyChunk.getToggle("allowMobGrief")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleMobGriefOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleMobGriefOff"));
                }
                return true;
                
            } else if (args[1].equalsIgnoreCase("useChatFormat")) {
                
                /* mobGrief */
                
                if (!sender.hasPermission("mychunk.commands.toggle.useChatFormat")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                plugin.toggleSetting("useChatFormat");
                
                if (MyChunk.getToggle("useChatFormat")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleChatFormatOn"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("ToggleChatFormatOff"));
                }
                return true;
                
            }
            
            sender.sendMessage(ChatColor.RED + "/mychunk toggle [refund | overbuy | neighbours | resales | unclaimed | expiry | allownether | allowend | notify | firstChunkFree | preventEntry | preventPVP | mobGrief | useChatFormat ]");
            return false;
            
        }
        
        return true;
        
    }
    
/*
                  _       _           
                 | |     (_)          
 _   _ _ __   ___| | __ _ _ _ __ ___  
| | | | '_ \ / __| |/ _` | | '_ ` _ \ 
| |_| | | | | (__| | (_| | | | | | | |
 \__,_|_| |_|\___|_|\__,_|_|_| |_| |_|

*/
    
    private boolean commandUnclaim(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.unclaim")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        // Player attempted to claim a chunk
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);

        if (!chunk.isClaimed()) {

            player.sendMessage(ChatColor.RED + Lang.get("ChunkNotOwned"));            
            return false;

        }

        String owner = chunk.getOwner();

        if (!owner.equalsIgnoreCase(player.getName())) {

            if (owner.equalsIgnoreCase("server") && !player.hasPermission("mychunk.unclaim.server")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimServer"));
                return false;

            } else if (owner.equalsIgnoreCase("public") && !player.hasPermission("mychunk.unclaim.public")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimPublic"));
                return false;

            } else if (!owner.equalsIgnoreCase("server") && !owner.equalsIgnoreCase("public") && !player.hasPermission("mychunk.unclaim.others")) {

                player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimOther"));
                return false;

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
        
        return true;
        
    }
    
/*
                  _       _                                
                 | |     (_)                               
 _   _ _ __   ___| | __ _ _ _ __ ___   __ _ _ __ ___  __ _ 
| | | | '_ \ / __| |/ _` | | '_ ` _ \ / _` | '__/ _ \/ _` |
| |_| | | | | (__| | (_| | | | | | | | (_| | | |  __/ (_| |
 \__,_|_| |_|\___|_|\__,_|_|_| |_| |_|\__,_|_|  \___|\__,_|

*/
    
    private boolean commandUnclaimarea(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.unclaimarea")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        // Player attempted to claim a chunk
        Player player = (Player)sender;
        Block block = player.getLocation().getBlock();
        
        String correctName;
        
        boolean isServer = false;
        boolean isPublic = false;
        boolean isOther = false;
        
        String targetName = player.getName();
        
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("server")) {
                isServer = true;
                targetName = "Server";
            } else if (args[1].equalsIgnoreCase("public")) {
                isPublic = true;
                targetName = "Public";
            } else if (!args[1].equalsIgnoreCase(player.getName())) {
                isOther = true;
                targetName = args[1];
            }
        }

        if (!isServer && !isPublic && !isOther) {

            correctName = player.getName();

        } else {

            if (isServer) {

                if (!player.hasPermission("mychunk.unclaim.server")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimServer"));
                    return false;

                } else {
                    correctName = "Server";
                }

            } else if (isPublic) {

                if (!player.hasPermission("mychunk.unclaim.public")) {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimPublic"));
                    return false;

                } else {
                    correctName = "Public";
                }

            } else {

                if (player.hasPermission("mychunk.unclaim.others")) {

                    OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(targetName);
                    if (!target.hasPlayedBefore()) {

                        player.sendMessage(ChatColor.RED + Lang.get("Player") + " " + ChatColor.WHITE + targetName + ChatColor.RED + " " + Lang.get("NotFound") + "!");
                        return false;

                    } else {
                        correctName = target.getName();
                    }

                } else {

                    player.sendMessage(ChatColor.RED + Lang.get("NoPermsUnclaimOther"));
                    return false;

                }
            }

        }

        if (args.length > 2 && args[2].equalsIgnoreCase("cancel")) {

            SignListener.pendingUnclaims.remove(correctName);
            player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaCancelled"));
            return false;

        }

        if (!SignListener.pendingUnclaims.containsKey(correctName)) {

            SignListener.pendingUnclaims.put(correctName, block);
            player.sendMessage(ChatColor.GOLD + Lang.get("StartUnclaimArea1"));
            player.sendMessage(ChatColor.GOLD + Lang.get("StartUnclaimArea2"));

        } else {

            Block startBlock = SignListener.pendingUnclaims.get(correctName);

            if (startBlock.getWorld() != block.getWorld()) {
                player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaWorldError"));
                return false;
            }

            Chunk startChunk = startBlock.getChunk();
            SignListener.pendingUnclaims.remove(correctName);
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
                        return false;

                    }

                }

            }

            if (foundChunks.isEmpty()) {
                player.sendMessage(ChatColor.RED + Lang.get("UnclaimAreaNoneFound"));
                return false;
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
        
        return true;
        
    }
    
/*
                    _     _ 
                   | |   | |
__      _____  _ __| | __| |
\ \ /\ / / _ \| '__| |/ _` |
 \ V  V / (_) | |  | | (_| |
  \_/\_/ \___/|_|  |_|\__,_|


*/
    
    private boolean commandWorld(CommandSender sender, String[] args) {
        
        if (args.length == 1) {
            
            /*
             * Command not complete
             */
            
            if (!sender.hasPermission("mychunk.commands.world.list") && !sender.hasPermission("mychunk.commands.world.enable") && !sender.hasPermission("mychunk.commands.world.disable")) {
                sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                return false;
            }
            
            sender.sendMessage(ChatColor.RED + " /mychunk world [enable|disable|list] {World Name}");
            
            return false;
            
        } else if (args.length == 2) {
            
            /*
             * Listing, Enabling or Disabling current worlds
             */
            
            if (args[1].equalsIgnoreCase("list")) {
                
                if (!sender.hasPermission("mychunk.commands.world.list")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                sender.sendMessage(ChatColor.GOLD + "MyChunk World List");
                for (World world : Bukkit.getWorlds()) {
                    if (MyChunk.isWorldEnabled(world.getName())) {
                        sender.sendMessage(ChatColor.GOLD + world.getName());
                    }
                }
                
                return true;
            }
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
                return false;
            }
            
            if (args[1].equalsIgnoreCase("enable")) {
                
                if (!sender.hasPermission("mychunk.commands.world.enable")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                Player player = (Player) sender;
                String worldName = player.getWorld().getName();
                MyChunk.enableWorld(worldName);
                sender.sendMessage(ChatColor.GOLD + Lang.get("WorldEnabled") + ": " + worldName);
                
                return true;
                
            } else if (args[1].equalsIgnoreCase("disable")) {
                
                if (!sender.hasPermission("mychunk.commands.world.disable")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                Player player = (Player) sender;
                String worldName = player.getWorld().getName();
                MyChunk.disableWorld(worldName);
                sender.sendMessage(ChatColor.GOLD + Lang.get("WorldDisabled") + ": " + worldName);
                
                return true;
                
            }
            
            sender.sendMessage(ChatColor.RED + " /mychunk world [enable|disable|list] {World Name}");
            
            return false;
            
        } else if (args.length == 3) {
            
            /*
             * Enabling or Disabling specified worlds
             */
            
            if (args[1].equalsIgnoreCase("enable")) {
                
                if (!sender.hasPermission("mychunk.commands.world.enable")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                if (args[2].equalsIgnoreCase("all")) {
                    
                    if (!sender.hasPermission("mychunk.commands.world.enable.all")) {
                        sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                        return false;
                    }
                    
                    MyChunk.enableAllWorlds();
                    
                    sender.sendMessage(ChatColor.GOLD + Lang.get("AllWorldsEnabled"));
                    
                } else {
                    
                    if (Bukkit.getWorld(args[2]) == null) {
                        sender.sendMessage(ChatColor.RED + Lang.get("CannotFindWorld") + ChatColor.WHITE + " " + args[2] + ChatColor.RED + "!");
                        return false;
                    }
                    
                    MyChunk.enableWorld(args[2]);
                    
                    sender.sendMessage(ChatColor.GOLD + Lang.get("WorldEnabled") + ": " + args[2]);

                }
                
                return true;
                
            } else if (args[1].equalsIgnoreCase("disable")) {
                
                if (!sender.hasPermission("mychunk.commands.world.disable")) {
                    sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                    return false;
                }
                
                if (args[2].equalsIgnoreCase("all")) {
                    if (!sender.hasPermission("mychunk.commands.world.disable.all")) {
                        sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
                        return false;
                    }
                    
                    MyChunk.disableAllWorlds();
                    
                    sender.sendMessage(ChatColor.GOLD + Lang.get("AllWorldsDisabled"));
                    
                } else {
                    
                    if (Bukkit.getWorld(args[2]) == null) {
                        sender.sendMessage(ChatColor.RED + Lang.get("CannotFindWorld") + ChatColor.WHITE + " " + args[2] + ChatColor.RED + "!");
                        return false;
                    }
                    
                    MyChunk.disableWorld(args[2]);
                    
                    sender.sendMessage(ChatColor.GOLD + Lang.get("WorldDisabled") + ": " + args[2]);

                }
                
                return true;
                
            }

        }
        
        sender.sendMessage(ChatColor.RED + " /mychunk world [enable|disable|list] {World Name}");
        
        return false;
        
    }
    
}