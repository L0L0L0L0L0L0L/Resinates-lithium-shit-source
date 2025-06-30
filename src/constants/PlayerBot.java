/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package constants;

import client.MapleCharacter;
import client.MapleClient;
import handling.channel.ChannelServer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Evil0
 */
public class PlayerBot {

    private Map<Integer, List<MapleCharacter>> bots = new ConcurrentHashMap<>();

    public MapleCharacter getBot(MapleClient c, int cid) {
        if (bots.containsKey(c.getAccID())) {
            var listBots = bots.get(c.getAccID());
            for (MapleCharacter bot : listBots) {
                if (bot.getId() == cid) {
                    return bot;
                }
            }
        }
        return null;
    }

    public boolean storeBot(MapleClient c, MapleCharacter bot) {
        var clientBots = bots.get(c.getAccID());
        if (!clientBots.contains(bot)) {
            clientBots.add(bot);
            return true;
        }
        return false;
    }

    public boolean loadBot(MapleClient c, int cid) {
        if (!bots.containsKey(c.getAccID())) {
            var clientBots = bots.get(c.getAccID());
            MapleCharacter bot = MapleCharacter.loadCharFromDB(cid, c, true);
            if (bot != null && !clientBots.contains(bot)) {
                c.processCharBot(bot, c.getChannelServer(), true);
                clientBots.add(bot);
                return true;
            }
        }
        return false;
    }

    public boolean unloadBot(MapleClient c, MapleCharacter bot) {
        var clientBots = bots.get(c.getAccID());
        if (clientBots.contains(bot)) {
            c.unloadChar(true, false, false);
            clientBots.remove(bot);
        }
        return false;
    }

    public List<MapleCharacter> getBots(int clientId) {
        if (bots.containsKey(clientId)) {
            return bots.get(clientId);
        }
        return null;
    }
}
