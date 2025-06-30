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
package client.inventory;

import client.MapleCharacter;
import constants.GameConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class Equip extends Item implements Serializable {

    public void setOWatk(double statPercent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public long getAtk() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public long getDef() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static enum ScrollResult {

        SUCCESS, FAIL, CURSE
    }
    public static final int ARMOR_RATIO = 350000;
    public static final int WEAPON_RATIO = 700000;
    //charm: -1 = has not been initialized yet, 0 = already been worn, >0 = has teh charm exp
    private byte vicioushammer = 0;
    private int str = 0, dex = 0, _int = 0, luk = 0, hp = 0, mp = 0, watk = 0, matk = 0, wdef = 0, mdef = 0, acc = 0, avoid = 0, hands = 0, speed = 0, jump = 0, charmExp = 0, pvpDamage = 0, hpr = 0, mpr = 0, enhance = 0;
    private int itemEXP = 0, durability = -1, incSkill = -1, potential1 = 0, potential2 = 0, potential3 = 0, potential4 = 0, potential5 = 0, socket1 = -1, socket2 = -1, socket3 = -1, power = 0, androidid = 0;
    private int overpower = 0, totaldamage = 0, bossdamage = 0, ied = 0, critdamage = 0, allstats = 0;
    private short upgradeSlots = 0, level = 0, eslot = 0;
    private MapleRing ring = null;
    private MapleAndroid android = null;
    private boolean epicItem = false;
    public boolean legend = false;
    public long ostr = 0, odex = 0, oint = 0, oluk = 0, oatk = 0, omatk = 0, odef = 0, omdef = 0;

    public Equip(int id, short position, byte flag) {
        super(id, position, (short) 1, flag);
    }

    public Equip(int id, short position, int uniqueid, short flag) {
        super(id, position, (short) 1, flag, uniqueid);
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUniqueId(), getFlag());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.hpr = hpr;
        ret.mpr = mpr;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.enhance = enhance;
        ret.upgradeSlots = upgradeSlots;
        ret.level = level;
        ret.itemEXP = itemEXP;
        ret.durability = durability;
        ret.vicioushammer = vicioushammer;
        ret.potential1 = potential1;
        ret.potential2 = potential2;
        ret.potential3 = potential3;
        ret.potential4 = potential4;
        ret.potential5 = potential5;
        ret.socket1 = socket1;
        ret.socket2 = socket2;
        ret.socket3 = socket3;
        ret.charmExp = charmExp;
        ret.pvpDamage = pvpDamage;
        ret.incSkill = incSkill;
        ret.setGiftFrom(getGiftFrom());
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.power = power;
        ret.androidid = androidid;
        ret.overpower = overpower;
        ret.totaldamage = totaldamage;
        ret.bossdamage = bossdamage;
        ret.ied = ied;
        ret.critdamage = critdamage;
        ret.allstats = allstats;
        ret.epicItem = epicItem;
        ret.eslot = eslot;
        ret.ostr = ostr;
        ret.odex = odex;
        ret.oint = oint;
        ret.oluk = oluk;
        ret.oatk = oatk;
        ret.omatk = omatk;
        ret.odef = odef;
        ret.omdef = omdef;
        return ret;
    }

    @Override
    public Item copy(short amount) {
        Equip ret = new Equip(getItemId(), getPosition(), getUniqueId(), getFlag());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.hpr = hpr;
        ret.mpr = mpr;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.enhance = enhance;
        ret.upgradeSlots = upgradeSlots;
        ret.level = level;
        ret.itemEXP = itemEXP;
        ret.durability = durability;
        ret.vicioushammer = vicioushammer;
        ret.potential1 = potential1;
        ret.potential2 = potential2;
        ret.potential3 = potential3;
        ret.potential4 = potential4;
        ret.potential5 = potential5;
        ret.socket1 = socket1;
        ret.socket2 = socket2;
        ret.socket3 = socket3;
        ret.charmExp = charmExp;
        ret.pvpDamage = pvpDamage;
        ret.incSkill = incSkill;
        ret.setGiftFrom(getGiftFrom());
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.power = power;
        ret.androidid = androidid;
        ret.overpower = overpower;
        ret.totaldamage = totaldamage;
        ret.bossdamage = bossdamage;
        ret.ied = ied;
        ret.critdamage = critdamage;
        ret.allstats = allstats;
        ret.epicItem = epicItem;
        ret.eslot = eslot;
        ret.ostr = ostr;
        ret.odex = odex;
        ret.oint = oint;
        ret.oluk = oluk;
        ret.oatk = oatk;
        ret.omatk = omatk;
        ret.odef = odef;
        ret.omdef = omdef;
        return ret;
    }

    public void copy(Equip base) {
        str = base.getStr();
        dex = base.getDex();
        _int = base.getInt();
        luk = base.getLuk();
        hp = base.getHp();
        mp = base.getMp();
        hpr = base.getHpr();
        mpr = base.getMpr();
        matk = base.getMatk();
        mdef = base.getMdef();
        watk = base.getWatk();
        wdef = base.getWdef();
        speed = base.getSpeed();
        jump = base.getJump();
        enhance = base.getEnhance();
        upgradeSlots = base.getUpgradeSlots();
        level = base.getLevel();
        itemEXP = base.getItemEXP();
        durability = base.getDurability();
        vicioushammer = base.getViciousHammer();
        potential1 = base.getPotential1();
        potential2 = base.getPotential2();
        potential3 = base.getPotential3();
        potential4 = base.getPotential4();
        potential5 = base.getPotential5();
        setOwner(base.getOwner());
        setQuantity(base.getQuantity());
        power = base.getPower();
        overpower = base.getOverPower();
        totaldamage = base.getTotalDamage();
        bossdamage = base.getBossDamage();
        ied = base.getIED();
        critdamage = base.getCritDamage();
        allstats = base.getAllStat();
        epicItem = base.isEpicItem();
        eslot = base.getESlot();
        ostr = base.getOStr();
        odex = base.getODex();
        oint = base.getOInt();
        oluk = base.getOLuk();
        oatk = base.getOAtk();
        omatk = base.getOMatk();
        odef = base.getODef();
        omdef = base.getOMdef();
    }

    @Override
    public byte getType() {
        return 1;
    }

    public long getTStr() {
        return str + ostr;
    }

    public long getTDex() {
        return dex + odex;
    }

    public long getTInt() {
        return _int + oint;
    }

    public long getTLuk() {
        return luk + oluk;
    }

    public long getTAtk() {
        return watk + oatk;
    }

    public long getTMatk() {
        return matk + omatk;
    }

    public long getTDef() {
        return wdef + odef;
    }

    public long getTMdef() {
        return mdef + omdef;
    }

    public short getUpgradeSlots() {
        return upgradeSlots;
    }

    public int getStr() {
        return str;
    }

    public int getPower() {
        return power < 1 ? 1 : power;
    }

    public short getESlot() {
        return eslot;
    }

    public int getDex() {
        return dex;
    }

    public int getInt() {
        return _int;
    }

    public int getLuk() {
        return luk;
    }

    public long getOStr() {
        return ostr;
    }

    public long getODex() {
        return odex;
    }

    public long getOInt() {
        return oint;
    }

    public long getOLuk() {
        return oluk;
    }

    public long getOAtk() {
        return oatk;
    }

    public long getOMatk() {
        return omatk;
    }

    public long getODef() {
        return odef;
    }

    public long getOMdef() {
        return omdef;
    }

    public int getHp() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getEquipStats(getItemId()).get("incMHP") != null) {
            return ii.getEquipStats(getItemId()).get("incMHP");
        }
        return 0;
    }

    public int getHpr() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getEquipStats(getItemId()).get("incMHPr") != null) {
            return ii.getEquipStats(getItemId()).get("incMHPr");
        }
        return 0;
    }

    public int getMp() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getEquipStats(getItemId()).get("incMMP") != null) {
            return ii.getEquipStats(getItemId()).get("incMMP");
        }
        return 0;
    }

    public int getMpr() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getEquipStats(getItemId()).get("incMMPr") != null) {
            return ii.getEquipStats(getItemId()).get("incMMPr");
        }
        return 0;
    }

    public int getWatk() {
        return watk;
    }

    public int getMatk() {
        return matk;
    }

    public int getWdef() {
        return wdef;
    }

    public int getMdef() {
        return mdef;
    }

    public int getAcc() {
        return acc;
    }

    public int getAvoid() {
        return avoid;
    }

    public int getHands() {
        return hands;
    }

    public int getSpeed() {
        return speed;
    }

    public int getJump() {
        return jump;
    }

    public int getAndroididId() {
        return androidid;
    }

    public void setAndroidId(int id) {
        this.androidid = id;
    }

    public int getOverPower() {
        return overpower;
    }

    public int getTotalDamage() {
        return totaldamage;
    }

    public int getBossDamage() {
        return bossdamage;
    }

    public int getIED() {
        return ied;
    }

    public int getCritDamage() {
        return critdamage;
    }

    public int getAllStat() {
        return allstats;
    }

    public boolean isEpicItem() {
        return epicItem;
    }

    public void setEpicItem(boolean toggle) {
        epicItem = toggle;
    }

    public void setStr(int str) {
        if (str < 0) {
            str = 0;
        }
        this.str = str;
    }

    public void setDex(int dex) {
        if (dex < 0) {
            dex = 0;
        }
        this.dex = dex;
    }

    public void setInt(int _int) {
        if (_int < 0) {
            _int = 0;
        }
        this._int = _int;
    }

    public void setLuk(int luk) {
        if (luk < 0) {
            luk = 0;
        }
        this.luk = luk;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public void setHpr(int hp) {
        this.hpr = hp;
    }

    public void setMpr(int mp) {
        this.mpr = mp;
    }

    public void setWatk(int watk) {
        if (watk < 0) {
            watk = 0;
        }
        this.watk = watk;
    }

    public void setMatk(int matk) {
        if (matk < 0) {
            matk = 0;
        }
        this.matk = matk;
    }

    public void setWdef(int wdef) {
        if (wdef < 0) {
            wdef = 0;
        }
        this.wdef = wdef;
    }

    public void setMdef(int mdef) {
        if (mdef < 0) {
            mdef = 0;
        }
        this.mdef = mdef;
    }

    public void setAcc(int acc) {
        if (acc < 0) {
            acc = 0;
        }
        this.acc = acc;
    }

    public void setAvoid(int avoid) {
        if (avoid < 0) {
            avoid = 0;
        }
        this.avoid = avoid;
    }

    public void setHands(int hands) {
        if (hands < 0) {
            hands = 0;
        }
        this.hands = hands;
    }

    public void setSpeed(int speed) {
        if (speed < 0) {
            speed = 0;
        }
        this.speed = speed;
    }

    public void setJump(int jump) {
        if (jump < 0) {
            jump = 0;
        }
        this.jump = jump;
    }

    public void setPower(int power) {
        this.power = power < 1 ? 1 : power;
    }

    public void setOverPower(int value) {
        if (value < 0) {
            value = 0;
        }
        this.overpower = value;
    }

    public void setESlot(short value) {
        this.eslot = value;
    }

    public void setTotalDamage(int value) {
        if (value < 0) {
            value = 0;
        }
        this.totaldamage = value;
    }

    public void setBossDamage(int value) {
        if (value < 0) {
            value = 0;
        }
        this.bossdamage = value;
    }

    public void setIED(int value) {
        if (value < 0) {
            value = 0;
        }
        this.ied = value;
    }

    public void setCritDamage(int value) {
        if (value < 0) {
            value = 0;
        }
        this.critdamage = value;
    }

    public void setAllStat(int value) {
        if (value < 0) {
            value = 0;
        }
        this.allstats = value;
    }

    public void setUpgradeSlots(short upgradeSlots) {
        if (upgradeSlots < 0) {
            upgradeSlots = 0;
        }
        if (upgradeSlots > 30000) {
            upgradeSlots = 30000;
        }
        this.upgradeSlots = upgradeSlots;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public byte getViciousHammer() {
        return vicioushammer;
    }

    public void setViciousHammer(byte ham) {
        vicioushammer = ham;
    }

    public void addViciousHammer(byte ham) {
        vicioushammer += ham;
    }

    public void addViciousHammer() {
        vicioushammer++;
    }

    public int getItemEXP() {
        return itemEXP;
    }

    public void setItemEXP(int itemEXP) {
        if (itemEXP < 0) {
            itemEXP = 0;
        }
        this.itemEXP = itemEXP;
    }

    public void setOStr(long value) {
        ostr = value;
    }

    public void setODex(long value) {
        odex = value;
    }

    public void setOInt(long value) {
        oint = value;
    }

    public void setOLuk(long value) {
        oluk = value;
    }

    public void setOAtk(long value) {
        oatk = value;
    }

    public void setOMatk(long value) {
        omatk = value;
    }

    public void setODef(long value) {
        odef = value;
    }

    public void setOMdef(long value) {
        omdef = value;
    }

    public int getEquipExp() {
        if (itemEXP <= 0) {
            return 0;
        }
        //aproximate value
        if (GameConstants.isWeapon(getItemId())) {
            return itemEXP / WEAPON_RATIO;
        } else {
            return itemEXP / ARMOR_RATIO;
        }
    }

    public int getEquipExpForLevel() {
        if (getEquipExp() <= 0) {
            return 0;
        }
        int expz = getEquipExp();
        for (int i = getBaseLevel(); i <= GameConstants.getMaxLevel(getItemId()); i++) {
            if (expz >= GameConstants.getExpForLevel(i, getItemId())) {
                expz -= GameConstants.getExpForLevel(i, getItemId());
            } else { //for 0, dont continue;
                break;
            }
        }
        return expz;
    }

    public int getExpPercentage() {
        if (getEquipLevel() < getBaseLevel() || getEquipLevel() > GameConstants.getMaxLevel(getItemId()) || GameConstants.getExpForLevel(getEquipLevel(), getItemId()) <= 0) {
            return 0;
        }
        return getEquipExpForLevel() * 100 / GameConstants.getExpForLevel(getEquipLevel(), getItemId());
    }

    public int getEquipLevel() {
        if (GameConstants.getMaxLevel(getItemId()) <= 0) {
            return 0;
        } else if (getEquipExp() <= 0) {
            return getBaseLevel();
        }
        int levelz = getBaseLevel();
        int expz = getEquipExp();
        for (int i = levelz; (GameConstants.getStatFromWeapon(getItemId()) == null ? (i <= GameConstants.getMaxLevel(getItemId())) : (i < GameConstants.getMaxLevel(getItemId()))); i++) {
            if (expz >= GameConstants.getExpForLevel(i, getItemId())) {
                levelz++;
                expz -= GameConstants.getExpForLevel(i, getItemId());
            } else { //for 0, dont continue;
                break;
            }
        }
        return levelz;
    }

    public int getBaseLevel() {
        return (GameConstants.getStatFromWeapon(getItemId()) == null ? 1 : 0);
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(final int dur) {
        this.durability = dur;
    }

    public int getEnhance() {
        return enhance;
    }

    public void setEnhance(final int en) {
        this.enhance = en > 250 ? 250 : en;
    }

    public int getPotential1() {
        return potential1;
    }

    public void setPotential1(final int en) {
        this.potential1 = en;
    }

    public int getPotential2() {
        return potential2;
    }

    public void setPotential2(final int en) {
        this.potential2 = en;
    }

    public int getPotential3() {
        return potential3;
    }

    public void setPotential3(final int en) {
        this.potential3 = en;
    }

    public int getPotential4() {
        return potential4;
    }

    public void setPotential4(final int en) {
        this.potential4 = en;
    }

    public int getPotential5() {
        return potential5;
    }

    public void setPotential5(final int en) {
        this.potential5 = en;
    }

    public int getPotential(int slot) {
        if (slot == 1) {
            return potential1;
        }
        if (slot == 2) {
            return potential2;
        }
        if (slot == 3) {
            return potential3;
        }
        if (slot == 4) {
            return potential4;
        }
        if (slot == 5) {
            return potential5;
        }
        return 0;
    }

    public byte getState() {
        final int pots = potential1 + potential2 + potential3 + potential4 + potential5;
        if (potential1 >= 40000 || potential2 >= 40000 || potential3 >= 40000 || potential4 >= 40000 || potential5 >= 40000) {
            return 20; // legendary
        } else if (potential1 >= 30000 || potential2 >= 30000 || potential3 >= 30000 || potential4 >= 30000 || potential5 >= 30000) {
            return 19; // unique
        } else if (potential1 >= 20000 || potential2 >= 20000 || potential3 >= 20000 || potential4 >= 20000 || potential5 >= 20000) {
            return 18; // epic
        } else if (pots >= 1) {
            return 17; // rare
        } else if (pots < 0) {
            return 1; // hidden
        }
        return 0;
    }

    public void resetPotential_Fuse(boolean half, int potentialState) { //maker skill - equip first receive
        //no legendary, 0.16% chance unique, 4% chance epic, else rare
        potentialState = -potentialState;
        if (Randomizer.nextInt(100) < 4) {
            potentialState -= Randomizer.nextInt(100) < 4 ? 2 : 1;
        }
        setPotential1(Randomizer.nextInt(100));
        setPotential2((Randomizer.nextInt(half ? 5 : 10) == 0 ? potentialState : 0)); //1/10 chance of 3 line
        setPotential3((Randomizer.nextInt(half ? 5 : 10) == 0 ? potentialState : 0)); //just set it theoretically
        setPotential4((Randomizer.nextInt(half ? 5 : 10) == 0 ? potentialState : 0)); //just set it theoretically
        setPotential5((Randomizer.nextInt(half ? 5 : 10) == 0 ? potentialState : 0)); //just set it theoretically
    }

    public void resetPotential(int type) {
        //0 = purple
        //1 = red
        //2 = black
        MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();

        List<Integer> pots = new ArrayList<>();
        int lines, chance1, chance2;
        switch (type) {
            case 2583000:
                chance1 = 32;
                chance2 = 16;
                lines = 3;
                pots = li.pots;
                break;
            case 2583001:
                chance1 = 0;
                chance2 = 32;
                lines = 4;
                pots = li.advpots;
                break;
            case 2583007:
                chance1 = 0;
                chance2 = 0;
                lines = 5;
                pots = li.endpots;
                break;
            case 2583005:
                chance1 = 0;
                chance2 = 0;
                lines = 5;
                pots = li.godpots;
                break;
            default:
                chance1 = 32;
                chance2 = 16;
                lines = 3;
                pots = li.pots;
        }
        setPotential1(pots.get(Randomizer.nextInt(pots.size())));

        if (getPotential2() != 0) {
            setPotential2(pots.get(Randomizer.nextInt(pots.size())));
        } else {
            if (getPotential1() != 0 && Randomizer.nextInt(5) == 0) {
                setPotential2(pots.get(Randomizer.nextInt(pots.size())));
            } else {
                setPotential2(0);
            }
        }

        if (getPotential3() != 0) {
            setPotential3(pots.get(Randomizer.nextInt(pots.size())));
        } else {
            if (getPotential2() != 0 && Randomizer.nextInt(10) == 0) {
                setPotential3(pots.get(Randomizer.nextInt(pots.size())));
            } else {
                setPotential3(0);
            }
        }

        if (getPotential4() != 0) {
            if (lines >= 4 || (getPotential5() != 0 || (Randomizer.nextInt(chance1) > 0 && lines < 4))) {
                setPotential4(pots.get(Randomizer.nextInt(pots.size())));
            } else {
                setPotential4(0);
            }
        } else {
            if (getPotential3() != 0 && Randomizer.nextInt(32) == 0 && lines >= 4) {
                setPotential4(pots.get(Randomizer.nextInt(pots.size())));
            } else {
                setPotential4(0);
            }
        }

        if (getPotential5() != 0) {
            if (lines >= 5 || (Randomizer.nextInt(chance2) > 0 && lines < 5)) {
                setPotential5(pots.get(Randomizer.nextInt(pots.size())));
            } else {
                setPotential5(0);
            }
        } else {
            if (getPotential4() != 0 && Randomizer.nextInt(128) == 0 && lines >= 5) {
                setPotential5(pots.get(Randomizer.nextInt(pots.size())));
            } else {
                setPotential5(0);
            }
        }
    }

    public void resetPotentials() { //equip first one, scroll hidden on it
        List<Integer> pots = MapleItemInformationProvider.getInstance().pots;
        setPotential1(pots.get(Randomizer.nextInt(pots.size())));
        setPotential2(getPotential1() != 0 ? Randomizer.nextInt(5) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
        setPotential3(getPotential2() != 0 ? Randomizer.nextInt(25) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
        setPotential4(getPotential3() != 0 ? Randomizer.nextInt(100) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
        setPotential5(getPotential4() != 0 ? Randomizer.nextInt(250) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
    }

    public void resetAdvPotentials() { //equip first one, scroll hidden on it
        List<Integer> pots = MapleItemInformationProvider.getInstance().advpots;
        setPotential1(pots.get(Randomizer.nextInt(pots.size())));
        setPotential2(getPotential1() != 0 ? Randomizer.nextInt(5) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
        setPotential3(getPotential2() != 0 ? Randomizer.nextInt(25) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
        setPotential4(getPotential3() != 0 ? Randomizer.nextInt(100) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
        setPotential5(getPotential4() != 0 ? Randomizer.nextInt(250) == 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0 : 0);
    }

    public void singlePotential(int id) { //equip first one, scroll hidden on it
        List<Integer> pots = MapleItemInformationProvider.getInstance().allpots;
        if (id == 1) {
            setPotential1(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 2) {
            setPotential2(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 3) {
            setPotential3(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 4) {
            setPotential4(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 5) {
            setPotential5(pots.get(Randomizer.nextInt(pots.size())));
        }
    }

    public void singlePotential(int id, int pot) { //equip first one, scroll hidden on it
        List<Integer> pots;
        switch (pot) {
            case 0:
                pots = MapleItemInformationProvider.getInstance().advpots;
                break;
            case 1:
                pots = MapleItemInformationProvider.getInstance().Mesopots;//meso rate
                break;
            case 2:
                pots = MapleItemInformationProvider.getInstance().XPpots;//exp pots
                break;
            case 3:
                pots = MapleItemInformationProvider.getInstance().Droppots;//drop rate
                break;
            case 4:
                pots = MapleItemInformationProvider.getInstance().OPpots;//overpower
                break;
            case 5:
                pots = MapleItemInformationProvider.getInstance().IEDpots;//IED
                break;
            case 6:
                pots = MapleItemInformationProvider.getInstance().BDpots;//Boss
                break;
            case 7:
                pots = MapleItemInformationProvider.getInstance().TDpots;//total damage
                break;
            case 8:
                pots = MapleItemInformationProvider.getInstance().ASpots;//all stats
                break;
            default:
                pots = MapleItemInformationProvider.getInstance().advpots;
        }
        if (id == 1) {
            setPotential1(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 2) {
            setPotential2(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 3) {
            setPotential3(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 4) {
            setPotential4(pots.get(Randomizer.nextInt(pots.size())));
        }
        if (id == 5) {
            setPotential5(pots.get(Randomizer.nextInt(pots.size())));
        }
    }

    public List<Integer> getPots(int id) {
        MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        switch (id) {
            case 2583000:
                return li.pots;
            case 2583001:
                return li.advpots;
        }
        return null;
    }

    public void renewPotential(int type) { // 0 = normal miracle cube, 1 = premium, 2 = epic pot scroll, 3 = super
        final int rank = type == 2 ? -18 : (Randomizer.nextInt(100) < 4 && getState() != (type == 3 ? 20 : 19) ? -(getState() + 1) : -(getState())); // 4 % chance to up 1 tier
        setPotential1(rank);
        if (getPotential3() > 0) {
            setPotential2(rank); // put back old 3rd line
        } else {
            switch (type) {
                case 1: // premium-> suppose to be 25%
                    setPotential2(Randomizer.nextInt(10) == 0 ? rank : 0); //1/10 chance of 3 line
                    break;
                case 2: // epic pot
                    setPotential2(Randomizer.nextInt(10) <= 1 ? rank : 0); //2/10 chance of 3 line
                    break;
                case 3: // super
                    setPotential2(Randomizer.nextInt(10) <= 2 ? rank : 0); //3/10 chance of 3 line
                    break;
                default:
                    setPotential2(0);
                    break;
            }
        }
        if (getPotential4() > 0) {
            setPotential3(rank); // put back old 4th line
        } else if (type == 3) { // super
            setPotential3(Randomizer.nextInt(100) <= 2 ? rank : 0); // 3/100 to get 4 lines
        } else { // premium cannot get 3 lines.
            setPotential3(0); //just set it theoretically
        }
        if (getPotential5() > 0) {
            setPotential4(rank); // put back old 5th line
        } else if (type == 3) { // super
            setPotential4(Randomizer.nextInt(100) <= 1 ? rank : 0); // 2/100 to get 5 lines
        } else {
            setPotential4(0); //just set it theoretically
        }
        setPotential5(0); //just set it theoretically
    }

    public int getIncSkill() {
        return incSkill;
    }

    public void setIncSkill(int inc) {
        this.incSkill = inc;
    }

    public int getCharmEXP() {
        return charmExp;
    }

    public int getPVPDamage() {
        return pvpDamage;
    }

    public void setCharmEXP(int s) {
        this.charmExp = s;
    }

    public void setPVPDamage(int p) {
        this.pvpDamage = p;
    }

    public MapleRing getRing() {
        if (getUniqueId() <= 0) {
            return null;
        }
        if (ring == null) {
            ring = MapleRing.loadFromDb(getUniqueId(), getPosition() < 0);
        }
        return ring;
    }

    public void setRing(MapleRing ring) {
        this.ring = ring;
    }

    public MapleAndroid getAndroid() {
        if (getItemId() / 10000 != 166 || getUniqueId() <= 0) {
            return null;
        }
        if (android == null) {
            android = MapleAndroid.loadFromDb(getItemId(), getUniqueId());
        }
        return android;
    }

    public void setAndroid(MapleAndroid ring) {
        this.android = ring;
    }

    public int getSocketState() {
        int flag = 0;
        if (socket1 != -1 || socket2 != -1 || socket3 != -1) { // Got empty sockets show msg 
            flag |= SocketFlag.DEFAULT.getValue();
        }
        if (socket1 != -1) {
            flag |= SocketFlag.SOCKET_BOX_1.getValue();
        }
        if (socket1 > 0) {
            flag |= SocketFlag.USED_SOCKET_1.getValue();
        }
        if (socket2 != -1) {
            flag |= SocketFlag.SOCKET_BOX_2.getValue();
        }
        if (socket2 > 0) {
            flag |= SocketFlag.USED_SOCKET_2.getValue();
        }
        if (socket3 != -1) {
            flag |= SocketFlag.SOCKET_BOX_3.getValue();
        }
        if (socket3 > 0) {
            flag |= SocketFlag.USED_SOCKET_3.getValue();
        }
        return (int) flag;
    }

    public int getSocket1() {
        return socket1;
    }

    public void setSocket1(int socket1) {
        this.socket1 = socket1;
    }

    public int getSocket2() {
        return socket2;
    }

    public void setSocket2(int socket2) {
        this.socket2 = socket2;
    }

    public int getSocket3() {
        return socket3;
    }

    public void setSocket3(int socket3) {
        this.socket3 = socket3;
    }

    public int randomBonus(int value) {
        if (value == 0) {
            int random = Randomizer.nextInt(4);
            return random == 0 ? value + 1 : value;
        } else {
            return value;
        }
    }

    private static int getRandUpgradedMaxStat(int defaultValue, int maxRange, int max) {
        int value = Randomizer.MinMax((int) (defaultValue + Math.floor(Randomizer.nextDouble() * (maxRange + 1))), 0, max);
        return defaultValue > 0 ? value > max ? max : value : defaultValue;
    }

    public final Equip randomizeStatsbyMobsLevel(MapleCharacter chr, Equip equip, int power, int scale) {
        MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        li.randomizeStats(chr, equip, scale);
        return equip;
    }

    public boolean isEE() {
        MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        return li.isCash(getItemId());
    }

    public void addStats(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2.5;
        }
        setStr((int) (getStr() == 0 ? randomBonus(getStr()) > 0 ? getStr() + power : 0 : getStr()));
        setDex((int) (getDex() == 0 ? randomBonus(getDex()) > 0 ? getDex() + power : 0 : getDex()));
        setLuk((int) (getLuk() == 0 ? randomBonus(getLuk()) > 0 ? getLuk() + power : 0 : getLuk()));
        setInt((int) (getInt() == 0 ? randomBonus(getInt()) > 0 ? getInt() + power : 0 : getInt()));
        setWatk((int) (getWatk() == 0 ? randomBonus(getWatk()) > 0 ? getWatk() + power : 0 : getWatk()));
        setMatk((int) (getMatk() == 0 ? randomBonus(getMatk()) > 0 ? getMatk() + power : 0 : getMatk()));
        setWdef((int) (getMdef() == 0 ? randomBonus(getWdef()) > 0 ? getWdef() + power : 0 : getWdef()));
        setMdef((int) (getWdef() == 0 ? randomBonus(getMdef()) > 0 ? getMdef() + power : 0 : getMdef()));
    }

    public void addBaseStats(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2.5;
        }
        if (getStr() > 0) {
            setStr((int) (Randomizer.Max(getStr() + power, 2000000000)));
        }
        if (getDex() > 0) {
            setDex((int) (Randomizer.Max(getDex() + power, 2000000000)));
        }
        if (getLuk() > 0) {
            setLuk((int) (Randomizer.Max(getLuk() + power, 2000000000)));
        }
        if (getInt() > 0) {
            setInt((int) (Randomizer.Max(getInt() + power, 2000000000)));
        }
        if (getWatk() > 0) {
            setWatk((int) (Randomizer.Max(getWatk() + power, 2000000000)));
        }
        if (getMatk() > 0) {
            setMatk((int) (Randomizer.Max(getMatk() + power, 2000000000)));
        }
        if (getMdef() > 0) {
            setMdef((int) (Randomizer.Max(getMdef() + power, 2000000000)));
        }
        if (getWdef() > 0) {
            setWdef((int) (Randomizer.Max(getWdef() + power, 2000000000)));
        }
    }

    public void addBaseNXStats(int power) {
        if (getStr() > 0) {
            setStr((int) (Randomizer.Max(getStr() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getDex() > 0) {
            setDex((int) (Randomizer.Max(getDex() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getLuk() > 0) {
            setLuk((int) (Randomizer.Max(getLuk() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getInt() > 0) {
            setInt((int) (Randomizer.Max(getInt() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getWatk() > 0) {
            setWatk((int) (Randomizer.Max(getWatk() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getMatk() > 0) {
            setMatk((int) (Randomizer.Max(getMatk() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getMdef() > 0) {
            setMdef((int) (Randomizer.Max(getMdef() + Randomizer.nextInt(power), 2000000000)));
        }
        if (getWdef() > 0) {
            setWdef((int) (Randomizer.Max(getWdef() + Randomizer.nextInt(power), 2000000000)));
        }
    }

    public void gainStats(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2.5;
        }
        setStr((int) (randomBonus(getStr()) > 0 ? getStr() + power : 0));
        setDex((int) (randomBonus(getDex()) > 0 ? getDex() + power : 0));
        setLuk((int) (randomBonus(getLuk()) > 0 ? getLuk() + power : 0));
        setInt((int) (randomBonus(getInt()) > 0 ? getInt() + power : 0));
        setWatk((int) (randomBonus(getWatk()) > 0 ? getWatk() + power : 0));
        setMatk((int) (randomBonus(getMatk()) > 0 ? getMatk() + power : 0));
        setWdef((int) (randomBonus(getWdef()) > 0 ? getWdef() + power : 0));
        setMdef((int) (randomBonus(getMdef()) > 0 ? getMdef() + power : 0));
    }

    public void gainRandomStats(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2.5;
        }
        setStr((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setDex((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setLuk((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setInt((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setWatk((int) (randomBonus(0) > 0 ? Randomizer.random((int) (power * 0.25), (int) (power * 0.5)) : 0));
        setMatk((int) (randomBonus(0) > 0 ? Randomizer.random((int) (power * 0.25), (int) (power * 0.5)) : 0));
        setWdef((int) (randomBonus(0) > 0 ? Randomizer.random(power * 2, (int) (power * 3)) : 0));
        setMdef((int) (randomBonus(0) > 0 ? Randomizer.random(power * 2, (int) (power * 3)) : 0));
    }

    public void resetStats(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2.5;
        }
        setStr((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setDex((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setLuk((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setInt((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setWatk((int) (randomBonus(0) > 0 ? Randomizer.random((int) (power * 0.25), (int) (power * 0.5)) : 0));
        setMatk((int) (randomBonus(0) > 0 ? Randomizer.random((int) (power * 0.25), (int) (power * 0.5)) : 0));
        setWdef((int) (randomBonus(0) > 0 ? Randomizer.random(power * 2, (int) (power * 3)) : 0));
        setMdef((int) (randomBonus(0) > 0 ? Randomizer.random(power * 2, (int) (power * 3)) : 0));
        setUpgradeSlots((short) 0);
        setLevel((byte) 0);
        setEnhance(0);
    }

    public void resetStatsFixed(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2.5;
        }
        setStr((int) (Randomizer.random(power, (int) (power * 1.5))));
        setDex((int) (Randomizer.random(power, (int) (power * 1.5))));
        setLuk((int) (Randomizer.random(power, (int) (power * 1.5))));
        setInt((int) (Randomizer.random(power, (int) (power * 1.5))));
        setWatk((int) (Randomizer.random((int) (power * 0.25), (int) (power * 0.5))));
        setMatk((int) (Randomizer.random((int) (power * 0.25), (int) (power * 0.5))));
        setWdef((int) (Randomizer.random(power * 2, (int) (power * 3))));
        setMdef((int) (Randomizer.random(power * 2, (int) (power * 3))));
        setUpgradeSlots((short) 0);
        setLevel((byte) 0);
        setEnhance(0);
    }

    public void resetStatsCash(double lvl, double scale) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        double ilevel = lvl * (scale * 0.25);
        if (GameConstants.isDoubleSlot(getItemId())) {
            ilevel *= 2.5;
        }

        if (GameConstants.isWeapon(getItemId())) {
            setWatk((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 5)), ((int) (ilevel * 10)), 2000000000) : 0));
            setMatk((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 5)), ((int) (ilevel * 10)), 2000000000) : 0));
        } else {
            setWatk((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 2)), ((int) (ilevel * 4)), 2000000000) : 0));
            setMatk((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 2)), ((int) (ilevel * 4)), 2000000000) : 0));
        }
        setStr((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 10)), ((int) (ilevel * 25)), 2000000000) : 0));
        setDex((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 10)), ((int) (ilevel * 25)), 2000000000) : 0));
        setLuk((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 10)), ((int) (ilevel * 25)), 2000000000) : 0));
        setInt((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 10)), ((int) (ilevel * 25)), 2000000000) : 0));
        setWdef((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 50)), ((int) (ilevel * 100)), 2000000000) : 0));
        setMdef((int) (ii.randomBonus(0, (int) scale) > 0 ? Randomizer.randomMinMaxCap(((int) (ilevel * 50)), ((int) (ilevel * 100)), 2000000000) : 0));
        setSpeed((int) (Randomizer.randomMinMaxCap(getSpeed() + (int) (scale * 2), getSpeed() + (int) (scale * 4), 100)));
        List<Integer> LootPots = new ArrayList<>();
        if (scale >= 0 && scale < 5) {
            LootPots = ii.pots;
        }
        if (scale >= 5 && scale <= 15) {
            LootPots = ii.advpots;
        }
        if (scale >= 15 && scale <= 20) {
            LootPots = ii.allpots;
        }
        if (scale > 20) {
            LootPots = ii.godpots;
        }
        setPotential1(LootPots.get(Randomizer.nextInt(LootPots.size())));
        int chance2 = Randomizer.random(0, (int) (4 / scale));
        setPotential2(getPotential1() != 0 ? chance2 == 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0 : 0);
        int chance3 = Randomizer.random(0, (int) (16 / scale));
        setPotential3(getPotential2() != 0 ? chance3 == 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0 : 0);
        int chance4 = Randomizer.random(0, (int) (64 / scale));
        setPotential4(getPotential3() != 0 ? chance4 == 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0 : 0);
        int chance5 = Randomizer.random(0, (int) (256 / scale));
        setPotential5(getPotential4() != 0 ? chance5 == 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0 : 0);
        setEnhance((int) 0);
        setLevel((short) 0);
        setItemEXP((short) 0);
    }

    public void resetNX() {
        MapleItemInformationProvider.getInstance().randomizeStats(this);
    }

    public void resetNX(MapleCharacter player) {
        MapleItemInformationProvider.getInstance().randomizeStats(player, this);
    }

    public void resetNXFlame(MapleCharacter player, int level, int tier) {
        MapleItemInformationProvider.getInstance().randomizeStats(player, this, tier, tier);
    }

    public void resetFull(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2;
        }
        setStr((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setDex((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setLuk((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setInt((int) (randomBonus(0) > 0 ? Randomizer.random(power, (int) (power * 1.5)) : 0));
        setWatk((int) (randomBonus(0) > 0 ? Randomizer.random((int) (power * 0.25), (int) (power * 0.5)) : 0));
        setMatk((int) (randomBonus(0) > 0 ? Randomizer.random((int) (power * 0.25), (int) (power * 0.5)) : 0));
        setWdef((int) (randomBonus(0) > 0 ? Randomizer.random(power * 2, (int) (power * 3)) : 0));
        setMdef((int) (randomBonus(0) > 0 ? Randomizer.random(power * 2, (int) (power * 3)) : 0));
        setUpgradeSlots((short) 0);
        setLevel((byte) 0);
        setEnhance(0);
        resetAdvPotentials();
    }

    public void resetFullFixed(int power) {
        if (GameConstants.isDoubleSlot(getItemId())) {
            power *= 2;
        }
        setStr((int) (Randomizer.random(power, (int) (power * 1.5))));
        setDex((int) (Randomizer.random(power, (int) (power * 1.5))));
        setLuk((int) (Randomizer.random(power, (int) (power * 1.5))));
        setInt((int) (Randomizer.random(power, (int) (power * 1.5))));
        setWatk((int) (Randomizer.random((int) (power * 0.25), (int) (power * 0.5))));
        setMatk((int) (Randomizer.random((int) (power * 0.25), (int) (power * 0.5))));
        setWdef((int) (Randomizer.random(power * 2, (int) (power * 3))));
        setMdef((int) (Randomizer.random(power * 2, (int) (power * 3))));
        setUpgradeSlots((short) 0);
        setLevel((byte) 0);
        setEnhance(0);
        resetAdvPotentials();
    }

    protected final int getRandStat(final int defaultValue, final int maxRange, int max) {
        int base = randomBonus(defaultValue);
        if (base == 0) {
            return 0;
        }
        // vary no more than ceil of 10% of stat
        final int lMaxRange = (int) Math.min(Math.ceil(base * 0.1), maxRange);

        return Randomizer.Max((int) ((base - lMaxRange) + Randomizer.nextInt(lMaxRange * 2 + 1)), max);
    }

    public final Equip upgradeAndroid(MapleCharacter chr, final Equip equip) {
        setUpgradeSlots((short) (getUpgradeSlots() + 1));
        setOwner("Lvl:" + chr.getAndroid().getLevel());
        return equip;
    }

    public Equip randomBonusStats(MapleCharacter chr, final Equip equip, int tier, int cap) {
        if (getUpgradeSlots() > 0) {
            if (tier < 1) {
                tier = 1;
            }
            if (GameConstants.isDoubleSlot(equip.getItemId())) {
                tier *= 2;
            }
            if (getOverPower() > 0) {
                setOverPower(Randomizer.Max(getOverPower() + Randomizer.random(1, tier), cap));//range 5-15 per tier
            }
            if (getTotalDamage() > 0) {
                setTotalDamage(Randomizer.Max(getTotalDamage() + Randomizer.random(1, tier), cap));//range 1-10 per tier
            }
            if (getBossDamage() > 0) {
                setBossDamage(Randomizer.Max(getBossDamage() + Randomizer.random(1, tier), cap));//range 10-30 per tier
            }
            if (getIED() > 0) {
                setIED(Randomizer.Max(getIED() + Randomizer.random(1, tier), cap));//range 10-30 per tier
            }
            if (getCritDamage() > 0) {
                setCritDamage(Randomizer.Max(getCritDamage() + Randomizer.random(1, tier), cap));//range 5-15 per tier
            }
            if (getAllStat() > 0) {
                setAllStat(Randomizer.Max(getAllStat() + Randomizer.random(1, tier), cap));//range 1-5 per tier
            }
            setUpgradeSlots((short) (getUpgradeSlots() - 1));
            //setLevel((short) (getLevel() + 1));
        }
        return equip;
    }

    public boolean canSoul() {
        int count = 0;
        if (getOverPower() > 0) {
            count++;
        }
        if (getTotalDamage() > 0) {
            count++;
        }
        if (getBossDamage() > 0) {
            count++;
        }
        if (getIED() > 0) {
            count++;
        }
        if (getCritDamage() > 0) {
            count++;
        }
        if (getAllStat() > 0) {
            count++;
        }
        return count > 0;
    }

    public void upgradeTier() {
        setUpgradeSlots((short) (getUpgradeSlots() + 5));
        setPower(getPower() + 1);
    }

    public void upgradeTier(int slot) {
        setUpgradeSlots((short) (getUpgradeSlots() + slot));
        setPower(getPower() + 1);
    }

    public void upgradeTiers(int value) {
        setUpgradeSlots((short) (getUpgradeSlots() + (value * 5)));
        setPower(getPower() + value);
    }
}
