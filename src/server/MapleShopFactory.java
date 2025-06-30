package server;

import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.FilePrinter;
import tools.Pair;

public class MapleShopFactory {

    private final Map<Integer, MapleShop> shops = new LinkedHashMap<>();
    private final Map<Integer, MapleShop> npcShops = new LinkedHashMap<>();
    private static final MapleShopFactory instance = new MapleShopFactory();

    public static MapleShopFactory getInstance() {
        return instance;
    }

    public void clear() {
        shops.clear();
        npcShops.clear();
    }

    public MapleShop getShop(int shopId) {
        if (shops.containsKey(shopId)) {
            return shops.get(shopId);
        }
        return null;
    }

    public MapleShop getShopForNPC(int npcId) {
        if (npcShops.containsKey(npcId)) {
            return npcShops.get(npcId);
        }
        return null;
    }

    private MapleShop loadShop(int id, boolean isShopId) {
        MapleShop ret = MapleShop.createFromDB(id, isShopId);
        if (ret != null) {
            shops.put(ret.getId(), ret);
        } else {
            shops.put(id, null);
        }
        return ret;
    }

    public void removeShop(int id) {
        if (shops.containsKey(id)) {
            shops.remove(id);
        }
        if (npcShops.containsKey(id)) {
            npcShops.remove(id);
        }
    }

    public void executeShops() {
        loadShops();
        System.out.println(shops.size() + " Shops Loaded...");
        loadShopsNpc();
        System.out.println(npcShops.size() + " Shops Loaded...");
        getShop(9300004).changeNXShop();
        getShop(2050012).changeQuestShop();
        System.out.println("Special Shops Restocked...");
    }

    public void loadShops() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        try (Connection con = DatabaseConnection.getWorldConnection()) {

            PreparedStatement ps = con.prepareStatement("SELECT * FROM shops ORDER BY shopid ASC");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int shopId = rs.getInt("shopid");
                    if (!shops.containsKey(shopId)) {
                        MapleShop ret = new MapleShop(shopId, rs.getInt("npcid"));
                        PreparedStatement ps2 = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
                        ps2.setInt(1, shopId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                if (!ii.itemExists(rs2.getInt("itemid"))) {
                                    continue;
                                }
                                short slotmax = rs2.getShort("buyable") > 0 ? rs2.getShort("buyable") : ii.getSlotMax(rs2.getInt("itemid"));
                                if (GameConstants.isRechargeable(rs2.getInt("itemid"))) {
                                    MapleShopItem starItem = new MapleShopItem((short) 1, rs2.getInt("itemid"), rs2.getInt("price"), rs2.getInt("reqitem"), rs2.getInt("reqitemq"), rs2.getByte("rank"), rs2.getInt("category"), rs2.getInt("minLevel"), rs2.getInt("expiration"), rs2.getInt("amount"));
                                    ret.addItem(starItem);
                                } else {
                                    ret.addItem(new MapleShopItem(slotmax, rs2.getInt("itemid"), rs2.getInt("price"), rs2.getInt("reqitem"), rs2.getInt("reqitemq"), rs2.getByte("rank"), rs2.getInt("category"), rs2.getInt("minLevel"), rs2.getInt("expiration"), rs2.getInt("amount")));
                                }
                            }
                        }
                        PreparedStatement ps3 = con.prepareStatement("SELECT * FROM shopranks WHERE shopid = ? ORDER BY rank ASC");
                        ps3.setInt(1, shopId);
                        ResultSet rs3 = ps3.executeQuery();
                        while (rs3.next()) {
                            if (!ii.itemExists(rs3.getInt("itemid"))) {
                                continue;
                            }
                            ret.ranks.add(new Pair<Integer, String>(rs3.getInt("itemid"), rs3.getString("name")));
                        }
                        rs3.close();
                        ps3.close();
                        shops.put(ret.getId(), ret);
                    }
                }
            }
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
    }

    public void loadShopsNpc() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        try (Connection con = DatabaseConnection.getWorldConnection()) {

            PreparedStatement ps = con.prepareStatement("SELECT * FROM shops ORDER BY npcid ASC");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int npcId = rs.getInt("npcid");
                    int shopId = rs.getInt("shopid");
                    if (!npcShops.containsKey(npcId)) {
                        MapleShop ret = new MapleShop(npcId, rs.getInt("npcid"), shopId);
                        PreparedStatement ps2 = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
                        ps2.setInt(1, shopId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next()) {
                                if (!ii.itemExists(rs2.getInt("itemid"))) {
                                    continue;
                                }
                                short slotmax = rs2.getShort("buyable") > 0 ? rs2.getShort("buyable") : ii.getSlotMax(rs2.getInt("itemid"));
                                if (GameConstants.isRechargeable(rs2.getInt("itemid"))) {
                                    MapleShopItem starItem = new MapleShopItem((short) 1, rs2.getInt("itemid"), rs2.getInt("price"), rs2.getInt("reqitem"), rs2.getInt("reqitemq"), rs2.getByte("rank"), rs2.getInt("category"), rs2.getInt("minLevel"), rs2.getInt("expiration"), rs2.getInt("amount"));
                                    ret.addItem(starItem);
                                } else {
                                    ret.addItem(new MapleShopItem(slotmax, rs2.getInt("itemid"), rs2.getInt("price"), rs2.getInt("reqitem"), rs2.getInt("reqitemq"), rs2.getByte("rank"), rs2.getInt("category"), rs2.getInt("minLevel"), rs2.getInt("expiration"), rs2.getInt("amount")));
                                }
                            }
                        }
                        PreparedStatement ps3 = con.prepareStatement("SELECT * FROM shopranks WHERE shopid = ? ORDER BY rank ASC");
                        ps3.setInt(1, shopId);
                        ResultSet rs3 = ps3.executeQuery();
                        while (rs3.next()) {
                            if (!ii.itemExists(rs3.getInt("itemid"))) {
                                continue;
                            }
                            ret.ranks.add(new Pair<Integer, String>(rs3.getInt("itemid"), rs3.getString("name")));
                        }
                        rs3.close();
                        ps3.close();
                        shops.put(ret.getId(), ret);
                        npcShops.put(ret.getNpcId(), ret);
                    }
                }
            }
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
    }

    public void reloadShop(int id, boolean npc) {
        if (shops.containsKey(id)) {
            shops.remove(id);
        }
        if (npcShops.containsKey(id)) {
            npcShops.remove(id);
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement(npc ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int shopId = rs.getInt("shopid");
                    MapleShop ret = new MapleShop(shopId, rs.getInt("npcid"));
                    PreparedStatement ps2 = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
                    ps2.setInt(1, shopId);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            if (!ii.itemExists(rs2.getInt("itemid"))) {
                                continue;
                            }
                            short slotmax = rs2.getShort("buyable") > 0 ? rs2.getShort("buyable") : ii.getSlotMax(rs2.getInt("itemid"));
                            if (GameConstants.isRechargeable(rs2.getInt("itemid"))) {
                                MapleShopItem starItem = new MapleShopItem((short) 1, rs2.getInt("itemid"), rs2.getInt("price"), rs2.getInt("reqitem"), rs2.getInt("reqitemq"), rs2.getByte("rank"), rs2.getInt("category"), rs2.getInt("minLevel"), rs2.getInt("expiration"), rs.getInt("amount"));
                                ret.addItem(starItem);
                            } else {
                                ret.addItem(new MapleShopItem(slotmax, rs2.getInt("itemid"), rs2.getInt("price"), rs2.getInt("reqitem"), rs2.getInt("reqitemq"), rs2.getByte("rank"), rs2.getInt("category"), rs2.getInt("minLevel"), rs2.getInt("expiration"), rs.getInt("amount")));
                            }
                        }
                    }
                    PreparedStatement ps3 = con.prepareStatement("SELECT * FROM shopranks WHERE shopid = ? ORDER BY rank ASC");
                    ps3.setInt(1, shopId);
                    ResultSet rs3 = ps3.executeQuery();
                    while (rs3.next()) {
                        if (!ii.itemExists(rs3.getInt("itemid"))) {
                            continue;
                        }
                        ret.ranks.add(new Pair<Integer, String>(rs3.getInt("itemid"), rs3.getString("name")));
                    }
                    rs3.close();
                    ps3.close();
                    if (npc) {
                        npcShops.put(shopId, ret);
                    } else {
                        shops.put(shopId, ret);
                    }
                }
            }
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
    }
}
