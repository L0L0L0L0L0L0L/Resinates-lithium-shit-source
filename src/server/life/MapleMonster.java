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
package server.life;

import client.inventory.Equip;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import constants.GameConstants;
import client.Skill;
import client.inventory.Item;
import client.MapleDisease;
import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.MapleClient;
import client.MapleTrait.MapleTraitType;
import handling.channel.ChannelServer;
import client.SkillFactory;
import client.maplepal.MaplePal;
import client.maplepal.PalTemplateProvider;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.channel.handler.DamageParse;
import java.awt.Point;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantLock;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import scripting.EventInstanceManager;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.ServerProperties;
import server.Timer.EtcTimer;
import server.TimerManager;
import server.life.MapleLifeFactory.selfDestruction;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import tools.AttackPair;
import tools.ConcurrentEnumMap;
import tools.Pair;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;

public final class MapleMonster extends AbstractLoadedMapleLife {

    private MapleMonsterStats stats;
    private ChangeableStats ostats = null;
    private long hp, nextKill = 0, lastDropTime = 0;
    private int mp;
    private byte carnivalTeam = -1;
    private MapleMap map;
    private MapleMonster sponge = null;
    private int linkoid = 0, lastNode = -1, highestDamageChar = 0, linkCID = 0; // Just a reference for monster EXP distribution after dead
    private MapleCharacter controller = null;
    private boolean fake = false, dropsDisabled = false, controllerHasAggro = false, tagged = false, expDisabled = false;
    private EventInstanceManager eventInstance;
    private MonsterListener listener = null;
    private byte[] reflectpack = null, nodepack = null;
    private ConcurrentEnumMap<MonsterStatus, MonsterStatusEffect> stati = new ConcurrentEnumMap<>(MonsterStatus.class);
    //private LinkedList<MonsterStatusEffect> poisons = new LinkedList<MonsterStatusEffect>();
    private ReentrantReadWriteLock poisonsLock = new ReentrantReadWriteLock();
    private int stolen = -1; //monster can only be stolen ONCE
    private boolean shouldDropItem = false, killed = false;
    private Map<MapleCharacter, AtomicLong> takenDamage = new ConcurrentHashMap<MapleCharacter, AtomicLong>();
    private List<Pair<Integer, Integer>> usedSkills = new ArrayList<>();
    private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<>();
    private Set<Integer> usedAttacks = new HashSet<>();
    private MonitoredReentrantLock externalLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_EXT);
    private MonitoredReentrantLock monsterLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB, true);
    private MonitoredReentrantLock statiLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_STATI);
    private MonitoredReentrantLock animationLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_ANI);
    private MonitoredReentrantLock aggroUpdateLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_AGGRO);
    private Map<Integer, Long> usedSkilled;
    public List<MapleCharacter> players = new ArrayList<MapleCharacter>();
    public MapleCharacter owner = null;
    public MapleCharacter killer = null;
    private Collection<MapleCharacter> attackers = new LinkedList<MapleCharacter>();
    public boolean spongy = false;
    public ConcurrentHashMap<MonsterStatusEffect, AtomicLong> buff = new ConcurrentHashMap<MonsterStatusEffect, AtomicLong>();
    public boolean finalboss = false;
    public int AttackDelay = 0;
    public int baseScale = 1;
    public boolean isSummon = false;
    public boolean normal = false;
    public int type = 0; //1 = monster park - 2 = life scroll
    public boolean isTotem = false;
    public Map<Integer, Integer> babies = new HashMap<>();
    public int delaySpawn = 0;
    public boolean mobTrans = false, localKaotic = false;
    public int tags = 0, counter = 0, bonusStat = 0;
    public Collection<MapleMonster> summons = new LinkedList<MapleMonster>();
    public boolean eventScript = true, instanced = false;
    public long damagecap = -1;
    public long movespeed = 0;
    public BigInteger grandHP;
    public boolean isDeadly = false, dead = false, aggro = false, deadProcessed = false, randomEgg = false, forced = false;
    public long spawning = 0;
    public AttackPair oned;
    public long moveDelay = 0, moveCounter = 0, lastPosTime = 0, posCount = 0;
    public Point lastPos = new Point();
    public int atkers = 0, eggModel = 0, eggEvo = 1;
    public boolean disposed = false, teleport = false, bossStats = true, bossAch = true;

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats, id);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.getStats(), monster.getId());
    }

    public void setAggro(boolean toggle) {
        aggro = toggle;
    }

    public boolean getAggro() {
        return aggro;
    }

    public void initWithStats(MapleMonsterStats baseStats, int mid) {
        setStance(5);
        //this.stats = stats.copy(mid);
        this.stats = baseStats.copy(mid);
        this.stats.revives = new ArrayList<>(baseStats.revives);
        if (baseStats.getFixedDamage() > 0) {
            this.damagecap = baseStats.getFixedDamage();
        }
        setBabies();
        hp = stats.getHp();
        mp = stats.getMp();
        if (stats.getNoSkills() > 0) {
            usedSkilled = new HashMap<Integer, Long>();
        }
    }

    public void setDelay(boolean effect) {
        //getAnimationTime("regen")
        spawning = System.currentTimeMillis() + 100;
    }

    public void setDeadly(boolean toggle) {
        isDeadly = toggle;
    }

    public boolean isDeadly() {
        return isDeadly;
    }

    public void setBossAch(boolean toggle) {
        bossAch = toggle;
    }

    public boolean getBossAch() {
        return bossAch;
    }

    public void setBossStats(boolean toggle) {
        bossStats = toggle;
    }

    public boolean getBossStats() {
        return bossStats;
    }

    public Rectangle getBounds() {
        return new Rectangle(getPosition().x - 25, getPosition().y - 75, 50, 75);
    }

    public BigInteger getGrandHP() {
        return grandHP;
    }

    public void setGrandHP(BigInteger hp) {
        grandHP = hp;
    }

    public void setBabies() {
        if (!this.stats.revives.isEmpty()) {
            for (Integer baby : this.stats.revives) {
                babies.put(baby, getAnimationTime("die1"));
            }
        }
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

    public MapleCharacter getKiller() {
        return killer;
    }

    public void setKiller(MapleCharacter chr) {
        killer = chr;
    }

    public void setDamageCap(long cap) {
        damagecap = cap;
    }

    public Long getDamageCap() {
        return damagecap;
    }

    public Long getDamageCap(int value) {
        if (value > 1) {
            return BigInteger.valueOf((long) damagecap).multiply(BigInteger.valueOf(value)).min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        } else {
            return damagecap;
        }
    }

    public void setInstanced() {
        instanced = true;
    }

    public boolean getInstanced() {
        return instanced;
    }

    public void addTags() {
        tags++;
    }

    public int getTags() {
        return tags;
    }

    public void setLocalKaotic(boolean toggle) {
        localKaotic = toggle;
    }

    public boolean isLocalKaotic() {
        return localKaotic;
    }

    public void setSpawnTrans(boolean toggle) {
        mobTrans = toggle;
    }

    public boolean getSpawnTrans() {
        return mobTrans;
    }

    public Map<Integer, Integer> getBabies() {
        return babies;
    }

    public int getBabyTime() {
        return delaySpawn;
    }

    public void setBabyTime(int value) {
        delaySpawn = value;
    }

    public void setTotem() {
        isTotem = true;
    }

    public boolean isTotem() {
        return isTotem;
    }

    public void setMonsterEventType(int type) {
        this.type = type;
    }

    public int getMonsterEventType() {
        return type;
    }

    public void cancelAllBuff() {
        if (!getAllBuffs().isEmpty()) {
            for (MonsterStatusEffect mse : getAllBuffs()) {
                if (mse != null && mse.shouldCancel(System.currentTimeMillis())) {
                    cancelSingleStatus(mse);
                }
            }
        }
    }

    public void cancelAllBuffs() {
        if (!getAllBuffs().isEmpty()) {
            for (MonsterStatusEffect mse : getAllBuffs()) {
                if (mse != null) {
                    cancelSingleStatus(mse);
                }
            }
        }
    }

    //public boolean allowMSE(MonsterStatusEffect mse) {
    //    return mse.getStati() != MonsterStatus.SUMMON && mse.getStati() != MonsterStatus.EMPTY && mse.getStati() != MonsterStatus.EMPTY_1 && mse.getStati() != MonsterStatus.EMPTY_2 && mse.getStati() != MonsterStatus.EMPTY_3 && mse.getStati() != MonsterStatus.EMPTY_4 && mse.getStati() != MonsterStatus.EMPTY_5 && mse.getStati() != MonsterStatus.EMPTY_6;
    //}
    public MapleCharacter getOwner() {
        return owner;
    }

    public void setOwner(MapleCharacter chr) {
        owner = chr;
    }

    public int getBaseScale() {
        return stats.getBaseScale();
    }

    public void setBaseScale(int scale) {
        this.baseScale = scale;
    }

    public final MapleMonsterStats getStats() {
        return stats;
    }

    public int getAnimationTime(String name) {
        return stats.getAnimationTime(name);
    }

    public final void disableDrops() {
        this.dropsDisabled = true;
    }

    public final void enableDrops() {
        this.dropsDisabled = false;
    }

    public final void toggleSummon(boolean toggle) {
        this.isSummon = toggle;
    }

    public final boolean isSummon() {
        return isSummon;
    }

    public final void setDrops(boolean toggle) {
        this.dropsDisabled = toggle;
    }

    public final boolean dropsDisabled() {
        return dropsDisabled;
    }

    public final void disableExp() {
        this.expDisabled = true;
    }

    public final boolean expDisabled() {
        return expDisabled;
    }

    public final void tagged() {
        this.tagged = true;
    }

    public final void setFinalBoss(boolean toggle) {
        this.finalboss = toggle;
    }

    public final boolean isFinalBoss() {
        return finalboss;
    }

    public final void setTagged(boolean toggle) {
        this.tagged = toggle;
    }

    public final boolean getTagged() {
        return tagged;
    }

    public final void setAtkDelay(int value) {
        this.AttackDelay = value;
    }

    public final int getAtkDelay() {
        return this.AttackDelay;
    }

    public final void setSponge(final MapleMonster mob) {
        sponge = mob;
        if (linkoid <= 0) {
            linkoid = mob.getObjectId();
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    public final void setMap(final MapleMap map) {
        this.map = map;
        startDropItemSchedule();
    }

    public final long getHp() {
        return hp;
    }

    public final void setHp(long hp) {
        this.hp = hp;
    }

    public final ChangeableStats getChangedStats() {
        return ostats;
    }

    public final long getMobMaxHp() {
        //if (ostats != null) {
        //    return ostats.hp;
        //}
        return stats.getMaxLives() > 0 ? Long.MAX_VALUE : stats.getHp();
    }

    public final int getMp() {
        return mp;
    }

    public final void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public final int getMobMaxMp() {
        //if (ostats != null) {
        //    return ostats.mp;
        //}
        return stats.getMp();
    }

    public final long getMobExp() {
        //if (ostats != null) {
        //    return ostats.exp;
        //}
        return stats.getExp();
    }

    public void hideServerMsg(boolean boss) {

        for (MapleCharacter player : this.getMap().getAllPlayers()) {
            if (boss) {
                player.getClient().announce(CWvsContext.serverMessage(""));
            } else {
                player.getClient().announce(CWvsContext.serverMessage(ServerProperties.getProperty("net.sf.odinms.world.serverMessage")));
                //this.getMap().getChannel()ChannelServer.getAllInstance().
            }
        }
    }

    public final void setOverrideStats(final OverrideMonsterStats ostats) {
        this.ostats = new ChangeableStats(stats, ostats);
        this.hp = ostats.getHp();
        this.mp = ostats.getMp();
    }

    public OverrideMonsterStats getOverrideStats() {
        return ostats;
    }

    public void changeLevel(final int newLevel, int scale, boolean aggro) {
        this.ostats = new ChangeableStats(stats, newLevel, scale, aggro);
        this.hp = ostats.getHp();
        this.mp = ostats.getMp();
        this.grandHP = stats.getTotalHp();
    }

    public final MapleMonster getSponge() {
        return sponge;
    }

    public MapleCharacter getHighestDamagerId() {
        MapleCharacter highest = null;
        long curDmg = 0;
        List<MapleCharacter> playersd = new LinkedList<MapleCharacter>(takenDamage.keySet());
        for (MapleCharacter chr : playersd) {
            highest = takenDamage.get(chr).get() >= curDmg ? chr : highest;
            curDmg = chr == chr ? takenDamage.get(chr).get() : curDmg;
        }

        return highest;
    }

    public long getDamage(MapleCharacter chr) {
        if (takenDamage.containsKey(chr)) {
            return takenDamage.get(chr).get();
        }
        return 0;
    }

    public boolean isAttackedBy(MapleCharacter chr) {
        return takenDamage.containsKey(chr);
    }

    public int getAttackerSize() {
        return getAttackers().size();
    }

    public List<MapleCharacter> getAttackers() {
        List<MapleCharacter> playersd = new LinkedList<MapleCharacter>(takenDamage.keySet());
        List<MapleCharacter> lootChars = new LinkedList<MapleCharacter>();
        for (MapleCharacter chr : playersd) {
            if (chr != null) {
                if (chr.isAlive() && chr.getMapId() == this.getMap().getId()) {
                    lootChars.add(chr);
                }
            }
        }

        return lootChars;
    }

    public int getPlayersSize() {
        List<MapleCharacter> chrz = new ArrayList<>(players);
        for (MapleCharacter chr : chrz) {
            if (chr != null && !chr.isGM() && !chr.isAlive()) {
                chrz.remove(chr);
            }
        }
        return chrz.size();
    }

    public void setForceBar() {
        this.getStats().setBar(true);
    }

    public void setForceBar(boolean toggle) {
        this.getStats().setBar(toggle);
    }

    public double getHpPerc() {
        BigDecimal value = new BigDecimal(grandHP).divide(new BigDecimal(stats.getTotalHp()), 4, RoundingMode.HALF_UP).movePointRight(2);
        return value.doubleValue();
    }

    public String getStatType(int id) {
        switch (id) {
            case 1 -> {
                return "XP";
            }
            case 2 -> {
                return "DR";
            }
            case 3 -> {
                return "AS";
            }
            case 4 -> {
                return "OP";
            }
            case 5 -> {
                return "MR";
            }
            case 6 -> {
                return "TD";
            }
            case 7 -> {
                return "BD";
            }
            case 8 -> {
                return "IED";
            }
            case 9 -> {
                return "IDP";
            }
            case 10 -> {
                return "ETC";
            }
            case 11 -> {
                return "CD";
            }
        }
        return "";
    }

    public String displayData(double perc) {
        if (getId() >= 2000 && getId() <= 2020) {
            return "                                                             Destory the Pal Egg to capture it!";
        }
        if (getId() >= 2050 && getId() <= 2099) {
            return "                                                           Destory the Pal Ball to gain Pal Loot!";
        }
        String Tier = NumberFormat.getInstance().format(getStats().getScale());
        String Level = NumberFormat.getInstance().format(getStats().getPower());
        String Defense = StringUtil.getUnitFullNumber(getStats().getDef());
        String maxhp = StringUtil.getUnitBigNumber(getGrandHP());
        String Cap = StringUtil.getUnitBigNumber(getStats().damageCap);
        boolean capper = getStats().damageCap.compareTo(BigInteger.ZERO) > 0;
        String target = "";
        if (getStats().bonusStats > 0 && !dropsDisabled && getBossStats()) {
            target = " - " + getStatType(getStats().bonusStats) + ": +" + (getStats().bonusAmount * getAttackerSize() * getStats().kdRate) + "%";
        }
        String atks = "";
        if (getStats().getHits() > 0) {
            atks = " - Hits: " + getStats().getHits();
        }
        String kaotic = "";
        String mega = "";
        if (getStats().omega && getStats().mega) {
            if (getStats().ultimate) {
                kaotic = "(KMU)";
            } else {
                kaotic = "(KM)";
            }
        } else {
            if (getStats().omega) {
                if (getStats().ultimate) {
                    kaotic = "(KU)";
                } else {
                    kaotic = "(K)";
                }
            }
            if (getStats().mega) {
                mega = "(M)";
            }
        }
        return "HP: " + maxhp + " - (" + perc + "%) - Lvl: " + Level + " - Tier: " + Tier + " - Def: " + Defense + " - D-Cap: " + (capper ? Cap : "None") + "" + atks + " " + target + "" + kaotic + "" + mega + "";
    }

    public String displayDataExpo(double perc) {
        if (getId() >= 2000 && getId() <= 2020) {
            return "                                                             Destory the Pal Egg to capture it!";
        }
        if (getId() >= 2050 && getId() <= 2099) {
            return "                                                           Destory the Pal Ball to gain Pal Loot!";
        }
        String Tier = NumberFormat.getInstance().format(getStats().getScale());
        String Level = NumberFormat.getInstance().format(getStats().getLevel());
        String Exp = StringUtil.getUnitFullNumber(getStats().getExp());

        String maxhp = StringUtil.getUnitBigNumberExpo(getGrandHP());
        String Cap = StringUtil.getUnitBigNumberExpo(getStats().damageCap);
        boolean capper = getStats().damageCap.compareTo(BigInteger.ZERO) > 0;
        String target = "";
        if (getStats().bonusStats > 0 && !dropsDisabled) {
            target = " - " + getStatType(getStats().bonusStats) + ": +" + (getStats().bonusAmount * getAttackerSize()) + "%";
        }
        String atks = "";
        if (getStats().getHits() > 0) {
            atks = " - Hits: " + getStats().getHits();
        }
        String kaotic = "";
        String mega = "";
        if (getStats().kdboss && getStats().mega) {
            if (getStats().ultimate) {
                kaotic = "(KMU)";
            } else {
                kaotic = "(KM)";
            }
        } else {
            if (getStats().kdboss) {
                if (getStats().ultimate) {
                    kaotic = "(KU)";
                } else {
                    kaotic = "(K)";
                }
            }
            if (getStats().mega) {
                mega = "(M)";
            }
        }

        return "HP: " + maxhp + " - (" + perc + "%) - Lvl: " + Level + " - Tier: " + Tier + " - D-Cap: " + (capper ? Cap : "None") + " - Exp: " + Exp + "" + atks + "" + target + " " + kaotic + "" + mega;
    }

    public boolean isDead() {
        return deadProcessed;
    }

    public final void superBigDamage(final MapleCharacter from, BigInteger damage, final boolean updateAttackTime, final int lastSkill, boolean dot) {
        /* Big int compares
        -1 if base < value
        0 if base == value
        +1 if base > value
        if base > value = true
        if base < value = false
         */
        if (from == null || deadProcessed || dead) {
            return;
        }
        try {
            if (!tagged) {
                tagged = true;
            }
            if (!takenDamage.containsKey(from)) {
                takenDamage.put(from, new AtomicLong(1));
                addTags();
            } else {
                takenDamage.get(from).addAndGet(1);
            }
            //System.out.println(grandHP.toString());
            if (grandHP.compareTo(BigInteger.ZERO) > 0) {
                if (grandHP.compareTo(damage) <= 0) {
                    grandHP = BigInteger.ZERO;
                } else {
                    grandHP = grandHP.subtract(damage).max(BigInteger.ZERO);
                }
                damage.clearBit(damage.bitLength());
                if (stats.selfDestruction() != null && stats.selfDestruction().getAction() != -1) {
                    if (grandHP.compareTo(BigInteger.valueOf(stats.selfDestruction().getHp())) <= 0) {
                        grandHP.clearBit(grandHP.bitLength());
                        updateBar(from, true);
                        kill(from, lastSkill, getMap(), 2, false);
                        return;
                    }
                }
            }
            if (grandHP.compareTo(BigInteger.ZERO) <= 0) {
                grandHP = BigInteger.ZERO;
                kill(from, lastSkill, getMap(), 1, true);
            } else {
                selfDestruction selfDestr = getStats().selfDestruction();
                if (selfDestr != null && selfDestr.getHp() > -1) {// should work ;p
                    if (grandHP.compareTo(BigInteger.valueOf(selfDestr.getHp())) <= 0) {
                        grandHP = BigInteger.ZERO;
                        kill(from, lastSkill, getMap(), 2, false);
                    }
                }
            }
            updateBar(from, dead);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //startDropItemSchedule();
    }

    public void updateBar(final MapleCharacter from, boolean dead) {
        if (dead) {
            if (stats.getBar()) {
                map.broadcastMessage(MobPacket.clearBossHP(this), this.getPosition());
            }
        } else {
            if (stats.getBar()) {
                double perc = getHpPerc();
                var data = displayData(perc);
                var dataExpo = displayDataExpo(perc);
                int mid = getMap().getId();
                map.broadcastMessage(MobPacket.forceBossHP(this, perc));
                for (MapleCharacter mpc : getMap().getAllPlayers()) {
                    if (mpc != null && mpc.getMapId() == mid) {
                        mpc.getClient().announce(MobPacket.SmartMobnotice(this.getId(), data, mpc == getController() ? true : false));
                    }
                }
            } else {
                switch (stats.getHPDisplayType()) {
                    case 1 -> {
                        break;
                    }
                    case 2 -> {
                        map.broadcastMessage(MobPacket.showMonsterHP(getObjectId(), (int) getHpPerc()));
                        break;
                    }
                    case 3 -> {
                        map.broadcastMessage(MobPacket.showMonsterHP(getObjectId(), (int) getHpPerc()));
                        break;
                    }
                }
            }
        }
    }

    public void setEventScript(boolean toggle) {
        eventScript = toggle;
    }

    public boolean getEventScript() {
        return eventScript;
    }

    public int getBonus() {
        return atkers;
    }

    public void kill(final MapleCharacter chr, final int lastSkill, final MapleMap Mmap, final int type, final boolean drops) {
        try {
            if (!deadProcessed) {
                deadProcessed = true;
                dead = true;
                atkers = getAttackerSize();
                setKiller(chr);
                cancelAllBuffs();
                if (chr != null && chr.isAlive() && chr.getMapId() == Mmap.getId()) {
                    giveExpToCharacter(chr, this.getStats().getExp(), true, 1, (byte) 0, (byte) 0, (byte) 0, lastSkill);
                    for (MapleCharacter attacker : getAttackers()) {
                        if (attacker != null && attacker.getMapId() == Mmap.getId()) {
                            long expBase = (long) (stats.getLevel() * stats.getScale() * ((stats.getTrueBoss() && stats.isExplosiveReward()) ? stats.getScale() : 1));
                            if (chr == attacker) {
                                attacker.gainLevelData(999, (long) (expBase * attacker.getEXPMod()));
                            } else {
                                attacker.gainLevelData(999, (long) (expBase * attacker.getEXPMod() * 0.1));
                            }
                        }
                    }
                }
                Mmap.broadcastMessage(MobPacket.killMonster(getObjectId(), (byte) type), getPosition());
                if (!getStats().getRevives().isEmpty()) {
                    spawnRevives(Mmap, this);
                }
                if (getId() == 8880140) { //kill flower when lucid dies
                    Mmap.killMonster(8880158);
                }
                if (getId() == 2030) { //RAID PAL EGG CODE
                    MaplePal.createEgg(chr, eggModel, (int) Math.floor(Randomizer.randomDouble(getStats().getLevel(), getStats().getLevel() * 2.5)));
                    chr.dropTopMessage("[Maple Pal] New Mega Egg has been created into your Pal Box!");
                }
                if (getId() >= 2000 && getId() <= 2020) { //PAL EGG CODE
                    if (forced && eggModel > 0) {
                        if (chr.canHoldEgg()) {
                            MaplePal.createEgg(chr, eggModel, getStats().getLevel());
                            int ev = getStats().getScale();
                            String rank = "Common";
                            if (ev == 2) {
                                rank = "Rare";
                            }
                            if (ev == 3) {
                                rank = "Epic";
                            }
                            if (ev == 4) {
                                rank = "Legendary";
                            }
                            chr.dropTopMessage("[Maple Pal] New " + rank + " Egg has been created into your Pal Box!");
                        } else {
                            chr.gainItem(4201001, getStats().getScale());
                            chr.dropColorMessage(12, "[Maple Pal] Your Egg Box is full!");
                        }
                    } else {
                        if (eggModel == 0) {
                            getMap().eggSpawn = false;
                            getMap().palTimer = 3600 + Randomizer.random(0, 36000);
                            if (chr.canHoldEgg()) {
                                if (getId() == 2020) {
                                    MaplePal.createEgg(chr);
                                } else {
                                    if (getId() >= 2000 && getId() <= 2008) {
                                        int e = getId() - 2000;
                                        PalTemplateProvider.getPalsbyType(e).size();
                                        List<Integer> p = PalTemplateProvider.getPalsbyEvoList(e, 1);
                                        int pid = p.get(Randomizer.nextInt(p.size()));
                                        MaplePal.createEgg(chr, pid, getStats().getLevel());
                                    } else {
                                        MaplePal.createEgg(chr, getStats().getLevel());
                                    }
                                }
                                chr.dropTopMessage("[Maple Pal] New egg has been created into your Pal Box!");
                            } else {
                                chr.gainItem(4201001, 1);
                                chr.dropColorMessage(12, "[Maple Pal] Unable to capture the pal due to full egg storage box!");
                            }
                        } else {
                            if (chr.canHoldEgg()) {
                                MaplePal.createEgg(chr, eggModel, getStats().getLevel());
                                if (randomEgg) {
                                    chr.dropTopMessage("[Maple Pal] New egg has been created into your Pal Box!");
                                } else {
                                    chr.dropTopMessage("[Maple Pal] " + PalTemplateProvider.getTemplate(eggModel).name() + " Egg has been created into your Pal Box!");
                                }
                            } else {
                                chr.gainItem(4201001, 1);
                                chr.dropColorMessage(12, "[Maple Pal] Unable to capture the pal due to full egg storage box!");
                            }
                        }
                    }
                }
                if (getId() >= 2050 && getId() <= 2099) { //PAL EGG CODE
                    getMap().ballCount--;
                }
                if (!summons.isEmpty()) {
                    for (MapleMonster summon : summons) {
                        if (summon != null && !summon.deadProcessed) {
                            getMap().removeMonster(summon, true);
                        }
                    }
                }
                removeControl();
                if (!getMap().getAllMists().isEmpty()) {
                    for (MapleMist mist : getMap().getAllMists()) {
                        if (mist.getMob() == this) {
                            mist.remove(getMap());
                        }
                    }
                }
                Mmap.killMonster(this, chr, drops, false, (byte) type);
                //System.out.println("test: " + mob.getMap().countMonsters() + " - event?: " + getEventInstance());
                //System.out.println("death: " + getAnimationTime("die1"));
                if (!isTotem() && getEventInstance() != null) {
                    getEventInstance().mobKilled(chr, this);
                    if (eventScript) {
                        int time = getAnimationTime("die1");
                        TimerManager.getInstance().schedule(() -> {
                            if (getEventInstance() != null) {
                                getEventInstance().monsterKilled(chr, this);  // Ensure 'this' works correctly here, if needed.
                            }
                        }, time);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void heal(long hp, int mp, final boolean broadcast) {
        grandHP = grandHP.add(BigInteger.valueOf(hp).min(stats.getTotalHp()));
        /*
        final long TotalHP = Randomizer.LongMax(hp + getHp(), Long.MAX_VALUE);
        if (TotalHP >= getMobMaxHp()) {
            setHp(getMobMaxHp());
        } else {
            setHp(TotalHP);
        }
         */
        if (getController() != null) {
            updateBar(getController(), false);
        }
        if (broadcast) {
            map.broadcastMessage(MobPacket.healMonster(getObjectId(), Randomizer.Max((int) hp, Integer.MAX_VALUE)));
        } else if (sponge != null) { // else if, since only sponge doesn't broadcast
            sponge.hp += hp;
        }
    }

    public final void killed() {
        if (listener != null) {
            listener.monsterKilled(getAnimationTime("die1"));
        }
        listener = null;
    }

    public void spawnRevives(MapleMap Mmap, MapleMonster mob) {
        if (!Mmap.locked && Mmap.specialEvent == 0) {
            Mmap.spawnRevives(getStats().getRevives(), mob);
        }
    }

    //System.out.println("spawn");
    private void giveExpToCharacter(final MapleCharacter attacker, long exp, final boolean highestDamage, final int numExpSharers, final byte pty, final byte Class_Bonus_EXP_PERCENT, final byte Premium_Bonus_EXP_PERCENT, final int lastskillID) {
        if (attacker != null && !attacker.isAlive()) {
            return;
        }
        if (highestDamage) {
            highestDamageChar = attacker.getId();
        }
        if (exp > 0 && !expDisabled) {
            attacker.getTrait(MapleTraitType.charisma).addExp(stats.getCharismaEXP(), attacker);
            if (attacker.getRaid() == null) {
                BigDecimal tExp = BigDecimal.valueOf(exp).multiply(BigDecimal.valueOf(getMap().getExpRate() * stats.getScale()));
                if (GameConstants.getGloablEvent()) {
                    tExp = tExp.multiply(BigDecimal.valueOf(2));
                }
                if (getMap().local) {
                    tExp = tExp.multiply(BigDecimal.valueOf(2));
                }
                if (getId() >= 9601961 && getId() <= 9601965) {
                    if (attacker.getVarZero("abyss_mob") == getId()) {
                        if (attacker.getVarZero("abyss_mob_count") < 9999) {
                            attacker.addVar("abyss_mob_count", 1);
                            attacker.dropTopMessage("Abyss Monster Defeated: " + attacker.getVarZero("abyss_mob_count") + " / 9999");
                        }
                    }
                }
                if (attacker.getMapId() == 5002 || attacker.getMapId() == 5003) {
                    BigInteger nexp = BigInteger.valueOf(attacker.getLevel() * 5);
                    attacker.getClient().announce(CField.customMobDamage(this, nexp, 9000));
                    attacker.gainExpMonster(nexp, true, highestDamage, pty, 0, 0, 0, stats.isPartyBonus(), stats.getPartyBonusRate());
                } else {
                    for (MapleCharacter player : getAttackers()) {
                        if (player.expMode && player.getMapId() == this.getMap().getId()) {
                            if (player.getTotalLevel() < player.getMaxLevel()) {
                                double range = Randomizer.DoubleMinMax((double) this.getStats().getLevel() / (double) player.getTotalLevel(), 0.25, 2.0);
                                if (range >= 0.75) {
                                    tExp.multiply(BigDecimal.valueOf(range * player.getEXPMod() * getMap().getExpRate())).toBigInteger();
                                    player.getClient().announce(CField.customMobDamage(this, tExp.toBigInteger(), range > 1.0 ? 9002 : 9000));
                                    player.gainExpMonster(tExp.toBigInteger(), true, highestDamage, pty, 0, 0, 0, stats.isPartyBonus(), stats.getPartyBonusRate());
                                }
                            }
                        }
                    }
                    if (attacker.isAlive() && this.getMap() == attacker.getMap() && attacker.getAndroid() != null) {
                        long aExp = Randomizer.LongMax((long) (Math.pow(this.getStats().getScale(), 2) * attacker.getEXPMod()), 99999999999999L);
                        attacker.gainAndroidExp(aExp);
                    }
                    //System.out.println("BEXP3 : " + tExp.toString());
                    //systems for masteries
                    long skinxp = (int) Math.pow(getStats().getScale(), 2);
                    if (getStats().isExplosiveReward() && getStats().getTrueBoss()) {
                        skinxp *= 10;
                    } else if (getMonsterEventType() == 1) {
                        skinxp *= 2;
                    } else if (getMonsterEventType() == 3) {
                        skinxp *= 3;
                    } else if (getMonsterEventType() == 5) {
                        skinxp *= 5;
                    }
                    if (lastskillID != 0 && attacker.isAlive() && this.getMap() == attacker.getMap()) {
                        Skill skillz = attacker.getSkillbyId(GameConstants.getLinkedAranSkill(lastskillID));
                        if (skillz != null) {
                            long sExp = (long) (skinxp * getMap().getSkillExp());
                            attacker.gainSkillExp(skillz, sExp);
                        }
                    }
                    if (!this.dropsDisabled() && this.getStats().isExplosiveReward()) {
                        int gexp = this.getStats().getScale() * this.getStats().getScale();
                        attacker.gainGP(gexp, false);
                    }
                    long wxp = (long) (Math.pow(getStats().getScale(), 2) * getMap().getSkillRate());
                    attacker.gainLevelData(wxp);

                }
                attacker.mobKilled(getId(), lastskillID);
            }
        }
    }

    public int getCustomExp() {
        int skinxp = (int) ((getStats().isExplosiveReward() && getStats().getTrueBoss()) ? !dropsDisabled() ? Math.pow(getStats().getScale(), 2) : getStats().getScale() : 1);
        if (GameConstants.getGloablEvent() || Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            skinxp *= 2;
        }
        return skinxp;
    }

    public final void killBy(final MapleCharacter killer) {
        if (killed) {
            return;
        }
        List<MapleCharacter> lootChars = new LinkedList<>(getAttackers());
        killed = true;
        if (getController() != null) { // this can/should only happen when a hidden gm attacks the monster
            getController().stopControllingMonster(this);
        }

        List<Integer> achievements = new LinkedList<>();
        if (stats.isBoss()) {
            achievements.add(18);
            if (stats.getScale() == 10) {
                achievements.add(800);
            }
            if (stats.getScale() >= 15) {
                achievements.add(801);
            }
            if (stats.getScale() >= 20) {
                achievements.add(802);
            }
            if (stats.getScale() >= 25) {
                achievements.add(803);
            }
            if (stats.getScale() >= 30) {
                achievements.add(804);
            }
            if (stats.getScale() >= 35) {
                achievements.add(805);
            }
            if (stats.getScale() >= 40) {
                achievements.add(806);
            }
            if (stats.getScale() >= 45) {
                achievements.add(807);
            }
            if (stats.getScale() >= 50) {
                achievements.add(808);
            }
            if (stats.getScale() >= 55) {
                achievements.add(809);
            }
            if (stats.getScale() >= 60) {
                achievements.add(810);
            }
            if (stats.getScale() >= 65) {
                achievements.add(811);
            }
            if (stats.getScale() >= 70) {
                achievements.add(812);
            }
            if (stats.getScale() >= 75) {
                achievements.add(813);
            }
            if (stats.getScale() >= 80) {
                achievements.add(814);
            }
            if (stats.getScale() >= 85) {
                achievements.add(815);
            }
            if (stats.getScale() >= 90) {
                achievements.add(816);
            }
            if (stats.getScale() >= 95) {
                achievements.add(817);
            }
            if (stats.getScale() == 99) {
                achievements.add(818);
            }

        }
        if (bossAch && !dropsDisabled) {
            int monsterArch = stats.getAchievement();
            if (monsterArch > 0) {
                achievements.add(monsterArch);
            }
        }
        if (getStats().bonusStats > 0) {
            int size = getAttackers().size();
            for (MapleCharacter mpc : getAttackers()) {
                if (mpc != null && mpc.getMapId() == getMap().getId() && mpc.isAlive()) {
                    mpc.gainStat((int) (getStats().bonusAmount * size * getStats().kdRate), getStats().bonusStats);
                }
            }
        }
        if (getStats().skin > 0 && !dropsDisabled) {
            for (MapleCharacter mpc : getAttackers()) {
                if (mpc != null && mpc.getMapId() == getMap().getId() && mpc.isAlive()) {
                    if (mpc.gainDamageSkin(getStats().skin)) {
                        mpc.dropMessage("A new skin has been unlocked");
                    }
                }
            }
        }
        for (Integer achievement : achievements) {
            for (MapleCharacter mpc : getAttackers()) {
                if (mpc != null && mpc.getMapId() == getMap().getId() && mpc.isAlive()) {
                    mpc.finishAchievement(achievement);
                }
            }
        }
        if (killer != null && killer.getPyramidSubway() != null) {
            killer.getPyramidSubway().onKill(killer);
        }
        hp = 0;
        MapleMonster oldSponge = getSponge();
        sponge = null;
        if (oldSponge != null && oldSponge.isAlive()) {
            boolean set = true;
            for (MapleMonster mons : map.getParkMonsters()) {
                if (mons.isAlive() && mons.getObjectId() != oldSponge.getObjectId() && mons.getStats().getLevel() > 1 && mons.getObjectId() != this.getObjectId() && (mons.getSponge() == oldSponge || mons.getLinkOid() == oldSponge.getObjectId())) { //sponge was this, please update
                    set = false;
                    break;
                }
            }
            if (set) { //all sponge monsters are dead, please kill off the sponge
                map.killMonster(oldSponge, killer, true, false, (byte) 1);
            }
        }

        reflectpack = null;
        nodepack = null;
        if (!stati.isEmpty()) {
            List<MonsterStatus> statuses = new LinkedList<MonsterStatus>(stati.keySet());
            for (MonsterStatus ms : statuses) {
                cancelStatus(ms);
            }
            statuses.clear();
        }
        for (MonsterStatusEffect mse : getAllBuffs()) {
            cancelSingleStatus(mse);
        }
        cancelDropItem();
        int v1 = highestDamageChar;
        this.highestDamageChar = 0; //reset so we dont kill twice

    }

    public final boolean isAlive() {
        return grandHP.compareTo(BigInteger.ZERO) > 0;
    }

    public final void setCarnivalTeam(final byte team) {
        carnivalTeam = team;
    }

    public final byte getCarnivalTeam() {
        return carnivalTeam;
    }

    public final MapleCharacter getController() {
        return controller;
    }

    public final void setController(final MapleCharacter controller) {
        this.controller = controller;
    }

    public void releaseControllers(MapleCharacter player) {
        if (getController() != null && player != null && getController() == player) {
            player.stopControllingMonster(this);
        }
    }

    public void changeController(MapleCharacter newController) {
        if (newController != null && newController.isAlive() && !newController.isMapChange()) {
            if (newController != getController()) {
                if (getController() != null) {
                    getController().stopControllingMonster(this);
                }
                newController.controlMonster(this, true);
            }
        }
    }

    public void setControl(MapleCharacter player) {
        setControl(player, false);
    }

    public void setControl(MapleCharacter player, boolean forced) {
        if (player == null || deadProcessed) {
            return;
        }
        boolean aggro = forced || getAggro() || getStats().getTrueBoss();
        if (!player.isAlive() && player.isChangingMaps()) {
            player.getClient().announce(MobPacket.controlMonster(this, false, false));
        } else {
            player.getClient().announce(MobPacket.controlMonster(this, false, aggro));
        }
        setController(player);
        setControllerHasAggro(true);
    }

    public void removeControl() {
        if (getController() != null && getController().getClient() != null) {
            getController().getClient().announce(MobPacket.stopControllingMonster(this));
        }
        setController(null);
        setControllerHasAggro(false);
    }

    public void findHost() {
        MapleCharacter newController = getMap().findClosestPlayerNoGm(this.getPosition());//fix issue with gms
        if (newController == null) {
            newController = getMap().findClosestPlayer(this.getPosition());//fix issue with gms
            if (newController != null) {
                setControl(newController);
            }
        } else {
            setControl(newController);
        }
    }

    public final void updateMonsterController() {//kaotic
        if (deadProcessed) {
            if (getController() != null) {
                removeControl();
            }
            return;
        }
        try {
            if (getController() != null && getController().getClient() != null && getController().isAlive()) {
                return;
            }
            removeControl();
            if (!isAlive() || getLinkCID() > 0 || getSpawnTrans()) {
                return;
            }
            findHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     public final void updateMonsterController() {//kaotic
     try {
     //System.out.println("mobs: " + this.getId() + " has owner: " + this.getController());
     //System.out.println("mobs: " + this.getId() + " has owner: " + this.getController());
     //System.out.println("test: ");
     if (!isAlive() || getLinkCID() > 0 || getSpawnTrans()) {
     return;
     }
     if (getController() != null && getController().isAlive() && getController().getMap() == getMap() && !getController().isMapChange() && !getController().isGM()) {
     getController().controlMonster(this, true);
     return;
     } else {
     setController(null);
     setControllerHasAggro(false);
     }
     MapleCharacter newController = getMap().findClosestPlayerNoGm(this.getPosition());//fix issue with gms
     if (newController == null) {
     newController = getMap().findClosestPlayer(this.getPosition());//fix issue with gms
     if (newController != null) {
     newController.controlMonster(this, true);
     }
     } else {
     newController.controlMonster(this, true);
     }
     } catch (Exception e) {
     e.printStackTrace();
     }
     }
     */
    public final void addListener(final MonsterListener listener) {
        this.listener = listener;
    }

    public final boolean isControllerHasAggro() {
        return controllerHasAggro;
    }

    public final void setControllerHasAggro(final boolean controllerHasAggro) {
        this.controllerHasAggro = controllerHasAggro;
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
        if (!isAlive()) {
            return;
        }
        client.announce(MobPacket.spawnMonster(this, fake && linkCID <= 0 ? -4 : -1, 0));
        if (map != null && !stats.isEscort() && client.getPlayer() != null) {
            this.updateMonsterController();
        }
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
        if (stats.isEscort() && getEventInstance() != null && lastNode >= 0) { //shammos
            //map.resetShammos(client);
        } else {
            client.announce(MobPacket.killMonster(getObjectId(), true));
        }
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(stats.getName());
        sb.append("(");
        sb.append(getId());
        sb.append(") (Level ");
        sb.append(stats.getLevel());
        sb.append(") at (X");
        sb.append(getTruePosition().x);
        sb.append("/ Y");
        sb.append(getTruePosition().y);
        sb.append(") with ");
        sb.append(getHp());
        sb.append("/ ");
        sb.append(getMobMaxHp());
        sb.append("hp, ");
        sb.append(getMp());
        sb.append("/ ");
        sb.append(getMobMaxMp());
        sb.append(" mp, oid: ");
        sb.append(getObjectId());
        sb.append(" || Controller : ");
        final MapleCharacter chr = controller;
        sb.append(chr != null ? chr.getName() : "none");

        return sb.toString();
    }

    public final EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public final void setEventInstance(final EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public final int getStatusSourceID(final MonsterStatus status) {
        final MonsterStatusEffect effect = stati.get(status);
        if (effect != null) {
            return effect.getSkill();
        }
        return -1;
    }

    public final ElementalEffectiveness getEffectiveness(final Element e) {
        if (stati.size() > 0 && stati.containsKey(MonsterStatus.DOOM)) {
            return ElementalEffectiveness.NORMAL; // like blue snails
        }
        return stats.getEffectiveness(e);
    }

    public final void applyStatus(final MapleCharacter from, final MonsterStatusEffect status, final boolean poison, long duration, final boolean checkboss, final MapleStatEffect eff) {
        //System.out.println("stati");
        if (!isAlive() || getLinkCID() > 0) {
            return;
        }
        Skill skilz = SkillFactory.getSkill(status.getSkill());
        if (skilz != null) {
            switch (stats.getEffectiveness(skilz.getElement())) {
                case IMMUNE:
                case STRONG:
                    return;
                case NORMAL:
                case WEAK:
                    break;
                default:
                    return;
            }
        }
        // compos don't have an elemental (they have 2 - so we have to hack here...)
        final int statusSkill = status.getSkill();
        switch (statusSkill) {
            case 2111006: { // FP compo
                switch (stats.getEffectiveness(Element.POISON)) {
                    case IMMUNE:
                    case STRONG:
                        return;
                }
                break;
            }
            case 2211006: { // IL compo
                switch (stats.getEffectiveness(Element.ICE)) {
                    case IMMUNE:
                    case STRONG:
                        return;
                }
                break;
            }
            case 4120005:
            case 4220005:
            case 14110004: {
                switch (stats.getEffectiveness(Element.POISON)) {
                    case IMMUNE:
                    case STRONG:
                        return;
                }
                break;
            }
        }
        if (duration >= 2000000000) {
            duration = 5000; //teleport master
        }
        final MonsterStatus stat = status.getStati();
        if (stats.isNoDoom() && stat == MonsterStatus.DOOM) {
            return;
        }
        if ((stat == MonsterStatus.BURN || stat == MonsterStatus.POISON)) {
            return;
        }
        if (stats.isBoss()) {
            if (stat == MonsterStatus.STUN) {
                return;
            }
            if (checkboss && stat != (MonsterStatus.SPEED) && stat != (MonsterStatus.NINJA_AMBUSH) && stat != (MonsterStatus.WATK) && stat != (MonsterStatus.POISON) && stat != MonsterStatus.BURN && stat != (MonsterStatus.DARKNESS) && stat != (MonsterStatus.MAGIC_CRASH)) {
                return;
            }
            //hack: don't magic crash cygnus boss
            if (getId() == 8850011 && stat == MonsterStatus.MAGIC_CRASH) {
                return;
            }
        }
        if (stats.isFriendly() || isFake()) {
            if (stat == MonsterStatus.STUN || stat == MonsterStatus.SPEED || stat == MonsterStatus.POISON || stat == MonsterStatus.BURN) {
                return;
            }
        }
        if (stati.containsKey(stat)) {
            cancelStatus(stat);
        }
        duration += from.getStat().dotTime * 1000;
        long aniTime = duration;
        if (skilz != null) {
            aniTime += skilz.getAnimationTime();
        }
        status.setCancelTask(aniTime);
        final MapleCharacter con = getController();
        stati.put(stat, status);
        if (con != null) {
            map.broadcastMessage(con, MobPacket.applyMonsterStatus(this, status), getTruePosition());
            con.getClient().announce(MobPacket.applyMonsterStatus(this, status));
        } else {
            map.broadcastMessage(MobPacket.applyMonsterStatus(this, status), getTruePosition());
        }
    }

    public void applyStatus(MonsterStatusEffect status) { //ONLY USED FOR POKEMONN, ONLY WAY POISON CAN FORCE ITSELF INTO STATI.
        if (stati.containsKey(status.getStati())) {
            cancelStatus(status.getStati());
        }
        stati.put(status.getStati(), status);
        map.broadcastMessage(MobPacket.applyMonsterStatus(this, status), getTruePosition());
    }

    public final void dispelSkill(final MobSkill skillId) {
        List<MonsterStatus> toCancel = new ArrayList<MonsterStatus>();
        for (Entry<MonsterStatus, MonsterStatusEffect> effects : stati.entrySet()) {
            MonsterStatusEffect mse = effects.getValue();
            if (mse.getMobSkill() != null && mse.getMobSkill().getSkillId() == skillId.getSkillId()) { //not checking for level.
                toCancel.add(effects.getKey());
            }
        }
        for (MonsterStatus stat : toCancel) {
            cancelStatus(stat);
        }
    }

    public final void applyMonsterBuff(final Map<MonsterStatus, Integer> effect, final int skillId, final long duration, final MobSkill skill, final List<Integer> reflection) {
        for (Entry<MonsterStatus, Integer> z : effect.entrySet()) {
            if (stati.containsKey(z.getKey())) {
                cancelStatus(z.getKey());
            }
            final MonsterStatusEffect effectz = new MonsterStatusEffect(z.getKey(), z.getValue(), 0, skill, true, reflection.size() > 0);
            effectz.setCancelTask(duration);
            stati.put(z.getKey(), effectz);
        }
        final MapleCharacter con = getController();
        if (reflection.size() > 0) {
            this.reflectpack = MobPacket.applyMonsterStatus(getObjectId(), effect, reflection, skill);
            if (con != null) {
                map.broadcastMessage(con, reflectpack, getPosition());
                con.getClient().announce(this.reflectpack);
            } else {
                map.broadcastMessage(reflectpack, getPosition());
            }
        } else {
            for (Entry<MonsterStatus, Integer> z : effect.entrySet()) {
                if (con != null) {
                    map.broadcastMessage(con, MobPacket.applyMonsterStatus(getObjectId(), z.getKey(), z.getValue(), skill), getPosition());
                    con.getClient().announce(MobPacket.applyMonsterStatus(getObjectId(), z.getKey(), z.getValue(), skill));
                } else {
                    map.broadcastMessage(MobPacket.applyMonsterStatus(getObjectId(), z.getKey(), z.getValue(), skill), getPosition());
                }
            }
        }
    }

    public final void setTempEffectiveness(final Element e, final long milli) {
        stats.setEffectiveness(e, ElementalEffectiveness.WEAK);
        EtcTimer.getInstance().schedule(() -> {
            stats.removeEffectiveness(e);
        }, milli);
    }

    public final boolean isBuffed(final MonsterStatus status) {
        return stati.containsKey(status);
    }

    public final MonsterStatusEffect getBuff(final MonsterStatus status) {
        return stati.get(status);
    }

    public final int getStatiSize() {
        return stati.size();
    }

    public final ArrayList<MonsterStatusEffect> getAllBuffs() {
        ArrayList<MonsterStatusEffect> ret = new ArrayList<MonsterStatusEffect>();
        if (!stati.isEmpty()) {
            for (MonsterStatusEffect e : stati.values()) {
                if (e != null) {
                    ret.add(e);
                }
            }
        }
        return ret;
    }

    public final void setFake(final boolean fake) {
        this.fake = fake;
    }

    public final boolean isFake() {
        return fake;
    }

    public final MapleMap getMap() {
        return map;
    }

    public final List<Pair<Integer, Integer>> getSkills() {
        return stats.getSkills();
    }

    public final boolean hasSkill(final int skillId, final int level) {
        return stats.hasSkill(skillId, level);
    }

    public final byte getNoSkills() {
        return stats.getNoSkills();
    }

    public final boolean isFirstAttack() {
        return stats.isFirstAttack();
    }

    public final int getBuffToGive() {
        return stats.getBuffToGive();
    }

    public final void doPoison(final MonsterStatusEffect status, final WeakReference<MapleCharacter> weakChr) {
    }

    private static class AttackingMapleCharacter {

        private MapleCharacter attacker;

        public AttackingMapleCharacter(final MapleCharacter attacker) {
            super();
            this.attacker = attacker;
        }

        public final MapleCharacter getAttacker() {
            return attacker;
        }
    }

    private interface AttackerEntry {

        List<AttackingMapleCharacter> getAttackers();

        public void addDamage(MapleCharacter from, long damage);

        public long getDamage();

        public boolean contains(MapleCharacter chr);

        public void killedMob(MapleMap map, long baseExp, boolean mostDamage, int lastSkill);
    }

    private final class SingleAttackerEntry implements AttackerEntry {

        private long damage = 0;
        private int chrid;

        public SingleAttackerEntry(final MapleCharacter from) {
            this.chrid = from.getId();
        }

        @Override
        public void addDamage(final MapleCharacter from, final long damage) {
            if (chrid == from.getId()) {
                this.damage += damage;
            }
        }

        @Override
        public final List<AttackingMapleCharacter> getAttackers() {
            final MapleCharacter chr = map.getCharacterById(chrid);
            if (chr != null) {
                return Collections.singletonList(new AttackingMapleCharacter(chr));
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public boolean contains(final MapleCharacter chr) {
            return chrid == chr.getId();
        }

        @Override
        public long getDamage() {
            return damage;
        }

        @Override
        public void killedMob(final MapleMap map, final long baseExp, final boolean mostDamage, final int lastSkill) {//kaotic
            final MapleCharacter chr = map.getCharacterById(chrid);
            if (chr != null && chr.isAlive() && !isSummon()) {
                giveExpToCharacter(chr, baseExp, mostDamage, 1, (byte) 0, (byte) 0, (byte) 0, lastSkill);
            }
        }

        @Override
        public int hashCode() {
            return chrid;
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SingleAttackerEntry other = (SingleAttackerEntry) obj;
            return chrid == other.chrid;
        }
    }

    public int getLinkOid() {
        return linkoid;
    }

    public void setLinkOid(int lo) {
        this.linkoid = lo;
    }

    public final ConcurrentEnumMap<MonsterStatus, MonsterStatusEffect> getStati() {
        return stati;
    }

    public void addEmpty() {
        for (MonsterStatus stat : MonsterStatus.values()) {
            if (stat.isEmpty()) {
                stati.put(stat, new MonsterStatusEffect(stat, 0, 0, null, false));
            }
        }
    }

    public final int getStolen() {
        return stolen;
    }

    public final void setStolen(final int s) {
        this.stolen = s;
    }

    public final void handleSteal(MapleCharacter chr) {
        double showdown = 100.0;
        final MonsterStatusEffect mse = getBuff(MonsterStatus.SHOWDOWN);
        if (mse != null) {
            showdown += mse.getX();
        }

        Skill steal = SkillFactory.getSkill(4201004);
        final int level = chr.getTotalSkillLevel(steal), chServerrate = ChannelServer.getInstance(chr.getClient().getChannel()).getDropRate();
        if (level > 0 && !getStats().isBoss() && stolen == -1 && steal.getEffect(level).makeChanceResult()) {
            final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
            final List<MonsterDropEntry> de = mi.retrieveDrop(getId());
            if (de == null) {
                stolen = 0;
                return;
            }
            final List<MonsterDropEntry> dropEntry = new ArrayList<MonsterDropEntry>(de);
            Collections.shuffle(dropEntry);
            Item idrop;
            for (MonsterDropEntry d : dropEntry) { //set to 4x rate atm, 40% chance + 10x
                if (d.itemId > 0 && d.questid == 0 && d.itemId / 10000 != 238 && Randomizer.nextInt(999999) < (int) (10 * d.chance * chServerrate * chr.getDropMod() * (showdown / 100.0))) { //kinda op
                    if (GameConstants.getInventoryType(d.itemId) == MapleInventoryType.EQUIP) {
                        Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(d.itemId);
                        idrop = MapleItemInformationProvider.getInstance().randomizeStats(eq);
                    } else {
                        idrop = new Item(d.itemId, (byte) 0, (short) (d.Maximum != 1 ? Randomizer.nextInt(d.Maximum - d.Minimum) + d.Minimum : 1), (byte) 0);
                    }
                    stolen = d.itemId;
                    map.spawnMobDrop(idrop, map.calcDropPos(getPosition(), getTruePosition()), this, chr.getId(), (byte) 0, (short) 0, false);
                    break;
                }
            }
        } else {
            stolen = 0; //failed once, may not go again
        }
    }

    public final void setLastNode(final int lastNode) {
        this.lastNode = lastNode;
    }

    public final int getLastNode() {
        return lastNode;
    }

    public final void cancelStatus(final MonsterStatus stat) {
        final MonsterStatusEffect mse = stati.get(stat);
        if (mse == null || !isAlive()) {
            return;
        }
        if (mse.isReflect()) {
            reflectpack = null;
        }
        final MapleCharacter con = getController();
        if (con != null) {
            map.broadcastMessage(con, MobPacket.cancelMonsterStatus(getObjectId(), stat), getTruePosition());
            con.getClient().announce(MobPacket.cancelMonsterStatus(getObjectId(), stat));
        } else {
            map.broadcastMessage(MobPacket.cancelMonsterStatus(getObjectId(), stat), getTruePosition());
        }
        stati.remove(stat);
    }

    public final void cancelSingleStatus(final MonsterStatusEffect stat) {
        if (stat == null) {
            return;
        }
        cancelStatus(stat.getStati());
    }

    public final void cancelDropItem() {
        lastDropTime = 0;
    }

    public final void startDropItemSchedule() {
        cancelDropItem();
        if (stats.getDropItemPeriod() <= 0 || !isAlive()) {
            return;
        }
        shouldDropItem = false;
        lastDropTime = System.currentTimeMillis();
    }

    public boolean shouldDrop(long now) {
        return lastDropTime > 0 && lastDropTime + (stats.getDropItemPeriod() * 1000) < now;
    }

    public void doDropItem(long now) {
        final int itemId;
        switch (getId()) {
            case 9300061:
                itemId = 4001101;
                break;
            default: //until we find out ... what other mobs use this and how to get the ITEMID
                cancelDropItem();
                return;
        }
        if (isAlive() && map != null) {
            if (shouldDropItem) {
                map.spawnAutoDrop(itemId, getTruePosition());
            } else {
                shouldDropItem = true;
            }
        }
        lastDropTime = now;
    }

    public byte[] getNodePacket() {
        return nodepack;
    }

    public void setNodePacket(final byte[] np) {
        this.nodepack = np;
    }

    public void registerKill(final long next) {
        this.nextKill = System.currentTimeMillis() + next;
    }

    public boolean shouldKill(long now) {
        return nextKill > 0 && now > nextKill;
    }

    public int getLinkCID() {
        return linkCID;
    }

    public void setLinkCID(int lc) {
        this.linkCID = lc;
        if (lc > 0) {
            stati.put(MonsterStatus.HYPNOTIZE, new MonsterStatusEffect(MonsterStatus.HYPNOTIZE, 60000, 30001062, null, false));
        }
    }

    public void yell(String msg) {
        getMap().broadcastMessage(CWvsContext.monsterChat(msg, this));
    }

    public int getSkillPos(int skillId, int level) {
        int pos = 0;
        for (Pair<Integer, Integer> ms : this.getSkills()) {
            if (ms.getLeft() == skillId && ms.getRight() == level) {
                return pos;
            }

            pos++;
        }

        return -1;
    }

    public boolean canUseSkill(MobSkill toUse, boolean apply) {
        if (toUse == null) {
            return false;
        }

        int useSkillid = toUse.getSkillId();
        monsterLock.lock();
        try {
            for (Pair<Integer, Integer> skill : usedSkills) {   // thanks OishiiKawaiiDesu for noticing an issue with mobskill cooldown
                if (skill.getLeft() == useSkillid && skill.getRight() == toUse.getSkillLevel()) {
                    return false;
                }
            }

            if (apply) {
                this.usedSkill(toUse);
            }
        } finally {
            monsterLock.unlock();
        }

        return true;
    }

    private void clearSkill(int skillId, int level) {
        int index = -1;
        for (Pair<Integer, Integer> skill : usedSkills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                index = usedSkills.indexOf(skill);
                break;
            }
        }
        if (index != -1) {
            usedSkills.remove(index);
        }
    }

    private void usedSkill(MobSkill skill) {
        final int skillId = skill.getSkillId(), level = skill.getSkillLevel();
        long cooltime = skill.getCoolTime();

        monsterLock.lock();
        try {
            Pair<Integer, Integer> skillKey = new Pair<>(skillId, level);
            this.usedSkills.add(skillKey);

            Integer useCount = this.skillsUsed.remove(skillKey);
            if (useCount != null) {
                this.skillsUsed.put(skillKey, useCount + 1);
            } else {
                this.skillsUsed.put(skillKey, 1);
            }
        } finally {
            monsterLock.unlock();
        }

        //final MapleMonster mons = this;
        //MapleMap mmap = mons.getMap();
    }

    public int canUseAttack(int attackPos, boolean isSkill) {
        monsterLock.lock();
        try {
            Pair<Integer, Integer> attackInfo = MapleMonsterInformationProvider.getInstance().getMobAttackInfo(this.getId(), attackPos);
            if (attackInfo == null) {
                return -1;
            }

            int mpCon = attackInfo.getLeft();
            if (mp < mpCon) {
                return -1;
            }
            usedAttack(attackPos, mpCon, attackInfo.getRight());
            return 1;
        } finally {
            monsterLock.unlock();
        }
    }

    private void usedAttack(final int attackPos, int mpCon, int cooltime) {
        monsterLock.lock();
        try {
            mp -= mpCon;
        } finally {
            monsterLock.unlock();
        }
    }

    private void clearAttack(int attackPos) {
        monsterLock.lock();
        try {
            usedAttacks.remove(attackPos);
        } finally {
            monsterLock.unlock();
        }
    }

    public final long getLastSkillUsed(final int skillId) {
        if (usedSkilled.containsKey(skillId)) {
            return usedSkilled.get(skillId);
        }
        return 0;
    }

    public final void setLastSkillUsed(final int skillId, final long now, final long cooltime) {
        switch (skillId) {
            case 140:
                usedSkilled.put(skillId, now + (cooltime * 2));
                usedSkilled.put(141, now);
                break;
            case 141:
                usedSkilled.put(skillId, now + (cooltime * 2));
                usedSkilled.put(140, now + cooltime);
                break;
            default:
                usedSkilled.put(skillId, now + cooltime);
                break;
        }
    }

    public boolean isMPBoss() {
        switch (this.getId()) {
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

    public boolean isMPFinalBoss() {
        switch (this.getId()) {
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
                return true;
        }
        return false;
    }

    public void setSpawnData(SpawnPoint sp) {
        this.setCy(sp.getCy());
        this.setF(sp.getF());
        this.setFh(sp.getFh());
        this.setRx0(sp.getRx0());
        this.setRx1(sp.getRx1());
        this.setPosition(sp.getPosition());
    }

    public void setFHData(Point pos) {
        this.setFh(getMap().getFH(pos));
    }

    public void setFHMapData(MapleMap map, Point pos) {
        this.setFh(map.getFH(pos));
    }

    public void randomTeleport() {
        teleportMob(getMap().randomPointOnMap());
    }

    public void teleportMob(Point pos) {
        if (!deadProcessed && getStats().getMobile()) {
            teleport = true;
            this.getMap().broadcastMessage(MobPacket.killMonster(getObjectId(), (byte) 6, 1000), getTruePosition());
            TimerManager.getInstance().schedule(() -> {
                if (!deadProcessed) {
                    removeControl();
                    Point nPos = getMap().calcPointBelow(pos);
                    setPosition(nPos);
                    setFHData(nPos);
                    findHost();
                    getMap().broadcastMessage(MobPacket.spawnTeleportMonster(MapleMonster.this));
                    teleport = false;
                }
            }, Randomizer.random(2500, 7500));
        }
    }
}
