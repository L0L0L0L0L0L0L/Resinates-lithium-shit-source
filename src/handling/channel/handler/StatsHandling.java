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
package handling.channel.handler;

import constants.GameConstants;

import client.Skill;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleStat;
import client.PlayerStats;
import client.SkillFactory;
import java.util.EnumMap;
import java.util.Map;
import scripting.NPCScriptManager;
import server.Randomizer;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CWvsContext;

public class StatsHandling {

    public static final void DistributeAP(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        c.announce(CWvsContext.updatePlayerStats(statupdate, true, chr));
        chr.updateTick(slea.readInt());
        if (!chr.isAlive() || chr.isStatLock()) {
            return;
        }
        final PlayerStats stat = chr.getStat();
        final int job = chr.getJob();
        boolean heal = false;
        int amount = Randomizer.Max(chr.getRemainingAp(), 5);
        if (chr.getRemainingAp() >= amount) {
            switch (slea.readInt()) {
                case 64: // Str
                    stat.setStr(stat.getStr() + amount, chr);
                    statupdate.put(MapleStat.STR, stat.getStr());
                    break;
                case 128: // Dex
                    stat.setDex(stat.getDex() + amount, chr);
                    statupdate.put(MapleStat.DEX, stat.getDex());
                    break;
                case 256: // Int
                    stat.setInt(stat.getInt() + amount, chr);
                    statupdate.put(MapleStat.INT, stat.getInt());
                    break;
                case 512: // Luk
                    stat.setLuk(stat.getLuk() + amount, chr);
                    statupdate.put(MapleStat.LUK, stat.getLuk());
                    break;
                case 2048: // HP
                    long totalHp = stat.getMaxHp() + (amount * 25);
                    stat.setMaxHp((int) Randomizer.LongMax(totalHp, 9999999), chr);
                    int maxhp = Randomizer.Max(stat.getMaxHp(), GameConstants.getMaxHpMp());
                    statupdate.put(MapleStat.MAXHP, maxhp);
                    statupdate.put(MapleStat.HP, maxhp);
                    heal = true;
                    break;
                case 8192: // MP
                    if (!GameConstants.isDemon(job)) {
                        long totalMp = stat.getMaxMp() + (amount * 25);
                        stat.setMaxMp((int) Randomizer.LongMax(totalMp, 9999999), chr);
                        int maxmp = Randomizer.Max(stat.getMaxMp(), GameConstants.getMaxHpMp());
                        statupdate.put(MapleStat.MAXMP, maxmp);
                        statupdate.put(MapleStat.MP, maxmp);
                        heal = true;
                        break;
                    } else {
                        chr.dropMessage("Mp cannot be applied.");
                    }
                default:
                    c.announce(CWvsContext.enableActions());
                    return;
            }
            chr.setRemainingAp(chr.getRemainingAp() - amount);
            statupdate.put(MapleStat.AVAILABLEAP, chr.getRemainingAp());
            c.announce(CWvsContext.updatePlayerStats(statupdate, true, chr));
            chr.setStatLock(System.currentTimeMillis() + 100);
            if (heal) {
                chr.Heal();
            }
        } else {
            chr.dropMessage("Need at least 5 Ap to apply any stat increases");
        }
    }
    
    public static final void DistributeSP(final int skillid, final MapleClient c, final MapleCharacter chr) {
        if (!chr.isAlive() || chr.isStatLock()) {
            return;
        }
        boolean isBeginnerSkill = false;
        final int remainingSp;
        if (skillid / 10000 == 8000) {
            chr.dropMessage(1, "This skill cannot be leveled up here.");
            //AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill without the required skill (" + skillid + ")");
            return;
        }
        if (GameConstants.isBeginnerJob(skillid / 10000) && (skillid % 10000 == 1000 || skillid % 10000 == 1001 || skillid % 10000 == 1002 || skillid % 10000 == 2)) {
            final boolean resistance = skillid / 10000 == 3000 || skillid / 10000 == 3001;
            final int snailsLevel = chr.getSkillLevel(SkillFactory.getSkill(((skillid / 10000) * 10000) + 1000));
            final int recoveryLevel = chr.getSkillLevel(SkillFactory.getSkill(((skillid / 10000) * 10000) + 1001));
            final int nimbleFeetLevel = chr.getSkillLevel(SkillFactory.getSkill(((skillid / 10000) * 10000) + (resistance ? 2 : 1002)));
            remainingSp = Math.min((chr.getLevel() - 1), resistance ? 9 : 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
            isBeginnerSkill = true;
        } else if (GameConstants.isBeginnerJob(skillid / 10000)) {
            return;
        } else {
            remainingSp = chr.getRemainingSp();
        }

        final Skill skill = SkillFactory.getSkill(skillid);
        if (skill == null) {
            chr.dropMessage(1, "Invalid skill. Please report to admin as soon as possible.");
            //AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill without the required skill (" + skillid + ")");
            return;
        }

        for (Pair<Integer, Byte> ski : skill.getRequiredSkills()) {
            if (chr.getSkillLevel(SkillFactory.getSkill(ski.left)) < ski.right) {
                chr.dropMessage(1, "Error with required skills. Please report to admin as soon as possible.");
                //AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill without the required skill (" + skillid + ")");
                return;
            }
        }
        final int maxlevel = skill.getMasterLevel() > 0 ? chr.getMasterLevel(skill) : skill.getMaxLevel();
        final int curLevel = chr.getSkillLevel(skill);

        for (int i : GameConstants.blockedSkills) {
            if (skill.getId() == i) {
                c.announce(CWvsContext.enableActions());
                chr.dropMessage(1, "This skill has been blocked and may not be added.");
                return;
            }
        }

        if ((remainingSp > 0)) {
            //Randomizer.Max(maxlevel - curLevel, chr.getRemainingSp())
            int diff = maxlevel - curLevel;
            int useSP = Randomizer.Max(chr.getRemainingSp(), Randomizer.Max(diff, 3));
            if (!isBeginnerSkill) {
                chr.setRemainingSp(chr.getRemainingSp() - useSP);
            }
            chr.updateSingleStat(MapleStat.AVAILABLESP, 0); // we don't care the value here
            int skillLevel = Randomizer.Max(curLevel + useSP, maxlevel);
            chr.changeSingleSkillLevel(skill, skillLevel, chr.getMasterLevel(skill));
            //} else if (!skill.canBeLearnedBy(chr.getJob())) {
            //    AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to learn a skill for a different job (" + skillid + ")");
            chr.setStatLock(System.currentTimeMillis() + 100);
        } else {
            c.announce(CWvsContext.enableActions());
        }

    }

    public static final void AutoAssignAP(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
        final PlayerStats stat = chr.getStat();
        final int job = chr.getJob();
        int ap = chr.getRemainingAp();
        int amount = ap * 10;
        long maxhp = stat.getMaxHp() + amount;
        int maxmp = stat.getMaxMp() + amount;
        int fmaxhp = (int) Randomizer.LongMax(maxhp, GameConstants.getMaxHpMp());
        stat.setMaxHp(fmaxhp, chr);
        statupdate.put(MapleStat.HP, fmaxhp);
        statupdate.put(MapleStat.MAXHP, fmaxhp);
        int fmaxmp = Randomizer.Max(maxmp, GameConstants.getMaxMp(job));
        stat.setMaxMp(fmaxmp, chr);
        statupdate.put(MapleStat.MP, fmaxmp);
        statupdate.put(MapleStat.MAXMP, fmaxmp);
        chr.setRemainingAp(0);
        statupdate.put(MapleStat.AVAILABLEAP, 0);
        c.announce(CWvsContext.updatePlayerStats(statupdate, true, chr));
        chr.Heal();
        chr.dropMessage(1, "Hp/Mp has increased by " + amount);
        chr.setStatLock(System.currentTimeMillis() + 100);
    }
}
