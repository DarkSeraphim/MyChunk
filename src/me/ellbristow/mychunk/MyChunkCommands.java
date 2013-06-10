package me.ellbristow.mychunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.utils.SQLiteBridge;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class MyChunkCommands implements CommandExecutor {

    private MyChunk plugin;
    
    public MyChunkCommands(MyChunk instance) {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        
        if (cmd.equalsIgnoreCase("mychunk")) {
            
            if (args.length == 0) {
                return commandMyChunk(sender, 0);
            }

            try {
                int page = Integer.parseInt(args[0]);
                return commandMyChunk(sender, page);
            } catch (NumberFormatException e) {}
            
            if (args[0].equalsIgnoreCase("expirydays")) {
                return commandExpiryDays(sender, args);
            }
            
            if (args[0].equalsIgnoreCase("flags")) {
                return commandFlags(sender);
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
            
            if (args[0].equalsIgnoreCase("obprice")) {
                return commandObprice(sender, args);
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
            
            if (args[0].equalsIgnoreCase("world")) {
                return commandWorld(sender, args);
            }
            
            // Command Not Found
            sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /mychunk " + args[0]);
            sender.sendMessage(ChatColor.RED + Lang.get("Try") + " /mychunk help");
            
            return false;
            
        }
        
        sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /" + cmd);
        
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
            
            HashMap<Integer, HashMap<String, Object>> result = SQLiteBridge.select("COUNT(*) AS counter", "MyChunks", "", "", "");
            Object count = result.get(0).get("counter");
            
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
                sender.sendMessage(ChatColor.GOLD + Lang.get("ChunkPrice") + ": " + ChatColor.WHITE + MyChunkVaultLink.getEconomy().format(thisPrice) + " " + ChatColor.GOLD + Lang.get("UnclaimRefunds") + ": " + ChatColor.WHITE + (MyChunk.getToggle("unclaimrefund") && MyChunk.getDoubleSetting("refundPercent") != 0 ? MyChunk.getDoubleSetting("refundPercent") + "%" : "No"));
                String overFee = "";
                
                if (MyChunk.getToggle("allowoverbuy")) {
                    String resales = "exc.";
                    if (MyChunk.getToggle("overbuyp2p")) {
                        resales = "inc.";
                    }
                    overFee = " " + ChatColor.GOLD + Lang.get("OverbuyFee") + ": " + ChatColor.WHITE + MyChunkVaultLink.getEconomy().format(MyChunk.getDoubleSetting("overbuyprice")) + "(" + resales + " " + Lang.get("Resales") + ")";
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
        sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GOLD + " = "+Lang.get("All")+" | " + ChatColor.GREEN + "B" + ChatColor.GOLD + " = "+Lang.get("Build"));
        sender.sendMessage(ChatColor.GREEN + "C" + ChatColor.GOLD + " = "+Lang.get("AccessChests")+" | " + ChatColor.GREEN + "D"  + ChatColor.GOLD + " = "+Lang.get("Destroy"));
        sender.sendMessage(ChatColor.GREEN + "E" + ChatColor.GOLD + " = "+Lang.get("Enter") + " | " + ChatColor.GREEN + "I"  + ChatColor.GOLD + " = "+Lang.get("IgniteBlocks"));
        sender.sendMessage(ChatColor.GREEN + "L"  + ChatColor.GOLD + " = "+Lang.get("DropLava") + " | " + ChatColor.GREEN + "O" + ChatColor.GOLD + " = "+Lang.get("OpenWoodenDoors"));
        sender.sendMessage(ChatColor.GREEN + "U"  + ChatColor.GOLD + " = "+Lang.get("UseButtonsLevers")+" | " + ChatColor.GREEN + "W"  + ChatColor.GOLD + " = "+Lang.get("DropWater"));
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
        
        if (sender.hasPermission("mychunk.commands.stats"))
            helpLines.add(ChatColor.GOLD + "/mychunk {"+Lang.get("Page")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageStats"));
        
        if (sender.hasPermission("mychunk.commands.help"))
            helpLines.add(ChatColor.GOLD + "/mychunk help {"+Lang.get("Page")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageHelp"));
        
        if (sender.hasPermission("mychunk.commands.info"))
            helpLines.add(ChatColor.GOLD + "/mychunk info {"+Lang.get("Player")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageInfo"));
        
        if (sender.hasPermission("mychunk.commands.flags"))
            helpLines.add(ChatColor.GOLD + "/mychunk flags: " + ChatColor.GRAY + Lang.get("CommandMessageFlags"));
        
        if (sender.hasPermission("mychunk.commands.allowmobs"))
            helpLines.add(ChatColor.GOLD + "/mychunk allowmobs ["+Lang.get("On")+"|"+Lang.get("Off")+"]: " + "\n" + ChatColor.GRAY + "  " + Lang.get("CommandMessageAllowmobs"));
        
        if (sender.hasPermission("mychunk.commands.allowpvp"))
            helpLines.add(ChatColor.GOLD + "/mychunk allowpvp ["+Lang.get("On")+"|"+Lang.get("Off")+"]: " + "\n" + ChatColor.GRAY + "  " + Lang.get("CommandMessageAllowpvp"));
        
        if (sender.hasPermission("mychunk.commands.reload"))
            helpLines.add(ChatColor.GOLD + "/mychunk reload: " + ChatColor.GRAY + Lang.get("CommandMessageReload"));
        
        if (sender.hasPermission("mychunk.commands.max"))
            helpLines.add(ChatColor.GOLD + "/mychunk max ["+Lang.get("Limit")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageMax"));
        
        if (MyChunk.getToggle("foundEconomy") && sender.hasPermission("mychunk.commands.obprice"))
            helpLines.add(ChatColor.GOLD + "/mychunk obprice ["+Lang.get("Price")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageObprice"));
        
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
                || sender.hasPermission("mychunk.commands.toggle.rampchunkprice"))
            helpLines.add(ChatColor.GOLD + "/mychunk toggle ["+Lang.get("Setting")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageToggle") + "\n" + ChatColor.GRAY + "  "
                    + Lang.get("CommandMessageAvailable") + ":\n   refund overbuy resales neighbours unclaimed tnt expiry\n   allownether allowend notify firstChunkFree preventEntry\n   preventPVP mobGrief rampChunkPrice");
        
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

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageOverbuyPrice") + " " + MyChunkVaultLink.getEconomy().format(newPrice));
            return true;
            
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

            sender.sendMessage(ChatColor.GOLD + Lang.get("CommandMessageChunkPrice")+ " " + MyChunkVaultLink.getEconomy().format(newPrice));
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
            
            HashMap<Integer, HashMap<String, Object>> results = SQLiteBridge.select("world, x, z","MyChunks","owner = '"+player.getName()+"'","","");
            List<Chunk> chunks = new ArrayList<Chunk>();
            
            for (int i = 0; i < results.size(); i++) {
                
                HashMap<String, Object> result = results.get(i);
                String world = (String)result.get("world");
                int x = Integer.parseInt(result.get("x")+"");
                int z = Integer.parseInt(result.get("z")+"");
                chunks.add(Bukkit.getWorld(world).getChunkAt(x, z));
                
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
            SQLiteBridge.query("DELETE FROM MyChunks WHERE world = '"+worldName+"'");
            
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
                // TODO: Lang
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
            sender.sendMessage(ChatColor.RED + "/mychunk toggle [refund | overbuy | neighbours | resales | unclaimed | expiry | allownether | allowend | notify | firstChunkFree | preventEntry | preventPVP | mobGrief ]");
            
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
                
            }
            
            sender.sendMessage(ChatColor.RED + "/mychunk toggle [refund | overbuy | neighbours | resales | unclaimed | expiry | allownether | allowend | notify | firstChunkFree | preventEntry | preventPVP | mobGrief ]");
            return false;
            
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