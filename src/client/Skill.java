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

import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.Element;
import tools.FileoutputUtil;
import tools.Pair;

public class Skill {

    private String name = "";
    private final List<MapleStatEffect> effects = new ArrayList<MapleStatEffect>();
    private List<MapleStatEffect> pvpEffects = null;
    private List<Integer> animation = null;
    private final List<Pair<Integer, Byte>> requiredSkill = new ArrayList<Pair<Integer, Byte>>();
    private Element element = Element.NEUTRAL;
    private double baseDamage = 0;
    private int id, animationTime = 0, masterLevel = 0, maxLevel = 0, delay = 0, trueMax = 0, eventTamingMob = 0, skillType = 0, attack_delay = 100, speed = 10, line_delay = 60, scale = 1; //4 is alert
    private int monsters, attacks, lines;
    private boolean invisible = false, chargeskill = false, timeLimited = false, combatOrders = false, pvpDisabled = false, magic = false, casterMove = false, pushTarget = false, pullTarget = false, newSkill = false, toggleBuff = false;

    public Skill(final int id) {
        super();
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getScale() {
        return scale;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isBuff() {
        return toggleBuff;
    }

    public static final Skill loadFromData(final int id, final MapleData data, final MapleData delayData) {
        Skill ret = new Skill(id);

        boolean isBuff = false;
        final int skillType = MapleDataTool.getInt("skillType", data, -1);
        final String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.element = Element.getFromChar(elem.charAt(0));
        }
        ret.skillType = skillType;
        ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
        ret.timeLimited = MapleDataTool.getInt("timeLimited", data, 0) > 0;
        ret.combatOrders = MapleDataTool.getInt("combatOrders", data, 0) > 0;
        //ret.combatOrders = true;
        ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
        ret.eventTamingMob = MapleDataTool.getInt("eventTamingMob", data, 0);
        final MapleData inf = data.getChildByPath("info");
        if (inf != null) {
            ret.pvpDisabled = MapleDataTool.getInt("pvp", inf, 1) <= 0;
            ret.magic = MapleDataTool.getInt("magicDamage", inf, 0) > 0;
            ret.casterMove = MapleDataTool.getInt("casterMove", inf, 0) > 0;
            ret.pushTarget = MapleDataTool.getInt("pushTarget", inf, 0) > 0;
            ret.pullTarget = MapleDataTool.getInt("pullTarget", inf, 0) > 0;
        }
        final MapleData effect = data.getChildByPath("effect");
        if (skillType == 2) {
            isBuff = true;
        } else if (skillType == 3) { //final attack
            ret.animation = new ArrayList<Integer>();
            ret.animation.add(0);
            isBuff = effect != null;
        } else {
            MapleData action_ = data.getChildByPath("action");
            final MapleData hit = data.getChildByPath("hit");
            final MapleData ball = data.getChildByPath("ball");

            boolean action = false;
            if (action_ == null) {
                if (data.getChildByPath("prepare/action") != null) {
                    action_ = data.getChildByPath("prepare/action");
                    action = true;
                }
            }
            isBuff = effect != null && hit == null && ball == null;
            if (action_ != null) {
                String d = null;
                if (action) { //prepare
                    d = MapleDataTool.getString(action_, null);
                } else {
                    d = MapleDataTool.getString("0", action_, null);
                }
                if (d != null) {
                    isBuff |= d.equals("alert2");
                    final MapleData dd = delayData.getChildByPath(d);
                    if (dd != null) {
                        for (MapleData del : dd) {
                            ret.delay += Math.abs(MapleDataTool.getInt("delay", del, 0));
                        }
                        if (ret.delay > 30) { //then, faster(2) = (10+2)/16 which is basically 3/4
                            ret.delay = (int) Math.round(ret.delay * 11.0 / 16.0); //fastest(1) lolol
                            ret.delay -= (ret.delay % 30); //round to 30ms
                        }
                    }
                    if (SkillFactory.getDelay(d) != null) { //this should return true always
                        ret.animation = new ArrayList<Integer>();
                        ret.animation.add(SkillFactory.getDelay(d));
                        if (!action) {
                            for (MapleData ddc : action_) {
                                if (!MapleDataTool.getString(ddc, d).equals(d)) {
                                    String c = MapleDataTool.getString(ddc);
                                    if (SkillFactory.getDelay(c) != null) {
                                        ret.animation.add(SkillFactory.getDelay(c));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            switch (id) {
                case 2301002: // heal is alert2 but not overtime...
                case 2111003: // poison mist
                case 12111005: // Flame Gear
                case 22161003:
                case 32121006:
                case 11076:
                case 2111002: // explosion
                case 4211001: // chakra
                case 2121001: // Big bang
                case 2221001: // Big bang
                case 2321001: // Big bang
                case 1076:
                case 2000:
                case 5211006: //homing beacon
                    isBuff = false;
                    break;
                case 1004: // monster riding
                case 10001004:
                case 20001004:
                case 20011004:
                case 80001000:
                case 1026:
                case 10001026:
                case 20001026:
                case 20011026:
                case 20021026:
                case 30001026:
                case 30011026:
                case 93:
                case 10000093:
                case 20000093:
                case 20010093:
                case 20020093:
                case 30000093:
                case 30010093:
                case 9101004: // hide is a buff -.- atleast for us o.o"
                case 1111002: // combo
                case 4211003: // pickpocket
                case 4111001: // mesoup
                case 15111002: // Super Transformation
                case 5111005: // Transformation
                case 5121003: // Super Transformation
                case 13111005: // Alabtross
                case 21000000: // Aran Combo
                case 21101003: // Body Pressure
                case 5211001: // Pirate octopus summon
                case 5211002:
                case 5220002: // wrath of the octopi
                case 5001005: //dash
                case 15001003:

                case 5220011: //bullseye
                case 5110001: //energy charge
                case 15100004:
                case 5121009: //speed infusion
                case 15111005:

                case 22121001: //element reset
                case 22131001: //magic shield
                case 22141002: //magic booster
                case 2311006: //magic booster
                case 22151002: //killer wing
                case 22151003: //magic resist
                case 22161002: //imprint
                case 22171000: //maple warrior
                case 22171004: //hero will
                case 22181000: //onyx blessing
                case 22181004:
                case 22161004:
                case 22181003: //soul stone
                //case 22121000:
                //case 22141003:
                //case 22151001:
                //case 22161002:
                case 4331003: //owl spirit
                case 15101006: //spark
                case 15111006: //spark
                case 4321000: //tornado spin
                case 1320009: //beholder's buff.. passive
                case 35120000:
                case 35001002: //TEMP. mech
                case 9001004: // hide
                case 4341002:

                case 32001003: //dark aura
                case 32120000:
                case 32111012: //blue aura
                case 32110000:
                case 32101003: //yellow aura
                case 32120001:
                case 35101007: //perfect armor
                case 35121006: //satellite safety
                case 35001001: //flame
                case 35101009:
                case 35121005: //missile
                case 35121013:
                case 35111004: //siege
                case 33111003: //puppet ?
                case 1211009:
                case 1111007:
                case 1311007: //magic,armor,atk crash
                case 32121003: //twister
                case 5111007:
                case 5211007:
                case 15111011:
                case 5311005:
                case 5320007:
                case 35111013: //dice
                case 32111006:
                case 5120011:
                case 5220012:
                case 1220013:
                case 33101006: //jaguar oshi
                case 32110007:
                case 32110008:
                case 32110009:
                case 32111005:
                case 31121005:
                case 35121003:
                case 35121009:
                case 35121010:
                case 35111005:
                case 35111001:
                case 35111010:
                case 35111009:
                case 35111011:
                case 35111002:
                case 35101005:
                case 3120006:
                case 3220005:
                case 2121009:
                case 2120010:
                case 2221009:
                case 2220010:
                case 2321010:
                case 2320011:
                case 5321003:
                case 5321004:
                case 2111008:
                case 2211008:
                case 22131002:
                case 80001089:
                case 24101005: // booster
                case 24121009: // hero's will
                case 24121008: // maple warrior
                case 11111001: // combo
                case 11101003://kains rage
                case 9001050:
                case 23121004:
                    ret.toggleBuff = true;
                    isBuff = true;
                    break;
            }
        }
        ret.chargeskill = data.getChildByPath("keydown") != null;
        //some skills have old system, some new
        final MapleData level = data.getChildByPath("common");
        if (level != null) {
            ret.newSkill = true;
            ret.maxLevel = MapleDataTool.getInt("maxLevel", level, 1); //10 just a failsafe, shouldn't actually happens
            ret.trueMax = ret.maxLevel + (ret.combatOrders ? 2 : 0);
            for (int i = 1; i <= ret.trueMax; i++) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff, i, "x"));
            }

        } else {
            for (final MapleData leve : data.getChildByPath("level")) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(leve, id, isBuff, Byte.parseByte(leve.getName()), null));
            }
            ret.maxLevel = ret.effects.size();
            ret.trueMax = ret.effects.size();
        }
        /*
         final MapleData level2 = data.getChildByPath("PVPcommon");
         if (level2 != null) {
         ret.pvpEffects = new ArrayList<MapleStatEffect>();
         for (int i = 1; i <= ret.trueMax; i++) {
         ret.pvpEffects.add(MapleStatEffect.loadSkillEffectFromData(level2, id, isBuff, i, "x"));
         }
         }
         */
        final MapleData reqDataRoot = data.getChildByPath("req");
        if (reqDataRoot != null) {
            for (final MapleData reqData : reqDataRoot.getChildren()) {
                ret.requiredSkill.add(new Pair<Integer, Byte>(Integer.parseInt(reqData.getName()), (byte) MapleDataTool.getInt(reqData, 1)));
            }
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (final MapleData effectEntry : effect) {
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }
        }

        try ( Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `skill_template` WHERE `skillid` = ?");
            ps.setInt(1, id);
            try ( ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    ret.attack_delay = rs.getInt("delay");
                    ret.speed = rs.getInt("speed");
                    ret.line_delay = rs.getInt("atkdelay");
                    ret.baseDamage = rs.getDouble("damage");
                    ret.scale = rs.getInt("scale");
                    ret.monsters = rs.getInt("monsters");
                    ret.attacks = rs.getInt("attacks");
                    ret.lines = rs.getInt("lines");
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int getMonsters() {
        return monsters;
    }

    public int getAttacks() {
        return attacks;
    }

    public int getLines() {
        return lines;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public MapleStatEffect getEffect(final int level) {
        if (effects.size() < level) {
            if (effects.size() > 0) { //incAllskill
                return effects.get(effects.size() - 1);
            }
            return null;
        } else if (level <= 0) {
            return effects.get(0);
        }
        return effects.get(level - 1);
    }

    public MapleStatEffect getPVPEffect(final int level) {
        if (pvpEffects == null) {
            return getEffect(level);
        }
        if (pvpEffects.size() < level) {
            if (pvpEffects.size() > 0) { //incAllskill
                return pvpEffects.get(pvpEffects.size() - 1);
            }
            return null;
        } else if (level <= 0) {
            return pvpEffects.get(0);
        }
        return pvpEffects.get(level - 1);
    }

    public int getSkillType() {
        return skillType;
    }

    public int getSpeed() {
        return speed;
    }

    public int getAttackDelay() {
        return attack_delay;
    }

    public int getLineDelay() {
        return line_delay;
    }

    public List<Integer> getAllAnimation() {
        return animation;
    }

    public int getAnimation() {
        if (animation == null) {
            return -1;
        }
        return animation.get(Randomizer.nextInt(animation.size()));
    }

    public boolean isPVPDisabled() {
        return pvpDisabled;
    }

    public boolean isChargeSkill() {
        return chargeskill;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public boolean hasRequiredSkill() {
        return requiredSkill.size() > 0;
    }

    public List<Pair<Integer, Byte>> getRequiredSkills() {
        return requiredSkill;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getTrueMax() {
        return trueMax;
    }

    public boolean combatOrders() {
        return combatOrders;
    }

    public boolean canBeLearnedBy(int job) {
        int jid = job;
        int skillForJob = id / 10000;
        if (skillForJob == 2001) {
            return GameConstants.isEvan(job); //special exception for beginner -.-
        } else if (skillForJob == 0) {
            return GameConstants.isAdventurer(job); //special exception for beginner
        } else if (skillForJob == 1000) {
            return GameConstants.isKOC(job); //special exception for beginner
        } else if (skillForJob == 2000) {
            return GameConstants.isAran(job); //special exception for beginner
        } else if (skillForJob == 3000) {
            return GameConstants.isResist(job); //special exception for beginner
        } else if (skillForJob == 1) {
            return GameConstants.isCannon(job); //special exception for beginner
        } else if (skillForJob == 3001) {
            return GameConstants.isDemon(job); //special exception for beginner
        } else if (skillForJob == 2002) {
            return GameConstants.isMercedes(job); //special exception for beginner
        } else if (jid / 100 != skillForJob / 100) { // wrong job
            return false;
        } else if (jid / 1000 != skillForJob / 1000) { // wrong job
            return false;
        } else if (GameConstants.isCannon(skillForJob) && !GameConstants.isCannon(job)) {
            return false;
        } else if (GameConstants.isDemon(skillForJob) && !GameConstants.isDemon(job)) {
            return false;
        } else if (GameConstants.isAdventurer(skillForJob) && !GameConstants.isAdventurer(job)) {
            return false;
        } else if (GameConstants.isKOC(skillForJob) && !GameConstants.isKOC(job)) {
            return false;
        } else if (GameConstants.isAran(skillForJob) && !GameConstants.isAran(job)) {
            return false;
        } else if (GameConstants.isEvan(skillForJob) && !GameConstants.isEvan(job)) {
            return false;
        } else if (GameConstants.isMercedes(skillForJob) && !GameConstants.isMercedes(job)) {
            return false;
        } else if (GameConstants.isResist(skillForJob) && !GameConstants.isResist(job)) {
            return false;
        } else if ((jid / 10) % 10 == 0 && (skillForJob / 10) % 10 > (jid / 10) % 10) { // wrong 2nd job
            return false;
        } else if ((skillForJob / 10) % 10 != 0 && (skillForJob / 10) % 10 != (jid / 10) % 10) { //wrong 2nd job
            return false;
        } else if (skillForJob % 10 > jid % 10) { // wrong 3rd/4th job
            return false;
        }
        return true;
    }

    public boolean isTimeLimited() {
        return timeLimited;
    }

    public boolean isFourthJob() {
        switch (id) { // I guess imma make an sql table to store these, so that we could max them all out.
            case 4321000:
            case 4311003:
            case 4331005:
            case 3220010:
            case 3120011:
            case 33120010:
            case 32120009:
            case 5321006:
            case 21120011:
            case 22181004:
            case 4340010:
            case 22111001:
            case 22140000:
            case 22141002:
                return true;
        }
        //resurrection has master level while ult.strafe does not.. wtf, impossible to tell from WZ
        if ((id / 10000) == 2312) { //all 10 skills.
            return true;
        }
        if ((getMaxLevel() <= 15 && !invisible && getMasterLevel() <= 0)) {
            return false;
        }
        if (id / 10000 >= 2212 && id / 10000 < 3000) { //evan skill
            return ((id / 10000) % 10) >= 7;
        }
        if (id / 10000 >= 430 && id / 10000 <= 434) { //db skill
            return ((id / 10000) % 10) == 4 || getMasterLevel() > 0;
        }
        return ((id / 10000) % 10) == 2 && id < 90000000 && !isBeginnerSkill();
    }

    public Element getElement() {
        return element;
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public int getMasterLevel() {
        return masterLevel;
    }

    public int getDelay() {
        return delay;
    }

    public int getTamingMob() {
        return eventTamingMob;
    }

    public boolean isBeginnerSkill() {
        int jobId = id / 10000;
        return GameConstants.isBeginnerJob(jobId);
    }

    public boolean isMagic() {
        return magic;
    }

    public boolean isMovement() {
        return casterMove;
    }

    public boolean isPush() {
        return pushTarget;
    }

    public boolean isPull() {
        return pullTarget;
    }

    public boolean isSpecialSkill() {
        int jobId = id / 10000;
        return jobId == 900 || jobId == 800 || jobId == 9000 || jobId == 9200 || jobId == 9201 || jobId == 9202 || jobId == 9203 || jobId == 9204;
    }

}
