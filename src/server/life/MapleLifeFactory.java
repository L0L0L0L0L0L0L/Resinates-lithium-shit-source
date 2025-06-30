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
import database.DatabaseConnection;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.Randomizer;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;

public class MapleLifeFactory {

    // private static MapleDataProvider data =
    // MapleDataProviderFactory.getDataProvider(new
    // File(System.getProperty("wzpath") + "/Mob.wz"));
    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Mob.wz"));
    private static final MapleDataProvider npcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Npc.wz"));
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
    private static final MapleDataProvider etcDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
    private static final MapleData mobStringData = stringDataWZ.getData("Mob.img");
    private static final MapleData npcStringData = stringDataWZ.getData("Npc.img");
    private static final MapleData npclocData = etcDataWZ.getData("NpcLocation.img");
    private static Map<Integer, String> npcNames = new HashMap<Integer, String>();
    private static Map<Integer, String> mobNames = new HashMap<Integer, String>();
    private static Map<Integer, MapleMonsterStats> monsterStats = new ConcurrentHashMap<Integer, MapleMonsterStats>();
    private static Map<Integer, MapleMonsterStats> summonStats = new ConcurrentHashMap<Integer, MapleMonsterStats>();
    private static Map<Integer, Integer> NPCLoc = new HashMap<Integer, Integer>();
    private static Map<Integer, List<Integer>> questCount = new HashMap<Integer, List<Integer>>();

    public static AbstractLoadedMapleLife getLife(int id, String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        } else if (type.equalsIgnoreCase("m")) {
            return getMonster(id);
        } else {
            System.err.println("Unknown Life type: " + type + "");
            return null;
        }
    }

    public static String getName(int id) {
        if (mobNames.containsKey(id)) {
            return mobNames.get(id);
        }
        return "unknown";
    }

    public static int getNPCLocation(int npcid) {
        if (NPCLoc.containsKey(npcid)) {
            return NPCLoc.get(npcid);
        }
        final int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", npclocData, -1);
        NPCLoc.put(npcid, map);
        return map;
    }

    public static void loadQuestCounts() {
        if (questCount.size() > 0) {
            return;
        }
        for (MapleDataDirectoryEntry mapz : data.getRoot().getSubdirectories()) {
            if (mapz.getName().equals("QuestCountGroup")) {
                for (MapleDataFileEntry entry : mapz.getFiles()) {
                    final int id = Integer.parseInt(entry.getName().substring(0, entry.getName().length() - 4));
                    MapleData dat = data.getData("QuestCountGroup/" + entry.getName());
                    if (dat != null && dat.getChildByPath("info") != null) {
                        List<Integer> z = new ArrayList<Integer>();
                        for (MapleData da : dat.getChildByPath("info")) {
                            z.add(MapleDataTool.getInt(da, 0));
                        }
                        questCount.put(id, z);
                    } else {
                        System.out.println("null questcountgroup");
                    }
                }
            }
        }
        System.out.println("[Quests] Mapz loaded...");
        for (MapleData c : npcStringData) {
            int nid = Integer.parseInt(c.getName());
            String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);
            try {
                if (npcData.getData(n) != null) {// only thing we really have to do is check if it exists. if we wanted
                    // to, we could get the script as well :3
                    String name = MapleDataTool.getString("name", c, "MISSINGNO");
                    if (name.contains("Maple TV") || name.contains("Baby Moon Bunny")) {
                        continue;
                    }
                    npcNames.put(nid, name);
                }
            } catch (NullPointerException e) {
                System.out.println("error npc ID: " + nid);
                e.printStackTrace();
            } catch (RuntimeException e) { // swallow, don't add if
                System.out.println("error npc ID: " + nid);
                e.printStackTrace();
            }
        }
        System.out.println("[Quests] Npc Strings loaded...");
        for (MapleData c : mobStringData) {
            int nid = Integer.parseInt(c.getName());
            try {
                if (mobStringData.getChildByPath(String.valueOf(nid)) != null) {// only thing we really have to do is
                    // check if it exists. if we wanted to,
                    // we could get the script as well :3
                    mobNames.put(nid, MapleDataTool.getString("name", c, "MISSINGNO"));
                }
            } catch (NullPointerException e) {
                System.out.println("error mob ID: " + nid);
                e.printStackTrace();
            } catch (RuntimeException e) { // swallow, don't add if
                System.out.println("error mob ID: " + nid);
                e.printStackTrace();
            }
        }
        System.out.println("[Quests] Mobs loaded...");
    }

    public static List<Integer> getQuestCount(final int id) {
        return questCount.get(id);
    }

    public static MapleMonsterStats getBasicStats(int id) {
        MapleMonsterStats stats;
        if (!monsterStats.containsKey(id)) {
            Pair<MapleMonsterStats, List<MobAttackInfoHolder>> mobStats = getMonsterStats(id);
            if (mobStats == null) {
                System.out.println("[SEVERE] MOB STATS " + id + " failed to load.");
                return null;
            }
            stats = mobStats.getLeft();
            setMonsterAttackInfo(id, mobStats.getRight());
            Rank.loadMob(stats, id);
            stats.setBaseScale(stats.getScale());
            monsterStats.put(id, stats);
        } else {
            stats = monsterStats.get(id);
        }
        return stats;
    }

    public static MapleMonster getBaseMonster(int mid) {
        try {
            MapleMonsterStats stats = getBasicStats(mid);
            if (stats == null) {
                return null;
            }
            return new MapleMonster(mid, stats);
        } catch (NullPointerException npe) {
            System.out.println("[SEVERE] MOB " + mid + " failed to load. Issue: " + npe.getMessage() + "\n\n");
            //npe.printStackTrace();
            return null;
        }
    }

    public static void forceLoad() {
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM mobdata"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                getBaseMonster(rs.getInt("mobid"));
            }
            System.out.println("Monsters from Database preloaded.");
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load quests");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
    }

    public static MapleMonster getRealMonster(int mid) {
        return getBaseMonster(mid);
    }

    public static MapleMonster getMonster(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
        }
        return monster;
    }

    public static MapleMonster getMonster(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(level, monster.getStats().getScale(), true);
        }
        return monster;
    }

    public static MapleMonster getMonster(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(level, scale, true);
        }
        return monster;
    }

    public static MapleMonster getMonsterCap(int mid, int level, int scale, int cap) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(level, scale, true);
            if (cap > 0) {
                monster.setDamageCap(cap);
            }
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setRaidBoss(true);
            monster.getStats().setExplosiveReward(true);
            monster.getStats().setKaotic(true);
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setRaidBoss(true);
            monster.getStats().setExplosiveReward(true);
            monster.getStats().setKaotic(true);
            monster.changeLevel(level, monster.getStats().getScale(), true);
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setRaidBoss(true);
            monster.getStats().setExplosiveReward(true);
            monster.getStats().setKaotic(true);
            monster.changeLevel(level, scale, true);
        }
        return monster;
    }

    public static MapleMonster getKaoticMonsters(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setKaotic(true);
            monster.changeLevel(level, scale, true);
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid, int level, int scale, boolean bar, boolean link, boolean drops) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setExplosiveReward(drops && bar);
            monster.getStats().setKaotic(true);
            monster.getStats().setDrops(drops);
            if (!link) {
                monster.getStats().disableRevives();
            }
            monster.changeLevel(level, scale, true);
            monster.getStats().setBar(bar);
            if (!drops) {
                monster.disableDrops();
            }
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getMonster(int mid, int level, int scale, boolean bar, boolean link, boolean drops, long fixed) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setBar(bar);
            monster.getStats().setExplosiveReward(drops && bar);
            monster.getStats().setDrops(drops);
            if (!link) {
                monster.getStats().disableRevives();
            }
            if (!drops) {
                monster.disableDrops();
            }
            if (fixed > 0) {
                monster.getStats().setCapped(true);
                monster.getStats().hits = fixed;
            }
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid, int level, int scale, boolean bar, boolean link, boolean drops, long fixed) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setBar(bar);
            monster.getStats().setExplosiveReward(drops && bar);
            monster.getStats().setKaotic(true);
            monster.getStats().setDrops(drops);
            if (!link) {
                monster.getStats().disableRevives();
            }
            if (!drops) {
                monster.disableDrops();
            }
            if (fixed > 0) {
                monster.getStats().setCapped(true);
                monster.getStats().hits = fixed;
            }
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid, int level, int scale, boolean bar, boolean link, boolean drops, long fixed, boolean mega, boolean kaotic) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().mega = mega;
            monster.getStats().kdboss = kaotic;
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setBar(bar);
            monster.getStats().setExplosiveReward(drops && bar);
            monster.getStats().setKaotic(true);
            monster.getStats().setDrops(drops);
            if (!link) {
                monster.getStats().disableRevives();
            }
            if (!drops) {
                monster.disableDrops();
            }
            if (fixed > 0) {
                monster.getStats().setCapped(true);
                monster.getStats().hits = fixed;
            }
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getKaoticMonster(int mid, int level, int scale, boolean bar, boolean link, boolean drops, long fixed, boolean mega, boolean kaotic, boolean ult) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().mega = mega;
            monster.getStats().kdboss = kaotic;
            monster.getStats().ultimate = ult;
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setBar(bar);
            monster.getStats().setExplosiveReward(drops && bar);
            monster.getStats().setKaotic(true);
            monster.getStats().setDrops(drops);
            if (!link) {
                monster.getStats().disableRevives();
            }
            if (!drops) {
                monster.disableDrops();
            }
            if (fixed > 0) {
                monster.getStats().setCapped(true);
                monster.getStats().hits = fixed;
            }
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getKaoticLinkMonster(int mid, int level, int scale, boolean bar, boolean link, boolean drops, long fixed) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setBar(bar);
            monster.getStats().setKaotic(true);
            if (!link) {
                monster.getStats().disableRevives();
            }
            if (drops && !monster.getStats().getRevives().isEmpty()) {
                monster.getStats().setExplosiveReward(drops && bar);
                if (!drops) {
                    monster.disableDrops();
                }
            } else {
                monster.getStats().setExplosiveReward(drops);
                if (!drops) {
                    monster.disableDrops();
                }
            }
            if (fixed > 0) {
                monster.getStats().setCapped(true);
                monster.getStats().hits = fixed;
            }
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getKaoticLinkMonsterMega(int mid, int level, int scale, boolean bar, boolean link, boolean drops, long fixed, boolean mega) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            if (bar) {
                monster.getStats().setRaidBoss(true);
                monster.getStats().setSuperKaotic(true);
            }
            monster.getStats().setBar(bar);
            monster.getStats().setKaotic(true);
            if (!link) {
                monster.getStats().disableRevives();
            }
            if (drops && !monster.getStats().getRevives().isEmpty()) {
                monster.getStats().setExplosiveReward(drops && bar);
                if (!drops) {
                    monster.disableDrops();
                }
            } else {
                monster.getStats().setExplosiveReward(drops);
                if (!drops) {
                    monster.disableDrops();
                }
            }
            if (fixed > 0) {
                monster.getStats().setCapped(true);
                monster.getStats().hits = fixed;
            }
            monster.getStats().setMega(mega);
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("ERROR with boss");
        }
        return monster;
    }

    public static MapleMonster getMonster(int mid, int level, int scale, boolean partyMob) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(level, scale, true);
        } else {
            System.out.println("NULL: " + mid);
        }
        return monster;
    }

    public static MapleMonster getMonsterNoLink(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
        }
        return monster;
    }

    public static MapleMonster getMonsterNoLink(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.changeLevel(level, monster.getStats().getScale(), true);
        }
        return monster;
    }

    public static MapleMonster getMonsterNoLink(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.changeLevel(level, scale, true);
        }
        return monster;
    }

    public static MapleMonster getMonsterNoLinkRank(int mid, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.changeLevel(monster.getStats().getLevel(), scale, true);
        }
        return monster;
    }

    public static MapleMonster getMonsterScale(int mid, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(monster.getStats().getLevel(), scale, true);
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDrops(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
            monster.disableDrops();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDrops(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, monster.getStats().getScale(), true);
            monster.disableDrops();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDrops(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, scale, true);
            monster.disableDrops();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDropsLink(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
            monster.getStats().disableRevives();
            monster.disableDrops();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDropsLink(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, monster.getStats().getScale(), true);
            monster.getStats().disableRevives();
            monster.disableDrops();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDropsLink(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, scale, true);
            monster.getStats().disableRevives();
            monster.disableDrops();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoAll(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
            monster.disableDrops();
            monster.disableExp();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoAll(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, monster.getStats().getScale(), true);
            monster.disableDrops();
            monster.disableExp();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoAll(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().disableRevives();
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, scale, true);
            monster.disableDrops();
            monster.disableExp();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDropsExp(int mid) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(monster.getStats().getLevel(), monster.getStats().getScale(), true);
            monster.disableDrops();
            monster.disableExp();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDropsExp(int mid, int level) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, monster.getStats().getScale(), true);
            monster.disableDrops();
            monster.disableExp();
        }
        return monster;
    }

    public static MapleMonster getMonsterNoDropsExp(int mid, int level, int scale) {
        MapleMonster monster = getBaseMonster(mid);
        if (monster != null) {
            monster.setDelay(false);
            monster.getStats().setExplosiveReward(false);
            monster.changeLevel(level, scale, true);
            monster.disableDrops();
            monster.disableExp();
        }
        return monster;
    }

    public static MapleMonster getSummonMonster(int mid, int level, int scale) {
        try {
            MapleMonsterStats stats = summonStats.get(Integer.valueOf(mid));
            if (stats == null) {
                Pair<MapleMonsterStats, List<MobAttackInfoHolder>> mobStats = getMonsterStats(mid);
                if (mobStats == null) {
                    System.out.println("[SEVERE] SUMMON MOB " + mid + " failed to load.");
                    return null;
                }
                stats = mobStats.getLeft();
                setMonsterAttackInfo(mid, mobStats.getRight());
                summonStats.put(mid, stats);
            }
            stats.setBaseScale(stats.getScale());
            MapleMonster mob = new MapleMonster(mid, stats);
            mob.setDelay(false);
            mob.getStats().setExplosiveReward(false);
            mob.changeLevel(level, Randomizer.Min(scale, 1), true);
            mob.disableDrops();
            mob.disableExp();
            mob.toggleSummon(true);
            return mob;
        } catch (NullPointerException npe) {
            System.out.println("[SEVERE] SUMMON MOB " + mid + " failed to load. Issue: " + npe.getMessage() + "\n\n");
            npe.printStackTrace();
            return null;
        }
    }

    public static MapleMonster getSummonKaoticMonster(int mid, int level, int scale) {
        try {
            MapleMonsterStats stats = summonStats.get(Integer.valueOf(mid));
            if (stats == null) {
                Pair<MapleMonsterStats, List<MobAttackInfoHolder>> mobStats = getMonsterStats(mid);
                if (mobStats == null) {
                    System.out.println("[SEVERE] SUMMON MOB " + mid + " failed to load.");
                    return null;
                }
                stats = mobStats.getLeft();
                setMonsterAttackInfo(mid, mobStats.getRight());
                summonStats.put(mid, stats);
            }
            stats.setBaseScale(stats.getScale());
            MapleMonster mob = new MapleMonster(mid, stats);
            mob.setDelay(false);
            mob.getStats().setKaotic(true);
            mob.getStats().setExplosiveReward(false);
            mob.changeLevel(level, Randomizer.Min(scale, 1), true);
            mob.disableDrops();
            mob.disableExp();
            mob.toggleSummon(true);
            return mob;
        } catch (NullPointerException npe) {
            System.out.println("[SEVERE] SUMMON MOB " + mid + " failed to load. Issue: " + npe.getMessage() + "\n\n");
            npe.printStackTrace();
            return null;
        }
    }

    private static Pair<MapleMonsterStats, List<MobAttackInfoHolder>> getMonsterStats(int mid) {
        MapleData monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
        if (monsterData == null) {
            return null;
        }

        MapleData monsterInfoData = monsterData.getChildByPath("info");

        List<MobAttackInfoHolder> attackInfos = new LinkedList<>();
        MapleMonsterStats stats;
        int linkMid = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
        if (linkMid == 0) {
            stats = new MapleMonsterStats();
        } else {
            Pair<MapleMonsterStats, List<MobAttackInfoHolder>> linkStats = getMonsterStats(linkMid);
            if (linkStats == null) {
                return null;
            }
            stats = linkStats.getLeft().copy(mid);
            attackInfos.addAll(linkStats.getRight());
        }
        stats.setHp(1);
        stats.setMp(Integer.MAX_VALUE);
        stats.setExp(1);
        stats.setId(mid);
        stats.setLevel((short) Rank.getMobLevel(mid));
        stats.setScale(Rank.getMobRank(mid));
        stats.setCharismaEXP((short) MapleDataTool.getIntConvert("charismaEXP", monsterInfoData, 0));
        stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
        stats.setrareItemDropLevel((byte) MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));
        stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));
        if (stats.getFixedDamage() != -1) {
            stats.setCapped(true);
        }
        stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
        stats.setBoss(MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0);
        stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0 || (stats.getScale() >= 5 && stats.isBoss()));
        stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
        stats.setEscort(MapleDataTool.getIntConvert("escort", monsterInfoData, 0) > 0);
        stats.setPartyBonusRate(MapleDataTool.getIntConvert("partyBonusR", monsterInfoData, 0));
        stats.setName(getName(mid));// add name later
        stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
        stats.setChange(MapleDataTool.getIntConvert("changeableMob", monsterInfoData, 0) > 0);
        stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);
        stats.setNoDoom(MapleDataTool.getIntConvert("noDoom", monsterInfoData, 0) > 0);
        stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
        stats.setCP((byte) MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
        stats.setPoint(MapleDataTool.getIntConvert("point", monsterInfoData, 0));
        stats.setDropItemPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0));
        stats.setPhysicalAttack(MapleDataTool.getIntConvert("PADamage", monsterInfoData, 0));
        stats.setMagicAttack(MapleDataTool.getIntConvert("MADamage", monsterInfoData, 0));
        // stats.setPDRate(MapleDataTool.getIntConvert("PDRate", monsterInfoData, 0));
        // stats.setMDRate(MapleDataTool.getIntConvert("MDRate", monsterInfoData, 0));

        stats.setPDRate(0);
        stats.setMDRate(0);

        stats.setAcc(MapleDataTool.getIntConvert("acc", monsterInfoData, 0));
        stats.setEva(MapleDataTool.getIntConvert("eva", monsterInfoData, 0));
        stats.setSummonType((byte) 1);
        stats.setCategory((byte) MapleDataTool.getIntConvert("category", monsterInfoData, 0));
        stats.setSpeed(MapleDataTool.getIntConvert("speed", monsterInfoData, 0));
        stats.setPushed(MapleDataTool.getIntConvert("pushed", monsterInfoData, 0));
        // final boolean hideHP = MapleDataTool.getIntConvert("HPgaugeHide",
        // monsterInfoData, 0) > 0 || MapleDataTool.getIntConvert("hideHP",
        // monsterInfoData, 0) > 0;
        MapleData special = monsterInfoData.getChildByPath("selfDestruction");
        if (special != null) {
            stats.setSelfDestruction(new selfDestruction((byte) MapleDataTool.getInt(special.getChildByPath("action")),
                    MapleDataTool.getIntConvert("removeAfter", special, -1),
                    MapleDataTool.getIntConvert("hp", special, -1)));
        }
        final MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
        if (firstAttackData != null) {
            if (firstAttackData.getType() == MapleDataType.FLOAT) {
                stats.setFirstAttack(Math.round(MapleDataTool.getFloat(firstAttackData)) > 0);
            } else {
                stats.setFirstAttack(MapleDataTool.getInt(firstAttackData) > 0);
            }
        }
        if (stats.isBoss() || isDmgSponge(mid)) {
            if (monsterInfoData.getChildByPath("hpTagColor") == null || monsterInfoData.getChildByPath("hpTagBgcolor") == null) {
                stats.setTagColor(0);
                stats.setTagBgColor(0);
            } else {
                stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
                stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
                stats.setBar(true);
            }
        }

        final MapleData banishData = monsterInfoData.getChildByPath("ban");
        if (banishData != null) {
            stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData),
                    MapleDataTool.getInt("banMap/0/field", banishData, -1),
                    MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
        }

        if (linkMid != 0) {
            stats.setRevives(new LinkedList<Integer>());
        }
        MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
        if (reviveInfo != null) {
            List<Integer> revives = new LinkedList<>();
            for (MapleData bdata : reviveInfo) {
                revives.add(MapleDataTool.getInt(bdata));
            }
            stats.setRevives(revives);
        }
        /*else {
         if (!stats.getRevives().isEmpty()) {
         stats.getRevives().clear();
         }
         }*/
        for (MapleData idata : monsterData) {
            if (!idata.getName().equals("info")) {
                int delay = 0;
                for (MapleData pic : idata.getChildren()) {
                    delay += MapleDataTool.getIntConvert("delay", pic, 0);
                }
                stats.setAnimationTime(idata.getName(), delay);
            }
        }
        final MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
        if (monsterSkillData != null) {
            int i = 0;
            List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
            while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                skills.add(new Pair<Integer, Integer>(
                        Integer.valueOf(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0)),
                        Integer.valueOf(MapleDataTool.getInt(i + "/level", monsterSkillData, 0))));
                i++;
            }
            stats.setSkills(skills);
        }

        decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));

        // Other data which isn;t in the mob, but might in the linked data
        final int link = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
        if (link != 0) { // Store another copy, for faster processing.
            monsterData = data.getData(StringUtil.getLeftPaddedStr(link + ".img", '0', 11));
        }

        for (MapleData idata : monsterData) {
            if (idata.getName().equals("fly")) {
                stats.setFly(true);
                stats.setMobile(true);
                break;
            } else if (idata.getName().equals("move")) {
                stats.setMobile(true);
            }
        }

        int i = 0;
        MapleData monsterAttackData;
        while ((monsterAttackData = monsterData.getChildByPath("attack" + (i + 1))) != null) {
            final MapleData monsterAtt = monsterInfoData.getChildByPath("attack/" + i);
            int animationTime = 0;
            for (MapleData effectEntry : monsterAttackData.getChildren()) {
                animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }

            // int mpCon = MapleDataTool.getIntConvert("info/conMP", monsterAttackData, 0);
            // int coolTime = MapleDataTool.getIntConvert("info/attackAfter",
            // monsterAttackData, 0);
            attackInfos.add(new MobAttackInfoHolder(i, 0, 1000, animationTime));
            i++;
        }

        /*
         * for (int i = 0; true; i++) { // TODO: Check and reprint all available values
         * again..doing like below is a ridiculous way final MapleData monsterAtt =
         * monsterInfoData.getChildByPath("attack/" + i); final MapleData attackData =
         * monsterData.getChildByPath("attack" + (i + 1) + "/info"); if (attackData ==
         * null || monsterAtt == null) { break; } final MobAttackInfo ret = new
         * MobAttackInfo();
         * 
         * mi.setMobAttackAnimationTime(mid, attackInfo.attackPos,
         * attackInfo.animationTime); mi.setMobAttackInfo(mid, attackInfo.attackPos,
         * attackInfo.mpCon, attackInfo.animationTime);
         * 
         * boolean deadlyAttack = monsterAtt.getChildByPath("deadlyAttack") != null; if
         * (!deadlyAttack) { deadlyAttack = attackData.getChildByPath("deadlyAttack") !=
         * null; } ret.setDeadlyAttack(deadlyAttack);
         * 
         * int mpBurn = MapleDataTool.getInt("mpBurn", monsterAtt, 0); if (mpBurn == 0)
         * { mpBurn = MapleDataTool.getInt("mpBurn", attackData, 0); }
         * ret.setMpBurn(mpBurn);
         * 
         * int disease = MapleDataTool.getInt("disease", monsterAtt, 0); if (disease ==
         * 0) { disease = MapleDataTool.getInt("disease", attackData, 0); }
         * ret.setDiseaseSkill(disease);
         * 
         * int level = MapleDataTool.getInt("level", monsterAtt, 0); if (level == 0) {
         * level = MapleDataTool.getInt("level", attackData, 0); }
         * ret.setDiseaseLevel(level);
         * 
         * int conMP = MapleDataTool.getInt("conMP", monsterAtt, 0); if (conMP == 0) {
         * conMP = MapleDataTool.getInt("conMP", attackData, 0); } ret.setMpCon(conMP);
         * 
         * int attackAfter = MapleDataTool.getInt("attackAfter", monsterAtt, 0); if
         * (attackAfter == 0) { attackAfter = MapleDataTool.getInt("attackAfter",
         * attackData, 0); } ret.attackAfter = attackAfter;
         * 
         * int PADamage = MapleDataTool.getInt("PADamage", monsterAtt, 0); if (PADamage
         * == 0) { PADamage = MapleDataTool.getInt("PADamage", attackData, 0); }
         * ret.PADamage = PADamage;
         * 
         * int MADamage = MapleDataTool.getInt("MADamage", monsterAtt, 0); if (MADamage
         * == 0) { MADamage = MapleDataTool.getInt("MADamage", attackData, 0); }
         * ret.MADamage = MADamage;
         * 
         * boolean magic = MapleDataTool.getInt("magic", monsterAtt, 0) > 0; if (!magic)
         * { magic = MapleDataTool.getInt("magic", attackData, 0) > 0; } ret.magic =
         * magic; ret.isElement = monsterAtt.getChildByPath("elemAttr") != null; // we
         * handle it like this, i don't know what it does
         * 
         * if (attackData.getChildByPath("range") != null) { // Definitely in attackData
         * ret.range = MapleDataTool.getInt("range/r", attackData, 0); if
         * (attackData.getChildByPath("range/lt") != null &&
         * attackData.getChildByPath("range/rb") != null) { ret.lt = (Point)
         * attackData.getChildByPath("range/lt").getData(); ret.rb = (Point)
         * attackData.getChildByPath("range/rb").getData(); } } stats.addMobAttack(ret);
         * }
         */
        byte hpdisplaytype = -1;
        if (stats.getTagColor() > 0) {
            hpdisplaytype = 0;
        } else if (stats.isFriendly()) {
            hpdisplaytype = 1;
        } else if (mid >= 9300184 && mid <= 9300215) { // Mulung TC mobs
            hpdisplaytype = 2;
        } else if (!stats.isBoss() || mid == 9410066 || stats.isPartyBonus()) { // Not boss and dong dong chiang
            hpdisplaytype = 3;
        }
        stats.setHPDisplayType(hpdisplaytype);
        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        mi.retrieveDrop(mid);
        return new Pair<>(stats, attackInfos);
    }

    public static final void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)),
                    ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }

    private static final boolean isDmgSponge(final int mid) {
        switch (mid) {
            case 8810018:
            case 8810118:
            case 8810119:
            case 8810120:
            case 8810121:
            case 8810122:
            case 8820009:
            case 8820010:
            case 8820011:
            case 8820012:
            case 8820013:
            case 8820014:
                return true;
        }
        return false;
    }

    public static MapleNPC getNPC(final int nid) {
        String name = npcNames.get(nid);
        if (name == null) {
            return null;
        }
        return new MapleNPC(nid, name);
    }

    public static int getRandomNPC() {
        List<Integer> vals = new ArrayList<Integer>(npcNames.keySet());
        int ret = 0;
        while (ret <= 0) {
            ret = vals.get(Randomizer.nextInt(vals.size()));
            if (npcNames.get(ret).contains("MISSINGNO")) {
                ret = 0;
            }
        }
        return ret;

    }

    public static class selfDestruction {

        private byte action;
        private int removeAfter;
        private int hp;

        private selfDestruction(byte action, int removeAfter, int hp) {
            this.action = action;
            this.removeAfter = removeAfter;
            this.hp = hp;
        }

        public int getHp() {
            return hp;
        }

        public byte getAction() {
            return action;
        }

        public int removeAfter() {
            return removeAfter;
        }
    }

    private static class MobAttackInfoHolder {

        protected int attackPos;
        protected int mpCon;
        protected int coolTime;
        protected int animationTime;

        protected MobAttackInfoHolder(int attackPos, int mpCon, int coolTime, int animationTime) {
            this.attackPos = attackPos;
            this.mpCon = mpCon;
            this.coolTime = coolTime;
            this.animationTime = animationTime;
        }
    }

    private static void setMonsterAttackInfo(int mid, List<MobAttackInfoHolder> attackInfos) {
        if (!attackInfos.isEmpty()) {
            MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();

            for (MobAttackInfoHolder attackInfo : attackInfos) {
                // mi.setMobAttackInfo(mid, attackInfo.attackPos, attackInfo.mpCon,
                // attackInfo.coolTime);
                mi.setMobAttackAnimationTime(mid, attackInfo.attackPos, attackInfo.animationTime);
                mi.setMobAttackInfo(mid, attackInfo.attackPos, attackInfo.mpCon, attackInfo.animationTime);
            }
        }
    }
}
