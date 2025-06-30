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
package scripting;

import client.MapleClient;
import server.MaplePortal;
import server.maps.MapleMap;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private final MaplePortal portal;

    public PortalPlayerInteraction(final MapleClient c, final MaplePortal portal) {
        super(c, portal.getId(), c.getPlayer().getMapId());
        this.portal = portal;
    }

    public final MaplePortal getPortal() {
        return portal;
    }

    public final void inFreeMarket() {
        if (getMapId() != 870000008) {
            if (getPlayer().getLevel() >= 10) {
                saveLocation("FREE_MARKET");
                playPortalSE();
                warp(870000008, "out00");
            } else {
                playerMessage(5, "You must be level 15 in order to be able to enter the FreeMarket.");
            }
        }
    }

    public final void inArdentmill() {
        if (getMapId() != 910001000) {
            if (getPlayer().getLevel() >= 10) {
                saveLocation("ARDENTMILL");
                playPortalSE();
                warp(910001000, "st00");
            } else {
                playerMessage(5, "You must be level 15 in order to be able to enter the Crafting Town.");
            }
        }
    }

    // summon one monster on reactor location
    @Override
    public void spawnMonster(int id) {
        spawnMonster(id, 1, portal.getPosition());
    }

    public void playPortalSound() {
        playPortalSE();
    }

    // summon monsters on reactor location
    @Override
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, portal.getPosition());
    }

    public String PortalName() {
        return portal.getPortalName();
    }

    public int PortalMap() {
        return portal.getTargetMapId();
    }

    public String TargetPortal() {
        return portal.getTarget();
    }

    public int getMinlevel() {
        switch (portal.getTargetMapId()) {

            case 98000://temple ruins - ark
                return 160;
            case 271000000://future hene/ksh
            case 82000://LKC
                return 200;
            case 273010000://future perion
            case 410000200://cheong village
            case 410000210://cheong left portal
            case 410000240://cheong temple
                return 250;
            case 86000://RA
            case 87500://base trunk
            case 87000://mid trunk
            case 87600://upper trunk
            case 87700://upper trunk
                return 300;
            case 310010010://edelstin
            case 310040000://edelstin outside caves
            case 701100000://Yu garden
            case 701103000://yu garden end of city
            case 241000000://krit
                return 400;
            case 410000000://fox village
            case 410000030://fox village right
            case 410000110://fox tree
            case 410000115://fox tree - top right
            case 410000114://fox tree - right
                return 700;
            case 400000001://pantheon
            case 401000002://helisium
            case 310070100://black heaven
            case 401050001://tyrant castle
            case 240090000://stone colosus
                return 600;
            case 701102000://Nanjing
                return 500;
            case 701210100://shaolin
            case 701220000://shaolin temple
                return 750;
            case 402000300://behind blackmarket
            case 402000000://black market
                return 800;
            case 402000501://santuary
            case 402000600://verdel
            case 402000521://san-temple
            case 402000631://verdel-temple
                return 900;
            //arc river maps
            case 450001003://arc river - ext town
            case 450001200://cave enterance - ext town
                return 1000;
            case 450014010://reverse-city
            case 450014100://reverse-city upper
            case 450014140://reverse-city lower
            case 450014320://RC tower upper
            case 450014220://RC tower lower
                return 1100;
            case 450001250://chuchu
            case 450002016://chuchu - whale
            case 450015020://yumyum
            case 450015150://yumyum - middle
            case 450015190://yumyum - dark
                return 1200;
            case 450003100://lach - nightmarket
            case 450003400://Lach - East
            case 450003500://Lach Clocktower
                return 1300;
            case 450005010://Arcana
            case 450005200://Arc - deep forest
            case 450005430://arc - east lagoon
            case 450005410://are - west lagoon
            case 450005500://arc - lagoon
                return 1400;
            case 450006010://morass
            case 450006130://tuffet town
            case 450006140://tuffet - upper area
            case 450006200://tuffet underground
            case 450006300://tuffet underground part 2
            case 450006400://destoryed tuffet
                return 1500;
            case 450007000://estera
            case 450007050://living sea
            case 450007110://Mirrior 
            case 450007140://Mirrior 2
            case 450007220://temple 2
            case 450007230://temple 3
            case 450007240://temple 4 - will
                return 1600;
            case 450009200://white spear ship 2
            case 450009300://white spear ship 3
                return 1800;
            //lab of suffering
            case 450011430: //lad top right
            case 450011500://core path
            case 450011540://core path right
            case 450011320://deep core
            case 450011630://deep split
                return 2000;
            case 450012200://Darknell
                return 2200;
            case 450012400://End of World 2-x
                return 2500;
            default:
                return 0;
        }
    }
}
