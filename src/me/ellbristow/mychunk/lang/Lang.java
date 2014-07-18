package me.ellbristow.mychunk.lang;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Lang {

    private static FileConfiguration langStore;
    private static HashMap<String, String> lang = new HashMap<String, String>();

    public static String get(String key) {
        String newLang = lang.get(key);
        if (newLang != null) {
            return newLang;
        } else {
            return key;
        }
    }
    
    static {
        reload();
    }
    
    public static void reload() {
        File langFile = new File(Bukkit.getPluginManager().getPlugin("MyChunk").getDataFolder(),"lang.yml");
        langStore = YamlConfiguration.loadConfiguration(langFile);
        
        lang.clear();
        
        // General
        loadLangPhrase("MyChunkCommands", "MyChunk Commands");
        loadLangPhrase("Yes", "Yes");
        loadLangPhrase("No", "No");
        loadLangPhrase("On", "On");
        loadLangPhrase("Off", "Off");
        loadLangPhrase("true", "True");
        loadLangPhrase("false", "False");
        loadLangPhrase("Your", "Your");
        loadLangPhrase("'s", "'s");
        loadLangPhrase("Unowned", "Unowned");
        loadLangPhrase("Server", "Server");
        loadLangPhrase("Public", "Public");
        loadLangPhrase("Price", "Price");
        loadLangPhrase("Player", "Player");
        loadLangPhrase("Flags", "Flags");
        loadLangPhrase("Page", "Page");
        loadLangPhrase("Limit", "Limit");
        loadLangPhrase("Rate", "Rate");
        loadLangPhrase("Days", "Days");
        loadLangPhrase("World", "World");
        loadLangPhrase("Setting", "Setting");
        loadLangPhrase("Optional", "Optional");
        loadLangPhrase("Required", "Required");
        loadLangPhrase("Everyone", "EVERYONE");
        loadLangPhrase("None", "None");
        loadLangPhrase("By", "by");
        loadLangPhrase("Disabled", "Disabled");
        loadLangPhrase("All", "ALL");
        loadLangPhrase("Animals", "Animals");
        loadLangPhrase("Build", "Build");
        loadLangPhrase("Destroy", "Destroy");
        loadLangPhrase("AccessChests", "Access Chests");
        loadLangPhrase("Enter", "Enter Chunk");
        loadLangPhrase("IgniteBlocks", "Ignite Blocks");
        loadLangPhrase("DropLava", "Drop Lava");
        loadLangPhrase("DropWater", "Drop Water");
        loadLangPhrase("OpenWoodenDoors", "Open Wooden Doors");
        loadLangPhrase("UseButtonsLevers", "Use Buttons/Levers etc");
        loadLangPhrase("PVP", "PVP");
        loadLangPhrase("NoPVP", "NoPVP");
        loadLangPhrase("Mobs", "Mobs");
        loadLangPhrase("NoMobs", "NoMobs");
        
        // Info
        loadLangPhrase("ChunkForSale", "Chunk For Sale");
        loadLangPhrase("FirstChunkFree", "Your first chunk claim is free!");
        loadLangPhrase("AmountDeducted", "was deducted from your account");
        loadLangPhrase("BoughtFor", "bought one of your chunks for");
        loadLangPhrase("ClaimedYourChunk", "claimed a chunk you were selling");
        loadLangPhrase("ChunkClaimed", "Chunk claimed!");
        loadLangPhrase("ChunkClaimedFor", "Chunk claimed for");
        loadLangPhrase("ChunkUnclaimed", "Chunk unclaimed!");
        loadLangPhrase("ChunkUnclaimedFor", "Chunk unclaimed for");
        loadLangPhrase("YouOwn", "You own this chunk!");
        loadLangPhrase("OwnedBy", "This Chunk is owned by");
        loadLangPhrase("Allowed", "Allowed");
        loadLangPhrase("AllowedPlayers", "Allowed Players");
        loadLangPhrase("PermissionsUpdated", "Permissions updated!");
        loadLangPhrase("ChunkIs", "This chunk is");
        loadLangPhrase("StartClaimArea1", "Claim Area Started!");
        loadLangPhrase("StartClaimArea2", "Place a second [ClaimArea] sign to claim all chunks in the area.");
        loadLangPhrase("StartUnclaimArea1", "Unclaim Area Started!");
        loadLangPhrase("StartUnclaimArea2", "Place a second [UnclaimArea] sign to unclaim all chunks in the area.");
        loadLangPhrase("ClaimAreaCancelled", "Area claim cancelled!");
        loadLangPhrase("UnclaimAreaCancelled", "Area unclaim cancelled!");
        loadLangPhrase("YouWereCharged", "You were charged");
        loadLangPhrase("ChunksClaimed", "Chunks Claimed");
        loadLangPhrase("ChunksUnclaimed", "Chunks Unclaimed");
        loadLangPhrase("ChunksOwned", "Owned Chunks");
        loadLangPhrase("TotalClaimedChunks", "Total Claimed Chunks");
        loadLangPhrase("Yours", "Yours");
        loadLangPhrase("Unlimited", "Unlimited");
        loadLangPhrase("DefaultMax", "Default Max Chunks Per Player");
        loadLangPhrase("MaxChunkClaim", "Max Chunks You Can Claim");
        loadLangPhrase("Chunk", "Max Chunks");
        loadLangPhrase("AllowNeighbours", "Allow Neighbours");
        loadLangPhrase("ChunkPrice", "Next Chunk Price");
        loadLangPhrase("AllowOverbuy", "Allow Overbuy");
        loadLangPhrase("OverbuyFee", "Fee");
        loadLangPhrase("AllowMobGrief", "Allow Mob Griefing");
        loadLangPhrase("ProtectUnclaimed", "Protect Unclaimed");
        loadLangPhrase("Resales", "resales");
        loadLangPhrase("UnclaimRefunds", "Unclaim Refunds");
        loadLangPhrase("DaysWithoutLogin", "day(s) with no login");
        loadLangPhrase("ClaimExpiry", "Claim Expiry");
        loadLangPhrase("AllowNether", "Allow Nether");
        loadLangPhrase("AllowEnd", "Allow End");
        loadLangPhrase("OwnerNotifications", "Owner Notifications");
        loadLangPhrase("PreventEntry", "Prevent Chunk Entry");
        loadLangPhrase("PreventPVP", "PVP Prevention");
        loadLangPhrase("PermissionFlags", "Permission Flags");
        loadLangPhrase("Reloaded", "Mychunk files have been reloaded!");
        loadLangPhrase("ToggleRefundOn", "Unclaiming chunks now provides a refund");
        loadLangPhrase("ToggleRefundOff", "Unclaming chunks no longer provides a refund");
        loadLangPhrase("ToggleOverbuyOn", "Players may now overbuy chunks");
        loadLangPhrase("ToggleOverbuyOff", "Players now cannot overbuy chunks");
        loadLangPhrase("ToggleResalesOn", "Players may now pay overbuy fees on resales");
        loadLangPhrase("ToggleResalesOff", "Players no longer pay overbuy fees on resales");
        loadLangPhrase("ToggleNeighboursOn", "Players may now claim chunks next to other players");
        loadLangPhrase("ToggleNeighboursOff", "Players no longer claimchunks next to other players");
        loadLangPhrase("ToggleUnclaimedOn", "Unclaimed chunks are now protected");
        loadLangPhrase("ToggleUnclaimedOff", "Unclaimed chunks are no longer protected");
        loadLangPhrase("ToggleTNTOn", "TNT protection in unclaimed chunks is now enabled");
        loadLangPhrase("ToggleTNTOff", "TNT protection in unclaimed chunks is now disabled");
        loadLangPhrase("ToggleExpiryOn", "Claimed chunks now expire after inactivity");
        loadLangPhrase("ToggleExpiryOff", "Claimed chunks no longer expire after inactivity");
        loadLangPhrase("ToggleNetherCannot", "Users now CANNOT claim chunks in Nether worlds");
        loadLangPhrase("ToggleNetherCan", "Users now CAN claim chunks in Nether worlds");
        loadLangPhrase("ToggleEndCannot", "Users now CANNOT claim chunks in End worlds");
        loadLangPhrase("ToggleEndCan", "Users now CAN claim chunks in End worlds");
        loadLangPhrase("ToggleNotifyOn", "Owners will now receive protection notifications");
        loadLangPhrase("ToggleNotifyOff", "Owners will now NOT receive protection notifications");
        loadLangPhrase("ToggleEntryOn", "Chunk entry protection enabled");
        loadLangPhrase("ToggleEntryOff", "Chunk entry protection disabled");
        loadLangPhrase("TogglePVPOn", "PVP protection enabled");
        loadLangPhrase("TogglePVPOff", "PVP protection disabled");
        loadLangPhrase("ToggleMobGriefOn", "Mob Griefing IS now Allowed");
        loadLangPhrase("ToggleMobGriefOff", "Mob Griefing is NO LONGER Allowed");
        loadLangPhrase("ToggleChatFormatOn", "Now using Chat Formatting");
        loadLangPhrase("ToggleChatFormatOff", "No longer using Chat Formatting");
        loadLangPhrase("ToggleFirstChunkFreeOn", "Players can now claim 1 chunk for free");
        loadLangPhrase("ToggleFirstChunkFreeOff", "Players now DO NOT get their first chunk free!");
        loadLangPhrase("RefundSet", "Refund Percentage set to ");
        loadLangPhrase("RampRateSet", "Ramp Rate set to ");
        loadLangPhrase("Percentage", "Percentage");
        loadLangPhrase("WorldEnabled", "World enabled");
        loadLangPhrase("WorldDisabled", "World disabled");
        loadLangPhrase("AllWorldsEnabled", "All worlds enabled");
        loadLangPhrase("AllWorldsDisabled", "All worlds disabled");
        loadLangPhrase("NewRampRate", "New rate");
        loadLangPhrase("NextPage", "Next Page");
        loadLangPhrase("OwnedChunks", "owned chunks");
        
        // Command Messages
        loadLangPhrase("CommandMessageStats", "Show stats and plugin settings");
        loadLangPhrase("CommandMessageHelp", "Command Help");
        loadLangPhrase("CommandMessageInfo", "Claimed chunk info");
        loadLangPhrase("CommandMessageAllow", "Allow players access to your chunk");
        loadLangPhrase("CommandMessageDisallow", "Disallow chunk access for players");
        loadLangPhrase("CommandMessageAllowAll", "Allow players access to all your chunks");
        loadLangPhrase("CommandMessageDisallowAll", "Disallow access for players in all your chunks");
        loadLangPhrase("CommandMessageClaim", "Claim the chunk you're standing in");
        loadLangPhrase("CommandMessageClaimArea", "Claim an area of chunks");
        loadLangPhrase("CommandMessageUnclaim", "Unclaim the chunk you're standing in");
        loadLangPhrase("CommandMessageUnclaimArea", "Claim an area of chunks");
        loadLangPhrase("CommandMessageFlags", "List all available chunk flags");
        loadLangPhrase("CommandMessageForsale", "Put your chunk up for sale");
        loadLangPhrase("CommandMessageNewGangMultiplier", "New maximum chunks per gang member set to");
        loadLangPhrase("CommandMessageGangMultiplier", "Set maximum chunks per gang member");
        loadLangPhrase("CommandMessageGangnamelength", "Set maximum length of a Gang name");
        loadLangPhrase("CommandMessageAllowmobs", "Allow mobs to spawn in the current chunk");
        loadLangPhrase("CommandMessageAllowmobs", "Set if mobs can spawn in the current chunk");
        loadLangPhrase("CommandMessageAllowpvp", "Set if PVP is allowed in the current chunk");
        loadLangPhrase("CommandMessageMax", "Set new maximum chunk claim limit");
        loadLangPhrase("CommandMessageNotforsale", "Take your chunk off sale");
        loadLangPhrase("CommandMessageObprice", "Set new overbuy price");
        loadLangPhrase("CommandMessageOwner", "See who owns the chunk you're in");
        loadLangPhrase("CommandMessagePrice", "Set new chunk price");
        loadLangPhrase("CommandMessageRamprate", "Set new price ramping rate");
        loadLangPhrase("CommandMessageExpirydays", "Set new chunk expiry cutoff");
        loadLangPhrase("CommandMessagePercentage", "Set new unclaim refund percentage");
        loadLangPhrase("CommandMessagePurgep", "Unclaim all chunks claimed by a player");
        loadLangPhrase("CommandMessagePurgew", "Unclaim all chunks claimed in a world");
        loadLangPhrase("CommandMessageReload", "Reload MyChunk config");
        loadLangPhrase("CommandMessageToggle", "Toggle plugin settings");
        loadLangPhrase("CommandMessageAvailable", "Available settings");
        loadLangPhrase("CommandMessageChunkPrice", "Chunk price set to");
        loadLangPhrase("CommandMessageOverbuyPrice", "Overbuy price set to");
        loadLangPhrase("CommandMessageMax", "Max Chunks is now set at");
        loadLangPhrase("CommandMessageAllChunksFor", "All chunk for");
        loadLangPhrase("CommandMessageAllChunksIn", "All chunk in");
        loadLangPhrase("CommandMessageAreNow", "are now");
        loadLangPhrase("CommandMessageClaimedChunksExpire", "Claimed chunks will now expire");
        loadLangPhrase("CommandMessageDaysofInactivity", "day(s) of inactivity");
        
        //Errors
        loadLangPhrase("AlreadyOwner", "You already own this chunk!");
        loadLangPhrase("AlreadyOwned", "This Chunk is already owned by");
        loadLangPhrase("AlreadyOwn", "You already own");
        loadLangPhrase("ChunkNotOwned", "This chunk is not owned!");
        loadLangPhrase("Chunks", "chunks");
        loadLangPhrase("ClaimWorldDisabled", "You cannot claim chunks in this world!");
        loadLangPhrase("NoNeighbours", "You cannot claim a chunk next to someone else's chunk!");
        loadLangPhrase("CantAfford", "You cannot afford to claim that chunk!");
        loadLangPhrase("GangCantAfford", "Your Gang cannot afford to claim this chunk!");
        loadLangPhrase("MaxChunksReached", "Claiming that would put you over your maximum chunk limit!");
        loadLangPhrase("DoNotOwn", "You do not own this chunk!");
        loadLangPhrase("GangDoesNotOwn", "Your Gang does not own this chunk!");
        loadLangPhrase("NoChunksOwned", "You do not own any chunks!");
        loadLangPhrase("Line2Player", "Line 2 must contain a player name (or * for all)!");
        loadLangPhrase("AllowSelf", "You dont need to allow yourself!");
        loadLangPhrase("DisallowSelf", "You cannot disallow yourself!");
        loadLangPhrase("CannotDestroyClaim", "You cannot destroy another player's Claim sign!");
        loadLangPhrase("ClaimAreaWorldError", "[ClaimArea] signs must both be in the same world!");
        loadLangPhrase("UnclaimAreaWorldError", "[UnclaimArea] signs must both be in the same world!");
        loadLangPhrase("UnclaimAreaNoneFound", "There were no chunks to be unclaimed in that area!");
        loadLangPhrase("AreaTooBig", "You cannot claim more than 64 chunks in one area!");
        loadLangPhrase("UnclaimAreaTooBig", "You cannot unclaim more than 64 chunks in one area!");
        loadLangPhrase("FoundClaimedInArea", "At least one chunk in the specified area is already claimed!");
        loadLangPhrase("FoundNeighboursInArea", "At least one chunk in the specified area has a neighbour!");
        loadLangPhrase("ClaimAreaTooLarge", "cannot claim that many chunks!");
        loadLangPhrase("ChunksInArea", "Chunks In Area");
        loadLangPhrase("CantAffordClaimArea", "You cannot afford to buy that many chunks!");
        loadLangPhrase("SpecifyNewGangMultiplier", "You must specify a new maximum chunk limit!");
        loadLangPhrase("SpecifyNewMaxChunks", "You must specify a new maximum chunk limit!");
        loadLangPhrase("NewLimit", "new limit");
        loadLangPhrase("SpecifyNewChunkPrice", "You must specify a new chunk price!");
        loadLangPhrase("NewPrice", "New Price");
        loadLangPhrase("SpecifyNewOverbuyPrice", "You must specify a new overbuy price!");
        loadLangPhrase("SpecifyToggle", "You must specify what to toggle!");
        loadLangPhrase("SpecifyPurgePlayer", "You must specify which player to purge!");
        loadLangPhrase("PlayerName", "Player Name");
        loadLangPhrase("SpecifyPurgeWorld", "You must specify which world to purge!");
        loadLangPhrase("WorldName", "World Name");
        loadLangPhrase("NoEcoPlugin", "There is no economy plugin running! Command aborted.");
        loadLangPhrase("NoOverbuy", "Overbuy is disabled! Command aborted.");
        loadLangPhrase("NoUnclaimed", "Unclaimed chunk protection is not enabled! Command aborted.");
        loadLangPhrase("NotFound", "not found");
        loadLangPhrase("FactionsClash", "You cannot claim land owned by a faction!");
        loadLangPhrase("TownyClash", "You cannot claim land owned by a town!");
        loadLangPhrase("MyChunkClash", "That land is already owned!");
        loadLangPhrase("NotPublicSign", "You can't use that sign in public chunks!");
        loadLangPhrase("NotPublicCommand", "You can't use that command in public chunks!");
        loadLangPhrase("SpecifyRefund", "You must specify a new refund percentage!");
        loadLangPhrase("SpecifyRampRate", "You must specify a new price ramp rate!");
        loadLangPhrase("RefundNotNumber", "Refund percentage must be a number!");
        loadLangPhrase("CommandMustBeRunByPlayer", "This command must be run by a player!");
        loadLangPhrase("ServerNoChunks", "The console doesn't own any chunks!");
        loadLangPhrase("NoEntry", "has denied you access to this chunk!");
        loadLangPhrase("PVPNotAllowed", "PVP is not allowed here!");
        loadLangPhrase("CannotFindWorld", "Cannot find a world called");
        loadLangPhrase("CommandNotRecognised", "Command not recognised:");
        loadLangPhrase("Try", "Try");
        loadLangPhrase("PriceNotNumber", "Price must be a number!");
        loadLangPhrase("PageNotNumber", "Page must be a number!");
        loadLangPhrase("RateNotNumber", "Rate must be a number!");
        loadLangPhrase("AmountNotInteger", "Amount must be a integer!");
        loadLangPhrase("NoChunksOwned", "You do not own any chunks");
        loadLangPhrase("Doesn'tOwnChunks", "doesn't own any chunks");
        loadLangPhrase("ClaimExpiryDisabled", "Claim Expiry is disabled!");
        loadLangPhrase("SpecifyClaimExpiry", "You must specify a new expiry period!");
        loadLangPhrase("NotInteger", "must be a integer");
        loadLangPhrase("LessThanOne", "must be greater than 0");
        loadLangPhrase("SpecifyPlayer", "You must specify a player name!");
        loadLangPhrase("FlagsNotFound", "Flags Not Found");
        loadLangPhrase("ReceivedFlags", "received the following flags");
        loadLangPhrase("LostFlags", "lost the following flags");
        loadLangPhrase("ReceivedFlagsAll", "received the following flags on all our chunks");
        loadLangPhrase("LostFlagsAll", "lost the following flags on all our chunks");
        loadLangPhrase("NewFlags", "New Flags");
        loadLangPhrase("DisallowEveryone", "You cannot disallow flags allowed to EVERYONE!");
        loadLangPhrase("DenyPublicEntry", "You cannot deny entry access (E) on public chunks!");
        loadLangPhrase("Line2SellPrice", "Line 2 must contain your sale price!");
        loadLangPhrase("SpecifySellPrice", "You must specify your sale price!");
        loadLangPhrase("SellPriceNumber", "Sell price must be a number!");
        loadLangPhrase("SellPriceZero", "Sale price cannot be 0!");
        loadLangPhrase("ChunkOnSale", "Chunk For Sale");
        loadLangPhrase("ChunkOffSale", "Chunk taken off sale!");
        loadLangPhrase("ChunkNotForSale", "This chunk is not for sale!");
        loadLangPhrase("SpecifyOnOff", "You must specify 'on' or 'off'!");
        loadLangPhrase("MobsCanSpawn", "Mobs now CAN spawn in this chunk!");
        loadLangPhrase("MobsCannotSpawn", "Mobs now CANNOT spawn in this chunk!");
        loadLangPhrase("PVPAllowed", "PVP is now ALLOWED in this chunk!");
        loadLangPhrase("PVPDisallowed", "PVP is now DISALLOWED in this chunk!");
        loadLangPhrase("LeaseRequiresEco", "[Lease] signs require an economy plugin!");
        loadLangPhrase("LeaseOnWall", "[Lease] signs must be attached to a wall!");
        loadLangPhrase("LeaseAboveDoor", "[Lease] signs must be above a Wooden Door, Gate or Hatch!");
        loadLangPhrase("DoorAlreadyLeased", "This door already has a [Lease]!");
        loadLangPhrase("Line2LeasePrice", "Line 2 must contain the lease price (or 0)!");
        loadLangPhrase("LeaseCreated", "Lease created!");
        
        // Permissions
        loadLangPhrase("NoPermsCommand", "You do not have permission to use this command!");
        loadLangPhrase("NoPermsAnimals", "You do not have permission to interact with animals here!");
        loadLangPhrase("NoPermsBuild", "You do not have permission to build here!");
        loadLangPhrase("NoPermsBreak", "You do not have permission to break blocks here!");
        loadLangPhrase("NoPermsFire", "FIRE! Oh phew... you're not allowed!");
        loadLangPhrase("NoPermsLava", "Are you crazy!? You can't drop lava there!");
        loadLangPhrase("NoPermsWater", "Are you crazy!? You can't drop water there!");
        loadLangPhrase("NoPermsDoor", ">KNOCK< >KNOCK< This door is locked!");
        loadLangPhrase("NoPermsDoorOwner", ">KNOCK< >KNOCK< Someone is visiting your chunk!");
        loadLangPhrase("NoPermsButton", ">BUZZZ< The button tripped a silent alarm!");
        loadLangPhrase("NoPermsButtonOwner", ">BUZZ< Someone pressed a button in your chunk!");
        loadLangPhrase("NoPermsLever", ">CLICK< The lever tripped a silent alarm!");
        loadLangPhrase("NoPermsLeverOwner", ">CLICK< Someone touched a lever in your chunk!");
        loadLangPhrase("NoPermsChest", ">CLUNK< That chest isn't yours!");
        loadLangPhrase("NoPermsChestOwner", ">CLUNK< Someone tryed to open a chest on your chunk!");
        loadLangPhrase("NoPermsSpecial", ">BUZZZ< Hands off! That's a special block!");
        loadLangPhrase("NoPermsSpecialOwner", ">BUZZZ< Someone touched a special block in your chunk!");
        loadLangPhrase("NoPermsPVP", "That player is protected by a magic shield!");
        loadLangPhrase("NoPermsClaim", "You do not have permission to claim chunks!");
        loadLangPhrase("NoPermsClaimArea", "You do not have permission to use [ClaimArea] signs!");
        loadLangPhrase("NoPermsBuyOwned", "You do not have permission to buy owned chunks!");
        loadLangPhrase("NoPermsClaimServer", "You do not have permission to claim chunks for the server!");
        loadLangPhrase("NoPermsClaimPublic", "You do not have permission to claim public chunks!");
        loadLangPhrase("NoPermsClaimOther", "You do not have permission to claim chunks for other players!");
        loadLangPhrase("NoPermsUnclaimServer", "You do not have permission to unclaim chunks for the server!");
        loadLangPhrase("NoPermsUnclaimPublic", "You do not have permission to unclaim public chunks!");
        loadLangPhrase("NoPermsUnclaimOther", "You do not have permission to unclaim chunks for other players!");
        loadLangPhrase("NoPermsNether", "You do not have permission to claim chunks in Nether worlds!");
        loadLangPhrase("NoPermsEnd", "You do not have permission to claim chunks in End worlds!");
        loadLangPhrase("NoPermsSell", "You do not have permission to sell chunks!");
        loadLangPhrase("NoPermsSellFree", "You can claim chunks for free! You're not allowed to sell them!");
        loadLangPhrase("NoPermsMobSign", "You do not have permission to use [AllowMobs] signs!");
        loadLangPhrase("NoPermsPVPSign", "You do not have permission to use [AllowPVP] signs!");
        loadLangPhrase("NoPermsLease", "You do not have permission to use [Lease] signs!");
        loadLangPhrase("NoPermsDisbandOther", "You do not have permission to disband other gangs!");
        
        /* GANG MESSAGES */
        
        // Gang Help Messages
        loadLangPhrase("GangCommands", "Gang Commands");
        loadLangPhrase("CommandMessageGangClaim", "Claim chunk for your gang");
        loadLangPhrase("CommandMessageGangCreate", "Create new gang");
        loadLangPhrase("CommandMessageGangDisband", "Disband your gang");
        loadLangPhrase("CommandMessageGangDetails", "Gang information");
        loadLangPhrase("CommandMessageGangHelp", "Gang command help");
        loadLangPhrase("CommandMessageGangInvite", "Invite player to your gang");
        loadLangPhrase("CommandMessageGangInviteCancel", "Cancel player invite to your gang");
        loadLangPhrase("CommandMessageGangUnclaim", "Unclaim chunk for your gang");
        loadLangPhrase("CommandMessageGangJoin", "Join a gang");
        loadLangPhrase("CommandMessageGangLeave", "Leave your gang");
        
        // General Gang Phrases
        loadLangPhrase("None", "None");
        loadLangPhrase("GangName", "Gang Name");
        loadLangPhrase("Boss", "Boss");
        loadLangPhrase("Assistants", "Assistants");
        loadLangPhrase("Members", "Members");
        loadLangPhrase("Influence", "Influence");
        loadLangPhrase("Invites", "Invites");
        loadLangPhrase("Allys", "Allys");
        loadLangPhrase("Enemies", "Enemies");
        
        // Gang Command Responses
        loadLangPhrase("NotInGang", "You are not in a Gang!");
        loadLangPhrase("GangCreated", "started a new Gang");
        loadLangPhrase("AlreadyInGang", "You are already in a Gang! Leave your Gang first!");
        loadLangPhrase("GangExists", "That Gang already exists!");
        loadLangPhrase("SpecifyGangName", "You must specify a Gang name!");
        loadLangPhrase("GangNameTooLong", "That Gang name is too long!");
        loadLangPhrase("SpecifyNewGangnamelength", "You musy specify a new Gang name length!");
        loadLangPhrase("CommandMessageGangnamelength", "New Gang name length set to");
        loadLangPhrase("NotGangBoss", "Only the Gang boss can do that!");
        loadLangPhrase("NotGangBossOrAssistant", "Only the Gang boss or an assistant can do that!");
        loadLangPhrase("GangFundsWithdrawn", "withdrawn from Gang account.");
        loadLangPhrase("GangDisbanded", "Gang has been disbanded!");
        loadLangPhrase("NotGang", "Gang name not found!");
        loadLangPhrase("SpecifyPlayerToInvite", "You musy specify a player to invite!");
        loadLangPhrase("NoInvitationFoundFor", "No invitation found for");
        loadLangPhrase("InvitationCancelledFor", "Gang invitation cancelled for");
        loadLangPhrase("NoLongerInvitedTo", "You are no longer invited to");
        loadLangPhrase("InvitationAddedFor", "Gang invitation added for");
        loadLangPhrase("InvitedToGang", "You received a gang invitation from");
        loadLangPhrase("SpecifyGangToJoin", "You musy specify a gang to join!");
        loadLangPhrase("NotInvitedToGang", "You are not invited to join this gang!");
        loadLangPhrase("JoinedGang", "joined the gang");
        loadLangPhrase("AlreadyGangMember", "is already a gang member!");
        loadLangPhrase("BossCantLeave", "The Gang Boss can't leave!");
        loadLangPhrase("LeftGang", "left the gang");
        loadLangPhrase("AlreadyInGang", "You are already in a gang!");
        
        try {
            langStore.save(langFile);
        } catch (IOException ex) {
            Bukkit.getLogger().severe("[MyChunk] Could not save " + langFile);
        }
    }
    
    private static void loadLangPhrase(String key, String defaultString) {
        String value = langStore.getString(key, defaultString);
        langStore.set(key, value);
        lang.put(key, value);
    }

}
