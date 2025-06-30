/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
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
package server;

import client.MapleCharacter;
import tools.packet.CField;
import tools.packet.CWvsContext;

/**
 *
 * @author KyleShum
 */
public class MapleAchievement {

    public String name;
    public int reward, reward_amount, stat, stat_amount, id, cag;
    public boolean notice;
    public String[] Stat_type = {"Exp Rate", "Drop Rate", "Drop Rate", "All Stats", "Overpower", "Meso Rate", "Total Damage", "Boss Damage", "IED"};

    public MapleAchievement(int id, int cag, String name, int reward, int reward_amount, int type, int amount) {
        this.id = id;
        this.cag = cag;
        this.name = name;
        this.reward = reward;
        this.reward_amount = reward_amount;
        this.stat = type;
        this.stat_amount = amount;
        this.notice = true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return stat;
    }

    public int getCag() {
        return cag;
    }

    public int getAmount() {
        return stat_amount;
    }

    public int getReward() {
        return reward;
    }

    public int getRewardAmount() {
        return reward_amount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public boolean getNotice() {
        return notice;
    }

    public void finishAchievement(MapleCharacter chr) {
        if (chr.tryGainAchievement(id)) {
            chr.getClient().announce(CField.EffectPacket.showForeignEffect(12));
            chr.getClient().announce(CWvsContext.serverNotice(6, "[Achievement Completed] " + name + ". Gained +" + stat_amount + "% " + Stat_type[stat] + "!"));
            chr.getAchieveStat();
            chr.getClient().announce(CWvsContext.serverNotice(6, "[Achievement] Bonus Stats have been updated."));
            chr.saveAchievement(id);
            chr.gainItem(reward, reward_amount, "collected " + reward_amount + " from achievement: " + id);
        }
    }
}
