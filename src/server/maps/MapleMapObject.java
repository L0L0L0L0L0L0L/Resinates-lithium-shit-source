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

import client.MapleClient;
import constants.GameConstants;
import java.awt.Point;

public abstract class MapleMapObject {

    private Point position = new Point();
    private int objectId;

    public Point getPosition() {
        return new Point(position);
    }

    public Point getTruePosition() {
        return this.position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setPosition(short x, short y) {
        this.position.x = x;
        this.position.y = y;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int id) {
        this.objectId = id;
    }

    public int getRange() {
        return (int) GameConstants.VIEW_RANGE;
    }

    public abstract MapleMapObjectType getType();

    public abstract void sendSpawnData(final MapleClient client);

    public abstract void sendDestroyData(final MapleClient client);
}
