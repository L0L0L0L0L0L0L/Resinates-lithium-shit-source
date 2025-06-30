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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.life.MonsterDropEntry;
import tools.FileoutputUtil;

public class MapleDamageSkins {

    private static final Map<Integer, MapleDamageSkin> skins = new LinkedHashMap<>();
    private static final MapleDamageSkins instance = new MapleDamageSkins();
    private static final List<Integer> ids = new LinkedList<Integer>();
    private static final Map<Integer, List<MapleDamageSkinsGacha>> gacha = new LinkedHashMap<Integer, List<MapleDamageSkinsGacha>>();

    public static MapleDamageSkins getInstance() {
        return instance;
    }

    public MapleDamageSkin getById(int id) {
        return skins.get(id);
    }

    public List<Integer> getDamageSkinsIds() {
        return new ArrayList<>(skins.keySet());
    }

    public List<MapleDamageSkin> getDamageSkinsbyTier(int tier) {
        List<MapleDamageSkin> Skins = new ArrayList<>();
        for (MapleDamageSkin skin : skins.values()) {
            if (skin.getTier() == tier) {
                Skins.add(skin);
            }
        }
        return Skins;
    }

    public List<MapleDamageSkin> getDamageSkins() {
        return new ArrayList<>(skins.values());
    }

    public List<Integer> getDamageSkinsbyIds() {
        return new ArrayList<>(ids);
    }

    public static void loadDamageSkins() {
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM damage_skins_template"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                skins.put(rs.getInt("id"), new MapleDamageSkin(rs.getInt("id"), rs.getInt("tier"), rs.getInt("element"), rs.getInt("stat"), rs.getInt("stat_amount"), rs.getInt("stat_2"), rs.getInt("stat_2_amount"), rs.getInt("stat_3"), rs.getInt("stat_3_amount")));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load quests");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM damage_skins_template_ids"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load quests");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(skins.size() + " Damage Skins Loaded");
        System.out.println(ids.size() + " Damage ID's Loaded");
        //saveAch();
    }

    public static List<MapleDamageSkinsGacha> retrieveSkins(final int gachaId) {
        if (gacha.containsKey(gachaId)) {
            return Collections.unmodifiableList(gacha.get(gachaId));
        } else {
            final List<MapleDamageSkinsGacha> ret = new LinkedList<>();
            try (Connection con = DatabaseConnection.getWorldConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM damage_skins_gacha WHERE gach_id = ?")) {
                    ps.setInt(1, gachaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ret.add(new MapleDamageSkinsGacha(rs.getInt("skin_id"), rs.getInt("tier"), rs.getInt("chance"), rs.getInt("month")));
                        }
                    }
                }
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return ret;
            }
            gacha.put(gachaId, ret);
            if (!gacha.isEmpty()) {
                return Collections.unmodifiableList(gacha.get(gachaId));
            }
            return null;
        }
    }

    public static void clearGacha() {
        gacha.clear();
    }

    public static void reloadGachaID(int gachaId) {
        if (gacha.containsKey(gachaId)) {
            gacha.get(gachaId).clear();
            retrieveSkins(gachaId);
        }
    }
}
