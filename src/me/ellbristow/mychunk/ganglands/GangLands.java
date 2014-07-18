package me.ellbristow.mychunk.ganglands;

import java.util.HashMap;
import me.ellbristow.mychunk.MyChunk;
import me.ellbristow.mychunk.utils.MyChunkVaultLink;
import me.ellbristow.mychunk.utils.db.SQLBridge;
import org.bukkit.entity.Player;

public class GangLands {
    
    private MyChunk plugin;
    
    public GangLands(MyChunk instance) {
        
        plugin = instance;
        
    }
    
    public static void createGang(String gangName, Player player) {
        
        SQLBridge.query("INSERT INTO MyChunkGangs (gangName, boss, members, assistants, invites, allys, enemies) VALUES ('"+gangName+"', '"+player.getName()+"', '{"+player.getName()+"}', '', '', '', '')");
        if (!MyChunkVaultLink.economy.hasAccount("gang-" + gangName)) {
            MyChunkVaultLink.economy.createPlayerAccount("gang-" + gangName);
        }
        double balance = MyChunkVaultLink.economy.getBalance("gang-" + gangName);
        MyChunkVaultLink.economy.bankWithdraw("gang-" + gangName, balance);
        
    }
    
    public static void dissolveGang(String gangName) {
        
        SQLBridge.query("DELETE FROM MyChunkGangs WHERE LOWER(gangName) = '"+gangName.toLowerCase()+"'");
        
    }
    
    public static void addInvite(String playerName, String gangName) {
        
        SQLBridge.query("UPDATE MyChunkGangs SET invites = CONCAT(invites, '{"+playerName+"}') WHERE LOWER(gangName) = '"+gangName.toLowerCase()+"'");
        
    }
    
    public static void removeInvite(String playerName, String gangName) {
        
        SQLBridge.query("UPDATE MyChunkGangs SET invites = REPLACE(invites, '{"+playerName+"}', '') WHERE LOWER(gangName) = '"+gangName.toLowerCase()+"'");
        
    }
    
    public static void join(Player player, String gangName) {
        
        SQLBridge.query("UPDATE MyChunkGangs SET members = CONCAT(members, '{"+player.getName()+"}') WHERE LOWER(gangName) = '"+gangName.toLowerCase()+"'");
        
    }
    
    public static void leave(Player player, String gangName) {
        
        SQLBridge.query("UPDATE MyChunkGangs SET members = REPLACE(members, '{"+player.getName()+"}', ''), assistants = REPLACE(assistants, '{"+player.getName()+"}', '')  WHERE LOWER(gangName) = '"+gangName.toLowerCase()+"'");
        
    }
    
    public static boolean isGang(String gangName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"'", "", "");
        
        if (results != null && !results.isEmpty()) {
            return true;
        }
        
        return false;
        
    }
    
    public static boolean isGangBoss(Player player) {

        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("boss", "MyChunkGangs", "LOWER(boss) = '"+player.getName().toLowerCase()+"'", "", "");

        if (results != null && !results.isEmpty()) {       
            return true;
        }
        
        return false;
        
    }
    
    public static boolean isGangBossOf(Player player, String gangName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("boss", "MyChunkGangs", "LOWER(boss) = '"+player.getName().toLowerCase()+"' AND LOWER(gangName) = '"+gangName.toLowerCase()+"'", "", "");

        if (results != null && !results.isEmpty()) {
            return true;
        }

        return false;
        
    }
    
    public static boolean isGangAssistant(Player player) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(assistants) LIKE '%{"+player.getName().toLowerCase()+"}%'", "", "");

        if (results != null && !results.isEmpty()) {
            return true;
        }
        
        return false;
        
    }
    
    public static boolean isGangAssistantOf(Player player, String gangName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(assistants) LIKE '%{"+player.getName().toLowerCase()+"}%' AND gangName = '"+gangName+"'", "", "");

        if (results != null && !results.isEmpty()) {
            return true;
        }
        
        return false;
        
    }
    
    public static boolean isGangMember(Player player) {
        
        return !getGang(player).equals("");
        
    }
    
    public static boolean isGangMemberOf(Player player, String gangName) {
        
        String playerGang = getGang(player);
        
        if (playerGang.equals(""))
            return false;
        
        return playerGang.equalsIgnoreCase(gangName);
        
    }
    
    public static boolean isGangMemberOf(String playerName, String gangName) {
        
        String playerGang = getGang(playerName);
        
        if (playerGang.equals(""))
            return false;
        
        return playerGang.equalsIgnoreCase(gangName);
        
    }
    
    public static boolean isAllyOf(Player player, String gangName) {
        
        String playerGang = getGang(player);
        
        if (playerGang.equals("")) {
            return false;
        }
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"' AND LOWER(allys) LIKE '%{"+playerGang.toLowerCase()+"}%'", "", "");
        
        if (results != null && !results.isEmpty()) {

            return true;
        }

        return false;
        
    }
    
    public static boolean isAllyOf(String playerName, String gangName) {
        
        String playerGang = getGang(playerName);
        
        if (playerGang.equals("")) {
            return false;
        }
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"' AND LOWER(allys) LIKE '%{"+playerGang.toLowerCase()+"}%'", "", "");
        
        if (results != null && !results.isEmpty()) {
            return true;
        }
        
        return false;
        
    }
    
    public static boolean isInvitedTo(Player player, String gangName) {
                
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"' AND LOWER(invites) LIKE '%{"+player.getName().toLowerCase()+"}%'", "", "");
        
        if (results != null && !results.isEmpty()) {

            return true;
        }

        return false;
        
    }
    
    public static boolean isInvitedTo(String playerName, String gangName) {
                
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"' AND LOWER(invites) LIKE '%{"+playerName.toLowerCase()+"}%'", "", "");
        
        if (results != null && !results.isEmpty()) {

            return true;
        }

        return false;
        
    }
    
    public static String getGang(Player player) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(members) LIKE '%{"+player.getName().toLowerCase()+"}%'", "", "");
        
        if (results == null || results.isEmpty()) {
            return "";
        }
        String gang = results.get(0).get("gangName");
        
        return gang;
        
    }
    
    public static String getGang(String playerName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("gangName", "MyChunkGangs", "LOWER(members) LIKE '%{"+playerName.toLowerCase()+"}%'", "", "");
        
        if (results == null || results.isEmpty()) {
            return "";
        } else {
            String gang = results.get(0).get("gangName");
                
            return gang;
        }
        
    }
    
    public static HashMap<String, String> getGangDetailed(String gangName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("*", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"'", "", "");

        if (results == null || results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
        
    }
    
    public static String getGangBoss(String gangName) {
        
        HashMap<Integer, HashMap<String, String>> results = SQLBridge.select("boss", "MyChunkGangs", "LOWER(gangName) = '"+gangName.toLowerCase()+"'", "", "");
        
        if (results == null || results.isEmpty()) {
            return "";
        } else {
            String boss = results.get(0).get("boss");
            
            return boss;
        }
        
    }
    
}
