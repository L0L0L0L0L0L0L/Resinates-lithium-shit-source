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
package server.life;

import constants.GameConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import client.inventory.MapleInventoryType;
import database.DatabaseConnection;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import server.MapleItemInformationProvider;
import tools.Pair;

public class MapleMonsterInformationProvider {

    private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
    private Map<Integer, List<MonsterDropEntry>> drops = new ConcurrentHashMap<>();
    private Map<Integer, List<Integer>> items = new ConcurrentHashMap<>();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();
    private final List<MonsterGlobalRareDropEntry> globalraredrops = new ArrayList<>();
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
    private static final MapleData mobStringData = stringDataWZ.getData("MonsterBook.img");
    private final Map<Pair<Integer, Integer>, Integer> mobAttackAnimationTime = new HashMap<>();
    private final Map<MobSkill, Integer> mobSkillAnimationTime = new HashMap<>();
    private final Map<Integer, Pair<Integer, Integer>> mobAttackInfo = new HashMap<>();

    public static MapleMonsterInformationProvider getInstance() {
        return instance;
    }

    public List<MonsterGlobalDropEntry> getGlobalDrop() {
        return globaldrops;
    }

    public Map<Integer, List<MonsterDropEntry>> getDrops() {
        return drops;
    }

    public List<Integer> getAllDropItemsType(int type) {
        List<Integer> item = new LinkedList<>();
        for (Integer itemid : items.keySet()) {
            if (GameConstants.getInventoryTypeId(itemid) == type) {
                if (!item.contains(itemid)) {
                    item.add(itemid);
                }
            }
        }
        return item;
    }

    public List<Integer> getMobsFromItemId(int id) {
        if (items.containsKey(id)) {
            return items.get(id);
        }
        return null;
    }

    public List<MonsterGlobalDropEntry> getShuffledGlobalDrop() {
        List<MonsterGlobalDropEntry> shuffled = new ArrayList<>(globaldrops);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public List<MonsterGlobalRareDropEntry> getGlobalRareDrop() {
        return globalraredrops;
    }

    public List<MonsterGlobalRareDropEntry> getShuffledRareDrop() {
        List<MonsterGlobalRareDropEntry> shuffled = new ArrayList<>(globalraredrops);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public void load() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try (Connection con = DatabaseConnection.getWorldConnection()) {

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (ii.getItemInformation(rs.getInt("itemid")) != null) {
                            globaldrops.add(
                                    new MonsterGlobalDropEntry(
                                            rs.getInt("itemid"),
                                            rs.getInt("chance"),
                                            rs.getInt("continent"),
                                            rs.getByte("dropType"),
                                            rs.getInt("minimum_quantity"),
                                            rs.getInt("maximum_quantity"),
                                            rs.getInt("questid")));
                        } else {
                            System.out.println("Invalid Item id in Global Drops: " + rs.getInt("itemid"));
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_rare_global WHERE chance > 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (ii.getItemInformation(rs.getInt("itemid")) != null) {
                            globalraredrops.add(
                                    new MonsterGlobalRareDropEntry(
                                            rs.getInt("itemid"),
                                            rs.getInt("chance"),
                                            rs.getInt("continent"),
                                            rs.getByte("dropType"),
                                            rs.getInt("minimum_quantity"),
                                            rs.getInt("maximum_quantity"),
                                            rs.getInt("questid")));
                        } else {
                            System.out.println("Invalid Item id in Global Rare Drops: " + rs.getInt("itemid"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving drop" + e);
        }
    }

    public void addValueToItems(int key, int value) {
        items.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void loadItemMobs() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data WHERE chance > 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {

                        int itemId = rs.getInt("itemid");
                        int mobId = rs.getInt("dropperid");
                        if (ii.getItemInformation(itemId) != null) {
                            addValueToItems(itemId, mobId);
                        } else {
                            System.out.println("Invalid Item id in Monster Drops: " + itemId);
                        }
                    }
                }
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Stored Items: " + items.size());
    }

    public List<MonsterDropEntry> retrieveDrop(final int monsterId) {
        if (drops.containsKey(monsterId)) {
            return drops.get(monsterId);
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<MonsterDropEntry> ret = new LinkedList<>();

        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?")) {
                ps.setInt(1, monsterId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (ii.getItemInformation(rs.getInt("itemid")) != null) {
                            ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid"), rs.getInt("rare")));
                        } else {
                            System.out.println("Invalid Item id in Monster Drops: " + rs.getInt("itemid"));
                        }
                    }
                }
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return ret;
        }
        drops.put(monsterId, ret);
        if (!drops.isEmpty()) {
            return drops.get(monsterId);
        }
        return null;
    }

    public void clearDrops() {
        drops.clear();
        globaldrops.clear();
        load();
        //addExtra();
    }

    public boolean contains(List<MonsterDropEntry> e, int toAdd) {
        for (MonsterDropEntry f : e) {
            if (f.itemId == toAdd) {
                return true;
            }
        }
        return false;
    }

    public int chanceLogic(int itemId) { //not much logic in here. most of the drops should already be there anyway.
        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
            return 50000; //with *10
        } else if (GameConstants.getInventoryType(itemId) == MapleInventoryType.SETUP || GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH) {
            return 500;
        } else {
            switch (itemId / 10000) {
                case 204:
                case 207:
                case 233:
                case 229:
                    return 500;
                case 401:
                case 402:
                    return 5000;
                case 403:
                    return 5000; //lol
            }
            return 20000;
        }
    }

    public final void setMobAttackAnimationTime(int monsterId, int attackPos, int animationTime) {
        mobAttackAnimationTime.put(new Pair<>(monsterId, attackPos), animationTime);
    }

    public final Integer getMobAttackAnimationTime(int monsterId, int attackPos) {
        Integer time = mobAttackAnimationTime.get(new Pair<>(monsterId, attackPos));
        return time == null ? 0 : time;
    }

    public final void setMobSkillAnimationTime(MobSkill skill, int animationTime) {
        mobSkillAnimationTime.put(skill, animationTime);
    }

    public final Integer getMobSkillAnimationTime(MobSkill skill) {
        Integer time = mobSkillAnimationTime.get(skill);
        return time == null ? 0 : time;
    }

    public final void setMobAttackInfo(int monsterId, int attackPos, int mpCon, int coolTime) {
        mobAttackInfo.put((monsterId << 3) + attackPos, new Pair<>(mpCon, coolTime));
    }

    public final Pair<Integer, Integer> getMobAttackInfo(int monsterId, int attackPos) {
        if (attackPos < 0 || attackPos > 7) {
            return null;
        }
        return mobAttackInfo.get((monsterId << 3) + attackPos);
    }

    //MESO DROP: level * (level / 10) = max, min = 0.66 * max
    //explosive Reward = 7 meso drops
    //boss, ffaloot = 2 meso drops
    //boss = level * level = max
    //no mesos if: mobid / 100000 == 97 or 95 or 93 or 91 or 90 or removeAfter > 0 or invincible or onlyNormalAttack or friendly or dropitemperiod > 0 or cp > 0 or point > 0 or fixeddamage > 0 or selfd > 0 or mobType != null and mobType.charat(0) == 7 or PDRate <= 0
}
