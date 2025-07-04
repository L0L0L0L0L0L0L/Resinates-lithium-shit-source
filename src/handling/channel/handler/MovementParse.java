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

import client.MapleCharacter;
import constants.GameConstants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import server.maps.AnimatedMapleMapObject;
import server.movement.*;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.data.LittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;

public class MovementParse {

    //1 = player, 2 = mob, 3 = pet, 4 = summon, 5 = dragon
    protected static List<LifeMovementFragment> parseMovement(final MapleCharacter chr, final SeekableLittleEndianAccessor lea, final int kind) {
        final List<LifeMovementFragment> res = new ArrayList<>();
        final byte numCommands = lea.readByte();
        for (byte i = 0; i < numCommands; i++) {
            final byte command = lea.readByte();
            switch (command) {
                case 0:
                case 7:
                case 14:
                case 16:
                case 44:
                case 45:
                case 46: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    short fh = 0, xoffset = 0, yoffset = 0;
                    if (command == 14) {
                        fh = lea.readShort();
                    }
                    if (command != 44) {
                        xoffset = lea.readShort();
                        yoffset = lea.readShort();
                    }
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setFh(fh);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    alm.setOffset(new Point(xoffset, yoffset));

                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 15:
                case 18:
                case 19:
                case 21:
                case 40:
                case 41:
                case 42:
                case 43: {
                    final short xmod = lea.readShort();
                    final short ymod = lea.readShort();
                    short unk = 0;
                    if (command == 18 || command == 19) {
                        unk = lea.readShort();
                    }
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);
                    rlm.setUnk(unk);
                    res.add(rlm);
                    break;
                }
                case 17: // special?...final charge aran
                case 22: // idk
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39: {
                    final byte newstate = lea.readByte();
                    final short unk = lea.readShort();

                    final GroundMovement am = new GroundMovement(command, new Point(0, 0), unk, newstate);

                    res.add(am);
                    break;
                }
                case 3:
                case 4:
                case 5:
                case 6:
                case 8:
                case 9:
                case 10:
                case 12:
                case 13: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short fh = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), duration, newstate);
                    tm.setFh(fh);

                    res.add(tm);
                    break;
                }
                case 20: {
                    final short xpos = lea.readShort();
                    final short ypos = lea.readShort();
                    final short xoffset = lea.readShort();
                    final short yoffset = lea.readShort();
                    final byte newstate = lea.readByte();
                    final short duration = lea.readShort();

                    final BounceMovement bm = new BounceMovement(command, new Point(xpos, ypos), duration, newstate);
                    bm.setOffset(new Point(xoffset, yoffset));

                    res.add(bm);
                    break;
                }
                case 11: { // Update Equip or Dash
                    res.add(new ChangeEquipSpecialAwesome(command, lea.readByte()));
                    break;
                }
                default:
                    chr.getClient().announce(HexTool.getByteArrayFromHexString("1A 00")); //give_buff with no data :D
                    System.out.println("Error with movement from player: " + chr.getName());
                    //return null;
            }
        }
        if (numCommands != res.size()) {
            System.out.println("Commands: " + numCommands + " - Res: " + res.size());
            System.out.println("Wrong Mob Move detected from player: " + chr.getName());
            return null;
        }
        if (res.isEmpty()) {
            //chr.getClient().announce(HexTool.getByteArrayFromHexString("1A 00")); //give_buff with no data :D
            System.out.println("Mob hack detected from player: " + chr.getName());
            return null; // Probably hack
        }
        return res;
    }

    public static void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target) {
        if (movement == null) {
            return;
        }
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    Point position = ((LifeMovement) move).getPosition();
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }

    protected static void updatePosition(final List<LifeMovementFragment> movement, final AnimatedMapleMapObject target, final int yoffset) {
        if (movement == null) {
            return;
        }
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    final Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }

    protected static void updatePositions(SeekableLittleEndianAccessor lea, AnimatedMapleMapObject target, int yOffset) {
        final byte numCommands = lea.readByte();
        //System.out.println("cmd " + numCommands);
        for (byte i = 0; i < numCommands; i++) {
            final byte command = lea.readByte();
            switch (command) {
                case 0:
                case 7:
                case 14:
                case 16:
                case 44:
                case 45:
                case 46: {
                    Point pos = lea.readPos();
                    final short xwobble = lea.readShort();
                    final short ywobble = lea.readShort();
                    final short unk = lea.readShort();
                    short fh = 0, xoffset = 0, yoffset = 0;
                    if (command == 14) {
                        fh = lea.readShort();
                    }
                    if (command != 44) {
                        xoffset = lea.readShort();
                        yoffset = lea.readShort();
                    }
                    target.setPosition((short) pos.x, (short) (pos.y + yOffset));
                    final byte newstate = lea.readByte();
                    target.setStance(newstate);
                    lea.readShort();

                    break;
                }
                case 1:
                case 2:
                case 15:
                case 18:
                case 19:
                case 21:
                case 40:
                case 41:
                case 42:
                case 43: {
                    Point pos = lea.readPos();
                    target.setPosition((short) pos.x, (short) (pos.y + yOffset));
                    short unk = 0;
                    if (command == 18 || command == 19) {
                        unk = lea.readShort();
                    }
                    final byte newstate = lea.readByte();
                    target.setStance(newstate);
                    lea.readShort();

                    break;
                }
                case 17: // special?...final charge aran
                case 22: // idk
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39: {
                    final byte newstate = lea.readByte();
                    target.setStance(newstate);
                    lea.readShort();

                    break;
                }
                case 3:
                case 4:
                case 5:
                case 6:
                case 8:
                case 9:
                case 10:
                case 12:
                case 13: {
                    Point pos = lea.readPos();
                    target.setPosition((short) pos.x, (short) (pos.y + yOffset));
                    final short fh = lea.readShort();
                    final byte newstate = lea.readByte();
                    target.setStance(newstate);
                    lea.readShort();

                    break;
                }
                case 20: {
                    Point pos = lea.readPos();
                    target.setPosition((short) pos.x, (short) (pos.y + yOffset));
                    final short xoffset = lea.readShort();
                    final short yoffset = lea.readShort();
                    final byte newstate = lea.readByte();
                    target.setStance(newstate);
                    lea.readShort();

                    break;
                }
                case 11: { // Update Equip or Dash
                    lea.readByte();
                    break;
                }
                default:
                    //chr.getClient().announce(HexTool.getByteArrayFromHexString("1A 00")); //give_buff with no data :D
                    System.out.println("Error with movements command: " + command);
            }
        }
    }
}
