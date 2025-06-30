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

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.Pair;
import server.life.MapleLifeFactory.selfDestruction;

public class MapleMonsterStats {

    public byte cp, selfDestruction_action, tagColor, tagBgColor, rareItemDropLevel, HPDisplayType, summonType, category;
    public short level, charismaEXP;
    public long Def, hp, exp, finalHP;
    public int maxcount, id, mp, removeAfter, buffToGive, fixedDamage, selfDestruction_hp, dropItemPeriod, point, eva, acc, PhysicalAttack, MagicAttack, speed, partyBonusR, pushed, scale, basescale, PDRate, MDRate, power, achievement, DamR = 1, tier = 1, armor = 1, lives, maxlives;
    public boolean raidBoss, boss, undead, ffaLoot, firstAttack, isExplosiveReward, mobile, fly, onlyNormalAttack, friendly, noDoom, invincible, partyBonusMob, changeable, escort, kaotic, bkaotic, trueBoss = false, bar = false;
    public String name, mobType;
    public double resist = 0;
    public EnumMap<Element, ElementalEffectiveness> resistance = new EnumMap<Element, ElementalEffectiveness>(Element.class);
    public List<Integer> revives = new ArrayList<Integer>();
    public List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
    public List<MobAttackInfo> mai = new ArrayList<MobAttackInfo>();
    public BanishInfo banish;
    public MapleLifeFactory.selfDestruction selfDestruction = null;
    public Map<String, Integer> animationTimes = new HashMap<String, Integer>();
    public int extraHP, bonusStats = 0, bonusAmount = 0, skin = 0;
    public BigInteger TotalHP;
    public BigInteger damageCap;
    public long hits = 1;
    public double kdRate = 1.0, dropTier = 1.0;
    public boolean capped = false, superKaotic = false, kdboss = false, mega = false, ultimate = false, drops = false, omega = false;

    public int getId() {
        return id;
    }

    public void setId(int mid) {
        this.id = mid;
    }

    public boolean superKaotic() {
        return superKaotic;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long value) {
        hits = value;
    }

    public void setSuperKaotic(boolean toggle) {
        superKaotic = toggle;
    }

    public void setMega(boolean toggle) {
        mega = toggle;
    }

    public boolean getMega() {
        return mega;
    }
    
    public boolean getUlt() {
        return ultimate;
    }

    public void setCapped(boolean toggle) {
        capped = toggle;
    }

    public boolean getCapped() {
        return capped;
    }

    public void setTotalHP(BigInteger value) {
        TotalHP = value;
    }

    public BigInteger getTotalHp() {
        return TotalHP;
    }

    public void setTier(int value) {
        this.tier = value;
    }

    public int getTier() {
        return tier;
    }

    public void setArmor(int value) {
        this.armor = value;
    }

    public int getArmor() {
        return armor;
    }

    public int getAchievement() {
        return achievement;
    }

    public void setAchievement(int exp) {
        this.achievement = exp;
    }

    public boolean getTrueBoss() {
        return trueBoss;
    }

    public void setTrueBoss(boolean toggle) {
        this.trueBoss = toggle;
    }

    public boolean getBar() {
        return bar;
    }

    public void setBar(boolean toggle) {
        this.bar = toggle;
    }

    public boolean getKaotic() {
        return kaotic;
    }

    public void setKaotic(boolean toggle) {
        this.kaotic = toggle;
    }

    public boolean getDrops() {
        return drops;
    }

    public void setDrops(boolean toggle) {
        this.drops = toggle;
    }

    public int getDamR() {
        return DamR;
    }

    public int getExtraHp() {
        return extraHP;
    }

    public void setExtraHp(int exp) {
        this.extraHP = exp;
    }

    public double getResist() {
        return resist;
    }

    public void setDamR(int value) {
        this.DamR = value;
    }

    public long getFinalHP() {
        return finalHP;
    }

    public void setFinalHP(long value) {
        this.finalHP = value;
    }

    public int getMaxCount() {
        return maxcount;
    }

    public void setMaxCount(int value) {
        this.maxcount = value;
    }

    public boolean getRaidBoss() {
        return raidBoss;
    }

    public void setRaidBoss(boolean value) {
        this.raidBoss = value;
    }

    public int getBaseScale() {
        return basescale;
    }

    public void setBaseScale(int value) {
        this.basescale = value;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getMaxLives() {
        return maxlives;
    }

    public void setMaxLives(int lives) {
        this.maxlives = lives;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getHp() {
        return hp;
    }

    public void setHp(long hp) {
        this.hp = hp;//(hp * 3L / 2L);
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int level) {
        this.scale = level;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int level) {
        this.power = level;
    }

    public short getCharismaEXP() {
        return charismaEXP;
    }

    public void setCharismaEXP(short leve) {
        this.charismaEXP = leve;
    }

    public selfDestruction selfDestruction() {
        return selfDestruction;
    }

    public void setSelfDestruction(selfDestruction sd) {
        this.selfDestruction = sd;
    }

    public void setFixedDamage(int damage) {
        this.fixedDamage = damage;
    }

    public int getFixedDamage() {
        return fixedDamage;
    }

    public void setPushed(int damage) {
        this.pushed = damage;
    }

    public int getPushed() {
        return pushed;
    }

    public void setPhysicalAttack(final int PhysicalAttack) {
        this.PhysicalAttack = PhysicalAttack;
    }

    public int getPhysicalAttack() {
        return PhysicalAttack;
    }

    public final void setMagicAttack(final int MagicAttack) {
        this.MagicAttack = MagicAttack;
    }

    public final int getMagicAttack() {
        return MagicAttack;
    }

    public final void setEva(final int eva) {
        this.eva = eva;
    }

    public final int getEva() {
        return eva;
    }

    public final void setAcc(final int acc) {
        this.acc = acc;
    }

    public final int getAcc() {
        return acc;
    }

    public final void setDef(final long acc) {
        this.Def = acc;
    }

    public final long getDef() {
        return Def;
    }

    public final void setSpeed(final int speed) {
        this.speed = speed;
    }

    public final int getSpeed() {
        return speed;
    }

    public final void setPartyBonusRate(final int speed) {
        this.partyBonusR = speed;
    }

    public final int getPartyBonusRate() {
        return partyBonusR;
    }

    public void setOnlyNormalAttack(boolean onlyNormalAttack) {
        this.onlyNormalAttack = onlyNormalAttack;
    }

    public boolean getOnlyNoramlAttack() {
        return onlyNormalAttack;
    }

    public BanishInfo getBanishInfo() {
        return banish;
    }

    public void setBanishInfo(BanishInfo banish) {
        this.banish = banish;
    }

    public int getRemoveAfter() {
        return removeAfter;
    }

    public void setRemoveAfter(int removeAfter) {
        this.removeAfter = removeAfter;
    }

    public byte getrareItemDropLevel() {
        return rareItemDropLevel;
    }

    public void setrareItemDropLevel(byte rareItemDropLevel) {
        this.rareItemDropLevel = rareItemDropLevel;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setFfaLoot(boolean ffaLoot) {
        this.ffaLoot = ffaLoot;
    }

    public boolean isFfaLoot() {
        return ffaLoot;
    }

    public void setEscort(boolean ffaL) {
        this.escort = ffaL;
    }

    public boolean isEscort() {
        return escort;
    }

    public void setExplosiveReward(boolean isExplosiveReward) {
        this.isExplosiveReward = isExplosiveReward;
    }

    public boolean isExplosiveReward() {
        return isExplosiveReward;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean getMobile() {
        return mobile;
    }

    public void setFly(boolean fly) {
        this.fly = fly;
    }

    public boolean getFly() {
        return fly;
    }

    public List<Integer> getRevives() {
        return revives;
    }

    public void disableRevives() {
        this.revives.clear();
    }

    public void setRevives(List<Integer> revives) {
        this.revives = revives;
    }

    public void setUndead(boolean undead) {
        this.undead = undead;
    }

    public boolean getUndead() {
        return undead;
    }

    public void setSummonType(byte selfDestruction) {
        this.summonType = selfDestruction;
    }

    public byte getSummonType() {
        return summonType;
    }

    public void setAnimationTime(String name, int delay) {
        animationTimes.put(name, delay);
    }

    public int getAnimationTime(String name) {
        Integer ret = animationTimes.get(name);
        if (ret == null) {
            return 500;
        }
        return ret.intValue();
    }

    public void setCategory(byte selfDestruction) {
        this.category = selfDestruction;
    }

    public byte getCategory() {
        return category;
    }

    public void setPDRate(int selfDestruction) {
        this.PDRate = selfDestruction;
    }

    public int getPDRate() {
        return PDRate;
    }

    public void setMDRate(int selfDestruction) {
        this.MDRate = selfDestruction;
    }

    public int getMDRate() {
        return MDRate;
    }

    public EnumMap<Element, ElementalEffectiveness> getElements() {
        return resistance;
    }

    public void setEffectiveness(Element e, ElementalEffectiveness ee) {
        resistance.put(e, ee);
    }

    public void removeEffectiveness(Element e) {
        resistance.remove(e);
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
        ElementalEffectiveness elementalEffectiveness = resistance.get(e);
        if (elementalEffectiveness == null) {
            return ElementalEffectiveness.NORMAL;
        } else {
            return elementalEffectiveness;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return mobType;
    }

    public void setType(String mobt) {
        this.mobType = mobt;
    }

    public byte getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = (byte) tagColor;
    }

    public byte getTagBgColor() {
        return tagBgColor;
    }

    public void setTagBgColor(int tagBgColor) {
        this.tagBgColor = (byte) tagBgColor;
    }

    public void setSkills(List<Pair<Integer, Integer>> skill_) {
        for (Pair<Integer, Integer> skill : skill_) {
            skills.add(skill);
        }
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return Collections.unmodifiableList(this.skills);
    }

    public byte getNoSkills() {
        return (byte) skills.size();
    }

    public boolean hasSkill(int skillId, int level) {
        for (Pair<Integer, Integer> skill : skills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                return true;
            }
        }
        return false;
    }

    public void setFirstAttack(boolean firstAttack) {
        this.firstAttack = firstAttack;
    }

    public boolean isFirstAttack() {
        return firstAttack;
    }

    public void setCP(byte cp) {
        this.cp = cp;
    }

    public byte getCP() {
        return cp;
    }

    public void setPoint(int cp) {
        this.point = cp;
    }

    public int getPoint() {
        return point;
    }

    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }

    public boolean isFriendly() {
        return friendly;
    }

    public void setInvincible(boolean invin) {
        this.invincible = invin;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void setChange(boolean invin) {
        this.changeable = invin;
    }

    public boolean isChangeable() {
        return changeable;
    }

    public void setPartyBonus(boolean invin) {
        this.partyBonusMob = invin;
    }

    public boolean isPartyBonus() {
        return partyBonusMob;
    }

    public void setNoDoom(boolean doom) {
        this.noDoom = doom;
    }

    public boolean isNoDoom() {
        return noDoom;
    }

    public void setBuffToGive(int buff) {
        this.buffToGive = buff;
    }

    public int getBuffToGive() {
        return buffToGive;
    }

    public byte getHPDisplayType() {
        return HPDisplayType;
    }

    public void setHPDisplayType(byte HPDisplayType) {
        this.HPDisplayType = HPDisplayType;
    }

    public int getDropItemPeriod() {
        return dropItemPeriod;
    }

    public void setDropItemPeriod(int d) {
        this.dropItemPeriod = d;
    }

    public void addMobAttack(MobAttackInfo ma) {
        this.mai.add(ma);
    }

    public MobAttackInfo getMobAttack(int attack) {
        if (attack >= this.mai.size() || attack < 0) {
            return null;
        }
        return this.mai.get(attack);
    }

    public List<MobAttackInfo> getMobAttacks() {
        return this.mai;
    }

    public int removeAfter() {
        return removeAfter;
    }

    public MapleMonsterStats copy(int mid) {
        MapleMonsterStats copy = new MapleMonsterStats();
        try {
            FieldCopyUtil.setFields(this, copy);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return copy;
    }

    // FieldCopyUtil src: http://www.codesenior.com/en/tutorial/Java-Copy-Fields-From-One-Object-to-Another-Object-with-Reflection
    public static class FieldCopyUtil { // thanks to Codesenior dev team

        public static void setFields(Object from, Object to) {
            Field[] fields = from.getClass().getDeclaredFields();
            for (Field field : fields) {
                try {
                    Field fieldFrom = from.getClass().getDeclaredField(field.getName());
                    Object value = fieldFrom.get(from);
                    to.getClass().getDeclaredField(field.getName()).set(to, value);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
