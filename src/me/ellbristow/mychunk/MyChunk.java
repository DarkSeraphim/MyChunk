package me.ellbristow.mychunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.ellbristow.mychunk.commands.GangCommand;
import me.ellbristow.mychunk.commands.MyChunkCommand;
import me.ellbristow.mychunk.ganglands.GangLands;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.listeners.*;
import me.ellbristow.mychunk.utils.FactionsHook;
import me.ellbristow.mychunk.utils.Metrics;
import me.ellbristow.mychunk.utils.MyChunkVaultLink;
import me.ellbristow.mychunk.utils.TownyHook;
import me.ellbristow.mychunk.utils.db.SQLBridge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MyChunk extends JavaPlugin {

    private static FileConfiguration config;
    private static boolean foundEconomy = false;
    
    // Toggleable settings
    private static boolean unclaimRefund = false;
    private static double refundPercent = 100.00;
    private static boolean allowNeighbours = false;
    private static boolean allowOverbuy = false;
    private static boolean allowMobGrief = true;
    private static boolean protectUnclaimed = false;
    private static boolean unclaimedTNT = false;
    private static boolean useClaimExpiry = false;
    private static boolean allowNether = true;
    private static boolean allowEnd = true;
    private static boolean preventEntry = false;
    private static boolean preventPVP = false;
    private static int claimExpiryDays;
    private static boolean overbuyP2P = true;
    private static double chunkPrice = 0.00;
    private static double overbuyPrice = 0.00;
    private static boolean firstChunkFree = false;
    private static boolean rampChunkPrice = false;
    private static boolean useChatFormat = false;
    private static double priceRampRate = 25.00;
    private static int maxChunks = 8;
    private static boolean notify = true;
    private static boolean defaultAllowMobs = false;
    private static boolean defaultAllowPVP = false;
    private static List<String> enabledWorlds = new ArrayList<String>();
    private static List<String> disabledWorlds = new ArrayList<String>();
    private static List<String> prefixes = new ArrayList<String>();
    private static int gangNameLength = 8;
    private static String groupTag = "[%PREFIX%&f] ";
    private static ChatColor gangTagColor = ChatColor.GRAY;
    private static String gangTag = "[%TAGCOLOR%%GANG%&f] ";
    private static String playerTag = "%RANKCOLOR%%DISPNAME%";
    private static String chatFormat = "%GROUPTAG%%PLAYERTAG%&7:&f %MSG%";
    private static String gangChatFormat = "%GROUPTAG%%GANGTAG%%PLAYERTAG%&7:&f %MSG%";
    private final String[] tableColumns = {"world","x","z","owner","allowed","salePrice","allowMobs","allowPVP","lastActive", "gang", "PRIMARY KEY"};
    private final String[] tableDims = {"VARCHAR(32) NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL", "TEXT NOT NULL", "TEXT NOT NULL", "INTEGER NOT NULL", "INTEGER(1) NOT NULL", "INTEGER(1) NOT NULL", "LONG NOT NULL", "TEXT", "(`world`, `x`, `z`)"};
    private final String[] gangColumns = {"gangName","boss","assistants","members","invites","allys","enemies","damage","PRIMARY KEY"};
    private final String[] gangDims = {"VARCHAR(16) NOT NULL","TEXT NOT NULL","TEXT NOT NULL","TEXT NOT NULL","TEXT NOT NULL","TEXT NOT NULL","TEXT NOT NULL","INTEGER NOT NULL DEFAULT 0","(`gangName`)"};    
    private static int gangMultiplier = 12;
    private boolean useMySQL = false;
    private String mysqlHost = "localhost";
    private String mysqlPort = "port";
    private String mysqlDatabase = "database";
    private String mysqlUser = "username";
    private String mysqlPass = "password";
    
    @Override
    public void onEnable() {
        
        // init Config
        loadConfig(false);
        
        // init SQLite
        initSQL();
        
        // Register Commands
        getCommand("mychunk").setExecutor(new MyChunkCommand(this));
//        getCommand("gang").setExecutor(new GangCommand(this));
        
        // Register Events
        getServer().getPluginManager().registerEvents(new AmbientListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new MobListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        if (FactionsHook.foundFactions()) {
            getServer().getPluginManager().registerEvents(new FactionsHook(), this);
            getLogger().info("Hooked into [Factions]");
        }
        
        if (TownyHook.foundTowny()) {
            getServer().getPluginManager().registerEvents(new TownyHook(), this);
            getLogger().info("Hooked into [Towny]");
            getServer().getPluginManager().registerEvents(new CommandListener(), this);
        }
        
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        
    }
    
    @Override
    public void onDisable() {
        
    }

/*
            _   _                
           | | | |         static
  __ _  ___| |_| |_ ___ _ __ ___ 
 / _` |/ _ \ __| __/ _ \ '__/ __|
| (_| |  __/ |_| ||  __/ |  \__ \
 \__, |\___|\__|\__\___|_|  |___/
  __/ |                          
 |___/                           

 */
    
    /**
     * Get the maximum number of chunks a player can claim
     * 
     * @param player Player to check
     * @return Number of chunks player can claim
     */
    public static int getMaxChunks(CommandSender player) {
        int max = maxChunks;
        if (player instanceof Player) {
            if (player.hasPermission("mychunk.claim.max.0") || player.hasPermission("mychunk.claim.unlimited")) {
                max = 0;
            } else {
                for (int i = 1; i <= 256; i++) {
                    if (player.hasPermission("mychunk.claim.max." + i)) {
                        max = i;
                    }
                }
            }
        } else {
            max = 0;
        }
        return max;
    }
    
/*
          _   _                
         | | | |       instance
 ___  ___| |_| |_ ___ _ __ ___ 
/ __|/ _ \ __| __/ _ \ '__/ __|
\__ \  __/ |_| ||  __/ |  \__ \
|___/\___|\__|\__\___|_|  |___/


*/
    
    public void setChunkPrice(double newPrice) {
        config.set("chunk_price", newPrice);
        chunkPrice = newPrice;
        saveConfig();
    }
    
    public void setExpiryDays(int newDays) {
        config.set("claimExpiresAfter", newDays);
        claimExpiryDays = newDays;
        saveConfig();
    }
    
    public void setGangnamelength(int newMax) {
        gangNameLength = newMax;
        config.set("gangNameLength", gangNameLength);
        saveConfig();
    }
    
    public void setGangMultiplier(int newMax) {
        gangMultiplier = newMax;
        config.set("gangChunkMultiplier", gangMultiplier);
        saveConfig();
    }
    
    public void setMaxChunks(int newMax) {
        config.set("max_chunks", newMax);
        maxChunks = newMax;
        saveConfig();
    }
    
    public void setOverbuyPrice(double newPrice) {
        config.set("overbuy_price", newPrice);
        overbuyPrice = newPrice;
        saveConfig();
    }
    
    public void setRampRate(double newRate) {
        priceRampRate = newRate;
        config.set("price_ramp_rate", priceRampRate);
        saveConfig();
    }
    
    public void setRefundPercent(double newPercent) {
        refundPercent = newPercent;
        config.set("refund_percent", refundPercent);
        saveConfig();
    }

/*
                _   _               _     
      instance | | | |             | |    
 _ __ ___   ___| |_| |__   ___   __| |___ 
| '_ ` _ \ / _ \ __| '_ \ / _ \ / _` / __|
| | | | | |  __/ |_| | | | (_) | (_| \__ \
|_| |_| |_|\___|\__|_| |_|\___/ \__,_|___/


*/
    
    /**
     * Get the number of chunks a player already owns
     * 
     * @param playerName Player to check
     * @return Number of chunks player owns
     */
    public int ownedChunkCount(String playerName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("COUNT(*) as counter", "MyChunks", "owner = '"+playerName+"'", "", "");
        if (results != null && !results.isEmpty()) {
            int counter = Integer.parseInt(results.get(0).get("counter"));
            return counter;
        }
        return 0;
        
    }
    
    private void initSQL() {
        
        if (useMySQL) {
            SQLBridge.initMySQL(mysqlHost, mysqlPort, mysqlDatabase, mysqlUser, mysqlPass);
        } else {
            SQLBridge.initSQLite();
        }
        
        if (!SQLBridge.checkTable("MyChunks")) {
            // Create empty table
            SQLBridge.createTable("MyChunks", tableColumns, tableDims);
        }
        if (!SQLBridge.checkTable("MyChunkGangs")) {
            // Create empty table
            SQLBridge.createTable("MyChunkGangs", gangColumns, gangDims);
        }
        // Check Missing Columns
        if (!SQLBridge.tableContainsColumn("MyChunks", "allowPVP")) {
            SQLBridge.query("ALTER TABLE MyChunks ADD COLUMN allowPVP INT(1) NOT NULL DEFAULT 0");
        }
        if (!SQLBridge.tableContainsColumn("MyChunks", "gang")) {
            SQLBridge.query("ALTER TABLE MyChunks ADD COLUMN gang TEXT");
        }

    }
    
    private void loadConfig(boolean reload) {
        if (reload) {
            reloadConfig();
        }
        config = getConfig();
        
        String databaseType = config.getString("databaseType", "sqlite");
        if (databaseType.equalsIgnoreCase("mysql")) {
            useMySQL = true;
        }
        config.set("databaseType", useMySQL ? "mysql" : "sqlite");
        
        mysqlHost = config.getString("mysqlHost", "localhost");
        config.set("mysqlHost", mysqlHost);
        mysqlPort = config.getString("mysqlPort", "3306");
        config.set("mysqlPort", mysqlPort);
        mysqlDatabase = config.getString("mysqlDatabase", "database");
        config.set("mysqlDatabase", mysqlDatabase);
        mysqlUser = config.getString("mysqlUser", "username");
        config.set("mysqlUser", mysqlUser);
        mysqlPass = config.getString("mysqlPass", "password");
        config.set("mysqlPass", mysqlPass);
        
        maxChunks = config.getInt("max_chunks", 8);
        config.set("max_chunks", maxChunks);
        allowNeighbours = config.getBoolean("allow_neighbours", false);
        config.set("allow_neighbours", allowNeighbours);
        protectUnclaimed = config.getBoolean("protect_unclaimed", false);
        config.set("protect_unclaimed", protectUnclaimed);
        unclaimedTNT = config.getBoolean("prevent_tnt_in_unclaimed", true);
        config.set("prevent_tnt_in_unclaimed", unclaimedTNT);
        useClaimExpiry = config.getBoolean("useClaimExpiry", false);
        config.set("useClaimExpiry", useClaimExpiry);
        claimExpiryDays = config.getInt("claimExpiresAfter", 7);
        config.set("claimExpiresAfter", claimExpiryDays);
        allowNether = config.getBoolean("allowNether", true);
        config.set("allowNether", allowNether);
        allowEnd = config.getBoolean("allowEnd", true);
        config.set("allowEnd", allowEnd);
        notify = config.getBoolean("owner_notifications", true);
        config.set("owner_notifications", notify);
        refundPercent = config.getDouble("refund_percent", 100);
        config.set("refund_percent", refundPercent);
        defaultAllowMobs = config.getBoolean("default_allow_mobs", false);
        config.set("default_allow_mobs", defaultAllowMobs);
        defaultAllowPVP = config.getBoolean("default_allow_pvp", false);
        config.set("default_allow_pvp", defaultAllowPVP);
        List<String> worldsList = config.getStringList("worlds");
        enabledWorlds = new ArrayList<String>(worldsList);
        List<String> disabledWorldsList = config.getStringList("disabledworlds");
        disabledWorlds = new ArrayList<String>(disabledWorldsList);
        if (enabledWorlds.isEmpty() && disabledWorlds.isEmpty()) {
            // Enable all worlds by default
            for (World world : getServer().getWorlds()) {
                enabledWorlds.add(world.getName());
            }
        } else {
            // check all worlds listed
            for (World world : getServer().getWorlds()) {
                if (!isWorldEnabled(world.getName()) && !isWorldDisabled(world.getName())) {
                    enabledWorlds.add(world.getName());
                }
            }
        }
        config.set("worlds", worldsList);
        config.set("disabledworlds", disabledWorldsList);
        allowMobGrief = config.getBoolean("allow_mob_griefing", true);
        config.set("allow_mob_griefing", allowMobGrief);
        preventEntry = config.getBoolean("prevent_chunk_entry", false);
        config.set("prevent_chunk_entry", preventEntry);
        preventPVP = config.getBoolean("preventPVP", false);
        config.set("preventPVP", preventPVP);
        useChatFormat = config.getBoolean("useChatFormat", false);
        config.set("useChatFormat", useChatFormat);
        gangNameLength = config.getInt("gangNameLength", 8);
        config.set("gangNameLength", gangNameLength);
        List<String> defaultPrefixes = new ArrayList<String>();
        defaultPrefixes.add("&9Member");
        defaultPrefixes.add("&6VIP");
        prefixes = config.getStringList("prefixes");

        groupTag = config.getString("groupTag", "[%PREFIX%&f] ");
        config.set("groupTag", groupTag);
        gangTagColor = ChatColor.valueOf(config.getString("gangTagColor", "GRAY"));
        config.set("gangTagColor", gangTagColor.name());
        gangTag = config.getString("gangTag", "[%TAGCOLOR%%GANG%&f] ");
        config.set("gangTag", gangTag);
        playerTag = config.getString("playerTag", "%RANKCOLOR%%DISPNAME%");
        config.set("playerTag", playerTag);
        chatFormat = config.getString("noGangChatFormat", "%GROUPTAG%%PLAYERTAG%&7:&f %MSG%");
        config.set("noGangChatFormat", chatFormat);
        gangChatFormat = config.getString("gangChatFormat", "%GROUPTAG%%GANGTAG%%PLAYERTAG%&7:&f %MSG%");
        config.set("gangChatFormat", gangChatFormat);
        gangMultiplier = config.getInt("gangChunkMultiplier", 12);
        config.set("gangChunkMultiplier", gangMultiplier);
        
        if (prefixes.isEmpty())
            prefixes = defaultPrefixes;
        config.set("prefixes", prefixes);
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            MyChunkVaultLink.initEconomy();
            getLogger().info("[Vault] found and hooked!");
            if (MyChunkVaultLink.foundEconomy) {
                foundEconomy = true;
                String message = "[" + MyChunkVaultLink.economyName + "] found and hooked!";
                getLogger().info(message);
                chunkPrice = config.getDouble("chunk_price", 0.00);
                config.set("chunk_price", chunkPrice);
                unclaimRefund = config.getBoolean("unclaim_refund", false);
                config.set("unclaim_refund", unclaimRefund);
                allowOverbuy = config.getBoolean("allow_overbuy", false);
                config.set("allow_overbuy", allowOverbuy);
                overbuyPrice = config.getDouble("overbuy_price", 0.00);
                config.set("overbuy_price", overbuyPrice);
                overbuyP2P = config.getBoolean("charge_overbuy_on_resales", true);
                config.set("charge_overbuy_on_resales", overbuyP2P);
                firstChunkFree = config.getBoolean("first_chunk_free", false);
                config.set("first_chunk_free", firstChunkFree);
                rampChunkPrice = config.getBoolean("ramp_chunk_price", false);
                config.set("ramp_chunk_price", rampChunkPrice);
                priceRampRate = config.getDouble("price_ramp_rate", 25.00);
                config.set("price_ramp_rate", priceRampRate);
            } else if (!reload) {
                getLogger().info("No economy plugin found! Chunks will be free");
            }
        } else if (!reload) {
            getLogger().info("Vault not found! Chunks will be free");
        }
        saveConfig();
    }
    
    /**
     * Return current value of double settings
     * <p>
     * Checkable settings: (not case sensitive)<br>
     * claimExpiryDays - Returns the number of days before a chunk claim expires<br>
     * maxChunks - Returns the maximum number of chunks a player can own<br>
     * gangNameLength - Returns the maximum length of a gang name<br>
     * gangMultiplier - Returns the maximum number of chunks a gang can own per member<br>
     * 
     * @param setting
     * @return Setting value or 0 if not found
     */
    public static int getIntSetting(String setting) {
        if (setting.equalsIgnoreCase("claimExpiryDays")) return claimExpiryDays;
        if (setting.equalsIgnoreCase("max_chunks")) return maxChunks;
        if (setting.equalsIgnoreCase("gangNameLength")) return gangNameLength;
        if (setting.equalsIgnoreCase("gangMultiplier")) return gangMultiplier;
        return 0;
    }
    
    /**
     * Return current value of double settings
     * <p>
     * Checkable settings: (not case sensitive)<br>
     * chunkPrice - Default price of a chunk<br>
     * priceRampRate - Default price of a chunk<br>
     * overbuyPrice - Premium added to chunk price when overbuying<br>
     * overbuyPrice - Percentage refunded when unclaiming chunks<br>
     * 
     * @param setting
     * @return Setting value or 0 if not found
     */
    public static double getDoubleSetting(String setting) {
        if (setting.equalsIgnoreCase("chunkPrice")) return chunkPrice;
        if (setting.equalsIgnoreCase("priceRampRate")) return priceRampRate;
        if (setting.equalsIgnoreCase("overbuyPrice")) return overbuyPrice;
        if (setting.equalsIgnoreCase("refundPercent")) return refundPercent;
        return 0;
    }
    
    /**
     * Return current state of boolean settings
     * <p>
     * Checkable settings: (not case sensitive)<br>
     * allowEnd - Checks if players can claim chunks in End worlds<br>
     * allowMobGrief - Checks if mobs can cause terrain damage<br>
     * allowNeighbours - Checks if players can claim chunks next to other players<br>
     * allowNether - Checks if players can claim chunks in Nether worlds<br>
     * allowOverbuy - Checks if players can buy more chunks than their allowance<br>
     * foundEconomy - Checks if an economy plugin was found<br>
     * protectUnclaimed - Checks if unclaimed chunks are protected<br>
     * unclaimRefund - Checks if players receive a refund hen unclaiming chunks<br>
     * unclaimedTNT - Checks if TNT is blocked when protectUnclimed is on<br>
     * useClaimExpiry - Checks if chunk ownership expires<br>
     * ownerNotifications - Checks if owners receive notifications<br>
     * firstChunkFree - Checks if players can claim 1 chunk for free<br>
     * rampChunkPrice - Checks if chunk prices increase with each claim<br>
     * 
     * @param setting
     * @return Setting state or false if not found
     */
    public static boolean getToggle(String setting) {
        if (setting.equalsIgnoreCase("allowEnd")) return allowEnd;
        if (setting.equalsIgnoreCase("allowNeighbours")) return allowNeighbours;
        if (setting.equalsIgnoreCase("allowNether")) return allowNether;
        if (setting.equalsIgnoreCase("allowOverbuy")) return allowOverbuy;
        if (setting.equalsIgnoreCase("foundEconomy")) return foundEconomy;
        if (setting.equalsIgnoreCase("overbuyP2P")) return overbuyP2P;
        if (setting.equalsIgnoreCase("protectUnclaimed")) return protectUnclaimed;
        if (setting.equalsIgnoreCase("unclaimRefund")) return unclaimRefund;
        if (setting.equalsIgnoreCase("unclaimedTNT")) return unclaimedTNT;
        if (setting.equalsIgnoreCase("useClaimExpiry")) return useClaimExpiry;
        if (setting.equalsIgnoreCase("ownerNotifications")) return notify;
        if (setting.equalsIgnoreCase("firstChunkFree")) return firstChunkFree;
        if (setting.equalsIgnoreCase("preventEntry")) return preventEntry;
        if (setting.equalsIgnoreCase("preventPVP")) return preventPVP;
        if (setting.equalsIgnoreCase("allowMobGrief")) return allowMobGrief;
        if (setting.equalsIgnoreCase("rampChunkPrice")) return rampChunkPrice;
        if (setting.equalsIgnoreCase("useChatFormat")) return useChatFormat;
        if (setting.equalsIgnoreCase("defaultAllowMobs")) return defaultAllowMobs;
        if (setting.equalsIgnoreCase("defaultAllowPVP")) return defaultAllowPVP;
        return false;
    }
    
    public static String formatChat(String msg, Player player) {
        
        String grTag = MyChunk.groupTag;
        String gTag = MyChunk.gangTag;
        String plTag = MyChunk.playerTag;
        
        String prefixList = "";
        for (String prefix : prefixes) {
            if (player.hasPermission("mychunk.prefix."+stripColor(prefix).toLowerCase())) {
                if (!prefixList.equals("")) {
                    prefixList += "&f,";
                }
                prefixList += prefix;
            }
        }
        if (prefixList.equals("")) {
            grTag = "";
        } else {
            grTag = grTag.replace("%PREFIX%", prefixList);
        }
        
        ChatColor rankColor = ChatColor.WHITE;
        if (GangLands.isGangMember(player)) {
            rankColor = ChatColor.BLUE;
            if (GangLands.isGangBoss(player)) {
                rankColor = ChatColor.GOLD;
            } else if (GangLands.isGangAssistant(player)) {
                rankColor = ChatColor.YELLOW;
            }
        }
        
        plTag = plTag.replace("%RANKCOLOR%", rankColor + "").replace("%DISPNAME%", player.getDisplayName());
        
        if (GangLands.isGangMember(player)) {
            
            ChatColor gangColor = gangTagColor;
            
            gTag = gTag.replace("%TAGCOLOR%", gangColor + "").replace("%GANG%",GangLands.getGang(player));
            
            msg = fixColors(gangChatFormat.replace("%GROUPTAG%", grTag).replace("%GANGTAG%", gTag).replace("%PLAYERTAG%", plTag).replace("%MSG%", msg));
            
            return msg.replace("%", "%%");
            
        } else {
            
            msg = fixColors(chatFormat.replace("%GANGTAG%", "").replace("%GROUPTAG%", grTag).replace("%PLAYERTAG%", plTag).replace("%MSG%", msg));
            
            return msg.replace("%", "%%");
            
        }

    }
    
    private static String stripColor(String format) {
        
        format = format.replace("&0", "");
        format = format.replace("&1", "");
        format = format.replace("&2", "");
        format = format.replace("&3", "");
        format = format.replace("&4", "");
        format = format.replace("&5", "");
        format = format.replace("&6", "");
        format = format.replace("&7", "");
        format = format.replace("&8", "");
        format = format.replace("&9", "");
        format = format.replace("&a", "");
        format = format.replace("&b", "");
        format = format.replace("&c", "");
        format = format.replace("&d", "");
        format = format.replace("&e", "");
        format = format.replace("&f", "");
        
        format = format.replace("&k", "");
        format = format.replace("&l", "");
        format = format.replace("&m", "");
        format = format.replace("&n", "");
        format = format.replace("&o", "");
        format = format.replace("&r", "");
        
        return format;
        
    }
    
    private static String fixColors(String format) {
        
        format = format.replace("&0", ChatColor.BLACK + "");
        format = format.replace("&1", ChatColor.DARK_BLUE + "");
        format = format.replace("&2", ChatColor.DARK_GREEN + "");
        format = format.replace("&3", ChatColor.DARK_AQUA + "");
        format = format.replace("&4", ChatColor.DARK_RED + "");
        format = format.replace("&5", ChatColor.DARK_PURPLE + "");
        format = format.replace("&6", ChatColor.GOLD + "");
        format = format.replace("&7", ChatColor.GRAY + "");
        format = format.replace("&8", ChatColor.DARK_GRAY + "");
        format = format.replace("&9", ChatColor.BLUE + "");
        format = format.replace("&a", ChatColor.GREEN + "");
        format = format.replace("&b", ChatColor.AQUA + "");
        format = format.replace("&c", ChatColor.RED + "");
        format = format.replace("&d", ChatColor.LIGHT_PURPLE + "");
        format = format.replace("&e", ChatColor.YELLOW + "");
        format = format.replace("&f", ChatColor.WHITE + "");
        
        format = format.replace("&k", ChatColor.MAGIC + "");
        format = format.replace("&l", ChatColor.BOLD + "");
        format = format.replace("&m", ChatColor.STRIKETHROUGH + "");
        format = format.replace("&n", ChatColor.UNDERLINE + "");
        format = format.replace("&o", ChatColor.ITALIC + "");
        format = format.replace("&r", ChatColor.RESET + "");
        
        return format;
        
    }
    
    /**
     * Toggle current state of boolean settings
     * <p>
     * Toggle settings: (not case sensitive)<br>
     * allowEnd - Checks if players can claim chunks in End worlds<br>
     * allowMobGrief - Checks if mobs can cause terrain damage<br>
     * allowNeighbours - Checks if players can claim chunks next to other players<br>
     * allowNether - Checks if players can claim chunks in Nether worlds<br>
     * allowOverbuy - Checks if players can buy more chunks than their allowance<br>
     * firstChunkFree - Checks if players can claim 1 chunk for free<br>
     * overbuyP2P - Checks if overbuy fee is charged on resales<br>
     * ownerNotifications - Checks if owners receive notifications<br>
     * preventEntry - Checks if entry protection is enabled<br>
     * preventPVP - Checks if PVP protection is enabled<br>
     * protectUnclaimed - Checks if unclaimed chunks are protected<br>
     * rampChunkPrice - Checks if chunk prices increase with each claim<br>
     * unclaimedTNT - Checks if TNT is blocked when protectUnclimed is on<br>
     * unclaimRefund - Checks if players receive a refund hen unclaiming chunks<br>
     * useClaimExpiry - Checks if chunk ownership expires<br>
     * 
     * @param setting
     */
    public void toggleSetting(String setting) {
        if (setting.equalsIgnoreCase("allowEnd")) {
            allowEnd = !allowEnd;
            config.set("allowEnd", !allowEnd);
        }
        if (setting.equalsIgnoreCase("allowMobGrief")) {
            allowMobGrief = !allowMobGrief;
            config.set("allow_mob_greifing", allowMobGrief);
        }
        if (setting.equalsIgnoreCase("allowNeighbours")) {
            allowNeighbours = !allowNeighbours;
            config.set("allow_neighbours", allowNeighbours);
        }
        if (setting.equalsIgnoreCase("allowNether")) {
            allowNether = !allowNether;
            config.set("allowNether", allowNether);
        }
        if (setting.equalsIgnoreCase("allowOverbuy")) {
            allowOverbuy = !allowOverbuy;
            config.set("allow_overbuy", allowOverbuy);
        }
        if (setting.equalsIgnoreCase("firstChunkFree")) {
            firstChunkFree = !firstChunkFree;
            config.set("first_chunk_free", firstChunkFree);
        }
        if (setting.equalsIgnoreCase("overbuyP2P")) {
            overbuyP2P = !overbuyP2P;
            config.set("charge_overbuy_on_resales", overbuyP2P);
        }
        if (setting.equalsIgnoreCase("ownerNotifications")) {
            notify = !notify;
            config.set("owner_notifications", notify);
        }
        if (setting.equalsIgnoreCase("preventEntry")) {
            preventEntry = !preventEntry;
            config.set("prevent_chunk_entry", preventEntry);
        }
        if (setting.equalsIgnoreCase("preventPVP")) {
            preventPVP = !preventPVP;
            config.set("preventPVP", preventPVP);
        }
        if (setting.equalsIgnoreCase("protectUnclaimed")) {
            protectUnclaimed = !protectUnclaimed;
            config.set("protect_unclaimed", protectUnclaimed);
        }
        if (setting.equalsIgnoreCase("rampChunkPrice")) {
            rampChunkPrice = !rampChunkPrice;
            config.set("ramp_chunk_price", rampChunkPrice);
        }
        if (setting.equalsIgnoreCase("unclaimedTNT")) {
            unclaimedTNT = !unclaimedTNT;
            config.set("prevent_tnt_in_unclaimed", unclaimedTNT);
        }
        if (setting.equalsIgnoreCase("unclaimRefund")) {
            unclaimRefund = !unclaimRefund;
            config.set("unclaim_refund", unclaimRefund);
        }
        if (setting.equalsIgnoreCase("useChatFormat")) {
            useChatFormat = !useChatFormat;
            config.set("useChatFormat", useChatFormat);
        }
        if (setting.equalsIgnoreCase("useClaimExpiry")) {
            useClaimExpiry = !useClaimExpiry;
            config.set("useClaimExpiry", useClaimExpiry);
        }
        saveConfig();
    }
    
    public static boolean isWorldEnabled(String name) {
        return enabledWorlds.contains(name);
    }
    
    public static boolean isWorldDisabled(String name) {
        return disabledWorlds.contains(name);
    }
    
    public static void enableWorld(String worldName) {
        
        enabledWorlds.add(worldName);
        disabledWorlds.remove(worldName);
        config.set("worlds", enabledWorlds);
        config.set("disabledworlds", disabledWorlds);
        Bukkit.getPluginManager().getPlugin("MyChunk").saveConfig();
        
    }
    
    public static void disableWorld(String worldName) {
        
        disabledWorlds.add(worldName);
        enabledWorlds.remove(worldName);
        config.set("worlds", enabledWorlds);
        config.set("disabledworlds", disabledWorlds);
        Bukkit.getPluginManager().getPlugin("MyChunk").saveConfig();
        
    }
    
    public static void enableAllWorlds() {
        
        for (World world : Bukkit.getWorlds()) {
            enabledWorlds.add(world.getName());
        }
        
        disabledWorlds.clear();
        config.set("worlds", enabledWorlds);
        config.set("disabledworlds", disabledWorlds);
        Bukkit.getPluginManager().getPlugin("MyChunk").saveConfig();
        
    }
    
    public static void disableAllWorlds() {
        
        for (World world : Bukkit.getWorlds()) {
            disabledWorlds.add(world.getName());
        }
        
        enabledWorlds.clear();
        config.set("worlds", enabledWorlds);
        config.set("disabledworlds", disabledWorlds);
        Bukkit.getPluginManager().getPlugin("MyChunk").saveConfig();
        
    }
    
    public void reload() {
        reloadConfig();
        loadConfig(true);
        Lang.reload();
    }
    
    public static List<String> getPrefixes() {
       return prefixes; 
    }
    
}
