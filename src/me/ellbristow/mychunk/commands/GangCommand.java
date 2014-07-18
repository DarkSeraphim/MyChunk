package me.ellbristow.mychunk.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.ellbristow.mychunk.MyChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import me.ellbristow.mychunk.ganglands.GangLands;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.utils.FactionsHook;
import me.ellbristow.mychunk.utils.MyChunkVaultLink;
import me.ellbristow.mychunk.utils.TownyHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class GangCommand implements CommandExecutor {
    
    private MyChunk plugin;
    
    public GangCommand(MyChunk instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
        
        if (!command.getName().equalsIgnoreCase("gang")) {    
            
            sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /" + cmd);
        
            return false;
        }
        
        if (args.length == 0) {
            return commandStatus(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("claim")) {
            return commandClaim(sender);
        }
        
        if (args[0].equalsIgnoreCase("create")) {
            return commandCreate(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("disband")) {
            return commandDisband(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            return commandHelp(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("invite")) {
            return commandInvite(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("join")) {
            return commandJoin(sender, args);
        }
        
        if (args[0].equalsIgnoreCase("leave")) {
            return commandLeave(sender);
        }
        
        if (args[0].equalsIgnoreCase("unclaim")) {
            return commandUnclaim(sender);
        }
        
        if (GangLands.isGang(args[0])) {
            return commandStatus(sender, args);
        }
        
        // Command Not Found
        sender.sendMessage(ChatColor.RED + Lang.get("CommandNotRecognised") + " /gang " + args[0]);
        sender.sendMessage(ChatColor.RED + Lang.get("Try") + " /gang help");

        return false;
        
    }
    
/*
     _        _             
    | |      | |            
 ___| |_ __ _| |_ _   _ ___ 
/ __| __/ _` | __| | | / __|
\__ \ || (_| | |_| |_| \__ \
|___/\__\__,_|\__|\__,_|___/

*/
    
    private boolean commandStatus(CommandSender sender, String[] args) {
        
        if (!sender.hasPermission("mychunk.commands.gang.status")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        if (args.length == 0) {
            
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
                return false;
            }

        }
        
        Player player = (Player)sender;
        
        String gangName = args.length == 0 ? GangLands.getGang(player) : args[0] ;
        
        if (gangName.equals("")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NotInGang"));
            return true;
        }
        
        HashMap<String, String> gang = GangLands.getGangDetailed(gangName);
        
        sender.sendMessage(ChatColor.GOLD + Lang.get("GangName") + ChatColor.GRAY + ": " + gang.get("gangName"));
        sender.sendMessage(ChatColor.GOLD + Lang.get("Boss") + ChatColor.GRAY + ": " + gang.get("boss"));
        sender.sendMessage(ChatColor.GOLD + Lang.get("Assistants") + ChatColor.GRAY + ": " + gang.get("assistants"));
        sender.sendMessage(ChatColor.GOLD + Lang.get("Members") + ChatColor.GRAY + ": " + gang.get("members"));
        String[] members = gang.get("members").toString().replaceAll("\\}", "").replaceFirst("\\{", "").split("\\{");
        sender.sendMessage(ChatColor.GOLD + Lang.get("Influence") + ChatColor.GRAY + ": " + (MyChunk.getIntSetting("gangMultiplier") * members.length - Integer.parseInt(gang.get("damage"))));
        sender.sendMessage(ChatColor.GOLD + Lang.get("Invites") + ChatColor.GRAY + ": " + gang.get("invites"));
        sender.sendMessage(ChatColor.GOLD + Lang.get("Allys") + ChatColor.GRAY + ": " + gang.get("allys"));
        sender.sendMessage(ChatColor.GOLD + Lang.get("Enemies") + ChatColor.GRAY + ": " + gang.get("enemies"));
        
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
    
    private boolean commandClaim(CommandSender sender) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.gang.claim")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        String gangName = GangLands.getGang(player);
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);

        if (chunk.isClaimed()) {

            String owner = chunk.getOwner();

            if (owner.equalsIgnoreCase(gangName)) {

                player.sendMessage(ChatColor.RED + Lang.get("AlreadyOwner"));
                return false;

            } else if (!chunk.isForSale()) {

                player.sendMessage(ChatColor.RED + Lang.get("AlreadyOwned") + " " + ChatColor.WHITE + owner + ChatColor.RED + "!");
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
        
        HashMap<String, String> gang = GangLands.getGangDetailed(gangName);
        String[] members = gang.get("members").toString().replaceAll("\\}", "").replaceFirst("\\{", "").split("\\{");
        
        
        int gangMax = MyChunk.getIntSetting("gangMultiplier") * members.length - Integer.parseInt(gang.get("damage"));
        int gangClaimed = MyChunkChunk.getOwnedChunkCount(gangName);

        if (gangMax != 0 && gangClaimed >= gangMax) {
            
            player.sendMessage(ChatColor.RED + Lang.get("MaxChunksReached") + " (" + gangMax + ")!");
            return false;

        }

        double claimPrice = chunk.getClaimPrice();
        
        if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
            int ramp = gangClaimed;
            if (MyChunk.getToggle("firstChunkFree") && gangClaimed > 0) {
                ramp--;
            }
            claimPrice += MyChunk.getDoubleSetting("priceRampRate") * ramp;
        }
        
        if (claimPrice != 0 && MyChunkVaultLink.economy.getBalance("gang-" + gangName) < claimPrice) {

            player.sendMessage(ChatColor.RED + Lang.get("GangCantAfford") + " (" + Lang.get("Price") + ": " + ChatColor.WHITE + MyChunkVaultLink.economy.format(claimPrice) + ChatColor.RED + ")!");
            return false;

        }

        MyChunkVaultLink.economy.withdrawPlayer("gang-" + gangName, claimPrice);
        player.sendMessage(MyChunkVaultLink.economy.format(claimPrice) + ChatColor.GOLD + " " + Lang.get("AmountDeducted"));

        if (chunk.isForSale()) {

            if (claimPrice != 0) {
                MyChunkVaultLink.economy.depositPlayer(chunk.getOwner(), claimPrice);
            }
            OfflinePlayer oldOwner = Bukkit.getServer().getOfflinePlayer(chunk.getOwner());

            if (oldOwner.isOnline()) {
                if (claimPrice != 0) {
                    oldOwner.getPlayer().sendMessage(gangName + ChatColor.GOLD + " " + Lang.get("BoughtFor") + " " + ChatColor.WHITE + MyChunkVaultLink.economy.format(claimPrice) + ChatColor.GOLD + "!");
                } else {
                    oldOwner.getPlayer().sendMessage(gangName + ChatColor.GOLD + " " + Lang.get("ClaimedYourChunk") + "!");
                }
            }

        }

        chunk.claim(player.getName(), gangName);
        player.sendMessage(ChatColor.GOLD + Lang.get("ChunkClaimed"));
        
        return true;
        
    }
    
/*
                     _       
                    | |      
  ___ _ __ ___  __ _| |_ ___ 
 / __| '__/ _ \/ _` | __/ _ \
| (__| | |  __/ (_| | ||  __/
 \___|_|  \___|\__,_|\__\___|

*/
    
    private boolean commandCreate(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.gang.create")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        
        if (GangLands.isGangMember(player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("AlreadyInGang"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyGangName"));
            return false;
        }
        
        String gangName = ChatColor.stripColor(args[1]);
        
        if (gangName.length() > MyChunk.getIntSetting("gangNameLength")) {
            sender.sendMessage(ChatColor.RED + Lang.get("GangNameTooLong"));
            return false;
        }
        
        if (GangLands.isGang(gangName)) {
            player.sendMessage(ChatColor.RED + Lang.get("GangExists"));
            return false;
        }
        
        GangLands.createGang(gangName, player);
        
        Bukkit.broadcastMessage(player.getName() + ChatColor.GOLD + " " + Lang.get("GangCreated") + ": "+ ChatColor.WHITE + gangName);
        
        return true;
        
    }
    
/*
     _ _     _                     _ 
    | (_)   | |                   | |
  __| |_ ___| |__   __ _ _ __   __| |
 / _` | / __| '_ \ / _` | '_ \ / _` |
| (_| | \__ \ |_) | (_| | | | | (_| |
 \__,_|_|___/_.__/ \__,_|_| |_|\__,_|

*/
    
    private boolean commandDisband(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.gang.disband")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        
        if (!GangLands.isGangBoss(player) && !player.hasPermission("mychunk.override")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NotGangBoss"));
            return false;
        }
        
        String gangName;
        
        if (args.length > 1) {
            
            if (!player.hasPermission("mychunk.override")) {
                player.sendMessage(ChatColor.RED + Lang.get("NoPermsDisbandOther"));
                return false;
            }
            
            if (!GangLands.isGang(args[1])) {
                player.sendMessage(ChatColor.RED + Lang.get("NotGang"));
                return false;
            }
            
            gangName = args[1];
            
        } else {
            gangName = GangLands.getGang(player);
        }
        
        double balance = MyChunkVaultLink.economy.getBalance("gang-"+gangName);
        
        if (balance > 0) {
            MyChunkVaultLink.economy.withdrawPlayer("gang-"+gangName, balance);
            MyChunkVaultLink.economy.depositPlayer(player.getName(), balance);
            player.sendMessage(MyChunkVaultLink.economy.format(balance) + ChatColor.GOLD + " " + Lang.get("GangFundsWithdrawn"));
        }
        
        GangLands.dissolveGang(gangName);
        
        Bukkit.broadcastMessage(gangName + ChatColor.GOLD + " " + Lang.get("GangDisbanded"));
        
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
        
        if (!sender.hasPermission("mychunk.commands.gang.help")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        int page = 1;
        
        if (args.length != 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + Lang.get("PageNotNumber"));
                sender.sendMessage(ChatColor.RED + "/gang ? {"+Lang.get("Page")+"}");
                return false;
            }
        }
        
        List<String> helpLines = new ArrayList<String>();
        
        if (sender.hasPermission("mychunk.commands.help"))
            helpLines.add(ChatColor.GOLD + "["+Lang.get("MyChunkCommands")+"] /mychunk help {"+Lang.get("Page")+"}");
        
        if (sender.hasPermission("mychunk.commands.gang.status"))
            helpLines.add(ChatColor.GOLD + "/gang {"+Lang.get("GangName")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageGangDetails"));
        
        if (sender instanceof Player && (GangLands.isGangBoss((Player)sender) || GangLands.isGangAssistant((Player)sender)))
            helpLines.add(ChatColor.GOLD + "/gang claim: " + ChatColor.GRAY + Lang.get("CommandMessageGangClaim"));
        
        if (sender.hasPermission("mychunk.commands.gang.create"))
            helpLines.add(ChatColor.GOLD + "/gang create ["+Lang.get("GangName")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageGangCreate"));
        
        if (sender.hasPermission("mychunk.commands.gang.disband")) {
            
            String override = "";
            if (sender.hasPermission("mychunk.override")) {
                override = " {"+Lang.get("GangName")+"}";
            }
            helpLines.add(ChatColor.GOLD + "/gang disband"+override+": " + ChatColor.GRAY + Lang.get("CommandMessageGangDisband"));
        }
        
        if (sender.hasPermission("mychunk.commands.gang.help"))
            helpLines.add(ChatColor.GOLD + "/gang help {"+Lang.get("Page")+"}: " + ChatColor.GRAY + Lang.get("CommandMessageGangHelp"));
        
        if (sender instanceof Player && (GangLands.isGangBoss((Player)sender) || GangLands.isGangAssistant((Player)sender)))
            helpLines.add(ChatColor.GOLD + "/gang invite ["+Lang.get("PlayerName")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageGangInvite"));
        
        if (sender.hasPermission("mychunk.commands.gang.join"))
            helpLines.add(ChatColor.GOLD + "/gang join ["+Lang.get("GangName")+"]: " + ChatColor.GRAY + Lang.get("CommandMessageGangJoin"));
        
        if (sender.hasPermission("mychunk.commands.gang.leave"))
            helpLines.add(ChatColor.GOLD + "/gang leave: " + ChatColor.GRAY + Lang.get("CommandMessageGangLeave"));
        
        if (sender instanceof Player && (GangLands.isGangBoss((Player)sender) || GangLands.isGangAssistant((Player)sender)))
            helpLines.add(ChatColor.GOLD + "/gang unclaim: " + ChatColor.GRAY + Lang.get("CommandMessageGangUnclaim"));
                
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
        sender.sendMessage(ChatColor.GOLD + "MyChunk Ganglands v" + ChatColor.WHITE + pdfFile.getVersion() + ChatColor.GOLD + " " + Lang.get("By") + " " + ChatColor.WHITE + "ellbristow");
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
            sender.sendMessage(ChatColor.GOLD + Lang.get("NextPage") + ": " + ChatColor.WHITE + "/gang help " + (page + 1));
        }

        return true;
        
    }
    
/*
 _            _ _       
(_)          (_) |      
 _ _ ____   ___| |_ ___ 
| | '_ \ \ / / | __/ _ \
| | | | \ V /| | ||  __/
|_|_| |_|\_/ |_|\__\___|

*/
    
    private boolean commandInvite(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        Player player = (Player)sender;
        
        if (!GangLands.isGangBoss(player) && !GangLands.isGangAssistant(player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("NotGangBossOrAssistant"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyPlayerToInvite"));
            return false;
        }
        
        String gangName = GangLands.getGang(player);
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(Lang.get("Player") + ChatColor.WHITE + target.getName() + ChatColor.RED + Lang.get("NotFound"));
            return false;
        }
        
        if (GangLands.isGangMemberOf(target.getName(), gangName)) {
            sender.sendMessage(target.getName() + ChatColor.RED + Lang.get("AlreadyGangMember"));
            return false;
        }
        
        if (args.length > 2 && args[2].equalsIgnoreCase("cancel")) {
            
            if (!GangLands.isInvitedTo(target.getName(), gangName)) {
                sender.sendMessage(Lang.get("NoInvitationFoundFor") + " " + ChatColor.WHITE + target.getName());
                return false;
            }
            
            GangLands.removeInvite(target.getName(), gangName);
            sender.sendMessage(ChatColor.GOLD + Lang.get("InvitationCancelledFor") + " " + ChatColor.WHITE + target.getName());
            
            if (target.isOnline()) {
                target.getPlayer().sendMessage(ChatColor.RED + Lang.get("NoLongerInvitedTo") + ChatColor.WHITE + " " + gangName);
            }
            
            return true;
            
        }

        GangLands.addInvite(target.getName(), gangName);
        sender.sendMessage(ChatColor.GOLD + Lang.get("InvitationAddedFor") + " " + ChatColor.WHITE + target.getName());

        if (target.isOnline()) {
            target.getPlayer().sendMessage(ChatColor.GREEN + Lang.get("InvitedToGang") + ChatColor.WHITE + " " + gangName);
        }

        return true;
        
    }

/*
   _       _       
  (_)     (_)      
   _  ___  _ _ __  
  | |/ _ \| | '_ \ 
  | | (_) | | | | |
  | |\___/|_|_| |_|
 _/ |              
|__/               

*/
    
    private boolean commandJoin(CommandSender sender, String[] args) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.gang.join")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        
        if (GangLands.isGangMember(player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("AlreadyGangMember"));
            return false;
        }
        
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + Lang.get("SpecifyGangToJoin"));
            return false;
        }
        
        String gangName = args[1];
        
        if (!GangLands.isGang(gangName)) {
            sender.sendMessage(ChatColor.RED + Lang.get("NotGang"));
            return false;
        }
        
        if (!GangLands.isInvitedTo(player, args[1])) {
            sender.sendMessage(ChatColor.RED + Lang.get("NotInvitedToGang"));
            return false;
        }
        
        GangLands.join(player, gangName);
        GangLands.removeInvite(player.getName(), gangName);
        
        Bukkit.broadcastMessage(player.getName() + " " + ChatColor.GREEN + Lang.get("JoinedGang") + " " + ChatColor.WHITE + gangName);
        
        return true;
        
    }
    
/*
 _                      
| |                     
| | ___  __ ___   _____ 
| |/ _ \/ _` \ \ / / _ \
| |  __/ (_| |\ V /  __/
|_|\___|\__,_| \_/ \___|

*/
    
    private boolean commandLeave(CommandSender sender) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.gang.leave")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        
        if (!GangLands.isGangMember(player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("NotInGang"));
            return false;
        }
        
        if (GangLands.isGangBoss(player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("BossCantLeave"));
            return false;
        }
        
        String gangName = GangLands.getGang(player);
        GangLands.leave(player, gangName);
        
        Bukkit.broadcastMessage(player.getName() + " " + ChatColor.GREEN + Lang.get("LeftGang") + " " + ChatColor.WHITE + gangName);
        
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
    
    private boolean commandUnclaim(CommandSender sender) {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + Lang.get("CommandMustBeRunByPlayer"));
            return false;
        }
        
        if (!sender.hasPermission("mychunk.commands.gang.unclaim")) {
            sender.sendMessage(ChatColor.RED + Lang.get("NoPermsCommand"));
            return false;
        }
        
        Player player = (Player)sender;
        String gangName = GangLands.getGang(player);
        Block block = player.getLocation().getBlock();
        MyChunkChunk chunk = new MyChunkChunk(block);

        if (!chunk.isClaimed()) {

            player.sendMessage(ChatColor.RED + Lang.get("ChunkNotOwned"));            
            return false;

        }

        String owner = chunk.getOwner();

        if (!owner.equalsIgnoreCase(gangName)) {

            player.sendMessage(ChatColor.RED + Lang.get("GangDoesNotOwn"));            
            return false;

        }

        chunk.unclaim();

        player.sendMessage(ChatColor.GOLD + Lang.get("ChunkUnclaimed"));

        if (MyChunk.getToggle("unclaimRefund")) {

            double price = MyChunk.getDoubleSetting("chunkPrice");
            if (MyChunk.getToggle("rampChunkPrice") && MyChunk.getDoubleSetting("priceRampRate") != 0) {
                int claimed = MyChunkChunk.getOwnedChunkCount(gangName);
                price += MyChunk.getDoubleSetting("priceRampRate") * claimed;
            }
            MyChunkVaultLink.economy.depositPlayer("gang-" + gangName, price / 100 * MyChunk.getDoubleSetting("refundPercent"));

        }
        
        return true;
        
    }

}
