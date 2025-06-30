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

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import provider.MapleData;
import provider.MapleDataTool;
import server.maps.MapleMap;

public class SpawnPoint {

    private final Point pos;
    private long nextPossibleSpawn;
    private int mobTime, carnival = -1, fh, f, id, level = -1, scale, count, rx0, rx1, cy, maxcount = 8;
    private final AtomicInteger spawnedMonsters = new AtomicInteger(0);
    private String msg;
    private byte carnivalTeam = 0;
    private boolean mobile, denySpawn = false, boss = false, spawned = false;

    public SpawnPoint(final MapleData life, boolean mobile, boolean boss, int scale, int level, int maxcount) {
        this.pos = new Point(MapleDataTool.getInt(life.getChildByPath("x")), MapleDataTool.getInt(life.getChildByPath("y")));
        this.id = Integer.parseInt(MapleDataTool.getString(life.getChildByPath("id")));
        this.fh = MapleDataTool.getInt(life.getChildByPath("fh"));
        this.f = MapleDataTool.getInt(life.getChildByPath("f"));
        this.rx0 = MapleDataTool.getInt(life.getChildByPath("rx0"));
        this.rx1 = MapleDataTool.getInt(life.getChildByPath("rx1"));
        this.cy = MapleDataTool.getInt(life.getChildByPath("cy"));
        this.mobTime = MapleDataTool.getInt(life.getChildByPath("mobTime"));
        this.mobile = mobile;
        this.boss = boss;
        this.scale = scale;
        this.level = level;
        this.maxcount = spawnMaxLimit();
    }

    public int getId() {
        return id;
    }

    public int getCy() {
        return cy;
    }

    public int getRx0() {
        return rx0;
    }

    public int getRx1() {
        return rx1;
    }

    public final void setCarnival(int c) {
        this.carnival = c;
    }

    public final void setLevel(int c) {
        this.level = c;
    }

    public final int getF() {
        return f;
    }

    public final int getScale() {
        return scale;
    }

    public final int getLevel() {
        return level;
    }

    public final int getFh() {
        return fh;
    }

    public final Point getPosition() {
        return pos;
    }

    public final byte getCarnivalTeam() {
        return carnivalTeam;
    }

    public final int getCarnivalId() {
        return carnival;
    }

    public final int getMobId() {
        return id;
    }

    public boolean isMobile() {
        return mobile;
    }

    public void mobTime(int value) {
        mobTime = value;
    }

    public final boolean shouldSpawn(long time) {
        if (mobTime < 0) {
            return false;
        }
        // regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or spawnpoints with mobtime a
        // maximum of 1
        if ((!mobile && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 1) {
            return false;
        }
        return nextPossibleSpawn <= time;
    }

    public final int getMobTime() {
        return mobTime;
    }

    public boolean shouldSpawn() {
        if (!mobile && spawnedMonsters.get() > 0) {
            return false;
        }
        if (boss && spawnedMonsters.get() > 0) {
            return false;
        }
        if (denySpawn || mobTime < 0) {
            return false;
        }
        if (spawnedMonsters.get() > spawnMaxLimit()) {
            return false;
        }
        return true;
    }

    public int spawnLimit() {
        if (mobTime < -100) {
            return 0;
        }
        if (!mobile || mobTime < 0 || boss) {
            return 1;
        }
        return maxcount;
    }

    public int spawnMaxLimit() {
        if (mobTime < -100) {
            return 0;
        }
        if (!mobile || mobTime < 0 || boss) {
            return 1;
        }
        return 4;
    }

    public void addCount(int value) {
        count += value;
    }

    public boolean shouldSpawnMission() {
        if (!mobile && spawnedMonsters.get() > 0) {
            return false;
        }
        if (count >= 4) {
            return false;
        }
        if (boss && spawnedMonsters.get() > 0) {
            return false;
        }
        if (spawnedMonsters.get() > 2) {
            return false;
        }
        return true;

    }

    public int getMonsterId() {
        return id;
    }

    public MapleMonster getMonster() {
        MapleMonster mob = MapleLifeFactory.getMonster(id);
        mob.setPosition(new Point(pos));
        mob.setFh(fh);
        mob.setF(f);
        spawnedMonsters.incrementAndGet();

        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled(int aniTime) {
                nextPossibleSpawn = System.currentTimeMillis() + aniTime;

                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        return mob;
    }

    public MapleMonster getMonster(MapleMonster mob) {
        mob.setPosition(new Point(pos));
        mob.setFh(fh);
        mob.setF(f);
        spawnedMonsters.incrementAndGet();

        mob.addListener(new MonsterListener() {

            @Override
            public void monsterKilled(int aniTime) {
                nextPossibleSpawn = System.currentTimeMillis() + aniTime;

                if (mobTime > 0) {
                    nextPossibleSpawn += mobTime;
                }
                spawnedMonsters.decrementAndGet();
            }
        });
        return mob;
    }

    public void spawnMonster(MapleMonster mob, MapleMap map, Point pos) {
        spawnMonster(mob, map, pos, false);
    }

    public void spawnMonster(MapleMonster mob, MapleMap map, Point pos, boolean forced) {
        Point npos = map.calcPointBelow(pos);
        mob.setMap(map);
        mob.setPosition(npos);
        mob.setFHMapData(map, npos);
        map.spawnMonsterOnGroundBelowSP(mob, npos);
        spawnedMonsters.incrementAndGet();
        mob.addListener(new MonsterListener() {
            @Override
            public void monsterKilled(int aniTime) {
                spawnedMonsters.decrementAndGet();
            }
        });
    }
}
