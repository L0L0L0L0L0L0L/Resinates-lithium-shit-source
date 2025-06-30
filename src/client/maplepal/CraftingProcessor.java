package client.maplepal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import client.MapleCharacter;
import database.DatabaseConnection;
import handling.SendPacketOpcode;
import java.util.Collections;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;

public class CraftingProcessor {

    public record CraftingEntry(int quantity, int craftTime, CraftingIngredient[] ingredients) {

    }

    public record CraftingIngredient(int itemId, int quantity) {

    }

    private static Map<Integer, CraftingEntry> recipes;

    //Only for convenience
    static {
        reloadRecipes();
    }

    //(Re)loads recipes from DB.
    public synchronized static void reloadRecipes() {
        //Test code
        recipes = new HashMap<>(); //HashMap is safe to use even with unsynchronized reading because it's recreated and not modified
        try ( Connection con = DatabaseConnection.getWorldConnection();  PreparedStatement ps = con.prepareStatement("SELECT * FROM recipes");  ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CraftingIngredient[] ingredients = new CraftingIngredient[4];
                for (int i = 0; i < 4; i++) {
                    ingredients[i] = new CraftingIngredient(rs.getInt("ritem" + (i + 1)), rs.getInt("ritemamount" + (i + 1)));
                }
                CraftingEntry entry = new CraftingEntry(rs.getInt("amount"), rs.getInt("time"), Arrays.stream(ingredients).filter(i -> i.itemId != 0).toArray(CraftingIngredient[]::new));
                recipes.put(rs.getInt("itemid"), entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //JDBI not working, no time to fix, leave this code here for when i do fix it
        /*
		Jdbi.j.useHandle(h -> h.createQuery("SELECT * FROM recipes").map((rs, ctx) -> {
			CraftingIngredient[] ingredients = new CraftingIngredient[4];
			for(int i = 0; i < 4; i++) {
				ingredients[i] = new CraftingIngredient(rs.getInt("ritem" + (i + 1)), rs.getInt("ritemamount" + (i + 1)));
			}
			CraftingEntry entry = new CraftingEntry(rs.getInt("amount"), rs.getInt("time"), Arrays.stream(ingredients).filter(i -> i.itemId != 0).toArray(CraftingIngredient[]::new));
			recipes.put(rs.getInt("itemid"), entry);
			
			return null;
		}));*/
    }

    public static List<Integer> getAllRecipeIds() {
        List<Integer> recipe = new ArrayList<>(recipes.keySet());
        Collections.sort(recipe);
        return recipe;
    }

    public static int getRecipeCraftingTime(int itemId) {
        CraftingEntry recipe = recipes.get(itemId);
        if (recipe == null) {
            return -1;
        }
        return recipe.craftTime;
    }

    public static CraftingEntry getRecipe(int itemId) {
        if (recipes.containsKey(itemId)) {
            return recipes.get(itemId);
        }
        return null;
    }

    public static int getMaxCraftable(MapleCharacter chr, int itemId) {
        CraftingEntry recipe = recipes.get(itemId);
        if (recipe == null) {
            return -1;
        }
        int maxCraftable = Integer.MAX_VALUE;
        for (CraftingIngredient ing : recipe.ingredients) {
            int maxForIngredient = (int) Math.floor(chr.countTotalItem(ing.itemId) / ing.quantity);
            if (maxForIngredient < maxCraftable) {
                maxCraftable = maxForIngredient;
            }
        }
        return maxCraftable != Integer.MAX_VALUE ? maxCraftable : 0;
    }

    /**
     * @param error 0 for success
     */
    public static byte[] sendCraftResult(int error) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_CRAFT_RESULT.getValue());
        mplew.writeBool(error != 0); //bError
        if (error != 0) {
            mplew.write(error);
        }
        return mplew.getPacket();
    }

    public static byte[] sendCraftingRecipes(List<Integer> unlockedRecipes) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_CRAFT_LOAD.getValue());
        mplew.writeShort(recipes.size());
        mplew.writeShort(unlockedRecipes.size());
        for (Integer itemId : unlockedRecipes) {
            CraftingEntry recipe = recipes.get(itemId);
            if (recipe != null) { //Could be null if player still has "unlocked" recipe that no longer exists
                mplew.writeInt(itemId);
                mplew.writeInt(recipe.quantity);
                mplew.writeInt(recipe.craftTime);
                mplew.write(recipe.ingredients.length);
                for (CraftingIngredient ig : recipe.ingredients) {
                    mplew.writeInt(ig.itemId());
                    mplew.writeInt(ig.quantity());
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] sendOpenWindow(Map<Integer, AtomicLong> overflowItems) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_CRAFT_OPEN.getValue());
        mplew.writeShort(overflowItems.size());
        for (Entry<Integer, AtomicLong> e : overflowItems.entrySet()) {
            mplew.writeInt(e.getKey());
            mplew.writeLong(e.getValue().get());
        }
        return mplew.getPacket();
    }

    public static byte[] sendOverflowWindow(Map<Integer, AtomicLong> overflowItems) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_OVERFLOW_OPEN.getValue());
        mplew.writeShort(overflowItems.size());
        for (Entry<Integer, AtomicLong> e : overflowItems.entrySet()) {
            mplew.writeInt(e.getKey());
            mplew.writeLong(e.getValue().get());
        }
        return mplew.getPacket();
    }

    public static byte[] sendUpdateOverflowItems(List<Pair<Integer, Long>> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_CRAFT_UPDATE.getValue());
        mplew.write(items.size());
        for (Pair<Integer, Long> p : items) {
            mplew.writeInt(p.getLeft());
            mplew.writeLong(p.getRight());
        }
        return mplew.getPacket();
    }
}
