package server;

public class MapleShopItem {

    private final short buyable;
    private final int itemId;
    private final int price;
    private final int reqItem;
    private final int reqItemQ;
    private final int category;
    private final int minLevel;
    private final int expiration;
    private final byte rank;
    private final int amount;

    public MapleShopItem(int itemId, int price, int amount, short buyable) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.reqItem = 0;
        this.reqItemQ = 0;
        this.rank = (byte) 0;
        this.category = 0;
        this.minLevel = 0;
        this.expiration = 0;
        this.amount = amount;
    }

    public MapleShopItem(short buyable, int itemId, int price, int reqItem, int reqItemQ, byte rank, int category, int minLevel, int expiration, int amount) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.reqItem = reqItem;
        this.reqItemQ = reqItemQ;
        this.rank = rank;
        this.category = category;
        this.minLevel = minLevel;
        this.expiration = expiration;
        this.amount = amount;
    }
    
    public short getAmount() {
        return (short) Randomizer.Max(amount, 30000);
    }

    public short getBuyable() {
        return buyable;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPrice() {
        return price;
    }

    public int getReqItem() {
        return reqItem;
    }

    public int getReqItemQ() {
        return reqItemQ;
    }

    public byte getRank() {
        return rank;
    }

    public int getCategory() {
        return category;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getExpiration() {
        return expiration;
    }
}
