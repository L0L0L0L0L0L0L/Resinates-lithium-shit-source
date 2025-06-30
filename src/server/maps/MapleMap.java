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
package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.Calendar;

import client.inventory.Equip;
import client.inventory.Item;
import constants.GameConstants;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.MonsterFamiliar;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.maplepal.MaplePalBattleManager;
import client.maplepal.PalTemplateProvider;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import database.DatabaseConnection;

import handling.channel.ChannelServer;
import handling.channel.handler.DamageParse;
import handling.channel.handler.GuildHandler;
import handling.world.PartyOperation;
import handling.world.exped.ExpeditionType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import scripting.EventInstanceManager;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.Randomizer;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MapleLifeFactory;
import server.life.SpawnPoint;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.MapleMonsterInformationProvider;
import tools.packet.PetPacket;
import tools.packet.MobPacket;
import scripting.EventManager;
import server.MapleCarnivalFactory;
import server.MapleCarnivalFactory.MCSkill;
import server.MapleSquad;
import server.MapleSquad.MapleSquadType;
import server.RewardDropEntry;
import server.SpeedRunner;
import server.Timer.MapTimer;
import server.Timer.EtcTimer;
import server.TimerManager;
import server.life.MapleLifeFactory.selfDestruction;
import server.life.MapleMonsterStats;
import server.life.MonsterGlobalRareDropEntry;
import server.maps.MapleNodes.DirectionInfo;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import tools.Pair;
import tools.packet.CField.EffectPacket;
import tools.packet.CField.NPCPacket;
import tools.packet.CField;
import tools.packet.CField.SummonPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.PartyPacket;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MaplePlayerShop;
import tools.AttackPair;
import tools.StringUtil;

public final class MapleMap {

    /*
     * Holds mappings of OID -> MapleMapObject separated by MapleMapObjectType.
     * Please acquire the appropriate lock when reading and writing to the LinkedHashMaps.
     * The MapObjectType Maps themselves do not need to synchronized in any way since they should never be modified.
     */
    //private final Map<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> mapobjects;
    //private Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<>();
    private Map<Integer, MapleMapObject> mapobjects = new ConcurrentHashMap<Integer, MapleMapObject>();
    //private final Map<MapleMapObjectType, ReentrantReadWriteLock> mapobjectlocks;
    private final List<MapleCharacter> characters = new ArrayList<MapleCharacter>();
    //private final ReentrantReadWriteLock charactersLock = new ReentrantReadWriteLock();
    //private int runningOid = 1000000001;
    private AtomicInteger runningOid = new AtomicInteger(1000000);
    private AtomicInteger playerCount = new AtomicInteger(0);
    private final Lock runningOidLock = new ReentrantLock();
    //private final List<Spawns> monsterSpawn = new ArrayList<Spawns>();
    private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private AtomicInteger spawnedSummonsOnMap = new AtomicInteger(0);
    private final Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
    //private MapleFootholdTree footholds = null;
    private float monsterRate, recoveryRate;
    private MapleMapEffect mapEffect;
    private byte channel;
    private short decHP = 0, createMobInterval = 9000, top = 0, bottom = 0, left = 0, right = 0;
    private int consumeItemCoolTime = 0, protectItem = 0, decHPInterval = 10000, mapid, returnMapId, timeLimit,
            fieldLimit, maxRegularSpawn = 0, fixedMob, forcedReturnMap = 999999999, instanceid = -1,
            lvForceMove = 0, lvLimit = 0, permanentWeather = 0, partyBonusRate = 0;
    private boolean town, clock, personalShop, everlast = false, dropsDisabled = false, gDropsDisabled = false,
            soaring = false, squadTimer = false, isSpawns = true, checkStates = true;
    private String mapName, streetName, onUserEnter, onFirstUserEnter, speedRunLeader = "";
    private List<Integer> dced = new ArrayList<Integer>();
    private ScheduledFuture<?> squadSchedule;
    private long speedRunStart = 0, lastSpawnTime = 0, lastHurtTime = 0;
    private MapleNodes nodes;
    private MapleSquadType squad;
    private Map<String, Integer> environment = new LinkedHashMap<String, Integer>();
    private boolean allowSummons = true; // All maps should have this true at the beginning
    private EventInstanceManager event = null;
    private short mobInterval = 3000;

    private final ReadLock chrRLock;
    private final WriteLock chrWLock;
    private final ReadLock mobRLock;
    private final WriteLock mobWLock;
    private final ReadLock objectRLock;
    private final WriteLock objectWLock;
    private static final Map<Integer, Pair<Integer, Integer>> dropBoundsCache = new HashMap<>(100);
    private static final Lock bndLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_BOUNDS, true);
    private final Lock lootLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MAP_LOOT, true);
    private Pair<Integer, Integer> xLimits;  // caches the min and max x's with available footholds
    public Rectangle mapArea = new Rectangle();
    public String bgm, tempBgm;
    public int kills = 0, avgLevel = 1, phase = 0, specialEvent = 0;
    public boolean locked = false;
    private long timeStarted = 0;
    private long eventTime = 0;
    public boolean mini = false;
    private Set<Integer> selfDestructives = new LinkedHashSet<>();
    public boolean instanced = false;
    private int numTimes = 0;
    public boolean eventlocked = false;
    public boolean pqlock = false;
    private Map<MapleCharacter, MapleMonster> totem = new ConcurrentHashMap<MapleCharacter, MapleMonster>();
    public boolean reset = false;
    public Rectangle MapBounds = null;
    public boolean eventClear = false;
    public int maxSpawn = 0;
    private ScheduledFuture<?> schedulerTask = null;
    public long lock = 0;
    public boolean kaotic = false, clear = false, local = false, cleaned = false, cleaning = false, loaded = false, endless = false, pot = false, start = false, allowPets = true, park = false;
    public boolean allowVac = true;
    public int scale = 1, boost = 0, timeout = 0;
    public Map<String, Boolean> objectFlags = new ConcurrentHashMap<String, Boolean>();
    public Map<String, Integer> objectInt = new ConcurrentHashMap<String, Integer>();
    public Map<String, Integer> var = new ConcurrentHashMap<String, Integer>();
    public Map<String, Boolean> flag = new ConcurrentHashMap<String, Boolean>();
    private final Map<Integer, List<RewardDropEntry>> rewardDrops = new ConcurrentHashMap<>();
    public int spawncap = 100;
    public int boosted = 1, palTimer = 0, ballCount = 0, pal_level = 1;
    public double skillexp = 1.0, expRate = 1.0;
    public boolean disposing = false, disposed = false;
    public boolean eggSpawn = false, loading = false;
    public MapleFootholdTree MapleFootholds = null;
    public List<SpawnPoint> SpawnPoints = new ArrayList<SpawnPoint>();
    public Lock itemLock = new ReentrantLock();
    public Lock mobLock = new ReentrantLock();
    public Lock itemDropLock = new ReentrantLock();
    public Lock reactorLock = new ReentrantLock();
    public int leftLimit = 0, rightLimit = 0;

    public MapleMap(final int mapid, final int channel, final int returnMapId, final float monsterRate) {
        this.mapid = mapid;
        this.channel = (byte) channel;
        this.returnMapId = returnMapId;
        if (this.returnMapId == 999999999) {
            this.returnMapId = mapid;
        }
        if (GameConstants.getPartyPlay(mapid) > 0) {
            this.monsterRate = (monsterRate - 1.0f) * 2.5f + 1.0f;
        } else {
            this.monsterRate = monsterRate;
        }
        //EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> objsMap = new EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>>(MapleMapObjectType.class);
        //EnumMap<MapleMapObjectType, ReentrantReadWriteLock> objlockmap = new EnumMap<MapleMapObjectType, ReentrantReadWriteLock>(MapleMapObjectType.class);
        //for (MapleMapObjectType type : MapleMapObjectType.values()) {
        //objsMap.put(type, new LinkedHashMap<Integer, MapleMapObject>());
        //objlockmap.put(type, new ReentrantReadWriteLock());
        //}
        //mapobjects = Collections.unmodifiableMap(objsMap);
        //mapobjectlocks = Collections.unmodifiableMap(objlockmap);

        final ReentrantReadWriteLock chrLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_CHRS, true);
        chrRLock = chrLock.readLock();
        chrWLock = chrLock.writeLock();

        final ReentrantReadWriteLock mobLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_MOB, true);
        mobRLock = mobLock.readLock();
        mobWLock = mobLock.writeLock();

        final ReentrantReadWriteLock objectLock = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_OBJS, true);
        objectRLock = objectLock.readLock();
        objectWLock = objectLock.writeLock();
        //timer();
        //maptats();
    }

    public Rectangle getBounds() {
        return mapArea;
    }

    public void timer() {
        if (schedulerTask == null) {
            loaded = true;
            schedulerTask = MapTimer.getInstance().register(() -> mapstats(), 1000, 1000);
        }
    }

    public MapleMap getMap() {
        return this;
    }

    public boolean isDisposing() {
        return disposing;
    }

    public void dispose() {
        disposing = true;
        MapTimer.getInstance().schedule(() -> {
            if (getPlayerCount() <= 0) {
                disposed = true;
                clean(true);
                locked = true;
                event = null;
                mapEffect = null;
                for (MapleMapObject obj : getAllObjects()) {
                    removeMapObject(obj, obj.getType());
                }
                portals.clear();
                mapobjects.clear();
                rewardDrops.clear();
            } else {
                disposing = false;
            }
        }, 5000);
    }

    public void forceDispose() {
        disposed = true;
        locked = true;
        event = null;
        mapEffect = null;
        clean(true);
        for (MapleMapObject obj : getAllObjects()) {
            removeMapObject(obj, obj.getType());
        }
        portals.clear();
        mapobjects.clear();
        rewardDrops.clear();
    }

    public boolean isPalMob(int id) {
        if (id >= 2000 && id <= 2100) {
            return true;
        }
        return false;
    }

    public void clean(boolean check) {
        if (getPlayers().isEmpty()) {
            if (schedulerTask != null) {
                loaded = false;
                schedulerTask.cancel(true);
                schedulerTask = null;
            }
            if (!getAllTotems().isEmpty()) {
                removeAllTotems();
            }
            if (!getAllTrueMonsters().isEmpty()) {
                removeAllMonsters();
            }
            if (!getAllItems().isEmpty()) {
                removeAllItems(false);
            }
        }
    }

    public boolean getClean() {
        return cleaned;
    }

    public void mapstats() {
        if (cleaning || !loaded || disposed) {
            return;
        }
        numTimes++;
        try {
            if (!loading && !getPlayers().isEmpty()) {
                if (timeout > 0) {
                    timeout = 0;
                }
                if (boost > 0) {
                    boost--;
                }
                if (lock > 0 && System.currentTimeMillis() >= lock) {
                    kills = 0;
                    lock = 0;
                }
                if (palTimer > 0) {
                    palTimer--;
                }
                if (spawnedMonstersOnMap.get() < 0) {
                    spawnedMonstersOnMap.set(getParkMonsters().size());
                }
                if (!getAllTrueMonsters().isEmpty()) {
                    long curr = System.currentTimeMillis();
                    for (MapleMonster mob : getAllTrueMonsters()) {
                        if (mob != null && !mob.isDead() && mob.spawning < curr) {
                            if (mob.getPosition().getY() > (bottom + 100)) {
                                mob.randomTeleport();
                            }
                            mob.cancelAllBuff();
                            mob.updateMonsterController();
                        }
                    }
                }
                for (MapleCharacter chr : getAllPlayers()) {
                    if (chr != null) {
                        if (chr.battle) {
                            broadcastMessage(chr, CField.EffectPacket.showCraftingEffect(chr.getId(), "Effect/BasicEff.img/professions/pal_battle", 1000, 0), false);
                        }
                        boolean flag = false;
                        if (!getAllMists().isEmpty()) {
                            for (MapleMist mist : getAllMists()) {
                                if (mist != null) {
                                    if (!flag && !mist.isMobMist() && mist.isPoisonMist() == 2) {
                                        if (getCharacterIntersect(chr, mist.getBox())) {
                                            flag = true;
                                        }
                                    }
                                }
                            }
                        }
                        chr.setInvincible(flag);
                        //fishing - needs finishing
                        if (chr.getMapId() == 870000203 && chr.checkFish()) {
                            int baitId = (int) chr.getVarZero("BAIT");
                            if (baitId >= 4430001 && baitId <= 4430006) {
                                Item reward = spawnRandomReward(baitId);
                                if (chr.canHold(reward.getItemId(), reward.getQuantity())) {
                                    chr.gainItem(baitId, -1, "");
                                    long xp = 0;
                                    int stam = 1;
                                    switch (baitId) {
                                        case 4430001:
                                            xp = 1;
                                            break;
                                        case 4430002:
                                            xp = 10;
                                            break;
                                        case 4430003:
                                            xp = 100;
                                            break;
                                        case 4430004:
                                            xp = 1000;
                                            break;
                                        case 4430005:
                                            if (Randomizer.random(1, 99999) == 1) {
                                                chr.dropMessage(1, "You have found the Ultimate Supreme Kaotic Mega Elite Trash Skin!");
                                                chr.gainDamageSkin(6004);
                                            }
                                            xp = 10000;
                                            stam = 10;
                                            break;
                                        case 4430006:
                                            if (Randomizer.random(1, 9999) == 1) {
                                                chr.dropMessage(1, "You have found the Ultimate Supreme Kaotic Mega Elite Trash Skin!");
                                                chr.gainDamageSkin(6004);
                                            }
                                            xp = chr.getTotalLevel() * chr.getTotalLevel();
                                            stam = 100;
                                            break;
                                    }
                                    chr.gainLevelData(106, xp);
                                    chr.addOverflow(reward.getItemId(), reward.getQuantity(), "collect from fishing");
                                    //chr.dropMessage(-1, "+" + reward.getQuantity() + " " + MapleItemInformationProvider.getInstance().getName(reward.getItemId()) + " sent to Overflow (Total: " + StringUtil.getUnitNumber(chr.getOverflowAmount(reward.getItemId())) + ")");
                                    chr.getClient().announce(EffectPacket.showInfo("Effect/BasicEff.img/Gachapon/Open"));
                                    broadcastMessage(chr, EffectPacket.showForeignInfo(chr.getId(), "Effect/BasicEff.img/Gachapon/Open"), false);
                                    chr.getClient().announce(CField.playSound("Custom/CatchSuccess"));
                                    broadcastMessage(chr, CField.playSound("Custom/CatchSuccess"), false);
                                    //chr.gainStamina(1, true);
                                    //int ipot = (int) (1 * chr.getStat().getItemKpRate());
                                    //chr.dropTopMessage("You have gained " + ipot + " Kaotic Points");
                                    //chr.addVar("eDrop", ipot);
                                }
                            }
                        }
                    }
                }
                if (!getEventClear()) {
                    int time = (local ? 2 : 3);
                    if (numTimes % time == 0) {
                        if (!SpawnPoints.isEmpty() && allowSummons() && !locked) {
                            if (getEventInstance() == null) {
                                if (ballCount < 8) {
                                    int bChance = Randomizer.random(1, getEventInstance() != null ? 100 : 10);
                                    if (bChance <= 1) {
                                        SpawnPoint sp = getRandomMonsterSpawnPoint();
                                        if (sp.getMobTime() == 0) {
                                            spawnWildPalBall(2050, sp);
                                            ballCount++;
                                        }
                                    }
                                }
                                /*
                                if (eggSpawn == false && palTimer <= 0) {
                                    int chance = Randomizer.random(1, 25);
                                    if (chance == 1) {
                                        eggSpawn = true;
                                        int type = Randomizer.random(0, 8);
                                        MapleMonster egg = MapleLifeFactory.getKaoticMonster(2000 + type, 30, 1, false, false, false, 100);
                                        forceSpawnMonster(egg, getRandomMonsterSpawnPoint().getPosition());
                                        broadcastColorMessage(6, "Maple Pal Egg has appeared! Capture it!");
                                    }
                                }
                                 */
                            }
                            if (!park) {
                                if (spawnedMonstersOnMap.get() < spawnCap() && lock == 0) {
                                    if (getEventInstance() != null) {
                                        if (!getEventInstance().getDisposed()) {
                                            respawn();
                                        }
                                    } else {
                                        respawn();
                                    }
                                }
                            }
                        }
                    }
                }
                if (!getAllItems().isEmpty()) {
                    boolean isEmpty = getPlayers().isEmpty();
                    for (MapleMapItem item : getAllItems()) {
                        if (isEmpty || item.shouldExpire(System.currentTimeMillis())) {
                            item.expire(this);
                        } else if (item.shouldFFA(System.currentTimeMillis())) {
                            item.setDropType((byte) 2);
                        }
                    }
                }
            } else {
                if (canUnload(getId())) {
                    timeout++;
                    if (timeout > 300) {
                        clean(false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canUnload(int id) {
        switch (id) {
            case 910000000:
            case 910000001:
            case 910000023:
                return false;
        }
        if (id >= 870000000 && id < 870001000) {
            return false;
        }
        if (getEventInstance() == null) {
            return false;
        }
        return true;
    }

    public boolean isEndless() {
        return endless;
    }

    public void setEndless(boolean value) {
        endless = value;
    }

    public double getSkillRate() {
        return skillexp;
    }

    public void setSkillRate(double value) {
        skillexp = value;
    }

    public void setPark(boolean value) {
        park = value;
    }

    public boolean getPark() {
        return park;
    }

    public double getExpRate() {
        return expRate;
    }

    public void setExpRate(double value) {
        expRate = value;
    }

    public boolean isDisposable() {
        if (isTown()) {
            return false;
        }
        switch (this.getId()) {
            case 251010102:
            case 101030404:
            case 221040301:
            case 104000400:
            case 105090310:
            case 220050200:
            case 260010201:
            case 250010304:
            case 220050100:
            case 107000300:
            case 220050000:
            case 110040000:
            case 230020100:
            case 200010300:
            case 100040106:
            case 222010310:
            case 261030000:
            case 100040105:
            case 240040401:
            case 910000000:
            case 910000001:
            case 4000:
            case 910000023:
                return false;
        }
        if (this.getId() >= 870000000 && this.getId() < 870001000) {
            return false;
        }
        return true;
    }

    public int getBoost() {
        return boost;
    }

    public void addBoost(int value) {
        boost += value;
    }

    public void setKaotic(boolean value) {
        kaotic = value;
    }

    public void setScale(int value) {
        scale = value;
    }

    public void setBoosted(int value) {
        boosted = value;
    }

    public int getBoosted() {
        return boosted;
    }

    public void setLocal(boolean value) {
        local = value;
    }

    public boolean getLocal() {
        return local;
    }

    public MapleMonster getTotem(MapleCharacter chr) {
        if (totem.containsKey(chr)) {
            return totem.get(chr);
        } else {
            return null;
        }
    }

    public final MapleFootholdTree getFootholds() {
        return MapleFootholds;
    }

    public final List<SpawnPoint> getSpawnPoints() {
        return new ArrayList<>(SpawnPoints);
    }

    public int getSummonCount() {
        return spawnedSummonsOnMap.get();
    }

    public double getTotemType(MapleCharacter chr) {
        if (totem.containsKey(chr)) {
            MapleMonster mob = totem.get(chr);
            if (mob != null && mob.getOwner() == chr) {
                switch (mob.getId()) {
                    case 7://mirrior
                        return 5.0;
                    case 4://demon portal
                        return 4.0;
                    case 6://sword
                        return 3.0;
                    case 5://shadow
                        return 2.5;
                    case 1://frenzy
                        return 2.0;
                    case 2://whte
                        return 1.5;
                    case 3://blue
                        return 1.25;
                    default:
                        return 1.0;
                }
            }
        }
        return 1.0;
    }

    public double getTotemEtcType(MapleCharacter chr) {
        if (totem.containsKey(chr)) {
            MapleMonster mob = totem.get(chr);
            if (mob != null && mob.getOwner() == chr) {
                switch (mob.getId()) {
                    case 7://mirrior
                        return 500.0;
                    case 4://demon portal
                        return 250.0;
                    case 6://sword
                        return 200.0;
                    case 5://shadow
                        return 150.0;
                    case 1://frenzy
                        return 125.0;
                    default:
                        return 0.0;
                }
            }
        }
        return 0.0;
    }

    public void setTotem(MapleCharacter chr, MapleMonster mob) {
        totem.put(chr, mob);
    }

    public void removeTotem(MapleCharacter chr) {
        if (totem.containsKey(chr)) {
            MapleMonster removed = totem.remove(chr);
            if (removed != null && removed.getOwner() == chr) {
                killMonster(removed);
                updatePlayerStats();
            }
        }
    }

    public boolean allowSummons() {
        return allowSummons;
    }

    public void setSummons(boolean toggle) {
        allowSummons = toggle;
    }

    public List<SpawnPoint> getSpawnCount() {
        return SpawnPoints;
    }

    public int getPlayerCount() {
        return Collections.unmodifiableList(characters).size();
    }

    public final void setSpawns(final boolean fm) {
        this.isSpawns = fm;
    }

    public final boolean getSpawns() {
        return isSpawns;
    }

    public final void setEventClear(final boolean fm) {
        this.eventClear = fm;
    }

    public final boolean getEventClear() {
        return eventClear;
    }

    public final void setPQLock(final boolean fm) {
        this.pqlock = fm;
    }

    public final boolean getPQlock() {
        return pqlock;
    }

    public final void setFixedMob(int fm) {
        this.fixedMob = fm;
    }

    public final void setForceMove(int fm) {
        this.lvForceMove = fm;
    }

    public final int getForceMove() {
        return lvForceMove;
    }

    public final void setLevelLimit(int fm) {
        this.lvLimit = fm;
    }

    public final int getLevelLimit() {
        return lvLimit;
    }

    public final void setReturnMapId(int rmi) {
        this.returnMapId = rmi;
    }

    public final void setSoaring(boolean b) {
        this.soaring = b;
    }

    public final boolean canSoar() {
        return soaring;
    }

    public final void toggleDrops() {
        this.dropsDisabled = !dropsDisabled;
    }

    public final void setDrops(final boolean b) {
        this.dropsDisabled = b;
    }

    public final void toggleGDrops() {
        this.gDropsDisabled = !gDropsDisabled;
    }

    public final int getId() {
        return mapid;
    }

    public final MapleMap getReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
    }

    public ChannelServer getChannelMap() {
        return ChannelServer.getInstance(channel);
    }

    public final int getReturnMapId() {
        return returnMapId;
    }

    public final int getForcedReturnId() {
        return forcedReturnMap;
    }

    public final MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public final void setForcedReturnMap(final int map) {
        this.forcedReturnMap = map;
    }

    public final float getRecoveryRate() {
        return recoveryRate;
    }

    public final void setRecoveryRate(final float recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public final int getFieldLimit() {
        return fieldLimit;
    }

    public final void setFieldLimit(final int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public final void setCreateMobInterval(final short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public final void setTimeLimit(final int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public final void setMapName(final String mapName) {
        this.mapName = mapName;
    }

    public final String getMapName() {
        return mapName;
    }

    public final String getStreetName() {
        return streetName;
    }

    public final void setFirstUserEnter(final String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public final void setUserEnter(final String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public final String getFirstUserEnter() {
        return onFirstUserEnter;
    }

    public final String getUserEnter() {
        return onUserEnter;
    }

    public final boolean hasClock() {
        return clock;
    }

    public final void setClock(final boolean hasClock) {
        this.clock = hasClock;
    }

    public void setClear(boolean value) {
        clear = value;
    }

    public final boolean isTown() {
        return town;
    }

    public final void setTown(final boolean town) {
        this.town = town;
    }

    public final boolean allowPersonalShop() {
        return personalShop;
    }

    public final void setPersonalShop(final boolean personalShop) {
        this.personalShop = personalShop;
    }

    public final void setStreetName(final String streetName) {
        this.streetName = streetName;
    }

    public final void setEverlast(final boolean everlast) {
        this.everlast = everlast;
    }

    public final boolean getEverlast() {
        return everlast;
    }

    public final int getHPDec() {
        return decHP;
    }

    public final void setHPDec(final int delta) {
        if (delta > 0 || mapid == 749040100) { //pmd
            lastHurtTime = System.currentTimeMillis(); //start it up
        }
        decHP = (short) delta;
    }

    public final int getHPDecInterval() {
        return decHPInterval;
    }

    public final void setHPDecInterval(final int delta) {
        decHPInterval = delta;
    }

    public final int getHPDecProtect() {
        return protectItem;
    }

    public final void setHPDecProtect(final int delta) {
        this.protectItem = delta;
    }

    public final int getCurrentPartyId() {
        chrRLock.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter chr;
            while (ltr.hasNext()) {
                chr = ltr.next();
                if (chr.getParty() != null) {
                    return chr.getParty().getId();
                }
            }
        } finally {
            chrRLock.unlock();
        }
        return -1;
    }

    public int getUsableOID() {
        objectRLock.lock();
        try {
            return runningOid.incrementAndGet();
        } finally {
            objectRLock.unlock();
        }
    }

    public void addMapObject(final MapleMapObject mapobject) {
        int curOID = getUsableOID();
        objectWLock.lock();
        try {
            if (mapobject.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mapobject;
                if (!mob.isTotem()) {
                    if (!mob.isSummon()) {
                        spawnedMonstersOnMap.getAndIncrement();
                    } else {
                        spawnedSummonsOnMap.getAndIncrement();
                    }
                }
            }
            mapobject.setObjectId(curOID);
            this.mapobjects.put(curOID, mapobject);
        } finally {
            objectWLock.unlock();
        }
    }

    public void updateObject(MapleMapObject mapobject) {
        for (MapleCharacter chr : getAllPlayers()) {
            mapobject.sendDestroyData(chr.getClient());
            mapobject.sendSpawnData(chr.getClient());
        }
    }

    public void destoryObject(MapleMapObject mapobject) {
        for (MapleCharacter chr : getAllPlayers()) {
            mapobject.sendDestroyData(chr.getClient());
        }
    }

    private void spawnPersonalMapObject(final MapleMapObject mapobject, final MapleCharacter chr, final DelayedPacketCreation packetbakery) {
        try {
            if (chr != null) {
                addMapObject(mapobject);
                packetbakery.sendPackets(chr.getClient());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //updateObjects(mapobject);
    }

    private void spawnAndAddRangedMapObject(final MapleMapObject mapobject, final DelayedPacketCreation packetbakery) {
        try {
            addMapObject(mapobject);
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null) {
                    //chr.addVisibleMapObject(mapobject);
                    packetbakery.sendPackets(chr.getClient());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //updateObjects(mapobject);
    }

    private void spawnAndAddRangedMapObjectRevive(final MapleMapObject mapobject, final DelayedPacketCreation packetbakery) {
        try {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null) {
                    //chr.addVisibleMapObject(mapobject);
                    packetbakery.sendPackets(chr.getClient());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //updateObjects(mapobject);
    }

    public void spawnMonsterOnGround(MapleMonster mob, Point pos) {
        //spawnMonsterOnGroundBelow(mob, pos);
        mob.setPosition(pos);
        spawnMonster(mob);
    }

    public void spawnMonsterOnGround(MapleMonster mob, Point pos, boolean drops) {
        mob.setPosition(pos);
        if (!drops) {
            mob.disableDrops();
        }
        //spawnMonsterOnGroundBelow(mob, pos);
        spawnMonster(mob);
    }

    public boolean bossMap(int id) {
        switch (id) {
            case 925020002:
            case 78001:
                return true;
            default:
                return false;
        }
    }

    public void removeMapObject(int num) {
        objectWLock.lock();
        try {
            this.mapobjects.remove(Integer.valueOf(num));
        } finally {
            objectWLock.unlock();
        }
    }

    public void removeAllTotems() {
        for (MapleMonster obj : getAllTotems()) {
            obj.disposed = true;
            removeMapObject(obj, MapleMapObjectType.MONSTER);
        }
    }

    public void removeAllMonsters() {
        for (MapleMonster obj : getAllTrueMonsters()) {
            obj.disposed = true;
            removeMapObject(obj, MapleMapObjectType.MONSTER);
        }
    }

    public boolean ignoreNpc(int id) {
        switch (id) {
            case 9071005, 1061017 -> {
                return true;
            }
        }
        return false;
    }

    public void removeAllNpcsPark() {
        for (MapleNPC obj : getAllNPCs()) {
            if (!ignoreNpc(obj.getId())) {
                removeMapObject(obj, MapleMapObjectType.NPC);
            }
        }
    }

    public void removeAllNpcs() {
        for (MapleNPC obj : getAllNPCs()) {
            removeMapObject(obj, MapleMapObjectType.NPC);
        }
    }

    public void removeAllItems(boolean show) {
        if (!getAllItems().isEmpty()) {
            for (MapleMapItem obj : getAllItems()) {
                obj.pickedUp = true;
                obj.disposed = true;
                if (show) {
                    broadcastMessage(CField.removeItemFromMap(obj.getObjectId(), 0, 0));
                }
                removeMapObject(obj, MapleMapObjectType.ITEM);
            }
        }
    }

    public void removeMapObject(final MapleMapObject obj, MapleMapObjectType type) {
        if (obj.getType() == type) {
            if (obj.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) obj;
                if (!mob.isTotem()) {
                    if (!mob.isSummon()) {
                        spawnedMonstersOnMap.getAndDecrement();
                    } else {
                        spawnedSummonsOnMap.getAndDecrement();
                    }
                }
            } else {
                destoryObject(obj);
            }
            removeMapObject(obj.getObjectId());

        } else {
            System.out.println("Error with wrong object type:");
            Thread.dumpStack();
        }
    }

    public int getFH(Point initial) {
        final MapleFoothold fh = getFootholds().findBelow(initial);
        if (fh == null) {
            return 0;
        }
        return fh.getId();
    }

    public final Point calcPointBelow(Point initial) {
        if (getFootholds() == null) {
            return null;
        }
        initial.y -= 20;
        MapleFoothold fh = getFootholds().findBelow(initial);
        if (fh == null) {
            return initial;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY - 2);
    }

    public final Point calcItemPointBelow(Point initial) {
        if (getFootholds() == null) {
            return null;
        }
        initial.y -= 50;
        MapleFoothold fh = getFootholds().findBelow(initial);
        if (fh == null) {
            return initial;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY + 1);
    }

    public void setInstanced(boolean toggle) {
        instanced = toggle;
    }

    public boolean getInstanced() {
        return instanced;
    }

    public List<MonsterDropEntry> getShuffledDrops(List<MonsterDropEntry> list) {
        List<MonsterDropEntry> tempEntry = new ArrayList<>(list);
        Collections.shuffle(tempEntry);
        return tempEntry;
    }

    public int setAmount(int amount) {
        return Randomizer.MinMax(amount, 1, 30000);
    }

    private void dropFromMonster(final MapleCharacter chr, final MapleMonster mob, final boolean instanced, List<MonsterDropEntry> dropEntry) {
        if (mob == null || chr == null || ChannelServer.getInstance(channel) == null || mob.isSummon()) { //no drops in pyramid ok? no cash either
            return;
        }
        if (dropsDisabled || mob.dropsDisabled() || chr.getPyramidSubway() != null || (mob.getAttackers() != null && mob.getAttackers().isEmpty())) {
            return;
        }
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final int mobpos = mob.getTruePosition().x;
            byte d = 1;
            Point pos = new Point(0, mob.getPosition().y);
            double showdown = 100.0;
            final MonsterStatusEffect mse = mob.getBuff(MonsterStatus.SHOWDOWN);
            int mobScale = (int) mob.getStats().dropTier;

            if (mse != null) {
                showdown += mse.getX();
            }
            final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
            double basedrop;
            int orbz = 0;
            int mpts = 0;
            int max = mob.getStats().getMega() ? 4 : 1;
            boolean global = true;
            boolean explode = mob.getStats().isExplosiveReward();
            if (mob.getId() >= 2050 && mob.getId() <= 2099) { //PAL EGG CODE
                global = false;
            }
            double pMobBonus = (mob.getAttackers().size() * 0.25) - 0.25;
            for (MapleCharacter player : mob.getAttackers()) {
                if (player != null && player.isAlive() && player.getMap() == mob.getMap()) {
                    int eqpz = 0;
                    int etcBuff = (int) (1 + player.getETCMod());
                    List<Item> loot = new ArrayList<>();
                    List<Item> etcloot = new ArrayList<>();
                    //Map<Integer, Integer> droploot = new ConcurrentHashMap<Integer, Integer>();
                    double itemTotalAmount = 1.0 + (player.getStat().getItemKpRate() + player.getETCMod());
                    if (global) {
                        if (explode && mob.getStats().getTrueBoss()) {
                            double range = Randomizer.randomDouble(1.0, itemTotalAmount);
                            loot.add(new Item(4310296, (byte) 0, (short) Randomizer.random(1, (int) (mobScale * range)), (byte) 0));
                            loot.add(new Item(4310018, (byte) 0, (short) (Randomizer.random(mobScale, (int) (Math.pow(mobScale, 1.1) * range))), (byte) 0));
                            loot.add(new Item(4001895, (byte) 0, (short) (Randomizer.random(mobScale, (int) (Math.pow(mobScale, 1.2) * range))), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                            loot.add(new Item(4310500, (byte) 0, (short) (Randomizer.random(mobScale, (int) (Math.pow(mobScale, 1.25) * range))), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                            if (mob.getStats().getMega() && Randomizer.random(1, 25) == 1) {
                                loot.add(new Item(2000012, (byte) 0, (short) 1, (byte) 0));
                            }
                        }
                    }
                    basedrop = (int) (player.getDropMod() * ((double) showdown / 100.0));
                    int equips = 1;
                    if (!dropEntry.isEmpty()) {
                        for (final MonsterDropEntry de : explode ? getShuffledDrops(dropEntry) : dropEntry) {
                            if (de == null) {
                                continue;
                            }
                            if (de.itemId / 10000 == 431 && explode) {
                                int amount = Randomizer.random((int) (de.Minimum), (int) (de.Maximum * itemTotalAmount));
                                while (amount > 30000) {
                                    loot.add(new Item(de.itemId, (byte) 0, (short) (30000), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                    amount -= 30000;
                                }
                                if (amount > 0) {
                                    loot.add(new Item(de.itemId, (byte) 0, (short) (amount), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                }
                            } else {
                                int dropR = Randomizer.nextInt(10000000);
                                int chance = (int) (Randomizer.LongMax((long) (de.chance * basedrop), 10000000));
                                if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                                    if (!explode) {
                                        continue;
                                    }
                                    dropR = 100;
                                    chance = 100;
                                }
                                if (de.rare > 0) {
                                    chance = de.chance;
                                }
                                if (dropR <= chance) {
                                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                                        if (!player.equipDrops || eqpz >= max) {
                                            continue;
                                        }
                                        if (!explode && player.getTotalLevel() < ii.getReqLevel(de.itemId)) {
                                            continue;
                                        }
                                        if (explode || player.getLoot()) {
                                            Item idrop = ii.randomizeMonsterStats(mob, player, (Equip) ii.getEquipById(de.itemId), mobScale, mobScale, explode);
                                            loot.add(idrop);
                                        } else {
                                            orbz += mobScale;
                                            if (ii.isCash(de.itemId)) {
                                                mpts++;
                                            }
                                        }
                                        equips++;
                                        eqpz++;
                                    } else {
                                        int amount = Randomizer.random(de.Minimum, de.Maximum);
                                        if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.ETC || GameConstants.getInventoryType(de.itemId) == MapleInventoryType.USE) {
                                            amount = Randomizer.random((int) (de.Minimum), (int) (de.Maximum * itemTotalAmount));
                                        }
                                        if (GameConstants.isPet(de.itemId)) {
                                            if (player.canHold(de.itemId)) {
                                                player.dropColorMessage(10, "You have obtained new a pet!");
                                                player.gainItem(de.itemId, 1, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)");
                                            }
                                        } else {
                                            //System.out.println("test: " + amount + " - boosted: " + boosted);
                                            //System.out.println("testBonus: " + itemBonus + " - boosted: " + boosted);
                                            //System.out.println("testTotal: " + itemTotalAmount + " - boosted: " + boosted);
                                            while (amount > 30000) {
                                                loot.add(new Item(de.itemId, (byte) 0, (short) (30000), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                                amount -= 30000;
                                            }
                                            if (amount > 0) {
                                                loot.add(new Item(de.itemId, (byte) 0, (short) (amount), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Global Drops
                    if (player.getGlobal()) {
                        if (global) {
                            for (final MonsterGlobalDropEntry de : explode ? mi.getShuffledGlobalDrop() : mi.getGlobalDrop()) {
                                if (de == null) {
                                    continue;
                                }
                                if (de.chance == 0) {
                                    continue;
                                }
                                if (de.itemId / 10000 == 431 && explode) {
                                    int amount = Randomizer.random((int) (de.Minimum), (int) (de.Maximum * itemTotalAmount));
                                    while (amount > 30000) {
                                        loot.add(new Item(de.itemId, (byte) 0, (short) (30000), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                        amount -= 30000;
                                    }
                                    if (amount > 0) {
                                        loot.add(new Item(de.itemId, (byte) 0, (short) (amount), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                    }
                                } else {
                                    int dropR = Randomizer.nextInt(1000000);
                                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                                        if (!explode) {
                                            continue;
                                        }
                                        dropR = Randomizer.nextInt((int) Randomizer.LongMax((long) (1000000 * Math.pow(equips, 2)), 2000000000));
                                    }
                                    int chance = (int) (Randomizer.Max((int) (de.chance * basedrop), dropR));
                                    if (dropR <= chance) {
                                        if (!gDropsDisabled) {
                                            if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                                                if (!player.equipDrops || eqpz >= 1) {
                                                    continue;
                                                }
                                                if (explode || player.getLoot()) {
                                                    Item idrop = ii.randomizeMonsterStats(mob, player, (Equip) ii.getEquipById(de.itemId), mobScale, mobScale, explode);
                                                    loot.add(idrop);
                                                } else {
                                                    orbz += mobScale;
                                                    if (ii.isCash(de.itemId)) {
                                                        mpts++;
                                                    }
                                                }
                                                equips++;
                                                eqpz++;
                                            } else {
                                                int amount = Randomizer.random(de.Minimum, de.Maximum);
                                                if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.ETC || GameConstants.getInventoryType(de.itemId) == MapleInventoryType.USE) {
                                                    amount = Randomizer.random((int) (de.Minimum), (int) (de.Maximum * itemTotalAmount));
                                                }
                                                if (GameConstants.isPet(de.itemId)) {
                                                    if (player.canHold(de.itemId)) {
                                                        player.dropColorMessage(10, "You have obtained new a pet!");
                                                        player.gainItem(de.itemId, 1, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)");
                                                    }
                                                } else {
                                                    while (amount > 30000) {
                                                        loot.add(new Item(de.itemId, (byte) 0, (short) (30000), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                                        amount -= 30000;
                                                    }
                                                    if (amount > 0) {
                                                        loot.add(new Item(de.itemId, (byte) 0, (short) (amount), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            for (final MonsterGlobalRareDropEntry de : explode ? mi.getShuffledRareDrop() : mi.getGlobalRareDrop()) {
                                if (de == null) {
                                    continue;
                                }
                                if (de.chance == 0) {
                                    continue;
                                }
                                if (!explode) {
                                    if (player.getTotalLevel() < ii.getReqLevel(de.itemId)) {
                                        continue;
                                    }
                                }
                                if (de.itemId / 10000 == 431 && explode) {
                                    int amount = (int) (Randomizer.random((int) (de.Minimum), (int) (de.Maximum * itemTotalAmount)));
                                    loot.add(new Item(de.itemId, (byte) 0, (short) (setAmount(amount * boosted)), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                } else {
                                    int chance = de.chance;
                                    if (Randomizer.nextInt(1000000) <= chance) {
                                        if (de.itemId == 4031034 && mob.getMonsterEventType() == 2) {
                                            continue;
                                        }
                                        if (!gDropsDisabled) {
                                            if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                                                if (!explode) {
                                                    continue;
                                                }
                                                if (!player.equipDrops || eqpz >= 1) {
                                                    continue;
                                                }
                                                if (explode || player.getLoot()) {
                                                    Item idrop = ii.randomizeMonsterStats(mob, player, (Equip) ii.getEquipById(de.itemId), mobScale, mobScale, explode);
                                                    loot.add(idrop);
                                                } else {
                                                    orbz += mobScale;
                                                    if (ii.isCash(de.itemId)) {
                                                        mpts++;
                                                    }
                                                }
                                                equips++;
                                                eqpz++;
                                            } else {
                                                int amount = Randomizer.random(de.Minimum, de.Maximum);
                                                if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.ETC || GameConstants.getInventoryType(de.itemId) == MapleInventoryType.USE) {
                                                    amount = (int) Math.floor(Randomizer.random((int) (de.Minimum), (int) (de.Maximum * itemTotalAmount)));
                                                }
                                                if (GameConstants.isPet(de.itemId)) {
                                                    if (player.canHold(de.itemId)) {
                                                        player.dropColorMessage(10, "You have obtained new a pet!");
                                                        player.gainItem(de.itemId, 1, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)");
                                                    }
                                                } else {
                                                    while (amount > 30000) {
                                                        loot.add(new Item(de.itemId, (byte) 0, (short) (30000), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                                        amount -= 30000;
                                                    }
                                                    if (amount > 0) {
                                                        loot.add(new Item(de.itemId, (byte) 0, (short) (amount), (byte) 0, "Dropped from monster " + mob.getId() + " on " + mapid + " (Global)"));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    int qId = (int) player.getVar("wanted_item");
                    int qAmount = (int) player.getVar("wanted_item_amount");
                    int drops = 0;
                    if (!loot.isEmpty()) {
                        boolean autoloot = false;
                        if (player.permVac) {
                            if (!explode) {
                                if (player.getAccVara("Perm_Vac") > 0) {
                                    autoloot = true;
                                }
                            }
                        }
                        Collections.shuffle(loot);
                        int x = (0 - (loot.size() / 2)) * 25;
                        for (Item item : loot) {
                            if (autoloot) {
                                if (!GameConstants.isOverFlowable(item.getItemId())) {
                                    autoloot = false;
                                }
                            }
                            if (!autoloot) {
                                pos.x = mob.getPosition().x + x;
                                spawnPersonalMobDrop(item, calcItemPointBelow(checkMapEdge(pos)), mob, player, (byte) (explode && mob.getStats().getTrueBoss() ? 3 : 0), 0, false);
                                x += 25;
                            } else {
                                if (qId > 0 && qId == item.getItemId()) {
                                    long iCount = player.countAllItem(item.getItemId());
                                    if (iCount <= qAmount) {
                                        player.dropMidMessage("Collected Wanted: " + item.getItemName(item.getItemId()) + " " + StringUtil.getUnitNumber(iCount + item.getQuantity()) + " / " + StringUtil.getUnitNumber(qAmount) + "");
                                    }
                                }
                                int dItemA = item.getQuantity();
                                item.setQuantity((short) (Randomizer.Max(dItemA * 2, 30000)));
                                etcloot.add(item);
                            }
                        }

                        drops = loot.size();
                        loot.clear();

                    }
                    if (orbz > 0) {
                        etcloot.add(new Item(4310066, (byte) 0, (short) (orbz), (byte) 0, "auto converted from equips from drop."));
                    }
                    if (mpts > 0) {
                        etcloot.add(new Item(4310501, (byte) 0, (short) (mpts), (byte) 0, "auto converted from equips from drop."));
                    }
                    if (!etcloot.isEmpty()) {
                        player.storeDropEtc(etcloot);
                        etcloot.clear();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeMonster(final MapleMonster monster, boolean animate) {
        if (monster == null) {
            return;
        }
        if (monster.getStats().getBar() || monster.getStats().getHPDisplayType() == 0) {
            broadcastMessage(MobPacket.clearBossHP(monster), monster.getPosition());
        }
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
        removeMapObject(monster, MapleMapObjectType.MONSTER);
        monster.killed();
    }

    public void killMonster(final MapleMonster monster) { // For mobs with removeAfter
        killMonster(monster, 1, 0);
    }

    public void killMonster(final MapleMonster monster, final int animation) { // For mobs with removeAfter
        killMonster(monster, animation, 0);
    }

    public void forceKillMonster(final MapleMonster monster) { // For mobs with removeAftera
        if (monster == null) {
            return;
        }
        monster.cancelAllBuffs();
        monster.setHp(0);
        if (monster.getStats().getBar() || monster.getStats().getHPDisplayType() == 0) {
            broadcastMessage(MobPacket.clearBossHP(monster), monster.getPosition());
        }
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), (byte) 1), monster.getPosition());
        removeMapObject(monster, MapleMapObjectType.MONSTER);
        monster.killed();
    }

    public void killMonster(final MapleMonster monster, final int animation, int check) { // For mobs with removeAftera
        if (monster == null) {
            return;
        }
        if ((monster.getId() == 8810122 || monster.getId() == 8810018)) {
            System.out.println("HT mob id: " + monster.getId());//8810018
        }
        monster.cancelAllBuffs();
        monster.setHp(0);
        if (monster.getStats().getBar() || monster.getStats().getHPDisplayType() == 0) {
            broadcastMessage(MobPacket.clearBossHP(monster), monster.getPosition());
        }
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), (byte) animation), monster.getPosition());
        if (!monster.getStats().getRevives().isEmpty()) {
            monster.spawnRevives(monster.getMap(), monster);
        }
        removeMapObject(monster, MapleMapObjectType.MONSTER);
        monster.killed();
    }

    public boolean htCheck() {
        int count = 0;
        for (int i = 8810010; i < 8810017; i++) {
            if (countMonsterById(i) > 0) {
                count++;
            }
        }
        return count >= 8;
    }

    public final void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean second, int animation) {
        if (monster != null) {

            try {
                MapleMonsterStats stat = monster.getStats();
                monster.killBy(chr);
                monster.cancelAllBuffs();
                removeMapObject(monster, MapleMapObjectType.MONSTER);
                if (monster.getId() == 8820014) { //pb sponge, kills pb(w) first before dying
                    killMonster(8820000);
                } else if (monster.getId() == 9300166) { //ariant pq bomb
                    animation = 2; //or is it 3?
                }
                if (!getClear() && monster.getId() == 9601175 && monster.getEventInstance() != null) {
                    int count = getAllReactor().size();
                    for (MapleReactor r : getAllReactor()) {
                        if (r.getState() == 0 && r.getPosition().distanceSq(monster.getPosition()) < 12500) {
                            r.forceHitReactor((byte) 1);
                        }
                        if (r.getState() == 1) {
                            count--;
                            if (count == 0) {
                                showClear();
                            }
                            broadcastMapMsg("Torch has been lit. " + count + " Torches remaining.", 5120205);
                        }
                    }
                }
                if (monster.getBuffToGive() > -1) {
                    final int buffid = monster.getBuffToGive();
                    final MapleStatEffect buff = MapleItemInformationProvider.getInstance().getItemEffect(buffid);

                    for (final MapleCharacter mc : getAllPlayers()) {
                        if (mc != null && mc.isAlive()) {
                            buff.applyTo(mc);

                            switch (monster.getId()) {
                                case 8810018:
                                case 8810122:
                                case 8820001:
                                    mc.getClient().announce(EffectPacket.showOwnBuffEffect(buffid, 13, mc.getLevel(), 1)); // HT nine spirit
                                    broadcastMessage(mc, EffectPacket.showBuffeffect(mc.getId(), buffid, 13, mc.getLevel(), 1), false); // HT nine spirit
                                    break;
                            }
                        }
                    }
                }
                final int mobid = monster.getId();
                ExpeditionType type = null;
                if (mobid == 8810018 && mapid == 240060200) { // Horntail
                    //World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!"));
                    //FileoutputUtil.log(FileoutputUtil.Horntail_Log, MapDebug_Log());
                    if (speedRunStart > 0) {
                        type = ExpeditionType.Horntail;
                    }
                    //doShrine(true);
                } else if (mobid == 8810122 && mapid == 240060201) { // Horntail
                    //World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "To the crew that have finally conquered Chaos Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!"));
//            FileoutputUtil.log(FileoutputUtil.Horntail_Log, MapDebug_Log());
                    if (speedRunStart > 0) {
                        type = ExpeditionType.ChaosHT;
                    }
                    //doShrine(true);
                } else if (mobid == 9400266 && mapid == 802000111) {
                    //doShrine(true);
                } else if (mobid == 9400265 && mapid == 802000211) {
                    //doShrine(true);
                } else if (mobid == 9400270 && mapid == 802000411) {
                    //doShrine(true);
                } else if (mobid == 9400273 && mapid == 802000611) {
                    //doShrine(true);
                } else if (mobid == 9400294 && mapid == 802000711) {
                    //doShrine(true);
                } else if (mobid == 9400296 && mapid == 802000803) {
                    //doShrine(true);
                } else if (mobid == 9400289 && mapid == 802000821) {
                    //doShrine(true);
                    //INSERT HERE: 2095_tokyo
                } else if (mobid == 8830000 && mapid == 105100300) {
                    if (speedRunStart > 0) {
                        type = ExpeditionType.Normal_Balrog;
                    }
                } else if ((mobid == 9420544 || mobid == 9420549) && mapid == 551030200 && monster.getEventInstance() != null) {
                    //doShrine(getAllReactor().isEmpty());
                } else if (mobid == 8820001 && mapid == 270050100) {
                    //World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "Oh, the exploration team who has defeated Pink Bean with undying fervor! You are the true victors of time!"));
                    if (speedRunStart > 0) {
                        type = ExpeditionType.Pink_Bean;
                    }
                    //doShrine(true);
                } else if (mobid == 8850011 && mapid == 274040200) {
                    //World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "To you whom have defeated Empress Cygnus in the future, you are the heroes of time!"));
                    if (speedRunStart > 0) {
                        type = ExpeditionType.Cygnus;
                    }
                    //doShrine(true);
                } else if (mobid == 8840000 && mapid == 211070100) {
                    if (speedRunStart > 0) {
                        type = ExpeditionType.Von_Leon;
                    }
                    //doShrine(true);
                } else if (mobid == 8800002 && mapid == 280030000) {
//            FileoutputUtil.log(FileoutputUtil.Zakum_Log, MapDebug_Log());
                    //doShrine(true);
                } else if (mobid == 8800102 && mapid == 280030001) {
                    //FileoutputUtil.log(FileoutputUtil.Zakum_Log, MapDebug_Log());
                    if (speedRunStart > 0) {
                        type = ExpeditionType.Chaos_Zakum;
                    }

                    //doShrine(true);
                } else if (mobid >= 8800003 && mobid <= 8800010) {
                    boolean makeZakReal = true;
                    final List<MapleMonster> monsters = getAllMonsters();

                    for (final MapleMonster mons : monsters) {
                        if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                            makeZakReal = false;
                            break;
                        }
                    }
                    if (makeZakReal) {
                        for (final MapleMonster mons : monsters) {
                            if (mons.getId() == 8800000) {
                                final Point pos = mons.getTruePosition();
                                this.killAllMonsters(true);
                                MapleMonster nMob = MapleLifeFactory.getMonster(8800000);
                                nMob.getStats().disableRevives();
                                nMob.setEventScript(true);
                                spawnMonsterOnGroundBelow(nMob, pos);
                                break;
                            }
                        }
                    }
                } else if (mobid >= 8800103 && mobid <= 8800110) {
                    boolean makeZakReal = true;
                    final List<MapleMonster> monsters = getAllMonsters();

                    for (final MapleMonster mons : monsters) {
                        if (mons.getId() >= 8800103 && mons.getId() <= 8800110) {
                            makeZakReal = false;
                            break;
                        }
                    }
                    if (makeZakReal) {
                        for (final MapleMonster mons : monsters) {
                            if (mons.getId() == 8800100) {
                                final Point pos = mons.getTruePosition();
                                this.killAllMonsters(true);
                                spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800100, mons.getStats().getLevel(), mons.getStats().getScale()), pos);
                                break;
                            }
                        }
                    }
                } else if (mobid == 8820008) { //wipe out statues and respawn
                    for (MapleMonster mons : getAllMonsters()) {
                        if (mons.getLinkOid() != monster.getObjectId()) {
                            killMonster(mons, chr, false, false, animation);
                        }
                    }
                } else if (mobid >= 8820010 && mobid <= 8820014) {
                    for (MapleMonster mons : getAllMonsters()) {
                        if (mons.getId() != 8820000 && mons.getId() != 8820001 && mons.getObjectId() != monster.getObjectId() && mons.isAlive() && mons.getLinkOid() == monster.getObjectId()) {
                            killMonster(mons, chr, false, false, animation);
                        }
                    }
                } else if (getParkMonsters().isEmpty()) {//MP events
                    if (park) {
                        if (endless) {
                            var eim = getEventInstance();
                            if (eim != null) {
                                if (!clear) {
                                    clear = true;
                                    eim.processClear(this);
                                }
                            }
                        } else {
                            switch ((chr.getMapId() % 1000) / 100) {
                                case 0:
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    if (this.getFlag("master") && this.getFlag("spawned")) {
                                        clear = true;
                                    }
                                    broadcastMessage(chr, CField.MapEff("monsterPark/clear"), true);
                                    break;
                                case 5:
                                    if (this.getFlag("master")) {
                                        if (this.getFlag("spawned")) {
                                            clear = true;
                                            broadcastMessage(chr, CField.MapEff("monsterPark/clear"), true);
                                        }
                                    } else {
                                        if (chr.getEventInstance() != null) {
                                            chr.getEventInstance().MPvictory();
                                        }
                                        if (chr.achievementFinished(200) && chr.achievementFinished(201) && chr.achievementFinished(202) && chr.achievementFinished(203) && chr.achievementFinished(204)) {
                                            chr.finishAchievement(205);
                                        }
                                        if (chr.achievementFinished(210) && chr.achievementFinished(211) && chr.achievementFinished(212) && chr.achievementFinished(213) && chr.achievementFinished(214) && chr.achievementFinished(215)) {
                                            chr.finishAchievement(216);
                                        }
                                        if (chr.achievementFinished(220) && chr.achievementFinished(221) && chr.achievementFinished(222) && chr.achievementFinished(223) && chr.achievementFinished(224) && chr.achievementFinished(225)) {
                                            chr.finishAchievement(226);
                                        }
                                        boolean clear = true;
                                        for (int i = 250; i < 266; i++) {
                                            if (!chr.achievementFinished(i)) {
                                                clear = false;
                                                break;
                                            }
                                        }
                                        if (clear) {
                                            chr.finishAchievement(267);
                                            chr.levelUp(false);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
                if (chr.getMapId() >= 46000 && chr.getMapId() < 47000) {
                    if (getParkMonsters().isEmpty()) {
                        broadcastMessage(chr, CField.MapEff("monsterPark/clear"), true);
                    }
                }

                boolean gollux = false;
                switch (this.getId()) {
                    case 863010100://base
                    case 863010200://leg
                    case 863010210://leg
                    case 863010220://leg
                    case 863010230://leg
                    case 863010300://left
                    case 863010310://left
                    case 863010320://left
                    case 863010400://right
                    case 863010410://right
                    case 863010420://right
                        gollux = true;
                }
                if (gollux) {
                    if (getParkMonsters().isEmpty()) {
                        broadcastMessage(chr, CField.MapEff("monsterPark/clear"), true);
                        var eim = getEventInstance();
                        if (eim != null) {
                            eim.dropMessage(6, "All monsters have been defeated, proceed to next stage.");
                            eim.setValue("clear", eim.getValue("clear") + 1);
                            clear = true;
                        }
                    }
                }
                if (chr.getMapId() == 2003) {
                    if (getParkMonsters().isEmpty()) {
                        broadcastMessage(chr, CField.MapEff("monsterPark/clear"), true);
                        if (getEventInstance() != null) {
                            getEventInstance().dropMessage(6, "The Castle has been purged of all monsters and switches!");
                            getEventInstance().dropMessage(6, "Head to final gate at the end to claim your reward!");
                            clear = true;
                        }
                    }
                }
                if (stat.getRevives().isEmpty()) {
                    if (!monster.dropsDisabled()) {
                        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
                        final List<MonsterDropEntry> dropEntry = mi.retrieveDrop(monster.getId());
                        if (dropEntry != null && !dropEntry.isEmpty()) {
                            dropFromMonster(chr, monster, instanced, dropEntry);
                        }
                        monster.getAttackers().clear();
                    }
                }
                if (chr != null && chr.isAlive()) {
                    if (chr.getBuffedValue(MapleBuffStat.REAPER) != null) {
                        int skillLevel = chr.getSkillLevel(32111006);
                        if (chr.getSummonsSize() < (Randomizer.Min(skillLevel / 10, 1))) {
                            //System.out.println("spawns: " + chr.getSummonsSize());
                            final MapleSummon tosummon = new MapleSummon(chr, 32111006, skillLevel, new Point(monster.getPosition()), SummonMovementType.WALK_STATIONARY);
                            spawnSummon(tosummon);
                            chr.addSummon(tosummon);
                            tosummon.addHP((short) (skillLevel * 100));
                        }
                    }
                    chr.addBattleExp(1);

                    if (monster.getId() == 9302038) {
                        Item idrop = new Item(4280002, (byte) 0, (short) 1, (byte) 0);
                        if (MapleInventoryManipulator.addFromDrop(chr.getClient(), idrop, false)) {
                            chr.dropMessage("You have gained a Golden Chest!");
                        } else {
                            spawnPersonalMobDrop(idrop, calcDropPos(monster.getPosition(), monster.getTruePosition()), monster, chr, (byte) 3, 0, false);
                        }
                    }
                }
                //questKill(chr, monster);
                monster.killed();
                if (getAllMonstersOnMapDead() && getEventInstance() != null) {
                    getEventInstance().allMobKilled();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isEndlessMap(int id) {
        return Math.floor(id / 10000000) == 95;
    }

    public void resetBGM() {
        if (tempBgm != null && !tempBgm.isEmpty()) {
            bgm = tempBgm;
        }
    }

    public void questKill(MapleCharacter chr, MapleMonster mob) {
        //abodeon quest chain
        if (chr.getVar("stumpy") > 0) {
            if (chr.getVar("stumpy") == 1 && chr.getVar("wolf") < 9999 && mob.getId() == 9390931) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Wolf killed Complete!");
                    chr.setVar("stumpy", 2);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Wolf killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }
            if (chr.getVar("stumpy") == 3 && chr.getVar("wolf") < 9999 && mob.getId() == 9390934) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Black Wolf killed Complete!");
                    chr.setVar("stumpy", 4);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Wolf killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }
            if (chr.getVar("stumpy") == 6 && chr.getVar("wolf") < 9999 && mob.getId() == 9390939) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Killed Kobolds Complete!");
                    chr.setVar("stumpy", 7);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Kobold killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }
            if (chr.getVar("stumpy") == 9 && chr.getVar("wolf") < 9999 && mob.getId() == 9390912) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Killed Slicer Kobolds Complete!");
                    chr.setVar("stumpy", 10);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Kobold Slicer killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }
            if (chr.getVar("stumpy") == 12 && chr.getVar("wolf") < 9999 && mob.getId() == 9390941) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Killed Poker Kobolds Complete!");
                    chr.setVar("stumpy", 13);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Poker Kobold killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }

            if (chr.getVar("stumpy") == 15 && chr.getVar("wolf") < 9999 && mob.getId() == 9390914) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Killed Shooter Kobolds Complete!");
                    chr.setVar("stumpy", 16);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Shooter Kobold killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }

            if (chr.getVar("stumpy") == 17 && chr.getVar("wolf") < 9999 && mob.getId() == 9390933) {
                chr.addVar("wolf", 1);
                if (chr.getVar("wolf") >= 9999) {
                    chr.dropTopMessage("Killed Zapper Kobolds Complete!");
                    chr.setVar("stumpy", 18);
                    chr.setVar("wolf", 0);
                } else {
                    chr.dropTopMessage("Zapper Kobold killed: " + chr.getVar("wolf") + "/9999");
                }
                return;
            }
        }
        if (chr.getVar("abreon") == 1 && chr.getVar("wolf") < 9999 && mob.getId() == 9390943) {
            chr.addVar("wolf", 1);
            if (chr.getVar("wolf") >= 9999) {
                chr.dropTopMessage("Killed Disgusting Kobolds Complete!");
                chr.setVar("abreon", 2);
                chr.setVar("wolf", 0);
            } else {
                chr.dropTopMessage("Disgusting Kobold killed: " + chr.getVar("wolf") + "/9999");
            }
            return;
        }
    }

    public boolean isRaidBoss(int id) {
        switch (id) {
            case 8520000, 8510000, 9400300, 8800000, 9420522, 8840000, 8820001, 8800400, 9400632, 8220011, 8500022, 8900102, 8900101, 8900100, 8500002, 8220012, 8910000, 9420549, 8860000, 8920001, 8850011, 8641010, 8820212, 8810018, 8642016, 8930100, 9601068, 8240099, 8644011, 8641059, 8880000, 8880101, 8220104, 9420620, 8880150, 8880302, 8880502, 8880404, 8880403, 8880410, 9400551, 8880405, 8645009, 2600800 -> {
                return true;
            }
        }
        return false;
    }

    public boolean processEvent() {
        if (!eventlocked && getEventInstance() == null && getId() != 5001 && specialEvent == 0) {
            if (!locked) {
                specialEvent(Randomizer.random(1, 6));
                return true;
            }
        }
        return false;
    }

    public boolean processEvent(int id) {
        if (!eventlocked && getEventInstance() == null && getId() != 5001 && specialEvent == 0) {
            if (!locked) {
                specialEvent(id);
                return true;
            }
        }
        return false;
    }

    public boolean isMPBossbyId(int id) {
        switch (id) {
            case 9800003://MP - metal golem
            case 9800008://MP - spirit of rock
            case 9800009://MP - crisom of rock
            case 9800016://MP - snow witch
            case 9800022://MP - 
            case 9800023://MP - 
            case 9800024://MP - 
            case 9800025://MP - 
            case 9800031://MP - 
            case 9800037://MP - 
            case 9800038://MP - 
            case 9800044://MP - 
            case 9800048://MP - 
            case 9800050://MP - 
            case 9800056://MP - 
            case 9800057://MP - 
            case 9800058://MP - 
            case 9800060://MP - 
            case 9800063://MP - 
            case 9800065://MP -
            case 9800066://MP - 
            case 9800072://MP - 
            case 9800075://MP - 
            case 9800076://MP - 
            case 9800077://MP - 
            case 9800082://MP - 
            case 9800083://MP - 
            case 9800084://MP - 
            case 9800090://MP - 
            case 9800091://MP - 
            case 9800099://MP - 
            case 9800105://MP - 
            case 9800108://MP - 
            case 9800109://MP - 
            case 9800113://MP - 
            case 9800119://MP - 
            case 9800120://MP - 
            case 9800122://MP - 
            case 9800123://MP - 
                return true;
        }
        return false;
    }

    public boolean isMPFinalBossbyId(int id) {
        switch (id) {
            //tier 1
            case 9800003://Metal Golem
            case 9800009://Crimson Rock
            case 9800016://Snow Witch
            case 9800024://Seruf
            case 9800025://Seruf
            case 9800031://Balrog
            case 9800037://ghost 
            case 9800044://Zeno
            case 9800050://deet
            case 9800056://Golem phase 1
            case 9800057://Golem phase 2
            case 9800058://Golem phase 3
            case 9800066://Centipede
            case 9800072://Thanos
            case 9800084://Airship
            case 9800091://Egos
            case 9800099://Ani
            case 9800105://Levi 
            case 9800113://Lyka
            case 9800124://CK boss
                return true;
        }
        return false;
    }

    public final void spawnRevives(List<Integer> toSpawn, MapleMonster parent) {
        try {
            if (getEventClear()) {
                return;
            }
            MapleMonster spongy = null;
            int level = parent.getStats().getLevel();
            int mobscale = parent.getStats().getTier();
            Point pos = parent.getPosition();
            boolean localKaotic = parent.isLocalKaotic();
            boolean superKaotic = parent.getStats().superKaotic();
            pos.y--;
            boolean isKaotic = parent.getStats().getKaotic();
            if (parent.getId() == 8880100 || parent.getId() == 8880300 || parent.getId() == 8880301) {
                if (!isKaotic) {
                    mobscale += 1;
                }
            }
            if (parent.getId() == 8220100 || parent.getId() == 8220101 || parent.getId() == 8220102 || parent.getId() == 8220103) {
                if (!isKaotic) {
                    mobscale += 1;
                }
            }
            if (parent.getId() >= 9800000 && parent.getId() <= 9800125) {//monster Park revives
                for (final int i : toSpawn) {
                    MapleMonster mob;
                    if (isKaotic) {
                        if (isMPBossbyId(i)) {
                            mob = MapleLifeFactory.getKaoticMonster(i, level, mobscale, true, true, false, parent.getStats().getHits());
                        } else {
                            mob = MapleLifeFactory.getKaoticMonster(i, level, mobscale, false, true, false);
                        }
                    } else {
                        mob = MapleLifeFactory.getMonsterNoDrops(i, level, mobscale);
                    }
                    if (mob != null) {
                        mob.spawning += (parent.getAnimationTime("die1") + 250);
                        mob.setPosition(pos);
                        mob.setBabyTime(parent.getAnimationTime("die1"));
                        mob.getStats().setBar(parent.getStats().getBar());
                        spawnMonster(mob, -2);
                    }
                }
                return;
            }
            if (parent.getId() >= 8820200 && parent.getId() < 8820212) {
                for (final int i : toSpawn) {
                    final MapleMonster mob = MapleLifeFactory.getMonster(i, level + 25, mobscale);
                    if (mob != null) {
                        mob.spawning += (parent.getAnimationTime("die1") + 250);
                        mob.setPosition(pos);
                        mob.setBabyTime(parent.getAnimationTime("die1"));
                        spawnMonster(mob, -2);
                    }
                }
                return;
            }
            if (parent.getId() == 9400551) {
                for (final int i : toSpawn) {
                    final MapleMonster mob = MapleLifeFactory.getMonster(i);
                    if (mob != null) {
                        mob.spawning += (parent.getAnimationTime("die1") + 250);
                        mob.setPosition(pos);
                        mob.setBabyTime(parent.getAnimationTime("die1"));
                        spawnMonster(mob, -2);
                    }
                }
                return;
            }
            switch (parent.getId()) {
                case 8810026://horntails
                case 8810130: {
                    final List<MapleMonster> mobs = new ArrayList<MapleMonster>();
                    long totalhp = 0;
                    for (final int i : toSpawn) {
                        final MapleMonster mob = MapleLifeFactory.getMonster(i);
                        if (mob != null) {
                            mob.spawning += (parent.getAnimationTime("die1") + 250);
                            mob.setPosition(pos);
                            mob.setBabyTime(parent.getAnimationTime("die1"));
                            if (mob.getId() >= 8810002 && mob.getId() <= 8810009) { //horntail parts
                                mob.disableDrops();
                                mob.disableExp();
                                totalhp += mob.getStats().getHp();
                            }
                            switch (mob.getId()) {
                                case 8810018: // Horntail Sponge
                                case 8810118:
                                    spongy = mob;
                                    break;
                                default:
                                    mobs.add(mob);
                                    break;
                            }
                        }
                    }
                    if (spongy != null && getMonsterById(spongy.getId()) == null) {
                        if (spongy.getId() == 8810018 || spongy.getId() == 8810118) {//HT body
                            spongy.getStats().setHp(totalhp);
                            spongy.setHp(totalhp);
                            spongy.spongy = true;
                            spongy.setDrops(false);
                        }
                        spawnMonster(spongy, -2);

                        for (final MapleMonster i : mobs) {
                            if (i != null) {
                                spawnMonster(i, -2);
                                i.setSponge(spongy);
                            }
                        }
                    }
                    break;
                }
                case 8820014: {
                    for (final int i : toSpawn) {
                        final MapleMonster mob = MapleLifeFactory.getMonster(i);
                        if (mob != null) {
                            mob.spawning += (parent.getAnimationTime("die1") + 250);
                            mob.setPosition(pos);
                            mob.setBabyTime(parent.getAnimationTime("die1"));
                            spawnMonster(mob, -2);
                        }
                    }
                    break;
                }
                default: {
                    for (final int i : toSpawn) {
                        final MapleMonster mob;
                        if (isKaotic) {
                            if (superKaotic) {
                                mob = MapleLifeFactory.getKaoticLinkMonster(i, level, mobscale, parent.getStats().getBar(), true, true, parent.getStats().hits);
                            } else {
                                if (localKaotic) {
                                    mob = MapleLifeFactory.getKaoticMonsters(i, level, mobscale);
                                } else {
                                    mob = MapleLifeFactory.getKaoticMonster(i, level, mobscale);
                                }
                            }
                        } else {
                            mob = MapleLifeFactory.getMonster(i, level, mobscale);
                        }
                        if (mob != null) {
                            mob.spawning += (parent.getAnimationTime("die1") + 250);
                            mob.setPosition(pos);
                            mob.getStats().setBar(parent.getStats().getBar());
                            mob.setBabyTime(parent.getAnimationTime("die1"));
                            if (parent.getMonsterEventType() > 0) {
                                mob.setMonsterEventType(parent.getMonsterEventType());
                            }
                            spawnMonster(mob, -2);

                            if (mob.getId() == 9300216) {
                                broadcastMessage(CField.environmentChange("Dojang/clear", 4));
                                broadcastMessage(CField.environmentChange("dojang/end/clear", 3));
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleEeventLock() {
        if (eventlocked) {
            eventlocked = false;
        } else {
            eventlocked = true;
        }
    }

    public void toggleEeventLock(boolean toggle) {
        eventlocked = toggle;
    }

    public int getTimer() {
        return (int) ((eventTime - (System.currentTimeMillis() - timeStarted)) / 1000);
    }

    public void specialEvent(final int type) {
        try {
            if (!getMonsterSpawn().isEmpty()) {
                if (specialEvent == 0 && !locked) {
                    avgLevel = getAvgPlayerLevel();
                    specialEvent = type;
                    MapMusic();
                    killAllMonsters(true);

                    int time = Randomizer.random(5, 10) * 60 * 1000;
                    timeStarted = System.currentTimeMillis();
                    eventTime = time;
                    for (MapleCharacter chr : getAllPlayers()) {
                        chr.getClient().announce(CField.getClock(getTimer()));
                    }

                    MapTimer.getInstance().schedule(() -> {
                        if (specialEvent == type) {
                            specialEvent = 0;
                            MapMusic();
                            killAllMonsters(true);
                            for (MapleCharacter chr : getAllPlayers()) {
                                if (chr != null) {
                                    chr.getClient().announce(CField.removeClock());
                                }
                            }
                        }
                    }, eventTime);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processEliteEvent(MapleCharacter chr, MapleMonster monster, boolean instanced) {
        if (!locked) {
            kills += 1;
            if (kills == 9000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You feel something in the dark energy."));
            }
            if (kills == 10000) {
                spawnElite(3);
            }
            if (kills == 24000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You really feel something in the dark energy."));
            }
            if (kills == 25000) {
                spawnElite(4);
            }
            if (kills == 49000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You feel the dark energy swarming."));
            }
            if (kills == 50000) {
                spawnElite(5);
            }
            if (kills == 99000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You feel the dark energy radianting everywhere."));
            }
            if (kills == 100000) {
                spawnElite(6);
            }
            if (kills == 249000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You feel the dark energy rapidly radianting everywhere."));
            }
            if (kills == 250000) {
                spawnElite(7);
            }
            if (kills == 499000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You feel the dark energy expanding everywhere."));
            }
            if (kills == 500000) {
                spawnElite(8);
            }
            if (kills == 999000) {
                broadcastMessage(CWvsContext.serverNotice(6, "You feel the dark energy rapidly expanding everywhere."));
            }
            if (kills == 1000000) {
                kills = 0;
                spawnElite(9);
            }
        } else {
            if ((monster.getId() >= 8220022 && monster.getId() <= 8220026)) {
                locked = false;
                broadcastMessage(CWvsContext.serverNotice(6, "The dark energy has faded away."));
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (MapleCharacter player : monster.getAttackers()) {
                    double dropPower = player.getStat().getItempower();
                    player.finishAchievement(113 + phase);
                    Item idrop = ii.randomizeMonsterStats(monster, chr, (Equip) ii.getEquipById(1182006), monster.getStats().getScale() * dropPower, monster.getStats().getScale());
                    if (!MapleInventoryManipulator.addFromDrop(player.getClient(), idrop, false)) {
                        spawnPersonalMobDrop(idrop, calcDropPos(monster.getPosition(), monster.getTruePosition()), monster, chr, (byte) 3, 0, false);
                    }
                    int itemid = 4310015;
                    short amount = (short) Randomizer.random((int) (1 + (chr.getStat().getItems() * monster.getStats().getScale())));
                    idrop = new Item(itemid, (byte) 0, amount, (byte) 0);
                    if (!MapleInventoryManipulator.addFromDrop(player.getClient(), idrop, false)) {
                        spawnPersonalMobDrop(idrop, calcDropPos(monster.getPosition(), monster.getTruePosition()), monster, chr, (byte) 3, 0, false);
                    }
                }
                phase = 0;
                MapMusic();
                sendEffectAll("Gstar/clearN");
                kills += 1;
                updateKills(mapid);
            }
        }
    }

    public void MapMusic() {
        for (MapleCharacter chr : getAllPlayers()) {
            battleMusic(chr);
        }
    }

    public void changeMusic(String txt) {
        for (MapleCharacter chr : getAllPlayers()) {
            chr.getClient().announce(CField.musicChange(txt));
        }
    }

    public void battleMusic(MapleCharacter chr) {
        if (getEventInstance() == null && chr.getEventInstance() == null) {

            if (specialEvent == 1) {
                chr.getClient().announce(CField.musicChange("GL_Masteria/Resurrection"));
                return;
            }
            if (specialEvent == 2) {
                chr.getClient().announce(CField.musicChange("BgmCustom/VHIFIVE"));
                return;
            }
            if (specialEvent == 3) {
                chr.getClient().announce(CField.musicChange("BgmCustom/RiverofSouls"));
                return;
            }
            if (specialEvent == 4) {
                chr.getClient().announce(CField.musicChange("BgmFF9/Pandemonium"));
                return;
            }
            if (specialEvent == 5) {
                chr.getClient().announce(CField.musicChange("BgmFF7/Weapon_Raid"));
                return;
            }
            if (specialEvent == 6) {
                chr.getClient().announce(CField.musicChange("BgmFF7/Jenova"));
                return;
            }
            if (specialEvent == 7) {
                chr.getClient().announce(CField.musicChange("BgmFF9/Ambush"));
                return;
            }
            switch (phase) {
                case 0:
                    chr.getClient().announce(CField.musicChange(getBGM()));
                    return;
                case 1:
                    chr.getClient().announce(CField.musicChange("BgmFF7/Fighting"));
                    return;
                case 2:
                    chr.getClient().announce(CField.musicChange("BgmFF8/Dont_be_Afraid"));
                    return;
                case 3:
                    chr.getClient().announce(CField.musicChange("BgmFF8/Man_with_Machine_Gun"));
                    return;
                case 4:
                    chr.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
                    return;
                case 5:
                    chr.getClient().announce(CField.musicChange("BgmFF8/Premotion"));
                    return;
                case 6:
                    chr.getClient().announce(CField.musicChange("BgmFF9/Evil_Messenger"));
                    return;
                case 7:
                    chr.getClient().announce(CField.musicChange("BgmFF8/Legendary_Beast"));
                    return;
                default:
                    chr.getClient().announce(CField.musicChange(getBGM()));
                    return;
            }
        }
    }

    public int getAvgPlayerLevel() {
        int total = 0;
        int count = 0;
        if (!getAllPlayers().isEmpty()) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (!chr.isGM() && chr.isAlive() && !chr.isClone() && !chr.isHidden() && !chr.isChangingMaps()) {
                    total += chr.getTotalLevel();
                    count += 1;
                }
            }
            if (count > 0) {
                total /= count;
            }
        }
        if (total == 0) {
            total = 1;
        }
        return total;
    }

    public void sendEffectAll(String effect) {
        //"monsterPark/clear" - map.wz/effect.img
        for (MapleCharacter chr : getAllPlayers()) {
            chr.getClient().announce(CField.MapEff(effect));
        }
    }

    public void spawnElite(int scale) {
        locked = true;
        phase = scale - 2;
        MapMusic();
        killAllMonsters(true);
        List<SpawnPoint> randomSpawns = getMonsterSpawn();
        SpawnPoint sp = randomSpawns.get(Randomizer.nextInt(randomSpawns.size()));
        MapleMonster mob = MapleLifeFactory.getMonster(Randomizer.random(8220022, 8220026), sp.getLevel(), phase);
        mob.getStats().setExplosiveReward(true);
        spawnMonsterOnGroundBelow(mob, sp.getPosition());
        broadcastMessage(CWvsContext.serverNotice(6, "Elite: " + mob.getStats().getName() + " has appeared."));
        sendEffectAll("Gstar/start");
    }

    public int getSpawnPointLevel(SpawnPoint spawn) {
        return spawn.getLevel();
    }

    public int getRandomSpawnPointLevel() {
        return getMonsterSpawn().get(Randomizer.nextInt(getMonsterSpawn().size())).getLevel();
    }

    public List<MapleMapObject> getAllObjects() {
        List<MapleMapObject> obj;
        objectRLock.lock();
        try {
            obj = new ArrayList<>(mapobjects.values());
        } finally {
            objectRLock.unlock();
        }
        return obj;
    }

    public List<MapleMapObject> getAllFamiliars() {
        ArrayList<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        objectRLock.lock();
        try {
            for (MapleMapObject mmo : mapobjects.values()) {
                if (mmo.getType() == MapleMapObjectType.FAMILIAR) {
                    ret.add(mmo);
                }
            }
        } finally {
            objectRLock.unlock();
        }
        return ret;
    }

    public final List<MapleMapObject> getReactors() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }

    public final List<MapleMapObject> getMonsters() {
        //System.out.println("monsters???");
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
    }

    public final List<MapleMapObject> getItems() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
    }

    public final List<MapleMapObject> getNpcs() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
    }

    public final List<MapleMapObject> getSummons() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.SUMMON));
    }

    public final List<MapleMapObject> getDoors() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.DOOR));
    }

    public final List<MapleMapObject> getShops() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.SHOP));
    }

    public final List<MapleMapObject> getPlayers() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public final List<MapleMapObject> getMists() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MIST));
    }

    public final List<MapleMapObject> getMercs() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT));
    }

    public List<MapleReactor> getAllReactor() {
        List<MapleReactor> list = new LinkedList<>();
        for (MapleMapObject mmo : getReactors()) {
            if (mmo.getType() == MapleMapObjectType.REACTOR) {
                list.add((MapleReactor) mmo);
            }
        }

        return list;
    }

    public List<MapleSummon> getAllSummons() {
        List<MapleSummon> list = new LinkedList<>();
        for (MapleMapObject mmo : getSummons()) {
            if (mmo.getType() == MapleMapObjectType.SUMMON) {
                list.add((MapleSummon) mmo);
            }
        }

        return list;
    }

    public List<MapleDoor> getAllDoors() {
        List<MapleDoor> list = new LinkedList<>();
        for (MapleMapObject mmo : getDoors()) {
            if (mmo.getType() == MapleMapObjectType.DOOR) {
                if (mmo instanceof MapleDoor) {
                    list.add((MapleDoor) mmo);
                }
            }
        }

        return list;
    }

    public List<MechDoor> getAllMechDoors() {
        List<MechDoor> list = new LinkedList<>();
        for (MapleMapObject mmo : getDoors()) {
            if (mmo.getType() == MapleMapObjectType.DOOR) {
                if (mmo instanceof MechDoor) {
                    list.add((MechDoor) mmo);
                }
            }
        }

        return list;
    }

    public List<HiredMerchant> getAllHiredMerchants() {
        List<HiredMerchant> list = new LinkedList<>();
        for (MapleMapObject mmo : getMercs()) {
            if (mmo.getType() == MapleMapObjectType.HIRED_MERCHANT) {
                list.add((HiredMerchant) mmo);
            }
        }

        return list;
    }

    public List<MapleMonster> getAllTotems() {
        List<MapleMonster> list = new ArrayList<>();
        for (MapleMapObject mmo : getMonsters()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (mob.isTotem()) {
                    list.add(mob);
                }
            }
        }
        Collections.shuffle(list);
        return list;
    }

    public List<MapleMonster> getAllMonsters() {
        List<MapleMonster> list = new ArrayList<>();
        for (MapleMapObject mmo : getMonsters()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (!mob.isTotem() && !mob.isSummon()) {
                    list.add(mob);
                }
            }
        }
        return list;
    }

    public List<MapleMonster> getAllTrueMonsters() {
        List<MapleMonster> list = new ArrayList<>();
        for (MapleMapObject mmo : getMonsters()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (!mob.isTotem()) {
                    list.add(mob);
                }
            }
        }

        return list;
    }

    public List<MapleMonster> getAllSummonMonsters() {
        List<MapleMonster> list = new ArrayList<>();
        for (MapleMapObject mmo : getMonsters()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (mob.isSummon()) {
                    list.add(mob);
                }
            }
        }

        return list;
    }

    public List<MapleMonster> getParkMonsters() {
        List<MapleMonster> list = new ArrayList<>();
        for (MapleMapObject mmo : getMonsters()) {
            if (mmo.getType() == MapleMapObjectType.MONSTER) {
                MapleMonster mob = (MapleMonster) mmo;
                if (!mob.isTotem() && !mob.isSummon()) {
                    list.add(mob);
                }
            }
        }

        return list;
    }

    public List<MapleMapItem> getAllItems() {
        List<MapleMapItem> list = new ArrayList<>();
        for (MapleMapObject mmo : getItems()) {
            if (mmo.getType() == MapleMapObjectType.ITEM) {
                list.add((MapleMapItem) mmo);
            }
        }
        return list;
    }

    public List<MapleMapItem> getAllDrops(Point pos, int range) {
        List<MapleMapItem> list = new ArrayList<>();
        for (MapleMapObject mmo : getItems()) {
            if (mmo.getType() == MapleMapObjectType.ITEM) {
                MapleMapItem item = (MapleMapItem) mmo;
                if (!item.isPickedUp() && pos.distance(item.getPosition()) <= range) {
                    list.add(item);
                }
            }
        }
        return list;

    }

    public List<MapleMapItem> getAllMesos() {
        List<MapleMapItem> list = new ArrayList<>();
        for (MapleMapObject mmo : getItems()) {
            if (mmo.getType() == MapleMapObjectType.ITEM) {
                MapleMapItem item = (MapleMapItem) mmo;
                if (item.getMeso() > 0) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    public List<MapleMonster> getAllUniqueMonsters() {
        List<MapleMonster> list = new LinkedList<>();
        for (MapleMonster mmo : getAllMonsters()) {
            boolean add = true;
            for (MapleMonster mob : list) {
                if (mob.getId() == mmo.getId()) {
                    add = false;
                    break;
                }
            }
            if (add) {
                list.add(mmo);
            }
        }
        return list;
    }

    public List<Integer> getAllUniqueMonstersId() {
        List<Integer> list = new LinkedList<>();
        for (MapleMonster mmo : getAllMonsters()) {
            if (mmo.getId() > 10) {
                if (!list.contains(mmo.getId())) {
                    list.add(mmo.getId());
                }
            }
        }
        return list;
    }

    public boolean getAllMonstersOnMapDead() {
        return countMonsters() == 0;
    }

    public List<MapleMonster> getAllMonstersOnMap() {
        return getAllMonsters();
    }

    public List<MapleMapItem> getAllItemsRange(Point pos) {
        List<MapleMapItem> list = new LinkedList<>();

        for (MapleMapItem mmo : getAllItems()) {
            if (pos.distanceSq(mmo.getPosition()) <= 5000) {
                list.add(mmo);
            }
        }

        return list;
    }

    public List<MapleMonster> getAllMobsRange(Point pos, int w, int h) {
        Rectangle bounds = new Rectangle(pos.x - w, pos.x - h, pos.x + w, pos.x + h);
        return getMonstersInRange(bounds);
    }

    public final void killAllMonsters(final boolean animate) {
        try {
            if (!getAllMonsters().isEmpty()) {
                for (final MapleMonster monster : getAllTrueMonsters()) {
                    if (monster != null && !monster.isTotem()) {
                        if (monster.getStats().getBar()) {
                            broadcastMessage(MobPacket.clearBossHP(monster), monster.getPosition());
                        }
                        removeMonster(monster, animate);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void killMonsters(MapleCharacter chr) {
        try {
            if (!getAllMonsters().isEmpty()) {
                for (final MapleMonster monster : getAllTrueMonsters()) {
                    if (monster != null && !monster.isTotem()) {
                        killMonster(monster.getId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void killMonster(final int monsId) {
        if (!getAllMonsters().isEmpty()) {
            for (final MapleMonster monster : getAllMonsters()) {
                if (monster != null) {
                    if (monster.getId() == monsId) {
                        if (monster.getStats().getBar()) {
                            broadcastMessage(MobPacket.clearBossHP(monster), monster.getPosition());
                        }
                        removeMonster(monster, true);
                    }
                }
            }
        }
    }

    public final void limitReactor(final int rid, final int num) {
        List<MapleReactor> toDestroy = new ArrayList<MapleReactor>();
        Map<Integer, Integer> contained = new LinkedHashMap<Integer, Integer>();
        for (MapleReactor mr : getAllReactor()) {
            if (contained.containsKey(mr.getReactorId())) {
                if (contained.get(mr.getReactorId()) >= num) {
                    toDestroy.add(mr);
                } else {
                    contained.put(mr.getReactorId(), contained.get(mr.getReactorId()) + 1);
                }
            } else {
                contained.put(mr.getReactorId(), 1);
            }
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
            toDestroy.remove(mr);
        }

    }

    public void removeReactors() {
        for (MapleReactor reactor : getAllReactor()) {
            broadcastMessage(CField.destroyReactor(reactor));
            reactor.setAlive(false);
            removeMapObject(reactor, MapleMapObjectType.REACTOR);
            reactor.setTimerActive(false);
        }
    }

    public final void destroyReactors(final int first, final int last) {
        List<MapleReactor> toDestroy = new ArrayList<MapleReactor>();
        for (MapleReactor mr : getAllReactor()) {
            if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                toDestroy.add(mr);
            }
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
            toDestroy.remove(mr);
        }
    }

    public final void destroyReactor(final int oid) {
        final MapleReactor reactor = (MapleReactor) getMapObject(oid);
        if (reactor == null) {
            return;
        }
        broadcastMessage(CField.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor, MapleMapObjectType.REACTOR);
        reactor.setTimerActive(false);

        if (reactor.getDelay() > 0) {
            MapTimer.getInstance().schedule(() -> {
                respawnReactor(reactor);
            }, reactor.getDelay());
        }
    }

    public final void reloadReactors() {
        List<MapleReactor> toSpawn = new ArrayList<MapleReactor>();
        for (MapleReactor reactor : getAllReactor()) {
            broadcastMessage(CField.destroyReactor(reactor));
            reactor.setAlive(false);
            reactor.setTimerActive(false);
            toSpawn.add(reactor);
        }
        for (MapleReactor r : toSpawn) {
            removeMapObject(r, MapleMapObjectType.REACTOR);
            if (!r.isCustom()) { //guardians cpq
                respawnReactor(r);
            }
        }
    }

    /*
     * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
     * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
     */
    public final void resetReactors() {
        setReactorState((byte) 0);
    }

    public final void setReactorState() {
        setReactorState((byte) 1);
    }

    public final void setReactorState(final byte state) {
        for (MapleReactor mr : getAllReactor()) {
            mr.forceHitReactor((byte) state);
        }
    }

    public final void setReactorDelay(final int state) {
        for (MapleReactor mr : getAllReactor()) {
            mr.setDelay(state);
        }
    }

    /*
     * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
     */
    public final void shuffleReactors() {
        shuffleReactors(0, 9999999); //all
    }

    public final void shuffleReactors(int first, int last) {
        List<Point> points = new ArrayList<Point>();

        for (MapleReactor mr : getAllReactor()) {
            if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                points.add(mr.getPosition());
            }
        }
        Collections.shuffle(points);
        for (MapleReactor mr : getAllReactor()) {
            if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                mr.setPosition(points.remove(points.size() - 1));
            }
        }
    }

    public final MapleMapObject getMapObject(int oid) {
        objectRLock.lock();
        try {
            return mapobjects.get(oid);
        } finally {
            objectRLock.unlock();
        }
    }

    public final boolean containsNPC(int npcid) {
        for (MapleNPC n : getAllNPCs()) {
            if (n.getId() == npcid) {
                return true;
            }
        }
        return false;
    }

    public MapleNPC getNPCById(int id) {
        for (MapleNPC n : getAllNPCs()) {
            if (n.getId() == id) {
                return n;
            }
        }
        return null;
    }

    public MapleMonster getMonsterById(int id) {
        for (MapleMonster m : getAllMonsters()) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public MapleMonster getTotem(int id) {
        for (MapleMonster m : getAllTotems()) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public int countMonsterById(int id) {
        int ret = 0;
        for (MapleMonster m : getAllMonsters()) {
            if (m.getId() == id) {
                ret++;
            }
        }
        return ret;
    }

    public MapleReactor getReactorById(int id) {
        for (MapleReactor n : getAllReactor()) {
            if (n.getReactorId() == id) {
                return n;
            }
        }
        return null;
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns
     * null
     *
     * @param oid
     * @return
     */
    public final MapleMonster getMonsterByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.MONSTER) ? (MapleMonster) mmo : null;
    }

    public final MapleNPC getNPCByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.NPC) ? (MapleNPC) mmo : null;
    }

    public final MapleReactor getReactorByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.REACTOR) ? (MapleReactor) mmo : null;
    }

    public final MonsterFamiliar getFamiliarByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.FAMILIAR) ? (MonsterFamiliar) mmo : null;
    }

    public final MapleSummon getSummonByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.SUMMON) ? (MapleSummon) mmo : null;
    }

    public final MaplePlayerShop getPlayerShopByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.SHOP) ? (MaplePlayerShop) mmo : null;
    }

    public final HiredMerchant getMerchantByOid(final int oid) {
        MapleMapObject mmo = getMapObject(oid);
        return (mmo != null && mmo.getType() == MapleMapObjectType.HIRED_MERCHANT) ? (HiredMerchant) mmo : null;
    }

    public final MapleReactor getReactorByName(final String name) {
        for (MapleReactor mr : getAllReactor()) {
            if (mr.getName().equalsIgnoreCase(name)) {
                return mr;
            }
        }
        return null;
    }

    public final void spawnNpc(final int id, final Point pos) {
        final MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y - 2);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFH(pos));
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(NPCPacket.spawnNPC(npc, true));
        addVisibleObjects(npc);
    }

    public final void spawnNpc(final int id, final Point pos, boolean flip) {
        final MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y - 2);
        npc.setRx0(pos.x + 5);
        npc.setRx1(pos.x - 5);
        npc.setFh(getFH(pos));
        npc.setCustom(true);
        npc.setF(flip ? 1 : 0);
        addMapObject(npc);
        broadcastMessage(NPCPacket.spawnNPC(npc, true));
        addVisibleObjects(npc);
    }

    public void clearAllNpcs() {
        if (!getAllNPCs().isEmpty()) {
            for (MapleNPC npc : getAllNPCs()) {
                broadcastMessage(NPCPacket.removeNPCController(npc.getObjectId()));
                broadcastMessage(NPCPacket.removeNPC(npc.getObjectId()));
            }
            getAllNPCs().clear();
        }
    }

    public void clearAllNpcs(MapleCharacter player) {
        if (!getAllNPCs().isEmpty()) {
            for (MapleNPC npc : getAllNPCs()) {
                player.getClient().announce(NPCPacket.removeNPCController(npc.getObjectId()));
                player.getClient().announce(NPCPacket.removeNPC(npc.getObjectId()));
            }
        }
    }

    public final void removeNpc(final int npcid) {
        for (MapleNPC npc : getAllNPCs()) {
            if (npc.isCustom() && (npcid == -1 || npc.getId() == npcid)) {
                broadcastMessage(NPCPacket.removeNPCController(npc.getObjectId()));
                broadcastMessage(NPCPacket.removeNPC(npc.getObjectId()));
                getAllNPCs().remove(npc);
                deleteVisibleObjects(npc);
            }
        }
    }

    public final void hideNpc(final int npcid) {
        for (MapleNPC npc : getAllNPCs()) {
            if (npc.isCustom() && (npcid == -1 || npc.getId() == npcid)) {
                broadcastMessage(NPCPacket.removeNPCController(npc.getObjectId()));
                broadcastMessage(NPCPacket.removeNPC(npc.getObjectId()));
                deleteVisibleObjects(npc);
            }
        }
    }

    public final void spawnReactorOnGroundBelow(final MapleReactor mob, final Point pos) {
        mob.setPosition(pos); //reactors dont need FH lol
        mob.setCustom(true);
        spawnReactor(mob);
    }

    public final void spawnMonster_sSack(final MapleMonster mob, final Point pos, final int spawnType) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 2));
        if (spos != null) {
            spos.y--;
            mob.setPosition(spos);
            spawnMonster(mob, spawnType);
        }
    }

    public final void spawnMonster_Pokemon(final MapleMonster mob, final Point pos, final int spawnType) {
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 2));
        if (spos != null) {
            spos.y--;
            mob.setPosition(spos);
            spawnMonster(mob, spawnType, true);
        }
    }

    public void spawnMonsterOnGroundBelowId(int id, Point pos) {
        MapleMonster mob = MapleLifeFactory.getMonster(id);
        if (mob != null) {
            Point spos = new Point(pos.x, pos.y - 10);
            spos = calcPointBelow(spos);
            if (spos != null) {
                spos.y--;
                mob.setPosition(spos);
                mob.setFHMapData(this, spos);
                spawnMonster(mob);
            }
        } else {
            System.out.println("Error with Monster ID: " + id);
        }
    }

    public void spawnMonsterOnGroundBelowSP(MapleMonster mob, Point pos) {
        if (mob != null) {
            spawnMonster(mob);
        } else {
            System.out.println("Error with Monster");
        }
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        if (mob != null) {
            Point spos = new Point(pos.x, pos.y - 10);
            spos = calcPointBelow(spos);
            if (spos != null) {
                spos.y -= 5;
                mob.setPosition(spos);
                mob.setFHMapData(this, spos);
                spawnMonster(mob);
            }
        } else {
            System.out.println("Error with Monster");
        }
    }

    public void spawnMonsterOnGroundBelowEffect(MapleMonster mob, int effect, Point pos) {
        if (mob != null) {
            Point spos = new Point(pos.x, pos.y - 10);
            spos = calcPointBelow(spos);
            if (spos != null) {
                spos.y -= 5;

                mob.setPosition(spos);
                mob.setFHMapData(this, spos);
                spawnMonster(mob, effect, false);
            }
        } else {
            System.out.println("Error with Monster");
        }
    }

    public final void spawnMonsterWithEffectBelow(final MapleMonster mob, final Point pos, final int effect) {
        if (mob != null) {
            final Point spos = calcPointBelow(new Point(pos.x, pos.y - 10));
            if (spos != null) {
                spos.y -= 5;
                spawnMonsterWithEffect(mob, effect, spos);
            }
        } else {
            System.out.println("Error with Monster");
        }
    }

    public final void spawnMonsterWithEffectBelow(final MapleMonster mob, final int effect, final Point pos) {
        if (mob != null) {
            final Point spos = calcPointBelow(new Point(pos.x, pos.y - 10));
            if (spos != null) {
                spos.y -= 5;
                spawnMonsterWithEffect(mob, effect, spos);
            }
        } else {
            System.out.println("Error with Monster");
        }
    }

    public final void spawnZakum(final int x, final int y) {
        final MapleMonster mainb = MapleLifeFactory.getMonster(8800000);
        final Point spos = calcPointBelow(new Point(x, y - 10));
        if (spos != null) {
            spos.y--;
            mainb.setPosition(spos);
            mainb.setFHMapData(this, spos);
            mainb.setFake(true);

            // Might be possible to use the map object for reference in future.
            spawnFakeMonster(mainb);

            final int[] zakpart = {8800003, 8800004, 8800005, 8800006, 8800007,
                8800008, 8800009, 8800010};

            for (final int i : zakpart) {
                final MapleMonster part = MapleLifeFactory.getMonster(i);
                part.setPosition(spos);
                part.setFHMapData(this, spos);
                spawnMonster(part, -2);
            }
            if (squadSchedule != null) {
                cancelSquadSchedule(false);
            }
        }
    }

    public final void spawnChaosZakum(final int x, final int y) {
        final Point pos = new Point(x, y);
        final MapleMonster mainb = MapleLifeFactory.getMonster(8800100);
        final Point spos = calcPointBelow(new Point(pos.x, pos.y - 2));
        if (spos != null) {
            spos.y--;
            mainb.setPosition(spos);
            mainb.setFake(true);

            // Might be possible to use the map object for reference in future.
            spawnFakeMonster(mainb);

            final int[] zakpart = {8800103, 8800104, 8800105, 8800106, 8800107,
                8800108, 8800109, 8800110};

            for (final int i : zakpart) {
                final MapleMonster part = MapleLifeFactory.getMonster(i);
                part.setPosition(spos);

                spawnMonster(part, -2);
            }
            if (squadSchedule != null) {
                cancelSquadSchedule(false);
            }
        }
    }

    public final void spawnFakeMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 2));
        if (spos != null) {
            spos.y--;
            mob.setPosition(spos);
            if (mob.getFh() == -1) {
                mob.setFh(getFH(pos));
            }
            spawnFakeMonster(mob);
        }
    }

    private void checkRemoveAfter(final MapleMonster monster) {
        final int ra = monster.getStats().getRemoveAfter();

        if (ra > 0 && monster.getLinkCID() <= 0) {
            monster.registerKill(ra * 1000);
        }
    }

    public void setEventInstance(EventInstanceManager eim) {
        //System.out.println("map locked to event: " + eim.getName());
        event = eim;
    }

    public EventInstanceManager getEventInstance() {
        return event;
    }

    private Point bsearchDropPos(Point initial, Point fallback) {
        Point res, dropPos = null;

        int awayx = fallback.x;
        int homex = initial.x;

        int y = initial.y - 85;

        do {
            int distx = awayx - homex;
            int dx = distx / 2;

            int searchx = homex + dx;
            if ((res = calcItemPointBelow(new Point(searchx, y))) != null) {
                awayx = searchx;
                dropPos = res;
            } else {
                homex = searchx;
            }
        } while (Math.abs(homex - awayx) > 5);

        return (dropPos != null) ? dropPos : fallback;
    }

    public Point checkMapEdge(Point pos) {
        if (pos.x < (xLimits.left + 50)) {
            pos.x = xLimits.left + 50;
        }
        if (pos.x > (xLimits.right - 50)) {
            pos.x = xLimits.right - 50;
        }
        return new Point(pos.x, pos.y);
    }

    public Point randomPointOnMap() {
        int x = Randomizer.random(leftLimit, rightLimit);
        int y = Randomizer.random(top - 100, bottom - 100);
        Point pos = checkMapEdge(new Point(x, y));
        Point fpos = calcPointBelow(pos);
        fpos.y -= 10;
        return fpos;
    }

    public final Point calcDropPos(Point initial, final Point fallback) {
        //System.out.println("left: " + getLeft() + " - Limit: " + xLimits.left);
        int L = getLeft() + 100;
        int R = getRight() - 100;
        if (initial.x < L) {
            initial.x = L;
        } else if (initial.x > R) {
            initial.x = R;
        }

        Point ret = initial;   // actual drop ranges: default - 120, explosive - 360
        if (ret == null) {
            ret = bsearchDropPos(initial, fallback);
        }

        if (!mapArea.contains(ret)) { // found drop pos outside the map :O
            return fallback;
        }
        return calcItemPointBelow(ret);

        /*
        
         Point ret = calcPointBelow(Base);
         if (ret == null) {
         ret = bsearchDropPos(initial, fallback);
         }

         if (!mapArea.contains(ret)) { // found drop pos outside the map :O
         return fallback;
         }

         return ret;
         */
    }

    public void setMapPointBoundings(int px, int py, int h, int w) {
        mapArea.setBounds(px, py, w, h);
    }

    public void setMapLineBoundings(int vrTop, int vrBottom, int vrLeft, int vrRight) {
        mapArea.setBounds(vrLeft, vrTop, vrRight - vrLeft, vrBottom - vrTop);
    }

    public void generateMapDropRangeCache() {
        bndLock.lock();
        try {
            Pair<Integer, Integer> bounds = dropBoundsCache.get(mapid);

            if (bounds != null) {
                xLimits = bounds;
            } else {
                // assuming MINIMAP always have an equal-greater picture representation of the map area (players won't walk beyond the area known by the minimap).
                Point lp = new Point(mapArea.x, mapArea.y), rp = new Point(mapArea.x + mapArea.width, mapArea.y), fallback = new Point(mapArea.x + (mapArea.width / 2), mapArea.y);

                lp = bsearchDropPos(lp, fallback);  // approximated leftmost fh node position
                rp = bsearchDropPos(rp, fallback);  // approximated rightmost fh node position

                xLimits = new Pair<>(lp.x + 14, rp.x - 14);
                dropBoundsCache.put(mapid, xLimits);
            }
        } finally {
            bndLock.unlock();
        }
    }

    public void spawnMap() {
        forceRespawn();
        setSummons(false);
    }

    public int spawnCap() {
        if (local) {
            return 150;
        }
        return spawncap;
    }

    public List<SpawnPoint> getAllMonsterSpawn() {
        return new ArrayList<>(getSpawnPoints());
    }

    public List<SpawnPoint> getAllSpawnData() {
        return new ArrayList<>(getSpawnPoints());
    }

    public void setSpawnCap(int value) {
        spawncap = value;
    }

    public int getSpawnCap() {
        if (local) {
            return 150;
        }
        return spawncap;
    }

    public int getAllMonsterSpawnSize() {
        return getAllMonsterSpawn().size();
    }

    public final void spawnMonster(final MapleMonster monster, final int spawnType) {
        spawnMonster(monster, spawnType, false);
    }

    public final void spawnMonster(final MapleMonster monster, final int spawnType, final boolean overwrite) {
        if (spawnedMonstersOnMap.get() < spawnCap()) {
            monster.setMap(this);
            if (monster.getFh() == -1) {
                monster.setFHMapData(this, monster.getPosition());
            }

            if (monster.getId() > 10) {
                if (getEventInstance() != null) {
                    getEventInstance().registerMonster(monster);
                }
            }
            //checkRemoveAfter(monster);
            if (monster.getBabyTime() > 0) {
                monster.setSpawnTrans(true);
                addMapObject(monster);
                TimerManager.getInstance().schedule(() -> {
                    spawnAndAddRangedMapObjectRevive(monster, new DelayedPacketCreation() {

                        @Override
                        public final void sendPackets(final MapleClient c) {
                            c.announce(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 || monster.getStats().getSummonType() == 27 || overwrite ? spawnType : monster.getStats().getSummonType(), 0));
                        }
                    });
                    monster.setSpawnTrans(false);
                    monster.updateMonsterController();
                }, monster.getBabyTime());
            } else {
                spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                    @Override
                    public final void sendPackets(final MapleClient c) {
                        c.announce(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 || monster.getStats().getSummonType() == 27 || overwrite ? spawnType : monster.getStats().getSummonType(), 0));

                    }
                });
                monster.updateMonsterController();
            }

            addSelfDestructive(monster);
            applyRemoveAfter(monster);  // thanks LightRyuzaki for pointing issues with spawned CWKPQ mobs not applying this
        }

    }

    public final void forceSpawnMonster(final MapleMonster monster, Point pos) {
        Point spos = new Point(pos.x, pos.y - 2);
        spos = calcPointBelow(spos);
        if (spos != null) {
            spos.y--;
            monster.setPosition(spos);
        }
        monster.setMap(this);
        if (monster.getFh() == -1) {
            monster.setFh(getFH(pos));
        }

        //checkRemoveAfter(monster);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            @Override
            public final void sendPackets(MapleClient c) {
                c.announce(MobPacket.spawnMonster(monster, -2, 0));
            }
        });
        monster.updateMonsterController();
        if (!monster.isTotem()) {
            if (getEventInstance() != null) {
                getEventInstance().registerMonster(monster);
            }
            addSelfDestructive(monster);
            applyRemoveAfter(monster);  // thanks LightRyuzaki for pointing issues with spawned CWKPQ mobs not applying this
        }
    }

    public void addSelfDestructive(MapleMonster mob) {
        if (mob.getStats().selfDestruction() != null) {
            this.selfDestructives.add(mob.getObjectId());
        }
    }

    public boolean removeSelfDestructive(int mapobjectid) {
        return this.selfDestructives.remove(mapobjectid);
    }

    public void applyRemoveAfter(final MapleMonster monster) {
        final selfDestruction selfDestruction = monster.getStats().selfDestruction();
        if (monster.getStats().removeAfter() > 0 || selfDestruction != null && selfDestruction.getHp() < 0) {
            if (selfDestruction == null) {
                TimerManager.getInstance().schedule(() -> {
                    if (monster.isAlive()) {
                        killMonster(monster);
                    }
                }, monster.getStats().removeAfter() * 1000);
            } else {
                TimerManager.getInstance().schedule(() -> {
                    if (monster.isAlive()) {
                        //System.out.println("bomber");
                        killMonster(monster, 2);
                        //broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 4));
                    }
                }, selfDestruction.removeAfter() * 1000);
            }
        }
    }

    public final void spawnMob(final MapleCharacter player, final int id, int level) {
        if (spawnedMonstersOnMap.get() < spawnCap()) {
            final MapleMonster monster = MapleLifeFactory.getMonster(id);
            if (monster == null) {
                return;
            }
            monster.setMap(this);
            monster.setPosition(player.getPosition());
            if (getEventInstance() != null) {
                getEventInstance().registerMonster(monster);
            }
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                @Override
                public final void sendPackets(MapleClient c) {
                    c.announce(MobPacket.spawnMonster(monster, -2, 0));
                }
            });
            monster.updateMonsterController();
            addSelfDestructive(monster);
            applyRemoveAfter(monster);  // thanks LightRyuzaki for pointing issues with spawned CWKPQ mobs not applying this
        }
    }

    public final void spawnMobScale(final MapleCharacter player, final int id, int level, int scale) {
        if (spawnedMonstersOnMap.get() < spawnCap()) {
            final MapleMonster monster = MapleLifeFactory.getMonster(id, level, scale);
            if (monster == null) {
                return;
            }
            monster.setMap(this);
            monster.setPosition(player.getPosition());

            if (monster.getFh() == -1) {
                monster.setFh(getFH(player.getPosition()));
            }

            if (getEventInstance() != null) {
                getEventInstance().registerMonster(monster);
            }
            //checkRemoveAfter(monster);

            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                @Override
                public final void sendPackets(MapleClient c) {
                    c.announce(MobPacket.spawnMonster(monster, -2, 0));
                }
            });
            monster.updateMonsterController();
            addSelfDestructive(monster);
            applyRemoveAfter(monster);  // thanks LightRyuzaki for pointing issues with spawned CWKPQ mobs not applying this
        }
    }

    public final void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        if (monster == null) {
            return;
        }
        try {
            monster.spawning = System.currentTimeMillis() + monster.getAnimationTime("regen") + 3000;
            monster.setMap(this);

            if (monster.getFh() == -1) {
                monster.setFh(getFH(pos));
            }
            if (getEventInstance() != null) {
                getEventInstance().registerMonster(monster);
            }
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

                @Override
                public final void sendPackets(MapleClient c) {
                    monster.setPosition(pos);
                    c.announce(MobPacket.spawnMonster(monster, effect, 0));
                }
            });
            monster.updateMonsterController();
            addSelfDestructive(monster);
            applyRemoveAfter(monster);  // thanks LightRyuzaki for pointing issues with spawned CWKPQ mobs not applying this
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void spawnFakeMonster(final MapleMonster monster) {
        if (monster == null) {
            return;
        }
        monster.setMap(this);
        monster.setFake(true);
        if (getEventInstance() != null) {
            getEventInstance().registerMonster(monster);
        }
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

            @Override
            public final void sendPackets(MapleClient c) {
                c.announce(MobPacket.spawnMonster(monster, -4, 0));
            }
        });
        monster.updateMonsterController();
    }

    public final void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);

        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

            @Override
            public final void sendPackets(MapleClient c) {
                c.announce(CField.spawnReactor(reactor));
            }
        });
    }

    private void respawnReactor(final MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public final void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

            @Override
            public final void sendPackets(MapleClient c) {
                door.sendSpawnData(c);
                c.announce(CWvsContext.enableActions());
            }
        });
    }

    public final void spawnMechDoor(final MechDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

            @Override
            public final void sendPackets(MapleClient c) {
                c.announce(CField.spawnMechDoor(door, true));
                c.announce(CWvsContext.enableActions());
            }
        });
    }

    public final void spawnSummon(final MapleSummon summon) {
        //summon.updateMap(this);
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                if (summon != null && c.getPlayer() != null && (!summon.isChangedMap() || summon.getOwnerId() == c.getPlayer().getId())) {
                    //System.out.println("summon spawned");
                    c.announce(SummonPacket.spawnSummon(summon, true));
                }
            }
        });
    }

    public final void spawnFamiliar(final MonsterFamiliar familiar) {
        spawnAndAddRangedMapObject(familiar, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                if (familiar != null && c.getPlayer() != null) {
                    c.announce(CField.spawnFamiliar(familiar, true));
                }
            }
        });
    }

    public final void spawnExtractor(final MapleExtractor ex) {
        spawnAndAddRangedMapObject(ex, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                ex.sendSpawnData(c);
            }
        });
    }

    public final void spawnMist(final MapleMist mist, final int duration, boolean fake) {
        if (mist != null) {
            spawnAndAddRangedMapObject(mist, new DelayedPacketCreation() {

                @Override
                public void sendPackets(MapleClient c) {
                    mist.sendSpawnData(c);
                }
            });

            final MapTimer tMan = MapTimer.getInstance();
            final ScheduledFuture<?> poisonSchedule;
            final MapleCharacter owner = getCharacterById(mist.getOwnerId());
            mist.setMap(this);
            final boolean pvp = owner != null ? owner.inPVP() : false;
            switch (mist.isPoisonMist()) {
                case 1:
                    //poison: 0 = none, 1 = poisonous, 2 = recovery
                    poisonSchedule = tMan.register(() -> {
                        if (mist != null) {
                            for (final MapleMapObject mo : getMapObjectsInRect(mist.getBox(), Collections.singletonList(pvp ? MapleMapObjectType.PLAYER : MapleMapObjectType.MONSTER))) {
                                if (pvp && mist.makeChanceResult() && !((MapleCharacter) mo).hasDOT() && ((MapleCharacter) mo).getId() != mist.getOwnerId()) {
                                    ((MapleCharacter) mo).setDOT(mist.getSource().getDOT(), mist.getSourceSkill().getId(), mist.getSkillLevel());
                                } else if (!pvp && mist.makeChanceResult()) {
                                    MapleMonster mob = ((MapleMonster) mo);
                                    if (mob.getId() > 10 && mob.getHp() > 1) {
                                        mob.applyStatus(owner, new MonsterStatusEffect(MonsterStatus.POISON, 1, mist.getSourceSkill().getId(), null, false), true, 500, true, mist.getSource());
                                    }
                                }
                            }
                        }
                    }, 1000, 1000);
                    break;

                case 2:
                    poisonSchedule = null;
                    break;
                case 4:

                    poisonSchedule = tMan.register(() -> {
                        if (mist != null) {
                            for (final MapleMapObject mo : getMapObjectsInRect(mist.getBox(), Collections.singletonList(pvp ? MapleMapObjectType.PLAYER : MapleMapObjectType.MONSTER))) {
                                if (pvp && mist.makeChanceResult() && !((MapleCharacter) mo).hasDOT() && ((MapleCharacter) mo).getId() != mist.getOwnerId()) {
                                    ((MapleCharacter) mo).setDOT(mist.getSource().getDOT(), mist.getSourceSkill().getId(), mist.getSkillLevel());
                                } else if (!pvp && mist.makeChanceResult()) {
                                    MapleMonster mob = ((MapleMonster) mo);
                                    if (mob.getId() > 10 && mob.getHp() > 1) {
                                        mob.applyStatus(owner, new MonsterStatusEffect(MonsterStatus.POISON, 1, mist.getSourceSkill().getId(), null, false), true, 500, true, mist.getSource());
                                    }
                                }
                            }
                        }
                    }, 1000, 1000);
                    break;
                default:
                    poisonSchedule = null;
                    break;
            }
            if (mist != null) {
                mist.setPoisonSchedule(poisonSchedule);
                mist.setSchedule(tMan.schedule(() -> {
                    if (mist != null) {
                        mist.remove(mist.getMap());
                    }
                }, duration));
            }
        }
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final int owner, final Item item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false, this.getFH(droppos));
        broadcastMessage(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 3), drop.getTruePosition());
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final Item item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, -1, (byte) 1, false, this.getFH(droppos));
        broadcastMessage(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 1), drop.getTruePosition());
    }

    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final int owner, final boolean playerDrop, final byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop, this.getId());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(mdrop, dropper.getTruePosition(), droppos, (byte) 1));
            }
        });
        if (!everlast) {
            setDropExpire(mdrop, droptype, 120, 30);
        }
    }

    public final void spawnMesoDrop(final int meso, final Point position, final Point lastposition, final MapleMapObject dropper, final int owner, final boolean playerDrop, final byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop, this.getId());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(mdrop, lastposition, droppos, (byte) 1));
            }
        });
        if (!everlast) {
            setDropExpire(mdrop, droptype, 120, 30);
        }
    }

    public void setDropExpire(MapleMapItem item, int type, long dur, long ffa) {
        item.registerExpire(dur * 1000);
        if (type == 0 || type == 1) {
            item.registerFFA(ffa * 1000);
        }
    }

    public final void spawnMobMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final int owner, final boolean playerDrop, final byte droptype) {
        final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner, droptype, playerDrop, this.getId());

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(mdrop, dropper.getTruePosition(), position, (byte) 1));
            }
        });

    }

    public final void spawnPersonalMobMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner.getId(), droptype, playerDrop, this.getId());

        spawnPersonalMapObject(mdrop, owner, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(mdrop, dropper.getPosition(), position, (byte) 1));
            }
        });
        setDropExpire(mdrop, droptype, 120, 30);
    }

    public final void spawnMobDrop(final Item idrop, final Point dropPos, final MapleMonster mob, final int chr, final byte droptype, final int questid, boolean instanced) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, this.getFH(dropPos));

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                if (c != null && c.getPlayer() != null && (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) && (idrop.getItemId() / 10000 != 238 || c.getPlayer().getMonsterBook().getLevelByCard(idrop.getItemId()) >= 2) && mob != null && dropPos != null) {
                    c.announce(CField.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte) 1));
                }
            }
        });
        setDropExpire(mdrop, droptype, 120, 30);
//	broadcastMessage(CField.dropItemFromMapObject(mdrop, mob.getTruePosition(), dropPos, (byte) 0));

        //activateItemReactors(mdrop, chr.getClient());
    }

    public final void spawnPersonalMobDrop(final Item idrop, final Point dropPos, final MapleMonster mob, final MapleCharacter chr, final byte droptype, final int questid, boolean instanced) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr.getId(), droptype, false, this.getFH(dropPos));
        mdrop.setPersonal(true);
        spawnPersonalMapObject(mdrop, chr, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if (c != null && c.getPlayer() != null && (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) && (idrop.getItemId() / 10000 != 238 || c.getPlayer().getMonsterBook().getLevelByCard(idrop.getItemId()) >= 2) && mob != null && dropPos != null) {
                    c.announce(CField.dropItemFromMapObject(mdrop, mob.getTruePosition(), dropPos, (byte) 1));
                }
            }
        });
        setDropExpire(mdrop, droptype, 120, 30);

        //activateItemReactors(mdrop, chr.getClient());
    }

    public final void spawnRandDrop() {
        if (mapid != 870000008 || channel != 1) {
            return; //fm, ch1
        }

        for (MapleMapItem o : getAllItems()) {
            if (o.isRandDrop()) {
                return;
            }
        }
        MapTimer.getInstance().schedule(() -> {
            final Point pos = new Point(Randomizer.nextInt(800) + 531, -806);
            final int theItem = Randomizer.nextInt(1000);
            int itemid = 0;
            if (theItem < 950) { //0-949 = normal, 950-989 = rare, 990-999 = super
                itemid = GameConstants.normalDrops[Randomizer.nextInt(GameConstants.normalDrops.length)];
            } else if (theItem < 990) {
                itemid = GameConstants.rareDrops[Randomizer.nextInt(GameConstants.rareDrops.length)];
            } else {
                itemid = GameConstants.superDrops[Randomizer.nextInt(GameConstants.superDrops.length)];
            }
            spawnAutoDrop(itemid, pos);
        }, 20000);
    }

    public final void spawnAutoDrop(final int itemid, final Point pos) {
        Item idrop = null;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
            idrop = ii.randomizeStats((Equip) ii.getEquipById(itemid));
        } else {
            idrop = new Item(itemid, (byte) 0, (short) 1, (byte) 0);
        }
        idrop.setGMLog("Dropped from auto " + " on " + mapid);
        final MapleMapItem mdrop = new MapleMapItem(pos, idrop, this.getId());
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(mdrop, pos, pos, (byte) 1));
            }
        });
        broadcastMessage(CField.dropItemFromMapObject(mdrop, pos, pos, (byte) 0));
        setDropExpire(mdrop, 2, 120, 30);
    }

    public final void spawnAutoDrop(final Item item, final byte type, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem mdrop = new MapleMapItem(item, type, droppos, this.getId());
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(mdrop, pos, droppos, (byte) 1));
            }
        });
        broadcastMessage(CField.dropItemFromMapObject(mdrop, pos, droppos, (byte) 0));
        setDropExpire(mdrop, 2, 120, 30);
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, Point pos, final boolean ffaDrop, final boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner.getId(), (byte) 2, playerDrop, this.getFH(droppos));

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 1));
            }
        });
        broadcastMessage(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 0));

        if (!everlast) {
            setDropExpire(drop, 2, 120, 30);
            activateItemReactors(drop, owner.getClient());
        }
    }

    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final Item item, final Point pos, Point pos2, final boolean ffaDrop, final boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos2);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner.getId(), (byte) 3, playerDrop, this.getFH(droppos));

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {

            @Override
            public void sendPackets(MapleClient c) {
                c.announce(CField.dropItemFromMapObject(drop, pos, droppos, (byte) 1));
            }
        });
        broadcastMessage(CField.dropItemFromMapObject(drop, pos, droppos, (byte) 0));

        if (!everlast) {
            setDropExpire(drop, 2, 120, 30);
            activateItemReactors(drop, owner.getClient());
        }
    }

    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final Item item = drop.getItem();

        for (final MapleReactor react : getAllReactor()) {
            if (react.getReactorType() == 100) {
                if (item.getItemId() == GameConstants.getCustomReactItem(react.getReactorId(), react.getReactItem().getLeft()) && react.getReactItem().getRight() == item.getQuantity()) {
                    if (react.getArea().contains(drop.getTruePosition())) {
                        if (!react.isTimerActive()) {
                            MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                            react.setTimerActive(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    public int getItemsSize() {
        return getAllItems().size();
    }

    public int getExtractorSize() {
        return getAllReactor().size();
    }

    public int getMobsSize() {
        return countMonsters();
    }

    public Point getPointOfItem(int itemid) {
        for (MapleMapItem mm : getAllItems()) {
            if (mm.getItem() != null && mm.getItem().getItemId() == itemid) {
                return mm.getPosition();
            }
        }
        return null;
    }

    public List<MapleMist> getAllMists() {
        List<MapleMist> list = new LinkedList<>();
        for (MapleMapObject mmo : getMists()) {
            list.add((MapleMist) mmo);
        }

        return list;
    }

    public List<MapleNPC> getAllNPCs() {
        List<MapleNPC> list = new LinkedList<>();
        for (MapleMapObject mmo : getNpcs()) {
            list.add((MapleNPC) mmo);
        }

        return list;
    }

    public final void returnEverLastItem(final MapleCharacter chr) {
        for (final MapleMapItem item : getAllItems()) {
            if (!item.isPickedUp() && item.getOwner() == chr.getId()) {
                item.setPickedUp(true);
                broadcastMessage(CField.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getTruePosition());
                if (item.getMeso() > 0) {
                    chr.gainMeso(item.getMeso(), false);
                } else {
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
                }
                removeMapObject(item, MapleMapObjectType.ITEM);
            }
        }
    }

    public final void talkMonster(final String msg, final int itemId, final int objectid) {
        if (itemId > 0) {
            startMapEffect(msg, itemId, false);
        }
        broadcastMessage(MobPacket.talkMonster(objectid, itemId, msg)); //5120035
        broadcastMessage(MobPacket.removeTalkMonster(objectid));
    }

    public final void startMapEffect(final String msg, final int itemId) {
        startMapEffect(msg, itemId, false);
    }

    public final void startMapEffect(final String msg, final int itemId, final boolean jukebox) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        mapEffect.setJukebox(jukebox);
        broadcastMessage(mapEffect.makeStartData());
        MapTimer.getInstance().schedule(() -> {
            if (mapEffect != null) {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, jukebox ? 300000 : 30000);
    }

    public final void startExtendedMapEffect(final String msg, final int itemId) {
        broadcastMessage(CField.startMapEffect(msg, itemId, true));
        MapTimer.getInstance().schedule(() -> {
            broadcastMessage(CField.removeMapEffect());
            broadcastMessage(CField.startMapEffect(msg, itemId, false));
            //dont remove mapeffect.
        }, 60000);
    }

    public final void startSimpleMapEffect(final String msg, final int itemId) {
        broadcastMessage(CField.startMapEffect(msg, itemId, true));
    }

    public final void startJukebox(final String msg, final int itemId) {
        startMapEffect(msg, itemId, true);
    }

    public MaplePortal findClosestPlayerSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (portal.getType() == 0 && distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        if (closest == null) {
            return portals.get(0);
        }
        return closest;
    }

    public void updatePlayer(final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        for (MapleCharacter player : getAllPlayers()) {
            if (player.isHidden()) {
                broadcastMessage(chr, CField.removePlayerFromMap(player.getObjectId()), false);
            } else {
                broadcastMessage(chr, CField.spawnPlayerMapobject(player, true), false);
            }
            player.setPosition(player.getPosition());
            sendObjectPlacement(player.getClient());
        }
    }

    public void spawnPlayer(final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        final byte[] packet = CField.spawnPlayerMapobject(chr, true);
        //chr.getClient().announce(packet);
        if (!chr.isGM() && !chr.isHidden()) {
            broadcastMessage(chr, packet, false);
        } else {
            broadcastGMMessage(chr, packet, false);
        }
        sendObjectPlacement(chr.getClient());
    }

    public final void addPlayer(final MapleCharacter chr, boolean first) {
        if (chr == null) {
            return;
        }
        objectWLock.lock();
        try {
            this.mapobjects.put(chr.getObjectId(), chr);
        } finally {
            objectWLock.unlock();
        }
        chrWLock.lock();
        try {
            characters.add(chr);
            if (!chr.isGM()) {
                playerCount.incrementAndGet();
            }
        } finally {
            chrWLock.unlock();
        }
        chr.battle = false;
        chr.setChangeTime();
        if (GameConstants.isTeamMap(mapid) && !chr.inPVP()) {
            chr.setTeam(getAndSwitchTeam() ? 0 : 1);
        }
        spawnPlayer(chr);
        if (!loaded) {
            timer();
        }
        if (!chr.getPets().isEmpty()) {
            for (final MaplePet pet : chr.getPets()) {
                if (first) {
                    pet.setSummoned(0);
                    broadcastMessage(chr, PetPacket.showPet(chr, pet, true, false), true);
                } else {
                    if (pet.getSummoned()) {
                        broadcastMessage(chr, PetPacket.showPet(chr, pet, false, false), true);
                    }
                }
            }
        }
        if (chr.hasSummon()) {
            if (getId() == 5000) {
                chr.getClient().announce(CField.UIPacket.summonMessage("Welcome to Kaotic Maple", 150, 10));
            }
            if (getId() == 5004) {
                chr.getClient().announce(CField.UIPacket.summonMessage("Defeat snails to reach level 10.", 300, 15));
            }
        }

        if (chr.getSummonedFamiliar() != null) {
            chr.spawnFamiliar(chr.getSummonedFamiliar());
        }
        if (chr.getAndroid() != null && !chr.hideAndroid) {
            chr.getAndroid().setPos(chr.getPosition());
            broadcastMessage(CField.spawnAndroid(chr, chr.getAndroid(), true));
        }
        if (GameConstants.isBeginnerJob(chr.getJob())) {
            chr.getClient().announce(CField.getEventNPCInfo());
        } else {
            chr.getClient().announce(CField.getPublicNPCInfo());
        }
        if (!chr.isClone()) {
            for (MapleSummon summon : chr.getSummonsValues()) {
                if (summon != null) {
                    Point pos = chr.getPosition();
                    pos.y--;
                    summon.setPosition(pos);
                    summon.updateMap(this);
                    //this.updateObject(summon);
                    chr.getMap().spawnSummon(summon);
                }
            }
        }
        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && !GameConstants.isResist(chr.getJob())) {
            if (FieldLimitType.Mount.check(fieldLimit)) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            }
        }
        if (!chr.isClone()) {
            if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted() && !chr.isClone()) {
                chr.getClient().announce(CField.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
            }
            if (hasClock()) {
                final Calendar cal = Calendar.getInstance();
                chr.getClient().announce((CField.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
            }
        }
        if (GameConstants.isEvan(chr.getJob()) && chr.getJob() >= 2200) {
            if (chr.getDragon() == null) {
                chr.makeDragon();
            } else {
                chr.getDragon().setPosition(chr.getPosition());
            }
            if (chr.getDragon() != null) {
                broadcastMessage(CField.spawnDragon(chr.getDragon()));
            }
        }
        if (permanentWeather > 0) {
            chr.getClient().announce(CField.startMapEffect("", permanentWeather, false)); //snow, no msg
        }
        if (getPlatforms().size() > 0) {
            chr.getClient().announce(CField.getMovingPlatforms(this));
        }
        if (environment.size() > 0) {
            chr.getClient().announce(CField.getUpdateEnvironment(this));
        }
        if (isTown()) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.RAINING_MINES);
        }
        if (!canSoar()) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.SOARING);
        }
        if (chr.getJob() < 3200 || chr.getJob() > 3212) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.AURA);
        }
        chr.getClient().announce(CField.musicChange(getBGM()));
        if (specialEvent > 0) {
            chr.getClient().announce(CField.getClock(getTimer()));
        }
        if (chr.getParty() != null && !chr.isClone()) {
            chr.silentPartyUpdate();
            chr.getClient().announce(PartyPacket.updateParty(chr.getClient().getChannel(), chr.getParty(), PartyOperation.SILENT_UPDATE, null));
            chr.updatePartyMemberHP();
            chr.receivePartyMemberHP();
        }
        chr.getStat().recalcLocalStats(false, chr);
        chr.getClient().announce(CField.achievementRatio(chr.getStamPerc()));
        if (!this.getObjectFlags().isEmpty()) {
            chr.getObjectVisible();
        }
        chr.checkTotem();
        int totemId = (int) chr.getVarZero("totem");
        if (totemId > 0) {
            chr.getMap().spawnTotem(chr, totemId, chr.getPosition());
        }
    }

    public int getEliteKillPercent() {
        return (int) (kills / 100000);
    }

    public int getNumItems() {
        return getAllItems().size();
    }

    public int getNumMonsters() {
        return countMonsters();
    }

    public void doShrine(final boolean spawned) { //false = entering map, true = defeated
        if (squadSchedule != null) {
            cancelSquadSchedule(true);
        }
        final MapleSquad sqd = getSquadByMap();
        if (sqd == null) {
            return;
        }
        final int mode = (mapid == 280030000 ? 1 : (mapid == 280030001 ? 2 : (mapid == 240060200 || mapid == 240060201 ? 3 : 0)));
        //chaos_horntail message for horntail too because it looks nicer
        final EventManager em = getEMByMap();
        if (sqd != null && em != null && getCharactersSize() > 0) {
            final String leaderName = sqd.getLeaderName();
            final String state = em.getProperty("state");
            final Runnable run;
            MapleMap returnMapa = getForcedReturnMap();
            if (returnMapa == null || returnMapa.getId() == mapid) {
                returnMapa = getReturnMap();
            }
            if (mode == 1 || mode == 2) { //chaoszakum
                broadcastMessage(CField.showChaosZakumShrine(spawned, 5));
            } else if (mode == 3) { //ht/chaosht
                broadcastMessage(CField.showChaosHorntailShrine(spawned, 5));
            } else {
                broadcastMessage(CField.showHorntailShrine(spawned, 5));
            }
            if (spawned) { //both of these together dont go well
                broadcastMessage(CField.getClock(300)); //5 min
            }
            final MapleMap returnMapz = returnMapa;
            if (!spawned) { //no monsters yet; inforce timer to spawn it quickly
                final List<MapleMonster> monsterz = getAllMonsters();
                final List<Integer> monsteridz = new ArrayList<Integer>();
                for (MapleMapObject m : monsterz) {
                    monsteridz.add(m.getObjectId());
                }
                run = new Runnable() {

                    @Override
                    public void run() {
                        final MapleSquad sqnow = MapleMap.this.getSquadByMap();
                        if (MapleMap.this.getCharactersSize() > 0 && MapleMap.this.getNumMonsters() == monsterz.size() && sqnow != null && sqnow.getStatus() == 2 && sqnow.getLeaderName().equals(leaderName) && MapleMap.this.getEMByMap().getProperty("state").equals(state)) {
                            boolean passed = monsterz.isEmpty();
                            for (MapleMonster m : MapleMap.this.getAllMonsters()) {
                                for (int i : monsteridz) {
                                    if (m.getObjectId() == i) {
                                        passed = true;
                                        break;
                                    }
                                }
                                if (passed) {
                                    break;
                                } //even one of the monsters is the same
                            }
                            if (passed) {
                                //are we still the same squad? are monsters still == 0?
                                byte[] packet;
                                if (mode == 1 || mode == 2) { //chaoszakum
                                    packet = CField.showChaosZakumShrine(spawned, 0);
                                } else {
                                    packet = CField.showHorntailShrine(spawned, 0); //chaoshorntail message is weird
                                }
                                for (MapleCharacter chr : MapleMap.this.getCharacters()) { //warp all in map
                                    chr.getClient().announce(packet);
                                    chr.changeMap(returnMapz, returnMapz.getPortal(0)); //hopefully event will still take care of everything once warp out
                                }
                                checkStates("");
                                resetFully();
                            }
                        }

                    }
                };
            } else { //inforce timer to gtfo
                run = new Runnable() {

                    @Override
                    public void run() {
                        MapleSquad sqnow = MapleMap.this.getSquadByMap();
                        //we dont need to stop clock here because they're getting warped out anyway
                        if (MapleMap.this.getCharactersSize() > 0 && sqnow != null && sqnow.getStatus() == 2 && sqnow.getLeaderName().equals(leaderName) && MapleMap.this.getEMByMap().getProperty("state").equals(state)) {
                            //are we still the same squad? monsters however don't count
                            byte[] packet;
                            if (mode == 1 || mode == 2) { //chaoszakum
                                packet = CField.showChaosZakumShrine(spawned, 0);
                            } else {
                                packet = CField.showHorntailShrine(spawned, 0); //chaoshorntail message is weird
                            }
                            for (MapleCharacter chr : MapleMap.this.getCharacters()) { //warp all in map
                                chr.getClient().announce(packet);
                                chr.changeMap(returnMapz, returnMapz.getPortal(0)); //hopefully event will still take care of everything once warp out
                            }
                            checkStates("");
                            resetFully();
                        }
                    }
                };
            }
            squadSchedule = MapTimer.getInstance().schedule(run, 300000); //5 mins
        }
    }

    public final MapleSquad getSquadByMap() {
        MapleSquadType zz = null;
        switch (mapid) {
            case 105100400:
            case 105100300:
                zz = MapleSquadType.bossbalrog;
                break;
            case 280030000:
                zz = MapleSquadType.zak;
                break;
            case 280030001:
                zz = MapleSquadType.chaoszak;
                break;
            case 240060200:
                zz = MapleSquadType.horntail;
                break;
            case 240060201:
                zz = MapleSquadType.chaosht;
                break;
            case 270050100:
                zz = MapleSquadType.pinkbean;
                break;
            case 802000111:
                zz = MapleSquadType.nmm_squad;
                break;
            case 802000211:
                zz = MapleSquadType.vergamot;
                break;
            case 802000311:
                zz = MapleSquadType.tokyo_2095;
                break;
            case 802000411:
                zz = MapleSquadType.dunas;
                break;
            case 802000611:
                zz = MapleSquadType.nibergen_squad;
                break;
            case 802000711:
                zz = MapleSquadType.dunas2;
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                zz = MapleSquadType.core_blaze;
                break;
            case 802000821:
            case 802000823:
                zz = MapleSquadType.aufheben;
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                zz = MapleSquadType.vonleon;
                break;
            case 551030200:
                zz = MapleSquadType.scartar;
                break;
            case 271040100:
                zz = MapleSquadType.cygnus;
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(channel).getMapleSquad(zz);
    }

    public final MapleSquad getSquadBegin() {
        if (squad != null) {
            return ChannelServer.getInstance(channel).getMapleSquad(squad);
        }
        return null;
    }

    public final EventManager getEMByMap() {
        String em = null;
        switch (mapid) {
            case 105100400:
                em = "BossBalrog_EASY";
                break;
            case 105100300:
                em = "BossBalrog_NORMAL";
                break;
            case 280030000:
                em = "ZakumBattle";
                break;
            case 240060200:
                em = "HorntailBattle";
                break;
            case 280030001:
                em = "ChaosZakum";
                break;
            case 240060201:
                em = "ChaosHorntail";
                break;
            case 270050100:
                em = "PinkBeanBattle";
                break;
            case 802000111:
                em = "NamelessMagicMonster";
                break;
            case 802000211:
                em = "Vergamot";
                break;
            case 802000311:
                em = "2095_tokyo";
                break;
            case 802000411:
                em = "Dunas";
                break;
            case 802000611:
                em = "Nibergen";
                break;
            case 802000711:
                em = "Dunas2";
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                em = "CoreBlaze";
                break;
            case 802000821:
            case 802000823:
                em = "Aufhaven";
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                em = "VonLeonBattle";
                break;
            case 551030200:
                em = "ScarTarBattle";
                break;
            case 271040100:
                em = "CygnusBattle";
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(channel).getEventSM().getEventManager(em);
    }

    public final void removePlayer(final MapleCharacter chr) {
        removePlayer(chr, false);
    }

    public final void removePlayer(final MapleCharacter chr, boolean dc) {
        //log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });
        chr.setInvincible(false);
        if (GuildHandler.hasGuildInvitation(chr)) {
            GuildHandler.cancelInvite(chr.getClient());
        }
        if (getTotem(chr) != null && getTotem(chr).getOwner() == chr) {
            removeTotem(chr);
        }
        if (everlast) {
            if (!noVacMap(getId()) && getId() != 4407) {
                returnEverLastItem(chr);
            }
        }

        chrWLock.lock();
        try {
            characters.remove(chr);
            if (!chr.isGM()) {
                playerCount.decrementAndGet();
            }
        } finally {
            chrWLock.unlock();
        }
        removeMapObject(chr, MapleMapObjectType.PLAYER);
        chr.checkFollow();
        chr.removeExtractor();
        chr.attacks = 0;
        if (!dc && chr.getShop() != null) {
            IMaplePlayerShop shop = chr.getPlayerShop();
            if (shop != null && shop.isOwner(chr)) {
                shop.closeShop(true);
            }
        }
        /*
        if (!chr.getPets().isEmpty()) {
            for (final MaplePet pet : chr.getPets()) {
                if (pet.getSummoned()) {
                    pet.setSummoned(0);
                    broadcastMessage(chr, PetPacket.showPet(chr, pet, true, false), false);
                }
            }
        }
         */
        broadcastMessage(CField.removePlayerFromMap(chr.getId()));

        if (chr.getSummonedFamiliar() != null) {
            chr.removeVisibleFamiliar();
        }

        List<MapleSummon> toCancel = new ArrayList<MapleSummon>();
        for (MapleSummon summon : chr.getSummonsValues()) {
            if (summon.isPuppet()) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
            } else if (summon.isReaper()) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.REAPER);
            }
            removeMapObject(summon, MapleMapObjectType.SUMMON);

        }
        for (MapleSummon summon : toCancel) {
            chr.removeSummon(summon);
            chr.dispelSkill(summon.getSkill()); //remove the buff
        }
        for (MapleMist mist : getAllMists()) {
            if (mist != null && mist.getOwnerId() == chr.getId()) {
                removeMapObject(mist, MapleMapObjectType.MIST);
                if (mist.getPoisonSchedule() != null) {
                    mist.getPoisonSchedule().cancel(false);
                }
                if (!getAllPlayers().isEmpty()) {
                    broadcastMessage(CField.removeMist(mist.getObjectId(), false));
                }
            }
        }
        if (spawnedMonstersOnMap.get() > 0) {
            for (MapleMonster mob : getAllTrueMonsters()) {
                if (mob != null && mob.isAlive() && mob.getController() != null && mob.getController() == chr) {
                    mob.releaseControllers(chr);
                }
            }
        }
        if (!chr.isClone()) {
            checkStates(chr.getName());
            if (mapid == 109020001) {
                chr.canTalk(true);
            }
            chr.getVisibleMapObjects().clear();
            chr.leaveMap(this);
        }
        if (getPlayers().isEmpty()) {
            removeAllItems(false);
        }
        removeObjectPlacement(chr.getClient());
    }

    public synchronized final void broadcastStatMessage(final MapleCharacter source, Map<MapleStat, Integer> stats) {
        for (MapleCharacter chr : getAllPlayers()) {
            if (chr != source) {
                chr.getClient().announce(CWvsContext.updatePlayerStats(stats, true, source));
            }
        }
    }

    public synchronized final void broadcastMessage(final byte[] packet) {
        broadcastMessage(null, packet, GameConstants.VIEW_RANGE, null);
    }

    public synchronized final void broadcastMessage(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, GameConstants.VIEW_RANGE, source.getPosition());
    }

    public synchronized final void broadcastSkill(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        if (getPlayerCount() > 0) {
            if (repeatToSource) {
                source.getClient().announce(packet);
            }
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null && chr.getEffects()) {
                    if (chr != source) {
                        chr.getClient().announce(packet);
                    }
                }
            }
        }
    }

    public synchronized final void forceBroadcastSkill(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        if (getPlayerCount() > 0) {
            if (repeatToSource) {
                source.getClient().announce(packet);
            }
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != source) {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    /*	public void broadcastMessage(MapleCharacter source, byte[] packet, boolean repeatToSource, boolean ranged) {
     broadcastMessage(repeatToSource ? null : source, packet, ranged ? MapleCharacter.MAX_VIEW_RANGE_SQ : Double.POSITIVE_INFINITY, source.getPosition());
     }*/
    public synchronized final void broadcastMessage(final byte[] packet, final Point rangedFrom) {
        broadcastMessage(null, packet, GameConstants.VIEW_RANGE, rangedFrom);
    }

    public synchronized final void broadcastMessage(final MapleCharacter source, final byte[] packet, final Point rangedFrom) {
        broadcastMessage(source, packet, GameConstants.VIEW_RANGE, rangedFrom);
    }

    public synchronized void broadcastMessage(final MapleCharacter source, final byte[] packet, final double rangeSq, final Point rangedFrom) {
        if (getPlayerCount() > 0) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null && chr != source) {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    public synchronized void broadcastMessage(final String msg) {
        if (getPlayerCount() > 0) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null) {
                    chr.dropMessage(msg);
                }
            }
        }
    }

    public synchronized void broadcastMessage(int type, final String msg) {
        if (getPlayerCount() > 0) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null) {
                    chr.dropMessage(type, msg);
                }
            }
        }
    }

    public synchronized void broadcastColorMessage(int type, final String msg) {
        if (getPlayerCount() > 0) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != null) {
                    chr.dropColorMessage(type, msg);
                }
            }
        }
    }

    private static boolean isNonRangedType(MapleMapObjectType type) {
        switch (type) {
            case NPC:
            case PLAYER:
            case HIRED_MERCHANT:
            case PLAYER_NPC:
            case DRAGON:
            case MIST:
            case KITE:
                return true;
            default:
                return false;
        }
    }

    public void removeObjectPlacement(final MapleClient c) {
        for (final MapleMapObject o : getAllObjects()) {
            o.sendDestroyData(c);
            //chr.addVisibleMapObject(o);
        }
    }

    public void sendObjectPlacement(final MapleClient c) {
        if (c != null) {
            MapleCharacter chr = c.getPlayer();
            if (chr != null) {
                for (final MapleMapObject o : getAllObjects()) {
                    if (o.getType() == MapleMapObjectType.ITEM) {
                        MapleMapItem item = ((MapleMapItem) o);
                        if (item.isPersonal() && c.getPlayer().getId() != item.getOwner()) {
                            continue;
                        }
                    }
                    if (o.getType() == MapleMapObjectType.REACTOR) {
                        if (!((MapleReactor) o).isAlive()) {
                            continue;
                        }
                    }
                    if (o.getType() == MapleMapObjectType.PLAYER) {
                        MapleCharacter player = ((MapleCharacter) o);
                        if (player.isGM() && !chr.isGM()) {
                            continue;
                        }
                        if (player.isHidden() && !chr.isHidden()) {
                            continue;
                        }
                        if (chr == player) {
                            continue;
                        }
                    }
                    o.sendSpawnData(c);

                    //chr.addVisibleMapObject(o);
                }
            }
        }
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public final List<MaplePortal> getPortalsInRange(final Point from, final double rangeSq) {
        final List<MaplePortal> ret = new ArrayList<MaplePortal>();
        for (MaplePortal type : getPortals()) {
            if (from.distanceSq(type.getPosition()) <= rangeSq && type.getTargetMapId() != mapid && type.getTargetMapId() != 999999999) {
                ret.add(type);
            }
        }
        return ret;
    }

    public final boolean shopPlace(final Point from, final double rangeSq) {
        for (MaplePortal type : getPortals()) {
            if (from.distanceSq(type.getPosition()) <= rangeSq && type.getTargetMapId() != mapid && type.getTargetMapId() != 999999999) {
                return true;
            }
        }
        for (MapleMapObject obj : getAllObjects()) {
            switch (obj.getType()) {
                case MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT, MapleMapObjectType.NPC -> {
                    if (from.distanceSq(obj.getPosition()) <= rangeSq) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public final List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq) {
        final List<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        for (MapleMapObject obj : getAllObjects()) {
            if (from.distanceSq(obj.getPosition()) <= Double.POSITIVE_INFINITY) {
                ret.add(obj);
            }
        }
        return ret;
    }

    public final List<MapleMapObject> getMapObjects() {
        return getAllObjects();
    }

    public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
        return getMapObjectsInRange(from, rangeSq, Arrays.asList(MapleMapObjectType.ITEM));
    }

    public final List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        for (MapleMapObject obj : getAllObjects()) {
            if (MapObject_types.contains(obj.getType())) {
                if (from.distanceSq(obj.getPosition()) <= rangeSq) {
                    ret.add(obj);
                }
            }
        }
        return ret;
    }

    public final List<MapleMapObject> getMapObjectsInRect(final Rectangle box, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        for (MapleMapObject obj : getAllObjects()) {
            if (MapObject_types.contains(obj.getType())) {
                if (box.contains(obj.getPosition())) {
                    ret.add(obj);
                }
            }
        }
        return ret;
    }

    public final List<MapleCharacter> getCharactersIntersect(final Rectangle box) {
        final List<MapleCharacter> ret = new ArrayList<MapleCharacter>();
        for (MapleCharacter chr : getAllPlayers()) {
            if (chr.getBounds().intersects(box)) {
                ret.add(chr);
            }
        }
        return ret;
    }

    public final boolean getCharacterIntersect(MapleCharacter chr, final Rectangle box) {
        return chr.getBounds().intersects(box);
    }

    public final boolean getMonsterIntersect(MapleMonster mob, final Rectangle box) {
        return mob.getBounds().intersects(box);
    }

    public final List<MapleCharacter> getPlayersInRectAndInList(final Rectangle box, final List<MapleCharacter> chrList) {
        final List<MapleCharacter> character = new LinkedList<MapleCharacter>();

        for (MapleCharacter chr : getAllPlayers()) {
            if (chrList.contains(chr) && box.contains(chr.getTruePosition())) {
                character.add(chr);
            }
        }
        return character;
    }

    public final void addPortal(final MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public final MaplePortal getPortal(final String portalname) {
        for (final MaplePortal port : portals.values()) {
            if (port.getPortalName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public final MaplePortal getPortal(final int portalid) {
        return portals.get(portalid);
    }

    public final void resetPortals() {
        for (final MaplePortal port : portals.values()) {
            port.setPortalState(true);
        }
    }

    public final int getNumSpawnPoints() {
        return getMonsterSpawn().size();
    }

    public void spawnMonster(final MapleMonster monster) {
        spawnMonster(monster, -2, false);
    }

    public final List<MapleCharacter> getCharacters() {
        return getAllPlayers();
    }

    public final MapleCharacter getCharacterByName(final String id) {
        for (MapleCharacter mc : getAllPlayers()) {
            if (mc.getName().equalsIgnoreCase(id)) {
                return mc;
            }
        }
        return null;
    }

    public final MapleCharacter getCharacterById_InMap(final int id) {
        return getCharacterById(id);
    }

    public final MapleCharacter getCharacterById(final int id) {
        for (MapleCharacter mc : getAllPlayers()) {
            if (mc.getId() == id) {
                return mc;
            }
        }
        return null;
    }

    public Map<Integer, MapleCharacter> getMapAllPlayersId() {
        Map<Integer, MapleCharacter> pchars = new HashMap<>();
        for (MapleCharacter chr : this.getAllPlayers()) {
            if (!chr.isGM() || chr.isAlive()) {
                pchars.put(chr.getId(), chr);
            }
        }

        return pchars;
    }

    public List<MapleCharacter> getAllPlayers() {
        List<MapleCharacter> character;
        chrRLock.lock();
        try {
            character = new ArrayList<>(characters);
        } finally {
            chrRLock.unlock();
        }
        return character;
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
    }

    public void deleteVisibleObjects(MapleMapObject obj) {
        for (MapleCharacter mc : getAllPlayers()) {
            mc.removeVisibleMapObject(obj);
        }
    }

    public void addVisibleObjects(MapleMapObject obj) {
        for (MapleCharacter mc : getAllPlayers()) {
            mc.addVisibleMapObject(obj);
        }
    }

    public void setVac(boolean toggle) {
        allowVac = toggle;
    }

    public boolean noVacMap(int id) {
        if (id >= 4001 && id <= 4007) {
            return false;
        }
        if (id >= 4100 && id < 4103) {
            return false;
        }
        if (id == 75006 || id == 4305) {
            return false;
        }

        return allowVac;
    }

    public void movePlayer(final MapleCharacter player, final Point newPosition) {
        player.setPosition(newPosition);
        /*
        if (!player.battleLimit()) {
            if (!player.getPalStorage().getActivePals().isEmpty()) {
                if (!player.isChangingMaps() && !player.battle && player.getEventInstance() == null && !getSpawnCount().isEmpty()) {
                    if (player.getAccVara("pal_battles") == 1) {
                        int chance = Randomizer.random(1, 25);
                        if (chance == 1) {
                            startRandomBattle(player, pal_level, true);
                        }
                    }
                }
            }
        }
         */
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = getPortal(0);
        double distance, shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public MaplePortal findRandomSpawnpoint() {
        final List<MaplePortal> spawns = new ArrayList<>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() == 0) {
                spawns.add(portal);
            }
        }
        if (spawns.isEmpty()) {
            return getPortal(0);
        }
        return spawns.get(Randomizer.nextInt(spawns.size()));
    }

    public Point getRandomSpawnPoint() {
        final List<MaplePortal> spawns = new ArrayList<>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() == 0) {
                spawns.add(portal);
            }
        }
        MaplePortal portal = spawns.get(Randomizer.nextInt(spawns.size()));
        if (portal == null) {
            portal = getPortal(0);
        }
        return portal.getPosition();
    }

    public MapleCharacter getRandomNormalPlayer() {
        final List<MapleCharacter> players = getAllPlayers();
        List<MapleCharacter> targets = new ArrayList<>();
        for (MapleCharacter chr : players) {
            if (!chr.isGM() && chr.isAlive()) {
                targets.add(chr);
            }
        }
        if (targets.isEmpty()) {
            return players.get(Randomizer.nextInt(players.size()));
        }
        return targets.get(Randomizer.nextInt(targets.size()));
    }

    public MapleCharacter getRandomPlayer() {
        final List<MapleCharacter> players = getAllPlayers();
        return players.get(Randomizer.nextInt(players.size()));
    }

    public MaplePortal getRandomSpawnPortal() {
        final List<MaplePortal> spawns = new ArrayList<>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() == 0) {
                spawns.add(portal);
            }
        }
        MaplePortal portal = getPortal(0);
        if (!spawns.isEmpty()) {
            portal = spawns.get(Randomizer.nextInt(spawns.size()));
        }
        if (portal == null) {
            portal = getPortal(0);
        }
        return portal;
    }

    public int getRandomSpawnPortalbyId() {
        final List<MaplePortal> spawns = new ArrayList<>();
        for (MaplePortal portal : portals.values()) {
            if (portal.getType() == 0) {
                spawns.add(portal);
            }
        }
        MaplePortal portal = spawns.get(Randomizer.nextInt(spawns.size()));
        if (portal == null) {
            portal = getPortal(0);
        }
        return portal.getId();
    }

    public MaplePortal findClosestPortal(Point from) {
        MaplePortal closest = getPortal(0);
        double distance, shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public MapleCharacter findClosestPlayer(Point from) {
        MapleCharacter closest = null;
        if (!getAllPlayers().isEmpty()) {
            double distance, shortestDistance = Double.POSITIVE_INFINITY;
            for (MapleCharacter player : getAllPlayers()) {
                distance = player.getPosition().distanceSq(from);
                if (distance < shortestDistance) {
                    closest = player;
                    shortestDistance = distance;
                    //System.out.println("Nearest player is: " + closest.getName());
                }
            }
        }
        return closest;
    }

    public MapleCharacter findClosestPlayerNoGm(Point from) {
        MapleCharacter closest = null;
        if (!getAllPlayers().isEmpty()) {
            double distance, shortestDistance = Double.POSITIVE_INFINITY;
            for (MapleCharacter player : getAllPlayers()) {
                if (player.isAlive() && !player.isHidden() && !player.isClone() && !player.isChangingMaps() && !player.isGM()) {
                    distance = player.getPosition().distanceSq(from);
                    if (distance < shortestDistance) {
                        closest = player;
                        shortestDistance = distance;
                        //System.out.println("Nearest player is: " + closest.getName());
                    }
                }
            }
        }
        return closest;
    }

    public String spawnDebug() {
        StringBuilder sb = new StringBuilder("Mobs in map : ");
        sb.append(this.getMobsSize());
        sb.append(" spawnedMonstersOnMap: ");
        sb.append(spawnedMonstersOnMap);
        sb.append(" spawnpoints: ");
        sb.append(getSpawnPoints().size());
        sb.append(" maxRegularSpawn: ");
        sb.append(maxRegularSpawn);
        sb.append(" actual monsters: ");
        sb.append(getNumMonsters());
        sb.append(" monster rate: ");
        sb.append(monsterRate);
        sb.append(" fixed: ");
        sb.append(fixedMob);

        return sb.toString();
    }

    public final int getMapObjectSize() {
        return getAllObjects().size();
    }

    public int characterSize() {
        return getPlayerCount();
    }

    public final int getCharactersSize() {
        return getPlayerCount();
    }

    public int getSpawnedMonstersOnMap() {
        return countMonsters();

    }

    public Lock getItemLock() {
        return itemLock;
    }

    public void itemLoot(MapleCharacter chr, Point pos, int range) {
        if (chr != null && !chr.isAlive()) {
            return;
        }
        itemLock.lock();
        try {
            List<Item> etcloot = new ArrayList<>();
            for (MapleMapItem mapitem : getAllDrops(pos, range)) {
                if (mapitem != null) {
                    if (chr != null && !GameConstants.getLock()) {
                        if (!mapitem.isPlayerDrop() && !mapitem.isPickedUp() && mapitem.getOwner() == chr.getId()) {
                            boolean picked = false;
                            if (mapitem.getMeso() > 0) {
                                chr.gainMeso(mapitem.getMeso(), true, false);
                                picked = true;
                            } else {
                                Item drop = mapitem.getItem();
                                if (drop != null) {
                                    MapleInventoryType type = GameConstants.getInventoryType(drop.getItemId());
                                    if (type == MapleInventoryType.ETC || type == MapleInventoryType.USE) {
                                        if (chr.getEtc()) {
                                            etcloot.add(drop);
                                            picked = true;
                                        } else {
                                            if (chr.canHold(drop.getItemId(), drop.getQuantity())) {
                                                if (MapleInventoryManipulator.addFromDrop(chr.getClient(), drop)) {
                                                    picked = true;
                                                }
                                            } else {
                                                etcloot.add(drop);
                                                picked = true;
                                            }
                                        }
                                    } else {
                                        if (chr.canHold(drop.getItemId(), drop.getQuantity())) {
                                            if (MapleInventoryManipulator.addFromDrop(chr.getClient(), drop)) {
                                                picked = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (picked) {
                                mapitem.setPickedUp(true);
                                chr.getClient().announce(CField.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()));
                                chr.getMap().removeMapObject(mapitem, MapleMapObjectType.ITEM);
                            }
                        }
                    }
                }
            }
            if (!etcloot.isEmpty()) {
                chr.storeDropEtc(etcloot);
                etcloot.clear();
            }
        } finally {
            itemLock.unlock();
        }
    }

    private class ActivateItemReactor implements Runnable {

        private final MapleMapItem mapitem;
        private final MapleReactor reactor;
        private final MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId()) && !mapitem.isPickedUp()) {
                mapitem.expire(MapleMap.this);
                reactor.hitReactor(c);
                reactor.setTimerActive(false);

                if (reactor.getDelay() > 0) {
                    MapTimer.getInstance().schedule(() -> {
                        reactor.forceHitReactor((byte) 0);
                    }, reactor.getDelay());
                }
            } else {
                reactor.setTimerActive(false);
            }
        }
    }

    public List<SpawnPoint> getMonsterSpawn() {
        return new ArrayList<>(getSpawnPoints());
    }

    public int countMonsters() {
        return spawnedMonstersOnMap.get();
    }

    public void setMini(boolean toggle) {
        mini = toggle;
    }

    public boolean banned(int id) {
        switch (id) {
            case 677000011:
                return true;
        }
        return false;
    }

    public void kaoticRespawn() {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sp = SpawnPoints;
                if (!sp.isEmpty()) {
                    int spawncount = sp.size();
                    Collections.shuffle(sp);
                    int spawnAttempts = 0;
                    int currentSpawn = 0;
                    int max = spawnCap();
                    if (specialEvent == 7) {
                        max = 40;
                    }
                    int count = spawnedMonstersOnMap.get();
                    if (count < max) {
                        while (count < max) {
                            if (sp.get(currentSpawn).shouldSpawn()) {
                                int mobid = sp.get(currentSpawn).getMobId();
                                int level = sp.get(currentSpawn).getLevel();
                                MapleMonster mob = sp.get(currentSpawn).getMonster(MapleLifeFactory.getKaoticMonsters(mobid, level, scale));
                                mob.setLocalKaotic(true);
                                mob.setEventScript(false);
                                sp.get(currentSpawn).spawnMonster(mob, this, sp.get(currentSpawn).getPosition());
                            } else {
                                spawnAttempts++;
                                if (spawnAttempts >= sp.size()) {
                                    break;
                                }
                            }
                            currentSpawn++;
                            currentSpawn %= spawncount;
                            count++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public boolean getHellPortal() {
        return getTotem(4) != null;
    }

    public void respawn() {
        if (!park) {
            try {
                if (!getPQlock() && !banned(this.getId())) {
                    List<SpawnPoint> sp = getAllMonsterSpawn();
                    if (!sp.isEmpty()) {
                        int spawncount = sp.size();
                        Collections.shuffle(sp);
                        int spawnAttempts = 0;
                        int currentSpawn = 0;
                        int max = getSpawnCap();
                        if (specialEvent == 7) {
                            max = 40;
                        }
                        boolean forced = false;
                        int count = spawnedMonstersOnMap.get();
                        //System.out.println("count: " + count + " - Monsters: " + max);
                        int needed = max - count;
                        for (int i = 0; i < needed; i++) {
                            SpawnPoint spawner = getRandomMonsterSpawnPoint();
                            int mid = spawner.getId();
                            if (spawner.shouldSpawn()) {
                                if (specialEvent == 0) {
                                    MapleMonster mob;
                                    if (mini) {
                                        mob = MapleLifeFactory.getMonsterScale(mid, 2);
                                        mob.normal = true;
                                    } else {
                                        mob = MapleLifeFactory.getMonster(mid);
                                    }
                                    mob.setEventScript(false);
                                    Point pos;
                                    if (spawner.isMobile()) {
                                        pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                                    } else {
                                        pos = new Point(spawner.getPosition());
                                    }
                                    spawner.spawnMonster(mob, this, pos, forced);
                                }
                                spawnAttempts = 0;
                            }
                        }
                        /*
                        if (count < max) {
                            while (count < max) {
                                SpawnPoint spawner = sp.get(currentSpawn);
                                int mid = spawner.getId();
                                if (spawner.shouldSpawn()) {
                                    if (specialEvent == 0) {
                                        MapleMonster mob;
                                        if (mini) {
                                            mob = MapleLifeFactory.getMonsterScale(mid, 2);
                                            mob.normal = true;
                                        } else {
                                            mob = MapleLifeFactory.getMonster(mid);
                                        }
                                        mob.setEventScript(false);
                                        Point pos;
                                        if (spawner.isMobile()) {
                                            pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                                        } else {
                                            pos = new Point(spawner.getPosition());
                                        }
                                        spawner.spawnMonster(mob, this, pos, forced);
                                    }
                                    spawnAttempts = 0;
                                } else {
                                    spawnAttempts++;
                                    if (spawnAttempts >= sp.size()) {
                                        break;
                                    }
                                }
                                currentSpawn++;
                                currentSpawn %= spawncount;
                                count++;
                            }
                        }
                         */
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public void forceRespawn() {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sps = SpawnPoints;
                if (!sps.isEmpty()) {
                    Collections.shuffle(sps);
                    int max = spawnCap();
                    if (specialEvent == 7) {
                        max = 40;
                    }
                    boolean forced = false;
                    Point tpos = null;
                    if (getTotem(4) != null) {
                        //System.out.println("test");
                        tpos = getTotem(4).getPosition();
                        forced = true;
                    }
                    int count = spawnedMonstersOnMap.get();
                    //System.out.println("count: " + count + " - Monsters: " + countMonsters());
                    if (count < max) {
                        for (SpawnPoint sp : sps) {
                            Point pos;
                            if (mini) {
                                MapleMonster mob = MapleLifeFactory.getMonsterScale(sp.getId(), 2);
                                mob.normal = true;
                                if (!forced) {
                                    pos = sp.getPosition();
                                } else {
                                    pos = new Point(tpos.x + (Randomizer.random(-20, 20)), tpos.y - 2);
                                    mob.setFHMapData(this, pos);
                                }
                                sp.spawnMonster(mob, this, pos);
                            } else {
                                MapleMonster mob;
                                if (this.getId() == 5001) {
                                    mob = MapleLifeFactory.getMonster(sp.getId(), 1, 1);
                                } else {
                                    mob = MapleLifeFactory.getMonster(sp.getId());
                                }
                                mob.setEventScript(false);
                                if (!forced) {
                                    pos = sp.getPosition();
                                } else {
                                    pos = new Point(tpos.x + (Randomizer.random(-20, 20)), tpos.y - 2);
                                    mob.setFHMapData(this, pos);
                                }
                                sp.spawnMonster(mob, this, pos);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void forceRespawn(int level, int tier, boolean drops) {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sps = SpawnPoints;
                if (!sps.isEmpty()) {
                    Collections.shuffle(sps);
                    int count = spawnedMonstersOnMap.get();
                    if (count == 0) {
                        for (SpawnPoint sp : sps) {
                            MapleMonster mob = MapleLifeFactory.getKaoticMonster(sp.getMobId(), level, tier, false, false, drops);
                            mob.setEventScript(false);
                            Point pos = sp.getPosition();
                            mob.setFHMapData(this, pos);
                            sp.spawnMonster(mob, this, pos);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void forceRespawn(int mobid, int level, int tier, boolean drops) {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sps = SpawnPoints;
                if (!sps.isEmpty()) {
                    Collections.shuffle(sps);
                    int count = spawnedMonstersOnMap.get();
                    //if (count == 0) {
                    for (SpawnPoint sp : sps) {
                        if (count < spawnCap() && sp.shouldSpawnMission()) {
                            MapleMonster mob = MapleLifeFactory.getKaoticMonster(mobid, level, tier, false, false, drops);
                            mob.setEventScript(false);
                            mob.setBossStats(false);
                            Point pos = sp.getPosition();
                            mob.setFHMapData(this, pos);
                            sp.spawnMonster(mob, this, pos);
                        }
                    }
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void forceRespawn(int mobid, int level, int tier, boolean drops, boolean stats, int chance) {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sps = SpawnPoints;
                if (!sps.isEmpty()) {
                    Collections.shuffle(sps);
                    int count = spawnedMonstersOnMap.get();
                    //if (count == 0) {
                    for (SpawnPoint sp : sps) {
                        if (Randomizer.random(0, 100) <= chance) {
                            if (count < 50 && sp.shouldSpawnMission()) {
                                MapleMonster mob = MapleLifeFactory.getKaoticMonster(mobid, level, tier, false, false, drops);
                                mob.setEventScript(false);
                                mob.setBossStats(stats);
                                Point pos = sp.getPosition();
                                mob.setFHMapData(this, pos);
                                sp.spawnMonster(mob, this, pos);
                            }
                        }
                    }
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void forceCapRespawn(EventInstanceManager eim, int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed) {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sps = SpawnPoints;
                if (!sps.isEmpty()) {
                    Collections.shuffle(sps);
                    int count = spawnedMonstersOnMap.get();
                    //if (count == 0) {
                    for (SpawnPoint sp : sps) {
                        if (count < spawnCap() && sp.shouldSpawnMission()) {
                            MapleMonster mob = eim.getKaoticMonster(id, level, scale, bar, link, drops, script, fixed);
                            mob.setBossStats(true);
                            Point pos = sp.getPosition();
                            mob.setFHMapData(this, pos);
                            sp.spawnMonster(mob, this, pos);
                        }
                    }
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void forceCapRandomRespawn(EventInstanceManager eim, int id, int level, int minscale, int maxscale, boolean bar, boolean link, boolean drops, boolean script, long fixed) {
        try {
            if (!getPQlock() && !banned(this.getId())) {
                var sps = SpawnPoints;
                if (!sps.isEmpty()) {
                    Collections.shuffle(sps);
                    int count = spawnedMonstersOnMap.get();
                    //if (count == 0) {
                    for (SpawnPoint sp : sps) {
                        if (count < spawnCap() && sp.shouldSpawnMission()) {
                            MapleMonster mob = eim.getKaoticMonster(id, level, Randomizer.random(minscale, maxscale), bar, link, drops, script, fixed);
                            mob.setEventScript(false);
                            mob.setBossStats(false);
                            Point pos = sp.getPosition();
                            mob.setFHMapData(this, pos);
                            sp.spawnMonster(mob, this, pos);
                        }
                    }
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public MaplePortal findClosestMobSpawnpoint(Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            double distance = portal.getPosition().distance(from);
            if (portal.getType() == 0 && distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        if (closest == null) {
            return portals.get(0);
        }
        return closest;
    }

    public void missionSpawn() {
        try {
            var sp = SpawnPoints;
            if (!sp.isEmpty()) {
                for (SpawnPoint spawn : sp) {
                    MapleCharacter chr = findClosestPlayer(spawn.getPosition());
                    double distance = spawn.getPosition().distance(chr.getPosition());

                    if (distance < 500) {
                        if (spawn.shouldSpawnMission()) {
                            spawnMonsterOnGroundBelow(spawn.getMonster(), spawn.getPosition());
                            if (spawn.getMonster().getStats().getScale() > 5) {
                                spawn.addCount(5);
                            } else {
                                spawn.addCount(1);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void missionKillSpawn() {
        try {
            List<MapleMonster> mobs = getAllMonsters();
            if (!mobs.isEmpty()) {
                for (MapleMonster mob : mobs) {
                    MapleCharacter chr = findClosestPlayer(mob.getPosition());
                    double distance = mob.getPosition().distance(chr.getPosition());
                    if (distance > 1200) {
                        killMonster(mob);
                    }
                }
                mobs.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public boolean allMonsterDead() {
        return getAllMonsters().isEmpty();
    }

    public void removeAllSpawn() {
        for (MapleMonster mob : getAllMonsters()) {
            removeMonster(mob, false);
        }
    }

    public void spawnMob(EventInstanceManager eim, SpawnPoint spawn, int level, int scale) {
        for (int i = 0; i < 10; i++) {
            MapleMonster nMob = eim.getKaoticMonster(spawn.getMonsterId(), level, scale, false, false, false);
            nMob.setSpawnData(spawn);
            spawnMonsterOnGroundBelow(nMob, spawn.getPosition());
            if (!nMob.getStats().getMobile()) {
                break;
            }
        }
    }

    public void mapSpawn(int level, int scale) {
        try {
            List<SpawnPoint> sp = getAllSpawnData();
            if (!sp.isEmpty()) {
                EventInstanceManager eim = this.getEventInstance();
                if (eim != null) {
                    locked = true;
                    killAllMonsters(false);
                    eim.setSpawnCap(getId(), 999);
                    for (SpawnPoint spawn : sp) {
                        spawnMob(eim, spawn, level, scale);
                    }
                } else {
                    System.out.println("error??");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parkSpawn(int level, int scale) {
        parkSpawn(level, scale, true);
    }

    public void parkSpawn(int level, int scale, boolean cap) {
        try {
            var sp = SpawnPoints;
            if (!sp.isEmpty()) {
                EventInstanceManager eim = this.getEventInstance();
                if (eim != null) {
                    int sps = sp.size() * 8;
                    int limit = 8;
                    if (sps < 100) {
                        limit = 10;
                    }
                    if (sps > 200) {
                        limit = 6;
                    }
                    if (sps > 400) {
                        limit = 4;
                    }
                    for (SpawnPoint spawn : sp) {
                        int mid = spawn.getMonsterId();
                        if (isMPFinalBoss(mid)) {
                            MapleMonster nMob = eim.getMonster(mid, level, scale + 2, true, true, false, false, cap ? 0 : 25);
                            nMob.setFinalBoss(true);
                            nMob.setMonsterEventType(5);
                            //nMob.setSpawnData(spawn);

                            Point pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                            nMob.setFHMapData(this, pos);
                            spawn.spawnMonster(nMob, this, pos);
                        } else {
                            if (isMPBoss(mid)) {
                                MapleMonster nMob = eim.getMonster(mid, level, scale + 1, true, true, false, false, cap ? 0 : 5);
                                nMob.setMonsterEventType(3);
                                //nMob.setSpawnData(spawn);
                                Point pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                                nMob.setFHMapData(this, pos);
                                spawn.spawnMonster(nMob, this, pos);
                            } else {
                                for (int i = 0; i < limit; i++) {
                                    MapleMonster nMob = eim.getMonster(mid, level, scale, false, true, true, false, cap ? 0 : 1);
                                    nMob.setMonsterEventType(1);
                                    //nMob.setSpawnData(spawn);
                                    Point pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                                    nMob.setFHMapData(this, pos);
                                    spawn.spawnMonster(nMob, this, pos);
                                }
                            }
                        }

                    }
                } else {
                    System.out.println("error??");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parkKaoticSpawn(int level, int scale) {
        try {
            var sp = SpawnPoints;
            if (!sp.isEmpty()) {
                EventInstanceManager eim = this.getEventInstance();
                if (eim != null) {
                    int limit = 8;
                    int sps = sp.size() * 8;
                    if (sps < 100) {
                        limit = 10;
                    }
                    if (sps > 200) {
                        limit = 6;
                    }
                    if (sps > 400) {
                        limit = 4;
                    }
                    for (SpawnPoint spawn : sp) {
                        int mid = spawn.getMonsterId();
                        if (isMPFinalBoss(mid)) {
                            MapleMonster nMob = eim.getKaoticMonster(mid, level, scale + 2, true, true, false, false, 25);
                            nMob.setFinalBoss(true);
                            nMob.setMonsterEventType(5);
                            Point pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                            nMob.setFHMapData(this, pos);
                            spawn.spawnMonster(nMob, this, pos);
                        } else {
                            if (isMPBoss(mid)) {
                                MapleMonster nMob = eim.getKaoticMonster(mid, level, scale + 1, true, true, false, false, 5);
                                nMob.setMonsterEventType(3);
                                Point pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                                nMob.setFHMapData(this, pos);
                                spawn.spawnMonster(nMob, this, pos);
                            } else {
                                for (int i = 0; i < limit; i++) {
                                    MapleMonster nMob = eim.getKaoticMonster(mid, level, scale, false, true, false);
                                    nMob.setMonsterEventType(1);
                                    Point pos = new Point(getRandomMonsterSpawnPoint().getPosition());
                                    nMob.setFHMapData(this, pos);
                                    spawn.spawnMonster(nMob, this, pos);
                                }
                            }
                        }

                    }
                } else {
                    System.out.println("error??");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMPBoss(int id) {
        switch (id) {
            case 50007:
            case 50017:
            case 9800003://MP - metal golem
            case 9800008://MP - spirit of rock
            case 9800009://MP - crisom of rock
            case 9800016://MP - snow witch
            case 9800022://MP - 
            case 9800023://MP - 
            case 9800024://MP - 
            case 9800025://MP - 
            case 9800031://MP - 
            case 9800037://MP - 
            case 9800038://MP - 
            case 9800044://MP - 
            case 9800048://MP - 
            case 9800050://MP - 
            case 9800056://MP - 
            case 9800057://MP - 
            case 9800058://MP - 
            case 9800060://MP - 
            case 9800063://MP - 
            case 9800065://MP -
            case 9800066://MP - 
            case 9800072://MP - 
            case 9800075://MP - 
            case 9800076://MP - 
            case 9800077://MP - 
            case 9800082://MP - 
            case 9800083://MP - 
            case 9800084://MP - 
            case 9800090://MP - 
            case 9800091://MP - 
            case 9800099://MP - 
            case 9800105://MP - 
            case 9800108://MP - 
            case 9800109://MP - 
            case 9800113://MP - 
            case 9800119://MP - 
            case 9800120://MP - 
            case 9800122://MP - 
            case 9800123://MP - 
                return true;
        }
        return false;
    }

    public boolean isMPFinalBoss(int id) {
        switch (id) {
            //tier 1
            case 50008:
            case 50018:
            case 9800003://Metal Golem
            case 9800009://Crimson Rock
            case 9800016://Snow Witch
            case 9800024://Seruf
            case 9800025://Seruf phase 2
            case 9800031://Balrog
            case 9800037://ghost 
            case 9800044://Zeno
            case 9800050://deet
            case 9800056://Golem phase 1
            case 9800057://Golem phase 2
            case 9800058://Golem phase 3
            case 9800066://Centipede
            case 9800072://Thanos
            case 9800084://Airship
            case 9800091://Egos
            case 9800099://Ani
            case 9800105://Levi 
            case 9800113://Lyka
            case 9800124://CK boss
            case 9402206://crystal - eluna
            case 9402196://crystal - eluna
                return true;
        }
        return false;
    }

    public void parkFelSpawn(int level, int scale) {
        try {
            var sp = SpawnPoints;
            if (!sp.isEmpty()) {
                Collections.shuffle(sp);
                EventInstanceManager eim = this.getEventInstance();
                if (eim != null) {
                    for (SpawnPoint spawn : sp) {
                        int mid = spawn.getMonsterId();
                        if (isMPFinalBoss(mid)) {
                            MapleMonster nMob = eim.getKaoticMonster(mid, level, Randomizer.Max(scale + 2, 99), true, true, false, false, 25);
                            nMob.setMonsterEventType(5);
                            nMob.setFinalBoss(true);
                            Point pos = getRandomMonsterSpawnPointPos();
                            nMob.setFHMapData(this, pos);
                            spawn.spawnMonster(nMob, this, pos);
                        } else {
                            if (isMPBoss(mid)) {
                                MapleMonster nMob = eim.getKaoticMonster(mid, level, Randomizer.Max(scale + 1, 99), true, true, false, false, 5);
                                nMob.setMonsterEventType(3);
                                Point pos = getRandomMonsterSpawnPointPos();
                                nMob.setFHMapData(this, pos);
                                spawn.spawnMonster(nMob, this, pos);
                            } else {
                                int count = 8;
                                while (count > 0) {
                                    MapleMonster nMob = eim.getKaoticMonster(mid, level, Randomizer.Max(scale, 99), false, true, false);
                                    nMob.setMonsterEventType(1);
                                    Point pos = getRandomMonsterSpawnPointPos();
                                    nMob.setFHMapData(this, pos);
                                    spawn.spawnMonster(nMob, this, pos);
                                    count--;
                                }
                            }
                        }

                    }
                } else {
                    System.out.println("error??");
                }
            } else {
                //System.out.println("spawn");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parkFelSpawn(int level, int scale, int max) {
        try {
            var sp = SpawnPoints;
            if (!sp.isEmpty()) {
                Collections.shuffle(sp);
                EventInstanceManager eim = this.getEventInstance();
                if (eim != null) {
                    for (SpawnPoint spawn : sp) {
                        int mid = spawn.getMonsterId();
                        if (isMPFinalBoss(mid)) {
                            MapleMonster nMob = eim.getKaoticMonster(mid, level, Randomizer.Max(scale + 2, max), true, true, false, false, 25);
                            nMob.setMonsterEventType(5);
                            nMob.setFinalBoss(true);
                            Point pos = getRandomMonsterSpawnPointPos();
                            nMob.setFHMapData(this, pos);
                            spawn.spawnMonster(nMob, this, pos);
                        } else {
                            if (isMPBoss(mid)) {
                                MapleMonster nMob = eim.getKaoticMonster(mid, level, Randomizer.Max(scale + 1, max), true, true, false, false, 5);
                                nMob.setMonsterEventType(3);
                                Point pos = getRandomMonsterSpawnPointPos();
                                nMob.setFHMapData(this, pos);
                                spawn.spawnMonster(nMob, this, pos);
                            } else {
                                int count = 8;
                                while (count > 0) {
                                    MapleMonster nMob = eim.getKaoticMonster(mid, level, Randomizer.Max(scale, max), false, true, false);
                                    nMob.setMonsterEventType(1);
                                    Point pos = getRandomMonsterSpawnPointPos();
                                    nMob.setFHMapData(this, pos);
                                    spawn.spawnMonster(nMob, this, pos);
                                    count--;
                                }
                            }
                        }

                    }
                } else {
                    System.out.println("error??");
                }
            } else {
                //System.out.println("spawn");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SwitchSpawn(int level, int scale) {
        try {
            var sp = SpawnPoints;

            if (!sp.isEmpty()) {
                EventInstanceManager eim = this.getEventInstance();
                if (eim != null) {
                    for (SpawnPoint spawn : sp) {
                        MapleMonster mob = spawn.getMonster();
                        Boolean Switch_Small = false;
                        Boolean Switch_Big = false;
                        int lvl = level;
                        if (mob.getId() == 51000 || mob.getId() == 51001 || mob.getId() == 51002 || mob.getId() == 51003) {
                            Switch_Small = true;
                            if (mob.getId() == 51000) {
                                lvl += 10;
                            }
                            if (mob.getId() == 51001) {
                                lvl += 15;
                            }
                            if (mob.getId() == 51002) {
                                lvl += 20;
                            }
                            if (mob.getId() == 51003) {
                                lvl += 25;
                            }
                        }
                        if (mob.getId() == 51004 || mob.getId() == 51005 || mob.getId() == 51006 || mob.getId() == 51007) {
                            Switch_Big = true;
                            if (mob.getId() == 51004) {
                                lvl += 10;
                            }
                            if (mob.getId() == 51005) {
                                lvl += 20;
                            }
                            if (mob.getId() == 51006) {
                                lvl += 35;
                            }
                            if (mob.getId() == 51007) {
                                lvl += 50;
                            }
                        }

                        if (Switch_Big) {
                            MapleMonster nMob = eim.getKaoticMonster(mob.getId(), lvl, scale + 2, true, false, false, false, 250 * scale);
                            nMob.setMonsterEventType(5);
                            nMob.setSpawnData(spawn);
                            spawnMonsterOnGroundBelow(nMob, nMob.getPosition());
                        } else {
                            if (Switch_Small) {
                                MapleMonster nMob = eim.getKaoticMonster(mob.getId(), lvl, scale + 1, false, false, false, false, 250 * scale);
                                nMob.setMonsterEventType(3);
                                nMob.setSpawnData(spawn);
                                spawnMonsterOnGroundBelow(nMob, nMob.getPosition());
                            } else {
                                MapleMonster nMob = eim.getKaoticMonster(mob.getId(), lvl, scale, false, false, false, false, 250 * scale);
                                nMob.setMonsterEventType(1);
                                nMob.setSpawnData(spawn);
                                spawnMonsterOnGroundBelow(nMob, nMob.getPosition());
                            }
                        }

                    }
                } else {
                    System.out.println("error??");
                }
            } else {
                //System.out.println("spawn");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static interface DelayedPacketCreation {

        void sendPackets(MapleClient c);
    }

    public String getSnowballPortal() {
        int[] teamss = new int[2];
        for (MapleCharacter chr : getAllPlayers()) {
            if (chr.getTruePosition().y > -80) {
                teamss[0]++;
            } else {
                teamss[1]++;
            }
        }
        if (teamss[0] > teamss[1]) {
            return "st01";
        } else {
            return "st00";
        }
    }

    public boolean isDisconnected(int id) {
        return dced.contains(Integer.valueOf(id));
    }

    public void addDisconnected(int id) {
        dced.add(Integer.valueOf(id));
    }

    public void resetDisconnected() {
        dced.clear();
    }

    public boolean startSpeedRun() {
        final MapleSquad squad = getSquadByMap();
        if (squad != null) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr.getName().equals(squad.getLeaderName()) && !chr.isIntern()) {
                    startSpeedRun(chr.getName());
                    return true;
                }
            }
        }
        return false;
    }

    public void startSpeedRun(String leader) {
        speedRunStart = System.currentTimeMillis();
        speedRunLeader = leader;
    }

    public void endSpeedRun() {
        speedRunStart = 0;
        speedRunLeader = "";
    }

    public void getRankAndAdd(String leader, String time, ExpeditionType type, long timz, Collection<String> squad) {
        try {
            long lastTime = SpeedRunner.getSpeedRunData(type) == null ? 0 : SpeedRunner.getSpeedRunData(type).right;
            //if(timz > lastTime && lastTime > 0) {
            //return;
            //}
            //Pair<String, Map<Integer, String>>
            StringBuilder rett = new StringBuilder();
            if (squad != null) {
                for (String chr : squad) {
                    rett.append(chr);
                    rett.append(",");
                }
            }
            String z = rett.toString();
            if (squad != null) {
                z = z.substring(0, z.length() - 1);
            }
            try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO speedruns(`type`, `leader`, `timestring`, `time`, `members`) VALUES (?,?,?,?,?)")) {
                ps.setString(1, type.name());
                ps.setString(2, leader);
                ps.setString(3, time);
                ps.setLong(4, timz);
                ps.setString(5, z);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error getting character default" + e);
            }
            if (lastTime == 0) { //great, we just add it
                SpeedRunner.addSpeedRunData(type, SpeedRunner.addSpeedRunData(new StringBuilder(SpeedRunner.getPreamble(type)), new HashMap<Integer, String>(), z, leader, 1, time), timz);
            } else {
                //i wish we had a way to get the rank
                //TODO revamp
                SpeedRunner.removeSpeedRunData(type);
                SpeedRunner.loadSpeedRunData(type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getSpeedRunStart() {
        return speedRunStart;
    }

    public final void disconnectAll() {
        //System.out.println("MAP DC ALL");
        for (MapleCharacter chr : getCharacters()) {
            if (!chr.isGM()) {
                chr.getClient().disconnect(true, false);
                chr.getClient().getSession().close();
            }
        }
    }

    public final void resetNPCs() {
        removeNpc(-1);
    }

    public final void resetPQ(int level) {
        resetFully();
        for (MapleMonster mons : getAllMonsters()) {
            mons.changeLevel(level, mons.getStats().getScale(), true);
        }
    }

    public final void resetFully() {
        resetFully(true);
    }

    public final void resetFully(final boolean respawn) {
        if (!getMonsters().isEmpty()) {
            if (respawn) {
                killAllMonsters(true);
                respawn();
            }
        }
        if (!getReactors().isEmpty()) {
            reloadReactors();
        }
        if (!getItems().isEmpty()) {
            removeDrops();
        }
        if (!getNpcs().isEmpty()) {
            resetNPCs();
        }
        resetPortals();
        environment.clear();
    }

    public final void cancelSquadSchedule(boolean interrupt) {
        squadTimer = false;
        checkStates = true;
        if (squadSchedule != null) {
            squadSchedule.cancel(interrupt);
            squadSchedule = null;
        }
    }

    public final void removeDrops() {
        for (MapleMapItem item : getAllItems()) {
            item.expire(this);
        }
    }

    public final boolean makeCarnivalReactor(final int team, final int num) {
        final MapleReactor old = getReactorByName(team + "" + num);
        if (old != null && old.getState() < 5) { //already exists
            return false;
        }
        Point guardz = null;
        final List<MapleReactor> react = getAllReactor();
        for (Pair<Point, Integer> guard : nodes.getGuardians()) {
            if (guard.right == team || guard.right == -1) {
                boolean found = false;
                for (MapleReactor r : react) {
                    if (r.getTruePosition().x == guard.left.x && r.getTruePosition().y == guard.left.y && r.getState() < 5) {
                        found = true;
                        break; //already used
                    }
                }
                if (!found) {
                    guardz = guard.left; //this point is safe for use.
                    break;
                }
            }
        }
        if (guardz != null) {
            final MapleReactor my = new MapleReactor(MapleReactorFactory.getReactor(9980000 + team), 9980000 + team);
            my.setState((byte) 1);
            my.setName(team + "" + num); //lol
            //with num. -> guardians in factory
            spawnReactorOnGroundBelow(my, guardz);
            final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            for (MapleMonster mons : getAllMonsters()) {
                if (mons.getCarnivalTeam() == team) {
                    skil.getSkill().applyEffect(null, mons, false);
                }
            }
        }
        return guardz != null;
    }

    public final void blockAllPortal() {
        for (MaplePortal p : portals.values()) {
            p.setPortalState(false);
        }
    }

    public boolean getAndSwitchTeam() {
        return getCharactersSize() % 2 != 0;
    }

    public void setSquad(MapleSquadType s) {
        this.squad = s;

    }

    public int getChannel() {
        return channel;
    }

    public int getConsumeItemCoolTime() {
        return consumeItemCoolTime;
    }

    public void setConsumeItemCoolTime(int ciit) {
        this.consumeItemCoolTime = ciit;
    }

    public void setPermanentWeather(int pw) {
        this.permanentWeather = pw;
    }

    public int getPermanentWeather() {
        return permanentWeather;
    }

    public void checkStates(final String chr) {
        if (!checkStates) {
            return;
        }
        final MapleSquad sqd = getSquadByMap();
        final EventManager em = getEMByMap();
        final int size = getCharactersSize();
        if (sqd != null && sqd.getStatus() == 2) {
            sqd.removeMember(chr);
            if (em != null) {
                if (sqd.getLeaderName().equalsIgnoreCase(chr)) {
                    em.setProperty("leader", "false");
                }
                if (chr.equals("") || size == 0) {
                    em.setProperty("state", "0");
                    em.setProperty("leader", "true");
                    cancelSquadSchedule(!chr.equals(""));
                    sqd.clear();
                    sqd.copy();
                }
            }
        }
        if (em != null && em.getProperty("state") != null && (sqd == null || sqd.getStatus() == 2) && size == 0) {
            em.setProperty("state", "0");
            if (em.getProperty("leader") != null) {
                em.setProperty("leader", "true");
            }
        }
        if (speedRunStart > 0 && size == 0) {
            endSpeedRun();
        }
        //if (squad != null) {
        //    final MapleSquad sqdd = ChannelServer.getInstance(channel).getMapleSquad(squad);
        //    if (sqdd != null && chr != null && chr.length() > 0 && sqdd.getAllNextPlayer().contains(chr)) {
        //	sqdd.getAllNextPlayer().remove(chr);
        //	broadcastMessage(CWvsContext.serverNotice(5, "The queued player " + chr + " has left the map."));
        //    }
        //}
    }

    public void setCheckStates(boolean b) {
        this.checkStates = b;
    }

    public void setNodes(final MapleNodes mn) {
        this.nodes = mn;
    }

    public final List<MaplePlatform> getPlatforms() {
        return nodes.getPlatforms();
    }

    public Collection<MapleNodeInfo> getNodes() {
        return nodes.getNodes();
    }

    public MapleNodeInfo getNode(final int index) {
        return nodes.getNode(index);
    }

    public boolean isLastNode(final int index) {
        return nodes.isLastNode(index);
    }

    public final List<Rectangle> getAreas() {
        return nodes.getAreas();
    }

    public final Rectangle getArea(final int index) {
        return nodes.getArea(index);
    }

    public final void changeEnvironment(final String ms, final int type) {
        broadcastMessage(CField.environmentChange(ms, type));
    }

    public final void toggleEnvironment(final String ms) {
        if (environment.containsKey(ms)) {
            moveEnvironment(ms, environment.get(ms) == 1 ? 2 : 1);
        } else {
            moveEnvironment(ms, 1);
        }
    }

    public final void moveEnvironment(final String ms, final int type) {
        broadcastMessage(CField.environmentMove(ms, type));
        environment.put(ms, type);
    }

    public final Map<String, Integer> getEnvironment() {
        return environment;
    }

    public final int getNumPlayersInArea(final int index) {
        return getNumPlayersInRect(getArea(index));
    }

    public final int getNumPlayersInRect(final Rectangle rect) {
        int ret = 0;
        for (MapleCharacter chr : getAllPlayers()) {
            if (rect.contains(chr.getTruePosition())) {
                ret++;
            }
        }
        return ret;
    }

    public final int getNumPlayersItemsInArea(final int index) {
        return getNumPlayersItemsInRect(getArea(index));
    }

    public final int getNumPlayersItemsInRect(final Rectangle rect) {
        int ret = getNumPlayersInRect(rect);

        for (MapleMapItem mmo : getAllItems()) {
            if (rect.contains(mmo.getTruePosition())) {
                ret++;
            }
        }
        return ret;
    }

    public void broadcastGMMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet);
    }

    private void broadcastGMMessage(MapleCharacter source, byte[] packet) {
        if (source == null) {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr.isStaff()) {
                    chr.getClient().announce(packet);
                }
            }
        } else {
            for (MapleCharacter chr : getAllPlayers()) {
                if (chr != source && (chr.getGMLevel() >= source.getGMLevel())) {
                    chr.getClient().announce(packet);
                }
            }
        }
    }

    public final List<Pair<Integer, Integer>> getMobsToSpawn() {
        return nodes.getMobsToSpawn();
    }

    public final List<Integer> getSkillIds() {
        return nodes.getSkillIds();
    }

    public final boolean canSpawn(long now) {
        return lastSpawnTime > 0 && lastSpawnTime + createMobInterval < now;
    }

    public final boolean canHurt(long now) {
        if (lastHurtTime > 0 && lastHurtTime + decHPInterval < now) {
            lastHurtTime = now;
            return true;
        }
        return false;
    }

    public final void resetShammos(final MapleClient c) {
        killAllMonsters(true);
        broadcastMessage(CWvsContext.serverNotice(5, "A player has moved too far from Shammos. Shammos is going back to the start."));
        EtcTimer.getInstance().schedule(() -> {
            if (c.getPlayer() != null) {
                c.getPlayer().changeMap(MapleMap.this, getPortal(0));
                if (getCharacters().size() > 1) {
                    MapScriptMethods.startScript_FirstUser(c, "shammos_Fenter");
                }
            }
        }, 500); //avoid dl
    }

    public int getInstanceId() {
        return instanceid;
    }

    public void setInstanceId(int ii) {
        this.instanceid = ii;
    }

    public int getPartyBonusRate() {
        return partyBonusRate;
    }

    public void setPartyBonusRate(int ii) {
        this.partyBonusRate = ii;
    }

    public short getTop() {
        return top;
    }

    public short getBottom() {
        return bottom;
    }

    public short getLeft() {
        return left;
    }

    public short getRight() {
        return right;
    }

    public void setTop(int ii) {
        this.top = (short) ii;
    }

    public void setBottom(int ii) {
        this.bottom = (short) ii;
    }

    public void setLeft(int ii) {
        this.left = (short) ii;
    }

    public void setRight(int ii) {
        this.right = (short) ii;
    }

    public List<Pair<Point, Integer>> getGuardians() {
        return nodes.getGuardians();
    }

    public DirectionInfo getDirectionInfo(int i) {
        return nodes.getDirection(i);
    }

    public void setBGM(String music) {
        bgm = music;
    }

    public String getBGM() {
        return bgm;
    }

    public void clearDrops() {
        for (MapleMapObject i : getAllItems()) {
            removeMapObject(i, MapleMapObjectType.ITEM);
            broadcastMessage(CField.removeItemFromMap(i.getObjectId(), 0, 0, 0));
        }
    }

    public void clearMapObjects() {
        killAllMonsters(true);
        clearDrops();
        resetReactors();
    }

    public void getKills(int id) {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT `kills` FROM `mapcounter` WHERE `mapid` = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery();) {
                    if (rs.next()) {
                        kills = rs.getInt("kills");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateKills() {
        if (kills > 0) {
            try (Connection con = DatabaseConnection.getWorldConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM mapcounter WHERE mapid = ?")) {
                    ps.setInt(1, this.mapid);
                    ps.executeUpdate();
                    ps.close();
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO mapcounter (mapid, kills) VALUES (?, ?)")) {
                    ps.setInt(1, this.mapid);
                    ps.setInt(2, this.kills);
                    ps.execute();
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateKills(int id) {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM mapcounter WHERE mapid = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();
            }
            if (kills > 0) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO mapcounter (mapid, kills) VALUES (?, ?)")) {
                    ps.setInt(1, id);
                    ps.setInt(2, this.kills);
                    ps.execute();
                    ps.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMapKills() {
        return kills;
    }

    public List<MapleCharacter> getPlayersInRange(Rectangle box) {
        List<MapleCharacter> character = new LinkedList<>();
        for (MapleCharacter chr : getAllPlayers()) {
            if (box.contains(chr.getPosition())) {
                character.add(chr);
            }
        }

        return character;
    }

    public List<MapleMonster> getMonstersInRange(Rectangle box) {
        List<MapleMonster> mobs = new LinkedList<>();
        for (MapleMonster mob : getAllMonsters()) {
            if (box.contains(mob.getPosition())) {
                mobs.add(mob);
            }
        }

        return mobs;
    }

    public void destoryNPC() {
        for (MapleCharacter chr : getAllPlayers()) {
            for (MapleNPC npc : getAllNPCs()) {
                npc.sendDestroyData(chr.getClient());
            }
        }
    }

    public void respawnNPC() {
        for (MapleCharacter chr : getAllPlayers()) {
            for (MapleNPC npc : getAllNPCs()) {
                npc.sendSpawnData(chr.getClient());
            }
        }
    }

    public void resetNPC() {
        for (MapleCharacter chr : getAllPlayers()) {
            for (MapleNPC npc : getAllNPCs()) {
                chr.dropMessage(npc.getName() + " respawned at " + npc.getTruePosition());
                npc.sendDestroyData(chr.getClient());
                npc.sendSpawnData(chr.getClient());
            }
        }
    }

    public SpawnPoint getRandomMonsterSpawnPoint() {
        if (!getMonsterSpawn().isEmpty()) {
            List<SpawnPoint> randomSpawns = getMonsterSpawn();
            return randomSpawns.get(Randomizer.nextInt(randomSpawns.size()));
        } else {
            return null;
        }
    }

    public Point getRandomMonsterSpawnPointPos() {
        if (!getMonsterSpawn().isEmpty()) {
            List<SpawnPoint> randomSpawns = getMonsterSpawn();
            return randomSpawns.get(Randomizer.nextInt(randomSpawns.size())).getPosition();
        } else {
            return null;
        }
    }

    public void updatePlayerStats() {
        for (MapleCharacter chr : getAllPlayers()) {
            chr.recalcLocalStats();
        }
    }

    public void updateAndroid(MapleCharacter chr) {
        if (chr.getAndroid() != null) {
            broadcastMessage(CField.deactivateAndroid(chr.getId()));
            broadcastMessage(CField.updateAndroid(chr, chr.getAndroid(), false, chr.getAndroid().getPos()));
        }
    }

    public void setObjectVisible(String tag, boolean show) {
        broadcastMessage(CField.spawnFlags(Collections.singletonList(new Pair<>(tag, show ? 1 : 0))));
    }

    public final Map<String, Boolean> getObjectFlags() {
        return objectFlags;
    }

    public void setObjectFlag(String msg, Boolean value) {
        this.objectFlags.put(msg, value);
        if (!getAllPlayers().isEmpty()) {
            for (MapleCharacter chr : getAllPlayers()) {
                chr.getObjectVisible();
            }
        }
    }

    public void setObjectFlagRaw(String msg, Boolean value) {
        this.objectFlags.put(msg, value);
    }

    public boolean getObjectFlag(String msg) {
        if (objectFlags.containsKey(msg)) {
            return objectFlags.get(msg);
        }
        return false;
    }

    public void setObjectInt(String msg, int value) {
        this.objectInt.put(msg, value);
    }

    public int getObjectInt(String msg) {
        if (objectInt.containsKey(msg)) {
            return objectInt.get(msg);
        }
        return 0;
    }

    public void setVar(String msg, int value) {
        this.var.put(msg, value);
    }

    public int getVar(String msg) {
        if (var != null && var.containsKey(msg)) {
            return var.get(msg);
        }
        return 0;
    }

    public void setFlag(String msg, boolean value) {
        this.flag.put(msg, value);
    }

    public boolean getFlag(String msg) {
        if (flag != null && flag.containsKey(msg)) {
            return flag.get(msg);
        }
        return false;
    }

    public void getVisibleObjects() {
        if (!getObjectFlags().isEmpty()) {
            for (MapleCharacter chr : getAllPlayers()) {
                chr.getObjectVisible();
            }
        }
    }

    public final void broadcastMapMsg(final String msg, int itemid) {
        for (MapleCharacter chr : getAllPlayers()) {
            chr.getClient().announce(CField.startMapEffect(msg, itemid, true));
        }
    }

    public void showClear() {
        broadcastMessage(CField.MapEff("monsterPark/clear"));
        broadcastMessage(CField.playSound("Party1/Clear"));
        setObjectFlag("exit", false);//door
        clear = true;
    }

    public void lockReactors(boolean toggle) {
        for (MapleReactor r : getAllReactor()) {
            r.setLock(toggle);
        }
    }

    public boolean getClear() {
        return clear;
    }

    public void closeDoor() {
        setObjectFlag("exit", true);//door
    }

    public void setFlagsAQ_1() {
        objectInt.put("Portal_2_", Randomizer.random(1, 2));
        objectInt.put("Portal_3_", Randomizer.random(1, 3));
        objectInt.put("Portal_4_", Randomizer.random(1, 2));
        objectInt.put("Portal_5_", Randomizer.random(1, 3));
        objectInt.put("Portal_6_", Randomizer.random(1, 3));
        objectInt.put("Portal_7_", Randomizer.random(1, 2));
        objectInt.put("Portal_8_", Randomizer.random(1, 3));
        //System.out.println("Doors: " + objectInt.get("Portal_2_") + " - " + objectInt.get("Portal_3_") + " - " + objectInt.get("Portal_4_") + " - " + objectInt.get("Portal_5_") + " - " + objectInt.get("Portal_6_") + " - " + objectInt.get("Portal_7_") + " - " + objectInt.get("Portal_8_"));
    }

    public void setStart(boolean toggle) {
        start = toggle;
    }

    public boolean getStart() {
        return start;
    }

    public void endSpikes(int id) {
        start = false;
        for (MapleCharacter chr : getCharacters()) { //warp all in map
            chr.changeMapbyId(id); //hopefully event will still take care of everything once warp out
        }
    }

    public void shuffleSpikes(boolean show) {
        for (int i = 0; i < 19; i++) {
            String spike = "spike_" + i + "_";
            int random = Randomizer.nextInt(4);
            for (int j = 0; j < 4; j++) {
                if (j == random) {
                    setObjectFlagRaw(spike + j, true);
                } else {
                    setObjectFlagRaw(spike + j, false);
                }
            }
        }
        if (show) {
            getVisibleObjects();
        }
    }

    public void setFlagsAQ_2() {
        ArrayList<String> YellowStars = new ArrayList<>();
        ArrayList<String> BlueStars = new ArrayList<>();
        ArrayList<String> RedStars = new ArrayList<>();
        for (int i = 1; i < 17; i++) {
            YellowStars.add("Y_" + i);
            setObjectFlag("Y_" + i, false);
            BlueStars.add("B_" + i);
            setObjectFlag("B_" + i, false);
            RedStars.add("R_" + i);
            setObjectFlag("R_" + i, false);
        }
        Collections.shuffle(YellowStars);
        Collections.shuffle(BlueStars);
        Collections.shuffle(RedStars);
        int Ystars = Randomizer.random(2, 9);
        int Bstars = Randomizer.random(2, 9);
        int Rstars = Randomizer.random(2, 9);
        for (int i = 0; i < Ystars; i++) {
            setObjectFlag(YellowStars.get(i), true);
        }
        for (int i = 0; i < Bstars; i++) {
            setObjectFlag(BlueStars.get(i), true);
        }
        for (int i = 0; i < Rstars; i++) {
            setObjectFlag(RedStars.get(i), true);
        }
        setObjectInt("Y", Ystars);
        setObjectInt("B", Bstars);
        setObjectInt("R", Rstars);

        setObjectFlag("Yclear", false);
        setObjectFlag("Bclear", false);
        setObjectFlag("Rclear", false);
        //System.out.println("Red: " + objectInt.get("R") + " - Yellow: " + objectInt.get("Y") + " - Blue: " + objectInt.get("B"));
    }

    public void setFlagsBM_2() {
        ArrayList<String> YellowStars = new ArrayList<>();
        ArrayList<String> BlueStars = new ArrayList<>();
        ArrayList<String> RedStars = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            YellowStars.add("Yellow_" + i);
            setObjectFlag("Yellow_" + i, false);
            BlueStars.add("Blue_" + i);
            setObjectFlag("Blue_" + i, false);
            RedStars.add("Red_" + i);
            setObjectFlag("Red_" + i, false);
        }
        Collections.shuffle(YellowStars);
        Collections.shuffle(BlueStars);
        Collections.shuffle(RedStars);
        int extra = 0;
        if (getEventInstance() != null) {
            extra = (int) (getEventInstance().getValue("scale") * 5);
        }
        int Ystars = Randomizer.random(5, 20 + extra);
        int Bstars = Randomizer.random(5, 20 + extra);
        int Rstars = Randomizer.random(5, 20 + extra);
        for (int i = 0; i < Ystars; i++) {
            setObjectFlag(YellowStars.get(i), true);
        }
        for (int i = 0; i < Bstars; i++) {
            setObjectFlag(BlueStars.get(i), true);
        }
        for (int i = 0; i < Rstars; i++) {
            setObjectFlag(RedStars.get(i), true);
        }
        setObjectInt("Yellow", Ystars + 1);
        setObjectInt("Blue", Bstars + 1);
        setObjectInt("Red", Rstars + 1);
        //System.out.println("Red: " + objectInt.get("Red") + " - Yellow: " + objectInt.get("Yellow") + " - Blue: " + objectInt.get("Blue"));
        //getVisibleObjects();
    }

    public void setFlagsAQ_3() {
        setObjectFlag("exit", true);//door
        setObjectFlag("D_1", true);//door
        setObjectFlag("D_2", false);//door
        for (int i = 0; i < 10; i++) {//blue stars
            setObjectFlag("Star_" + i, false);
            setObjectFlag("RStar_" + i, true);
        }
        //system.out.println("Room - 3 : Flags set");
    }

    public void setFlagsAQ_4() {
        List<MapleReactor> reactors = getAllReactor();
        if (!reactors.isEmpty()) {
            int lit = 0;
            if (getEventInstance() != null) {
                lit = (int) getEventInstance().getValue("torch");
            }
            if (lit > 0) {
                Collections.shuffle(reactors);
                for (int i = 0; i < lit; i++) {
                    reactors.get(i).forceHitReactor((byte) 1);
                }
            }
        }
    }

    public List<RewardDropEntry> retrieveRewardDrop(final int id) {
        if (rewardDrops.containsKey(id)) {
            return Collections.unmodifiableList(rewardDrops.get(id));
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<RewardDropEntry> ret = new LinkedList<>();

        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_rewards WHERE drop_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (ii.getItemInformation(rs.getInt("itemid")) != null) {
                            ret.add(new RewardDropEntry(rs.getInt("drop_id"), rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity")));
                        } else {
                            System.out.println("Invalid Item id in Monster Drops: " + rs.getInt("itemid"));
                        }
                    }
                }
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return ret;
        }
        rewardDrops.put(id, ret);
        if (!rewardDrops.isEmpty()) {
            return Collections.unmodifiableList(rewardDrops.get(id));
        }
        return null;
    }

    public void clearRewards() {
        rewardDrops.clear();
    }

    public Item spawnRandomReward(int type) {
        final List<RewardDropEntry> drops = retrieveRewardDrop(type);
        if (!drops.isEmpty()) {
            RewardDropEntry item = null;
            while (item == null) {
                item = drops.get(Randomizer.nextInt(drops.size()));
                if (item.chance >= 1000000 || Randomizer.random(0, 1000000) <= item.chance) {
                    return new Item(item.itemId, (byte) 0, (short) Randomizer.random(item.Minimum, item.Maximum), (byte) 0);
                } else {
                    item = null;
                }
            }
        }
        return null;
    }

    public void spawnReward(int type, int count) {
        final List<RewardDropEntry> drops = retrieveRewardDrop(type);
        MapleCharacter chr = getRandomPlayer();
        if (!drops.isEmpty()) {
            while (count > 0) {
                RewardDropEntry item = drops.get(Randomizer.nextInt(drops.size()));
                if (item.chance >= 1000000 || Randomizer.random(0, 1000000) <= item.chance) {
                    Item idrop = new Item(item.itemId, (byte) 0, (short) Randomizer.random(item.Minimum, item.Maximum), (byte) 0);
                    Point startpos = randomPointOnMap();
                    spawnItemDrop(chr, chr, idrop, startpos, startpos, true, false);
                    count--;
                }
            }
        }
    }

    public void setSkillExp(double value) {
        skillexp = value;
    }

    public double getSkillExp() {
        return skillexp;
    }

    public void setPot(boolean value) {
        pot = value;
    }

    public boolean getPot() {
        return pot;
    }

    public void mapEffect(String text) {
        //"monsterPark/clear"
        broadcastMessage(CField.MapEff(text));
    }

    public void warpEveryone(int id) {
        for (MapleCharacter chr : getCharacters()) { //warp all in map
            chr.changeMapbyId(id); //hopefully event will still take care of everything once warp out
        }
    }

    public void damageMobs(MapleCharacter chr, int monsters, int lines, int range, Point pos) {
        if (allowPets) {
            List<MapleMonster> mobs = getBossesAndMonstersInRange(pos, range, 8);
            if (!mobs.isEmpty()) {
                long curr = System.currentTimeMillis();
                BigDecimal cBase = DamageParse.getBaseDamage(chr);
                for (MapleMonster mob : mobs) {
                    if (mob != null && (mob.spawning + 500) < curr) {
                        if (!mob.isDead() && mob.getStats().selfDestruction() == null && !mob.teleport) {
                            List<Pair<BigInteger, Boolean>> allBigDamageNumbers = new ArrayList<>();
                            BigDecimal booster = new BigDecimal(DamageParse.getDamage(chr, mob, 100, cBase));
                            BigDecimal base = booster.multiply(BigDecimal.valueOf(Randomizer.randomDouble(0.95, 1.05)));
                            if (mob.getStats().isExplosiveReward() || mob.getMonsterEventType() >= 3) {
                                base = base.multiply(BigDecimal.valueOf(4));
                            }
                            if (!mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                                if (mob.getStats().getCapped()) {
                                    base = base.min(new BigDecimal(mob.getStats().damageCap));
                                }
                            } else {
                                base = BigDecimal.ONE;
                            }
                            if (chr.tagged) {
                                base = BigDecimal.ONE;
                            }
                            allBigDamageNumbers.add(new Pair<>(base.toBigInteger(), true)); //m.e. never crits
                            mob.oned = new AttackPair(mob.getObjectId(), allBigDamageNumbers, true);
                            if (!mob.expDisabled()) {
                                chr.gainLevelData(107, (long) Math.pow(mob.getStats().getTier(), 2));
                            }
                            int dir = (110 >>> 15);
                            chr.getClient().announce(CField.gainForce(mob.getObjectId(), Randomizer.nextInt(1000000), 1, chr.getEffects()));
                            broadcastSkill(chr, CField.customSummonDamage(mob, dir, chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin()), true);
                            mob.superBigDamage(chr, base.toBigInteger(), true, 9101010, false);

                        }
                    }
                }
            }
        }
    }

    public void damageMobsInBox(MapleCharacter chr, int monsters, Point pos, int width, int height) {
        //x and y must be halved
        List<MapleMonster> mobs = getBossesAndMonstersInBox(pos, monsters, width, height);
        if (!mobs.isEmpty()) {
            long curr = System.currentTimeMillis();
            BigDecimal cBase = DamageParse.getBaseDamage(chr);
            for (MapleMonster mob : mobs) {
                if (mob != null && (mob.spawning + 500) < curr) {
                    if (!mob.isDead() && mob.getStats().selfDestruction() == null && !mob.teleport) {
                        List<Pair<BigInteger, Boolean>> allBigDamageNumbers = new ArrayList<>();
                        BigDecimal booster = new BigDecimal(DamageParse.getDamage(chr, mob, 100, cBase));
                        BigDecimal base = booster.multiply(BigDecimal.valueOf(Randomizer.randomDouble(0.95, 1.05)));
                        if (mob.getStats().isExplosiveReward() || mob.getMonsterEventType() >= 3) {
                            base = base.multiply(BigDecimal.valueOf(4));
                        }
                        if (!mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                            if (mob.getStats().getCapped()) {
                                base = base.min(new BigDecimal(mob.getStats().damageCap));
                            }
                        } else {
                            base = BigDecimal.ONE;
                        }
                        if (chr.tagged) {
                            base = BigDecimal.ONE;
                        }
                        allBigDamageNumbers.add(new Pair<>(base.toBigInteger(), !chr.tagged)); //m.e. never crits
                        mob.oned = new AttackPair(mob.getObjectId(), allBigDamageNumbers, true);
                        if (!mob.expDisabled()) {
                            chr.gainLevelData(107, (long) Math.pow(mob.getStats().getTier(), 2));
                        }
                        int dir = (110 >>> 15);
                        chr.getClient().announce(CField.gainForce(mob.getObjectId(), Randomizer.nextInt(1000000), 1, chr.getEffects()));
                        broadcastSkill(chr, CField.customSummonDamage(mob, dir, chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin()), true);
                        mob.superBigDamage(chr, base.toBigInteger(), true, 9101010, false);
                    }
                }
            }
        }
    }

    public void damagePalsInBox(MapleCharacter chr, int monsters, Point pos, int width, int height) {
        //x and y must be halved
        List<MapleMonster> mobs = getBossesAndMonstersInBox(pos, monsters, width, height);
        if (!mobs.isEmpty()) {
            long curr = System.currentTimeMillis();
            BigDecimal cBase = DamageParse.getBaseDamage(chr);
            for (MapleMonster mob : mobs) {
                if (mob != null && !mob.isDead() && mob.spawning < curr && mob.getStats().selfDestruction() == null && !mob.teleport) {
                    List<Pair<BigInteger, Boolean>> allBigDamageNumbers = new ArrayList<>();
                    BigDecimal booster = new BigDecimal(DamageParse.getDamage(chr, mob, 100, cBase));
                    BigDecimal base = booster.multiply(BigDecimal.valueOf(Randomizer.randomDouble(0.95, 1.05)));
                    if (mob.getStats().isExplosiveReward() || mob.getMonsterEventType() >= 3) {
                        base = base.multiply(BigDecimal.valueOf(4));
                    }
                    if (!mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                        if (mob.getStats().getCapped()) {
                            base = base.min(new BigDecimal(mob.getStats().damageCap));
                        }
                    } else {
                        base = BigDecimal.ONE;
                    }
                    if (chr.tagged) {
                        base = BigDecimal.ONE;
                    }
                    allBigDamageNumbers.add(new Pair<>(base.toBigInteger(), !chr.tagged)); //m.e. never crits
                    mob.oned = new AttackPair(mob.getObjectId(), allBigDamageNumbers, true);
                    if (!mob.expDisabled()) {
                        chr.gainLevelData(107, (long) Math.pow(mob.getStats().getTier(), 2));
                    }
                    int dir = (110 >>> 15);
                    chr.getClient().announce(CField.gainForce(mob.getObjectId(), Randomizer.nextInt(1000000), 1, chr.getEffects()));

                    broadcastSkill(chr, CField.customSummonDamage(mob, dir, chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin()), true);
                    mob.superBigDamage(chr, base.toBigInteger(), true, 9101010, false);
                }
            }
        }
    }

    public List<MapleMonster> getBossesAndMonstersInRange(Point pos, int range, int count) {
        return getMapObjectsInRange(pos, range, Collections.singletonList(MapleMapObjectType.MONSTER)).stream().map(mmo -> (MapleMonster) mmo).filter(m -> !m.isDead() && !m.isTotem()).sorted((t, o) -> {
            int tVal = t.getStats().getTrueBoss() ? 1 : 0;
            int oVal = o.getStats().getTrueBoss() ? 1 : 0;
            return Integer.compare(tVal, oVal);
        }).limit(count).toList();
    }

    public List<MapleMonster> getBossesAndMonstersInBox(Point pos, int count, int width, int height) {
        return getMapObjectsInRect(calculateBoundingBox(pos, width, height), Collections.singletonList(MapleMapObjectType.MONSTER)).stream().map(mmo -> (MapleMonster) mmo).filter(m -> !m.isDead() && !m.isTotem()).sorted((t, o) -> {
            int tVal = t.getStats().getTrueBoss() ? 1 : 0;
            int oVal = o.getStats().getTrueBoss() ? 1 : 0;
            return Integer.compare(tVal, oVal);
        }).limit(count).toList();
    }

    private Rectangle calculateBoundingBox(Point posFrom, int width, int height) {
        Point mylt, myrb;
        mylt = new Point(posFrom.x - width, posFrom.y - height);
        myrb = new Point(posFrom.x + width, posFrom.y + height);
        final Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public void expMob(MapleMonster mob, BigInteger exp) {
        broadcastMessage(CField.customMobDamage(mob, exp, 9000));
    }

    public void palDamage(MapleMonster mob, long dam, boolean heal) {
        broadcastMessage(CField.customMobDamage(mob, BigInteger.valueOf(dam), heal ? 9011 : 9010));
    }

    public void expPal(MapleMonster mob, BigInteger exp) {
        broadcastMessage(CField.customMobDamage(mob, exp, 9012));
    }

    public void spawnPalEgg(int id, int x, int y, int hits) {
        int eggId = 2000 + PalTemplateProvider.getTemplate(id).element();
        MapleMonster egg = MapleLifeFactory.getKaoticMonster(id > 0 ? eggId : 2000, 30, 1, true, false, false, hits);
        if (id > 0) {
            egg.eggModel = id;
        }
        forceSpawnMonster(egg, calcPointBelow(new Point(x, y)));
    }

    public void spawnPalBall(int id, int x, int y, int hits) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, 30, 1, false, false, true, hits);
        Point nPos = calcPointBelow(new Point(x, y));
        mob.setPosition(nPos);
        forceSpawnMonster(mob, nPos);

    }

    public void spawnPalBall(int id, Point pos, int hits) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, 30, 1, false, false, true, hits);
        Point nPos = calcPointBelow(pos);
        mob.setPosition(nPos);
        forceSpawnMonster(mob, nPos);
    }

    public void spawnWildPalBall(int id, SpawnPoint sp) {
        MapleMonster mob = MapleLifeFactory.getKaoticMonster(id, sp.getLevel(), 1, false, false, true, Randomizer.Max(sp.getLevel(), 100));
        Point nPos = calcPointBelow(sp.getPosition());
        mob.setPosition(nPos);
        forceSpawnMonster(mob, nPos);
    }

    public void spawnPalBallMap(boolean clear) {
        if (clear) {
            killAllMonsters(false);
        }
        try {
            int count = 0;
            for (SpawnPoint sp : getAllMonsterSpawn()) {
                count++;
                int chance = Randomizer.random(1, 4);
                if (chance == 1) {
                    int ball = Randomizer.random(1, 10);
                    if (ball <= 7) {
                        spawnPalBall(2050, sp.getPosition(), Randomizer.random(50, 100));
                    }
                    if (ball == 8 || ball == 9) {
                        spawnPalBall(2051, sp.getPosition(), Randomizer.random(50, 100));
                    }
                    if (ball == 10) {
                        int ball2 = Randomizer.random(1, 10);
                        if (ball2 == 10) {
                            spawnPalBall(2053, sp.getPosition(), Randomizer.random(50, 100));
                        } else {
                            spawnPalBall(2052, sp.getPosition(), Randomizer.random(50, 100));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void spawnPalBallMapNormal(boolean clear) {
        if (clear) {
            killAllMonsters(false);
        }
        try {
            int count = 0;
            for (SpawnPoint sp : getAllMonsterSpawn()) {
                count++;
                int chance = Randomizer.random(1, 5);
                if (chance == 1) {
                    spawnPalBall(sp.getMobId(), sp.getPosition(), Randomizer.random(25, 50));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void spawnPalDataMap() {
        try {
            killAllMonsters(false);
            for (SpawnPoint sp : getAllMonsterSpawn()) {
                if (sp.getMonsterId() == 2020) {
                    int chance = Randomizer.random(1, 10);
                    if (chance == 1) {
                        int eggModel = Randomizer.random(0, 8);
                        MapleMonster egg = MapleLifeFactory.getKaoticMonster(2000 + eggModel, 30, 1, false, false, false, Randomizer.random(50, 100));
                        egg.eggModel = eggModel;
                        Point pos = calcPointBelow(new Point(sp.getPosition().x, sp.getPosition().y));
                        egg.setSpawnData(sp);
                        forceSpawnMonster(egg, pos);
                    }
                }
                if (sp.getMonsterId() >= 2050 && sp.getMonsterId() <= 2099) {
                    int chance = Randomizer.random(1, 5);
                    if (chance == 1) {
                        spawnPalBall(sp.getMonsterId(), sp.getPosition(), Randomizer.random(50, 100));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void spawnPalDataMap(int level, int tier, int cPal, int cBall) {
        try {
            killAllMonsters(false);
            for (SpawnPoint sp : getAllMonsterSpawn()) {
                if (sp.getMonsterId() == 2000 || sp.getMonsterId() == 2020) {
                    int chance = Randomizer.random(1, cPal);
                    if (chance == 1) {
                        int t = Randomizer.random(1, tier);
                        List<Integer> gen = PalTemplateProvider.getPalsbyEvo(t);
                        int id = gen.get(Randomizer.nextInt(gen.size())).shortValue();
                        int eggId = 2000 + PalTemplateProvider.getTemplate(id).element();
                        MapleMonster egg = MapleLifeFactory.getKaoticMonster(eggId, level, 1, false, false, false, 10);
                        egg.eggModel = id;
                        egg.randomEgg = true;
                        Point pos = calcPointBelow(sp.getPosition());
                        egg.setPosition(pos);
                        forceSpawnMonster(egg, pos);
                    }
                }
                if (sp.getMonsterId() >= 2050 && sp.getMonsterId() <= 2099) {
                    int ball = sp.getMonsterId();
                    if (sp.getMonsterId() == 2052) {
                        if (Randomizer.random(1, 5) == 1) {
                            ball = 2053;
                        }
                    } else {
                        if (Randomizer.random(1, cBall) > 1) {
                            continue;
                        }
                    }
                    MapleMonster mob = MapleLifeFactory.getKaoticMonster(ball, level, 1, false, false, true, 25);
                    Point pos = calcPointBelow(sp.getPosition());
                    mob.setPosition(pos);
                    forceSpawnMonster(mob, pos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void spawnPal(int level, int evo, Point pos, int hit, boolean bar) {
        List<Integer> gen = PalTemplateProvider.getPalsbyEvo(evo);
        int id = gen.get(Randomizer.nextInt(gen.size())).shortValue();
        int eggId = 2000 + PalTemplateProvider.getTemplate(id).element();
        MapleMonster egg = MapleLifeFactory.getKaoticMonster(eggId, level, evo, true, false, false, hit);
        egg.eggModel = id;
        egg.randomEgg = true;
        egg.forced = true;
        egg.setPosition(pos);
        forceSpawnMonster(egg, pos);
    }

    public void spawnPalElement(int level, int type, int evo, Point pos, int hit, boolean bar) {
        List<Integer> gen = PalTemplateProvider.getPalsbyEvoLock(type, evo);
        int id = gen.get(Randomizer.nextInt(gen.size())).shortValue();
        int eggId = 2000 + PalTemplateProvider.getTemplate(id).element();
        MapleMonster egg = MapleLifeFactory.getKaoticMonster(eggId, level, evo, true, false, false, hit);
        egg.eggModel = id;
        egg.randomEgg = true;
        egg.forced = true;
        egg.setPosition(pos);
        forceSpawnMonster(egg, pos);
    }

    public void spawnRaidPal(int level, Point pos, int hit, boolean bar) {
        List<Integer> gen = PalTemplateProvider.getPalsbyEvoLock(5);
        int id = gen.get(Randomizer.nextInt(gen.size())).shortValue();
        MapleMonster egg = MapleLifeFactory.getKaoticMonster(2030, level, 5, true, false, false, hit);
        egg.eggModel = id;
        egg.randomEgg = true;
        egg.forced = true;
        egg.setPosition(pos);
        forceSpawnMonster(egg, pos);
    }

    public void spawnPalDataMapType(int level, int tier, int type) {
        try {
            killAllMonsters(false);
            for (SpawnPoint sp : getAllMonsterSpawn()) {
                if (sp.getMonsterId() == 2000 || sp.getMonsterId() == 2020) {
                    int chance = Randomizer.random(1, 5);
                    if (chance == 1) {
                        int t = Randomizer.random(1, tier);
                        List<Integer> gen = PalTemplateProvider.getPalsbyEvoList(type, t);
                        int id = gen.get(Randomizer.nextInt(gen.size())).shortValue();
                        int eggId = 2000 + PalTemplateProvider.getTemplate(id).element();
                        MapleMonster egg = MapleLifeFactory.getKaoticMonster(eggId, level, 1, false, false, false, Randomizer.random(50, 100));
                        egg.eggModel = id;
                        egg.randomEgg = true;
                        Point pos = calcPointBelow(sp.getPosition());
                        egg.setPosition(pos);
                        forceSpawnMonster(egg, pos);
                    }
                }
                if (sp.getMonsterId() >= 2050 && sp.getMonsterId() <= 2099) {
                    int ball = sp.getMonsterId();
                    if (sp.getMonsterId() == 2052) {
                        if (Randomizer.random(1, 4) == 1) {
                            ball = 2053;
                        }
                    } else {
                        if (Randomizer.random(1, 4) > 1) {
                            continue;
                        }
                    }
                    MapleMonster mob = MapleLifeFactory.getKaoticMonster(ball, level, 1, false, false, true, Randomizer.random(25, 50));
                    Point pos = calcPointBelow(sp.getPosition());
                    mob.setPosition(pos);
                    forceSpawnMonster(mob, pos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRandomBattle(MapleCharacter chr, boolean rewards) {
        int bg = Randomizer.random(1, 36);
        int minL = Randomizer.random(1, 100);
        int maxL = Randomizer.random(minL, 100);
        int Iv = Randomizer.random(minL, 250);
        MaplePalBattleManager.randomBattle(chr, bg, minL, minL, maxL, 1, 1, Iv, rewards);
    }

    public void startRandomBattle(MapleCharacter chr, int level, boolean rewards) {
        int bg = Randomizer.random(1, 36);
        int maxL = Randomizer.random(level, (int) Math.floor(level * 1.1));
        int Iv = Randomizer.random(level, 250);
        MaplePalBattleManager.randomBattle(chr, bg, level, level, maxL, 1, 1, Iv, rewards);
    }

    public int getAvgPalLevel() {
        int L = 1;
        if (!getSpawnPoints().isEmpty()) {
            int T = 0;
            for (SpawnPoint sp : getSpawnPoints()) {
                T += sp.getLevel();
            }
            L = Randomizer.MinMax((int) (Math.floor(T / getSpawnPoints().size()) * 0.01), 1, 100);
        }
        return L;
    }

    public void spawnTotem(MapleCharacter chr, int itemId, Point pos) {
        MapleMonster totemMonster = null;
        if (itemId == 2005000 || itemId == 2005001) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(1);
        } else if (itemId == 2005002) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(2);
        } else if (itemId == 2005003 || itemId == 2005004) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(3);
        } else if (itemId == 2005005 || itemId == 2005006) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(4);
        } else if (itemId == 2005007) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(5);
        } else if (itemId == 2005008) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(6);
        } else if (itemId == 2005009) {
            totemMonster = MapleLifeFactory.getMonsterNoAll(7);
        }
        if (totemMonster != null) {
            totemMonster.setOwner(chr);
            totemMonster.setTotem();
            setTotem(chr, totemMonster);
            forceSpawnMonster(totemMonster, pos);
            chr.recalcLocalStats();
            chr.dropTopMessage(totemMonster.getStats().getName() + " has been summoned!");
        }
    }
}
