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
var status = 0;
var ticketId = 5062002;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 1;
var level = 1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        var skillz = cm.getPlayer().getSkillz();
        if (!skillz.isEmpty()) {
            var rewards = "";
            for (var i = 0; i < skillz.size(); i++) {
                var skill = skillz.get(i);
                var entry = cm.getPlayer().getSkillEntry(skill);
                var jobid = Math.floor(skill.getId() / 10000);
                if (jobid == 0) {
                    rewards += "#fSkill/000.img/skill/000" + skill.getId() + "/icon#";
                } else {
                    rewards += "#fSkill/" + jobid + ".img/skill/" + skill.getId() + "/icon#";
                }
                rewards += "  #d#e" + cm.getPlayer().getSkillName(skill) + "#n#k #B" + cm.getPlayer().getSkillExpPercent(skill) + "# (" + cm.getPlayer().getSkillExpPercent(skill) + "%)\r\n";
                rewards += "  #r#eSkill ID: " + skill.getId() + "\r\n";
                if (entry.getLevel() >= 9999) {
                    rewards += "#rLevel#k: #b" + entry.getSkillLevel() + "#k + #g" + entry.getLevel() + "#k (#rMax#k)\r\n";
                } else {
                    rewards += "#rLevel#k: #b" + entry.getSkillLevel() + "#k + #g" + entry.getLevel() + "#k\r\n";
                    rewards += "#rExp#k: #b" + cm.getUnitNumber(entry.getExp()) + "#k / #r" + cm.getUnitNumber(cm.getPlayer().getSkillNeededExp(skill)) + "#k Exp rate: #b" + skill.getScale() + "#kx\r\n";
                }
                var dam = 1;
                if (entry.getLevel() > 100) {
                    dam = 1 + (entry.getLevel() * 0.01);
                }
                rewards += "#rDamage#k: #b" + parseInt((Math.floor(((entry.getLevel() + entry.getSkillLevel()) * skill.getBaseDamage())))) + "%#k\r\n";
                rewards += "#rMonsters#k: #b" + skill.getMonsters() + "#k - #rLines#k: #b" + skill.getLines() + "#k  \r\n";
                rewards += "\r\n";
            }
            cm.sendOkS(rewards, 16);
        } else {
            cm.sendOk("You currently do not have any Etc to Store.");
        }
    } else if (status == 1) {
        equip = equiplist.get(selection);
        var eid = equip.getItemId();
        var eamount = equip.getQuantity();
        if (cm.getPlayer().addOverflowNPC(equip, eamount, " From ace of spades.")) {
            cm.sendYesNo("#i" + eid + "# #b" + cm.getItemName(eid) + " (" + eamount + "x) successfully stored.#k\r\n\Do you wish to store more items?");
        } else {
            cm.sendOk("#i" + eid + "# #b" + cm.getItemName(eid) + " (" + eamount + "x) successfully stored.#k");
        }
    } else if (status == 2) {
        status = 1;
        action(0, 0, 0);
    }
}


