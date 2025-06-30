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
package tools.packet;

import java.util.Map;
import java.util.List;
import java.awt.Point;

import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;

import constants.GameConstants;
import handling.SendPacketOpcode;
import java.util.Calendar;
import java.util.Collection;
import server.Randomizer;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.MapleMap;
import server.maps.MapleNodes.MapleNodeInfo;
import server.movement.LifeMovementFragment;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.input.SeekableLittleEndianAccessor;

public class MobPacket {

    public static byte[] damageMonster(final int oid, final long damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt((int) (damage > Integer.MAX_VALUE ? Integer.MAX_VALUE : damage));
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] damageFriendlyMob(final MapleMonster mob, final long damage, final boolean display) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.write(display ? 1 : 2); //false for when shammos changes map!
        mplew.writeInt((int) (damage > Integer.MAX_VALUE ? Integer.MAX_VALUE : damage));
        mplew.writeInt((int) (((double) mob.getHp() / (double) mob.getMobMaxHp()) * 10000)); //currhp
        mplew.writeInt((int) 10000);//max hp
        return mplew.getPacket();
    }

    public static byte[] killMonster(int oid, boolean animation) {
        return killMonster(oid, animation ? 1 : 0);
    }

    public static byte[] killMonster(final int oid, final int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(animation); // 0 = dissapear, 1 = fade out, 2+ = bomb, 5 = summon timeout
        return mplew.getPacket();
    }

    public static byte[] killMonster(final int oid, final int animation, int dur) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(animation); // 0 = dissapear, 1 = fade out, 2+ = bomb, 5 = summon timeout
        mplew.writeInt(dur);
        return mplew.getPacket();
    }

    public static byte[] suckMonster(final int oid, final int chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(4);
        mplew.writeInt(chr);

        return mplew.getPacket();
    }

    public static byte[] healMonster(final int oid, final int heal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(-heal);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] MobToMobDamage(final int oid, final int dmg, final int mobid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOB_TO_MOB_DAMAGE.getValue());
        mplew.writeInt(oid);
        mplew.write(0); // looks like the effect, must be > -2
        mplew.writeInt(dmg);
        mplew.writeInt(mobid);
        mplew.write(1); // ?

        return mplew.getPacket();
    }

    public static byte[] getMobSkillEffect(final int oid, final int skillid, final int cid, final int skilllevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT_MOB.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(skillid); // 3110001, 3210001, 13110009, 2210000
        mplew.writeInt(cid);
        mplew.writeShort(skilllevel);

        return mplew.getPacket();
    }

    public static byte[] getMobCoolEffect(final int oid, final int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ITEM_EFFECT_MOB.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid); // 2022588

        return mplew.getPacket();
    }

    public static byte[] showMonsterHP(int oid, int remhppercentage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(oid);
        mplew.write(remhppercentage);

        return mplew.getPacket();
    }

    public static byte[] SmartMobnotice(int mobId, String message, boolean yellow) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SMART_MOB_NOTICE.getValue());
        mplew.writeInt(yellow ? 1 : 0);//0:white, 1:yellow
        mplew.writeInt(mobId);
        mplew.writeMapleAsciiString(message);

        return mplew.getPacket();
    }

    public static byte[] showCygnusAttack(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CYGNUS_ATTACK.getValue());
        mplew.writeInt(oid); // mob must be 8850011

        return mplew.getPacket();
    }

    public static byte[] showMonsterResist(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_RESIST.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(0);
        mplew.writeShort(1); // resist >0
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] clearBossHP(final MapleMonster mob) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(9833521); //hack: MV cant have boss hp bar
        mplew.writeInt((int) (-1)); //currhp
        mplew.writeInt((int) (100));//max hp
        mplew.write(0);//front color
        mplew.write(5);//background color
        return mplew.getPacket();
    }

    public static byte[] forceBossHP(final MapleMonster mob, double perc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(9833521); //hack: MV cant have boss hp bar
        //mplew.writeInt(8850011); //hack: MV cant have boss hp bar
        mplew.writeInt(Randomizer.MinMax((int) (perc * 100), 1, 10000)); //currhp
        mplew.writeInt((int) (10000));//max hp
        int percent = (int) (perc * 0.1);
        mplew.write(20 + percent);//front color
        mplew.write(31);//background color
        return mplew.getPacket();
    }

    public static byte[] moveNewMonster(byte useskill, int skill, int targetInfo, int oid, int tEncodedGatherDuration, Point startPos, Point velPos, List<LifeMovementFragment> moves, List<Pair<Short, Short>> multiTargetForBall, List<Short> randTimeForAreaAttack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(useskill);
        mplew.write(skill);
        mplew.writeInt(targetInfo);
        mplew.write(multiTargetForBall.size());

        for (int i = 0; i < multiTargetForBall.size(); i++) {
            mplew.writeShort(multiTargetForBall.get(i).left);
            mplew.writeShort(multiTargetForBall.get(i).right);
        }
        mplew.write(randTimeForAreaAttack.size());
        for (int i = 0; i < randTimeForAreaAttack.size(); i++) {
            mplew.writeShort(randTimeForAreaAttack.get(i));
        }
        mplew.writePos(startPos);
        mplew.writePos(velPos);
        PacketHelper.serializeMovementList(mplew, moves);
        //mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(useskill ? 1 : 0);
        mplew.write(skill);
        mplew.write(skill1);
        mplew.write(skill2);
        mplew.write(skill3);
        mplew.write(skill4);
        mplew.writeShort(0);
        mplew.writePos(startPos);
        mplew.writeInt(Randomizer.nextInt());
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    /*

     public static byte[] moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, Point startPos, SeekableLittleEndianAccessor movementSlea, long movementDataLength) {
     MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

     mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
     mplew.writeInt(oid);
     mplew.write(useskill ? 1 : 0);
     mplew.write(skill);
     mplew.write(skill1);
     mplew.write(skill2);
     mplew.write(skill3);
     mplew.write(skill4);
     mplew.writeShort(0);
     mplew.writePos(startPos);
     mplew.writeInt(Randomizer.nextInt());
     rebroadcastMovementList(mplew, movementSlea, movementDataLength);

     return mplew.getPacket();
     }
    
     */
    private static void rebroadcastMovementList(MaplePacketLittleEndianWriter lew, SeekableLittleEndianAccessor slea, long movementDataLength) {
        //movement command length is sent by client, probably not a big issue? (could be calculated on server)
        //if multiple write/reads are slow, could use a (cached?) byte[] buffer
        for (long i = 0; i < movementDataLength; i++) {
            lew.write(slea.readByte());
        }
    }

    /*
     public static byte[] moveMonster(boolean useskill, int skill, int unk, int oid, Point startPos, List<LifeMovementFragment> moves, final List<Integer> unk2, final List<Pair<Integer, Integer>> unk3) {
     MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

     mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
     mplew.writeInt(oid);
     mplew.write(useskill ? 1 : 0);
     mplew.write(skill);
     mplew.writeInt(unk);
     mplew.write(unk3 == null ? 0 : unk3.size()); // For each, 2 short
     if (unk3 != null) {
     for (final Pair<Integer, Integer> i : unk3) {
     mplew.writeShort(i.left);
     mplew.writeShort(i.right);
     }
     }
     mplew.write(unk2 == null ? 0 : unk2.size()); // For each, 1 short
     if (unk2 != null) {
     for (final Integer i : unk2) {
     mplew.writeShort(i);
     }
     }
     mplew.writePos(startPos);
     mplew.writeShort(8);
     mplew.writeShort(1);
     PacketHelper.serializeMovementList(mplew, moves);

     return mplew.getPacket();
     }
     */
    private static void rebroadcastMovementList(MaplePacketLittleEndianWriter lew, LittleEndianAccessor slea, long movementDataLength) {
        //movement command length is sent by client, probably not a big issue? (could be calculated on server)
        //if multiple write/reads are slow, could use a (cached?) byte[] buffer
        for (long i = 0; i < movementDataLength; i++) {
            lew.write(slea.readByte());
        }
    }

    public static byte[] spawnMonster(MapleMonster life, int spawnType, int link) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.write(5); // 1 = Control normal, 5 = Control none
        mplew.writeInt(life.getId());
        addMonsterStatus(mplew, life);
        mplew.writePos(life.getTruePosition());
        mplew.write(life.getStance());
        mplew.writeShort(0); // FH
        mplew.writeShort(life.getFh()); // Origin FH
        mplew.write(spawnType);
        //System.out.println("type " + spawnType);
        if (spawnType == -3 || spawnType >= 0) {
            mplew.writeInt(link);
        }
        mplew.write(life.getCarnivalTeam());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(-1);

        return mplew.getPacket();
    }

    public static byte[] spawnTeleportMonster(MapleMonster life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.write(life.getController() != null ? 1 : 5); // 1 = Control normal, 5 = Control none
        mplew.writeInt(life.getId());
        addMonsterStatus(mplew, life);
        mplew.writePos(life.getPosition());
        mplew.write(life.getStance());
        mplew.writeShort(0); // FH
        mplew.writeShort(life.getFh()); // Origin FH
        mplew.write(-1);
        mplew.writeInt(0);
        mplew.write(life.getCarnivalTeam());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(-1);
        mplew.writeInt(life.getId());

        return mplew.getPacket();
    }

    public static void addMonsterStatus(MaplePacketLittleEndianWriter mplew, MapleMonster life) {
        if (life.isAlive()) {

            if (life.getStati().size() <= 1) {
                life.addEmpty(); //not done yet lulz ok so we add it now for the lulz
            }
            mplew.write(life.getChangedStats() != null ? 1 : 0);
            if (life.getChangedStats() != null) {
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().hp, Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().mp, Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().exp, Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().watk, Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().matk, Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().PDRate, Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().MDRate, Integer.MAX_VALUE));
                mplew.writeInt(Integer.MAX_VALUE);
                mplew.writeInt(0);
                mplew.writeInt(Randomizer.Max((int) (life.getStats().getScale() >= 4 ? Integer.MAX_VALUE : life.getChangedStats().pushed), Integer.MAX_VALUE));
                mplew.writeInt(Randomizer.Max((int) life.getChangedStats().level, 9999));
            }
            final boolean ignore_imm = life.getStati().containsKey(MonsterStatus.WEAPON_DAMAGE_REFLECT) || life.getStati().containsKey(MonsterStatus.MAGIC_DAMAGE_REFLECT);
            Collection<MonsterStatusEffect> buffs = life.getStati().values();
            getLongMask_NoRef(mplew, buffs, ignore_imm);
            for (MonsterStatusEffect buff : buffs) {
                if (buff != null && buff.getStati() != MonsterStatus.WEAPON_DAMAGE_REFLECT && buff.getStati() != MonsterStatus.MAGIC_DAMAGE_REFLECT && (!ignore_imm || (buff.getStati() != MonsterStatus.WEAPON_IMMUNITY && buff.getStati() != MonsterStatus.MAGIC_IMMUNITY && buff.getStati() != MonsterStatus.DAMAGE_IMMUNITY))) {
                    mplew.writeInt(buff.getX().intValue());
                    if (buff.getMobSkill() != null) {
                        mplew.writeShort(buff.getMobSkill().getSkillId());
                        mplew.writeShort(buff.getMobSkill().getSkillLevel());
                    } else if (buff.getSkill() > 0) {
                        mplew.writeInt(buff.getSkill());
                    }
                    mplew.writeShort(buff.getStati().isEmpty() ? 0 : 1);
                }
            }
        }
    }

    public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(aggro ? 2 : 1);
        mplew.writeInt(life.getObjectId());
        mplew.write(life.getController() != null ? 1 : 5); // 1 = Control normal, 5 = Control none
        mplew.writeInt(life.getId());
        addMonsterStatus(mplew, life);

        mplew.writePos(life.getTruePosition());
        mplew.write(life.getStance()); // Bitfield
        mplew.writeShort(0); // FH
        mplew.writeShort(life.getFh()); // Origin FH
        mplew.write(life.isFake() ? -4 : newSpawn ? -2 : -1);
        mplew.write(life.getCarnivalTeam());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(-1);

        return mplew.getPacket();
    }

    public static byte[] stopControllingMonster(MapleMonster life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(0);
        mplew.writeInt(life.getObjectId());

        return mplew.getPacket();
    }

    public static byte[] ControllingMonster(MapleMonster life, boolean aggro) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
        mplew.write(aggro ? 2 : 1);
        mplew.writeInt(life.getObjectId());
        mplew.write(1); // 1 = Control normal, 5 = Control none
        mplew.writeInt(life.getId());
        addMonsterStatus(mplew, life);

        return mplew.getPacket();
    }

    public static byte[] makeMonsterReal(MapleMonster life) {
        return spawnMonster(life, -1, 0);
    }

    public static byte[] makeMonsterFake(MapleMonster life) {
        return spawnMonster(life, -4, 0);
    }

    public static byte[] makeMonsterEffect(MapleMonster life, int effect) {
        return spawnMonster(life, effect, 0);
    }

    public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
        return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
    }

    public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort(moveid);
        mplew.write(useSkills ? 1 : 0);
        mplew.writeShort(Short.MAX_VALUE);
        mplew.write(skillId);
        mplew.write(skillLevel);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getMonsterTeleport(int objectid, int nTeleportType, Point pos, Point oldpos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TELE_MONSTER.getValue());
        mplew.writeInt(objectid);
        mplew.write(nTeleportType > 0 ? 1 : 0);
        if (nTeleportType != 0) {
            mplew.writeInt(nTeleportType);
            switch (nTeleportType) {
                case 3:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 100: {
                    mplew.writePos(oldpos);
                    mplew.writePos(pos);
                    break;
                }
                case 4:
                case 10:
                case 11:
                case 12:
                case 14: {
                    mplew.writePos(pos);
                    break;
                }
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getMonsterTeleport(int objectid, int nOffsetX, int nOffsetY) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TELE_MONSTER.getValue());
        mplew.writeInt(objectid);
        mplew.writeInt(nOffsetX);
        mplew.writeInt(nOffsetY);

        return mplew.getPacket();
    }

    private static void getLongMask_NoRef(MaplePacketLittleEndianWriter mplew, Collection<MonsterStatusEffect> ss, boolean ignore_imm) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        for (MonsterStatusEffect statup : ss) {
            if (statup != null && statup.getStati() != MonsterStatus.WEAPON_DAMAGE_REFLECT && statup.getStati() != MonsterStatus.MAGIC_DAMAGE_REFLECT && (!ignore_imm || (statup.getStati() != MonsterStatus.WEAPON_IMMUNITY && statup.getStati() != MonsterStatus.MAGIC_IMMUNITY && statup.getStati() != MonsterStatus.DAMAGE_IMMUNITY))) {
                mask[statup.getStati().getPosition() - 1] |= statup.getStati().getValue();
            }
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[i - 1]);
        }
    }

    public static byte[] applyMonsterStatus(final int oid, final MonsterStatus mse, int x, MobSkill skil) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        PacketHelper.writeSingleMask(mplew, mse);

        mplew.writeInt(x);
        mplew.writeShort(skil.getSkillId());
        mplew.writeShort(skil.getSkillLevel());
        mplew.writeShort(mse.isEmpty() ? 1 : 0); // might actually be the buffTime but it's not displayed anywhere

        mplew.writeShort(0); // delay in ms
        mplew.write(1); // size
        mplew.write(1); // ? v97

        return mplew.getPacket();
    }

    public static byte[] applyMonsterStatus(final MapleMonster mons, final MonsterStatusEffect ms) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(mons.getObjectId());
        PacketHelper.writeSingleMask(mplew, ms.getStati());

        mplew.writeInt(ms.getX().intValue());
        if (ms.isMonsterSkill()) {
            mplew.writeShort(ms.getMobSkill().getSkillId());
            mplew.writeShort(ms.getMobSkill().getSkillLevel());
        } else if (ms.getSkill() > 0) {
            mplew.writeInt(ms.getSkill());
        }
        mplew.writeShort(ms.getStati().isEmpty() ? 1 : 0); // might actually be the buffTime but it's not displayed anywhere

        mplew.writeShort(0); // delay in ms
        mplew.write(1); // size
        mplew.write(1); // ? v97

        return mplew.getPacket();
    }

    public static byte[] applyMonsterStatus(final MapleMonster mons, final List<MonsterStatusEffect> mse) {
        if (mse.size() <= 0 || mse.get(0) == null) {
            return CWvsContext.enableActions();
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(mons.getObjectId());
        final MonsterStatusEffect ms = mse.get(0);
        if (ms.getStati() == MonsterStatus.POISON) { // burn also here
            //PacketHelper.writeSingleMask(mplew, MonsterStatus.EMPTY);
            PacketHelper.writeSingleMask(mplew, ms.getStati().POISON);
            mplew.write(mse.size());
            for (MonsterStatusEffect m : mse) {
                mplew.writeInt(m.getFromID()); //character ID
                if (m.isMonsterSkill()) {
                    mplew.writeShort(m.getMobSkill().getSkillId());
                    mplew.writeShort(m.getMobSkill().getSkillLevel());
                } else if (m.getSkill() > 0) {
                    mplew.writeInt(m.getSkill());
                }
                mplew.writeInt(m.getX()); //dmg
                mplew.writeInt(1000); //delay
                mplew.writeInt(0); // tick count
                mplew.writeInt(5); //buff time ?
                mplew.writeInt(0);
            }
            mplew.writeShort(300); // delay in ms
            mplew.write(1); // size
            mplew.write(1); // ? v97
        } else {
            PacketHelper.writeSingleMask(mplew, ms.getStati());

            mplew.writeInt(ms.getX().intValue());
            if (ms.isMonsterSkill()) {
                mplew.writeShort(ms.getMobSkill().getSkillId());
                mplew.writeShort(ms.getMobSkill().getSkillLevel());
            } else if (ms.getSkill() > 0) {
                mplew.writeInt(ms.getSkill());
            }
            mplew.writeShort(0); // might actually be the buffTime but it's not displayed anywhere

            mplew.writeShort(0); // delay in ms
            mplew.write(1); // size
            mplew.write(1); // ? v97
        }

        return mplew.getPacket();
    }

    public static byte[] applyMonsterStatus(final int oid, final Map<MonsterStatus, Integer> stati, final List<Integer> reflection, MobSkill skil) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        PacketHelper.writeMask(mplew, stati.keySet());

        for (Map.Entry<MonsterStatus, Integer> mse : stati.entrySet()) {
            mplew.writeInt(mse.getValue().intValue());
            mplew.writeShort(skil.getSkillId());
            mplew.writeShort(skil.getSkillLevel());
            mplew.writeShort(0); // might actually be the buffTime but it's not displayed anywhere
        }
        //System.out.println("reflection size: " + reflection.size());
        for (Integer ref : reflection) {
            //System.out.println("ref value: " + ref.intValue());
            mplew.writeInt(ref.intValue());
        }
        mplew.writeLong(0L);
        mplew.writeShort(0); // delay in ms

        int size = stati.size(); // size
        if (reflection.size() > 0) {
            size /= 2; // This gives 2 buffs per reflection but it's really one buff
        }
        mplew.write(size); // size
        mplew.write(1); // ? v97

        return mplew.getPacket();
    }

    public static byte[] cancelMonsterStatus(int oid, MonsterStatus stat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        PacketHelper.writeSingleMask(mplew, stat);
        mplew.write(1); // reflector is 3~!??
        mplew.write(2); // ? v97

        return mplew.getPacket();
    }

    public static byte[] cancelPoison(int oid, MonsterStatusEffect m) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(oid);
        //PacketHelper.writeSingleMask(mplew, MonsterStatus.EMPTY);
        PacketHelper.writeSingleMask(mplew, m.getStati());
        mplew.writeInt(0);
        mplew.writeInt(1); //size probably
        mplew.writeInt(m.getFromID()); //character ID
        if (m.isMonsterSkill()) {
            mplew.writeShort(m.getMobSkill().getSkillId());
            mplew.writeShort(m.getMobSkill().getSkillLevel());
        } else if (m.getSkill() > 0) {
            mplew.writeInt(m.getSkill());
        }
        mplew.write(3); // ? v97

        return mplew.getPacket();
    }

    public static byte[] talkMonster(int oid, int itemId, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TALK_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(500); //?
        mplew.writeInt(itemId);
        mplew.write(itemId <= 0 ? 0 : 1);
        mplew.write(msg == null || msg.length() <= 0 ? 0 : 1);
        if (msg != null && msg.length() > 0) {
            mplew.writeMapleAsciiString(msg);
        }
        mplew.writeInt(1); //?

        return mplew.getPacket();
    }

    public static byte[] removeTalkMonster(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_TALK_MONSTER.getValue());
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static final byte[] getNodeProperties(final MapleMonster objectid, final MapleMap map) {
        if (objectid.getNodePacket() != null) {
            return objectid.getNodePacket();
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
        mplew.writeInt(objectid.getObjectId()); //?
        mplew.writeInt(map.getNodes().size());
        mplew.writeInt(objectid.getPosition().x);
        mplew.writeInt(objectid.getPosition().y);
        for (MapleNodeInfo mni : map.getNodes()) {
            mplew.writeInt(mni.x);
            mplew.writeInt(mni.y);
            mplew.writeInt(mni.attr);
            if (mni.attr == 2) { //msg
                mplew.writeInt(500); //? talkMonster
            }
        }
        mplew.writeInt(0);
        mplew.write(0); // tickcount, extra 1 int
        mplew.write(0);

        objectid.setNodePacket(mplew.getPacket());
        return objectid.getNodePacket();
    }

    public static byte[] showMagnet(int mobid, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success ? 1 : 0);
        mplew.write(0); // times, 0 = once, > 0 = twice

        return mplew.getPacket();
    }

    public static byte[] catchMonster(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);

        return mplew.getPacket();
    }
}
