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

import client.inventory.Item;
import client.Skill;
import client.SkillFactory;
import client.SkillMacro;
import constants.GameConstants;
import client.inventory.MapleInventoryType;
import client.MapleBuffStat;
import client.MapleClient;
import client.MapleCharacter;
import client.PlayerStats;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import server.events.MapleEvent;
import server.events.MapleEventType;
import scripting.PortalScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.MaplePortal;
import server.Randomizer;
import server.Timer.CloneTimer;
import server.events.MapleSnowball.MapleSnowballs;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.FieldLimitType;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.Pair;
import tools.packet.MobPacket;
import tools.packet.MTSCSPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField.EffectPacket;
import tools.packet.CField.UIPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InventoryPacket;

public class PlayerHandler {

    public static int isFinisher(final int skillid) {
        switch (skillid) {
            case 1111003:
                return GameConstants.GMS ? 0 : 10;
            case 1111005:
                return GameConstants.GMS ? 0 : 10;
            case 11111002:
                return GameConstants.GMS ? 0 : 10;
            case 11111003:
                return GameConstants.GMS ? 0 : 10;
        }
        return 0;
    }

    public static void ChangeSkillMacro(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final int num = slea.readByte();
        String name;
        int shout, skill1, skill2, skill3;
        SkillMacro macro;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE skill1 = VALUES(skill1), skill2 = VALUES(skill2), skill3 = VALUES(skill3), name = VALUES(name), skill3 = VALUES(shout)")) {
                for (int i = 0; i < num; i++) {
                    name = slea.readMapleAsciiString();
                    shout = slea.readByte();
                    skill1 = slea.readInt();
                    skill2 = slea.readInt();
                    skill3 = slea.readInt();

                    macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
                    chr.updateMacros(i, macro);

                    ps.setInt(1, chr.getId());
                    ps.setInt(2, macro.getSkill1());
                    ps.setInt(3, macro.getSkill2());
                    ps.setInt(4, macro.getSkill3());
                    ps.setString(5, macro.getName());
                    ps.setInt(6, macro.getShout());
                    ps.setInt(7, i);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }
    }

    public static final void ChangeKeymap(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        if (slea.available() > 8 && chr != null) { // else = pet auto pot
            slea.skip(4); //0
            final int numChanges = slea.readInt();

            for (int i = 0; i < numChanges; i++) {
                final int key = slea.readInt();
                final byte type = slea.readByte();
                final int action = slea.readInt();
                if (type == 1 && action >= 1000) { //0 = normal key, 1 = skill, 2 = item
                    final Skill skil = SkillFactory.getSkill(action);
                    if (skil != null) { //not sure about aran tutorial skills..lol
                        if ((!skil.isBeginnerSkill() && skil.isInvisible() && chr.getSkillLevel(skil) <= 0) || GameConstants.isLinkedAranSkill(action) || action % 10000 < 1000 || action >= 91000000) { //cannot put on a key
                            continue;
                        }
                    }
                }
                chr.changeKeybinding(key, type, action);
            }
            chr.getKeyLayout().saveKeys(chr.getId());
        } else if (chr != null) {
            final int type = slea.readInt(), data = slea.readInt();
            switch (type) {
                case 1:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(GameConstants.HP_ITEM));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.HP_ITEM)).setCustomData(String.valueOf(data));
                    }
                    break;
                case 2:
                    if (data <= 0) {
                        chr.getQuestRemove(MapleQuest.getInstance(GameConstants.MP_ITEM));
                    } else {
                        chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.MP_ITEM)).setCustomData(String.valueOf(data));
                    }
                    break;
            }
        }
    }

    public static final void UseTitle(final int itemId, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
        if (toUse == null) {
            return;
        }
        if (itemId <= 0) {
            chr.getQuestRemove(MapleQuest.getInstance(GameConstants.ITEM_TITLE));
        } else {
            chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.ITEM_TITLE)).setCustomData(String.valueOf(itemId));
        }
        chr.getMap().broadcastMessage(chr, CField.showTitle(chr.getId(), itemId), false);
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseChair(final int itemId, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        if (chr.getEventInstance() != null) {
            chr.dropMessage("Not usable here.");
            return;
        }
        final Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
        if (toUse == null) {
            //chr.getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(itemId));
            return;
        }
        if (GameConstants.isFishingMap(chr.getMapId()) && (!GameConstants.GMS || itemId == 3011000)) {
            if (chr.getStat().canFish) {
                chr.startFishingTask();
            }
        }
        chr.setChair(itemId);
        chr.getMap().broadcastMessage(chr, CField.showChair(chr.getId(), itemId), false);
        c.announce(CWvsContext.enableActions());
    }

    public static final void CancelChair(final short id, final MapleClient c, final MapleCharacter chr) {
        if (id == -1) { // Cancel Chair
            chr.cancelFishingTask();
            chr.setChair(0);
            c.announce(CField.cancelChair(-1));
            if (chr.getMap() != null) {
                chr.getMap().broadcastMessage(chr, CField.showChair(chr.getId(), 0), false);
            }
        } else { // Use In-Map Chair
            chr.setChair(id);
            c.announce(CField.cancelChair(id));
        }
    }

    public static final void TrockAddMap(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        final byte addrem = slea.readByte();
        final byte vip = slea.readByte();
        if (c.getPlayer().getEventInstance() != null) {
            chr.dropMessage(1, "Maps cannot be saved while inside an instance.");
        } else {
            if (vip == 1) { // Regular rocks
                if (addrem == 0) {
                    chr.deleteFromRegRocks(slea.readInt());
                } else if (addrem == 1) {
                    if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
                        chr.addRegRockMap();
                    } else {
                        chr.dropMessage(1, "This map is not available to enter for the list.");
                    }
                }
            } else if (vip == 2) { // VIP Rock
                if (addrem == 0) {
                    chr.deleteFromRocks(slea.readInt());
                } else if (addrem == 1) {
                    if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
                        chr.addRockMap();
                    } else {
                        chr.dropMessage(1, "This map is not available to enter for the list.");
                    }
                }
            } else if (vip == 3) { // Hyper Rocks
                if (addrem == 0) {
                    chr.deleteFromHyperRocks(slea.readInt());
                } else if (addrem == 1) {
                    if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
                        chr.addHyperRockMap();
                    } else {
                        chr.dropMessage(1, "This map is not available to enter for the list.");
                    }
                }
            }
        }
        c.announce(MTSCSPacket.OnMapTransferResult(chr, vip, addrem == 0));
    }

    public static final void CharInfoRequest(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (chr.getMap() == null) {
            return;
        }
        chr.updateTick(slea.readInt());
        int objectid = slea.readInt();
        final MapleCharacter player = chr.getMap().getCharacterById(objectid);
        c.announce(CWvsContext.enableActions());
        if (player != null && !player.isClone()) {
            if (!player.isGM() || chr.isGM()) {
                c.announce(CWvsContext.charInfo(player, chr.getId() == objectid));
            }
        }
    }

    public static boolean isBomb(int id) {
        return switch (id) {
            case 8240100, 8240101, 8240102, 8240107, 8240200, 8240108, 8240201, 8240109, 8240202, 8240203, 8240204, 8240205, 8240206, 9833641, 9833642, 9833643, 9833661, 8240126, 8880315, 8880317, 8880319, 9601278, 8880810 ->
                true;
            default ->
                false;
        };
    }

    public static double bomb(int id) {
        return switch (id) {
            case 8240100, 8240101, 8240102 ->
                0.25;
            case 8240107, 8240200 ->
                0.25;
            case 8240108, 8240201 ->
                0.50;
            case 8240109, 8240202 ->
                0.75;
            case 8240203 ->
                0.25;
            case 8240204 ->
                0.50;
            case 8240205 ->
                0.75;
            case 8240206, 8880810 ->
                1.00;
            case 9833641, 9833642, 9833643, 9833661, 8240126 ->
                0.25;
            case 8880315 ->
                0.25;
            case 8880317 ->
                0.50;
            case 8880319, 9601278 ->
                0.25;
            default ->
                0.25;
        };
    }

    public static final void TakeDamage(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        //System.out.println("damage: " + slea);
        if (c == null) {
            return;
        }
        if (chr == null) {
            return;
        }
        if (chr.battle) {
            return;
        }
        if (!chr.canDamage()) {
            return;
        }
        if (chr.getPortalDelay() || chr.isGM()) {
            chr.getClient().announce(CWvsContext.damagePlayer(chr, 0));
            chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), 0, false), false);
            return;
        }
        slea.skip(4);//ticks?
        slea.skip(4);//ticks?
        //
        //chr.updateTick(slea.readInt()); // ticks
        final byte type = slea.readByte(); //-4 is mist, -3 and -2 are map damage.
        slea.skip(1); // Element - 0x00 = elementless, 0x01 = ice, 0x02 = fire, 0x03 = lightning
        int damage = slea.readInt();

        slea.skip(2);
        boolean isDeadlyAttack = false, object = false;

        int oid;
        int monsteridfrom;
        int fake;
        byte direction;
        double mobdiff = 1.0;
        MapleMonster attacker = null;
        if (chr == null || chr.isHidden() || chr.getMap() == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.getImmune()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        final PlayerStats stats = chr.getStat();
        //System.out.println("Type: " + type);//-1 = touch 0=magic
        if (chr.getTotalLevel() <= 10 && chr.getMapId() == 5003) {
            return;
        }
        if (type == -3) {
            if (!chr.isGM()) {
                if (damage == 0) {
                    damage = 99999999;
                }
                object = true;
                damage = Randomizer.Max(damage, 9999999);
                chr.addMPHP(-damage, 0);
                chr.getMap().broadcastMessage(chr, CWvsContext.damagePlayer(chr, damage), true);
            }
            return;
        }
        if (type != -2 && type != -3 && type != -4) { // Not map damage
            double dam;
            monsteridfrom = slea.readInt();
            oid = slea.readInt();
            attacker = chr.getMap().getMonsterByOid(oid);
            direction = slea.readByte(); // Knock direction
            //System.out.println("Type: " + type);//-1 = touch 0=magic
            if (damage > 0) { // Bump damage
                //System.out.println("P Atk: " + attacker.getStats().PhysicalAttack);
                //System.out.println("M Atk: " + attacker.getStats().MagicAttack);
                if (attacker == null) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                if (attacker.isDead()) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                if (attacker.getId() != monsteridfrom || attacker.getLinkCID() > 0 || attacker.isFake() || attacker.getStats().isFriendly()) {
                    if (isBomb(monsteridfrom) && type == 0 && !chr.isGM()) {
                        if (chr.getEventInstance() != null && chr.getEventInstance().getValue("deadly") > 0) {
                            int pdamage = Randomizer.random(1, chr.getStat().getHp());
                            int mdamage = 0;
                            if (!GameConstants.isDemon(chr.getJob())) {
                                mdamage = Randomizer.random(1, chr.getStat().getMp());
                            }
                            chr.addMPHP(-pdamage, -mdamage);
                            chr.getMap().broadcastMessage(chr, CWvsContext.damagePlayer(chr, pdamage), true);
                        } else {
                            if (!chr.getLock()) {
                                damage = (int) ((double) (chr.getStat().getCurrentMaxHp() * bomb(monsteridfrom)));
                                damage = Randomizer.Min((int) (damage - (damage * chr.getStat().getResist())), 1);
                                chr.addMPHP(-damage, 0);
                                chr.getMap().broadcastMessage(chr, CWvsContext.damagePlayer(chr, damage), true);
                            }
                        }
                    }
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                if (chr.isInvincible() || !chr.canDamage() || !chr.canDamage()) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                double defscale;
                double atkpower;
                double basediff = ((((double) attacker.getStats().getLevel() / (double) chr.getTotalLevel()) - 1) * 5) + 1;
                double diff = Math.pow(Randomizer.DoubleMin(basediff, 1), 2.5);
                double basediff2 = (double) attacker.getStats().getLevel() / (double) chr.getTotalLevel();
                mobdiff = Randomizer.DoubleMinMax(basediff2, 1.0, 4.0);

                if (type == -1) {
                    atkpower = Randomizer.MaxLong((long) (attacker.getStats().getPhysicalAttack() * diff), 99999999);
                    defscale = Randomizer.DoubleMin(Randomizer.DoubleMax((double) atkpower / (double) chr.getStat().getWDef(), 1.0), 0.25);
                } else {
                    atkpower = Randomizer.MaxLong((long) (attacker.getStats().getMagicAttack() * diff), 99999999);
                    defscale = Randomizer.DoubleMin(Randomizer.DoubleMax((double) atkpower / (double) chr.getStat().getMDef(), 1.0), 0.25);
                }
                dam = (atkpower * defscale * (GameConstants.isDemon(chr.getJob()) ? 0.5 : 1));
                double resist = Randomizer.DoubleMax(chr.getStat().getResist(), 0.9);
                dam = Randomizer.Min((int) (dam - (dam * resist)), 1);
                if (chr.isGM() || chr.getLock()) {
                    damage = 1;
                } else {
                    damage = Randomizer.Min((int) (Randomizer.randomDouble(dam * 0.95, dam * 1.05)), 1);
                }
                if (attacker.getId() == 9990007) {
                    damage = 0;
                }
                if (attacker.isDeadly()) {
                    damage = chr.getStat().getCurrentMaxHp();
                }
                if (type != -1) {
                    final MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, type);
                    if (attackInfo != null) {
                        if (attackInfo.isElement && stats.TER > 0 && Randomizer.nextInt(100) < stats.TER) {
                            //System.out.println("Avoided ER from mob id: " + monsteridfrom);
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (attackInfo.isDeadlyAttack()) {
                            isDeadlyAttack = true;
                            //chr.dispel();
                        }
                        final MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
                        if (skill != null && damage > 0) {
                            //System.out.println("Skill ID: " + skill.getSkillId() + " - Skill Lvl: " + skill.getSkillLevel() + "        ");
                            if (skill.getSkillId() != 0 && skill.getSkillLevel() > 0) {
                                skill.applyEffect(chr, attacker, false);
                                if (!chr.canDamage()) {
                                    c.announce(CWvsContext.enableActions());
                                    return;
                                }
                            }
                        }
                        attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
                    }

                }
            }
            /*
             skillid = slea.readInt();
             pDMG = slea.readInt(); // we don't use this, incase packet edit..
             //System.out.println("pDMG: " + pDMG);
             final byte defType = slea.readByte();
             slea.skip(10); // ?
             if (defType == 1) { // Guard
             final Skill bx = SkillFactory.getSkill(31110008);
             final int bof = chr.getTotalSkillLevel(bx);
             if (bof > 0) {
             final MapleStatEffect eff = bx.getEffect(bof);
             if (Randomizer.nextInt(100) <= eff.getX()) { // estimate
             chr.handleForceGain(oid, 31110008, eff.getZ());
             }
             }
             }
             if (skillid != 0) {
             pPhysical = slea.readByte() > 0;
             pID = slea.readInt();
             pType = slea.readByte();
             slea.skip(4); // Mob position garbage
             pPos = slea.readPos();
             }
             */
        }
        if (damage == -1) {
            fake = 4020002 + ((chr.getJob() / 10 - 40) * 100000);
            if (fake != 4120002 && fake != 4220002) {
                fake = 4120002;
            }
            if (type == -1 && chr.getJob() == 122 && attacker != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10) != null) {
                if (chr.getTotalSkillLevel(1220006) > 0) {
                    final MapleStatEffect eff = SkillFactory.getSkill(1220006).getEffect(chr.getTotalSkillLevel(1220006));
                    attacker.applyStatus(chr, new MonsterStatusEffect(MonsterStatus.STUN, 1, 1220006, null, false), false, eff.getDuration(), true, eff);
                    fake = 1220006;
                }
            }
            if (chr.getTotalSkillLevel(fake) <= 0) {
                return;
            }
        } else if (damage < -1) {
            //AutobanManager.getInstance().addPoints(c, 1000, 60000, "Taking abnormal amounts of damge from " + monsteridfrom + ": " + damage);
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.getStat().dodgeChance > 0 && Randomizer.nextInt(100) < chr.getStat().dodgeChance) {
            c.announce(EffectPacket.showForeignEffect(20));
            return;
        }
        //chr.getCheatTracker().checkTakeDamage(damage);
        Pair<Double, Boolean> modify = chr.modifyDamageTaken((double) damage, attacker);
        int chance = Randomizer.random(1, 100);
        boolean crit = false;
        if (attacker != null) {
            int rate = 5;
            if (attacker.getStats().kaotic) {
                rate = 10;
            }
            if (attacker.getStats().ultimate) {
                rate = 25;
            }
            if (chance <= rate) {
                crit = true;
            }
        }
        damage = modify.left.intValue() * (crit ? 2 : 1);
        if (damage > 0 && !chr.getLock()) {
            int hploss = 0, mploss = 0;
            int realDamage = damage;
            //chr.getCheatTracker().setAttacksWithoutHit(false);

            if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
                chr.cancelMorphs();
            }
            if (!isDeadlyAttack && chr.getStat().mesoGuardMeso > 0) {
                //damage = (int) Math.ceil(damage * chr.getStat().mesoGuard / 100.0);
                //handled in client
                int mesoloss = (int) ((double) damage * ((double) chr.getStat().mesoGuardMeso / 100.0));
                if (mesoloss > stats.getHp()) {
                    mesoloss = stats.getHp();
                }
                if (chr.getMeso() <= 0) {
                    chr.cancelBuffStats(MapleBuffStat.MESOGUARD);
                } else {
                    if (chr.getMeso() < mesoloss) {
                        chr.gainMeso(-chr.getMeso(), false);
                        chr.cancelBuffStats(MapleBuffStat.MESOGUARD);
                        damage -= chr.getMeso();
                    } else {
                        chr.gainMeso(-mesoloss, false);
                        damage -= mesoloss;
                    }

                    if (damage < 1) {
                        damage = 1;
                    }
                }
            }

            if (chr.getBuffedValue(MapleBuffStat.INVINCIBLE) != null) {
                damage = (int) ((double) damage - ((double) damage * ((double) chr.getBuffedValue(MapleBuffStat.INVINCIBLE).doubleValue() / 100.0)));
            }
            if (chr.getTotalSkillLevel(1001003) > 0) {
                damage = (int) ((double) damage - ((double) damage * (((double) chr.getTotalSkillLevel(1001003) * 0.25) / 100.0)));
            }
            if (chr.getTotalSkillLevel(1220005) > 0) {
                damage = (int) ((double) damage - ((double) damage * (((double) (chr.getTotalSkillLevel(1220005) * 0.005) + 0.05) / 100.0)));
            }
            if (chr.getTotalSkillLevel(2001003) > 0) {
                damage = (int) ((double) damage - ((double) damage * (((double) chr.getTotalSkillLevel(2001003) * 0.25) / 100.0)));
            }
            if (chr.getTotalSkillLevel(11001001) > 0) {
                damage = (int) ((double) damage - ((double) damage * (((double) chr.getTotalSkillLevel(11001001) * 0.50) / 100.0)));
            }
            if (chr.getTotalSkillLevel(12001002) > 0) {
                damage = (int) ((double) damage - ((double) damage * (((double) chr.getTotalSkillLevel(12001002) * 0.50) / 100.0)));
            }
            int realHPLoss = damage;
            if (damage > 1) {
                if (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null) {
                    double MGuard = (chr.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue()) / 100.0;
                    mploss = (int) (damage * MGuard);
                    if (mploss > stats.getMp()) {
                        mploss = stats.getMp();
                    }
                    hploss = damage - mploss;
                } else {
                    hploss = damage;
                }
            }
            if (isDeadlyAttack) {
                chr.addMPHP(-(chr.getStat().getMaxHp() - 1), -(chr.getStat().getMaxMp() - 1));
                chr.getClient().announce(CWvsContext.damagePlayer(chr, (chr.getStat().getMaxHp() - 1)));
                chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), (chr.getStat().getMaxHp() - 1), true), false);
            } else {
                chr.addMPHP(-hploss, -mploss);
                chr.getClient().announce(CWvsContext.damagePlayer(chr, damage));
                chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), damage, crit), false);
            }
            chr.setDamage(System.currentTimeMillis() + 2000);
            if (!chr.isAlive() && chr.itemQuantity(4420054) > 0) {
                chr.gainItem(4420054, -1);
                chr.Heal();
                chr.changeMap(chr.getMap());
                chr.getClient().announce(EffectPacket.showForeignEffect(0));
                chr.dropTopMessage("You have been auto-revived!");
                chr.portalDelay(5000);
            }
        }

        /*
         byte offset = 0;
         int offset_d = 0;
         slea.skip(10); // ?
         if (slea.available() == 1) {
         offset = slea.readByte();
         System.out.println("offset: " + offset);
         if (offset == 1 && slea.available() >= 4) {
         offset_d = slea.readInt();
         }
         if (offset < 0 || offset > 2) {
         offset = 0;
         }
         }
         */
        //chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), type, damage, monsteridfrom, direction, skillid, pDMG, pPhysical, pID, pType, pPos, offset, offset_d, fake), true);
    }

    public static final void AranCombo(final MapleClient c, final MapleCharacter chr, int toAdd) {
        if (chr != null && chr.isAlive() && chr.canCombo) {
            int max = Randomizer.Max((int) chr.getMaxCombo(), 99999);
            int combo = Randomizer.Max(chr.getCombo() + toAdd, max);
            chr.setLastCombo(System.currentTimeMillis() + 4000);
            chr.setCombo(combo);
            c.announce(CField.testCombo(combo));
            chr.updateStats();
        }
    }

    public static final void UseItemEffect(final int itemId, final MapleClient c, final MapleCharacter chr) {
        final Item toUse = chr.getInventory(MapleInventoryType.CASH).findById(itemId);
        if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (itemId != 5510000) {
            chr.setItemEffect(itemId);
        }
        chr.getMap().broadcastMessage(chr, CField.itemEffect(chr.getId(), itemId), false);
    }

    public static final void CancelItemEffect(final int id, final MapleCharacter chr) {
        chr.cancelEffect(
                MapleItemInformationProvider.getInstance().getItemEffect(-id), false, -1);
    }

    public static final void CancelBuffHandler(final int sourceid, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final Skill skill = SkillFactory.getSkill(sourceid);

        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0);
            chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
        } else {
            chr.cancelEffect(skill.getEffect(1), false, -1);
        }
    }

    public static final void CancelMech(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        int sourceid = slea.readInt();
        if (sourceid % 10000 < 1000 && SkillFactory.getSkill(sourceid) == null) {
            sourceid += 1000;
        }
        final Skill skill = SkillFactory.getSkill(sourceid);
        if (skill == null) { //not sure
            return;
        }
        if (skill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0);
            chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
        } else {
            chr.cancelEffect(skill.getEffect(slea.readByte()), false, -1);
        }
    }

    public static final void QuickSlot(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        if (slea.available() == 32 && chr != null) {
            final StringBuilder ret = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                ret.append(slea.readInt()).append(",");
            }
            ret.deleteCharAt(ret.length() - 1);
            chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.QUICK_SLOT)).setCustomData(ret.toString());
        }
    }

    public static final void SkillEffect(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {

        final int skillId = slea.readInt();
        if (skillId >= 91000000) { //guild/recipe? no
            chr.getClient().announce(CWvsContext.enableActions());
            return;
        }
        final byte level = slea.readByte();
        final short direction = slea.readShort();
        final byte unk = slea.readByte(); // Added on v.82

        final Skill skill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(skillId));
        if (chr == null || skill == null || chr.getMap() == null) {
            return;
        }
        final int skilllevel_serv = chr.getTotalSkillLevel(skill);

        if (skilllevel_serv > 0 && skilllevel_serv == level && (skillId == 33101005 || skill.isChargeSkill())) {
            chr.setKeyDownSkill_Time(System.currentTimeMillis());
            if (skillId == 33101005) {
                chr.setLinkMid(slea.readInt(), 0);
            }
            chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, skillId, level, direction, unk), false);
        }
    }

    public static final void SpecialMove(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.hasBlockedInventory() || chr.getMap() == null || slea.available() < 9) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        slea.skip(4); // Old X and Y
        int skillid = slea.readInt();
        if (skillid >= 91000000) { //guild/recipe? no
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (skillid == 23111008) { //spirits, hack
            skillid += Randomizer.nextInt(2);
        }
        int skillLevel = slea.readByte();
        final Skill skill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(skillid));
        final int skillLevels = chr.getTotalSkillLevel(skill);
        if (skill == null || (GameConstants.isAngel(skillid) && (chr.getStat().equippedSummon % 10000) != (skillid % 10000)) || (chr.inPVP() && skill.isPVPDisabled())) {
            c.announce(CWvsContext.enableActions());
            return;
        }

        if (skillLevels <= 0 || skillLevels != skillLevel) {
            if (!GameConstants.isMulungSkill(skillid) && !GameConstants.isPyramidSkill(skillid) && skillLevels <= 0) {
                c.getSession().close();
                return;
            }
            if (GameConstants.isMulungSkill(skillid)) {
                if (chr.getMapId() / 10000 != 92502) {
                    return;
                } else {
                    if (chr.getMulungEnergy() < 10000) {
                        return;
                    }
                    chr.mulung_EnergyModify(false);
                }
            } else if (GameConstants.isPyramidSkill(skillid)) {
                if (chr.getMapId() / 10000 != 92602 && chr.getMapId() / 10000 != 92601) {
                    return;
                }
            }
        }
        if (GameConstants.isEventMap(chr.getMapId())) {
            for (MapleEventType t : MapleEventType.values()) {
                final MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                if (e.isRunning() && !chr.isGM()) {
                    for (int i : e.getType().mapids) {
                        if (chr.getMapId() == i) {
                            chr.dropMessage(5, "You may not use that here.");
                            return; //non-skill cannot use
                        }
                    }
                }
            }
        }
        final MapleStatEffect effect = chr.inPVP() ? skill.getPVPEffect(skillLevels) : skill.getEffect(skillLevels);
        if (effect.isMPRecovery() && chr.getStat().getHp() < (chr.getStat().getMaxHp() / 100) * 10) { //less than 10% hp
            chr.dropMessage(5, "You do not have the HP to use this skill.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (effect.getCooldown(chr) > 0 && !chr.isGM()) {
            if (chr.skillisCooling(skillid)) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (skillid != 5221006 && skillid != 35111002) { // Battleship
                c.announce(CField.skillCooldown(skillid, effect.getCooldown(chr)));
                chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown(chr));
            }
        }
        //chr.checkFollow(); //not msea-like but ALEX'S WISHES
        switch (skillid) {
            case 1121001:
            case 1221001:
            case 1321001:
            case 9001020: // GM magnet
            case 9101020:
                //case 31111003:
                final byte number_of_mobs = slea.readByte();
                slea.skip(3);
                for (int i = 0; i < number_of_mobs; i++) {
                    int mobId = slea.readInt();

                    final MapleMonster mob = chr.getMap().getMonsterByOid(mobId);
                    if (mob != null) {
//			chr.getMap().broadcastMessage(chr, CField.showMagnet(mobId, slea.readByte()), chr.getTruePosition());
                        mob.updateMonsterController();
                        mob.applyStatus(chr, new MonsterStatusEffect(MonsterStatus.STUN, 1, skillid, null, false), false, effect.getDuration(), true, effect);
                    }
                }
                chr.getMap().broadcastMessage(chr, EffectPacket.showBuffeffect(chr.getId(), skillid, 1, chr.getLevel(), skillLevels, slea.readByte()), chr.getTruePosition());
                c.announce(CWvsContext.enableActions());
                break;
            case 30001061: //capture
                int mobID = slea.readInt();
                MapleMonster mob = chr.getMap().getMonsterByOid(mobID);
                if (mob != null) {
                    boolean success = mob.getHp() <= mob.getMobMaxHp() / 2 && mob.getId() >= 9304000 && mob.getId() < 9305000;
                    chr.getMap().broadcastMessage(chr, EffectPacket.showBuffeffect(chr.getId(), skillid, 1, chr.getLevel(), skillLevels, (byte) (success ? 1 : 0)), chr.getTruePosition());
                    if (success) {
                        chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAGUAR)).setCustomData(String.valueOf(90));
                        chr.getMap().killMonster(mob, chr, true, false, (byte) 1);
                        chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                        c.announce(CWvsContext.updateJaguar(chr));
                    } else {
                        chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
                    }
                }
                c.announce(CWvsContext.enableActions());
                break;
            case 30001062: //hunter call
                chr.dropMessage(5, "No monsters can be summoned. Capture a monster first."); //lool
                c.announce(CWvsContext.enableActions());
                break;
            case 33101005: //jaguar oshi
                mobID = chr.getFirstLinkMid();
                mob = chr.getMap().getMonsterByOid(mobID);
                chr.setKeyDownSkill_Time(0);
                chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
                if (mob != null) {
                    boolean success = mob.getStats().getLevel() < chr.getLevel() && mob.getId() < 9000000 && !mob.getStats().isBoss();
                    if (success) {
                        chr.getMap().broadcastMessage(MobPacket.suckMonster(mob.getObjectId(), chr.getId()));
                        chr.getMap().killMonster(mob, chr, false, false, (byte) -1);
                    } else {
                        chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
                    }
                } else {
                    chr.dropMessage(5, "No monster was sucked. The skill failed.");
                }
                c.announce(CWvsContext.enableActions());
                break;
            case 4341003: //monster bomb
                chr.setKeyDownSkill_Time(0);
                chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
            //fallthrough intended
            default:
                Point pos = null;
                if (slea.available() == 5 || slea.available() == 7) {
                    pos = slea.readPos();
                }
                if (effect.isMagicDoor()) { // Mystic Door
                    if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
                        effect.applyTo(chr, pos);
                    } else {
                        c.announce(CWvsContext.enableActions());
                    }
                } else {
                    final int mountid = MapleStatEffect.parseMountInfo(chr, skill.getId());
                    if (mountid != 0 && mountid != GameConstants.getMountItem(skill.getId(), chr) && !chr.isIntern() && chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -122) == null) {
                        if (!GameConstants.isMountItemAvailable(mountid, chr.getJob())) {
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                    }
                    effect.applyTo(chr, pos);
                }
                break;
        }

    }

    public static final void closeRangeAttack(final SeekableLittleEndianAccessor slea, final MapleClient c, final boolean energy) {
        if (c == null) {
            return;
        }
        final MapleCharacter chr = c.getPlayer();
        if (chr == null || (energy && chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null && chr.getBuffedValue(MapleBuffStat.BODY_PRESSURE) == null && chr.getBuffedValue(MapleBuffStat.DARK_AURA) == null && chr.getBuffedValue(MapleBuffStat.TORNADO) == null && chr.getBuffedValue(MapleBuffStat.RAINING_MINES) == null)) {
            return;
        }
        if (chr.hasBlockedInventory() || chr.getMap() == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        AttackInfo attack = DamageParse.parseDmgM(slea, chr);
        if (attack == null) {
            c.announce(CWvsContext.enableActions());
            return;
        } else {
            if (attack.skill > 0) {
                chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, attack.skill, (byte) attack.level, (short) attack.display, (byte) attack.unk), false);
            } else {
                chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, 2321007, (byte) 250, (short) attack.display, (byte) attack.unk), false);
            }
        }
        final boolean mirror = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
        double maxdamage = chr.getStat().getCurrentMaxBaseDamage();
        final Item shield = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        int attackCount = (shield != null && shield.getItemId() / 10000 == 134 ? 2 : 1);
        int skillLevel = 0;
        MapleStatEffect effect = null;
        Skill skill = attack.baseSkill;
        if (skill != null) {
            skillLevel = attack.level;

            if ((GameConstants.isAngel(attack.skill) && (chr.getStat().equippedSummon % 10000) != (attack.skill % 10000))) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            effect = attack.getAttackEffect(chr, skillLevel, skill);
            if (effect == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (GameConstants.isEventMap(chr.getMapId())) {
                for (MapleEventType t : MapleEventType.values()) {
                    final MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                    if (e.isRunning() && !chr.isGM()) {
                        for (int i : e.getType().mapids) {
                            if (chr.getMapId() == i) {
                                chr.dropMessage(5, "You may not use that here.");
                                c.announce(CWvsContext.enableActions());
                                return; //non-skill cannot use
                            }
                        }
                    }
                }
            }
            maxdamage *= (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skill)) / 100.0;
            attackCount = effect.getAttackCount();

            if (effect.getCooldown(chr) > 0 && !chr.isGM() && !energy) {
                if (chr.skillisCooling(attack.skill)) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                c.announce(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
                chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(chr));
            }
        } else {
            if (attack.skill != 0) {
                c.announce(CWvsContext.enableActions());
                return;
            }
        }
        attackCount *= (mirror ? 2 : 1);
        if (!energy) {
            if ((chr.getMapId() == 109060000 || chr.getMapId() == 109060002 || chr.getMapId() == 109060004) && attack.skill == 0) {
                MapleSnowballs.hitSnowball(chr);
            }
            // handle combo orbconsume
            int numFinisherOrbs = 0;
            final Integer comboBuff = chr.getBuffedValue(MapleBuffStat.COMBO);

            if (isFinisher(attack.skill) > 0) { // finisher
                if (comboBuff != null) {
                    numFinisherOrbs = comboBuff.intValue() - 1;
                }
                if (numFinisherOrbs <= 0) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                chr.handleOrbconsume(isFinisher(attack.skill));
                if (!GameConstants.GMS) {
                    maxdamage *= numFinisherOrbs;
                }
            }
        }
        chr.checkFollow();
        if (!attack.allDamage.isEmpty() || !attack.allBigDamage.isEmpty()) {
            int skin = chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin();
            if (!chr.isHidden()) {
                //chr.getMap().broadcastSkill(chr, CField.closeRangeAttack(chr.getId(), (byte) 0, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, energy, chr.getLevel(), chr.getStat().passive_mastery(), attack.unk, attack.charge), true);
                if (chr.getEffects()) {
                    chr.getMap().broadcastSkill(chr, CField.customShowDamage(chr, attack, attack.allBigDamage, attack.direction, skin), true);
                }
            } else {
                //chr.getMap().broadcastGMMessage(chr, CField.closeRangeAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, energy, chr.getLevel(), chr.getStat().passive_mastery(), attack.unk, attack.charge), false);
                if (chr.getEffects()) {
                    chr.getMap().broadcastGMMessage(chr, CField.customShowDamage(chr, attack, attack.allBigDamage, attack.direction, skin), true);
                }
            }
        }
        DamageParse.applyAttack(attack, skill, chr, attackCount, maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
        /*
         WeakReference<MapleCharacter>[] clones = chr.getClones();
         for (int i = 0; i < clones.length; i++) {
         if (clones[i].get() != null) {
         final MapleCharacter clone = clones[i].get();
         final Skill skil2 = skill;
         final int skillLevel2 = skillLevel;
         final int attackCount2 = attackCount;
         final double maxdamage2 = maxdamage;
         final MapleStatEffect eff2 = effect;
         final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
         CloneTimer.getInstance().schedule(new Runnable() {

         @Override
         public void run() {
         if (!clone.isHidden()) {
         clone.getMap().broadcastMessage(CField.closeRangeAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, attack2.allDamage, energy, clone.getLevel(), clone.getStat().passive_mastery(), attack2.unk, attack2.charge));
         } else {
         clone.getMap().broadcastGMMessage(clone, CField.closeRangeAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, attack2.allDamage, energy, clone.getLevel(), clone.getStat().passive_mastery(), attack2.unk, attack2.charge), false);
         }
         DamageParse.applyAttack(attack2, skil2, chr, attackCount2, maxdamage2, eff2, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
         }
         }, 500 * i + 500);
         }
         }
         */
    }

    public static final void rangedAttack(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {

            return;
        }
        final MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }

        if (chr.hasBlockedInventory() || chr.getMap() == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        AttackInfo attack = DamageParse.parseDmgR(slea, chr);
        if (attack == null) {
            c.announce(CWvsContext.enableActions());
            return;
        } else {
            if (attack.skill > 0) {
                chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, attack.skill, (byte) attack.level, (short) attack.display, (byte) attack.unk), false);
            } else {
                chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, 2321007, (byte) 250, (short) attack.display, (byte) attack.unk), false);
            }
        }
        int bulletCount = 1, skillLevel = 1;
        MapleStatEffect effect = null;
        Skill skill = attack.baseSkill;
        boolean AOE = attack.skill == 4111004;
        boolean noBullet = (chr.getJob() >= 3500 && chr.getJob() <= 3512) || GameConstants.isCannon(chr.getJob()) || GameConstants.isMercedes(chr.getJob());
        if (attack.skill != 0 && skill != null) {
            if ((GameConstants.isAngel(attack.skill) && (chr.getStat().equippedSummon % 10000) != (attack.skill % 10000))) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            skillLevel = attack.level;
            effect = attack.getAttackEffect(chr, skillLevel, skill);
            if (effect == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (GameConstants.isEventMap(chr.getMapId())) {
                for (MapleEventType t : MapleEventType.values()) {
                    final MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                    if (e.isRunning() && !chr.isGM()) {
                        for (int i : e.getType().mapids) {
                            if (chr.getMapId() == i) {
                                chr.dropMessage(5, "You may not use that here.");
                                c.announce(CWvsContext.enableActions());
                                return; //non-skill cannot use
                            }
                        }
                    }
                }
            }
            if (effect.getMobCount() > 1) {
                AOE = true;
            }
            if (effect.getBulletCount() > 0) {
                bulletCount *= effect.getBulletCount();
            }
            if (effect.getAttackCount() > 0) {
                bulletCount *= effect.getAttackCount();
            }
            if (effect.getCooldown(chr) > 0 && !chr.isGM() && ((attack.skill != 35111004 && attack.skill != 35121013) || chr.getBuffSource(MapleBuffStat.MECH_CHANGE) != attack.skill)) {
                if (chr.skillisCooling(attack.skill)) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                c.announce(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
                chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(chr));
            }
        } else {
            if (attack.skill != 0) {
                c.announce(CWvsContext.enableActions());
                return;
            }
        }

        final Integer ShadowPartner = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER);
        if (ShadowPartner != null) {
            bulletCount *= 2;
        }
        int projectile = 0, visProjectile = 0;

        if (!AOE && chr.getBuffedValue(MapleBuffStat.SOULARROW) == null && !noBullet) {
            Item ipp = chr.getInventory(MapleInventoryType.USE).getItem(attack.slot);
            if (ipp == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            projectile = ipp.getItemId();

            if (attack.csstar > 0) {
                if (chr.getInventory(MapleInventoryType.CASH).getItem(attack.csstar) == null) {
                    c.announce(CWvsContext.enableActions());
                    return;
                }
                visProjectile = chr.getInventory(MapleInventoryType.CASH).getItem(attack.csstar).getItemId();
            } else {
                visProjectile = projectile;
            }
            // Handle bulletcount
            if (chr.getBuffedValue(MapleBuffStat.SPIRIT_CLAW) == null) {
                int bulletConsume = bulletCount;
                if (effect != null && effect.getBulletConsume() != 0) {
                    bulletConsume = effect.getBulletConsume() * (ShadowPartner != null ? 2 : 1);
                }
                if (chr.getJob() == 412 && bulletConsume > 0 && ipp.getQuantity() < MapleItemInformationProvider.getInstance().getSlotMax(projectile)) {
                    final Skill expert = SkillFactory.getSkill(4120010);
                    if (chr.getTotalSkillLevel(expert) > 0) {
                        final MapleStatEffect eff = expert.getEffect(chr.getTotalSkillLevel(expert));
                        if (eff.makeChanceResult()) {
                            ipp.setQuantity((short) (ipp.getQuantity() + 1));
                            c.announce(InventoryPacket.updateInventorySlot(MapleInventoryType.USE, ipp, false));
                            bulletConsume = 0; //regain a star after using
                            c.announce(InventoryPacket.getInventoryStatus());
                        }
                    }
                }
                if (bulletConsume > 0) {
                    if (!MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true)) {
                        chr.dropMessage(5, "You do not have enough arrows/bullets/stars.");
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                }
            }
        } else if (chr.getJob() >= 3500 && chr.getJob() <= 3512) {
            visProjectile = 2333000;
        } else if (GameConstants.isCannon(chr.getJob())) {
            visProjectile = 2333001;
        }
        double basedamage = 1.0;
        int projectileWatk = 0;
        if (projectile != 0) {
            projectileWatk = MapleItemInformationProvider.getInstance().getWatkForProjectile(projectile);
        }
        final PlayerStats statst = chr.getStat();
        if (effect != null) {
            int money = effect.getMoneyCon();
            if (money != 0) {
                if (money > chr.getMeso()) {
                    money = chr.getMeso();
                }
                chr.gainMeso(-money, false);
            }
        }
        chr.checkFollow();
        if (!attack.allDamage.isEmpty() || !attack.allBigDamage.isEmpty()) {
            int skin = chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin();
            if (!chr.isHidden()) {
                if (chr.getEffects()) {
                    chr.getMap().broadcastSkill(chr, CField.customShowDamage(chr, attack, attack.allBigDamage, attack.direction, skin), true);
                }
            } else {
                if (chr.getEffects()) {
                    chr.getMap().broadcastGMMessage(chr, CField.customShowDamage(chr, attack, attack.allBigDamage, attack.direction, skin), true);
                }
            }
        }
        DamageParse.applyAttack(attack, skill, chr, bulletCount, basedamage, effect, ShadowPartner != null ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED);
        /*
         WeakReference<MapleCharacter>[] clones = chr.getClones();

         for (int i = 0; i < clones.length; i++) {
         if (clones[i].get() != null) {
         final MapleCharacter clone = clones[i].get();
         final Skill skil2 = skill;
         final MapleStatEffect eff2 = effect;
         final double basedamage2 = basedamage;
         final int bulletCount2 = bulletCount;
         final int visProjectile2 = visProjectile;
         final int skillLevel2 = skillLevel;
         final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
         CloneTimer.getInstance().schedule(new Runnable() {

         @Override
         public void run() {
         if (!clone.isHidden()) {
         if (attack2.skill == 3111006 || attack2.skill == 3211006) {
         clone.getMap().broadcastMessage(CField.strafeAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().passive_mastery(), attack2.unk, chr.getTotalSkillLevel(3220010)));
         } else {
         clone.getMap().broadcastMessage(CField.rangedAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().passive_mastery(), attack2.unk));
         }
         } else {
         if (attack2.skill == 3111006 || attack2.skill == 3211006) {
         clone.getMap().broadcastGMMessage(clone, CField.strafeAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().passive_mastery(), attack2.unk, chr.getTotalSkillLevel(3220010)), false);
         } else {
         clone.getMap().broadcastGMMessage(clone, CField.rangedAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().passive_mastery(), attack2.unk), false);
         }
         }
         DamageParse.applyAttack(attack2, skil2, chr, bulletCount2, basedamage2, eff2, AttackType.RANGED);
         }
         }, 500 * i + 500);
         }
         }
         */
    }

    public static final void MagicDamage(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        final MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.hasBlockedInventory() || chr.getMap() == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        AttackInfo attack = DamageParse.parseDmgMa(slea, chr);

        if (attack == null) {
            c.announce(CWvsContext.enableActions());
            return;
        } else {
            if (attack.skill > 0) {
                chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, attack.skill, (byte) attack.level, (short) attack.display, (byte) attack.unk), false);
            } else {
                chr.getMap().broadcastSkill(chr, CField.skillEffect(chr, 2321007, (byte) 250, (short) attack.display, (byte) attack.unk), false);
            }
        }
        final Skill skill = attack.baseSkill;
        if (skill == null || (GameConstants.isAngel(attack.skill) && (chr.getStat().equippedSummon % 10000) != (attack.skill % 10000))) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        final int skillLevel = attack.level;
        final MapleStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
        if (effect == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (GameConstants.isEventMap(chr.getMapId())) {
            for (MapleEventType t : MapleEventType.values()) {
                final MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                if (e.isRunning() && !chr.isGM()) {
                    for (int i : e.getType().mapids) {
                        if (chr.getMapId() == i) {
                            chr.dropMessage(5, "You may not use that here.");
                            c.announce(CWvsContext.enableActions());
                            return; //non-skill cannot use
                        }
                    }
                }
            }
        }
        double maxdamage = chr.getStat().getCurrentMaxBaseDamage() * (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skill)) / 100.0;
        if (GameConstants.isPyramidSkill(attack.skill)) {
            maxdamage = 1;
        } else if (GameConstants.isBeginnerJob(skill.getId() / 10000) && skill.getId() % 10000 == 1000) {
            maxdamage = 40;
        }
        if (effect.getCooldown(chr) > 0 && !chr.isGM()) {
            if (chr.skillisCooling(attack.skill)) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            c.announce(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
            chr.addCooldown(attack.skill, System.currentTimeMillis(), effect.getCooldown(chr));
        }
        chr.checkFollow();
        if (!attack.allDamage.isEmpty() || !attack.allBigDamage.isEmpty()) {
            int skin = chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin();
            if (!chr.isHidden()) {
                //chr.getMap().broadcastSkill(chr, CField.magicAttack(chr.getId(), (byte) 0, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk), true);
                if (chr.getEffects()) {
                    chr.getMap().broadcastSkill(chr, CField.customShowDamage(chr, attack, attack.allBigDamage, attack.direction, skin), true);
                }
            } else {
                //chr.getMap().broadcastGMMessage(chr, CField.magicAttack(chr.getId(), attack.tbyte, attack.skill, skillLevel, attack.display, attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk), false);
                if (chr.getEffects()) {
                    chr.getMap().broadcastGMMessage(chr, CField.customShowDamage(chr, attack, attack.allBigDamage, attack.direction, skin), true);
                }
            }
        }
        DamageParse.applyAttackMagic(attack, skill, chr, effect, maxdamage);
        /*
         WeakReference<MapleCharacter>[] clones = chr.getClones();
         for (int i = 0; i < clones.length; i++) {
         if (clones[i].get() != null) {
         final MapleCharacter clone = clones[i].get();
         final Skill skil2 = skill;
         final MapleStatEffect eff2 = effect;
         final double maxd = maxdamage;
         final int skillLevel2 = skillLevel;
         final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
         CloneTimer.getInstance().schedule(new Runnable() {

         @Override
         public void run() {
         if (!clone.isHidden()) {
         clone.getMap().broadcastMessage(CField.magicAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, attack2.allDamage, attack2.charge, clone.getLevel(), attack2.unk));
         } else {
         clone.getMap().broadcastGMMessage(clone, CField.magicAttack(clone.getId(), attack2.tbyte, attack2.skill, skillLevel2, attack2.display, attack2.speed, attack2.allDamage, attack2.charge, clone.getLevel(), attack2.unk), false);
         }
         DamageParse.applyAttackMagic(attack2, skil2, chr, eff2, maxd);
         }
         }, 500 * i + 500);
         }
         }
         */
    }

    public static final void DropMeso(final int meso, final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        if (!chr.isAlive() || (meso < 10 || meso > 50000) || (meso > chr.getMeso())) {
            chr.getClient().announce(CWvsContext.enableActions());
            return;
        }
        if (chr.getMap().getItemsSize() > 250) {
            chr.dropMessage(1, "Too many mesos on map");
            chr.getClient().announce(CWvsContext.enableActions());
            return;
        }
        chr.gainMeso(-meso, false, true);
        chr.getMap().spawnMesoDrop(meso, chr.getTruePosition(), chr, chr.getId(), true, (byte) 0);
        //chr.getCheatTracker().checkDrop(true);
    }

    public static final void ChangeAndroidEmotion(final int emote, final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        if (emote > 0 && chr.getMap() != null && !chr.isHidden() && emote <= 17 && chr.getAndroid() != null) { //O_o
            chr.getMap().broadcastMessage(CField.showAndroidEmotion(chr.getId(), emote));
        }
    }

    public static final void MoveAndroid(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || chr.getAndroid() == null) {
            return;
        }
        slea.skip(8);
        final List<LifeMovementFragment> res = MovementParse.parseMovement(chr, slea, 1);

        if (res != null && !res.isEmpty() && chr.getMap() != null && chr.getAndroid() != null) { // map crash hack
            final Point pos = new Point(chr.getAndroid().getPos() != null ? chr.getAndroid().getPos() : chr.getPosition());
            //chr.getMap().mesoLoot(chr, pos);
            chr.getAndroid().updatePosition(res);
            chr.getMap().broadcastMessage(chr, CField.moveAndroid(chr.getId(), pos, res), false);
        }
    }

    public static final void ChangeEmotion(final int emote, final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        if (emote > 7) {
            final int emoteid = 5159992 + emote;
            final MapleInventoryType type = GameConstants.getInventoryType(emoteid);
            if (chr.getInventory(type).findById(emoteid) == null) {
                //chr.getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(emoteid));
                return;
            }
        }
        if (emote > 0 && chr.getMap() != null && !chr.isHidden()) { //O_o
            chr.getMap().broadcastMessage(chr, CField.facialExpression(chr, emote), false);
            WeakReference<MapleCharacter>[] clones = chr.getClones();
            for (int i = 0; i < clones.length; i++) {
                if (clones[i].get() != null) {
                    final MapleCharacter clone = clones[i].get();
                    CloneTimer.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            clone.getMap().broadcastMessage(CField.facialExpression(clone, emote));
                        }
                    }, 500 * i + 500);
                }
            }
        }
    }

    public static final void Heal(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        chr.updateTick(slea.readInt());
        if (slea.available() >= 8) {
            slea.skip(slea.available() >= 12 && GameConstants.GMS ? 8 : 4);
        }
        int healHP = slea.readShort();
        int healMP = slea.readShort();

        final PlayerStats stats = chr.getStat();

        if (stats.getHp() <= 0) {
            return;
        }
        final long now = System.currentTimeMillis();
        if (healHP != 0 && chr.canHP(now + 1000)) {
            if (healHP > stats.getHealHP()) {
                //chr.getCheatTracker().registerOffense(CheatingOffense.REGEN_HIGH_HP, String.valueOf(healHP));
                healHP = (int) stats.getHealHP();
            }
            chr.addHP(healHP);
        }
        if (healMP != 0 && !GameConstants.isDemon(chr.getJob()) && chr.canMP(now + 1000)) { //just for lag
            if (healMP > stats.getHealMP()) {
                //chr.getCheatTracker().registerOffense(CheatingOffense.REGEN_HIGH_MP, String.valueOf(healMP));
                healMP = (int) stats.getHealMP();
            }
            chr.addMP(healMP);
        }
    }

    public static final void MovePlayer(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        //System.out.println("Move Player " + slea.toString());
        slea.skip(1); // portal count
        slea.skip(4); // crc?
        slea.skip(4); // tickcount
        slea.skip(4); // position
        slea.skip(4);
        final Point Original_Pos = chr.getPosition(); // 4 bytes Added on v.80 MSEA
        List<LifeMovementFragment> res;
        try {
            res = MovementParse.parseMovement(chr, slea, 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            c.announce(CWvsContext.enableActions());
            System.out.println("AIOBE Type1:\n" + slea.toString(true));
            return;
        }

        if (res != null && chr.getMap() != null) {
            if (slea.available() < 11 || slea.available() > 26) { // estimation, should be exact 18
                c.announce(CWvsContext.enableActions());
                return;
            }
            final MapleMap map = chr.getMap();

            MovementParse.updatePosition(res, chr, 0);
            if (chr.isHidden()) {
                chr.setLastRes(res);
                chr.getMap().broadcastGMMessage(chr, CField.movePlayer(chr.getId(), res, Original_Pos), false);
            } else {
                chr.getMap().broadcastMessage(chr, CField.movePlayer(chr.getId(), res, Original_Pos), false);
            }
            final Point pos = chr.getTruePosition();
            map.movePlayer(chr, pos);
            if (chr.getLoot()) {
                chr.itemVac();
            }

            if (chr.getFollowId() > 0 && chr.isFollowOn() && chr.isFollowInitiator()) {
                final MapleCharacter fol = map.getCharacterById(chr.getFollowId());
                if (fol != null) {
                    final Point original_pos = fol.getPosition();
                    fol.getClient().announce(CField.moveFollow(Original_Pos, original_pos, pos, res));
                    MovementParse.updatePosition(res, fol, 0);
                    map.movePlayer(fol, pos);
                    map.broadcastMessage(fol, CField.movePlayer(fol.getId(), res, original_pos), false);
                } else {
                    chr.checkFollow();
                }
            }
            chr.setOldPosition(pos);
            if (chr.getPosition().getY() > (map.getBottom() + 1000)) {
                chr.respawn();
            }
        }
    }

    public static final void ChangeMapSpecial(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        slea.readByte();
        String startwp = slea.readMapleAsciiString();
        slea.readShort();
        MaplePortal portal = chr.getMap().getPortal(startwp);

        if (portal == null || chr.portalDelay() > System.currentTimeMillis()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.isChangingMaps()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.getTrade() != null) {
            chr.cancelTrade();
        }
        if (c.getChat()) {
            return;
        }
        portal.enterPortal(c);
    }

    public static final void ChangeMap(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {//kaotic
        if (chr == null || chr.getMap() == null) {
            return;
        }
        if (chr.portalDelay() > System.currentTimeMillis()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.isChangingMaps()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (slea.available() != 0) {
            //slea.skip(6); //D3 75 00 00 00 00
            slea.readByte(); // 1 = from dying 2 = regular portals
            int targetid = slea.readInt(); // FF FF FF FF
            if (GameConstants.GMS) { //todo jump?
                slea.readInt();
            }
            final MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
            if (slea.available() >= 7) {
                chr.updateTick(slea.readInt());
            }
            slea.skip(1);
            final boolean wheel = slea.readShort() > 0 && !GameConstants.isEventMap(chr.getMapId()) && chr.haveItem(5510000, 1, false, true) && chr.getMapId() / 1000000 != 925;

            if (targetid != -1 && !chr.isAlive()) {
                chr.setStance(0);
                if (chr.getEventInstance() != null) {
                    if (chr.isAlive()) {
                        return;
                    } else {
                        chr.getEventInstance().revivePlayer(chr);
                        chr.eventChangeMap = false;
                    }
                    return;
                }
                if (chr.getPyramidSubway() != null) {
                    chr.getStat().setHp((short) 50, chr);
                    chr.getPyramidSubway().fail(chr);
                    return;
                }

                if (!wheel) {
                    chr.getStat().setHp((short) 50, chr);

                    final MapleMap to = chr.getMap().getReturnMap();
                    chr.changeMap(to, to.getPortal(0));
                } else {
                    c.announce(EffectPacket.useWheel((byte) (chr.getInventory(MapleInventoryType.CASH).countById(5510000) - 1)));
                    chr.getStat().setHp(((chr.getStat().getMaxHp() / 100) * 40), chr);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);

                    final MapleMap to = chr.getMap();
                    chr.changeMap(to, to.getPortal(0));
                }
            } else if (targetid != -1 && chr.isIntern()) {
                final MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                if (to != null) {
                    chr.changeMap(to, to.getPortal(0));
                } else {
                    chr.dropMessage(5, "Map is NULL. Use !warp <mapid> instead.");
                }
            } else if (targetid != -1 && !chr.isIntern()) {

                final int divi = chr.getMapId() / 100;
                boolean unlock = false, warp = false;
                if (divi == 9130401) { // Only allow warp if player is already in Intro map, or else = hack
                    warp = targetid / 100 == 9130400 || targetid / 100 == 9130401; // Cygnus introduction
                    if (targetid / 10000 != 91304) {
                        warp = true;
                        unlock = true;
                        targetid = 130030000;
                    }
                } else if (divi == 9130400) { // Only allow warp if player is already in Intro map, or else = hack
                    warp = targetid / 100 == 9130400 || targetid / 100 == 9130401; // Cygnus introduction
                    if (targetid / 10000 != 91304) {
                        warp = true;
                        unlock = true;
                        targetid = 130030000;
                    }
                } else if (divi == 9140900) { // Aran Introductio
                    warp = targetid == 914090011 || targetid == 914090012 || targetid == 914090013 || targetid == 140090000;
                } else if (divi == 9120601 || divi == 9140602 || divi == 9140603 || divi == 9140604 || divi == 9140605) {
                    warp = targetid == 912060100 || targetid == 912060200 || targetid == 912060300 || targetid == 912060400 || targetid == 912060500 || targetid == 3000100;
                    unlock = true;
                } else if (divi == 9101500) {
                    warp = targetid == 910150006 || targetid == 101050010;
                    unlock = true;
                } else if (divi == 9140901 && targetid == 140000000) {
                    unlock = true;
                    warp = true;
                } else if (divi == 9240200 && targetid == 924020000) {
                    unlock = true;
                    warp = true;
                } else if (targetid == 980040000 && divi >= 9800410 && divi <= 9800450) {
                    warp = true;
                } else if (divi == 9140902 && (targetid == 140030000 || targetid == 140000000)) { //thing is. dont really know which one!
                    unlock = true;
                    warp = true;
                } else if (divi == 9000900 && targetid / 100 == 9000900 && targetid > chr.getMapId()) {
                    warp = true;
                } else if (divi / 1000 == 9000 && targetid / 100000 == 9000) {
                    unlock = targetid < 900090000 || targetid > 900090004; //1 movie
                    warp = true;
                } else if (divi / 10 == 1020 && targetid == 1020000) { // Adventurer movie clip Intro
                    unlock = true;
                    warp = true;
                } else if (chr.getMapId() == 900090101 && targetid == 100030100) {
                    unlock = true;
                    warp = true;
                } else if (chr.getMapId() == 2010000 && targetid == 104000000) {
                    unlock = true;
                    warp = true;
                } else if (chr.getMapId() == 106020001 || chr.getMapId() == 106020502) {
                    if (targetid == (chr.getMapId() - 1)) {
                        unlock = true;
                        warp = true;
                    }
                } else if (chr.getMapId() == 0 && targetid == 10000) {
                    unlock = true;
                    warp = true;
                } else if (chr.getMapId() == 931000011 && targetid == 931000012) {
                    unlock = true;
                    warp = true;
                } else if (chr.getMapId() == 931000021 && targetid == 931000030) {
                    unlock = true;
                    warp = true;
                }
                if (unlock) {
                    c.announce(UIPacket.IntroDisableUI(false));
                    c.announce(UIPacket.IntroLock(false));
                    c.announce(CWvsContext.enableActions());
                }
                if (warp) {
                    final MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                    chr.changeMap(to, to.getPortal(0));
                }
            } else {
                if (portal != null && !chr.hasBlockedInventory()) {
                    portal.enterPortal(c);
                } else {
                    c.announce(CWvsContext.enableActions());
                }
            }
        }
        //chr.setMapTransitionComplete();
    }

    public static final void InnerPortal(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
        final int toX = slea.readShort();
        final int toY = slea.readShort();
//	slea.readShort(); // Original X pos
//	slea.readShort(); // Original Y pos

        if (portal == null) {
            return;
        }
        if (portal.getScriptName() != null) {
            PortalScriptManager.getInstance().executePortalScript(portal, c);
        }
        chr.getMap().movePlayer(chr, new Point(toX, toY));
        chr.checkFollow();
    }

    public static final void snowBall(SeekableLittleEndianAccessor slea, MapleClient c) {
        //B2 00
        //01 [team]
        //00 00 [unknown]
        //89 [position]
        //01 [stage]
        c.announce(CWvsContext.enableActions());
        //empty, we do this in closerange
    }

    public static final void leftKnockBack(SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().getMapId() / 10000 == 10906) { //must be in snowball map or else its like infinite FJ
            c.announce(CField.leftKnockBack());
            c.announce(CWvsContext.enableActions());
        }
    }

    public static final void ReIssueMedal(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        /*
        final MapleQuest q = MapleQuest.getInstance(slea.readShort());
        final int itemid = q.getMedalItem();
        if (itemid != slea.readInt() || itemid <= 0 || chr.getQuestStatus(q.getId()) != 2) {
            c.announce(UIPacket.reissueMedal(itemid, 4));
            return;
        }
        if (chr.haveItem(itemid, 1, true, true)) {
            c.announce(UIPacket.reissueMedal(itemid, 3));
            return;
        }
        if (!MapleInventoryManipulator.checkSpace(c, itemid, (short) 1, "")) {
            c.announce(UIPacket.reissueMedal(itemid, 2));
            return;
        }
        if (chr.getMeso() < 100) {
            c.announce(UIPacket.reissueMedal(itemid, 1));
            return;
        }
        chr.gainMeso(-100, true, true);
        MapleInventoryManipulator.addById(c, itemid, (short) 1, "Redeemed item through medal quest " + q.getId() + " on " + FileoutputUtil.CurrentReadable_Date());
        c.announce(UIPacket.reissueMedal(itemid, 0));
         */
    }
}
