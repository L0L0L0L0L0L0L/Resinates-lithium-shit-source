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
package constants;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.TimerManager;
import tools.packet.CField;

public class ServerSlots {//73.35.242.13

    public boolean TESPIA = false; // true = uses GMS test server, for MSEA it does nothing though

    private static final Map<Integer, List<ServerSlotItem>> slots = new ConcurrentHashMap<>();
    private static final Map<Integer, List<ServerSlotItem>> slotList = new ConcurrentHashMap<>();
    private static ServerSlots instance = new ServerSlots();

    public static ServerSlots getInstance() {
        return instance;
    }

    public static int getRandomSlotIcon(int id) {
        return slots.get(id).get(Randomizer.nextInt(slots.get(id).size())).getIcon();
    }

    public static int getFrame(int id) {
        return slots.get(id).get(Randomizer.nextInt(slots.get(id).size())).getFrame();
    }

    public static int getItemIdFromSlot(int id, int icon) {
        return slots.get(id).get(icon).getId();
    }

    public static ServerSlotItem getById(int id, int item) {
        for (ServerSlotItem sItem : getItemsFromSlot(id)) {
            if (sItem.getIcon() == item) {
                return sItem;
            }
        }
        return null;
    }

    public static List<ServerSlotItem> getItemsFromSlot(int id) {
        if (slots.containsKey(id)) {
            return Collections.unmodifiableList(slots.get(id));
        } else {
            retrieveSlots(id);
            if (slots.containsKey(id)) {
                return Collections.unmodifiableList(slots.get(id));
            }
            return null;
        }
    }

    public static List<ServerSlotItem> getItemListFromSlot(int id) {
        if (slotList.containsKey(id)) {
            return Collections.unmodifiableList(slotList.get(id));
        } else {
            retrieveSlots(id);
            if (slotList.containsKey(id)) {
                return Collections.unmodifiableList(slotList.get(id));
            }
            return null;
        }
    }

    public static List<ServerSlotItem> retrieveSlots(final int slotId) {
        if (slots.containsKey(slotId)) {
            return Collections.unmodifiableList(slots.get(slotId));
        } else {
            final List<ServerSlotItem> ret = new LinkedList<>();
            final List<ServerSlotItem> ret2 = new LinkedList<>();
            try (Connection con = DatabaseConnection.getWorldConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM slot_items WHERE slot_id = ?")) {
                    ps.setInt(1, slotId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            for (int i = 0; i < rs.getInt("rounds"); i++) {
                                ret.add(new ServerSlotItem(rs.getInt("icon"), rs.getInt("item"), rs.getInt("amount_1"), rs.getInt("amount_2"), rs.getInt("amount_3"), rs.getInt("frame"), rs.getInt("rounds")));
                            }
                            ret2.add(new ServerSlotItem(rs.getInt("icon"), rs.getInt("item"), rs.getInt("amount_1"), rs.getInt("amount_2"), rs.getInt("amount_3"), rs.getInt("frame"), rs.getInt("rounds")));
                        }
                    }
                }
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return ret;
            }
            slots.put(slotId, ret);
            slotList.put(slotId, ret2);
            if (!slots.isEmpty()) {
                return Collections.unmodifiableList(slots.get(slotId));
            }
            return null;
        }
    }

    public static void clearGacha() {
        slots.clear();
    }

    public static void reloadGachaID(int gachaId) {
        if (slots.containsKey(gachaId)) {
            slots.get(gachaId).clear();
            retrieveSlots(gachaId);
        }
    }

    public static void runSlot(MapleClient c, int type, int time, final int rounds, final int item, final int cost) {
        runSlot(c, type, time, rounds, item, cost, false);
    }

    public static boolean Ignore(int id) {
        switch (id) {
            case 4310101 -> { //casino coins
                return true;
            }
        }
        return false;
    }

    public static void runSlot(MapleClient c, int type, int time, final int rounds, final int item, final int cost, boolean show) {
        MapleCharacter player = c.getPlayer();
        if (player != null && !player.run) {
            retrieveSlots(type);
            player.isSlot = true;
            player.run = true;
            player.setWin(show);
            player.getClient().announce(CField.UIPacket.IntroLock(true));
            player.getClient().announce(CField.UIPacket.IntroDisableUI(true));
            player.getClient().announce(CField.musicChange("BgmCustom/slot"));
            final AtomicInteger counter = new AtomicInteger();
            if (player.haveItem(item, cost * rounds * player.getMulti())) {
                player.slotTask = TimerManager.getInstance().register(() -> {
                    if (!GameConstants.getLockSlot() && player.run && counter.incrementAndGet() <= rounds) {
                        if (player.haveItem(item, cost * player.getMulti())) {
                            player.gainItem(item, -cost * player.getMulti(), "");
                            player.dropMidMessage("" + counter.get());
                            runRewards(player, type, getRandomSlotIcon(type), getRandomSlotIcon(type), getRandomSlotIcon(type));
                        } else {
                            System.out.println("[" + Calendar.getInstance().getTime() + "] - " + player.getName() + " possible DP duping for free slots.");
                            if (player.getClient().getCMS() != null) {
                                player.getClient().getCMS().sendOkS("ERROR, Please contact Resinate on Discord with this error!", (byte) 16);
                            }
                            player.run = false;
                        }
                    } else {
                        player.run = false;
                    }
                    if (!player.run) {
                        player.isSlot = false;
                        player.getClient().announce(CField.UIPacket.IntroLock(false));
                        player.getClient().announce(CField.UIPacket.IntroDisableUI(false));
                        player.getClient().announce(CField.musicChange(player.getMap().getBGM()));
                        player.getClient().announce(CField.getPublicNPCInfo());
                        player.setWin(false);
                        player.setMulti(1);
                        if (player.slotTask != null) {
                            player.slotTask.cancel(true);
                            player.slotTask = null;
                            player.run = false;
                        }
                    }
                }, time);
            }
        }
        //getPlayer().getClient().announce(UIPacket.IntroDisableUI(true));
    }

    public static void runRewards(MapleCharacter player, int type, final int a, final int b, final int c) {
        if (player != null) {
            player.getClient().announce(CField.showEffect("miro/back"));
            player.getClient().announce(CField.showEffect("miro/frame" + getFrame(type)));
            player.getClient().announce(CField.showEffect("miro/RR1/" + a));
            player.getClient().announce(CField.showEffect("miro/RR2/" + b));
            player.getClient().announce(CField.showEffect("miro/RR3/" + c));
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (this != null) {
                        if (a == b && a == c && b == c) {
                            procressReward(player, type, a, true);
                        } else {
                            if (a == b) {
                                procressReward(player, type, a, false);
                            }
                            if (a == c) {
                                procressReward(player, type, a, false);
                            }
                            if (b == c) {
                                procressReward(player, type, b, false);
                            }
                        }
                        Reward(player, type, c);
                        Reward(player, type, b);
                        Reward(player, type, a);
                    }
                }
            }, 1600);
            //if ()
            //gainItem(4310502, 1);
        }
    }

    public static int getExpType(int type) {
        switch (type) {
            case 1 -> {//basic
                return 5;
            }
            case 10 -> {//rainbow
                return 50;
            }
            case 11 -> {
                return 25;
            }
            case 12 -> {
                return 100;
            }
            case 13 -> {
                return 10;
            }
            case 20 -> {
                return 10;
            }
            case 25 -> {
                return 1;
            }
            case 26 -> {
                return 10;
            }
            case 27 -> {
                return 25;
            }
            case 28 -> {
                return 50;
            }
            case 29 -> {
                return 100;
            }
            case 30 -> {
                return 250;
            }
        }
        return 1;
    }

    public static void procressReward(MapleCharacter player, int type, int value, final boolean jackpot) {
        final int item = getById(type, value).getId();
        int amount = (jackpot ? getById(type, value).getAmount3() : getById(type, value).getAmount2()) * player.getMulti();
        if (type >= 25 && type <= 30) {
            amount *= (1 + (player.getLevelData(101) * 0.01));
        }
        if (amount > 0) {
            if (!player.rewards.containsKey(item)) {
                player.rewards.put(item, new AtomicLong(amount));
            } else {
                player.rewards.get(item).addAndGet(amount);
            }
            if (GameConstants.getInventoryType(item) == MapleInventoryType.ETC && !player.canHold(item, amount)) {
                player.addOverflow(item, amount);
            } else {
                player.gainItem(item, amount, "from casino reward");
            }
            player.getClient().announce(CField.EffectPacket.showForeignEffect(35));
            player.dropTopMessage("You won bonus " + amount + " " + MapleItemInformationProvider.getInstance().getName(item));
            if (jackpot) {
                if (player.showWin()) {
                    player.getClient().announce(CField.showEffect("miro/win3"));
                    player.getClient().getChannelServer().dropMessage(player.getName() + " has won the Jackpot: " + amount + " " + MapleItemInformationProvider.getInstance().getName(item));
                }
            } else {
                if (player.showWin()) {
                    player.getClient().announce(CField.showEffect("miro/win2"));
                }
            }
            long exp = getExpType(type) * player.getMulti() * (jackpot ? 10 : 1);
            player.gainLevelData(101, exp);
            player.dropMidMessage("You earned +" + exp + " Gambling exp");
        }
    }

    public static void Reward(MapleCharacter player, int type, int value) {
        final int item = getById(type, value).getId();
        int amount = getById(type, value).getAmount() * player.getMulti();
        if (type >= 25 && type <= 30) {
            amount *= (1 + (player.getLevelData(101) * 0.01));
        }
        if (amount > 0) {
            if (!player.rewards.containsKey(item)) {
                player.rewards.put(item, new AtomicLong(amount));
            } else {
                player.rewards.get(item).addAndGet(amount);
            }
            if (GameConstants.getInventoryType(item) == MapleInventoryType.ETC && !player.canHold(item, amount)) {
                player.addOverflow(item, amount);
            } else {
                player.gainItem(item, amount, "from casino reward");
            }
            player.dropTopMessage("You won " + amount + " " + MapleItemInformationProvider.getInstance().getName(item));
        }
    }
}
