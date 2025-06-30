/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
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
var status = 0;
var reward = 4033320;
var rewamount = 1;
var items = new Array(4032906, 4033335);
var amount = new Array(25000, 2500);

var bitems = new Array(4034180, 4034181, 4034182, 4034183);
var bamount = new Array(2500, 2500, 2500, 2500);
var option = 0;

var boss = "Khan";
var ach = 404;
var instance = "BGC_Khan";

var Cantidad;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getMapId() == 610052100) {
                if (!cm.getPlayer().achievementFinished(405)) {
                    cm.sendOk("I must Defeate Khan!!");
                } else {
                    cm.sendYesNo("Need a ride over to Commceri?");
                }
            } else {
                cm.sendOk("For Mordor!!!");
            }
        } else if (status == 1) {
            cm.warp(865010200, "berry_out");
        } else {
            cm.dispose();
        }
    }
}


