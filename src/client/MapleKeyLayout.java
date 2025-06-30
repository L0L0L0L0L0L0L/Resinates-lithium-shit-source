/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
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
package client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;

import tools.data.MaplePacketLittleEndianWriter;

import database.DatabaseConnection;
import server.maps.MapleMapFactory;
import tools.Pair;
import tools.packet.CField;

public class MapleKeyLayout implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private boolean changed = false;
    //private Map<Integer, Pair<Byte, Integer>> keymap = new HashMap<Integer, Pair<Byte, Integer>>();
    public Map<Integer, Map<Integer, Pair<Byte, Integer>>> keymaps;

    public MapleKeyLayout() {
        keymaps = new HashMap<>();
    }

    public MapleKeyLayout(int job) {
        keymaps.put(job, new HashMap<Integer, Pair<Byte, Integer>>());
    }

    public MapleKeyLayout(int jobid, Map<Integer, Pair<Byte, Integer>> keys) {
        if (keymaps.get(jobid) == null) {
            keymaps.put(jobid, new HashMap<>());
        }
        keymaps.put(jobid, keys);
    }

    public void addKeyLayout(int jobid, int key, byte type, int action) {
        if (keymaps.get(jobid) == null) {
            keymaps.put(jobid, new HashMap<>());
        }
        Map<Integer, Pair<Byte, Integer>> jobMap = keymaps.get(jobid);
        jobMap.put(key, new Pair<>(type, action));
        keymaps.put(jobid, jobMap);
    }

    public void removeKeyLayout(int jobid, int key) {
        if (keymaps.containsKey(jobid)) {
            if (keymaps.get(jobid).containsKey(key)) {
                keymaps.get(jobid).remove(key);
            }
        }
    }

    public boolean checkKey(int jobid, int key) {
        if (keymaps.containsKey(jobid)) {
            return keymaps.get(jobid).containsKey(key);
        }
        return false;
    }

    public Map<Integer, Pair<Byte, Integer>> getLayout(int jobid) {
        if (keymaps.get(jobid) == null) {
            keymaps.put(jobid, new HashMap<Integer, Pair<Byte, Integer>>());
        }
        return keymaps.get(jobid);
    }

    public final void changed() {
        changed = false;
    }

    public final void clearKeys(final int charid, int jobid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ? AND jobid = ?");
            ps.setInt(1, charid);
            ps.setInt(2, jobid);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error getting character default" + e);
        }
        keymaps.get(jobid).clear();
        saveKeysbyJob(charid, jobid);
    }

    public void changeSkillKeys(MapleCharacter player) {
        var keys = getLayout(player.getJob());
        if (keys.isEmpty()) {
            return;
        }
        Pair<Byte, Integer> binding;
        for (int x = 0; x < 89; x++) {
            binding = keys.get(x);
            if (binding != null) {
                if (binding.getLeft() == 1) {
                    keys.remove(x);
                }
            }
        }
        player.getClient().announce(CField.getKeymap(this, player.getJob()));
        saveKeysbyJob(player.getId(), player.getJob());
    }

    public final void writeData(final MaplePacketLittleEndianWriter mplew, int jobid) {
        var keymap = getLayout(jobid);
        boolean check = true;
        if (keymap == null || keymap.isEmpty()) {
            check = false;
        }
        mplew.write(check ? 0 : 1);
        if (!check) {
            return;
        }
        if (keymap != null) {
            Pair<Byte, Integer> binding;
            for (int x = 0; x < 89; x++) {
                binding = keymap.get(x);
                if (binding != null) {
                    mplew.write(binding.getLeft());
                    mplew.writeInt(binding.getRight());
                } else {
                    mplew.write(0);
                    mplew.writeInt(0);
                }
            }
        }
    }

    public final void saveKeys(final int charid) {
        if (!keymaps.isEmpty()) {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                ps.execute();
                ps.close();
                ps = con.prepareStatement("INSERT INTO `keymap` VALUES(?, ?, ?, ?, ?)");
                for (int jobid : keymaps.keySet()) {
                    if (!keymaps.get(jobid).isEmpty()) {
                        ps.setInt(1, charid);
                        ps.setInt(2, jobid);
                        for (int key : keymaps.get(jobid).keySet()) {
                            ps.setByte(3, (byte) key);
                            ps.setByte(4, keymaps.get(jobid).get(key).getLeft());
                            ps.setInt(5, keymaps.get(jobid).get(key).getRight());
                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();
                ps.close();
            } catch (SQLException e) {
                System.err.println("Error getting character default" + e);
                e.printStackTrace();
            }
        }
    }

    public final void saveKeysbyJob(final int charid, int jobid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ? AND jobid = ?");
            ps.setInt(1, charid);
            ps.setInt(2, jobid);
            ps.execute();
            ps.close();
            if (!keymaps.get(jobid).isEmpty()) {
                ps = con.prepareStatement("INSERT INTO `keymap` VALUES(?, ?, ?, ?, ?)");
                ps.setInt(1, charid);
                ps.setInt(2, jobid);
                for (int key : keymaps.get(jobid).keySet()) {
                    ps.setByte(3, (byte) key);
                    ps.setByte(4, keymaps.get(jobid).get(key).getLeft());
                    ps.setInt(5, keymaps.get(jobid).get(key).getRight());
                    ps.addBatch();
                }
                ps.executeBatch();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("Error getting character job default" + e);
            e.printStackTrace();
        }
    }
}
