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
package scripting;

import java.awt.Point;
import java.util.List;

import client.inventory.Equip;
import client.SkillFactory;
import constants.GameConstants;
import client.Skill;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.MapleQuestStatus;
import client.MapleTrait.MapleTraitType;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.guild.MapleGuild;
import server.Randomizer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleMapObject;
import server.maps.SavedLocationType;
import server.maps.Event_DojoAgent;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import server.quest.MapleQuest;
import tools.packet.CField;
import tools.packet.PetPacket;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryProof;
import client.maplepal.BattleData;
import client.maplepal.MaplePal;
import client.maplepal.MaplePalBattleManager;
import client.maplepal.MaplePalPacket;
import client.maplepal.MaplePalStorage;
import client.maplepal.PalTemplateProvider;
import client.maplepal.PalTemplateProvider.PalSkillTemplate;
import client.maplepal.PalTemplateProvider.PalTemplate;
import static constants.GameConstants.pals;
import constants.ServerSlots;
import handling.channel.handler.NPCHandler;
import handling.world.World;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import server.GachaponEntry;
import server.GachaponProvider;
import server.MapleAchievement;
import server.MapleAchievements;
import server.MapleDamageSkin;
import server.MapleDamageSkins;
import server.MapleDamageSkinsGacha;
import server.MapleKQuest;
import server.MapleKQuests;
import server.TimerManager;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MonsterDropEntry;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.packet.CField.EffectPacket;
import tools.packet.CField.NPCPacket;
import tools.packet.CField.UIPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InfoPacket;

//quest effect
//c.announce(EffectPacket.showForeignEffect(12)); // Quest completion
//chr.getMap().broadcastMessage(chr, EffectPacket.showForeignEffect(chr.getId(), 12), false);
public class AbstractPlayerInteraction {

    public MapleClient c;
    public int id, id2;

    public AbstractPlayerInteraction(MapleClient c) {
        this.c = c;
    }

    public AbstractPlayerInteraction(final MapleClient c, final int id, final int id2) {
        this.c = c;
        this.id = id;
        this.id2 = id2;
    }

    public final MapleClient getClient() {
        return c;
    }

    public final MapleClient getC() {
        return c;
    }

    public MapleCharacter getChar() {
        return c.getPlayer();
    }

    public final ChannelServer getChannelServer() {
        return c.getChannelServer();
    }

    public final MapleCharacter getPlayer() {
        return c.getPlayer();
    }

    public final EventManager getEventManager(final String event) {
        return c.getChannelServer().getEventSM().getEventManager(event);
    }

    public final EventInstanceManager getEventInstance() {
        return c.getPlayer().getEventInstance();
    }

    public final void warp(final int map) {
        final MapleMap mapz = getWarpMap(map);
        try {
            c.getPlayer().changeMap(mapz, mapz.findRandomSpawnpoint());
        } catch (Exception e) {
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public final void warp_Instanced(final int map) {
        final MapleMap mapz = getMap_Instanced(map);
        try {
            c.getPlayer().changeMap(mapz, mapz.findRandomSpawnpoint());
        } catch (Exception e) {
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public final void warp(final int map, final int portal) {
        final MapleMap mapz = getWarpMap(map);
        try {
            c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        } catch (Exception e) {
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }

    }

    public final void warpS(final int map, final int portal) {
        final MapleMap mapz = getWarpMap(map);
        try {
            c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        } catch (Exception e) {
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public final void warp(final int map, String portal) {
        final MapleMap mapz = getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }
        try {
            c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        } catch (Exception e) {
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }

    }

    public final void warpS(final int map, String portal) {
        final MapleMap mapz = getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }
        c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
    }

    public final void warpMap(final int mapid, final int portal) {
        final MapleMap map = getMap(mapid);
        for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
            chr.changeMap(map, map.getPortal(portal));
        }
    }

    public void instantWarp(Point pos) {
        c.announce(CField.instanWarp(c.getPlayer(), pos));
        c.getPlayer().getMap().movePlayer(c.getPlayer(), new Point(pos));
    }

    public Point getPos(String pto) {
        return c.getPlayer().getMap().getPortal(pto).getPosition();
    }

    public final void playPortalSE() {
        c.announce(EffectPacket.showOwnBuffEffect(0, 7, 1, 1));
    }

    private final MapleMap getWarpMap(final int map) {
        return ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(map);
    }

    public final MapleMap getMap() {
        return c.getPlayer().getMap();
    }

    public final MapleMap getMap(final int map) {
        return getWarpMap(map);
    }

    public final MapleMap getMap_Instanced(final int map) {
        return c.getPlayer().getEventInstance() == null ? getMap(map) : c.getPlayer().getEventInstance().getMapInstance(map);
    }

    public final MapleMap getEventMap() {
        return c.getPlayer().getEventInstance() == null ? getMap(c.getPlayer().getMapId()) : c.getPlayer().getEventInstance().getMapInstance(c.getPlayer().getMapId());
    }

    public void spawnMonster(final int id, final int qty) {
        spawnMob(id, qty, c.getPlayer().getTruePosition());
    }

    public final void spawnMobOnMap(final int id, final int qty, final int x, final int y, final int map) {
        for (int i = 0; i < qty; i++) {
            getMap(map).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y));
        }
    }

    public final void spawnMob(final int id, final int qty, final int x, final int y) {
        spawnMob(id, qty, new Point(x, y));
    }

    public final void spawnMob(final int id, final int x, final int y) {
        spawnMob(id, 1, new Point(x, y));
    }

    public final void spawnMob(final int id, final int qty, final Point pos) {
        for (int i = 0; i < qty; i++) {
            c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public final void killMob(int ids) {
        c.getPlayer().getMap().killMonster(ids);
    }

    public final void killAllMob() {
        c.getPlayer().getMap().killAllMonsters(true);
    }

    public final void addHP(final int delta) {
        c.getPlayer().addHP(delta);
    }

    public final int getPlayerStat(final String type) {
        if (type.equals("LVL")) {
            return c.getPlayer().getLevel();
        } else if (type.equals("STR")) {
            return (int) c.getPlayer().getStat().getStr();
        } else if (type.equals("DEX")) {
            return (int) c.getPlayer().getStat().getDex();
        } else if (type.equals("INT")) {
            return (int) c.getPlayer().getStat().getInt();
        } else if (type.equals("LUK")) {
            return (int) c.getPlayer().getStat().getLuk();
        } else if (type.equals("HP")) {
            return c.getPlayer().getStat().getHp();
        } else if (type.equals("MP")) {
            return c.getPlayer().getStat().getMp();
        } else if (type.equals("MAXHP")) {
            return c.getPlayer().getStat().getMaxHp();
        } else if (type.equals("MAXMP")) {
            return c.getPlayer().getStat().getMaxMp();
        } else if (type.equals("RAP")) {
            return c.getPlayer().getRemainingAp();
        } else if (type.equals("RSP")) {
            return c.getPlayer().getRemainingSp();
        } else if (type.equals("GID")) {
            return c.getPlayer().getGuildId();
        } else if (type.equals("GRANK")) {
            return c.getPlayer().getGuildRank();
        } else if (type.equals("ARANK")) {
            return c.getPlayer().getAllianceRank();
        } else if (type.equals("GM")) {
            return c.getPlayer().isGM() ? 1 : 0;
        } else if (type.equals("ADMIN")) {
            return c.getPlayer().isAdmin() ? 1 : 0;
        } else if (type.equals("GENDER")) {
            return c.getPlayer().getGender();
        } else if (type.equals("FACE")) {
            return c.getPlayer().getFace();
        } else if (type.equals("HAIR")) {
            return c.getPlayer().getHair();
        }
        return -1;
    }

    public final String getName() {
        return c.getPlayer().getName();
    }

    public final boolean haveItem(final int itemid) {
        return haveItem(itemid, 1);
    }

    public final boolean haveItem(final int itemid, final double amount) {
        if (amount < 1.0) {
            return false;
        }
        return haveItem(itemid, Randomizer.Max((int) Math.floor(amount), Integer.MAX_VALUE), false, true);
    }

    public final boolean haveItem(final int itemid, final double quantity, final boolean checkEquipped, final boolean greaterOrEquals) {
        if (quantity < 1.0) {
            return false;
        }
        return c.getPlayer().haveItem(itemid, Randomizer.Max((int) Math.floor(quantity), Integer.MAX_VALUE), checkEquipped, greaterOrEquals);
    }

    public final boolean canHold() {
        for (int i = 1; i <= 5; i++) {
            if (c.getPlayer().getInventory(MapleInventoryType.getByType((byte) i)).getNextFreeSlot() <= -1) {
                return false;
            }
        }
        return true;
    }

    public final boolean canHoldSlots(final int slot) {
        for (int i = 1; i <= 5; i++) {
            if (c.getPlayer().getInventory(MapleInventoryType.getByType((byte) i)).isFull(slot)) {
                return false;
            }
        }
        return true;
    }

    public final boolean canHold(final int itemid) {
        return c.getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public final boolean canHold(final int itemid, final int quantity) {
        return MapleInventoryManipulator.checkSpace(c, itemid, Randomizer.Max(quantity, Integer.MAX_VALUE), "");
    }

    public final MapleQuestStatus getQuestRecord(final int id) {
        return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id));
    }

    public final MapleQuestStatus getQuestNoRecord(final int id) {
        return c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(id));
    }

    public final byte getQuestStatus(final int id) {
        return c.getPlayer().getQuestStatus(id);
    }

    public final boolean isQuestActive(final int id) {
        return getQuestStatus(id) == 1;
    }

    public final boolean isQuestFinished(final int id) {
        return getQuestStatus(id) == 2;
    }

    public final void showQuestMsg(final String msg) {
        c.announce(CWvsContext.showQuestMsg(msg));
    }

    public final void forceStartQuest(final int id, final String data) {
        MapleQuest.getInstance(id).forceStart(c.getPlayer(), 0, data);
    }

    public final void forceStartQuest(final int id, final int data, final boolean filler) {
        MapleQuest.getInstance(id).forceStart(c.getPlayer(), 0, filler ? String.valueOf(data) : null);
    }

    public void forceStartQuest(final int id) {
        MapleQuest.getInstance(id).forceStart(c.getPlayer(), 0, null);
    }

    public void forceCompleteQuest(final int id) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), 0);
    }

    public void spawnNpc(final int npcId) {
        c.getPlayer().getMap().spawnNpc(npcId, c.getPlayer().getPosition());
    }

    public final void spawnNpc(final int npcId, final int x, final int y) {
        c.getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
    }

    public final void spawnNpc(final int npcId, final Point pos) {
        c.getPlayer().getMap().spawnNpc(npcId, pos);
    }

    public final void removeNpc(final int mapid, final int npcId) {
        c.getChannelServer().getMapFactory().getMap(mapid).removeNpc(npcId);
    }

    public final void removeNpc(final int npcId) {
        c.getPlayer().getMap().removeNpc(npcId);
    }

    public final void forceStartReactor(final int mapid, final int id) {
        MapleMap map = c.getChannelServer().getMapFactory().getMap(mapid);
        for (final MapleReactor react : map.getAllReactor()) {
            if (react.getReactorId() == id) {
                react.forceStartReactor(c);
                break;
            }
        }
    }

    public final void destroyReactor(final int mapid, final int id) {
        MapleMap map = c.getChannelServer().getMapFactory().getMap(mapid);
        for (final MapleReactor react : map.getAllReactor()) {
            if (react.getReactorId() == id) {
                react.hitReactor(c);
                break;
            }
        }
    }

    public final void hitReactor(final int mapid, final int id) {
        MapleMap map = c.getChannelServer().getMapFactory().getMap(mapid);
        for (final MapleReactor react : map.getAllReactor()) {
            if (react.getReactorId() == id) {
                react.hitReactor(c);
                break;
            }
        }
    }

    public final int getJob() {
        return c.getPlayer().getJob();
    }

    public final void gainNX(final int amount) {
        c.getPlayer().modifyCSPoints(1, amount, true);
    }

    public final boolean removeItem(int id, int amount) {
        if (amount > 0) {
            if (haveItem(id, amount)) {
                final MapleInventoryType type = GameConstants.getInventoryType(id);
                MapleInventoryManipulator.removeById(c, type, id, amount, false, false);
                c.announce(InfoPacket.getShowItemGain(id, -amount, true));
                return true;
            }
        }
        return false;
    }

    public final void gainItemPeriod(final int id, final int quantity, final int period) { // period is in days
        gainItem(id, quantity, false, period, -1, "collect from abstract player system");
    }

    public final void gainItemPeriod(final int id, final int quantity, final long period, final String owner) { // period
        // is
        // in
        // days
        gainItem(id, quantity, false, period, -1, owner);
    }

    public final void gainItem(final int id, final int quantity) {
        gainItem(id, quantity, false, 0, -1, "");
    }

    public final void gainItem(final int id, final int quantity, String owner) {
        gainItem(id, quantity, false, 0, -1, owner);
    }

    public final void gainItemSilent(final int id, final int quantity) {
        gainItem(id, quantity, false, 0, -1, "", c, false);
    }

    public final void gainItem(final int id, final int quantity, final boolean randomStats, final long period, final int slots, final String owner) {
        gainItem(id, quantity, randomStats, period, slots, owner, c);
    }

    public final void gainItem(final int id, final int quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient cg) {
        gainItem(id, quantity, randomStats, period, slots, owner, cg, true);
    }

    public final void gainItem(final int id, final int quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient cg, final boolean show) {
        if (cg != null) {
            if (quantity >= 0) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final MapleInventoryType type = GameConstants.getInventoryType(id);

                if (MapleInventoryManipulator.checkSpace(cg, id, quantity, "")) {
                    if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                        final Equip item = (Equip) (randomStats ? ii.randomizeStats(getChar(), (Equip) ii.getEquipById(id)) : ii.getEquipById(id));
                        if (period > 0) {
                            item.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                        }
                        if (slots > 0) {
                            item.setUpgradeSlots((short) (item.getUpgradeSlots() + slots));
                        }
                        if (owner != null) {
                            item.setOwner(owner);
                        }
                        item.setGMLog("Received from interaction " + this.id + " (" + id2 + ") on " + FileoutputUtil.CurrentReadable_Time());
                        final String name = ii.getName(id);
                        if (id / 10000 == 114 && name != null && name.length() > 0) { // medal
                            final String msg = "You have attained title <" + name + ">";
                            cg.getPlayer().dropMessage(-1, msg);
                            cg.getPlayer().dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addbyItem(cg, item.copy());
                    } else {
                        if (GameConstants.isPet(id)) {
                            try {
                                MaplePet pet = MaplePet.createPet(id, MapleInventoryIdentifier.getInstance());
                                MapleInventoryManipulator.addById(cg, id, (short) quantity, "", pet, -1, "");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            int amount = quantity;
                            while (amount > 0) {
                                MapleInventoryManipulator.addById(cg, id, (short) (amount > 30000 ? 30000 : amount), owner == null ? "" : owner, null, period, "Received from interaction " + this.id + " (" + id2 + ") on " + FileoutputUtil.CurrentReadable_Date());
                                amount -= (amount > 30000 ? 30000 : amount);
                            }
                        }
                    }
                }
            } else {
                if (haveItem(id, -quantity)) {
                    int removed = MapleInventoryManipulator.removeByIdCount(cg, GameConstants.getInventoryType(id), id, -quantity, true, false);
                    if (removed > 0) {
                        if (GameConstants.getInventoryType(id) == MapleInventoryType.USE || GameConstants.getInventoryType(id) == MapleInventoryType.ETC) {
                            cg.getPlayer().removeOverflow(id, removed);
                        }
                    }
                }
            }
            if (show) {
                cg.announce(InfoPacket.getShowItemGain(id, quantity, true));
            }
        }
    }

    public final boolean removeItem(final int id) { // quantity 1
        if (haveItem(id)) {
            if (MapleInventoryManipulator.removeById_Lock(c, GameConstants.getInventoryType(id), id)) {
                c.announce(InfoPacket.getShowItemGain(id, (short) -1, true));
                return true;
            }
        }
        return false;
    }

    public final void changeMusic(final String songName) {
        getPlayer().getMap().broadcastMessage(CField.musicChange(songName));
    }

    public final void worldMessage(final int type, final String message) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(type, message));
    }

    // default playerMessage and mapMessage to use type 5
    public final void playerMessage(final String message) {
        playerMessage(5, message);
    }

    public final void mapMessage(final String message) {
        mapMessage(5, message);
    }

    public final void guildMessage(final String message) {
        guildMessage(5, message);
    }

    public final void playerMessage(final int type, final String message) {
        c.getPlayer().dropMessage(type, message);
    }

    public final void mapMessage(final int type, final String message) {
        c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(type, message));
    }

    public final void guildMessage(final int type, final String message) {
        if (getPlayer().getGuildId() > 0) {
            World.Guild.guildPacket(getPlayer().getGuildId(), CWvsContext.serverNotice(type, message));
        }
    }

    public final MapleGuild getGuild() {
        return getGuild(getPlayer().getGuildId());
    }

    public final MapleGuild getGuild(int guildid) {
        return World.Guild.getGuild(guildid);
    }

    public final MapleParty getParty() {
        return c.getPlayer().getParty();
    }

    public final int getCurrentPartyId(int mapid) {
        return getMap(mapid).getCurrentPartyId();
    }

    public final boolean isLeader() {
        if (getPlayer().getParty() == null) {
            return false;
        }
        return getParty().getLeader().getId() == c.getPlayer().getId();
    }

    public final boolean isAllPartyMembersAllowedJob(final int job) {
        if (c.getPlayer().getParty() == null) {
            return false;
        }
        for (final MaplePartyCharacter mem : c.getPlayer().getParty().getMembers()) {
            if (mem.getJobId() / 100 != job) {
                return false;
            }
        }
        return true;
    }

    public final boolean allMembersHere() {
        if (c.getPlayer().getParty() == null) {
            return false;
        }
        for (final MaplePartyCharacter mem : c.getPlayer().getParty().getMembers()) {
            final MapleCharacter chr = c.getPlayer().getMap().getCharacterById(mem.getId());
            if (chr == null) {
                return false;
            }
        }
        return true;
    }

    public final void warpParty(final int mapId) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            warp(mapId, 0);
            return;
        }
        final MapleMap target = getMap(mapId);
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public final void warpParty(final int mapId, final int portal) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            if (portal < 0) {
                warp(mapId);
            } else {
                warp(mapId, portal);
            }
            return;
        }
        final boolean rand = portal < 0;
        final MapleMap target = getMap(mapId);
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                if (rand) {
                    try {
                        curChar.changeMap(target, target.getPortal(Randomizer.nextInt(target.getPortals().size())));
                    } catch (Exception e) {
                        curChar.changeMap(target, target.getPortal(0));
                    }
                } else {
                    curChar.changeMap(target, target.getPortal(portal));
                }
            }
        }
    }

    public final void warpParty_Instanced(final int mapId) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            warp_Instanced(mapId);
            return;
        }
        final MapleMap target = getMap_Instanced(mapId);

        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void gainMeso(long gain) {
        c.getPlayer().gainMeso(gain, true, true);
    }

    public void gainExp(long gain) {
        c.getPlayer().gainExp(gain, true, true, true);
    }

    public void gainExpR(int gain) {
        c.getPlayer().gainExp(gain, true, true, true);
    }

    public final void givePartyItems(final int id, final int quantity, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            if (quantity >= 0) {
                gainItem(id, quantity);
            } else {
                MapleInventoryManipulator.removeById(chr.getClient(), GameConstants.getInventoryType(id), id, -quantity, true, false);
            }
            chr.getClient().announce(InfoPacket.getShowItemGain(id, quantity, true));
        }
    }

    public void addPartyTrait(String t, int e, final List<MapleCharacter> party) {
        for (final MapleCharacter chr : party) {
            chr.getTrait(MapleTraitType.valueOf(t)).addExp(e, chr);
        }
    }

    public void addPartyTrait(String t, int e) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            addTrait(t, e);
            return;
        }
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null
                    && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.getTrait(MapleTraitType.valueOf(t)).addExp(e, curChar);
            }
        }
    }

    public void addTrait(String t, int e) {
        getPlayer().getTrait(MapleTraitType.valueOf(t)).addExp(e, getPlayer());
    }

    public final void givePartyItems(final int id, final int quantity) {
        givePartyItems(id, quantity, false);
    }

    public final void givePartyItems(final int id, final int quantity, final boolean removeAll) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            gainItem(id, (short) (removeAll ? -getPlayer().itemQuantity(id) : quantity));
            return;
        }

        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null
                    && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                gainItem(id, (short) (removeAll ? -curChar.itemQuantity(id) : quantity), false, 0, 0, "",
                        curChar.getClient());
            }
        }
    }

    public final void givePartyExp_PQ(final int maxLevel, final double mod, final List<MapleCharacter> party) {
        for (final MapleCharacter chr : party) {
            final int amount = (int) Math.round(GameConstants.getExpNeededForLevel(
                    chr.getLevel() > maxLevel ? (maxLevel + ((maxLevel - chr.getLevel()) / 10)) : chr.getLevel())
                    / (Math.min(chr.getLevel(), maxLevel) / 5.0) / (mod * 2.0));
            chr.gainExp(amount * c.getChannelServer().getExpRate(), true, true, true);
        }
    }

    public final void gainExp_PQ(final int maxLevel, final double mod) {
        final int amount = (int) Math.round(GameConstants.getExpNeededForLevel(
                getPlayer().getLevel() > maxLevel ? (maxLevel + (getPlayer().getLevel() / 10)) : getPlayer().getLevel())
                / (Math.min(getPlayer().getLevel(), maxLevel) / 10.0) / mod);
        gainExp(amount * c.getChannelServer().getExpRate());
    }

    public final void givePartyExp_PQ(final int maxLevel, final double mod) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            final int amount = (int) Math.round(GameConstants
                    .getExpNeededForLevel(getPlayer().getLevel() > maxLevel ? (maxLevel + (getPlayer().getLevel() / 10))
                            : getPlayer().getLevel())
                    / (Math.min(getPlayer().getLevel(), maxLevel) / 10.0) / mod);
            gainExp(amount * c.getChannelServer().getExpRate());
            return;
        }
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null
                    && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                final int amount = (int) Math.round(GameConstants.getExpNeededForLevel(
                        curChar.getLevel() > maxLevel ? (maxLevel + (curChar.getLevel() / 10)) : curChar.getLevel())
                        / (Math.min(curChar.getLevel(), maxLevel) / 10.0) / mod);
                curChar.gainExp(amount * c.getChannelServer().getExpRate(), true, true, true);
            }
        }
    }

    public final void givePartyExp(final int amount, final List<MapleCharacter> party) {
        for (final MapleCharacter chr : party) {
            chr.gainExp(amount * c.getChannelServer().getExpRate(), true, true, true);
        }
    }

    public final void givePartyExp(final int amount) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            gainExp(amount * c.getChannelServer().getExpRate());
            return;
        }
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null
                    && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.gainExp(amount * c.getChannelServer().getExpRate(), true, true, true);
            }
        }
    }

    public final void givePartyNX(final int amount, final List<MapleCharacter> party) {
        for (final MapleCharacter chr : party) {
            chr.modifyCSPoints(1, amount, true);
        }
    }

    public final void givePartyNX(final int amount) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            gainNX(amount);
            return;
        }
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null
                    && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.modifyCSPoints(1, amount, true);
            }
        }
    }

    public final void endPartyQuest(final int amount, final List<MapleCharacter> party) {
        for (final MapleCharacter chr : party) {
            chr.endPartyQuest(amount);
        }
    }

    public final void endPartyQuest(final int amount) {
        if (getPlayer().getParty() == null || getPlayer().getParty().getMembers().size() == 1) {
            getPlayer().endPartyQuest(amount);
            return;
        }
        final int cMap = getPlayer().getMapId();
        for (final MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null
                    && (curChar.getMapId() == cMap || curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.endPartyQuest(amount);
            }
        }
    }

    public final void removeFromParty(final int id, final List<MapleCharacter> party) {
        for (final MapleCharacter chr : party) {
            final int possesed = chr.getInventory(GameConstants.getInventoryType(id)).countById(id);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(id), id, possesed, true, false);
                chr.getClient().announce(InfoPacket.getShowItemGain(id, (short) -possesed, true));
            }
        }
    }

    public final void removeFromParty(final int id) {
        givePartyItems(id, (short) 0, true);
    }

    public final void useSkill(final int skill, final int level) {
        if (level <= 0) {
            return;
        }
        SkillFactory.getSkill(skill).getEffect(level).applyTo(c.getPlayer());
    }

    public final void useItem(final int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(c.getPlayer());
        c.announce(InfoPacket.getStatusMsg(id));
    }

    public final void cancelItem(final int id) {
        c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(id), false, -1);
    }

    public final int getMorphState() {
        return c.getPlayer().getMorphState();
    }

    public final void removeAll(final int id) {
        c.getPlayer().removeAll(id);
    }

    public final void gainCloseness(final int closeness, final int index) {
        final MaplePet pet = getPlayer().getPet(index);
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + (closeness * getChannelServer().getTraitRate()));
            getClient().announce(PetPacket.updatePet(pet,
                    getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()),
                    true));
        }
    }

    public final void gainClosenessAll(final int closeness) {
        for (final MaplePet pet : getPlayer().getPets()) {
            if (pet != null && pet.getSummoned()) {
                pet.setCloseness(pet.getCloseness() + closeness);
                getClient().announce(PetPacket.updatePet(pet,
                        getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()),
                        true));
            }
        }
    }

    public final void resetMap(final int mapid) {
        getMap(mapid).resetFully();
    }

    public final void openNpc(final int id) {
        openNpc(id, null);
    }

    public final void openNpc(final MapleClient cg, final int id) {
        openNpc(id, null);
    }

    public void openNpc(int npcid, String script) {
        NPCScriptManager.getInstance().startNPC(c, npcid, script);
    }

    public final int getMapId() {
        return c.getPlayer().getMap().getId();
    }

    public final boolean haveMonster(final int mobid) {
        for (MapleMonster mob : c.getPlayer().getMap().getAllMonsters()) {
            if (mob.getId() == mobid) {
                return true;
            }
        }
        return false;
    }

    public final int getChannelNumber() {
        return c.getChannel();
    }

    public final int getMonsterCount(final int mapid) {
        return c.getChannelServer().getMapFactory().getMap(mapid).getNumMonsters();
    }

    public final void teachSkill(final int id, final int level, final byte masterlevel) {
        getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }

    public final void teachSkill(final int id, int level) {
        final Skill skil = SkillFactory.getSkill(id);
        if (getPlayer().getSkillLevel(skil) > level) {
            level = getPlayer().getSkillLevel(skil);
        }
        getPlayer().changeSingleSkillLevel(skil, level, skil.getMaxLevel());
    }

    public final int getPlayerCount(final int mapid) {
        return c.getChannelServer().getMapFactory().getMap(mapid).getCharactersSize();
    }

    public final void dojo_getUp() {
        c.announce(InfoPacket.updateInfoQuest(1207, "pt=1;min=4;belt=1;tuto=1")); // todo
        c.announce(EffectPacket.Mulung_DojoUp2());
        c.announce(CField.instantMapWarp((byte) 6));
    }

    public final boolean dojoAgent_NextMap(final boolean dojo, final boolean fromresting) {
        if (dojo) {
            return Event_DojoAgent.warpNextMap(c.getPlayer(), fromresting, c.getPlayer().getMap());
        }
        return Event_DojoAgent.warpNextMap_Agent(c.getPlayer(), fromresting);
    }

    public final boolean dojoAgent_NextMap(final boolean dojo, final boolean fromresting, final int mapid) {
        if (dojo) {
            return Event_DojoAgent.warpNextMap(c.getPlayer(), fromresting, getMap(mapid));
        }
        return Event_DojoAgent.warpNextMap_Agent(c.getPlayer(), fromresting);
    }

    public final int dojo_getPts() {
        return c.getPlayer().getIntNoRecord(GameConstants.DOJO);
    }

    public final MapleEvent getEvent(final String loc) {
        return c.getChannelServer().getEvent(MapleEventType.valueOf(loc));
    }

    public final int getSavedLocation(final String loc) {
        final Integer ret = c.getPlayer().getSavedLocation(SavedLocationType.fromString(loc));
        if (ret == null || ret == -1) {
            return 100000000;
        }
        return ret;
    }

    public final void saveLocation(final String loc) {
        c.getPlayer().saveLocation(SavedLocationType.fromString(loc));
    }

    public final void saveReturnLocation(final String loc) {
        c.getPlayer().saveLocation(SavedLocationType.fromString(loc), c.getPlayer().getMap().getReturnMap().getId());
    }

    public final void clearSavedLocation(final String loc) {
        c.getPlayer().clearSavedLocation(SavedLocationType.fromString(loc));
    }

    public final void summonMsg(final String msg) {
        if (!c.getPlayer().hasSummon()) {
            playerSummonHint(true);
        }
        c.announce(UIPacket.summonMessage(msg));
    }

    public final void summonMsg(final int type) {
        if (!c.getPlayer().hasSummon()) {
            playerSummonHint(true);
        }
        c.announce(UIPacket.summonMessage(type));
    }

    public final void showInstruction(final String msg, final int width, final int height) {
        c.announce(CField.sendHint(msg, width, height));
    }

    public final void playerSummonHint(final boolean summon) {
        c.getPlayer().setHasSummon(summon);
        c.announce(UIPacket.summonHelper(summon));
    }

    public final String getInfoQuest(final int id) {
        return c.getPlayer().getInfoQuest(id);
    }

    public final void updateInfoQuest(final int id, final String data) {
        c.getPlayer().updateInfoQuest(id, data);
    }

    public final boolean getEvanIntroState(final String data) {
        return getInfoQuest(22013).equals(data);
    }

    public final void updateEvanIntroState(final String data) {
        updateInfoQuest(22013, data);
    }

    public final void Aran_Start() {
        c.announce(CField.Aran_Start());
    }

    public final void evanTutorial(final String data, final int v1) {
        c.announce(NPCPacket.getEvanTutorial(data));
    }

    public final void AranTutInstructionalBubble(final String data) {
        c.announce(EffectPacket.AranTutInstructionalBalloon(data));
    }

    public final void ShowWZEffect(final String data) {
        c.announce(EffectPacket.AranTutInstructionalBalloon(data));
    }

    public final void showWZEffect(final String data) {
        c.announce(EffectPacket.ShowWZEffect(data));
    }

    public final void EarnTitleMsg(final String data) {
        c.announce(CWvsContext.getTopMsg(data));
    }

    public final void EnableUI(final short i) {
        c.announce(UIPacket.IntroEnableUI(i));
    }

    public final void DisableUI(final boolean enabled) {
        c.announce(UIPacket.IntroDisableUI(enabled));
    }

    public final void MovieClipIntroUI(final boolean enabled) {
        c.announce(UIPacket.IntroDisableUI(enabled));
        c.announce(UIPacket.IntroLock(enabled));
    }

    public MapleInventoryType getInvType(int i) {
        return MapleInventoryType.getByType((byte) i);
    }

    public String getItemName(final int id) {
        return MapleItemInformationProvider.getInstance().getName(id);
    }

    public void gainPet(int id, String name, int level, int closeness, int fullness, long period, short flags) {
        if (id > 5000200 || id < 5000000) {
            id = 5000000;
        }
        if (level > 30) {
            level = 30;
        }
        if (closeness > 30000) {
            closeness = 30000;
        }
        if (fullness > 100) {
            fullness = 100;
        }
        try {
            MapleInventoryManipulator.addById(c, id, (short) 1, "", MaplePet.createPet(id, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), id == 5000054 ? (int) period : 0, flags), 45, "Pet from interaction " + id + " (" + id2 + ")" + " on " + FileoutputUtil.CurrentReadable_Date());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void gainGP(final int gp) {
        if (getPlayer().getGuildId() <= 0) {
            return;
        }
        World.Guild.gainGP(getPlayer().getGuildId(), gp); // 1 for
    }

    public long getGP() {
        if (getPlayer().getGuildId() <= 0) {
            return 0;
        }
        return World.Guild.getGP(getPlayer().getGuildId()); // 1 for
    }

    public void showMapEffect(String path) {
        getClient().announce(CField.MapEff(path));
    }

    public int itemQuantity(int itemid) {
        return getPlayer().itemQuantity(itemid);
    }

    public EventInstanceManager getDisconnected(String event) {
        EventManager em = getEventManager(event);
        if (em == null) {
            return null;
        }
        for (EventInstanceManager eim : em.getInstances()) {
            if (eim.isDisconnected(c.getPlayer()) && eim.getPlayerCount() > 0) {
                return eim;
            }
        }
        return null;
    }

    public boolean isAllReactorState(final int reactorId, final int state) {
        boolean ret = false;
        for (MapleReactor r : getMap().getAllReactor()) {
            if (r.getReactorId() == reactorId) {
                ret = r.getState() == state;
            }
        }
        return ret;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPlayer().getTruePosition());
    }

    // summon one monster, remote location
    public void spawnMonster(int id, int x, int y) {
        spawnMonster(id, 1, new Point(x, y));
    }

    // multiple monsters, remote location
    public void spawnMonster(int id, int qty, int x, int y) {
        spawnMonster(id, qty, new Point(x, y));
    }

    // handler for all spawnMonster
    public void spawnMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public void sendNPCText(final String text, final int npc) {
        getMap().broadcastMessage(NPCPacket.getNPCTalk(npc, (byte) 0, text, (byte) 0, (byte) 0, (byte) 0));
    }

    public boolean getTempFlag(final int flag) {
        return (c.getChannelServer().getTempFlag() & flag) == flag;
    }

    public void logPQ(String text) {
//	FileoutputUtil.log(FileoutputUtil.PQ_Log, text);
    }

    public void outputFileError(Throwable t) {
        FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, t);
    }

    public void trembleEffect(int type, int delay) {
        c.announce(CField.trembleEffect(type, delay));
    }

    public int nextInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    public MapleQuest getQuest(int arg0) {
        return MapleQuest.getInstance(arg0);
    }

    public void achievement(int a) {
        c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(a));
    }

    public final MapleInventory getInventory(int type) {
        return c.getPlayer().getInventory(MapleInventoryType.getByType((byte) type));
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return getPlayer().getInventory(type);
    }

    public boolean isGMS() {
        return GameConstants.GMS;
    }

    public int randInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    public void sendDirectionStatus(int key, int value) {
        c.announce(UIPacket.getDirectionInfo(key, value));
        c.announce(UIPacket.getDirectionStatus(true));
    }

    public void sendDirectionInfo(String data) {
        c.announce(UIPacket.getDirectionInfo(data, 2000, 0, -100, 0));
        c.announce(UIPacket.getDirectionInfo(1, 2000));
    }

    /*
     * UNDEFINED(0), //2 EQUIP(1), //4 USE(2), //8 SETUP(3), //10 ETC(4), //20
     * CASH(5), //40 EQUIPPED(-1);
     */
    public Item getEquipbySlot(int slot) {
        return getInventory(1).getItem((byte) slot);
    }

    public boolean isEquipLock(Equip eqp) {
        return ItemFlag.LOCK.check(eqp.getFlag());
    }

    public int getItembySlot(Item item) {
        return (int) (item.getPosition());
    }

    public Item getEquippedbySlot(int slot) {
        return getInventory(-1).getItem((byte) slot);
    }

    public Item getUsebySlot(int slot) {
        return getInventory(2).getItem((byte) slot);
    }

    public void removeEquipFromSlot(short slot) {
        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIPPED, slot, tempItem.getQuantity(), false, false);
    }

    public void removeEquipFromItemSlot(short slot) {
        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, slot, tempItem.getQuantity(), false, false);
    }

    public void removeItem(short type, short slot, short amount) {
        Item tempItem = null;
        switch (type) {
            case -1:
                //tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIPPED, slot, amount, false, false);
                return;
            case 1:
                tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
                break;
            case 2:
                tempItem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
                break;
            case 3:
                tempItem = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem(slot);
                break;
            case 4:
                tempItem = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
                break;
        }
        if (tempItem != null) {
            MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(tempItem.getItemId()), tempItem.getItemId(), -amount, false, false);
        }
    }

    public boolean removeItemFromSlot(Item item) {
        if (item != null) {
            return MapleInventoryManipulator.removeFromSlot(c, item.getInventoryType(), item.getPosition(), item.getQuantity(), false, false);
        }
        return false;
    }

    public boolean gainEquip(Item equip) {
        return MapleInventoryManipulator.addFromDrop(c, equip, true, false);
    }

    public boolean gainEquip(int id, int scale) {
        if (getPlayer().canHold(id)) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Item idrop = ii.randomizeStats(getPlayer(), (Equip) ii.getEquipById(id), scale);
            return MapleInventoryManipulator.addFromDrop(c, idrop, true, false);
        } else {
            return false;
        }
    }

    public boolean gainEquip(int id, int level, int scale) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item idrop = ii.randomizeStats(getPlayer(), (Equip) ii.getEquipById(id), level, scale);
        return MapleInventoryManipulator.addFromDrop(c, idrop, true, false);
    }

    public boolean gainEquipNoStars(int id, int level, int scale) {
        return gainEquip(id, level, scale);
    }

    public boolean gainEquip(int id, int level, int scale, int pot, int slots) {
        return gainEquip(id, level, scale);
    }

    public boolean gainEquip(int id, int level, int scale, int pot, int slots, boolean toggle) {
        return gainEquip(id, level, scale);
    }

    public boolean gainEquipPend(int id, int power, int scale, int pot, int lines) {
        return gainEquip(id, 1, scale);
    }

    public boolean gainNXEquip(int id, int level) {
        return gainEquip(id, level, 2);
    }

    public boolean gainDonarEquip(int id, int level, int scale, int blevel) {
        return gainEquip(id, level, scale);
    }

    public void resetEquip(Item item) {
        Equip eqp = (Equip) item;
        eqp.resetNX();
        c.announce(CWvsContext.InventoryPacket.updateItemslot(item));
    }

    public boolean gainHeart() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item idrop = ii.heartStats((Equip) ii.getEquipById(1672005));
        return MapleInventoryManipulator.addFromDrop(c, idrop, true, false);
    }

    public boolean gainGodEquip(int id) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item idrop = ii.heartStats((Equip) ii.getEquipById(id));
        return MapleInventoryManipulator.addFromDrop(c, idrop, true, false);
    }

    public boolean gainGodEquipTier(int id, int tier) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item idrop = ii.heartStats((Equip) ii.getEquipById(id), tier);
        return MapleInventoryManipulator.addFromDrop(c, idrop, true, false);
    }

    public boolean gainGodEquipTier(int id, int tier, int TD, int BD, int OP, int IED, int CD, int AS) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item idrop = ii.godStats((Equip) ii.getEquipById(id), tier, TD, BD, OP, IED, CD, AS);
        return MapleInventoryManipulator.addFromDrop(c, idrop, true, false);
    }

    public final int getPlayerTotalLevel() {
        return c.getPlayer().getTotalLevel();
    }

    public String convertNumber(long value) {
        return NumberFormat.getInstance().format(value);
    }

    public boolean isEquip(Item item) {
        return item.getInventoryType() == MapleInventoryType.EQUIP;
    }

    public boolean isEquipped(Item item) {
        return item.getInventoryType() == MapleInventoryType.EQUIPPED;
    }

    public int getScale(int base) {
        List<Integer> power = new ArrayList<>();
        int start = base;
        for (int i = 1; i < start; i++) {
            for (int j = 1; j < base; j++) {
                power.add(i);
            }
            base--;
        }
        Collections.shuffle(power);
        return power.get(0);
    }

    private static List<Pair<Item, MapleInventoryType>> prepareProofInventoryItems(List<Pair<Integer, Integer>> items) {
        List<Pair<Item, MapleInventoryType>> addedItems = new LinkedList<>();
        for (Pair<Integer, Integer> p : items) {
            Item it = new Item(p.getLeft(), (byte) 0, p.getRight().shortValue());
            addedItems.add(new Pair<>(it, MapleInventoryType.CANHOLD));
        }

        return addedItems;
    }

    private static List<List<Pair<Integer, Integer>>> prepareInventoryItemList(List<Integer> itemids,
            List<Integer> quantity) {
        int size = Math.min(itemids.size(), quantity.size());

        List<List<Pair<Integer, Integer>>> invList = new ArrayList<>(6);
        for (int i = MapleInventoryType.UNDEFINED.getType(); i < MapleInventoryType.CASH.getType(); i++) {
            invList.add(new LinkedList<Pair<Integer, Integer>>());
        }

        for (int i = 0; i < size; i++) {
            int itemid = itemids.get(i);
            invList.get(GameConstants.getInventoryType(itemid).getType()).add(new Pair<>(itemid, quantity.get(i)));
        }

        return invList;
    }

    public boolean canHoldAllAfterRemoving(List<Integer> toAddItemids, List<Integer> toAddQuantity,
            List<Integer> toRemoveItemids, List<Integer> toRemoveQuantity) {
        List<List<Pair<Integer, Integer>>> toAddItemList = prepareInventoryItemList(toAddItemids, toAddQuantity);
        List<List<Pair<Integer, Integer>>> toRemoveItemList = prepareInventoryItemList(toRemoveItemids,
                toRemoveQuantity);

        MapleInventoryProof prfInv = (MapleInventoryProof) this.getInventory(MapleInventoryType.CANHOLD);
        prfInv.lockInventory();
        try {
            for (int i = MapleInventoryType.EQUIP.getType(); i < MapleInventoryType.CASH.getType(); i++) {
                List<Pair<Integer, Integer>> toAdd = toAddItemList.get(i);

                if (!toAdd.isEmpty()) {
                    List<Pair<Integer, Integer>> toRemove = toRemoveItemList.get(i);

                    MapleInventory inv = this.getInventory(i);
                    prfInv.cloneContents(inv);

                    for (Pair<Integer, Integer> p : toRemove) {
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.CANHOLD, p.getLeft(), p.getRight(), false, false);
                    }

                    List<Pair<Item, MapleInventoryType>> addItems = prepareProofInventoryItems(toAdd);

                    boolean canHold = MapleInventory.checkSpots(c.getPlayer(), addItems, true);
                    if (!canHold) {
                        return false;
                    }
                }
            }
        } finally {
            prfInv.flushContents();
            prfInv.unlockInventory();
        }

        return true;
    }

    public boolean canHold(int itemid, int quantity, int removeItemid, int removeQuantity) {
        return canHoldAllAfterRemoving(Collections.singletonList(itemid), Collections.singletonList(quantity),
                Collections.singletonList(removeItemid), Collections.singletonList(removeQuantity));
    }

    private static List<Integer> convertToIntegerArray(List<Double> list) {
        List<Integer> intList = new LinkedList<>();
        for (Double d : list) {
            intList.add(d.intValue());
        }

        return intList;
    }

    public boolean canHoldAll(List<Double> itemids) {
        List<Double> quantity = new LinkedList<>();
        for (int i = 0; i < itemids.size(); i++) {
            quantity.add(1.0);
        }

        return canHoldAll(itemids, quantity);
    }

    public boolean canHoldAll(List<Double> itemids, List<Double> quantity) {
        return canHoldAll(convertToIntegerArray(itemids), convertToIntegerArray(quantity), true);
    }

    private boolean canHoldAll(List<Integer> itemids, List<Integer> quantity, boolean isInteger) {
        int size = Math.min(itemids.size(), quantity.size());

        List<Pair<Item, MapleInventoryType>> addedItems = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            Item it = new Item(itemids.get(i), (byte) 0, quantity.get(i).shortValue());
            addedItems.add(new Pair<>(it, GameConstants.getInventoryType(itemids.get(i))));
        }

        return MapleInventory.checkSpots(c.getPlayer(), addedItems, false);
    }

    public int random(int min, int max) {
        return Randomizer.random(min, max);
    }

    public void resetItem(Item item, int level) {
        getInventory(MapleInventoryType.EQUIP).removeItem((short) item.getPosition());
        if (!gainEquip(item.getItemId(), level, Randomizer.random(1, 10))) {
            c.getPlayer().dropMessage("Error with NPC ITEM ID: " + item.getItemId());
        }
    }

    public void resetItem(Item item, int level, int minscale, int maxscale) {
        getInventory(MapleInventoryType.EQUIP).removeItem((short) item.getPosition());
        if (!gainEquip(item.getItemId(), level, Randomizer.random(minscale, maxscale))) {
            c.getPlayer().dropMessage("Error with NPC ITEM ID: " + item.getItemId());
        }
    }

    public boolean unlockEquip(Equip eqp) {
        if (eqp != null) {
            final MapleInventoryType type = MapleInventoryType.getByType((byte) eqp.getType());
            final Item item = eqp;
            // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
            if (ItemFlag.LOCK.check(item.getFlag())) {
                item.setFlag((byte) (item.getFlag() - ItemFlag.LOCK.getValue()));
                c.getPlayer().forceReAddItem_Flag(item, type);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public MapleCharacter getPlayerByName(String name) {
        return c.getChannelServer().getPlayerStorage().getCharacterByName(name.toLowerCase());
    }

    public List<MapleAchievement> getAchievements() {
        return MapleAchievements.getInstance().getAchievements();
    }

    public List<MapleAchievement> getAchievementsbyCag(int type) {
        return MapleAchievements.getInstance().getAchievementsbyCag(type);
    }

    public int getAchievementId(MapleAchievement ma) {
        return ma.getId();
    }

    public String getAchievementName(int id) {
        return MapleAchievements.getInstance().getById(id).getName();
    }

    public MapleAchievement getAchievement(int id) {
        return MapleAchievements.getInstance().getById(id);
    }

    public String getType(MapleAchievement Ach) {
        switch (Ach.getType()) {
            case 0:
                return "Exp";
            case 1:
                return "Drop Rate";
            case 3:
                return "All Stat";
            case 4:
                return "Overpower";
            case 5:
                return "Meso Rate";
            case 6:
                return "Total Damage";
            case 7:
                return "Boss Damage";
            case 8:
                return "Ignore Defense";
        }
        return "";
    }

    public int getTypeId(MapleAchievement Ach) {
        return Ach.getType();
    }

    public List<Integer> getNxWeapons() {
        return MapleItemInformationProvider.getInstance().nxweapons;
    }

    public List<Integer> getNxHat() {
        return MapleItemInformationProvider.getInstance().nxhat;
    }

    public List<Integer> getNxOverall() {
        return MapleItemInformationProvider.getInstance().nxoverall;
    }

    public List<Integer> getNxTop() {
        return MapleItemInformationProvider.getInstance().nxtop;
    }

    public List<Integer> getNxBottom() {
        return MapleItemInformationProvider.getInstance().nxbottom;
    }

    public List<Integer> getNxGlove() {
        return MapleItemInformationProvider.getInstance().nxglove;
    }

    public List<Integer> getNxShoe() {
        return MapleItemInformationProvider.getInstance().nxshoe;
    }

    public List<Integer> getNxCape() {
        return MapleItemInformationProvider.getInstance().nxcape;
    }

    public List<Integer> getNxShield() {
        return MapleItemInformationProvider.getInstance().nxshield;
    }

    public List<Integer> getEmotes() {
        return MapleItemInformationProvider.getInstance().emotes;
    }

    public List<Integer> getNxAcc() {
        return MapleItemInformationProvider.getInstance().nxacc;
    }

    public List<Integer> getChairs() {
        return MapleItemInformationProvider.getInstance().chairs;
    }

    public List<Integer> getPets() {
        return MapleItemInformationProvider.getInstance().pets;
    }

    public List<Integer> getNXPool() {
        return MapleItemInformationProvider.getInstance().NXPool;
    }

    public int getRandomNxWeapon() {
        return MapleItemInformationProvider.getInstance().getWeapons().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getWeapons().size()));
    }

    public int getRandomNxEquip() {
        return MapleItemInformationProvider.getInstance().getEquips().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getEquips().size()));
    }

    public int getRandomChair() {
        return MapleItemInformationProvider.getInstance().getChairs().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getChairs().size()));
    }

    public int getRandomHair() {
        return MapleItemInformationProvider.getInstance().getHairs().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getHairs().size()));
    }

    public int getRandomFace() {
        return MapleItemInformationProvider.getInstance().getFaces().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getFaces().size()));
    }

    public int getNumberofHair() {
        return MapleItemInformationProvider.getInstance().getHairs().size();
    }

    public int getRandomNx() {
        return MapleItemInformationProvider.getInstance().getAllNX().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getAllNX().size()));
    }

    public int getRandomNxPool() {
        return MapleItemInformationProvider.getInstance().getAllNXPool().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getAllNXPool().size()));
    }

    public int getRandomPet() {
        return MapleItemInformationProvider.getInstance().getPets().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getPets().size()));
    }

    public int getRandomEmote() {
        return MapleItemInformationProvider.getInstance().getPets().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getEmotes().size()));
    }

    public List<MapleDamageSkin> getDamageSkins() {
        return MapleDamageSkins.getInstance().getDamageSkins();
    }

    public List<MapleDamageSkin> getDamageSkinsTier(int tier) {
        return MapleDamageSkins.getInstance().getDamageSkinsbyTier(tier);
    }

    public List<Integer> getDamageSkinsbyIds() {
        return MapleDamageSkins.getInstance().getDamageSkinsbyIds();
    }

    public List<MapleKQuest> getQuests() {
        return MapleKQuests.getInstance().getQuests();
    }

    public List<MapleKQuest> getQuestsbyCag(int type) {
        return MapleKQuests.getInstance().getQuestsbyCag(type);
    }

    public String getQuestsbyCagName(int cag) {
        for (MapleKQuest ach : getQuests()) {
            if (ach.getCag() == cag) {
                return ach.getTownName();
            }
        }
        return "";
    }

    public int getQuestId(MapleKQuest ma) {
        return ma.getId();
    }

    public String getQuestName(int id) {
        return MapleKQuests.getInstance().getById(id).getQuestName();
    }

    public MapleKQuest getQuestbyId(int id) {
        return MapleKQuests.getInstance().getById(id);
    }

    public boolean getQuestsCompletedbyCagName(int cag) {
        for (MapleKQuest ach : getQuests()) {
            if (ach.getCag() == cag) {
                if (!c.getPlayer().questFinished(ach.getId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getQuestsCompletedbyCag(int cag) {
        int count = 0;
        for (MapleKQuest ach : getQuests()) {
            if (ach.getCag() == cag) {
                count++;
            }
        }
        return count;
    }

    public void processReward(int type) {
        MapleCharacter chr = c.getPlayer();
        chr.addFame(1);
        switch (type) {
            case 1: {//random amount of AP
                short ap = (short) (Randomizer.random(5, 10));
                chr.gainAp(ap);
                chr.dropMessage("You have gained " + ap + " AP.");
            }
            break;
            case 2: {
                int itemid = getRandomPet();
                if (canHold(itemid)) {
                    gainItem(itemid, (short) 1);
                    chr.dropMessage("You have gained random Pet: " + MapleItemInformationProvider.getInstance().getName(itemid) + ".");
                }
            }
            break;
            case 3: {
                int itemid = getRandomChair();
                if (canHold(itemid)) {
                    gainItem(itemid, (short) 1);
                    chr.dropMessage("You have gained random Chair: " + MapleItemInformationProvider.getInstance().getName(itemid) + ".");
                }
            }
            break;
            case 4: {
                //chr.addStat(5, Randomizer.random(0, 8), true);
            }
            break;
            case 5: {
                int itemid = 4310501;
                short amount = (short) (Randomizer.random(25, 100));
                if (canHold(itemid, amount)) {
                    gainItem(itemid, (short) amount);
                    chr.dropMessage("You have gained " + amount + " " + MapleItemInformationProvider.getInstance().getName(itemid) + "'s.");
                }
            }
            break;
            case 6: {
                int itemid = 4310502;
                if (canHold(itemid)) {
                    gainItem(itemid, (short) 1);
                    chr.dropMessage("You have gained a " + MapleItemInformationProvider.getInstance().getName(itemid) + ".");
                }
            }
            break;
            case 7: {
                int itemid = 2473000;
                short amount = (short) (Randomizer.random(25, 50));
                if (canHold(itemid, amount)) {
                    gainItem(itemid, (short) amount);
                    chr.dropMessage("You have gained " + amount + " " + MapleItemInformationProvider.getInstance().getName(itemid) + "'s.");
                }
            }
            break;
            case 8: {
                int itemid = 2473001;
                short amount = (short) (Randomizer.random(5, 10));
                if (canHold(itemid, amount)) {
                    gainItem(itemid, (short) amount);
                    chr.dropMessage("You have gained " + amount + " " + MapleItemInformationProvider.getInstance().getName(itemid) + "'s.");
                }
            }
            break;
            case 9: {
                int itemid = 2049300;
                short amount = (short) (Randomizer.random(50, 100));
                if (canHold(itemid, amount)) {
                    gainItem(itemid, (short) amount);
                    chr.dropMessage("You have gained " + amount + " " + MapleItemInformationProvider.getInstance().getName(itemid) + "'s.");
                }
            }
            break;
            case 10: {
                int itemid = 2340000;
                short amount = (short) (Randomizer.random(25, 50));
                if (canHold(itemid, amount)) {
                    gainItem(itemid, (short) amount);
                    chr.dropMessage("You have gained " + amount + " " + MapleItemInformationProvider.getInstance().getName(itemid) + "'s.");
                }
            }
            break;
        }
    }

    public List<Item> getRewards(int townId, int cost, int count) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<GachaponEntry> townEntries = ii.getGachRewards(townId);
        List<Item> rewards = new ArrayList<>();
        GachaponEntry reward = townEntries.get(Randomizer.nextInt(townEntries.size()));
        if (GameConstants.getInventoryType(reward.itemId) == MapleInventoryType.EQUIP) {
            rewards.add((Item) ii.randomizeStats((Equip) ii.getEquipById(reward.itemId), count));
        } else {
            short amount = (short) Randomizer.random(reward.Minimum, reward.Maximum);
            rewards.add(new Item(reward.itemId, (short) 0, amount, (short) 0, -1));
        }
        return rewards;
    }

    public List<Item> getRewardsByPower(int townId, int power, int scale) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<GachaponEntry> townEntries = ii.getGachRewards(townId);
        List<Item> rewards = new ArrayList<>();
        GachaponEntry reward = townEntries.get(Randomizer.nextInt(townEntries.size()));
        if (GameConstants.getInventoryType(reward.itemId) == MapleInventoryType.EQUIP) {
            rewards.add((Item) ii.randomizeStats(getPlayer(), (Equip) ii.getEquipById(reward.itemId), scale));
        } else {
            short amount = (short) Randomizer.random(reward.Minimum, reward.Maximum);
            rewards.add(new Item(reward.itemId, (short) 0, amount, (short) 0, -1));
        }
        return rewards;
    }

    public List<Item> getRewardsByPower2(int townId, int power, int scale) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<GachaponEntry> townEntries = ii.getGachRewards(townId);
        List<Item> rewards = new ArrayList<>();
        GachaponEntry reward = townEntries.get(Randomizer.nextInt(townEntries.size()));
        if (GameConstants.getInventoryType(reward.itemId) == MapleInventoryType.EQUIP) {
            rewards.add((Item) ii.randomizeStatsCustom(getPlayer(), (Equip) ii.getEquipById(reward.itemId), power, scale));
        } else {
            short amount = (short) Randomizer.random(reward.Minimum, reward.Maximum);
            rewards.add(new Item(reward.itemId, (short) 0, amount, (short) 0, -1));
        }
        return rewards;
    }

    public List<Item> getRewardsByPower3(int townId, int power, int scale) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<GachaponEntry> townEntries = ii.getGachRewards(townId);
        List<Item> rewards = new ArrayList<>();
        GachaponEntry reward = townEntries.get(Randomizer.nextInt(townEntries.size()));
        if (GameConstants.getInventoryType(reward.itemId) == MapleInventoryType.EQUIP) {
            rewards.add((Item) ii.randomizeStats(getPlayer(), (Equip) ii.getEquipById(reward.itemId), scale));
        } else {
            short amount = (short) Randomizer.random(reward.Minimum, reward.Maximum);
            rewards.add(new Item(reward.itemId, (short) 0, amount, (short) 0, -1));
        }
        return rewards;
    }

    public List<Item> getRewardsByPower4(int townId, int power, int scale, int slots) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<GachaponEntry> townEntries = ii.getGachRewards(townId);
        List<Item> rewards = new ArrayList<>();
        GachaponEntry reward = townEntries.get(Randomizer.nextInt(townEntries.size()));
        if (GameConstants.getInventoryType(reward.itemId) == MapleInventoryType.EQUIP) {
            rewards.add((Item) ii.randomizePlayerStats(getPlayer(), (Equip) ii.getEquipById(reward.itemId), power, scale, slots));
        } else {
            short amount = (short) Randomizer.random(reward.Minimum, reward.Maximum);
            rewards.add(new Item(reward.itemId, (short) 0, amount, (short) 0, -1));
        }
        return rewards;
    }

    public List<Integer> getAllRewards(int townId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Integer> ret = new ArrayList<>();
        for (GachaponEntry cur : ii.getGachRewards(townId)) {
            ret.add(cur.itemId);
        }
        return ret;
    }

    public List<Item> getRewards(int townId, MapleCharacter chr, int cost, int count) { // amount of tickets
        // used
        List<Item> rewards = getRewards(townId, cost, count);
        // check for inventory space
        List<Integer> itemIds = new LinkedList<>();
        List<Integer> itemQuantities = new LinkedList<>();
        for (Item i : rewards) {
            itemIds.add(i.getItemId());
            itemQuantities.add((int) i.getQuantity());
        }
        if (!canHoldAll(itemIds, itemQuantities, true)) {
            return null;
        }
        // give items
        for (Item i : rewards) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), i);
        }
        return rewards;
    }

    public List<Item> getRewardsByPower(int townId, MapleCharacter chr, int power, int scale) { // amount of tickets
        // used
        List<Item> rewards = getRewardsByPower(townId, power, scale);
        // check for inventory space
        List<Integer> itemIds = new LinkedList<>();
        List<Integer> itemQuantities = new LinkedList<>();
        for (Item i : rewards) {
            itemIds.add(i.getItemId());
            itemQuantities.add((int) i.getQuantity());
        }
        if (!canHoldAll(itemIds, itemQuantities, true)) {
            return null;
        }
        // give items
        for (Item i : rewards) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), i);
        }
        return rewards;
    }

    public List<Item> getRewardsByPower2(int townId, MapleCharacter chr, int power, int scale) { // amount of tickets
        // used
        List<Item> rewards = getRewardsByPower2(townId, power, scale);
        // check for inventory space
        List<Integer> itemIds = new LinkedList<>();
        List<Integer> itemQuantities = new LinkedList<>();
        for (Item i : rewards) {
            itemIds.add(i.getItemId());
            itemQuantities.add((int) i.getQuantity());
        }
        if (!canHoldAll(itemIds, itemQuantities, true)) {
            return null;
        }
        // give items
        for (Item i : rewards) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), i);
        }
        return rewards;
    }

    public List<Item> getRewardsByPower3(int townId, MapleCharacter chr, int power, int scale) { // amount of tickets
        // used
        List<Item> rewards = getRewardsByPower3(townId, power, scale);
        // check for inventory space
        List<Integer> itemIds = new LinkedList<>();
        List<Integer> itemQuantities = new LinkedList<>();
        for (Item i : rewards) {
            itemIds.add(i.getItemId());
            itemQuantities.add((int) i.getQuantity());
        }
        if (!canHoldAll(itemIds, itemQuantities, true)) {
            return null;
        }
        // give items
        for (Item i : rewards) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), i);
        }
        return rewards;
    }

    public List<Item> getRewardsByPower4(int townId, MapleCharacter chr, int power, int scale, int slots) { // amount of tickets
        // used
        List<Item> rewards = getRewardsByPower4(townId, power, scale, slots);
        // check for inventory space
        List<Integer> itemIds = new LinkedList<>();
        List<Integer> itemQuantities = new LinkedList<>();
        for (Item i : rewards) {
            itemIds.add(i.getItemId());
            itemQuantities.add((int) i.getQuantity());
        }
        if (!canHoldAll(itemIds, itemQuantities, true)) {
            return null;
        }
        // give items
        for (Item i : rewards) {
            MapleInventoryManipulator.addFromDrop(chr.getClient(), i);
        }
        return rewards;
    }

    public boolean canHoldGachReward() {
        return getPlayer().getInventory(client.inventory.MapleInventoryType.EQUIP).getNumFreeSlot() >= 1 && getPlayer().getInventory(client.inventory.MapleInventoryType.USE).getNumFreeSlot() >= 1;
    }

    public String botTest(int password) {
        String text1 = "", text2 = "";
        String color = "";
        int phase1 = random(0, 50), phase2 = 50 - phase1;
        boolean check = true;
        for (int i = 0; i < phase1; i++) {
            if (random(0, 9) == 0 && check) {
                int col = random(0, 2);
                if (col == 0) {
                    color = "#r";
                }
                if (col == 1) {
                    color = "#g";
                }
                if (col == 2) {
                    color = "#b";
                }
                check = false;
                text1 += color + random(0, 9) + "#k";
            } else {
                check = true;
                text1 += ".";
            }
        }
        for (int i = 0; i < phase2; i++) {
            if (random(0, 9) == 0 && check) {
                int col = random(0, 2);
                if (col == 0) {
                    color = "#r";
                }
                if (col == 1) {
                    color = "#g";
                }
                if (col == 2) {
                    color = "#b";
                }
                check = false;
                text2 += color + random(0, 9) + "#k";
            } else {
                check = true;
                text2 += ".";
            }
        }
        int col = random(0, 2);
        if (col == 0) {
            color = "#r";
        }
        if (col == 1) {
            color = "#g";
        }
        if (col == 2) {
            color = "#b";
        }
        return (text1 + "." + color + password + "#k." + text2);
    }

    public String getDamageSkinNumber(int value1, int value2, int value3, int value4) {
        int[] itemRewCount = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 25, 26, 27, 28, 29, 35, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 89, 92, 93, 95, 5000, 5001, 5002, 5003, 5004, 5005, 5006, 5007, 5010, 5011, 5012, 5013};
        String txt = "#fEffect/DamageSkin.img/" + itemRewCount[randInt(itemRewCount.length)] + "/NoCri0/" + value1 + "#";
        txt += "#fEffect/DamageSkin.img/" + itemRewCount[randInt(itemRewCount.length)] + "/NoCri0/" + value2 + "#";
        txt += "#fEffect/DamageSkin.img/" + itemRewCount[randInt(itemRewCount.length)] + "/NoCri0/" + value3 + "#";
        txt += "#fEffect/DamageSkin.img/" + itemRewCount[randInt(itemRewCount.length)] + "/NoCri0/" + value4 + "#";
        return txt;
    }

    public int getCode(int value1, int value2, int value3, int value4) {
        int total = 0;
        total += (value1 * 1000);
        total += (value2 * 100);
        total += (value3 * 10);
        total += (value4);
        return total;
    }

    public void destoryEquip(Equip eqp) {
        short slot = eqp.getPosition();
        String equipName = eqp.getItemName(eqp.getItemId());
        getPlayer().dropMessage(equipName + " was blown up!");
        getMap().broadcastMessage(getPlayer(), CField.getScrollEffect(eqp.getItemId(), Equip.ScrollResult.FAIL, false, false), true);
        if (eqp.getPosition() < 0) {
            Item tempItem = getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
            MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.EQUIPPED, slot, tempItem.getQuantity(), false, false);
        } else {
            Item tempItem = getInventory(MapleInventoryType.EQUIP).getItem(slot);
            MapleInventoryManipulator.removeFromSlot(getClient(), MapleInventoryType.EQUIP, slot, tempItem.getQuantity(), false, false);
        }
    }

    public String durationToString(long duration) {
        return String.format("%d Hours, %d Minutes, %d Seconds",
                TimeUnit.MILLISECONDS.toHours(duration % (1000 * 60 * 60 * 24)), TimeUnit.MILLISECONDS.toMinutes(duration % (1000 * 60 * 60)), TimeUnit.MILLISECONDS.toSeconds(duration % (1000 * 60))
        );
    }

    public String secondsToString(long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;  // Remaining minutes after extracting hours
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;  // Remaining seconds after extracting minutes

        return String.format("%d Hours, %d Minutes, %d Seconds", hours, minutes, seconds);
    }

    public String secondsLongToString(long duration) {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

        return String.format("%d Days, %d Hours, %d Minutes, %d Seconds", days, hours, minutes, seconds);
    }

    public String createTime(long value) {
        return new Date(value).toString();
    }

    public final boolean isAndroid(final int id) { // period is in days
        return GameConstants.isAndroid(id);
    }

    public final boolean isCash(final int id) { // period is in days
        return MapleItemInformationProvider.getInstance().isCash(id);
    }

    public void gainStartEquip() {
        MapleCharacter chr = getPlayer();
        int job = chr.getJob();
        switch (job) {
            case 100://warrior
            case 1100://CK warrior
                gainEquip(1302000, 1);
                return;
            case 200://mage
            case 1200://CK mage
            case 3200://Bam
                gainEquip(1382000, 1);
                return;
            case 2200://Evan
                gainEquip(1372005, 1);
                return;
            case 300://bowman
            case 1300://CK bowman
            case 3300://WH
                gainEquip(1452002, 1);
                gainEquip(1462001, 1);
                return;
            case 400://thief
            case 1400://CK thief
                gainEquip(1332005, 1);
                gainEquip(1472000, 1);
                return;
            case 430://DB
                gainEquip(1332005, 1);
                gainEquip(1342000, 1);
                return;
            case 500://pirate
            case 1500://CK pirate
            case 3500://Mech
                gainEquip(1482000, 1);
                gainEquip(1492000, 1);
                return;
            case 501://cannon master
                gainEquip(1532000, 1);
                return;
            case 3100://demon
                gainEquip(1322000, 1);
                return;
            case 2100://aran
                gainEquip(1442000, 1);
                return;
            case 2300://merc
                gainEquip(1522000, 1);
                return;
        }
        if (GameConstants.isWarriorJob(job)) {
            gainEquip(1302000, 1);
            gainEquip(1442000, 1);
        }
        if (GameConstants.isMageJob(job)) {
            gainEquip(1382000, 1);
            gainEquip(1372005, 1);
        }
        if (GameConstants.isBowmanJob(job)) {
            gainEquip(1452002, 1);
            gainEquip(1462001, 1);
            gainEquip(1522000, 1);
        }
        if (GameConstants.isThiefJob(job)) {
            gainEquip(1332005, 1);
            gainEquip(1472000, 1);
        }
        if (GameConstants.isPirateJob(job)) {
            gainEquip(1532000, 1);
            gainEquip(1482000, 1);
            gainEquip(1492000, 1);
        }
    }

    public boolean generateQuest() {
        if (getPlayer().getQuestStatus() > 0) {
            return false;
        }
        Map<Integer, Integer> temp = new LinkedHashMap<>(MapleKQuests.getInstance().getQuest_pool());
        List<Integer> items = new LinkedList<>();
        for (Entry<Integer, Integer> e : temp.entrySet()) {
            if (getPlayer().getTotalLevel() >= e.getValue() && e.getValue() > 0) {
                items.add(e.getKey());
            }
        }
        if (!items.isEmpty()) {
            Collections.shuffle(items);
            getPlayer().quest_status = 1;
            getPlayer().quest_level = getPlayer().getTotalLevel();
            getPlayer().quest_item1 = items.get(0);
            getPlayer().quest_item2 = items.get(1);
            getPlayer().quest_item3 = items.get(2);
            getPlayer().quest_item4 = items.get(3);
            getPlayer().quest_item5 = items.get(4);
            getPlayer().saveQuestPool();
            items.clear();
            temp.clear();
            return true;
        } else {
            temp.clear();
            return false;
        }
    }

    public String getQuestTown(int id) {
        return MapleKQuests.getInstance().getByItemId(id).getTownName();
    }

    public int getQuestItem(int slot) {
        if (slot == 1) {
            return getPlayer().quest_item1;
        }
        if (slot == 2) {
            return getPlayer().quest_item2;
        }
        if (slot == 3) {
            return getPlayer().quest_item3;
        }
        if (slot == 4) {
            return getPlayer().quest_item4;
        }
        if (slot == 5) {
            return getPlayer().quest_item5;
        }
        return 0;
    }

    public String getQuestPool() {
        String text = "";
        if (getPlayer().quest_status > 0) {
            int count = 9999;
            if (haveItem(getPlayer().quest_item1, count)) {
                text += "(#d" + getQuestTown(getPlayer().quest_item1) + "#k) #i" + getPlayer().quest_item1 + "# " + getItemName(getPlayer().quest_item1) + " (#gCompleted!#k)\r\n";
            } else {
                text += "(#d" + getQuestTown(getPlayer().quest_item1) + "#k) #i" + getPlayer().quest_item1 + "# " + getItemName(getPlayer().quest_item1) + " (#r" + getPlayer().countTotalItem(getPlayer().quest_item1) + "#k/#b" + count + "#k)\r\n";
            }
            if (haveItem(getPlayer().quest_item2, count)) {
                text += "(#d" + getQuestTown(getPlayer().quest_item2) + "#k) #i" + getPlayer().quest_item2 + "# " + getItemName(getPlayer().quest_item2) + " (#gCompleted!#k)\r\n";
            } else {
                text += "(#d" + getQuestTown(getPlayer().quest_item2) + "#k) #i" + getPlayer().quest_item2 + "# " + getItemName(getPlayer().quest_item2) + " (#r" + getPlayer().countTotalItem(getPlayer().quest_item2) + "#k/#b" + count + "#k)\r\n";
            }
            if (haveItem(getPlayer().quest_item3, count)) {
                text += "(#d" + getQuestTown(getPlayer().quest_item3) + "#k) #i" + getPlayer().quest_item3 + "# " + getItemName(getPlayer().quest_item3) + " (#gCompleted!#k)\r\n";
            } else {
                text += "(#d" + getQuestTown(getPlayer().quest_item3) + "#k) #i" + getPlayer().quest_item3 + "# " + getItemName(getPlayer().quest_item3) + " (#r" + getPlayer().countTotalItem(getPlayer().quest_item3) + "#k/#b" + count + "#k)\r\n";
            }
            if (haveItem(getPlayer().quest_item4, count)) {
                text += "(#d" + getQuestTown(getPlayer().quest_item4) + "#k) #i" + getPlayer().quest_item4 + "# " + getItemName(getPlayer().quest_item4) + " (#gCompleted!#k)\r\n";
            } else {
                text += "(#d" + getQuestTown(getPlayer().quest_item4) + "#k) #i" + getPlayer().quest_item4 + "# " + getItemName(getPlayer().quest_item4) + " (#r" + getPlayer().countTotalItem(getPlayer().quest_item4) + "#k/#b" + count + "#k)\r\n";
            }
            if (haveItem(getPlayer().quest_item5, count)) {
                text += "(#d" + getQuestTown(getPlayer().quest_item5) + "#k) #i" + getPlayer().quest_item5 + "# " + getItemName(getPlayer().quest_item5) + " (#gCompleted!#k)\r\n";
            } else {
                text += "(#d" + getQuestTown(getPlayer().quest_item5) + "#k) #i" + getPlayer().quest_item5 + "# " + getItemName(getPlayer().quest_item5) + " (#r" + getPlayer().countTotalItem(getPlayer().quest_item5) + "#k/#b" + count + "#k)\r\n";
            }
        } else {
            text = "Error please contact Resinated on discord.";
        }
        return text;
    }

    public boolean hasQuestItems() {
        int count = 9999;
        if (!haveItem(getPlayer().quest_item1, count)) {
            return false;
        }
        if (!haveItem(getPlayer().quest_item2, count)) {
            return false;
        }
        if (!haveItem(getPlayer().quest_item3, count)) {
            return false;
        }
        if (!haveItem(getPlayer().quest_item4, count)) {
            return false;
        }
        if (!haveItem(getPlayer().quest_item5, count)) {
            return false;
        }
        return true;
    }

    public boolean completeQuest() {
        if (hasQuestItems()) {
            int count = 9999;
            gainItem(getPlayer().quest_item1, -count);
            gainItem(getPlayer().quest_item2, -count);
            gainItem(getPlayer().quest_item3, -count);
            gainItem(getPlayer().quest_item4, -count);
            gainItem(getPlayer().quest_item5, -count);
            getPlayer().quest_status = 0;
            getPlayer().quest_level = 0;
            getPlayer().quest_item1 = 0;
            getPlayer().quest_item2 = 0;
            getPlayer().quest_item3 = 0;
            getPlayer().quest_item4 = 0;
            getPlayer().quest_item5 = 0;
            getPlayer().saveQuestPool();
            return true;
        } else {
            return false;
        }
    }

    public void resetQuest() {
        getPlayer().quest_status = 0;
        getPlayer().quest_level = 0;
        getPlayer().quest_item1 = 0;
        getPlayer().quest_item2 = 0;
        getPlayer().quest_item3 = 0;
        getPlayer().quest_item4 = 0;
        getPlayer().quest_item5 = 0;
        getPlayer().saveQuestPool();
    }

    public boolean isJobType(int type) {
        switch (type) {
            case 1:
                return GameConstants.isWarriorJob(getJob());
            case 2:
                return GameConstants.isMageJob(getJob());
            case 3:
                return GameConstants.isBowmanJob(getJob());
            case 4:
                return GameConstants.isThiefJob(getJob());
            case 5:
                return GameConstants.isPirateJob(getJob());
        }
        return false;
    }

    public String getUnitNumber(long value) {
        return StringUtil.getUnitNumber(value);
    }

    public String getFullUnitNumber(long value) {
        return StringUtil.getUnitFullNumber(value);
    }

    public void generateQuestItem() {
        Map<Integer, Integer> temp = new LinkedHashMap<>(MapleKQuests.getInstance().getQuest_pool());
        List<Integer> items = new LinkedList<>();
        for (Entry<Integer, Integer> e : temp.entrySet()) {
            if (getPlayer().getTotalLevel() >= e.getValue() && e.getValue() > 0) {
                items.add(e.getKey());
            }
        }
        if (!items.isEmpty()) {
            Collections.shuffle(items);
            int itemid = items.get(0);
            getPlayer().setMonsterHunt(itemid);
        }
    }

    public int generateRandomQuestItem() {
        int itemid = 0;
        List<Integer> items = new LinkedList<>();
        for (Entry<Integer, Integer> e : MapleKQuests.getInstance().getQuest_pool().entrySet()) {
            int cag = MapleKQuests.getInstance().getById(e.getKey()).getCag();
            if (getPlayer().getTotalLevel() >= e.getValue() && e.getValue() > 0) {
                items.add(e.getKey());
            }
        }
        if (!items.isEmpty()) {
            itemid = items.get(randInt(items.size()));
        }
        return itemid;
    }

    public void playMovie(String path) {
        c.announce(CField.UIPacket.playMovie(path, true));
    }

    public String getTimeSec(long value) {
        return StringUtil.secondsToString(value);
    }

    public MapleDamageSkin getDamageSkin(int id) {
        return MapleDamageSkins.getInstance().getById(id);
    }

    public int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    public int getDayofWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public int getWeekofMonth() {
        return Calendar.getInstance().get(Calendar.WEEK_OF_MONTH);
    }

    public String getMonthName(int id) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return monthNames[id];
    }

    public List<MapleDamageSkinsGacha> fetchDamageSkins(int id) {
        final List<MapleDamageSkinsGacha> skinEntry = MapleDamageSkins.retrieveSkins(id);
        List<MapleDamageSkinsGacha> skinEntryMonth = new LinkedList<>();
        if (skinEntry != null && !skinEntry.isEmpty()) {
            for (final MapleDamageSkinsGacha skin : skinEntry) {
                if (skin.getMonth() == 0 || skin.getMonth() == (Calendar.getInstance().get(Calendar.MONTH) + 1)) {
                    skinEntryMonth.add(skin);
                }
            }
        }

        return skinEntryMonth;
    }

    public double fetchDamageSkinChance(int id, int skinid) {
        double chance = 0.0;
        int total = 1;
        final List<MapleDamageSkinsGacha> skinEntry = fetchDamageSkins(id);
        if (skinEntry != null && !skinEntry.isEmpty()) {
            total = skinEntry.size();
            for (final MapleDamageSkinsGacha skin : skinEntry) {
                if (skin.getId() == skinid) {
                    if (skin.getMonth() == 0 || skin.getMonth() == (Calendar.getInstance().get(Calendar.MONTH) + 1)) {
                        chance = skin.getChance();
                    }
                }
            }
        }
        double tchance = ((int) ((chance / 1000.0 / total) * 10000.0) / 10000.0);

        return tchance;
    }

    public int fetchDamageSkin(int id) {
        final List<MapleDamageSkinsGacha> skinEntry = MapleDamageSkins.retrieveSkins(id);
        List<Integer> skins = new LinkedList<>();
        if (skinEntry != null && !skinEntry.isEmpty()) {
            for (final MapleDamageSkinsGacha skin : skinEntry) {
                if (skin.getMonth() == 0 || skin.getMonth() == (Calendar.getInstance().get(Calendar.MONTH) + 1)) {
                    if (Randomizer.nextInt(100000) <= skin.chance) {
                        skins.add(skin.getId());
                    }
                }
            }
            if (!skins.isEmpty()) {
                Collections.shuffle(skins);
                int skin = skins.get(0);
                skins.clear();
                return skin;
            }
        }
        return 0;
    }

    public void clearGachaSkins() {
        MapleDamageSkins.clearGacha();
    }

    public void reloadGachaId(int id) {
        MapleDamageSkins.reloadGachaID(id);
    }

    public boolean getLockSlot() {
        return GameConstants.getLockSlot();
    }

    public int getSlotExp(int type) {
        return ServerSlots.getExpType(type);
    }

    public void openLink(String link) {
        c.announce(CField.open_url(link));
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public long getTimeDiff(long value) {
        return System.currentTimeMillis() - value;
    }

    public String getJobName(int id) {
        switch (id) {
            case 112 -> {
                return "Hero";
            }
            case 122 -> {
                return "Paladin";
            }
            case 132 -> {
                return "Dark Knight";
            }
            case 212 -> {
                return "Fire Mage";
            }
            case 222 -> {
                return "Ice Mage";
            }
            case 232 -> {
                return "Bishop";
            }
            case 312 -> {
                return "Bowman";
            }
            case 322 -> {
                return "X-Bowman";
            }
            case 412 -> {
                return "Hermit";
            }
            case 422 -> {
                return "Shadower";
            }
            case 434 -> {
                return "Dual-Blade";
            }
            case 512 -> {
                return "Buccaneer";
            }
            case 522 -> {
                return "Corsair";
            }
            case 532 -> {
                return "Cannon Master";
            }
            case 1112 -> {
                return "Kain";
            }
            case 1212 -> {
                return "Kanna";
            }
            case 1312 -> {
                return "Path Finder";
            }
            case 1412 -> {
                return "NightWalker";
            }
            case 1512 -> {
                return "Ark";
            }
            case 2218 -> {
                return "Evan";
            }
            case 3112 -> {
                return "Demon Slayer";
            }
            case 3212 -> {
                return "Battle Mage";
            }
            case 3312 -> {
                return "Wild Hunter";
            }
            case 3512 -> {
                return "Mechanic";
            }
            case 900 -> {
                return "Grand Master";
            }
        }
        return "";
    }

    public List<MapleCharacter> getPlayers() {
        List<MapleCharacter> players = new ArrayList<>(getPlayer().getClient().getChannelServer().getPlayerStorage().getAllCharacters());
        if (players.size() > 1) {
            Collections.sort(players, new Comparator<MapleCharacter>() {

                @Override
                public int compare(MapleCharacter o1, MapleCharacter o2) {
                    return o1.getClient().getSessionIPAddress().compareTo(o2.getClient().getSessionIPAddress());
                }

            });
        }
        return players;
    }

    public void OpenPalWindow(MapleCharacter chr) {
        c.announce(MaplePalPacket.openPalWindow(chr));
    }

    public void OpenHatchWindow(MapleCharacter chr) {
        c.announce(MaplePalPacket.openPalHatchUI(chr));
    }

    public void AddHatchSlot(int count) {
        if (getHatchSlots() + count < 99) {
            getPlayer().addAccVar("Hatch_Slot", count);
            if (getHatchSlots() > 99) {
                getPlayer().addAccVar("Hatch_Slot", 99);
            }
        }
    }

    public int getHatchSlots() {
        return (int) getPlayer().getAccVar("Hatch_Slot");
    }

    public void addKaoticPowerTime(int value) {
        GameConstants.setServerVar("KP_Rate", GameConstants.getServerVar("KP_Rate") + value);
        sendServerMsg("Server has gained " + value + " Minutes of Kaotic Drop Buff! Time Remainging " + GameConstants.getServerVar("KP_Rate") + " Minutes");
    }

    public void setServerVar(String txt, long value) {
        GameConstants.setServerVar(txt, value);
    }

    public void addServerVar(String txt, long value) {
        long total = GameConstants.getServerVar(txt) + value;
        GameConstants.setServerVar(txt, total);
    }

    public long getServerVar(String txt) {
        return GameConstants.getServerVar(txt);
    }

    public boolean canEnterRaid(String txt) {
        return GameConstants.getServerVar(txt) < System.currentTimeMillis();
    }

    public void setRaidTime(String txt, long time) {
        GameConstants.setServerVar(txt, System.currentTimeMillis() + (time * 1000));
    }

    public long getRaidTime(String txt) {
        return Randomizer.Lmin(GameConstants.getServerVar(txt) - System.currentTimeMillis(), 0);
    }

    public void sendServerMsg(String msg) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                if (c.getPlayer().isGM()) {
                    victim.dropMessage(-6, msg); //-6
                } else {
                    victim.dropMessage(6, msg);
                }
            }
        }
    }

    public List<MapleCharacter> getOtherPlayers(MapleCharacter player) {
        List<MapleCharacter> players = new ArrayList<>();
        for (MapleCharacter chr : getPlayer().getClient().getChannelServer().getPlayerStorage().getAllCharacters()) {
            if (chr != player && !chr.isGM()) {
                players.add(chr);
            }
        }
        if (!players.isEmpty()) {
            Collections.sort(players, new Comparator<MapleCharacter>() {

                @Override
                public int compare(MapleCharacter o1, MapleCharacter o2) {
                    return o1.getName().compareTo(o2.getName());
                }

            });
        }
        return players;
    }

    public List<Equip> getWeaponsofType(MapleCharacter player, Equip eqp) {
        List<Equip> w = new ArrayList<>();
        boolean weapon = GameConstants.isWeapon(eqp.getItemId());
        if (weapon) {
            boolean twoHand = GameConstants.isRealTwoHanded(eqp.getItemId());
            for (Equip e : player.getEquipItems()) {
                if (eqp != e) {
                    if (GameConstants.isWeapon(e.getItemId())) {
                        if (twoHand && GameConstants.isRealTwoHanded(e.getItemId())) {
                            w.add(e);
                        }
                        if (!twoHand && !GameConstants.isRealTwoHanded(e.getItemId())) {
                            w.add(e);
                        }
                    }
                }
            }
        }
        return w;
    }

    public void changeEquipItem(MapleCharacter player, Equip eqp, int id) {
        eqp.setItemId(id);
        player.updateEquip(eqp);
    }

    public void callBattle() {
        MaplePalBattleManager.battleNpc(c.getPlayer(), 1);
    }

    public PalSkillTemplate getPalSkill(int id) {
        return PalTemplateProvider.getSkill(id);
    }

    public int getPalAtb(MaplePal pal) {
        return pal.getSpeed();
    }

    public int getDonationRate() {
        return GameConstants.getDonationRate();
    }

    public static List<Integer> getPals() {
        return GameConstants.getPals();
    }

    public static Integer getPal(int slotid) {
        return GameConstants.getPal(slotid);
    }

    public static void removePal(int id) {
        GameConstants.removePal(id);
    }

    public PalTemplateProvider getPalTemp() {
        return new PalTemplateProvider();
    }

    public boolean checkPal(int id, int tier) {
        return PalTemplateProvider.getTemplate(id).evo() <= tier;
    }

    public PalTemplate getTemplate(int templateId) {
        return PalTemplateProvider.getTemplate(templateId);
    }

    public int setMaxValue(int value, int max) {
        return Randomizer.Max(value, max);
    }

    public void systemMsg(String msg) {
        System.out.println(msg);
    }

    public List<Integer> getPalsEvo(int evo) {
        List<Integer> newPals = new ArrayList<>();
        for (PalTemplate pal : PalTemplateProvider.getAllMaplePal().values()) {
            if (pal.evo() == evo) {
                newPals.add(pal.templateId());
            }
        }
        return newPals;
    }

    public String getData(int item) {
        return MapleItemInformationProvider.getInstance().getStringData(item).getName();
    }

}
