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
var ticketId = 2430130;
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
var skillz = null;
var skill = null;
var entry = null;
var job = 0;
var exp = 0;
var id = 0;

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
        skillz = cm.getPlayer().getSkillz();
        if (!skillz.isEmpty()) {
            var rewards = "Which skill do you want to boost?\r\n#rMaximum Level of boosted skill is 999.#k\r\n\r\n";
            var skil = 0;
            for (var i = 0; i < skillz.size(); i++) {
                var skill = skillz.get(i);
                var entry = cm.getPlayer().getSkillEntry(skill);
                if (entry.getLevel() < 999) {
                    skil++;
                    var jobid = Math.floor(skill.getId() / 10000);
                    rewards += "#L" + i + "#";
                    if (jobid == 0) {
                        rewards += "#fSkill/000.img/skill/000" + skill.getId() + "/icon#";
                    } else {
                        rewards += "#fSkill/" + jobid + ".img/skill/" + skill.getId() + "/icon#";
                    }
                    rewards += "  #d#e" + cm.getPlayer().getSkillName(skill) + "#n#k #rLevel#k: #b" + entry.getSkillLevel() + "#k + #g" + entry.getLevel() + "#k#l\r\n";
                }
            }
            if (skil > 0) {
                cm.sendOk(rewards);
            } else {
                cm.sendOk("You currently do not have any skills high enough loser.");
            }
        } else {
            cm.sendOk("You currently do not have any skills loser.");
        }
    } else if (status == 1) {
        if (id == 0) {
            id = selection;
        }
        skill = skillz.get(id);
        entry = cm.getPlayer().getSkillEntry(skill);
        cm.sendGetText("Please enter the number of boosters you wish to apply to\r\n#d#e" + cm.getPlayer().getSkillName(skill) + "#n#k\r\n#rYou can only apply upto " + (999 - entry.getLevel()) + " Boosters.#k\r\n\r\n");

    } else if (status == 2) {
        count = cm.getNumber();
        if (count > 0 && count < 999) {
            if (cm.haveItem(ticketId, count)) {
                skill = skillz.get(id);
                entry = cm.getPlayer().getSkillEntry(skill);
                job = Math.floor(skill.getId() / 10000);
                exp = cm.getPlayer().getSkillNeededExp(skill);
                var rewards = "";
                if (job == 0) {
                    rewards += "#fSkill/000.img/skill/000" + skill.getId() + "/icon#";
                } else {
                    rewards += "#fSkill/" + job + ".img/skill/" + skill.getId() + "/icon#";
                }
                if (entry.getLevel() + count <= 999) {
                    rewards += " #d#e" + cm.getPlayer().getSkillName(skill) + "#n#k Bonus Level: #b" + entry.getLevel() + "#k -> #r" + (entry.getLevel() + count) + "#k\r\n";

                    cm.sendYesNo(rewards + "Do you want to spend #r" + count + "#k #i" + ticketId + "# to expand this skill?");
                } else {
                    cm.sendOk("You cannot use this many.");
                }
            } else {
                cm.sendOk("You currently do not have enough #i" + ticketId + "# #b" + cm.getItemName(ticketId) + "#k.");
            }
        } else {
            cm.sendOk("You cannot use this many.");
        }
    } else if (status == 3) {
        if (cm.haveItem(ticketId, count)) {
            cm.gainItem(ticketId, -count);
            skill = skillz.get(id);
            var rewards = "";
            cm.getPlayer().gainSkillLevel(skill, count);
            if (job == 0) {
                rewards += "#fSkill/000.img/skill/000" + skill.getId() + "/icon#";
            } else {
                rewards += "#fSkill/" + job + ".img/skill/" + skill.getId() + "/icon#";
            }
            rewards += "  #d#e" + cm.getPlayer().getSkillName(skill) + "#n#k ";
            entry = cm.getPlayer().getSkillEntry(skill);
            cm.sendOk(rewards + " has been leveled to #b" + entry.getLevel() + "#k.");
        } else {
            cm.sendOk("You currently do not have enough #b" + cm.getItemName(ticketId) + "#k.");
        }
    } else if (status == 4) {
        status = 2;
        action(0, 0, 0);
    }
}


