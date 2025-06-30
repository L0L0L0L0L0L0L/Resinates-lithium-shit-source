package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import constants.GameConstants;
import client.inventory.ItemLoader;
import client.inventory.Item;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import database.DatabaseConnection;
import database.DatabaseException;
import java.util.EnumMap;
import java.util.HashMap;
import scripting.NPCScriptManager;
import tools.Pair;
import tools.packet.CField.NPCPacket;

public class MapleStorage implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private int id;
    private int accountId;
    private List<Item> items;
    private int meso;
    private int lastNPC = 0;
    private byte slots;
    private boolean changed = false;
    //private Map<MapleInventoryType, List<Item>> typeItems = new EnumMap<MapleInventoryType, List<Item>>(MapleInventoryType.class);
    private Map<MapleInventoryType, List<Item>> typeItems = new HashMap<>();
    private final boolean[] used = new boolean[6];

    private MapleStorage(int id, byte slots, int meso, int accountId) {
        this.id = id;
        this.slots = slots;
        this.items = new LinkedList<Item>();
        this.meso = meso;
        this.accountId = accountId;
    }

    private static MapleStorage create(int id) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO storages (accountid, slots, meso) VALUES (?, 120, 0)");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loadOrCreateFromDB(id);
    }

    public static MapleStorage loadOrCreateFromDB(int id) {
        MapleStorage ret = null;
        int storeId;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT storageid, slots, meso FROM storages WHERE accountid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return create(id);
            } else {
                storeId = rs.getInt("storageid");
                ret = new MapleStorage(storeId, rs.getByte("slots"), rs.getInt("meso"), id);
                rs.close();
                ps.close();
                for (Pair<Item, MapleInventoryType> mit : ItemLoader.STORAGE.loadItemsStorage(ret.id, false)) {
                    ret.items.add(mit.getLeft());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public void saveToDB(boolean mesos) {
        if (mesos) {
            try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")) {
                ps.setInt(1, slots);
                ps.setInt(2, meso);
                ps.setInt(3, id);
                ps.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("Error saving storage" + ex);
            }
        }
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

            List<Item> list = getItems();
            for (Item item : list) {
                itemsWithType.add(new Pair<>(item, GameConstants.getInventoryType(item.getItemId())));
            }

            ItemLoader.STORAGE.saveItems(itemsWithType, id, con);
        } catch (SQLException ex) {
            System.err.println("Error saving storage" + ex);
        }
    }

    public void saveItemToDB(Item item) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ItemLoader.STORAGE.saveItem(item, id, con);
        } catch (SQLException ex) {
            System.err.println("Error saving storage" + ex);
        }
    }

    public void saveMesos() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")) {
            ps.setInt(1, slots);
            ps.setInt(2, meso);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error saving storage" + ex);
        }
    }

    public Item getItem(byte slot) {
        return items.get(slot);
    }

    public Item takeOut(byte slot) {
        Item ret = items.get(slot);
        items.remove(slot);
        MapleInventoryType type = ret.getInventoryType();
        typeItems.put(type, new ArrayList<>(filterItems(type)));
        return ret;
    }

    public void store(Item item, int quantity) {
        MapleInventoryType type = item.getInventoryType();
        if (type == MapleInventoryType.EQUIP && item.getQuantity() > 1) {
            System.out.println("possible Equip storage dupe?");
            return;
        }
        if (quantity > item.getQuantity()) {
            System.out.println("possible ETC storgae dupe?");
            return;
        }
        items.add(item);
        typeItems.put(type, new ArrayList<>(filterItems(type)));
    }

    public void arrange() { //i believe gms does by itemID
        /*
         Collections.sort(items, new Comparator<Item>() {

         @Override
         public int compare(Item o1, Item o2) {
         if (o1.getItemId() < o2.getItemId()) {
         return -1;
         } else if (o1.getItemId() == o2.getItemId()) {
         return 0;
         } else {
         return 1;
         }
         }
         });
         for (MapleInventoryType type : MapleInventoryType.values()) {
         typeItems.put(type, items);
         }
         */
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    private List<Item> filterItems(MapleInventoryType type) {
        List<Item> storageItems = getItems();
        List<Item> ret = new LinkedList<>();

        for (Item item : storageItems) {
            if (item.getInventoryType() == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) {
        // MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte ret = 0;
        List<Item> storageItems = getItems();
        if (!getItems().isEmpty() && slot < getItems().size()) {
            for (Item item : storageItems) {
                if (item == typeItems.get(type).get(slot)) {
                    return ret;
                }
                ret++;
            }
        }
        return -1;
    }

    public void sendStorage(MapleClient c, int npcId) {
        if (c.getPlayer().isStorageOpened()) {
            return;
        }
        //c.getPlayer().dropMessage(1, "Storage is currently disabled, please you Free Markey");
        //NPCScriptManager.getInstance().dispose(c);
        
        // sort by inventorytype to avoid confusion
        lastNPC = npcId;

        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if (o1.getInventoryType().getType() < o2.getInventoryType().getType()) {
                    return -1;
                } else if (o1.getInventoryType() == o2.getInventoryType()) {
                    return 0;
                }
                return 1;
            }
        });

        List<Item> storageItems = getItems();
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, new ArrayList<>(storageItems));
        }
        
        c.getPlayer().storageOpen(true);
        c.announce(NPCPacket.getStorage(npcId, slots, storageItems, meso));
        
    }

    public void update(MapleClient c) {
        c.announce(NPCPacket.arrangeStorage(slots, getItems()));
    }

    public void sendError(MapleClient c, int type) {
        c.announce(NPCPacket.getStorageError(type));
    }

    public void sendStored(MapleClient c, MapleInventoryType type) {
        c.announce(NPCPacket.storeStorage(slots, type, new ArrayList<>(filterItems(type))));
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) {
        c.announce(NPCPacket.takeOutStorage(slots, type, new ArrayList<>(filterItems(type))));
    }

    public int getMeso() {
        return meso;
    }

    public Item findById(int itemId) {
        for (Item item : items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public void setMeso(int meso) {
        if (meso < 0) {
            return;
        }
        changed = true;
        this.meso = meso;
    }

    public void sendMeso(MapleClient c) {
        c.announce(NPCPacket.mesoStorage(slots, meso));
    }

    public boolean isFull() {
        return items.size() >= slots;
    }

    public int getSlots() {
        return slots;
    }

    public void increaseSlots(byte gain) {
        changed = true;
        this.slots += gain;
    }

    public void setSlots(byte set) {
        changed = true;
        this.slots = set;
    }

    public void close() {
        typeItems.clear();
    }
}
