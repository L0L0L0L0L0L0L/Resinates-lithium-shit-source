/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; w"ithout even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import javax.script.Invocable;
import javax.script.ScriptException;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.MapleAchievements;
import server.maps.MapleReactor;
import server.MapleSquad;
import server.Randomizer;
import server.Timer.EventTimer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapManager;
import server.maps.MapleReactorFactory;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class EventManager {

    private static int[] eventChannel = new int[2];
    private Invocable iv;
    private int channel;
    //private Map<String, EventInstanceManager> instances = new WeakHashMap<String, EventInstanceManager>();
    private Map<String, EventInstanceManager> instances = new ConcurrentHashMap<String, EventInstanceManager>();
    private List<EventInstanceManager> readyInstances = new LinkedList<>();
    private MonitoredReentrantLock lobbyLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EM_LOBBY);
    private MonitoredReentrantLock queueLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EM_QUEUE);
    private MonitoredReentrantLock startLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EM_START);
    private Properties props = new Properties();
    private String name;

    public EventManager(ChannelServer cserv, Invocable iv, String name) {
        this.iv = iv;
        this.channel = cserv.getChannel();
        this.name = name;
    }

    public void cancel() {
        try {
            iv.invokeFunction("cancelSchedule", (Object) null);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
        }
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay) {
        return EventTimer.getInstance().schedule(() -> {
            try {
                iv.invokeFunction(methodName, (Object) null);
            } catch (Exception ex) {
                System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                ex.printStackTrace();
            }
        }, delay);
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
        return EventTimer.getInstance().schedule(() -> {
            try {
                iv.invokeFunction(methodName, eim);
            } catch (Exception ex) {
                System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                ex.printStackTrace();
            }
        }, delay);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return EventTimer.getInstance().scheduleAtTimestamp(() -> {
            try {
                iv.invokeFunction(methodName, (Object) null);
            } catch (ScriptException ex) {
                System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
            } catch (NoSuchMethodException ex) {
                System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
            }
        }, timestamp);
    }

    public int getChannel() {
        return channel;
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(channel);
    }

    public EventInstanceManager getInstance(String name) {
        return instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public EventInstanceManager newInstance(MapleCharacter chr, String name, boolean scale) {
        EventInstanceManager ret = null;

        try {
            name += ("_" + Randomizer.nextInt(1000000000));
            ret = new EventInstanceManager(chr, this, name, scale);
            synchronized (instances) {
                if (instances.containsKey(name)) {
                    System.out.println("Error with Event: " + name);
                } else {
                    instances.put(name, ret);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void disposeInstance(String name) {
        synchronized (instances) {
            instances.remove(name);
        }
        if (getProperty("state") != null && instances.isEmpty()) {
            setProperty("state", "0");
        }
        if (getProperty("leader") != null && instances.isEmpty() && getProperty("leader").equals("false")) {
            setProperty("leader", "true");
        }
        if (this.name.equals("CWKPQ")) { //hard code it because i said so
            final MapleSquad squad = ChannelServer.getInstance(channel).getMapleSquad("CWKPQ");//so fkin hacky
            if (squad != null) {
                squad.clear();
                squad.copy();
            }
        }
    }

    public Invocable getIv() {
        return iv;
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public final Properties getProperties() {
        return props;
    }

    public String getName() {
        return name;
    }

    public void startInstance() {
        try {
            iv.invokeFunction("setup", (Object) null);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    public void startInstance_Solo(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
            eim.registerPlayer(chr);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    public void startInstance(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
            eim.registerCarnivalParty(chr, chr.getMap(), (byte) 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    public void startInstance_Party(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
            eim.registerParty(chr.getParty(), chr.getMap());
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup:\n" + ex);
        }
    }

    //GPQ
    public void startInstance(MapleCharacter character, String leader) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerPlayer(character);
            eim.setProperty("leader", leader);
            eim.setProperty("guildid", String.valueOf(character.getGuildId()));
            setProperty("guildid", String.valueOf(character.getGuildId()));
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-Guild:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-Guild:\n" + ex);
        }
    }

    public void startInstance_CharID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", character.getId()));
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-CharID:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-CharID:\n" + ex);
        }
    }

    public void startInstance_CharMapID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", character.getId(), character.getMapId()));
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-CharID:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-CharID:\n" + ex);
        }
    }

    public void startInstance(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
        }
    }

    //PQ method: starts a PQ
    public void startInstance(MapleParty party, MapleMap map) {
        startInstance(party, map, 255);
    }

    public void startInstance(MapleParty party, MapleMap map, int maxLevel) {
        try {
            int averageLevel = 0, size = 0;
            for (MaplePartyCharacter mpc : party.getMembers()) {
                if (mpc.isOnline() && mpc.getMapid() == map.getId() && mpc.getChannel() == map.getChannel()) {
                    averageLevel += mpc.getLevel();
                    size++;
                }
            }
            if (size <= 0) {
                return;
            }
            averageLevel /= size;
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", Math.min(maxLevel, averageLevel), party.getId()));
            eim.registerParty(party, map);
        } catch (ScriptException ex) {
            System.out.println("Event name : " + name + ", method Name : setup-partyid:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-partyid:\n" + ex);
        } catch (Exception ex) {
            //ignore
            startInstance_NoID(party, map, ex);
        }
    }

    public void startInstance_NoID(MapleParty party, MapleMap map) {
        startInstance_NoID(party, map, null);
    }

    public void startInstance_NoID(MapleParty party, MapleMap map, final Exception old) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerParty(party, map);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-party:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-party:\n" + ex + "\n" + (old == null ? "no old exception" : old));
        }
    }

    //non-PQ method for starting instance
    public void startInstance(EventInstanceManager eim, String leader) {
        try {
            iv.invokeFunction("setup", eim);
            eim.setProperty("leader", leader);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-leader:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-leader:\n" + ex);
        }
    }

    public void startInstance(MapleSquad squad, MapleMap map) {
        startInstance(squad, map, -1);
    }

    public void startInstance(MapleSquad squad, MapleMap map, int questID) {
        if (squad.getStatus() == 0) {
            return; //we dont like cleared squads
        }
        if (!squad.getLeader().isGM()) {
            if (squad.getMembers().size() < squad.getType().i) { //less than 3
                squad.getLeader().dropMessage(5, "The squad has less than " + squad.getType().i + " people participating.");
                return;
            }
            if (name.equals("CWKPQ") && squad.getJobs().size() < 5) {
                squad.getLeader().dropMessage(5, "The squad requires members from every type of job.");
                return;
            }
        }
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", squad.getLeaderName()));
            eim.registerSquad(squad, map, questID);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : setup-squad:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-squad:\n" + ex);
        }
    }

    public void warpAllPlayer(int from, int to) {
        final MapleMap tomap = getMapFactory().getMap(to);
        final MapleMap frommap = getMapFactory().getMap(from);
        List<MapleCharacter> list = frommap.getCharacters();
        if (tomap != null && frommap != null && list != null && frommap.getCharactersSize() > 0) {
            for (MapleMapObject mmo : list) {
                ((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
            }
        }
    }

    public MapleMapManager getMapFactory() {
        return getChannelServer().getMapFactory();
    }

    public OverrideMonsterStats newMonsterStats() {
        return new OverrideMonsterStats();
    }

    public List<MapleCharacter> newCharList() {
        return new ArrayList<MapleCharacter>();
    }

    public MapleReactor getReactor(final int id) {
        return new MapleReactor(MapleReactorFactory.getReactor(id), id);
    }

    public void broadcastShip(final int mapid, final int effect, final int mode) {
        getMapFactory().getMap(mapid).broadcastMessage(CField.boatPacket(effect, mode));
    }

    public void broadcastYellowMsg(final String msg) {
        getChannelServer().broadcastPacket(CWvsContext.yellowChat(msg));
    }

    public void broadcastServerMsg(final int type, final String msg, final boolean weather) {
        if (!weather) {
            getChannelServer().broadcastPacket(CWvsContext.serverNotice(type, msg));
        } else {
            for (MapleMap load : getMapFactory().getAllMaps()) {
                if (load.getCharactersSize() > 0) {
                    load.startMapEffect(msg, type);
                }
            }
        }
    }

    public boolean scheduleRandomEvent() {
        boolean omg = false;
        for (int i = 0; i < eventChannel.length; i++) {
            omg |= scheduleRandomEventInChannel(eventChannel[i]);
        }
        return omg;
    }

    public boolean scheduleRandomEventInChannel(int chz) {
        final ChannelServer cs = ChannelServer.getInstance(chz);
        if (cs == null || cs.getEvent() > -1) {
            return false;
        }
        MapleEventType t = null;
        while (t == null) {
            for (MapleEventType x : MapleEventType.values()) {
                if (Randomizer.nextInt(MapleEventType.values().length) == 0 && x != MapleEventType.OxQuiz) {
                    t = x;
                    break;
                }
            }
        }
        final String msg = MapleEvent.scheduleEvent(t, cs);
        if (msg.length() > 0) {
            broadcastYellowMsg(msg);
            return false;
        }
        EventTimer.getInstance().schedule(() -> {
            if (cs.getEvent() >= 0) {
                MapleEvent.setEvent(cs, true);
            }
        }, 180000);
        return true;
    }

    public void setWorldEvent() {
        for (int i = 0; i < eventChannel.length; i++) {
            eventChannel[i] = Randomizer.nextInt(ChannelServer.getAllInstances().size() - 4) + 2 + i; //2-13
        }
    }

    public boolean startPlayerInstance(MapleCharacter chr) {
        if (chr.getEventInstance() != null) {
            //System.out.println("Event name : " + name + ", Player Name: " + chr.getName() + " Illegal instance.");
            //chr.getEventInstance().dispose();
            return false;
        }
        try {
            EventInstanceManager eim = createInstance("setup", chr);
            eim.register(chr);
            eim.startEvent();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
        }
        return false;
    }

    /*

    public boolean startPlayerInstance(MapleCharacter chr, long level) {
        if (chr.getEventInstance() != null) {
            //System.out.println("Event name : " + name + ", Player Name: " + chr.getName() + " Illegal instance.");
            //chr.getEventInstance().dispose();
            return false;
        }
        try {
            EventInstanceManager eim = createInstance("setup", chr, level);
            eim.register(chr);
            eim.startEvent();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
        }
        return false;
    }

    public boolean startPlayerInstance(MapleCharacter chr, long level, long scale) {
        if (chr.getEventInstance() != null) {
            //System.out.println("Event name : " + name + ", Player Name: " + chr.getName() + " Illegal instance.");
            //chr.getEventInstance().dispose();
            return false;
        }
        try {
            EventInstanceManager eim = createInstance("setup", chr, level, scale);
            eim.register(chr);
            eim.startEvent();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
        }
        return false;
    }
     */
    private EventInstanceManager createInstance(String name, Object... args) throws ScriptException, NoSuchMethodException {
        return (EventInstanceManager) iv.invokeFunction(name, args);
    }

    public boolean startPlayerInstance(Object... args) {
        if (args.length <= 0) {
            System.out.println("Error with EM with missing parms in startPlayerInstance()");
            return false;
        }
        MapleCharacter chr = (MapleCharacter) args[0];
        if (chr.getEventInstance() != null) {
            //System.out.println("Event name : " + name + ", Player Name: " + chr.getName() + " Illegal instance.");
            //chr.getEventInstance().dispose();
            return false;
        }
        try {
            EventInstanceManager eim = createInstance("setup", args);
            eim.register(chr);
            eim.startEvent();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Event name : " + name + ", method Name : setup-character:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " + name + ", method Name : setup-character:\n" + ex);
        }
        return false;
    }

    public boolean getEligibleParty(MapleCharacter leader) {
        try {
            int total = 0;
            for (MapleCharacter player : leader.getGroupMembers()) {
                if (player.isAlive()) {
                    if (player.getEventInstance() == null) {
                        if (!player.isMapChange()) {
                            if (!player.battle) {
                                if (player.getMapId() == leader.getMapId()) {
                                    total += 1;
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");
                        return false;
                    }
                } else {
                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                    return false;
                }
            }
            if (leader.getRaid() != null) {
                return leader.getRaid().getMembers().size() == total;
            } else {
                return leader.getParty().getMembers().size() == total;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligibleParty(MapleCharacter leader, int lvlmin) {
        try {
            int total = 0;
            for (MapleCharacter player : leader.getGroupMembers()) {
                if (player.isAlive()) {
                    if (player.getEventInstance() == null) {
                        if (player.getTotalLevel() >= lvlmin) {
                            if (!player.isMapChange()) {
                                if (!player.battle) {
                                    if (player.getMapId() == leader.getMapId()) {
                                        total += 1;
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");
                        return false;
                    }
                } else {
                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                    return false;
                }
            }
            if (leader.getRaid() != null) {
                return leader.getRaid().getMembers().size() == total;
            } else {
                return leader.getParty().getMembers().size() == total;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligiblePartyLevel(MapleCharacter leader, int lvlmin, int lvlmax) {
        try {
            int total = 0;
            for (MapleCharacter player : leader.getGroupMembers()) {
                if (player.isAlive()) {
                    if (player.getEventInstance() == null) {
                        if (player.getTotalLevel() >= lvlmin) {
                            if (player.getTotalLevel() <= lvlmax) {
                                if (!player.battle) {
                                    if (!player.isMapChange()) {
                                        if (player.getMapId() == leader.getMapId()) {
                                            total += 1;
                                        } else {
                                            leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                                            return false;
                                        }
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet maximum level requirement of " + lvlmax + ".");
                                return false;
                            }

                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");
                        return false;
                    }
                } else {
                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                    return false;
                }
            }
            if (leader.getRaid() != null) {
                return leader.getRaid().getMembers().size() == total;
            } else {
                return leader.getParty().getMembers().size() == total;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligibleParty(MapleCharacter leader, int lvlmin, int size) {
        try {
            int total = 0;
            for (MapleCharacter player : leader.getGroupMembers()) {
                if (player.isAlive()) {
                    if (player.getEventInstance() == null) {
                        if (!player.isMapChange()) {
                            if (player.getTotalLevel() >= lvlmin) {
                                if (!player.battle) {
                                    if (player.getMapId() == leader.getMapId()) {
                                        total += 1;
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");
                        return false;
                    }
                } else {
                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                    return false;
                }
            }
            if (total < size) {
                leader.dropMessage(1, "Party does not contain enough players. Requires at least " + size + " members to enter");
                return false;
            } else {
                if (leader.getRaid() != null) {
                    return leader.getRaid().getMembers().size() == total;
                } else {
                    return leader.getParty().getMembers().size() == total;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligiblePartyAch(MapleCharacter leader, int lvlmin, int id) {
        try {
            int total = 0;
            if (leader.isGroup()) {
                for (MapleCharacter player : leader.getGroupMembers()) {
                    if (player.getMapId() == leader.getMapId()) {
                        if (player.getEventInstance() == null) {
                            if (player.isAlive()) {
                                if (!player.isMapChange()) {
                                    if (!player.battle) {
                                        if (player.getTotalLevel() >= lvlmin) {
                                            if (id == 0 || player.getAchievement(id)) {
                                                total += 1;
                                            } else {
                                                if (MapleAchievements.getInstance().getById(id) != null) {
                                                    leader.dropMessage(1, "Party Member: " + player.getName() + " has not comepleted " + MapleAchievements.getInstance().getById(id).getName() + ".");
                                                    return false;
                                                } else {
                                                    return true;
                                                }
                                            }
                                        } else {
                                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                                            return false;
                                        }
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");

                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                        return false;
                    }
                }

                return leader.getGroupMembers().size() == total;
            }
            leader.dropMessage(1, "You must be in a party to compete.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligiblePartyAch(MapleCharacter leader, int lvlmin, int id, int size) {
        try {
            int total = 0;
            if (leader.isGroup()) {
                for (MapleCharacter player : leader.getGroupMembers()) {
                    if (player.getMapId() == leader.getMapId()) {
                        if (player.getEventInstance() == null) {
                            if (player.isAlive()) {
                                if (!player.isMapChange()) {
                                    if (!player.battle) {
                                        if (player.getTotalLevel() >= lvlmin) {
                                            if (id == 0 || player.getAchievement(id)) {
                                                total += 1;
                                            } else {
                                                leader.dropMessage(1, "Party Member: " + player.getName() + " has not comepleted " + MapleAchievements.getInstance().getById(id).getName() + ".");
                                                return false;
                                            }
                                        } else {
                                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                                            return false;
                                        }
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");

                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                        return false;
                    }
                }
                if (total < size) {
                    leader.dropMessage(1, "Party does not contain enough players. Requires at least " + size + " members to enter");
                    return false;
                } else {
                    return leader.getGroupMembers().size() == total;
                }
            }
            leader.dropMessage(1, "You must be in a party to compete.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligiblePartyAch(MapleCharacter leader, int lvlmin, int id, int min, int max) {
        try {
            int total = 0;
            if (leader.isGroup()) {
                for (MapleCharacter player : leader.getGroupMembers()) {
                    if (player.getMapId() == leader.getMapId()) {
                        if (player.getEventInstance() == null) {
                            if (player.isAlive()) {
                                if (!player.isMapChange()) {
                                    if (!player.battle) {
                                        if (player.getTotalLevel() >= lvlmin) {
                                            if (id == 0 || player.getAchievement(id)) {
                                                total += 1;
                                            } else {
                                                if (MapleAchievements.getInstance().getById(id) != null) {
                                                    leader.dropMessage(1, "Party Member: " + player.getName() + " has not comepleted " + MapleAchievements.getInstance().getById(id).getName() + ".");
                                                    return false;
                                                } else {
                                                    return true;
                                                }
                                            }
                                        } else {
                                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                                            return false;
                                        }
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");

                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                        return false;
                    }
                }
                if (total < min) {
                    leader.dropMessage(1, "Party does not contain enough players. Requires at least " + min + " members to enter");
                    return false;
                }
                if (total > max) {
                    leader.dropMessage(1, "Party contains to many players. Maximum allowed is " + max + " members to enter");
                    return false;
                }
                return leader.getGroupMembers().size() == total;
            }
            leader.dropMessage(1, "You must be in a party to compete.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligiblePartyAchReborn(MapleCharacter leader, int lvlmin, int id, int min, int max, int rb) {
        try {
            int total = 0;
            if (leader.isGroup()) {
                for (MapleCharacter player : leader.getGroupMembers()) {
                    if (player.getMapId() == leader.getMapId()) {
                        if (player.getEventInstance() == null) {
                            if (player.isAlive()) {
                                if (!player.isMapChange()) {
                                    if (!player.battle) {
                                        if (player.getTotalLevel() >= lvlmin) {
                                            if (player.getReborns() >= rb) {
                                                if (id == 0 || player.getAchievement(id)) {
                                                    total += 1;
                                                } else {
                                                    if (MapleAchievements.getInstance().getById(id) != null) {
                                                        leader.dropMessage(1, "Party Member: " + player.getName() + " has not comepleted " + MapleAchievements.getInstance().getById(id).getName() + ".");
                                                        return false;
                                                    } else {
                                                        return true;
                                                    }
                                                }
                                            } else {
                                                leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum reborn requirement of " + rb + ".");
                                                return false;
                                            }
                                        } else {
                                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                                            return false;
                                        }
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");

                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                        return false;
                    }
                }
                if (total < min) {
                    leader.dropMessage(1, "Party does not contain enough players. Requires at least " + min + " members to enter");
                    return false;
                }
                if (total > max) {
                    leader.dropMessage(1, "Party contains to many players. Maximum allowed is " + max + " members to enter");
                    return false;
                }
                return leader.getGroupMembers().size() == total;
            }
            leader.dropMessage(1, "You must be in a party to compete.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getEligibleParty(MapleCharacter leader, int lvlmin, int min, int max) {
        try {
            int total = 0;
            if (leader.isGroup()) {
                for (MapleCharacter player : leader.getGroupMembers()) {
                    if (player.getMapId() == leader.getMapId()) {
                        if (player.getEventInstance() == null) {
                            if (player.isAlive()) {
                                if (!player.isMapChange()) {
                                    if (!player.battle) {
                                        if (player.getTotalLevel() >= lvlmin) {
                                            total += 1;
                                        } else {
                                            leader.dropMessage(1, "Party Member: " + player.getName() + " does not meet minimum level requirement of " + lvlmin + ".");
                                            return false;
                                        }
                                    } else {
                                        leader.dropMessage(1, "Party Member: " + player.getName() + " is currently in a pal battle.");
                                        return false;
                                    }
                                } else {
                                    leader.dropMessage(1, "Party Member: " + player.getName() + " is currently changing maps.");
                                    return false;
                                }
                            } else {
                                leader.dropMessage(1, "Party Member: " + player.getName() + " is currently dead.");
                                return false;
                            }
                        } else {
                            leader.dropMessage(1, "Party Member: " + player.getName() + " is currently inside an instance.");

                            return false;
                        }
                    } else {
                        leader.dropMessage(1, "Party Member: " + player.getName() + " cannot be found on your map.");
                        return false;
                    }
                }
                if (total < min) {
                    leader.dropMessage(1, "Party does not contain enough players. Requires at least " + min + " members to enter");
                    return false;
                }
                if (total > max) {
                    leader.dropMessage(1, "Party contains to many players. Maximum allowed is " + max + " members to enter");
                    return false;
                }
                return leader.getGroupMembers().size() == total;
            }
            leader.dropMessage(1, "You must be in a party to compete.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disposedInstance(final String name) {
        synchronized (instances) {
            instances.remove(name);
        }
    }

    public void print(String msg) {
        System.out.println(msg);
    }

    public int getRandom(int x, int y) {
        return Randomizer.random(x, y);
    }

    public void systemMsg(String msg) {
        System.out.println(msg);
    }

    public MapleMonster getMonster(int id) {
        return MapleLifeFactory.getMonster(id);
    }

    public MapleMonster getMonster(int id, int level) {
        return MapleLifeFactory.getMonster(id, level);
    }

    public MapleMonster getMonster(int id, int level, int scale) {
        return MapleLifeFactory.getMonster(id, level, scale);
    }

    public MapleMonster getMonsterNoDrops(int id) {
        return MapleLifeFactory.getMonsterNoDrops(id);
    }

    public MapleMonster getMonsterNoDrops(int id, int level) {
        return MapleLifeFactory.getMonsterNoDrops(id, level);
    }

    public MapleMonster getMonsterNoDrops(int id, int level, int scale) {
        return MapleLifeFactory.getMonsterNoDrops(id, level, scale);
    }

    public MapleMonster getMonsterNoDropsLink(int id) {
        return MapleLifeFactory.getMonsterNoDropsLink(id);
    }

    public MapleMonster getMonsterNoDropsLink(int id, int level) {
        return MapleLifeFactory.getMonsterNoDropsLink(id, level);
    }

    public MapleMonster getMonsterNoDropsLink(int id, int level, int scale) {
        return MapleLifeFactory.getMonsterNoDropsLink(id, level, scale);
    }

    public MapleMonster getMonsterNoLink(int id) {
        return MapleLifeFactory.getMonsterNoLink(id);
    }

    public MapleMonster getMonsterNoLink(int id, int level) {
        return MapleLifeFactory.getMonsterNoLink(id, level);
    }

    public MapleMonster getMonsterNoLink(int id, int level, int scale) {
        return MapleLifeFactory.getMonsterNoLink(id, level, scale);
    }

    public MapleMonster getMonsterNoLinkRank(int id, int scale) {
        return MapleLifeFactory.getMonsterNoLinkRank(id, scale);
    }

    public MapleMonster getMonsterNoAll(int id) {
        return MapleLifeFactory.getMonsterNoAll(id);
    }

    public MapleMonster getMonsterNoAll(int id, int level) {
        return MapleLifeFactory.getMonsterNoAll(id, level);
    }

    public MapleMonster getMonsterNoAll(int id, int level, int scale) {
        return MapleLifeFactory.getMonsterNoAll(id, level, scale);
    }

    public Point newPoint(int x, int y) {
        return new Point(x, y);
    }

    public Point newRandomPoint(int x1, int x2, int y1, int y2) {
        return new Point(Randomizer.random(x1, x2), Randomizer.random(y1, y2));
    }

    public void announce(int color, String msg) {
        for (MapleCharacter victim : getChannelServer().getPlayerStorage().getAllCharacters()) {
            victim.dropColorMessage(color, msg);
        }
    }
}
