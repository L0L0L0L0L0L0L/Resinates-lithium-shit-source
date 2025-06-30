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
package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventInstanceManager;
import server.MaplePortal;
import server.Randomizer;
import server.life.AbstractLoadedMapleLife;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.life.MapleNPC;
import server.life.SpawnPoint;
import server.maps.MapleNodes.DirectionInfo;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import tools.Pair;
import tools.StringUtil;
import tools.packet.CField;

public class MapleMapFactory {

    private static MapleDataProvider source;
    private static MapleData nameData;
    //private final MapleDataProvider source = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz"));
    //private final MapleData nameData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Map.img");
    private final static Map<Integer, Pair<String, String>> mapsName = new HashMap<>();
    private final static HashMap<Integer, MapleMap> maps = new HashMap<Integer, MapleMap>();
    //private final static HashMap<Integer, MapleMap> instanceMap = new HashMap<Integer, MapleMap>();
    //private final static HashMap<Integer, MapleFootholdTree> MapleFootholds = new HashMap<Integer, MapleFootholdTree>();
    //private final static HashMap<Integer, Collection<SpawnPoint>> SpawnPoints = new HashMap<Integer, Collection<SpawnPoint>>();

    static {
        nameData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Map.img");
        source = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz"));
    }

    /*
     public final MapleMap getMap(final int mapid) {
     return getMap(mapid, true, true, true);
     }

     //backwards-compatible
     public final MapleMap getMap(final int mapid, final boolean respawns, final boolean npcs) {
     return getMap(mapid, respawns, npcs, true);
     }
     */
    public static MapleMap getMap(final int mapid, final boolean respawns, final boolean npcs, final boolean reactors, int channel, EventInstanceManager event) {
        Integer omapid = Integer.valueOf(mapid);
        MapleMap map;
        MapleData mapData;
        try {
            mapData = source.getData(getMapName(mapid));
        } catch (Exception e) {
            return null;
        }
        if (mapData == null) {
            return null;
        }
        MapleData link = mapData.getChildByPath("info/link");
        if (link != null) {
            mapData = source.getData(getMapName(MapleDataTool.getIntConvert("info/link", mapData)));
        }

        float monsterRate = 0;
        if (respawns) {
            MapleData mobRate = mapData.getChildByPath("info/mobRate");
            if (mobRate != null) {
                monsterRate = ((Float) mobRate.getData()).floatValue();
            }
        }
        map = new MapleMap(mapid, channel, MapleDataTool.getInt("info/returnMap", mapData), monsterRate);
        map.loading = true;
        map.setEventInstance(event);
        loadPortals(map, mapData.getChildByPath("portal"));
        map.setTop(MapleDataTool.getInt(mapData.getChildByPath("info/VRTop"), 0));
        map.setLeft(MapleDataTool.getInt(mapData.getChildByPath("info/VRLeft"), 0));
        map.setBottom(MapleDataTool.getInt(mapData.getChildByPath("info/VRBottom"), 0));
        map.setRight(MapleDataTool.getInt(mapData.getChildByPath("info/VRRight"), 0));
        List<MapleFoothold> allFootholds = new LinkedList<MapleFoothold>();
        Point lBound = new Point();
        Point uBound = new Point();
        MapleFoothold fh;
        for (MapleData footRoot : mapData.getChildByPath("foothold")) {
            for (MapleData footCat : footRoot) {
                for (MapleData footHold : footCat) {
                    fh = new MapleFoothold(
                            new Point(MapleDataTool.getInt(footHold.getChildByPath("x1"), 0), MapleDataTool.getInt(footHold.getChildByPath("y1"), 0)),
                            new Point(MapleDataTool.getInt(footHold.getChildByPath("x2"), 0), MapleDataTool.getInt(footHold.getChildByPath("y2"), 0)),
                            Integer.parseInt(footHold.getName()));
                    fh.setPrev((short) MapleDataTool.getInt(footHold.getChildByPath("prev"), 0));
                    fh.setNext((short) MapleDataTool.getInt(footHold.getChildByPath("next"), 0));

                    if (fh.getX1() < lBound.x) {
                        lBound.x = fh.getX1();
                    }
                    if (fh.getX2() > uBound.x) {
                        uBound.x = fh.getX2();
                    }
                    if (fh.getY1() < lBound.y) {
                        lBound.y = fh.getY1();
                    }
                    if (fh.getY2() > uBound.y) {
                        uBound.y = fh.getY2();
                    }
                    allFootholds.add(fh);
                }
            }
        }
        MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
        int left = 99999;
        int right = -99999;
        for (MapleFoothold foothold : allFootholds) {
            if (foothold.getX1() != foothold.getX2()) {
                if (left > foothold.getX1()) {
                    left = foothold.getX1();
                }
                if (left > foothold.getX2()) {
                    left = foothold.getX2();
                }
                if (right < foothold.getX1()) {
                    right = foothold.getX1();
                }
                if (right < foothold.getX2()) {
                    right = foothold.getX2();
                }
            }
            fTree.insert(foothold);
        }
        map.MapleFootholds = fTree;
        allFootholds.clear();
        if (map.getTop() == 0) {
            map.setTop(lBound.y - 500);
        }
        if (map.getBottom() == 0) {
            map.setBottom(uBound.y + 50);
        }
        if (map.getLeft() == 0) {
            map.setLeft(lBound.x + 50);
        }
        if (map.getRight() == 0) {
            map.setRight(uBound.x - 50);
        }

        map.leftLimit = left + 50;
        map.rightLimit = right - 50;

        int bounds[] = new int[4];
        bounds[0] = MapleDataTool.getInt(mapData.getChildByPath("VRTop"));
        bounds[1] = MapleDataTool.getInt(mapData.getChildByPath("VRBottom"));
        if (bounds[0] == bounds[1]) {    // old-style baked map
            MapleData minimapData = mapData.getChildByPath("miniMap");
            if (minimapData != null) {
                bounds[0] = MapleDataTool.getInt(minimapData.getChildByPath("centerX")) * -1;
                bounds[1] = MapleDataTool.getInt(minimapData.getChildByPath("centerY")) * -1;
                bounds[2] = MapleDataTool.getInt(minimapData.getChildByPath("height"));
                bounds[3] = MapleDataTool.getInt(minimapData.getChildByPath("width"));
                map.setMapPointBoundings(bounds[0], bounds[1], bounds[2], bounds[3]);
            } else {
                int dist = (1 << 18);
                map.setMapPointBoundings(-dist / 2, -dist / 2, dist, dist);
            }
        } else {
            bounds[2] = MapleDataTool.getInt(mapData.getChildByPath("VRLeft"));
            bounds[3] = MapleDataTool.getInt(mapData.getChildByPath("VRRight"));

            map.setMapLineBoundings(bounds[0], bounds[1], bounds[2], bounds[3]);
        }

        int bossid = -1;
        String msg = null;
        if (mapData.getChildByPath("info/timeMob") != null) {
            bossid = MapleDataTool.getInt(mapData.getChildByPath("info/timeMob/id"), 0);
            msg = MapleDataTool.getString(mapData.getChildByPath("info/timeMob/message"), null);
        }
        // load life data (npc, monsters)
        List<Point> herbRocks = new ArrayList<Point>();
        int lowestLevel = 200, highestLevel = 0;
        String type, limited;
        Collection<SpawnPoint> spawns = Collections.synchronizedList(new LinkedList<>());
        for (MapleData life : mapData.getChildByPath("life")) {
            type = MapleDataTool.getString(life.getChildByPath("type"));
            limited = MapleDataTool.getString("limitedname", life, "");
            if ((npcs || !type.equals("n")) && !limited.equals("Stage0")) { //alien pq stuff
                String Lid = MapleDataTool.getString(life.getChildByPath("id"));
                if (!Lid.isEmpty()) {
                    AbstractLoadedMapleLife myLife = loadLife(life, map, MapleDataTool.getString(life.getChildByPath("id")), type);
                    if (myLife != null) {
                        if (myLife instanceof MapleMonster) {
                            MapleMonsterStats stats = MapleLifeFactory.getBasicStats(myLife.getId());
                            if (stats != null) {
                                SpawnPoint sp = new SpawnPoint(life, stats.getMobile(), stats.isBoss(), stats.getScale(), stats.getLevel(), stats.getMaxCount());
                                map.SpawnPoints.add(sp);
                                map.maxSpawn += sp.spawnLimit();
                            } else {
                                System.out.println("Error with Map id: " + mapid + " - missing mobinfo for spawnpoint");
                            }
                        }
                        if (myLife instanceof MapleNPC) {
                            //map.spawnNpc(myLife.getId(), myLife.getPosition(), myLife.getF() > 0);
                            map.addMapObject(myLife);
                            map.updateObject(myLife);
                        }
                    } else {
                        System.out.println("Error with map id: " + mapid + " Error with type: " + type + " - ID:" + Lid);
                    }
                } else {
                    System.out.println("Error with id: " + Lid);
                }
            }
        }
        //addAreaBossSpawn(map);
        map.setCreateMobInterval((short) MapleDataTool.getInt(mapData.getChildByPath("info/createMobInterval"), 9000));
        map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
        map.setPartyBonusRate(GameConstants.getPartyPlay(mapid, MapleDataTool.getInt(mapData.getChildByPath("info/partyBonusR"), 0)));
        //map.loadMonsterRate(true);
        map.setNodes(loadNodes(mapid, mapData));

        //load reactor data
        String id;
        if (reactors && mapData.getChildByPath("reactor") != null) {
            for (MapleData reactor : mapData.getChildByPath("reactor")) {
                id = MapleDataTool.getString(reactor.getChildByPath("id"));
                if (id != null) {
                    map.spawnReactor(loadReactor(reactor, id, (byte) MapleDataTool.getInt(reactor.getChildByPath("f"), 0)));
                }
            }
        }
        map.setFirstUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onFirstUserEnter"), ""));
        map.setUserEnter(mapid == GameConstants.JAIL ? "jail" : MapleDataTool.getString(mapData.getChildByPath("info/onUserEnter"), ""));
        if (reactors && herbRocks.size() > 0 && highestLevel >= 30 && map.getFirstUserEnter().equals("") && map.getUserEnter().equals("")) {
            final List<Integer> allowedSpawn = new ArrayList<Integer>(24);
            allowedSpawn.add(100011);
            allowedSpawn.add(200011);
            if (highestLevel >= 100) {
                for (int i = 0; i < 10; i++) {
                    for (int x = 0; x < 4; x++) { //to make heartstones rare
                        allowedSpawn.add(100000 + i);
                        allowedSpawn.add(200000 + i);
                    }
                }
            } else {
                for (int i = (lowestLevel % 10 > highestLevel % 10 ? 0 : (lowestLevel % 10)); i < (highestLevel % 10); i++) {
                    for (int x = 0; x < 4; x++) { //to make heartstones rare
                        allowedSpawn.add(100000 + i);
                        allowedSpawn.add(200000 + i);
                    }
                }
            }
            final int numSpawn = Randomizer.nextInt(allowedSpawn.size()) / 6; //0-7
            for (int i = 0; i < numSpawn && !herbRocks.isEmpty(); i++) {
                final int idd = allowedSpawn.get(Randomizer.nextInt(allowedSpawn.size()));
                final int theSpawn = Randomizer.nextInt(herbRocks.size());
                final MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(idd), idd);
                myReactor.setPosition(herbRocks.get(theSpawn));
                myReactor.setDelay(idd % 100 == 11 ? 60000 : 5000); //in the reactor's wz
                map.spawnReactor(myReactor);
                herbRocks.remove(theSpawn);
            }
        }

        try {
            map.setMapName(mapsName.get(omapid).getLeft());
            map.setStreetName(mapsName.get(omapid).getRight());
        } catch (Exception e) {
            map.setMapName("");
            map.setStreetName("");
        }
        spawnNPC(map);
        map.setBGM(MapleDataTool.getString(mapData.getChildByPath("info/bgm")));
        map.setClock(mapData.getChildByPath("clock") != null); //clock was changed in wz to have x,y,width,height
        map.setEverlast(MapleDataTool.getInt(mapData.getChildByPath("info/everlast"), 0) > 0);
        map.setTown(MapleDataTool.getInt(mapData.getChildByPath("info/town"), 0) > 0);
        map.setSoaring(MapleDataTool.getInt(mapData.getChildByPath("info/needSkillForFly"), 0) > 0);
        map.setPersonalShop(MapleDataTool.getInt(mapData.getChildByPath("info/personalShop"), 0) > 0);
        map.setForceMove(MapleDataTool.getInt(mapData.getChildByPath("info/lvForceMove"), 0));
        map.setHPDec(MapleDataTool.getInt(mapData.getChildByPath("info/decHP"), 0));
        map.setHPDecInterval(MapleDataTool.getInt(mapData.getChildByPath("info/decHPInterval"), 10000));
        map.setHPDecProtect(MapleDataTool.getInt(mapData.getChildByPath("info/protectItem"), 0));
        map.setForcedReturnMap(mapid == 0 ? 999999999 : MapleDataTool.getInt(mapData.getChildByPath("info/forcedReturn"), 999999999));
        map.setTimeLimit(MapleDataTool.getInt(mapData.getChildByPath("info/timeLimit"), -1));
        map.setFieldLimit(MapleDataTool.getInt(mapData.getChildByPath("info/fieldLimit"), 0));
        map.setRecoveryRate(MapleDataTool.getFloat(mapData.getChildByPath("info/recovery"), 1));
        map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
        map.setPartyBonusRate(GameConstants.getPartyPlay(mapid, MapleDataTool.getInt(mapData.getChildByPath("info/partyBonusR"), 0)));
        map.setConsumeItemCoolTime(MapleDataTool.getInt(mapData.getChildByPath("info/consumeItemCoolTime"), 0));
        map.pal_level = map.getAvgPalLevel();
        HashMap<Integer, Integer> backTypes = new HashMap<>();
        try {
            for (MapleData layer : mapData.getChildByPath("back")) { // yolo
                int layerNum = Integer.parseInt(layer.getName());
                int btype = MapleDataTool.getInt(layer.getChildByPath("type"), 0);

                backTypes.put(layerNum, btype);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // swallow cause I'm cool
        }
        map.generateMapDropRangeCache();
        map.loading = false;
        return map;
    }

    private static AbstractLoadedMapleLife loadLife(MapleData life, MapleMap map, String id, String type) {
        AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type);
        if (myLife == null) {
            return null;
        }
        Point base = new Point(MapleDataTool.getInt(life.getChildByPath("x")), MapleDataTool.getInt(life.getChildByPath("y")) - 2);
        myLife.setPosition(map.calcPointBelow(base));
        if (type.equals("n")) {
            myLife.setCy(base.y - 2);
            myLife.setRx0(base.x + 5);
            myLife.setRx1(base.x - 5);
            myLife.setFh(map.getFH(base));
            MapleData dF = life.getChildByPath("f");
            if (dF != null) {
                myLife.setF(MapleDataTool.getInt(dF));
            }
            myLife.setHide(false);
        } else {
            myLife.setCy(MapleDataTool.getInt(life.getChildByPath("cy")));
            MapleData dF = life.getChildByPath("f");
            if (dF != null) {
                myLife.setF(MapleDataTool.getInt(dF));
            }
            myLife.setFh(MapleDataTool.getInt(life.getChildByPath("fh")));
            myLife.setRx0(MapleDataTool.getInt(life.getChildByPath("rx0")));
            myLife.setRx1(MapleDataTool.getInt(life.getChildByPath("rx1")));
            myLife.setHide(false);

            /*
         if (MapleDataTool.getInt("hide", life, 0) == 1 && myLife instanceof MapleNPC) {
         myLife.setHide(true);
         //		} else if (hide > 1) {
         //			System.err.println("Hide > 1 ("+ hide +")");
         }
             */
        }
        return myLife;
    }

    public static void spawnNPC(MapleMap map) {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `npc_spawns` WHERE `mapid` = ?")) {
                ps.setInt(1, map.getId());
                try (ResultSet rs = ps.executeQuery();) {
                    while (rs.next()) {
                        map.spawnNpc(rs.getInt("id"), map.calcPointBelow(new Point(rs.getInt("x"), rs.getInt("y") - 2)), rs.getInt("flip") != 0);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static final MapleReactor loadReactor(final MapleData reactor, final String id, final byte FacingDirection) {
        final MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(id)), Integer.parseInt(id));

        myReactor.setFacingDirection(FacingDirection);
        myReactor.setPosition(new Point(MapleDataTool.getInt(reactor.getChildByPath("x")), MapleDataTool.getInt(reactor.getChildByPath("y"))));
        myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
        myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));

        return myReactor;
    }

    public static boolean isRealMap(int mapid) {
        if (mapsName == null || mapsName.get(mapid) == null || mapsName.get(mapid).getLeft() == null || mapsName.get(mapid).getRight() == null) {
            return false;
        }
        return true;
    }

    public static String getRealMapName(int mapid) {
        return "#r" + mapsName.get(mapid).getRight() + "#k:#b" + mapsName.get(mapid).getLeft() + "#k";
    }

    public static String getMapName(int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        StringBuilder builder = new StringBuilder("Map/Map");
        builder.append(mapid / 100000000);
        builder.append("/");
        builder.append(mapName);
        builder.append(".img");

        mapName = builder.toString();
        return mapName;
    }

    public static void loadMapNames() {
        MapleData root = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Map.img");
        for (MapleData child : root) {
            for (MapleData map : child) {
                int mapid = Integer.parseInt(map.getName());
                String mapName = MapleDataTool.getString("mapName", map, null);
                String streetName = MapleDataTool.getString("streetName", map, null);
                mapsName.put(mapid, new Pair<>(mapName, streetName));
            }
        }
    }

    private static String getMapStringName(int mapid) {
        StringBuilder builder = new StringBuilder();
        if (mapid < 100000000) {
            builder.append("maple");
        } else if ((mapid >= 100000000 && mapid < 200000000) || mapid / 100000 == 5540) {
            builder.append("victoria");
        } else if (mapid >= 200000000 && mapid < 300000000) {
            builder.append("ossyria");
        } else if (mapid >= 300000000 && mapid < 400000000) {
            builder.append("3rd");
        } else if (mapid >= 500000000 && mapid < 510000000) {
            builder.append(GameConstants.GMS ? "thai" : "TH");
        } else if (mapid >= 555000000 && mapid < 556000000) {
            builder.append("SG");
        } else if (mapid >= 540000000 && mapid < 600000000) {
            builder.append(GameConstants.GMS ? "singapore" : "SG");
        } else if (mapid >= 682000000 && mapid < 683000000) {
            builder.append(GameConstants.GMS ? "HalloweenGL" : "GL");
        } else if (mapid >= 600000000 && mapid < 670000000) {
            builder.append(GameConstants.GMS ? "MasteriaGL" : "GL");
        } else if (mapid >= 677000000 && mapid < 678000000) {
            builder.append(GameConstants.GMS ? "Episode1GL" : "GL");
        } else if (mapid >= 670000000 && mapid < 682000000) {
            builder.append(GameConstants.GMS ? "weddingGL" : "GL");
        } else if (mapid >= 687000000 && mapid < 688000000) {
            builder.append("Gacha_GL");
        } else if (mapid >= 689000000 && mapid < 690000000) {
            builder.append("CTF_GL");
        } else if (mapid >= 683000000 && mapid < 684000000) {
            builder.append("event");
        } else if (mapid >= 684000000 && mapid < 685000000) {
            builder.append("event_5th");
        } else if (mapid >= 700000000 && mapid < 700000300) {
            builder.append("wedding");
        } else if (mapid >= 701000000 && mapid < 701020000) {
            builder.append("china");
        } else if ((mapid >= 702090000 && mapid <= 702100000) || (mapid >= 740000000 && mapid < 741000000)) {
            builder.append("TW");
        } else if (mapid >= 702000000 && mapid < 742000000) {
            builder.append("CN");
        } else if (mapid >= 800000000 && mapid < 900000000) {
            builder.append(GameConstants.GMS ? "jp" : "JP");
        } else {
            builder.append("etc");
        }
        builder.append("/");
        builder.append(mapid);

        return builder.toString();
    }

    private static void loadPortals(MapleMap map, MapleData port) {
        if (port == null) {
            return;
        }
        int nextDoorPortal = 0x80;
        for (MapleData portal : port.getChildren()) {
            MaplePortal myPortal = new MaplePortal(MapleDataTool.getInt(portal.getChildByPath("pt")));
            myPortal.setName(MapleDataTool.getString(portal.getChildByPath("pn")));
            myPortal.setTarget(MapleDataTool.getString(portal.getChildByPath("tn")));
            myPortal.setTargetMapId(MapleDataTool.getInt(portal.getChildByPath("tm")));
            myPortal.setPosition(new Point(MapleDataTool.getInt(portal.getChildByPath("x")), MapleDataTool.getInt(portal.getChildByPath("y"))));
            String script = MapleDataTool.getString("script", portal, null);
            if (script != null && script.equals("")) {
                script = null;
            }
            myPortal.setScriptName(script);

            if (myPortal.getType() == MaplePortal.DOOR_PORTAL) {
                myPortal.setId(nextDoorPortal);
                nextDoorPortal++;
            } else {
                myPortal.setId(Integer.parseInt(portal.getName()));
            }
            map.addPortal(myPortal);
        }
    }

    private static MapleNodes loadNodes(final int mapid, final MapleData mapData) {
        MapleNodes nodeInfo = new MapleNodes(mapid);
        if (mapData.getChildByPath("nodeInfo") != null) {
            for (MapleData node : mapData.getChildByPath("nodeInfo")) {
                try {
                    if (node.getName().equals("start")) {
                        nodeInfo.setNodeStart(MapleDataTool.getInt(node, 0));
                        continue;
                    }
                    List<Integer> edges = new ArrayList<Integer>();
                    if (node.getChildByPath("edge") != null) {
                        for (MapleData edge : node.getChildByPath("edge")) {
                            edges.add(MapleDataTool.getInt(edge, -1));
                        }
                    }
                    final MapleNodeInfo mni = new MapleNodeInfo(
                            Integer.parseInt(node.getName()),
                            MapleDataTool.getIntConvert("key", node, 0),
                            MapleDataTool.getIntConvert("x", node, 0),
                            MapleDataTool.getIntConvert("y", node, 0),
                            MapleDataTool.getIntConvert("attr", node, 0), edges);
                    nodeInfo.addNode(mni);
                } catch (NumberFormatException e) {
                } //start, end, edgeInfo = we dont need it
            }
            nodeInfo.sortNodes();
        }
        for (int i = 1; i <= 7; i++) {
            if (mapData.getChildByPath(String.valueOf(i)) != null && mapData.getChildByPath(i + "/obj") != null) {
                for (MapleData node : mapData.getChildByPath(i + "/obj")) {
                    if (node.getChildByPath("SN_count") != null && node.getChildByPath("speed") != null) {
                        int sn_count = MapleDataTool.getIntConvert("SN_count", node, 0);
                        String name = MapleDataTool.getString("name", node, "");
                        int speed = MapleDataTool.getIntConvert("speed", node, 0);
                        if (sn_count <= 0 || speed <= 0 || name.equals("")) {
                            continue;
                        }
                        final List<Integer> SN = new ArrayList<Integer>();
                        for (int x = 0; x < sn_count; x++) {
                            SN.add(MapleDataTool.getIntConvert("SN" + x, node, 0));
                        }
                        final MaplePlatform mni = new MaplePlatform(
                                name, MapleDataTool.getIntConvert("start", node, 2), speed,
                                MapleDataTool.getIntConvert("x1", node, 0),
                                MapleDataTool.getIntConvert("y1", node, 0),
                                MapleDataTool.getIntConvert("x2", node, 0),
                                MapleDataTool.getIntConvert("y2", node, 0),
                                MapleDataTool.getIntConvert("r", node, 0), SN);
                        nodeInfo.addPlatform(mni);
                    } else if (node.getChildByPath("tags") != null) {
                        String name = MapleDataTool.getString("tags", node, "");
                        nodeInfo.addFlag(new Pair<String, Integer>(name, name.endsWith("3") ? 1 : 0)); //idk, no indication in wz
                    }
                }
            }
        }
        // load areas (EG PQ platforms)
        if (mapData.getChildByPath("area") != null) {
            int x1, y1, x2, y2;
            Rectangle mapArea;
            for (MapleData area : mapData.getChildByPath("area")) {
                x1 = MapleDataTool.getInt(area.getChildByPath("x1"));
                y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
                x2 = MapleDataTool.getInt(area.getChildByPath("x2"));
                y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
                mapArea = new Rectangle(x1, y1, (x2 - x1), (y2 - y1));
                nodeInfo.addMapleArea(mapArea);
            }
        }
        if (mapData.getChildByPath("CaptureTheFlag") != null) {
            final MapleData mc = mapData.getChildByPath("CaptureTheFlag");
            for (MapleData area : mc) {
                nodeInfo.addGuardianSpawn(new Point(MapleDataTool.getInt(area.getChildByPath("FlagPositionX")), MapleDataTool.getInt(area.getChildByPath("FlagPositionY"))), area.getName().startsWith("Red") ? 0 : 1);
            }
        }
        if (mapData.getChildByPath("directionInfo") != null) {
            final MapleData mc = mapData.getChildByPath("directionInfo");
            for (MapleData area : mc) {
                DirectionInfo di = new DirectionInfo(Integer.parseInt(area.getName()), MapleDataTool.getInt("x", area, 0), MapleDataTool.getInt("y", area, 0), MapleDataTool.getInt("forcedInput", area, 0) > 0);
                final MapleData mc2 = area.getChildByPath("eventQ");
                if (mc2 != null) {
                    for (MapleData event : mc2) {
                        di.eventQ.add(MapleDataTool.getString(event));
                    }
                }
                nodeInfo.addDirection(Integer.parseInt(area.getName()), di);
            }
        }
        if (mapData.getChildByPath("monsterCarnival") != null) {
            final MapleData mc = mapData.getChildByPath("monsterCarnival");
            if (mc.getChildByPath("mobGenPos") != null) {
                for (MapleData area : mc.getChildByPath("mobGenPos")) {
                    nodeInfo.addMonsterPoint(MapleDataTool.getInt(area.getChildByPath("x")),
                            MapleDataTool.getInt(area.getChildByPath("y")),
                            MapleDataTool.getInt(area.getChildByPath("fh")),
                            MapleDataTool.getInt(area.getChildByPath("cy")),
                            MapleDataTool.getInt("team", area, -1));
                }
            }
            if (mc.getChildByPath("mob") != null) {
                for (MapleData area : mc.getChildByPath("mob")) {
                    nodeInfo.addMobSpawn(MapleDataTool.getInt(area.getChildByPath("id")), MapleDataTool.getInt(area.getChildByPath("spendCP")));
                }
            }
            if (mc.getChildByPath("guardianGenPos") != null) {
                for (MapleData area : mc.getChildByPath("guardianGenPos")) {
                    nodeInfo.addGuardianSpawn(new Point(MapleDataTool.getInt(area.getChildByPath("x")), MapleDataTool.getInt(area.getChildByPath("y"))), MapleDataTool.getInt("team", area, -1));
                }
            }
            if (mc.getChildByPath("skill") != null) {
                for (MapleData area : mc.getChildByPath("skill")) {
                    nodeInfo.addSkillId(MapleDataTool.getInt(area));
                }
            }
        }
        return nodeInfo;
    }
}
