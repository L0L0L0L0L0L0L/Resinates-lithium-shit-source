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
import client.MapleClient;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

/**
 *
 * @author KyleShum
 */
public class MapleKQuest {

    public String town, name;
    public int id, cag, item, itemamount, stat, statamount, reward, rewardamount, minlevel;

    public MapleKQuest(int id, int cag, String town, String name, int item, int itemamount, int minlevel, int stat, int statamount, int reward, int rewardamount) {
        this.id = id;
        this.cag = cag;
        this.town = town;
        this.name = name;
        this.item = item;
        this.itemamount = itemamount;
        this.minlevel = minlevel;
        this.stat = stat;
        this.statamount = statamount;
        this.reward = reward;
        this.rewardamount = rewardamount;
    }

    public int getId() {
        return id;
    }

    public int getCag() {
        return cag;
    }

    public String getTownName() {
        return town;
    }

    public String getQuestName() {
        return name;
    }

    public int getItem() {
        return item;
    }

    public int getItemAmount() {
        return itemamount;
    }

    public int getStat() {
        return stat;
    }

    public int getStatAmount() {
        return statamount;
    }

    public int getReward() {
        return reward;
    }

    public int getRewardAmount() {
        return rewardamount;
    }

    public int getQuestLevel() {
        return minlevel;
    }

    public boolean finishQuest(MapleCharacter chr) {
        if (chr.haveItem(item, itemamount)) {
            if (chr.tryGainQuest(id)) {
                chr.getClient().announce(CField.EffectPacket.showForeignEffect(12));
                chr.getClient().announce(CWvsContext.serverNotice(5, "[Quest Completed] You've comepleted: " + name + "."));
                chr.gainItem(reward, rewardamount, "collected " + rewardamount + " from quest: " + id + " - Name: " + name);
                saveQuest(chr, id);
                boolean check = true;
                if (!chr.getAchievement(500 + cag)) {
                    for (MapleKQuest quest : MapleKQuests.getInstance().getQuestsbyCag(cag)) {
                        if (!chr.getQuest(quest.getId())) {
                            check = false;
                            break;
                        }
                    }
                    if (check) {
                        chr.finishAchievement(500 + cag);
                    }
                }
                int comepleted = chr.getFinishedQuestss().size();
                if (comepleted >= 10) {
                    chr.finishAchievement(171);
                }
                if (comepleted >= 25) {
                    chr.finishAchievement(172);
                }
                if (comepleted >= 50) {
                    chr.finishAchievement(173);
                }
                if (comepleted >= 75) {
                    chr.finishAchievement(174);
                }
                if (comepleted >= 100) {
                    chr.finishAchievement(175);
                }
                if (comepleted >= 250) {
                    chr.finishAchievement(176);
                }
                if (comepleted >= 500) {
                    chr.finishAchievement(177);
                }
                if (comepleted >= 750) {
                    chr.finishAchievement(179);
                }
                return true;
            }
        }
        return false;
    }

    public boolean finishQuestETC(MapleCharacter chr) {
        if (chr.getOverflowAmount(item) >= itemamount) {
            if (chr.tryGainQuest(id)) {
                chr.removeOverflow(item, itemamount);
                String ItemName = MapleItemInformationProvider.getInstance().getName(item);
                chr.dropMessage(ItemName + " (" + itemamount + "x) has been removed from your ETC storage.");
                chr.getClient().announce(CField.EffectPacket.showForeignEffect(12));
                chr.getClient().announce(CWvsContext.serverNotice(5, "[Quest Completed] You've comepleted: " + name + "."));
                chr.gainItem(reward, rewardamount, "collected " + rewardamount + " from etc quest: " + id + " - Name: " + name);
                saveQuest(chr, id);
                boolean check = true;
                if (!chr.getAchievement(500 + cag)) {
                    for (MapleKQuest quest : MapleKQuests.getInstance().getQuestsbyCag(cag)) {
                        if (!chr.getQuest(quest.getId())) {
                            check = false;
                            break;
                        }
                    }
                    if (check) {
                        chr.finishAchievement(500 + cag);
                    }
                }
                int comepleted = chr.getFinishedQuestss().size();
                if (comepleted >= 10) {
                    chr.finishAchievement(171);
                }
                if (comepleted >= 25) {
                    chr.finishAchievement(172);
                }
                if (comepleted >= 50) {
                    chr.finishAchievement(173);
                }
                if (comepleted >= 75) {
                    chr.finishAchievement(174);
                }
                if (comepleted >= 100) {
                    chr.finishAchievement(175);
                }
                if (comepleted >= 250) {
                    chr.finishAchievement(176);
                }
                if (comepleted >= 500) {
                    chr.finishAchievement(177);
                }
                if (comepleted >= 750) {
                    chr.finishAchievement(179);
                }
                return true;
            }
        }
        return false;
    }

    public void saveQuest(MapleCharacter chr, int id) {
        try ( Connection con = DatabaseConnection.getPlayerConnection()) {
            try ( PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO quests(accountid, questid) VALUES(?, ?)")) {
                ps.setInt(1, chr.getAccountID());
                ps.setInt(2, id);
                ps.execute();
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ex);
            ex.printStackTrace();
        }
    }
}
