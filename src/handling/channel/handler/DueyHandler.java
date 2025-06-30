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

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public class DueyHandler {

    /*
     * 19 = Successful
     * 18 = One-of-a-kind Item is already in Reciever's delivery
     * 17 = The Character is unable to recieve the parcel
     * 15 = Same account
     * 14 = Name does not exist
     */
    public static final void DueyOperation(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        /*
        final byte operation = slea.readByte();

        switch (operation) {
        case 1: { // Start Duey, 13 digit AS
        final String AS13Digit = slea.readMapleAsciiString();
        //		int unk = slea.readInt(); // Theres an int here, value = 1
        //  9 = error
        final int conv = c.getPlayer().getConversation();

        if (conv == 2) { // Duey
        c.announce(CField.sendDuey((byte) 10, loadItems(c.getPlayer())));
        }
        break;
        }
        case 3: { // Send Item
        if (c.getPlayer().getConversation() != 2) {
        return;
        }
        final byte inventId = slea.readByte();
        final short itemPos = slea.readShort();
        final short amount = slea.readShort();
        final int mesos = slea.readInt();
        final String recipient = slea.readMapleAsciiString();
        boolean quickdelivery = slea.readByte() > 0;

        final int finalcost = mesos + GameConstants.getTaxAmount(mesos) + (quickdelivery ? 0 : 5000);

        if (mesos >= 0 && mesos <= 100000000 && c.getPlayer().getMeso() >= finalcost) {
        final int accid = MapleCharacterUtil.getIdByName(recipient);
        if (accid != -1) {
        if (accid != c.getAccID()) {
        boolean recipientOn = false;
        /*			    MapleClient rClient = null;
        try {
        int channel = c.getChannelServer().getWorldInterface().find(recipient);
        if (channel > -1) {
        recipientOn = true;
        ChannelServer rcserv = ChannelServer.getInstance(channel);
        rClient = rcserv.getPlayerStorage().getCharacterByName(recipient).getClient();
        }
        } catch (RemoteException re) {
        c.getChannelServer().reconnectWorld();
        }*/
        /*
        if (inventId > 0) {
        final MapleInventoryType inv = MapleInventoryType.getByType(inventId);
        final Item item = c.getPlayer().getInventory(inv).getItem((byte) itemPos);
        if (item == null) {
        c.announce(CField.sendDuey((byte) 17, null)); // Unsuccessfull
        return;
        }
        final byte flag = item.getFlag();
        if (ItemFlag.UNTRADEABLE.check(flag) || ItemFlag.LOCK.check(flag)) {
        c.announce(CWvsContext.enableActions());
        return;
        }
        if (c.getPlayer().getItemQuantity(item.getItemId(), false) >= amount) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.isDropRestricted(item.getItemId()) && !ii.isAccountShared(item.getItemId())) {
        if (addItemToDB(item, amount, mesos, c.getPlayer().getName(), accid, recipientOn)) {
        if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
        MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, item.getQuantity(), true);
        } else {
        MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, amount, true, false);
        }
        c.getPlayer().gainMeso(-finalcost, false);
        c.announce(CField.sendDuey((byte) 19, null)); // Successfull
        } else {
        c.announce(CField.sendDuey((byte) 17, null)); // Unsuccessful
        }
        } else {
        c.announce(CField.sendDuey((byte) 17, null)); // Unsuccessfull
        }
        } else {
        c.announce(CField.sendDuey((byte) 17, null)); // Unsuccessfull
        }
        } else {
        if (addMesoToDB(mesos, c.getPlayer().getName(), accid, recipientOn)) {
        c.getPlayer().gainMeso(-finalcost, false);

        c.announce(CField.sendDuey((byte) 19, null)); // Successfull
        } else {
        c.announce(CField.sendDuey((byte) 17, null)); // Unsuccessfull
        }
        }
        //                            if (recipientOn && rClient != null) {
        //                              rClient.announce(CField.sendDueyMSG(Actions.PACKAGE_MSG.getCode()));
        //                        }
        } else {
        c.announce(CField.sendDuey((byte) 15, null)); // Same acc error
        }
        } else {
        c.announce(CField.sendDuey((byte) 14, null)); // Name does not exist
        }
        } else {
        c.announce(CField.sendDuey((byte) 12, null)); // Not enough mesos
        }
        break;
        }
        case 5: { // Recieve Package
        if (c.getPlayer().getConversation() != 2) {
        return;
        }
        final int packageid = slea.readInt();
        //System.out.println("Item attempted : " + packageid);
        final MapleDueyActions dp = loadSingleItem(packageid, c.getPlayer().getId());
        if (dp == null) {
        return;
        }
        if (dp.getItem() != null && !MapleInventoryManipulator.checkSpace(c, dp.getItem().getItemId(), dp.getItem().getQuantity(), dp.getItem().getOwner())) {
        c.announce(CField.sendDuey((byte) 16, null)); // Not enough Space
        return;
        } else if (dp.getMesos() < 0 || (dp.getMesos() + c.getPlayer().getMeso()) < 0) {
        c.announce(CField.sendDuey((byte) 17, null)); // Unsuccessfull
        return;
        }
        removeItemFromDB(packageid, c.getPlayer().getId()); // Remove first
        //System.out.println("Item removed : " + packageid);
        if (dp.getItem() != null) {
        MapleInventoryManipulator.addFromDrop(c, dp.getItem(), false);
        }
        if (dp.getMesos() != 0) {
        c.getPlayer().gainMeso(dp.getMesos(), false);
        }
        c.announce(CField.removeItemFromDuey(false, packageid));
        break;
        }
        case 6: { // Remove package
        if (c.getPlayer().getConversation() != 2) {
        return;
        }
        final int packageid = slea.readInt();
        removeItemFromDB(packageid, c.getPlayer().getId());
        c.announce(CField.removeItemFromDuey(true, packageid));
        break;
        }
        case 8: { // Close Duey
        c.getPlayer().setConversation(0);
        break;
        }
        default: {
        System.out.println("Unhandled Duey operation : " + slea.toString());
        break;
        }
        }
         */
    }
}
