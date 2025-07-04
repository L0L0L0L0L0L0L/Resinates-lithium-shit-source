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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import client.inventory.Equip;
import client.Skill;
import client.inventory.Item;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import constants.GameConstants;
import client.inventory.ItemFlag;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.SkillFactory;
import client.SkillEntry;
import client.MapleStat;
import client.maplepal.MaplePalBattleManager;
import client.maplepal.TrainerTemplate;
import server.MapleCarnivalParty;
import server.Randomizer;
import server.MapleInventoryManipulator;
import server.MapleShopFactory;
import server.MapleSquad;
import server.maps.MapleMap;
import server.maps.Event_DojoAgent;
import server.quest.MapleQuest;
import tools.packet.CField;
import server.MapleItemInformationProvider;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import database.DatabaseConnection;
import handling.channel.handler.HiredMerchantHandler;
import handling.channel.handler.PlayersHandler;
import handling.login.LoginInformationProvider;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import handling.world.exped.ExpeditionType;
import handling.world.guild.MapleGuild;
import server.MapleCarnivalChallenge;
import java.util.HashMap;
import handling.world.guild.MapleGuildAlliance;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.script.Invocable;
import server.CashItemFactory;
import server.MapleStatEffect;
import server.RankingWorker;
import server.RankingWorker.PokebattleInformation;
import server.RankingWorker.PokedexInformation;
import server.RankingWorker.PokemonInformation;
import server.SpeedRunner;
import server.StructItemOption;
import server.Timer.CloneTimer;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.maps.Event_PyramidSubway;
import static server.quest.MapleQuestActionType.item;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.packet.CField.NPCPacket;
import tools.packet.CField.UIPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.GuildPacket;
import tools.packet.CWvsContext.InfoPacket;
import tools.packet.CWvsContext.InventoryPacket;

public class NPCConversationManager extends AbstractPlayerInteraction {

    private String getText;
    private byte type; // -1 = NPC, 0 = start quest, 1 = end quest
    private byte lastMsg = -1;
    public boolean pendingDisposal = false;
    private int npc, npcOid;
    private boolean flip = false;
    private String scriptName = null;
    public boolean getTextNpc = false;
    private Invocable iv;
    public boolean sendOK = false;

    public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv, String scriptName) {
        super(c, npc, questid);
        this.type = type;
        this.iv = iv;
        this.scriptName = scriptName;
        this.npc = npc;
    }

    public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv) {
        super(c, npc, questid);
        this.type = type;
        this.iv = iv;
        this.npc = npc;
    }

    public Invocable getIv() {
        return iv;
    }

    public int getNpc() {
        return id;
    }

    public boolean getOk() {
        return sendOK;
    }

    public int getQuest() {
        return id2;
    }

    public byte getType() {
        return type;
    }

    public void setScriptName(String script) {
        scriptName = script;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void safeDispose() {
        pendingDisposal = true;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(c);
    }

    public void askMapSelection(final String sel) {
        if (lastMsg > -1) {
            return;
        }
        c.announce(NPCPacket.getMapSelection(id, sel));
        lastMsg = (byte) (GameConstants.GMS ? 0x11 : 0x10);
    }

    public void askMapSelectionOption(final String sel, boolean slider) {
        if (lastMsg > -1) {
            return;
        }
        c.announce(NPCPacket.getMapSelection(id, sel, slider));
        lastMsg = (byte) (GameConstants.GMS ? 0x11 : 0x10);
    }

    public void sendNext(String text) {
        sendNext(text, id);
    }

    public void sendNext(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { //sendNext will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 0, (byte) 1, (byte) (flip ? 8 : 0)));
        //c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, "00 01", chr.getMap().getNPCByOid(slea.readInt()) (byte) 0));
        lastMsg = 0;
    }

    public void sendPlayerToNpc(String text) {
        sendNextS(text, (byte) 3, id);
    }

    public void sendNextNoESC(String text) {
        sendNextS(text, (byte) 1, id);
    }

    public void sendNextNoESC(String text, int id) {
        sendNextS(text, (byte) 1, id);
    }

    public void sendNextS(String text, byte type) {
        sendNextS(text, type, id);
    }

    public void sendNextS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 0, (byte) 1, type, idd));
        lastMsg = 0;
    }

    public void sendPrev(String text) {
        sendPrev(text, id);
    }

    public void sendPrev(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 1, (byte) 0, (byte) (flip ? 8 : 0)));
        lastMsg = 0;
    }

    public void sendPrevS(String text, byte type) {
        sendPrevS(text, type, id);
    }

    public void sendPrevS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 1, (byte) 0, type, idd));
        lastMsg = 0;
    }

    public void sendNextPrev(String text) {
        sendNextPrev(text, id);
    }

    public void sendNextPrev(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 1, (byte) 1, (byte) (flip ? 8 : 0)));
        lastMsg = 0;
    }

    public void PlayerToNpc(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text, byte type) {
        sendNextPrevS(text, type, id);
    }

    public void sendNextPrevS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 1, (byte) 1, type, idd));
        lastMsg = 0;
    }

    public void sendOk(String text) {
        sendOk(text, id);
    }

    public void sendOk(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        sendOK = true;
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 0, (byte) 0, (byte) (flip ? 8 : 0), id));
        lastMsg = 0;
    }

    public void sendOkS(String text, byte type) {
        sendOkS(text, type, id);
    }

    public void sendOkS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        sendOK = true;
        c.announce(NPCPacket.getNPCTalk(id, (byte) 0, text, (byte) 0, (byte) 0, type, idd));
        lastMsg = 0;
    }

    public void sendYesNo(String text) {
        sendYesNo(text, id);
    }

    public void sendYesNo(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 2, text, (byte) 0, (byte) 0, (byte) (flip ? 8 : 0)));
        lastMsg = 2;
    }

    public void sendYesNoS(String text, byte type) {
        sendYesNoS(text, type, id);
    }

    public void sendYesNoS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.announce(NPCPacket.getNPCTalk(id, (byte) 2, text, (byte) 0, (byte) 0, type, idd));
        lastMsg = 2;
    }

    public void sendAcceptDecline(String text) {
        askAcceptDecline(text);
    }

    public void sendAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text);
    }

    public void askAcceptDecline(String text) {
        askAcceptDecline(text, id);
    }

    public void askAcceptDecline(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        lastMsg = (byte) (GameConstants.GMS ? 0xF : 0xE);
        c.announce(NPCPacket.getNPCTalk(id, (byte) lastMsg, text, (byte) 0, (byte) 0, (byte) (flip ? 8 : 0)));
    }

    public void askAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text, id);
    }

    public void askAcceptDeclineNoESC(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        lastMsg = (byte) (GameConstants.GMS ? 0xF : 0xE);
        c.announce(NPCPacket.getNPCTalk(id, (byte) lastMsg, text, (byte) 0, (byte) 0, (byte) 1));
    }

    public void askAvatar(String text, int... args) {
        if (lastMsg > -1) {
            return;
        }
        c.announce(NPCPacket.getNPCTalkStyle(id, text, args));
        lastMsg = 9;
    }

    public void sendSimple(String text) {
        sendSimple(text, id);
    }

    public void sendSimple(String text, int id) {

        if (lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) { //sendSimple will dc otherwise!
            sendNext(text);
            return;
        }
        //0 = crash
        //1 = crash
        c.announce(NPCPacket.getNPCTalk(id, (byte) 5, text, (byte) 0, (byte) 0, (byte) (flip ? 8 : 0)));
        lastMsg = 5;
    }

    public void sendSimpleS(String text, byte type) {
        sendSimpleS(text, type, id);
    }

    public void sendSimpleS(String text, byte type, int idd) {
        try {
            if (lastMsg > -1) {
                return;
            }
            if (!text.contains("#L")) { //sendSimple will dc otherwise!
                sendNextS(text, type);
                return;
            }
            c.announce(NPCPacket.getNPCTalk(id, (byte) 5, text, (byte) 0, (byte) 0, (byte) type, idd));
            lastMsg = 5;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStyle(String text, int styles[]) {
        if (lastMsg > -1) {
            return;
        }
        c.announce(NPCPacket.getNPCTalkStyle(id, text, styles));
        lastMsg = 9;
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalkNum(id, text, def, min, max));
        lastMsg = 4;
    }

    public void sendGetNumberS(String text, byte type) {
        sendGetNumberS(text, 1, 1, Integer.MAX_VALUE, type);
    }

    public void sendGetNumberS(String text, int min, int max, byte type) {
        sendGetNumberS(text, min, max, max, type);
    }

    public void sendGetNumberS(String text, int def, int min, int max, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalkNum(id, text, def, min, max, type));
        lastMsg = 4;
    }

    public void sendGetText(String text) {
        sendGetText(text, id);
    }

    public void sendGetTextMax(String text) {
        sendGetText(text, id);
    }

    public void sendGetNumber(String text) {
        int i = (int) Math.floor(Double.parseDouble(text));
        sendGetText(text, id);
    }

    public void sendGetText(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalkText(id, text));
        lastMsg = 3;
    }

    public void sendGetTextS(String text, byte type) {
        sendGetTextS(text, id, type);
    }

    public void sendGetTextS(String text, int id, byte type) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.announce(NPCPacket.getNPCTalkText(id, text, type));
        lastMsg = 3;
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public boolean getTextSize(int value) {
        return getText.length() <= value;
    }

    public int getTextSize() {
        return getText.length();
    }

    public String getText() {
        return getText;
    }

    public long getNumber() {
        try {
            return (long) Math.floor(Double.parseDouble(getText));
        } catch (Exception e) {
            return 0;
        }
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor((byte) color);
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int setRandomAvatar(int ticket, int... args_all) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        int args = args_all[Randomizer.nextInt(args_all.length)];
        if (args < 100) {
            c.getPlayer().setSkinColor((byte) args);
            c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            c.getPlayer().setFace(args);
            c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            c.getPlayer().setHair(args);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    public int setAvatar(int ticket, int args) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        if (args < 100) {
            c.getPlayer().setSkinColor((byte) args);
            c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            c.getPlayer().setFace(args);
            c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            c.getPlayer().setHair(args);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    public void sendStorage() {
        try {
            c.getPlayer().setConversation(4);
            c.getPlayer().getStorage().sendStorage(c, this.id);
            //c.getPlayer().dropMessage(1, "Storage is currently disabled, please you Free Markey");
            //NPCScriptManager.getInstance().dispose(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openShop(int id) {
        c.getPlayer().dropMessage("Shop ID: " + id);
        MapleShopFactory.getInstance().getShop(id).sendShop(c);
    }

    public void openShopNPC(int id) {
        c.getPlayer().dropMessage("Shop ID: " + id);
        MapleShopFactory.getInstance().getShop(id).sendShop(c, this.id);
    }

    public void openShopNPC(int id, int npc) {
        c.getPlayer().dropMessage("Shop ID: " + id);
        MapleShopFactory.getInstance().getShop(id).sendShop(c, npc);
    }

    public int gainGachaponItem(int id, int quantity) {
        return gainGachaponItem(id, quantity, c.getPlayer().getMap().getStreetName());
    }

    public int gainGachaponItem(int id, int quantity, final String msg) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            final Item item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity);

            if (item == null) {
                return -1;
            }
            final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
            if (rareness > 0) {
                World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(c.getPlayer().getName(), " : got a(n)", item, rareness, msg));
            }
            c.announce(InfoPacket.getShowItemGain(item.getItemId(), (short) quantity, true));
            return item.getItemId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int useNebuliteGachapon() {
        try {
            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() < 1) {
                return -1;
            }
            int grade = 0; // Default D
            final int chance = Randomizer.nextInt(100); // cannot gacha S, only from alien cube.
            if (chance < 1) { // Grade A
                grade = 3;
            } else if (chance < 5) { // Grade B
                grade = 2;
            } else if (chance < 35) { // Grade C
                grade = 1;
            } else { // grade == 0
                grade = Randomizer.nextInt(100) < 25 ? 5 : 0; // 25% again to get premium ticket piece				
            }
            int newId = 0;
            if (grade == 5) {
                newId = 4420000;
            } else {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final List<StructItemOption> pots = new LinkedList<>(ii.getAllSocketInfo(grade).values());
                while (newId == 0) {
                    StructItemOption pot = pots.get(Randomizer.nextInt(pots.size()));
                    if (pot != null) {
                        newId = pot.opID;
                    }
                }
            }
            final Item item = MapleInventoryManipulator.addbyId_Gachapon(c, newId, (short) 1);
            if (item == null) {
                return -1;
            }
            if (grade >= 2 && grade != 5) {
                World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(c.getPlayer().getName(), " : got a(n)", item, (byte) 0, "Maple World"));
            }
            c.announce(InfoPacket.getShowItemGain(newId, (short) 1, true));
            gainItem(2430748, (short) 1);
            gainItemSilent(5220094, (short) -1);
            return item.getItemId();
        } catch (Exception e) {
            System.out.println("[Error] Failed to use Nebulite Gachapon. " + e);
        }
        return -1;
    }

    public void changeJob(int job) {
        c.getPlayer().changeJob(job);
    }

    public void startQuest(int idd) {
        MapleQuest.getInstance(idd).start(getPlayer(), id);
    }

    public void completeQuest(int idd) {
        MapleQuest.getInstance(idd).complete(getPlayer(), id);
    }

    public void forfeitQuest(int idd) {
        MapleQuest.getInstance(idd).forfeit(getPlayer());
    }

    public void forceStartQuest() {
        MapleQuest.getInstance(id2).forceStart(getPlayer(), getNpc(), null);
    }

    public void forceStartQuest(int idd) {
        MapleQuest.getInstance(idd).forceStart(getPlayer(), getNpc(), null);
    }

    public void forceStartQuest(String customData) {
        MapleQuest.getInstance(id2).forceStart(getPlayer(), getNpc(), customData);
    }

    public void forceCompleteQuest() {
        MapleQuest.getInstance(id2).forceComplete(getPlayer(), getNpc());
    }

    public void forceCompleteQuest(final int idd) {
        MapleQuest.getInstance(idd).forceComplete(getPlayer(), getNpc());
    }

    public String getQuestCustomData() {
        return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id2)).getCustomData();
    }

    public void setQuestCustomData(String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(id2)).setCustomData(customData);
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainAp(final int amount) {
        c.getPlayer().gainAp(amount);
    }

    public void expandInventory(byte type, int amt) {
        c.getPlayer().expandInventory(type, amt);
    }

    public void unequipEverything() {
        MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Short> ids = new LinkedList<Short>();
        for (Item item : equipped.newList()) {
            ids.add(item.getPosition());
        }
        for (short id : ids) {
            MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
        }
    }

    public final void clearSkills() {
        final Map<Skill, SkillEntry> skills = new HashMap<>(getPlayer().getSkills());
        final Map<Skill, SkillEntry> newList = new HashMap<>();
        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            newList.put(skill.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
        }
        getPlayer().changeSkillsLevel(newList);
        newList.clear();
        skills.clear();
    }

    public boolean hasSkill(int skillid) {
        Skill theSkill = SkillFactory.getSkill(skillid);
        if (theSkill != null) {
            return c.getPlayer().getSkillLevel(theSkill) > 0;
        }
        return false;
    }

    public void showEffect(boolean broadcast, String effect) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(CField.showEffect(effect));
        } else {
            c.announce(CField.showEffect(effect));
        }
    }

    public void playSound(boolean broadcast, String sound) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(CField.playSound(sound));
        } else {
            c.announce(CField.playSound(sound));
        }
    }

    public void environmentChange(boolean broadcast, String env) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(CField.environmentChange(env, 2));
        } else {
            c.announce(CField.environmentChange(env, 2));
        }
    }

    public void updateBuddyCapacity(int capacity) {
        c.getPlayer().setBuddyCapacity((byte) capacity);
    }

    public int getBuddyCapacity() {
        return c.getPlayer().getBuddyCapacity();
    }

    public int partyMembersInMap() {
        int inMap = 0;
        if (getPlayer().getParty() == null) {
            return inMap;
        }
        for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() != null && char2.getParty().getId() == getPlayer().getParty().getId()) {
                inMap++;
            }
        }
        return inMap;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates an empty array full of shit..
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                if (ch != null) { // double check <3
                    chars.add(ch);
                }
            }
        }
        return chars;
    }

    public void warpPartyWithExp(int mapId, int exp) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            return;
        }
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
            }
        }
    }

    public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            gainMeso(meso);
            return;
        }
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
                curChar.gainMeso(meso, true);
            }
        }
    }

    public MapleSquad getSquad(String type) {
        return c.getChannelServer().getMapleSquad(type);
    }

    public int getSquadAvailability(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        return squad.getStatus();
    }

    public boolean registerSquad(String type, int minutes, String startText) {
        if (c.getChannelServer().getMapleSquad(type) == null) {
            final MapleSquad squad = new MapleSquad(c.getChannel(), type, c.getPlayer(), minutes * 60 * 1000, startText);
            final boolean ret = c.getChannelServer().addMapleSquad(squad, type);
            if (ret) {
                final MapleMap map = c.getPlayer().getMap();

                map.broadcastMessage(CField.getClock(minutes * 60));
                map.broadcastMessage(CWvsContext.serverNotice(6, c.getPlayer().getName() + startText));
            } else {
                squad.clear();
            }
            return ret;
        }
        return false;
    }

    public boolean getSquadList(String type, byte type_) {
        try {
            final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
            if (squad == null) {
                return false;
            }
            if (type_ == 0 || type_ == 3) { // Normal viewing
                sendNext(squad.getSquadMemberString(type_));
            } else if (type_ == 1) { // Squad Leader banning, Check out banned participant
                sendSimple(squad.getSquadMemberString(type_));
            } else if (type_ == 2) {
                if (squad.getBannedMemberSize() > 0) {
                    sendSimple(squad.getSquadMemberString(type_));
                } else {
                    sendNext(squad.getSquadMemberString(type_));
                }
            }
            return true;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            return false;
        }
    }

    public byte isSquadLeader(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        } else {
            if (squad.getLeader() != null && squad.getLeader().getId() == c.getPlayer().getId()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public boolean reAdd(String eim, String squad) {
        EventInstanceManager eimz = getDisconnected(eim);
        MapleSquad squadz = getSquad(squad);
        if (eimz != null && squadz != null) {
            squadz.reAddMember(getPlayer());
            eimz.registerPlayer(getPlayer());
            return true;
        }
        return false;
    }

    public void banMember(String type, int pos) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(pos);
        }
    }

    public void acceptMember(String type, int pos) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.acceptMember(pos);
        }
    }

    public int addMember(String type, boolean join) {
        try {
            final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
            if (squad != null) {
                return squad.addMember(c.getPlayer(), join);
            }
            return -1;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            return -1;
        }
    }

    public byte isSquadMember(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        } else {
            if (squad.getMembers().contains(c.getPlayer())) {
                return 1;
            } else if (squad.isBanned(c.getPlayer())) {
                return 2;
            } else {
                return 0;
            }
        }
    }

    public void resetReactors() {
        getPlayer().getMap().resetReactors();
    }

    public void genericGuildMessage(int code) {
        c.announce(GuildPacket.genericGuildMessage((byte) code));
    }

    public void disbandGuild() {
        final int gid = c.getPlayer().getGuildId();
        if (gid <= 0 || c.getPlayer().getGuildRank() != 1) {
            return;
        }
        World.Guild.disbandGuild(gid);
    }

    public void increaseGuildCapacity(boolean trueMax) {
        if (c.getPlayer().getMeso() < 5000 && !trueMax) {
            c.announce(CWvsContext.serverNotice(1, "You do not have enough mesos."));
            return;
        }
        final int gid = c.getPlayer().getGuildId();
        if (gid <= 0) {
            return;
        }
        if (World.Guild.increaseGuildCapacity(gid, trueMax)) {
            if (!trueMax) {
                c.getPlayer().gainMeso(-5000, true, true);
            } else {
                gainGP(-25000);
            }
            sendNext("Your guild capacity has been raised...");
        } else if (!trueMax) {
            sendNext("Please check if your guild capacity is full. (Limit: 100)");
        } else {
            sendNext("Please check if your guild capacity is full, if you have the GP needed or if subtracting GP would decrease a guild level. (Limit: 200)");
        }
    }

    public void displayGuildRanks() {
        c.announce(GuildPacket.showGuildRanks(id, MapleGuildRanking.getInstance().getRank()));
    }

    public boolean removePlayerFromInstance() {
        if (c.getPlayer().getEventInstance() != null) {
            c.getPlayer().getEventInstance().removePlayer(c.getPlayer());
            return true;
        }
        return false;
    }

    public boolean isPlayerInstance() {
        if (c.getPlayer().getEventInstance() != null) {
            return true;
        }
        return false;
    }

    public void changeStat(byte slot, int type, int amount) {
        Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        switch (type) {
            case 0:
                sel.setStr((short) amount);
                break;
            case 1:
                sel.setDex((short) amount);
                break;
            case 2:
                sel.setInt((short) amount);
                break;
            case 3:
                sel.setLuk((short) amount);
                break;
            case 4:
                sel.setHp((short) amount);
                break;
            case 5:
                sel.setMp((short) amount);
                break;
            case 6:
                sel.setWatk((short) amount);
                break;
            case 7:
                sel.setMatk((short) amount);
                break;
            case 8:
                sel.setWdef((short) amount);
                break;
            case 9:
                sel.setMdef((short) amount);
                break;
            case 10:
                sel.setAcc((short) amount);
                break;
            case 11:
                sel.setAvoid((short) amount);
                break;
            case 12:
                sel.setHands((short) amount);
                break;
            case 13:
                sel.setSpeed((short) amount);
                break;
            case 14:
                sel.setJump((short) amount);
                break;
            case 15:
                sel.setUpgradeSlots((short) amount);
                break;
            case 16:
                sel.setViciousHammer((byte) amount);
                break;
            case 17:
                sel.setLevel((byte) amount);
                break;
            case 18:
                sel.setEnhance((byte) amount);
                break;
            case 19:
                sel.setPotential1(amount);
                break;
            case 20:
                sel.setPotential2(amount);
                break;
            case 21:
                sel.setPotential3(amount);
                break;
            case 22:
                sel.setPotential4(amount);
                break;
            case 23:
                sel.setPotential5(amount);
                break;
            case 24:
                sel.setOwner(getText());
                break;
            default:
                break;
        }
        c.getPlayer().equipChanged();
        c.getPlayer().fakeRelog();
    }

    public void openDuey() {
        c.getPlayer().setConversation(2);
        c.announce(CField.sendDuey((byte) 9, null));
    }

    public void openMerchantItemStore() {
        c.getPlayer().setConversation(3);
        HiredMerchantHandler.displayMerch(c);
        //c.announce(PlayerShopPacket.merchItemStore((byte) 0x22));
        //c.getPlayer().dropMessage(5, "Please enter ANY 13 characters.");
    }

    public void sendPVPWindow() {
        c.announce(UIPacket.openUI(50));
        c.announce(CField.sendPVPMaps());
    }

    public void sendRepairWindow() {
        c.announce(UIPacket.sendRepairWindow(id));
    }

    public void sendProfessionWindow() {
        c.announce(UIPacket.openUI(42));
    }

    public final int getDojoPoints() {
        return dojo_getPts();
    }

    public final int getDojoRecord() {
        return c.getPlayer().getIntNoRecord(GameConstants.DOJO_RECORD);
    }

    public void setDojoRecord(final boolean reset) {
        if (reset) {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO_RECORD)).setCustomData("0");
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO)).setCustomData("0");
        } else {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO_RECORD)).setCustomData(String.valueOf(c.getPlayer().getIntRecord(GameConstants.DOJO_RECORD) + 1));
        }
    }

    public boolean start_DojoAgent(final boolean dojo, final boolean party) {
        if (dojo) {
            return Event_DojoAgent.warpStartDojo(c.getPlayer(), party);
        }
        return Event_DojoAgent.warpStartAgent(c.getPlayer(), party);
    }

    public boolean start_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpStartPyramid(c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpStartSubway(c.getPlayer());
    }

    public boolean bonus_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpBonusPyramid(c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpBonusSubway(c.getPlayer());
    }

    public final short getKegs() {
        return c.getChannelServer().getFireWorks().getKegsPercentage();
    }

    public void giveKegs(final int kegs) {
        c.getChannelServer().getFireWorks().giveKegs(c.getPlayer(), kegs);
    }

    public final short getSunshines() {
        return c.getChannelServer().getFireWorks().getSunsPercentage();
    }

    public void addSunshines(final int kegs) {
        c.getChannelServer().getFireWorks().giveSuns(c.getPlayer(), kegs);
    }

    public final short getDecorations() {
        return c.getChannelServer().getFireWorks().getDecsPercentage();
    }

    public void addDecorations(final int kegs) {
        try {
            c.getChannelServer().getFireWorks().giveDecs(c.getPlayer(), kegs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final MapleCarnivalParty getCarnivalParty() {
        return c.getPlayer().getCarnivalParty();
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return c.getPlayer().getNextCarnivalRequest();
    }

    public final MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
        return new MapleCarnivalChallenge(chr);
    }

    public void maxStats() {
        Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        c.getPlayer().getStat().str = (short) 32767;
        c.getPlayer().getStat().dex = (short) 32767;
        c.getPlayer().getStat().int_ = (short) 32767;
        c.getPlayer().getStat().luk = (short) 32767;

        int overrDemon = GameConstants.isDemon(c.getPlayer().getJob()) ? GameConstants.getMPByJob(c.getPlayer().getJob()) : Integer.MAX_VALUE;
        c.getPlayer().getStat().maxhp = Integer.MAX_VALUE;
        c.getPlayer().getStat().maxmp = overrDemon;
        c.getPlayer().getStat().setHp(Integer.MAX_VALUE, c.getPlayer());
        c.getPlayer().getStat().setMp(overrDemon, c.getPlayer());

        statup.put(MapleStat.STR, Integer.valueOf(32767));
        statup.put(MapleStat.DEX, Integer.valueOf(32767));
        statup.put(MapleStat.LUK, Integer.valueOf(32767));
        statup.put(MapleStat.INT, Integer.valueOf(32767));
        statup.put(MapleStat.HP, Integer.valueOf(99999));
        statup.put(MapleStat.MAXHP, Integer.valueOf(99999));
        statup.put(MapleStat.MP, Integer.valueOf(overrDemon));
        statup.put(MapleStat.MAXMP, Integer.valueOf(overrDemon));
        c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
        c.announce(CWvsContext.updatePlayerStats(statup, c.getPlayer()));
    }

    public Triple<String, Map<Integer, String>, Long> getSpeedRun(String typ) {
        final ExpeditionType type = ExpeditionType.valueOf(typ);
        if (SpeedRunner.getSpeedRunData(type) != null) {
            return SpeedRunner.getSpeedRunData(type);
        }
        return new Triple<String, Map<Integer, String>, Long>("", new HashMap<Integer, String>(), 0L);
    }

    public boolean getSR(Triple<String, Map<Integer, String>, Long> ma, int sel) {
        if (ma.mid.get(sel) == null || ma.mid.get(sel).length() <= 0) {
            dispose();
            return false;
        }
        sendOk(ma.mid.get(sel));
        return true;
    }

    public Equip getEquip(int itemid) {
        return (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemid);
    }

    public void setExpiration(Object statsSel, long expire) {
        if (statsSel instanceof Equip) {
            ((Equip) statsSel).setExpiration(System.currentTimeMillis() + (expire * 24 * 60 * 60 * 1000));
        }
    }

    public void setLock(Object statsSel) {
        if (statsSel instanceof Equip) {
            Equip eq = (Equip) statsSel;
            if (eq.getExpiration() == -1) {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
            } else {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
        }
    }

    public boolean addFromDrop(Object statsSel) {
        if (statsSel instanceof Item) {
            final Item it = (Item) statsSel;
            return MapleInventoryManipulator.checkSpace(getClient(), it.getItemId(), it.getQuantity(), it.getOwner()) && MapleInventoryManipulator.addFromDrop(getClient(), it, false);
        }
        return false;
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
        return replaceItem(slot, invType, statsSel, offset, type, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        Item item = getPlayer().getInventory(inv).getItem((byte) slot);
        if (item == null || statsSel instanceof Item) {
            item = (Item) statsSel;
        }
        if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
                return false;
            }
            Equip eq = (Equip) item;
            if (takeSlot) {
                if (eq.getUpgradeSlots() < 1) {
                    return false;
                } else {
                    eq.setUpgradeSlots((short) (eq.getUpgradeSlots() - 1));
                }
                if (eq.getExpiration() == -1) {
                    eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
                } else {
                    eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
                }
            }
            if (type.equalsIgnoreCase("Slots")) {
                eq.setUpgradeSlots((short) (eq.getUpgradeSlots() + offset));
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("Level")) {
                eq.setLevel((byte) (eq.getLevel() + offset));
            } else if (type.equalsIgnoreCase("Hammer")) {
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("STR")) {
                eq.setStr((short) (eq.getStr() + offset));
            } else if (type.equalsIgnoreCase("DEX")) {
                eq.setDex((short) (eq.getDex() + offset));
            } else if (type.equalsIgnoreCase("INT")) {
                eq.setInt((short) (eq.getInt() + offset));
            } else if (type.equalsIgnoreCase("LUK")) {
                eq.setLuk((short) (eq.getLuk() + offset));
            } else if (type.equalsIgnoreCase("HP")) {
                eq.setHp((short) (eq.getHp() + offset));
            } else if (type.equalsIgnoreCase("MP")) {
                eq.setMp((short) (eq.getMp() + offset));
            } else if (type.equalsIgnoreCase("WATK")) {
                eq.setWatk((short) (eq.getWatk() + offset));
            } else if (type.equalsIgnoreCase("MATK")) {
                eq.setMatk((short) (eq.getMatk() + offset));
            } else if (type.equalsIgnoreCase("WDEF")) {
                eq.setWdef((short) (eq.getWdef() + offset));
            } else if (type.equalsIgnoreCase("MDEF")) {
                eq.setMdef((short) (eq.getMdef() + offset));
            } else if (type.equalsIgnoreCase("Speed")) {
                eq.setSpeed((short) (eq.getSpeed() + offset));
            } else if (type.equalsIgnoreCase("ItemEXP")) {
                eq.setItemEXP(eq.getItemEXP() + offset);
            } else if (type.equalsIgnoreCase("Expiration")) {
                eq.setExpiration((long) (eq.getExpiration() + offset));
            } else if (type.equalsIgnoreCase("Flag")) {
                eq.setFlag((byte) (eq.getFlag() + offset));
            }
            item = eq.copy();
        }
        MapleInventoryManipulator.removeFromSlot(getClient(), inv, (short) slot, item.getQuantity(), false);
        return MapleInventoryManipulator.addFromDrop(getClient(), item, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
        return replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
    }

    public int getTotalStat(final int itemId) {
        return MapleItemInformationProvider.getInstance().getTotalStat((Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId));
    }

    public int getReqLevel(final int itemId) {
        return MapleItemInformationProvider.getInstance().getReqLevel(itemId);
    }

    public MapleStatEffect getEffect(int buff) {
        return MapleItemInformationProvider.getInstance().getItemEffect(buff);
    }

    public void buffGuild(final int buff, final int duration, final String msg) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getItemEffect(buff) != null && getPlayer().getGuildId() > 0) {
            final MapleStatEffect mse = ii.getItemEffect(buff);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr.getGuildId() == getPlayer().getGuildId()) {
                        mse.applyTo(chr, chr, true, null, duration);
                        chr.dropMessage(5, "Your guild has gotten a " + msg + " buff.");
                    }
                }
            }
        }
    }

    public boolean createAlliance(String alliancename) {
        MapleParty pt = c.getPlayer().getParty();
        MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
        if (otherChar == null || otherChar.getId() == c.getPlayer().getId()) {
            return false;
        }
        try {
            return World.Alliance.createAlliance(alliancename, c.getPlayer().getId(), otherChar.getId(), c.getPlayer().getGuildId(), otherChar.getGuildId());
        } catch (Exception re) {
            re.printStackTrace();
            return false;
        }
    }

    public boolean addCapacityToAlliance() {
        try {
            final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
                if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && World.Alliance.changeAllianceCapacity(gs.getAllianceId())) {
                    gainMeso(-MapleGuildAlliance.CHANGE_CAPACITY_COST);
                    return true;
                }
            }
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    public boolean disbandAlliance() {
        try {
            final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
                if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && World.Alliance.disbandAlliance(gs.getAllianceId())) {
                    return true;
                }
            }
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    public byte getLastMsg() {
        return lastMsg;
    }

    public final void setLastMsg(final byte last) {
        this.lastMsg = last;
    }

    public final void maxAllSkills() {
        HashMap<Skill, SkillEntry> sa = new HashMap<>();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isApplicableSkill(skil.getId()) && skil.getId() < 90000000) { //no db/additionals/resistance skills
                sa.put(skil, new SkillEntry(skil.getMaxLevel(), skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        }
        getPlayer().changeSkillsLevel(sa);
    }

    public final void maxSkillsByJob() {
        HashMap<Skill, SkillEntry> sa = new HashMap<>();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isApplicableSkill(skil.getId()) && skil.canBeLearnedBy(getPlayer().getJob())) { //no db/additionals/resistance skills
                sa.put(skil, new SkillEntry(skil.getMaxLevel(), skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        }
        getPlayer().changeSkillsLevel(sa);
    }

    public final void maxSkillLevelByJob() {
        HashMap<Skill, SkillEntry> sa = new HashMap<>();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isApplicableSkill(skil.getId()) && skil.canBeLearnedBy(getPlayer().getJob())) { //no db/additionals/resistance skills
                sa.put(skil, new SkillEntry(skil.getMaxLevel(), skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        }
        getPlayer().changeSkillsLevel(sa);
    }

    public final void resetStats(int str, int dex, int z, int luk) {
        c.getPlayer().resetStats(str, dex, z, luk);
    }

    public final boolean dropItem(int slot, int invType, int quantity) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        return MapleInventoryManipulator.drop(c, inv, (short) slot, (short) quantity, true);
    }

    public final List<Integer> getAllPotentialInfo() {
        List<Integer> list = new ArrayList<Integer>(MapleItemInformationProvider.getInstance().getAllPotentialInfo().keySet());
        Collections.sort(list);
        return list;
    }

    public final List<Integer> getAllPotentialInfoSearch(String content) {
        List<Integer> list = new ArrayList<>();
        for (Entry<Integer, List<StructItemOption>> i : MapleItemInformationProvider.getInstance().getAllPotentialInfo().entrySet()) {
            for (StructItemOption ii : i.getValue()) {
                if (ii.toString().contains(content)) {
                    list.add(i.getKey());
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public final String getPotentialInfo(final int id) {
        final List<StructItemOption> potInfo = MapleItemInformationProvider.getInstance().getPotentialInfo(id);
        final StringBuilder builder = new StringBuilder("#b#ePOTENTIAL INFO FOR ID: ");
        builder.append(id);
        builder.append("#n#k\r\n\r\n");
        int minLevel = 1, maxLevel = 10;
        for (StructItemOption item : potInfo) {
            builder.append("#eLevels ");
            builder.append(minLevel);
            builder.append("~");
            builder.append(maxLevel);
            builder.append(": #n");
            builder.append(item.toString());
            minLevel += 10;
            maxLevel += 10;
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public final void sendRPS() {
        c.announce(CField.getRPSMode((byte) 8, -1, -1, -1));
    }

    public final void setQuestRecord(Object ch, final int questid, final String data) {
        ((MapleCharacter) ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
    }

    public final void doWeddingEffect(final Object ch) {
        final MapleCharacter chr = (MapleCharacter) ch;
        final MapleCharacter player = getPlayer();
        getMap().broadcastMessage(CWvsContext.yellowChat(player.getName() + ", do you take " + chr.getName() + " as your wife and promise to stay beside her through all downtimes, crashes, and lags?"));
        CloneTimer.getInstance().schedule(new Runnable() {

            public void run() {
                if (chr == null || player == null) {
                    warpMap(680000500, 0);
                } else {
                    chr.getMap().broadcastMessage(CWvsContext.yellowChat(chr.getName() + ", do you take " + player.getName() + " as your husband and promise to stay beside him through all downtimes, crashes, and lags?"));
                }
            }
        }, 10000);
        CloneTimer.getInstance().schedule(new Runnable() {

            public void run() {
                if (chr == null || player == null) {
                    if (player != null) {
                        setQuestRecord(player, 160001, "3");
                        setQuestRecord(player, 160002, "0");
                    } else if (chr != null) {
                        setQuestRecord(chr, 160001, "3");
                        setQuestRecord(chr, 160002, "0");
                    }
                    warpMap(680000500, 0);
                } else {
                    setQuestRecord(player, 160001, "2");
                    setQuestRecord(chr, 160001, "2");
                    sendNPCText(player.getName() + " and " + chr.getName() + ", I wish you two all the best on your " + chr.getClient().getChannelServer().getServerName() + " journey together!", 9201002);
                    chr.getMap().startExtendedMapEffect("You may now kiss the bride, " + player.getName() + "!", 5120006);
                    if (chr.getGuildId() > 0) {
                        World.Guild.guildPacket(chr.getGuildId(), CWvsContext.sendMarriage(false, chr.getName()));
                    }
                    if (chr.getFamilyId() > 0) {
                        World.Family.familyPacket(chr.getFamilyId(), CWvsContext.sendMarriage(true, chr.getName()), chr.getId());
                    }
                    if (player.getGuildId() > 0) {
                        World.Guild.guildPacket(player.getGuildId(), CWvsContext.sendMarriage(false, player.getName()));
                    }
                    if (player.getFamilyId() > 0) {
                        World.Family.familyPacket(player.getFamilyId(), CWvsContext.sendMarriage(true, chr.getName()), player.getId());
                    }
                }
            }
        }, 20000); //10 sec 10 sec

    }

    public boolean checkKey(int key) {

        return false;
    }

    public void putKey(int key, int type, int action) {
        getPlayer().changeKeybinding(key, (byte) type, action);
        getPlayer().getKeyLayout().saveKeysbyJob(getPlayer().getId(), getPlayer().getJob());
        getClient().announce(CField.getKeymap(getPlayer().getKeyLayout(), getPlayer().getJob()));
    }

    public void logDonator(String log, int previous_points) {
        final StringBuilder logg = new StringBuilder();
        logg.append(MapleCharacterUtil.makeMapleReadable(getPlayer().getName()));
        logg.append(" [CID: ").append(getPlayer().getId()).append("] ");
        logg.append(" [Account: ").append(MapleCharacterUtil.makeMapleReadable(getClient().getAccountName())).append("] ");
        logg.append(log);
        logg.append(" [Previous: " + previous_points + "] [Now: " + getPlayer().getPoints() + "]");

        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO donorlog VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, MapleCharacterUtil.makeMapleReadable(getClient().getAccountName()));
            ps.setInt(2, getClient().getAccID());
            ps.setString(3, MapleCharacterUtil.makeMapleReadable(getPlayer().getName()));
            ps.setInt(4, getPlayer().getId());
            ps.setString(5, log);
            ps.setString(6, FileoutputUtil.CurrentReadable_Time());
            ps.setInt(7, previous_points);
            ps.setInt(8, getPlayer().getPoints());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        FileoutputUtil.log(FileoutputUtil.Donator_Log, logg.toString());
    }

    public void doRing(final String name, final int itemid) {
        PlayersHandler.DoRing(getClient(), name, itemid);
    }

    public int getNaturalStats(final int itemid, final String it) {
        Map<String, Integer> eqStats = MapleItemInformationProvider.getInstance().getEquipStats(itemid);
        if (eqStats != null && eqStats.containsKey(it)) {
            return eqStats.get(it);
        }
        return 0;
    }

    public boolean isEligibleName(String t) {
        return MapleCharacterUtil.canCreateChar(t, getPlayer().isGM()) && (!LoginInformationProvider.getInstance().isForbiddenName(t) || getPlayer().isGM());
    }

    public List<Integer> getDropItems(int type) {
        var list = MapleMonsterInformationProvider.getInstance().getAllDropItemsType(type);
        Collections.sort(list);
        return list;
    }

    public String whoDrop(int itemid) {
        List<Integer> mobNames = new LinkedList<>();
        String name = "";
        var drops = MapleMonsterInformationProvider.getInstance().getMobsFromItemId(itemid);
        if (drops != null && !drops.isEmpty()) {
            var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
            for (int mob : drops) {
                name += "     " + star + " #r" + MapleLifeFactory.getName(mob) + "#k (" + mob + ")\r\n";
            }
        } else {
            name = "\r\nThis item does not drop from any monsters.\r\n#rMost likely a crafted item.#k\r\n";
        }
        return name;

    }

    public String checkDrop(MapleMonster mob) {
        if (mob != null) {
            int mobId = mob.getId();
            List<MonsterDropEntry> udrops = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
            List<MonsterGlobalDropEntry> globalEntry = MapleMonsterInformationProvider.getInstance().getGlobalDrop();
            List<MonsterDropEntry> drops = new LinkedList<>();

            if (udrops != null && !udrops.isEmpty()) {
                for (MonsterDropEntry drop : udrops) {
                    boolean check = mob.getStats().getBar() && mob.getStats().isExplosiveReward();
                    if (check) {
                        drops.add(drop);
                    } else {
                        if (GameConstants.getInventoryType(drop.itemId) != MapleInventoryType.EQUIP) {
                            drops.add(drop);
                        }
                    }
                }
                int total = drops.size() + globalEntry.size();
                int num = 0, itemId = 0, ch = 0;
                MonsterDropEntry de;
                MonsterGlobalDropEntry df;
                StringBuilder name = new StringBuilder();
                name.append(total + " Drops found for ID: \r\n#b").append(mobId).append(" - #o").append(mobId).append("##k \r\n#rEquips have dimishing drop rates#k\r\n");
                name.append("--------------------------------------\r\n");
                double basedrop = getClient().getChannelServer().getDropRate() * (c.getPlayer() != null ? c.getPlayer().getDropMod() : 1);
                int cbon = mob.getStats().ultimate ? 2 : 1;
                double itemAmount = 1.0;
                if (c.getPlayer() != null) {
                    itemAmount = 1.0 + (c.getPlayer().getStat().getItemKpRate() + c.getPlayer().getETCMod());
                }
                for (int i = 0; i < drops.size(); i++) {
                    de = drops.get(i);
                    if (de.chance > 0 && (de.questid <= 0 || (de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0))) {
                        itemId = de.itemId;
                        String namez = "#z" + itemId + "#";
                        if (itemId == 0) { //meso
                            itemId = 4031041; //display sack of cash
                            namez = (de.Minimum * getClient().getChannelServer().getMesoRate()) + " to " + (de.Maximum * getClient().getChannelServer().getMesoRate()) + " meso";
                        }
                        if (de.rare > 0) {
                            ch = Randomizer.Max((int) (de.chance), 10000000);
                        } else {
                            ch = Randomizer.Max((int) (de.chance * basedrop), 10000000);
                        }
                        int min = (int) (de.Minimum);
                        int max = (int) (de.Maximum);
                        if (de.rare == 0) {
                            if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.USE || GameConstants.getInventoryType(de.itemId) == MapleInventoryType.ETC) {
                                min = (int) Math.floor(de.Minimum);
                                max = (int) Math.floor(de.Maximum * itemAmount);
                            }
                        } else {
                            if (c.getPlayer() != null) {
                                min = (int) Math.floor(de.Minimum);
                                max = (int) Math.floor(de.Maximum * c.getPlayer().getStat().getItemKpRate());
                            }
                        }
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append(" - ID: ").append(itemId).append(de.rare > 0 ? " (#rRare#k)" : "").append("\r\n");
                        name.append("     Chance: #b").append(Integer.valueOf(ch >= 10000000 ? 10000000 : ch).doubleValue() / 100000.0).append("#k%").append(" ");
                        if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                            name.append("- Drop Power: ").append("#b" + (mob.getStats().getTier() * mob.getStats().getTier() * 10) + "#k%").append(" ");
                        } else {
                            if (max > 1) {
                                name.append("- Drop Amount: ").append("#b" + min + "#k").append("-#b" + max + "#k").append(" ");
                            } else {
                                name.append("- Drop Amount: #b1#k");
                            }
                        }
                        //name.append("Chance: ").append(Integer.valueOf(ch >= 10000000 ? 10000000 : ch).doubleValue() / 100000.0).append("%").append(" ");
                        name.append("\r\n\r\n");
                        num++;
                    }
                }
                for (int i = 0; i < globalEntry.size(); i++) {
                    df = globalEntry.get(i);
                    if (c.getPlayer() != null) {
                        itemAmount = 1.0 + (c.getPlayer().getStat().getItemKpRate() + c.getPlayer().getETCMod());
                    }
                    if (df.chance > 0 && (df.questid <= 0 || (df.questid > 0 && MapleQuest.getInstance(df.questid).getName().length() > 0))) {
                        itemId = df.itemId;
                        String namez = "#z" + itemId + "#";
                        if (itemId == 0) { //meso
                            itemId = 4031041; //display sack of cash
                            namez = (df.Minimum * getClient().getChannelServer().getMesoRate()) + " to " + (df.Maximum * getClient().getChannelServer().getMesoRate()) + " meso";
                        }
                        ch = Randomizer.Max((int) (df.chance * basedrop), 100000);
                        int min = df.Minimum;
                        int max = df.Maximum;
                        if (GameConstants.getInventoryType(df.itemId) == MapleInventoryType.ETC) {
                            min = (int) (df.Minimum);
                            max = (int) (df.Maximum * itemAmount);
                        }
                        name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append(" - ID: ").append(itemId).append(" (#bGlobal#k)").append("\r\n");
                        name.append("     Chance: #b").append(Integer.valueOf(ch >= 100000 ? 100000 : ch).doubleValue() / 1000.0).append("#k%").append(" ");
                        if (GameConstants.getInventoryType(df.itemId) == MapleInventoryType.EQUIP) {
                            name.append("- Drop Power: ").append("#b" + (mob.getStats().getTier() * mob.getStats().getTier() * 10) + "#k%").append(" ");
                        } else {
                            name.append("- Drop Amount: ").append("#b" + min + "#k").append("-#b" + max + "#k").append(" ");
                        }
                        //name.append("Chance: ").append(Integer.valueOf(ch >= 10000000 ? 10000000 : ch).doubleValue() / 100000.0).append("%").append(" ");
                        name.append("\r\n\r\n");
                        num++;
                    }
                }
                if (name.length() > 0) {
                    return name.toString();
                }
            }

        }
        return "No drops was returned.";
    }

    public String getLeftPadded(final String in, final char padchar, final int length) {
        return StringUtil.getLeftPaddedStr(in, padchar, length);
    }

    public void handleDivorce() {
        if (getPlayer().getMarriageId() <= 0) {
            sendNext("Please make sure you have a marriage.");
            return;
        }
        final int chz = World.Find.findChannel(getPlayer().getMarriageId());
        if (chz == -1) {
            //sql queries
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE queststatus SET customData = ? WHERE characterid = ? AND (quest = ? OR quest = ?)");
                ps.setString(1, "0");
                ps.setInt(2, getPlayer().getMarriageId());
                ps.setInt(3, 160001);
                ps.setInt(4, 160002);
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("UPDATE characters SET marriageid = ? WHERE id = ?");
                ps.setInt(1, 0);
                ps.setInt(2, getPlayer().getMarriageId());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                outputFileError(e);
                return;
            }
            setQuestRecord(getPlayer(), 160001, "0");
            setQuestRecord(getPlayer(), 160002, "0");
            getPlayer().setMarriageId(0);
            sendNext("You have been successfully divorced...");
            return;
        } else if (chz < -1) {
            sendNext("Please make sure your partner is logged on.");
            return;
        }
        MapleCharacter cPlayer = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterById(getPlayer().getMarriageId());
        if (cPlayer != null) {
            cPlayer.dropMessage(1, "Your partner has divorced you.");
            cPlayer.setMarriageId(0);
            setQuestRecord(cPlayer, 160001, "0");
            setQuestRecord(getPlayer(), 160001, "0");
            setQuestRecord(cPlayer, 160002, "0");
            setQuestRecord(getPlayer(), 160002, "0");
            getPlayer().setMarriageId(0);
            sendNext("You have been successfully divorced...");
        } else {
            sendNext("An error occurred...");
        }
    }

    public String getReadableMillis(long startMillis, long endMillis) {
        return StringUtil.getReadableMillis(startMillis, endMillis);
    }

    public void sendUltimateExplorer() {
        getClient().announce(CWvsContext.ultimateExplorer());
    }

    public String getPokemonRanking() {
        StringBuilder sb = new StringBuilder();
        for (PokemonInformation pi : RankingWorker.getPokemonInfo()) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public String getPokemonRanking_Caught() {
        StringBuilder sb = new StringBuilder();
        for (PokedexInformation pi : RankingWorker.getPokemonCaught()) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public String getPokemonRanking_Ratio() {
        StringBuilder sb = new StringBuilder();
        for (PokebattleInformation pi : RankingWorker.getPokemonRatio()) {
            sb.append(pi.toString());
        }
        return sb.toString();
    }

    public void sendPendant(boolean b) {
        c.announce(CWvsContext.pendantSlot(b));
    }

    public Triple<Integer, Integer, Integer> getCompensation() {
        Triple<Integer, Integer, Integer> ret = null;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM compensationlog_confirmed WHERE chrname LIKE ?");
            ps.setString(1, getPlayer().getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = new Triple<Integer, Integer, Integer>(rs.getInt("value"), rs.getInt("taken"), rs.getInt("donor"));
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
            return ret;
        }
    }

    public boolean deleteCompensation(int taken) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE compensationlog_confirmed SET taken = ? WHERE chrname LIKE ?");
            ps.setInt(1, taken);
            ps.setString(2, getPlayer().getName());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
            return false;
        }
    }

    /*Start of Custom Features*/
    public void gainAPS(int gain) {
        getPlayer().gainAPS(gain);
    }

    /*End of Custom Features*/
    public int[] getHairs() {
        int size = MapleItemInformationProvider.getInstance().getHairs().size();
        int[] result = new int[size];
        for (int n = 0; n < size; ++n) {
            result[n] = MapleItemInformationProvider.getInstance().getHairs().get(n);
        }
        return result;
    }

    public int[] getFaces() {
        int size = MapleItemInformationProvider.getInstance().getFaces().size();
        int[] result = new int[size];
        for (int n = 0; n < size; ++n) {
            result[n] = MapleItemInformationProvider.getInstance().getFaces().get(n);
        }
        return result;
    }

    public void displayPlayerRanks() {
        displayPlayerRanks(getClient(), npc);
    }

    public void displayPlayerRanks(MapleClient c, int npcid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `totallevel` FROM characters WHERE `gm` < 2 ORDER BY `totallevel` DESC LIMIT 50");
            try (ResultSet rs = ps.executeQuery()) {
                c.announce(CWvsContext.getPlayerRanking(rs));
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("failed to display dojo ranks. " + e);
        }
    }

    public int getServerLevel() {
        int total = 0;
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            if (!cserv.getPlayerStorage().getAllCharacters().isEmpty()) {
                int count = 0;
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr != null && !chr.isGM()) {
                        total += chr.getTotalLevel();
                        count++;
                    }
                }
                total /= count;
            }
        }
        return total;
    }

    public static String resolvePotentialID(final Item item, final int potID) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<StructItemOption> potInfo = MapleItemInformationProvider.getInstance().getPotentialInfo(potID);
        //gets the "real potential level" 
        int itemLevel = (int) Math.floor((double) ii.getReqLevel(item.getItemId()) / 10);

        if (potID == 0) {
            return "No potential";
        } else if (potID < 0) {
            return "Hidden potential";
        }
        StructItemOption st = potInfo.get(Randomizer.MinMax(itemLevel - 1, 0, 19));

        String sb = st.potName;
        for (int i = 0; i < st.potName.length(); i++) {
            //# denotes the beginning of the parameter name that needs to be replaced, e.g. "Weapon DEF: +#incPDD" 
            if (st.potName.charAt(i) == '#') {
                int j = i + 2;
                while ((j < st.potName.length()) && st.potName.substring(i + 1, j).matches("^[a-zA-Z]+$")) {
                    j++;
                }
                String curParam = st.potName.substring(i, j);
                String curParamStripped;
                //get rid of any trailing percent signs on the parameter name 
                if (j != st.potName.length() || st.potName.charAt(st.potName.length() - 1) == '%') { //hacky 
                    curParamStripped = curParam.substring(1, curParam.length() - 1);
                } else {
                    curParamStripped = curParam.substring(1);
                }

                String paramValue = Integer.toString(st.get(curParamStripped));

                if (curParam.charAt(curParam.length() - 1) == '%') {
                    paramValue = paramValue.concat("%");
                }
                sb = sb.replace(curParam, paramValue);
            }
        }
        return sb;
    }

    public void cube(Item item, int type) {
        Equip eqp = (Equip) item;
        eqp.resetPotential(type);
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void reset(Item item) {
        Equip eqp = (Equip) item;
        eqp.resetFull(c.getPlayer().getTotalLevel());
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void resetFullFixed(Item item) {
        Equip eqp = (Equip) item;
        eqp.resetFullFixed(c.getPlayer().getTotalLevel());
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void resetFullFixed(Item item, int power) {
        Equip eqp = (Equip) item;
        eqp.resetFullFixed(power);
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void resetNXFixed(Item item, int power) {
        Equip eqp = (Equip) item;
        eqp.resetStatsFixed(power);
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void resetNX(Item item, int power) {
        Equip eqp = (Equip) item;
        eqp.resetStats(power);
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public final boolean repairAll() {
        Equip eq;
        double rPercentage;
        int price = 0;
        Map<String, Integer> eqStats;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<Equip, Integer> eqs = new HashMap<Equip, Integer>();
        final MapleInventoryType[] types = {MapleInventoryType.EQUIP, MapleInventoryType.EQUIPPED};
        for (MapleInventoryType type : types) {
            for (Item item : c.getPlayer().getInventory(type).newList()) {
                if (item instanceof Equip) { //redundant
                    eq = (Equip) item;
                    if (eq.getDurability() >= 0) {
                        eqStats = ii.getEquipStats(eq.getItemId());
                        if (eqStats.containsKey("durability") && eqStats.get("durability") > 0 && eq.getDurability() < eqStats.get("durability")) {
                            rPercentage = (100.0 - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
                            eqs.put(eq, eqStats.get("durability"));
                            price += (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0));
                        }
                    }
                }
            }
        }
        if (eqs.size() <= 0 || c.getPlayer().getMeso() < price) {
            return false;
        }
        c.getPlayer().gainMeso(-price, true);
        Equip ez;
        for (Entry<Equip, Integer> eqqz : eqs.entrySet()) {
            ez = eqqz.getKey();
            ez.setDurability(eqqz.getValue());
            c.getPlayer().forceReAddItem(ez.copy(), ez.getPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
        }
        return true;
    }

    public void broadcastSlotMessage(String msg) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                victim.dropMessage(-6, msg);
            }
        }
    }

    public void broadcastServerMessage(int type, String msg) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                victim.dropMessage(type, msg);
            }
        }
    }

    public void broadcastMapMessage(int type, String msg) {
        for (MapleCharacter victim : c.getPlayer().getMap().getAllPlayers()) {
            victim.dropMessage(type, msg);
        }
    }

    public void broadcastMessage(int type, String msg) {
        c.getPlayer().dropMessage(type, msg);
    }

    public void singleCube(Item item, int row) {
        Equip eqp = (Equip) item;
        eqp.singlePotential(row);
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void singleCube(Item item, int row, int pots) {
        Equip eqp = (Equip) item;
        eqp.singlePotential(row, pots);
        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
        c.announce(InventoryPacket.updateItemslot(item));
    }

    public void setEnchance(Item item, int amount) {
        Equip eqp = (Equip) item;
        eqp.setEnhance(eqp.getEnhance() + amount);
        c.announce(InventoryPacket.updateItemslot(item));
        if (eqp.getEnhance() == 50) {
            c.getPlayer().finishAchievement(126);
        }
    }

    public void soulUpgrade(Item item, int tier) {
        Equip eqp = (Equip) item;
        if (eqp.getUpgradeSlots() > 0) {
            int cap = Randomizer.Max((int) (99999999 + (c.getPlayer().getReborns() * 10000000)), 999999999);
            eqp.randomBonusStats(getPlayer(), eqp, tier, cap);
            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, 5062006));
            c.announce(InventoryPacket.updateItemslot(item));
        }
    }

    public Point getNpcPos() {
        return c.getPlayer().getMap().getNPCByOid(npcOid).getPosition();
    }

    public boolean isCloseNpc() {
        int distance = (int) c.getPlayer().getMap().getNPCByOid(c.getPlayer().getNpcOid()).getPosition().distanceSq(c.getPlayer().getPosition());
        return distance <= 30000;
    }

    public boolean isCloseNpc(int range) {
        int distance = (int) c.getPlayer().getMap().getNPCByOid(c.getPlayer().getNpcOid()).getPosition().distanceSq(c.getPlayer().getPosition());
        return distance <= range;
    }

    public void delayNPC(final int mode, int time, final String msg, final byte type) {
        TimerManager.getInstance().schedule(() -> {
            if (c.getPlayer() != null) {
                //c.announce(UIPacket.IntroDisableUI(false));
                //c.announce(UIPacket.IntroLock(false));
                switch (mode) {
                    case 0:
                        sendOkS(msg, type);
                        break;
                    case 1:
                        sendNextS(msg, type);
                        break;
                    case 2:
                        sendYesNoS(msg, type);
                        break;
                    default:
                        sendOkS(msg, type);
                }
            }
        }, time);
    }

    public void delayRewardNPC(int time) {
        TimerManager.getInstance().schedule(() -> {
            MapleCharacter player = c.getPlayer();
            if (player != null) {
                if (!player.getRewards().isEmpty()) {
                    String msg = "I have won the following items:\r\n";
                    for (Integer id : player.getRewards()) {
                        msg += "#i" + id + "# " + getItemName(id) + " - x#b" + player.getRewardAmount(id) + "#k\r\n";
                    }
                    sendOkS(msg, (byte) 16);
                    player.clearRewards();
                } else {
                    sendOkS("I did not win anything, sadface.", (byte) 16);
                }
            }
        }, time);
    }

    public void delaySingleReward(int time) {
        getClient().announce(CField.UIPacket.IntroLock(true));
        getClient().announce(CField.UIPacket.IntroDisableUI(true));
        final int rewardid = Randomizer.random(3, 20);
        c.announce(CField.showEffect("miro2/frame"));
        c.announce(CField.showEffect("miro2/RR1/" + rewardid));
        TimerManager.getInstance().schedule(() -> {
            MapleCharacter player = c.getPlayer();
            if (player != null) {
                int amount = (int) (random(1, 4));
                gainItem(2005106, 1);
                gainItem(4310501, 5);
                gainItem(2049300, 10);
                gainItem(2583000, 15);
                gainItem(4310018, getPlayer().getTotalLevel());
                gainItem(4310020, getPlayer().getTotalLevel() * 2);
                gainItem(4310015, getPlayer().getTotalLevel() * 5);
                gainMeso((int) (250000 * getPlayer().getMesoMod()));
                getPlayer().miniLevelUp(5);
                getPlayer().removeStamina(100);
                player.Reward(rewardid, amount);
                String msg = "You have claimed your rewards including this bonus reward:\r\n";
                msg += "#i2005106# " + getItemName(2005106) + " (x1)\r\n";
                msg += "#i4310501# " + getItemName(4310501) + " (x5)\r\n";
                msg += "#i2049300# " + getItemName(2049300) + " (x10)\r\n";
                msg += "#i2583000# " + getItemName(2583000) + " (x15)\r\n";
                msg += "#i4310018# " + getItemName(4310018) + " (x" + (getPlayer().getTotalLevel()) + ")\r\n";
                msg += "#i4310020# " + getItemName(4310020) + " (x" + (2 * getPlayer().getTotalLevel()) + ")\r\n";
                msg += "#i4310015# " + getItemName(4310015) + " (x" + (5 * getPlayer().getTotalLevel()) + ")\r\n";
                msg += "#fUI/UIWindow2.img/QuestIcon/7/0# #b" + (convertNumber((int) (250000 * getPlayer().getMesoMod()))) + "#k (#g+" + getPlayer().getMesoMod() * 100 + "%#k)\r\n";
                msg += "#fUI/UIWindow2.img/QuestIcon/8/0# #bMini Level Up (x5)#k\r\n";
                msg += "#bRandom Reward from slot:#k\r\n";
                for (Integer id : player.getRewards()) {
                    msg += "#i" + id + "# " + getItemName(id) + " - x#b" + amount + "#k\r\n";
                }
                getClient().announce(CField.UIPacket.IntroLock(false));
                getClient().announce(CField.UIPacket.IntroDisableUI(false));
                getClient().announce(CField.getPublicNPCInfo());
                sendOkS(msg, (byte) 16);
                player.clearRewards();
            }
        }, time);
    }

    public static record SellixCodeInfo(String code, int itemId, int amount) {

    }

    public SellixCodeInfo checkSellixCode(String code) {

        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM `sellix_available_codes` WHERE `code` = ?")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    return new SellixCodeInfo(code, rs.getInt("itemid"), rs.getInt("amount"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean tryRedeemCode(SellixCodeInfo info) {
        try (Connection con = DatabaseConnection.getWorldConnection();) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `sellix_available_codes` WHERE `code` = ?")) {
                ps.setString(1, info.code);
                if (ps.executeUpdate() == 0) {
                    con.setAutoCommit(true);
                    return false;
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO `sellix_redeemed_codes` values(?,?,?,?,?,?,?) ")) {
                ps.setString(1, info.code);
                ps.setInt(2, info.itemId);
                ps.setInt(3, info.amount);
                ps.setInt(4, getClient().getAccID());
                ps.setString(5, getClient().getAccountName());
                ps.setString(6, getClient().getSessionIPAddress());
                ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                if (ps.executeUpdate() == 0) {
                    con.setAutoCommit(true);
                    return false;
                }
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void startBattle() {
        MaplePalBattleManager.battleNpc(c.getPlayer(), id);
    }

    public void startRandomBattle(boolean rewards) {
        int bg = random(1, 36);
        int minL = random(1, 100);
        int maxL = random(minL, 100);
        int minP = random(1, 6);
        int maxP = random(minP, 6);
        int Iv = random(minL, 250);
        MaplePalBattleManager.battleNpc(c.getPlayer(), id, bg, minL, minL, maxL, minP, maxP, Iv, rewards);
    }

    public void startRandomBattle(int level, boolean rewards) {
        int bg = random(1, 36);
        int maxL = random(level, (int) Math.floor(level * 1.1));
        int minP = random(1, 6);
        int maxP = random(minP, 6);
        int Iv = random(level, 250);
        MaplePalBattleManager.battleNpc(c.getPlayer(), id, bg, level, level, maxL, minP, maxP, Iv, rewards);
    }

    public void startBattle(int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int Iv) {
        MaplePalBattleManager.battleNpc(c.getPlayer(), id, bg, level, min_level, max_level, min_pal, max_pal, Iv, true);
    }

    public void startBattle(int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int Iv, boolean rewards) {
        MaplePalBattleManager.battleNpc(c.getPlayer(), id, bg, level, min_level, max_level, min_pal, max_pal, Iv, rewards);
    }

    public void startSuperBattle(int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, double multi, boolean rewards) {
        MaplePalBattleManager.battleSuperNpc(c.getPlayer(), id, bg, level, min_level, max_level, min_pal, max_pal, iv, multi, rewards);
    }

    public void startSuperBattle(int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, double multi, boolean rewards, int ach) {
        MaplePalBattleManager.battleSuperNpc(c.getPlayer(), id, bg, level, min_level, max_level, min_pal, max_pal, iv, multi, rewards, ach);
    }

    public TrainerTemplate.Trainer getTrainer() {
        return TrainerTemplate.loadNpc(id);
    }

}
