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
package scripting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import client.MapleClient;
import database.DatabaseConnection;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import server.maps.ReactorDropEntry;
import server.maps.MapleReactor;
import tools.FilePrinter;
import tools.FileoutputUtil;

public class ReactorScriptManager extends AbstractScriptManager {

    private static final ReactorScriptManager instance = new ReactorScriptManager();
    private final Map<Integer, List<ReactorDropEntry>> drops = new HashMap<Integer, List<ReactorDropEntry>>();
    //private Map<String, PortalScript> scripts = new HashMap<String, PortalScript>();
    private ScriptEngineFactory sef;

    public static final ReactorScriptManager getInstance() {
        return new ReactorScriptManager();
    }

    private ReactorScriptManager() {
        ScriptEngineManager sem = new ScriptEngineManager();
        sef = sem.getEngineByName("javascript").getFactory();
    }

    public final void act(final MapleClient c, final MapleReactor reactor) {
        try {
            Invocable iv = getInvocable("reactor/" + reactor.getId() + ".js", c);
            if (iv == null) return;
            
            ReactorActionManager rm = new ReactorActionManager(c, reactor, iv);
            engine.put("rm", rm);
            iv.invokeFunction("act");
        } catch (final ScriptException | NoSuchMethodException | NullPointerException e) {
            FilePrinter.printError(FilePrinter.REACTOR + reactor.getId() + ".txt", e);
        }
    }

    public final List<ReactorDropEntry> getDrops(final int rid) {
        List<ReactorDropEntry> ret = drops.get(rid);
        if (ret != null) {
            return ret;
        }
        ret = new LinkedList<ReactorDropEntry>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try (Connection con = DatabaseConnection.getWorldConnection()) {
            ps = con.prepareStatement("SELECT * FROM reactordrops WHERE reactorid = ?");
            ps.setInt(1, rid);
            rs = ps.executeQuery();

            while (rs.next()) {
                ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
            }
            rs.close();
            ps.close();
        } catch (final SQLException e) {
            System.err.println("Could not retrieve drops for reactor " + rid + e);
            return ret;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignore) {
                return ret;
            }
        }
        drops.put(rid, ret);
        return ret;
    }

    public final void clearDrops() {
        drops.clear();
    }
}
