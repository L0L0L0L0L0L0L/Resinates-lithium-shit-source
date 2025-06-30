package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import tools.packet.CField;

public class MapleExtractor extends MapleMapObject {

    public int owner, timeLeft, itemId, fee;
    public long startTime;
    public String ownerName;

    public MapleExtractor(MapleCharacter owner, int itemId, int fee, int timeLeft) {
        super();
        this.owner = owner.getId();
        this.itemId = itemId;
        this.fee = fee;
        this.ownerName = owner.getName();
        this.startTime = System.currentTimeMillis();
        this.timeLeft = timeLeft;
        setPosition(owner.getPosition());
    }

    public int getTimeLeft() { //tbh idk if this is even right, lol
        return timeLeft;
    }

    public void sendSpawnData(MapleClient client) {
        client.announce(CField.makeExtractor(owner, ownerName, getTruePosition(), getTimeLeft(), itemId, fee));
    }

    public void sendDestroyData(MapleClient client) {
        client.announce(CField.removeExtractor(this.owner));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.EXTRACTOR;
    }
}
