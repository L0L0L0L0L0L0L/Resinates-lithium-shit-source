/*
 This file is part of the HeavenMS MapleStory Server
 Copyleft (L) 2016 - 2018 RonanLana

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import scripting.EventInstanceManager;

public class MapleMapManager {

    private int channel, world;
    private EventInstanceManager event;
    private Map<Integer, MapleMap> maps = new ConcurrentHashMap<Integer, MapleMap>();
    private ScheduledFuture<?> updateTask;
    private ReadLock mapsRLock;
    private WriteLock mapsWLock;

    public MapleMapManager(int channel) {
        ReentrantReadWriteLock rrwl = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_MANAGER);
        this.channel = channel;
        this.event = null;
        this.mapsRLock = rrwl.readLock();
        this.mapsWLock = rrwl.writeLock();
    }

    public MapleMapManager(EventInstanceManager eim, int channel) {
        ReentrantReadWriteLock rrwl = new MonitoredReentrantReadWriteLock(MonitoredLockType.MAP_MANAGER);
        this.channel = channel;
        this.event = eim;
        this.mapsRLock = rrwl.readLock();
        this.mapsWLock = rrwl.writeLock();
    }

    public MapleMap resetMap(int mapid) {
        if (maps.containsKey(mapid)) {
            mapsWLock.lock();
            try {
                maps.remove(mapid);
            } finally {
                mapsWLock.unlock();
            }
        }

        return getMap(mapid);
    }

    public void loadAllMaps() {
        final MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz/Map"));
        final MapleDataDirectoryEntry root = datasource.getRoot();
        int mapid;
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) { // Loop thru jobs
            if (!topDir.getName().startsWith("Map")) {
                continue;
            }
            for (MapleDataFileEntry data : topDir.getFiles()) { // Loop thru each jobs
                String name = data.getName().replace(".xml", "");
                name = data.getName().replace(".img", "");
                int id = Integer.parseInt(name);
                loadMapFromWz(id, true);
                System.out.println("id: " + id);
            }

        }

        //System.out.println("img: " + img);
    }

    private synchronized MapleMap loadMapFromWz(int mapid, boolean cache) {
        MapleMap map;

        if (cache) {
            mapsRLock.lock();
            try {
                map = maps.get(mapid);
            } finally {
                mapsRLock.unlock();
            }

            if (map != null) {
                return map;
            }
        }

        map = MapleMapFactory.getMap(mapid, true, true, true, channel, event);
        if (map != null) {
            if (cache) {
                mapsWLock.lock();
                try {
                    maps.put(mapid, map);
                } finally {
                    mapsWLock.unlock();
                }
            }
        } else {
            System.out.println("Error with mapid: " + mapid);
            return null;
        }

        return map;
    }

    public MapleMap getMap(int mapid) {

        MapleMap map = null;

        mapsRLock.lock();
        try {
            map = maps.get(mapid);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mapsRLock.unlock();
        }
        if (map != null) {
            return map;
        } else {
            map = loadMapFromWz(mapid, true);
            if (map == null) {
                System.out.println("Map null - ID: " + mapid);
                return null;
            }
        }
        return map;
    }

    public MapleMap getDisposableMap(int mapid) {
        return loadMapFromWz(mapid, false);
    }

    public boolean isMapLoaded(int mapId) {
        mapsRLock.lock();
        try {
            return maps.containsKey(mapId);
        } finally {
            mapsRLock.unlock();
        }
    }

    public Map<Integer, MapleMap> getMaps() {
        mapsRLock.lock();
        try {
            return new HashMap<>(maps);
        } finally {
            mapsRLock.unlock();
        }
    }

    public Collection<MapleMap> getAllMaps() {
        mapsRLock.lock();
        try {
            return maps.values();
        } finally {
            mapsRLock.unlock();
        }

    }

    public void removeMap(MapleMap map) {
        if (map != null && maps.containsKey(map.getId())) {
            mapsWLock.lock();
            try {
                map.dispose();
                maps.remove(map.getId());
            } finally {
                mapsWLock.unlock();
            }
        }
    }

    public void dispose() {
        maps.clear();
        this.event = null;
    }

    public void forceDispose() {
        mapsWLock.lock();
        try {
            for (MapleMap map : maps.values()) {
                map.forceDispose();
            }
            maps.clear();
        } finally {
            mapsWLock.unlock();
        }
    }

    public void disposeMap(MapleMap map) {
        removeMap(map);
    }

}
