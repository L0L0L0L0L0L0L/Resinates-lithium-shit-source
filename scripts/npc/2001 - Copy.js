/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public              as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 .
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public              for more details.
 
 You should have received a copy of the GNU Affero General Public             
 along with this program.  If not, see <http://www.gnu.org/            s/>.
 */
/* Author: Xterminator
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;

function start() {
    //cm.sendSimple("Which beginning would you like to pick from?\r\n\#L0# Adventurer #l\r\n\#L1# Noblesse #l\r\n\#L2# Resistance #l\r\n\#L3# Demon Slayer #l\r\n\#L4# Mercedes #l"); 
    //cm.sendSimple("Which beginning would you like to pick from?\r\n\#L0# Adventurer #l\r\n\#L2# Resistance #l\r\n\#L3# Demon Slayer #l\r\n\#L6# Evan #l");
    //cm.sendSimple("Which beginning would you like to pick from?\r\n\#L0# Adventurer #l\r\n\#L2# Resistance #l");
    cm.sendYesNo("Welcome to Kaotic Maple. Be sure to read the entire Server-info section in discord for custom features. Are you ready to proceed?");
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        cm.getPlayer().addDP();
        switch (cm.getPlayer().getJob()) {
            case 0://adv
            case 1000://ck
            case 2001://evan
            case 3000://resist
                cm.warp(5001, 0);
                break;
            case 3001:
                cm.getPlayer().gainMeso(25000, true);
                cm.getPlayer().addLevel(9);
                cm.getPlayer().changeJob(3100);
                cm.gainEquip(1112920, 250, 2, 0, 0, false);
                cm.gainEquip(1112920, 250, 2, 0, 0, false);
                cm.gainEquip(1112920, 250, 2, 0, 0, false);
                cm.gainEquip(1112920, 250, 2, 0, 0, false);
                cm.gainEquip(1662006, 250, 1, 0, 0, false);
                cm.gainEquip(1672000, 250, 1, 0, 0, false);
                //cm.getPlayer().superbuff();
                cm.warp(102000003);
                cm.sendOkS("You have been giving 4x #i1112920# and free buffs to start your journey. Outside of town is Training instructor, talk to him about leveling. If you are lost please check out discord and @say command to ask for help. Also you can take on quests from this job instructor for early Cubes and Perma Stats '@bonus' and quick level boost.", 2);
                break;
            case 2000://aran
            case 2002://merc
                cm.sendOk("This job is currently disabled. Please remake character. Disabled jobs are Aran and Mercedes.");
                return;
        }
        cm.getPlayer().updateSkills(cm.getPlayer().getJob());

    }

}

