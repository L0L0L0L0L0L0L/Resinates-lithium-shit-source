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
package client;

import client.MapleTrait.MapleTraitType;
import constants.GameConstants;
import client.inventory.MapleInventoryType;
import client.inventory.MapleInventory;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.ItemFlag;
import client.inventory.MapleRing;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Deque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;

import handling.login.LoginInformationProvider.JobType;
import client.inventory.Equip;
import client.inventory.MapleAndroid;
import client.inventory.MapleImp;
import client.inventory.MapleImp.ImpFlag;
import client.maplepal.MaplePal;
import client.maplepal.MaplePalStorage;
import client.maplepal.PalTemplateProvider;
import static constants.GameConstants.pals;
import constants.ServerConstants.PlayerGMRank;
import constants.ServerSlotItem;
import constants.ServerSlots;
import database.DatabaseConnection;
import database.DatabaseException;

import handling.channel.ChannelServer;
import handling.channel.handler.AttackInfo;
import handling.world.CharacterTransfer;
import handling.world.MapleCharacterLook;
import handling.world.MapleMessenger;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.MapleRaid;
import handling.world.PartyOperation;
import handling.world.PlayerBuffValueHolder;
import handling.world.World;
import handling.world.family.MapleFamily;
import handling.world.family.MapleFamilyBuff;
import handling.world.family.MapleFamilyCharacter;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import scripting.AbstractPlayerInteraction;
import tools.MockIOSession;
import scripting.EventInstanceManager;
import scripting.NPCScriptManager;
import server.MapleAchievements;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.Randomizer;
import server.MapleCarnivalParty;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.maps.AnimatedMapleMapObject;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.FieldLimitType;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import server.CashShop;
import server.MapleAchievement;
import tools.Pair;
import tools.packet.MTSCSPacket;
import tools.packet.MobPacket;
import tools.packet.PetPacket;
import tools.packet.MonsterCarnivalPacket;
import server.MapleCarnivalChallenge;
import server.MapleDamageSkin;
import server.MapleDamageSkins;
import server.MapleInventoryManipulator;
import server.MapleKQuest;
import server.MapleKQuests;
import server.MapleStatEffect.CancelEffectAction;
import server.Timer.BuffTimer;
import server.Timer.MapTimer;
import server.TimerManager;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.Event_PyramidSubway;
import server.maps.MapleDragon;
import server.maps.MapleExtractor;
import server.maps.MapleFoothold;
import server.maps.MapleMapFactory;
import server.maps.MapleMapManager;
import server.maps.MechDoor;
import server.movement.LifeMovementFragment;
import server.shops.MaplePlayerShop;
import tools.ConcurrentEnumMap;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.Triple;
import tools.packet.CField.EffectPacket;
import tools.packet.CField;
import tools.packet.CField.SummonPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.BuddylistPacket;
import tools.packet.CWvsContext.BuffPacket;
import tools.packet.CWvsContext.InfoPacket;
import tools.packet.CWvsContext.InventoryPacket;
import tools.packet.PlayerShopPacket;

public class MapleCharacter extends AnimatedMapleMapObject implements Serializable, MapleCharacterLook {

    private static final long serialVersionUID = 845748950829L;
    private String name, chalktext, BlessOfFairy_Origin, BlessOfEmpress_Origin, teleportname;
    private long lastCombo, lastfametime, keydown_skill, nextConsume, pqStartTime, lastDragonBloodTime, lastBerserkTime,
            lastRecoveryTime, lastSummonTime, mapChangeTime, lastFishingTime, lastFairyTime, lastHPTime, lastMPTime,
            lastFamiliarEffectTime, lastDOTTime, bank, dojo_exp = 0, lasthit = 0;
    private byte gmLevel, gender, initialSpawnPoint = 0, skinColor, guildrank = 5, allianceRank = 5, world, fairyExp,
            numClones, subcategory;
    private short level, mulung_energy, force, availableCP, fatigue, totalCP, hpApUsed, job, remainingSp,
            scrolledPosition;
    private int accountid, id, meso, hair, face, demonMarking, mapid, fame, pvpLvl, pvpExp, pvpPoints, totalWins,
            totalLosses, guildid = 0, fallcounter, maplepoints, acash, chair, itemEffect, points, vpoints, rank = 1,
            rankMove = 0, jobRank = 1, jobRankMove = 0, marriageId, marriageItemId, dotHP, currentrep, totalrep,
            coconutteam, followid, battleshipHP, gachexp, challenge, guildContribution = 0, totallevel, maxlevel = 250, slots = 0, dojo_level = 1, remainingAp, combo;
    private Point old;
    private MonsterFamiliar summonedFamiliar;
    private int[] wishlist, rocks, savedLocations, regrocks, hyperrocks;
    private transient AtomicInteger inst, insd;
    private transient List<LifeMovementFragment> lastres;
    private List<Integer> lastmonthfameids, lastmonthbattleids, extendedSlots;
    private List<MapleDoor> doors;
    private List<MechDoor> mechDoors;
    private List<MaplePet> pets;
    private List<Item> rebuy;
    private MapleImp[] imps;
    private transient WeakReference<MapleCharacter>[] clones;
    private transient List<MapleMonster> controlled;
    private transient List<MapleMapObject> visibleMapObjects;

    private final Lock chrLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_CHR);
    private final Lock evtLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_EVT);
    private final Lock petLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_PET);
    private final Lock prtLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_PRT);
    private final Lock cpnLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CHARACTER_CPN);

    private transient ReentrantReadWriteLock visibleMapObjectsLock;
    private transient ReentrantReadWriteLock summonsLock;
    private transient ReentrantReadWriteLock controlledLock;
    private transient MapleAndroid android;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Map<Integer, String> questinfo;
    private Map<Skill, SkillEntry> skills;
    private transient Map<MapleBuffStat, MapleBuffStatValueHolder> effects;
    private List<MapleSummon> summons = new ArrayList<>();
    private transient Map<Integer, MapleCoolDownValueHolder> coolDowns;
    private transient Map<MapleDisease, MapleDiseaseValueHolder> diseases;
    private CashShop cs;
    private transient Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
    private transient MapleCarnivalParty carnivalParty;
    private BuddyList buddylist;
    private MonsterBook monsterbook;
    private MapleClient client;
    private transient MapleParty party;
    private PlayerStats stats;
    private transient MapleMap map;
    private transient MapleShop shop;
    private transient MapleDragon dragon;
    private transient MapleExtractor extractor;
    private ScheduledFuture<?> diseaseExpireTask = null;
    private transient RockPaperScissors rps;
    private Map<Integer, MonsterFamiliar> familiars;
    private MapleStorage storage;
    private transient MapleTrade trade;
    private MapleMount mount;
    private CopyOnWriteArrayList<Integer> finishedAchievements;
    private CopyOnWriteArrayList<Integer> finishedQuests;
    private MapleMessenger messenger;
    private byte[] petStore;
    private transient MaplePlayerShop playerShop;
    private boolean invincible, canTalk, clone, followinitiator, followon, smega, hasSummon;
    private MapleGuildCharacter mgc;
    private MapleFamilyCharacter mfc;
    private transient EventInstanceManager eventInstance;
    private MapleInventory[] inventory;
    public SkillMacro[] skillMacros = new SkillMacro[5];
    private final EnumMap<MapleTraitType, MapleTrait> traits;
    private MapleKeyLayout keylayout;
    private transient ScheduledFuture<?> mapTimeLimitTask;
    private transient Event_PyramidSubway pyramidSubway = null;
    private transient List<Integer> pendingExpiration = null;
    private transient Map<Skill, SkillEntry> pendingSkills = null;
    private transient Map<Integer, Integer> linkMobs;
    private boolean changed_wishlist, changed_trocklocations, changed_regrocklocations, changed_hyperrocklocations,
            changed_skillmacros, changed_achievements, changed_savedlocations, changed_pokemon, changed_questinfo,
            changed_skills, changed_reports, changed_extendedSlots;
    /* Start of Custom Feature */
 /* All custom shit declare here */
    private int reborns, apstorage;
    private final MapleJob jobClass = MapleJob.BEGINNER;
    public boolean lock = false, warp = false;
    private AtomicInteger exp = new AtomicInteger();
    private BigInteger overexp;
    public long cooldown;
    public long Commandcooldown;
    public List<Equip> eqpOverflow = new ArrayList<>();
    public boolean recycle = false;
    private MaplePartyCharacter mpc = null;
    private long portaldelay = 0;
    public List<Item> itemz = new ArrayList<>();
    public List<Integer> blackItemz = new ArrayList<>();
    private Map<Integer, AtomicLong> etc = new ConcurrentHashMap<Integer, AtomicLong>();
    public boolean online = false;
    private AtomicBoolean mapTransitioning = new AtomicBoolean(true); // player client is currently trying to change
    // maps or log in the game map
    public boolean visualEffects = true, toggleLoot = false, npc = false, toggleEtcLoot = false, jail = false, toggleCool = false;
    public long tower = 0, bosspq = 0, dojo = 0, monsterpark = 0, trails = 0;
    public double XP, DR, IDP, AS, OP, MR, TD, BD, IED, ETC, CD;
    public double LinkXP = 0.0, LinkDR = 0.0, LinkIDP = 0.0, LinkAS = 0.0, LinkOP = 0.0, LinkMR = 0.0, LinkTD = 0.0, LinkBD = 0.0, LinkIED = 0.0, LinkRESIST = 0.0;
    public double AchXP = 0.0, AchDR = 0.0, AchAS = 0.0, AchOP = 0.0, AchMR = 0.0, AchTD = 0.0, AchBD = 0.0, AchIED = 0.0;
    public double QXP = 0.0, QDR = 0.0, QIDP = 0.0, QAS = 0.0, QOP = 0.0, QMR = 0.0, QTD = 0.0, QBD = 0.0, QIED = 0.0;
    public double DXP = 0.0, DDR = 0.0, DAS = 0.0, DOP = 0.0, DMR = 0.0, DTD = 0.0, DBD = 0.0, DIED = 0.0, DCD = 0.0;
    public double DSXP = 0.0, DSDR = 0.0, DSAS = 0.0, DSOP = 0.0, DSMR = 0.0, DSTD = 0.0, DSBD = 0.0, DSIED = 0.0, DSCD = 0.0;
    public int CStr = 0, CDex = 0, CInt = 0, CLuk = 0, CAtk = 0, CMatk = 0, CDef = 0, CMdef = 0, CHp = 0, CMp = 0, CMob = 0, CBoss = 0, CIed = 0, CCd = 0;
    public Equip cubeEquip = null;
    public boolean raidstatus = false;
    public boolean raidLeader = false;
    public boolean invite = false;
    private MapleRaid raid;
    public boolean playerlock = false;
    public long atkcooldown = 0;
    public boolean cash = true;
    public boolean mute = false;
    public boolean immune = false;
    private boolean opened = false, hidden = false;
    private boolean StorageOpened = false;
    public boolean buff = false;
    public boolean mapEvents = true;
    private int numTimes = 1;
    public boolean bot = false, saved = false, afk = false;
    private ScheduledFuture<?> buffTimer = null;
    private ScheduledFuture<?> saveTimer = null;
    private ScheduledFuture<?> botTimer = null;
    public long totalitems = 0;
    public int attacks = 0, tier = 1, hackCount = 0;
    public long vipBuff = 0, dropBuff = 0, etcBuff = 0, mesoBuff = 0, damaged = 0;
    public int lastAttack = 0, lastAttackDelay;
    public boolean boost = true;
    public boolean toggleBuff = false;
    public Pair<Integer, Long> stakeDP = null;
    public boolean EtcLock = false;
    private Map<Integer, Long> questLock = new ConcurrentHashMap<Integer, Long>();
    private boolean saveQuestLock = false;
    public int quest_status = 0, quest_level = 0, quest_item1 = 0, quest_item2 = 0, quest_item3 = 0, quest_item4 = 0, quest_item5 = 0;
    public List<Item> shopItems = new ArrayList<Item>();
    public boolean update = false;
    private int runningStack = 0;
    public boolean canFish = false;
    public boolean eventChangeMap = false, hideAndroid = true, changingMap = false;
    public long portalLock = 0, statLock = 0;
    public long damagecap = 1000000000000000L;
    public boolean itemLock = false;
    public int stamina = 0, time = 0;
    public int monsterHunt = 0;
    public int npcOid = 0;
    public int rolls = 0;
    public ScheduledFuture<?> slotTask = null;
    public boolean isSlot = false;
    public boolean run = false;
    public int damageSkin = 9999;
    public int damageMask = 9999;
    public int damageLevel = 1;
    public int damagePower = 1, battle_limit = 0;
    private Map<Integer, Pair<Integer, Long>> damageSkins;
    public Lock overflowLock = new ReentrantLock();
    public boolean eventExit = false, compressed = false, shopEvent = false;
    public Map<Integer, AtomicLong> rewards = new ConcurrentHashMap<Integer, AtomicLong>();
    private static final int[] itemRew = {4310502, 4310501, 4310500, 4310015, 4310020, 2583007, 2585005, 2586002, 2587001, 2340000, 2049305, 2049189, 4001895, 5220020, 4032521, 4001760, 4310260, 4430003, 4000313, 2000012};
    private static final int[] itemRewCount = {1, 1, 1, 5, 10, 5, 5, 10, 5, 1, 10, 10, 100, 1, 1, 10, 10, 1, 10, 5, 1};
    public int buffed = 0, counter = 0, multi = 1, pots = 0, lives = 0, pAtk = 0;
    public long movespeed = 0, damageExp = 0, shoptime = 0;
    public double weaponType = 0;
    public Map<Integer, Pair<Integer, Long>> level_data = new ConcurrentHashMap<Integer, Pair<Integer, Long>>();
    public Lock level_data_lock = new ReentrantLock();
    public boolean showWin = false;
    private Map<String, Long> var = new ConcurrentHashMap<String, Long>();
    private Map<String, Long> accountvar = new ConcurrentHashMap<String, Long>();
    public boolean boosted = false, tagged = false, shoplock = false, canCombo = false;
    public long monsterDelay = 0, monsterCounter = 0, getBaseTier = 1, reactorDelay = 0;
    public int eDrop = 0;
    public BigDecimal getXpLvl;
    private MaplePalStorage palStorage;
    public long activePalId = 0, shouldUpdate = 0;
    public boolean bonus_loaded = false, battle = false, battlemode = false, global = true, expo = false, showLevel = true;
    public List<Integer> shopPals = new ArrayList<>();
    public List<Integer> maps = new ArrayList<>();
    public List<Equip> equips = new ArrayList<>();
    public int storageId = 0;
    public String hash, ip;
    public boolean offline = false, equipDrops = true, check = false, permVac = false, expMode = true;
    public int jobTier = 0;
    public double jobBonus = 1.0;
    public boolean isBot = false;

    public void handleBuffs() {
        if (!isBot) {
            time++;
            if (time % 30 == 0) {
                if (!isSlot) {
                    getClient().announce(CField.achievementRatio(getStamPerc()));
                }
            }
            if (vipBuff > 0) {
                vipBuff--;
            }
            if (buffed > 0) {
                buffed--;
            }
            if (pAtk > 0) {
                pAtk--;
            }
            if (shoplock && shoptime > 0) {
                shoptime--;
            }
            if (dropBuff > 0) {
                dropBuff--;
            }
            if (toggleBuff) {

                if (etcBuff > 0) {
                    etcBuff--;
                }
            }

            if (reactorDelay > 0) {
                reactorDelay--;
            }
            if (combo > 0 && (getLastCombo() - System.currentTimeMillis()) <= 0) {
                combo = 0;
                getStat().recalcLocalStats(this);
            }
            if (!getLoot()) {
                if (!permVac) {
                    itemVac();
                }
            }
            if (permVac) {
                long perm = getAccVara("Perm_Vac");
                if (perm > 0) {
                    setAccVar("Perm_Vac", perm - 1);
                }
            }
        }
        if (atkcooldown > 0) {
            atkcooldown--;
        }
    }

    public int getBattleLimit() {
        return battle_limit;
    }

    public boolean isStatLock() {
        return statLock > System.currentTimeMillis();
    }

    public long getStatLock() {
        return statLock;
    }

    public double getJobBonus() {
        double bonus = 1.0;
        int[] jobz = {112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3212, 3312, 2312, 3512, 2112};
        for (int i = 0; i < jobz.length; i++) {
            if (getJob() != jobz[i]) {
                bonus += ((double) getVarZero("" + jobz[i]) * 0.025);
            }
        }
        return bonus;
    }

    public double getJobBonusCount() {
        return jobBonus;
    }

    public void updateJobTier() {
        jobTier = (int) getVarZero("" + job);
        jobBonus = getJobBonus();
        getStat().recalcLocalStats(this);
        client.announce(CField.customMainStatUpdate(this));
        client.announce(CField.customStatDetail(this));
    }

    public void setJobTier(int value) {
        jobTier = value;
        jobBonus = getJobBonus();
        getStat().recalcLocalStats(this);
        client.announce(CField.customMainStatUpdate(this));
        client.announce(CField.customStatDetail(this));
    }

    public int getJobTier() {
        return jobTier;
    }

    public double getFullJobBonus() {
        return jobBonus + (jobTier * 0.1);
    }

    public void setStatLock(long value) {
        statLock = value;
    }

    public boolean getBotCheck() {
        return check;
    }

    public void setBotCheck(boolean value) {
        check = value;
    }

    public boolean battleLimit() {
        return false;
    }

    public long getReactorDelay() {
        return reactorDelay;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean toggle) {
        isBot = toggle;
    }

    public int getSkinMask() {
        return damageMask;
    }

    public boolean getGlobal() {
        return global;
    }

    public void setGlobal(boolean toggle) {
        global = toggle;
    }

    public boolean getBattleMode() {
        return getAccVara("battle_mode") > 0;
    }

    public void setBattleMode(boolean toggle) {
        setAccVar("battle_mode", toggle ? 1 : 0);
    }

    public void setSkinMask(int id) {
        damageMask = id;
    }

    public void setReactorDelay(long value) {
        reactorDelay = value;
    }

    public long getDropBuff() {
        return dropBuff;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int value) {
        lives = value;
    }

    public static void shufflePals() {
        pals.clear();
        List<Integer> newPals = new ArrayList<>(PalTemplateProvider.getPals());
        Collections.shuffle(newPals);
        pals = newPals.stream().limit(8).toList();
    }

    public static List<Integer> getPals() {
        return pals;
    }

    public static Integer getPal(int slotid) {
        return pals.get(slotid);
    }

    public boolean getTag() {
        return tagged;
    }

    public void setTag(boolean value) {
        tagged = value;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public void setBoosted(boolean value) {
        boosted = value;
    }

    public boolean getCompressed() {
        return compressed;
    }

    public void toggleCompressed() {
        compressed = !compressed;
    }

    public int getDamagePower() {
        return damagePower;
    }

    public void setWin(boolean toggle) {
        showWin = toggle;
    }

    public boolean showWin() {
        return showWin;
    }

    public void setMulti(int value) {
        multi = value;
    }

    public int getMulti() {
        return multi;
    }

    public void setDamagePower(int value) {
        damagePower = compressed ? value : 1;
    }

    public boolean canDamage() {
        return damaged <= System.currentTimeMillis();
    }

    public void setDamage(long value) {
        damaged = value;
    }

    public void toggleBuff() {
        toggleBuff = !toggleBuff;
    }

    public void toggleETC() {
        toggleEtcLoot = !toggleEtcLoot;
    }

    public boolean getEtc() {
        return toggleEtcLoot;
    }

    public void setQuestLock(int qid, long time) {
        questLock.put(qid, System.currentTimeMillis() + (time * 1000));
    }

    public long getQuestLock(int qid) {
        Long quest = questLock.get(qid);
        if (quest != null) {
            return quest - System.currentTimeMillis();
        }
        return 0;
    }

    public void setVaraLock(String qid, long time) {
        setVar(qid, System.currentTimeMillis() + (time * 1000));
    }

    public void setAccVaraLock(String qid, long time) {
        setAccVar(qid, System.currentTimeMillis() + (time * 1000));
    }

    public long getAccVaraLock(String qid) {
        Long quest = getAccVara(qid);
        long curr = System.currentTimeMillis();
        if (quest != null && quest > curr) {
            return quest - curr;  // Return the remaining time in seconds
        }
        return 0;  // Return 0 if there is no lock or the lock time has passed
    }

    public long getVaraLock(String qid) {
        Long quest = getVarZero(qid);
        if (quest != null) {
            long time = quest - System.currentTimeMillis();
            return time;
        }
        return 0;
    }

    public int getNpcOid() {
        return npcOid;
    }

    public void setNpcOid(int id) {
        npcOid = id;
    }

    public boolean getBuff() {
        return toggleBuff;
    }

    public boolean getEtcLock() {
        return EtcLock;
    }

    public void setEtcLock(boolean lock) {
        EtcLock = lock;
    }

    public void addBuff(int type, int value) {
        int timez = value * 60 * 60 * 24;
        switch (type) {
            case 0 -> //exp
                vipBuff += timez;
            case 1 -> //drop rate
                dropBuff += timez;
            case 2 -> //etc rate
                etcBuff += timez;
            case 3 -> //meso rate
                mesoBuff += timez;
        }
    }

    public void loadBuffs() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `buff` WHERE `accid` = ?");
            ps.setInt(1, getAccountID());
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    vipBuff = rs.getLong("exp");
                    dropBuff = rs.getLong("drop");
                    etcBuff = rs.getLong("etc");
                    mesoBuff = rs.getLong("meso");
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBuffs(final Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO buff (accid, `exp`, `drop`, `etc`, `meso`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `exp` = VALUES(`exp`), `drop` = VALUES(`drop`), `etc` = VALUES(`etc`), `meso` = VALUES(`meso`)")) {
            ps.setInt(1, accountid);
            ps.setLong(2, vipBuff);
            ps.setLong(3, dropBuff);
            ps.setLong(4, etcBuff);
            ps.setLong(5, mesoBuff);
            ps.execute();
        }
    }

    public void saveBuffs() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO buff (accid, `exp`, `drop`, `etc`, `meso`) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE `exp` = VALUES(`exp`), `drop` = VALUES(`drop`), `etc` = VALUES(`etc`), `meso` = VALUES(`meso`)")) {
            ps.setInt(1, accountid);
            ps.setLong(2, vipBuff);
            ps.setLong(3, dropBuff);
            ps.setLong(4, etcBuff);
            ps.setLong(5, mesoBuff);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAttack(int value) {
        attacks = value;
    }

    public int getAttacks() {
        return attacks;
    }

    public void saveTimer() {
        saveTimer = TimerManager.getInstance().register(() -> {
            if (!getLock() && isAlive() && getClient().getCMS() == null) {
                saveToDB();
            }
        }, Randomizer.random(5, 10) * 60 * 1000, 10 * 60 * 1000);
    }

    public void botTimer() {
        botTimer = TimerManager.getInstance().register(() -> {
            if (!bot) {
                if (getEventInstance() == null || (getEventInstance() != null && getEventInstance().getMiniDungeon())) {
                    if (!getLock() && isAlive() && isCombat() && getClient().getCMS() == null) {
                        NPCScriptManager.getInstance().startNPC(getClient(), 9010106, "bot");
                    }
                }
            }
        }, Randomizer.random(60, 120) * 60 * 1000, Randomizer.random(60, 120) * 60 * 1000);
    }

    public void dispose() {
        //if (buffTimer != null) {
        //    buffTimer.cancel(false);
        //}
        if (saveTimer != null) {
            saveTimer.cancel(false);
        }
        //if (botTimer != null) {
        //    botTimer.cancel(false);
        //}
    }

    /* End of Custom Feature */
    private MapleCharacter(final boolean ChannelServer) {
        setStance(0);
        setPosition(new Point(0, 0));

        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type);
        }
        quests = new ConcurrentHashMap<MapleQuest, MapleQuestStatus>(); // Stupid erev quest.
        skills = new ConcurrentHashMap<Skill, SkillEntry>(); // Stupid UAs.
        stats = new PlayerStats();
        traits = new EnumMap<MapleTraitType, MapleTrait>(MapleTraitType.class);
        for (MapleTraitType t : MapleTraitType.values()) {
            traits.put(t, new MapleTrait(t));
        }

        if (ChannelServer) {
            changed_reports = false;
            changed_skills = false;
            changed_achievements = false;
            changed_wishlist = false;
            changed_trocklocations = false;
            changed_regrocklocations = false;
            changed_hyperrocklocations = false;
            changed_skillmacros = false;
            changed_savedlocations = false;
            changed_pokemon = false;
            changed_extendedSlots = false;
            changed_questinfo = false;
            scrolledPosition = 0;
            lastCombo = 0;
            mulung_energy = 0;
            combo = 0;
            force = 0;
            keydown_skill = 0;
            nextConsume = 0;
            pqStartTime = 0;
            fairyExp = 0;
            mapChangeTime = 0;
            lastRecoveryTime = 0;
            lastDragonBloodTime = 0;
            lastBerserkTime = 0;
            lastFishingTime = 0;
            lastFairyTime = 0;
            lastHPTime = 0;
            lastMPTime = 0;
            lastFamiliarEffectTime = 0;
            old = new Point(0, 0);
            coconutteam = 0;
            followid = 0;
            battleshipHP = 0;
            marriageItemId = 0;
            fallcounter = 0;
            challenge = 0;
            dotHP = 0;
            lastSummonTime = 0;
            hasSummon = false;
            invincible = false;
            canTalk = true;
            clone = false;
            followinitiator = false;
            followon = false;
            rebuy = new ArrayList<Item>();
            linkMobs = new HashMap<Integer, Integer>();
            finishedAchievements = new CopyOnWriteArrayList<Integer>();
            finishedQuests = new CopyOnWriteArrayList<Integer>();
            damageSkins = new ConcurrentHashMap<Integer, Pair<Integer, Long>>();
            teleportname = "";
            smega = true;
            petStore = new byte[3];
            for (int i = 0; i < petStore.length; i++) {
                petStore[i] = (byte) -1;
            }
            wishlist = new int[10];
            rocks = new int[10];
            regrocks = new int[5];
            hyperrocks = new int[13];
            imps = new MapleImp[3];
            clones = new WeakReference[5]; // for now
            for (int i = 0; i < clones.length; i++) {
                clones[i] = new WeakReference<MapleCharacter>(null);
            }
            familiars = new LinkedHashMap<Integer, MonsterFamiliar>();
            extendedSlots = new ArrayList<Integer>();
            effects = new ConcurrentEnumMap<MapleBuffStat, MapleBuffStatValueHolder>(MapleBuffStat.class);
            coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
            diseases = new ConcurrentEnumMap<MapleDisease, MapleDiseaseValueHolder>(MapleDisease.class);
            inst = new AtomicInteger(0);// 1 = NPC/ Quest, 2 = Duey, 3 = Hired Merch store, 4 = Storage
            insd = new AtomicInteger(-1);
            keylayout = new MapleKeyLayout();
            doors = new ArrayList<MapleDoor>();
            mechDoors = new ArrayList<MechDoor>();
            controlled = new LinkedList<MapleMonster>();
            controlledLock = new ReentrantReadWriteLock();
            summons = new ArrayList<MapleSummon>();
            summonsLock = new ReentrantReadWriteLock();
            visibleMapObjects = new LinkedList<MapleMapObject>();
            visibleMapObjectsLock = new ReentrantReadWriteLock();
            pendingCarnivalRequests = new LinkedList<MapleCarnivalChallenge>();

            savedLocations = new int[SavedLocationType.values().length];
            for (int i = 0; i < SavedLocationType.values().length; i++) {
                savedLocations[i] = -1;
            }
            questinfo = new LinkedHashMap<Integer, String>();
            pets = new ArrayList<MaplePet>();
        }
    }

    public void toggleBoost() {
        if (boost) {
            boost = false;
            yellowMessage("Drop Rates Disabled.");
        } else {
            boost = true;
            yellowMessage("Drops Rates Enabled.");
        }
        stats.recalcLocalStats(this);
    }

    public void toggleEffects() {
        if (visualEffects) {
            visualEffects = false;
            yellowMessage("Visual Effects disabled.");
        } else {
            visualEffects = true;
            yellowMessage("Visual Effects enabled.");
        }
    }

    public boolean getEffects() {
        return visualEffects;
    }

    public void setMute(boolean toggle) {
        mute = toggle;
    }

    public boolean getMute() {
        return mute;
    }

    public void setMapEvent(boolean toggle) {
        mapEvents = toggle;
    }

    public boolean getMapEvent() {
        return mapEvents;
    }

    public void setCash(boolean toggle) {
        cash = toggle;
    }

    public boolean getCash() {
        return cash;
    }

    public void toggleCash() {
        if (cash) {
            cash = false;
        } else {
            cash = true;
        }
    }

    public void setOnline(boolean toggle) {
        online = toggle;
    }

    public boolean getOnline() {
        return online;
    }

    public void setLock(boolean toggle) {
        playerlock = toggle;
    }

    public boolean getLock() {
        return playerlock;
    }

    public void setAtkCooldown(long x) {
        atkcooldown = x;
    }

    public long getAtkCooldown() {
        return atkcooldown;
    }

    public boolean isCombat() {
        return System.currentTimeMillis() < getAtkCooldown();
    }

    public void setNPC(boolean toggle) {
        npc = toggle;
    }

    public boolean getNPC() {
        return npc;
    }

    public static MapleCharacter getDefault(final MapleClient client, final JobType type) {
        MapleCharacter ret = new MapleCharacter(false);
        ret.client = client;
        ret.map = null;
        ret.exp.get();
        ret.gmLevel = 0;
        ret.job = (short) 900;
        ret.meso = 0;
        ret.level = 1;
        ret.totallevel = 1;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList((byte) 20);

        ret.stats.str = 12;
        ret.stats.dex = 5;
        ret.stats.int_ = 4;
        ret.stats.luk = 4;
        ret.stats.maxhp = 250;
        ret.stats.hp = 250;
        ret.stats.maxmp = 100;
        ret.stats.mp = 100;
        ret.gachexp = 0;

        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ret.client.setAccountName(rs.getString("name"));
                ret.acash = rs.getInt("ACash");
                ret.maplepoints = rs.getInt("mPoints");
                ret.points = rs.getInt("points");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error getting character default" + e);
        }
        return ret;
    }

    public int getExp() {
        return exp.get();
    }

    public final static MapleCharacter ReconstructChr(final CharacterTransfer ct, final MapleClient client, final boolean isChannel) {
        final MapleCharacter ret = new MapleCharacter(true); // Always true, it's change channel
        ret.client = client;
        client.setAccountName(ct.accountname);
        if (!isChannel) {
            ret.client.setChannel(ct.channel);
        }
        ret.id = ct.characterid;
        ret.name = ct.name;
        ret.level = ct.level;
        ret.totallevel = ct.totallevel;
        ret.fame = ct.fame;

        ret.CRand = new PlayerRandomStream();

        ret.stats.str = ct.str;
        ret.stats.dex = ct.dex;
        ret.stats.int_ = ct.int_;
        ret.stats.luk = ct.luk;
        ret.stats.maxhp = ct.maxhp;
        ret.stats.maxmp = ct.maxmp;
        ret.stats.hp = ct.hp;
        ret.stats.mp = ct.mp;

        ret.chalktext = ct.chalkboard;
        ret.gmLevel = ct.gmLevel;
        ret.exp.set(ct.exp);
        ret.overexp = ct.overexp;
        ret.hpApUsed = ct.hpApUsed;
        ret.remainingSp = (short) ct.remainingSp;
        ret.remainingAp = ct.remainingAp;
        ret.meso = ct.meso;
        ret.skinColor = ct.skinColor;
        ret.gender = ct.gender;
        ret.job = ct.job;
        ret.hair = ct.hair;
        ret.face = ct.face;
        ret.demonMarking = ct.demonMarking;
        ret.accountid = ct.accountid;
        ret.totalWins = ct.totalWins;
        ret.totalLosses = ct.totalLosses;
        client.setAccID(ct.accountid);
        ret.mapid = ct.mapid;
        // ret.initialSpawnPoint = ct.initialSpawnPoint;
        ret.world = ct.world;
        ret.guildid = ct.guildid;
        ret.guildrank = ct.guildrank;
        ret.guildContribution = ct.guildContribution;
        ret.allianceRank = ct.alliancerank;
        ret.points = ct.points;
        ret.vpoints = ct.vpoints;
        ret.fairyExp = ct.fairyExp;
        ret.marriageId = ct.marriageId;
        ret.currentrep = ct.currentrep;
        ret.totalrep = ct.totalrep;
        ret.gachexp = ct.gachexp;
        ret.pvpLvl = ct.pvpLvl;
        ret.pvpExp = ct.pvpExp;
        ret.pvpPoints = ct.pvpPoints;
        /* Start of Custom Feature */
        ret.reborns = ct.reborns;
        ret.apstorage = ct.apstorage;
        /* End of Custom Feature */
        ret.makeMFC(ct.familyid, ct.seniorid, ct.junior1, ct.junior2);
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }
        ret.fatigue = ct.fatigue;
        ret.buddylist = new BuddyList(ct.buddysize);
        ret.subcategory = ct.subcategory;

        if (isChannel) {
            final MapleMapManager mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) { // char is on a map that doesn't exist warp it to henesys
                ret.map = mapFactory.getMap(100000000);
            } else {
                if (ret.map.getForcedReturnId() != 999999999 && ret.map.getForcedReturnMap() != null) {
                    ret.map = ret.map.getForcedReturnMap();
                }
            }
            MaplePortal portal = ret.map.getPortal(0);
            ret.setPosition(portal.getPosition());

            final int messengerid = ct.messengerid;
            if (messengerid > 0) {
                ret.messenger = World.Messenger.getMessenger(messengerid);
            }
        } else {

            ret.messenger = null;
        }
        int partyid = ct.partyid;
        if (partyid >= 0) {
            MapleParty party = World.Party.getParty(partyid);
            if (party != null && party.getMemberById(ret.id) != null) {
                ret.party = party;
            }
        }

        MapleQuestStatus queststatus_from;
        for (final Map.Entry<Integer, Object> qs : ct.Quest.entrySet()) {
            queststatus_from = (MapleQuestStatus) qs.getValue();
            queststatus_from.setQuest(qs.getKey());
            ret.quests.put(queststatus_from.getQuest(), queststatus_from);
        }
        for (final Map.Entry<Integer, SkillEntry> qs : ct.Skills.entrySet()) {
            ret.skills.put(SkillFactory.getSkill(qs.getKey()), qs.getValue());
        }
        for (final Integer zz : ct.finishedAchievements) {
            ret.finishedAchievements.add(zz);
        }
        for (final Integer zz : ct.finishedQuests) {
            ret.finishedQuests.add(zz);
        }
        for (Entry<MapleTraitType, Integer> t : ct.traits.entrySet()) {
            ret.traits.get(t.getKey()).setExp(t.getValue());
        }
        ret.monsterbook = new MonsterBook(ct.mbook, ret);
        ret.inventory = (MapleInventory[]) ct.inventorys;
        ret.BlessOfFairy_Origin = ct.BlessOfFairy;
        ret.BlessOfEmpress_Origin = ct.BlessOfEmpress;
        ret.petStore = ct.petStore;
        ret.questinfo = ct.InfoQuest;
        ret.familiars = ct.familiars;
        ret.savedLocations = ct.savedlocation;
        ret.wishlist = ct.wishlist;
        ret.keylayout = new MapleKeyLayout();
        ret.rocks = ct.rocks;
        ret.regrocks = ct.regrocks;
        ret.hyperrocks = ct.hyperrocks;
        ret.buddylist.loadFromTransfer(ct.buddies);
        // ret.lastfametime
        // ret.lastmonthfameids
        ret.keydown_skill = 0; // Keydown skill can't be brought over
        ret.lastfametime = ct.lastfametime;
        ret.lastmonthfameids = ct.famedcharacters;
        ret.lastmonthbattleids = ct.battledaccs;
        ret.extendedSlots = ct.extendedSlots;
        ret.storage = (MapleStorage) ct.storage;
        ret.cs = (CashShop) ct.cs;
        ret.acash = ct.ACash;
        ret.maplepoints = ct.MaplePoints;
        ret.numClones = ct.clonez;
        ret.imps = ct.imps;
        ret.rebuy = ct.rebuy;
        ret.mount = new MapleMount(ret, ct.mount_itemid, ret.stats.getSkillByJob(1004, ret.job), ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
        ret.expirationTask(false, false);
        //ret.stats.recalcLocalStats(ret);
        client.setTempIP(ct.tempIP);

        return ret;
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) {
        final MapleCharacter ret = new MapleCharacter(channelserver);
        ret.client = client;
        ret.id = charid;

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        try (Connection con = DatabaseConnection.getPlayerConnection()) {

            ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                client.getSession().close();
                System.out.println("Account: " + client.getAccountName() + " possible remote hack with IP: " + client.getSessionIPAddress());
                return null;
            }
            ret.name = rs.getString("name");
            ret.level = rs.getShort("level");
            ret.totallevel = rs.getInt("totallevel");
            ret.fame = rs.getInt("fame");

            ret.stats.str = rs.getInt("str");
            ret.stats.dex = rs.getInt("dex");
            ret.stats.int_ = rs.getInt("int");
            ret.stats.luk = rs.getInt("luk");
            ret.stats.maxhp = rs.getInt("maxhp");
            ret.stats.maxmp = rs.getInt("maxmp");
            ret.stats.hp = rs.getInt("hp");
            ret.stats.mp = rs.getInt("mp");
            ret.job = rs.getShort("job");
            ret.gmLevel = rs.getByte("gm");
            ret.exp.set(rs.getInt("exp"));
            BigInteger bigExp = new BigInteger(rs.getString("overexp"));
            ret.overexp = (bigExp);
            ret.hpApUsed = rs.getShort("hpApUsed");
            ret.remainingSp = (short) Randomizer.MinMax(rs.getShort("sp"), 0, (short) 9999);
            ret.remainingAp = rs.getInt("ap");
            ret.meso = rs.getInt("meso");
            ret.skinColor = rs.getByte("skincolor");
            ret.gender = rs.getByte("gender");

            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.demonMarking = rs.getInt("demonMarking");
            ret.accountid = rs.getInt("accountid");
            client.setAccID(ret.accountid);
            ret.mapid = rs.getInt("map");
            ret.initialSpawnPoint = 0;
            ret.world = rs.getByte("world");
            ret.guildid = rs.getInt("guildid");
            ret.guildrank = rs.getByte("guildrank");
            ret.allianceRank = rs.getByte("allianceRank");
            ret.guildContribution = rs.getInt("guildContribution");
            ret.totalWins = rs.getInt("totalWins");
            ret.totalLosses = rs.getInt("totalLosses");
            ret.currentrep = rs.getInt("currentrep");
            ret.totalrep = rs.getInt("totalrep");
            ret.makeMFC(rs.getInt("familyid"), rs.getInt("seniorid"), rs.getInt("junior1"), rs.getInt("junior2"));
            if (ret.guildid > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            ret.gachexp = rs.getInt("gachexp");
            ret.buddylist = new BuddyList(rs.getByte("buddyCapacity"));
            ret.subcategory = rs.getByte("subcategory");
            ret.mount = new MapleMount(ret, 0, ret.stats.getSkillByJob(1004, ret.job), (byte) 0, (byte) 1, 0);
            ret.rank = rs.getInt("rank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");
            ret.marriageId = rs.getInt("marriageId");
            ret.fatigue = rs.getShort("fatigue");
            ret.pvpLvl = rs.getInt("pvpLevel");
            ret.pvpExp = rs.getInt("pvpExp");
            ret.pvpPoints = rs.getInt("pvpPoints");
            /* Start of Custom Features */
            ret.reborns = rs.getInt("reborns");
            ret.apstorage = rs.getInt("apstorage");
            ret.tier = rs.getInt("tier");
            ret.stamina = rs.getInt("stamina");
            ret.damageSkin = rs.getInt("damage");
            /* End of Custom Features */
            for (MapleTrait t : ret.traits.values()) {
                t.setExp(rs.getInt(t.getType().name()));
            }
            if (channelserver) {
                ret.CRand = new PlayerRandomStream();
                MapleMapManager mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
                ret.map = mapFactory.getMap(ret.mapid);
                if (ret.map == null) { // char is on a map that doesn't exist warp it to henesys
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(0);
                ret.setPosition(portal.getPosition());

                int partyid = rs.getInt("party");
                if (partyid >= 0) {
                    MapleParty party = World.Party.getParty(partyid);
                    if (party != null && party.getMemberById(ret.id) != null) {
                        ret.party = party;
                    }
                }
                /*
                 final String[] pets = rs.getString("pets").split(",");
                 for (int i = 0; i < ret.petStore.length; i++) {
                 ret.petStore[i] = Byte.parseByte(pets[i]);
                 }
                 */
                //achievements
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM achievements WHERE accountid = ?");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    int achid = rs.getInt("achievementid");
                    MapleAchievement Ach = MapleAchievements.getInstance().getById(achid);
                    if (Ach != null) {
                        ret.finishedAchievements.addIfAbsent(achid);
                    }
                }
                rs.close();
                ps.close();
                //quests
                ps = con.prepareStatement("SELECT * FROM quests WHERE accountid = ?");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.finishedQuests.addIfAbsent(rs.getInt("questid"));
                }
                rs.close();
                ps.close();

                //damage skins
                ps = con.prepareStatement("SELECT * FROM damage_skins WHERE accid = ?");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.damageSkins.put(rs.getInt("id"), new Pair<Integer, Long>(rs.getInt("level"), rs.getLong("exp")));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM dojo WHERE charid = ?");
                ps.setInt(1, ret.id);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.dojo_level = rs.getInt("level");
                    ret.dojo_exp = rs.getLong("exp");
                }
                rs.close();
                ps.close();

            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");

            while (rs.next()) {
                final int id = rs.getInt("quest");
                final MapleQuest q = MapleQuest.getInstance(id);
                final byte stat = rs.getByte("status");
                if ((stat == 1 || stat == 2) && channelserver && (q == null || q.isBlocked())) { // bigbang
                    continue;
                }
                if (stat == 1 && channelserver && !q.canStart(ret, null)) { // bigbang
                    continue;
                }
                final MapleQuestStatus status = new MapleQuestStatus(q, stat);
                final long cTime = rs.getLong("time");
                if (cTime > -1) {
                    status.setCompletionTime(cTime * 1000);
                }
                status.setForfeited(rs.getInt("forfeited"));
                status.setCustomData(rs.getString("customData"));
                ret.quests.put(q, status);
                pse.setInt(1, rs.getInt("queststatusid"));
                final ResultSet rsMobs = pse.executeQuery();

                while (rsMobs.next()) {
                    status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                }
                rsMobs.close();
            }
            pse.close();
            rs.close();
            ps.close();

            if (channelserver) {
                ret.monsterbook = MonsterBook.loadCards(ret.accountid, ret);

                ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
                } else {
                    ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
                    ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
                    ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
                    ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
                    ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));
                }
                ps.close();
                rs.close();
                for (Pair<Item, MapleInventoryType> mit : ItemLoader.INVENTORY.loadItems(ret.id, !channelserver)) {
                    ret.getInventory(mit.getRight()).addItemFromDB(mit.getLeft());
                    if (mit.getLeft().getPet() != null) {
                        ret.pets.add(mit.getLeft().getPet());
                    }
                }

                ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                if (rs.next()) {
                    ret.getClient().setAccountName(rs.getString("name"));
                    ret.acash = rs.getInt("ACash");
                    ret.maplepoints = rs.getInt("mPoints");
                    ret.points = rs.getInt("points");
                    ret.hash = rs.getString("password");

                    if (rs.getTimestamp("lastlogon") != null) {
                        final Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(rs.getTimestamp("lastlogon").getTime());
                        if (cal.get(Calendar.DAY_OF_WEEK) + 1 == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                            ret.acash += 500;
                        }
                    }
                    if (rs.getInt("banned") > 0) {
                        rs.close();
                        ps.close();
                        ret.getClient().getSession().close();
                        throw new RuntimeException("Loading a banned character");
                    }

                } else {
                    rs.close();
                }
                ps.close();

                ps = con.prepareStatement("UPDATE accounts SET lastlogon = CURRENT_TIMESTAMP() WHERE id = ?");
                ps.setInt(1, ret.accountid);
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                while (rs.next()) {
                    ret.questinfo.put(rs.getInt("quest"), rs.getString("customData"));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                Skill skil;
                List<Integer> removeSkills = new ArrayList<>();
                while (rs.next()) {
                    final int skid = rs.getInt("skillid");
                    skil = SkillFactory.getSkill(skid);
                    int skl = rs.getInt("skilllevel");
                    if (skil != null && GameConstants.isApplicableSkill(skid)) {
                        int msl = skil.getMaxLevel();
                        if (skl > skil.getMaxLevel() && skid < 92000000) {
                            if (!skil.isBeginnerSkill() && skil.canBeLearnedBy(ret.job) && !skil.isSpecialSkill()) {
                                // System.out.println("test: " + (skl - skil.getMaxLevel()) + " - " +
                                // skil.getName() + " ");
                                ret.addRemainingSp(skl - skil.getMaxLevel());
                            }
                            skl = skil.getMaxLevel();
                        }
                        if (msl > skil.getMaxLevel()) {
                            msl = skil.getMaxLevel();
                        }
                        ret.skills.put(skil, new SkillEntry(skl, msl, rs.getLong("expiration")));
                        ret.skills.get(skil).setLevel(rs.getInt("level"));
                        //System.out.println("level: " + ret.skills.get(skil).getLevel());
                        ret.skills.get(skil).setExp(rs.getLong("exp"));
                        //System.out.println("exp: " + ret.skills.get(skil).getExp());
                        ret.skills.get(skil).setLoaded();
                    } else if (skil == null) { // doesnt. exist. e.g. bb
                        if (!GameConstants.isBeginnerJob(skid / 10000) && skid / 10000 != 900 && skid / 10000 != 800 && skid / 10000 != 9000) {
                            removeSkills.add(skid);
                            ret.addRemainingSp(skl);
                        }
                    }
                }
                rs.close();
                ps.close();

                if (!removeSkills.isEmpty()) {
                    for (Integer i : removeSkills) {
                        //System.out.println(ret.getName() + " had skill " + i + " removed.");
                        ps = con.prepareStatement("DELETE FROM skills WHERE skillid = ? AND characterid = ?");
                        ps.setInt(1, i);
                        ps.setInt(2, ret.id);
                        ps.executeUpdate();
                        ps.close();
                    }
                }

                ret.expirationTask(false, true); // do it now

                // Bless of Fairy handling
                ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY level DESC");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                int maxlevel_ = 0, maxlevel_2 = 0;
                while (rs.next()) {
                    if (GameConstants.isKOC(rs.getShort("job"))) {
                        int maxlevel = (rs.getShort("level") / 5);

                        if (maxlevel > 24) {
                            maxlevel = 24;
                        }
                        if (maxlevel > maxlevel_2 || maxlevel_2 == 0) {
                            maxlevel_2 = maxlevel;
                            ret.BlessOfEmpress_Origin = rs.getString("name");
                        }
                    }
                    int maxlevel = (rs.getShort("level") / 10);

                    if (maxlevel > 20) {
                        maxlevel = 20;
                    }
                    if (maxlevel > maxlevel_ || maxlevel_ == 0) {
                        maxlevel_ = maxlevel;
                        ret.BlessOfFairy_Origin = rs.getString("name");
                    }
                }
                /*
                 * if (!compensate_previousSP) { for (Entry<Skill, SkillEntry> skill :
                 * ret.skills.entrySet()) { if (!skill.getKey().isBeginnerSkill() &&
                 * !skill.getKey().isSpecialSkill()) {
                 * ret.remainingSp[GameConstants.getSkillBookForSkill(skill.getKey().getId())]
                 * += skill.getValue().skillevel; skill.getValue().skillevel = 0; } }
                 * ret.setQuestAdd(MapleQuest.getInstance(170000), (byte) 0, null); //set it so
                 * never again }
                 */
                if (ret.BlessOfFairy_Origin == null) {
                    ret.BlessOfFairy_Origin = ret.name;
                }
                ret.skills.put(SkillFactory.getSkill(GameConstants.getBOF_ForJob(ret.job)), new SkillEntry(maxlevel_, (byte) 0, -1));
                if (SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)) != null) {
                    if (ret.BlessOfEmpress_Origin == null) {
                        ret.BlessOfEmpress_Origin = ret.BlessOfFairy_Origin;
                    }
                    ret.skills.put(SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)), new SkillEntry(maxlevel_2, (byte) 0, -1));
                }
                ps.close();
                rs.close();
                // END

                ps = con.prepareStatement("SELECT * FROM familiars WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("expiry") <= System.currentTimeMillis()) {
                        continue;
                    }
                    ret.familiars.put(rs.getInt("familiar"), new MonsterFamiliar(charid, rs.getInt("id"), rs.getInt("familiar"), rs.getLong("expiry"), rs.getString("name"), rs.getInt("fatigue"), rs.getByte("vitality")));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map");
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0;
                ret.lastmonthfameids = new ArrayList<Integer>(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `accid_to`,`when` FROM battlelog WHERE accid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                ret.lastmonthbattleids = new ArrayList<Integer>();
                while (rs.next()) {
                    ret.lastmonthbattleids.add(Integer.valueOf(rs.getInt("accid_to")));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `itemId` FROM extendedSlots WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.extendedSlots.add(Integer.valueOf(rs.getInt("itemId")));
                }
                rs.close();
                ps.close();

                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
                ret.cs = new CashShop(ret.accountid, charid, ret.getJob());

                ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int i = 0;
                while (rs.next()) {
                    ret.wishlist[i] = rs.getInt("sn");
                    i++;
                }
                while (i < 10) {
                    ret.wishlist[i] = 0;
                    i++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int r = 0;
                while (rs.next()) {
                    ret.rocks[r] = rs.getInt("mapid");
                    r++;
                }
                while (r < 10) {
                    ret.rocks[r] = 999999999;
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT mapid FROM regrocklocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.regrocks[r] = rs.getInt("mapid");
                    r++;
                }
                while (r < 5) {
                    ret.regrocks[r] = 999999999;
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT mapid FROM hyperrocklocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.hyperrocks[r] = rs.getInt("mapid");
                    r++;
                }
                while (r < 13) {
                    ret.hyperrocks[r] = 999999999;
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM imps WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.imps[r] = new MapleImp(rs.getInt("itemid"));
                    ret.imps[r].setLevel(rs.getByte("level"));
                    ret.imps[r].setState(rs.getByte("state"));
                    ret.imps[r].setCloseness(rs.getShort("closeness"));
                    ret.imps[r].setFullness(rs.getShort("fullness"));
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("No mount data found on SQL column");
                }
                final Item mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) (GameConstants.GMS ? -18 : -23));
                ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, GameConstants.GMS ? 80001000 : ret.stats.getSkillByJob(1004, ret.job), rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                rs.close();
                ps.close();

                // ret.stats.recalcLocalStats(ret);
            } else { // Not channel server
                for (Pair<Item, MapleInventoryType> mit : ItemLoader.INVENTORY.loadItems(ret.id, !channelserver)) {
                    ret.getInventory(mit.getRight()).addItemFromDB(mit.getLeft());
                }
            }
            ret.loadBank(con);
            ret.loadStakeDP(con);
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load character..");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }

            } catch (SQLException ignore) {
            }
        }

        return ret;
    }

    public static void saveNewCharToDB(final MapleCharacter chr, final JobType type, short db) {

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);

            ps = con.prepareStatement(
                    "INSERT INTO characters (level, totallevel, str, dex, luk, `int`, hp, mp, maxhp, maxmp, sp, ap, skincolor, gender, job, hair, face, demonMarking, map, meso, party, buddyCapacity, pets, subcategory, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, chr.level); // Level
            ps.setInt(2, chr.totallevel); // Total Level
            final PlayerStats stat = chr.stats;
            ps.setInt(3, stat.getStr()); // Str
            ps.setInt(4, stat.getDex()); // Dex
            ps.setInt(5, stat.getInt()); // Int
            ps.setInt(6, stat.getLuk()); // Luk
            ps.setInt(7, stat.getHp()); // HP
            ps.setInt(8, stat.getMp());
            ps.setInt(9, stat.getMaxHp()); // MP
            ps.setInt(10, stat.getMaxMp());
            ps.setShort(11, (short) chr.remainingSp); // Remaining SP
            ps.setShort(12, (short) chr.remainingAp); // Remaining AP
            ps.setByte(13, chr.skinColor);
            ps.setByte(14, chr.gender);
            ps.setShort(15, chr.job);
            ps.setInt(16, chr.hair);
            ps.setInt(17, chr.face);
            ps.setInt(18, chr.demonMarking);
            if (db < 0 || db > 2) { // todo legend
                db = 0;
            }
            // ps.setInt(18, db == 2 ? 3000600 : type.map);
            ps.setInt(19, GameConstants.STARTING_MAP);
            ps.setInt(20, chr.meso); // Meso
            ps.setInt(21, -1); // Party
            ps.setByte(22, chr.buddylist.getCapacity()); // Buddylist
            ps.setString(23, "-1,-1,-1");
            ps.setInt(24, db); // for now
            ps.setInt(25, chr.getAccountID());
            ps.setString(26, chr.name);
            ps.setByte(27, chr.world);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                chr.id = rs.getInt(1);
            } else {
                ps.close();
                rs.close();
                throw new DatabaseException("Inserting char failed.");
            }
            ps.close();
            rs.close();
            ps = con.prepareStatement(
                    "INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (final MapleQuestStatus q : chr.quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                ps.setInt(5, q.getForfeited());
                ps.setString(6, q.getCustomData());
                ps.execute();
                rs = ps.getGeneratedKeys();
                if (q.hasMobKills()) {
                    rs.next();
                    for (int mob : q.getMobKills().keySet()) {
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.execute();
                    }
                }
                rs.close();
            }
            ps.close();
            pse.close();

            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration, level, exp) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);

            for (final Entry<Skill, SkillEntry> skill : chr.skills.entrySet()) {
                if (GameConstants.isApplicableSkill(skill.getKey().getId())) { // do not save additional skills
                    ps.setInt(2, skill.getKey().getId());
                    ps.setInt(3, skill.getValue().skillevel);
                    ps.setInt(4, skill.getValue().masterlevel);
                    ps.setLong(5, skill.getValue().expiration);
                    ps.setInt(6, skill.getValue().level);
                    ps.setLong(7, skill.getValue().exp);
                    ps.execute();
                }
            }
            ps.close();

            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 96); // Eq
            ps.setByte(3, (byte) 96); // Use
            ps.setByte(4, (byte) 96); // Setup
            ps.setByte(5, (byte) 96); // ETC
            ps.setByte(6, (byte) 96); // Cash
            ps.execute();
            ps.close();

            ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 1);
            ps.setInt(3, 0);
            ps.setByte(4, (byte) 0);
            ps.execute();
            ps.close();

            final int[] array1 = {2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 23, 25, 26, 27, 31, 34, 37, 38, 41, 44, 45, 46, 50, 57, 59, 60, 61, 62, 63, 64, 65, 8, 9, 24, 30};
            final int[] array2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4, 5, 6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4};
            final int[] array3 = {10, 12, 13, 18, 6, 11, 8, 5, 0, 4, 1, 19, 14, 15, 3, 17, 9, 20, 22, 50, 51, 52, 7, 53, 100, 101, 102, 103, 104, 105, 106, 16, 23, 24, 2};

            ps = con.prepareStatement("INSERT INTO keymap (characterid, jobid, `key`, `type`, `action`) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setInt(2, chr.job);
            for (int i = 0; i < array1.length; i++) {
                ps.setInt(3, array1[i]);
                ps.setInt(4, array2[i]);
                ps.setInt(5, array3[i]);
                ps.execute();
            }
            ps.close();

            List<Pair<Item, MapleInventoryType>> listing = new ArrayList<Pair<Item, MapleInventoryType>>();
            for (final MapleInventory iv : chr.inventory) {
                for (final Item item : iv.list()) {
                    listing.add(new Pair<Item, MapleInventoryType>(item, iv.getType()));
                }
            }
            ItemLoader.INVENTORY.saveItems(listing, chr.id, con);

            //con.commit();
            //con.setAutoCommit(true);
            //con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
            System.err.println("[charsave] Error saving character data");
        } finally {
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }

            } catch (SQLException e) {
                FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                e.printStackTrace();
                System.err.println("[charsave] Error going back to autocommit mode");
            }
        }
    }

    public boolean checkPlayer() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    return false;
                }
            }
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }
        return true;
    }

    public void isChangingMaps(boolean toggle) {
        this.mapTransitioning.set(toggle);
    }

    public boolean isChangingMaps() {
        return this.mapTransitioning.get();
    }

    public void setMapTransitionComplete() {
        opened = false;
        if (isStorageOpened()) {
            getStorage().saveToDB(false);
            StorageOpened = false;
        }
        isChangingMaps(false);
        changingMap = false;
        eventAfterChangedMap(getMap());
    }

    public boolean check(int cid) {
        if (this.id == cid) {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?")) {
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        rs.close();
                        return false;
                    } else {
                        return true;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        return false;
    }

    public void saveToDB() {
        saveToDB(false, false);
    }

    public void saveToDBToggle() {
        saveToDB(false, false, false);
    }

    public void saveToDB(boolean dc, boolean fromcs) {
        saveToDB(dc, fromcs, true);
    }

    public void saveItems() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET meso = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, meso);
                ps.setInt(2, id);
            }
            saveInventory(con);
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character item data") + ex);
        }
    }

    public void saveToDB(boolean dc, boolean fromcs, boolean toggle) {
        if (isClone() || isBot) {
            return;
        }

        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    getClient().getSession().close();
                    return;
                }
            }

            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, overexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, demonMarking = ?, map = ?, meso = ?, hpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, pets = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, gachexp = ?, fatigue = ?, charm = ?, charisma = ?, craft = ?, insight = ?, sense = ?, will = ?, totalwins = ?, totallosses = ?, pvpLevel = ?, pvpExp = ?, pvpPoints = ?, reborns = ?, apstorage = ?, name = ?, totallevel = ?, tier = ?, stamina = ? WHERE id = ?",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, level);
                ps.setInt(2, fame);
                ps.setInt(3, stats.getStr());
                ps.setInt(4, stats.getDex());
                ps.setInt(5, stats.getLuk());
                ps.setInt(6, stats.getInt());
                ps.setInt(7, Math.abs(exp.get()));
                ps.setString(8, overexp.toString());
                ps.setInt(9, stats.getHp() < 1 ? 50 : stats.getHp());
                ps.setInt(10, stats.getMp());
                ps.setInt(11, stats.getMaxHp());
                ps.setInt(12, stats.getMaxMp());
                ps.setShort(13, remainingSp); // Remaining SP
                ps.setInt(14, remainingAp);
                ps.setByte(15, gmLevel);
                ps.setByte(16, skinColor);
                ps.setByte(17, gender);
                ps.setShort(18, job);
                ps.setInt(19, hair);
                ps.setInt(20, face);
                ps.setInt(21, demonMarking);
                if (!fromcs && map != null) {
                    if (map.getForcedReturnId() != 999999999 && map.getForcedReturnMap() != null) {
                        ps.setInt(22, map.getForcedReturnId());
                    } else {
                        ps.setInt(22, stats.getHp() < 1 ? map.getReturnMapId() : map.getId());
                    }
                } else {
                    ps.setInt(22, mapid);
                }
                ps.setInt(23, meso);
                ps.setShort(24, hpApUsed);
                if (map == null) {
                    ps.setByte(25, (byte) 0);
                } else {
                    final MaplePortal closest = map.findClosestSpawnpoint(getTruePosition());
                    ps.setByte(25, (byte) (closest != null ? closest.getId() : 0));
                }
                ps.setInt(26, party == null ? -1 : party.getId());
                ps.setShort(27, buddylist.getCapacity());
                /*
                 * final StringBuilder petz = new StringBuilder(); int petLength = 0; for (final
                 * MaplePet pet : pets) { if (pet.getSummoned()) { pet.saveToDb();
                 * petz.append(pet.getInventoryPosition()); petz.append(","); petLength++; } }
                 * while (petLength < 3) { petz.append("-1,"); petLength++; }
                 * 
                 * final String petstring = petz.toString();
                 */
                ps.setString(28, "-1,-1,-1");
                ps.setByte(29, subcategory);
                ps.setInt(30, marriageId);
                ps.setInt(31, currentrep);
                ps.setInt(32, totalrep);
                ps.setInt(33, gachexp);
                ps.setShort(34, fatigue);
                ps.setInt(35, traits.get(MapleTraitType.charm).getTotalExp());
                ps.setInt(36, traits.get(MapleTraitType.charisma).getTotalExp());
                ps.setInt(37, traits.get(MapleTraitType.craft).getTotalExp());
                ps.setInt(38, traits.get(MapleTraitType.insight).getTotalExp());
                ps.setInt(39, traits.get(MapleTraitType.sense).getTotalExp());
                ps.setInt(40, traits.get(MapleTraitType.will).getTotalExp());
                ps.setInt(41, totalWins);
                ps.setInt(42, totalLosses);
                ps.setInt(43, pvpLvl);
                ps.setInt(44, pvpExp);
                ps.setInt(45, pvpPoints);
                /* Start of Custom Features */
                ps.setInt(46, reborns);
                ps.setInt(47, apstorage);
                /* End of Custom Features */
                ps.setString(48, name);
                ps.setInt(49, totallevel);
                ps.setInt(50, tier);
                ps.setInt(51, stamina);
                ps.setInt(52, id);
                if (ps.executeUpdate() < 1) {
                    throw new DatabaseException("Character not in database (" + id + ")");
                }
            }

            deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `customData`) VALUES (?, ?, ?)")) {
                ps.setInt(1, id);
                for (final Entry<Integer, String> q : questinfo.entrySet()) {
                    ps.setInt(2, q.getKey());
                    ps.setString(3, q.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, id);
                for (final MapleQuestStatus q : quests.values()) {
                    ps.setInt(2, q.getQuest().getId());
                    ps.setInt(3, q.getStatus());
                    ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                    ps.setInt(5, q.getForfeited());
                    ps.setString(6, q.getCustomData());
                    ps.addBatch();
                    if (q.hasMobKills()) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                try (PreparedStatement pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)")) {
                                    for (int mob : q.getMobKills().keySet()) {
                                        pse.setInt(1, rs.getInt(1));
                                        pse.setInt(2, mob);
                                        pse.setInt(3, q.getMobKills(mob));
                                        pse.addBatch();
                                    }
                                    pse.executeBatch();
                                }
                            }
                        }
                    }
                    ps.executeBatch();
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration, level, exp) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE skilllevel = VALUES(skilllevel), masterlevel = VALUES(masterlevel), expiration = VALUES(expiration), level = VALUES(level), exp = VALUES(exp)")) {
                ps.setInt(1, id);

                for (final Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                    if (GameConstants.isApplicableSkill(skill.getKey().getId())) { // do not save additional skills
                        ps.setInt(2, skill.getKey().getId());
                        ps.setInt(3, skill.getValue().skillevel);
                        ps.setInt(4, skill.getValue().masterlevel);
                        ps.setLong(5, skill.getValue().expiration);
                        //System.out.println("level2: " + skill.getValue().level);
                        ps.setInt(6, skill.getValue().level);
                        //System.out.println("exp2: " + skill.getValue().exp);
                        ps.setLong(7, skill.getValue().exp);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            List<MapleCoolDownValueHolder> cd = getCooldowns();
            if (dc && cd.size() > 0) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, getId());
                    for (final MapleCoolDownValueHolder cooling : cd) {
                        ps.setInt(2, cooling.skillId);
                        ps.setLong(3, cooling.startTime);
                        ps.setLong(4, cooling.length);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)")) {
                ps.setInt(1, id);
                for (final SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (savedLocations[savedLocationType.getValue()] != -1) {
                        ps.setInt(2, savedLocationType.getValue());
                        ps.setInt(3, savedLocations[savedLocationType.getValue()]);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?");
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, ?)")) {
                ps.setInt(1, id);
                for (BuddylistEntry entry : buddylist.getBuddies()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setInt(3, entry.isVisible() ? 0 : 1);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET `ACash` = ?, `mPoints` = ?, `points` = ? WHERE id = ?")) {
                ps.setInt(1, acash);
                ps.setInt(2, maplepoints);
                ps.setInt(3, points);
                ps.setInt(4, client.getAccID());
                ps.executeUpdate();
            }
            if (cs != null) {
                cs.save();
            }
            // keylayout.saveKeys(id);
            // PlayerNPC.updateByCharId(this);
            // mount.saveMount(id);
            // monsterbook.saveCards(accountid);

            if (imps.length > 0) {
                deleteWhereCharacterId(con, "DELETE FROM imps WHERE characterid = ?");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO imps (characterid, itemid, closeness, fullness, state, level) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, id);
                    for (MapleImp imp : imps) {
                        if (imp != null) {
                            ps.setInt(2, imp.getItemId());
                            ps.setShort(3, imp.getCloseness());
                            ps.setShort(4, imp.getFullness());
                            ps.setByte(5, imp.getState());
                            ps.setByte(6, imp.getLevel());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
            }
            if (changed_wishlist) {
                deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?");
                for (int i = 0; i < getWishlistSize(); i++) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ")) {
                        ps.setInt(1, getId());
                        ps.setInt(2, wishlist[i]);
                        ps.execute();
                    }
                }
            }
            if (rocks.length > 0) {
                deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
                for (int i = 0; i < rocks.length; i++) {
                    if (rocks[i] != 999999999) {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid) VALUES(?, ?) ")) {
                            ps.setInt(1, getId());
                            ps.setInt(2, rocks[i]);
                            ps.execute();
                        }
                    }
                }
            }
            if (regrocks.length > 0) {
                deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?");
                for (int i = 0; i < regrocks.length; i++) {
                    if (regrocks[i] != 999999999) {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO regrocklocations(characterid, mapid) VALUES(?, ?) ")) {
                            ps.setInt(1, getId());
                            ps.setInt(2, regrocks[i]);
                            ps.execute();
                        }
                    }
                }
            }
            if (hyperrocks.length > 0) {
                deleteWhereCharacterId(con, "DELETE FROM hyperrocklocations WHERE characterid = ?");
                for (int i = 0; i < hyperrocks.length; i++) {
                    if (hyperrocks[i] != 999999999) {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO hyperrocklocations(characterid, mapid) VALUES(?, ?) ")) {
                            ps.setInt(1, getId());
                            ps.setInt(2, hyperrocks[i]);
                            ps.execute();
                        }
                    }
                }
            }
            if (!extendedSlots.isEmpty()) {
                deleteWhereCharacterId(con, "DELETE FROM extendedSlots WHERE characterid = ?");
                for (int i : extendedSlots) {
                    if (getInventory(MapleInventoryType.ETC).findById(i) != null) { // just in case
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO extendedSlots(characterid, itemId) VALUES(?, ?) ")) {
                            ps.setInt(1, getId());
                            ps.setInt(2, i);
                            ps.execute();
                        }
                    }
                }
            }
            updateEventScore(con);
            saveInventory(con);
            saveBuffs(con);
            saveBank(con);
            saveDojo(con);
            //con.commit();
            //con.setAutoCommit(true);
            //con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + ex);
        }
        if (damageSkin < 9000 && damageExp > 0) {
            saveDamageSkin(damageSkin, damageLevel, damageExp);
        }
    }

    public void loadMacros() {
        skillMacros = new SkillMacro[5];
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            int position;
            while (rs.next()) {
                position = rs.getInt("position");
                SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                skillMacros[position] = macro;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
            System.err.println("[charsave] Error saving character data");
        } finally {
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                client.announce(CField.getMacros(skillMacros));
            } catch (SQLException e) {
                FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                e.printStackTrace();
                System.err.println("[charsave] Error going back to autocommit mode");
            }
        }
    }

    public void saveMacro() {

    }

    public void saveCharToDB() {
        saveCharToDB2();
    }

    public void saveCharToDB2() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    getClient().getSession().close();
                    return;
                }
            }
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, overexp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, demonMarking = ?, map = ?, meso = ?, hpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, pets = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, gachexp = ?, fatigue = ?, charm = ?, charisma = ?, craft = ?, insight = ?, sense = ?, will = ?, totalwins = ?, totallosses = ?, pvpLevel = ?, pvpExp = ?, pvpPoints = ?, reborns = ?, apstorage = ?, name = ?, totallevel = ?, tier = ?, stamina = ? WHERE id = ?", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, level);
                ps.setInt(2, fame);
                ps.setInt(3, stats.getStr());
                ps.setInt(4, stats.getDex());
                ps.setInt(5, stats.getLuk());
                ps.setInt(6, stats.getInt());
                ps.setInt(7, Math.abs(exp.get()));
                ps.setString(8, overexp.toString());
                ps.setInt(9, stats.getHp() < 1 ? 50 : stats.getHp());
                ps.setInt(10, stats.getMp());
                ps.setInt(11, stats.getMaxHp());
                ps.setInt(12, stats.getMaxMp());
                ps.setShort(13, remainingSp); // Remaining SP
                ps.setInt(14, remainingAp);
                ps.setByte(15, gmLevel);
                ps.setByte(16, skinColor);
                ps.setByte(17, gender);
                ps.setShort(18, job);
                ps.setInt(19, hair);
                ps.setInt(20, face);
                ps.setInt(21, demonMarking);
                if (map != null) {
                    if (map.getForcedReturnId() != 999999999 && map.getForcedReturnMap() != null) {
                        ps.setInt(22, map.getForcedReturnId());
                    } else {
                        ps.setInt(22, stats.getHp() < 1 ? map.getReturnMapId() : map.getId());
                    }
                } else {
                    ps.setInt(22, mapid);
                }
                ps.setInt(23, meso);
                ps.setShort(24, hpApUsed);
                if (map == null) {
                    ps.setByte(25, (byte) 0);
                } else {
                    final MaplePortal closest = map.findClosestSpawnpoint(getTruePosition());
                    ps.setByte(25, (byte) (closest != null ? closest.getId() : 0));
                }
                ps.setInt(26, party == null ? -1 : party.getId());
                ps.setShort(27, buddylist.getCapacity());
                /*
                 * final StringBuilder petz = new StringBuilder(); int petLength = 0; for (final
                 * MaplePet pet : pets) { if (pet.getSummoned()) { pet.saveToDb();
                 * petz.append(pet.getInventoryPosition()); petz.append(","); petLength++; } }
                 * while (petLength < 3) { petz.append("-1,"); petLength++; }
                 * 
                 * final String petstring = petz.toString();
                 */
                ps.setString(28, "-1,-1,-1");
                ps.setByte(29, subcategory);
                ps.setInt(30, marriageId);
                ps.setInt(31, currentrep);
                ps.setInt(32, totalrep);
                ps.setInt(33, gachexp);
                ps.setShort(34, fatigue);
                ps.setInt(35, traits.get(MapleTraitType.charm).getTotalExp());
                ps.setInt(36, traits.get(MapleTraitType.charisma).getTotalExp());
                ps.setInt(37, traits.get(MapleTraitType.craft).getTotalExp());
                ps.setInt(38, traits.get(MapleTraitType.insight).getTotalExp());
                ps.setInt(39, traits.get(MapleTraitType.sense).getTotalExp());
                ps.setInt(40, traits.get(MapleTraitType.will).getTotalExp());
                ps.setInt(41, totalWins);
                ps.setInt(42, totalLosses);
                ps.setInt(43, pvpLvl);
                ps.setInt(44, pvpExp);
                ps.setInt(45, pvpPoints);
                /* Start of Custom Features */
                ps.setInt(46, reborns);
                ps.setInt(47, apstorage);
                /* End of Custom Features */
                ps.setString(48, name);
                ps.setInt(49, totallevel);
                ps.setInt(50, tier);
                ps.setInt(51, stamina);
                ps.setInt(52, id);
                if (ps.executeUpdate() < 1) {
                    throw new DatabaseException("Character not in database (" + id + ")");
                }
            }

            //con.commit();
            //con.setAutoCommit(true);
            //con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + e);
        }
        saveDojo();
    }

    public void setReborns() {
        reborns = (int) getReborns();
    }

    private void deleteWhereAccountId(Connection con, String sql) throws SQLException {
        deleteWhereCharacterId(con, sql, accountid);
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        deleteWhereCharacterId(con, sql, id);
    }

    private static void deleteWhereAccountId(Connection con, String sql, int id) throws SQLException {
        deleteWhereCharacterId(con, sql, id);
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void deleteWhereCharacterId_NoLock(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.execute();
        ps.close();
    }

    public void saveInventory(final Connection con) throws SQLException {
        List<Pair<Item, MapleInventoryType>> listing = new ArrayList<Pair<Item, MapleInventoryType>>();
        for (final MapleInventory iv : inventory) {
            for (final Item item : iv.list()) {
                listing.add(new Pair<Item, MapleInventoryType>(item, iv.getType()));
            }
        }
        if (con != null) {
            ItemLoader.INVENTORY.saveItems(listing, id, con);
        } else {
            ItemLoader.INVENTORY.saveItems(listing, id);
        }
    }

    public final PlayerStats getStat() {
        return stats;
    }

    public final void QuestInfoPacket(final tools.data.MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(questinfo.size()); // // Party Quest data (quest needs to be added in the quests list)

        for (final Entry<Integer, String> q : questinfo.entrySet()) {
            mplew.writeShort(q.getKey());
            mplew.writeMapleAsciiString(q.getValue() == null ? "" : q.getValue());
        }
    }

    public final void updateInfoQuest(final int questid, final String data) {
        questinfo.put(questid, data);
        changed_questinfo = true;
        client.announce(InfoPacket.updateInfoQuest(questid, data));
    }

    public final String getInfoQuest(final int questid) {
        if (questinfo.containsKey(questid)) {
            return questinfo.get(questid);
        }
        return "";
    }

    public final int getNumQuest() {
        int i = 0;
        for (final MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2 && !(q.isCustom())) {
                i++;
            }
        }
        return i;
    }

    public final byte getQuestStatus(final int quest) {
        final MapleQuest qq = MapleQuest.getInstance(quest);
        if (getQuestNoAdd(qq) == null) {
            return 0;
        }
        return getQuestNoAdd(qq).getStatus();
    }

    public final MapleQuestStatus getQuest(final MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            return new MapleQuestStatus(quest, (byte) 0);
        }
        return quests.get(quest);
    }

    public final void setQuestAdd(final MapleQuest quest, final byte status, final String customData) {
        if (!quests.containsKey(quest)) {
            final MapleQuestStatus stat = new MapleQuestStatus(quest, status);
            stat.setCustomData(customData);
            quests.put(quest, stat);
        }
    }

    public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            final MapleQuestStatus status = new MapleQuestStatus(quest, (byte) 0);
            quests.put(quest, status);
            return status;
        }
        return quests.get(quest);
    }

    public final MapleQuestStatus getQuestNoAdd(final MapleQuest quest) {
        return quests.get(quest);
    }

    public final MapleQuestStatus getQuestRemove(final MapleQuest quest) {
        return quests.remove(quest);
    }

    public final void updateQuest(final MapleQuestStatus quest) {
        updateQuest(quest, false);
    }

    public final void updateQuest(final MapleQuestStatus quest, final boolean update) {
        quests.put(quest.getQuest(), quest);
        if (!(quest.isCustom())) {
            client.announce(InfoPacket.updateQuest(quest));
            if (quest.getStatus() == 1 && !update) {
                client.announce(CField.updateQuestInfo(this, quest.getQuest().getId(), quest.getNpc(), (byte) 10));
            }
        }
    }

    public final Map<Integer, String> getInfoQuest_Map() {
        return questinfo;
    }

    public final Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
        return quests;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        return mbsvh == null ? null : Integer.valueOf(mbsvh.value);
    }

    public final Integer getBuffedSkill_X(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect.getX();
    }

    public final Integer getBuffedSkill_Y(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect.getY();
    }

    public boolean isBuffFrom(MapleBuffStat stat, Skill skill) {
        final MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null || mbsvh.effect == null) {
            return false;
        }
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public int getBuffSource(MapleBuffStat stat) {
        final MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh == null ? -1 : mbsvh.effect.getSourceId();
    }

    public int getTrueBuffSource(MapleBuffStat stat) {
        final MapleBuffStatValueHolder mbsvh = effects.get(stat);
        return mbsvh == null ? -1 : (mbsvh.effect.isSkill() ? mbsvh.effect.getSourceId() : -mbsvh.effect.getSourceId());
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public int getEquipped(int itemid) {
        int possesed = inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        return possesed;
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setSchedule(MapleBuffStat effect, ScheduledFuture<?> sched) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.schedule.cancel(false);
        mbsvh.schedule = sched;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        return mbsvh == null ? null : Long.valueOf(mbsvh.startTime);
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = effects.get(effect);
        return mbsvh == null ? null : mbsvh.effect;
    }

    public void doDragonBlood() {
        final MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.DRAGONBLOOD);
        if (bloodEffect == null) {
            lastDragonBloodTime = 0;
            return;
        }
        prepareDragonBlood();
        if (stats.getHp() - bloodEffect.getX() <= 1) {
            cancelBuffStats(MapleBuffStat.DRAGONBLOOD);
        } else {
            addHP(-bloodEffect.getX());
            client.announce(EffectPacket.showOwnBuffEffect(bloodEffect.getSourceId(), 7, getLevel(), bloodEffect.getLevel()));
            map.broadcastMessage(MapleCharacter.this, EffectPacket.showBuffeffect(getId(), bloodEffect.getSourceId(), 7, getLevel(), bloodEffect.getLevel()), false);
        }
    }

    public final boolean canBlood(long now) {
        return lastDragonBloodTime > 0 && lastDragonBloodTime + 4000 < now;
    }

    private void prepareDragonBlood() {
        lastDragonBloodTime = System.currentTimeMillis();
    }

    public void doRecovery() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.RECOVERY);
        if (bloodEffect == null) {
            bloodEffect = getStatForBuff(MapleBuffStat.MECH_CHANGE);
            if (bloodEffect == null) {
                lastRecoveryTime = 0;
            } else if (bloodEffect.getSourceId() == 35121005) {
                prepareRecovery();
                if (stats.getMp() < bloodEffect.getU()) {
                    cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                    cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
                } else {
                    addMP(-bloodEffect.getU());
                }
            }
        } else {
            prepareRecovery();
            if (stats.getHp() >= stats.getMaxHp()) {
                cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
            } else {
                healHP(bloodEffect.getX());
            }
        }
    }

    public final boolean canRecover(long now) {
        return lastRecoveryTime > 0 && lastRecoveryTime + 5000 < now;
    }

    private void prepareRecovery() {
        lastRecoveryTime = System.currentTimeMillis();
    }

    public void startMapTimeLimitTask(int time, final MapleMap to) {
        if (time <= 0) { // jail
            time = 1;
        }
        client.announce(CField.getClock(time));
        final MapleMap ourMap = getMap();
        time *= 1000;
        mapTimeLimitTask = MapTimer.getInstance().register(() -> {
            if (ourMap.getId() == GameConstants.JAIL) {
                getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_TIME)).setCustomData(String.valueOf(System.currentTimeMillis()));
                getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData("0"); // release them!
            }
            changeMap(to, to.getPortal(0));
        }, time, time);
    }

    public boolean canDOT(long now) {
        return lastDOTTime > 0 && lastDOTTime + 8000 < now;
    }

    public boolean hasDOT() {
        return dotHP > 0;
    }

    public void doDOT() {
        addHP(-(dotHP * 4));
        dotHP = 0;
        lastDOTTime = 0;
    }

    public void setDOT(int d, int source, int sourceLevel) {
        this.dotHP = d;
        addHP(-(dotHP * 4));
        map.broadcastMessage(CField.getPVPMist(id, source, sourceLevel, d));
        lastDOTTime = System.currentTimeMillis();
    }

    public void startFishingTask() {
        cancelFishingTask();
        lastFishingTime = System.currentTimeMillis();
    }

    public boolean canFish(long now) {
        return lastFishingTime > 0 && lastFishingTime + GameConstants.getFishingTime(stats.canFishVIP, isGM()) < now;
    }

    public void doFish(long now) {
        lastFishingTime = now;
        final boolean expMulti = haveItem(2300001, 1, false, true);
        if (client == null || client.getPlayer() == null || !client.isReceiving()
                || (!expMulti && !haveItem(GameConstants.GMS ? 2270008 : 2300000, 1, false, true))
                || !GameConstants.isFishingMap(getMapId()) || !stats.canFish || chair <= 0) {
            cancelFishingTask();
            return;
        }
        MapleInventoryManipulator.removeById(client, MapleInventoryType.USE,
                expMulti ? 2300001 : (GameConstants.GMS ? 2270008 : 2300000), 1, false, false);
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
            mapTimeLimitTask = null;
        }
    }

    public int getNeededExp() {
        return GameConstants.getExpNeededForLevel(level);
    }

    public void cancelFishingTask() {
        lastFishingTime = 0;
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule, int from) {
        registerEffect(effect, starttime, schedule, effect.getStatups(), false, effect.getDuration(), from);
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule, Map<MapleBuffStat, Integer> statups, boolean silent, final int localDuration, final int cid) {
        if (effect.isHide()) {
            map.broadcastMessage(this, CField.removePlayerFromMap(getId()), false);
        } else if (effect.isDragonBlood()) {
            prepareDragonBlood();
        } else if (effect.isRecovery()) {
            prepareRecovery();
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isMonsterRiding_()) {
            getMount().startSchedule();
        }
        int clonez = 0;
        for (Entry<MapleBuffStat, Integer> statup : statups.entrySet()) {
            if (statup.getKey() == MapleBuffStat.ILLUSION) {
                clonez = statup.getValue();
            }
            int value = statup.getValue().intValue();
            if (statup.getKey() == MapleBuffStat.MONSTER_RIDING) {
                if (effect.getSourceId() == 5221006 && battleshipHP <= 0) {
                    battleshipHP = maxBattleshipHP(effect.getSourceId()); // copy this as well
                }
                removeFamiliar();
            }
            effects.put(statup.getKey(), new MapleBuffStatValueHolder(effect, starttime, schedule, value, localDuration, cid));
        }
        if (clonez > 0) {
            int cloneSize = Math.max(getNumClones(), getCloneSize());
            if (clonez > cloneSize) { // how many clones to summon
                for (int i = 0; i < clonez - cloneSize; i++) { // 1-1=0
                    cloneLook();
                }
            }
        }
        if (!silent) {
            //System.out.println("test");
            stats.recalcLocalStats(this);
        }
        // System.out.println("Effect registered. Effect: " + effect.getSourceId());
    }

    public List<MapleBuffStat> getBuffStats(final MapleStatEffect effect, final long startTime) {
        final List<MapleBuffStat> bstats = new ArrayList<MapleBuffStat>();
        final Map<MapleBuffStat, MapleBuffStatValueHolder> allBuffs = new EnumMap<MapleBuffStat, MapleBuffStatValueHolder>(
                effects);
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : allBuffs.entrySet()) {
            final MapleBuffStatValueHolder mbsvh = stateffect.getValue();
            if (mbsvh.effect.sameSource(effect)
                    && (startTime == -1 || startTime == mbsvh.startTime || stateffect.getKey().canStack())) {
                bstats.add(stateffect.getKey());
            }
        }
        return bstats;
    }

    private boolean deregisterBuffStats(List<MapleBuffStat> stats) {
        boolean clonez = false;
        chrLock.lock();
        try {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
            for (MapleBuffStat stat : stats) {
                final MapleBuffStatValueHolder mbsvh = effects.remove(stat);
                if (mbsvh != null) {
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET || stat == MapleBuffStat.REAPER
                            || stat == MapleBuffStat.BEHOLDER || stat == MapleBuffStat.DAMAGE_BUFF
                            || stat == MapleBuffStat.RAINING_MINES || stat == MapleBuffStat.ANGEL_ATK) {
                        final int summonId = mbsvh.effect.getSourceId();
                        for (MapleSummon summon : getSummonsValues()) {
                            if (summon.getSkill() == summonId
                                    || (stat == MapleBuffStat.RAINING_MINES && summonId == 33101008)
                                    || (summonId == 35121009 && summon.getSkill() == 35121011)
                                    || ((summonId == 86 || summonId == 88 || summonId == 91)
                                    && summon.getSkill() == summonId + 999)
                                    || ((summonId == 1085 || summonId == 1087 || summonId == 1090 || summonId == 1179)
                                    && summon.getSkill() == summonId - 999)) { // removes bots n tots
                                map.broadcastMessage(SummonPacket.removeSummon(summon, true));
                                map.removeMapObject(summon, MapleMapObjectType.SUMMON);
                                map.destoryObject(summon);
                                removeSummon(summon);
                                // summons.remove(summon);
                            }
                        }
                        if (summonId == 3111005 || summonId == 3211005) {
                            cancelEffectFromBuffStat(MapleBuffStat.SPIRIT_LINK);
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        lastDragonBloodTime = 0;
                    } else if (stat == MapleBuffStat.RECOVERY || mbsvh.effect.getSourceId() == 35121005) {
                        lastRecoveryTime = 0;
                    } else if (stat == MapleBuffStat.HOMING_BEACON || stat == MapleBuffStat.ARCANE_AIM) {
                        linkMobs.clear();
                    } else if (stat == MapleBuffStat.ILLUSION) {
                        disposeClones();
                        clonez = true;
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).size() == 0) {
                    if (cancelEffectCancelTasks.schedule != null) {
                        cancelEffectCancelTasks.schedule.cancel(false);
                    }
                }
            }

        } finally {
            chrLock.unlock();
        }
        return clonez;
    }

    /**
     * @param effect
     * @param overwrite when overwrite is set no data is sent and all the
     * Buffstats in the StatEffect are deregistered
     * @param startTime
     */
    public void cancelEffect(final MapleStatEffect effect, final boolean overwrite, final long startTime) {
        if (effect == null) {
            return;
        }
        cancelEffect(effect, overwrite, startTime, effect.getStatups());
    }

    public void cancelEffect(final MapleStatEffect effect, final boolean overwrite, final long startTime, Map<MapleBuffStat, Integer> statups) {
        chrLock.lock();
        try {
            if (effect == null) {
                return;
            }
            List<MapleBuffStat> buffstats;
            if (!overwrite) {
                buffstats = getBuffStats(effect, startTime);
            } else {
                buffstats = new ArrayList<MapleBuffStat>(statups.keySet());
            }
            if (buffstats.size() <= 0) {
                return;
            }
            if (effect.isInfinity() && getBuffedValue(MapleBuffStat.INFINITY) != null) { // before
                int duration = Math.max(effect.getDuration(), effect.alchemistModifyVal(this, effect.getDuration(), false));
                final long start = getBuffedStarttime(MapleBuffStat.INFINITY);
                duration += (int) ((start - System.currentTimeMillis()));
                if (duration > 0) {
                    final int neworbcount = getBuffedValue(MapleBuffStat.INFINITY) + effect.getDamage();
                    final Map<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                    stat.put(MapleBuffStat.INFINITY, neworbcount);

                    setBuffedValue(MapleBuffStat.INFINITY, neworbcount);

                    client.announce(BuffPacket.giveBuff(effect.getSourceId(), duration, stat, effect));
                    addHP((int) (effect.getHpR() * this.stats.getCurrentMaxHp()));
                    addMP((int) (effect.getMpR() * this.stats.getCurrentMaxMp(this.getJob())));
                    setSchedule(MapleBuffStat.INFINITY, BuffTimer.getInstance().schedule(new CancelEffectAction(this, effect, start, stat), effect.alchemistModifyVal(this, 4000, false)));

                    return;
                }
            }
            final boolean clonez = deregisterBuffStats(buffstats);
            if (effect.isMagicDoor()) {
                // remove for all on maps
                if (!getDoors().isEmpty()) {
                    removeDoor();
                    silentPartyUpdate();
                }
            } else if (effect.isMechDoor()) {
                if (!getMechDoors().isEmpty()) {
                    removeMechDoor();
                }
            } else if (effect.isMonsterRiding_()) {
                getMount().cancelSchedule();
            } else if (effect.isMonsterRiding()) {
                cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
            } else if (effect.isAranCombo()) {
                combo = 0;
            }
            // check if we are still logged in o.o
            cancelPlayerBuffs(buffstats, overwrite);
            if (!overwrite) {
                if (effect.isHide()
                        && client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null) { // Wow
                    // this
                    // is
                    // so
                    // fking
                    // hacky...
                    map.broadcastMessage(this, CField.spawnPlayerMapobject(this, false), false);

                    for (final MaplePet pet : pets) {
                        if (pet.getSummoned()) {
                            map.broadcastMessage(this, PetPacket.showPet(this, pet, false, false), false);
                        }
                    }
                    for (final WeakReference<MapleCharacter> chr : clones) {
                        if (chr.get() != null) {
                            map.broadcastMessage(chr.get(), CField.spawnPlayerMapobject(chr.get(), false), false);
                        }
                    }
                }
            }
            if (effect.getSourceId() == 35121013 && !overwrite) { // when siege 2 deactivates, missile re-activates
                SkillFactory.getSkill(35121005).getEffect(getTotalSkillLevel(35121005)).applyTo(this);
            }
            if (!clonez) {
                for (WeakReference<MapleCharacter> chr : clones) {
                    if (chr.get() != null) {
                        chr.get().cancelEffect(effect, overwrite, startTime);
                    }
                }
            }
        } finally {
            chrLock.unlock();
        }
        // System.out.println("Effect deregistered. Effect: " + effect.getSourceId());
    }

    public void cancelBuffStats(MapleBuffStat... stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList, false);
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        if (effects.get(stat) != null) {
            cancelEffect(effects.get(stat).effect, false, -1);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat, int from) {
        if (effects.get(stat) != null && effects.get(stat).cid == from) {
            cancelEffect(effects.get(stat).effect, false, -1);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats, boolean overwrite) {
        boolean write = client != null && client.getChannelServer() != null && client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null;
        if (buffstats.contains(MapleBuffStat.HOMING_BEACON)) {
            client.announce(BuffPacket.cancelHoming());
        } else {
            if (overwrite) {
                List<MapleBuffStat> z = new ArrayList<MapleBuffStat>();
                for (MapleBuffStat s : buffstats) {
                    if (s.canStack()) {
                        z.add(s);
                    }
                }
                if (z.size() > 0) {
                    buffstats = z;
                } else {
                    return; // don't write anything
                }
            } else if (write) {
                stats.recalcLocalStats(this);
            }
            client.announce(BuffPacket.cancelBuff(buffstats));
            map.broadcastMessage(this, BuffPacket.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public void dispelAll() {
        if (!isHidden() && isAlive()) {
            final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void dispel() {
        if (!isHidden() && isAlive()) {
            final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.effect.isSkill() && mbsvh.schedule != null && !mbsvh.effect.isMorph()
                        && !mbsvh.effect.isGmBuff() && !mbsvh.effect.isMonsterRiding() && !mbsvh.effect.isMechChange()
                        && !mbsvh.effect.isEnergyCharge() && !mbsvh.effect.isAranCombo()) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }

    public void dispelSkill(int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void dispelSummons() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSummonMovementType() != null) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void dispelBuff(int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void cancelAllBuffs_() {
        effects.clear();
    }

    public void cancelAllBuffs() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

    public void cancelMorphs() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.effect.getSourceId()) {
                case 5111005:
                case 5121003:
                case 15111002:
                case 13111005:
                    return; // Since we can't have more than 1, save up on loops
                default:
                    if (mbsvh.effect.isMorph()) {
                        cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                        continue;
                    }
            }
        }
    }

    public int getMorphState() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph()) {
                return mbsvh.effect.getSourceId();
            }
        }
        return -1;
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        if (buffs == null) {
            return;
        }
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime, mbsvh.localDuration, mbsvh.statup, mbsvh.cid);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        final List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        final Map<Pair<Integer, Integer>, Integer> alreadyDone = new HashMap<Pair<Integer, Integer>, Integer>();
        final LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>> allBuffs = new LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>>(effects.entrySet());
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> mbsvh : allBuffs) {
            final Pair<Integer, Integer> key = new Pair<Integer, Integer>(mbsvh.getValue().effect.getSourceId(), mbsvh.getValue().effect.getLevel());
            if (alreadyDone.containsKey(key)) {
                ret.get(alreadyDone.get(key)).statup.put(mbsvh.getKey(), mbsvh.getValue().value);
            } else {
                alreadyDone.put(key, ret.size());
                final EnumMap<MapleBuffStat, Integer> list = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                list.put(mbsvh.getKey(), mbsvh.getValue().value);
                ret.add(new PlayerBuffValueHolder(mbsvh.getValue().startTime, mbsvh.getValue().effect, list, mbsvh.getValue().localDuration, mbsvh.getValue().cid));
            }
        }
        return ret;
    }

    public void cancelMagicDoor() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(
                effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public int getSkillLevel(int skillid) {
        return getSkillLevel(SkillFactory.getSkill(skillid));
    }

    public int getTotalSkillLevel(int skillid) {
        return getTotalSkillLevel(SkillFactory.getSkill(skillid));
    }

    public final void handleEnergyCharge(final int skillid, final int targets) {
        final Skill echskill = SkillFactory.getSkill(skillid);
        final int skilllevel = getTotalSkillLevel(echskill);
        if (skilllevel > 0) {
            final MapleStatEffect echeff = echskill.getEffect(skilllevel);
            if (targets > 0) {
                if (getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null) {
                    echeff.applyEnergyBuff(this, true); // Infinity time
                }
                Integer energyLevel = getBuffedValue(MapleBuffStat.ENERGY_CHARGE);
                if (energyLevel < 10000) {
                    // TODO: bar going down
                    if (energyLevel < 0) {
                        energyLevel = 0;
                    }
                    energyLevel += echeff.getX();

                    client.announce(EffectPacket.showOwnBuffEffect(skillid, 2, getLevel(), skilllevel));
                    map.broadcastMessage(this, EffectPacket.showBuffeffect(id, skillid, 2, getLevel(), skilllevel), false);

                    if (energyLevel >= 10000) {
                        energyLevel = 10000;
                    }
                    client.announce(BuffPacket.giveEnergyChargeTest(energyLevel, echeff.getDuration() / 1000));
                    if (energyLevel < 10000) {
                        setBuffedValue(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(energyLevel));
                    } else if (energyLevel == 10000) {
                        echeff.applyEnergyBuff(this, false); // One with time
                        setBuffedValue(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(10001));
                    }
                }
            }
        }
    }

    public final void handleBattleshipHP(int damage) {
        if (damage < 0) {
            final MapleStatEffect effect = getStatForBuff(MapleBuffStat.MONSTER_RIDING);
            if (effect != null && effect.getSourceId() == 5221006) {
                battleshipHP += damage;
                client.announce(CField.skillCooldown(5221999, battleshipHP / 10));
                if (battleshipHP <= 0) {
                    battleshipHP = 0;
                    client.announce(CField.skillCooldown(5221006, effect.getCooldown(this)));
                    addCooldown(5221006, System.currentTimeMillis(), effect.getCooldown(this));
                    cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                }
            }
        }
    }

    public final void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.COMBO);
        Skill combo;
        Skill advcombo;

        switch (getJob()) {
            case 1110:
            case 1111:
            case 1112:
                combo = SkillFactory.getSkill(11111001);
                advcombo = SkillFactory.getSkill(11110005);
                break;
            default:
                combo = SkillFactory.getSkill(1111002);
                advcombo = SkillFactory.getSkill(1120003);
                break;
        }
        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getTotalSkillLevel(advcombo);
        if (advComboSkillLevel > 0) {
            ceffect = advcombo.getEffect(advComboSkillLevel);
        } else if (getSkillLevel(combo) > 0) {
            ceffect = combo.getEffect(getTotalSkillLevel(combo));
        } else {
            return;
        }

        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                if (neworbcount < ceffect.getX() + 1) {
                    neworbcount++;

                }
            }
            EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class
            );
            stat.put(MapleBuffStat.COMBO, neworbcount);

            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

            client.announce(BuffPacket.giveBuff(combo.getId(), duration, stat, ceffect));
            map.broadcastMessage(this, BuffPacket.giveForeignBuff(getId(), stat, ceffect), false);
        }
    }

    public void handleOrbconsume(int howmany) {
        Skill combo;

        switch (getJob()) {
            case 1110:
            case 1111:
            case 1112:
                combo = SkillFactory.getSkill(11111001);
                break;
            default:
                combo = SkillFactory.getSkill(1111002);
                break;
        }
        if (getSkillLevel(combo) <= 0) {
            return;
        }
        MapleStatEffect ceffect = getStatForBuff(MapleBuffStat.COMBO);
        if (ceffect == null) {
            return;

        }
        EnumMap<MapleBuffStat, Integer> stat = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class
        );
        stat.put(MapleBuffStat.COMBO, Math.max(1, getBuffedValue(MapleBuffStat.COMBO) - howmany));
        setBuffedValue(MapleBuffStat.COMBO, Math.max(1, getBuffedValue(MapleBuffStat.COMBO) - howmany));
        int duration = ceffect.getDuration();
        duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

        client.announce(BuffPacket.giveBuff(combo.getId(), duration, stat, ceffect));
        map.broadcastMessage(this, BuffPacket.giveForeignBuff(getId(), stat, ceffect), false);
    }

    public void silentEnforceMaxHpMp() {
        stats.setMp(stats.getMaxMp(), this);
        stats.setHp(stats.getMaxHp(), true, this);
    }

    public void enforceMaxHpMp() {
        Map<MapleStat, Integer> statups = new EnumMap<MapleStat, Integer>(MapleStat.class);
        if (stats.getMp() > stats.getCurrentMaxMp(this.getJob())) {
            stats.setMp(stats.getMp(), this);
            statups.put(MapleStat.MP, Integer.valueOf(stats.getMp()));
        }

        if (stats.getHp() > stats.getCurrentMaxHp()) {
            stats.setHp(stats.getHp(), this);
            statups.put(MapleStat.HP, Integer.valueOf(stats.getHp()));
        }

        if (statups.size() > 0) {
            client.announce(CWvsContext.updatePlayerStats(statups, this));
        }
    }

    public void refreshMaxHpMp(boolean update) {
        if (update) {
            stats.recalcLocalStats(this);
        }
        Map<MapleStat, Integer> statups = new EnumMap<>(MapleStat.class);
        stats.setMp(stats.getMp(), this);
        statups.put(MapleStat.MP, stats.getMp());
        stats.setHp(stats.getHp(), this);
        statups.put(MapleStat.HP, stats.getHp());
        client.getSession().write(CWvsContext.updatePlayerStats(statups, this));
    }

    public MapleMap getMap() {
        return map;
    }

    public MonsterBook getMonsterBook() {
        return monsterbook;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public boolean isJailed() {
        return getMapId() == 10;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public byte getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public final String getBlessOfFairyOrigin() {
        return this.BlessOfFairy_Origin;
    }

    public final String getBlessOfEmpressOrigin() {
        return this.BlessOfEmpress_Origin;
    }

    public final short getLevel() {
        return level;
    }

    public final int getFame() {
        return fame;
    }

    public final int getFallCounter() {
        return fallcounter;
    }

    public final MapleClient getClient() {
        return client;
    }

    public final void setClient(final MapleClient client) {
        this.client = client;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return Randomizer.MaxShort(remainingSp, (short) 9999); // default
    }

    public void addRemainingSp(int value) {
        remainingSp = (short) Randomizer.Max(remainingSp + value, (short) 9999);
    }

    public short getHpApUsed() {
        return hpApUsed;
    }

    public void setHidden(boolean toggle) {
        hidden = toggle;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHpApUsed(short hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    @Override
    public byte getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(byte skinColor) {
        this.skinColor = skinColor;
    }

    @Override
    public short getJob() {
        return job;
    }

    @Override
    public byte getGender() {
        return gender;
    }

    @Override
    public int getHair() {
        return hair;
    }

    @Override
    public int getFace() {
        return face;
    }

    @Override
    public int getDemonMarking() {
        return demonMarking;
    }

    public void setDemonMarking(int mark) {
        this.demonMarking = mark;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExp(int exp) {
        this.exp.set(exp);
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFallCounter(int fallcounter) {
        this.fallcounter = fallcounter;
    }

    public Point getOldPosition() {
        return old;
    }

    public void setOldPosition(Point x) {
        this.old = x;
    }

    public void setStat(int amount, int type) {//kaotic
        final PlayerStats playerst = getStat();
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class
        );
        if (getRemainingAp() >= amount) {
            switch (type) {
                case 1: // Str
                    playerst.setStr((playerst.getStr() + amount), this);
                    statupdate.put(MapleStat.STR, (int) playerst.getStr());
                    break;
                case 2: // Dex
                    playerst.setDex((playerst.getDex() + amount), this);
                    statupdate.put(MapleStat.DEX, (int) playerst.getDex());
                    break;
                case 3: // Int
                    playerst.setInt((playerst.getInt() + amount), this);
                    statupdate.put(MapleStat.INT, (int) playerst.getInt());
                    break;
                case 4: // Luk
                    playerst.setLuk((playerst.getLuk() + amount), this);
                    statupdate.put(MapleStat.LUK, (int) playerst.getLuk());
                    break;
                case 5: // HP
                    int maxhp = stats.getMaxHp();
                    if (maxhp < GameConstants.getMaxHpMp()) {
                        maxhp += (amount * 100);
                    }
                    maxhp = Randomizer.Max(maxhp, GameConstants.getMaxHpMp());
                    statupdate.put(MapleStat.MAXHP, maxhp);
                    statupdate.put(MapleStat.HP, maxhp);
                    playerst.setMaxHp((playerst.getMaxHp() + amount), this);
                    break;
                case 6: // MP
                    if (!GameConstants.isDemon(job)) {
                        int maxmp = stats.getMaxMp();
                        if (maxmp < GameConstants.getMaxHpMp()) {
                            maxmp += (amount * 100);
                        }
                        maxmp = Randomizer.Max(maxmp, GameConstants.getMaxHpMp());
                        statupdate.put(MapleStat.MAXMP, maxmp);
                        statupdate.put(MapleStat.MP, maxmp);
                        playerst.setMaxMp((playerst.getMaxMp() + amount), this);
                        break;
                    } else {
                        dropMessage("Mp cannot be applied");
                    }
                default:
                    client.announce(CWvsContext.enableActions());
                    return;
            }
            setRemainingAp((getRemainingAp() - amount));
            statupdate.put(MapleStat.AVAILABLEAP, (int) getRemainingAp());
            client.announce(CWvsContext.updatePlayerStats(statupdate, true, this));
            saveCharToDB2();
        }
    }

    public void addRemainingAp(int remainingAp) {
        this.remainingAp += remainingAp;
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statupdate.put(MapleStat.AVAILABLEAP, this.remainingAp);
        client.announce(CWvsContext.updatePlayerStats(statupdate, true, this));
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statupdate.put(MapleStat.AVAILABLEAP, this.remainingAp);
        client.announce(CWvsContext.updatePlayerStats(statupdate, true, this));
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = (short) Randomizer.Max(remainingSp, (short) 9999); // default
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public void setInvincible(boolean invinc) {
        invincible = invinc;
    }

    public boolean isInvincible() {
        if (getBuffedValue(MapleBuffStat.WATER_SHIELD) != null) {
            return true;
        }
        return invincible;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
        getTrait(MapleTraitType.charm).addLocalExp(famechange);
        if (this.fame >= 10) {
            finishAchievement(7);
        }
        if (this.fame >= 25) {
            finishAchievement(121);
        }
        if (this.fame >= 50) {
            finishAchievement(122);
        }
        if (this.fame >= 100) {
            finishAchievement(123);
        }
        if (this.fame >= 250) {
            finishAchievement(124);
        }
        if (this.fame >= 500) {
            finishAchievement(178);
        }
        if (this.fame >= 1000) {
            finishAchievement(125);
        }
    }

    public void portalDelay(long delay) {
        this.portaldelay = System.currentTimeMillis() + delay;
    }

    public long portalDelay() {
        return portaldelay;
    }

    public boolean getPortalDelay() {
        return this.portaldelay > System.currentTimeMillis();
    }

    public void forceChangeMap(final MapleMap target, final MaplePortal pto, final Point pos) {
        // will actually enter the map given as parameter, regardless of being an
        // eventmap or whatnot
        if (target == null) {
            dropMessage("Error with portal, Please report");
        }
        eventChangedMap(999999999);

        EventInstanceManager mapEim = target.getEventInstance();
        if (mapEim != null) {
            EventInstanceManager playerEim = this.getEventInstance();
            if (playerEim != null) {
                playerEim.exitPlayer(this);
                if (playerEim.getPlayerCount() == 0) {
                    playerEim.dispose();
                }
            }

            // thanks Thora for finding an issue with players not being actually warped into
            // the target event map (rather sent to the event starting map)
            mapEim.registerPlayer(this);
        }

        MapleMap to = target; // warps directly to the target intead of the target's map id, this allows GMs
        // to patrol players inside instances.
        changeMapInternal(to, pos, CField.getWarpToMap(to, pto.getId(), this));
        eventAfterChangedMap(this.getMapId());
    }

    public void changeMapBanish(final int mapid, final String portal, final String msg) {
        dropMessage(5, msg);
        final MapleMap map = client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(map, map.getPortal(portal));
    }

    public void changeMapbyId(final int mapid) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null && !eventExit) {
            warpMap = eim.getMapInstance(mapid);
        } else {
            warpMap = this.getClient().getChannelServer().getMapFactory().getMap(mapid);
        }

        changeMap(warpMap);
    }

    public void exitMapbyId(final int mapid) {
        eventExit = false;
        changeMap(this.getClient().getChannelServer().getMapFactory().getMap(mapid));
    }

    public void changeMap(int map, MaplePortal portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null && !eventExit) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = this.getClient().getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, portal);
    }

    public void changeMap(int map, String portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null && !eventExit) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = this.getClient().getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();

        if (eim != null && !eventExit) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = this.getClient().getChannelServer().getMapFactory().getMap(map);
        }

        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMap(MapleMap to) {
        changeMap(to, to.getRandomSpawnPortal());
    }

    public void changeMap(MapleMap to, int portal) {
        if (to != null) {
            changeMap(to, to.getPortal(portal));
        } else {
            dropMessage("Error with portal, Please report");
        }
    }

    public void warpTo(final MaplePortal pto) {
        if (pto != null) {
            client.announce(CField.instantMapWarp((byte) pto.getId()));
        } else {
            dropMessage("Error with portal, Please report");
        }
    }

    public void warpTo(final String name) {
        //System.out.println("name: " + name);
        client.announce(CField.instantMapWarp((byte) getMap().getPortal(name).getId()));
    }

    public void changeMap(final MapleMap target, final MaplePortal pto) {
        if (target != null) {
            MapleMap to = getWarpMap(target.getId());
            byte[] warpPacket;
            if (pto != null) {
                warpPacket = CField.getWarpToMap(to, pto.getId(), this);
                changeMapInternal(to, pto.getPosition(), warpPacket);
            } else {
                changeMap(to.getReturnMap());
            }
        } else {
            dropMessage("Error with portal, Please report");
        }
    }

    public void changeMapPortal(final MapleMap to, final MaplePortal pto) {
        if (to != null) {
            changeMap(to, pto);
        } else {
            dropMessage("Error with portal, Please report");
        }
    }

    public boolean isMapChange() {
        return changingMap;
    }

    private void changeMapInternal(final MapleMap to, final Point pos, byte[] warpPacket) {
        if (to == null) {
            return;
        }
        if (getEventInstance() != null) {
            getEventInstance().changedMap(this, to.getId());
        }
        if (!eventExit) {
            if (isMapChange()) {
                dropMessage("Portal on cooldown");
                return;
            }
            if (getTrade() != null) {
                MapleTrade.cancelTrade(getTrade(), client, this);
            }
            if (isStorageOpened()) {
                getStorage().saveToDB(false);
                StorageOpened = false;
            }
            if (client.getCMS() != null) {
                client.getCMS().dispose();
            }
            isChangingMaps(true);
            changingMap = true;
            portalDelay(1500);
            final boolean pyramid = pyramidSubway != null;
            //System.out.println("from: " + map.getId());
            //System.out.println("to: " + to.getId());
            map.removePlayer(this);
            client.announce(warpPacket);
            setAtkCooldown(0);
            setLock(false);
            map = to;
            setPosition(pos);
            to.addPlayer(this, false);

            stats.relocHeal(this);
            silentPartyUpdate();
            if (pyramid && pyramidSubway != null) { // checks if they had pyramid before AND after changing
                pyramidSubway.onChangeMap(this, to.getId());
            }
        } else {
            eventExit = false;
        }
    }

    public void cancelTrade() {
        MapleTrade.cancelTrade(getTrade(), client, this);
    }

    private void changeMapInternal(final MapleMap to, final MaplePortal pto) {//new code portal
        if (to == null) {
            return;
        }
        long cd = 250;
        long curr = System.currentTimeMillis();
        if (curr - portalLock < cd) {
            return;
        }
        if (getTrade() != null) {
            MapleTrade.cancelTrade(getTrade(), client, this);
        }
        if (isStorageOpened()) {
            getStorage().saveToDB(false);
            StorageOpened = false;
        }
        if (client.getCMS() != null) {
            client.getCMS().dispose();
        }
        portalLock = (System.currentTimeMillis() + 250);
        isChangingMaps(true);
        if (getEventInstance() != null) {
            getEventInstance().changedMap(this, to.getId());
        }
        final boolean pyramid = pyramidSubway != null;
        //System.out.println("from: " + map.getId());
        //System.out.println("to: " + to.getId());
        client.announce(CField.getWarpToMap(to, pto.getId(), this));
        map.removePlayer(this);
        setAtkCooldown(0);
        setLock(false);
        map = to;
        setPosition(pto.getPosition());
        to.addPlayer(this, false);
        stats.relocHeal(this);
        silentPartyUpdate();
        if (pyramid && pyramidSubway != null) { // checks if they had pyramid before AND after changing
            pyramidSubway.onChangeMap(this, to.getId());
        }
        eventAfterChangedMap(map.getId());
    }

    public void closePlayerShop() {
        if (getPlayerShop() != null && getPlayerShop().isOwner(this)) {
            if (getPlayerShop().isAvailable()) {
                getPlayerShop().removeVisitor(this, 3);
            }
            getPlayerShop().closeShop(true);
        }
    }

    public void cancelChallenge() {
        if (challenge != 0 && client.getChannelServer() != null) {
            final MapleCharacter chr = client.getChannelServer().getPlayerStorage().getCharacterById(challenge);
            if (chr != null) {
                chr.dropMessage(6, getName() + " has denied your request.");
                chr.setChallenge(0);
            }
            dropMessage(6, "Denied the challenge.");
            challenge = 0;
        }
    }

    public void releaseControlledMonsters() {
        controlledLock.writeLock().lock();
        try {
            Collection<MapleMonster> controlledMonsters;

            controlledMonsters = new ArrayList<>(controlled);
            controlled.clear();

            for (MapleMonster mons : controlledMonsters) {
                mons.setController(null);
                mons.setControllerHasAggro(false);
            }
        } finally {
            controlledLock.writeLock().unlock();
        }
    }

    public void leaveMap(MapleMap map) {
        if (getBuffedValue(MapleBuffStat.MORPH) != null) {
            cancelMorphs();
        }
        setShop(null);
        closePlayerShop();
        releaseControlledMonsters();
        if (chair != 0) {
            chair = 0;
        }
        clearLinkMid();
        cancelFishingTask();
        cancelChallenge();
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.clear();
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
        cancelMapTimeLimitTask();
        if (getTrade() != null) {
            MapleTrade.cancelTrade(getTrade(), client, this);
        }
        client.closePlayerScriptInteractions();
    }

    public void changeJob(int newJob) {
        try {
            cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
            this.job = (short) newJob;
            updateSingleStat(MapleStat.JOB, newJob);
            map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 11), false);
            if (GameConstants.isEvan(job)) {
                if (getDragon() == null) {
                    makeDragon();
                } else {
                    getDragon().setPosition(getMap().calcPointBelow(getPosition()));
                }
                if (getDragon() != null) {
                    getMap().broadcastMessage(CField.spawnDragon(getDragon()));
                }
            }
            if (dragon != null) {
                map.broadcastMessage(CField.removeDragon(this.id));
                dragon = null;
            }

            baseSkills();
            stats.recalcLocalStats(this);
            silentPartyUpdate();
            guildUpdate();
            familyUpdate();
            saveToDB();
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e); // all jobs throw errors :(
        }
    }

    public void recalcLocalStats() {
        this.stats.recalcLocalStats(this);
    }

    public boolean isMultiJob(int job) {
        if (job >= 2200 && job <= 2218) { // evan
            return true;
        }
        if (job >= 430 && job <= 434) { // db
            return true;
        }
        return false;
    }

    public void baseSkills() {
        Map<Skill, SkillEntry> list = new HashMap<>();
        List<Integer> skills = SkillFactory.getSkillsByClass(job / 10);
        if (skills != null) {
            if (!skills.isEmpty()) {
                for (int i : skills) {
                    final Skill skil = SkillFactory.getSkill(i);

                    if (skil != null && !skil.isInvisible()) {
                        if (skil.getMasterLevel() > 0 && !checkSkill(skil)) {
                            list.put(skil, new SkillEntry(getSkillLevel(skil), skil.getMasterLevel() > 0 ? skil.getMasterLevel() : skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil))); // usually 10 master
                        }
                    }
                }
            }
        } else {
            System.out.println("Skill by class is empty for classid: " + (job / 10));
        }
        Skill skil;
        if (job >= 2211 && job <= 2218) { // evan fix magic guard
            skil = SkillFactory.getSkill(22111001);
            if (skil != null) {
                if (getSkillLevel(skil) <= 0 && !checkSkill(skil)) { // no total
                    list.put(skil, new SkillEntry(0, 15, -1));
                }
            }
        }
        if (GameConstants.isMercedes(job)) {
            final int[] ss = {20021000, 20021001, 20020002, 20020022, 20020109, 20021110, 20020111, 20020112};
            for (int i : ss) {
                skil = SkillFactory.getSkill(i);
                if (skil != null) {
                    if (getSkillLevel(skil) <= 0 && !checkSkill(skil)) { // no total
                        list.put(skil, new SkillEntry((byte) 1, (byte) 1, -1));
                    }
                }
            }
        }
        if (GameConstants.isDemon(job)) {
            final int[] ss1 = {30010185, 30010112, 30010111, 30010110, 30011109};//(demon wings)
            for (int i : ss1) {
                skil = SkillFactory.getSkill(i);
                if (skil != null) {
                    if (getSkillLevel(skil) <= 0 && !checkSkill(skil)) { // no total
                        list.put(skil, new SkillEntry((byte) 1, (byte) 1, -1));
                    }
                }
            }
        }

        if (GameConstants.isAdventurer(job)) {
            if (getSkillLevel(SkillFactory.getSkill(190)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(1003)) <= 0 && !checkSkill(SkillFactory.getSkill(1003))) {
                list.put(SkillFactory.getSkill(1003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(3101003)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(3101003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(8)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(8), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isAran(job)) {
            if (getSkillLevel(SkillFactory.getSkill(20001003)) <= 0 && !checkSkill(SkillFactory.getSkill(20001003))) {
                list.put(SkillFactory.getSkill(20001003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isEvan(job)) {
            if (getSkillLevel(SkillFactory.getSkill(20011003)) <= 0 && !checkSkill(SkillFactory.getSkill(20011003))) {
                list.put(SkillFactory.getSkill(20011003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(20010190)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(20010190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(20011024)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(20011024), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isMercedes(job)) {
            if (getSkillLevel(SkillFactory.getSkill(20021003)) <= 0 && !checkSkill(SkillFactory.getSkill(20021003))) {
                list.put(SkillFactory.getSkill(20021003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(20020190)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(20020190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(20021024)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(20021024), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isKOC(job)) {
            if (getSkillLevel(SkillFactory.getSkill(10001003)) <= 0 && !checkSkill(SkillFactory.getSkill(10001003))) {
                list.put(SkillFactory.getSkill(10001003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(10000190)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(10000190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(10000018)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(10000018), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(3101003)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(3101003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isResist(job)) {
            if (getSkillLevel(SkillFactory.getSkill(30001003)) <= 0 && !checkSkill(SkillFactory.getSkill(30001003))) {
                list.put(SkillFactory.getSkill(30001003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(30000190)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(30000190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(30001024)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(30001024), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isDemon(job)) {
            if (getSkillLevel(SkillFactory.getSkill(30001003)) <= 0 && !checkSkill(SkillFactory.getSkill(30001003))) {
                list.put(SkillFactory.getSkill(30001003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(30010190)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(30010190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (getSkillLevel(SkillFactory.getSkill(30011024)) <= 0 && !checkSkill(SkillFactory.getSkill(190))) {
                list.put(SkillFactory.getSkill(30011024), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        list.put(SkillFactory.getSkill(80000002), new SkillEntry((byte) 1, (byte) 0, -1));
        if (!list.isEmpty()) {
            changeSkillsLevel(list);
        }
    }

    public void makeDragon() {
        dragon = new MapleDragon(this);
        map.broadcastMessage(CField.spawnDragon(dragon));
    }

    public MapleDragon getDragon() {
        return dragon;
    }

    public void gainAp(int ap) {
        this.remainingAp += ap;
        updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
    }

    public void gainSP(int sp) {
        addRemainingSp(sp);
        updateSingleStat(MapleStat.AVAILABLESP, 0); // we don't care the value here
        client.announce(InfoPacket.getSPMsg((byte) sp, (short) job));
    }

    public void resetSP(int sp) {
        setRemainingSp(sp);
        updateSingleStat(MapleStat.AVAILABLESP, 0); // we don't care the value here
    }

    public void resetAPSP() {
        resetSP(0);
        gainAp((short) -this.remainingAp);
    }

    public List<Integer> getProfessions() {
        List<Integer> prof = new ArrayList<Integer>();
        for (int i = 9200; i <= 9204; i++) {
            if (getProfessionLevel(id * 10000) > 0) {
                prof.add(i);
            }
        }
        return prof;
    }

    public byte getProfessionLevel(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (byte) ((ret >>> 24) & 0xFF); // the last byte
    }

    public short getProfessionExp(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (short) (ret & 0xFFFF); // the first two byte
    }

    public boolean addProfessionExp(int id, int expGain) {
        int ret = getProfessionLevel(id);
        if (ret <= 0 || ret >= 10) {
            return false;
        }
        int newExp = getProfessionExp(id) + expGain;
        if (newExp >= GameConstants.getProfessionEXP(ret)) {
            // gain level
            changeProfessionLevelExp(id, ret + 1, newExp - GameConstants.getProfessionEXP(ret));
            int traitGain = (int) Math.pow(2, ret + 1);
            switch (id) {
                case 92000000:
                    traits.get(MapleTraitType.sense).addExp(traitGain, this);
                    break;
                case 92010000:
                    traits.get(MapleTraitType.will).addExp(traitGain, this);
                    break;
                case 92020000:
                case 92030000:
                case 92040000:
                    traits.get(MapleTraitType.craft).addExp(traitGain, this);
                    break;
            }
            return true;
        } else {
            changeProfessionLevelExp(id, ret, newExp);
            return false;
        }
    }

    public void changeProfessionLevelExp(int id, int level, int exp) {
        changeSingleSkillLevel(SkillFactory.getSkill(id), ((level & 0xFF) << 24) + (exp & 0xFFFF), (byte) 10);
    }

    public void SkillLevelup(int skillid, int newLevel) { // 1 month
        Skill skill = SkillFactory.getSkill(skillid);
        if (skill == null) {
            return;
        }
        changeSingleSkillLevel(skill, getSkillLevel(skill) + newLevel, skill.getMasterLevel(), SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(int skillid, int newLevel) { // 1 month
        Skill skill = SkillFactory.getSkill(skillid);
        if (skill == null) {
            return;
        }
        changeSingleSkillLevel(skill, newLevel, skill.getMasterLevel(), SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(int skillid, int newLevel, int newMasterlevel) { // 1 month
        Skill skill = SkillFactory.getSkill(skillid);
        if (skill == null) {
            return;
        }
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(final Skill skill, int newLevel, int newMasterlevel) { // 1 month
        if (skill == null) {
            return;
        }
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
    }

    public Skill getSkillbyId(int id) {
        return SkillFactory.getSkill(id);
    }

    public int getSkillOverLevel(Skill skill) {
        if (skills.containsKey(skill)) {
            return skills.get(skill).getLevel();
        } else {
            System.out.println("[Level] Skill: " + skill.getName() + " ID: " + skill.getId() + " Not valid");
        }
        return 1;
    }

    public long getSkillExp(Skill skill) {
        if (skills.containsKey(skill)) {
            return skills.get(skill).getExp();
        } else {
            System.out.println("[Exp] Skill: " + skill.getName() + " ID: " + skill.getId() + " Not valid");
        }
        return 0;
    }

    public int getSkillExpPercent(Skill skill) {
        return (int) (((double) getSkillExp(skill) / (double) getSkillNeededExp(skill)) * 100);
    }

    public long getSkillNeededExp(Skill skill) {
        if (skills.get(skill).getLevel() >= 9999) {
            return 0;
        }
        return (long) (Math.pow(skills.get(skill).getLevel(), 3) + 250);
    }

    public void gainSkillExp(Skill skill, long gain) { // needa
        SkillEntry skil = skills.get(skill);
        if (skil != null && skil.getLevel() >= 1 && skil.getLevel() < 9999) {
            long nexp = getSkillNeededExp(skill);
            if (nexp > 0) {
                skil.exp += gain;
                if (skil.exp >= nexp) {
                    skil.exp = 0;
                    skil.level += 1;
                    getClient().announce(EffectPacket.showForeignEffect(35));
                    dropTopMessage("Your Skill has Leveled Up to " + skil.getLevel());
                }
            }
        }
    }

    public void gainSkillLevel(Skill skill, int gain) { // needa
        SkillEntry skil = skills.get(skill);
        if (skil != null && (skil.getLevel() + gain) <= 999) {
            skil.exp = 0;
            skil.level += gain;
            getClient().announce(EffectPacket.showForeignEffect(35));
            dropTopMessage("Your Skill has Leveled Up to " + skil.getLevel());
        }
    }

    public void changeSingleSkillLevel(final Skill skill, int newLevel, int newMasterlevel, long expiration) {
        final Map<Skill, SkillEntry> list = new HashMap<>();
        boolean hasRecovery = false, recalculate = false;
        if (changeSkillData(skill, newLevel, newMasterlevel, expiration)) { // no loop, only 1
            if (!checkSkill(skill)) {
                list.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            } else {
                skills.get(skill).skillevel = newLevel;
                list.put(skill, skills.get(skill));
            }
            if (GameConstants.isRecoveryIncSkill(skill.getId())) {
                hasRecovery = true;
            }
            if (skill.getId() < 80000000) {
                recalculate = true;
            }
        }
        if (list.isEmpty()) { // nothing is changed
            return;
        }
        client.announce(CWvsContext.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    public void changeSkillsLevel(final Map<Skill, SkillEntry> ss) {
        if (ss.isEmpty()) {
            return;
        }
        final Map<Skill, SkillEntry> list = new HashMap<>();
        boolean hasRecovery = false, recalculate = false;
        for (final Entry<Skill, SkillEntry> data : ss.entrySet()) {
            if (changeSkillData(data.getKey(), data.getValue().skillevel, data.getValue().masterlevel, data.getValue().expiration)) {
                list.put(data.getKey(), data.getValue());
                if (GameConstants.isRecoveryIncSkill(data.getKey().getId())) {
                    hasRecovery = true;
                }
                if (data.getKey().getId() < 80000000) {
                    recalculate = true;
                }
            }
        }
        if (list.isEmpty()) { // nothing is changed
            return;
        }
        client.announce(CWvsContext.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    private void reUpdateStat(boolean hasRecovery, boolean recalculate) {
        changed_skills = true;
        if (hasRecovery) {
            stats.relocHeal(this);
        }
        if (recalculate) {
            stats.recalcLocalStats(this);
        }
    }

    public boolean changeSkillData(final Skill skill, int newLevel, int newMasterlevel, long expiration) {
        if (skill == null || (!GameConstants.isApplicableSkill(skill.getId()) && !GameConstants.isApplicableSkill_(skill.getId()))) {
            return false;
        }
        if (skills.containsKey(skill)) {
            skills.get(skill).skillevel = newLevel;
            skills.get(skill).masterlevel = newMasterlevel;
        } else {
            skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
        }
        return true;
    }

    public void updateSkills(int job) {
        final Map<Skill, SkillEntry> ss = new HashMap<>();

        if (GameConstants.isAdventurer(job)) {
            if (!checkSkill(SkillFactory.getSkill(190))) {
                ss.put(SkillFactory.getSkill(190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
            if (!checkSkill(SkillFactory.getSkill(3101003))) {
                ss.put(SkillFactory.getSkill(3101003), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isEvan(job)) {
            if (!checkSkill(SkillFactory.getSkill(20010190))) {
                ss.put(SkillFactory.getSkill(20010190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isMercedes(job)) {
            if (!checkSkill(SkillFactory.getSkill(20020190))) {
                ss.put(SkillFactory.getSkill(20020190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isResist(job)) {
            if (!checkSkill(SkillFactory.getSkill(30000190))) {
                ss.put(SkillFactory.getSkill(30000190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (GameConstants.isDemon(job)) {
            if (!checkSkill(SkillFactory.getSkill(30010190))) {
                ss.put(SkillFactory.getSkill(30010190), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        if (!ss.isEmpty()) {
            changeSkillLevel_Skip(ss, false);
            client.announce(CWvsContext.updateSkills(ss));
        }
    }

    public void changeSkillLevel_Skip(final Map<Skill, SkillEntry> skill, final boolean write) { // only used for
        // temporary skills
        // (not saved into
        // db)
        if (skill.isEmpty()) {
            return;
        }
        final Map<Skill, SkillEntry> newL = new HashMap<>();
        for (final Entry<Skill, SkillEntry> z : skill.entrySet()) {
            if (z.getKey() == null) {
                continue;
            }
            newL.put(z.getKey(), z.getValue());
            if (z.getValue().skillevel == 0 && z.getValue().masterlevel == 0) {
                if (skills.containsKey(z.getKey())) {
                    skills.remove(z.getKey());
                }
            } else {
                skills.put(z.getKey(), z.getValue());
            }
        }
        if (write && !newL.isEmpty()) {
            client.announce(CWvsContext.updateSkills(newL));
        }
    }

    public void playerDead() {
        final MapleStatEffect statss = getStatForBuff(MapleBuffStat.SOUL_STONE);
        if (statss != null) {
            dropMessage(5, "You have been revived by Soul Stone.");
            getStat().setHp(((getStat().getMaxHp() / 100) * statss.getX()), this);
            setStance(0);
            changeMap(getMap(), getMap().getPortal(0));
            return;
        }
        final MapleStatEffect energy = getStatForBuff(MapleBuffStat.ENERGY_CHARGE);
        if (energy != null) {
            setBuffedValue(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(0));
        }

        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
        cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        cancelEffectFromBuffStat(MapleBuffStat.SOARING);
        cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
        cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
        cancelEffectFromBuffStat(MapleBuffStat.HP_BOOST_PERCENT);
        cancelEffectFromBuffStat(MapleBuffStat.MP_BOOST_PERCENT);
        cancelEffectFromBuffStat(MapleBuffStat.HP_BOOST);
        cancelEffectFromBuffStat(MapleBuffStat.MP_BOOST);
        cancelEffectFromBuffStat(MapleBuffStat.ENHANCED_MAXHP);
        cancelEffectFromBuffStat(MapleBuffStat.ENHANCED_MAXMP);
        cancelEffectFromBuffStat(MapleBuffStat.MAXHP);
        cancelEffectFromBuffStat(MapleBuffStat.MAXMP);
        cancelEffectFromBuffStat(MapleBuffStat.ENERGY_CHARGE);
        dispelSummons();
        checkFollow();
        dotHP = 0;
        lastDOTTime = 0;
        if (pyramidSubway != null) {
            stats.setHp((short) 50, this);
            pyramidSubway.fail(this);
        }
    }

    public void updatePartyMemberHP() {
        if (party != null && client.getChannelServer() != null) {
            final int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar != null && partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    final MapleCharacter other = client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.getClient().announce(CField.updatePartyMemberHP(getId(), stats.getHp(), stats.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void receivePartyMemberHP() {
        if (party == null) {
            return;
        }
        int channel = client.getChannel();
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar != null && partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                MapleCharacter other = client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                if (other != null) {
                    client.announce(CField.updatePartyMemberHP(other.getId(), other.getStat().getHp(), other.getStat().getCurrentMaxHp()));
                }
            }
        }
    }

    public void healHP(int delta) {
        addHP(delta);
        client.announce(EffectPacket.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, EffectPacket.showHpHealed(getId(), delta), false);
    }

    public void healMP(int delta) {
        addMP(delta);
    }

    public void healAll(int delta) {
        addHP(delta);
        addMP(delta);
        client.announce(EffectPacket.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, EffectPacket.showHpHealed(getId(), delta), false);
    }

    /**
     * Convenience function which adds the supplied parameter to the current hp
     * then directly does a updateSingleStat.
     *
     * @see MapleCharacter#setHp(int)
     * @param delta
     */
    public void addHP(int delta) {
        if (stats.setHp(stats.getHp() + delta, this)) {
            updateSingleStat(MapleStat.HP, stats.getHp());
        }
    }

    /**
     * Convenience function which adds the supplied parameter to the current mp
     * then directly does a updateSingleStat.
     *
     * @see MapleCharacter#setMp(int)
     * @param delta
     */
    public void addMP(int delta) {
        addMP(delta, false);
    }

    public void addMP(int delta, boolean ignore) {
        if ((delta < 0 && GameConstants.isDemon(getJob())) || !GameConstants.isDemon(getJob()) || ignore) {
            if (stats.setMp(stats.getMp() + delta, this)) {
                updateSingleStat(MapleStat.MP, stats.getMp());
            }
        }
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        Map<MapleStat, Integer> statups = new EnumMap<MapleStat, Integer>(MapleStat.class);
        if (stats.setHp(stats.getHp() + hpDiff, this)) {
            updateSingleStat(MapleStat.HP, stats.getHp());

        }
        if (!GameConstants.isDemon(getJob())) {
            if (stats.setMp(stats.getMp() + mpDiff, this)) {
                updateSingleStat(MapleStat.MP, stats.getMp());
            }
        }
        getMap().updatePlayerStats();
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }

    /**
     * Updates a single stat of this MapleCharacter for the client. This method
     * only creates and sends an update packet, it does not update the stat
     * stored in this MapleCharacter instance.
     *
     * @param stat
     * @param newval
     * @param itemReaction
     */
    public void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(stat, newval);
        client.announce(CWvsContext.updatePlayerStats(statup, itemReaction, this));
    }

    public void gainExp(final long total, final boolean show, final boolean inChat, final boolean white) {
        try {
            if (isAlive()) {
                gainExpInternal(BigInteger.valueOf(total), 0, 0, show, inChat, white);
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e); // all jobs throw errors :(
        }
    }

    public void gainExpMonster(BigInteger gain, final boolean show, final boolean white, final byte pty, int Class_Bonus_EXP, int Equipment_Bonus_EXP, int Premium_Bonus_EXP, boolean partyBonusMob, final int partyBonusRate) {
        if (getJob() == 900) {
            return;
        }
        long expGain = Class_Bonus_EXP + Equipment_Bonus_EXP + Premium_Bonus_EXP;
        gainExpInternal(gain.add(BigInteger.valueOf(expGain)), 0, 0, false, false, white);
    }

    public void getExpLevel() {
        int total = totallevel;
        double rb = (double) getReborns();

        if (total < 200) {
            getXpLvl = BigDecimal.valueOf(getNeededExp());
            return;
        }
        if (rb > 0) {
            getXpLvl = BigDecimal.valueOf(Math.pow(total, 4.0 + ((double) totallevel * 0.0001))).multiply(BigDecimal.valueOf(Math.pow(getReborns(), 2.0)));
        } else {
            getXpLvl = BigDecimal.valueOf(Math.pow(total, 4.0 + ((double) totallevel * 0.0001)));
        }
    }

    public double getExpGrowth() {
        return 4.25 + ((double) totallevel * 0.00025);
    }

    public int getJobBranch() {
        return GameConstants.getJobBranch(getJob());
    }

    public long getDamageCap() {
        return 9000000000000000000L;
    }

    public void gainTier(int value) {
        tier += value;
    }

    public void setTier(int value) {
        tier = value;
    }

    public int getTier() {
        return tier;
    }

    public int getMaxLevel() {
        return getTotalMaxLevel();   // 4th job: cygnus is 120, rest is 200
    }

    public int getTotalMaxLevel() {
        /*
        if (isGM()) {
            return 9999;
        }
        int max = 250;
        if (achievementFinished(17)) {//pb->lotus 2
            max = 1000;
        }
        if (achievementFinished(65)) {//lucid->c-will 3
            max = 2500;
        }
        if (achievementFinished(181)) {//maru->kobold 4
            max = 5000;
        }
        if (achievementFinished(422)) {//angel slime raid boss
            max = 6000;
        }
        if (achievementFinished(425)) {//kalos raid boss
            max = 7000;
        }
        if (achievementFinished(435)) {//kaling raid boss
            max = 8000;
        }
        if (achievementFinished(438)) {//eLotus
            max = 9000;
        }
        if (achievementFinished(440)) {//Limbo
            max = 9999;
        }
         */
        return 9999;
    }

    public int getBaseTier() {
        int max = 1;
        if (achievementFinished(15)) {
            max = 5;
        }
        if (achievementFinished(17)) {
            max = 10;
        }
        if (achievementFinished(51)) {
            max = 15;
        }
        if (achievementFinished(39)) {
            max = 20;
        }
        if (achievementFinished(61)) {
            max = 25;
        }
        if (achievementFinished(70)) {
            max = 30;
        }
        if (achievementFinished(66)) {
            max = 35;
        }
        if (achievementFinished(65)) {
            max = 40;
        }
        if (achievementFinished(64)) {
            max = 45;
        }
        if (achievementFinished(50)) {
            max = 50;
        }
        if (achievementFinished(69)) {
            max = 60;
        }
        if (achievementFinished(181)) {
            max = 70;
        }
        if (achievementFinished(405)) {
            max = 75;
        }
        if (achievementFinished(408)) {
            max = 80;
        }
        return max;
    }

    public double getItemPower() {
        return Math.pow(getStat().getBaseItempower() + Math.pow(getTotalBaseMastery(), 1.1), 1.2) * 0.01;
    }

    public void gainExpInternal(BigInteger gain, int equip, long party, boolean show, boolean inChat, boolean white) { // need

        boolean levelchange = false;
        if (totallevel >= 200) {
            if (totallevel > getMaxLevel()) {
                gain.clearBit(gain.bitLength());
                return;
            }
            BigInteger total = gain.add(BigInteger.valueOf(equip + party));
            if (total.compareTo(BigInteger.ZERO) > 0) {
                if (show) {
                    if (total.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
                        dropMidMessage("+" + StringUtil.getUnitBigNumber(total) + " Exp");
                        //System.out.println("total: " + total.toString());
                    } else {
                        client.announce(InfoPacket.GainEXP_Others(total.intValue(), inChat, white));
                    }
                }
                overexp = overexp.add(total);
                if (overexp.compareTo(getXpLvl.toBigInteger()) >= 0) {
                    levelUp();
                    levelchange = true;
                }
                double temp = new BigDecimal(overexp).divide(getXpLvl, 4, RoundingMode.HALF_UP).movePointRight(2).doubleValue();
                int exppercent = (int) (temp * 100);
                if (exppercent >= 10000) {
                    exppercent = 9999;
                }
                exp.set(exppercent);
                updateSingleStat(MapleStat.EXP, exppercent);
            }
            total.clearBit(total.bitLength());
        } else {
            if (level < getMaxLevel()) {
                long total = gain.longValue() + equip + party;
                if (total > 0) {
                    if (getMapId() == 5001 && GameConstants.isBeginnerJob(getJob())) {
                        total = level * 5;
                    }
                    long nextExp = exp.get() + total;
                    if (show) {
                        client.announce(InfoPacket.GainEXP_Others((int) (total > Integer.MAX_VALUE ? Integer.MAX_VALUE : total), inChat, white));
                        if (total > Integer.MAX_VALUE) {
                            dropMidMessage("+" + NumberFormat.getInstance().format(total) + " Exp");
                        }
                    }
                    if (nextExp >= getXpLvl.longValue()) {
                        levelUp();
                        levelchange = true;
                    } else {
                        exp.set((int) nextExp);
                    }
                    updateSingleStat(MapleStat.EXP, exp.get());
                }
            } else {
                updateSingleStat(MapleStat.EXP, 0);
            }
        }
        gain.clearBit(gain.bitLength());
        if (levelchange) {
            if (totallevel > 250 && showLevel) {
                getClient().announce(EffectPacket.showForeignEffect(0));
            }
            updateChar();
        }
    }

    public void familyRep(int prevexp, int needed, boolean leveled) {
        if (mfc != null) {
            int onepercent = needed / 100;
            if (onepercent <= 0) {
                return;
            }
            int percentrep = (getExp() / onepercent - prevexp / onepercent);
            if (leveled) {
                percentrep = 100 - percentrep + (level / 2);
            }
            if (percentrep > 0) {
                int sensen = World.Family.setRep(mfc.getFamilyId(), mfc.getSeniorId(), percentrep * 10, level, name);
                if (sensen > 0) {
                    World.Family.setRep(mfc.getFamilyId(), sensen, percentrep * 5, level, name); // and we stop here
                }
            }
        }
    }

    public void forceReAddItem_NoUpdate(Item item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addItemFromDB(item);
    }

    public void forceReAddItem(Item item, MapleInventoryType type) { // used for stuff like durability, item exp/level,
        // probably owner?
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            client.announce(InventoryPacket.updateSpecialItemUse(item, type == MapleInventoryType.EQUIPPED ? (byte) 1 : type.getType(), this));
        }
    }

    public void forceReAddItem_Flag(Item item, MapleInventoryType type) { // used for flags
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            client.announce(InventoryPacket.updateSpecialItemUse_(item, type == MapleInventoryType.EQUIPPED ? (byte) 1 : type.getType(), this));
        }
    }

    public void forceReAddItem_Book(Item item, MapleInventoryType type) { // used for mbook
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            client.announce(CWvsContext.upgradeBook(item, this));
        }
    }

    public boolean isPartyLeader() {
        prtLock.lock();
        try {
            return party.getLeaderId() == getId();
        } finally {
            prtLock.unlock();
        }
    }

    public void silentPartyUpdate() {
        if (party != null) {
            World.Party.updateParty(party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
        }
    }

    public boolean isSuperGM() {
        return false;
    }

    public boolean isIntern() {
        return false;
    }

    public boolean isGM() {
        return gmLevel >= PlayerGMRank.GM.getLevel();
    }

    public boolean isGMJob() {
        return GameConstants.isGMJob(job);
    }

    public boolean isAdmin() {
        return gmLevel >= PlayerGMRank.ADMIN.getLevel();
    }

    public int getGMLevel() {
        return gmLevel;
    }

    public boolean hasGmLevel(int level) {
        return gmLevel >= level;
    }

    public final MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public final MapleInventory[] getInventorys() {
        return inventory;
    }

    public final void expirationTask(boolean pending, boolean firstLoad) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (pending) {
            if (pendingExpiration != null) {
                for (Integer z : pendingExpiration) {
                    client.announce(InfoPacket.itemExpired(z.intValue()));
                    if (!firstLoad) {
                        final Pair<Integer, String> replace = ii.replaceItemInfo(z.intValue());
                        if (replace != null && replace.left > 0 && replace.right.length() > 0) {
                            dropMessage(5, replace.right);
                        }
                    }
                }
            }
            pendingExpiration = null;
            if (pendingSkills != null) {
                client.announce(CWvsContext.updateSkills(pendingSkills));
                for (Skill z : pendingSkills.keySet()) {
                    client.announce(CWvsContext.serverNotice(5, "[" + SkillFactory.getSkillName(z.getId())
                            + "] skill has expired and will not be available for use."));
                }
            } // not real msg
            pendingSkills = null;
            return;
        }
        final MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
        long expiration;
        final List<Integer> ret = new ArrayList<Integer>();
        final long currenttime = System.currentTimeMillis();
        final List<Triple<MapleInventoryType, Item, Boolean>> toberemove = new ArrayList<Triple<MapleInventoryType, Item, Boolean>>(); // This
        // is
        // here
        // to
        // prevent
        // deadlock.
        final List<Item> tobeunlock = new ArrayList<Item>(); // This is here to prevent deadlock.

        for (final MapleInventoryType inv : MapleInventoryType.values()) {
            for (final Item item : getInventory(inv)) {
                if (item.getItemId() == 2450024 || item.getItemId() == 2450025 || item.getItemId() == 2450050 || item.getItemId() == 2450051 || item.getItemId() == 2450052 || item.getItemId() == 2450053) {
                    continue;
                }
                expiration = item.getExpiration();
                if ((expiration != -1 && !GameConstants.isPet(item.getItemId()) && currenttime > expiration) || (firstLoad && ii.isLogoutExpire(item.getItemId()))) {
                    if (ItemFlag.LOCK.check(item.getFlag())) {
                        tobeunlock.add(item);
                    } else if (currenttime > expiration) {
                        toberemove.add(new Triple<MapleInventoryType, Item, Boolean>(inv, item, false));
                    }
                } else if (item.getItemId() == 5000054 && item.getPet() != null && item.getPet().getSecondsLeft() <= 0) {
                    toberemove.add(new Triple<MapleInventoryType, Item, Boolean>(inv, item, false));
                } else if (item.getPosition() == -59) {
                    if (stat == null || stat.getCustomData() == null || Long.parseLong(stat.getCustomData()) < currenttime) {
                        toberemove.add(new Triple<MapleInventoryType, Item, Boolean>(inv, item, true));
                    }
                }
            }
        }
        Item item;
        for (final Triple<MapleInventoryType, Item, Boolean> itemz : toberemove) {
            item = itemz.getMid();
            getInventory(itemz.getLeft()).removeItem(item.getPosition(), item.getQuantity(), false);
            if (itemz.getRight() && getInventory(GameConstants.getInventoryType(item.getItemId())).getNextFreeSlot() > -1) {
                item.setPosition(getInventory(GameConstants.getInventoryType(item.getItemId())).getNextFreeSlot());
                getInventory(GameConstants.getInventoryType(item.getItemId())).addFromDB(item);
            } else {
                ret.add(item.getItemId());
            }
            if (!firstLoad) {
                final Pair<Integer, String> replace = ii.replaceItemInfo(item.getItemId());
                if (replace != null && replace.left > 0) {
                    Item theNewItem = null;
                    if (GameConstants.getInventoryType(replace.left) == MapleInventoryType.EQUIP) {
                        theNewItem = ii.getEquipById(replace.left);
                        theNewItem.setPosition(item.getPosition());
                    } else {
                        theNewItem = new Item(replace.left, item.getPosition(), (short) 1, (byte) 0);
                    }
                    getInventory(itemz.getLeft()).addFromDB(theNewItem);
                }
            }
        }
        for (final Item itemz : tobeunlock) {
            itemz.setExpiration(-1);
            itemz.setFlag((byte) (itemz.getFlag() - ItemFlag.LOCK.getValue()));
        }
        this.pendingExpiration = ret;

        final Map<Skill, SkillEntry> skilz = new HashMap<>();
        final List<Skill> toberem = new ArrayList<Skill>();
        for (Entry<Skill, SkillEntry> skil : skills.entrySet()) {
            if (skil.getValue().expiration != -1 && currenttime > skil.getValue().expiration) {
                toberem.add(skil.getKey());
            }
        }
        for (Skill skil : toberem) {
            skilz.put(skil, new SkillEntry(0, (byte) 0, -1));
            this.skills.remove(skil);
            changed_skills = true;
        }
        this.pendingSkills = skilz;
    }

    public MapleShop getShop() {
        return shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public int getMeso() {
        return meso;
    }

    public final int[] getSavedLocations() {
        return savedLocations;
    }

    public int getSavedLocation(SavedLocationType type) {
        return savedLocations[type.getValue()];
    }

    public void saveLocation(String str) {
        saveLocation(SavedLocationType.fromString(str));
    }

    public void saveLocation(SavedLocationType type) {
        savedLocations[type.getValue()] = getMapId();
        changed_savedlocations = true;
    }

    public void saveLocation(SavedLocationType type, int mapz) {
        savedLocations[type.getValue()] = mapz;
        changed_savedlocations = true;
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.getValue()] = -1;
        changed_savedlocations = true;
    }

    public boolean canHoldMeso(long gain) { // thanks lucasziron found pointing out a need to check space availability
        // for mesos on player transactions
        long nextMeso = (long) meso + gain;
        return nextMeso <= Integer.MAX_VALUE;
    }

    public boolean canGainMeso(long gain) { // thanks lucasziron found pointing out a need to check space availability
        // for mesos on player transactions
        long nextMeso = (long) meso + gain;
        return nextMeso <= Integer.MAX_VALUE;
    }

    public void gainMeso(long gain) {
        gainMeso(gain, false, true);
    }

    public void gainMeso(long gain, boolean show) {
        gainMeso(gain, show, false);
    }

    public void gainMeso(long gain, boolean show, boolean inChat) {
        if (meso + gain > Integer.MAX_VALUE) {
            bank += gain;
            if (show) {
                dropTopMessage("Banked " + gain + " Mesos.");
            }
            client.announce(CWvsContext.enableActions());
            return;
        }
        if (gain == 0) {
            client.announce(CWvsContext.enableActions());
        } else {
            if (gain > 0 && !canHoldMeso(gain)) {
                bank += gain;
                client.announce(CWvsContext.enableActions());
            } else {
                if (meso + gain < 0) {
                    System.out.println(getName() + " trying to obtained -" + gain + " mesos!");
                    client.announce(CWvsContext.enableActions());
                    return;
                }
                meso += gain;
                if (meso >= 1000000) {
                    finishAchievement(31);
                }
                if (meso >= 10000000) {
                    finishAchievement(32);
                }
                if (meso >= 100000000) {
                    finishAchievement(33);
                }
                if (meso >= 1000000000) {
                    finishAchievement(34);
                }

                updateSingleStat(MapleStat.MESO, meso, false);
                if (show) {
                    client.announce(InfoPacket.showMesoGain((int) gain, inChat));
                }
                client.announce(CWvsContext.enableActions());
            }
        }
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        if (clone || monster == null || !monster.isAlive()) {
            return;
        }
        monster.setController(this);
        controlledLock.writeLock().lock();
        try {
            controlled.add(monster);
        } finally {
            controlledLock.writeLock().unlock();
        }
        client.announce(MobPacket.controlMonster(monster, false, aggro));
        monster.setControllerHasAggro(aggro);
    }

    public void stopControllingMonster(MapleMonster monster) {
        if (clone || monster == null) {
            return;
        }
        monster.setController(null);
        controlledLock.writeLock().lock();
        try {
            if (controlled.contains(monster)) {
                controlled.remove(monster);
            }
        } finally {
            controlledLock.writeLock().unlock();
        }
        client.announce(MobPacket.stopControllingMonster(monster));
        monster.setControllerHasAggro(false);
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (clone || monster == null) {
            return;
        }
        if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
        } else {
            monster.updateMonsterController();
        }
    }

    public int getControlledSize() {
        return controlled.size();
    }

    public int getAccountID() {
        return accountid;
    }

    public void mobKilled(final int id, final int skillID) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() != 1 || !q.hasMobKills()) {
                continue;
            }
            if (q.mobKilled(id, skillID)) {
                client.announce(InfoPacket.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null)) {
                    client.announce(CWvsContext.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 1 && !q.isCustom() && !q.getQuest().isBlocked()) {
                ret.add(q);
            }
        }
        return ret;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2 && !q.isCustom() && !q.getQuest().isBlocked()) {
                ret.add(q);
            }
        }
        return ret;
    }

    public final List<Pair<Integer, Long>> getCompletedMedals() {
        List<Pair<Integer, Long>> ret = new ArrayList<Pair<Integer, Long>>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == 2 && !q.isCustom() && !q.getQuest().isBlocked() && q.getQuest().getMedalItem() > 0
                    && GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP) {
                ret.add(new Pair<Integer, Long>(q.getQuest().getId(), q.getCompletionTime()));
            }
        }
        return ret;
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public List<Skill> getSkillz() {
        List<Skill> newSkillz = new ArrayList<>();
        for (Skill skill : getSkills().keySet()) {
            if (skill.getBaseDamage() > 0) {
                newSkillz.add(skill);
            }
        }
        newSkillz.sort((t, o) -> Integer.compare(t.getId(), o.getId()));
        return newSkillz;
    }

    public SkillEntry getSkillEntry(Skill skill) {
        if (skills.containsKey(skill)) {
            return skills.get(skill);
        }
        return null;
    }

    public String getSkillName(Skill skill) {
        return skill.getName();
    }

    public int getTotalSkillLevel(final Skill skill) {
        if (skill == null) {
            return 0;
        }
        final SkillEntry ret = skills.get(skill);
        if (ret == null || ret.skillevel <= 0) {
            return 0;
        }
        int skilllevel = Math.min(skill.getTrueMax(), ret.skillevel + (skill.isBeginnerSkill() ? 0 : (stats.combatOrders + (skill.getMaxLevel() > 10 ? stats.incAllskill : 0) + stats.getSkillIncrement(skill.getId()))));
        // updateSkills(getJob());
        return skilllevel;
    }

    public int getAllSkillLevels() {
        int rett = 0;
        for (Entry<Skill, SkillEntry> ret : skills.entrySet()) {
            if (!ret.getKey().isBeginnerSkill() && !ret.getKey().isSpecialSkill() && ret.getValue().skillevel > 0) {
                rett += ret.getValue().skillevel;
            }
        }
        return rett;
    }

    public List<Skill> getAllSkills() {
        List<Skill> ret = new LinkedList<Skill>();
        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            if (skill.getValue().skillevel > 0) {
                ret.add(skill.getKey());
            }
        }
        return ret;
    }

    public long getSkillExpiry(final Skill skill) {
        if (skill == null) {
            return 0;
        }
        final SkillEntry ret = skills.get(skill);
        if (ret == null || ret.skillevel <= 0) {
            return 0;
        }
        return ret.expiration;
    }

    public boolean checkSkill(final Skill skill) {
        return skills.containsKey(skill);
    }

    public int getSkillLevel(final Skill skill) {
        if (skill == null) {
            return 0;
        }
        final SkillEntry ret = skills.get(skill);
        if (ret == null || ret.skillevel <= 0) {
            return 0;
        }
        return ret.skillevel;
    }

    public int getSkillDamageBoost(final Skill skillz) {
        Skill skill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(skillz.getId()));
        if (skills.containsKey(skill)) {
            SkillEntry skil = skills.get(skill);
            return (skil.skillevel + skil.level) * skill.getScale();
        }
        return 0;
    }

    public int getMasterLevel(final int skill) {
        return getMasterLevel(SkillFactory.getSkill(skill));
    }

    public int getMasterLevel(final Skill skill) {
        final SkillEntry ret = skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public void updateAP() {
        // idk how i fucked this up when removing Auto Assign, but meh.
        updateSingleStat(MapleStat.STR, client.getPlayer().getStat().getStr());
        updateSingleStat(MapleStat.DEX, client.getPlayer().getStat().getDex());
        updateSingleStat(MapleStat.INT, client.getPlayer().getStat().getInt());
        updateSingleStat(MapleStat.LUK, client.getPlayer().getStat().getLuk());

    }

    public void setLevel(int lvl) {
        totallevel = (lvl);
        level = 10;
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(MapleStat.LEVEL, (int) level);
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        stats.heal(this);
        getClient().announce(EffectPacket.showForeignEffect(35));
        silentPartyUpdate();
        guildUpdate();
        familyUpdate();
    }

    public void addLevels(int lvl) {
        totallevel += (lvl);
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(MapleStat.LEVEL, (int) level);
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        stats.heal(this);
        map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        getClient().announce(EffectPacket.showForeignEffect(35));
        silentPartyUpdate();
        guildUpdate();
        familyUpdate();
    }

    public void addLevels(int lvl, int cap) {
        int o = totallevel;
        totallevel = Randomizer.Max(totallevel + lvl, cap);
        int n = totallevel;
        if (o != n) {
            final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
            statup.put(MapleStat.LEVEL, (int) level);
            client.announce(CWvsContext.updatePlayerStats(statup, this));
            stats.recalcLocalStats(this);
            stats.heal(this);
            map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
            getClient().announce(EffectPacket.showForeignEffect(35));
            silentPartyUpdate();
            guildUpdate();
            familyUpdate();
        }
    }

    public void addLevel(int lvl) {
        totallevel += (lvl);
        remainingAp += (level * 5);
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class
        );
        int maxhp = stats.getMaxHp();
        int maxmp = stats.getMaxMp();
        for (int i = 0; i < lvl; i++) {
            maxhp += (i * 100);
            maxmp += (i * 100);
        }

        if (GameConstants.isDemon(job)) {
            maxmp = GameConstants.getMPByJob(job);
        }

        statup.put(MapleStat.MAXHP, maxhp);
        statup.put(MapleStat.HP, maxhp);
        statup.put(MapleStat.MAXMP, maxmp);
        statup.put(MapleStat.MP, maxmp);
        statup.put(MapleStat.LEVEL, (int) level);
        statup.put(MapleStat.AVAILABLEAP, (int) remainingAp);
        stats.setInfo(maxhp, maxmp, maxhp, maxmp);
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        silentPartyUpdate();
        guildUpdate();
        familyUpdate();
    }

    public void skillBoost() {
        addRemainingSp(5);
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(MapleStat.AVAILABLESP, 0);
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        getClient().announce(EffectPacket.showForeignEffect(35));
    }

    public void skillBoost(int value) {
        addRemainingSp(5 * value);
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(MapleStat.AVAILABLESP, 0);
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        getClient().announce(EffectPacket.showForeignEffect(35));
    }

    public void miniLevelUp() {
        int maxhp = stats.getMaxHp();
        int maxmp = stats.getMaxMp();
        if (maxhp < GameConstants.getMaxHpMp()) {
            maxhp += 100;
        }
        if (!GameConstants.isDemon(job) && maxmp < GameConstants.getMaxHpMp()) {
            maxmp += 100;
        }
        addRemainingAp(3);
        addRemainingSp(1);
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        maxhp = Randomizer.Max(maxhp, GameConstants.getMaxHpMp());
        maxmp = Randomizer.Max(maxmp, GameConstants.getMaxHpMp());
        statup.put(MapleStat.MAXHP, maxhp);
        statup.put(MapleStat.MAXMP, maxmp);
        statup.put(MapleStat.HP, maxhp);
        statup.put(MapleStat.MP, maxmp);
        statup.put(MapleStat.AVAILABLEAP, (int) remainingAp);
        statup.put(MapleStat.AVAILABLESP, 0);
        stats.setInfo(maxhp, maxmp, maxhp, maxmp);
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        stats.heal(this);
        map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        getClient().announce(EffectPacket.showForeignEffect(35));
    }

    public void miniLevelUpNoSP() {

    }

    public void miniLevelUp(int scale) {
        int maxhp = stats.getMaxHp();
        int maxmp = stats.getMaxMp();
        if (maxhp < GameConstants.getMaxHpMp()) {
            maxhp += (100 * scale);
        }
        if (!GameConstants.isDemon(job) && maxmp < GameConstants.getMaxHpMp()) {
            maxmp += (100 * scale);
        }
        addRemainingAp(3 * scale);
        addRemainingSp(scale);

        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        maxhp = Randomizer.Max(maxhp, GameConstants.getMaxHpMp());
        maxmp = Randomizer.Max(maxmp, GameConstants.getMaxHpMp());
        statup.put(MapleStat.MAXHP, maxhp);
        statup.put(MapleStat.MAXMP, maxmp);
        statup.put(MapleStat.HP, maxhp);
        statup.put(MapleStat.MP, maxmp);
        statup.put(MapleStat.AVAILABLEAP, (int) remainingAp);
        statup.put(MapleStat.AVAILABLESP, 0);
        stats.setInfo(maxhp, maxmp, maxhp, maxmp);

        client.announce(CWvsContext.updatePlayerStats(statup, this));
        stats.recalcLocalStats(this);
        stats.heal(this);
        map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        getClient().announce(EffectPacket.showForeignEffect(35));
    }

    public void resetLevelOffline() {
        overexp = BigInteger.ZERO;
        level = 10;
        exp.set(0);
        totallevel = 10;
    }

    public void resetLevel() {
        overexp = BigInteger.ZERO;
        exp.set(0);
        setLevel(10);
        client.announce(CField.customMainStatUpdate(this));
    }

    public void levelUp() {
        levelUp(true);
    }

    public void levelUp(boolean save) {
        if (totallevel >= getMaxLevel()) {
            return;
        }
        overexp = BigInteger.ZERO;
        exp.set(0);
        if (level < 250) {
            level++;
        }
        if (totallevel < getMaxLevel()) {
            addAccVar("Rank", 1);
            totallevel += 1;
            //this.yellowMessage("Congrats!! You are now Level " + totallevel);
        } else {
            gainDamageSkinNoOrb(6000);
            this.yellowMessage("Congrats!! You are now Max Level " + totallevel + " - New Ultimate skin is now Unlocked!");
        }

        if (getGuild() != null) {
            gainGP(totallevel, false);
        }
        if (totallevel >= 250) {
            if (GameConstants.isWarriorJob(job)) {
                finishAchievement(600);
            }
            if (GameConstants.isMageJob(job)) {
                finishAchievement(601);
            }
            if (GameConstants.isBowmanJob(job)) {
                finishAchievement(602);
            }
            if (GameConstants.isThiefJob(job)) {
                finishAchievement(603);
            }
            if (GameConstants.isPirateJob(job)) {
                finishAchievement(604);
            }
            if (GameConstants.isGMJob(job)) {
                finishAchievement(605);
            }
        }
        switch (totallevel) {
            case 100:
                finishAchievement(850);
                break;
            case 250:
                finishAchievement(851);
                break;
            case 500:
                finishAchievement(852);
                break;
            case 750:
                finishAchievement(853);
                break;
            case 1000:
                finishAchievement(854);
                break;
            case 1500:
                finishAchievement(855);
                break;
            case 2000:
                finishAchievement(856);
                break;
            case 2500:
                finishAchievement(857);
                break;
            case 3000:
                finishAchievement(858);
                break;
            case 3500:
                finishAchievement(859);
                break;
            case 4000:
                finishAchievement(860);
                break;
            case 4500:
                finishAchievement(861);
                break;
            case 5000:
                finishAchievement(862);
                break;
            case 5500:
                finishAchievement(863);
                break;
            case 6000:
                finishAchievement(864);
                break;
            case 6500:
                finishAchievement(865);
                break;
            case 7000:
                finishAchievement(866);
                break;
            case 7500:
                finishAchievement(867);
                break;
            case 8000:
                finishAchievement(868);
                break;
            case 8500:
                finishAchievement(869);
                break;
            case 9000:
                finishAchievement(870);
                break;
            case 9500:
                finishAchievement(871);
                break;
            case 9999:
                World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "[Congratulations] " + getName() + " has achieved Level " + totallevel + ". Let us Celebrate Maplers!"));
                finishAchievement(872);
                break;
        }
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(MapleStat.EXP, exp.get());
        statup.put(MapleStat.LEVEL, (int) level);
        int maxhp = stats.getMaxHp() + 100;
        int maxmp = stats.getMaxMp() + 50;
        addRemainingAp(3);
        maxhp = Randomizer.Max(maxhp, GameConstants.getMaxHpMp());
        maxmp = Randomizer.Max(maxmp, GameConstants.getMaxHpMp());
        statup.put(MapleStat.MAXHP, maxhp);
        statup.put(MapleStat.MAXMP, maxmp);
        statup.put(MapleStat.HP, maxhp);
        statup.put(MapleStat.MP, maxmp);
        statup.put(MapleStat.AVAILABLEAP, (int) remainingAp);
        stats.setInfo(maxhp, maxmp, maxhp, maxmp);
        stats.heal(this);
        map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 0), false);
        client.announce(EffectPacket.showForeignEffect(0));
        client.announce(CWvsContext.updatePlayerStats(statup, this));
        client.announce(CField.customMainStatUpdate(this));
        stats.recalcLocalStats(this);
        guildUpdate();
        silentPartyUpdate();
    }

    public void updateChar() {
        if ((shouldUpdate + 1000) <= System.currentTimeMillis()) {
            guildUpdate();
            silentPartyUpdate();
            getExpLevel();
            shouldUpdate = System.currentTimeMillis();
        }
    }

    public void Heal() {
        this.stats.heal(this);
    }

    public void autoJob() {
        boolean save = false;
        if (GameConstants.isAdventurer(job)) {
            if (!GameConstants.isCannon(job) && !GameConstants.isDB(job)) {
                switch (totallevel) {
                    case 100:
                        client.announce(CField.startMapEffect("You have reached level " + totallevel + "! To sub-job advance, @job to job advance.", 5120000, true));
                        save = true;
                        break;
                    case 250:
                    case 500:
                        changeJob(job + 1); // automatic
                        yellowMessage("You have auto job advanced, check out your new skills!");
                        save = true;
                        break;
                }
            }
        }
        if (GameConstants.isEvan(job)) {
            switch (totallevel) {
                case 50:
                case 100:
                case 150:
                case 200:
                case 250:
                case 300:
                case 350:
                case 400:
                case 500:
                    if (job < 2218) {
                        changeJob(job == 2001 ? 2200 : (job == 2200 ? 2210 : (job + 1))); // automatic
                        yellowMessage("You have auto job advanced, check out your new skills!");
                        save = true;
                    }
                    break;
            }
        }
        if (totallevel == 100) {
            if (job == 501) {// cannon
                changeJob(530);
                yellowMessage("You have auto job advanced, check out your new skills!");
                save = true;
            }
            if (job == 430) {// db
                changeJob(431);
                yellowMessage("You have auto job advanced, check out your new skills!");
                save = true;
            }
            if (job == 3100 || job == 3200 || job == 3300 || job == 3500 || job == 2300 || job == 1100 || job == 1200 || job == 1300 || job == 1400 || job == 1500 || job == 2100) {
                changeJob(job + 10);
                yellowMessage("You have auto job advanced, check out your new skills!");
                save = true;
            }
        }
        if (totallevel == 200) {
            if (job == 431) {// db
                changeJob(job + 1);
                yellowMessage("You have auto job advanced, check out your new skills!");
                save = true;
            }
        }
        if (totallevel == 250) {// cannon/db
            if (job == 530 || job == 432 || job == 3110 || job == 3210 || job == 3310 || job == 3510 || job == 2310 || job == 1110 || job == 1210 || job == 1310 || job == 1410 || job == 1510 || job == 2110) {
                changeJob(job + 1);
                yellowMessage("You have auto job advanced, check out your new skills!");
                save = true;
            }
        }
        if (totallevel == 500) {// cannon/db
            if (job == 531 || job == 433 || job == 3111 || job == 3211 || job == 3311 || job == 3511 || job == 2311 || job == 1111 || job == 1211 || job == 1311 || job == 1411 || job == 1511 || job == 2111) {
                changeJob(job + 1);
                yellowMessage("You have auto job advanced, check out your new skills!");
                save = true;
            }
        }
        if (save) {
            saveToDB();
            stats.recalcLocalStats(this);
        }
    }

    public void sendMacros() {
        for (int i = 0; i < 5; i++) {
            if (skillMacros[i] != null) {
                client.announce(CField.getMacros(skillMacros));
                break;
            }
        }
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
        changed_skillmacros = true;
    }

    public final SkillMacro[] getMacros() {
        return skillMacros;
    }

    public void tempban(String reason, Calendar duration, int greason, boolean IPMac) {
        if (IPMac) {
            client.banMacs();
        }
        client.announce(CWvsContext.GMPoliceMessage(true));
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps;
            if (IPMac) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, client.getSession().getRemoteAddress().toString().split(":")[0]);
                ps.execute();
                ps.close();
            }

            client.getSession().close();

            ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("Error while tempbanning" + ex);
        }

    }

    public final boolean ban(String reason) {
        client.banMacs();
        client.kick();
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?")) {
            ps.setInt(1, 1);
            ps.setString(2, reason);
            ps.setInt(3, accountid);
            ps.execute();
        } catch (SQLException ex) {
            System.err.println("Error while banning" + ex);
            return false;
        }
        return true;
    }

    public static boolean ban(String id, String reason, boolean accountId, int gmlevel, boolean hellban) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps;
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.execute();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ? AND gm < ?");
                psb.setString(1, reason);
                psb.setInt(2, z);
                psb.setInt(3, gmlevel);
                psb.execute();
                psb.close();

                if (gmlevel > 100) { // admin ban
                    PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    psa.setInt(1, z);
                    ResultSet rsa = psa.executeQuery();
                    if (rsa.next()) {
                        String sessionIP = rsa.getString("sessionIP");
                        if (sessionIP != null && sessionIP.matches("/[0-9]{1,3}\\..*")) {
                            PreparedStatement psz = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                            psz.setString(1, sessionIP);
                            psz.execute();
                            psz.close();
                        }
                        if (rsa.getString("macs") != null) {
                            String[] macData = rsa.getString("macs").split(", ");
                            if (macData.length > 0) {
                                MapleClient.banMacs(macData);
                            }
                        }
                        if (hellban) {
                            PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE email = ?" + (sessionIP == null ? "" : " OR SessionIP = ?"));
                            pss.setString(1, reason);
                            pss.setString(2, rsa.getString("email"));
                            if (sessionIP != null) {
                                pss.setString(3, sessionIP);
                            }
                            pss.execute();
                            pss.close();
                        }
                    }
                    rsa.close();
                    psa.close();
                }
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            System.err.println("Error while banning" + ex);
        }
        return false;
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        if (clone) {
            return;
        }
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.add(mo);
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        if (clone) {
            return;
        }
        visibleMapObjectsLock.writeLock().lock();
        try {
            visibleMapObjects.remove(mo);
        } finally {
            visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        visibleMapObjectsLock.readLock().lock();
        try {
            return visibleMapObjects.contains(mo);
        } finally {
            visibleMapObjectsLock.readLock().unlock();
        }
    }

    public List<MapleMapObject> getVisibleMapObjects() {
        return visibleMapObjects;
    }

    public void lockWriteVisibleMapObjects() {
        visibleMapObjectsLock.writeLock().lock();
    }

    public void unlockWriteVisibleMapObjects() {
        visibleMapObjectsLock.writeLock().unlock();
    }

    public boolean isAlive() {
        return stats.getHp() > 0;
    }

    @Override
    public int getObjectId() {
        return getId();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(CField.removePlayerFromMap(this.getObjectId()));
        for (final WeakReference<MapleCharacter> chr : clones) {
            if (chr.get() != null) {
                chr.get().sendDestroyData(client);
            }
        }
        getMap().deleteVisibleObjects(this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (client.getPlayer() != null && client.getPlayer().allowedToTarget(this)) {
            client.announce(CField.spawnPlayerMapobject(this, false));

            for (final MaplePet pet : pets) {
                if (pet.getSummoned()) {
                    client.announce(PetPacket.showPet(this, pet, false, false));
                }
            }
            for (final WeakReference<MapleCharacter> chr : clones) {
                if (chr.get() != null) {
                    chr.get().sendSpawnData(client);
                }
            }
            getMap().addVisibleObjects(this);
            if (dragon != null) {
                client.announce(CField.spawnDragon(dragon));
            }
            if (android != null && !hideAndroid) {
                client.announce(CField.spawnAndroid(this, android, true));
            }
            if (summonedFamiliar != null) {
                client.announce(CField.spawnFamiliar(summonedFamiliar, true));
            }
            if (followid > 0 && followon) {
                client.announce(CField.followEffect(followinitiator ? followid : id, followinitiator ? id : followid, null));
            }
        }
    }

    public final void equipChanged() {
        if (map == null) {
            return;
        }
        map.broadcastMessage(this, CField.updateCharLook(this), false);
        stats.recalcLocalStats(this);
        if (getMessenger() != null) {
            World.Messenger.updateMessenger(getMessenger().getId(), getName(), client.getChannel());
        }
    }

    public final MaplePet getPet(final int index) {
        byte count = 0;
        if (!pets.isEmpty()) {
            for (final MaplePet pet : pets) {
                if (pet.getSummoned()) {
                    if (count == index) {
                        return pet;
                    }
                    count++;
                }
            }
        }
        return null;
    }

    public void removePetCS(MaplePet pet) {
        pets.remove(pet);
    }

    public void addPet(final MaplePet pet) {
        if (pets.contains(pet)) {
            pets.remove(pet);
        }
        pets.add(pet);
        // So that the pet will be at the last
        // Pet index logic :(
    }

    public void removePet(MaplePet pet, boolean shiftLeft) {
        pet.setSummoned(0);
        /*
         * int slot = -1; for (int i = 0; i < 3; i++) { if (pets[i] != null) { if
         * (pets[i].getUniqueId() == pet.getUniqueId()) { pets[i] = null; slot = i;
         * break; } } } if (shiftLeft) { if (slot > -1) { for (int i = slot; i < 3; i++)
         * { if (i != 2) { pets[i] = pets[i + 1]; } else { pets[i] = null; } } } }
         */
    }

    public final byte getPetIndex(final MaplePet petz) {
        byte count = 0;
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet.getUniqueId() == petz.getUniqueId()) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public final byte getPetIndex(final int petId) {
        byte count = 0;
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet.getUniqueId() == petId) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public final List<MaplePet> getSummonedPets() {
        List<MaplePet> ret = new ArrayList<MaplePet>();
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                ret.add(pet);
            }
        }
        return ret;
    }

    public final byte getPetById(final int petId) {
        byte count = 0;
        for (final MaplePet pet : pets) {
            if (pet.getSummoned()) {
                if (pet.getPetItemId() == petId) {
                    return count;
                }
                count++;
            }
        }
        return -1;
    }

    public final List<MaplePet> getPets() {
        return pets;
    }

    public final void unequipAllPets() {
        for (final MaplePet pet : pets) {
            if (pet != null) {
                unequipPet(pet, true, false);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shiftLeft, boolean hunger) {
        if (pet.getSummoned()) {
            pet.saveToDb();

            client.announce(PetPacket.updatePet(pet, getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), false));
            if (map != null) {
                map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
            }
            removePet(pet, shiftLeft);
            // List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat,
            // Integer>>();
            // stats.put(MapleStat.PET, Integer.valueOf(0)));
            // showpetupdate isn't done here...
            if (GameConstants.GMS) {
                client.announce(PetPacket.petStatUpdate(this));
            }
            client.announce(CWvsContext.enableActions());
        }
    }

    /*
     * public void shiftPetsRight() { if (pets[2] == null) { pets[2] = pets[1];
     * pets[1] = pets[0]; pets[0] = null; } }
     */
    public final long getLastFameTime() {
        return lastfametime;
    }

    public final List<Integer> getFamedCharacters() {
        return lastmonthfameids;
    }

    public final List<Integer> getBattledCharacters() {
        return lastmonthbattleids;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (lastfametime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
            return FameStatus.NOT_TODAY;
        } else if (from == null || lastmonthfameids == null
                || lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        }
        return FameStatus.OK;
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con
                    .prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("ERROR writing famelog for char " + getName() + " to " + to.getName() + e);
        }
    }

    public boolean canBattle(MapleCharacter to) {
        if (to == null || lastmonthbattleids == null
                || lastmonthbattleids.contains(Integer.valueOf(to.getAccountID()))) {
            return false;
        }
        return true;
    }

    public void hasBattled(MapleCharacter to) {
        lastmonthbattleids.add(Integer.valueOf(to.getAccountID()));
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO battlelog (accid, accid_to) VALUES (?, ?)");
            ps.setInt(1, getAccountID());
            ps.setInt(2, to.getAccountID());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("ERROR writing battlelog for char " + getName() + " to " + to.getName() + e);
        }
    }

    public MapleParty getParty() {
        if (party == null) {
            return null;
        } else if (party.isDisbanded()) {
            party = null;
        }
        return party;
    }

    public byte getWorld() {
        return world;
    }

    public void setWorld(byte world) {
        this.world = world;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void clearDoors() {
        doors.clear();
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public void updateDoor() {
        for (MapleDoor door : getMap().getAllDoors()) {
            System.out.println("portals");
            if (this == door.getOwner()) {
                door.updatePartyDoor(client, door);
                break;
            }
        }
    }

    public void addMechDoor(MechDoor door) {
        mechDoors.add(door);
    }

    public void clearMechDoors() {
        mechDoors.clear();
    }

    public List<MechDoor> getMechDoors() {
        return new ArrayList<MechDoor>(mechDoors);
    }

    public void setSmega() {
        if (smega) {
            smega = false;
            dropMessage(5, "You have set megaphone to disabled mode");
        } else {
            smega = true;
            dropMessage(5, "You have set megaphone to enabled mode");
        }
    }

    public boolean getSmega() {
        return smega;
    }

    public List<MapleSummon> getSummonsValues() {
        List<MapleSummon> summon = new ArrayList<>(summons);
        summonsLock.writeLock().lock();
        try {
            return summon;
        } finally {
            summonsLock.writeLock().unlock();
        }

    }

    public int getSummonsSize() {
        return summons.size();
    }

    public void addSummon(MapleSummon summon) {
        summonsLock.writeLock().lock();
        try {
            summons.add(summon);
        } finally {
            summonsLock.writeLock().unlock();
        }

    }

    public void removeSummon(MapleSummon s) {
        summonsLock.writeLock().lock();
        try {
            summons.remove(s);
        } finally {
            summonsLock.writeLock().unlock();
        }
    }

    public int getChair() {
        return chair;
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public void setChair(int chair) {
        this.chair = chair;
        stats.relocHeal(this);
        checkFish();
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public int getFamilyId() {
        if (mfc == null) {
            return 0;
        }
        return mfc.getFamilyId();
    }

    public int getSeniorId() {
        if (mfc == null) {
            return 0;
        }
        return mfc.getSeniorId();
    }

    public int getJunior1() {
        if (mfc == null) {
            return 0;
        }
        return mfc.getJunior1();
    }

    public int getJunior2() {
        if (mfc == null) {
            return 0;
        }
        return mfc.getJunior2();
    }

    public int getCurrentRep() {
        return currentrep;
    }

    public int getTotalRep() {
        return totalrep;
    }

    public void setCurrentRep(int _rank) {
        currentrep = _rank;
        if (mfc != null) {
            mfc.setCurrentRep(_rank);
        }
    }

    public void setTotalRep(int _rank) {
        totalrep = _rank;
        if (mfc != null) {
            mfc.setTotalRep(_rank);
        }
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public void increaseTotalWins() {
        totalWins++;
    }

    public void increaseTotalLosses() {
        totalLosses++;
    }

    public int getGuildId() {
        return guildid;
    }

    public byte getGuildRank() {
        return guildrank;
    }

    public int getGuildContribution() {
        return guildContribution;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
            guildContribution = 0;
        }
    }

    public void setGuildRank(byte _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public void setGuildContribution(int _c) {
        this.guildContribution = _c;
        if (mgc != null) {
            mgc.setGuildContribution(_c);
        }
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public void setAllianceRank(byte rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public byte getAllianceRank() {
        return allianceRank;
    }

    public MapleGuild getGuild() {
        if (getGuildId() <= 0) {
            return null;
        }
        return World.Guild.getGuild(getGuildId());
    }

    public void setJob(int j) {
        this.job = (short) j;
    }

    public void guildUpdate() {
        if (guildid <= 0) {
            return;
        }
        mgc.setLevel((short) totallevel);
        mgc.setJobId(job);
        World.Guild.memberLevelJobUpdate(mgc);
    }

    public void saveGuildStatus() {
        MapleGuild.setOfflineGuildStatus(guildid, guildrank, guildContribution, allianceRank, id);
    }

    public void familyUpdate() {
        if (mfc == null) {
            return;
        }
        World.Family.memberFamilyUpdate(mfc, this);
    }

    public void saveFamilyStatus() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE characters SET familyid = ?, seniorid = ?, junior1 = ?, junior2 = ? WHERE id = ?");
            if (mfc == null) {
                ps.setInt(1, 0);
                ps.setInt(2, 0);
                ps.setInt(3, 0);
                ps.setInt(4, 0);
            } else {
                ps.setInt(1, mfc.getFamilyId());
                ps.setInt(2, mfc.getSeniorId());
                ps.setInt(3, mfc.getJunior1());
                ps.setInt(4, mfc.getJunior2());
            }
            ps.setInt(5, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            System.out.println("SQLException: " + se.getLocalizedMessage());
            se.printStackTrace();
        }
        // MapleFamily.setOfflineFamilyStatus(familyid, seniorid, junior1, junior2,
        // currentrep, totalrep, id);
    }

    public void modifyCSPoints(int type, int quantity) {
        modifyCSPoints(type, quantity, false);
    }

    public void modifyCSPoints(int type, int quantity, boolean show) {

        switch (type) {
            case 1:
            case 4:
                if (acash + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
                    }
                    return;
                }
                acash += quantity;
                break;
            case 2:
                if (maplepoints + quantity < 0) {
                    if (show) {
                        dropMessage(6, "You have gained the max maple points.");
                    }
                    return;
                }
                maplepoints += quantity;
                break;
            default:
                break;
        }
        if (show && quantity != 0) {
            dropMessage(-1, "You have " + (quantity > 0 ? "gained " : "lost ") + quantity
                    + (type == 1 ? " cash." : " maple points."));
            // client.announce(EffectPacket.showForeignEffect(20));
        }
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
            case 4:
                return acash;
            case 2:
                return maplepoints;
            default:
                return 0;
        }
    }

    public final boolean hasEquipped(int itemid) {
        return inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }

    public final long countTotalItem(int itemid) {
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        long possesed = inventory[type.ordinal()].countById(itemid);
        if (type == MapleInventoryType.USE || type == MapleInventoryType.ETC) {
            possesed += getOverflowAmount(itemid);
        }
        return possesed;
    }

    public final boolean haveItem(int itemid, double quantity, boolean checkEquipped, boolean greaterOrEquals) {
        if (quantity < 1) {
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        long possesed = inventory[type.ordinal()].countById(itemid);
        if (checkEquipped && type == MapleInventoryType.EQUIP) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (type == MapleInventoryType.USE || type == MapleInventoryType.ETC) {
            possesed += getOverflowAmount(itemid);
        }
        if (greaterOrEquals) {
            return possesed >= Math.floor(quantity);
        } else {
            return possesed == Math.floor(quantity);
        }
    }

    public final boolean haveStoreItem(int itemid, double quantity) {
        if (quantity <= 0) {
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        long possesed = inventory[type.ordinal()].countById(itemid);
        return possesed >= Math.floor(quantity);
    }

    public final boolean havePlayerStoreItem(int itemid, double quantity) {
        if (quantity <= 0) {
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        long possesed = inventory[type.ordinal()].countById(itemid);
        if (type == MapleInventoryType.USE || type == MapleInventoryType.ETC) {
            possesed += getOverflowAmount(itemid);
        }
        return possesed >= Math.floor(quantity);
    }

    public final boolean haveItem(int itemid, double quantity, boolean checkEquipped, boolean greaterOrEquals, boolean etc) {
        if (quantity <= 0) {
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        long possesed = inventory[type.ordinal()].countById(itemid);
        if (checkEquipped && type == MapleInventoryType.EQUIP) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (etc) {
            if (type == MapleInventoryType.USE || type == MapleInventoryType.ETC) {
                possesed += getOverflowAmount(itemid);
            }
        }
        if (greaterOrEquals) {
            return possesed >= Math.floor(quantity);
        } else {
            return possesed == Math.floor(quantity);
        }
    }

    public final boolean haveItem(MapleInventoryType type, byte src, double quantity) {
        if (quantity >= 1) {
            if (getInventory(type).getItem(src) != null) {
                long possesed = inventory[type.ordinal()].countById(getInventory(type).getItem(src).getItemId());
                return possesed >= Math.floor(quantity);
            }
        }
        return false;
    }

    public final boolean haveItem(int id, byte src, double quantity) {
        if (quantity >= 1) {
            final MapleInventoryType type = GameConstants.getInventoryType(id);
            if (getInventory(type).getItem(src) != null) {
                long possesed = inventory[type.ordinal()].countById(getInventory(type).getItem(src).getItemId());
                return possesed >= Math.floor(quantity);
            }
        }
        return false;
    }

    public final boolean haveItem(int itemid, double quantity) {
        return haveItem(itemid, quantity, true, true);
    }

    public final boolean haveItem(int itemid) {
        return haveItem(itemid, 1, true, true);

    }

    public int countItemSlot(int slot, int itemid) {
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        return getInventory(type).getItem((short) slot).getQuantity();
    }

    public static enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public byte getBuddyCapacity() {
        return buddylist.getCapacity();
    }

    public void setBuddyCapacity(byte capacity) {
        buddylist.setCapacity(capacity);
        client.announce(BuddylistPacket.updateBuddyCapacity(capacity));
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void addCooldown(int skillId, long startTime, long length) {
        coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length));
    }

    public void removeCooldown(int skillId) {
        if (coolDowns.containsKey(Integer.valueOf(skillId))) {
            coolDowns.remove(Integer.valueOf(skillId));
        }
    }

    public boolean skillisCooling(int skillId) {
        return coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void giveCoolDowns(final int skillid, long starttime, long length) {
        addCooldown(skillid, starttime, length);
    }

    public void giveCoolDowns(final List<MapleCoolDownValueHolder> cooldowns) {
        int time;
        if (cooldowns != null) {
            for (MapleCoolDownValueHolder cooldown : cooldowns) {
                coolDowns.put(cooldown.skillId, cooldown);
            }
        } else {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?");
                ps.setInt(1, getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() <= 0) {
                        continue;
                    }
                    giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
                }
                ps.close();
                rs.close();
                deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");

            } catch (SQLException e) {
                System.err.println("Error while retriving cooldown from SQL storage");
            }
        }
    }

    public int getCooldownSize() {
        return coolDowns.size();
    }

    public int getDiseaseSize() {
        return diseases.size();
    }

    public List<MapleCoolDownValueHolder> getCooldowns() {
        List<MapleCoolDownValueHolder> ret = new ArrayList<MapleCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mc : coolDowns.values()) {
            if (mc != null) {
                ret.add(mc);
            }
        }
        return ret;
    }

    public final List<MapleDiseaseValueHolder> getAllDiseases() {
        return new ArrayList<MapleDiseaseValueHolder>(diseases.values());
    }

    public final boolean hasDisease(final MapleDisease dis) {
        return diseases.containsKey(dis);
    }

    public final int getDiseasesSize() {
        return diseases.size();
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill) {
        // System.out.println("Skill X: " + skill.getX() + " - Skill Duration: " +
        // skill.getDuration() + " - Skill ID: " + skill.getSkillId() + " - skill Lvl: "
        // + skill.getSkillLevel() + " ");
        if (!hasDisease(disease)) {
            if (getJob() >= 230 && getJob() <= 232) {
                return;
            }
            if (getBuffedValue(MapleBuffStat.HOLY_SHIELD) != null) {
                return;
            }
            final int mC = getBuffSource(MapleBuffStat.MECH_CHANGE);
            if (mC > 0 && mC != 35121005) { // missile tank can have debuffs
                return; // flamethrower and siege can't
            }

            if (stats.ASR > 0 && Randomizer.nextInt(100) < stats.ASR) {
                return;
            }
            long duration = skill.getDuration() - stats.decreaseDebuff;
            if (duration > 0) {
                BuffTimer.getInstance().schedule(() -> {
                    if (hasDisease(disease)) {
                        dispelDebuff(disease);
                    }
                }, duration);
                diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration));
                client.announce(BuffPacket.giveDebuff(disease, skill));
                map.broadcastMessage(this, BuffPacket.giveForeignDebuff(id, disease, skill), false);
                if (skill.getX() > 0 && disease == MapleDisease.POISON) { // poison, subtract all HP
                    addHP((int) -(stats.getCurrentMaxHp() * 0.05));
                }
            }
        }
    }

    public void giveDebuff(final MapleDisease disease, int x, long duration, int skillid, int level) {
        if (map != null && !hasDisease(disease)) {
            if (!(disease == MapleDisease.SEDUCE || disease == MapleDisease.STUN || disease == MapleDisease.FLAG)) {
                if (getBuffedValue(MapleBuffStat.HOLY_SHIELD) != null) {
                    return;
                }
            }
            final int mC = getBuffSource(MapleBuffStat.MECH_CHANGE);
            if (mC > 0 && mC != 35121005) { // missile tank can have debuffs
                return; // flamethrower and siege can't
            }
            if (stats.ASR > 0 && Randomizer.nextInt(100) < stats.ASR) {
                return;
            }
            chrLock.lock();
            try {
                diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration - stats.decreaseDebuff));
            } finally {
                chrLock.unlock();
            }
            client.announce(BuffPacket.giveDebuff(disease, x, skillid, level, (int) duration));
            map.broadcastMessage(this, BuffPacket.giveForeignDebuff(id, disease, skillid, level, x), false);

            if (x > 0 && disease == MapleDisease.POISON) { // poison, subtract all HP
                addHP((int) -(x * ((duration - stats.decreaseDebuff) / 1000)));
            }
        }
    }

    public void giveDebuff(final MapleDisease disease, MobSkill skill, int duracion) {
        // System.out.println("Skill X: " + skill.getX() + " - Skill Duration: " +
        // skill.getDuration() + " - Skill ID: " + skill.getSkillId() + " - skill Lvl: "
        // + skill.getSkillLevel() + " ");
        if (!hasDisease(disease)) {
            if (getJob() >= 230 && getJob() <= 232) {
                return;
            }
            if (getBuffedValue(MapleBuffStat.HOLY_SHIELD) != null) {
                return;
            }
            final int mC = getBuffSource(MapleBuffStat.MECH_CHANGE);
            if (mC > 0 && mC != 35121005) { // missile tank can have debuffs
                return; // flamethrower and siege can't
            }

            if (stats.ASR > 0 && Randomizer.nextInt(100) < stats.ASR) {
                return;
            }
            long duration = duracion;
            if (duration > 0) {
                BuffTimer.getInstance().schedule(() -> {
                    if (hasDisease(disease)) {
                        dispelDebuff(disease);
                    }
                }, duration);
                diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration));
                client.announce(BuffPacket.giveDebuff(disease, skill));
                map.broadcastMessage(this, BuffPacket.giveForeignDebuff(id, disease, skill), false);
                if (skill.getX() > 0 && disease == MapleDisease.POISON) { // poison, subtract all HP
                    addHP((int) -(stats.getCurrentMaxHp() * 0.05));
                }
            }
        }
    }

    public void giveForceDebuff(final MapleDisease disease, MobSkill skill, int duracion) {
        // System.out.println("Skill X: " + skill.getX() + " - Skill Duration: " +
        // skill.getDuration() + " - Skill ID: " + skill.getSkillId() + " - skill Lvl: "
        // + skill.getSkillLevel() + " ");
        if (!hasDisease(disease)) {
            final int mC = getBuffSource(MapleBuffStat.MECH_CHANGE);
            if (mC > 0 && mC != 35121005) { // missile tank can have debuffs
                return; // flamethrower and siege can't
            }
            long duration = duracion;
            if (duration > 0) {
                BuffTimer.getInstance().schedule(() -> {
                    if (hasDisease(disease)) {
                        dispelDebuff(disease);
                    }
                }, duration);
                diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration));
                client.announce(BuffPacket.giveDebuff(disease, skill));
                map.broadcastMessage(this, BuffPacket.giveForeignDebuff(id, disease, skill), false);
                if (skill.getX() > 0 && disease == MapleDisease.POISON) { // poison, subtract all HP
                    addHP((int) -(stats.getCurrentMaxHp() * 0.05));
                }
            }
        }
    }

    public final void giveSilentDebuff(final List<MapleDiseaseValueHolder> ld) {
        if (ld != null) {
            for (final MapleDiseaseValueHolder disease : ld) {
                diseases.put(disease.disease, disease);
            }
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            client.announce(BuffPacket.cancelDebuff(debuff));
            map.broadcastMessage(this, BuffPacket.cancelForeignDebuff(id, debuff), false);
            diseases.remove(debuff);
        }
    }

    public void dispelDebuffs() {
        List<MapleDisease> diseasess = new ArrayList<MapleDisease>(diseases.keySet());
        if (!diseasess.isEmpty()) {
            for (MapleDisease d : diseasess) {
                dispelDebuff(d);
            }
        }
    }

    public void cancelAllDebuffs() {
        chrLock.lock();
        try {
            diseases.clear();
        } finally {
            chrLock.unlock();
        }
    }

    public void setLevel(final short level) {
        this.level = (short) (level - 1);
    }

    public void sendNote(String to, String msg) {
        sendNote(to, msg, 0);
    }

    public void sendNote(String to, String msg, int fame) {
        MapleCharacterUtil.sendNote(to, getName(), msg, fame);
    }

    public void showNote() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            int count = rs.getRow();
            rs.first();
            client.announce(MTSCSPacket.showNotes(rs, count));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Unable to show note" + e);
        }
    }

    public void deleteNote(int id, int fame) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT gift FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("gift") == fame && fame > 0) { // not exploited! hurray
                    addFame(fame);
                    updateSingleStat(MapleStat.FAME, getFame());
                    client.announce(InfoPacket.getShowFameGain(fame));
                }
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Unable to delete note" + e);
        }
    }

    public int getMulungEnergy() {
        return mulung_energy;
    }

    public void mulung_EnergyModify(boolean inc) {
        if (inc) {
            if (mulung_energy + 100 > 10000) {
                mulung_energy = 10000;
            } else {
                mulung_energy += 100;
            }
        } else {
            mulung_energy = 0;
        }
        client.announce(CWvsContext.MulungEnergy(mulung_energy));
    }

    public void writeMulungEnergy() {
        client.announce(CWvsContext.MulungEnergy(mulung_energy));
    }

    public void writeEnergy(String type, String inc) {
        client.announce(CWvsContext.sendPyramidEnergy(type, inc));
    }

    public void writeStatus(String type, String inc) {
        client.announce(CWvsContext.sendGhostStatus(type, inc));
    }

    public void writePoint(String type, String inc) {
        client.announce(CWvsContext.sendGhostPoint(type, inc));
    }

    public final int getCombo() {
        return combo;
    }

    public void setCombo(final int combo) {
        this.combo = combo;
    }

    public final long getLastCombo() {
        return lastCombo;
    }

    public void setLastCombo(final long combo) {
        this.lastCombo = combo;
    }

    public final long getKeyDownSkill_Time() {
        return keydown_skill;
    }

    public void setKeyDownSkill_Time(final long keydown_skill) {
        this.keydown_skill = keydown_skill;
    }

    public void checkBerserk() { // berserk is special in that it doesn't use worldtimer :)
        if (job != 132 || lastBerserkTime < 0 || lastBerserkTime + 10000 > System.currentTimeMillis()) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        final Skill BerserkX = SkillFactory.getSkill(1320006);
        final int skilllevel = getTotalSkillLevel(BerserkX);
        if (skilllevel >= 1 && map != null) {
            lastBerserkTime = System.currentTimeMillis();
            final MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            stats.Berserk = (stats.getHp() * 100.0 / stats.getCurrentMaxHp()) >= ampStat.getX();
            client.announce(EffectPacket.showOwnBuffEffect(1320006, 1, getLevel(), skilllevel, (byte) (stats.Berserk ? 1 : 0)));
            map.broadcastMessage(this, EffectPacket.showBuffeffect(getId(), 1320006, 1, getLevel(), skilllevel, (byte) (stats.Berserk ? 1 : 0)), false);
        } else {
            lastBerserkTime = -1;
        }
    }

    public void setChalkboard(String text) {
        this.chalktext = text;
        if (map != null) {
            map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
        }
    }

    public String getChalkboard() {
        return chalktext;
    }

    public MapleMount getMount() {
        return mount;
    }

    public int[] getWishlist() {
        return wishlist;
    }

    public void clearWishlist() {
        for (int i = 0; i < 10; i++) {
            wishlist[i] = 0;
        }
        changed_wishlist = true;
    }

    public int getWishlistSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (wishlist[i] > 0) {
                ret++;
            }
        }
        return ret;
    }

    public void setWishlist(int[] wl) {
        this.wishlist = wl;
        changed_wishlist = true;
    }

    public int[] getRocks() {
        return rocks;
    }

    public int getRockSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (rocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRocks(int map) {
        for (int i = 0; i < 10; i++) {
            if (rocks[i] == map) {
                rocks[i] = 999999999;
                changed_trocklocations = true;
                break;
            }
        }
    }

    public void addRockMap() {
        if (getRockSize() >= 10) {
            return;
        }
        rocks[getRockSize()] = getMapId();
        changed_trocklocations = true;
    }

    public boolean isRockMap(int id) {
        for (int i = 0; i < 10; i++) {
            if (rocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int[] getRegRocks() {
        return regrocks;
    }

    public int getRegRockSize() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            if (regrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRegRocks(int map) {
        for (int i = 0; i < 5; i++) {
            if (regrocks[i] == map) {
                regrocks[i] = 999999999;
                changed_regrocklocations = true;
                break;
            }
        }
    }

    public void addRegRockMap() {
        if (getRegRockSize() >= 5) {
            return;
        }
        regrocks[getRegRockSize()] = getMapId();
        changed_regrocklocations = true;
    }

    public boolean isRegRockMap(int id) {
        for (int i = 0; i < 5; i++) {
            if (regrocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int[] getHyperRocks() {
        return hyperrocks;
    }

    public int getHyperRockSize() {
        int ret = 0;
        for (int i = 0; i < 13; i++) {
            if (hyperrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromHyperRocks(int map) {
        for (int i = 0; i < 13; i++) {
            if (hyperrocks[i] == map) {
                hyperrocks[i] = 999999999;
                changed_hyperrocklocations = true;
                break;
            }
        }
    }

    public void addHyperRockMap() {
        if (getRegRockSize() >= 13) {
            return;
        }
        hyperrocks[getHyperRockSize()] = getMapId();
        changed_hyperrocklocations = true;
    }

    public boolean isHyperRockMap(int id) {
        for (int i = 0; i < 13; i++) {
            if (hyperrocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public List<LifeMovementFragment> getLastRes() {
        return lastres;
    }

    public void setLastRes(List<LifeMovementFragment> lastres) {
        this.lastres = lastres;
    }

    public void dropTopMessage(String message) {
        client.announce(CWvsContext.getTopMsg(message));
    }

    public void dropMidMessage(String message) {
        client.announce(CWvsContext.getMidMsg(message, false, 1));
    }

    public void dropMidMessage(String message, int row) {
        client.announce(CWvsContext.getMidMsg(message, false, row));
    }

    public void dropShopMessage(String message, byte slot) {
        client.announce(PlayerShopPacket.shopChat(message, slot)); // 0 or what
    }

    public void dropMessage(int type, String message) {
        if (type == -1) {
            client.announce(CWvsContext.getTopMsg(message));
        } else if (type == -2) {
            client.announce(PlayerShopPacket.shopChat(message, 0)); // 0 or what
        } else if (type == -3) {
            client.announce(CField.getChatText(getId(), message, isSuperGM(), 0)); // 1 = hide
        } else if (type == -4) {
            client.announce(CField.getChatText(getId(), message, isSuperGM(), 1)); // 1 = hide
        } else if (type == -5) {
            client.announce(CField.getGameMessage(message, false)); // pink
        } else if (type == -6) {
            client.announce(CField.getGameMessage(message, true)); // white bg
        } else if (type == -7) {
            client.announce(CWvsContext.getMidMsg(message, false, 0));
        } else if (type == -8) {
            client.announce(CWvsContext.getMidMsg(message, true, 0));
        } else {
            client.announce(CWvsContext.serverNotice(type, message));
        }
    }

    public void dropColorMessage(int color, String message) {
        // 0: Normal Chat
        // 1: Whisper
        // 2: Party
        // 3: Buddy
        // 4: Guild
        // 5: Alliance
        // 6: Spouse [Dark Red]
        // 7: Grey
        // 8: Yellow
        // 9: Light Yellow
        // 10: Blue
        // 11: White
        // 12: Red
        // 13: Light Blue
        client.announce(CField.getGameMessage(message, color, false)); // pink
    }

    public void dropMessage(String message) {
        client.announce(CWvsContext.serverNotice(5, message));
    }

    public void dropErrorMessage(String message) {
        client.announce(CWvsContext.serverNotice(14, message));
    }

    public void yellowMessage(String message) {
        client.announce(CWvsContext.yellowChat(message));
    }

    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public int getConversation() {
        return inst.get();
    }

    public void setConversation(int inst) {
        if (inst > 0) {
            opened = true;
        } else {
            opened = false;
        }
        this.inst.set(inst);
    }

    public int getDirection() {
        return insd.get();
    }

    public void setDirection(int inst) {
        this.insd.set(inst);
    }

    public MapleCarnivalParty getCarnivalParty() {
        return carnivalParty;
    }

    public void setCarnivalParty(MapleCarnivalParty party) {
        carnivalParty = party;
    }

    public void addCP(int ammount) {
        totalCP += ammount;
        availableCP += ammount;
    }

    public void useCP(int ammount) {
        availableCP -= ammount;
    }

    public int getAvailableCP() {
        return availableCP;
    }

    public int getTotalCP() {
        return totalCP;
    }

    public void resetCP() {
        totalCP = 0;
        availableCP = 0;
    }

    public void addCarnivalRequest(MapleCarnivalChallenge request) {
        pendingCarnivalRequests.add(request);
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return pendingCarnivalRequests.pollLast();
    }

    public void clearCarnivalRequests() {
        pendingCarnivalRequests = new LinkedList<MapleCarnivalChallenge>();
    }

    public void startMonsterCarnival(final int enemyavailable, final int enemytotal) {
        client.announce(MonsterCarnivalPacket.startMonsterCarnival(this, enemyavailable, enemytotal));
    }

    public void CPUpdate(final boolean party, final int available, final int total, final int team) {
        client.announce(MonsterCarnivalPacket.CPUpdate(party, available, total, team));
    }

    public void playerDiedCPQ(final String name, final int lostCP, final int team) {
        client.announce(MonsterCarnivalPacket.playerDiedMessage(name, lostCP, team));
    }

    public boolean tryGainAchievement(int id) {
        return finishedAchievements.addIfAbsent(id);
    }

    public int getMaxStamFromChar() {
        return getStat().getStamina();
    }

    public boolean achievementFinished(int achievementid) {
        if (achievementid == 0) {
            return true;
        }
        return finishedAchievements.contains(achievementid);
    }

    public void finishAchievement(int id) {
        // System.out.println("test23: " + id);
        if (isAlive() && !isClone()) {
            if (!achievementFinished(id)) {
                MapleAchievement Ach = MapleAchievements.getInstance().getById(id);
                if (Ach != null) {
                    Ach.finishAchievement(this);
                }
            }
        }
    }

    public void saveAchievement(int aid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO achievements(accountid, achievementid) VALUES(?, ?)")) {
                ps.setInt(1, accountid);
                ps.setInt(2, aid);
                ps.execute();
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + ex);
        }
    }

    public boolean getAchievement(int id) {
        return achievementFinished(id);
    }

    public MapleAchievement getAchievementInfo(int id) {
        return MapleAchievements.getInstance().getById(id);
    }

    public List<Integer> getFinishedAchievements() {
        return Collections.unmodifiableList(finishedAchievements);
    }

    public boolean tryGainQuest(int id) {
        return finishedQuests.addIfAbsent(id);
    }

    public boolean questFinished(int id) {
        return finishedQuests.contains(id);
    }

    //damage skins -------------------------------------
    public void saveDamageSkins() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO damage_skins (accid, id, level, exp) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level), exp = VALUES(exp)")) {
                for (int skinid : damageSkins.keySet()) {
                    if (skinid < 9000) {
                        ps.setInt(1, accountid);
                        ps.setInt(2, skinid);
                        ps.setInt(3, damageSkins.get(skinid).getLeft());
                        ps.setLong(4, damageSkins.get(skinid).getRight());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + ex);
        }
    }

    public void saveDamageSkin(int skin, int level, long exp) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO damage_skins (accid, id, level, exp) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level), exp = VALUES(exp)")) {
                ps.setInt(1, accountid);
                ps.setInt(2, skin);
                ps.setInt(3, level);
                ps.setLong(4, exp);
                ps.execute();
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + ex);
        }
    }

    public long getSkinExpFromDB(int skinid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM damage_skins where accid = ? and id = ?")) {
                ps.setInt(1, accountid);
                ps.setInt(2, skinid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("exp");
                    }
                }
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + ex);
        }
        return 0;
    }

    public int getOrbs(int id) {
        return GameConstants.getOrbsFromTier(MapleDamageSkins.getInstance().getById(id).getTier());
    }

    public boolean gainDamageSkin(int id) {
        if (id < 9000) {
            if (damageSkins.containsKey(id)) {
                gainSkinLevel(id);
                return false;
            } else {
                damageSkins.put(id, new Pair<Integer, Long>(1, 0L));
            }
            saveDamageSkin(id, 1, 0);
            return true;
        }
        return false;
    }

    public boolean gainDamageSkinNoOrb(int id) {
        if (id < 9000) {
            if (damageSkins.containsKey(id)) {
                gainSkinLevel(id);
                return false;
            } else {
                damageSkins.put(id, new Pair<Integer, Long>(1, 0L));
            }
            saveDamageSkin(id, 1, 0);
            return true;
        }
        return false;
    }

    public void setDefaultSkin() {
        if (damageSkin < 9000) {
            saveDamageSkin(damageSkin, damageLevel, damageExp);
        }
        damageSkin = 9999;
        damageLevel = 1;
        damageExp = 0;
        getSkinStat(true);
        setVar("skin", 0);
    }

    public List<Integer> getDamageSkins() {
        return Collections.unmodifiableList(new ArrayList<Integer>(damageSkins.keySet()));
    }

    public void changeSkin(int id) {
        if (damageSkin < 9000) {
            saveDamageSkin(damageSkin, damageLevel, damageExp);
        }
        if (damageSkins.containsKey(id)) {
            damageSkin = id;
            damageLevel = damageSkins.get(id).getLeft();
            damageExp = getSkinExp(damageSkin);
            setVar("skin", damageSkin);
        } else {
            System.out.println(getName() + " is missing skin id: " + id);
            dropMessage("Skin ID missing: " + id + " - contact Staff on discord whith this id and error!");
            damageSkin = 9999;
            damageLevel = 1;
            damageExp = 0;
            setVar("skin", 0);
        }
        getSkinStat(true);
    }

    public void changeSkinLevel(int id, int level) {
        if (level <= 999) {
            damageSkins.put(id, new Pair<Integer, Long>(level, 0L));
            saveDamageSkin(id, level, 0);
            if (damageSkin == id) {
                damageExp = 0;
                damageLevel = level;
                getSkinStat(true);
                dropMessage("Your Damage skin has been leveled up to " + damageLevel + ".");
            }
        }
    }

    public void gainSkinLevel(int id) {
        if (damageSkins.containsKey(id)) {
            int level = damageSkins.get(id).getLeft() + 1;
            damageSkins.put(id, new Pair<Integer, Long>(level, 0L));
            saveDamageSkin(id, level, 0);
            if (damageSkin == id) {
                damageExp = 0;
                damageLevel = level;
            }
            getSkinStat(true);
            dropMessage("Your Damage skin has been leveled up to " + (damageSkins.get(id).getLeft()) + ".");
        }
    }

    public void setDamageSkin(int value) {
        damageSkin = value;
    }

    public int getDamageSkin() {
        return damageSkin;
    }

    public void maxSkin(int id) {
        damageSkins.put(id, new Pair<Integer, Long>(999, 0L));
        saveDamageSkin(id, 999, 0);
        if (damageSkin == id) {
            damageExp = 0;
            damageLevel = 999;
            getSkinStat(true);
            dropMessage("Your Damage skin has been leveled up to " + damageLevel + ".");
        }
    }

    public int getSkinLevel(int id) {
        return damageSkins.get(id).getLeft();
    }

    public long getSkinExp(int id) {
        return damageSkins.get(id).getRight();
    }

    public long getSkinExp() {
        return damageExp;
    }

    public int getSkinExpPercent() {
        if (damageSkin < 9000) {
            if (damageLevel < getMaxSkinLevel(damageSkin)) {
                return (int) (((double) getSkinExp() / (double) getSkinNeededExp(damageLevel)) * 100);
            }
        }
        return 0;
    }

    public int getSkinExpPercent(int skin) {
        if (skin < 9000) {
            if (getSkinLevel(skin) < getMaxSkinLevel(skin)) {
                return (int) (((double) getSkinExp(skin) / (double) getSkinNeededExp(skin, getSkinLevel(skin))) * 100);
            }
        }
        return 0;
    }

    public long getSkinNeededExp(int skin, int level) {
        if (getSkinLevel(skin) < getMaxSkinLevel(skin)) {
            return (long) (Math.pow(level, 2.5) + 2474);
        } else {
            return 0;
        }
    }

    public long getSkinNeededExp(int level) {
        return Long.MAX_VALUE;
    }

    public int getMaxSkinLevel(int id) {
        int max = 999;
        MapleDamageSkin skin = MapleDamageSkins.getInstance().getById(id);
        if (skin.getTier() == 6) {
            max = 9999;
        }
        if (skin.getTier() == 7) {
            max = 99999;
        }
        return max;
    }

    public void gainSkinExp(int gain) { // needa
        /*
        if (damageSkin < 9000) {
            if (damageLevel < getMaxSkinLevel(damageSkin)) {
                double bGain = (double) (damageExp + Math.floor(gain * getStat().getMasterBuff()));
                damageExp = (long) Randomizer.DoubleMinMax(bGain, 0, Long.MAX_VALUE);
                if (damageExp >= getSkinNeededExp(damageLevel)) {
                    damageExp = 0;
                    damageLevel += 1;
                    getClient().announce(EffectPacket.showForeignEffect(35));
                    saveDamageSkin(damageSkin, damageLevel, 0);
                    getSkinStat(true);
                    dropTopMessage("Your Damage Skin Leveled Up to " + damageLevel);
                }
                damageSkins.put(damageSkin, new Pair<Integer, Long>(damageLevel, damageExp));
            }
        }
         */
    }

    public void gainSkinLevel() { // needa
        if (damageSkin < 9000) {
            if (damageLevel < 999) {
                damageExp = 0;
                damageLevel += 1;
                damageSkins.put(damageSkin, new Pair<Integer, Long>(damageLevel, 0L));
                getClient().announce(EffectPacket.showForeignEffect(35));
                saveDamageSkin(damageSkin, damageLevel, 0);
                getSkinStat(true);
                dropTopMessage("Your Damage Skin Leveled Up to " + damageLevel);
            }
        }
    }

    public boolean hasSkin(int id) {
        return damageSkins.containsKey(id);
    }

    public int getElement(int id) {
        return MapleDamageSkins.getInstance().getById(id).getElement();
    }

    public double getElementBonus(int id, int mobelement) {
        /*
         0 = non-elemental
         1 = fire
         2 = water
         3 = wind
         4 = earth
         5 = shadow
         6 = light
         7 = heal
         8 = rainbow
         */
        double damage = 1.0;
        if (id > 9000) {
            return 0.75;
        }
        int element = MapleDamageSkins.getInstance().getById(id).getElement();
        if (element == 8) {
            if (mobelement == 8) {
                return 1.0;
            }
            return 1.5;
        }
        if (element == mobelement || mobelement == 8) {
            return 0.5;
        }
        if ((element == 1 && mobelement == 2) || (element == 2 && mobelement == 1)) {
            return 2.0;
        }
        if ((element == 3 && mobelement == 4) || (element == 4 && mobelement == 3)) {
            return 2.0;
        }
        if ((element == 5 && mobelement == 6) || (element == 6 && mobelement == 5)) {
            return 2.0;
        }
        if (element == 0 && mobelement == 7) {
            return 2.0;
        }
        if (element == 7) {
            if (mobelement == 0) {
                return 2.5;
            }
            return 0.75;
        }
        return damage;
    }

    public void getSkinStat(boolean update) {
        double skinValue = 0.2;
        DXP = 0.0;
        DDR = 0.0;
        DAS = 0.0;
        DOP = 0.0;
        DMR = 0.0;
        DTD = 0.0;
        DBD = 0.0;
        DCD = 0.0;
        DIED = 0.0;
        DSXP = 0.0;
        DSDR = 0.0;
        DSAS = 0.0;
        DSOP = 0.0;
        DSMR = 0.0;
        DSTD = 0.0;
        DSBD = 0.0;
        DSCD = 0.0;
        DSIED = 0.0;
        for (int skinid : damageSkins.keySet()) {
            if (damageSkin < 9000) {
                MapleDamageSkin skin = MapleDamageSkins.getInstance().getById(skinid);
                if (skin != null) {
                    int slevel = damageSkins.get(skinid).getLeft();
                    //int slevel = skin.getMaxLevel();
                    for (int i = 0; i < 3; i++) {
                        if (skin.getAmount(i) > 0) {
                            switch (skin.getStat(i)) {
                                case 0 -> {
                                    DXP += (skin.getAmount(i) * slevel);
                                    DSXP += (skin.getAmount(i) * slevel);
                                }
                                case 1 -> {
                                    DDR += (skin.getAmount(i) * slevel);
                                    DSDR += (skin.getAmount(i) * slevel);
                                }
                                case 2 -> {
                                    DCD += (skin.getAmount(i) * slevel);
                                    DSCD += (skin.getAmount(i) * slevel);
                                }
                                case 3 -> {
                                    DAS += (skin.getAmount(i) * slevel);
                                    DSAS += (skin.getAmount(i) * slevel);
                                }
                                case 4 -> {
                                    DOP += (skin.getAmount(i) * slevel);
                                    DSOP += (skin.getAmount(i) * slevel);
                                }
                                case 5 -> {
                                    DMR += (skin.getAmount(i) * slevel);
                                    DSMR += (skin.getAmount(i) * slevel);
                                }
                                case 6 -> {
                                    DTD += (skin.getAmount(i) * slevel);
                                    DSTD += (skin.getAmount(i) * slevel);
                                }
                                case 7 -> {
                                    DBD += (skin.getAmount(i) * slevel);
                                    DSBD += (skin.getAmount(i) * slevel);
                                }
                                case 8 -> {
                                    DIED += (skin.getAmount(i) * slevel);
                                    DSIED += (skin.getAmount(i) * slevel);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (update) {
            getStat().recalcLocalStats(this);
        }
    }

    //--------------------------------------------------
    //dojo
    public int getDojoLevel() {
        return dojo_level;
    }

    public long getDojoExp() {
        return dojo_exp;
    }

    public long getNeededDojoExp() {
        return (getAndroidNeededExp(dojo_level));
    }

    public int getDojoPercent() {
        return (int) (((double) dojo_exp / (double) getAndroidNeededExp(dojo_level)) * 100);
    }

    public int getDojoPercent(int level, long exp) {
        return (int) (((double) exp / (double) getAndroidNeededExp(level)) * 100);
    }

    public void gainDojoExp(long gain) { // needa
        boolean friday = GameConstants.getGloablEvent() || Calendar.getInstance().get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY && Calendar.getInstance().get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY;
        long total = (long) (gain * (friday ? 2 : 1) * getStat().getMasterBuff() * GameConstants.getMasteryRate());
        dojo_exp += total;
        dropTopMessage("+" + total + " Dojo Exp" + (friday ? " (Doubled)" : ""));
        if (dojo_exp >= getAndroidNeededExp(dojo_level)) {
            dojo_exp = 0;
            dojo_level += 1;
            getClient().announce(EffectPacket.showForeignEffect(35));
            getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
            dropMessage("[Dojo] You have gained a Dojo Level! Dojo Level: " + dojo_level);
            saveDojo();
            recalcLocalStats();
        }
    }

    public void saveDojo(final Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO dojo (charid, level, exp) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level), exp = VALUES(exp)")) {
            ps.setInt(1, id);
            ps.setInt(2, dojo_level);
            ps.setLong(3, dojo_exp);
            ps.execute();
        }
    }

    public void saveDojo() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO dojo (charid, level, exp) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level), exp = VALUES(exp)")) {
                ps.setInt(1, id);
                ps.setInt(2, dojo_level);
                ps.setLong(3, dojo_exp);
                ps.execute();
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
            System.err.println(MapleClient.getLogMessage(this, "[charsave] Error saving character data") + ex);
        }
    }

    public String getCharById(final Connection con, int cid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM characters where id = ?")) {
            ps.setInt(1, cid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return "";
    }

    public String getDojoRank() {
        String list = "";
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `dojo` ORDER BY `level` DESC, `exp` DESC LIMIT 0, 50")) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    String star;
                    if (rs.getRow() == 1) {
                        star = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
                    } else {
                        star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
                    }
                    list += star + "[#" + rs.getRow() + "] #r" + getCharById(con, rs.getInt("charid")) + "#k - #bDojo Level: " + rs.getInt("level") + "#k (" + getDojoPercent(rs.getInt("level"), rs.getLong("exp")) + "%)" + star + " \r\n";
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error handling dojoRanking");
            e.printStackTrace();
        }
        return list;
    }

    public String getRebornRank() {
        String list = "";
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_variables` WHERE `var` = 'reborn' ORDER BY `amount` DESC LIMIT 50")) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    String star;
                    if (rs.getRow() == 1) {
                        star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
                    } else {
                        star = "#fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
                    }
                    long tot = rs.getLong("amount");
                    list += star + "[#b" + rs.getRow() + "#k] #r" + getCharById(con, rs.getInt("charid")) + "#k - #bReborns: " + tot + "#k\r\n";
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error handling RebornRanking");
            e.printStackTrace();
        }
        return list;
    }

    public boolean finishMapleQuest(int id) {
        // System.out.println("test23: " + id);
        if (isAlive() && !isClone()) {
            MapleKQuest Ach = MapleKQuests.getInstance().getById(id);
            if (Ach != null) {
                return Ach.finishQuest(this);
            }
        }
        return false;
    }

    public boolean finishMapleQuestEtc(int id) {
        // System.out.println("test23: " + id);
        if (isAlive() && !isClone()) {
            MapleKQuest Ach = MapleKQuests.getInstance().getById(id);
            if (Ach != null) {
                return Ach.finishQuestETC(this);
            }
        }
        return false;
    }

    public int getquestFinishedbyCag(int cag) {
        int count = 0;
        for (MapleKQuest ach : MapleKQuests.getInstance().getQuestsbyCag(cag)) {
            if (ach.getCag() == cag) {
                if (questFinished(ach.getId())) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean getQuest(int id) {
        return questFinished(id);
    }

    public List<Integer> getFinishedQuestss() {
        return Collections.unmodifiableList(finishedQuests);
    }

    public boolean getCanTalk() {
        return this.canTalk;
    }

    public void canTalk(boolean talk) {
        this.canTalk = talk;
    }

    public double expMax() {
        double max = 0.01;
        if (getTotalLevel() < 500) {
            max = 0.02;
        }
        if (getTotalLevel() < 250) {
            max = 0.03;
        }
        if (getTotalLevel() < 150) {
            max = 0.04;
        }
        if (getTotalLevel() < 100) {
            max = 0.05;
        }
        return Math.pow(Randomizer.DoubleMax(getTotalLevel(), 10000), 2) * max;
    }

    public double rebornExpRate() {
        return 1;
        //return Math.pow(0.75, Randomizer.Max((int) getReborns(), 50));
    }

    public double getBaseEXPMod() {
        double bexp = getStat().expMod;
        double totem = getMap().getTotemType(this);
        return Randomizer.DoubleMin(bexp * rebornExpRate(), 1.0);
    }

    public double getEXPMod() {
        double bonus = getMap().getTotemType(this) * getStat().getItemExpRate();
        double rate = Randomizer.DoubleMin(getStat().expMod, 1.0);
        return rate * bonus;
    }

    public double getDropMod() {
        double bdrop = getStat().dropMod;
        double totem = getMap().getTotemType(this);
        return Randomizer.DoubleMax(bdrop, Randomizer.DoubleMin(Math.pow(getTotalLevel(), 2) * 0.01, 1.0)) * totem;
    }

    public double getMesoMod() {
        double bmeso = getStat().mesoMod;
        double totem = getMap().getTotemType(this);
        return Randomizer.DoubleMax(bmeso, Randomizer.DoubleMin(Math.pow(getTotalLevel(), 2) * 0.01, 1.0)) * totem;
    }

    public double getETCModCap() {
        double b = 100.0 + Randomizer.DoubleMax(getAccVara("Morale") * 0.01, 500.0) + Randomizer.DoubleMax(getAccVara("Etc_Mod") * 0.01, 500.0);
        return Randomizer.DoubleMax(b, 1000.0);
    }

    public double getETCMod() {
        return stats.items;
    }

    public int getCashMod() {
        return stats.cashMod;
    }

    public void setPoints(int p) {
        this.points = p;
        if (this.points >= 1) {
            finishAchievement(1);
        }
    }

    public int getPoints() {
        return points;
    }

    public void setVPoints(int p) {
        this.vpoints = p;
    }

    public int getVPoints() {
        return vpoints;
    }

    public CashShop getCashInventory() {
        return cs;
    }

    public void removeItem(int id, int quantity) {
        MapleInventoryManipulator.removeById(client, GameConstants.getInventoryType(id), id, -quantity, true, false);
        client.announce(InfoPacket.getShowItemGain(id, (short) quantity, true));
    }

    public void removeAll(int id) {
        removeAll(id, true);
    }

    public void removeAll(int id, boolean show) {
        MapleInventoryType type = GameConstants.getInventoryType(id);
        int possessed = getInventory(type).countById(id);

        if (possessed > 0) {
            MapleInventoryManipulator.removeById(getClient(), type, id, possessed, true, false);
            if (show) {
                getClient().announce(InfoPacket.getShowItemGain(id, (short) -possessed, true));
            }
        }
        /*
         * if (type == MapleInventoryType.EQUIP) { //check equipped type =
         * MapleInventoryType.EQUIPPED; possessed = getInventory(type).countById(id);
         * 
         * if (possessed > 0) { MapleInventoryManipulator.removeById(getClient(), type,
         * id, possessed, true, false); getClient().announce(CField.getShowItemGain(id,
         * (short)-possessed, true)); } }
         */
    }

    public Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> getRings(boolean equip) {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        List<MapleRing> crings = new ArrayList<MapleRing>(), frings = new ArrayList<MapleRing>(), mrings = new ArrayList<MapleRing>();
        MapleRing ring;
        for (Item ite : equipped) {
            Equip item = (Equip) ite;
            if (item.getRing() != null) {
                ring = item.getRing();
                ring.setEquipped(true);
                /*
                 * if (GameConstants.isEffectRing(item.getItemId())) { if (equip) { if
                 * (GameConstants.isCrushRing(item.getItemId())) { crings.add(ring); } else if
                 * (GameConstants.isFriendshipRing(item.getItemId())) { frings.add(ring); } else
                 * if (GameConstants.isMarriageRing(item.getItemId())) { mrings.add(ring); } }
                 * else { if (crings.isEmpty() && GameConstants.isCrushRing(item.getItemId())) {
                 * crings.add(ring); } else if (frings.isEmpty() &&
                 * GameConstants.isFriendshipRing(item.getItemId())) { frings.add(ring); } else
                 * if (mrings.isEmpty() && GameConstants.isMarriageRing(item.getItemId())) {
                 * mrings.add(ring); } //for 3rd person the actual slot doesnt matter, so we'll
                 * use this to have both shirt/ring same? //however there seems to be something
                 * else behind this, will have to sniff someone with shirt and ring, or more
                 * conveniently 3-4 of those } }
                 */
            }
        }
        if (equip) {
            iv = getInventory(MapleInventoryType.EQUIP);
            for (Item ite : iv.list()) {
                Equip item = (Equip) ite;
                if (item.getRing() != null && GameConstants.isCrushRing(item.getItemId())) {
                    ring = item.getRing();
                    ring.setEquipped(false);
                    /*
                     * if (GameConstants.isFriendshipRing(item.getItemId())) { frings.add(ring); }
                     * else if (GameConstants.isCrushRing(item.getItemId())) { crings.add(ring); }
                     * else if (GameConstants.isMarriageRing(item.getItemId())) { mrings.add(ring);
                     * }
                     */
                }
            }
        }
        Collections.sort(frings, new MapleRing.RingComparator());
        Collections.sort(crings, new MapleRing.RingComparator());
        Collections.sort(mrings, new MapleRing.RingComparator());

        return new Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>>(crings, frings, mrings);
    }

    public int getFH() {
        MapleFoothold fh = getMap().getFootholds().findBelow(getTruePosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public void startFairySchedule(boolean exp) {
        startFairySchedule(exp, false);
    }

    public void startFairySchedule(boolean exp, boolean equipped) {
        cancelFairySchedule(exp || stats.equippedFairy == 0);
        if (fairyExp <= 0) {
            fairyExp = (byte) stats.equippedFairy;
        }
        if (equipped && fairyExp < stats.equippedFairy * 3 && stats.equippedFairy > 0) {
            dropMessage(5, "The Fairy Pendant's experience points will increase to " + (fairyExp + stats.equippedFairy) + "% after one hour.");
        }
        lastFairyTime = System.currentTimeMillis();
    }

    public final boolean canFairy(long now) {
        return lastFairyTime > 0 && lastFairyTime + (60 * 60 * 1000) < now;
    }

    public final boolean canHP(long now) {
        if (lastHPTime + 5000 < now) {
            lastHPTime = now;
            return true;
        }
        return false;
    }

    public final boolean canMP(long now) {
        if (lastMPTime + 5000 < now) {
            lastMPTime = now;
            return true;
        }
        return false;
    }

    public final boolean canHPRecover(long now) {
        if (stats.hpRecoverTime > 0 && lastHPTime + stats.hpRecoverTime < now) {
            lastHPTime = now;
            return true;
        }
        return false;
    }

    public final boolean canMPRecover(long now) {
        if (stats.mpRecoverTime > 0 && lastMPTime + stats.mpRecoverTime < now) {
            lastMPTime = now;
            return true;
        }
        return false;
    }

    public void cancelFairySchedule(boolean exp) {
        lastFairyTime = 0;
        if (exp) {
            this.fairyExp = 0;
        }
    }

    public void doFairy() {
        if (fairyExp < stats.equippedFairy * 3 && stats.equippedFairy > 0) {
            fairyExp += stats.equippedFairy;
            dropMessage(5, "The Fairy Pendant's EXP was boosted to " + fairyExp + "%.");
        }
        if (getGuildId() > 0) {
            World.Guild.gainGP(getGuildId(), 20, id);
            client.announce(InfoPacket.getGPContribution(20));
        }
        traits.get(MapleTraitType.will).addExp(5, this); // willpower every hour
        startFairySchedule(false, true);
    }

    public byte getFairyExp() {
        return fairyExp;
    }

    public int getTeam() {
        return coconutteam;
    }

    public void setTeam(int v) {
        this.coconutteam = v;
    }

    public void spawnPet(byte slot) {
        spawnPet(slot, false, true);
    }

    public void spawnPet(byte slot, boolean lead) {
        spawnPet(slot, lead, true);
    }

    public void spawnPet(byte slot, boolean lead, boolean broadcast) {
        final Item item = getInventory(MapleInventoryType.CASH).getItem(slot);
        if (item == null || item.getItemId() > 5003000 || item.getItemId() < 5000000) {
            return;
        }
        switch (item.getItemId()) {
            case 5000047:
            case 5000028: {
                final MaplePet pet = MaplePet.createPet(item.getItemId() + 1, MapleInventoryIdentifier.getInstance());
                if (pet != null) {
                    MapleInventoryManipulator.addById(client, item.getItemId() + 1, (short) 1, item.getOwner(), pet, 45, "Evolved from pet " + item.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                    MapleInventoryManipulator.removeFromSlot(client, MapleInventoryType.CASH, slot, (short) 1, false);
                }
                break;
            }
            default: {
                final MaplePet pet = item.getPet();
                if (pet != null && (item.getItemId() != 5000054 || pet.getSecondsLeft() > 0) && (item.getExpiration() == -1 || item.getExpiration() > System.currentTimeMillis())) {
                    if (pet.getSummoned()) { // Already summoned, let's keep it
                        unequipPet(pet, true, false);
                    } else {
                        int leadid = 8;
                        if (GameConstants.isKOC(getJob())) {
                            leadid = 10000018;
                        } else if (GameConstants.isAran(getJob())) {
                            leadid = 20000024;
                        } else if (GameConstants.isEvan(getJob())) {
                            leadid = 20011024;
                        } else if (GameConstants.isMercedes(getJob())) {
                            leadid = 20021024;
                        } else if (GameConstants.isDemon(getJob())) {
                            leadid = 30011024;
                        } else if (GameConstants.isResist(getJob())) {
                            leadid = 30001024;
                            // } else if (GameConstants.isCannon(getJob())) {
                            // leadid = 10008; //idk, TODO JUMP
                        }
                        if (getSkillLevel(SkillFactory.getSkill(leadid)) == 0 && getPet(0) != null) {
                            unequipPet(getPet(0), false, false);
                        } else if (lead && getSkillLevel(SkillFactory.getSkill(leadid)) > 0) { // Follow the Lead
                            // shiftPetsRight();
                        }
                        final Point pos = getPosition();
                        pos.y--;
                        pet.setPos(pos);
                        try {
                            pet.setFh(getMap().getFootholds().findBelow(pos).getId());
                        } catch (NullPointerException e) {
                            pet.setFh(0); // lol, it can be fixed by movement
                        }
                        pet.setStance(0);
                        pet.setSummoned(1); // let summoned be true..
                        addPet(pet);
                        pet.setSummoned(getPetIndex(pet) + 1); // then get the index
                        if (broadcast && getMap() != null) {
                            getMap().broadcastMessage(this, PetPacket.showPet(this, pet, false, false), true);
                            client.announce(PetPacket.showPetUpdate(this, pet.getUniqueId(), (byte) (pet.getSummonedValue() - 1)));
                            if (GameConstants.GMS) {
                                client.announce(PetPacket.petStatUpdate(this));
                            }
                        }
                        item.setSummoned(true);
                    }
                }
                break;
            }
        }
        client.announce(CWvsContext.enableActions());
    }

    public void clearLinkMid() {
        linkMobs.clear();
        cancelEffectFromBuffStat(MapleBuffStat.HOMING_BEACON);
        cancelEffectFromBuffStat(MapleBuffStat.ARCANE_AIM);
    }

    public int getFirstLinkMid() {
        for (Integer lm : linkMobs.keySet()) {
            return lm.intValue();
        }
        return 0;
    }

    public Map<Integer, Integer> getAllLinkMid() {
        return linkMobs;
    }

    public void setLinkMid(int lm, int x) {
        linkMobs.put(lm, x);
    }

    public int getDamageIncrease(int lm) {
        if (linkMobs.containsKey(lm)) {
            return linkMobs.get(lm);
        }
        return 0;
    }

    public boolean isClone() {
        return clone;
    }

    public void setClone(boolean c) {
        this.clone = c;
    }

    public WeakReference<MapleCharacter>[] getClones() {
        return clones;
    }

    public MapleCharacter cloneLooks() {
        MapleClient cs = new MapleClient(null, null, new MockIOSession());

        final int minus = (getId() + Randomizer.nextInt(Integer.MAX_VALUE - getId())); // really randomize it, dont want
        // it to fail

        MapleCharacter ret = new MapleCharacter(true);
        ret.client.setAccountName(client.getAccountName());
        ret.id = minus;
        ret.client = cs;
        ret.exp.set(0);
        ret.meso = 0;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = client.getAccID();
        ret.name = name;
        ret.level = level;
        ret.fame = fame;
        ret.job = job;
        ret.hair = hair;
        ret.face = face;
        ret.demonMarking = demonMarking;
        ret.skinColor = skinColor;
        ret.monsterbook = monsterbook;
        ret.mount = mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = gmLevel;
        ret.gender = gender;
        ret.mapid = map.getId();
        ret.map = map;
        ret.setStance(getStance());
        ret.chair = chair;
        ret.itemEffect = itemEffect;
        ret.guildid = guildid;
        ret.currentrep = currentrep;
        ret.totalrep = totalrep;
        ret.stats = stats;
        ret.effects.putAll(effects);
        ret.dispelSummons();
        ret.guildrank = guildrank;
        ret.guildContribution = guildContribution;
        ret.allianceRank = allianceRank;
        ret.setPosition(getTruePosition());
        for (Item equip : getInventory(MapleInventoryType.EQUIPPED).newList()) {
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip.copy());
        }
        ret.keylayout = keylayout;
        ret.questinfo = questinfo;
        ret.savedLocations = savedLocations;
        ret.wishlist = wishlist;
        ret.buddylist = buddylist;
        ret.keydown_skill = 0;
        ret.lastmonthfameids = lastmonthfameids;
        ret.lastfametime = lastfametime;
        ret.storage = storage;
        ret.cs = this.cs;
        ret.acash = acash;
        ret.maplepoints = maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());
        while (map.getCharacterById(ret.id) != null
                || client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null) {
            ret.id++;
        }
        ret.client.setPlayer(ret);
        return ret;
    }

    public final void cloneLook() {
        if (clone || inPVP()) {
            return;
        }
        for (int i = 0; i < clones.length; i++) {
            if (clones[i].get() == null) {
                final MapleCharacter newp = cloneLooks();
                map.addPlayer(newp, false);
                map.broadcastMessage(CField.updateCharLook(newp));
                // map.movePlayer(newp, getTruePosition());
                clones[i] = new WeakReference<MapleCharacter>(newp);
                return;
            }
        }
    }

    public final void disposeClones() {
        numClones = 0;
        for (int i = 0; i < clones.length; i++) {
            if (clones[i].get() != null) {
                map.removePlayer(clones[i].get());
                if (clones[i].get().getClient() != null) {
                    clones[i].get().getClient().setPlayer(null);
                    clones[i].get().client = null;
                }
                clones[i] = new WeakReference<MapleCharacter>(null);
                numClones++;
            }
        }
    }

    public final int getCloneSize() {
        int z = 0;
        for (int i = 0; i < clones.length; i++) {
            if (clones[i].get() != null) {
                z++;
            }
        }
        return z;
    }

    public void spawnClones() {
        if (!isGM()) { // removed tetris piece likely, expired or whatever
            numClones = (byte) (stats.hasClone ? 1 : 0);
        }
        for (int i = 0; i < numClones; i++) {
            cloneLook();
        }
        numClones = 0;
    }

    public byte getNumClones() {
        return numClones;
    }

    public void setDragon(MapleDragon d) {
        this.dragon = d;
    }

    public MapleExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(MapleExtractor me) {
        removeExtractor();
        this.extractor = me;
    }

    public void removeExtractor() {
        if (extractor != null) {
            map.broadcastMessage(CField.removeExtractor(this.id));
            map.removeMapObject(extractor, MapleMapObjectType.EXTRACTOR);
            extractor = null;
        }
    }

    public final void spawnSavedPets() {
        for (int i = 0; i < petStore.length; i++) {
            if (petStore[i] > -1) {
                spawnPet(petStore[i], false, false);
            }
        }
        if (GameConstants.GMS) {
            client.announce(PetPacket.petStatUpdate(this));
        }
        petStore = new byte[]{-1, -1, -1};
    }

    public final byte[] getPetStores() {
        return petStore;
    }

    public void resetStats(final int str, final int dex, final int int_, final int luk) {
        Map<MapleStat, Integer> stat = new EnumMap<MapleStat, Integer>(MapleStat.class);
        int total = stats.getStr() + stats.getDex() + stats.getLuk() + stats.getInt() + getRemainingAp();

        total -= str;
        stats.str = str;

        total -= dex;
        stats.dex = dex;

        total -= int_;
        stats.int_ = int_;

        total -= luk;
        stats.luk = luk;

        setRemainingAp(50);
        stats.recalcLocalStats(this);
        stat.put(MapleStat.STR, str);

        stat.put(MapleStat.DEX, dex);

        stat.put(MapleStat.INT, int_);

        stat.put(MapleStat.LUK, luk);

        stat.put(MapleStat.AVAILABLEAP, 50);

        client.announce(CWvsContext.updatePlayerStats(stat, false, this));
    }

    public Event_PyramidSubway getPyramidSubway() {
        return pyramidSubway;
    }

    public void setPyramidSubway(Event_PyramidSubway ps) {
        this.pyramidSubway = ps;
    }

    public byte getSubcategory() {
        if (job >= 430 && job <= 434) {
            return 1; // dont set it
        }
        if (GameConstants.isCannon(job) || job == 1) {
            return 2;
        }
        if (job != 0 && job != 400) {
            return 0;
        }
        return subcategory;
    }

    public void setSubcategory(int z) {
        this.subcategory = (byte) z;
    }

    public int itemQuantity(final int itemid) {
        return getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
    }

    public void setRPS(RockPaperScissors rps) {
        this.rps = rps;
    }

    public RockPaperScissors getRPS() {
        return rps;
    }

    public long getNextConsume() {
        return nextConsume;
    }

    public void setNextConsume(long nc) {
        this.nextConsume = nc;
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public void changeChannel(final int channel) {
        client.getSession().write(CField.serverBlocked(1));
        /*
        
         final ChannelServer toch = ChannelServer.getInstance(channel);
         String[] socket = ChannelServer.getInstance(client.getChannel()).getIP().split(":");

         if (channel == client.getChannel() || toch == null || toch.isShutdown()) {
         client.getSession().write(CField.serverBlocked(1));
         return;
         }

         World.ChannelChange_Data(new CharacterTransfer(this), getId(), channel);
         final String s = client.getSessionIPAddress();
         LoginServer.addIPAuth(s.substring(s.indexOf('/') + 1, s.length()));

         if (getMessenger() != null) {
         World.Messenger.silentLeaveMessenger(getMessenger().getId(), new MapleMessengerCharacter(this));
         }
         changeRemoval();
         PlayerBuffStorage.addBuffsToStorage(getId(), getAllBuffs());
         PlayerBuffStorage.addDiseaseToStorage(getId(), getAllDiseases());
         PlayerBuffStorage.addCooldownsToStorage(getId(), getCooldowns());
         getMap().removePlayer(this);
         getClient().getChannelServer().removePlayer(this);
         saveToDB(false, false);
         client.updateLoginState(MapleClient.CHANGE_CHANNEL, client.getSessionIPAddress());
         try {
         client.announce(CField.getChannelChange(client, InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
         } catch (UnknownHostException | NumberFormatException e) {
         e.printStackTrace();
         }
         client.setPlayer(null);
         client.setReceiving(false);
         */
    }

    public void expandInventory(byte type, int amount) {
        final MapleInventory inv = getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte) amount);
        client.announce(InventoryPacket.getSlotUpdate(type, (byte) inv.getSlotLimit()));
    }

    public boolean allowedToTarget(MapleCharacter other) {
        return other != null && (!other.isHidden() || getGMLevel() >= other.getGMLevel());
    }

    public int getFollowId() {
        return followid;
    }

    public void setFollowId(int fi) {
        this.followid = fi;
        if (fi == 0) {
            this.followinitiator = false;
            this.followon = false;
        }
    }

    public void setFollowInitiator(boolean fi) {
        this.followinitiator = fi;
    }

    public void setFollowOn(boolean fi) {
        this.followon = fi;
    }

    public boolean isFollowOn() {
        return followon;
    }

    public boolean isFollowInitiator() {
        return followinitiator;
    }

    public void checkFollow() {
        if (followid <= 0) {
            return;
        }
        if (followon) {
            map.broadcastMessage(CField.followEffect(id, 0, null));
            map.broadcastMessage(CField.followEffect(followid, 0, null));
        }
        MapleCharacter tt = map.getCharacterById(followid);
        client.announce(CField.getFollowMessage("Follow canceled."));
        if (tt != null) {
            tt.setFollowId(0);
            tt.getClient().announce(CField.getFollowMessage("Follow canceled."));
        }
        setFollowId(0);
    }

    public int getMarriageId() {
        return marriageId;
    }

    public void setMarriageId(final int mi) {
        this.marriageId = mi;
    }

    public int getMarriageItemId() {
        return marriageItemId;
    }

    public void setMarriageItemId(final int mi) {
        this.marriageItemId = mi;
    }

    public boolean isStaff() {
        return this.gmLevel >= 3;
    }

    public boolean isDonator() {
        return this.gmLevel == 1;
    }

    // TODO: gvup, vic, lose, draw, VR
    public boolean startPartyQuest(final int questid) {
        boolean ret = false;
        MapleQuest q = MapleQuest.getInstance(questid);
        if (q == null || !q.isPartyQuest()) {
            return false;
        }
        if (!quests.containsKey(q) || !questinfo.containsKey(questid)) {
            final MapleQuestStatus status = getQuestNAdd(q);
            status.setStatus((byte) 1);
            updateQuest(status);
            switch (questid) {
                case 1300:
                case 1301:
                case 1302: // carnival, ariants.
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0;gvup=0;vic=0;lose=0;draw=0");
                    break;
                case 1303: // ghost pq
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0;vic=0;lose=0");
                    break;
                case 1204: // herb town pq
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;have2=0;have3=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                case 1206: // ellin pq
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                default:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
            }
            ret = true;
        } // started the quest.
        return ret;
    }

    public String getOneInfo(final int questid, final String key) {
        if (!questinfo.containsKey(questid) || key == null || MapleQuest.getInstance(questid) == null
                || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return null;
        }
        final String[] split = questinfo.get(questid).split(";");
        for (String x : split) {
            final String[] split2 = x.split("="); // should be only 2
            if (split2.length == 2 && split2[0].equals(key)) {
                return split2[1];
            }
        }
        return null;
    }

    public void updateOneInfo(final int questid, final String key, final String value) {
        if (!questinfo.containsKey(questid) || key == null || value == null || MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        final String[] split = questinfo.get(questid).split(";");
        boolean changed = false;
        final StringBuilder newQuest = new StringBuilder();
        for (String x : split) {
            final String[] split2 = x.split("="); // should be only 2
            if (split2.length != 2) {
                continue;
            }
            if (split2[0].equals(key)) {
                newQuest.append(key).append("=").append(value);
            } else {
                newQuest.append(x);
            }
            newQuest.append(";");
            changed = true;
        }

        updateInfoQuest(questid, changed ? newQuest.toString().substring(0, newQuest.toString().length() - 1) : newQuest.toString());
    }

    public void recalcPartyQuestRank(final int questid) {
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        if (!startPartyQuest(questid)) {
            final String oldRank = getOneInfo(questid, "rank");
            if (oldRank == null || oldRank.equals("S")) {
                return;
            }
            String newRank = null;
            if (oldRank.equals("A")) {
                newRank = "S";
            } else if (oldRank.equals("B")) {
                newRank = "A";
            } else if (oldRank.equals("C")) {
                newRank = "B";
            } else if (oldRank.equals("D")) {
                newRank = "C";
            } else if (oldRank.equals("F")) {
                newRank = "D";
            } else {
                return;
            }
            final List<Pair<String, Pair<String, Integer>>> questInfo = MapleQuest.getInstance(questid)
                    .getInfoByRank(newRank);
            if (questInfo == null) {
                return;
            }
            for (Pair<String, Pair<String, Integer>> q : questInfo) {
                boolean found = false;
                final String val = getOneInfo(questid, q.right.left);
                if (val == null) {
                    return;
                }
                int vall = 0;
                try {
                    vall = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    return;
                }
                if (q.left.equals("less")) {
                    found = vall < q.right.right;
                } else if (q.left.equals("more")) {
                    found = vall > q.right.right;
                } else if (q.left.equals("equal")) {
                    found = vall == q.right.right;
                }
                if (!found) {
                    return;
                }
            }
            // perfectly safe
            updateOneInfo(questid, "rank", newRank);
        }
    }

    public void tryPartyQuest(final int questid) {
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        try {
            startPartyQuest(questid);
            pqStartTime = System.currentTimeMillis();
            updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(getOneInfo(questid, "try")) + 1));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("tryPartyQuest error");
        }
    }

    public void endPartyQuest(final int questid) {
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        try {
            startPartyQuest(questid);
            if (pqStartTime > 0) {
                final long changeTime = System.currentTimeMillis() - pqStartTime;
                final int mins = (int) (changeTime / 1000 / 60), secs = (int) (changeTime / 1000 % 60);
                final int mins2 = Integer.parseInt(getOneInfo(questid, "min"));
                if (mins2 <= 0 || mins < mins2) {
                    updateOneInfo(questid, "min", String.valueOf(mins));
                    updateOneInfo(questid, "sec", String.valueOf(secs));
                    updateOneInfo(questid, "date", FileoutputUtil.CurrentReadable_Date());
                }
                final int newCmp = Integer.parseInt(getOneInfo(questid, "cmp")) + 1;
                updateOneInfo(questid, "cmp", String.valueOf(newCmp));
                updateOneInfo(questid, "CR", String
                        .valueOf((int) Math.ceil((newCmp * 100.0) / Integer.parseInt(getOneInfo(questid, "try")))));
                recalcPartyQuestRank(questid);
                pqStartTime = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("endPartyQuest error");
        }

    }

    public void havePartyQuest(final int itemId) {
        int questid = 0, index = -1;
        switch (itemId) {
            case 1002798:
                questid = 1200; // henesys
                break;
            case 1072369:
                questid = 1201; // kerning
                break;
            case 1022073:
                questid = 1202; // ludi
                break;
            case 1082232:
                questid = 1203; // orbis
                break;
            case 1002571:
            case 1002572:
            case 1002573:
            case 1002574:
                questid = 1204; // herbtown
                index = itemId - 1002571;
                break;
            case 1102226:
                questid = 1303; // ghost
                break;
            case 1102227:
                questid = 1303; // ghost
                index = 0;
                break;
            case 1122010:
                questid = 1205; // magatia
                break;
            case 1032061:
            case 1032060:
                questid = 1206; // ellin
                index = itemId - 1032060;
                break;
            case 3010018:
                questid = 1300; // ariant
                break;
            case 1122007:
                questid = 1301; // carnival
                break;
            case 1122058:
                questid = 1302; // carnival2
                break;
            default:
                return;
        }
        if (MapleQuest.getInstance(questid) == null || !MapleQuest.getInstance(questid).isPartyQuest()) {
            return;
        }
        startPartyQuest(questid);
        updateOneInfo(questid, "have" + (index == -1 ? "" : index), "1");
    }

    public void resetStatsByJob(boolean beginnerJob) {
        int baseJob = (beginnerJob ? (job % 1000) : (((job % 1000) / 100) * 100)); // 1112 -> 112 -> 1 -> 100
        boolean UA = getQuestNoAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER)) != null;
        if (baseJob == 100) { // first job = warrior
            resetStats(UA ? 4 : 35, 4, 4, 4);
        } else if (baseJob == 200) {
            resetStats(4, 4, UA ? 4 : 20, 4);
        } else if (baseJob == 300 || baseJob == 400) {
            resetStats(4, UA ? 4 : 25, 4, 4);
        } else if (baseJob == 500) {
            resetStats(4, UA ? 4 : 20, 4, 4);
        } else if (baseJob == 0) {
            resetStats(4, 4, 4, 4);
        }
    }

    public boolean hasSummon() {
        return hasSummon;
    }

    public void setHasSummon(boolean summ) {
        this.hasSummon = summ;
        client.announce(CField.UIPacket.summonHelper(summ));
    }

    public void removeDoor() {
        final MapleDoor door = getDoors().iterator().next();
        for (MapleDoor dr : door.getTarget().getAllDoors()) {
            if (dr.getOwner() == this) {
                for (final MapleCharacter chr : dr.getTarget().getAllPlayers()) {
                    dr.sendDestroyData(chr.getClient());
                }
                dr.getTarget().removeMapObject(dr, MapleMapObjectType.DOOR);
            }
        }
        for (MapleDoor tdr : door.getTown().getAllDoors()) {
            if (tdr.getOwner() == this) {
                for (final MapleCharacter chr : tdr.getTown().getAllPlayers()) {
                    tdr.sendDestroyData(chr.getClient());
                }
                tdr.getTarget().removeMapObject(tdr, MapleMapObjectType.DOOR);
            }
        }
        clearDoors();
    }

    public void removeMechDoor() {
        for (final MechDoor destroyDoor : getMechDoors()) {
            getMap().removeMapObject(destroyDoor, MapleMapObjectType.DOOR);
        }
        clearMechDoors();
    }

    public void changeRemoval() {
        changeRemoval(false);
    }

    public void changeRemoval(boolean dc) {
        removeFamiliar();
        dispelSummons();
        if (!dc) {
            cancelEffectFromBuffStat(MapleBuffStat.SOARING);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
            cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
        }
        if (getPyramidSubway() != null) {
            getPyramidSubway().dispose(this);
        }
        if (playerShop != null && !dc) {
            playerShop.removeVisitor(this, 3);
            if (playerShop.isOwner(this)) {
                playerShop.setOpen(true);
            }
        }
        if (!getDoors().isEmpty()) {
            removeDoor();
        }
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        disposeClones();
        NPCScriptManager.getInstance().dispose(client);
        cancelFairySchedule(false);
    }

    public void updateTick(int newTick) {

    }

    public boolean canUseFamilyBuff(MapleFamilyBuff buff) {
        final MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(buff.questID));
        if (stat == null) {
            return true;
        }
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Long.parseLong(stat.getCustomData()) + (24 * 3600000) < System.currentTimeMillis();
    }

    public void useFamilyBuff(MapleFamilyBuff buff) {
        final MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(buff.questID));
        stat.setCustomData(String.valueOf(System.currentTimeMillis()));
    }

    public List<Integer> usedBuffs() {
        // assume count = 1
        List<Integer> used = new ArrayList<Integer>();
        MapleFamilyBuff[] z = MapleFamilyBuff.values();
        for (int i = 0; i < z.length; i++) {
            if (!canUseFamilyBuff(z[i])) {
                used.add(i);
            }
        }
        return used;
    }

    public String getTeleportName() {
        return teleportname;
    }

    public void setTeleportName(final String tname) {
        teleportname = tname;
    }

    public int getNoJuniors() {
        if (mfc == null) {
            return 0;
        }
        return mfc.getNoJuniors();
    }

    public MapleFamilyCharacter getMFC() {
        return mfc;
    }

    public void makeMFC(final int familyid, final int seniorid, final int junior1, final int junior2) {
        if (familyid > 0) {
            MapleFamily f = World.Family.getFamily(familyid);
            if (f == null) {
                mfc = null;
            } else {
                mfc = f.getMFC(id);
                if (mfc == null) {
                    mfc = f.addFamilyMemberInfo(this, seniorid, junior1, junior2);
                }
                if (mfc.getSeniorId() != seniorid) {
                    mfc.setSeniorId(seniorid);
                }
                if (mfc.getJunior1() != junior1) {
                    mfc.setJunior1(junior1);
                }
                if (mfc.getJunior2() != junior2) {
                    mfc.setJunior2(junior2);
                }
            }
        } else {
            mfc = null;
        }
    }

    public void setFamily(final int newf, final int news, final int newj1, final int newj2) {
        if (mfc == null || newf != mfc.getFamilyId() || news != mfc.getSeniorId() || newj1 != mfc.getJunior1()
                || newj2 != mfc.getJunior2()) {
            makeMFC(newf, news, newj1, newj2);
        }
    }

    public int maxBattleshipHP(int skillid) {
        return (getTotalSkillLevel(skillid) * 5000) + ((getLevel() - 120) * 3000);
    }

    public int currentBattleshipHP() {
        return battleshipHP;
    }

    public void setBattleshipHP(int v) {
        this.battleshipHP = v;
    }

    public void decreaseBattleshipHP() {
        this.battleshipHP--;
    }

    public int getGachExp() {
        return gachexp;
    }

    public void setGachExp(int ge) {
        this.gachexp = ge;
    }

    public boolean isInBlockedMap() {
        if (getMapId() == GameConstants.JAIL) {
            return true;
        }
        if (!isAlive() || getPyramidSubway() != null || getMap().getSquadByMap() != null || getEventInstance() != null || getMap().getEMByMap() != null) {
            return true;
        }
        if ((getMapId() >= 680000210 && getMapId() <= 680000502) || (getMapId() / 10000 == 92502 && getMapId() >= 925020100) || (getMapId() / 10000 == 92503)) {
            return true;
        }
        for (int i : GameConstants.blockedMaps) {
            if (getMapId() == i) {
                return true;
            }
        }
        return false;
    }

    public boolean isInTownMap() {
        if (hasBlockedInventory() || !getMap().isTown() || FieldLimitType.VipRock.check(getMap().getFieldLimit()) || getEventInstance() != null) {
            return false;
        }
        for (int i : GameConstants.blockedMaps) {
            if (getMapId() == i) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBlockedInventory() {
        return !isAlive() || getTrade() != null || getDirection() >= 0 || getPlayerShop() != null || map == null || isStorageOpened();
    }

    public void startPartySearch(final List<Integer> jobs, final int maxLevel, final int minLevel, final int membersNeeded) {
        for (MapleCharacter chr : map.getCharacters()) {
            if (chr.getId() != id && chr.getParty() == null && chr.getLevel() >= minLevel && chr.getLevel() <= maxLevel
                    && (jobs.isEmpty() || jobs.contains(Integer.valueOf(chr.getJob()))) && (isGM() || !chr.isGM())) {
                if (party != null && party.getMembers().size() < GameConstants.getPartySize()
                        && party.getMembers().size() < membersNeeded) {
                    chr.setParty(party);
                    World.Party.updateParty(party.getId(), PartyOperation.JOIN, new MaplePartyCharacter(chr));
                    chr.receivePartyMemberHP();
                    chr.updatePartyMemberHP();
                } else {
                    break;
                }
            }
        }
    }

    public void changedBattler() {
        changed_pokemon = true;
    }

    public int getChallenge() {
        return challenge;
    }

    public void setChallenge(int c) {
        this.challenge = c;
    }

    public short getFatigue() {
        return fatigue;
    }

    public void setFatigue(int j) {
        this.fatigue = (short) Math.max(0, j);
        updateSingleStat(MapleStat.FATIGUE, this.fatigue);
    }

    public void fakeRelog() {
        isChangingMaps(true);
        client.announce(CField.getCharInfo(this));
        final MapleMap mapp = getMap();
        mapp.setCheckStates(false);
        mapp.removePlayer(this);
        mapp.addPlayer(this, false);
        mapp.setCheckStates(true);
        client.announce(CWvsContext.getFamiliarInfo(this));
        isChangingMaps(false);
    }

    public boolean canSummon() {
        return canSummon(5000);
    }

    public boolean canSummon(int g) {
        if (lastSummonTime + g < System.currentTimeMillis()) {
            lastSummonTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public int getIntNoRecord(int questID) {
        final MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(questID));
        if (stat == null || stat.getCustomData() == null) {
            return 0;
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public int getIntRecord(int questID) {
        final MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public void updatePetAuto() {
        if (getIntNoRecord(GameConstants.HP_ITEM) > 0) {
            client.announce(CField.petAutoHP(getIntRecord(GameConstants.HP_ITEM)));
        }
        if (getIntNoRecord(GameConstants.MP_ITEM) > 0) {
            client.announce(CField.petAutoMP(getIntRecord(GameConstants.MP_ITEM)));
        }
    }

    public void sendEnglishQuiz(String msg) {
        // client.announce(CField.englishQuizMsg(msg));
    }

    public void setChangeTime() {
        mapChangeTime = System.currentTimeMillis();
    }

    public long getChangeTime() {
        return mapChangeTime;
    }

    public short getScrolledPosition() {
        return scrolledPosition;
    }

    public void setScrolledPosition(short s) {
        this.scrolledPosition = s;
    }

    public MapleTrait getTrait(MapleTraitType t) {
        return traits.get(t);
    }

    public void forceCompleteQuest(int id) {
        MapleQuest.getInstance(id).forceComplete(this, 9270035); // troll
    }

    public List<Integer> getExtendedSlots() {
        return extendedSlots;
    }

    public int getExtendedSlot(int index) {
        if (extendedSlots.size() <= index || index < 0) {
            return -1;
        }
        return extendedSlots.get(index);
    }

    public void changedExtended() {
        changed_extendedSlots = true;
    }

    public MapleAndroid getAndroid() {
        return android;
    }

    public boolean changeLookAndriod() {
        if (android != null) {
            android.changeLook();
            map.updateAndroid(this);
            return true;
        }
        return false;
    }

    public void removeAndroid() {
        if (android != null) {
            android.saveToDb();
        }
        if (map != null) {
            map.broadcastMessage(CField.deactivateAndroid(this.id));
        }
        android = null;
    }

    public void setAndroid(MapleAndroid a) {
        this.android = a;
        if (map != null && a != null && !hideAndroid) {
            map.broadcastMessage(CField.spawnAndroid(this, a, true));
            //map.broadcastMessage(CField.showAndroidEmotion(this.getId(), Randomizer.nextInt(17) + 1));
        }
    }

    public List<Item> getRebuy() {
        return rebuy;
    }

    public Map<Integer, MonsterFamiliar> getFamiliars() {
        return familiars;
    }

    public MonsterFamiliar getSummonedFamiliar() {
        return summonedFamiliar;
    }

    public void removeFamiliar() {
        if (summonedFamiliar != null && map != null) {
            removeVisibleFamiliar();
        }
        summonedFamiliar = null;
    }

    public void removeVisibleFamiliar() {
        getMap().removeMapObject(summonedFamiliar, MapleMapObjectType.FAMILIAR);
        removeVisibleMapObject(summonedFamiliar);
        getMap().broadcastMessage(CField.removeFamiliar(this.getId()));
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        cancelEffect(ii.getItemEffect(ii.getFamiliar(summonedFamiliar.getFamiliar()).passive), false,
                System.currentTimeMillis());
    }

    public void spawnFamiliar(MonsterFamiliar mf) {
        summonedFamiliar = mf;

        mf.setStance(0);
        mf.setPosition(getPosition());
        mf.setFh(getFH());
        addVisibleMapObject(mf);
        getMap().spawnFamiliar(mf);

        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleStatEffect eff = ii.getItemEffect(ii.getFamiliar(summonedFamiliar.getFamiliar()).passive);
        if (eff != null && eff.getInterval() <= 0 && eff.makeChanceResult()) { // i think this is actually done through
            // a recv, which is ATTACK_FAMILIAR +1
            eff.applyTo(this);
        }
        lastFamiliarEffectTime = System.currentTimeMillis();
    }

    public final boolean canFamiliarEffect(long now, MapleStatEffect eff) {
        return lastFamiliarEffectTime > 0 && lastFamiliarEffectTime + eff.getInterval() < now;
    }

    public void doFamiliarSchedule(long now) {
        if (familiars == null) {
            return;
        }
        for (MonsterFamiliar mf : familiars.values()) {
            if (summonedFamiliar != null && summonedFamiliar.getId() == mf.getId()) {
                mf.addFatigue(this, 5);
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final MapleStatEffect eff = ii.getItemEffect(ii.getFamiliar(summonedFamiliar.getFamiliar()).passive);
                if (eff != null && eff.getInterval() > 0 && canFamiliarEffect(now, eff) && eff.makeChanceResult()) {
                    eff.applyTo(this);
                }
            } else if (mf.getFatigue() > 0) {
                mf.setFatigue(Math.max(0, mf.getFatigue() - 5));
            }
        }
    }

    public MapleImp[] getImps() {
        return imps;
    }

    public void sendImp() {
        for (int i = 0; i < imps.length; i++) {
            if (imps[i] != null) {
                client.announce(CWvsContext.updateImp(imps[i], ImpFlag.SUMMONED.getValue(), i, true));
            }
        }
    }

    public int getBattlePoints() {
        return this.stats.getStarForce();
    }

    public void setBattlePoints(int p) {
        // client.announce(InfoPacket.getBPMsg(p));
        updateSingleStat(MapleStat.BATTLE_POINTS, p);
    }

    public int getBattleExp() {
        return pvpExp;
    }

    public void setBattleExp(int value) {
        pvpExp = value;
    }

    public void addBattleExp(int gain) {
        pvpExp += gain;
        updateSingleStat(MapleStat.BATTLE_EXP, pvpExp);
        switch (pvpExp) {
            case 10000:
                finishAchievement(101);
                break;
            case 25000:
                finishAchievement(102);
                break;
            case 50000:
                finishAchievement(103);
                break;
            case 100000:
                finishAchievement(104);
                break;
            case 250000:
                finishAchievement(105);
                break;
            case 500000:
                finishAchievement(106);
                break;
            case 1000000:
                finishAchievement(107);
                break;
            case 2500000:
                finishAchievement(108);
                break;
            case 5000000:
                finishAchievement(109);
                break;
            case 10000000:
                finishAchievement(110);
                break;
            case 25000000:
                finishAchievement(111);
                break;
            case 50000000:
                finishAchievement(112);
                break;
            case 99999999:
                finishAchievement(113);
                break;
            case 125000000:
                finishAchievement(599);
                break;
            case 150000000:
                finishAchievement(600);
                break;
            case 175000000:
                finishAchievement(601);
                break;
            case 200000000:
                finishAchievement(602);
                break;
        }
        client.announce(CField.customStatDetail_Kill(pvpExp));

        /*
         * client.announce(CWvsContext.getMidMsg("Gained Battle Exp +" + gain + ": " +
         * pvpExp + "/" + GameConstants.getPVPExpNeededForLevel(pvpLvl) + "  ", false,
         * 1)); while (pvpExp >= GameConstants.getPVPExpNeededForLevel(pvpLvl)) {
         * //pvpExp = 0; if (pvpLvl < 99) { pvpLvl += 1;
         * client.announce(CField.startMapEffect("Your Battle Rank has leveled up!!!",
         * 5120000, true)); updateSingleStat(MapleStat.BATTLE_RANK, pvpLvl); break; } }
         */
    }

    public int getBattleLevel() {
        return pvpLvl;
    }

    public void changeTeam(int newTeam) {
        this.coconutteam = newTeam;

        if (inPVP()) {
            client.announce(CField.getPVPTransform(newTeam + 1));
            map.broadcastMessage(CField.changeTeam(id, newTeam + 1));
        } else {
            client.announce(CField.showEquipEffect(newTeam));
        }
    }

    public void disease(int type, int level) {
        if (MapleDisease.getBySkill(type) == null) {
            return;
        }
        chair = 0;
        client.announce(CField.cancelChair(-1));
        map.broadcastMessage(this, CField.showChair(id, 0), false);
        giveDebuff(MapleDisease.getBySkill(type), MobSkillFactory.getMobSkill(type, level));
    }

    public boolean inPVP() {
        return eventInstance != null && eventInstance.getName().startsWith("PVP");
    }

    public void clearAllCooldowns() {
        for (MapleCoolDownValueHolder m : getCooldowns()) {
            final int skil = m.skillId;
            removeCooldown(skil);
            client.announce(CField.skillCooldown(skil, 0));
        }
    }

    public Pair<Double, Boolean> modifyDamageTaken(double damage, MapleMapObject attacke) {
        Pair<Double, Boolean> ret = new Pair<Double, Boolean>(damage, false);
        if (damage <= 0) {
            return ret;
        }
        if (stats.ignoreDAMr > 0 && Randomizer.nextInt(100) < stats.ignoreDAMr_rate) {
            damage -= Math.floor((stats.ignoreDAMr * damage) / 100.0f);
        }
        if (stats.ignoreDAM > 0 && Randomizer.nextInt(100) < stats.ignoreDAM_rate) {
            damage -= stats.ignoreDAM;
        }
        final Integer div = getBuffedValue(MapleBuffStat.DIVINE_SHIELD);
        final Integer div2 = getBuffedValue(MapleBuffStat.HOLY_MAGIC_SHELL);
        if (div2 != null) {
            if (div2 <= 0) {
                cancelEffectFromBuffStat(MapleBuffStat.HOLY_MAGIC_SHELL);
            } else {
                setBuffedValue(MapleBuffStat.HOLY_MAGIC_SHELL, div2 - 1);
                damage = 0;
            }
        } else if (div != null) {
            if (div <= 0) {
                cancelEffectFromBuffStat(MapleBuffStat.DIVINE_SHIELD);
            } else {
                setBuffedValue(MapleBuffStat.DIVINE_SHIELD, div - 1);
                damage = 0;
            }
        }
        MapleStatEffect barrier = getStatForBuff(MapleBuffStat.COMBO_BARRIER);
        if (barrier != null) {
            damage = ((barrier.getX() / 1000.0) * damage);
        }
        barrier = getStatForBuff(MapleBuffStat.MAGIC_SHIELD);
        if (barrier != null) {
            damage = ((barrier.getX() / 1000.0) * damage);
        }
        barrier = getStatForBuff(MapleBuffStat.WATER_SHIELD);
        if (barrier != null) {
            damage = ((barrier.getX() / 1000.0) * damage);
        }
        List<Integer> attack = attacke instanceof MapleMonster || attacke == null ? null : (new ArrayList<Integer>());
        if (attack != null && attack.size() > 0 && attacke != null) {
            getMap().broadcastMessage(CField.pvpCool(attacke.getObjectId(), attack));
        }
        ret.left = damage;
        return ret;
    }

    public void onAttack(AttackInfo attack) {
        if (attack != null) {
            if (stats.getHp() < stats.getCurrentMaxHp()) {
                if (stats.hpRecoverProp > 0) {
                    if (Randomizer.nextInt(100) <= stats.hpRecoverProp) {// i think its out of 100, anyway
                        if (stats.hpRecover > 0) {
                            healHP(stats.hpRecover);
                        }
                        if (stats.hpRecoverPercent > 0) {
                            int drain = Randomizer.Max((int) (stats.getCurrentMaxHp() * ((double) stats.hpRecoverPercent / 400.0)), stats.getCurrentMaxHp());
                            healHP(drain);
                        }
                    }
                }
                if (getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                    int drain = Randomizer.Max((int) (stats.getMaxHp() * ((double) getStatForBuff(MapleBuffStat.COMBO_DRAIN).getX() / 100.0)), stats.getMaxHp());
                    healAll(drain);
                }
            }
            if (stats.getMp() < stats.getCurrentMaxMp(getJob())) {
                if (stats.mpRecoverProp > 0 && !GameConstants.isDemon(getJob())) {
                    if (Randomizer.nextInt(100) <= stats.mpRecoverProp) {// i think its out of 100, anyway
                        healMP(stats.mpRecover);
                    }
                }
            }
            // effects
            if (attack.skill > 0) {
                switch (attack.skill) {
                    case 3111008://drain arrow
                    case 4101005:// drain
                    case 5111004:// Energy Drain
                    case 14101006:
                    case 15111001:
                    case 31111003:
                        if (stats.getHp() < stats.getMaxHp()) {
                            healHP((int) (stats.getMaxHp() * 0.1));
                        }
                        break;
                }
            }
        }
    }

    public void setJag() {
        getQuestNAdd(MapleQuest.getInstance(GameConstants.JAGUAR)).setCustomData(String.valueOf(90));
        getClient().announce(CWvsContext.updateJaguar(this));
    }

    public void handleForceGain(int oid, int skillid) {
        handleForceGain(oid, skillid, 0);
    }

    public void handleForceGain(int oid, int skillid, int extraForce) {
        if (!GameConstants.isForceIncrease(skillid) && extraForce <= 0) {
            return;
        }
        int forceGain = 5;
        int color = 1;
        if (getLevel() >= 30 && getLevel() < 70) {
            forceGain = 10;
            color = 3;
        } else if (getLevel() >= 70 && getLevel() < 120) {
            forceGain = 15;
            color = 6;
        } else if (getLevel() >= 120) {
            forceGain = 20;
            color = 10;
        }
        if (force > 1000) {
            force = 0;
        }
        force++; // counter
        addMP(extraForce > 0 ? extraForce : forceGain, true);
        if (getEffects()) {
            getClient().announce(CField.gainForce(oid, force, color, getEffects()));
        }
    }

    public void afterAttack(int mobCount, int attackCount, int skillid) {
        switch (getJob()) {
            case 511:
            case 512: {
                handleEnergyCharge(5110001, mobCount * attackCount);
                break;
            }
            case 1510:
            case 1511:
            case 1512: {
                handleEnergyCharge(15100004, mobCount * attackCount);
                break;
            }
            case 111:
            case 112:
            case 1111:
            case 1112:
                if (getBuffedValue(MapleBuffStat.COMBO) != null) { // shout should not give orbs
                    handleOrbgain();
                }
                break;
        }
        if (!isIntern()) {
            cancelEffectFromBuffStat(MapleBuffStat.WIND_WALK);
            cancelEffectFromBuffStat(MapleBuffStat.INFILTRATE);
            final MapleStatEffect ds = getStatForBuff(MapleBuffStat.DARKSIGHT);
            if (ds != null) {
                if (ds.getSourceId() != 4330001 || !ds.makeChanceResult()) {
                    cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
                }
            }
        }
    }

    public void applyIceGage(int x) {
        updateSingleStat(MapleStat.ICE_GAGE, x);
    }

    public Rectangle getBounds() {
        return new Rectangle(getPosition().x - 25, getPosition().y - 75, 50, 75);
    }

    @Override
    public final Map<Byte, Integer> getEquips() {
        final Map<Byte, Integer> eq = new HashMap<>();
        for (final Item item : inventory[MapleInventoryType.EQUIPPED.ordinal()].newList()) {
            eq.put((byte) item.getPosition(), item.getItemId());
        }
        return eq;
    }

    private transient PlayerRandomStream CRand;

    public final PlayerRandomStream CRand() {
        return CRand;
    }

    public int getAPS() {
        return apstorage;
    }

    public void gainAPS(int aps) {
        apstorage += aps;
    }

    public int getFullTotalLevel() {
        return totallevel + getLevelDataLvl(200);
    }

    public int getTotalLevel() {
        return totallevel;
    }

    public void setTotalLevel(int value) {
        totallevel = value;
    }

    public BigInteger getOverExp() {
        return overexp;
    }

    public int getOverExpPerc() {
        return new BigDecimal(overexp).divide(getXpLvl, 4, RoundingMode.HALF_UP).movePointRight(2).intValue();
    }

    public long getOverExpPercValue(long value) {
        return getXpLvl.multiply(BigDecimal.valueOf(0.01 * value)).longValue();
    }

    public void setCoolDown(long value) {
        cooldown = value;
    }

    public long getCoolDown() {
        return cooldown;
    }

    public void setCommandCoolDown(long value) {
        Commandcooldown = value;
    }

    public long getCommandCoolDown() {
        return Commandcooldown;
    }

    public void respawn() {
        changeMap(getMap());
    }

    public void resetCore() {
        setVar("Main", Randomizer.random(0, 3));
        setVar("Sub", Randomizer.random(0, 3));
        setVar("Attack", Randomizer.random(0, 1));
        getStat().recalcLocalStats(this);
    }

    public long getLimit() {
        long bstats = 0;
        switch ((int) getVarZero("Main")) {
            case 0 ->
                bstats += ((long) getStat().getTStr());
            case 1 ->
                bstats += ((long) getStat().getTDex());
            case 2 ->
                bstats += ((long) getStat().getTInt());
            case 3 ->
                bstats += ((long) getStat().getTLuk());
        }
        switch ((int) getVarZero("Attack")) {
            case 0 ->
                bstats += ((long) getStat().getTatk());
            case 1 ->
                bstats += ((long) getStat().getTmatk());
        }
        return bstats;
    }

    public long getPrimary() {
        long bstats = 0;
        switch ((int) getVarZero("Main")) {
            case 0 ->
                bstats += ((long) getStat().getTStr());
            case 1 ->
                bstats += ((long) getStat().getTDex());
            case 2 ->
                bstats += ((long) getStat().getTInt());
            case 3 ->
                bstats += ((long) getStat().getTLuk());
        }
        return Randomizer.LongMin(bstats, 0);
    }

    public long getSecondary() {
        long bstats = 0;
        switch ((int) getVarZero("Sub")) {
            case 0 ->
                bstats += ((long) getStat().getTStr() * 0.1);
            case 1 ->
                bstats += ((long) getStat().getTDex() * 0.1);
            case 2 ->
                bstats += ((long) getStat().getTInt() * 0.1);
            case 3 ->
                bstats += ((long) getStat().getTLuk() * 0.1);
        }
        return 0;
    }

    public long getAtk() {
        long bstats = 0;
        switch ((int) getVarZero("Attack")) {
            case 0 ->
                bstats += ((long) getStat().getTatk());
            case 1 ->
                bstats += ((long) getStat().getTmatk());
        }
        return Randomizer.LongMin(bstats, 0);
    }

    public double getOPStat() {
        double bstats = 0;
        switch ((int) getVarZero("Main")) {
            case 0 ->
                bstats += ((long) getStat().getTStr());
            case 1 ->
                bstats += ((long) getStat().getTDex());
            case 2 ->
                bstats += ((long) getStat().getTInt());
            case 3 ->
                bstats += ((long) getStat().getTLuk());
        }
        switch ((int) getVarZero("Attack")) {
            case 0 ->
                bstats += ((long) (getStat().getTatk()));
            case 1 ->
                bstats += ((long) (getStat().getTmatk()));
        }
        return Math.floor(Randomizer.DoubleMin(bstats, 1));
    }

    public long getOpDamage() {
        long total = (long) (getOPStat());
        return total;
    }

    public double totalMasteryBonus() {
        return getPQBonus();
    }

    public long getStarForce() {
        return getStat().getStarForce();
    }

    public double getPQBonus() {
        double total = dojo + (getLevelDataLvl(getWeaponType()));
        for (int type : level_data.keySet()) {
            if (type >= 100) {
                total += getLevelDataPerc(type);
            }
        }
        return 1.0 + (total * 0.01);
    }

    public boolean isGroup() {
        return (this.getRaid() != null) || (this.getParty() != null);
    }

    public int getGroupSize() {
        if (this.getRaid() != null) {
            return this.getRaid().getMembers().size();
        }
        if (this.getParty() != null) {
            return this.getPartySize();
        }
        return 1;
    }

    public boolean isParty() {
        return this.getParty() != null;
    }

    public boolean isRaid() {
        return this.getRaid() != null;
    }

    public boolean isLeader() {
        if (this.getRaid() != null) {
            if (raid.getLeader() == this) {
                return true;
            }
        }
        if (this.getParty() != null) {
            if (party.getLeaderId() == getId()) {
                return true;
            }
        }
        if (this.getRaid() == null && this.getParty() == null) {
            return true;
        }
        return false;
    }

    public int getPartySize() {
        return party.getMembers().size();
    }

    public MapleMap getWarpMap(int map) {
        MapleMap warpMap;
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            warpMap = eim.getMapInstance(map);
        } else {
            warpMap = client.getChannelServer().getMapFactory().getMap(map);
        }
        return warpMap;
    }

    private void eventChangedMap(int map) {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            eim.changedMap(this, map);
        }
    }

    private void eventAfterChangedMap(int map) {
        EventInstanceManager eim = getEventInstance();
        if (eim != null) {
            eim.afterChangedMap(this, map);
        }
    }

    private void eventAfterChangedMap(MapleMap map) {
        EventInstanceManager eim = map.getEventInstance();
        if (eim != null) {
            eim.afterChangedMap(this, map.getId());
        }
    }

    public List<MapleCharacter> getPartyMembersOnSameMap() {
        List<MapleCharacter> list = new LinkedList<>();
        int thisMapHash = this.getMap().hashCode();

        prtLock.lock();
        try {
            if (party != null) {
                for (MaplePartyCharacter mpc : party.getMembers()) {
                    if (mpc != null && mpc.isOnline()) {
                        MapleCharacter chr = mpc.getPlayer();
                        if (chr != null && chr.isAlive()) {
                            MapleMap chrMap = chr.getMap();
                            if (chrMap != null && chrMap.hashCode() == thisMapHash) {
                                list.add(chr);
                            }
                        }
                    }
                }
            }
            if (raid != null) {
                for (MapleCharacter chr : raid.getMembers()) {
                    if (chr != null && chr.isAlive()) {
                        MapleMap chrMap = chr.getMap();
                        if (chrMap != null && chrMap.hashCode() == thisMapHash) {
                            list.add(chr);
                        }
                    }
                }
            }
        } finally {
            prtLock.unlock();
        }

        return list;
    }

    public void saveOverflow() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM playeritems WHERE charid = ?")) {
                ps.setInt(1, accountid);
                ps.executeUpdate();
                ps.close();
            }
            if (!etc.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO playeritems (charid, itemid, amount, name) VALUES (?, ?, ?, ?)")) {
                    for (int itemid : etc.keySet()) {
                        ps.setInt(1, accountid);
                        ps.setInt(2, itemid);
                        ps.setLong(3, etc.get(itemid).get());
                        ps.setString(4, MapleItemInformationProvider.getInstance().getName(itemid));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    ps.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateOverflow(int itemid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM playeritems WHERE charid = ? and itemid = ?")) {
                ps2.setInt(1, accountid);
                ps2.setInt(2, itemid);
                ps2.executeUpdate();
                ps2.close();
            }
            if (etc.containsKey(itemid)) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO playeritems (charid, itemid, amount, name) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, accountid);
                    ps.setInt(2, itemid);
                    ps.setLong(3, etc.get(itemid).get());
                    ps.setString(4, MapleItemInformationProvider.getInstance().getName(itemid));
                    ps.executeUpdate();
                    ps.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkOverFlow() {
        int types = etc.size();
        if (types >= 25) {
            finishAchievement(186);
        }
        if (types >= 50) {
            finishAchievement(187);
        }
        if (types >= 100) {
            finishAchievement(188);
        }
        if (types >= 250) {
            finishAchievement(189);
        }
        if (types >= 500) {
            finishAchievement(190);
        }
        if (types >= 750) {
            finishAchievement(191);
        }
        if (totalitems >= 1000L) {
            finishAchievement(192);
        }
        if (totalitems >= 10000L) {
            finishAchievement(193);
        }
        if (totalitems >= 100000L) {
            finishAchievement(194);
        }
        if (totalitems >= 1000000L) {
            finishAchievement(195);
        }
        if (totalitems >= 10000000L) {
            finishAchievement(196);
        }
        if (totalitems >= 100000000L) {
            finishAchievement(197);
        }
        if (totalitems >= 1000000000L) {
            finishAchievement(198);
        }
        if (totalitems >= 1000000000000L) {
            finishAchievement(199);
        }
    }

    public void loadOverflow() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `playeritems` WHERE `charid` = ?")) {
                ps.setInt(1, accountid);
                try (ResultSet rs = ps.executeQuery();) {
                    while (rs.next()) {
                        long amount = rs.getLong("amount");
                        etc.put(rs.getInt("itemid"), new AtomicLong(amount));
                        totalitems += amount;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        checkOverFlow();
    }

    public boolean hasEtc(int id, int amount) {
        overflowLock.lock();
        try {
            if (etc.containsKey(id)) {
                return etc.get(id).get() >= amount;
            }
        } finally {
            overflowLock.unlock();
        }
        return false;
    }

    public void addOverflow(int itemid, int quantity) {
        addOverflow(itemid, quantity, "");
    }

    public void addOverflow(int itemid, int quantity, String log) {
        if (getTrade() != null) {
            dropMessage(6, "You are currently in a trade with someone.");
            return;
        }
        if (isStorageOpened()) {
            dropMessage(6, "You are currently in storage system right now.");
            return;
        }
        if (!isChangingMaps()) {
            if (quantity > 0) {
                EtcLock = true;
                //Item item = new Item(itemid, (byte) 0, (short) (quantity), (byte) 0);
                if (GameConstants.getInventoryType(itemid) == MapleInventoryType.USE || GameConstants.getInventoryType(itemid) == MapleInventoryType.ETC) {
                    overflowLock.lock();
                    try {
                        etc.computeIfAbsent(itemid, k -> new AtomicLong()).addAndGet(quantity);
                        dropMessage(-1, "+" + quantity + " " + MapleItemInformationProvider.getInstance().getName(itemid) + " sent to Overflow (Total: " + StringUtil.getUnitNumber(etc.get(itemid).get()) + ")");
                    } finally {
                        overflowLock.unlock();
                    }
                    totalitems += quantity;
                    checkOverFlow();
                }
                EtcLock = false;
            } else {
                System.out.println("[" + Calendar.getInstance().getTime() + "] - " + getName() + " tried to store " + quantity + " itemid " + itemid + "?");
                kick();
            }
        }
    }

    public boolean storeEtc() {
        if (getTrade() != null) {
            dropMessage(6, "You are currently in a trade with someone.");
            return false;
        }
        if (isStorageOpened()) {
            dropMessage(6, "You are currently in storage system right now.");
            return false;
        }
        if (!isChangingMaps()) {
            Collection<Item> items = getInventory(MapleInventoryType.ETC).sortedList();
            if (!items.isEmpty()) {
                EtcLock = true;
                overflowLock.lock();
                try {
                    for (Item item : items) {
                        int itemid = item.getItemId();
                        if (canStoreItem(item)) {
                            short quantity = item.getQuantity();
                            if (MapleInventoryManipulator.removeFromSlot(client, GameConstants.getInventoryType(itemid), item.getPosition(), quantity, false, false)) {
                                etc.computeIfAbsent(itemid, k -> new AtomicLong()).addAndGet(quantity);
                                totalitems += quantity;
                            }
                        }
                    }
                    checkOverFlow();
                } finally {
                    overflowLock.unlock();
                }
                items.clear();
                dropMessage(1, "ETC items have been stored in overflow. Character is saved.");
                saveToDB();
                EtcLock = false;
                return true;
            }
        }
        return false;
    }

    public boolean canStore(int id) {
        if (getTrade() != null || isStorageOpened()) {
            return false;
        }
        if (blackItemz.contains(id)) {
            return false;
        }
        return true;
    }

    public boolean canStoreItem(Item item) {
        if (getTrade() != null || isStorageOpened()) {
            return false;
        }
        if (blackItemz.contains(item.getItemId())) {
            return false;
        }
        if (item.getExpiration() > 0) {
            return false;
        }
        return true;
    }

    public boolean isTimeLimited(Item item) {
        if (item.getExpiration() > 0) {
            return false;
        }
        return true;
    }

    public void loadBlackList() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `player_blacklist` WHERE `charid` = ?")) {
                ps.setInt(1, accountid);
                try (ResultSet rs = ps.executeQuery();) {
                    while (rs.next()) {
                        addBlackList(rs.getInt("itemid"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBlackList() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM player_blacklist WHERE charid = ?")) {
                ps.setInt(1, accountid);
                ps.executeUpdate();
                ps.close();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO player_blacklist (charid, itemid) VALUES (?, ?)")) {
                for (Integer itemid : blackItemz) {
                    ps.setInt(1, accountid);
                    ps.setInt(2, itemid);
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBlackList(int id) {
        if (!blackItemz.contains(id)) {
            dropMessage(-1, MapleItemInformationProvider.getInstance().getName(id) + " added to Black List");
            blackItemz.add(id);
        }
    }

    public void clearBlackList() {
        blackItemz.clear();
    }

    public void removeBlackList(int id) {
        List<Integer> itemz = new ArrayList<>();
        dropMessage(-1, MapleItemInformationProvider.getInstance().getName(id) + " removed from Black List");
        for (Integer ids : blackItemz) {
            if (id != ids) {
                itemz.add(ids);
            }
        }
        blackItemz = itemz;
    }

    public boolean storeUse() {
        if (getTrade() != null) {
            dropMessage(6, "You are currently in a trade with someone.");
            return false;
        }
        if (isStorageOpened()) {
            dropMessage(6, "You are currently in storage system right now.");
            return false;
        }
        if (!isChangingMaps()) {
            Collection<Item> items = getInventory(MapleInventoryType.USE).sortedList();
            if (!items.isEmpty()) {
                EtcLock = true;
                overflowLock.lock();
                try {
                    for (Item item : items) {
                        int itemid = item.getItemId();
                        if (canStoreItem(item)) {
                            short quantity = item.getQuantity();
                            if (MapleInventoryManipulator.removeFromSlot(client, GameConstants.getInventoryType(itemid), item.getPosition(), quantity, false, false)) {
                                etc.computeIfAbsent(itemid, k -> new AtomicLong()).addAndGet(quantity);
                                totalitems += quantity;
                            }
                        }
                    }

                    checkOverFlow();
                } finally {
                    overflowLock.unlock();
                }
                items.clear();
                dropMessage(1, "ETC items have been stored in overflow. Character is saved.");
                saveToDB();
                EtcLock = false;
                return true;
            }
        }
        return false;
    }

    public boolean storeDropEtc(Collection<Item> items) {
        if (getTrade() != null) {
            dropMessage(6, "You are currently in a trade with someone.");
            return false;
        }
        if (isStorageOpened()) {
            dropMessage(6, "You are currently in storage system right now.");
            return false;
        }
        if (!isChangingMaps()) {
            if (!items.isEmpty()) {
                EtcLock = true;
                overflowLock.lock();
                try {
                    for (Item item : items) {
                        int itemid = item.getItemId();
                        short quantity = item.getQuantity();
                        etc.computeIfAbsent(itemid, k -> new AtomicLong()).addAndGet(quantity);
                        totalitems += quantity;
                        dropMessage(-1, "+" + quantity + " " + MapleItemInformationProvider.getInstance().getName(itemid) + " sent to Overflow (Total: " + StringUtil.getUnitNumber(etc.get(itemid).get()) + ")");
                    }
                    checkOverFlow();
                } finally {
                    overflowLock.unlock();
                }
                items.clear();
                EtcLock = false;
                return true;
            }
        }
        return false;
    }

    public boolean removeItemFromSlot(Item item) {
        if (item != null) {
            return MapleInventoryManipulator.removeFromSlot(client, item.getInventoryType(), item.getPosition(), item.getQuantity(), false, false);
        }
        return false;
    }

    public boolean addOverflowNPC(Item item, int quantity, String log) {
        if (getTrade() != null) {
            dropMessage(6, "You are currently in a trade with someone.");
            return false;
        }
        if (isStorageOpened()) {
            dropMessage(6, "You are currently in storage system right now.");
            return false;
        }
        if (!isChangingMaps()) {
            if (quantity > 0) {
                if (countItemSlot(item.getPosition(), item.getItemId()) == item.getQuantity()) {
                    EtcLock = true;
                    int itemid = item.getItemId();
                    if (GameConstants.getInventoryType(itemid) == MapleInventoryType.USE || GameConstants.getInventoryType(itemid) == MapleInventoryType.ETC) {
                        overflowLock.lock();
                        try {
                            if (removeItemFromSlot(item)) {
                                etc.computeIfAbsent(itemid, k -> new AtomicLong()).addAndGet(quantity);
                            }
                        } finally {
                            overflowLock.unlock();
                        }
                        totalitems += quantity;
                        checkOverFlow();
                    }
                    EtcLock = false;
                    return true;
                } else {
                    System.out.println("[NPC] [" + Calendar.getInstance().getTime() + "] - " + getName() + " tried to illegally store " + quantity + " itemid " + item.getItemId() + " to npc");
                }

                //Item item = new Item(itemid, (byte) 0, (short) (quantity), (byte) 0);
            } else {
                System.out.println("[NPC] [" + Calendar.getInstance().getTime() + "] - " + getName() + " tried to store " + quantity + " itemid " + item.getItemId() + " to npc?");
                kick();
            }
        }
        return false;

    }

    public boolean removeOverflow(int itemid, long quantity) {
        return removeOverflow(itemid, quantity, "");
    }

    public boolean removeOverflow(int itemid, long quantity, String log) {
        boolean check = false;
        if (getTrade() != null || isStorageOpened()) {
            return false;
        }
        if (quantity > 0) {
            //Item item = new Item(itemid, (byte) 0, (short) (quantity), (byte) 0);
            if (GameConstants.getInventoryType(itemid) == MapleInventoryType.USE || GameConstants.getInventoryType(itemid) == MapleInventoryType.ETC) {
                overflowLock.lock();
                EtcLock = true;
                try {
                    if (etc.containsKey(itemid)) {
                        long count = etc.get(itemid).get();
                        if (count <= 0) {
                            System.out.println("[" + Calendar.getInstance().getTime() + "] - " + getName() + " possible removeOverflow dupe: Item " + itemid + " - Amount: " + (quantity));
                            etc.remove(itemid);
                            EtcLock = false;
                            return false;
                        }
                        if (count >= quantity) {
                            etc.get(itemid).addAndGet(-quantity);
                            long newCount = etc.get(itemid).get();
                            if (newCount <= 0) {
                                if (newCount < 0) {
                                    System.out.println("[" + Calendar.getInstance().getTime() + "] - " + getName() + " possible removeOverflow dupe: Item " + itemid + " - Amount: " + (quantity));
                                }
                                etc.remove(itemid);
                            }
                            check = true;
                        }
                    }
                } finally {
                    overflowLock.unlock();
                }
                if (check) {
                    totalitems -= quantity;
                    checkOverFlow();
                    EtcLock = false;
                    return true;
                }
                EtcLock = false;
            }
        } else {
            System.out.println("[" + Calendar.getInstance().getTime() + "] - " + getName() + " tried to withdrawl (removeOverflow) " + quantity + " itemid " + itemid + "?");
            kick();
        }
        return false;
    }

    public final boolean canHold(final int itemid, final int quantity) {
        return MapleInventoryManipulator.checkSpace(client, itemid, Randomizer.Max(quantity, Integer.MAX_VALUE), "");
    }

    public boolean removeOverflowNpc(int itemid, long quantity, String log) {
        if (getTrade() != null || isStorageOpened()) {
            return false;
        }
        if (!isChangingMaps()) {
            EtcLock = true;
            boolean check = false;
            if (quantity > 0 && quantity <= 2500000) {
                //Item item = new Item(itemid, (byte) 0, (short) (quantity), (byte) 0);
                if (GameConstants.getInventoryType(itemid) == MapleInventoryType.USE || GameConstants.getInventoryType(itemid) == MapleInventoryType.ETC) {
                    if (!canHold(itemid, (int) quantity)) {
                        EtcLock = false;
                        return false;
                    }
                    overflowLock.lock();
                    try {
                        if (etc.containsKey(itemid)) {
                            long count = etc.get(itemid).get();
                            if (count > 0 && quantity <= count) {
                                etc.get(itemid).addAndGet(-quantity);
                                long newCount = etc.get(itemid).get();
                                if (newCount <= 0) {
                                    if (newCount < 0) {
                                        System.out.println("[NPC] [" + Calendar.getInstance().getTime() + "] - " + getName() + " possible overflow dupe: Item " + itemid + " - Amount: " + (quantity));
                                        EtcLock = false;
                                        return false;
                                    }
                                    etc.remove(itemid);
                                }
                                gainItem(itemid, (int) quantity, " collected " + quantity + " from overflow system");
                                check = true;
                            }
                            if (check) {
                                totalitems -= quantity;
                                checkOverFlow();
                                EtcLock = false;
                                return true;
                            }
                        } else {
                            System.out.println("NPC [" + Calendar.getInstance().getTime() + "] - " + getName() + " possible overflow dupe: Non exist Item " + itemid + " - Amount: " + (quantity));
                        }
                    } finally {
                        overflowLock.unlock();
                    }

                }
            } else {
                System.out.println("NPC [" + Calendar.getInstance().getTime() + "] - " + getName() + " tried to withdrawl (removeOverflowNpc)" + quantity + " itemid " + itemid + "?");
                kick();
            }
            EtcLock = false;
        }
        return false;
    }

    public boolean OverflowExchange(int itemid, int quantity, int leaf) {
        boolean check = false;
        if (!isChangingMaps()) {
            EtcLock = true;
            if (quantity > 0) {
                //Item item = new Item(itemid, (byte) 0, (short) (quantity), (byte) 0);
                if (GameConstants.getInventoryType(itemid) == MapleInventoryType.USE || GameConstants.getInventoryType(itemid) == MapleInventoryType.ETC) {
                    overflowLock.lock();
                    try {
                        if (etc.containsKey(itemid)) {
                            long count = etc.get(itemid).get();
                            if (count > 0 && quantity <= count) {
                                etc.get(itemid).addAndGet(-quantity);
                                long newCount = etc.get(itemid).get();
                                if (newCount <= 0) {
                                    if (newCount < 0) {
                                        System.out.println("[NPC] [" + Calendar.getInstance().getTime() + "] - " + getName() + " possible overflow dupe: Item " + itemid + " - Amount: " + (quantity));
                                        EtcLock = false;
                                        return false;
                                    }
                                    etc.remove(itemid);
                                }
                                etc.computeIfAbsent(leaf, k -> new AtomicLong()).addAndGet(quantity);
                                check = true;
                            }
                        } else {
                            System.out.println("NPC [" + Calendar.getInstance().getTime() + "] - " + getName() + " possible overflow dupe: Non exist Item " + itemid + " - Amount: " + (quantity));
                        }
                    } finally {
                        overflowLock.unlock();
                    }
                }
            } else {
                System.out.println("NPC [" + Calendar.getInstance().getTime() + "] - " + getName() + " tried to withdrawl (overflowExchange)" + quantity + " itemid " + itemid + "?");
                kick();
            }
            EtcLock = false;
        }
        return check;
    }

    public List<Integer> getBlackList() {
        List<Integer> items = new ArrayList<>(blackItemz);
        Collections.sort(items);
        return Collections.unmodifiableList(items);
    }

    public List<Integer> getOverflow() {
        List<Integer> items = new ArrayList<>(etc.keySet());
        Collections.sort(items);
        return Collections.unmodifiableList(items);
    }

    public List<Integer> getOverflowEtc() {
        List<Integer> items = new ArrayList<>(etc.keySet());
        List<Integer> itemz = new ArrayList<>();
        for (int id : items) {
            if (GameConstants.getInventoryType(id) == MapleInventoryType.ETC) {
                itemz.add(id);
            }
        }
        items.clear();
        Collections.sort(itemz);
        return Collections.unmodifiableList(itemz);
    }

    public Map<Integer, AtomicLong> getOverflowMap() {
        return Collections.unmodifiableMap(etc);
    }

    public Map<Integer, AtomicLong> getOverflowInv() {//fix dupe
        Map<Integer, AtomicLong> itemMap = new ConcurrentHashMap<Integer, AtomicLong>();
        for (int i : etc.keySet()) {
            itemMap.put(i, new AtomicLong(etc.get(i).get()));
        }
        MapleInventoryType Use = MapleInventoryType.USE;
        for (Item i : inventory[Use.ordinal()]) {
            if (itemMap.containsKey(i.getItemId())) {
                itemMap.get(i.getItemId()).getAndAdd(i.getQuantity());
            } else {
                itemMap.put(i.getItemId(), new AtomicLong(i.getQuantity()));
            }
        }
        Use = MapleInventoryType.ETC;
        for (Item i : inventory[Use.ordinal()]) {
            if (itemMap.containsKey(i.getItemId())) {
                itemMap.get(i.getItemId()).getAndAdd(i.getQuantity());
            } else {
                itemMap.put(i.getItemId(), new AtomicLong(i.getQuantity()));
            }
        }

        Map<Integer, AtomicLong> overflowItems = new LinkedHashMap<>();
        itemMap.entrySet().stream().sorted((t, o) -> Integer.compare(t.getKey(), o.getKey())).forEach(e -> overflowItems.put(e.getKey(), e.getValue()));
        return Collections.unmodifiableMap(overflowItems);
    }

    public List<Integer> getOverflowUse() {
        List<Integer> items = new ArrayList<>(etc.keySet());
        List<Integer> itemz = new ArrayList<>();
        for (int id : items) {
            if (GameConstants.getInventoryType(id) == MapleInventoryType.USE) {
                itemz.add(id);
            }
        }
        items.clear();
        Collections.sort(itemz);
        return Collections.unmodifiableList(itemz);
    }

    public final long getOverflowAmount(int id) {
        long count = 0;
        if (etc.containsKey(id)) {
            count = etc.get(id).get();
        }
        return count;
    }

    public long getTotalItems() {
        return totalitems;
    }

    public int getTotalTypes() {
        return etc.size();
    }

    public void removeEquipOverflow(Equip equip) {
        itemz.remove(equip);
    }

    public List<Item> getEquipOverflow() {
        return itemz;
    }

    public void clearEquipOverflow() {
        itemz.clear();
    }

    public List<Equip> getEquippedItems() {
        List<Equip> equipped = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.EQUIPPED)) {
            equipped.add((Equip) it);
        }
        return equipped;
    }

    public List<Equip> getEquipItems() {
        List<Equip> equips = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.EQUIP).sortedList()) {
            Equip equip = (Equip) it;
            equips.add(equip);
        }
        return equips;
    }

    public List<Equip> getWeaponItems() {
        List<Equip> equips = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.EQUIP).sortedList()) {
            if (GameConstants.isWeapon(it.getItemId())) {
                Equip equip = (Equip) it;
                equips.add(equip);
            }
        }
        return equips;
    }

    public List<Equip> getLockedEquipItems() {
        List<Equip> equips = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.EQUIP).sortedList()) {
            Equip equip = (Equip) it;
            if (ItemFlag.LOCK.check(equip.getFlag())) {
                equips.add(equip);
            }
        }
        return equips;
    }

    public List<Equip> getEquipNoCash() {
        List<Equip> equips = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.EQUIP).sortedList()) {
            Equip equip = (Equip) it;
            if (!MapleItemInformationProvider.getInstance().isCash(equip.getItemId())) {
                equips.add(equip);
            }
        }
        return equips;
    }

    public List<Equip> getEquipCash() {
        List<Equip> equips = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.EQUIP).sortedList()) {
            Equip equip = (Equip) it;
            if (MapleItemInformationProvider.getInstance().isCash(equip.getItemId())) {
                equips.add(equip);
            }
        }
        return equips;
    }

    public List<Item> getItemPets() {
        List<Item> pets = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.CASH).sortedList()) {
            if (GameConstants.isPet(it.getItemId())) {
                pets.add(it);
            }
        }
        return pets;
    }

    public List<Item> getScrolls() {
        List<Item> equips = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.USE).sortedList()) {
            if (GameConstants.isScroll(it.getItemId())) {
                equips.add(it);
            }
        }
        return equips;
    }

    public List<Item> getItemsByType(byte type) {
        List<Item> itemb = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.getByType(type)).sortedList()) {
            itemb.add(it);
        }
        return itemb;
    }

    public List<Item> getItemsByOverflow() {
        List<Item> itemb = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.USE).sortedList()) {
            itemb.add(it);
        }
        for (Item it : getInventory(MapleInventoryType.ETC).sortedList()) {
            itemb.add(it);
        }
        return itemb;
    }

    public List<Integer> getItemsByOverflowIds() {
        List<Integer> itemb = new ArrayList<>();
        for (Item it : getInventory(MapleInventoryType.USE).sortedList()) {
            itemb.add(it.getItemId());
        }
        for (Item it : getInventory(MapleInventoryType.ETC).sortedList()) {
            itemb.add(it.getItemId());
        }
        return itemb;
    }

    public void updatePets() {
        for (Item it : getInventory(MapleInventoryType.CASH).sortedList()) {
            if (it.getPet() != null) {
                //if (it.getPet().getChanged()) {
                getClient().announce(InventoryPacket.updateItemslot(it));
                getClient().announce(PetPacket.updatePet(it.getPet(), getInventory(MapleInventoryType.CASH).getItem((byte) it.getPet().getInventoryPosition()), true));
                getClient().announce(CWvsContext.enableActions());
                if (it.getPet().getSummoned()) {
                    getMap().broadcastMessage(MTSCSPacket.changePetName(this, it.getPet().getName(), 1));
                }
                //}
            }
        }
    }

    public int usePowerScrolls(Equip eqp, int min, int rounds, int scroll) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<String, Integer> stats = ii.getEquipStats(scroll);
        int chance = stats.get("success");
        var scrolls = 0;
        for (int x = 0; x < rounds; x++) {
            boolean fail = usePScroll(eqp, chance, min, ii) > 0;
            if (fail) {
                break;
            } else {
                scrolls++;
            }
        }
        getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.SUCCESS, false, false), true);
        getClient().announce(InventoryPacket.updateItemslot(eqp));
        return scrolls;
    }

    public int usePScroll(Equip eqp, int chance, long min, MapleItemInformationProvider ii) {
        if (eqp.getUpgradeSlots() > 0) {
            int roll = Randomizer.random(1, 100);
            if (roll <= chance) {
                ii.scrollOptionEquipWithChaos(eqp, min);
                eqp.setUpgradeSlots((short) (eqp.getUpgradeSlots() - 1));
            }
            return 0;
        } else {
            return 1;
        }
    }

    public int usePowerShards(Equip eqp, int chance, int min, int rounds, int scroll) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<String, Integer> stats = ii.getEquipStats(scroll);
        var scrolls = 0;
        for (int x = 0; x < rounds; x++) {
            boolean fail = usePScroll(eqp, chance, min, ii) > 0;
            if (fail) {
                break;
            } else {
                scrolls++;
            }
        }
        getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.SUCCESS, false, false), true);
        getClient().announce(InventoryPacket.updateItemslot(eqp));
        return scrolls;
    }

    public int useBulkShardScroll(Equip eqp, int chance, int min, int rounds, int scroll) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<String, Integer> stats = ii.getEquipStats(scroll);
        int used = useBulkPScroll(eqp, chance, min, ii, rounds);
        if (used > 0) {
            getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.SUCCESS, false, false), true);
            getClient().announce(InventoryPacket.updateItemslot(eqp));
        }
        return used;
    }

    public int useBulkPowerScroll(Equip eqp, int min, int rounds, int scroll) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<String, Integer> stats = ii.getEquipStats(scroll);
        int chance = stats.get("success");
        int used = useBulkPScroll(eqp, chance, min, ii, rounds);
        if (used > 0) {
            getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.SUCCESS, false, false), true);
            getClient().announce(InventoryPacket.updateItemslot(eqp));
        }
        return used;
    }

    public int useBulkPScroll(Equip eqp, int chance, long min, MapleItemInformationProvider ii, int scrolls) {
        if (eqp.getUpgradeSlots() > 0) {
            int count = eqp.getUpgradeSlots();
            int used = 0, suc = 0;
            while (count > 0 && scrolls > 0) {
                int roll = Randomizer.random(1, 100);
                if (roll <= chance) {
                    suc++;
                    count--;
                }
                scrolls--;
                used++;

            }
            if (suc > 0) {
                ii.scrollOptionEquipWithBulkChaos(eqp, min, suc);
                eqp.setUpgradeSlots((short) count);
            }
            return used;
        } else {
            return 0;
        }
    }

    public void addTime(Item eqp, int day) {
        long time = Randomizer.Lmin(eqp.getExpiration(), System.currentTimeMillis()) + (day * 24 * 60 * 60 * 1000L);
        eqp.setExpiration(time);
        getClient().announce(InventoryPacket.updateItemslot(eqp));
    }

    public long getRemainingTime(long time) {
        return time - System.currentTimeMillis();
    }

    public boolean hasLucky(Equip eqp) {
        return ItemFlag.LUCKS_KEY.check(eqp.getFlag());
    }

    public void addLucky(Equip eqp) {
        if (!ItemFlag.LUCKS_KEY.check(eqp.getFlag())) {
            short flag = eqp.getFlag();
            flag |= ItemFlag.LUCKS_KEY.getValue();
            eqp.setFlag(flag);
            getClient().announce(EffectPacket.showForeignEffect(35));
            getClient().announce(InventoryPacket.updateItemslot(eqp));
        }
    }

    public int usePShard(Equip eqp, int chance, int min, MapleItemInformationProvider ii) {
        if (eqp.getUpgradeSlots() > 0) {
            int roll = Randomizer.random(1, 100);
            if (roll <= chance) {
                ii.scrollOptionEquipWithChaos(eqp, min);
                eqp.setUpgradeSlots((short) (eqp.getUpgradeSlots() - 1));
            }
            return 0;
        } else {
            return 1;
        }
    }

    public int useEEScrolls(Equip eqp, int chance, int min, int max, int rounds) {
        var scrolls = 0;
        int count = 0;
        int start = eqp.getEnhance();
        int[] stats = new int[8];
        for (int x = 0; x < rounds; x++) {
            int rate = (int) Randomizer.DoubleMin(Math.pow(0.75, start) * 1000, 1.0);
            if (start < 50) {
                int roll = Randomizer.random(1, 1000);
                if (roll <= rate) {
                    for (int z = 0; z < 8; z++) {
                        stats[z] += Randomizer.random(min, max);
                    }
                    start++;
                    scrolls++;
                }
                count++;
            } else {
                break;
            }
        }
        if (scrolls > 0) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            ii.scrollOptionEquipWithEE(eqp, stats);
            eqp.setEnhance(eqp.getEnhance() + scrolls);
            getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.SUCCESS, false, false), true);
            getClient().announce(InventoryPacket.updateItemslot(eqp));
            if (eqp.getEnhance() >= 50) {
                finishAchievement(126);
            }
        }
        return count;
    }

    public int useEScroll(int start, int chance, int min, int max) {
        return 0;
    }

    public int resetNXFlame(Equip eqp, int level, int tier, int chance, int safe) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (eqp.isCash(eqp.getItemId())) {
            if (safe == 1 || Randomizer.randomMinMax(1, 100) > chance) {
                eqp.resetStatsCash(level, tier);
                getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.SUCCESS, false, false), true);
                getClient().announce(InventoryPacket.updateItemslot(eqp));
                return 1;
            } else {
                short slot = eqp.getPosition();
                String equipName = eqp.getItemName(eqp.getItemId());
                dropMessage(equipName + " was blown up from power of the Flame!");
                getMap().broadcastMessage(this, CField.getScrollEffect(getId(), Equip.ScrollResult.FAIL, false, false), true);
                if (eqp.getPosition() < 0) {
                    Item tempItem = getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
                    MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.EQUIPPED, slot, tempItem.getQuantity(), false, false);
                } else {
                    Item tempItem = getInventory(MapleInventoryType.EQUIP).getItem(slot);
                    MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.EQUIP, slot, tempItem.getQuantity(), false, false);
                }
                return 2;
            }
        }
        return 0;
    }

    public void recycleItems() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int power = 0;
        int cash = 0;
        for (Item item : itemz) {
            if (item.getInventoryType() == MapleInventoryType.EQUIP) {
                Equip eqp = (Equip) item;
                if (ii.isCash(eqp.getItemId())) {
                    cash += 1;
                }
                power += eqp.getEnhance();
                itemz.remove(item);
            }
        }
        if (power > 0) {
            dropMessage("Gained " + power + " Unleashed Coins.");
            MapleInventoryManipulator.addById(client, 4310059, (short) power, "Recycled");
        }
        if (cash > 0) {
            dropMessage("Gained " + cash + " Maple Points.");
            MapleInventoryManipulator.addById(client, 4310501, (short) cash, "Recycled");
        }
    }

    public boolean partyQuestCheck(int id) {
        if (this.getParty() != null) {
            for (MapleCharacter player : this.getPartyMembersOnSameMap()) {
                if (player.getQuestStatus(id) != 2) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void loadBank(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT `mesos` FROM `accounts` WHERE `id` = ?");
        ps.setInt(1, getAccountID());
        try (ResultSet rs = ps.executeQuery();) {
            if (rs.next()) {
                bank = rs.getLong("mesos");
            }
        }
        ps.close();
    }

    public long getBank() {
        return bank;
    }

    public void updateBank(long value) {
        if (bank + value >= 0) {
            bank += value;
            if (value < 0) {
                dropMessage((value * -1) + " Mesos has been removed from your bank. Balance is " + StringUtil.getUnitNumber(bank));
            } else {
                dropMessage(value + " Mesos has added to your bank. Balance is " + StringUtil.getUnitNumber(bank));
            }
        }
    }

    public void addBank(long value, boolean show) {
        bank += value;
        if (show) {
            dropTopMessage("+" + value + " Mesos");
            dropMessage((value * -1) + " Mesos has been removed from your bank. Balance is " + StringUtil.getUnitNumber(bank));
        }
    }

    public void saveBank(final Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE accounts SET mesos = ? WHERE `id` = ?");
        ps.setLong(1, bank);
        ps.setInt(2, getAccountID());
        ps.executeUpdate();
        ps.close();
    }

    public void saveBank() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET mesos = ? WHERE `id` = ?");
            ps.setLong(1, bank);
            ps.setInt(2, getAccountID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMP(int value) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
        FileoutputUtil.log(FileoutputUtil.Donator_Log, this.getName() + " has received " + value + " Donation Points on IP: " + this.getClient().getSessionIPAddress() + " on " + formatter.format(date));
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET mp = mp + ? WHERE `id` = ?");
            ps.setInt(1, value);
            ps.setInt(2, getAccountID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleCooldowns(final int numTimes, final boolean hurt, final long now) { // is putting it here a good
        // idea? expensive?
        if (!getCooldowns().isEmpty()) {
            for (MapleCoolDownValueHolder m : getCooldowns()) {
                if (m.startTime + m.length < now) {
                    final int skil = m.skillId;
                    removeCooldown(skil);
                    getClient().announce(CField.skillCooldown(skil, 0));
                }
            }
        }
        if (!getAllDiseases().isEmpty()) {
            for (MapleDiseaseValueHolder m : getAllDiseases()) {
                if (m != null && m.startTime + m.length < now) {
                    dispelDebuff(m.disease);
                }
            }
        }
        if (numTimes % 10 == 0) { // we're parsing through the characters anyway (:
            if (isAlive()) {
                if (getJob() == 131 || getJob() == 132) {
                    if (canBlood(now)) {
                        doDragonBlood();
                    }
                }
                if (canRecover(now)) {
                    doRecovery();
                }
                if (canHPRecover(now)) {
                    addHP((int) getStat().getHealHP());
                }
                if (canMPRecover(now)) {
                    addMP((int) getStat().getHealMP());
                }
                if (canFairy(now)) {
                    doFairy();
                }
                if (canFish(now)) {
                    doFish(now);
                }
                if (canDOT(now)) {
                    doDOT();
                }
            }
            if (hurt && isAlive()) {
                if (getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) == null) {
                    if (getMapId() == 749040100 && getInventory(MapleInventoryType.CASH).findById(5451000) == null) { // minidungeon
                        addHP(-getMap().getHPDec());
                    } else if (getMapId() != 749040100) {
                        addHP(-(getMap().getHPDec() - (getBuffedValue(MapleBuffStat.HP_LOSS_GUARD) == null ? 0 : getBuffedValue(MapleBuffStat.HP_LOSS_GUARD).intValue())));
                    }
                }
            }
        }
        /*
         * if (numTimes % 70 == 0 && getMount() != null && getMount().canTire(now)) {
         * getMount().increaseFatigue(); }
         * 
         * if (numTimes % 130 == 0) { //we're parsing through the characters anyway (:
         * //chr.doFamiliarSchedule(now); if (!getSummonedPets().isEmpty()) { for
         * (MaplePet pet : getSummonedPets()) { int newFullness = pet.getFullness() -
         * PetDataFactory.getHunger(pet.getPetItemId()); if (newFullness <= 5) {
         * pet.setFullness(15); unequipPet(pet, true, true); } else {
         * pet.setFullness(newFullness);
         * 
         * getClient().announce(PetPacket.updatePet(pet,
         * getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()),
         * true)); } } } }
         */

    }

    public int partyLevel() {
        int value = 0;
        List<MapleCharacter> pary = getPartyMembersOnSameMap();
        for (MapleCharacter chr : pary) {
            value += chr.getTotalLevel();
        }
        return value /= pary.size();
    }

    public void updateEventScore(final Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO event(charid, tower, bosspq, dojo, monsterpark) VALUES (?, ?,?,?,?) ON DUPLICATE KEY UPDATE tower = VALUES(tower), bosspq = VALUES(bosspq), dojo = VALUES(dojo), monsterpark = VALUES(monsterpark)")) {
            ps.setInt(1, accountid);
            ps.setLong(2, tower);
            ps.setLong(3, trails);
            ps.setLong(4, dojo);
            ps.setLong(5, monsterpark);
            ps.execute();
        }
    }

    public void updateEventScore() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO event(charid, tower, bosspq, dojo, monsterpark) VALUES (?, ?,?,?,?) ON DUPLICATE KEY UPDATE tower = VALUES(tower), bosspq = VALUES(bosspq), dojo = VALUES(dojo), monsterpark = VALUES(monsterpark)")) {
            ps.setInt(1, accountid);
            ps.setLong(2, tower);
            ps.setLong(3, trails);
            ps.setLong(4, dojo);
            ps.setLong(5, monsterpark);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadEventScore() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `event` WHERE `charid` = ?");
            ps.setInt(1, getAccountID());
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    tower = rs.getLong("tower");
                    trails = rs.getLong("bosspq");
                    dojo = rs.getLong("dojo");
                    monsterpark = rs.getLong("monsterpark");
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTower(long value) {
        tower += value;
    }

    public long getTower() {
        return tower;
    }

    public void updateTrials(long value) {
        trails += value;
        if (trails >= 10) {
            finishAchievement(303);
        }
        if (trails >= 50) {
            finishAchievement(304);
        }
        if (trails >= 100) {
            finishAchievement(305);
        }
        if (trails >= 150) {
            finishAchievement(306);
        }
        if (trails >= 200) {
            finishAchievement(307);
        }
        if (trails >= 250) {
            finishAchievement(308);
        }
        if (trails >= 300) {
            finishAchievement(309);
        }
        if (trails >= 400) {
            finishAchievement(310);
        }
        if (trails >= 500) {
            finishAchievement(311);
        }
    }

    public long getTrials() {
        return trails;
    }

    public long getTrialsLevel() {
        return trails % 10;
    }

    public void updateDojo(long value) {
        dojo += value;
    }

    public void updateMonsterPark(long value) {
        monsterpark += value;
    }

    public void addPendantSlot(int days) {
        this.getQuestNAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)).setCustomData(String.valueOf(System.currentTimeMillis() + ((long) days * 24 * 60 * 60 * 1000)));
    }

    public void addPocket() {
        //pockets?
        // Pocket Slots
        MapleQuest pocket = MapleQuest.getInstance(6500);
        pocket.forceComplete(this, 1012117);
    }

    public final void loadQuests(MapleClient c) {
        // extra pendant
        c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)).setCustomData(String.valueOf(System.currentTimeMillis() + ((long) 2014 * 24 * 60 * 60000)));
        c.getSession().write(CWvsContext.pendantSlot(true));

        // Quick Slots
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(GameConstants.QUICK_SLOT));
        c.getSession().write(CField.quickSlot(stat != null && stat.getCustomData() != null ? stat.getCustomData() : null));
    }

    public boolean getLoot() {
        return getVarZero("loot") != 1;
    }

    public void setLoot(boolean toggle) {
        toggleLoot = toggle;
    }

    public boolean getCool() {
        return toggleCool;
    }

    public void setCool(boolean toggle) {
        toggleCool = toggle;
    }

    public AbstractPlayerInteraction getAbstractPlayerInteraction() {
        return client.getAbstractPlayerInteraction();
    }

    public void buff() {
        if (buffed <= 0) {
            int[] buffs = {1121002, 4101004};// ,
            for (int sid : buffs) {
                Skill skill = SkillFactory.getSkill(sid);
                //skill.getEffect(skill.getMaxLevel()).applyTo(this);
                MapleStatEffect mse = skill.getEffect(skill.getMaxLevel());
                mse.applyBuffEffect(this, this, true, mse.getDuration());
            }
            getStat().recalcLocalStats(this);
            buffed = 10;
        }
    }

    public void superbuff() {
        dispelDebuffs();
        if (buffed <= 0) {
            int[] buffs = {1121002, 2301003, 2001002, 4101004};// ,
            for (int sid : buffs) {
                Skill skill = SkillFactory.getSkill(sid);
                //skill.getEffect(skill.getMaxLevel()).applyTo(this);
                MapleStatEffect mse = skill.getEffect(skill.getMaxLevel());
                mse.applyBuffEffect(this, this, true, mse.getDuration());
            }
            getStat().recalcLocalStats(this);
            buffed = 5;
        }
    }

    public void updateEquipSlot(Equip equip) {
        getClient().announce(CWvsContext.InventoryPacket.updateItemslot(equip));
    }

    public void loadStat() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `account_bonus` WHERE `id` = ?");
            ps.setInt(1, getAccountID());
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    XP = rs.getLong("XP");
                    DR = rs.getLong("DR");
                    IDP = rs.getLong("IDP");
                    AS = rs.getLong("STAT");
                    OP = rs.getLong("OP");
                    MR = rs.getLong("MR");
                    TD = rs.getLong("TD");
                    BD = rs.getLong("BD");
                    IED = rs.getLong("IED");
                    ETC = rs.getLong("ETC");
                    CD = rs.getLong("CD");
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        bonus_loaded = true;
    }

    public void updateStat() {
        if (bonus_loaded) {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO account_bonus (id, XP, DR, IDP, STAT, OP, MR, TD, BD, IED, ETC, CD) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `XP` = VALUES(`XP`), `DR` = VALUES(`DR`), `IDP` = VALUES(`IDP`), `STAT` = VALUES(`STAT`), `OP` = VALUES(`OP`), `MR` = VALUES(`MR`), `TD` = VALUES(`TD`), `BD` = VALUES(`BD`), `IED` = VALUES(`IED`), `ETC` = VALUES(`ETC`), `CD` = VALUES(`CD`)")) {
                    ps.setInt(1, accountid);
                    ps.setLong(2, (long) XP);
                    ps.setLong(3, (long) DR);
                    ps.setLong(4, (long) IDP);
                    ps.setLong(5, (long) AS);
                    ps.setLong(6, (long) OP);
                    ps.setLong(7, (long) MR);
                    ps.setLong(8, (long) TD);
                    ps.setLong(9, (long) BD);
                    ps.setLong(10, (long) IED);
                    ps.setLong(11, (long) ETC);
                    ps.setLong(12, (long) CD);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadBonusStat() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_bonus` WHERE `id` = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    CStr = rs.getInt("str");
                    CDex = rs.getInt("dex");
                    CInt = rs.getInt("int_");
                    CLuk = rs.getInt("luk");
                    CAtk = rs.getInt("atk");
                    CMatk = rs.getInt("matk");
                    CDef = rs.getInt("def");
                    CMdef = rs.getInt("mdef");
                    CHp = rs.getInt("hp");
                    CMp = rs.getInt("mp");
                    CMob = rs.getInt("mob");
                    CBoss = rs.getInt("boss");
                    CIed = rs.getInt("ied");
                    CCd = rs.getInt("cd");
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBonusStat() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO character_bonus (id, str, dex, int_, luk, atk, matk, def, mdef, hp, mp, mob, boss, ied, cd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `str` = VALUES(`str`), `dex` = VALUES(`dex`), `int_` = VALUES(`int_`), `luk` = VALUES(`luk`), `atk` = VALUES(`atk`), `matk` = VALUES(`matk`), `def` = VALUES(`def`), `mdef` = VALUES(`mdef`), `hp` = VALUES(`hp`), `mp` = VALUES(`mp`), `mob` = VALUES(`mob`), `boss` = VALUES(`boss`), `ied` = VALUES(`ied`), `cd` = VALUES(`cd`)")) {
                ps.setInt(1, id);
                ps.setInt(2, CStr);
                ps.setInt(3, CDex);
                ps.setInt(4, CInt);
                ps.setInt(5, CLuk);
                ps.setInt(6, CAtk);
                ps.setInt(7, CMatk);
                ps.setInt(8, CDef);
                ps.setInt(9, CMdef);
                ps.setInt(10, CHp);
                ps.setInt(11, CMp);
                ps.setInt(12, CMob);
                ps.setInt(13, CBoss);
                ps.setInt(14, CIed);
                ps.setInt(15, CCd);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setBonusStat(int type, int value) {
        switch (type) {
            case 1 ->
                CStr += value;
            case 2 ->
                CDex += value;
            case 3 ->
                CInt += value;
            case 4 ->
                CLuk += value;
            case 5 ->
                CAtk += value;
            case 6 ->
                CMatk += value;
            case 7 ->
                CDef += value;
            case 8 ->
                CMdef += value;
            case 9 ->
                CHp += value;
            case 10 ->
                CMp += value;
            case 11 ->
                CMob += value;
            case 12 ->
                CBoss += value;
            case 13 ->
                CIed += value;
            case 14 ->
                CCd += value;
        }
        if (type == 9 || type == 10) {
            AssignHPMP(value, type == 9);
        }
        updateBonusStat();
        getStat().recalcLocalStats(this);
    }

    public void setRnadomBonusStat(int rounds) {
        int[] stats = {0, 1, 1, 1, 1, 2, 2, 5, 5, 250, 250, 1, 1, 5, 1};
        for (int i = 0; i < rounds; i++) {
            int type = Randomizer.random(1, 14);
            switch (type) {
                case 1 ->
                    CStr += stats[type];
                case 2 ->
                    CDex += stats[type];
                case 3 ->
                    CInt += stats[type];
                case 4 ->
                    CLuk += stats[type];
                case 5 ->
                    CAtk += stats[type];
                case 6 ->
                    CMatk += stats[type];
                case 7 ->
                    CDef += stats[type];
                case 8 ->
                    CMdef += stats[type];
                case 9 ->
                    CHp += stats[type];
                case 10 ->
                    CMp += stats[type];
                case 11 ->
                    CMob += stats[type];
                case 12 ->
                    CBoss += stats[type];
                case 13 ->
                    CIed += stats[type];
                case 14 ->
                    CCd += stats[type];
            }
            if (type == 9 || type == 10) {
                AssignHPMP(stats[type], type == 9);
            }
        }
        updateBonusStat();
        getStat().recalcLocalStats(this);
    }

    public int getBonusStat(int type) {
        switch (type) {
            case 1:
                return CStr;
            case 2:
                return CDex;
            case 3:
                return CInt;
            case 4:
                return CLuk;
            case 5:
                return CAtk;
            case 6:
                return CMatk;
            case 7:
                return CDef;
            case 8:
                return CMdef;
            case 9:
                return CHp;
            case 10:
                return CMp;
            case 11:
                return CMob;
            case 12:
                return CBoss;
            case 13:
                return CIed;
            case 14:
                return CCd;
        }
        return 0;
    }

    public void updateStats() {
        getStat().recalcLocalStats(this);
    }

    public void saveMesos() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("UPDATE characters SET meso = ? WHERE`id` = ?")) {
            ps.setInt(1, meso);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addStat(int amount, int type) {
        //addStat(amount, type, false);
    }

    public void gainStat(int amount, int type) {
        int gain = (int) (amount * getStat().getMasterBuff());
        dropTopMessage("Gained +" + gain + "% " + getStatType(type));
        addStat(gain, type, false);
    }

    public String getStatType(int id) {
        switch (id) {
            case 1 -> {
                return "Exp Rate";
            }
            case 2 -> {
                return "Drop Rate";
            }
            case 3 -> {
                return "All Stats";
            }
            case 4 -> {
                return "Overpower";
            }
            case 5 -> {
                return "Meso Rate";
            }
            case 6 -> {
                return "Mob Damage";
            }
            case 7 -> {
                return "Boss Damage";
            }
            case 8 -> {
                return "Ignore Defense";
            }
            case 9 -> {
                return "Item Drop Power";
            }
            case 10 -> {
                return "ETC%";
            }
            case 11 -> {
                return "Critical Damage";
            }
        }
        return "";
    }

    public double getStat(int type) {
        switch (type) {
            case 1 -> {
                return Randomizer.DoubleMax(XP, getMaxStat(1));
            }
            case 2 -> {
                return Randomizer.DoubleMax(DR, getMaxStat(2));
            }
            case 3 -> {
                return Randomizer.DoubleMax(AS, getMaxStat(3));
            }
            case 4 -> {
                return Randomizer.DoubleMax(OP, getMaxStat(4));
            }
            case 5 -> {
                return Randomizer.DoubleMax(MR, getMaxStat(5));
            }
            case 6 -> {
                return Randomizer.DoubleMax(TD, getMaxStat(6));
            }
            case 7 -> {
                return Randomizer.DoubleMax(BD, getMaxStat(7));
            }
            case 8 -> {
                return Randomizer.DoubleMax(IED, getMaxStat(8));
            }
            case 9 -> {
                return Randomizer.DoubleMax(IDP, getMaxStat(9));
            }
            case 10 -> {
                return Randomizer.DoubleMax(ETC, getMaxStat(10));
            }
            case 11 -> {
                return Randomizer.DoubleMax(CD, getMaxStat(11));
            }
        }
        return 0;
    }

    public long getMaxStat(int type) {
        double rb = (int) getReborns();
        switch (type) {
            case 1:
                return (long) (100000000.0 + (100000000.0 * rb));
            case 2:
                return (long) (100000.0 + (10000.0 * rb));
            case 3:
                return (long) Randomizer.DoubleMax((10000000.0 + (10000000.0 * rb)), 1000000000);
            case 4:
                return (long) Randomizer.DoubleMax((10000000.0 + (10000000.0 * rb)), 1000000000);
            case 5:
                return (long) (100000.0 + (10000.0 * rb));
            case 6:
                return (long) Randomizer.DoubleMax((10000000.0 + (10000000.0 * rb)), 1000000000);
            case 7:
                return (long) Randomizer.DoubleMax((10000000.0 + (10000000.0 * rb)), 1000000000);
            case 8:
                return (long) Randomizer.DoubleMax((10000000.0 + (5000000.0 * rb)), 1000000000);
            case 9:
                return (long) 10000;
            case 10:
                double itotal = 10000.0 + (rb * 1000.0) + Randomizer.DoubleMax(getAccVara("Etc_Mod"), 50000.0);
                return (long) Randomizer.DoubleMax(itotal, 100000.0);
            case 11:
                return (long) Randomizer.DoubleMax((10000000.0 + (10000000.0 * rb)), 1000000000);
        }
        return 0;
    }

    public void addStat(int amount, int type, boolean bypass) {
        switch (type) {
            case 1:
                XP += amount;
                break;
            case 2:
                DR += amount;
                break;
            case 3:
                AS += amount;
                break;
            case 4:
                OP += amount;
                break;
            case 5:
                MR += amount;
                break;
            case 6:
                TD += amount;
                break;
            case 7:
                BD += amount;
                break;
            case 8:
                IED += amount;
                break;
            case 9:
                IDP += amount;
                break;
            case 10:
                ETC += amount;
                break;
            case 11:
                CD += amount;
                break;
        }
    }

    public void gainStat(int amount, int type, boolean bypass) {
        switch (type) {
            case 1:
                XP += amount;
                break;
            case 2:
                DR += amount;
                break;
            case 3:
                AS += amount;
                break;
            case 4:
                OP += amount;
                break;
            case 5:
                MR += amount;
                break;
            case 6:
                TD += amount;
                break;
            case 7:
                BD += amount;
                break;
            case 8:
                IED += amount;
                break;
            case 9:
                IDP += amount;
                break;
            case 10:
                ETC += amount;
                break;
            case 11:
                CD += amount;
                break;
        }
    }

    public double getXP() {
        return Randomizer.DoubleMax(XP + LinkXP + AchXP + QXP + DXP, getMaxStat(1));
    }

    public double getDR() {
        return Randomizer.DoubleMax(DR + LinkDR + AchDR + QDR + DDR, getMaxStat(2));
    }

    public double getIDP() {
        return Randomizer.DoubleMax(IDP, getMaxStat(9));
    }

    public double getETC() {
        return Randomizer.DoubleMax(ETC, getMaxStat(10));
    }

    public double getCD() {
        return Randomizer.DoubleMax(CD + DCD, getMaxStat(11));
    }

    public double getAS() {
        return Randomizer.DoubleMax(AS + LinkAS + AchAS + QAS + DAS, getMaxStat(3));
    }

    public double getOP() {
        return Randomizer.DoubleMax(OP + LinkOP + AchOP + QOP + DOP, getMaxStat(4));
    }

    public double getMR() {
        return Randomizer.DoubleMax(MR + LinkMR + AchMR + QMR + DMR, getMaxStat(5));
    }

    public double getBD() {
        return Randomizer.DoubleMax(BD + LinkBD + AchBD + QBD + DBD, getMaxStat(7));
    }

    public double getTD() {
        return Randomizer.DoubleMax(TD + LinkTD + AchTD + QTD + DTD, getMaxStat(6));
    }

    public double getIED() {
        return (finishedAchievements.size() * 10) + (finishedQuests.size() * 5);
    }

    public double getIEDP() {
        return IED + AchIED + QIED + LinkIED + DIED;
    }

    public double getResist() {
        return LinkRESIST;
    }

    public double getLinkXP() {
        return LinkXP;
    }

    public double getLinkDR() {
        return LinkDR;
    }

    public double getLinkAS() {
        return LinkAS;
    }

    public double getLinkOP() {
        return LinkOP;
    }

    public double getLinkMR() {
        return LinkMR;
    }

    public double getLinkBD() {
        return LinkBD;
    }

    public double getLinkTD() {
        return LinkTD;
    }

    public double getLinkIED() {
        return LinkIED;
    }

    public boolean getWarp() {
        return warp;
    }

    public void setWarp(boolean lock) {
        warp = lock;
    }

    public void toggleWarp() {
        if (warp) {
            warp = false;
            dropMessage(5, "You have unblocked players from using hyper rocks to teleport to you.");
        } else {
            warp = true;
            dropMessage(5, "You have blocked players from using hyper rocks to teleport to you.");
        }
    }

    public int countItem(int itemid) {
        return inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
    }

    public long countAllItem(int itemid) {
        long count = inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (etc.containsKey(itemid)) {
            count += etc.get(itemid).get();
        }
        return count;
    }

    public Equip getCubeEquip() {
        return cubeEquip;
    }

    public void setCubeEquip(Equip eqp) {
        cubeEquip = eqp;
    }

    public void clearCubeEquip() {
        cubeEquip = null;
    }

    public boolean addDP() {
        if (maplepoints > 0) {
            gainItem(4310502, maplepoints, " collected " + maplepoints + " from DP system.");
            dropMessage(6, "You have recieved " + maplepoints + " Donation Points. Check your ETC.");
            updateMP(maplepoints);
            maplepoints = 0;
            return true;
        }
        return false;
    }

    public void createRaid() {
        MapleRaid.createRaid(this);
    }

    public MapleRaid getRaid() {
        return raid;
    }

    public void setRaid(MapleRaid p) {
        raid = p;
    }

    public boolean getRaidStatus() {
        return raidstatus;
    }

    public void setRaidStatus(boolean value) {
        this.raidstatus = value;
    }

    public boolean isRaidLeader() {
        return raid.getLeader() == this;
    }

    public void setRaidLeader(boolean value) {
        this.raidLeader = value;
    }

    public boolean getRaidInvite() {
        return invite;
    }

    public void setRaidinvite(boolean value) {
        this.invite = value;
    }

    public List<MapleCharacter> getGroupMembers() {
        List<MapleCharacter> members = new LinkedList<MapleCharacter>();
        if (party != null) {
            for (MaplePartyCharacter chr : party.getMembers()) {
                if (chr.isOnline()) {
                    members.add(chr.getPlayer());
                }
            }
        }
        if (raid != null) {
            for (MapleCharacter chr : raid.getMembers()) {
                members.add(chr);
            }
        }
        if (!isGroup()) {
            members.add(this);
        }
        return members;
    }

    public List<MapleCharacter> getFreeMembers() {
        List<MapleCharacter> members = new LinkedList<MapleCharacter>();
        for (MapleCharacter chr : getMap().getAllPlayers()) {
            if (chr != null && chr.getRaid() == null && chr.getParty() == null && !chr.isGM()) {
                members.add(chr);
            }
        }
        return members;
    }

    public List<MapleCharacter> getRaidMembers() {
        List<MapleCharacter> members = new LinkedList<MapleCharacter>();
        for (MapleCharacter chr : getRaid().getMembers()) {
            if (chr != null) {
                members.add(chr);
            }
        }
        return members;
    }

    public void kick() {
        client.announce(CWvsContext.sendKick());
        client.forceDisconnect();
        //client.announce(HexTool.getByteArrayFromHexString("1A 00")); // give_buff with no data :D
    }

    public void dc() {
        client.announce(CWvsContext.sendKick());
        client.forceDisconnect();
        //client.announce(HexTool.getByteArrayFromHexString("1A 00")); // give_buff with no data :D
    }

    public void botchecker() {
        TimerManager.getInstance().schedule(() -> {
            if (getLock()) {
                kick();
            }
        }, 60000);
    }

    public void setImmune(boolean toggle) {
        immune = toggle;
    }

    public boolean getImmune() {
        return immune;
    }

    public void immune() {
        TimerManager.getInstance().schedule(() -> {
            if (immune) {
                immune = false;
                dropMessage("debuff");
            }
        }, 2000);
    }

    public void getLink() {
        getLink(true);
    }

    public void getLink(boolean update) {
        int a = 0, b = 0, c = 0, d = 0, f = 0, g = 0, h = 0, i = 0, j = 0, k = 0;
        int[] jb = {112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3212, 3312, 2312, 3512, 2112};
        for (var x = 0; x < jb.length; x++) {
            int jba = jb[x];
            long lvl = getVarZero(String.valueOf(jba));
            int value = 0;
            if (lvl >= 100) {
                value = 200;
            } else if (lvl >= 75) {
                value = 150;
            } else if (lvl >= 50) {
                value = 100;
            } else if (lvl >= 25) {
                value = 50;
            } else if (lvl >= 10) {
                value = 25;
            } else if (lvl >= 5) {
                value = 10;
            }
            if (jba == 112 || jba == 122 || jba == 512 || jba == 3112 || jba == 3512) {//boss damage
                a += value;
            }
            if (jba == 434 || jba == 532 || jba == 1512 || jba == 3212 || jba == 2112 || jba == 2312) {//overpower
                c += value;
            }
            if (jba == 132 || jba == 522 || jba == 3312 || jba == 1112) {//total damage
                d += value;
            }
            if (jba == 212 || jba == 222 || jba == 2218 || jba == 900 || jba == 232 || jba == 1212) {//all stats
                f += value;
            }
            if (jba == 312 || jba == 322 || jba == 1312 || jba == 412 || jba == 1412 || jba == 422) {//IED
                j += value;
            }
        }

        LinkBD = a;
        LinkRESIST = b;
        LinkOP = c;
        LinkTD = d;
        LinkAS = f;
        LinkXP = g;
        LinkDR = h;
        LinkMR = i;
        LinkIED = j;
        if (update) {
            getStat().recalcLocalStats(this);
        }
    }

    public String getLinkText() {
        String list = "";
        String star = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
        int count = 0;
        int a = 0, b = 0, c = 0, d = 0, f = 0, g = 0, h = 0, i = 0, j = 0, k = 0;
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ?")) {
            ps.setInt(1, accountid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (id != rs.getInt("id")) {
                        String stat = "#rNone#k";
                        int lvl = rs.getShort("totallevel");
                        short jb = rs.getShort("job");
                        int rb = rs.getInt("reborns");
                        int value = 0;
                        if (rb > 0) {
                            value = 100000 * rb;
                        } else {
                            if (lvl >= 9999) {
                                value = 25000;
                            } else if (lvl >= 9000) {
                                value = 10000;
                            } else if (lvl >= 8000) {
                                value = 7500;
                            } else if (lvl >= 7000) {
                                value = 5000;
                            } else if (lvl >= 6000) {
                                value = 2500;
                            } else if (lvl >= 5000) {
                                value = 1000;
                            } else if (lvl >= 4000) {
                                value = 750;
                            } else if (lvl >= 3000) {
                                value = 500;
                            } else if (lvl >= 2000) {
                                value = 250;
                            } else if (lvl >= 1500) {
                                value = 100;
                            } else if (lvl >= 1000) {
                                value = 50;
                            } else if (lvl >= 750) {
                                value = 25;
                            } else if (lvl >= 500) {
                                value = 10;
                            }
                        }
                        if (jb == 112 || jb == 122 || jb == 512 || jb == 3112 || jb == 3512) {//boss damage
                            a += value;
                            stat = "Boss Damage";
                        }
                        if (jb == 434 || jb == 532 || jb == 1512 || jb == 3212 || jb == 2112 || jb == 2312) {//overpower
                            c += value;
                            stat = "Overpower";
                        }
                        if (jb == 132 || jb == 522 || jb == 3312 || jb == 1112) {//total damage
                            d += value;
                            stat = "Total Damage";
                        }
                        if (jb == 212 || jb == 222 || jb == 2218 || jb == 900) {//all stats
                            f += value;
                            stat = "All Stats";
                        }
                        if (jb == 232 || jb == 1212) {//XP
                            g += value;
                            stat = "Exp rate";
                        }
                        if (jb == 412 || jb == 1412) {//drop rate
                            h += value;
                            stat = "Drop Rate";
                        }
                        if (jb == 422) {//meso
                            i += value;
                            stat = "Meso Rate";
                        }
                        if (jb == 312 || jb == 322 || jb == 1312) {//IED
                            j += value;
                            stat = "Ignore Defense";
                        }
                        if (value > 0) {
                            list += star + " [" + rs.getString("name") + " (#r" + lvl + "#k)] Link: #b" + stat + " +" + value + "%#k \r\n";
                        } else {
                            list += star + " [" + rs.getString("name") + " (#r" + lvl + "#k)] Link: #rUnlocks at Lvl. 500+#k \r\n";
                        }
                        count++;
                    }

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (count == 0) {
            list = "You currently have no characters high enough level for link stats.";
        }
        return list;
    }

    public boolean isOpened() {
        return opened || StorageOpened;
    }

    public void open(boolean b) {
        opened = b;
    }

    public boolean isStorageOpened() {
        return StorageOpened;
    }

    public void storageOpen(boolean b) {
        StorageOpened = b;
    }

    public void getAchieveStat() {
        getAchieveStat(true);
    }

    public void getAchieveStat(boolean update) {
        AchXP = 0.0;
        AchDR = 0.0;
        AchAS = 0.0;
        AchOP = 0.0;
        AchMR = 0.0;
        AchTD = 0.0;
        AchBD = 0.0;
        AchIED = 0.0;
        for (int aid : finishedAchievements) {
            MapleAchievement Ach = MapleAchievements.getInstance().getById(aid);
            if (Ach != null) {
                switch (Ach.getType()) {
                    case 0:
                        break;
                    case 1:
                    case 2:
                        AchDR += Ach.getAmount();
                        break;
                    case 3:
                        AchAS += Ach.getAmount();
                        break;
                    case 4:
                        AchOP += Ach.getAmount();
                        break;
                    case 5:
                        AchMR += Ach.getAmount();
                        break;
                    case 6:
                        AchTD += Ach.getAmount();
                        break;
                    case 7:
                        AchBD += Ach.getAmount();
                        break;
                    case 8:
                        AchIED += Ach.getAmount();
                        break;
                }
            }
        }
        if (update) {
            getStat().recalcLocalStats(this);
        }
    }

    public void getQuestStat() {
        getQuestStat(true);
    }

    public void getQuestStat(boolean update) {
        QXP = 0.0;
        QDR = 0.0;
        QAS = 0.0;
        QOP = 0.0;
        QMR = 0.0;
        QTD = 0.0;
        QBD = 0.0;
        QIED = 0.0;
        for (int aid : finishedQuests) {
            MapleKQuest Ach = MapleKQuests.getInstance().getById(aid);
            if (Ach != null) {
                switch (Ach.getStat()) {
                    case 0:
                        QXP += Ach.getStatAmount();
                        break;
                    case 1:
                    case 2:
                        QDR += Ach.getStatAmount();
                        break;
                    case 3:
                        QAS += Ach.getStatAmount();
                        break;
                    case 4:
                        QOP += Ach.getStatAmount();
                        break;
                    case 5:
                        QMR += Ach.getStatAmount();
                        break;
                    case 6:
                        QTD += Ach.getStatAmount();
                        break;
                    case 7:
                        QBD += Ach.getStatAmount();
                        break;
                    case 8:
                        QIED += Ach.getStatAmount();
                        break;
                }
            } else {
                System.out.println("Error with quest ID: " + aid);
            }
        }
        if (update) {
            getStat().recalcLocalStats(this);
        }
    }

    public int getTopLevel() {
        int value = 1;
        List<MapleCharacter> pary = getPartyMembersOnSameMap();
        for (MapleCharacter chr : pary) {
            if (chr != null && chr.getTotalLevel() > value) {
                value = chr.getTotalLevel();
            }
        }
        return value;
    }

    public void kill() {
        addMPHP(-999999999, 0);
        getClient().announce(CWvsContext.damagePlayer(this, 99999999));
        getMap().broadcastMessage(this, CField.damagePlayer(this.getId(), 99999999), false);
    }

    public boolean gainGP(int value) {
        return gainGP(value, true);
    }

    public boolean gainGP(int value, boolean show) {
        if (getGuild() != null) {
            if (guildContribution < 0) {
                guildContribution = Integer.MAX_VALUE;
            }
            if (guildContribution < Integer.MAX_VALUE) {
                if ((guildContribution + value > Integer.MAX_VALUE)) {
                    value = Integer.MAX_VALUE - guildContribution;
                }
                getGuild().gainGP(value, false, id);
                dropTopMessage("Gained " + NumberFormat.getInstance().format(value) + " Guild Points");
                if (show) {
                    getGuild().broadcast(CWvsContext.yellowChat("[Guild] " + getName() + " has added " + value + " Points towards the Guild."));
                }
                if (mgc.getGuildContribution() >= 100000) {
                    finishAchievement(160);
                }
                if (mgc.getGuildContribution() >= 250000) {
                    finishAchievement(161);
                }
                if (mgc.getGuildContribution() >= 500000) {
                    finishAchievement(162);
                }
                if (mgc.getGuildContribution() >= 1000000) {
                    finishAchievement(163);
                }
                if (mgc.getGuildContribution() >= 2500000) {
                    finishAchievement(164);
                }
                if (mgc.getGuildContribution() >= 5000000) {
                    finishAchievement(165);
                }
                if (mgc.getGuildContribution() >= 10000000) {
                    finishAchievement(166);
                }
                if (mgc.getGuildContribution() >= 25000000) {
                    finishAchievement(167);
                }
                if (mgc.getGuildContribution() >= 50000000) {
                    finishAchievement(168);
                }
                if (mgc.getGuildContribution() >= 100000000) {
                    finishAchievement(169);
                }
                if (guildContribution > Integer.MAX_VALUE) {
                    guildContribution = Integer.MAX_VALUE;
                }
                return true;
            }
        }
        return false;
    }

    public void checkQuest() {
        for (int i : MapleKQuests.getInstance().getQuestsCag()) {
            if (!getAchievement(500 + i)) {
                boolean check = true;
                for (MapleKQuest quest : MapleKQuests.getInstance().getQuestsbyCag(i)) {
                    if (!getQuest(quest.getId())) {
                        check = false;
                        break;
                    }
                }
                if (check) {
                    finishAchievement(500 + i);
                }
            }
        }
    }

    public boolean checkjob(int trial) {
        if (trial == 1 && GameConstants.isWarriorJob(job)) {
            return true;
        }
        if (trial == 2 && GameConstants.isMageJob(job)) {
            return true;
        }
        if (trial == 3 && GameConstants.isPirateJob(job)) {
            return true;
        }
        if (trial == 4 && GameConstants.isDemon(job)) {
            return true;
        }
        if (trial == 5 && GameConstants.isMech(job)) {
            return true;
        }
        if (trial == 6 && GameConstants.isThiefJob(job)) {
            return true;
        }
        if (trial == 7 && GameConstants.isArk(job)) {
            return true;
        }
        if (trial == 8 && GameConstants.isPath(job)) {
            return true;
        }
        if (trial == 9 && GameConstants.isKain(job)) {
            return true;
        }
        if (trial == 10) {
            return true;
        }
        return false;

    }

    public final void gainItem(final int id, final int quantity) {
        gainItem(id, quantity, false, 0, -1, quantity > 0 ? "player created item: " + id + " - Amount: " + quantity : "");
    }

    public final void gainItem(final int id, final int quantity, final String owner) {
        gainItem(id, quantity, false, 0, -1, owner);
    }

    public final void gainItemSilent(final int id, final int quantity) {
        gainItem(id, quantity, false, 0, -1, "", client, false);
    }

    public final void gainItem(final int id, final int quantity, final boolean randomStats, final long period, final int slots, final String owner) {
        gainItem(id, quantity, randomStats, period, slots, owner, client);
    }

    public final void gainItem(final int id, final int quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient c) {
        gainItem(id, quantity, randomStats, period, slots, owner, c, true);
    }

    public final void gainItem(final int id, final int quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient c, final boolean show) {
        if (c != null) {
            if (quantity >= 0) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final MapleInventoryType type = GameConstants.getInventoryType(id);
                if (MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
                    if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                        final Equip item = (Equip) (randomStats ? ii.randomizeStats(this, (Equip) ii.getEquipById(id)) : ii.getEquipById(id));
                        if (period > 0) {
                            item.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                        }
                        if (slots > 0) {
                            item.setUpgradeSlots((short) (item.getUpgradeSlots() + slots));
                        }

                        final String name = ii.getName(id);
                        if (id / 10000 == 114 && name != null && name.length() > 0) { // medal
                            final String msg = "You have attained title <" + name + ">";
                            dropMessage(-1, msg);
                            dropMessage(5, msg);
                        }
                        if (owner != null) {
                            item.setGMLog(owner);
                        } else {
                            System.out.println("item: " + id + " - Amount: " + quantity + " has no gmlog.");
                        }
                        MapleInventoryManipulator.addbyItem(c, item.copy());
                    } else {
                        if (GameConstants.isPet(id)) {
                            try {
                                MaplePet pet = MaplePet.createPet(id, MapleInventoryIdentifier.getInstance());
                                MapleInventoryManipulator.addById(c, id, (short) quantity, "", pet, -1, owner != null ? owner : "collected " + quantity + " from player gain item pet");
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            if (owner != null) {
                                int amount = quantity;
                                while (amount > 0) {
                                    MapleInventoryManipulator.addById(c, id, (short) (amount > 30000 ? 30000 : amount), "", null, period, owner);
                                    amount -= (amount > 30000 ? 30000 : amount);
                                }
                            }
                        }
                    }
                }
            } else {
                if (haveItem(id, -quantity)) {
                    int removed = MapleInventoryManipulator.removeByIdCount(c, GameConstants.getInventoryType(id), id, -quantity, true, false);
                    if (removed > 0) {
                        if (GameConstants.getInventoryType(id) == MapleInventoryType.USE || GameConstants.getInventoryType(id) == MapleInventoryType.ETC) {
                            c.getPlayer().removeOverflow(id, removed);
                        }
                    }
                }
            }
            if (show) {
                c.announce(InfoPacket.getShowItemGain(id, quantity, true));
            }
        }
    }

    public boolean getItemWarning(int id, int amount) {
        if (id == 5140007 && amount >= 2500) {
            return true;
        }
        if (amount >= 250) {
            switch (id) {
                case 2049189, 2049175, 2049176, 2049305, 2049306, 2049307 -> {
                    return true;
                }
                case 2430131, 2000012, 4420008, 4420009, 2340000 -> {
                    return true;
                }
            }
        }
        if (amount >= 1000) {
            switch (id) {
                case 4001895, 4000313, 4001760 -> {
                    return true;
                }
            }
        }
        switch (id) {
            case 4420005, 4420006, 4420007, 4420010, 2049032 -> {
                return true;
            }
        }
        if (id / 10000 == 258 && amount >= 250) {
            return true;
        }
        if (id / 10000 == 431 && amount >= 1000) {
            return true;
        }
        return false;
    }

    public long getAndroidNeededExp(int level) {
        if (level < 200) {
            return GameConstants.getExpNeededForLevel(level);
        }
        long exp = (long) (Math.pow(level, 4.0));
        return Randomizer.LongMax(exp, 9000000000000000000L);
    }

    public void gainAndroidExp(long gain) { // need
        MapleAndroid droid = getAndroid();
        if (droid != null && droid.getLevel() < 99999) {
            long nextExp = Randomizer.LongMax(droid.getExp() + gain, Long.MAX_VALUE);
            if (nextExp >= getAndroidNeededExp(droid.getLevel())) {
                androidLevelUp(droid);
                upgradeAndroid();
            } else {
                droid.setExp(nextExp);
            }
        }
    }

    public void upgradeAndroid() {
        if (getInventory(MapleInventoryType.EQUIPPED).getItem((short) -53) != null) {
            final MapleInventory equip = getInventory(MapleInventoryType.EQUIPPED);
            Item item = equip.getItem((short) -53);
            Equip eqp = (Equip) item;
            eqp.upgradeAndroid(this, eqp);
            client.announce(InventoryPacket.updateSpecialItemUse_(equip.getItem((short) -53), (byte) 1, this));
            //getClient().announce(InventoryPacket.updateEquippedslot(item));
            getClient().announce(EffectPacket.showForeignEffect(35));
            equipChanged();
            if (!hideAndroid) {
                map.updateAndroid(this);
            }
        }
    }

    public void androidLevelUp(MapleAndroid droid) {
        droid.setExp(0);
        droid.setLevel(droid.getLevel() + 1);
        if (droid.getLevel() >= 10) {
            finishAchievement(700);
        }
        if (droid.getLevel() >= 25) {
            finishAchievement(701);
        }
        if (droid.getLevel() >= 50) {
            finishAchievement(702);
        }
        if (droid.getLevel() >= 75) {
            finishAchievement(703);
        }
        if (droid.getLevel() >= 100) {
            finishAchievement(704);
        }
        if (droid.getLevel() >= 150) {
            finishAchievement(705);
        }
        if (droid.getLevel() >= 200) {
            finishAchievement(706);
        }
        if (droid.getLevel() >= 250) {
            finishAchievement(707);
        }
        if (droid.getLevel() >= 500) {
            finishAchievement(708);
        }
        if (droid.getLevel() >= 999) {
            finishAchievement(709);
        }
        dropTopMessage("[Android] " + droid.getName() + " has gained a level! Now Level: " + droid.getLevel());
    }

    public Pair<Integer, Long> getFinalStake() {
        int hours = (int) ((System.currentTimeMillis() - stakeDP.getRight()) / (1000 * 60 * 60 * 12));
        int totalDP = (int) Math.floor(stakeDP.getLeft() + (int) (stakeDP.getLeft() / 100.0 * hours));
        return new Pair<Integer, Long>(totalDP, stakeDP.getRight());
    }

    public Pair<Integer, Long> getBaseStake() {
        return stakeDP;
    }

    public void stakeDP(int amount) {
        if (stakeDP == null) {
            stakeDP = new Pair<>(amount, System.currentTimeMillis());
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO `stake` VALUES(?, ?, ?)");
                ps.setInt(1, accountid);
                ps.setInt(2, amount);
                ps.setTimestamp(3, new Timestamp(stakeDP.getRight()));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void withdrawDP() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM `stake` where accid = ?");
            ps.setInt(1, accountid);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int hours = (int) ((System.currentTimeMillis() - stakeDP.getRight()) / (1000 * 60 * 60 * 12));
        int totalDP = (int) Math.floor(stakeDP.getLeft() + (int) (stakeDP.getLeft() / 100.0 * hours));
        gainItem(4310502, totalDP, " collected " + totalDP + " from stake system");//gain dp
        stakeDP = null;
    }

    public void loadStakeDP(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM `stake` WHERE `accid` = ?");
        ps.setInt(1, getAccountID());
        try (ResultSet rs = ps.executeQuery();) {
            if (rs.next()) {
                int amount = rs.getInt("amount");
                Timestamp time = rs.getTimestamp("starttime");
                stakeDP = new Pair<>(amount, time.getTime());
            }
        }
        ps.close();
    }

    public int getSlot() {
        return slots;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    public void loadQuestLocks() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM questLock WHERE accid = ?");
            ps.setInt(1, getAccountID());
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    questLock.put(rs.getInt("id"), rs.getLong("time"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveQuestLocks() {
        if (!questLock.isEmpty()) {
            saveQuestLock = true;
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                deleteWhereAccountId(con, "DELETE FROM questLock WHERE accid = ?", getAccountID());
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO questLock (`accid`, `id`, `time`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `id` = VALUES(`id`), `time` = VALUES(`time`)")) {
                    for (Entry<Integer, Long> e : questLock.entrySet()) {
                        ps.setInt(1, getAccountID());
                        ps.setInt(2, e.getKey());
                        ps.setLong(3, e.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            saveQuestLock = false;
        }
    }

    public long getTime(String i) {
        return System.currentTimeMillis() + (getVar(i) + 1000);
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public long timeClear(String i) {
        long quest = getVar(i);
        if (quest > 0) {
            long time = (long) (quest - System.currentTimeMillis());
            return time;
        }
        return 0;
    }

    public int getQuestStatus() {
        return quest_status;
    }

    public int getQuestlevel() {
        return quest_level;
    }

    public void loadQuestPool() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM quest_items WHERE charid = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    quest_status = (rs.getInt("status"));
                    quest_level = (rs.getInt("level"));
                    quest_item1 = (rs.getInt("item1"));
                    quest_item2 = (rs.getInt("item2"));
                    quest_item3 = (rs.getInt("item3"));
                    quest_item4 = (rs.getInt("item4"));
                    quest_item5 = (rs.getInt("item5"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveQuestPool() {
        saveQuestLock = true;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO quest_items (charid, `status`, `level`, `item1`, `item2`, `item3`, `item4`, `item5`) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE `status` = VALUES(`status`), `level` = VALUES(`level`), `item1` = VALUES(`item1`), `item2` = VALUES(`item2`), `item3` = VALUES(`item3`), `item4` = VALUES(`item4`), `item5` = VALUES(`item5`)")) {
                ps.setInt(1, id);
                ps.setInt(2, quest_status);
                ps.setInt(3, quest_level);
                ps.setInt(4, quest_item1);
                ps.setInt(5, quest_item2);
                ps.setInt(6, quest_item3);
                ps.setInt(7, quest_item4);
                ps.setInt(8, quest_item5);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        saveQuestLock = false;

    }

    /* End of Custom Feature */
    @Override
    public String toString() {
        return name;
    }

    public void loadShopItems() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryshop WHERE charid = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    MapleInventoryType type = GameConstants.getInventoryType(rs.getInt("itemid"));
                    if (type != MapleInventoryType.EQUIP) {
                        Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                        item.setGMLog("recalled from player shop");
                        shopItems.add(item);
                    } else {
                        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                        if (equip.getPosition() != -55) { //monsterbook
                            equip.setQuantity((short) 1);
                            equip.setInventoryId(rs.getLong("inventoryitemid"));
                            equip.setExpiration(rs.getLong("expiredate"));
                            equip.setUpgradeSlots(rs.getShort("upgradeslots"));
                            equip.setLevel(rs.getShort("level"));
                            equip.setStr(rs.getInt("str"));
                            equip.setDex(rs.getInt("dex"));
                            equip.setInt(rs.getInt("int"));
                            equip.setLuk(rs.getInt("luk"));
                            equip.setWatk(rs.getInt("watk"));
                            equip.setMatk(rs.getInt("matk"));
                            equip.setWdef(rs.getInt("wdef"));
                            equip.setMdef(rs.getInt("mdef"));
                            equip.setAcc(rs.getInt("acc"));
                            equip.setAvoid(rs.getInt("avoid"));
                            equip.setHands(rs.getInt("hands"));
                            equip.setSpeed(rs.getInt("speed"));
                            equip.setJump(rs.getInt("jump"));
                            equip.setViciousHammer(rs.getByte("ViciousHammer"));
                            equip.setItemEXP(rs.getInt("itemEXP"));
                            equip.setGMLog("recalled from player shop");
                            equip.setDurability(rs.getInt("durability"));
                            equip.setEnhance(rs.getInt("enhance"));
                            equip.setPotential1(rs.getInt("potential1"));
                            equip.setPotential2(rs.getInt("potential2"));
                            equip.setPotential3(rs.getInt("potential3"));
                            equip.setPotential4(rs.getInt("potential4"));
                            equip.setPotential5(rs.getInt("potential5"));
                            equip.setSocket1(rs.getInt("socket1"));
                            equip.setSocket2(rs.getInt("socket2"));
                            equip.setSocket3(rs.getInt("socket3"));
                            equip.setIncSkill(rs.getInt("incSkill"));
                            equip.setPVPDamage(rs.getInt("pvpDamage"));
                            equip.setCharmEXP(rs.getInt("charmEXP"));
                            equip.setOverPower(rs.getInt("overpower"));
                            equip.setTotalDamage(rs.getInt("totaldamage"));
                            equip.setBossDamage(rs.getInt("bossdamage"));
                            equip.setIED(rs.getInt("ied"));
                            equip.setCritDamage(rs.getInt("critdamage"));
                            equip.setAllStat(rs.getInt("allstat"));
                            equip.setPower(rs.getInt("power"));
                            equip.setOStr(rs.getLong("ostr"));
                            equip.setODex(rs.getLong("odex"));
                            equip.setOInt(rs.getLong("oint"));
                            equip.setOLuk(rs.getLong("oluk"));
                            equip.setOAtk(rs.getLong("oatk"));
                            equip.setOMatk(rs.getLong("omatk"));
                            equip.setODef(rs.getLong("odef"));
                            equip.setOMdef(rs.getLong("omdef"));

                            if (equip.getCharmEXP() < 0) { //has not been initialized yet
                                equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                            }
                            if (equip.getUniqueId() > -1) {
                                if (equip.getItemId() / 10000 == 166) {
                                    MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                                    if (ring != null) {
                                        equip.setAndroid(ring);
                                    }
                                }
                            }
                            shopItems.add(equip);
                        }
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Item> getShopItems() {
        return shopItems;
    }

    public Item getShopItem(int slot) {
        return shopItems.get(slot);
    }

    public boolean gainShopItem(int slot) {
        Item item = shopItems.get(slot);
        if (MapleInventoryManipulator.addFromDrop(getClient(), item)) {
            shopItems.remove(item);
            return true;
        }
        return false;
    }

    public boolean gainAllShopItems() {
        if (shopItems.isEmpty()) {
            return false;
        }
        boolean full = false;
        List<Item> itemw = new ArrayList<>(shopItems);
        for (Item itemb : itemw) {
            if (!canHold(itemb.getItemId(), itemb.getQuantity())) {
                full = true;
                break;
            }
        }
        if (!full) {
            for (Item itemb : itemw) {
                MapleInventoryManipulator.addFromDrop(getClient(), itemb);
            }
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM inventoryshop WHERE charid = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            itemw.clear();
            shopItems.clear();
            return true;
        }

        return false;
    }

    public int checkAllQuests() {
        int value = 0;
        for (MapleKQuest ach : MapleKQuests.getInstance().getQuests()) {
            if (finishMapleQuestEtc(ach.getId())) {
                value += 1;
            }
        }
        return value;
    }

    public void checkLevels() {

        if (totallevel >= 250) {
            if (GameConstants.isWarriorJob(job)) {
                finishAchievement(600);
            }
            if (GameConstants.isMageJob(job)) {
                finishAchievement(601);
            }
            if (GameConstants.isBowmanJob(job)) {
                finishAchievement(602);
            }
            if (GameConstants.isThiefJob(job)) {
                finishAchievement(603);
            }
            if (GameConstants.isPirateJob(job)) {
                finishAchievement(604);
            }
        }
    }

    public void sendCard() {
        this.runningStack += 1;
        this.client.getSession().write(CField.gainCardStack(getId(), this.runningStack, 1, 0, Randomizer.rand(100000, 500000), 1));
    }

    public int getBaitValue() {
        int itemid = (int) getVarZero("BAIT");
        switch (itemid) {
            case 4430001 -> {
                return 1;
            }
            case 4430002 -> {
                return 5;
            }
            case 4430003 -> {
                return 25;
            }
            case 4430004 -> {
                return 50;
            }
            case 4430005, 4430006 -> {
                return 100;
            }
            default -> {
            }
        }
        return 0;
    }

    public boolean canFish() {
        return canFish;
    }

    public void setFish(boolean toggle) {
        canFish = toggle;
    }

    public boolean checkFish() {
        if (!isAlive()) {
            return false;
        }
        if (getChair() == 0) {
            return false;
        }
        if (getTrade() != null || getShop() != null || isStorageOpened()) {
            return false;
        }
        if (getLock()) {
            return false;
        }
        if (getConversation() > 0) {
            return false;
        }
        if (getVarZero("BAIT") == 0) {
            return false;
        }
        if (getBaitValue() == 0) {
            return false;
        }
        if (!haveItem((int) getVarZero("BAIT"))) {
            return false;
        }
        int chance = Randomizer.random(1, 100);
        return chance <= getBaitValue();
    }

    public void setFusion(Equip base, Equip fused) {
        base.setItemId(fused.getItemId());
        getClient().announce(InventoryPacket.updateItemslot(base));
        short slot = fused.getPosition();
        MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.EQUIP, slot, fused.getQuantity(), false, false);
    }

    public void setMedalFusion(Equip base, Equip fused) {
        base.setItemId(fused.getItemId());
        getClient().announce(InventoryPacket.updateItemslot(base));
        short slot = fused.getPosition();
        MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.EQUIP, slot, fused.getQuantity(), false, false);
    }

    public void recoverStamina() {
        stamina = 1000;
        dropColorMessage(9, "Stamina has been recovered");
        int max = getMaxStamFromChar();
        int stam = (int) (((double) stamina / (double) max) * 100.0);
        getClient().announce(CField.achievementRatio(stam));
    }

    public void gainStamina(int value) {
        gainStamina(value, true);
    }

    public void gainStamina(int value, boolean show) {
        int max = getMaxStamFromChar();
        stamina = Randomizer.MinMax(stamina + value, 0, max);
        int stam = (int) (((double) stamina / (double) max) * 100.0);
        getClient().announce(CField.achievementRatio(stam));
        if (show) {
            if (value > 0) {
                dropColorMessage(9, value + " stamina has been added. (" + stamina + "/" + max + ")");
            } else {
                dropColorMessage(9, value + " stamina has been removed. (" + stamina + "/" + max + ")");
            }
        }
    }

    public int getStamina() {
        return stamina;
    }

    public int getStamPerc() {
        return (int) (((double) stamina / (double) getMaxStamFromChar()) * 100.0);
    }

    public boolean removeStamina(int value) {
        if (stamina >= value) {
            int max = getStat().getStamina();
            stamina = Randomizer.MinMax(stamina - value, 0, max);
            int stam = (int) (((double) stamina / (double) max) * 100.0);
            getClient().announce(CField.achievementRatio(stam));
            if (value < 0) {
                dropColorMessage(9, value + " stamina has been added. (" + stamina + "/" + max + ")");
            } else {
                dropColorMessage(9, value + " stamina has been removed. (" + stamina + "/" + max + ")");
            }
            return true;
        }
        return false;
    }

    public int singleCubes(Equip eqp, int row, int pot, int rounds) {
        int count = 0;
        if (eqp != null) {
            for (int x = 0; x < rounds; x++) {
                if (eqp.getPotential(row) != pot) {
                    eqp.singlePotential(row);
                    count++;
                } else {
                    break;
                }
            }
            if (count > 0) {
                client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
                equipChanged();
                getClient().announce(EffectPacket.showForeignEffect(35));
                getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
            }
        }
        return count;
    }

    public int singleCubes(Equip eqp, int cube, int row, int pot, int rounds) {
        int count = 0;
        long cubes = countAllItem(cube);
        if (eqp != null) {
            for (int x = 0; x < rounds; x++) {
                if (haveItem(cube, cubes)) {
                    if (eqp.getPotential(row) != pot) {
                        eqp.singlePotential(row);
                        count++;
                    } else {
                        break;
                    }
                    cubes -= 1;
                } else {
                    break;
                }
            }
            if (count > 0) {
                client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
                equipChanged();
                getClient().announce(EffectPacket.showForeignEffect(35));
                getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
            }
        }
        return count;
    }

    public int soulUpgrade(Equip eqp, int tier, int rounds) {
        int count = 0;
        if (eqp != null) {
            int cap = Randomizer.Max((int) (99999999 + (getReborns() * 10000000)), 999999999);
            for (int x = 0; x < rounds; x++) {
                if (eqp.getUpgradeSlots() > 0) {
                    eqp.randomBonusStats(this, eqp, tier, cap);
                    count++;
                } else {
                    break;
                }
            }
            if (count > 0) {
                client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
                equipChanged();
                getClient().announce(EffectPacket.showForeignEffect(35));
                getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
            }
        }
        return count;
    }

    public void upgradeTier(Equip eqp) {
        //formula = 10 * (power ^ 2)
        if (eqp != null) {
            eqp.upgradeTier();
            client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
            equipChanged();
            getClient().announce(EffectPacket.showForeignEffect(35));
            getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        }
    }

    public void updateEquip(Equip eqp) {
        //formula = 10 * (power ^ 2)
        if (eqp != null) {
            client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
            equipChanged();
        }
    }

    public int upgradeTiers(Equip eqp, int rounds, int chance, int cube) {
        int success = 0;
        int cost = 0;
        if (haveItem(cube, rounds)) {
            if (rounds > 0) {
                if (eqp != null) {
                    for (int x = 0; x < rounds; x++) {
                        if (eqp.getPower() < getBaseTier()) {
                            if (Randomizer.random(1, eqp.getPower()) == 1) {
                                eqp.upgradeTier();
                                success++;
                            }
                            cost++;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (success > 0) {
                client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
                equipChanged();
                getClient().announce(EffectPacket.showForeignEffect(35));
                getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
            }
            if (cost > 0) {
                gainItem(cube, -cost);
            }
        }
        return cost;
    }

    public void upgradeTier(Equip eqp, int slots) {
        //formula = 10 * (power ^ 2)
        if (eqp != null) {
            eqp.upgradeTier(slots);
            client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
            equipChanged();
            getClient().announce(EffectPacket.showForeignEffect(35));
            getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        }
    }

    public void setMonsterHunt(int value) {
        monsterHunt = value;
    }

    public int getMonsterHunt() {
        return monsterHunt;
    }

    public int getMonsterItem() {
        return (int) getVar("wanted_item");
    }

    public String getTownFromItem(int id) {
        for (MapleKQuest k : MapleKQuests.getInstance().getQuests()) {
            if (k.getItem() == id) {
                return k.getTownName();
            }
        }
        return "";
    }

    public void Reward(int value) {
        int item = itemRew[value];
        int amount = itemRewCount[value];
        if (!rewards.containsKey(item)) {
            rewards.put(item, new AtomicLong(amount));
        } else {
            rewards.get(item).addAndGet(amount);
        }
        gainItem(item, amount, " collected " + amount + " from casino slots");
        dropTopMessage("You won " + amount + " " + MapleItemInformationProvider.getInstance().getName(item));
    }

    public void Reward(int value, int amount) {
        int item = itemRew[value];
        if (!rewards.containsKey(item)) {
            rewards.put(item, new AtomicLong(amount));
        } else {
            rewards.get(item).addAndGet(amount);
        }
        gainItem(item, amount, " collected " + amount + " from casino slots");
        dropTopMessage("You won " + amount + " " + MapleItemInformationProvider.getInstance().getName(item));
    }

    public void procressReward(int value, final boolean jackpot) {
        final int item = itemRew[value];
        int amount = 0;
        switch (value) {
            case 0 ->
                amount = jackpot ? 25 : 5;
            case 1 ->
                amount = jackpot ? 2500 : 25;
            case 2 ->
                amount = jackpot ? 25 : 5;
            case 3 ->
                amount = jackpot ? 25000 : 100;
            case 4 ->
                amount = jackpot ? 50000 : 250;
            case 5 ->
                amount = jackpot ? 1000 : 25;
            case 6 ->
                amount = jackpot ? 250 : 10;
            case 7 ->
                amount = jackpot ? 1000 : 25;
            case 8 ->
                amount = jackpot ? 250 : 10;
            case 9 ->
                amount = jackpot ? 500 : 10;
            case 10 ->
                amount = jackpot ? 1000 : 25;
            case 11 ->
                amount = jackpot ? 1000 : 25;
            case 12 ->
                amount = jackpot ? 50000 : 1000;
            case 13 ->
                amount = jackpot ? 250 : 5;
            case 14 ->
                amount = jackpot ? 25 : 5;
            case 15 ->
                amount = jackpot ? 2500 : 50;
            case 16 ->
                amount = jackpot ? 5000 : 50;
            case 17 ->
                amount = jackpot ? 250 : 5;
            case 18 ->
                amount = jackpot ? 10000 : 100;
            case 19 ->
                amount = jackpot ? 1000 : 25;
            case 20 ->
                amount = jackpot ? 250 : 10;

        }
        final int Iid = item;
        final int count = amount;
        if (!rewards.containsKey(Iid)) {
            rewards.put(Iid, new AtomicLong(count));
        } else {
            rewards.get(Iid).addAndGet(count);
        }
        if (GameConstants.getInventoryType(Iid) == MapleInventoryType.ETC && !canHold(Iid, count)) {
            addOverflow(Iid, amount);
        } else {
            gainItem(Iid, count, " collected " + count + "from casino slots");
        }
        getClient().announce(EffectPacket.showForeignEffect(jackpot ? 0 : 35));
        dropTopMessage("You won bonus " + count + " " + MapleItemInformationProvider.getInstance().getName(Iid));
        //if (jackpot) {
        //    getClient().getChannelServer().dropMessage(14, "[JACKPOT] " + getName() + " has won x" + count + " " + MapleItemInformationProvider.getInstance().getName(Iid) + "!");
        //}
    }

    public List<Integer> getRewards() {
        List<Integer> items = new ArrayList<>(rewards.keySet());
        return Collections.unmodifiableList(items);
    }

    public long getRewardAmount(int index) {
        return rewards.get(index).get();
    }

    public void clearRewards() {
        rewards.clear();
    }

    public void runRewards(final int a, final int b, final int c) {
        if (this != null) {
            getClient().announce(CField.showEffect("miro/back"));
            getClient().announce(CField.showEffect("miro/frame"));
            getClient().announce(CField.showEffect("miro/RR1/" + a));
            getClient().announce(CField.showEffect("miro/RR2/" + b));
            getClient().announce(CField.showEffect("miro/RR3/" + c));
            TimerManager.getInstance().schedule(() -> {
                if (this != null) {
                    if (a == b && a == c && c == b) {
                        procressReward(a, true);
                    } else {
                        if (a == b) {
                            procressReward(a, false);
                        }
                        if (a == c) {
                            procressReward(a, false);
                        }
                        if (b == c) {
                            procressReward(b, false);
                        }
                    }
                    Reward(c);
                    Reward(b);
                    Reward(a);
                }
            }, 1600);

            //if ()
            //gainItem(4310502, 1);
        }
    }

    public void runSlot(int time, final int rounds, final int item, final int cost) {
        isSlot = true;
        run = true;
        getClient().announce(CField.UIPacket.IntroLock(true));
        getClient().announce(CField.UIPacket.IntroDisableUI(true));
        getClient().announce(CField.musicChange("BgmCustom/VHIFIVE"));
        final AtomicInteger counter = new AtomicInteger();
        if (haveItem(item, cost * rounds)) {
            slotTask = TimerManager.getInstance().register(() -> {
                if (this != null && run && counter.incrementAndGet() <= rounds) {
                    if (haveItem(item, cost)) {
                        gainItem(item, -cost, "");
                        dropMidMessage("" + counter.get());
                        runRewards(Randomizer.random(3, 20), Randomizer.random(3, 20), Randomizer.random(3, 20));
                    } else {
                        System.out.println("[" + Calendar.getInstance().getTime() + "] - " + getName() + " possible DP duping for free slots.");
                        if (getClient().getCMS() != null) {
                            getClient().getCMS().sendOkS("ERROR, Please contact Resinate on Discord with this error!", (byte) 16);
                        }
                        run = false;
                    }
                } else {
                    run = false;
                }
                if (!run) {
                    if (this != null) {
                        isSlot = false;
                        getClient().announce(CField.UIPacket.IntroLock(false));
                        getClient().announce(CField.UIPacket.IntroDisableUI(false));
                        getClient().announce(CField.musicChange(getMap().getBGM()));
                        getClient().announce(CField.getPublicNPCInfo());
                    }
                    if (slotTask != null) {
                        slotTask.cancel(true);
                        slotTask = null;
                        run = false;
                    }
                }
            }, time);
        }
        //getPlayer().getClient().announce(UIPacket.IntroDisableUI(true));
    }

    public void runCustomSlot(int type, int time, final int rounds, final int item, final int cost) {
        ServerSlots.runSlot(client, type, time, rounds, item, cost);
    }

    public void runCustomSlotShow(int type, int time, final int rounds, final int item, final int cost, boolean show) {
        ServerSlots.runSlot(client, type, time, rounds, item, cost, show);
    }

    public static List<ServerSlotItem> getItemsFromSlot(int id) {
        return ServerSlots.retrieveSlots(id);
    }

    public static List<ServerSlotItem> getItemListFromSlot(int id) {
        return ServerSlots.getItemListFromSlot(id);
    }

    public void getObjectVisible() {
        getClient().announce(CField.setVisibleObjects(getMap().getObjectFlags()));
    }

    public void Update(String Metodo, String Key, String Value, String KeyID, int ID) {
        String sql;
        try {
            sql = "UPDATE " + Metodo + " SET " + Key + " =  ? WHERE " + KeyID + " = ? ";
            PreparedStatement ps = DatabaseConnection.getPlayerConnection().prepareStatement(sql);
            ps.setString(1, Value);
            ps.setInt(2, ID);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
            dropMessage("Error: " + se);
        }
    }

    public int getVP() {
        int votes = 0;
        int totalvotes = 0;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accountid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    votes = rs.getInt("vpoints");
                    totalvotes = rs.getInt("totalvotes");
                }
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error getting character default" + e);
        }
        if (totalvotes > 0) {
            finishAchievement(610);
        }
        if (totalvotes >= 5) {
            finishAchievement(611);
        }
        if (totalvotes >= 10) {
            finishAchievement(612);
        }
        if (totalvotes >= 25) {
            finishAchievement(613);
        }
        if (totalvotes >= 50) {
            finishAchievement(614);
        }
        if (totalvotes >= 100) {
            finishAchievement(615);
        }
        return votes;
    }

    public boolean addVP() {
        int pts = getVP();
        if (pts > 0) {
            gainItem(4310503, pts * 5, "from vote points");
            vpoints = 0;
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET vpoints = 0 WHERE `id` = ?");
                ps.setInt(1, getAccountID());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            dropMessage(6, "You have recieved " + (pts * 5) + " Vote Points. Check your ETC.");
            return true;
        }
        return false;
    }

    public final boolean canHold(final int itemid) {
        return getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public void setSpeed(long value) {
        movespeed = value;
    }

    public void addSpeed() {
        movespeed++;
    }

    public long getSpeed() {
        return movespeed;
    }

    public void setCounter(int value) {
        counter = value;
    }

    public void addCounter() {
        counter++;
    }

    public long getCounter() {
        return counter;
    }

    public void getMasterys() {
        try {
            dropColorMessage(10, "[Weapon Masteries]");
            int total = 0;
            int base = 0;
            for (int type : level_data.keySet()) {
                if (type >= 30 && type <= 53) {
                    if (type == getWeaponType()) {
                        dropColorMessage(1, "[" + getWeaponName(type) + " Mastery] " + getBaseLevelDataWeaponPerc(type) + "% - [Exp] " + StringUtil.getUnitNumber(getLevelDataExp(type)) + " / " + StringUtil.getUnitNumber(getLevelDataNeededExp(getLevelDataLvl(type))) + " (" + getLevelExpDataPerc(type) + "%)");
                        total += getBaseLevelDataWeaponPerc(type);
                    } else {
                        dropColorMessage(8, "[" + getWeaponName(type) + " Mastery] " + getBaseLevelDataWeaponPerc(type) + "% - [Exp] " + StringUtil.getUnitNumber(getLevelDataExp(type)) + " / " + StringUtil.getUnitNumber(getLevelDataNeededExp(getLevelDataLvl(type))) + " (" + getLevelExpDataPerc(type) + "%)");
                    }

                }
            }
            dropColorMessage(10, "[Special Masteries]");
            for (int type : level_data.keySet()) {
                if (type >= 100 && type < 200) {
                    dropColorMessage(8, "[" + getWeaponName(type) + " Power] " + getLevelDataPerc(type) + "% - [Exp] " + StringUtil.getUnitNumber(getLevelDataExp(type)) + " / " + StringUtil.getUnitNumber(getLevelDataNeededExp(getLevelDataLvl(type))) + " (" + getLevelExpDataPerc(type) + "%)");
                    total += getLevelDataPerc(type);
                    base += getLevelDataPerc(type);
                }
            }
            dropColorMessage(8, "[Dojo Mastery] " + (int) (dojo_level) + "%");
            total += dojo_level;
            base += dojo_level;
            dropColorMessage(6, "[Base Mastery] " + base + "%");
            dropColorMessage(6, "[Total Mastery] " + total + "%");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getTotalBaseMastery() {
        int total = 0;
        for (int type : level_data.keySet()) {
            if (type >= 100 && type < 200) {
                total += getLevelDataPerc(type);
            }
        }
        total += dojo_level;
        return total;
    }

    public void loadLevelData() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `level_data` WHERE `charid` = ?");
            ps.setInt(1, accountid);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    level_data.put(rs.getInt("type"), new Pair<>(rs.getInt("level"), rs.getLong("exp")));
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveLevelData() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO level_data (charid, `type`, `level`, `exp`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `level` = VALUES(`level`), `exp` = VALUES(`exp`)")) {
            ps.setInt(1, accountid);
            for (int type : level_data.keySet()) {
                ps.setInt(2, type);
                ps.setInt(3, level_data.get(type).left);
                ps.setLong(4, level_data.get(type).right);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveLevelDataType(int type) {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO level_data (charid, `type`, `level`, `exp`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE `level` = VALUES(`level`), `exp` = VALUES(`exp`)")) {
            ps.setInt(1, accountid);
            ps.setInt(2, type);
            ps.setInt(3, level_data.get(type).left);
            ps.setLong(4, level_data.get(type).right);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getWeaponLevelDataNeededExp(int level) {
        return Randomizer.LongMax((long) (Math.pow(level, 3.5) + 100), Long.MAX_VALUE);
    }

    public long getLevelDataNeededExp(int level) {
        if (level < 9999) {
            return Randomizer.LongMax((long) (Math.pow(level, 3.5) + 100), Long.MAX_VALUE);
        }
        double x = (level - 9999) * 0.00001;
        return Randomizer.LongMax((long) (Math.pow(level, 4.0 + x)), Long.MAX_VALUE);
    }

    public int getLevelData(int type) {
        if (level_data.containsKey(type)) {
            return level_data.get(type).left;
        }
        return 1;
    }

    public int getMaxLevelData(int type) {
        switch (type) {
            case 100 -> {
                return 99999;
            }
            case 101 -> {
                return 99999;
            }
            case 102 -> {
                return 99999;
            }
            case 103 -> {
                return 99999;
            }
            case 104 -> {
                return 99999;
            }
            case 105 -> {
                return 99999;
            }
            case 106 -> {
                return 9999;
            }
            case 107 -> {
                return 9999;
            }
            case 108 -> {
                return 99999;
            }
            case 109 -> {
                return 99999;
            }
        }
        return 99999;
    }

    //weapon mastery system
    public void gainLevelData(long exp) {
        if (exp > 0) {
            int type = getWeaponType();
            if (type != 0) {
                level_data_lock.lock();
                try {

                    if (!level_data.containsKey(type)) {
                        level_data.put(type, new Pair<Integer, Long>(1, 0L));
                    }
                    if (level_data.get(type).left < getMaxLevelData(type)) {

                        double bGain = (double) (level_data.get(type).right) + Math.floor(exp * getStat().getMasterBuff() * GameConstants.getMasteryRate());
                        level_data.get(type).right = (long) Randomizer.DoubleMinMax(bGain, 0, Long.MAX_VALUE);
                        if (level_data.get(type).right >= getWeaponLevelDataNeededExp(level_data.get(type).left)) {
                            level_data.get(type).left += 1;
                            level_data.get(type).right = 0L;
                            getClient().announce(EffectPacket.showForeignEffect(35));
                            getLevelDataMsg(type, level_data.get(type).left);
                            saveLevelDataType(type);
                            recalcLocalStats();
                        }
                    }
                } finally {
                    level_data_lock.unlock();
                }
            }
        }
    }

    public void gainLevelData(int type, long exp) {
        if (exp > 0) {
            level_data_lock.lock();
            try {
                if (!level_data.containsKey(type)) {
                    level_data.put(type, new Pair<Integer, Long>(1, 0L));
                }
                int lvl = level_data.get(type).left;
                if (lvl < getMaxLevelData(type)) {
                    //System.out.println("exp: " + exp);
                    double bexp = exp;
                    if (lvl > 9999) {
                        bexp = Randomizer.Lmin((long) (exp * Math.pow(0.9999, lvl - 9999)), 1);
                    }
                    double bGain = (double) (level_data.get(type).right) + Math.floor(bexp * getStat().getMasterBuff() * GameConstants.getMasteryRate());
                    level_data.get(type).right = (long) Randomizer.DoubleMinMax(bGain, 0, Long.MAX_VALUE);
                    //System.out.println("exp2: " + gain);
                    if (level_data.get(type).right >= getLevelDataNeededExp(lvl)) {
                        level_data.get(type).left += 1;
                        level_data.get(type).right = 0L;
                        getClient().announce(EffectPacket.showForeignEffect(35));
                        getLevelDataMsg(type, level_data.get(type).left);
                        saveLevelDataType(type);
                        recalcLocalStats();
                    }
                }
            } finally {
                level_data_lock.unlock();
            }
        }
    }

    public void gainLevelsData(int type, int amount) {
        level_data_lock.lock();
        try {

            if (!level_data.containsKey(type)) {
                level_data.put(type, new Pair<Integer, Long>(1, 0L));
            }
            if (level_data.get(type).left + amount <= getMaxLevel()) {
                level_data.get(type).left += amount;
                level_data.get(type).right = 0L;
                getClient().announce(EffectPacket.showForeignEffect(35));
                getLevelDataMsg(type, level_data.get(type).left);
                saveLevelDataType(type);
                recalcLocalStats();
            }
        } finally {
            level_data_lock.unlock();
        }
    }

    public void setLevelsData(int type, int amount) {
        level_data_lock.lock();
        try {

            if (!level_data.containsKey(type)) {
                level_data.put(type, new Pair<Integer, Long>(1, 0L));
            }
            if (amount <= getMaxLevel()) {
                level_data.get(type).left = amount;
                level_data.get(type).right = 0L;
                getClient().announce(EffectPacket.showForeignEffect(35));
                getLevelDataMsg(type, level_data.get(type).left);
                saveLevelDataType(type);
                recalcLocalStats();
            }
        } finally {
            level_data_lock.unlock();
        }
    }

    public boolean changeLevelsData(int type1, int type2) {
        level_data_lock.lock();
        try {
            if (level_data.containsKey(type1)) {
                int dlevel = level_data.get(type1).left;
                level_data.get(type1).left = 0;
                level_data.get(type1).right = 0L;
                if (!level_data.containsKey(type2)) {
                    level_data.put(type2, new Pair<Integer, Long>(dlevel, 0L));
                } else {
                    level_data.get(type2).left = dlevel;
                    level_data.get(type2).right = 0L;
                }
                getClient().announce(EffectPacket.showForeignEffect(35));
                getLevelDataMsg(type2, level_data.get(type2).left);
                dropMessage("Weapon Mastery successfully changed to " + getWeaponName(type2));
                saveLevelData();
                recalcLocalStats();
                return true;
            }
        } finally {
            level_data_lock.unlock();
        }
        return false;
    }

    public void gainLevelData(int type, long exp, int rounds) {
        while (rounds > 0) {
            gainLevelData(type, exp);
            rounds--;
        }

    }

    public void getLevelDataMsg(int type, int level) {
        if (type >= 30 && type <= 53) {
            double base = (int) ((weaponType + (level * 0.01)) * 100.0);
            switch (type) {
                case 30 -> {
                    dropTopMessage("Your Mastery with One-Handed Swords increased to " + base + "%");
                }
                case 31 -> {
                    dropTopMessage("Your Mastery with One-Handed Axes increased to " + base + "%");
                }
                case 32 -> {
                    dropTopMessage("Your Mastery with One-Handed Blunts increased to " + base + "%");
                }
                case 33 -> {
                    dropTopMessage("Your Mastery with Daggers increased to " + base + "%");
                }
                case 37 -> {
                    dropTopMessage("Your Mastery with Wands increased to " + base + "%");
                }
                case 38 -> {
                    dropTopMessage("Your Mastery with Staves increased to " + base + "%");
                }
                case 40 -> {
                    dropTopMessage("Your Mastery with Two-Handed Swords increased to " + base + "%");
                }
                case 41 -> {
                    dropTopMessage("Your Mastery with Two-Handed Axes increased to " + base + "%");
                }
                case 42 -> {
                    dropTopMessage("Your Mastery with Two-Handed Blunts increased to " + base + "%");
                }
                case 43 -> {
                    dropTopMessage("Your Mastery with Spears increased to " + base + "%");
                }
                case 44 -> {
                    dropTopMessage("Your Mastery with Pole Arms increased to " + base + "%");
                }
                case 45 -> {
                    dropTopMessage("Your Mastery with Bow increased to " + base + "%");
                }
                case 46 -> {
                    dropTopMessage("Your Mastery with Cross Bows increased to " + base + "%");
                }
                case 47 -> {
                    dropTopMessage("Your Mastery with Claws increased to " + base + "%");
                }
                case 48 -> {
                    dropTopMessage("Your Mastery with Knuckles increased to " + base + "%");
                }
                case 49 -> {
                    dropTopMessage("Your Mastery with Guns increased to " + base + "%");
                }
                case 52 -> {
                    dropTopMessage("Your Mastery with Dual-Bows increased to " + base + "%");
                }
                case 53 -> {
                    dropTopMessage("Your Mastery with Cannons increased to " + base + "%");
                }
            }
        }
        if (type >= 100) {
            switch (type) {
                case 100 -> {
                    dropTopMessage("Your Elwin Power has increased to " + level);
                }
                case 101 -> {
                    dropTopMessage("Your Gambling Power has increased to " + level);
                }
                case 102 -> {
                    dropTopMessage("Your Commerci Power has increased to " + level);
                }
                case 103 -> {
                    dropTopMessage("Your Shadow Power has increased to " + level);
                }
                case 104 -> {
                    dropTopMessage("Your Meso Power has increased to " + level);
                }
                case 105 -> {
                    dropTopMessage("Your Fel Power has increased to " + level);
                }
                case 106 -> {
                    dropTopMessage("Your Fishing Power has increased to " + level);
                }
                case 107 -> {
                    dropTopMessage("Your Pet Power has increased to " + level);
                }
                case 108 -> {
                    dropTopMessage("Your Pal Mastery has increased to " + level);
                }
                case 109 -> {
                    dropTopMessage("Your Monster Mastery has increased to " + level);
                }
                case 200 -> {
                    dropTopMessage("Your Paragon has increased to " + level);
                }
                case 999 -> {
                    dropTopMessage("You have gained a Super Level Current S-Level: " + level);
                }
            }
        }
    }

    public String getWeaponName() {
        int type = getWeaponType();
        switch (type) {
            case 30 -> {
                return "One-Handed Swords";
            }
            case 31 -> {
                return "One-Handed Axes";
            }
            case 32 -> {
                return "One-Handed Blunts";
            }
            case 33 -> {
                return "Daggers";
            }
            case 37 -> {
                return "Wands";
            }
            case 38 -> {
                return "Staves";
            }
            case 40 -> {
                return "Two-Handed Swords";
            }
            case 41 -> {
                return "Two-Handed Axes";
            }
            case 42 -> {
                return "Two-Handed Blunts";
            }
            case 43 -> {
                return "Spears";
            }
            case 44 -> {
                return "Pole Arms";
            }
            case 45 -> {
                return "Bow";
            }
            case 46 -> {
                return "Cross Bows";
            }
            case 47 -> {
                return "Claws";
            }
            case 48 -> {
                return "Knuckles";
            }
            case 49 -> {
                return "Guns";
            }
            case 52 -> {
                return "Dual-Bows";
            }
            case 53 -> {
                return "Cannons";
            }
        }
        return "Un-armed";
    }

    public String getWeaponName(int type) {
        switch (type) {
            case 30 -> {
                return "One-Handed Swords";
            }
            case 31 -> {
                return "One-Handed Axes";
            }
            case 32 -> {
                return "One-Handed Blunts";
            }
            case 33 -> {
                return "Daggers";
            }
            case 37 -> {
                return "Wands";
            }
            case 38 -> {
                return "Staves";
            }
            case 40 -> {
                return "Two-Handed Swords";
            }
            case 41 -> {
                return "Two-Handed Axes";
            }
            case 42 -> {
                return "Two-Handed Blunts";
            }
            case 43 -> {
                return "Spears";
            }
            case 44 -> {
                return "Pole Arms";
            }
            case 45 -> {
                return "Bow";
            }
            case 46 -> {
                return "Cross Bows";
            }
            case 47 -> {
                return "Claws";
            }
            case 48 -> {
                return "Knuckles";
            }
            case 49 -> {
                return "Guns";
            }
            case 52 -> {
                return "Dual-Bows";
            }
            case 53 -> {
                return "Cannons";
            }
            case 100 -> {
                return "Elwin";
            }
            case 101 -> {
                return "Gambling";
            }
            case 102 -> {
                return "Commceri";
            }
            case 103 -> {
                return "Shadow";
            }
            case 104 -> {
                return "Meso";
            }
            case 105 -> {
                return "Fel";
            }
            case 106 -> {
                return "Fishing";
            }
            case 107 -> {
                return "Pet";
            }
            case 108 -> {
                return "Pal";
            }
            case 109 -> {
                return "Monster";
            }
            case 200 -> {
                return "Paragon";
            }
            case 999 -> {
                return "Super Level";
            }
        }
        return "Un-armed";
    }

    public long getLevelDataExp(int type) {
        if (type != 0) {
            level_data_lock.lock();
            try {
                if (level_data.containsKey(type)) {
                    return level_data.get(type).getRight();
                }
            } finally {
                level_data_lock.unlock();
            }
        }
        return 1;
    }

    public int getLevelDataLvl(int type) {
        if (type != 0) {
            level_data_lock.lock();
            try {
                if (level_data.containsKey(type)) {
                    return level_data.get(type).getLeft();
                }
            } finally {
                level_data_lock.unlock();
            }
        }
        return 0;
    }

    public int getLevelExpDataPerc(int type) {
        if (type != 0) {
            level_data_lock.lock();
            try {
                if (level_data.containsKey(type)) {
                    return (int) (((double) level_data.get(type).getRight() / (double) getLevelDataNeededExp(level_data.get(type).getLeft())) * 100);
                }
            } finally {
                level_data_lock.unlock();
            }
        }
        return 0;
    }

    public int getLevelDataPerc(int type) {
        return getLevelDataLvl(type);
    }

    public int getBaseLevelDataWeaponPerc() {
        return (int) (getLevelDataLvl(getWeaponType()));
    }

    public int getBaseLevelDataWeaponPerc(int type) {
        return (int) (level_data.get(type).getLeft());
    }

    public int getLevelDataWeaponPerc() {
        return (int) ((1.0 + (getLevelDataLvl(getWeaponType()) * 0.01)) * 100.0);
    }

    public int getLevelDataWeaponPerc(int type) {
        return (int) ((1.0 + (level_data.get(type).getLeft() * 0.01)) * 100.0);
    }

    public int getLevelData() {
        return getLevelDataLvl(getWeaponType());
    }

    public double getLevelDataWeapon() {
        return 1.0 + (getLevelDataLvl(getWeaponType()) * 0.01);
    }

    public double getParagon() {
        return 1.0 + (getLevelDataLvl(200) * 0.01);
    }

    public double getParagonLevel() {
        return getLevelDataLvl(200);
    }

    public double getLevelDataWeapon(int type) {
        return type + (level_data.get(type).getLeft() * 0.01);
    }

    public int getWeaponType() {
        if (getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11) != null) {
            int wid = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11).getItemId();
            int cat = (wid / 10000) % 100;
            return cat;
        }
        return 0;
    }

    public boolean compareVar(String name, long value) {
        if (!var.containsKey(name)) {
            var.put(name, (long) 0);
        }
        return var.get(name) < value;
    }

    public void setVar(String name, long value) {
        if (!var.containsKey(name)) {
            var.put(name, (long) 0);
        }
        var.put(name, value);
    }

    public void setSavedVar(String name, long value) {
        if (!var.containsKey(name)) {
            var.put(name, (long) 0);
        }
        var.put(name, value);
        saveVarData();
    }

    public void addVar(String name, long value) {
        if (!var.containsKey(name)) {
            var.put(name, (long) 0);
        }
        var.put(name, var.get(name) + value);
    }

    public long getVar(String name) {
        if (var.get(name) != null) {
            return var.get(name);
        } else {
            return -1;
        }
    }

    public long getVarZero(String name) {
        if (var.get(name) != null) {
            return var.get(name);
        } else {
            return 0;
        }
    }

    public void removeVar(String name) {
        if (var.containsKey(name)) {
            var.remove(name);
        }
    }

    public double getRewardScale() {
        if (var.containsKey("Magic_Pot")) {
            return (System.currentTimeMillis() - var.get("Magic_Pot")) / 3600000.0;
        }
        return 1.0;
    }

    public void loadVarData() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_variables` WHERE `charid` = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    var.put(rs.getString("var"), rs.getLong("amount"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveVarData() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO character_variables (charid, `var`, `amount`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `amount` = VALUES(`amount`)")) {
            ps.setInt(1, id);
            for (String type : var.keySet()) {
                ps.setString(2, type);
                ps.setLong(3, var.get(type));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getRankVariable(String var) {
        String list = "";
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_variables` WHERE var = ? ORDER BY `amount` DESC LIMIT 0, 50")) {
                ps.setString(1, var);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String star;
                    if (rs.getRow() == 1) {
                        star = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
                    } else {
                        star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
                    }
                    list += star + "[#" + rs.getRow() + "] #r" + getCharById(con, rs.getInt("charid")) + "#k - #bWaves cleared: " + rs.getLong("amount") + "#k\r\n";
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error handling guildRanking");
            e.printStackTrace();
        }
        return list;
    }

    public String getRankById(String var) {
        String list = "";
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_variables` WHERE var = ? ORDER BY `amount` DESC LIMIT 0, 50")) {
                ps.setString(1, var);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String star;
                    if (rs.getRow() == 1) {
                        star = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
                    } else {
                        star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
                    }
                    list += star + "[#" + rs.getRow() + "] #r" + getCharById(con, rs.getInt("charid")) + "#k - #bRank: " + rs.getLong("amount") + "#k\r\n";
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error handling guildRanking");
            e.printStackTrace();
        }
        return list;
    }

    public boolean compareAccVar(String name, long value) {
        if (!accountvar.containsKey(name)) {
            accountvar.put(name, (long) 0);
        }
        return accountvar.get(name) < value;
    }

    public void setAccVar(String name, long value) {
        if (!accountvar.containsKey(name)) {
            accountvar.put(name, (long) 0);
        }
        accountvar.put(name, value);
    }

    public void setSavedAccVar(String name, long value) {
        if (!accountvar.containsKey(name)) {
            accountvar.put(name, (long) 0);
        }
        accountvar.put(name, value);
        saveVarAccData();
    }

    public void addAccVar(String name, long value) {
        if (!accountvar.containsKey(name)) {
            accountvar.put(name, (long) 0);
        }
        accountvar.put(name, accountvar.get(name) + value);
    }

    public long getAccVar(String name) {
        if (accountvar.get(name) != null) {
            return accountvar.get(name);
        } else {
            return -1;
        }
    }

    public long getAccVara(String name) {
        if (accountvar.get(name) != null) {
            return accountvar.get(name);
        } else {
            return 0;
        }
    }

    public void removeAccVar(String name) {
        if (accountvar.containsKey(name)) {
            accountvar.remove(name);
        }
    }

    public double getAccRewardScale() {
        if (accountvar.containsKey("Magic_Pot")) {
            double hour = 1000 * 60 * 60;
            double time = System.currentTimeMillis() - accountvar.get("Magic_Pot");
            return Randomizer.DoubleMinMax(time / hour, 1.0, 168.0);
        }
        return 1.0;
    }

    public void loadVarAccData() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `account_variables` WHERE `account` = ?");
            ps.setInt(1, accountid);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    accountvar.put(rs.getString("var"), rs.getLong("amount"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveVarAccData() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO account_variables (account, `var`, `amount`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `amount` = VALUES(`amount`)")) {
            ps.setInt(1, accountid);
            for (String type : accountvar.keySet()) {
                ps.setString(2, type);
                ps.setLong(3, accountvar.get(type));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void upgradeJob(String job) {
        if (getVarZero(job) < 100) {
            addVar(job, 1);
            if (getVarZero(job) >= 100) {
                finishAchievement(10000 + Integer.parseInt(job));
            }
            updateJobTier();
        }
    }

    public void switchJob(int job) {
        getClient().announce(CField.UIPacket.IntroLock(true));
        getClient().announce(CField.UIPacket.IntroDisableUI(true));
        saveCurrentKeys(getJob());
        changeJob(job);
        defaultKeys(job);
        if (job == 3312) {
            setJag();
        }
        changeKeys();
        skillMacros = new SkillMacro[5];
        client.announce(CField.getMacros(skillMacros));
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?", id);
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }
        getClient().announce(CField.UIPacket.IntroLock(false));
        getClient().announce(CField.UIPacket.IntroDisableUI(false));
        getClient().announce(CField.getPublicNPCInfo());
        updateJobTier();
        updateSingleStat(MapleStat.AVAILABLESP, 0); // we don't care the value here
        saveCharToDB();
    }

    public void switchJobs(int job) {
        changeJob(job);
        if (job == 3312) {
            setJag();
        }
        saveCharToDB();
    }

    public void gainEquipStat(Equip eqp, int type, int amount) {
        if (eqp != null) {
            switch (type) {
                case 1 -> //str
                    eqp.setStr(Randomizer.Max(eqp.getStr() + (eqp.getPower() * amount), 2000000000));
                case 2 -> //dex
                    eqp.setDex(Randomizer.Max(eqp.getDex() + (eqp.getPower() * amount), 2000000000));
                case 3 -> //int
                    eqp.setInt(Randomizer.Max(eqp.getInt() + (eqp.getPower() * amount), 2000000000));
                case 4 -> //luk
                    eqp.setLuk(Randomizer.Max(eqp.getLuk() + (eqp.getPower() * amount), 2000000000));
                case 5 -> //def
                    eqp.setWdef(Randomizer.Max(eqp.getWdef() + (eqp.getPower() * amount), 2000000000));
                case 6 -> //mdef
                    eqp.setMdef(Randomizer.Max(eqp.getMdef() + (eqp.getPower() * amount), 2000000000));
                case 7 -> //atk
                    eqp.setWatk(Randomizer.Max(eqp.getWatk() + (eqp.getPower() * amount), 2000000000));
                case 8 -> //matk
                    eqp.setMatk(Randomizer.Max(eqp.getMatk() + (eqp.getPower() * amount), 2000000000));
                case 10 -> //OP
                    eqp.setOverPower(Randomizer.Max(eqp.getOverPower() + ((int) (1 + (eqp.getPower() * amount * 0.1))), 999999999));
                case 11 -> //TD
                    eqp.setTotalDamage(Randomizer.Max(eqp.getTotalDamage() + ((int) (1 + (eqp.getPower() * amount * 0.1))), 999999999));
                case 12 -> //BD
                    eqp.setBossDamage(Randomizer.Max(eqp.getBossDamage() + ((int) (1 + (eqp.getPower() * amount * 0.1))), 999999999));
                case 13 -> //IED
                    eqp.setIED(Randomizer.Max(eqp.getIED() + ((int) (1 + (eqp.getPower() * amount * 0.1))), 999999999));
                case 14 -> //CRIT
                    eqp.setCritDamage(Randomizer.Max(eqp.getCritDamage() + ((int) (1 + (eqp.getPower() * amount * 0.1))), 999999999));
                case 15 -> //AS
                    eqp.setAllStat(Randomizer.Max(eqp.getAllStat() + ((int) (1 + (eqp.getPower() * amount * 0.1))), 999999999));
            }
            eqp.setUpgradeSlots((short) (eqp.getUpgradeSlots() - amount));
            client.announce(InventoryPacket.updateSpecialItemUse_(eqp, (byte) 1, this));
            equipChanged();
            getClient().announce(EffectPacket.showForeignEffect(35));
            getMap().broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 35), false);
        }
    }

    public boolean hasBonusPot(Equip eqp, int type) {
        switch (type) {
            case 10:
                return eqp.getOverPower() > 0;
            case 11:
                return eqp.getTotalDamage() > 0;
            case 12:
                return eqp.getBossDamage() > 0;
            case 13:
                return eqp.getIED() > 0;
            case 14:
                return eqp.getCritDamage() > 0;
            case 15:
                return eqp.getAllStat() > 0;
        }
        return false;
    }

    public double getBonusPot(Equip eqp, int type) {
        switch (type) {
            case 10:
                return eqp.getOverPower();
            case 11:
                return eqp.getTotalDamage();
            case 12:
                return eqp.getBossDamage();
            case 13:
                return eqp.getIED();
            case 14:
                return eqp.getCritDamage();
            case 15:
                return eqp.getAllStat();
        }
        return 0;
    }

    public boolean getOffline() {
        return offline;
    }

    public void enableHelper() {
        if (!hasSummon()) {
            setHasSummon(true);
        }
        if (!GameConstants.isBeginnerJob(getJob())) {
            getClient().announce(CField.playSound("Zelda/Hey"));
            client.announce(CField.UIPacket.summonMessage("Hello, I am here to provide random tool tips to help your journey!", 200, 10));
        }
    }

    public String handleTip() {
        getClient().announce(CField.playSound("Zelda/Hey"));
        String tip = "";
        if (!achievementFinished(226)) {
            return "Work on completeing Tutorial Zone. Using Gacha Tickets to gain new equips.";
        }
        if (!achievementFinished(270)) {
            int choice = Randomizer.random(1, 6);
            switch (choice) {
                case 1 ->
                    tip = "Work on leveling in Party Easy Mode and building up.";
                case 2 ->
                    tip = "Work on Wanted Poster quests to gain easy Energy Charges";
                case 3 ->
                    tip = "Work on farming Monster park coins and turn them into Agent E in various Towns";
                case 4 ->
                    tip = "You Room 4 to scroll and EE gear.";
                case 5 ->
                    tip = "Clear Easy Party Mode, may take few attempts.";
                case 6 ->
                    tip = "Work on Randolf's Quests.";
            }
            return tip;
        }
        int choice = Randomizer.random(1, 14);
        switch (choice) {
            case 1 ->
                tip = "Use @skin to apply skins. All skin stats apply reguardless of active or not.";
            case 2 ->
                tip = "Room 1 handles all the Maple Pals.";
            case 3 ->
                tip = "Room 2 is the place to AFK and fish to recover stamina over time.";
            case 4 ->
                tip = "Room 3 handles all Damage skin systems.";
            case 5 ->
                tip = "Room 4 handles all equip upgrading";
            case 6 ->
                tip = "Room 5 handles all the Guild stuff.";
            case 7 ->
                tip = "Room 6 is where you will spend your life grinding away.";
            case 8 ->
                tip = "Room 7 is the Donation Room, where you can help support the server and buy amazing rewards.";
            case 9 ->
                tip = "Maple Pals stats carry over to players.";
            case 10 ->
                tip = "Dont forget to claim your free daily rewards at Donation Box in Main Hall.";
            case 11 ->
                tip = "Demain in Main Hall can quickly warp you to where you need to go.";
            case 12 ->
                tip = "Wu Yaun sells NX items for maple points found from very dungeon monsters.";
            case 13 ->
                tip = "Cody in Main Hall handles character cosmetic looks.";
            case 14 ->
                tip = "Complete Tutorial or Random (Hell) dungeons to aquire Gacha Tickets for Equips.";
        }
        return tip;
    }

    public void claimShopItems() {
        if (!getShopItems().isEmpty()) {
            if (gainAllShopItems()) {
                dropMessage(1, "All Shop items have been returned");
                saveToDB();
            } else {
                dropMessage(1, "Make sure you have room for 48 Items in Equip, Use, and ETC.");
            }
        } else {
            dropMessage(1, "You have no shop items to claim.");
        }
    }

    public MaplePalStorage getPalStorage() {
        return palStorage;
    }

    public int getAvgPalLevel() {
        return getPalStorage().getAvgLevel();
    }

    public int getPalLevel() {
        return getPalStorage().getHighLevel();
    }

    public boolean getAvgPalLevel(int level, int size) {
        return getPalStorage().getAvgLevel() == level && getPalStorage().getActivePals().size() == size;
    }

    public boolean canHoldEgg() {
        Collection<MaplePal> eggs = getPalStorage().getStoredPals();
        if (eggs.stream().filter(MaplePal::isEgg).toList().size() < 99) {
            return true;
        }
        return false;
    }

    public boolean canHoldPal() {
        Collection<MaplePal> eggs = getPalStorage().getStoredPals();
        if (eggs.size() < 999) {
            int total = eggs.stream().filter(MaplePal::isNotEgg).toList().size() + getPalStorage().getHatchingEggs().size() + getPalStorage().getActivePals().size();
            if (total < getPalSlots()) {
                return true;
            }
        }
        return false;
    }

    public void makeEgg() {
        if (canHoldEgg()) {
            MaplePal.createEgg(this);
            dropColorMessage(1, "[Maple Pal] New egg has been created into your Pal Box!");
        }
    }

    public void makeEgg(int level) {
        if (canHoldEgg()) {
            MaplePal.createEgg(this, level);
            dropColorMessage(1, "[Maple Pal] New egg has been created into your Pal Box!");
        }
    }

    public void makeEgg(int pal, int level) {
        if (canHoldEgg()) {
            MaplePal.createEgg(this, pal, level);
        }
    }

    public void makeForcedEgg(int pal, int level) {
        MaplePal.createEgg(this, pal, level);
    }

    public void makeEggEvo(int evo, int level) {
        if (canHoldEgg()) {
            MaplePal.createEggFromEvo(this, evo, level);
        }
    }

    public boolean createEggBaby(MaplePal male, MaplePal female) {
        if (canHoldEgg()) {
            MaplePal.createEggFromParents(this, male, female);
            return true;
        }
        return false;
    }

    public int getHatchSlots() {
        return (int) getAccVar("Hatch_Slot");
    }

    public void setHatchSlots(int value) {
        setAccVar("Hatch_Slot", Randomizer.Max((int) (getAccVar("Hatch_Slot") + value), 99));
    }

    public int getPalSlots() {
        if (getAccVar("Pal_Slot") < 27) {
            setAccVar("Pal_Slot", 27);
        }
        return (int) getAccVar("Pal_Slot");
    }

    public void setPalSlots(int value) {
        int slots = (int) getAccVar("Pal_Slot");
        setAccVar("Pal_Slot", Randomizer.Max(slots + value, 800));
    }

    public int getMaxCombo() {
        return (int) (999 + getAccVara("Combo"));
    }

    public void addCombo(int value) {
        setAccVar("Combo", getAccVara("Combo") + value);
    }

    public MaplePal getActivePal() {
        return getPalStorage().getPal(getAccVar("active_pal"));
    }

    public boolean isActivePal(MaplePal pal) {
        return pal == getActivePal();
    }

    public int getPalIVBoost(int value) {
        return Randomizer.random(1, 100);
    }

    public void loadPals() {
        if (getAccVar("Hatch_Slot") < 7) {
            setAccVar("Hatch_Slot", 7);
        }
        try (Connection con = DatabaseConnection.getWorldConnection();) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM player_pals WHERE template_id = 0 and charid = ?");
            ps.setInt(1, accountid);
            ps.executeUpdate();
            ps.close();
            palStorage = new MaplePalStorage(accountid);
            palStorage.load(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getElement() {
        int e = (int) getVar("element");
        if (e == -1) {
            e = 8;
        }
        return e;
    }

    public void saveActivePals() {
        getPalStorage().saveActivePals();
    }

    public void checkPalAch() {
        long wins = getAccVara("Pal_Win");
        if (wins >= 1) {
            finishAchievement(3002);
        }
        if (wins >= 10) {
            finishAchievement(3003);
        }
        if (wins >= 25) {
            finishAchievement(3004);
        }
        if (wins >= 50) {
            finishAchievement(3005);
        }
        if (wins >= 100) {
            finishAchievement(3006);
        }
        if (wins >= 250) {
            finishAchievement(3007);
        }
        if (wins >= 500) {
            finishAchievement(3008);
        }
        if (wins >= 1000) {
            finishAchievement(3009);
        }
        if (wins >= 2500) {
            finishAchievement(3010);
        }
        if (wins >= 5000) {
            finishAchievement(3011);
        }
        if (wins >= 10000) {
            finishAchievement(3012);
        }
        if (wins >= 25000) {
            finishAchievement(3013);
        }
        if (wins >= 50000) {
            finishAchievement(3014);
        }
    }

    public boolean canAddMap(int mapId) {
        if (getEventInstance() != null) {
            return false;
        }
        if (maps.contains(mapId)) {
            return false;
        }
        if (!MapleMapFactory.isRealMap(mapId)) {
            return false;
        }
        return maps.size() < getVarZero("MAPS");
    }

    public List<Integer> getMaps() {
        return maps;
    }

    public void updateMap(int o, int n) {
        removeMap(o);
        addMap(n);
    }

    public void removeMap(int id) {
        if (maps.contains(id)) {
            maps.remove(Integer.valueOf(id));
        }
    }

    public void addMap(int id) {
        if (!maps.contains(id)) {
            maps.add(id);
        }
    }

    public void clearMaps() {
        maps.clear();
        saveMaps();
    }

    public void loadMaps() {
        long count = getVarZero("MAPS");
        if (count < 10) {
            setVar("MAPS", 10);
        }
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `character_maps` WHERE `charid` = ?");
            ps.setInt(1, getId());
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    int mapid = rs.getInt("mapid");
                    if (MapleMapFactory.isRealMap(mapid)) {
                        maps.add(mapid);
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveMaps() {
        try (Connection con = DatabaseConnection.getPlayerConnection();) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM character_maps WHERE charid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO `character_maps` VALUES(?, ?)");
            ps.setInt(1, getId());
            for (int mid : maps) {
                if (MapleMapFactory.isRealMap(mid)) {
                    ps.setInt(2, mid);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isRealMap(int mapId) {
        return MapleMapFactory.isRealMap(mapId);
    }

    public String getMapName(int mapId) {
        return MapleMapFactory.getRealMapName(mapId);
    }

    public int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public String getCurrentDay() {
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return "None";
    }

    public String getLastDay() {
        int day = (int) getAccVara("DAY");
        switch (day) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return "None";
    }

    public boolean checkDay() {
        return getDay() == getAccVara("DAY");
    }

    public void handleReborn() {
        //achievementFinished(440) && 
        if (getTotalLevel() >= 9000) {
            setTotalLevel(1000);
            overexp = BigInteger.ZERO;
            exp.set(0);
            addVar("reborn", 1);
            map.broadcastMessage(this, EffectPacket.showForeignEffect(getId(), 0), false);
            updateChar();
            changeMapbyId(100000000);
            int rb = (int) getReborns();
            if (rb <= 50) {
                int ach = (int) (6699 + rb);
                finishAchievement(ach);
            }
            if (rb >= 250) {
                finishAchievement(6753);
            }
            if (rb >= 200) {
                finishAchievement(6752);
            }
            if (rb >= 150) {
                finishAchievement(6751);
            }
            if (rb >= 100) {
                finishAchievement(6750);
            }
            String msg = getName() + " has successfully reborned - Current Reborns: " + rb;
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, msg));
            if (rb >= 50) {
                gainDamageSkinNoOrb(7000);
            }
            reborns = rb;
            saveCharToDB();
        }
    }

    public long getReborns() {
        return getVarZero("reborn");
    }

    public void worldMsg(String msg) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, msg));
    }

    public List<Equip> getStorageEquips() {
        return equips;
    }

    public void loadItemsStorage() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            //load storage id
            PreparedStatement ps2 = con.prepareStatement("SELECT storageid FROM storages WHERE accountid = ?");
            ps2.setInt(1, accountid);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                storageId = rs2.getInt("storageid");
                rs2.close();
                ps2.close();
            }
            //storage
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventorystorage` WHERE `accountid` = ?");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, storageId);
            rs = ps.executeQuery();

            while (rs.next()) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (mit == MapleInventoryType.EQUIP) {
                    Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                    equip.setQuantity((short) 1);
                    equip.setInventoryId(rs.getLong("inventoryitemid"));
                    equip.setOwner(rs.getString("owner"));
                    equip.setExpiration(rs.getLong("expiredate"));
                    equip.setUpgradeSlots(rs.getShort("upgradeslots"));
                    equip.setLevel(rs.getShort("level"));
                    equip.setStr(rs.getInt("str"));
                    equip.setDex(rs.getInt("dex"));
                    equip.setInt(rs.getInt("int"));
                    equip.setLuk(rs.getInt("luk"));
                    equip.setWatk(rs.getInt("watk"));
                    equip.setMatk(rs.getInt("matk"));
                    equip.setWdef(rs.getInt("wdef"));
                    equip.setMdef(rs.getInt("mdef"));
                    equip.setAcc(rs.getInt("acc"));
                    equip.setAvoid(rs.getInt("avoid"));
                    equip.setHands(rs.getInt("hands"));
                    equip.setSpeed(rs.getInt("speed"));
                    equip.setJump(rs.getInt("jump"));
                    equip.setViciousHammer(rs.getByte("ViciousHammer"));
                    equip.setItemEXP(rs.getInt("itemEXP"));
                    equip.setGMLog(rs.getString("GM_Log"));
                    equip.setDurability(rs.getInt("durability"));
                    equip.setEnhance(rs.getInt("enhance"));
                    equip.setPotential1(rs.getInt("potential1"));
                    equip.setPotential2(rs.getInt("potential2"));
                    equip.setPotential3(rs.getInt("potential3"));
                    equip.setPotential4(rs.getInt("potential4"));
                    equip.setPotential5(rs.getInt("potential5"));
                    equip.setSocket1(rs.getInt("socket1"));
                    equip.setSocket2(rs.getInt("socket2"));
                    equip.setSocket3(rs.getInt("socket3"));
                    equip.setGiftFrom(rs.getString("sender"));
                    equip.setIncSkill(rs.getInt("incSkill"));
                    equip.setPVPDamage(rs.getInt("pvpDamage"));
                    equip.setCharmEXP(rs.getInt("charmEXP"));
                    equip.setOverPower(rs.getInt("overpower"));
                    equip.setTotalDamage(rs.getInt("totaldamage"));
                    equip.setBossDamage(rs.getInt("bossdamage"));
                    equip.setIED(rs.getInt("ied"));
                    equip.setCritDamage(rs.getInt("critdamage"));
                    equip.setAllStat(rs.getInt("allstat"));
                    equip.setPower(rs.getInt("power"));
                    equip.setOStr(rs.getLong("ostr"));
                    equip.setODex(rs.getLong("odex"));
                    equip.setOInt(rs.getLong("oint"));
                    equip.setOLuk(rs.getLong("oluk"));
                    equip.setOAtk(rs.getLong("oatk"));
                    equip.setOMatk(rs.getLong("omatk"));
                    equip.setODef(rs.getLong("odef"));
                    equip.setOMdef(rs.getLong("omdef"));
                    if (equip.getCharmEXP() < 0) { //has not been initialized yet
                        equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                    }
                    if (equip.getUniqueId() > -1) {
                        if (equip.getItemId() / 10000 == 166) {
                            MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                            if (ring != null) {
                                equip.setAndroid(ring);
                            }
                        }
                    }
                    equips.add(equip);
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getHash() {
        return hash;
    }

    public void changePassword(String pwd) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET password = ?, salt = ? WHERE id = ?")) {
                //ps.setString(2, LoginCrypto.hexSha1(pwd));
                final String newSalt = LoginCrypto.makeSalt();
                ps.setString(1, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
                ps.setString(2, newSalt);
                ps.setInt(3, getAccountID());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void fix() {
        getClient().removeClickedNPC();
        NPCScriptManager.getInstance().dispose(getClient());
        getClient().announce(CWvsContext.enableActions());
        setConversation(0);
        open(false);
        storageOpen(false);
        getClient().setChat(false);
        battle = false;
    }

    public void executeJob() {
        fix();
        NPCScriptManager.getInstance().start(getClient(), 9010009, "random_job");
    }

    public void canUseJob() {
        if (!isGMJob()) {
            if (getVarZero(String.valueOf(job)) <= 0) {
                switchJob(910);
            }
        }
    }

    public void updateStat(int type, int amount) {
        Map<MapleStat, Integer> statupdate = new EnumMap<>(MapleStat.class);
        PlayerStats stat = getStat();
        switch (type) {
            case 1 -> {
                stat.setStr(stat.getStr() + amount, this);
                statupdate.put(MapleStat.STR, stat.getLuk());
            }
            case 2 -> {
                stat.setDex(stat.getDex() + amount, this);
                statupdate.put(MapleStat.DEX, stat.getLuk());
            }
            case 3 -> {
                stat.setInt(stat.getInt() + amount, this);
                statupdate.put(MapleStat.INT, stat.getLuk());
            }
            case 4 -> {
                stat.setLuk(stat.getLuk() + amount, this);
                statupdate.put(MapleStat.LUK, stat.getLuk());
            }
            case 5 -> {
                long maxhp = stat.getMaxHp() + amount;
                int maxmp = stat.getMaxMp() + amount;
                int fmaxhp = (int) Randomizer.LongMax(maxhp, GameConstants.getMaxHpMp());
                stat.setMaxHp(fmaxhp, this);
                statupdate.put(MapleStat.HP, fmaxhp);
                statupdate.put(MapleStat.MAXHP, fmaxhp);
                int fmaxmp = Randomizer.Max(maxmp, GameConstants.getMaxMp(job));
                stat.setMaxMp(fmaxmp, this);
                statupdate.put(MapleStat.MP, fmaxmp);
                statupdate.put(MapleStat.MAXMP, fmaxmp);
            }
        }
        client.announce(CWvsContext.updatePlayerStats(statupdate, this));
        getStat().recalcLocalStats(this);
        client.announce(CField.customMainStatUpdate(this));
        client.announce(CField.customStatDetail(this));
    }

    public List<Integer> randomStatsOrder() {
        List<Integer> nStats = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        return nStats;
    }

    public List<Integer> randomStats() {
        List<Integer> nStats = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        Collections.shuffle(nStats);
        return nStats;
    }

    public void AssignHPMP(int value, boolean hp) {
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        final PlayerStats stat = getStat();
        int amount = value;
        if (hp) {
            long maxhp = stat.getMaxHp() + amount;
            int fmaxhp = (int) Randomizer.LongMax(maxhp, GameConstants.getMaxHpMp());
            stat.setMaxHp(fmaxhp, this);
            statupdate.put(MapleStat.HP, fmaxhp);
            statupdate.put(MapleStat.MAXHP, fmaxhp);
        } else {
            int maxmp = stat.getMaxMp() + amount;
            int fmaxmp = Randomizer.Max(maxmp, GameConstants.getMaxMp(job));
            stat.setMaxMp(fmaxmp, this);
            statupdate.put(MapleStat.MP, fmaxmp);
            statupdate.put(MapleStat.MAXMP, fmaxmp);
        }
        client.announce(CWvsContext.updatePlayerStats(statupdate, this));
        getStat().recalcLocalStats(this);
        client.announce(CField.customMainStatUpdate(this));
        client.announce(CField.customStatDetail(this));
        Heal();
    }

    public void changeKeybinding(int key, byte type, int action) {
        if (type != 0) {
            keylayout.addKeyLayout(getJob(), key, type, action);
        } else {
            keylayout.removeKeyLayout(getJob(), key);
        }
        saveCurrentKeys(job);
    }

    public boolean checkKey(int key) {
        return keylayout.checkKey(getJob(), key);
    }

    public void changeKeys() {
        getClient().announce(CField.getKeymap(keylayout, getJob()));
        saveCurrentKeys(job);
    }

    public final MapleKeyLayout getKeyLayout() {
        return keylayout;
    }

    public final void loadKeys() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT `key`,`type`,`action`,`jobid` FROM keymap WHERE characterid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int jobId = rs.getInt("jobid");
                int key = rs.getInt("key");
                byte type = rs.getByte("type");
                int action = rs.getInt("action");
                keylayout.addKeyLayout(jobId, key, type, action);
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error getting character default" + e);
        }
        defaultKeys(getJob());
        client.announce(CField.getKeymap(keylayout, getJob()));
    }

    public void defaultKeys(int job) {
        if (this.keylayout.getLayout(job).isEmpty()) {
            final int[] key = {2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 23, 25, 26, 27, 31, 34, 37, 38, 41, 44, 45, 46, 50, 57, 59, 60, 61, 62, 63, 64, 65, 8, 9, 24, 30};
            final byte[] type = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4, 5, 6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4};
            final int[] action = {10, 12, 13, 18, 6, 11, 8, 5, 0, 4, 1, 19, 14, 15, 3, 17, 9, 20, 22, 50, 51, 52, 7, 53, 100, 101, 102, 103, 104, 105, 106, 16, 23, 24, 2};

            for (int i = 0; i < key.length; i++) {
                keylayout.addKeyLayout(job, key[i], type[i], action[i]);
            }
            saveCurrentKeys(job);
        }
    }

    public void saveCurrentKeys(int job) {
        keylayout.saveKeysbyJob(getId(), job);
    }

    public void itemVac() {
        if (!isStatLock()) {
            if (!GameConstants.getLock() && isAlive() && getMap().noVacMap(getId())) {
                setStatLock(System.currentTimeMillis() + 100);
                int range = (int) (200 + (getAccVara("Pickup")));
                getMap().itemLoot(this, getPosition(), range);
            }
        }
    }

    public void setLastHit() {
        lasthit = System.currentTimeMillis() + 2000;
    }

    public boolean checkLastHit() {
        return System.currentTimeMillis() < lasthit;
    }

    public double getSuperLevelBonus() {
        return 1 + (getLevelDataLvl(999) * 0.001);
    }

    public boolean canPlaceShop() {
        if (getTrade() != null) {
            dropMessage(1, "You may not use shop while trading.");
            return false;
        }
        if (getShop() != null) {
            dropMessage(1, "You may not use shop while trading.");
            return false;
        }
        if (getEventInstance() != null) {
            dropMessage(1, "You may not use shop inside instances.");
            return false;
        }
        if (getMap().shopPlace(getPosition(), 20000)) {
            dropMessage(1, "You may not establish a store here.");
            return false;
        }
        if (!getShopItems().isEmpty()) {
            dropMessage(1, "You have unclaimed items.");
            return false;
        }
        return true;
    }

    public boolean createShop(int store, String title) {
        if (canPlaceShop()) {
            MaplePlayerShop mps = new MaplePlayerShop(this, store, title);
            setPlayerShop(mps);
            getMap().addMapObject(mps);
            getClient().getSession().write(PlayerShopPacket.getPlayerStore(this, true));
            return true;
        } else {
            dropMessage(1, "You may not establish a store here.");
        }
        return false;
    }

    public void checkTotem() {
        int tot = (int) getVarZero("totem");
        if (!haveItem(tot)) {
            setVar("totem", 0);
        }
    }

}
