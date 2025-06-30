package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import client.inventory.Item;
import client.SkillFactory;
import constants.GameConstants;
import client.inventory.MapleInventoryIdentifier;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import database.DatabaseConnection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.CField.NPCPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InventoryPacket;

public class MapleShop {

    private int id;
    private int npcId;
    private int shopId = 0;
    private int day = 0;
    public int standId = 0;
    public List<MapleShopItem> items = new LinkedList<MapleShopItem>();
    public List<Pair<Integer, String>> ranks = new ArrayList<Pair<Integer, String>>();
    private static final Set<Integer> rechargeableItems = new LinkedHashSet<Integer>();
    private static final int[] rechargeable = {2070000, 2070001, 2070002, 2070003, 2070004, 2070005, 2070006, 2070007, 2070008, 2070009, 2070010, 2070011, 2070012, 2070013, 2070016, 2070018, 2070023, 2070024, 2330000, 2330001, 2330002, 2330003, 2330004, 2330005, 2330008, 2331000, 2332000};

    private static final Map<Integer, Integer> prices = new LinkedHashMap<Integer, Integer>();
    private static final int[] coin = {4310500, 4310501, 4310015, 4310020, 4310028, 4310064, 4310065, 4310066, 4310156, 4310211, 4310218, 4310249, 4310260, 4310018, 4310009, 4310010, 4310241, 4310150};
    private static final int[] base = {1, 5, 25, 100, 25, 50, 25, 500, 25, 100, 25, 25, 25, 5, 25, 25, 1000, 1};

    private static final List<Integer> itemz = Arrays.asList(new Integer[]{2587000, 2049300, 2049301, 2049302, 2049303, 2049304, 2049185, 2049186, 2049187, 2049188, 5220020, 4031034, 4000313, 2583000, 2583001, 2585000, 2585001, 2585002, 2585003, 4001895, 4001238, 4001760});
    public List<Integer> endItemz = Arrays.asList(new Integer[]{2587000, 2049302, 2049303, 2049304, 2049185, 2049186, 2049187, 2049188, 5220020, 4031034, 4000313, 2583001, 2583002, 2583007, 2585002, 2585003, 4310501, 2340000, 4310015, 4310020, 4310211, 4310066, 2587001, 2586000, 2586001, 2586002, 2585004, 2585005, 4001760});

    private static final int[] tier_1 = {2586002, 2585005, 2049305, 2049189, 2583000};
    private static final int[] tier_2 = {2586003, 2585006, 2049305, 2049175, 2583001};
    private static final int[] tier_3 = {2586004, 2585007, 2049305, 2049176, 2583007};
    private static final int[] tier_4 = {2586005, 2585008, 2049305, 2049177};

    static {
        for (int i : rechargeable) {
            rechargeableItems.add(i);
        }
    }

    /**
     * Creates a new instance of MapleShop
     */
    public MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
        this.shopId = id;
    }

    public MapleShop(int id, int npcId, int shopId) {
        this.id = id;
        this.npcId = npcId;
        this.shopId = shopId;
    }

    public void addItem(MapleShopItem item) {
        items.add(item);
    }

    public MapleShopItem getItem(int slot) {
        return items.get(slot);
    }

    public int getItemCag(int slot) {
        return items.get(slot).getCategory();
    }

    public List<MapleShopItem> getItems() {
        return items;
    }

    public void setShopId(int value) {
        shopId = value;
    }

    public int getShopId() {
        return shopId;
    }

    public void removeAllItems() {
        items.clear();
    }

    public void changeNXShop() {
        if (!items.isEmpty()) {
            removeAllItems();
            MapleItemInformationProvider.getInstance().NXPool();
        }

        for (int i = 0; i < 50; i++) {
            addItem(new MapleShopItem((short) 1, MapleItemInformationProvider.getInstance().getNXPool().get(i), 0, 4310501, Randomizer.random(1, 9), (byte) 0, 1, 0, 0, 1));
        }
        for (int i = 50; i < 100; i++) {
            addItem(new MapleShopItem((short) 1, MapleItemInformationProvider.getInstance().getNXPool().get(i), 0, 4310501, Randomizer.random(1, 9), (byte) 0, 7, 0, 0, 1));
        }
        for (int i = 100; i < 150; i++) {
            addItem(new MapleShopItem((short) 1, MapleItemInformationProvider.getInstance().getNXPool().get(i), 0, 4310501, Randomizer.random(1, 9), (byte) 0, 12, 0, 0, 1));
        }
        for (int i = 150; i < 200; i++) {
            addItem(new MapleShopItem((short) 1, MapleItemInformationProvider.getInstance().getNXPool().get(i), 0, 4310501, Randomizer.random(1, 9), (byte) 0, 23, 0, 0, 1));
        }
        for (int i = 200; i < 250; i++) {
            addItem(new MapleShopItem((short) 1, MapleItemInformationProvider.getInstance().getNXPool().get(i), 0, 4310501, Randomizer.random(1, 9), (byte) 0, 32, 0, 0, 1));
        }
    }

    public void changeQuestShop() {
        if (!items.isEmpty()) {
            removeAllItems();
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Integer> questItem = new ArrayList<>(MapleKQuests.getInstance().getRandomQuestPool());
        Collections.shuffle(questItem);

        for (int i = 0; i < 15; i++) {
            int randomCoin = questItem.get(i);
            if (i >= 0 && i <= 4) {
                addItem(new MapleShopItem((short) ii.getSlotMax(randomCoin), randomCoin, 0, 4310066, Randomizer.random(1, 2), (byte) 0, 1, 0, 0, 1));
            }
            if (i >= 5 && i <= 9) {
                addItem(new MapleShopItem((short) ii.getSlotMax(randomCoin), randomCoin, 0, 4310066, Randomizer.random(2, 4), (byte) 0, 7, 0, 0, 1));
            }
            if (i >= 10 && i <= 15) {
                addItem(new MapleShopItem((short) ii.getSlotMax(randomCoin), randomCoin, 0, 4310066, Randomizer.random(4, 8), (byte) 0, 12, 0, 0, 1));
            }
        }
    }

    public int getDay() {
        return day;
    }

    public void changeRandomShop() {
        if (!items.isEmpty()) {
            removeAllItems();
        }
        day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        Collections.shuffle(itemz);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (int i = 0; i < 15; i++) {
            int randomCoin = Randomizer.nextInt(coin.length - 1);
            if (i >= 0 && i <= 4) {
                addItem(new MapleShopItem((short) ii.getSlotMax(itemz.get(i)), itemz.get(i), 0, coin[randomCoin], base[randomCoin] * Randomizer.random(5, 25), (byte) 0, 7, 0, 0, 1));
            }
        }
    }

    public void changeRandomDPShop() {
        try {
            if (!items.isEmpty()) {
                removeAllItems();
            }
            day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            Collections.shuffle(endItemz);
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            for (int i = 0; i < 5; i++) {
                addItem(new MapleShopItem((short) ii.getSlotMax(endItemz.get(i)), endItemz.get(i), 0, 4310502, Randomizer.random(2, 9), (byte) 0, 7, 0, 0, 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendShop(MapleClient c) {
        if (c.getPlayer().getShop() != null || c.getPlayer().isStorageOpened()) {
            return;
        }
        if (c.getCMS() != null) {
            c.getCMS().dispose();
        }
        c.getPlayer().open(true);
        c.getPlayer().setShop(this);
        c.setChat(true);
        c.announce(NPCPacket.getNPCShop(getNpcId(), this, c));
    }

    public void sendShop(MapleClient c, int customNpc) {
        if (c.getPlayer().getShop() != null || c.getPlayer().isStorageOpened()) {
            return;
        }
        if (c.getCMS() != null) {
            c.getCMS().dispose();
        }
        c.getPlayer().setShop(this);
        c.getPlayer().open(true);
        c.setChat(true);
        c.announce(NPCPacket.getNPCShop(customNpc, this, c));
    }

    private MapleShopItem findBySlot(short slot) {
        return items.get(slot);
    }

    public void buy(MapleClient c, short slot, int itemId, short quantity) {
        if (c == null || c.getPlayer() == null || c.getPlayer().isChangingMaps()) {
            return;
        }
        //shop category: 1 = equip, 2 = use, 3 = setup, 4 = etc, 5 = recipe, 6 = scroll, 7 = special, 8 = 7th anniversary, 9 = button, 10 = invitation ticket, 11 = materials, 12 = korean word, 0 = no tab 
        MapleShopItem item = findBySlot(slot);
        if (quantity <= 0) {
            System.out.println(c.getPlayer().getName() + " tired to buy illegal items!");
            return;
        }
        if (itemId / 10000 == 190 && !GameConstants.isMountItemAvailable(itemId, c.getPlayer().getJob())) {
            c.getPlayer().dropMessage(1, "You may not buy this item.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int x = 0, index = -1;
        for (Item i : c.getPlayer().getRebuy()) {
            if (i.getItemId() == itemId) {
                index = x;
                break;
            }
            x++;
        }
        if (index >= 0) {
            final Item i = c.getPlayer().getRebuy().get(index);
            final int price = GameConstants.isRechargable(itemId) ? item.getPrice() : (item.getPrice() * quantity);
            //final int price = (int) Math.max(Math.ceil(ii.getPrice(itemId) * (GameConstants.isRechargable(itemId) ? 1 : i.getQuantity())), 0);
            if (price >= 0) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, i.getQuantity(), i.getOwner())) {
                    if (c.getPlayer().getMeso() >= price) {
                        c.getPlayer().gainMeso(-price, false);
                        MapleInventoryManipulator.addbyItem(c, i);
                        c.getPlayer().getRebuy().remove(index);
                        c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, x));
                    } else {
                        if (c.getPlayer().getBank() >= price) {
                            c.getPlayer().updateBank(-price);
                            MapleInventoryManipulator.addbyItem(c, i);
                            c.getPlayer().getRebuy().remove(index);
                            c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, x));
                        } else {
                            c.getPlayer().dropMessage(1, "You current do not have enough mesos to buy this item.");
                            c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your inventory is full.");
                    c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
                }
            } else {
                c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
            }
            return;
        }
        if (item != null && item.getPrice() > 0 && item.getReqItem() == 0) {
            if (item.getRank() >= 0) {
                boolean passed = true;
                int y = 0;
                for (Pair<Integer, String> i : getRanks()) {
                    if (c.getPlayer().haveItem(i.left, 1, true, true) && item.getRank() >= y) {
                        passed = true;
                        break;
                    }
                    y++;
                }
                if (!passed) {
                    c.getPlayer().dropMessage(1, "You need a higher rank.");
                    c.announce(CWvsContext.enableActions());
                    return;
                }
            }
            final long price = GameConstants.isRechargable(itemId) ? (long) item.getPrice() : ((long) item.getPrice() * quantity);
            if (price >= 0) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    boolean canBuy = false;
                    if (c.getPlayer().getMeso() >= price) {
                        c.getPlayer().gainMeso(-price, true, true);
                        canBuy = true;
                    } else {
                        if (c.getPlayer().getBank() >= price) {
                            c.getPlayer().updateBank(-price);
                            canBuy = true;
                        }
                    }
                    if (canBuy) {
                        if (GameConstants.isPet(itemId)) {
                            try {
                                MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, "Bought from shop " + id + ", " + npcId + " on " + FileoutputUtil.CurrentReadable_Date());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (GameConstants.isRechargable(itemId)) {
                                quantity = ii.getSlotMax(item.getItemId());
                            }
                            c.getPlayer().gainItem(itemId, quantity * item.getAmount());
                            //MapleInventoryManipulator.addById(c, itemId, quantity, "Bought from shop " + id + ", " + npcId + ", Count: " + quantity + " on " + FileoutputUtil.CurrentReadable_Date());
                        }
                    } else {
                        c.getPlayer().dropMessage(1, "You do not have enough mesos in the bank to buy this item.");
                    }
                } else {
                    c.getPlayer().dropMessage(1, "Your Inventory is full");
                }
                c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
            }
        } else if (item != null) {
            if (quantity > 0 && item.getReqItemQ() > 0) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    long rItem = item.getReqItemQ();
                    long qItem = quantity;
                    long bPrice = rItem * qItem;
                    if (bPrice > 2000000000) {
                        c.getPlayer().dropMessage(1, "You cannot purchase this many items for this price!");
                    } else {
                        int vPrice = (int) bPrice;
                        if (c.getPlayer().haveStoreItem(item.getReqItem(), vPrice)) {
                            if (MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(item.getReqItem()), item.getReqItem(), vPrice, false, false)) {
                                if (GameConstants.isPet(itemId)) {
                                    MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, "Bought from shop " + id + ", " + npcId + " on " + FileoutputUtil.CurrentReadable_Date());
                                } else {
                                    if (GameConstants.isRechargable(itemId)) {
                                        quantity = ii.getSlotMax(item.getItemId());
                                    }
                                    c.getPlayer().gainItem(itemId, quantity * item.getAmount(), "Bought from shop " + id + ", " + npcId + ", Count: " + quantity + " on " + FileoutputUtil.CurrentReadable_Date());
                                    //MapleInventoryManipulator.addById(c, itemId, quantity, "Bought from shop " + id + ", " + npcId + ", Count: " + quantity + " on " + FileoutputUtil.CurrentReadable_Date());
                                }
                            } else {
                                c.getPlayer().dropMessage(1, "You current do not have enough ETC to buy this item!");
                            }
                        } else {
                            if (c.getPlayer().getOverflowAmount(item.getReqItem()) >= vPrice) {
                                if (c.getPlayer().removeOverflow(item.getReqItem(), vPrice, "From shop " + id + ", " + npcId + " - Count: " + quantity)) {
                                    c.getPlayer().gainItem(itemId, quantity * item.getAmount(), "Bought from shop " + id + ", " + npcId + ", Count: " + quantity + " on " + FileoutputUtil.CurrentReadable_Date());
                                    //MapleInventoryManipulator.addById(c, itemId, quantity, "Bought from shop " + id + ", " + npcId + ", Count: " + quantity + " on " + FileoutputUtil.CurrentReadable_Date());
                                } else {
                                    c.getPlayer().dropMessage(1, "You currently do not have enough ETC to buy this item!");
                                }
                            } else {
                                c.getPlayer().dropMessage(1, "You currently do not have enough ETC to buy this item!");
                            }
                        }
                    }

                } else {
                    c.getPlayer().dropMessage(1, "Your Inventory is full");
                }
                c.announce(NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
            }
        }
    }

    public void sell(MapleClient c, MapleInventoryType type, byte slot, short quantity) {
        if (c == null || c.getPlayer() == null || c.getPlayer().isChangingMaps()) {
            return;
        }
        if (quantity == 0xFFFF || quantity == 0) {
            quantity = 1;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item == null) {
            return;
        }
        if (!c.getPlayer().haveItem(type, slot, quantity)) {
            System.out.println("Possible dupe attempt from selling in shops: Account: " + c.getAccountName() + " Char: " + c.getPlayer().getName());
            return;
        }

        if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
            quantity = item.getQuantity();
        }
        if (quantity < 0) {
            return;
        }
        short iQuant = item.getQuantity();
        if (iQuant == 0xFFFF) {
            iQuant = 1;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.cantSell(item.getItemId()) || GameConstants.isPet(item.getItemId())) {
            return;
        }
        if (quantity <= iQuant && iQuant > 0) {
            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            double price = ii.getPrice(item.getItemId());
            int recvMesos = (int) Math.max(Math.ceil(quantity), 0);
            if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
                recvMesos = 1;
            }
            if (GameConstants.isEquipment(item.getItemId())) {
                recvMesos = ii.getReqLevel(item.getItemId());
                if (recvMesos == 0) {
                    recvMesos = 1;
                }
            }
            if (price != -1.0 && recvMesos > 0) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            c.announce(NPCPacket.confirmShopTransaction((byte) 0x4, this, c, -1));
        }
    }

    public void recharge(final MapleClient c, final byte slot) {
        final Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

        if (item == null || (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId()))) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slotMax = ii.getSlotMax(item.getItemId());
        final int skill = GameConstants.getMasterySkill(c.getPlayer().getJob());

        if (skill != 0) {
            slotMax += c.getPlayer().getTotalSkillLevel(SkillFactory.getSkill(skill)) * 10;
        }
        if (item.getQuantity() < slotMax) {
            final int price = (int) Math.round(ii.getPrice(item.getItemId()) * (slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                c.announce(InventoryPacket.updateInventorySlot(MapleInventoryType.USE, (Item) item, false));
                c.getPlayer().gainMeso(-price, false, false);
                c.announce(NPCPacket.confirmShopTransaction((byte) 0x8, this, c, -1));
            }
        }
    }

    protected MapleShopItem findById(int itemId) {
        for (MapleShopItem item : items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public static MapleShop createFromDB(int id, boolean isShopId) {
        MapleShop ret = null;
        int shopId;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shopId = rs.getInt("shopid");
                ret = new MapleShop(shopId, rs.getInt("npcid"));
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                return null;
            }
            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            List<Integer> recharges = new ArrayList<Integer>(rechargeableItems);
            while (rs.next()) {
                if (!ii.itemExists(rs.getInt("itemid"))) {
                    continue;
                }
                short slotmax = rs.getShort("buyable") > 0 ? rs.getShort("buyable") : ii.getSlotMax(rs.getInt("itemid"));
                if (GameConstants.isThrowingStar(rs.getInt("itemid")) || GameConstants.isBullet(rs.getInt("itemid"))) {
                    MapleShopItem starItem = new MapleShopItem(slotmax, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq"), rs.getByte("rank"), rs.getInt("category"), rs.getInt("minLevel"), rs.getInt("expiration"), rs.getInt("amount"));
                    ret.addItem(starItem);
                    if (rechargeableItems.contains(starItem.getItemId())) {
                        recharges.remove(Integer.valueOf(starItem.getItemId()));
                    }
                } else {
                    ret.addItem(new MapleShopItem(slotmax, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq"), rs.getByte("rank"), rs.getInt("category"), rs.getInt("minLevel"), rs.getInt("expiration"), rs.getInt("amount")));
                }
            }
            for (Integer recharge : recharges) {
                ret.addItem(new MapleShopItem((short) 1, recharge.intValue(), 0, 0, 0, (byte) 0, 0, 0, 0, 1));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM shopranks WHERE shopid = ? ORDER BY rank ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!ii.itemExists(rs.getInt("itemid"))) {
                    continue;
                }
                ret.ranks.add(new Pair<Integer, String>(rs.getInt("itemid"), rs.getString("name")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Could not load shop");
            e.printStackTrace();
        }
        return ret;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getId() {
        return id;
    }

    public List<Pair<Integer, String>> getRanks() {
        return ranks;
    }

    public void clearShop() {
        items.clear();
    }
}
