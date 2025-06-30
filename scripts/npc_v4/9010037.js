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
var options = 0;
var quest;
var questid;
var list;

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
        var selStr = "";
        for (var i = 0; i <= 99; i++) {
            if (!cm.getQuestsbyCag(i).isEmpty()) {
                if (cm.getQuestsCompletedbyCagName(i)) {
                    selStr += ("#L" + i + "#  #g" + cm.getQuestsbyCagName(i) + " (Comeplete)#k #l\r\n");
                } else {
                    selStr += ("#L" + i + "#  #r" + cm.getQuestsbyCagName(i) + "#k #l\r\n");
                }
            }
        }
        var rate = Math.floor((cm.getPlayer().getFinishedQuestss().size() / cm.getQuests().size()) * 100);
        cm.sendSimpleS("0 #B" + rate + "# " + cm.getQuests().size() + " Quest Progression\r\n\Quests Completed: " + cm.getPlayer().getFinishedQuestss().size() + " (" + rate + "%)\r\n\#b#L9999#  Collect All Quests#k#l\r\n\r\n\Select a Town:\r\n" + selStr, 2);
    } else if (status == 1) {
        if (selection == 9999) {
            var count = cm.getPlayer().checkAllQuests();
            if (count > 0) {
                cm.sendOkS("#b" + count + " Quests has been completed.#k", 2);
            } else {
                cm.sendOkS("#rNo quests have been completed.#k", 2);
            }
        } else {
            list = cm.getQuestsbyCag(selection);
            var rate = Math.floor((cm.getPlayer().getquestFinishedbyCag(selection) / cm.getQuestsCompletedbyCag(selection)) * 100);
            var selStr = ("Select which quest you wish to turn in under " + cm.getQuestsbyCagName(selection) + ":\r\n0 #B" + rate + "# " + cm.getQuestsCompletedbyCag(selection) + " " + rate + "% Quests Completed:\r\n\r\n  ");
            for (var i = 0; i < list.size(); i++) {
                var Ach = list.get(i);
                if (Ach != null) {
                    var lvl = Ach.getQuestLevel() > 0 ? "#bLv." +Ach.getQuestLevel()+ "#k"  : "";
                    if (cm.getPlayer().getQuest(cm.getQuestId(Ach))) {
                        selStr += ("#L" + i + "#"+lvl+"#i" + Ach.getItem() + "##g Collect " + Ach.getItemAmount() + " " + Ach.getQuestName() + "'s#k #l\r\n\  ");
                    } else {
                        selStr += ("#L" + i + "#"+lvl+"#i" + Ach.getItem() + "##r Collect " + Ach.getItemAmount() + " " + Ach.getQuestName() + "'s#k #l\r\n\  ");
                    }
                }
            }
            cm.sendSimpleS(selStr + "\r\n", 2);
        }
    } else if (status == 2) {
        questid = cm.getQuestId(list.get(selection));
        quest = cm.getQuestbyId(questid);
        if (cm.getPlayer().getQuest(questid)) {
            cm.sendOkS("This Quest has already been completed.", 2);
        } else {
            cm.sendYesNoS("Do you have " + quest.getItemAmount() + "x #i" + quest.getItem() + "#?", 2);
        }
    } else if (status == 3) {
        if (cm.getPlayer().finishMapleQuest(questid)) {
            cm.gainItem(quest.getItem(), -quest.getItemAmount());
            cm.sendOkS("Quest has been completed. You have gained #i" + quest.getReward() + " (" + quest.getRewardAmount() + "x) ", 2);
        } else if (cm.getPlayer().finishMapleQuestEtc(questid)) {
            cm.sendOkS("Quest has been completed. You have gained #i" + quest.getReward() + " (" + quest.getRewardAmount() + "x) ", 2);
        } else {
            cm.sendOkS("Seems like you are missing some required items for this quest.", 2);
        }
    }
}

