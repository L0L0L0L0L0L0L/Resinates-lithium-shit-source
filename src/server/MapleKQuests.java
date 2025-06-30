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

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tools.FileoutputUtil;

public class MapleKQuests {

    private static final Map<Integer, MapleKQuest> quests = new LinkedHashMap<>();
    private static final List<Integer> cag = new ArrayList<>();
    private static final MapleKQuests instance = new MapleKQuests();
    private static final Map<Integer, Integer> quest_pool = new LinkedHashMap<>();
    private static final List<Integer> random_quest_pool = new ArrayList<>();

    public static MapleKQuests getInstance() {
        return instance;
    }

    public MapleKQuest getById(int id) {
        return quests.get(id);
    }

    public MapleKQuest getByItemId(int id) {
        for (MapleKQuest quest : quests.values()) {
            if (quest.getItem() == id) {
                return quest;
            }
        }
        return null;
    }

    public List<Integer> getQuestsIds() {
        return new ArrayList<>(quests.keySet());
    }

    public List<MapleKQuest> getQuestsbyCag(int cag) {
        List<MapleKQuest> Achs = new ArrayList<>();
        for (MapleKQuest ach : quests.values()) {
            if (ach.getCag() == cag) {
                Achs.add(ach);
            }
        }
        return Achs;
    }

    public Map<Integer, Integer> getQuest_pool() {
        return quest_pool;
    }

    public List<Integer> getRandomQuestPool() {
        return random_quest_pool;
    }

    public List<MapleKQuest> getQuests() {
        return new ArrayList<>(quests.values());
    }

    public List<Integer> getQuestsCag() {
        return new ArrayList<>(cag);
    }

    public boolean isBannedCag(int qcag) {
        switch (qcag) {
            case 95:
            case 97:
                return true;
        }
        return false;
    }

    public static boolean bannedCag(int qcag) {
        switch (qcag) {
            case 95:
            case 97:
                return true;
        }
        return false;
    }

    public static void addQuest(MapleKQuest ach) {
        if (!quests.containsKey(ach.getId())) {
            if (!cag.contains(ach.getCag())) {
                cag.add(ach.getCag());
            }
            quests.put(ach.getId(), ach);
            if (!bannedCag(ach.getCag())) {
                quest_pool.put(ach.item, ach.minlevel);
                random_quest_pool.add(ach.item);
            }
        }
    }

    public static void loadQuests() {
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM quest_template"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                addQuest(new MapleKQuest(rs.getInt("id"), rs.getInt("town"), rs.getString("townname"), rs.getString("name"), rs.getInt("itemid"), rs.getInt("itemamount"), rs.getInt("minlevel"), rs.getInt("stat"), rs.getInt("statamount"), rs.getInt("reward"), rs.getInt("rewardamount")));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load quests");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(quests.size() + " Quests Loaded");
    }

}
