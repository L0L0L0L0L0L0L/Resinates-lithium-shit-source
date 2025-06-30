/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import static server.MapleKQuests.addQuest;
import tools.FileoutputUtil;

/**
 *
 * @author Evil0
 */
public class Parks {

    private static final Parks instance = new Parks();

    public static Parks getInstance() {
        return instance;
    }
    
    private static final Map<Integer, Park> parks = new LinkedHashMap<>();
    
    public Park getById(int id) {
        return parks.get(id);
    }

    public static void loadParks() {
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM park"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                int mode = rs.getInt("mode");
                Park park = new Park(mode, rs.getInt("tier"), rs.getInt("tier_mini"), rs.getInt("tier_boss"), rs.getInt("waves"), rs.getInt("maps"), rs.getInt("mini_bosses"), rs.getInt("bosses"), rs.getInt("req_ach"), rs.getInt("ach"), rs.getInt("mini_cap"), rs.getInt("boss_cap"), rs.getInt("boss_cap_final"));
                park.setRank(rs.getInt("rank"));
                park.setReward(rs.getInt("reward"));
                park.setRewardAmount(rs.getInt("reward_amount"));
                parks.put(mode, park);
                park.loadPark(con);
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load quests");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(parks.size() + " Dungeons Loaded");
    }

}
