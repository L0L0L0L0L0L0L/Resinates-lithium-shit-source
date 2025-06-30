/*
 This file is part of the ZeroFusion MapleStory Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 ZeroFusion organized by "RMZero213" <RMZero213@hotmail.com>

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
package client.inventory;

import constants.GameConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import database.DatabaseConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.Pair;

public enum ItemLoader {

    //INVENTORY("inventoryitems", "inventoryequipment", 0, "characterid"),
    //STORAGE("inventoryitems", "inventoryequipment", 1, "accountid"),
    //CASHSHOP("csitems", "csequipment", 2, "accountid"),
    //HIRED_MERCHANT("hiredmerchitems", "hiredmerchequipment", 5, "packageid"),
    //DUEY("dueyitems", "dueyequipment", 6, "packageid"),
    //MTS("mtsitems", "mtsequipment", 8, "packageid"),
    //MTS_TRANSFER("mtstransfer", "mtstransferequipment", 9, "characterid");
    INVENTORY(0, false),
    STORAGE(1, true),
    CASH_EXPLORER(2, true),
    CASH_CYGNUS(2, true),
    CASH_ARAN(2, true),
    MERCHANT(5, false),
    CASH_OVERALL(7, true),
    MARRIAGE_GIFTS(8, false),
    DUEY(9, false);
    private final byte value;
    private final boolean account;

    private static final int lockCount = 1000;
    private static final Lock locks[] = new Lock[lockCount];  // thanks Masterrulax for pointing out a bottleneck issue here
    private final Lock invLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.INVENTORY);

    static {
        for (int i = 0; i < lockCount; i++) {
            locks[i] = MonitoredReentrantLockFactory.createLock(MonitoredLockType.ITEM, true);
        }
    }

    private ItemLoader(int value, boolean account) {
        this.value = (byte) value;
        this.account = account;
    }

    public byte getValue() {
        return value;
    }

    public List<Pair<Item, MapleInventoryType>> loadItems(int id, boolean login) throws SQLException {
        return loadItemsCommon(id, login);
    }

    private List<Pair<Item, MapleInventoryType>> loadItemsCommon(int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        //items
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventoryitems` WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");

            ps = con.prepareStatement(query.toString());
            ps.setByte(1, value);
            ps.setInt(2, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                item.setOwner(rs.getString("owner"));
                item.setInventoryId(rs.getLong("inventoryitemid"));
                item.setExpiration(rs.getLong("expiredate"));
                item.setGMLog(rs.getString("GM_Log"));
                item.setGiftFrom(rs.getString("sender"));
                if (GameConstants.isPet(item.getItemId())) {
                    if (item.getUniqueId() > -1) {
                        MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                        if (pet != null) {
                            item.setPet(pet);
                        }
                    } else {
                        //O_O hackish fix
                        item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                    }
                }
                items.add(new Pair<>(item, mit));

            }

            rs.close();
            ps.close();

            //equips!!!
            query = new StringBuilder();
            query.append("SELECT * FROM `inventoryequips` WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");

            if (login) {
                query.append(" AND `inventorytype` = ").append(MapleInventoryType.EQUIPPED.getType());
            }

            ps = con.prepareStatement(query.toString());
            ps.setByte(1, value);
            ps.setInt(2, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                if (equip.getPosition() != -55) { //monsterbook
                    equip.setQuantity((short) 1);
                    equip.setInventoryId(rs.getLong("inventoryitemid"));
                    equip.setOwner(rs.getString("owner"));
                    equip.setExpiration(rs.getLong("expiredate"));
                    equip.setUpgradeSlots(rs.getShort("upgradeslots"));
                    equip.setLevel(rs.getShort("level"));
                    equip.setStr(rs.getInt("str"));
                    equip.setDex(rs.getInt("dex"));
                    equip.setInt(rs.getInt("int"));
                    equip.setLuk(rs.getInt("luk"));
                    equip.setWatk(rs.getInt("watk"));
                    equip.setMatk(rs.getInt("matk"));
                    equip.setWdef(rs.getInt("wdef"));
                    equip.setMdef(rs.getInt("mdef"));
                    equip.setAcc(rs.getInt("acc"));
                    equip.setAvoid(rs.getInt("avoid"));
                    equip.setHands(rs.getInt("hands"));
                    equip.setSpeed(rs.getInt("speed"));
                    equip.setJump(rs.getInt("jump"));
                    equip.setViciousHammer(rs.getByte("ViciousHammer"));
                    equip.setItemEXP(rs.getInt("itemEXP"));
                    equip.setGMLog(rs.getString("GM_Log"));
                    equip.setDurability(rs.getInt("durability"));
                    equip.setEnhance(rs.getInt("enhance"));
                    equip.setPotential1(rs.getInt("potential1"));
                    equip.setPotential2(rs.getInt("potential2"));
                    equip.setPotential3(rs.getInt("potential3"));
                    equip.setPotential4(rs.getInt("potential4"));
                    equip.setPotential5(rs.getInt("potential5"));
                    equip.setSocket1(rs.getInt("socket1"));
                    equip.setSocket2(rs.getInt("socket2"));
                    equip.setSocket3(rs.getInt("socket3"));
                    equip.setGiftFrom(rs.getString("sender"));
                    equip.setIncSkill(rs.getInt("incSkill"));
                    equip.setPVPDamage(rs.getInt("pvpDamage"));
                    equip.setCharmEXP(rs.getInt("charmEXP"));
                    equip.setOverPower(rs.getInt("overpower"));
                    equip.setTotalDamage(rs.getInt("totaldamage"));
                    equip.setBossDamage(rs.getInt("bossdamage"));
                    equip.setIED(rs.getInt("ied"));
                    equip.setCritDamage(rs.getInt("critdamage"));
                    equip.setAllStat(rs.getInt("allstat"));
                    equip.setPower(rs.getInt("power"));
                    equip.setOStr(rs.getLong("ostr"));
                    equip.setODex(rs.getLong("odex"));
                    equip.setOInt(rs.getLong("oint"));
                    equip.setOLuk(rs.getLong("oluk"));
                    equip.setOAtk(rs.getLong("oatk"));
                    equip.setOMatk(rs.getLong("omatk"));
                    equip.setODef(rs.getLong("odef"));
                    equip.setOMdef(rs.getLong("omdef"));

                    if (equip.getCharmEXP() < 0) { //has not been initialized yet
                        equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                    }
                    if (equip.getUniqueId() > -1) {
                        if (equip.getItemId() / 10000 == 166) {
                            MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                            if (ring != null) {
                                equip.setAndroid(ring);
                            }
                        }
                    }
                }
                items.add(new Pair<Item, MapleInventoryType>(equip, mit));
            }

            rs.close();
            ps.close();
            return items;
        }

    }

    public List<Pair<Item, MapleInventoryType>> loadItemsStorage(int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        //items
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            //storage
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventorystorage` WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");

            ps = con.prepareStatement(query.toString());
            ps.setByte(1, value);
            ps.setInt(2, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (mit == MapleInventoryType.EQUIP) {
                    Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                    equip.setQuantity((short) 1);
                    equip.setInventoryId(rs.getLong("inventoryitemid"));
                    equip.setOwner(rs.getString("owner"));
                    equip.setExpiration(rs.getLong("expiredate"));
                    equip.setUpgradeSlots(rs.getShort("upgradeslots"));
                    equip.setLevel(rs.getShort("level"));
                    equip.setStr(rs.getInt("str"));
                    equip.setDex(rs.getInt("dex"));
                    equip.setInt(rs.getInt("int"));
                    equip.setLuk(rs.getInt("luk"));
                    equip.setWatk(rs.getInt("watk"));
                    equip.setMatk(rs.getInt("matk"));
                    equip.setWdef(rs.getInt("wdef"));
                    equip.setMdef(rs.getInt("mdef"));
                    equip.setAcc(rs.getInt("acc"));
                    equip.setAvoid(rs.getInt("avoid"));
                    equip.setHands(rs.getInt("hands"));
                    equip.setSpeed(rs.getInt("speed"));
                    equip.setJump(rs.getInt("jump"));
                    equip.setViciousHammer(rs.getByte("ViciousHammer"));
                    equip.setItemEXP(rs.getInt("itemEXP"));
                    equip.setGMLog(rs.getString("GM_Log"));
                    equip.setDurability(rs.getInt("durability"));
                    equip.setEnhance(rs.getInt("enhance"));
                    equip.setPotential1(rs.getInt("potential1"));
                    equip.setPotential2(rs.getInt("potential2"));
                    equip.setPotential3(rs.getInt("potential3"));
                    equip.setPotential4(rs.getInt("potential4"));
                    equip.setPotential5(rs.getInt("potential5"));
                    equip.setSocket1(rs.getInt("socket1"));
                    equip.setSocket2(rs.getInt("socket2"));
                    equip.setSocket3(rs.getInt("socket3"));
                    equip.setGiftFrom(rs.getString("sender"));
                    equip.setIncSkill(rs.getInt("incSkill"));
                    equip.setPVPDamage(rs.getInt("pvpDamage"));
                    equip.setCharmEXP(rs.getInt("charmEXP"));
                    equip.setOverPower(rs.getInt("overpower"));
                    equip.setTotalDamage(rs.getInt("totaldamage"));
                    equip.setBossDamage(rs.getInt("bossdamage"));
                    equip.setIED(rs.getInt("ied"));
                    equip.setCritDamage(rs.getInt("critdamage"));
                    equip.setAllStat(rs.getInt("allstat"));
                    equip.setPower(rs.getInt("power"));
                    equip.setOStr(rs.getLong("ostr"));
                    equip.setODex(rs.getLong("odex"));
                    equip.setOInt(rs.getLong("oint"));
                    equip.setOLuk(rs.getLong("oluk"));
                    equip.setOAtk(rs.getLong("oatk"));
                    equip.setOMatk(rs.getLong("omatk"));
                    equip.setODef(rs.getLong("odef"));
                    equip.setOMdef(rs.getLong("omdef"));

                    if (equip.getCharmEXP() < 0) { //has not been initialized yet
                        equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                    }
                    if (equip.getUniqueId() > -1) {
                        if (equip.getItemId() / 10000 == 166) {
                            MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                            if (ring != null) {
                                equip.setAndroid(ring);
                            }
                        }
                    }

                    items.add(new Pair<Item, MapleInventoryType>(equip, mit));
                } else {
                    Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                    item.setOwner(rs.getString("owner"));
                    item.setInventoryId(rs.getLong("inventoryitemid"));
                    item.setExpiration(rs.getLong("expiredate"));
                    item.setGMLog(rs.getString("GM_Log"));
                    item.setGiftFrom(rs.getString("sender"));
                    if (GameConstants.isPet(item.getItemId())) {
                        if (item.getUniqueId() > -1) {
                            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                            if (pet != null) {
                                item.setPet(pet);
                            }
                        } else {
                            //O_O hackish fix
                            item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                        }
                    }
                    items.add(new Pair<>(item, mit));
                }
            }
            rs.close();
            ps.close();
            return items;
        }

    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id) throws SQLException {
        saveItems(items, id, DatabaseConnection.getPlayerConnection());
    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException {
        Lock lock = locks[id % lockCount];
        lock.lock();
        try {
            StringBuilder query = new StringBuilder();
            if (value == 0) {
                query.append("DELETE `inventoryitems` FROM `inventoryitems` WHERE `type` = ? AND `");
                query.append(account ? "accountid" : "characterid").append("` = ?");
                try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                    ps.setByte(1, value);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    ps.close();
                }
                query = new StringBuilder();
                query.append("DELETE `inventoryequips` FROM `inventoryequips` WHERE `type` = ? AND `");
                query.append(account ? "accountid" : "characterid").append("` = ?");
                try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                    ps.setByte(1, value);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    ps.close();
                }
                if (!items.isEmpty()) {
                    List<Pair<Item, MapleInventoryType>> itemz = new ArrayList<>();
                    List<Pair<Item, MapleInventoryType>> equips = new ArrayList<>();
                    for (Pair<Item, MapleInventoryType> pair : items) {
                        MapleInventoryType mit = pair.getRight();
                        if (mit == MapleInventoryType.EQUIP || mit == MapleInventoryType.EQUIPPED) {
                            equips.add(pair);
                        } else {
                            itemz.add(pair);
                        }
                    }
                    if (!itemz.isEmpty()) {
                        //items
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventoryitems` VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                            for (Pair<Item, MapleInventoryType> pair : itemz) {
                                Item item = pair.getLeft();
                                MapleInventoryType mit = pair.getRight();
                                ps.setByte(1, value);
                                ps.setString(2, account ? null : String.valueOf(id));
                                ps.setString(3, account ? String.valueOf(id) : null);
                                ps.setInt(4, item.getItemId());
                                ps.setString(5, item.getItemName(item.getItemId()));
                                ps.setInt(6, mit.getType());
                                ps.setInt(7, item.getPosition());
                                ps.setInt(8, item.getQuantity());
                                ps.setString(9, item.getOwner());
                                ps.setString(10, item.getGMLog());
                                if (item.getPet() != null) { //expensif?
                                    ps.setInt(11, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                                } else {
                                    ps.setInt(11, item.getUniqueId());
                                }
                                ps.setShort(12, item.getFlag());
                                ps.setLong(13, item.getExpiration());
                                ps.setString(14, item.getGiftFrom());
                                ps.addBatch();
                            }
                            ps.executeBatch();

                        }
                    }
                    //equips
                    if (!equips.isEmpty()) {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventoryequips` VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                            for (Pair<Item, MapleInventoryType> pair : equips) {
                                Item item = pair.getLeft();
                                MapleInventoryType mit = pair.getRight();
                                ps.setByte(1, value);
                                ps.setString(2, account ? null : String.valueOf(id));
                                ps.setString(3, account ? String.valueOf(id) : null);
                                ps.setInt(4, item.getItemId());
                                ps.setString(5, item.getItemName(item.getItemId()));
                                ps.setInt(6, mit.getType());
                                ps.setInt(7, item.getPosition());
                                ps.setInt(8, item.getQuantity());
                                ps.setString(9, item.getOwner());
                                ps.setString(10, item.getGMLog());
                                if (item.getPet() != null) { //expensif?
                                    ps.setInt(11, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                                } else {
                                    ps.setInt(11, item.getUniqueId());
                                }
                                ps.setShort(12, item.getFlag());
                                ps.setLong(13, item.getExpiration());
                                ps.setString(14, item.getGiftFrom());
                                Equip equip = null;
                                if (item.getInventoryType() == MapleInventoryType.EQUIP) {
                                    equip = (Equip) item;
                                }
                                ps.setInt(15, equip != null ? equip.getUpgradeSlots() : 0);
                                ps.setInt(16, equip != null ? equip.getLevel() : 0);
                                ps.setInt(17, equip != null ? equip.getStr() : 0);
                                ps.setInt(18, equip != null ? equip.getDex() : 0);
                                ps.setInt(19, equip != null ? equip.getInt() : 0);
                                ps.setInt(20, equip != null ? equip.getLuk() : 0);
                                ps.setInt(21, equip != null ? equip.getHp() : 0);
                                ps.setInt(22, equip != null ? equip.getHpr() : 0);
                                ps.setInt(23, equip != null ? equip.getMp() : 0);
                                ps.setInt(24, equip != null ? equip.getMpr() : 0);
                                ps.setInt(25, equip != null ? equip.getWatk() : 0);
                                ps.setInt(26, equip != null ? equip.getMatk() : 0);
                                ps.setInt(27, equip != null ? equip.getWdef() : 0);
                                ps.setInt(28, equip != null ? equip.getMdef() : 0);
                                ps.setInt(29, equip != null ? equip.getAcc() : 0);
                                ps.setInt(30, equip != null ? equip.getAvoid() : 0);
                                ps.setInt(31, equip != null ? equip.getHands() : 0);
                                ps.setInt(32, equip != null ? equip.getSpeed() : 0);
                                ps.setInt(33, equip != null ? equip.getJump() : 0);
                                ps.setInt(34, equip != null ? equip.getViciousHammer() : 0);
                                ps.setInt(35, equip != null ? equip.getItemEXP() : 0);
                                ps.setInt(36, equip != null ? equip.getDurability() : 0);
                                ps.setInt(37, equip != null ? equip.getEnhance() : 0);
                                ps.setInt(38, equip != null ? equip.getPotential1() : 0);
                                ps.setInt(39, equip != null ? equip.getPotential2() : 0);
                                ps.setInt(40, equip != null ? equip.getPotential3() : 0);
                                ps.setInt(41, equip != null ? equip.getPotential4() : 0);
                                ps.setInt(42, equip != null ? equip.getPotential5() : 0);
                                ps.setInt(43, equip != null ? equip.getSocket1() : 0);
                                ps.setInt(44, equip != null ? equip.getSocket2() : 0);
                                ps.setInt(45, equip != null ? equip.getSocket3() : 0);
                                ps.setInt(46, equip != null ? equip.getIncSkill() : 0);
                                ps.setInt(47, equip != null ? equip.getCharmEXP() : 0);
                                ps.setInt(48, equip != null ? equip.getPVPDamage() : 0);
                                ps.setInt(49, equip != null ? equip.getPower() : 0);
                                ps.setInt(50, equip != null ? equip.getOverPower() : 0);
                                ps.setInt(51, equip != null ? equip.getTotalDamage() : 0);
                                ps.setInt(52, equip != null ? equip.getBossDamage() : 0);
                                ps.setInt(53, equip != null ? equip.getIED() : 0);
                                ps.setInt(54, equip != null ? equip.getCritDamage() : 0);
                                ps.setInt(55, equip != null ? equip.getAllStat() : 0);
                                ps.setLong(56, equip != null ? equip.getOStr() : 0);
                                ps.setLong(57, equip != null ? equip.getODex() : 0);
                                ps.setLong(58, equip != null ? equip.getOInt() : 0);
                                ps.setLong(59, equip != null ? equip.getOLuk() : 0);
                                ps.setLong(60, equip != null ? equip.getOAtk() : 0);
                                ps.setLong(61, equip != null ? equip.getOMatk() : 0);
                                ps.setLong(62, equip != null ? equip.getODef() : 0);
                                ps.setLong(63, equip != null ? equip.getOMdef() : 0);
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        }
                    }
                }
            } else {
                query.append("DELETE `inventorystorage` FROM `inventorystorage` WHERE `type` = ? AND `");
                query.append(account ? "accountid" : "characterid").append("` = ?");
                try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                    ps.setByte(1, value);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    ps.close();
                }
                if (!items.isEmpty()) {
                    List<Pair<Item, MapleInventoryType>> itemz = new ArrayList<>();
                    for (Pair<Item, MapleInventoryType> pair : items) {
                        itemz.add(pair);
                    }

                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventorystorage` VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        for (Pair<Item, MapleInventoryType> pair : itemz) {
                            Item item = pair.getLeft();
                            MapleInventoryType mit = pair.getRight();
                            ps.setByte(1, value);
                            ps.setString(2, account ? null : String.valueOf(id));
                            ps.setString(3, account ? String.valueOf(id) : null);
                            ps.setInt(4, item.getItemId());
                            ps.setString(5, item.getItemName(item.getItemId()));
                            ps.setInt(6, mit.getType());
                            ps.setInt(7, item.getPosition());
                            ps.setInt(8, item.getQuantity());
                            ps.setString(9, item.getOwner());
                            ps.setString(10, item.getGMLog());
                            if (item.getPet() != null) { //expensif?
                                ps.setInt(11, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                            } else {
                                ps.setInt(11, item.getUniqueId());
                            }
                            ps.setShort(12, item.getFlag());
                            ps.setLong(13, item.getExpiration());
                            ps.setString(14, item.getGiftFrom());
                            Equip equip = null;
                            if (item.getInventoryType() == MapleInventoryType.EQUIP) {
                                equip = (Equip) item;
                            }
                            ps.setInt(15, equip != null ? equip.getUpgradeSlots() : 0);
                            ps.setInt(16, equip != null ? equip.getLevel() : 0);
                            ps.setInt(17, equip != null ? equip.getStr() : 0);
                            ps.setInt(18, equip != null ? equip.getDex() : 0);
                            ps.setInt(19, equip != null ? equip.getInt() : 0);
                            ps.setInt(20, equip != null ? equip.getLuk() : 0);
                            ps.setInt(21, equip != null ? equip.getHp() : 0);
                            ps.setInt(22, equip != null ? equip.getHpr() : 0);
                            ps.setInt(23, equip != null ? equip.getMp() : 0);
                            ps.setInt(24, equip != null ? equip.getMpr() : 0);
                            ps.setInt(25, equip != null ? equip.getWatk() : 0);
                            ps.setInt(26, equip != null ? equip.getMatk() : 0);
                            ps.setInt(27, equip != null ? equip.getWdef() : 0);
                            ps.setInt(28, equip != null ? equip.getMdef() : 0);
                            ps.setInt(29, equip != null ? equip.getAcc() : 0);
                            ps.setInt(30, equip != null ? equip.getAvoid() : 0);
                            ps.setInt(31, equip != null ? equip.getHands() : 0);
                            ps.setInt(32, equip != null ? equip.getSpeed() : 0);
                            ps.setInt(33, equip != null ? equip.getJump() : 0);
                            ps.setInt(34, equip != null ? equip.getViciousHammer() : 0);
                            ps.setInt(35, equip != null ? equip.getItemEXP() : 0);
                            ps.setInt(36, equip != null ? equip.getDurability() : 0);
                            ps.setInt(37, equip != null ? equip.getEnhance() : 0);
                            ps.setInt(38, equip != null ? equip.getPotential1() : 0);
                            ps.setInt(39, equip != null ? equip.getPotential2() : 0);
                            ps.setInt(40, equip != null ? equip.getPotential3() : 0);
                            ps.setInt(41, equip != null ? equip.getPotential4() : 0);
                            ps.setInt(42, equip != null ? equip.getPotential5() : 0);
                            ps.setInt(43, equip != null ? equip.getSocket1() : 0);
                            ps.setInt(44, equip != null ? equip.getSocket2() : 0);
                            ps.setInt(45, equip != null ? equip.getSocket3() : 0);
                            ps.setInt(46, equip != null ? equip.getIncSkill() : 0);
                            ps.setInt(47, equip != null ? equip.getCharmEXP() : 0);
                            ps.setInt(48, equip != null ? equip.getPVPDamage() : 0);
                            ps.setInt(49, equip != null ? equip.getPower() : 0);
                            ps.setInt(50, equip != null ? equip.getOverPower() : 0);
                            ps.setInt(51, equip != null ? equip.getTotalDamage() : 0);
                            ps.setInt(52, equip != null ? equip.getBossDamage() : 0);
                            ps.setInt(53, equip != null ? equip.getIED() : 0);
                            ps.setInt(54, equip != null ? equip.getCritDamage() : 0);
                            ps.setInt(55, equip != null ? equip.getAllStat() : 0);
                            ps.setLong(56, equip != null ? equip.getOStr() : 0);
                            ps.setLong(57, equip != null ? equip.getODex() : 0);
                            ps.setLong(58, equip != null ? equip.getOInt() : 0);
                            ps.setLong(59, equip != null ? equip.getOLuk() : 0);
                            ps.setLong(60, equip != null ? equip.getOAtk() : 0);
                            ps.setLong(61, equip != null ? equip.getOMatk() : 0);
                            ps.setLong(62, equip != null ? equip.getODef() : 0);
                            ps.setLong(63, equip != null ? equip.getOMdef() : 0);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
    //OLD SYSTEM

    public void saveItem(Item item, int id, Connection con) throws SQLException {
        Lock lock = locks[id % lockCount];
        lock.lock();
        try {
            MapleInventoryType mit = item.getInventoryType();
            StringBuilder query = new StringBuilder();
            if (value == 1) {
                query.append("DELETE `inventorystorage` FROM `inventorystorage` WHERE `type` = ? AND `");
            } else {
                if (mit == MapleInventoryType.EQUIP) {
                    query.append("DELETE `inventoryequips` FROM `inventoryequips` WHERE `type` = ? AND `");
                } else {
                    query.append("DELETE `inventoryitems` FROM `inventoryitems` WHERE `type` = ? AND `");
                }
            }
            query.append(account ? "accountid" : "characterid").append("` = ? AND `");
            query.append("position").append("` = ? AND `");
            query.append("inventorytype").append("` = ?");

            try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                ps.setByte(1, value);
                ps.setInt(2, id);
                ps.setInt(3, item.getPosition());
                ps.setInt(4, mit.getType());
                ps.executeUpdate();
                ps.close();
            }
            if (value == 1) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventorystorage` VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setByte(1, value);
                    ps.setString(2, account ? null : String.valueOf(id));
                    ps.setString(3, account ? String.valueOf(id) : null);
                    ps.setInt(4, item.getItemId());
                    ps.setString(5, item.getItemName(item.getItemId()));
                    ps.setInt(6, mit.getType());
                    ps.setInt(7, item.getPosition());
                    ps.setInt(8, item.getQuantity());
                    ps.setString(9, item.getOwner());
                    ps.setString(10, item.getGMLog());
                    if (item.getPet() != null) { //expensif?
                        ps.setInt(11, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                    } else {
                        ps.setInt(11, item.getUniqueId());
                    }
                    ps.setShort(12, item.getFlag());
                    ps.setLong(13, item.getExpiration());
                    ps.setString(14, item.getGiftFrom());
                    Equip equip = null;
                    if (mit == MapleInventoryType.EQUIP) {
                        equip = (Equip) item;
                    }
                    ps.setInt(15, equip != null ? equip.getUpgradeSlots() : 0);
                    ps.setInt(16, equip != null ? equip.getLevel() : 0);
                    ps.setInt(17, equip != null ? equip.getStr() : 0);
                    ps.setInt(18, equip != null ? equip.getDex() : 0);
                    ps.setInt(19, equip != null ? equip.getInt() : 0);
                    ps.setInt(20, equip != null ? equip.getLuk() : 0);
                    ps.setInt(21, equip != null ? equip.getHp() : 0);
                    ps.setInt(22, equip != null ? equip.getHpr() : 0);
                    ps.setInt(23, equip != null ? equip.getMp() : 0);
                    ps.setInt(24, equip != null ? equip.getMpr() : 0);
                    ps.setInt(25, equip != null ? equip.getWatk() : 0);
                    ps.setInt(26, equip != null ? equip.getMatk() : 0);
                    ps.setInt(27, equip != null ? equip.getWdef() : 0);
                    ps.setInt(28, equip != null ? equip.getMdef() : 0);
                    ps.setInt(29, equip != null ? equip.getAcc() : 0);
                    ps.setInt(30, equip != null ? equip.getAvoid() : 0);
                    ps.setInt(31, equip != null ? equip.getHands() : 0);
                    ps.setInt(32, equip != null ? equip.getSpeed() : 0);
                    ps.setInt(33, equip != null ? equip.getJump() : 0);
                    ps.setInt(34, equip != null ? equip.getViciousHammer() : 0);
                    ps.setInt(35, equip != null ? equip.getItemEXP() : 0);
                    ps.setInt(36, equip != null ? equip.getDurability() : 0);
                    ps.setInt(37, equip != null ? equip.getEnhance() : 0);
                    ps.setInt(38, equip != null ? equip.getPotential1() : 0);
                    ps.setInt(39, equip != null ? equip.getPotential2() : 0);
                    ps.setInt(40, equip != null ? equip.getPotential3() : 0);
                    ps.setInt(41, equip != null ? equip.getPotential4() : 0);
                    ps.setInt(42, equip != null ? equip.getPotential5() : 0);
                    ps.setInt(43, equip != null ? equip.getSocket1() : 0);
                    ps.setInt(44, equip != null ? equip.getSocket2() : 0);
                    ps.setInt(45, equip != null ? equip.getSocket3() : 0);
                    ps.setInt(46, equip != null ? equip.getIncSkill() : 0);
                    ps.setInt(47, equip != null ? equip.getCharmEXP() : 0);
                    ps.setInt(48, equip != null ? equip.getPVPDamage() : 0);
                    ps.setInt(49, equip != null ? equip.getPower() : 0);
                    ps.setInt(50, equip != null ? equip.getOverPower() : 0);
                    ps.setInt(51, equip != null ? equip.getTotalDamage() : 0);
                    ps.setInt(52, equip != null ? equip.getBossDamage() : 0);
                    ps.setInt(53, equip != null ? equip.getIED() : 0);
                    ps.setInt(54, equip != null ? equip.getCritDamage() : 0);
                    ps.setInt(55, equip != null ? equip.getAllStat() : 0);
                    ps.setLong(56, equip != null ? equip.getOStr() : 0);
                    ps.setLong(57, equip != null ? equip.getODex() : 0);
                    ps.setLong(58, equip != null ? equip.getOInt() : 0);
                    ps.setLong(59, equip != null ? equip.getOLuk() : 0);
                    ps.setLong(60, equip != null ? equip.getOAtk() : 0);
                    ps.setLong(61, equip != null ? equip.getOMatk() : 0);
                    ps.setLong(62, equip != null ? equip.getODef() : 0);
                    ps.setLong(63, equip != null ? equip.getOMdef() : 0);
                    ps.executeUpdate();
                }
            } else {
                if (mit != MapleInventoryType.EQUIP) {
                    //items
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventoryitems` VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setByte(1, value);
                        ps.setString(2, account ? null : String.valueOf(id));
                        ps.setString(3, account ? String.valueOf(id) : null);
                        ps.setInt(4, item.getItemId());
                        ps.setString(5, item.getItemName(item.getItemId()));
                        ps.setInt(6, mit.getType());
                        ps.setInt(7, item.getPosition());
                        ps.setInt(8, item.getQuantity());
                        ps.setString(9, item.getOwner());
                        ps.setString(10, item.getGMLog());
                        if (item.getPet() != null) { //expensif?
                            ps.setInt(11, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                        } else {
                            ps.setInt(11, item.getUniqueId());
                        }
                        ps.setShort(12, item.getFlag());
                        ps.setLong(13, item.getExpiration());
                        ps.setString(14, item.getGiftFrom());
                        ps.executeUpdate();

                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventoryequips` VALUES (DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setByte(1, value);
                        ps.setString(2, account ? null : String.valueOf(id));
                        ps.setString(3, account ? String.valueOf(id) : null);
                        ps.setInt(4, item.getItemId());
                        ps.setString(5, item.getItemName(item.getItemId()));
                        ps.setInt(6, mit.getType());
                        ps.setInt(7, item.getPosition());
                        ps.setInt(8, item.getQuantity());
                        ps.setString(9, item.getOwner());
                        ps.setString(10, item.getGMLog());
                        if (item.getPet() != null) { //expensif?
                            ps.setInt(11, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                        } else {
                            ps.setInt(11, item.getUniqueId());
                        }
                        ps.setShort(12, item.getFlag());
                        ps.setLong(13, item.getExpiration());
                        ps.setString(14, item.getGiftFrom());
                        Equip equip = (Equip) item;
                        ps.setInt(15, equip.getUpgradeSlots());
                        ps.setInt(16, equip.getLevel());
                        ps.setInt(17, equip.getStr());
                        ps.setInt(18, equip.getDex());
                        ps.setInt(19, equip.getInt());
                        ps.setInt(20, equip.getLuk());
                        ps.setInt(21, equip.getHp());
                        ps.setInt(22, equip.getHpr());
                        ps.setInt(23, equip.getMp());
                        ps.setInt(24, equip.getMpr());
                        ps.setInt(25, equip.getWatk());
                        ps.setInt(26, equip.getMatk());
                        ps.setInt(27, equip.getWdef());
                        ps.setInt(28, equip.getMdef());
                        ps.setInt(29, equip.getAcc());
                        ps.setInt(30, equip.getAvoid());
                        ps.setInt(31, equip.getHands());
                        ps.setInt(32, equip.getSpeed());
                        ps.setInt(33, equip.getJump());
                        ps.setInt(34, equip.getViciousHammer());
                        ps.setInt(35, equip.getItemEXP());
                        ps.setInt(36, equip.getDurability());
                        ps.setInt(37, equip.getEnhance());
                        ps.setInt(38, equip.getPotential1());
                        ps.setInt(39, equip.getPotential2());
                        ps.setInt(40, equip.getPotential3());
                        ps.setInt(41, equip.getPotential4());
                        ps.setInt(42, equip.getPotential5());
                        ps.setInt(43, equip.getSocket1());
                        ps.setInt(44, equip.getSocket2());
                        ps.setInt(45, equip.getSocket3());
                        ps.setInt(46, equip.getIncSkill());
                        ps.setInt(47, equip.getCharmEXP());
                        ps.setInt(48, equip.getPVPDamage());
                        ps.setInt(49, equip.getPower());
                        ps.setLong(50, equip.getOStr());
                        ps.setLong(51, equip.getODex());
                        ps.setLong(52, equip.getOInt());
                        ps.setLong(53, equip.getOLuk());
                        ps.setLong(54, equip.getOAtk());
                        ps.setLong(55, equip.getOMatk());
                        ps.setLong(56, equip.getODef());
                        ps.setLong(57, equip.getOMdef());
                        ps.executeUpdate();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
