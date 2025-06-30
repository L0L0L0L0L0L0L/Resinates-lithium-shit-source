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

import client.MapleCharacter;
import client.MapleClient;
import client.maplepal.CraftingProcessor;

import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.World;
import java.util.List;
import scripting.NPCScriptManager;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CWvsContext;

public class InterServerHandler {

    public static final void EnterCS(final MapleClient c, final boolean mts) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (chr.isChangingMaps()) {
            return;
        }
        if (chr.isOpened() || chr.isStorageOpened()) {
            return;
        }
        if (chr.hasBlockedInventory() || chr.getMap() == null || c.getChannelServer() == null) {
            chr.dropMessage(1, "Crafting is not avaiable during event instances.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.getShop() != null) {
            chr.dropMessage(1, "Crafting is not avaiable while shop is opened.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.announce(CWvsContext.enableActions());
        List<Integer> unlockedRecipes = CraftingProcessor.getAllRecipeIds();
        c.announce(CraftingProcessor.sendCraftingRecipes(unlockedRecipes));
        c.announce(CraftingProcessor.sendOpenWindow(c.getPlayer().getOverflowInv()));
    }

    public static final void EnterMTS(final MapleClient c) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (chr.isChangingMaps()) {
            return;
        }
        if (chr.hasBlockedInventory() || chr.getMap() == null || chr.getEventInstance() != null || c.getChannelServer() == null) {
            chr.dropMessage(1, "Please try again later.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.getMapId() == 5000) {
            chr.dropMessage(1, "Not usable inside the Tutoral Zone.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        chr.dropMessage(5, "You will be transported to the Free Market Entrance.");
        chr.saveLocation(SavedLocationType.fromString("FREE_MARKET"));
        final MapleMap warpz = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(870000008);
        if (warpz != null) {
            chr.changeMap(warpz, warpz.getPortal("out00"));
        } else {
            chr.dropMessage(5, "Please try again later.");
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final void Loggedin(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final int playerid = slea.readInt();
        final ChannelServer channelServer = c.getChannelServer();
        MapleCharacter player;
        final CharacterTransfer transfer = channelServer.getPlayerStorage().getPendingCharacter(playerid);

        if (transfer == null) { // Player isn't in storage, probably isn't CC
            Pair<String, String> ip = LoginServer.getLoginAuth(playerid);
            String s = c.getSessionIPAddress();
            player = MapleCharacter.loadCharFromDB(playerid, c, true);
            if (player == null) {
                System.out.println("Char failed to load on account: " + c.getAccountName());
                c.getSession().close();
                return;
            }
            if (ip == null || !s.substring(s.indexOf('/') + 1, s.length()).equals(ip.left)) {
                if (ip != null) {
                    c.setTempIP(ip.right);
                    System.out.println("Account: " + c.getAccountName() + " Error with getting IP?");
                    LoginServer.putLoginAuth(playerid, ip.left, ip.right);
                }
                c.getSession().close();
                return;
            }
        } else {
            player = MapleCharacter.ReconstructChr(transfer, c, true);
        }

        if (!c.CheckIPAddress()) { // Remote hack
            System.out.println("Remote hack from: " + c.getAccountName());
            c.getSession().close();
            return;
        }
        if (c.getlockAccount() > 0) {
            boolean allowLogin = World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
            if (allowLogin) {
                System.out.println("Account: " + c.getAccountName() + " not allowed login?");
                c.getSession().close();
            } else {
                if (player != null) {
                    c.setPlayer(player);
                    c.setAccID(player.getAccountID());
                    c.setOnline(true);
                    channelServer.addPlayer(player);
                    c.updateLoginState(MapleClient.POST_LOGGIN, c.getSessionIPAddress());
                    c.processChar(player, channelServer, true);
                }
            }
        }
    }

    public static final void ChangeChannel(final SeekableLittleEndianAccessor slea, final MapleClient c, final boolean room) {

        //c.forceDisconnect();
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            c.announce(CWvsContext.enableActions());
            return;
        }

        chr.dropMessage(1, "Changing Channels and Rooms is disabled.");
        c.announce(CWvsContext.enableActions());
    }
}
