
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
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scripting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import javax.script.ScriptException;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleTrait.MapleTraitType;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import handling.world.exped.PartySearch;
import java.awt.Point;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.MapleCarnivalParty;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleSquad;
import server.Park;
import server.Parks;
import server.Randomizer;
import static server.Randomizer.nextInt;
import static server.Randomizer.random;
import server.Timer;
import server.TimerManager;
import server.quest.MapleQuest;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapManager;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.Pair;
import tools.packet.CWvsContext.InfoPacket;

public final class EventInstanceManager {

    private Map<Integer, MapleCharacter> chars = new HashMap<>();
    private List<Integer> dced = new LinkedList<Integer>();
    private Map<MapleCharacter, Integer> killCount = new HashMap<>();
    private EventManager em;
    private String name;
    public MapleMap storedmap;
    private Properties props = new Properties();
    private long timeStarted = 0;
    private long eventTime = 0;
    private List<Integer> mapIds = new LinkedList<Integer>();
    private Map<Integer, Integer> monsterParkMaps = new HashMap<>();
    //public List<MapleMap> maps;
    //private final HashMap<Integer, MapleMap> maps = new HashMap<Integer, MapleMap>();
    private ScheduledFuture<?> eventTimer;
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = mutex.readLock(), wL = mutex.writeLock();
    private boolean disposed = false;
    public MapleCharacter Owner = null;
    public MapleMapManager mapManager;
    private final ReentrantReadWriteLock lock = new MonitoredReentrantReadWriteLock(MonitoredLockType.EIM, true);
    private MonitoredReentrantLock pL = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EIM_PARTY, true);
    private MonitoredReentrantLock sL = MonitoredReentrantLockFactory.createLock(MonitoredLockType.EIM_SCRIPT, true);
    private boolean eventStarted = false;
    private EventScriptScheduler ess;
    private Invocable iv;
    private int leaderId = -1;
    public boolean scale = false;
    public boolean unlocked = false;
    public int MissionID = 0;
    public int rank = 0;
    public boolean mini_dungeon = false;
    public int exitMap = 0;
    public boolean forced = false;
    public boolean kaotic = false;
    private Map<String, Object> objectProps = new HashMap<>();
    private Map<String, Long> values = new ConcurrentHashMap<String, Long>();
    private Map<String, Long> longvalues = new ConcurrentHashMap<String, Long>();
    private Map<Integer, Integer> spawncap = new ConcurrentHashMap<Integer, Integer>();
    private ScheduledFuture<?> schedulerTask = null;
    public boolean ending = false, running = false, timer = false;
    public long cooldown = 0;
    public List<Integer> dojoBosses = Arrays.asList(new Integer[]{9305300, 9305301, 9305302, 9305303, 9305304, 9305305, 9305306, 9305307, 9305308, 9305309, 9305310, 9305311, 9305312, 9305313, 9305314, 9305315, 9305316, 9305317, 9305318, 9305319, 9305320, 9305321, 9305322, 9305323, 9305324, 9305325, 9305326, 9305327, 9305328, 9305329, 9305330, 9305331, 9305332, 9305333, 9305334, 9305335, 9305336, 9305337, 9305338, 9305339, 9833380, 9833381, 9833382, 9305672, 9305673, 9305674, 9305675, 9305676, 9305677, 8644011, 8642016, 2600800, 8220011, 8220012, 8645009, 8240099, 8800400, 8840000, 8850011, 8860000, 8880000, 8880101, 8880150, 8880302, 8880405, 8930100, 9420620, 9601068, 9421583, 9480235, 9480236, 9480237, 9480238, 9480239, 9390812, 9390822, 9390915});

    public EventInstanceManager(MapleCharacter chr, EventManager em, String name, boolean scale) {
        this.Owner = chr;
        this.scale = scale;
        this.em = em;
        this.name = name;
        this.ess = new EventScriptScheduler();
        this.mapManager = new MapleMapManager(this, em.getChannelServer().getChannel());
        this.iv = em.getIv();
        em.getChannelServer().addEvent(EventInstanceManager.this);
        this.eventStarted = true;
        chr.dropMessage("Starting event: " + name);
        timer();
    }

    public void timer() {
        schedulerTask = Timer.MapTimer.getInstance().register(() -> {
            if (!disposed && getPlayers().isEmpty()) {
                schedulerTask.cancel(true);
                dispose();
            }
        }, 5000, 5000);
    }

    public MapleCharacter getOwner() {
        return Owner;
    }

    public void register(MapleCharacter chr) {
        if (chr.getRaid() != null || chr.getParty() != null) {
            registerGroup(chr, chr.getMap());
        } else {
            registerPlayer(chr);
        }
    }

    public boolean isDojoBoss(int id) {
        return dojoBosses.contains(id);
    }

    public void setValue(String name, long value) {
        if (!values.containsKey(name)) {
            values.put(name, 0L);
        }
        values.put(name, value);
    }

    public long getValue(String name) {
        if (values.get(name) != null && values.containsKey(name)) {
            return values.get(name);
        }
        return -1;
    }

    public void setLongValue(String name, long value) {
        if (!longvalues.containsKey(name)) {
            longvalues.put(name, 0L);
        }
        longvalues.put(name, value);
    }

    public long getLongValue(String name) {
        return longvalues.get(name);
    }

    public void setKaotic(boolean value) {
        kaotic = value;
    }

    public boolean getKaotic() {
        return kaotic;
    }

    public boolean getDisposed() {
        return disposed;
    }

    public Invocable getIv() {
        return iv;
    }

    public boolean getUnlocked() {
        return unlocked;
    }

    public void setMiniDungeon(boolean toggle) {
        this.mini_dungeon = toggle;
    }

    public boolean getMiniDungeon() {
        return mini_dungeon;
    }

    public void setMission(int id) {
        MissionID = id;
    }

    public int getMission() {
        return MissionID;
    }

    public void setRank(int id) {
        rank = id;
    }

    public int getRank() {
        return rank;
    }

    public void setIntProperty(String key, Integer value) {
        setProperty(key, value);
    }

    public void setProperty(String key, Integer value) {
        setProperty(key, "" + value);
    }

    public void setProperty(String key, String value) {
        pL.lock();
        try {
            props.setProperty(key, value);
        } finally {
            pL.unlock();
        }
    }

    public Object setProperty(String key, String value, boolean prev) {
        pL.lock();
        try {
            return props.setProperty(key, value);
        } finally {
            pL.unlock();
        }
    }

    public void setObjectProperty(String key, Object obj) {
        pL.lock();
        try {
            objectProps.put(key, obj);
        } finally {
            pL.unlock();
        }
    }

    public String getProperty(String key) {
        pL.lock();
        try {
            return props.getProperty(key);
        } finally {
            pL.unlock();
        }
    }

    public int getIntProperty(String key) {
        pL.lock();
        try {
            String value = props.getProperty(key);
            return value != null ? Integer.parseInt(value) : -1;
        } finally {
            pL.unlock();
        }
    }

    public int getIntProperty(String key, int def) {
        pL.lock();
        try {
            String value = props.getProperty(key);
            return value != null ? Integer.parseInt(value) : def;
        } finally {
            pL.unlock();
        }
    }

    public Object getObjectProperty(String key) {
        pL.lock();
        try {
            return objectProps.get(key);
        } finally {
            pL.unlock();
        }
    }

    public final synchronized void registerPlayer(final MapleCharacter chr) {
        if (chr == null || disposed) {
            return;
        }

        wL.lock();
        try {
            if (chars.containsKey(chr.getId())) {
                return;
            }

            chars.put(chr.getId(), chr);
            chr.setEventInstance(this);
        } finally {
            wL.unlock();
        }
        try {
            invokeScriptFunction("playerEntry", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public Object invokeScriptFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (!disposed) {
            try {
                return iv.invokeFunction(name, args);
                //return em.getIv().invokeFunction(name, args);
            } catch (ScriptException e) {
                System.err.println("Exception in script \"" + this.getName() + "\" in function \"" + name + "\".");
                e.printStackTrace();
                throw e;
            }
        } else {
            return null;
        }
    }

    public void invokeScriptFunction(String name, boolean runEntryScript) {
        if (runEntryScript) {
            try {
                invokeScriptFunction(name, EventInstanceManager.this);
            } catch (ScriptException | NoSuchMethodException ex) {
                //System.out.println("test");
                //ex.printStackTrace();
            }
        }
    }

    public void executeScriptFunction(String name, Object... args) {
        if (!disposed) {
            try {
                iv.invokeFunction(name, args);
            } catch (ScriptException | NoSuchMethodException ex) {
            }
        }
    }

    public void changeMap(final MapleCharacter chr, final int mapId) {
        try {
            invokeScriptFunction("changedMap", EventInstanceManager.this, chr, mapId);
        } catch (ScriptException | NoSuchMethodException ex) {
        } // optional
    }

    public void changedMap(final MapleCharacter chr, final int mapId) {
        try {
            invokeScriptFunction("changedMap", EventInstanceManager.this, chr, mapId);
        } catch (ScriptException | NoSuchMethodException ex) {
        } // optional
    }

    public void afterChangedMap(final MapleCharacter chr, final int mapId) {
        try {
            invokeScriptFunction("afterChangedMap", EventInstanceManager.this, chr, mapId);
        } catch (ScriptException | NoSuchMethodException ex) {
        } // optional
    }

    public void startEventTimer(long time) {
        dismissEventTimer();
        timeStarted = System.currentTimeMillis();
        eventTime = time;
        timer = true;
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().announce(CField.getClock((int) Math.floor(time / 1000.0)));
        }

        eventTimer = TimerManager.getInstance().schedule(() -> {

            if (timer) {
                dismissEventTimer();
                try {
                    if (!disposed) {
                        invokeScriptFunction("scheduledTimeout", EventInstanceManager.this);
                    }
                } catch (ScriptException | NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, "Event '" + em.getName() + "' does not implement scheduledTimeout function.", ex);
                }
            }
        }, time);
    }

    public void createEventTimer(long time) {
        dismissEventTimer();
        timeStarted = System.currentTimeMillis();
        eventTime = time;
        try {
            for (MapleCharacter chr : getPlayers()) {
                chr.getClient().announce(CField.getClock((int) (time / 1000)));
            }
        } catch (Exception ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : restartEventTimer:\n");
        }

        eventTimer = TimerManager.getInstance().schedule(() -> {
            dismissEventTimer();
        }, time);

    }

    private void dismissEventTimer() {
        for (MapleCharacter chr : this.getPlayers()) {
            chr.getClient().announce(CField.removeClock());
        }
        eventTime = 0;
        timeStarted = 0;
        if (eventTimer != null) {
            eventTimer.cancel(false);
        }
        timer = false;
    }

    public void stopEventTimer() {
        eventTime = 0;
        timeStarted = 0;
        if (eventTimer != null) {
            eventTimer.cancel(false);
        }
        for (MapleCharacter chr : this.getPlayers()) {
            chr.getClient().announce(CField.removeClock());
        }
    }

    public boolean isTimerStarted() {
        return eventTime > 0 && timeStarted > 0;
    }

    public long getTimeLeft() {
        return eventTime - (System.currentTimeMillis() - timeStarted);
    }

    public final void registerGroup(MapleCharacter leader, MapleMap map) {
        if (disposed) {
            return;
        }
        for (MapleCharacter pc : leader.getPartyMembersOnSameMap()) {
            registerPlayer(pc);
        }
    }

    public final void registerParty(MapleParty party, MapleMap map) {
        if (disposed) {
            return;
        }
        for (MapleCharacter pc : party.getPlayerMembers()) {
            registerPlayer(pc);
        }
    }

    public void unregisterPlayer(final MapleCharacter chr) {
        if (chars.containsKey(chr.getId())) {
            chars.remove(chr.getId());
            chr.setEventInstance(null);
        }
        if (!disposed && getPlayers().isEmpty()) {
            dispose(false);
        }
        //chr.resetLevel();
    }

    public final boolean disposeIfPlayerBelow(final byte size, final int towarp) {
        if (disposed) {
            return true;
        }
        if (chars == null) {
            return false;
        }

        MapleMap map = null;
        if (towarp > 0) {
            map = this.getMapFactory().getMap(towarp);
        }

        List<MapleCharacter> players = getPlayers();

        try {
            if (players.size() < size) {
                for (MapleCharacter chr : players) {
                    if (chr == null) {
                        continue;
                    }

                    unregisterPlayer(chr);
                    if (towarp > 0) {
                        chr.changeMap(map, 0);
                    }
                }

                dispose();

                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public final void saveBossQuest(final int points) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            final MapleQuestStatus record = chr.getQuestNAdd(MapleQuest.getInstance(150001));

            if (record.getCustomData() != null) {
                record.setCustomData(String.valueOf(points + Integer.parseInt(record.getCustomData())));
            } else {
                record.setCustomData(String.valueOf(points)); // First time
            }
            chr.modifyCSPoints(1, points / 5, true);
            chr.getTrait(MapleTraitType.will).addExp(points / 100, chr);
        }
    }

    public final void saveNX(final int points) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.modifyCSPoints(1, points, true);
        }
    }

    public List<MapleCharacter> getAllPlayers() {
        if (disposed) {
            return Collections.emptyList();
        }
        rL.lock();
        try {
            return new ArrayList<>(chars.values());
        } finally {
            rL.unlock();
        }
    }

    public List<MapleCharacter> getPlayers() {
        List chrz = new ArrayList<>();
        for (MapleCharacter chr : getAllPlayers()) {
            if (chr != null) {
                chrz.add(chr);
            }
        }
        return chrz;
    }

    public List<Integer> getDisconnected() {
        return dced;
    }

    public final int getPlayerCount() {
        if (disposed) {
            return 0;
        }
        return chars.size();
    }

    public final int getLeaderId() {
        rL.lock();
        try {
            return leaderId;
        } finally {
            rL.unlock();
        }
    }

    public MapleCharacter getLeader() {
        rL.lock();
        try {
            return chars.get(leaderId);
        } finally {
            rL.unlock();
        }
    }

    public final void setLeader(MapleCharacter chr) {
        wL.lock();
        try {
            leaderId = chr.getId();
        } finally {
            wL.unlock();
        }
    }

    public void playerKilled(final MapleCharacter chr) {
        try {
            invokeScriptFunction("playerDead", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
        } // optional
    }

    public void revivePlayer(MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        chr.cancelAllBuffs();
        chr.getStat().heal(chr);
        chr.setStance(0);
        try {
            invokeScriptFunction("playerRevive", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void playerDisconnected(final MapleCharacter chr) {
        if (disposed) {
            return;
        }
        byte ret;
        try {
            ret = ((Double) em.getIv().invokeFunction("playerDisconnected", this, chr)).byteValue();
        } catch (Exception e) {
            ret = 0;
        }

        wL.lock();
        try {
            if (disposed) {
                return;
            }
            if (chr != null) {
                unregisterPlayer(chr);
            }

            if (!chars.isEmpty()) {
                if (chars.get(0) != null) {
                    Owner = chars.get(0);
                    dropMessage(6, Owner.getName() + " is now the new event leader, due to old leader dc.");
                } else {
                    Owner = null;
                }
            } else {
                dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
        } finally {
            wL.unlock();
        }

    }

    public void monsterKilled(MapleCharacter chr, final MapleMonster mob) {
        if (mob == null || chr == null) {
            return;
        }
        if (mob.getId() <= 10) {
            return;
        }
        int scriptResult = 0;
        sL.lock();
        try {
            //mobs.remove(mob);
            if (eventStarted) {
                scriptResult = 1;
                if (mob.getMap().getAllMonstersOnMapDead()) {
                    scriptResult = 2;
                }
            }
        } finally {
            sL.unlock();
        }
        if (scriptResult > 0) {
            try {
                invokeScriptFunction("monsterKilled", mob, EventInstanceManager.this);
            } catch (ScriptException | NoSuchMethodException ex) {
                ex.printStackTrace();
            }

            if (scriptResult > 1) {
                try {
                    invokeScriptFunction("allMonstersDead", EventInstanceManager.this);
                } catch (ScriptException | NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }
        }
        mob.setEventInstance(null);
    }

    public void mobKilled(MapleCharacter chr, final MapleMonster mob) {
        try {
            invokeScriptFunction("mobKilled", mob, EventInstanceManager.this);
        } catch (ScriptException | NoSuchMethodException ex) {
            //ex.printStackTrace();
        }
        if (mob.getMap().getAllMonstersOnMapDead()) {
            try {
                invokeScriptFunction("allMobKilled", EventInstanceManager.this);
            } catch (ScriptException | NoSuchMethodException ex) {
                //ex.printStackTrace();
            }
        }
    }

    public void allMobKilled() {
        try {
            invokeScriptFunction("allMonstersKilled", EventInstanceManager.this);
        } catch (ScriptException | NoSuchMethodException ex) {
            //ex.printStackTrace();
        }
    }

    public void monsterDamaged(final MapleCharacter chr, final MapleMonster mob, final int damage) {
        if (disposed || mob.getId() != 9700037) { //ghost PQ boss only.
            return;
        }
        try {
            em.getIv().invokeFunction("monsterDamaged", this, chr, mob.getId(), damage);
        } catch (ScriptException ex) {
            System.out.println("Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
        } catch (NoSuchMethodException ex) {
            System.out.println("Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
        }
    }

    public void addPVPScore(final MapleCharacter chr, final int score) {
        if (disposed) { //ghost PQ boss only.
            return;
        }
        try {
            em.getIv().invokeFunction("addPVPScore", this, chr, score);
        } catch (ScriptException ex) {
            System.out.println("Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
        } catch (NoSuchMethodException ex) {
            System.out.println("Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
        }
    }

    public int getKillCount(MapleCharacter chr) {
        if (disposed) {
            return 0;
        }
        Integer kc = killCount.get(chr.getId());
        if (kc == null) {
            return 0;
        } else {
            return kc;
        }
    }

    public void runCleaner() {
        if (!disposed) {
            TimerManager.getInstance().schedule(() -> {

                if (!disposed) {
                    if (getPlayers().isEmpty()) {
                        dispose();
                    } else {
                        runCleaner();
                    }
                }
            }, 10000);
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        dispose(false);
    }

    public synchronized void dispose(boolean shutdown) {    // should not trigger any event script method after disposed
        if (disposed) {
            return;
        }
        try {
            wL.lock();
            try {
                disposed = true;
                if (!getPlayers().isEmpty()) {
                    for (MapleCharacter chr : getPlayers()) {
                        exitPlayer(chr, chr.getMap().getReturnMapId());
                    }
                }
                if (schedulerTask != null) {
                    schedulerTask.cancel(false);
                }
                if (eventTimer != null) {
                    eventTimer.cancel(false);
                    eventTimer = null;
                }
            } finally {
                wL.unlock();
            }
            TimerManager.getInstance().schedule(() -> {
                if (!getPlayers().isEmpty()) {
                    for (MapleCharacter chr : getPlayers()) {
                        chr.kick();
                    }
                }
                Owner = null;
                ess.forceDispose();
                chars.clear();
                killCount.clear();
                mapIds.clear();
                props.clear();
                objectProps.clear();
                values.clear();
                longvalues.clear();
                spawncap.clear();
            }, 5000);
            TimerManager.getInstance().schedule(() -> {
                mapManager.forceDispose();
                em.getChannelServer().removeEvent(EventInstanceManager.this);
                em.disposedInstance(name);
            }, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearMaps() {
        mapManager.dispose();
    }

    public ChannelServer getChannelServer() {
        return em.getChannelServer();
    }

    public final void giveAchievement(final int type) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            if (chr.isAlive()) {
                chr.finishAchievement(type);
            }
        }
    }

    public final void broadcastPlayerMsg(final int type, final String msg) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.dropMessage(type, msg);
        }
    }

    public final void broadcastPlayerTopMsg(final String msg) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.dropTopMessage(msg);
        }
    }

    public final void broadcastPlayerMidMsg(final String msg) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.dropMidMessage(msg);
        }
    }

    public final void broadcastMapMsg(final String msg) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().announce(CField.startMapEffect(msg, 5122000, true));
        }
    }

    public final void broadcastMapMsg(final String msg, int itemid) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().announce(CField.startMapEffect(msg, itemid, true));
        }
    }

    public final void broadcastMapMsg(MapleCharacter chr, final String msg, int itemid) {
        if (disposed) {
            return;
        }
        chr.getClient().announce(CField.startMapEffect(msg, itemid, true));
    }

    public final void broadcastClearMsg() {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().announce(CField.removeMapEffect());
        }
    }

    //PVP
    public final List<Pair<Integer, String>> newPair() {
        return new ArrayList<Pair<Integer, String>>();
    }

    public void addToPair(List<Pair<Integer, String>> e, int e1, String e2) {
        e.add(new Pair<Integer, String>(e1, e2));
    }

    public final List<Pair<Integer, MapleCharacter>> newPair_chr() {
        return new ArrayList<Pair<Integer, MapleCharacter>>();
    }

    public void addToPair_chr(List<Pair<Integer, MapleCharacter>> e, int e1, MapleCharacter e2) {
        e.add(new Pair<Integer, MapleCharacter>(e1, e2));
    }

    public final void broadcastPacket(byte[] p) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().announce(p);
        }
    }

    public final void broadcastTeamPacket(byte[] p, int team) {
        if (disposed) {
            return;
        }
        for (MapleCharacter chr : getPlayers()) {
            if (chr.getTeam() == team) {
                chr.getClient().announce(p);
            }
        }
    }

    public MapleMapManager getMapFactory() {
        return mapManager;
    }

    public MapleMap getMapInstance(int mapId) {
        MapleMap map = this.mapManager.getMap(mapId);
        if (map != null) {
            if (map.getEventInstance() == this) {
                return map;
            }
            map.setEventInstance(this);
        } else {
            System.out.println("Map error with event " + this.getName() + " - ID: " + mapId);
            return null;
        }

        if (!this.mapManager.isMapLoaded(mapId)) {
            sL.lock();
            try {
                if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
                    map.shuffleReactors();
                }
            } finally {
                sL.unlock();
            }
        }
        return map;
    }

    public void schedule(final String methodName, long delay) {
        if (disposed) {
            return;
        }
        rL.lock();
        try {
            if (this.ess != null) {
                this.ess.registerEntry(() -> {
                    try {
                        if (em.getInstance(getName()) != null && !disposed) {
                            invokeScriptFunction(methodName, this);
                        }
                    } catch (ScriptException | NoSuchMethodException ex) {
                        System.out.println("Error in Event Script " + name);
                        ex.printStackTrace();
                    }
                }, delay);
            }
        } finally {
            rL.unlock();
        }
    }

    public final String getName() {
        return name;
    }

    public final Properties getProperties() {
        return props;
    }

    public void leftParty(final MapleCharacter chr) {
        try {
            invokeScriptFunction("leftParty", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void disbandParty() {
        try {
            invokeScriptFunction("disbandParty", EventInstanceManager.this);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void clearPQ() {
        try {
            invokeScriptFunction("clearPQ", EventInstanceManager.this);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void movePlayer(final MapleCharacter chr) {
        try {
            invokeScriptFunction("moveMap", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void removePlayer(final MapleCharacter chr) {
        try {
            invokeScriptFunction("playerExit", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public final void registerCarnivalParty(final MapleCharacter leader, final MapleMap map, final byte team) {
        if (disposed) {
            return;
        }
        leader.clearCarnivalRequests();
        List<MapleCharacter> characters = new LinkedList<MapleCharacter>();
        final MapleParty party = leader.getParty();

        if (party == null) {
            return;
        }
        for (MaplePartyCharacter pc : party.getMembers()) {
            final MapleCharacter c = map.getCharacterById(pc.getId());
            if (c != null) {
                characters.add(c);
                registerPlayer(c);
                c.resetCP();
            }
        }
        PartySearch ps = World.Party.getSearch(party);
        if (ps != null) {
            World.Party.removeSearch(ps, "The Party Listing has been removed because the Party Quest started.");
        }
        final MapleCarnivalParty carnivalParty = new MapleCarnivalParty(leader, characters, team);
        try {
            em.getIv().invokeFunction("registerCarnivalParty", this, carnivalParty);
        } catch (ScriptException ex) {
            System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : registerCarnivalParty:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : registerCarnivalParty:\n" + ex);
        } catch (NoSuchMethodException ex) {
            //ignore
        }
    }

    public void onMapLoad(final MapleCharacter chr) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("onMapLoad", this, chr);
        } catch (ScriptException ex) {
            System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : onMapLoad:\n" + ex);
            FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : onMapLoad:\n" + ex);
        } catch (NoSuchMethodException ex) {
            // Ignore, we don't want to update this for all events.
        }
    }

    public boolean isLeader(final MapleCharacter chr) {
        return (chr != null && chr.getParty() != null && chr.getParty().getLeader().getId() == chr.getId());
    }

    public void registerSquad(MapleSquad squad, MapleMap map, int questID) {
        if (disposed) {
            return;
        }
        final int mapid = map.getId();

        for (String chr : squad.getMembers()) {
            MapleCharacter player = squad.getChar(chr);
            if (player != null && player.getMapId() == mapid) {
                if (questID > 0) {
                    player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData(String.valueOf(System.currentTimeMillis()));
                }
                registerPlayer(player);
                if (player.getParty() != null) {
                    PartySearch ps = World.Party.getSearch(player.getParty());
                    if (ps != null) {
                        World.Party.removeSearch(ps, "The Party Listing has been removed because the Party Quest has started.");
                    }
                }
            }
        }
        squad.setStatus((byte) 2);
        squad.getBeginMap().broadcastMessage(CField.stopClock());
    }

    public boolean isDisconnected(final MapleCharacter chr) {
        if (disposed) {
            return false;
        }
        return (dced.contains(chr.getId()));
    }

    public void removeDisconnected(final int id) {
        if (disposed) {
            return;
        }
        dced.remove(id);
    }

    public final synchronized void startEvent() {
        eventStarted = true;

        try {
            invokeScriptFunction("afterSetup", EventInstanceManager.this);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public EventManager getEventManager() {
        return em;
    }

    public void applyBuff(final MapleCharacter chr, final int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(chr);
        chr.getClient().announce(InfoPacket.getStatusMsg(id));
    }

    public void applySkill(final MapleCharacter chr, final int id) {
        SkillFactory.getSkill(id).getEffect(1).applyTo(chr);
    }

    public void setOwner(MapleCharacter chr) {
        if (disposed) {
            return;
        }
        Owner = chr;
    }

    public void registerMonster(MapleMonster mob) {
        mob.setEventInstance(this);
        //mobs.add(mob);
    }

    public void unregisterMonster(MapleMonster mob) {
        mob.setEventInstance(null);
        //mobs.remove(mob);
    }

    public void exitPlayer(final MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("playerExit", EventInstanceManager.this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public boolean getForced() {
        return forced;
    }

    public void exitPlayer(MapleCharacter chr, int map) {
        if (chr == null) {
            return;
        }
        this.unregisterPlayer(chr);
        chr.eventExit = true;
        chr.exitMapbyId(map);
    }

    public void exitParty(int map) {
        if (!this.getPlayers().isEmpty()) {
            forced = true;
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr != null) {
                    exitPlayer(chr, map);
                }
            }
        }
    }

    public final void showClearEffect() {
        showClearEffect(false);
    }

    public final void showClearEffect(boolean hasGate) {
        if (getPlayers() == null) {
            return;
        }

        for (MapleCharacter chr : this.getPlayers()) {
            chr.getClient().announce(CField.MapEff("quest/party/clear"));
            chr.getClient().announce(CField.playSound("Party1/Clear"));
        }

    }

    public void gainEventScore(int type, int amount) {
        if (!getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr != null) {
                    if (type == 0) {
                        chr.updateTower(amount);
                    }
                    if (type == 1) {
                        chr.updateTrials(amount);
                    }
                }
            }
        }
    }

    public void victory(final int id) {
        if (!ending) {
            ending = true;
            for (MapleMap map : getMapFactory().getAllMaps()) {
                map.setEventClear(true);
                map.killAllMonsters(true);
            }
            broadcastMapMsg("You have won the event!", 5121104);
            startEventTimer(45000);
            changeMusic("BgmCustom/Fanfare");
            dropMessage(5, "Event will end in 45 seconds.");
        }
    }

    public void defeat(final int id) {
        if (!ending) {
            ending = true;
            for (MapleMap map : getMapFactory().getAllMaps()) {
                map.setEventClear(true);
                map.killAllMonsters(true);
            }
            broadcastMapMsg("You have lost the battle!", 5121104);
            startEventTimer(45000);
            dropMessage(5, "Event will end in 45 seconds.");
        }
    }

    public void MPvictory() {
        if (getValue("start") > 0) {
            long b = getValue("baseAch");
            setIntProperty("finished", 1);
            if (b > 0) {
                gainAchievement((int) (b + getValue("ach")));
            }
            if (getValue("coin") > 0) {
                gainPartyEtc(4310020, (int) (getValue("coin") * getPlayerCount()));
            }
            if (getValue("reward") > 0) {
                gainPartyEtc(4420015, (int) getValue("reward"));
            }
            if (getValue("rock") > 0) {
                gainPartyEtc((int) getValue("rock"), Randomizer.random(1, 4));
            }
            if (getValue("ticket") > 0) {
                gainPartyEtc((int) getValue("ticket"), 1);
            }
            if (getValue("gach") > 0) {
                gainPartyItemBase((int) getValue("gach"), Randomizer.random(1, 3));
            }
            long mexp = getValue("reward") * getValue("exp");
            if (mexp > 0) {
                gainPowerExp(109, mexp);
            }
            gainPartyItem(2430131, 1);
            victory(870000010);
        }
    }

    public void dropMessage(int type, String message) {
        for (MapleCharacter chr : this.getPlayers()) {
            chr.dropMessage(type, message);
        }
    }

    public void changeMusic(String value) {
        if (getPlayers() == null) {
            return;
        }
        for (MapleCharacter chr : this.getPlayers()) {
            chr.getClient().announce(CField.musicChange(value));
        }
    }

    public MapleMap getStoredmap() {
        return storedmap;
    }

    public void setStoredmap(MapleMap storedmap) {
        this.storedmap = storedmap;
    }

    public int eimLevel() {
        int value = 1;
        List<MapleCharacter> pary = this.getPlayers();
        for (MapleCharacter chr : pary) {
            if (chr != null && !chr.isGM() && chr.getTotalLevel() > value) {
                value = chr.getTotalLevel();
            }
        }
        return value;
    }

    public boolean getScale() {
        return scale;
    }

    public void setScale(boolean toggle) {
        this.scale = toggle;
    }

    public void gainPartyItemBase(int id, int amount) {
        if (getPlayers() == null) {
            return;
        }
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr.isAlive()) {
                if (canHold(chr, id, amount)) {
                    chr.gainItem(id, amount, "collected " + amount + " from party instance: " + this.getName());
                }
            }
        }
    }

    public void gainPartyItem(int id, int amount) {
        if (getPlayers() == null) {
            return;
        }
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr.isAlive()) {
                if (canHold(chr, id, amount)) {
                    chr.gainItem(id, amount, "collected " + amount + " from party instance: " + this.getName());
                } else {
                    if (GameConstants.canOverflowItem(id)) {
                        chr.dropMessage(amount + " " + MapleItemInformationProvider.getInstance().getName(id) + " Sent to Overflow");
                        chr.addOverflow(id, amount);
                    }
                }
            }
        }
    }

    public void gainPartyEtc(int id, int amount) {
        if (getPlayers() == null) {
            return;
        }
        String item = MapleItemInformationProvider.getInstance().getName(id);
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr.isAlive()) {
                if (GameConstants.canOverflowItem(id)) {
                    chr.addOverflow(id, amount);
                    chr.dropMessage(amount + " " + item + " has been added to your Etc Storage");
                }
            }
        }
    }

    public void gainPartyEquip(int id, int power, int scale) {
        if (getPlayers() == null) {
            return;
        }
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr.isAlive()) {
                chr.getAbstractPlayerInteraction().gainEquip(id, power, scale);
            }
        }
    }

    public void gainPartyStat(int type, int amount) {
        if (!this.getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr.isAlive()) {
                    //chr.addStat(amount, type);
                }
            }
        }
    }

    public void gainPartyStat(int type, int amount, boolean bypass) {
        if (!this.getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr.isAlive()) {
                    //chr.addStat(amount, type, bypass);
                }
            }
        }
    }

    public List<MapleCharacter> getPlayersInMap(int id) {
        List<MapleCharacter> members = new LinkedList<>();
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && getMapInstance(id) == chr.getMap() && !chr.isMapChange() && !chr.isGM()) {
                members.add(chr);
            }
        }
        return members;
    }

    public List<MapleCharacter> getActivePlayers() {
        List<MapleCharacter> members = new LinkedList<>();
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && !chr.isMapChange() && !chr.isGM()) {
                members.add(chr);
            }
        }
        return members;
    }

    public final void warpEventTeam(int warpFrom, int warpTo) {
        if (getPlayersInMap(warpFrom).size() == getPlayerCount()) {
            for (MapleCharacter chr : getPlayersInMap(warpFrom)) {
                if (chr.getMapId() == warpFrom) {
                    chr.changeMapbyId(warpTo);
                }
            }
        } else {
            dropMessage(6, "Missing party memebers on Map.");
        }
    }

    public final void warpMapTeam(int warpFrom, int warpTo) {
        for (MapleCharacter chr : getPlayersInMap(warpFrom)) {
            if (chr.getMapId() == warpFrom) {
                chr.changeMapbyId(warpTo);
            }
        }
    }

    public final void warpFullEventTeam(int warpTo) {
        for (MapleCharacter chr : getActivePlayers()) {
            if (chr != null && chr.isAlive()) {
                if (chr.getMapId() != warpTo) {
                    chr.changeMapbyId(warpTo);
                } else {
                    chr.changeMap(chr.getMap());
                }
            }
        }
    }

    public final void warpFullEventTeam(int warpTo, String pto) {
        for (MapleCharacter chr : getActivePlayers()) {
            if (chr != null && chr.isAlive()) {
                if (chr.getMapId() != warpTo) {
                    chr.changeMapbyId(warpTo);
                } else {
                    chr.changeMap(chr.getMap());
                }
            }
        }
    }

    public final void warpEventTeam(int warpTo) {
        for (MapleCharacter chr : getActivePlayers()) {
            if (chr != null && chr.isAlive() && chr.getMapId() != warpTo) {
                chr.changeMapbyId(warpTo);
            }
        }
    }

    public final void warpEventTeam(int warpTo, String pto) {
        for (MapleCharacter chr : getActivePlayers()) {
            if (chr != null && chr.isAlive() && chr.getMapId() != warpTo) {
                chr.changeMap(warpTo, pto);
            }
        }
    }

    public final void warp(MapleCharacter chr, int warpTo) {
        chr.changeMapbyId(warpTo);
    }

    public final void warp(MapleCharacter chr, int warpTo, String pto) {
        chr.changeMap(warpTo, pto);
    }

    public final void gainAchievement(int id) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr.isAlive()) {
                chr.finishAchievement(id);
            }
        }
    }

    public void gainPartyExp(int amount) {
        if (!getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr.isAlive() && chr.getTotalLevel() < 5000) {
                    long exp = chr.getOverExpPercValue(amount);
                    chr.dropMessage("Gained +" + NumberFormat.getInstance().format(exp) + " Exp");
                    chr.gainExp(exp, true, false, false);
                }
            }
        }
    }

    public void gainPartyEquip(int id, boolean toggle) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr.isAlive()) {
                    Item idrop = ii.randomizeStats((Equip) ii.getEquipById(id), 1);
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), idrop, true, false);
                }
            }
        }
    }

    public void gainPartyEquip(int id, int scale, boolean toggle) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr.isAlive()) {
                    Item idrop = ii.randomizeStats((Equip) ii.getEquipById(id), scale);
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), idrop, true, false);
                }
            }
        }
    }

    public void gainPartyEquip(int id, int scale, int pot, boolean toggle) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!getPlayers().isEmpty()) {
            for (MapleCharacter chr : this.getPlayers()) {
                if (chr.isAlive()) {
                    Item idrop = ii.randomizeStats((Equip) ii.getEquipById(id), scale);
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), idrop, true, false);
                }
            }
        }
    }

    public int getRandom(int x, int y) {
        return Randomizer.random(x, y);
    }

    public EventManager getEM() {
        return getEM();
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

    public MapleMonster getMonster(int id, int level, int scale, boolean script) {
        MapleMonster mob = MapleLifeFactory.getMonster(id, level, scale);
        mob.setEventScript(script);
        return mob;
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

    public MapleMonster getMonsterNoDrops(int id, int level, int scale, boolean script) {
        MapleMonster mob = MapleLifeFactory.getMonsterNoDrops(id, level, scale);
        mob.setEventScript(script);
        return mob;
    }

    public MapleMonster getMonsterNoDropsLink(int id) {
        return MapleLifeFactory.getMonsterNoDropsLink(id);
    }

    public MapleMonster getMonsterNoDropsLink(int id, int level) {
        return MapleLifeFactory.getMonsterNoDropsLink(id, level);
    }

    public MapleMonster getMonsterNoDropsLink(int id, int level, int scale) {
        return MapleLifeFactory.getMonsterNoAll(id, level, scale);
    }

    public MapleMonster getMonsterNoDropsLink(int id, int level, int scale, boolean script) {
        MapleMonster mob = MapleLifeFactory.getMonsterNoDropsLink(id, level, scale);
        mob.setEventScript(script);
        return mob;
    }

    public MapleMonster getMonsterNoLink(int id) {
        return MapleLifeFactory.getMonsterNoLink(id);
    }

    public MapleMonster getMonsterNoLink(int id, int level) {
        return MapleLifeFactory.getMonsterNoLink(id, level);
    }

    public MapleMonster getMonsterNoLink(int id, int level, int scale) {
        return MapleLifeFactory.getMonsterNoAll(id, level, scale);
    }

    public MapleMonster getMonsterNoLink(int id, int level, int scale, boolean script) {
        MapleMonster mob = MapleLifeFactory.getMonsterNoLink(id, level, scale);
        mob.setEventScript(script);
        return mob;
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

    public MapleMonster getMonsterNoAll(int id, int level, int scale, boolean script) {
        MapleMonster mob = MapleLifeFactory.getMonsterNoAll(id, level, scale);
        mob.setEventScript(script);
        return mob;
    }

    public MapleMonster getKaoticMonster(int id) {
        return MapleLifeFactory.getKaoticMonster(id);
    }

    public MapleMonster getKaoticMonster(int id, int level) {
        return MapleLifeFactory.getKaoticMonster(id, level);
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale) {
        return MapleLifeFactory.getKaoticMonster(id, level, scale);
    }

    public MapleMonster getKaoticMonsters(int id, int level, int scale) {
        return MapleLifeFactory.getKaoticMonsters(id, level, scale);
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, level, scale, bar, link, drops);
        mob.getStats().setBar(bar);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        mob.setBossAch(drops);
        return mob;
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, level, scale, bar, link, drops);
        if (script) {
            mob.setEventScript(script);
        }
        mob.getStats().setBar(bar);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        mob.setBossAch(drops);
        return mob;
    }

    public MapleMonster getMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed) {
        MapleMonster mob = MapleLifeFactory.getMonster(id, level, scale, bar, link, drops, Randomizer.Max((int) fixed, 9999));
        if (script) {
            mob.setEventScript(script);
        }
        mob.getStats().setBar(bar);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        mob.setBossAch(drops);
        return mob;
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, level, scale, bar, link, drops, Randomizer.Max((int) fixed, 9999));
        if (script) {
            mob.setEventScript(script);
        }
        mob.getStats().setBar(bar);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        mob.setBossAch(drops);
        return mob;
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed, boolean mega, boolean kaotic) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, level, scale, bar, link, drops, Randomizer.Max((int) fixed, 9999), mega, kaotic);
        if (script) {
            mob.setEventScript(script);
        }
        mob.getStats().setBar(bar);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        mob.setBossAch(drops);
        return mob;
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed, boolean mega, boolean kaotic, boolean ultimate) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, level, scale, bar, link, drops, Randomizer.Max((int) fixed, 9999), mega, kaotic, ultimate);
        mob.setEventScript(script);
        mob.getStats().setBar(bar);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        mob.setBossAch(drops);
        return mob;
    }

    public MapleMonster getKaoticMonster(int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed, boolean mega, boolean kaotic, boolean ultimate, boolean stats) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, level, scale, bar, link, drops, Randomizer.Max((int) fixed, 9999), mega, kaotic, ultimate);
        mob.setEventScript(script);
        mob.getStats().setBar(bar);
        mob.setBossStats(stats);
        mob.setBossAch(stats);
        if (!link) {
            mob.getStats().disableRevives();
        }
        if (!drops) {
            mob.disableDrops();
        }
        return mob;
    }

    public Point newPoint(int x, int y) {
        return new Point(x, y);
    }

    public Point newRandomPoint(int x1, int x2, int y1, int y2) {
        return new Point(Randomizer.random(x1, x2), Randomizer.random(y1, y2));
    }

    public void setMapInfo(MapleMap map, int mapid) {
        map.setReturnMapId(mapid);
        map.setForcedReturnMap(mapid);
    }

    public void setExitMap(int id) {
        exitMap = id;
    }

    public int getExitMap() {
        return exitMap;
    }

    public void playerItemMsg(MapleCharacter chr, String Msg, int id) {
        chr.getClient().announce(CField.startMapEffect(Msg, id, true));
    }

    public void eimItemMsg(String Msg, int id) {
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().announce(CField.startMapEffect(Msg, id, true));
        }
    }

    public String getMobName(int id) {
        return MapleLifeFactory.getName(id);
    }

    public void levelUp() {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive()) {
                chr.levelUp();
            }
        }
    }

    public void levelUp(int cap) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getTotalLevel() < cap) {
                chr.levelUp();
            }
        }
    }

    public void levelUpMax(int min, int max) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getTotalLevel() < max && chr.getTotalLevel() >= min) {
                chr.levelUp();
            }
        }
    }

    public int getAvgLevel() {
        double lvl = 0;
        for (MapleCharacter chr : getPlayers()) {
            lvl += chr.getTotalLevel();
        }
        return (int) Math.floor(lvl / getPlayerCount());
    }

    public void levelUp(int levels, int max) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive()) {
                int total = levels;
                if (levels + chr.getTotalLevel() >= max) {
                    total = 0;
                }
                if (levels + chr.getTotalLevel() < max) {
                    total = Randomizer.Max(max - chr.getTotalLevel(), levels);
                }
                if (total > 0) {
                    for (int i = 0; i < total; i++) {
                        chr.levelUp();
                    }
                }
            }
        }
    }

    public void levelUpMax(int max) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getTotalLevel() < max) {
                chr.levelUp();
            }
        }
    }

    public void gainDojoExp(long value) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive()) {
                chr.gainDojoExp(value);
            }
        }
    }

    public void gainDojoLeechExp(long value, MapleCharacter player) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr != player && chr.isAlive()) {
                chr.gainDojoExp(value);
            }
        }
    }

    public void gainDojoMobExp(long value, MapleMonster mob) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getMap() == mob.getMap()) {
                if (mob.getAttackers().contains(chr)) {
                    chr.gainDojoExp(value);
                }
            }
        }
    }

    public void gainPartyItem(int id, int amount, MapleMonster mob) {
        if (getPlayers() == null) {
            return;
        }
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr != null && chr.isAlive()) {
                if (mob.getAttackers().contains(chr)) {
                    if (canHold(chr, id, amount)) {
                        chr.gainItem(id, amount, "collected " + amount + " from party instance: " + this.getName());
                    } else {
                        chr.dropMessage(-1, amount + " " + MapleItemInformationProvider.getInstance().getName(id) + " Sent to Overflow");
                        chr.addOverflow(id, amount);
                    }
                }
            }
        }
    }

    public void gainPartySkinLevel(MapleMonster mob) {
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr != null && chr.isAlive()) {
                if (mob.getAttackers().contains(chr)) {
                    chr.gainSkinLevel();
                }
            }
        }
    }

    public void gainPartyStam(MapleMonster mob, int value) {
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr != null && chr.isAlive()) {
                if (mob.getAttackers().contains(chr)) {
                    chr.gainStamina(value, true);
                }
            }
        }
    }

    public void gainPartyItem(int id, int amount, MapleMonster mob, int range, int chance) {
        if (getPlayers() == null) {
            return;
        }
        for (MapleCharacter chr : this.getPlayers()) {
            if (chr != null && chr.isAlive()) {
                int roll = getRandom(1, range);
                if (roll <= chance) {
                    if (canHold(chr, id, amount)) {
                        chr.gainItem(id, amount, "collected " + amount + " from party instance: " + this.getName());
                    } else {
                        chr.dropMessage(-1, amount + " " + MapleItemInformationProvider.getInstance().getName(id) + " Sent to Overflow");
                        chr.addOverflow(id, amount);
                    }
                }
            }
        }
    }

    public boolean canHold(MapleCharacter chr, int id, int amount) {
        boolean etc = GameConstants.getInventoryType(id) == MapleInventoryType.USE || GameConstants.getInventoryType(id) == MapleInventoryType.ETC;
        if (chr.canHold(id, amount)) {
            if (etc && amount > 30000) {
                return false;
            } else {
                return true;
            }
        } else {
            if (etc) {
                return false;
            }
        }
        return true;
    }

    public void gainPowerLvl(int type, int value) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive()) {
                chr.gainLevelsData(type, value);
            }
        }
    }

    public void gainPartyPowerLvl(int type, int value, int range, int chance) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive()) {
                if (getRandom(1, range) <= chance) {
                    chr.gainLevelsData(type, value);
                }
            }
        }
    }

    public void gainPowerExp(int type, long value) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive()) {
                chr.gainLevelData(type, value);
            }
        }
    }

    public void gainPowerLvl(int type, int value, MapleMonster mob) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getMap() == mob.getMap()) {
                if (mob.getAttackers().contains(chr)) {
                    chr.gainLevelsData(type, value);
                }
            }
        }
    }

    public void gainPowerLvl(int type, int value, MapleMonster mob, int range, int chance) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getMap() == mob.getMap()) {
                if (mob.getAttackers().contains(chr)) {
                    if (getRandom(1, range) <= chance) {
                        chr.gainLevelsData(type, value);
                    }
                }
            }
        }
    }

    public void gainPowerExp(int type, long value, MapleMonster mob) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getMap() == mob.getMap()) {
                if (mob.getAttackers().contains(chr)) {
                    chr.gainLevelData(type, value);
                }
            }
        }
    }

    public void gainRewardByLevel(int mapid, int id) {
        for (MapleCharacter chr : getPlayers()) {
            if (chr != null && chr.isAlive() && chr.getMapId() == mapid) {
                chr.gainItem(id, chr.getTotalLevel(), "collected " + chr.getTotalLevel() + " from party instance level: " + this.getName());
            }
        }
    }

    public int getSpawnCap(int map) {
        if (spawncap == null) {
            return 0;
        }
        if (spawncap.isEmpty() || !spawncap.containsKey(map)) {
            return 0;
        }
        return spawncap.get(map);
    }

    public void setSpawnCap(int map, int value) {
        spawncap.put(map, value);
    }

    public void sysMsg(String msg) {
        System.out.println(msg);
    }

    public boolean isWed() {
        /*
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.FRIDAY,Calendar.SATURDAY, Calendar.SUNDAY -> {
                return true;
            }
        }
         */
        return true;
    }

    public void setupPark(int mode) {
        Park park = Parks.getInstance().getById(mode);
        setValue("level", park.getLevel());
        setValue("tier", park.getBaseTier());
        setValue("baseTier", park.getBaseTier());
        setValue("last", park.getWaves());
        setValue("raidboss", park.getWaves());
        setValue("rank", park.getRank());
        setValue("reward_item", park.getReward());
        setValue("reward_amount", park.getRewardAmount());
        setValue("endless", 1);
    }

    public int getRandomMap(int mode) {
        List<Integer> maps = Parks.getInstance().getById(mode).getMaps();
        int map = maps.get(random(maps.size() - 1));
        setValue("mapid", map);
        return map;
    }

    public int spawnRandomMap(int mode) {
        int map = (int) getValue("mapid");
        int old = map;
        while (old == map) {
            List<Integer> maps = Parks.getInstance().getById(mode).getMaps();
            map = maps.get(random(maps.size() - 1));
        }
        setValue("mapid", map);
        return map;
    }

    public void clearBotChecks() {

    }

    public boolean processNextMap(MapleMap oldMap) {
        long time = System.currentTimeMillis();
        if (cooldown <= time) {
            if (getValue("clear") == 1) {
                int phase = 0;
                int stage = (int) (getValue("stage") + 1);
                int last = (int) getValue("raidboss");
                int baseTier = (int) getValue("baseTier");
                int mode = (int) getValue("mode");
                int level = (int) getValue("level");
                int shop = (int) getValue("shop");
                int mtimer = 300;
                boolean mini_boss = getValue("miniboss") > 0;
                boolean mid_boss = getValue("midboss") > 0;
                boolean final_boss = getValue("finalboss") > 0;
                boolean reward = getValue("reward") > 0;
                boolean job = getValue("job") > 0;
                if (mini_boss) {
                    if (stage > 0 && stage % 5 == 0) {
                        phase = 1;
                    }
                }
                if (mid_boss) {
                    if (stage > 0 && stage % 25 == 0) {
                        phase = 2;
                    }
                }
                if (final_boss && stage == last) {
                    phase = 3;
                }
                if (phase == 0 && random(1, 20) == 1) {
                    phase = 4;
                }
                var mapi = spawnRandomMap(mode);
                MapleMap mapz = this.getMapInstance(mapi);
                if (mapz != null) {
                    Park park = Parks.getInstance().getById(mode);
                    boolean cap = park.getMiniCap() > 0;
                    mapz.setPark(true);
                    mapz.setEndless(true);
                    mapz.killAllMonsters(false);
                    mapz.setSpawnCap(250);
                    int tier = (int) (baseTier + ((int) Math.floor(stage * 0.1)));
                    setValue("tier", tier);
                    setValue("stage", stage);
                    setMapInfo(mapz, (int) getValue("exit"));
                    mapz.tempBgm = mapz.getBGM();
                    if (job) {
                        mtimer = 120;
                    }
                    if (phase == 0) {
                        mapz.parkSpawn(level + getAvgLevel(), tier, cap);
                    }
                    if (phase == 1) {
                        mtimer = 600;
                        mapz.setBGM("BgmFF8/Force_your_way");
                        int bossId = park.getRandomMiniBoss();
                        setValue("boss", bossId);
                        MapleMonster mob = getKaoticMonster(bossId, level + getAvgLevel(), (tier + park.getMiniBossTier()), true, false, true, true, park.getMiniCap() * getPlayerCount(), false, false, false, true);
                        mob.setBossAch(false);
                        getMapInstance(mapz.getId()).spawnMonsterOnGroundBelow(mob, mapz.getRandomMonsterSpawnPointPos());
                    }
                    if (phase == 2) {
                        mtimer = 1200;
                        mapz.setBGM("BgmFF8/Premotion");
                        int bossId = park.getRandomBoss();
                        setValue("boss", bossId);
                        MapleMonster mob = getKaoticMonster(bossId, level + getAvgLevel(), (tier + park.getFinalBossTier()), true, false, true, true, park.getBossCap() * getPlayerCount(), Randomizer.nextBoolean(), false, false, true);
                        mob.setBossAch(false);
                        getMapInstance(mapz.getId()).spawnMonsterOnGroundBelow(mob, mapz.getRandomMonsterSpawnPointPos());
                    }
                    if (phase == 3) {
                        mtimer = 1800;
                        mapz.setBGM("BgmFF8/Legendary_Beast");
                        int bossId = park.getRandomFinalBoss();
                        setValue("boss", bossId);
                        MapleMonster mob = getKaoticMonster(bossId, level + getAvgLevel(), (tier + park.getFinalBossTier()), true, false, true, true, park.getFinalCap(), true, false, false, true);
                        mob.setBossAch(false);
                        getMapInstance(mapz.getId()).spawnMonsterOnGroundBelow(mob, mapz.getRandomMonsterSpawnPointPos());
                    }
                    if (phase == 4) {
                        mapz.spawnNpc(9201186, mapz.getRandomMonsterSpawnPointPos(), Randomizer.nextBoolean());
                    }
                    warpEventTeam(mapi);
                    oldMap.setClear(false);
                    oldMap.resetBGM();
                    oldMap.removeAllItems(true);
                    oldMap.removeAllNpcsPark();
                    if (shop > 0) {
                        if (random(1, 20) == 1) {
                            mapz.spawnNpc(shop, mapz.getRandomMonsterSpawnPointPos(), Randomizer.nextBoolean());
                        }
                    }
                    setValue("clear", 0);
                    cooldown = System.currentTimeMillis() + 5000;
                    if (phase == 4) {
                        broadcastMapMsg("Talk to Bot Checker to clear this wave and recieve rewards", 5120205);
                    } else {
                        broadcastMapMsg("Wave: " + stage + ": Defeat all the Monsters", 5120205);
                    }
                    if (!reward) {
                        startEventTimer(1000 * mtimer);
                    }
                    for (MapleCharacter chr : getAllPlayers()) {
                        chr.setBotCheck(false);
                    }
                    return true;
                } else {
                    System.out.println("error with mapid: " + mapi);
                }
            }
            cooldown = System.currentTimeMillis() + 5000;
        }
        return false;
    }

    public String getCoolDown() {
        long curr = System.currentTimeMillis();
        return "Time until portal unlock " + ((cooldown - curr) * 0.001) + " Seconds.";
    }

    public void checkBots(MapleMap map) {
        boolean clear = true;
        for (MapleCharacter chr : getAllPlayers()) {
            if (!chr.getBotCheck()) {
                clear = false;
            }
        }
        if (clear) {
            processClear(map);
        }
    }

    public void processClear(MapleMap map) {
        if (getValue("clear") == 0) {
            int stage = (int) getValue("stage");
            int last = (int) getValue("raidboss");
            boolean mini_boss = getValue("miniboss") > 0;
            boolean mid_boss = getValue("midboss") > 0;
            boolean final_boss = getValue("finalboss") > 0;
            boolean job = getValue("job") > 0;
            boolean boss = false;
            if (mini_boss) {
                if (stage > 0 && stage % 5 == 0) {
                    boss = true;
                }
            }
            if (mid_boss) {
                if (stage > 0 && stage % 25 == 0) {
                    boss = true;
                }
            }
            if (final_boss && stage == last) {
                boss = true;
            }
            if (!boss) {
                map.broadcastMessage(CField.MapEff("monsterPark/clear"));
                setValue("clear", 1);
            }
            gainPowerExp(102, (long) (Math.pow(stage, 1.0 + (stage * 0.01)) * getPlayerCount()));
            executeScriptFunction("waveClear", EventInstanceManager.this);
            //levelUp();
        }
    }

    public final void warpEventTeamScript0(int warpTo) {
        for (MapleCharacter chr : getAllPlayers()) {
            if (chr != null && chr.isAlive() && chr.getMapId() != warpTo) {
                chr.changeMapbyId(warpTo);
            }
        }
    }

    public void showClear(MapleMap map) {
        map.broadcastMessage(CField.MapEff("monsterPark/clear"));
    }

    public void countPots(MapleCharacter player) {
        player.pots = 0;
    }

    public void sendServerMsg(String msg) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                victim.dropMessage(6, msg);
            }
        }
    }

    public Point getRandomPlayerPos() {
        if (!getActivePlayers().isEmpty()) {
            return getActivePlayers().get(Randomizer.nextInt(getActivePlayers().size())).getPosition();
        }
        return null;
    }

    public boolean getRandomBool() {
        return Randomizer.nextBoolean();
    }

    public boolean checkGM() {
        boolean check = true;
        for (MapleCharacter chr : getAllPlayers()) {
            if (chr.isGMJob()) {
                check = false;
            }
        }
        return check;
    }

    public void executeJob() {
        for (MapleCharacter chr : getAllPlayers()) {
            NPCScriptManager.getInstance().start(chr.getClient(), 9010009, "random_job");
        }
    }

    public void executeJob(MapleCharacter chr) {
        NPCScriptManager.getInstance().start(chr.getClient(), 9010009, "random_job");
    }

    public void resetJob() {
        for (MapleCharacter chr : getAllPlayers()) {
            if (!chr.isGMJob()) {
                chr.switchJob(910);
            }
        }
    }

    public void resetJob(MapleCharacter chr) {
        if (!chr.isGMJob()) {
            chr.switchJob(910);
        }
    }

    public void upgradeJob() {
        for (MapleCharacter chr : getAllPlayers()) {
            String job = "" + chr.getJob();
            chr.upgradeJob(job);
        }
    }

    public void sendSysMsg(String msg) {
        System.out.println(msg);
    }

    public void spawnMonsterOnGroundBelow(int mapid, MapleMonster mob, Point pos) {
        getMapInstance(mapid).spawnMonsterOnGroundBelow(mob, pos);
    }

    public void spawnFakeMonster(int mapid, MapleMonster mob, Point pos) {
        MapleMap map = getMapInstance(mapid);
        final Point spos = map.calcPointBelow(new Point(pos.x, pos.y - 10));
        if (spos != null) {
            spos.y--;
            mob.setPosition(spos);
            mob.setFHMapData(map, pos);
        }
        map.spawnFakeMonster(mob);
    }

    public void setChrQuestLock(int id, long time) {
        for (MapleCharacter chr : getAllPlayers()) {
            chr.setQuestLock(id, time);
        }
    }

    public void setAccQuestLock(String id, long time) {
        for (MapleCharacter chr : getAllPlayers()) {
            chr.setAccVaraLock(id, time);
        }
    }
}
