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
package handling.channel;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import client.MapleCharacterUtil;
import client.MapleCharacter;

import handling.world.CharacterTransfer;
import handling.world.CheaterData;
import handling.world.World;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.MonitoredReentrantReadWriteLock;
import server.Timer.PingTimer;

public class PlayerStorage {

    private final ReentrantReadWriteLock locks = new MonitoredReentrantReadWriteLock(MonitoredLockType.PLAYER_STORAGE, true);
    private final Map<String, MapleCharacter> nameToChar = new LinkedHashMap<String, MapleCharacter>();
    private final Map<Integer, MapleCharacter> idToChar = new LinkedHashMap<Integer, MapleCharacter>();
    private final Map<Integer, CharacterTransfer> PendingCharacter = new LinkedHashMap<Integer, CharacterTransfer>();
    private ReentrantReadWriteLock.ReadLock rlock = locks.readLock();
    private ReentrantReadWriteLock.WriteLock wlock = locks.writeLock();
    private int channel;

    public PlayerStorage(int channel) {
        this.channel = channel;
        // Prune once every 15 minutes
        //PingTimer.getInstance().register(new PersistingTask(), 60000);
    }

    public final ArrayList<MapleCharacter> getAllCharacters() {
        rlock.lock();
        try {
            return new ArrayList<MapleCharacter>(idToChar.values());
        } finally {
            rlock.unlock();
        }
    }

    public final void registerPlayer(final MapleCharacter chr) {
        wlock.lock();
        try {
            nameToChar.put(chr.getName().toLowerCase(), chr);
            idToChar.put(chr.getId(), chr);
        } finally {
            wlock.unlock();
        }
        World.Find.register(chr.getId(), chr.getName(), channel);
    }

    public final void registerPendingPlayer(final CharacterTransfer chr, final int playerid) {
        wlock.lock();
        try {
            PendingCharacter.put(playerid, chr);//new Pair(System.currentTimeMillis(), chr));
        } finally {
            wlock.unlock();
        }
    }

    public final void deregisterPlayer(final MapleCharacter chr) {
        wlock.lock();
        try {
            nameToChar.remove(chr.getName().toLowerCase());
            idToChar.remove(chr.getId());
        } finally {
            wlock.unlock();
        }
        World.Find.forceDeregister(chr.getId(), chr.getName());
    }

    public final void deregisterPlayer(final int idz, final String namez) {
        wlock.lock();
        try {
            nameToChar.remove(namez.toLowerCase());
            idToChar.remove(idz);
        } finally {
            wlock.unlock();
        }
        World.Find.forceDeregister(idz, namez);
    }

    public final int pendingCharacterSize() {
        return PendingCharacter.size();
    }

    public final void deregisterPendingPlayer(final int charid) {
        wlock.lock();
        try {
            PendingCharacter.remove(charid);
        } finally {
            wlock.unlock();
        }
    }

    public final CharacterTransfer getPendingCharacter(final int charid) {
        wlock.lock();
        try {
            return PendingCharacter.remove(charid);
        } finally {
            wlock.unlock();
        }
    }

    public final MapleCharacter getCharacterByName(final String name) {
        rlock.lock();
        try {
            return nameToChar.get(name.toLowerCase());
        } finally {
            rlock.unlock();
        }
    }

    public final MapleCharacter getCharacterById(final int id) {
        rlock.lock();
        try {
            return idToChar.get(id);
        } finally {
            rlock.unlock();
        }
    }

    public final int getConnectedClients() {
        return idToChar.size();
    }

    public final void disconnectAll() {
        disconnectAll(false);
    }

    public final void disconnectAll(final boolean checkGM) {
        wlock.lock();
        try {
            final Iterator<MapleCharacter> itr = getAllCharacters().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();
                if (!chr.isGM() || !checkGM) {
                    chr.getClient().disconnect(false, false, true);
                    chr.getClient().getSession().close();
                    World.Find.forceDeregister(chr.getId(), chr.getName());
                    itr.remove();
                }
            }
        } finally {
            wlock.unlock();
        }
    }

    public final void dcAll() {
        wlock.lock();
        try {
            for (MapleCharacter chr : getAllCharacters()) {
                if (chr != null && !chr.isGM()) {
                    try {
                        //chr.getClient().disconnect(true, false, true);
                        //chr.saveCharToDB();
                        chr.getClient().getSession().close();
                        //World.Find.forceDeregister(chr.getId(), chr.getName());
                        deregisterPlayer(chr);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            System.out.println("Saving Players Complete");
        } finally {
            wlock.unlock();
        }
    }

    public final String getOnlinePlayers(final boolean byGM) {
        final StringBuilder sb = new StringBuilder();

        if (byGM) {
            rlock.lock();
            try {
                final Iterator<MapleCharacter> itr = getAllCharacters().iterator();
                while (itr.hasNext()) {
                    sb.append(MapleCharacterUtil.makeMapleReadable(itr.next().getName()));
                    sb.append(", ");
                }
            } finally {
                rlock.unlock();
            }
        } else {
            rlock.lock();
            try {
                final Iterator<MapleCharacter> itr = getAllCharacters().iterator();
                MapleCharacter chr;
                while (itr.hasNext()) {
                    chr = itr.next();

                    if (!chr.isGM()) {
                        sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                        sb.append(", ");
                    }
                }
            } finally {
                rlock.unlock();
            }
        }
        return sb.toString();
    }

    public final void broadcastPacket(final byte[] data) {
        rlock.lock();
        try {
            final Iterator<MapleCharacter> itr = getAllCharacters().iterator();
            while (itr.hasNext()) {
                itr.next().getClient().announce(data);
            }
        } finally {
            rlock.unlock();
        }
    }

    public final void broadcastSmegaPacket(final byte[] data) {
        rlock.lock();
        try {
            final Iterator<MapleCharacter> itr = getAllCharacters().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (chr.getClient().isLoggedIn() && chr.getSmega()) {
                    chr.getClient().announce(data);
                }
            }
        } finally {
            rlock.unlock();
        }
    }

    public final void broadcastGMPacket(final byte[] data) {
        rlock.lock();
        try {
            final Iterator<MapleCharacter> itr = getAllCharacters().iterator();
            MapleCharacter chr;
            while (itr.hasNext()) {
                chr = itr.next();

                if (chr.getClient().isLoggedIn() && chr.isIntern()) {
                    chr.getClient().announce(data);
                }
            }
        } finally {
            rlock.unlock();
        }
    }

    public class PersistingTask implements Runnable {

        @Override
        public void run() {
            wlock.lock();
            try {
                final long currenttime = System.currentTimeMillis();
                final Iterator<Map.Entry<Integer, CharacterTransfer>> itr = PendingCharacter.entrySet().iterator();

                while (itr.hasNext()) {
                    if (currenttime - itr.next().getValue().TranferTime > 40000) { // 40 sec
                        itr.remove();
                    }
                }
            } finally {
                wlock.unlock();
            }
        }
    }
}
