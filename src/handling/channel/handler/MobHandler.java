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

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import java.util.ArrayList;
import server.MapleInventoryManipulator;
import server.Randomizer;
import server.maps.MapleMap;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleNodes.MapleNodeInfo;
import tools.Pair;
import tools.packet.MobPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CWvsContext;

public class MobHandler {

    public static final void MoveMonster(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (c == null || chr == null || chr.getMap() == null) {
            return; //?
        }
        if (chr.isChangingMaps()) {  // thanks Lame for noticing mob movement shuffle (mob OID on different maps) happening on map transitions
            return;
        }
        //System.out.println("test");a

        final int oid = slea.readInt();
        final MapleMap map = chr.getMap();
        final MapleMonster monster = map.getMonsterByOid(oid);
        if (monster == null || monster.getLinkCID() > 0) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (!monster.isAlive()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        long now = System.currentTimeMillis();

        //slea.skip(1); //?
        short moveid = slea.readShort();
        //System.out.println("action move: " + moveid);
        byte actionAndDirMask = slea.readByte();
        //System.out.println("action Mask: " + actionAndDirMask);
        byte actionAndDir = slea.readByte();
        //System.out.println("action dir: " + actionAndDir);

        final int targetInfo = slea.readInt();
        final int skillId = (int) (targetInfo & 0xFF);
        final int skillLevel = (int) ((targetInfo >> 16) & 0xFF);
        boolean movingAttack = (actionAndDirMask & 0x01) == 0x01;
        //System.out.println("action move: " + movingAttack);
        final short option = (short) ((targetInfo >> 16) & 0xFFFF);

        //final int skill1 = slea.readByte() & 0xFF; // unsigned?
        //final int skill2 = slea.readByte();
        //final int skill3 = slea.readByte();
        //final int skill4 = slea.readByte();
        int realskill = 0;
        int level = 0;
        int action = actionAndDir;
        if (action < 0) {
            action = -1;
        } else {
            action = action >> 1;
        }
        boolean used = false;
        boolean usedSkill = false;
        //System.out.println("action: " + action);

        if (!usedSkill) {
            usedSkill = true;
            final byte size = monster.getNoSkills();
            if (size > 0) {
                final Pair<Integer, Integer> skillToUse = monster.getSkills().get((byte) Randomizer.nextInt(size));
                realskill = ((Integer) skillToUse.getLeft()).intValue();
                level = ((Integer) skillToUse.getRight()).intValue();
                // Skill ID and Level
                MobSkill mobSkill = MobSkillFactory.getMobSkill(realskill, level);

                if (mobSkill != null && !mobSkill.checkCurrentBuff(chr, monster)) {
                    long ls = monster.getLastSkillUsed(realskill);
                    if (ls <= 0 || (((now - ls) > mobSkill.getCoolTime()) && !mobSkill.onlyOnce())) {
                        monster.setLastSkillUsed(realskill, now, mobSkill.getCoolTime());

                        int reqHp = (int) (((float) monster.getHp() / monster.getMobMaxHp()) * 100f); // In case this monster have 2.1b and above HP
                        if (reqHp <= mobSkill.getHP()) {
                            used = true;
                            mobSkill.applyEffect(chr, monster, true);
                        }
                    }
                }
            }
        }
        if (!used) {
            realskill = 0;
            level = 0;
        }
        int count = slea.readByte();
        //System.out.println("count1: " + count);
        List<Pair<Short, Short>> multiTargetForBall = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            multiTargetForBall.add(new Pair<>(slea.readShort(), slea.readShort()));
        }
        count = slea.readByte();
        //System.out.println("count2: " + count);
        final List< Short> randTimeForAreaAttack = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // randTimeForAreaAttack
            randTimeForAreaAttack.add(slea.readShort());
        }
        slea.skip(1);
        slea.skip(4); // sometimes 0, 1
        slea.skip(4); // CC DD FF 00  same for all mobs
        slea.skip(4); // CC DD FF 00  same for all mobs
        slea.skip(4); // 9D E1 87 48  same for all mobs
        //slea.skip(9);//mob position info
        //slea.skip(1); // aggro or possible stance?
        //Point startPos2 = slea.readPos();
        slea.skip(1);//stance

        //slea.skip(4);//duration
        //slea.skip(4);//pos
        //slea.skip(4);//pos
        //int tEncodedGatherDuration = slea.readInt(); // m_tEncodedGatherDuration
        int tEncodedGatherDuration = 0; // m_tEncodedGatherDuration
        //System.out.println("enc duration: " + tEncodedGatherDuration);
        final Point startPos = new Point(
                slea.readShort(), // m_x
                slea.readShort() // m_y
        );
        //System.out.println("start pos: x: " + startPos.x + " y: " + startPos.y);
        final Point velPos = new Point(
                slea.readShort(), // m_vx
                slea.readShort() // m_vy
        );
        //System.out.println("vel pos: x: " + velPos.x + " y: " + velPos.y);

        List res;
        try {
            res = MovementParse.parseMovement(chr, slea, 2);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error with monster movement parse");
            c.announce(CWvsContext.enableActions());
            return;
        }
        long timeDiff = System.currentTimeMillis() - monster.lastPosTime;
        if (res != null) {
            c.announce(MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), realskill, level));
            MovementParse.updatePosition(res, monster, -1);
            map.moveMonster(monster, monster.getPosition());
            map.broadcastMessage(chr, MobPacket.moveNewMonster(actionAndDirMask, actionAndDir, targetInfo, oid, tEncodedGatherDuration, startPos, velPos, res, multiTargetForBall, randTimeForAreaAttack), monster.getPosition());
            double dist = monster.lastPos.distance(monster.getPosition());
            //System.out.println("dist: " + dist);
            //System.out.println("speed: " + monster.getStats().getSpeed());
            if (dist > 300.0) {
                monster.posCount++;
                if (monster.posCount > 5) {
                    chr.kill();
                } else {
                    monster.posCount = 0;
                }
            }
            monster.lastPos.setLocation(monster.getPosition());
            monster.lastPosTime = System.currentTimeMillis();
        } else {
            c.announce(CWvsContext.enableActions());
        }

        //res.clear();
    }

    private static boolean inRangeInclusive(Byte pVal, Integer pMin, Integer pMax) {
        return !(pVal < pMin) || (pVal > pMax);
    }

    public static final void FriendlyDamage(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        final MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
        slea.skip(4); // Player ID
        final MapleMonster mobto = map.getMonsterByOid(slea.readInt());

        if (mobfrom != null && mobto != null && mobto.getStats().isFriendly()) {
            //code kaotic friendly damage
        }
    }

    public static final void MobBomb(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        final MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
        slea.skip(4); // something, 9E 07
        slea.readInt(); //-204?

        if (mobfrom != null && mobfrom.getBuff(MonsterStatus.MONSTER_BOMB) != null) {
            /* not sure
             12D -    0B 3D 42 00 EC 05 00 00 32 FF FF FF 00 00 00 00 00 00 00 00
             <monsterstatus done>
             108 - 07 0B 3D 42 00 EC 05 00 00 32 FF FF FF 01 00 00 00 7B 00 00 00
             */
        }
    }

    public static final void checkShammos(final MapleCharacter chr, final MapleMonster mobto, final MapleMap map) {
        if (!mobto.isAlive() && mobto.getStats().isEscort()) { //shammos
            for (MapleCharacter chrz : map.getCharacters()) { //check for 2022698
                if (chrz.getParty() != null && chrz.getParty().getLeader().getId() == chrz.getId()) {
                    //leader
                    if (chrz.haveItem(2022698)) {
                        MapleInventoryManipulator.removeById(chrz.getClient(), MapleInventoryType.USE, 2022698, 1, false, true);
                        mobto.heal((int) mobto.getMobMaxHp(), mobto.getMobMaxMp(), true);
                        return;
                    }
                    break;
                }
            }
            map.broadcastMessage(CWvsContext.serverNotice(6, "Your party has failed to protect the monster."));
            final MapleMap mapp = chr.getMap().getForcedReturnMap();
            for (MapleCharacter chrz : map.getCharacters()) {
                chrz.changeMap(mapp, mapp.getPortal(0));
            }
        } else if (mobto.getStats().isEscort() && mobto.getEventInstance() != null) {
            mobto.getEventInstance().setProperty("HP", String.valueOf(mobto.getHp()));
        }
    }

    public static final void MonsterBomb(SeekableLittleEndianAccessor slea, MapleClient c) {
        //System.out.println("bomb");
        //System.out.println("packet: " + slea.toString());
        int oid = slea.readInt();
        MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(oid);

        if (!c.getPlayer().isAlive() || monster == null) {
            return;
        }

        if (monster.getStats().selfDestruction() != null) {
            //System.out.println("BOMB2");
            //monster.getMap().broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 4));
            //c.getPlayer().getMap().removeMapObject(oid);
        }
    }

    public static final void AutoAggro(final int monsteroid, final MapleCharacter chr) {
        if (chr != null && chr.isAlive()) {
            final MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);
        }
        /*
         if (chr == null || chr.getMap() == null || chr.isHidden()) { //no evidence :)
            
         }
         final MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);
         if (monster != null && monster.getLinkCID() <= 0) {
         if (!monster.getMap().getAllPlayers().isEmpty()) {
         if (!monster.getTagged()) {
         monster.switchController(monster.getMap().findClosestPlayer(monster.getPosition()), true);
         } else {
         if (monster.getController() == null && !monster.getController().isAlive()) {
         monster.setTagged(false);
         monster.switchController(monster.getMap().findClosestPlayer(monster.getPosition()), true);
         }
         monster.switchController(chr, true);
         }
         }
         }
         */
    }

    public static final void HypnotizeDmg(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
        slea.skip(4); // Player ID
        final int to = slea.readInt(); // mobto
        slea.skip(1); // Same as player damage, -1 = bump, integer = skill ID
        final int damage = slea.readInt();
//	slea.skip(1); // Facing direction
//	slea.skip(4); // Some type of pos, damage display, I think
    }

    public static final void DisplayNode(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
        if (mob_from != null) {
            chr.getClient().announce(MobPacket.getNodeProperties(mob_from, chr.getMap()));
        }
    }

    public static final void MobNode(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt()); // From
        final int newNode = slea.readInt();
        final int nodeSize = chr.getMap().getNodes().size();
        if (mob_from != null && nodeSize > 0) {
            final MapleNodeInfo mni = chr.getMap().getNode(newNode);
            if (mni == null) {
                return;
            }
            if (mni.attr == 2) { //talk
                switch (chr.getMapId() / 100) {
                    case 9211200:
                    case 9211201:
                    case 9211202:
                    case 9211203:
                    case 9211204:
                        chr.getMap().talkMonster("Please escort me carefully.", 5120035, mob_from.getObjectId()); //temporary for now. itemID is located in WZ file
                        break;
                    case 9320001:
                    case 9320002:
                    case 9320003:
                        chr.getMap().talkMonster("Please escort me carefully.", 5120051, mob_from.getObjectId()); //temporary for now. itemID is located in WZ file
                        break;
                }
            }
            mob_from.setLastNode(newNode);
            if (chr.getMap().isLastNode(newNode)) { //the last node on the map.
                switch (chr.getMapId() / 100) {
                    case 9211200:
                    case 9211201:
                    case 9211202:
                    case 9211203:
                    case 9211204:
                    case 9320001:
                    case 9320002:
                    case 9320003:
                        chr.getMap().broadcastMessage(CWvsContext.serverNotice(5, "Proceed to the next stage."));
                        chr.getMap().removeMonster(mob_from, false);
                        break;

                }
            }
        }
    }

    public static final void RenameFamiliar(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.announce(CWvsContext.enableActions());
    }

    public static final void SpawnFamiliar(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.announce(CWvsContext.enableActions());
    }

    public static final void MoveFamiliar(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.announce(CWvsContext.enableActions());
    }

    public static final void AttackFamiliar(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.announce(CWvsContext.enableActions());
    }

    public static final void TouchFamiliar(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseFamiliar(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.announce(CWvsContext.enableActions());
    }
}
