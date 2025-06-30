/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tools.FilePrinter;

/**
 *
 * @author David
 */
public class GachaponProvider {

    private static final Map<Integer, List<GachaponEntry>> entries = new HashMap<>();

    private static void loadFromDB() {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM gach_data");
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int townId = rs.getInt("gachid");
                    List<GachaponEntry> items = entries.get(townId);
                    if (items == null) {
                        items = new LinkedList<>();
                        entries.put(townId, items);
                    }
                    GachaponEntry itemEntry = new GachaponEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"));
                    items.add(itemEntry);
                }
            }
            ps.close();
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
    }

    public static void reloadFromDB() {
        synchronized (entries) {
            entries.clear();
            loadFromDB();
        }
    }
}
