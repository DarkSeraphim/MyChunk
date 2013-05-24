package me.ellbristow.mychunk;

import java.util.HashMap;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.utils.SQLiteBridge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public abstract class MyChunkCommands {

    private static MyChunk plugin;

    static {
        plugin = (MyChunk) Bukkit.getPluginManager().getPlugin("MyChunk");
    }

    public static boolean runCommand(CommandSender sender, String cmd, String[] args) {
        
        if (cmd.equalsIgnoreCase("mychunk")) {
            
            if (args.length == 0) {
                return commandMyChunk(sender, 0);
            }

            try {
                int page = Integer.parseInt(args[0]);
                return commandMyChunk(sender, page);
            } catch (NumberFormatException e) {}

            if (args[0].equalsIgnoreCase("flags")) {
                return commandFlags(sender);
            }
            
            if (args[0].equalsIgnoreCase("world")) {
                return commandWorld(sender, args);
            }
            
        }
        
        // Command Not Found
        return false;
        
    }

    private static boolean commandMyChunk(CommandSender sender, int page) {

        if (!sender.hasPermission("mychunk.commands.stats")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }

        PluginDescriptionFile pdfFile = plugin.getDescription();

        sender.sendMessage(ChatColor.GOLD + "MyChunk v" + ChatColor.WHITE + pdfFile.getVersion() + ChatColor.GOLD + " " + Lang.get("By") + " " + ChatColor.WHITE + "ellbristow");
        if (page != 2) {
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
            String claimExpiry;
            if (!MyChunk.getToggle("useclaimexpiry")) {
                claimExpiry = Lang.get("Disabled");
            } else {
                claimExpiry = MyChunk.getIntSetting("claimexpirydays") + " " + Lang.get("DaysWithoutLogin");
            }
            sender.sendMessage(ChatColor.GOLD + Lang.get("ClaimExpiry") + ": " + ChatColor.WHITE + claimExpiry);
            sender.sendMessage(ChatColor.GRAY + Lang.get("NextPage") + ": /mychunk 2");
            
            return true;
            
        } else {
            
            sender.sendMessage(ChatColor.GOLD + Lang.get("AllowMobGrief") + ": " + ChatColor.WHITE + (MyChunk.getToggle("allowmobgrief") ? Lang.get("Yes") : Lang.get("No")) + "  " + ChatColor.GOLD + Lang.get("PreventPVP") + ": " + ChatColor.WHITE + (MyChunk.getToggle("preventpvp") ? Lang.get("Yes") : Lang.get("No")));
            sender.sendMessage(ChatColor.GOLD + Lang.get("OwnerNotifications") + ": " + ChatColor.WHITE + (MyChunk.getToggle("ownerNotifications") ? Lang.get("Yes") : Lang.get("No")) + " " + ChatColor.GOLD + Lang.get("PreventEntry") + ": " + ChatColor.WHITE + (MyChunk.getToggle("prevententry") ? Lang.get("Yes") : Lang.get("No")));
            sender.sendMessage(ChatColor.GOLD + Lang.get("AllowNeighbours") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allowneighbours")) + ChatColor.GOLD + "  " + Lang.get("ProtectUnclaimed") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("protectunclaimed")));
            sender.sendMessage(ChatColor.GOLD + Lang.get("AllowNether") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allownether")) + "  " + ChatColor.GOLD + Lang.get("AllowEnd") + ": " + ChatColor.WHITE + Lang.get("" + MyChunk.getToggle("allowend")));
            
            return true;
            
        }

    }
    
    private static boolean commandFlags(CommandSender sender) {
        
        if (!sender.hasPermission("mychunk.commands.flags")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        sender.sendMessage(ChatColor.GOLD + "MyChunk "+Lang.get("PermissionFlags"));
        sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GOLD + " = "+Lang.get("All")+" | " + ChatColor.GREEN + "B" + ChatColor.GOLD + " = "+Lang.get("Build")+" | " + ChatColor.GREEN + "C" + ChatColor.GOLD + " = "+Lang.get("AccessChests")+" | " + ChatColor.GREEN + "D"  + ChatColor.GOLD + " = "+Lang.get("Destroy"));
        sender.sendMessage(ChatColor.GREEN + "E" + ChatColor.GOLD + Lang.get("Enter") + " | " + ChatColor.GREEN + "I"  + ChatColor.GOLD + " = "+Lang.get("IgniteBlocks")+" | " + ChatColor.GREEN + "L"  + ChatColor.GOLD + " = "+Lang.get("DropLava") );
        sender.sendMessage(ChatColor.GREEN + "O"  + ChatColor.GOLD + " = "+Lang.get("OpenWoodenDoors") + " | " + ChatColor.GREEN + "U"  + ChatColor.GOLD + " = "+Lang.get("UseButtonsLevers")+" | " + ChatColor.GREEN + "W"  + ChatColor.GOLD + " = "+Lang.get("DropWater"));
        return true;
        
    }
    
    private static boolean commandWorld(CommandSender sender, String[] args) {
        
        if (args.length == 3) {
            
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
                    
                } else {
                    
                    if (Bukkit.getWorld(args[2]) == null) {
                        sender.sendMessage(ChatColor.RED + Lang.get("CannotFindWorld") + ChatColor.WHITE + " " + args[2] + ChatColor.RED + "!");
                        return false;
                    }
                    
                    MyChunk.enableWorld(args[2]);

                }
                
                if (args[2].equalsIgnoreCase("all")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("AllWorldsEnabled"));
                } else {
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
                    
                } else {
                    
                    if (Bukkit.getWorld(args[2]) == null) {
                        sender.sendMessage(ChatColor.RED + Lang.get("CannotFindWorld") + ChatColor.WHITE + " " + args[2] + ChatColor.RED + "!");
                        return false;
                    }
                    
                    MyChunk.disableWorld(args[2]);

                }
                
                if (args[2].equalsIgnoreCase("all")) {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("AllWorldsDisabled"));
                } else {
                    sender.sendMessage(ChatColor.GOLD + Lang.get("WorldDisabled") + ": " + args[2]);
                }
                
                return true;
                
            }
            
            sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /mychunk world " + ChatColor.WHITE + args[1]);
            
            return false;

        }
        
        sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /mychunk " + ChatColor.WHITE + args[0]);
        
        return false;
        
    }
    
}
