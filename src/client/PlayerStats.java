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
import client.inventory.Item;
import client.inventory.Equip;
import client.inventory.EquipAdditions;
import client.inventory.MapleWeaponType;
import client.maplepal.MaplePal;
import client.maplepal.MaplePalAbility;
import client.maplepal.PalTemplateProvider;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildSkill;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.StructSetItem;
import server.StructSetItem.SetItem;
import server.StructItemOption;
import server.life.Element;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CField.EffectPacket;
import static tools.packet.CField.customMainStatUpdate;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InventoryPacket;

public class PlayerStats implements Serializable {

    private static final long serialVersionUID = -679541993413738569L;
    private Map<Integer, Integer> setHandling = new ConcurrentHashMap<Integer, Integer>(), skillsIncrement = new ConcurrentHashMap<>(), damageIncrease = new ConcurrentHashMap<>();
    private EnumMap<Element, Integer> elemBoosts = new EnumMap<>(Element.class);
    private List<Equip> durabilityHandling = new ArrayList<>(), equipLevelHandling = new ArrayList<>();
    private transient float shouldHealHP, shouldHealMP;
    public int hp, maxhp, mp, maxmp, passive_sharpeye_min_percent, passive_sharpeye_percent, passive_sharpeye_rate, str, dex, luk, int_;
    public int stamina, p_str, p_dex, p_int, p_luk, p_atk, p_def, p_matk, p_mdef;
    private transient byte passive_mastery;
    private transient long localstr, localdex, localluk, localint;
    private transient long tstr, tdex, tluk, tint, tatk, tmatk, ignoreTargetDEF, wdef, mdef, magic, watk;
    private transient long estr, edex, eluk, eint, bstr, bdex, bluk, bint;
    private transient int hands, accuracy, localmaxhp, localmaxmp;
    public transient boolean equippedWelcomeBackRing, hasClone, hasPartyBonus, Berserk, canFish, canFishVIP;
    public transient double expBuff, expItemBuff, kpItemBuff, dropBuff, mesoBuff, cashBuff, mesoGuard, mesoGuardMeso, expMod, pickupRange, dropMod, mesoMod, damResist, itempower, overpower, ignoreTargetPERC, allstat, masterBuff;
    public transient double dam_r, bossdam_r, percent_str, percent_dex, percent_int, percent_luk, percent_items, items;
    public transient int recoverHP, recoverMP, mpconReduce, mpconPercent, incMesoProp, reduceCooltime, DAMreflect, DAMreflect_rate, ignoreDAMr, ignoreDAMr_rate, ignoreDAM, ignoreDAM_rate, mpRestore,
            hpRecover, hpRecoverProp, hpRecoverPercent, mpRecover, mpRecoverProp, RecoveryUP, BuffUP, RecoveryUP_Skill, BuffUP_Skill,
            incAllskill, combatOrders, BaseDEF, defRange, BuffUP_Summon, dodgeChance, speed, jump, harvestingTool,
            equipmentBonusExp, cashMod, levelBonus, ASR, TER, pickRate, decreaseDebuff, equippedFairy, equippedSummon,
            percent_hp, percent_mp, percent_acc, percent_atk, percent_matk, percent_wdef, percent_mdef,
            pvpDamage, hpRecoverTime = 0, mpRecoverTime = 0, dot, dotTime, questBonus, pvpRank, pvpExp, trueMastery, starforce;
    private transient float localmaxbasedamage, localmaxbasepvpdamage, localmaxbasepvpdamageL;
    public transient int def, element_ice, element_fire, element_light, element_psn;

    // TODO: all psd skills (Passive)
    public final void init(MapleCharacter chra) {
        recalcLocalStats(chra);
    }

    public double getMasterBuff() {
        return masterBuff * 0.01;
    }

    public double getAllStats() {
        return allstat * 0.01;
    }

    public final int getMastery() {
        return trueMastery;
    }

    public final int getStr() {
        return Randomizer.Max(str, Integer.MAX_VALUE);
    }

    public final int getDex() {
        return Randomizer.Max(dex, Integer.MAX_VALUE);
    }

    public final int getLuk() {
        return Randomizer.Max(luk, Integer.MAX_VALUE);
    }

    public final int getInt() {
        return Randomizer.Max(int_, Integer.MAX_VALUE);
    }

    public final long getWDef() {
        return wdef;
    }

    public final long getMDef() {
        return mdef;
    }

    public final long getBStr() {
        return bstr;
    }

    public final long getBDex() {
        return bdex;
    }

    public final long getBInt() {
        return bint;
    }

    public final long getBLuk() {
        return bluk;
    }

    public final long getTStr() {
        return tstr;
    }

    public final long getTDex() {
        return tdex;
    }

    public final long getTInt() {
        return tint;
    }

    public final long getTLuk() {
        return tluk;
    }

    public final long getTatk() {
        return tatk;
    }

    public final long getTmatk() {
        return tmatk;
    }

    public final long getEquipStr() {
        return estr;
    }

    public final long getEquipDex() {
        return edex;
    }

    public final long getEquipInt() {
        return eint;
    }

    public final long getEquipLuk() {
        return eluk;
    }

    public final double getOverpower() {
        return overpower;
    }

    public final double getItempower() {
        return itempower / 100.0;
    }

    public final double getBaseItempower() {
        return itempower;
    }

    public final double getResist() {
        return damResist;
    }

    public final void setStr(final int str, MapleCharacter chra) {
        this.str = str;
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statupdate.put(MapleStat.STR, this.str);
        chra.getClient().announce(CWvsContext.updatePlayerStats(statupdate, true, chra));
        recalcLocalStats(chra);
        chra.updateAP();
    }

    public final void setDex(final int dex, MapleCharacter chra) {
        this.dex = dex;
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statupdate.put(MapleStat.DEX, this.dex);
        chra.getClient().announce(CWvsContext.updatePlayerStats(statupdate, true, chra));
        recalcLocalStats(chra);
        chra.updateAP();
    }

    public final void setInt(final int int_, MapleCharacter chra) {
        this.int_ = int_;
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statupdate.put(MapleStat.INT, this.int_);
        chra.getClient().announce(CWvsContext.updatePlayerStats(statupdate, true, chra));
        recalcLocalStats(chra);
        chra.updateAP();
    }

    public final void setLuk(final int luk, MapleCharacter chra) {
        this.luk = luk;
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statupdate.put(MapleStat.LUK, this.luk);
        chra.getClient().announce(CWvsContext.updatePlayerStats(statupdate, true, chra));
        recalcLocalStats(chra);
        chra.updateAP();
    }

    public final boolean setHp(final int newhp, MapleCharacter chra) {
        return setHp(newhp, false, chra);
    }

    public final boolean setHp(int newhp, boolean silent, MapleCharacter chra) {
        final int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;

        if (chra != null) {
            if (!silent) {
                chra.checkBerserk();
                chra.updatePartyMemberHP();
            }
            if (oldHp > hp && !chra.isAlive()) {
                chra.playerDead();
            }
        }
        return hp != oldHp;
    }

    public final boolean setMp(final int newmp, final MapleCharacter chra) {
        final int oldMp = mp;
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
        return mp != oldMp;
    }

    public final void setInfo(final int maxhp, final int maxmp, final int hp, final int mp) {
        this.maxhp = maxhp;
        this.maxmp = maxmp;
        this.hp = hp;
        this.mp = mp;
    }

    public final void setMaxHp(final int hp, MapleCharacter chra) {
        this.maxhp = hp;
        recalcLocalStats(chra);
    }

    public final void setMaxMp(final int mp, MapleCharacter chra) {
        this.maxmp = mp;
        recalcLocalStats(chra);
    }

    public final int getHp() {
        return Randomizer.Max(hp, GameConstants.getMaxHpMp());
    }

    public final int getMaxHp() {
        return Randomizer.Max(maxhp, GameConstants.getMaxHpMp());
    }

    public final int getMp() {
        return Randomizer.Max(mp, GameConstants.getMaxHpMp());
    }

    public final int getMaxMp() {
        return Randomizer.Max(maxmp, GameConstants.getMaxHpMp());
    }

    public final long getTotalDex() {
        return Randomizer.LongMax(localdex, Long.MAX_VALUE);
    }

    public final long getTotalInt() {
        return Randomizer.LongMax(localint, Long.MAX_VALUE);
    }

    public final long getTotalStr() {
        return Randomizer.LongMax(localstr, Long.MAX_VALUE);
    }

    public final long getTotalLuk() {
        return Randomizer.LongMax(localluk, Long.MAX_VALUE);
    }

    public final long getTotalMagic() {
        return Randomizer.LongMax(magic, Long.MAX_VALUE);
    }

    public final int getSpeed() {
        return speed;
    }

    public final int getJump() {
        return jump;
    }

    public final long getTotalWatk() {
        return Randomizer.LongMax(watk, Long.MAX_VALUE);
    }

    public final int getCurrentMaxHp() {
        return localmaxhp;
    }

    public final int getCurrentMaxMp(final int job) {
        return localmaxmp;
    }

    public final int getHands() {
        return hands;
    }

    public final float getCurrentMaxBaseDamage() {
        return localmaxbasedamage;
    }

    public final float getCurrentMaxBasePVPDamage() {
        return localmaxbasepvpdamage;
    }

    public final float getCurrentMaxBasePVPDamageL() {
        return localmaxbasepvpdamageL;
    }

    public void recalcLocalStats(MapleCharacter chra) {
        recalcLocalStats(false, chra);
    }

    public int getStamina() {
        return stamina;
    }

    public double getItemExpRate() {
        return expItemBuff * 0.01;
    }

    public double getItemKpRate() {
        return kpItemBuff * 0.01;
    }

    public double getAtkR() {
        return percent_atk;
    }

    public double getMAtkR() {
        return percent_matk;
    }

    public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
        if (chra.isClone()) {
            return; //clones share PlayerStats objects and do not need to be recalculated
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int oldmaxhp = localmaxhp;
        int localmaxhp_ = getMaxHp();
        int localmaxmp_ = getMaxMp();
        int localmaxdf = 0;
        accuracy = 0;
        wdef = 0;
        mdef = 0;
        allstat = chra.getAS();
        localdex = dex;
        localint = int_;
        localstr = str;
        localluk = luk;
        tdex = localdex;
        tint = localint;
        tstr = localstr;
        tluk = localluk;
        tatk = 0;
        tmatk = 0;
        speed = 100;
        jump = 100;
        pickupRange = 0.0;
        decreaseDebuff = 0;
        ASR = 0;
        TER = 0;
        dot = 0;
        questBonus = 1;
        dotTime = 0;
        trueMastery = 0;
        kpItemBuff = 100.0;
        percent_wdef = 100 + chra.CDef;
        percent_mdef = 100 + chra.CMdef;
        percent_hp = 100;
        percent_mp = 100;
        percent_str = 100.0 + chra.CStr;
        percent_dex = 100.0 + chra.CDex;
        percent_int = 100.0 + chra.CInt;
        percent_luk = 100.0 + chra.CLuk;
        percent_acc = 0;
        percent_atk = 100 + chra.CAtk;
        percent_matk = 100 + chra.CMatk;
        expItemBuff = 100.0;
        masterBuff = 100.0;
        passive_sharpeye_rate = 5;
        passive_sharpeye_min_percent = (int) (25 + chra.getCD() + chra.CCd);
        passive_sharpeye_percent = (int) (50 + chra.getCD() + chra.CCd);
        magic = 0;
        watk = 0;
        items = 1.0;
        percent_items = 100.0 + chra.getMap().getTotemEtcType(chra);
        starforce = 0;
        overpower = 100.0;
        itempower = 100.0;
        damResist = 0.0;
        if (chra.getJob() == 500 || (chra.getJob() >= 520 && chra.getJob() <= 522)) {
            watk = 20; //bullet
        } else if (chra.getJob() == 400 || (chra.getJob() >= 410 && chra.getJob() <= 412) || (chra.getJob() >= 1400 && chra.getJob() <= 1412)) {
            watk = 30; //stars
        }
        StructItemOption soc;
        dodgeChance = 0;
        pvpDamage = 0;
        mesoGuard = 50.0;
        mesoGuardMeso = 0.0;
        dam_r = 100.0 + chra.getTD() + chra.CMob;
        bossdam_r = 100.0 + chra.getBD() + chra.CBoss;
        expBuff = 100.0 + chra.getXP();
        cashBuff = 100.0;
        dropBuff = 100.0 + chra.getDR();
        mesoBuff = 100.0 + chra.getMR();
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        mpconPercent = 100;
        reduceCooltime = 0;
        DAMreflect = 0;
        DAMreflect_rate = 0;
        ignoreDAMr = 0;
        ignoreDAMr_rate = 0;
        ignoreDAM = 0;
        ignoreDAM_rate = 0;
        long bied = (long) (100 + chra.getIED() + (Math.pow(chra.getFullTotalLevel(), 1.5)));
        ignoreTargetDEF = Randomizer.MaxLong(bied + chra.CIed, Long.MAX_VALUE);
        ignoreTargetPERC = 100.0 + chra.getIEDP();
        hpRecover = 0;
        hpRecoverProp = 0;
        hpRecoverPercent = 0;
        mpRecover = 0;
        mpRecoverProp = 0;
        mpRestore = 0;
        pickRate = 0;
        equippedWelcomeBackRing = false;
        equippedFairy = 0;
        equippedSummon = 0;
        hasPartyBonus = false;
        hasClone = false;
        Berserk = false;
        canFish = GameConstants.GMS;
        canFishVIP = false;
        equipmentBonusExp = 0;
        RecoveryUP = 0;
        BuffUP = 0;
        RecoveryUP_Skill = 0;
        BuffUP_Skill = 100;
        BuffUP_Summon = 100;
        expMod = (double) (1.0);
        dropMod = (double) (1.0);
        mesoMod = (double) (1.0);
        //expMod = chra.getTotalLevel();
        cashMod = 1;
        levelBonus = 0;
        incAllskill = 0;
        combatOrders = 0;
        defRange = 0;
        durabilityHandling.clear();
        equipLevelHandling.clear();
        skillsIncrement.clear();
        damageIncrease.clear();
        setHandling.clear();
        harvestingTool = 0;
        element_fire = 100;
        element_ice = 100;
        element_light = 100;
        element_psn = 100;
        def = 100;
        estr = 0;
        edex = 0;
        eint = 0;
        eluk = 0;
        stamina = 1000;
        if (chra.getPalStorage() != null) {
            if (!chra.getPalStorage().getActivePals().isEmpty()) {
                for (MaplePal pal : chra.getPalStorage().getActivePals()) {
                    //stamina += Randomizer.DoubleMax(pal.getLevel(), 999);
                    localstr += pal.getStats()[1];
                    localdex += pal.getStats()[2];
                    localint += pal.getStats()[3];
                    localluk += pal.getStats()[4];
                    magic += pal.getStats()[5];
                    watk += pal.getStats()[6];
                    wdef += pal.getStats()[7];
                    mdef += pal.getStats()[8];

                    for (int i = 0; i < 4; i++) {
                        int abl = pal.abilities[i];
                        if (abl > 0) {
                            MaplePalAbility par = PalTemplateProvider.getAbility(abl);
                            percent_str += par.getStr();
                            percent_dex += par.getDex();
                            percent_int += par.getInt();
                            percent_luk += par.getLuk();
                            percent_atk += par.getAtk();
                            percent_matk += par.getMatk();
                            percent_wdef += par.getDef();
                            percent_mdef += par.getMdef();
                        }
                    }
                }
            }
        }
        for (MapleTraitType t : MapleTraitType.values()) {
            chra.getTrait(t).clearLocalExp();
        }
        final Map<Skill, SkillEntry> sData = new HashMap<>();
        int rb = (int) chra.getReborns();
        for (Item item : chra.getInventory(MapleInventoryType.EQUIPPED)) {
            final Equip equip = (Equip) item;
            if (!GameConstants.canWearEquip(chra.getClient(), equip)) {
                continue;
            }

            //System.out.println("Equip name: " + MapleItemInformationProvider.getInstance().getName(equip.getItemId()));
            final Map<String, Integer> eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());
            if (equip.getPosition() == -11) {
                if (GameConstants.isMagicWeapon(equip.getItemId())) {
                    if (eqstat != null) {
                        if (eqstat.containsKey("incRMAF")) {
                            element_fire = eqstat.get("incRMAF");
                        }
                        if (eqstat.containsKey("incRMAI")) {
                            element_ice = eqstat.get("incRMAI");
                        }
                        if (eqstat.containsKey("incRMAL")) {
                            element_light = eqstat.get("incRMAL");
                        }
                        if (eqstat.containsKey("incRMAS")) {
                            element_psn = eqstat.get("incRMAS");
                        }
                        if (eqstat.containsKey("elemDefault")) {
                            def = eqstat.get("elemDefault");
                        }
                    }
                }
            }
            if (equip.getItemId() / 10000 == 166 && equip.getAndroid() != null && chra.getAndroid() == null) {
                chra.setAndroid(equip.getAndroid());
            }
            //if (equip.getItemId() / 1000 == 1099) {
            //    equippedForce += equip.getMp();
            //}
            chra.getTrait(MapleTraitType.craft).addLocalExp(equip.getHands());
            accuracy += equip.getAcc();

            if (GameConstants.isForceShield(equip.getItemId())) {
                localmaxdf += equip.getMp();
            }

            localstr += equip.getStr() + equip.getOStr();
            localdex += equip.getDex() + equip.getODex();
            localint += equip.getInt() + equip.getOInt();
            localluk += equip.getLuk() + equip.getOLuk();

            magic += equip.getMatk() + equip.getOMatk();
            watk += equip.getWatk() + equip.getOAtk();
            wdef += equip.getWdef() + equip.getODef();
            mdef += equip.getMdef() + equip.getOMdef();

            speed += equip.getSpeed();
            jump += equip.getJump();
            pvpDamage += equip.getPVPDamage();
            starforce += equip.getEnhance();

            overpower += equip.getOverPower();
            dam_r += equip.getTotalDamage();
            bossdam_r += equip.getBossDamage();

            passive_sharpeye_percent += equip.getCritDamage();
            ignoreTargetPERC += equip.getIED();
            //localmaxhp_ += equip.getHp();
            //localmaxmp_ += equip.getMp();
            //percent_hp += equip.getHpr();
            //percent_mp += equip.getMpr();
            allstat += (double) equip.getAllStat();

            switch (equip.getItemId()) {
                case 1112127:
                    equippedWelcomeBackRing = true;
                    break;
                case 1122017:
                    equippedFairy = 10;
                    break;
                case 1122158:
                    equippedFairy = 5;
                    break;
                case 1112585:
                    equippedSummon = 1085;
                    break;
                case 1112586:
                    equippedSummon = 1087;
                    break;
                case 1112594:
                    equippedSummon = 1179;
                    break;
                default:
                    for (int eb_bonus : GameConstants.Equipments_Bonus) {
                        if (equip.getItemId() == eb_bonus) {
                            equipmentBonusExp += GameConstants.Equipment_Bonus_EXP(eb_bonus);
                            break;
                        }
                    }
                    break;
            } //slow, poison, darkness, seal, freeze
            Integer set = ii.getSetItemID(equip.getItemId());
            if (set != null && set > 0) {
                int value = 1;
                if (setHandling.containsKey(set)) {
                    value += setHandling.get(set).intValue();
                }
                setHandling.put(set, value); //id of Set, number of items to go with the set
            }
            if (equip.getIncSkill() > 0 && ii.getEquipSkills(equip.getItemId()) != null) {
                for (int zzz : ii.getEquipSkills(equip.getItemId())) {
                    final Skill skil = SkillFactory.getSkill(zzz);
                    if (skil != null && skil.canBeLearnedBy(chra.getJob())) { //dont go over masterlevel :D
                        int value = 1;
                        if (skillsIncrement.get(skil.getId()) != null) {
                            value += skillsIncrement.get(skil.getId());
                        }
                        skillsIncrement.put(skil.getId(), value);
                    }
                }

            }
            EnumMap<EquipAdditions, Pair<Integer, Integer>> additions = ii.getEquipAdditions(equip.getItemId());
            if (additions != null) {
                for (Entry<EquipAdditions, Pair<Integer, Integer>> add : additions.entrySet()) {
                    switch (add.getKey()) {
                        case elemboost:
                            int value = add.getValue().right;
                            Element key = Element.getFromId(add.getValue().left);
                            if (elemBoosts.get(key) != null) {
                                value += elemBoosts.get(key);
                            }
                            elemBoosts.put(key, value);
                            break;
                        case mobcategory: //skip the category, thinkings too expensive to have yet another Map<Integer, Integer> for damage calculations
                            dam_r += add.getValue().right;
                            bossdam_r += add.getValue().right;
                            break;
                        case critical:
                            passive_sharpeye_rate += add.getValue().left;
                            passive_sharpeye_min_percent += add.getValue().right;
                            passive_sharpeye_percent += add.getValue().right; //???CONFIRM - not sure if this is max or minCritDmg
                            break;
                        case boss:
                            bossdam_r += add.getValue().right;
                            break;
                        case mobdie:
                            if (add.getValue().left > 0) {
                                hpRecover += add.getValue().left; //no indication of prop, so i made myself
                                hpRecoverProp += 5;
                            }
                            if (add.getValue().right > 0) {
                                mpRecover += add.getValue().right; //no indication of prop, so i made myself
                                mpRecoverProp += 5;
                            }
                            break;
                        case skill: //now, i'm a bit iffy on this one
                            sData.put(SkillFactory.getSkill(add.getValue().left), new SkillEntry((byte) (int) add.getValue().right, (byte) 0, -1));
                            break;
                        case hpmpchange:
                            recoverHP += add.getValue().left;
                            recoverMP += add.getValue().right;
                            break;
                    }
                }
            }
            int[] potentials = {equip.getPotential1(), equip.getPotential2(), equip.getPotential3(), equip.getPotential4(), equip.getPotential5()};
            for (int i : potentials) {
                if (i > 0) {
                    int itemLevel = (int) Math.ceil((double) ii.getReqLevel(equip.getItemId()) / 10);
                    //System.out.println("ilevel = " + itemLevel);
                    //System.out.println("POT ID: " + i + " on equip ID: " + equip.getItemId());
                    //System.out.println("pots: " + ii.getPotentialInfo(i).size());
                    //System.out.println("pots: " + ii.getPotentialInfo(i).size());
                    soc = ii.getPotentialInfo(i).get(Randomizer.MinMax(itemLevel - 1, 0, 19));
                    if (soc != null) {
                        //localmaxhp_ += soc.get("incMHP");
                        //localmaxmp_ += soc.get("incMMP");
                        //localmaxmp_ += (soc.get("incMHPr") + 100.0) / 100.0;
                        //localmaxmp_ += (soc.get("incMMPr") + 100.0) / 100.0;
                        handleItemOption(soc, chra, sData, rb);
                    }
                }
            }
            /*
            if (equip.getSocketState() > 15) {
                int[] sockets = {equip.getSocket1(), equip.getSocket2(), equip.getSocket3()};
                for (int i : sockets) {
                    if (i > 0) {
                        soc = ii.getSocketInfo(i);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            localmaxmp_ += soc.get("incMMP");
                            handleItemOption(soc, chra, sData, rb);
                        }
                    }
                }
            }
             */
            if (equip.getDurability() > 0) {
                durabilityHandling.add((Equip) equip);
            }
            if (GameConstants.getMaxLevel(equip.getItemId()) > 0 && (GameConstants.getStatFromWeapon(equip.getItemId()) == null ? (equip.getEquipLevel() <= GameConstants.getMaxLevel(equip.getItemId())) : (equip.getEquipLevel() < GameConstants.getMaxLevel(equip.getItemId())))) {
                equipLevelHandling.add((Equip) equip);
            }
        }
        /*
        for (Entry<Integer, Integer> entry : setHandling.entrySet()) {
            final StructSetItem set = ii.getSetItem(entry.getKey());
            if (set != null) {
                final Map<Integer, SetItem> itemz = set.getItems();
                for (Entry<Integer, SetItem> ent : itemz.entrySet()) {
                    if (ent.getKey() <= entry.getValue()) {
                        SetItem se = ent.getValue();
                        allstat += se.incAllStat;
                        watk += se.incPAD;
                        magic += se.incMAD;
                        speed += se.incSpeed;
                        accuracy += se.incACC;
                        localmaxhp_ += se.incMHP;
                        localmaxmp_ += se.incMMP;
                        percent_hp += se.incMHPr;
                        percent_mp += se.incMMPr;
                        wdef += se.incPDD;
                        mdef += se.incMDD;
                        if (se.option1 > 0 && se.option1Level > 0) {
                            soc = ii.getPotentialInfo(se.option1).get(se.option1Level);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                localmaxmp_ += soc.get("incMMP");
                                handleItemOption(soc, chra, sData, rb);
                            }
                        }
                        if (se.option2 > 0 && se.option2Level > 0) {
                            soc = ii.getPotentialInfo(se.option2).get(se.option2Level);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                localmaxmp_ += soc.get("incMMP");
                                handleItemOption(soc, chra, sData, rb);
                            }
                        }
                    }
                }
            }
        }
         */
        //handleProfessionTool(chra);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        for (Item item : chra.getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getItemId() / 100000 == 52) {
                if (expMod < 3 && (item.getItemId() == 5211060 || item.getItemId() == 5211050 || item.getItemId() == 5211051 || item.getItemId() == 5211052 || item.getItemId() == 5211053 || item.getItemId() == 5211054)) {
                    expMod += 3.0;//overwrite
                } else if (expMod < 2 && (item.getItemId() == 5210000 || item.getItemId() == 5210001 || item.getItemId() == 5210002 || item.getItemId() == 5210003 || item.getItemId() == 5210004 || item.getItemId() == 5210005 || item.getItemId() == 5211061 || item.getItemId() == 5211000 || item.getItemId() == 5211001 || item.getItemId() == 5211002 || item.getItemId() == 5211003 || item.getItemId() == 5211046 || item.getItemId() == 5211047 || item.getItemId() == 5211048 || item.getItemId() == 5211049)) {
                    expMod += 2.0;
                } else if (expMod < 1.5 && (item.getItemId() == 5211077 || item.getItemId() == 5211078 || item.getItemId() == 5211079 || item.getItemId() == 5211068)) {
                    expMod += 1.5;
                } else if (expMod < 1.2 && (item.getItemId() == 5211071 || item.getItemId() == 5211072 || item.getItemId() == 5211073 || item.getItemId() == 5211074 || item.getItemId() == 5211075 || item.getItemId() == 5211076 || item.getItemId() == 5211067)) {
                    expMod += 1.2;
                }
            } else if (dropMod == 1 && item.getItemId() / 10000 == 536) {
                if (item.getItemId() == 5360000 || item.getItemId() == 5360009 || item.getItemId() == 5360010 || item.getItemId() == 5360011 || item.getItemId() == 5360012 || item.getItemId() == 5360013 || item.getItemId() == 5360014 || item.getItemId() == 5360017 || item.getItemId() == 5360050 || item.getItemId() == 5360053 || item.getItemId() == 5360042 || item.getItemId() == 5360052) {
                    dropMod += 2;
                }
            } else if (item.getItemId() == 5650000) {
                hasPartyBonus = true;
            } else if (item.getItemId() == 5590001) {
                levelBonus = 10;
            } else if (levelBonus == 0 && item.getItemId() == 5590000) {
                levelBonus = 5;
            } else if (item.getItemId() == 5710000) {
                questBonus = 2;
            } else if (item.getItemId() == 5340000) {
                canFish = true;
            } else if (item.getItemId() == 5340001) {
                canFish = true;
                canFishVIP = true;
            }
        }
        for (Item item : chra.getInventory(MapleInventoryType.ETC).list()) { //omfg;
            switch (item.getItemId()) {
                case 4030003:
                    pickupRange = Double.POSITIVE_INFINITY;
                    break;
                case 4030004:
                    hasClone = true;
                    break;
                case 4030005:
                    cashMod = 2;
                    break;
            }
        }
        if (equippedSummon > 0) {
            equippedSummon = getSkillByJob(equippedSummon, chra.getJob());
        }
        Skill bx;
        int bof;
        MapleStatEffect eff = chra.getStatForBuff(MapleBuffStat.MONSTER_RIDING);

        if (eff != null && eff.getSourceId() == 33001001) { //jaguar
            passive_sharpeye_rate += eff.getW();
            //percent_hp += eff.getZ();
        }
        //buffs
        Integer buff = chra.getBuffedValue(MapleBuffStat.DICE_ROLL);
        if (buff != null) {
            percent_wdef += GameConstants.getDiceStat(buff.intValue(), 2);
            percent_mdef += GameConstants.getDiceStat(buff.intValue(), 2);
            passive_sharpeye_rate += GameConstants.getDiceStat(buff.intValue(), 4);
            dam_r += GameConstants.getDiceStat(buff.intValue(), 5);
            bossdam_r += GameConstants.getDiceStat(buff.intValue(), 5);
            expBuff += GameConstants.getDiceStat(buff.intValue(), 6);
        }
        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST_PERCENT);
        if (buff != null) {
            //incAllskill

            //percent_hp += buff.intValue();
            //System.out.println(percent_hp);
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST_PERCENT);
        if (buff != null) {
            percent_mp += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DEFENCE_BOOST_R);
        if (buff != null) {
            percent_wdef += buff.intValue();
            percent_mdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ABNORMAL_STATUS_R);
        if (buff != null) {
            ASR += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ELEMENTAL_STATUS_R);
        if (buff != null) {
            TER += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.INFINITY);
        if (buff != null) {
            percent_matk += buff.intValue() - 1;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ONYX_SHROUD);
        if (buff != null) {
            dodgeChance += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.PVP_DAMAGE);
        if (buff != null) {
            pvpDamage += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.PVP_ATTACK);
        if (buff != null) {
            pvpDamage += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.FELINE_BERSERK);
        if (buff != null) {
            //percent_hp += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.BLUE_AURA);
        if (eff != null) {
            percent_wdef += eff.getZ() + eff.getY();
            percent_mdef += eff.getZ() + eff.getY();
            if (eff.getASRRate() > 0) {
                ASR += eff.getASRRate();
            }
            if (eff.getTERRate() > 0) {
                TER += eff.getTERRate();
            }
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAXMP);
        if (buff != null) {
            percent_mp += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BUFF);
        if (buff != null) {
            percent_mp += buff.intValue();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.BUFF_MASTERY);
        if (buff != null) {
            BuffUP_Skill += buff.intValue();
        }

        bx = SkillFactory.getSkill(12110000);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            TER += bx.getEffect(bof).getX();
        }
        bx = SkillFactory.getSkill(5311001);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            damageIncrease.put(5301001, (int) bx.getEffect(bof).getDAMRate());
        }
        bx = SkillFactory.getSkill(5310007);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            //percent_hp += eff.getPercentHP();
            ASR += eff.getASRRate();
            percent_wdef += eff.getWDEFRate();
            percent_mdef += eff.getMDEFRate();
        }
        final Item shieldEquip = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
        if (shieldEquip != null) {
            boolean shield = GameConstants.isShield(shieldEquip.getItemId());
            if (shield) {
                bx = SkillFactory.getSkill(4210000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }
                bx = SkillFactory.getSkill(1210001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }
            }
        }
        bx = SkillFactory.getSkill(1310000);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            TER += bx.getEffect(bof).getX();
        }
        bx = SkillFactory.getSkill(22160000);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            dam_r += eff.getDAMRate();
            bossdam_r += eff.getDAMRate();
        }
        bx = SkillFactory.getSkill(22150000);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            mpconPercent += eff.getX() - 100;
            dam_r += eff.getDAMRate();
            bossdam_r += eff.getDAMRate();
        }
        //this.localint_ += Math.floor((localint_ * percent_matk) / 100.0f); //overpowered..
        if (GameConstants.isAdventurer(chra.getJob())) {
            bx = SkillFactory.getSkill(74);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(80);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(10074);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(10080);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }
        }
        if (chra.getGuildId() > 0) {
            final MapleGuild g = chra.getGuild();
            if (g != null && g.getSkills().size() > 0) {
                for (MapleGuildSkill gs : g.getSkills()) {
                    if (gs != null && SkillFactory.getSkill(gs.skillID) != null) {
                        final MapleStatEffect e = SkillFactory.getSkill(gs.skillID).getEffect(gs.level);
                        passive_sharpeye_rate += e.getCr();
                        expBuff += e.getEXPRate();
                        dropBuff += e.getDropRate();
                        mesoBuff += e.getMesoRate();
                        dam_r += e.getDAMRate();
                        bossdam_r += e.getBossDamage();
                        ignoreTargetPERC += e.getIgnoreMob();
                        overpower += e.getOverpower();
                        allstat += e.getStats();

                        damResist += e.getResist();
                        BuffUP_Skill += e.getBuffR();

                        watk *= (1 + (e.getAtkR() / 100.0));
                        magic *= (1 + (e.getAtkR() / 100.0));

                        tatk *= (1 + (e.getAtkR() / 100.0));
                        tmatk *= (1 + (e.getAtkR() / 100.0));

                        percent_items += (int) (e.getEtcR() * 100.0);
                        reduceCooltime += e.getBaseCooldown();

                        ASR += e.getASRRate();
                        TER += e.getTERRate();
                    }

                }
            }
        }
        CalcPassiveSkill(chra);
        //System.out.println("PERC: " + percent_mp);
        //System.out.println("Max MP: " + localmaxmp_);
        //magic = Math.min(magic, 1999); //buffs can make it higher
        buff = chra.getBuffedValue(MapleBuffStat.STR);
        if (buff != null) {
            localstr += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DEX);
        if (buff != null) {
            localdex += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.INT);
        if (buff != null) {
            localint += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.LUK);
        if (buff != null) {
            localluk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_STAT);
        if (buff != null) {
            allstat += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXHP);
        if (buff != null) {
            //localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXMP);
        if (buff != null) {
            //localmaxmp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_WDEF);
        if (buff != null) {
            wdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MDEF);
        if (buff != null) {
            mdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            wdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            mdef += buff.intValue();
        }

        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST);
        if (buff != null) {
            //localmaxhp_ += buff.intValue();

        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST);
        if (buff != null) {
            //localmaxmp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (buff != null) {
            allstat += buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ECHO_OF_HERO);
        if (buff != null) {
            percent_atk += buff.doubleValue();
            percent_matk += buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ARAN_COMBO);
        if (buff != null) {
            watk += buff.intValue() / 10;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOGUARD);
        if (buff != null) {
            mesoGuardMeso += buff.doubleValue();
        }
        bx = SkillFactory.getSkill(GameConstants.getBOF_ForJob(chra.getJob()));
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getX();
        }

        bx = SkillFactory.getSkill(GameConstants.getEmpress_ForJob(chra.getJob()));
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getZ();
        }

        buff = chra.getBuffedValue(MapleBuffStat.KPRATE);
        if (buff != null) {
            kpItemBuff = buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.EXPRATE);
        if (buff != null) {
            expItemBuff = buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MASTERRATE);
        if (buff != null) {
            masterBuff = buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
        if (buff != null) {
            expBuff += buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DROP_RATE);
        if (buff != null) {
            double base = (buff.doubleValue()) / 100.0;
            dropBuff = dropBuff * base;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACASH_RATE);
        if (buff != null) {
            cashBuff *= (buff.doubleValue() + 100) / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESO_RATE);
        if (buff != null) {
            mesoBuff += buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOUP);
        if (buff != null) {
            mesoBuff += buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACC);
        if (buff != null) {
            accuracy += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_ACC);
        if (buff != null) {
            accuracy += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_ATK);
        if (buff != null) {
            watk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_MATK);
        if (buff != null) {
            magic += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WATK);
        if (buff != null) {
            watk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.SPIRIT_SURGE);
        if (buff != null) {
            passive_sharpeye_rate += buff.intValue();
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_WATK);
        if (buff != null) {
            watk += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.ENERGY_CHARGE);
        if (eff != null) {
            watk += eff.getWatk();
            accuracy += eff.getAcc();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MATK);
        if (buff != null) {
            magic += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.SPEED);
        if (buff != null) {
            speed += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.JUMP);
        if (buff != null) {
            jump += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_SPEED);
        if (buff != null) {
            speed += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_JUMP);
        if (buff != null) {
            jump += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.HIDDEN_POTENTIAL);
        if (eff != null) {
            passive_sharpeye_rate = 100; //INTENSE
            ASR = 100; //INTENSE

            wdef += eff.getX();
            mdef += eff.getX();
            watk += eff.getX();
            magic += eff.getX();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DAMAGE_BUFF);
        if (buff != null) {
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.FINAL_CUT);
        if (buff != null) {
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.OWL_SPIRIT);
        if (buff != null) {
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.BERSERK_FURY);
        if (buff != null) {
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.BLESS);
        if (eff != null) {
            watk += eff.getX();
            magic += eff.getY();
            def += eff.getZ();
            mdef += eff.getU();
            accuracy += eff.getV();

        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.CONCENTRATE);
        if (buff != null) {
            mpconReduce += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.HOLY_SHIELD);
        if (eff != null) {
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getV();
            mpconReduce += eff.getMPConReduce();
            ASR += eff.getLevel();
        }
        eff = chra.getStatForBuff(MapleBuffStat.MAGIC_RESISTANCE);
        if (eff != null) {
            ASR += eff.getX();
        }

        eff = chra.getStatForBuff(MapleBuffStat.COMBO);
        buff = chra.getBuffedValue(MapleBuffStat.COMBO);
        if (eff != null && buff != null) {
            dam_r += eff.getDAMRate() * (buff.intValue() - 1);
            bossdam_r += eff.getDAMRate() * (buff.intValue() - 1);
        }
        eff = chra.getStatForBuff(MapleBuffStat.SUMMON);
        if (eff != null) {
            if (eff.getSourceId() == 35121010) { //amp
                dam_r += eff.getX();
                bossdam_r += eff.getX();
            }
        }
        eff = chra.getStatForBuff(MapleBuffStat.DARK_AURA);
        if (eff != null) {
            dam_r += eff.getX();
            bossdam_r += eff.getX();
        }
        eff = chra.getStatForBuff(MapleBuffStat.BODY_BOOST);
        if (eff != null) {
            dam_r += eff.getV();
            bossdam_r += eff.getV();
        }
        eff = chra.getStatForBuff(MapleBuffStat.BEHOLDER);
        if (eff != null) {
            trueMastery += eff.getMastery();
        }
        eff = chra.getStatForBuff(MapleBuffStat.MECH_CHANGE);
        if (eff != null) {
            passive_sharpeye_rate += eff.getCr();
        }
        eff = chra.getStatForBuff(MapleBuffStat.PYRAMID_PQ);
        if (eff != null && eff.getBerserk() > 0) {
            dam_r += eff.getBerserk();
            bossdam_r += eff.getBerserk();
        }
        eff = chra.getStatForBuff(MapleBuffStat.WK_CHARGE);
        if (eff != null) {
            dam_r += eff.getDamage();
            bossdam_r += eff.getDamage();
        }
        eff = chra.getStatForBuff(MapleBuffStat.PICKPOCKET);
        if (eff != null) {
            pickRate = eff.getProb();
        }
        eff = chra.getStatForBuff(MapleBuffStat.PIRATES_REVENGE);
        if (eff != null) {
            dam_r += eff.getDAMRate();
            bossdam_r += eff.getDAMRate();
        }
        eff = chra.getStatForBuff(MapleBuffStat.LIGHTNING_CHARGE);
        if (eff != null) {
            dam_r += eff.getDamage();
            bossdam_r += eff.getDamage();
        }
        eff = chra.getStatForBuff(MapleBuffStat.WIND_WALK);
        if (eff != null) {
            dam_r += eff.getDamage();
            bossdam_r += eff.getDamage();
        }
        eff = chra.getStatForBuff(MapleBuffStat.DIVINE_SHIELD);
        if (eff != null) {
            watk += eff.getEnhancedWatk();
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.DARKSIGHT);
        if (buff != null) {
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.ENRAGE);
        if (buff != null) {
            dam_r += buff.intValue();
            bossdam_r += buff.intValue();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.COMBAT_ORDERS);
        if (buff != null) {
            combatOrders += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.SHARP_EYES);
        if (eff != null) {
            passive_sharpeye_rate += eff.getX();
            passive_sharpeye_percent += eff.getCriticalMax();
        }
        buff = chra.getBuffedValue(MapleBuffStat.CRITICAL_RATE_BUFF);
        if (buff != null) {
            passive_sharpeye_rate += buff.intValue();
        }
        if (speed > 500) {
            speed = 500;
        }
        if (jump > 250) {
            jump = 250;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (buff != null) {
            jump = 120;
            switch (buff.intValue()) {
                case 1:
                    speed = 150;
                    break;
                case 2:
                    speed = 170;
                    break;
                case 3:
                    speed = 180;
                    break;
                default:
                    speed = 200; //lol
                    break;
            }

        }
        //hands = this.localdex + this.localint_ + this.localluk;
        calculateFame(chra);
        ignoreTargetDEF += (chra.getTrait(MapleTraitType.charisma).getLevel() - 1) / 10;
        //pvpDamage += chra.getTrait(MapleTraitType.charisma).getLevel() / 10;
        //localmaxmp_ += (chra.getTrait(MapleTraitType.sense).getLevel() - 1) * 20;
        //localmaxhp_ += (chra.getTrait(MapleTraitType.will).getLevel() - 1) * 20;
        ASR += (chra.getTrait(MapleTraitType.will).getLevel() - 1) / 5;
        accuracy += chra.getTrait(MapleTraitType.insight).getLevel() * 15 / 10;

        if (chra.getEventInstance() != null && chra.getEventInstance().getName().startsWith("PVP")) { //hack
            localmaxhp = Math.min(40000, localmaxhp * 3); //approximate.
            localmaxmp = Math.min(20000, localmaxmp * 2);
            //not sure on 20000 cap
            for (int i : pvpSkills) {
                Skill skil = SkillFactory.getSkill(i);
                if (skil != null && skil.canBeLearnedBy(chra.getJob())) {
                    sData.put(skil, new SkillEntry((byte) 1, (byte) 0, -1));
                    eff = skil.getEffect(1);
                    switch ((i / 1000000) % 10) {
                        case 1:
                            if (eff.getX() > 0) {
                                pvpDamage += (wdef / eff.getX());
                            }
                            break;
                        case 3:
                            hpRecoverProp += eff.getProb();
                            hpRecover += eff.getX();
                            mpRecoverProp += eff.getProb();
                            mpRecover += eff.getX();
                            break;
                        case 5:
                            passive_sharpeye_rate += eff.getProb();
                            passive_sharpeye_percent = 100;
                            break;
                    }
                    break;
                }
            }
            eff = chra.getStatForBuff(MapleBuffStat.MORPH);
            if (eff != null && eff.getSourceId() % 10000 == 1105) { //ice knight
                localmaxhp = 999999;
                localmaxmp = 999999;
            }
        }
        chra.changeSkillLevel_Skip(sData, false);
        bx = SkillFactory.getSkill(2110001);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            mpconPercent += eff.getX() - 100;
            dam_r += eff.getDAMRate();
            bossdam_r += eff.getDAMRate();
        }
        bx = SkillFactory.getSkill(2120009);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            //magic += eff.getMagicX();
            BuffUP_Skill += eff.getX();
        }
        bx = SkillFactory.getSkill(2120010);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            dam_r += (eff.getX() * eff.getY());
            bossdam_r += (eff.getX() * eff.getY());
            //ignoreTargetDEF += eff.getIgnoreMob();
        }
        bx = SkillFactory.getSkill(2210001);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            mpconPercent += eff.getX() - 100;
            dam_r += eff.getDAMRate();
            bossdam_r += eff.getDAMRate();
        }
        bx = SkillFactory.getSkill(2220009);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            //magic += eff.getMagicX();
            BuffUP_Skill += eff.getX();
        }
        bx = SkillFactory.getSkill(2220010);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            dam_r += (eff.getX() * eff.getY());
            bossdam_r += (eff.getX() * eff.getY());
            //ignoreTargetDEF += eff.getIgnoreMob();
        }
        bx = SkillFactory.getSkill(2320010);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            //magic += eff.getMagicX();
            BuffUP_Skill += eff.getX();
        }
        bx = SkillFactory.getSkill(2320011);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            dam_r += (eff.getX() * eff.getY());
            bossdam_r += (eff.getX() * eff.getY());
            //ignoreTargetDEF += eff.getIgnoreMob();
        }
        bx = SkillFactory.getSkill(12110001);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            mpconPercent += eff.getX() - 100;
            dam_r += eff.getDAMRate();
            bossdam_r += eff.getDAMRate();
        }
        bx = SkillFactory.getSkill(23120010);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            ignoreTargetDEF += bx.getEffect(bof).getX(); //or should we do 100?
        }

        bx = SkillFactory.getSkill(23120009);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            watk += bx.getEffect(bof).getX();
            magic += bx.getEffect(bof).getX();
        }
        bx = SkillFactory.getSkill(3220004);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            watk += bx.getEffect(bof).getX();
            magic += bx.getEffect(bof).getX();
        }
        bx = SkillFactory.getSkill(4220009);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            mesoBuff += eff.getMesoRate();
            pickRate += eff.getU();
            mesoGuard -= eff.getV();
            mesoGuardMeso -= eff.getW();
            damageIncrease.put(4211006, eff.getX());
        }
        bx = SkillFactory.getSkill(4330007);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            hpRecoverProp += eff.getProb();
            hpRecoverPercent += eff.getX();
        }
        bx = SkillFactory.getSkill(4110000);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            RecoveryUP += eff.getX() - 100;
            BuffUP += eff.getY() - 100;
        }
        bx = SkillFactory.getSkill(14110003);
        bof = chra.getTotalSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            RecoveryUP += eff.getX() - 100;
            BuffUP += eff.getY() - 100;
        }
        //ranged job
        if (GameConstants.isBowmanJob(chra.getJob()) || GameConstants.isPirateJob(chra.getJob()) || GameConstants.isThiefJob(chra.getJob())) {
            defRange = 200;
            bx = SkillFactory.getSkill(4000001);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                defRange += bx.getEffect(bof).getRange();
            }
            bx = SkillFactory.getSkill(3000002);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                defRange += bx.getEffect(bof).getRange();
            }
            bx = SkillFactory.getSkill(13000001);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                defRange += bx.getEffect(bof).getRange();
            }
            bx = SkillFactory.getSkill(3000002);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                defRange += bx.getEffect(bof).getRange();
            }
        }

        switch (chra.getJob()) {
            case 2001:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
                bx = SkillFactory.getSkill(20020112);
                bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    chra.getTrait(MapleTraitType.charm).addLocalExp(GameConstants.getTraitExpNeededForLevel(30));
                }
                break;
            case 433:
            case 434:
                bx = SkillFactory.getSkill(4341002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Fatal Blow, Slash Storm, Tornado Spin, Bloody Storm, Upper Stab, and Flying Assaulter
                    eff = bx.getEffect(bof);
                    dam_r += eff.getDAMRate();
                    bossdam_r += eff.getDAMRate();
                }
                break;
            case 511:
            case 512:
                bx = SkillFactory.getSkill(5110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Backspin Blow, Double Uppercut, and Corkscrew Blow
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5101010, eff.getX());
                    damageIncrease.put(5101003, eff.getY());
                    damageIncrease.put(5101011, eff.getZ());
                }
                break;
            case 520:
            case 521:
            case 522:
                bx = SkillFactory.getSkill(5220001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Flamethrower and Ice Splitter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5211010, (int) eff.getDamage());
                    damageIncrease.put(5211011, (int) eff.getDamage());
                }
                break;
            case 130:
            case 131:
            case 132:
                bx = SkillFactory.getSkill(1310009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    hpRecoverProp += eff.getProb();
                    hpRecoverPercent += eff.getX();
                }
                bx = SkillFactory.getSkill(1320006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r += eff.getDamage();
                    bossdam_r += eff.getDamage();
                }
                break;
        }
        if (GameConstants.isResist(chra.getJob())) {
            bx = SkillFactory.getSkill(30000002);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                RecoveryUP += bx.getEffect(bof).getX() - 100;
            }
        }

        CalcPassive_Mastery(chra);
        //percent_str = Randomizer.DoubleMax(percent_str + chra.getAS(), 9999999);
        //percent_dex = Randomizer.DoubleMax(percent_dex + chra.getAS(), 9999999);
        //percent_int = Randomizer.DoubleMax(percent_int + chra.getAS(), 9999999);
        //percent_luk = Randomizer.DoubleMax(percent_luk + chra.getAS(), 9999999);

        localstr += allstat;
        localdex += allstat;
        localint += allstat;
        localluk += allstat;
        //System.out.println("Str: " + chra.DBD);

        long tempStr = (long) (localstr * (percent_str / 100.0) * (1 + (p_str * 0.01)));
        long tempDex = (long) (localdex * (percent_dex / 100.0) * (1 + (p_dex * 0.01)));
        long tempInt = (long) (localint * (percent_int / 100.0) * (1 + (p_int * 0.01)));
        long tempLuk = (long) (localluk * (percent_luk / 100.0) * (1 + (p_luk * 0.01)));

        items = (percent_items / 100.0);

        tstr = tempStr;
        tdex = tempDex;
        tint = tempInt;
        tluk = tempLuk;

        bstr = localstr;
        bdex = localdex;
        bint = localint;
        bluk = localluk;

        estr = tempStr - str;
        edex = tempDex - dex;
        eint = tempInt - int_;
        eluk = tempLuk - luk;

        //localstr = tempStr;
        //localdex = tempDex;
        //localint_ = tempInt;
        //localluk = tempLuk;
        /*
         if (localint_ > localdex) {
         accuracy += localint_ + Math.floor(localluk * 1.2);
         } else {
         accuracy += localluk + Math.floor(localdex * 1.2);
         }
         accuracy += Math.floor((accuracy * percent_acc) / 100.0f);
         */
        //System.out.println("base local hp: " + localmaxhp_);
        //localmaxhp_ *= (percent_hp / 100.0f);
        //localmaxmp_ *= (percent_mp / 100.0f);
        localmaxhp = Randomizer.Max(localmaxhp_, GameConstants.getMaxHpMp());
        //localmaxhp = Randomizer.Max(localmaxhp_, Integer.MAX_VALUE);
        if (GameConstants.isDemon(chra.getJob())) {
            localmaxmp = GameConstants.getMPByJob(chra.getJob()) + localmaxdf;
        } else {
            localmaxmp = Randomizer.Max(localmaxmp_, GameConstants.getMaxHpMp());
        }
        //System.out.println("total local hp: " + localmaxhp_);
        //System.out.println("% hp: " + percent_hp);
        //System.out.println("hp: " + hp);
        //System.out.println("maxhp: " + maxhp);
        //System.out.println("localamxhp: " + localmaxhp);
        //System.out.println("mp: " + mp);
        //System.out.println("maxmp: " + maxmp);
        //System.out.println("localamxmp: " + localmaxmp);
        //System.out.println("hp: " + hp);
        //System.out.println("maxhp: " + maxhp);
        //System.out.println("localamxhp: " + localmaxhp);
        //wdef += (localstr * 1.5) + ((localdex + localluk) * 0.4);
        //mdef += (localint_ * 1.5) + ((localdex + localluk) * 0.4);

        wdef *= (percent_wdef / 100.0f) * (1 + (p_def * 0.01));
        mdef *= (percent_mdef / 100.0f) * (1 + (p_mdef * 0.01));

        long tempAtk = (long) (watk * (percent_atk / 100.0) * (1 + (p_atk * 0.01)));
        long tempMatk = (long) (magic * (percent_matk / 100.0) * (1 + (p_matk * 0.01)));
        tatk = tempAtk;
        tmatk = tempMatk;
        watk = Randomizer.LongMax(tempAtk, Long.MAX_VALUE);
        magic = Randomizer.LongMax(tempMatk, Long.MAX_VALUE);

        //calculateMaxBaseDamage(Math.max(magic, watk), pvpDamage, chra);
        trueMastery = Math.min(100, trueMastery);

        passive_sharpeye_min_percent = (Math.min(passive_sharpeye_min_percent, passive_sharpeye_percent));
        expMod *= (expBuff / 100.0f);
        dropMod *= (dropBuff / 100.0f);
        mesoMod *= (mesoBuff / 100.0f);
        //System.out.println("drop mod " + dropMod);
        //System.out.println("drop Buff " + dropBuff);
        if (first_login) {
            chra.silentEnforceMaxHpMp();
            relocHeal(chra);
        } else {
            chra.enforceMaxHpMp();
        }

        ignoreTargetDEF = Randomizer.MaxLong((long) (ignoreTargetDEF * (ignoreTargetPERC / 100.0f)), Long.MAX_VALUE);
        overpower += chra.getOP();
        damResist = Randomizer.DoubleMax((damResist + chra.getResist()) / 100.0f, 0.90);
        ASR = (Randomizer.Max(ASR, 100));
        dam_r += overpower;
        bossdam_r += overpower;

        /*
         dam_r = (Randomizer.DoubleMax(dam_r, 99999));
         bossdam_r = (Randomizer.DoubleMax(bossdam_r, 99999));
         overpower = (Randomizer.DoubleMax(overpower, 99999));

         expMod = (Randomizer.DoubleMax(expMod, 999.99));
         dropMod = (Randomizer.DoubleMax(dropMod, 999.99));
         mesoMod = (Randomizer.DoubleMax(mesoMod, 999.99));
         */
        if (starforce >= 25 && !chra.getAchievement(80)) {
            chra.finishAchievement(80);
        }
        if (starforce >= 50 && !chra.getAchievement(81)) {
            chra.finishAchievement(81);
        }
        if (starforce >= 100 && !chra.getAchievement(82)) {
            chra.finishAchievement(82);
        }
        if (starforce >= 250 && !chra.getAchievement(83)) {
            chra.finishAchievement(83);
        }
        if (starforce >= 500 && !chra.getAchievement(84)) {
            chra.finishAchievement(84);
        }
        if (starforce >= 750 && !chra.getAchievement(85)) {
            chra.finishAchievement(85);
        }

        //chra.setBattlePoints(starforce);
        //chra.updateSingleStat(MapleStat.BATTLE_POINTS, 0);
        //chra.updateSkills(chra.getJob());
        chra.getClient().announce(CField.customMainStatUpdate(chra));
        chra.getClient().announce(CField.customStatDetail(chra));
        if (chra.getJob() == 132) { // DARKKNIGHT
            chra.checkBerserk();
        }
    }

    public boolean checkEquipLevels(final MapleCharacter chr, int gain) {
        if (chr.isClone()) {
            return false;
        }
        boolean changed = false;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Equip> all = new ArrayList<>(equipLevelHandling);
        for (Equip eq : all) {
            int lvlz = eq.getEquipLevel();
            eq.setItemEXP(eq.getItemEXP() + gain);

            if (eq.getEquipLevel() > lvlz) { //lvlup
                for (int i = eq.getEquipLevel() - lvlz; i > 0; i--) {
                    //now for the equipment increments...
                    final Map<Integer, Map<String, Integer>> inc = ii.getEquipIncrements(eq.getItemId());
                    if (inc != null && inc.containsKey(lvlz + i)) { //flair = 1
                        eq = ii.levelUpEquip(eq, inc.get(lvlz + i));
                    }
                    //UGH, skillz
                    if (GameConstants.getStatFromWeapon(eq.getItemId()) == null && GameConstants.getMaxLevel(eq.getItemId()) < (lvlz + i) && Math.random() < 0.1 && eq.getIncSkill() <= 0 && ii.getEquipSkills(eq.getItemId()) != null) {
                        for (int zzz : ii.getEquipSkills(eq.getItemId())) {
                            final Skill skil = SkillFactory.getSkill(zzz);
                            if (skil != null && skil.canBeLearnedBy(chr.getJob())) { //dont go over masterlevel :D
                                eq.setIncSkill(skil.getId());
                                chr.dropMessage(5, "Your skill has gained a levelup: " + skil.getName() + " +1");
                                chr.updateSkills(chr.getJob());
                            }
                        }
                    }
                }
                changed = true;
            }
            chr.forceReAddItem(eq.copy(), MapleInventoryType.EQUIPPED);
        }
        if (changed) {
            chr.equipChanged();
            chr.getClient().announce(EffectPacket.showItemLevelupEffect());
            chr.getMap().broadcastMessage(chr, EffectPacket.showForeignItemLevelupEffect(chr.getId()), false);
        }
        return changed;
    }

    public boolean checkEquipDurabilitys(final MapleCharacter chr, int gain) {
        return checkEquipDurabilitys(chr, gain, false);
    }

    public boolean checkEquipDurabilitys(final MapleCharacter chr, int gain, boolean aboveZero) {
        if (chr.isClone() || chr.inPVP()) {
            return true;
        }
        List<Equip> all = new ArrayList<>(durabilityHandling);
        for (Equip item : all) {
            if (item != null && ((item.getPosition() >= 0) == aboveZero)) {
                item.setDurability(item.getDurability() + gain);
                if (item.getDurability() < 0) { //shouldnt be less than 0
                    item.setDurability(0);
                }
            }
        }
        for (Equip eqq : all) {
            if (eqq != null && eqq.getDurability() == 0 && eqq.getPosition() < 0) { //> 0 went to negative
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    chr.getClient().announce(InventoryPacket.getInventoryFull());
                    chr.getClient().announce(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                durabilityHandling.remove(eqq);
                final short pos = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                MapleInventoryManipulator.unequip(chr.getClient(), eqq.getPosition(), pos);
            } else if (eqq != null) {
                chr.forceReAddItem(eqq.copy(), MapleInventoryType.EQUIPPED);
            }
        }
        return true;
    }

    public final void handleProfessionTool(final MapleCharacter chra) {
        if (chra.getProfessionLevel(92000000) > 0 || chra.getProfessionLevel(92010000) > 0) {
            final Iterator<Item> itera = chra.getInventory(MapleInventoryType.EQUIP).newList().iterator();
            while (itera.hasNext()) { //goes to first harvesting tool and stops
                final Equip equip = (Equip) itera.next();
                if (equip.getDurability() != 0 && (equip.getItemId() / 10000 == 150 && chra.getProfessionLevel(92000000) > 0) || (equip.getItemId() / 10000 == 151 && chra.getProfessionLevel(92010000) > 0)) {
                    if (equip.getDurability() > 0) {
                        durabilityHandling.add(equip);
                    }
                    harvestingTool = equip.getPosition();
                    break;
                }
            }
        }
    }

    private void CalcPassive_Mastery(final MapleCharacter player) {
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11) == null) {
            passive_mastery = 0;
            return;
        }
        final int skil;
        final MapleWeaponType weaponType = GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11).getItemId());
        boolean acc = true;
        switch (weaponType) {
            case BOW:
                skil = GameConstants.isKOC(player.getJob()) ? 13100000 : 3100000;
                break;
            case CLAW:
                skil = 4100000;
                break;
            case CANE:
                skil = player.getTotalSkillLevel(24120006) > 0 ? 24120006 : 24100004;
                break;
            case CANNON:
                skil = 5300005;
                break;
            case KATARA:
            case DAGGER:
                skil = player.getJob() >= 430 && player.getJob() <= 434 ? 4300000 : 4200000;
                break;
            case CROSSBOW:
                skil = GameConstants.isResist(player.getJob()) ? 33100000 : 3200000;
                break;
            case AXE1H:
            case BLUNT1H:
                skil = GameConstants.isResist(player.getJob()) ? 31100004 : (GameConstants.isKOC(player.getJob()) ? 11100000 : (player.getJob() > 112 ? 1200000 : 1100000)); //hero/pally
                break;
            case AXE2H:
            case SWORD1H:
            case SWORD2H:
            case BLUNT2H:
                skil = GameConstants.isKOC(player.getJob()) ? 11100000 : (player.getJob() > 112 ? 1200000 : 1100000); //hero/pally
                break;
            case POLE_ARM:
                skil = GameConstants.isAran(player.getJob()) ? 21100000 : 1300000;
                break;
            case SPEAR:
                skil = 1300000;
                break;
            case KNUCKLE:
                skil = GameConstants.isKOC(player.getJob()) ? 15100001 : 5100001;
                break;
            case GUN:
                skil = GameConstants.isResist(player.getJob()) ? 35100000 : 5200000;
                break;
            case DUAL_BOW:
                skil = 23100005;
                break;
            case WAND:
            case STAFF:
                acc = false;
                skil = GameConstants.isResist(player.getJob()) ? 32100006 : (player.getJob() <= 212 ? 2100006 : (player.getJob() <= 222 ? 2200006 : (player.getJob() <= 232 ? 2300006 : (player.getJob() <= 2000 ? 12100007 : 22120002))));
                break;
            default:
                passive_mastery = 0;
                return;

        }
        Skill skill = SkillFactory.getSkill(skil);
        if (player.getSkillLevel(skill) <= 0) {
            passive_mastery = 0;
            return;
        }

        final MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
        if (acc) {
            accuracy += eff.getX();
            if (skil == 35100000) {
                watk += eff.getX();
            }
        } else {
            magic += eff.getX();
        }
        trueMastery += eff.getMastery() + weaponType.getBaseMastery();
    }

    private void calculateFame(final MapleCharacter player) {
        player.getTrait(MapleTraitType.charm).addLocalExp(player.getFame());
        for (MapleTraitType t : MapleTraitType.values()) {
            player.getTrait(t).recalcLevel();
        }
    }

    private void CalcPassiveSkill(final MapleCharacter player) {
        int critlevel;
        for (Skill critSkill : player.getAllSkills()) {
            if (!critSkill.isBuff()) {
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    MapleStatEffect skill = critSkill.getEffect(critlevel);
                    if (skill != null) {
                        if (critlevel > 0) {
                            if (skill.getCr() > 0) {
                                passive_sharpeye_rate += skill.getCr();
                            }
                            if (skill.getCriticalMin() > 0) {
                                passive_sharpeye_min_percent += skill.getCriticalMin();
                            }
                            if (skill.getCriticalMax() > 0) {
                                passive_sharpeye_percent += skill.getCriticalMax();
                            }
                            if (skill.getMastery() > 0) {
                                passive_mastery = (byte) skill.getMastery(); //after bb, simpler?
                            }
                            if (skill.getDAMRate() > 0) {
                                dam_r += skill.getDAMRate();
                            }
                            if (skill.getBossDamage() > 0) {
                                bossdam_r += skill.getBossDamage();
                            }
                            if (skill.getIgnoreMob() > 0) {
                                ignoreTargetPERC += skill.getIgnoreMob();
                            }
                            if (skill.getStr() > 0) {
                                localstr += skill.getStr();
                            }
                            if (skill.getDex() > 0) {
                                localdex += skill.getDex();
                            }
                            if (skill.getInt() > 0) {
                                localint += skill.getInt();
                            }
                            if (skill.getLuk() > 0) {
                                localluk += skill.getLuk();
                            }
                            if (skill.getStrX() > 0) {
                                localstr += skill.getStrX();
                            }
                            if (skill.getDexX() > 0) {
                                localdex += skill.getDexX();
                            }
                            if (skill.getIntX() > 0) {
                                localint += skill.getIntX();
                            }
                            if (skill.getLukX() > 0) {
                                localluk += skill.getLukX();
                            }
                            if (skill.getStrR() > 0) {
                                percent_str += skill.getStrR();
                            }
                            if (skill.getDexR() > 0) {
                                percent_dex += skill.getDexR();
                            }
                            if (skill.getIntR() > 0) {
                                percent_int += skill.getIntR();
                            }
                            if (skill.getLukR() > 0) {
                                percent_luk += skill.getLukR();
                            }
                            if (skill.getWatk() > 0) {
                                watk += skill.getWatk();
                            }
                            if (skill.getMatk() > 0) {
                                magic += skill.getMatk();
                            }
                            if (skill.getWdef() > 0) {
                                def += skill.getWdef();
                            }
                            if (skill.getMdef() > 0) {
                                mdef += skill.getMdef();
                            }
                            if (skill.getWdefX() > 0) {
                                percent_wdef += skill.getWdefX();
                            }
                            if (skill.getMdefX() > 0) {
                                percent_mdef += skill.getMdefX();
                            }
                            if (skill.getPassiveSpeed() > 0) {
                                speed += skill.getPassiveSpeed();
                            }
                            if (skill.getPassiveJump() > 0) {
                                jump += skill.getPassiveJump();
                            }
                            if (skill.getASRRate() > 0) {
                                ASR += skill.getASRRate();
                            }
                            if (skill.getTERRate() > 0) {
                                TER += skill.getTERRate();
                            }
                            if (skill.getWDEFRate() > 0) {
                                percent_wdef += skill.getWDEFRate();
                            }
                            if (skill.getMDEFRate() > 0) {
                                percent_mdef += skill.getMDEFRate();
                            }
                            if (skill.getWdefR() > 0) {
                                percent_wdef += skill.getWdefR();
                            }
                            if (skill.getMdefR() > 0) {
                                percent_mdef += skill.getMdefR();
                            }
                            if (skill.getER() > 0) {
                                dodgeChance += skill.getER();
                            }
                            if (skill.getEXPRate() > 0) {
                                expBuff += skill.getEXPRate();
                            }
                            if (skill.getOverpower() > 0) {
                                overpower += skill.getOverpower();
                            }
                            if (skill.getAttackX() > 0) {
                                watk += skill.getAttackX();
                            }
                            if (skill.getMagicX() > 0) {
                                magic += skill.getMagicX();
                            }
                            if (skill.getWatkR() > 0) {
                                percent_atk += skill.getWatkR();
                            }
                            if (skill.getMatkR() > 0) {
                                percent_matk += skill.getMatkR();
                            }
                            if (skill.getAtkR() > 0) {
                                percent_atk += skill.getAtkR();
                                percent_matk += skill.getAtkR();
                            }
                            if (skill.getAllStat() > 0) {
                                allstat += skill.getAllStat();
                            }
                            if (skill.getResist() > 0) {
                                damResist += skill.getResist();
                            }
                            if (skill.getDropRate() > 0) {
                                dropBuff += skill.getDropRate();
                            }
                            if (skill.getMesoRate() > 0) {
                                mesoBuff += skill.getMesoRate();
                            }
                        }
                    }
                }
            }
        }
    }

    public final int passive_sharpeye_min_percent() {
        return passive_sharpeye_min_percent;
    }

    public final int passive_sharpeye_percent() {
        return passive_sharpeye_percent;
    }

    public final int passive_sharpeye_rate() {
        return passive_sharpeye_rate;
    }

    public final byte passive_mastery() {
        return passive_mastery; //* 5 + 10 for mastery %
    }

    /*
     public final void calculateMaxBaseDamage(final int watk, final int pvpDamage, MapleCharacter chra) {
     if (watk <= 0) {
     localmaxbasedamage = 1;
     localmaxbasepvpdamage = 1;
     } else {
     final Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
     final Item weapon_item2 = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
     final int job = chra.getJob();
     final MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item.getItemId());
     final MapleWeaponType weapon2 = weapon_item2 == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item2.getItemId());
     int mainstat, secondarystat, mainstatpvp, secondarystatpvp;
     final boolean mage = (job >= 200 && job <= 232) || (job >= 1200 && job <= 1212) || (job >= 2200 && job <= 2218) || (job >= 3200 && job <= 3212);
     switch (weapon) {
     case BOW:
     case CROSSBOW:
     case GUN:
     mainstat = localdex;
     secondarystat = localstr;
     mainstatpvp = dex;
     secondarystatpvp = str;
     break;
     case DAGGER:
     case KATARA:
     case CLAW:
     case CANE:
     mainstat = localluk;
     secondarystat = localdex + localstr;
     mainstatpvp = luk;
     secondarystatpvp = dex + str;
     break;
     default:
     if (mage) {
     mainstat = localint_;
     secondarystat = localluk;
     mainstatpvp = int_;
     secondarystatpvp = luk;
     } else {
     mainstat = localstr;
     secondarystat = localdex;
     mainstatpvp = str;
     secondarystatpvp = dex;
     }
     break;
     }
     localmaxbasepvpdamage = weapon.getMaxDamageMultiplier() * (4 * mainstatpvp + secondarystatpvp) * (100.0f + (pvpDamage / 100.0f));
     localmaxbasepvpdamageL = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (100.0f + (pvpDamage / 100.0f));
     if (weapon2 != MapleWeaponType.NOT_A_WEAPON && weapon_item != null && weapon_item2 != null) {
     Equip we1 = (Equip) weapon_item;
     Equip we2 = (Equip) weapon_item2;
     localmaxbasedamage = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * ((watk - (mage ? we2.getMatk() : we2.getWatk())) / 100.0f);
     localmaxbasedamage += weapon2.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * ((watk - (mage ? we1.getMatk() : we1.getWatk())) / 100.0f);
     } else {
     localmaxbasedamage = weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (watk / 100.0f);
     }
     }
     }
     */
    public final float getHealHP() {
        return shouldHealHP;
    }

    public final float getHealMP() {
        return shouldHealMP;
    }

    public final void relocHeal(MapleCharacter chra) {
        if (chra.isClone()) {
            return;
        }
        final int playerjob = chra.getJob();

        shouldHealHP = 10 + recoverHP; // Reset
        shouldHealMP = (float) (GameConstants.isDemon(chra.getJob()) ? 0 : (3 + mpRestore + recoverMP + (localint / 10.0f))); // i think
        mpRecoverTime = 0;
        hpRecoverTime = 0;
        if (playerjob == 111 || playerjob == 112) {
            final Skill effect = SkillFactory.getSkill(1110000); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                MapleStatEffect eff = effect.getEffect(lvl);
                if (eff.getHp() > 0) {
                    shouldHealHP += eff.getHp();
                    hpRecoverTime = 4000;
                }
                shouldHealMP += eff.getMp();
                mpRecoverTime = 4000;
            }

        } else if (playerjob == 1111 || playerjob == 1112) {
            final Skill effect = SkillFactory.getSkill(11110000); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                shouldHealMP += effect.getEffect(lvl).getMp();
                mpRecoverTime = 4000;
            }
        } else if (GameConstants.isMercedes(playerjob)) {
            final Skill effect = SkillFactory.getSkill(20020109); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                shouldHealHP += (effect.getEffect(lvl).getX() * localmaxhp) / 100;
                hpRecoverTime = 4000;
                shouldHealMP += (effect.getEffect(lvl).getX() * localmaxmp) / 100;
                mpRecoverTime = 4000;
            }
        } else if (playerjob == 3111 || playerjob == 3112) {
            final Skill effect = SkillFactory.getSkill(31110009); // Improving MP Recovery
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                shouldHealMP += effect.getEffect(lvl).getY();
                mpRecoverTime = 4000;
            }
        }
        if (chra.getChair() != 0) { // Is sitting on a chair.
            shouldHealHP += 99; // Until the values of Chair heal has been fixed,
            shouldHealMP += 99; // MP is different here, if chair data MP = 0, heal + 1.5
        } else if (chra.getMap() != null) { // Because Heal isn't multipled when there's a chair :)
            final float recvRate = chra.getMap().getRecoveryRate();
            if (recvRate > 0) {
                shouldHealHP *= recvRate;
                shouldHealMP *= recvRate;
            }
        }
    }

    public final void connectData(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(0); // str
        mplew.writeShort(0); // dex
        mplew.writeShort(0); // int
        mplew.writeShort(0); // luk

        mplew.writeInt(Randomizer.Max(hp, GameConstants.getMaxHpMp())); // hp -- INT after bigbanga
        mplew.writeInt(Randomizer.Max(maxhp, GameConstants.getMaxHpMp())); // maxhp
        //System.out.println("MP/MMP: " + mp +"/" + maxmp);
        mplew.writeInt(Randomizer.Max(mp, GameConstants.getMaxHpMp())); // mp
        mplew.writeInt(Randomizer.Max(maxmp, GameConstants.getMaxHpMp())); // maxmp
    }
    private final static int[] allJobs = {0, 10000, 10000000, 20000000, 20010000, 20020000, 30000000, 30010000};
    public final static int[] pvpSkills = {1000007, 2000007, 3000006, 4000010, 5000006, 5010004, 11000006, 12000006, 13000005, 14000006, 15000005, 21000005, 22000002, 23000004, 31000005, 32000012, 33000004, 35000005};

    public static int getSkillByJob(final int skillID, final int job) {
        if (GameConstants.isKOC(job)) {
            return skillID + 10000000;
        } else if (GameConstants.isAran(job)) {
            return skillID + 20000000;
        } else if (GameConstants.isEvan(job)) {
            return skillID + 20010000;
        } else if (GameConstants.isMercedes(job)) {
            return skillID + 20020000;
        } else if (GameConstants.isDemon(job)) {
            return skillID + 30010000;
        } else if (GameConstants.isResist(job)) {
            return skillID + 30000000;
            //} else if (GameConstants.isCannon(job)) {
            //    return skillID + 10000;
        }
        return skillID;
    }

    public final int getSkillIncrement(final int skillID) {
        if (skillsIncrement.containsKey(skillID)) {
            return skillsIncrement.get(skillID);
        }
        return 0;
    }

    public final int getElementBoost(final Element key) {
        if (elemBoosts.containsKey(key)) {
            return elemBoosts.get(key);
        }
        return 0;
    }

    public final int getDamageIncrease(final int key) {
        if (damageIncrease.containsKey(key)) {
            return damageIncrease.get(key);
        }
        return 0;
    }

    public final int getAccuracy() {
        return accuracy;
    }

    public final double getDmgR() {
        return dam_r * 0.01;
    }

    public final double getBossDmgR() {
        return bossdam_r * 0.01;
    }

    public final long getPDR() {
        return ignoreTargetDEF;
    }

    public final int getCooldown() {
        return (int) reduceCooltime;
    }

    public void heal_noUpdate(MapleCharacter chra) {
        setHp(getCurrentMaxHp(), chra);
        setMp(getCurrentMaxMp(chra.getJob()), chra);
    }

    public void heal(MapleCharacter chra) {
        //System.out.println("Player: " + chra.getName() + " - Max HP: " + getCurrentMaxHp());
        //System.out.println("Player: " + chra.getName() + " - Max MP: " + getCurrentMaxMp(chra.getJob()));
        heal_noUpdate(chra);
        chra.updateSingleStat(MapleStat.HP, getCurrentMaxHp());
        chra.updateSingleStat(MapleStat.MP, getCurrentMaxMp(chra.getJob()));
    }

    public void handleItemOption(StructItemOption soc, MapleCharacter chra, Map<Skill, SkillEntry> hmm, int rb) {
        localstr += soc.get("incSTR");
        localdex += soc.get("incDEX");
        localint += soc.get("incINT");
        localluk += soc.get("incLUK");
        accuracy += soc.get("incACC");
        // incEVA -> increase dodge
        speed += soc.get("incSpeed");
        jump += soc.get("incJump");
        watk += soc.get("incPAD");
        magic += soc.get("incMAD");
        wdef += soc.get("incPDD");
        mdef += soc.get("incMDD");
        percent_hp += soc.get("incMHPr");
        percent_mp += soc.get("incMMPr");
        percent_acc += soc.get("incACCr");
        dodgeChance += soc.get("incEVAr");
        percent_str += soc.get("incSTRr");
        percent_dex += soc.get("incDEXr");
        percent_int += soc.get("incINTr");
        percent_luk += soc.get("incLUKr");
        percent_atk += soc.get("incPADr");
        percent_matk += soc.get("incMADr");
        percent_wdef += soc.get("incPDDr");
        percent_mdef += soc.get("incMDDr");
        passive_sharpeye_rate += soc.get("incCr");
        if (soc.get("boss") > 0) {
            bossdam_r += soc.get("incDAMr");
        } else {
            dam_r += soc.get("incDAMr");
        }
        recoverHP += soc.get("RecoveryHP"); // This shouldn't be here, set 4 seconds.
        recoverMP += soc.get("RecoveryMP"); // This shouldn't be here, set 4 seconds.
        ignoreTargetPERC += soc.get("ignoreTargetDEF");
        if (soc.get("ignoreDAM") > 0) {
            ignoreDAM += soc.get("ignoreDAM");
            ignoreDAM_rate += soc.get("prop");
        }
        incAllskill += soc.get("incAllskill");
        if (soc.get("ignoreDAMr") > 0) {
            ignoreDAMr += soc.get("ignoreDAMr");
            ignoreDAMr_rate += soc.get("prop");
        }
        RecoveryUP += soc.get("RecoveryUP"); // only for hp items and skills
        passive_sharpeye_min_percent += soc.get("incCriticaldamageMin");
        passive_sharpeye_percent += soc.get("incCriticaldamageMax");
        TER += soc.get("incTerR"); // elemental resistance = avoid element damage from monster
        ASR += soc.get("incAsrR"); // abnormal status = disease
        if (soc.get("DAMreflect") > 0) {
            DAMreflect += soc.get("DAMreflect");
            DAMreflect_rate += soc.get("prop");
        }
        mpconReduce += soc.get("mpconReduce");
        reduceCooltime += soc.get("reduceCooltime"); // in seconds
        mesoBuff += soc.get("incMesoProp"); // mesos + %
        dropBuff += soc.get("incRewardProp"); // extra drop rate for item
        //System.out.println("drop: " + dropBuff);
        //System.out.println("exp: " + expBuff);
        if (soc.get("skillID") > 0) {
            hmm.put(SkillFactory.getSkill(getSkillByJob(soc.get("skillID"), chra.getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
        }
        if (soc.get("DR") > 0) {
            damResist += soc.get("level");
        }
        if (soc.get("droppower") > 0) {
            itempower += soc.get("level");
        }
        if (soc.get("overpower") > 0) {
            overpower += soc.get("level");
        }
        if (soc.get("incExpProp") > 0) {
            if (soc.get("incExpProp") == 1) {
                expBuff += Math.pow(soc.get("level"), (2.0 + (rb * 0.025)));
            } else {
                expBuff += soc.get("incExpProp");
            }
        }
        //chra.setBattlePoints(starforce);

        //recalcPVPRank(chra);
        // TODO: Auto Steal potentials (modify handleSteal), potentials with invincible stuffs, abnormal status duration decrease,
        // poison, stun, etc (uses level field -> cast disease to mob/player), face?
    }

    public int getHPPercent() {
        return (int) Math.ceil((hp * 100.0) / localmaxhp);
    }

    public int getStarForce() {
        return starforce;
    }

    public double getItems() {
        return items;
    }

}
