package me.ellbristow.mychunk;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.ellbristow.mychunk.lang.Lang;
import me.ellbristow.mychunk.listeners.*;
import me.ellbristow.mychunk.utils.Metrics;
import me.ellbristow.mychunk.utils.SQLiteBridge;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
    private static double priceRampRate = 25.00;
    private static int maxChunks = 8;
    private static boolean notify = true;
    private static Set<String> enabledWorlds = new HashSet<String>();
    private static Set<String> disabledWorlds = new HashSet<String>();
    private MyChunkVaultLink vault;
    private String[] tableColumns = {"world","x","z","owner","allowed","salePrice","allowMobs","allowPVP","lastActive", "PRIMARY KEY"};
    private String[] tableDims = {"TEXT NOT NULL", "INTEGER NOT NULL", "INTEGER NOT NULL", "TEXT NOT NULL", "TEXT NOT NULL", "INTEGER NOT NULL", "INTEGER(1) NOT NULL", "INTEGER(1) NOT NULL", "LONG NOT NULL", "(world, x, z)"};
    
    @Override
    public void onEnable() {
        
        // init Config
        loadConfig(false);
        
        // init SQLite
        initSQLite();
        
        // Register Commands
        getCommand("mychunk").setExecutor(new MyChunkCommands(this));
        
        // Register Events
        getServer().getPluginManager().registerEvents(new AmbientListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new MobListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        if (FactionsHook.foundFactions()) {
            getServer().getPluginManager().registerEvents(new FactionsHook(), this);
            getLogger().info("Hooked into [Factions]");
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
        SQLiteBridge.close();
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
     * @param playerName Player to check
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
        HashMap<Integer, HashMap<String, Object>> results = SQLiteBridge.select("COUNT(*) as counter", "MyChunks", "owner = '"+playerName+"'", "","");
        return Integer.parseInt(results.get(0).get("counter")+"");
    }
    
    private void initSQLite() {
        if (!SQLiteBridge.checkTable("MyChunks")) {
            // Create empty table
            SQLiteBridge.createTable("MyChunks", tableColumns, tableDims);
        }
        // Check Missing Columns
        if (!SQLiteBridge.tableContainsColumn("MyChunks", "allowPVP")) {
            SQLiteBridge.addColumn("MyChunks", "allowPVP INT(1) NOT NULL DEFAULT 0");
        }
        File chunkFile = new File(getDataFolder(),"chunks.yml");
        if (chunkFile.exists()) {
            // Transfer old data
            getLogger().info("Converting old YML data to SQLite");
            FileConfiguration chunkStore = YamlConfiguration.loadConfiguration(chunkFile);
            Set<String> keys = chunkStore.getKeys(false);
            String values = "";
            for (String key : keys) {
                if (!key.equalsIgnoreCase("TotalOwned")) {
                    int split = key.lastIndexOf("_", key.lastIndexOf("_") - 1);
                    String world = key.substring(0, split);
                    String[] elements = key.substring(split + 1).split("_");
                    int x = Integer.parseInt(elements[0]);
                    int z = Integer.parseInt(elements[1]);
                    String owner = chunkStore.getString(key+".owner");
                    String allowed = chunkStore.getString(key+".allowed","");
                    double salePrice = chunkStore.getDouble(key+".forsale",0);
                    boolean allowMobs = chunkStore.getBoolean(key+".allowmobs",false);
                    long lastActive = chunkStore.getLong(key+".lastActive",0);
                    if (!values.equals("")) {
                        values += ",";
                    }
                    SQLiteBridge.query("INSERT OR REPLACE INTO MyChunks (world,x,z,owner,allowed,salePrice,allowMobs,allowPVP,lastActive) VALUES ('"+world+"',"+x+","+z+",'"+owner+"','"+allowed+"',"+salePrice+","+(allowMobs?"1":"0")+",0,"+lastActive+")");
                }
            }
            getLogger().info("YML > SQLite Conversion Complete!");
        }
        chunkFile.delete();
    }
    
    private void loadConfig(boolean reload) {
        if (reload) {
            reloadConfig();
        }
        config = getConfig();
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
        List<String> worldsList = config.getStringList("worlds");
        enabledWorlds = new HashSet<String>(worldsList);
        List<String> disabledWorldsList = config.getStringList("disabledworlds");
        disabledWorlds = new HashSet<String>(disabledWorldsList);
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
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            vault = new MyChunkVaultLink(this);
            getLogger().info("[Vault] found and hooked!");
            if (vault.foundEconomy) {
                foundEconomy = true;
                String message = "[" + vault.economyName + "] found and hooked!";
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
     * 
     * @param setting
     * @return Setting value or 0 if not found
     */
    public static int getIntSetting(String setting) {
        if (setting.equalsIgnoreCase("claimExpiryDays")) return claimExpiryDays;
        if (setting.equalsIgnoreCase("max_chunks")) return maxChunks;
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
        return false;
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
            if (allowEnd) allowEnd = false; else allowEnd = true;
            config.set("allowEnd", allowEnd);
        }
        if (setting.equalsIgnoreCase("allowMobGrief")) {
            if (allowMobGrief) allowMobGrief = false; else allowMobGrief = true;
            config.set("allow_mob_greifing", allowMobGrief);
        }
        if (setting.equalsIgnoreCase("allowNeighbours")) {
            if (allowNeighbours) allowNeighbours = false; else allowNeighbours = true;
            config.set("allow_neighbours", allowNeighbours);
        }
        if (setting.equalsIgnoreCase("allowNether")) {
            if (allowNether) allowNether = false; else allowNether = true;
            config.set("allowNether", allowNether);
        }
        if (setting.equalsIgnoreCase("allowOverbuy")) {
            if (allowOverbuy) allowOverbuy = false; else allowOverbuy = true;
            config.set("allow_overbuy", allowOverbuy);
        }
        if (setting.equalsIgnoreCase("firstChunkFree")) {
            if (firstChunkFree) firstChunkFree = false; else firstChunkFree = true;
            config.set("first_chunk_free", firstChunkFree);
        }
        if (setting.equalsIgnoreCase("overbuyP2P")) {
            if (overbuyP2P) overbuyP2P = false; else overbuyP2P = true;
            config.set("charge_overbuy_on_resales", overbuyP2P);
        }
        if (setting.equalsIgnoreCase("ownerNotifications")) {
            if (notify) notify = false; else notify = true;
            config.set("owner_notifications", notify);
        }
        if (setting.equalsIgnoreCase("preventEntry")) {
            if (preventEntry) preventEntry = false; else preventEntry = true;
            config.set("prevent_chunk_entry", preventEntry);
        }
        if (setting.equalsIgnoreCase("preventPVP")) {
            if (preventPVP) preventPVP = false; else preventPVP = true;
            config.set("preventPVP", preventPVP);
        }
        if (setting.equalsIgnoreCase("protectUnclaimed")) {
            if (protectUnclaimed) protectUnclaimed = false; else protectUnclaimed = true;
            config.set("protect_unclaimed", protectUnclaimed);
        }
        if (setting.equalsIgnoreCase("rampChunkPrice")) {
            if (rampChunkPrice) rampChunkPrice = false; else rampChunkPrice = true;
            config.set("ramp_chunk_price", rampChunkPrice);
        }
        if (setting.equalsIgnoreCase("unclaimedTNT")) {
            if (unclaimedTNT) unclaimedTNT = false; else unclaimedTNT = true;
            config.set("prevent_tnt_in_unclaimed", unclaimedTNT);
        }
        if (setting.equalsIgnoreCase("unclaimRefund")) {
            if (unclaimRefund) unclaimRefund = false; else unclaimRefund = true;
            config.set("unclaim_refund", unclaimRefund);
        }
        if (setting.equalsIgnoreCase("useClaimExpiry")) {
            if (useClaimExpiry) useClaimExpiry = false; else useClaimExpiry = true;
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
    
    protected void reload() {
        reloadConfig();
        loadConfig(true);
        Lang.reload();
    }
    
}
