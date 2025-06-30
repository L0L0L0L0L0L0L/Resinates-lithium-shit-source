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
package client.inventory;

import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;

import java.util.List;
import database.DatabaseConnection;
import java.util.ArrayList;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.Pair;
import tools.Triple;

public class MapleAndroid implements Serializable {

    public static class AndroidTemplate {

        public List<Integer> hairs = new ArrayList<>();
        public List<Integer> faces = new ArrayList<>();
        public List<Integer> skins = new ArrayList<>();
        public int capId;
        public int accId;
        public int overallId;
        public int gloveId;
        public int capeId;
        public int pantsId;
        public int shoeId;
    }

    private static final long serialVersionUID = 9179541993413738569L;
    private int stance = 0, uniqueid, itemid, hair, face, skin, model, level;
    private long exp;
    private String name;
    private Point pos = new Point(0, 0);
    private boolean changed = false;
    private AndroidTemplate template;

    private MapleAndroid(final int itemid, final int uniqueid) {
        this.itemid = itemid;
        this.uniqueid = uniqueid;
        this.template = MapleItemInformationProvider.getInstance().getAndroidInfo(MapleItemInformationProvider.getInstance().getAndroid(itemid));
    }

    public static final MapleAndroid loadFromDb(final int itemid, final int uid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            final MapleAndroid ret = new MapleAndroid(itemid, uid);

            PreparedStatement ps = con.prepareStatement("SELECT * FROM androids WHERE uniqueid = ?"); // Get pet details..
            ps.setInt(1, uid);

            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }

            ret.setHair(rs.getInt("hair"));
            ret.setFace(rs.getInt("face"));
            ret.setSkin(rs.getInt("skin"));
            ret.setName(rs.getString("name"));
            ret.setModel(rs.getInt("model"));
            ret.setLevel(rs.getInt("level"));
            ret.setExp(rs.getLong("exp"));
            ret.changed = false;

            rs.close();
            ps.close();

            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public final void saveToDb() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            final PreparedStatement ps = con.prepareStatement("UPDATE androids SET hair = ?, face = ?, skin = ?, name = ?, model = ?, level = ?, exp = ? WHERE uniqueid = ?");
            ps.setInt(1, hair);
            ps.setInt(2, face);
            ps.setInt(3, skin);
            ps.setString(4, name);
            ps.setInt(5, model);
            ps.setInt(6, level);
            ps.setLong(7, exp);
            ps.setInt(8, uniqueid); // Set ID
            ps.executeUpdate(); // Execute statement
            ps.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static final MapleAndroid create(final int itemid, final int uniqueid) {
        int model = MapleItemInformationProvider.getInstance().getAndroid(itemid);
        if (model == 0) {
            return null;
        }
        AndroidTemplate aInfo = MapleItemInformationProvider.getInstance().getAndroidInfo(model);
        if (aInfo == null) {
            return null;
        }
        if (itemid == 1662006) {
            int face = MapleItemInformationProvider.getInstance().getFaces().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getFaces().size()));
            int hair = MapleItemInformationProvider.getInstance().getHairs().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getHairs().size()));
            int skin = MapleItemInformationProvider.getInstance().getSkins().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getSkins().size()));
            return create(itemid, uniqueid, hair, face, skin, model);
        } else {
            return create(itemid, uniqueid, aInfo.hairs.get(Randomizer.nextInt(aInfo.hairs.size())), aInfo.faces.get(Randomizer.nextInt(aInfo.faces.size())), aInfo.skins.get(Randomizer.nextInt(aInfo.skins.size())) - 2000, model);
        }
    }

    public static final MapleAndroid create(int itemid, int uniqueid, int hair, int face, int skin, int model) {
        if (uniqueid <= -1) { //wah
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        String aname = MapleItemInformationProvider.getInstance().setRandomName();

        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement pse = con.prepareStatement("INSERT INTO androids (uniqueid, hair, face, skin, name, model) VALUES (?, ?, ?, ?, ?, ?)");
            pse.setInt(1, uniqueid);
            pse.setInt(2, hair);
            pse.setInt(3, face);
            pse.setInt(4, skin);
            pse.setString(5, aname);
            pse.setInt(6, model);
            pse.executeUpdate();
            pse.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        final MapleAndroid pet = new MapleAndroid(itemid, uniqueid);
        pet.setHair(hair);
        pet.setFace(face);
        pet.setSkin(skin);
        pet.setName(aname);
        pet.setModel(model);
        pet.setLevel(1);
        pet.setExp(0);

        return pet;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public final void setSkin(final int closeness) {
        this.skin = closeness;
        this.changed = true;
    }

    public final int getSkin() {
        return skin;
    }

    public final void setHair(final int closeness) {
        this.hair = closeness;
        this.changed = true;
    }

    public final int getHair() {
        return hair;
    }

    public final void setFace(final int closeness) {
        this.face = closeness;
        this.changed = true;
    }

    public final int getFace() {
        return face;
    }

    public final void setExp(final long exp) {
        this.exp = exp;
    }

    public final long getExp() {
        return exp;
    }

    public final void setLevel(final int level) {
        this.level = level;
    }

    public final int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
        this.changed = true;
    }

    public final Point getPos() {
        return pos;
    }

    public final void setPos(final Point pos) {
        this.pos = pos;
    }

    public final int getStance() {
        return stance;
    }

    public final void setStance(final int stance) {
        this.stance = stance;
    }

    public final int getModel() {
        return model;
    }

    public final void setModel(final int id) {
        this.model = id;
    }

    public final int getItemId() {
        return itemid;
    }

    public void changeLook() {
        this.face = MapleItemInformationProvider.getInstance().getFaces().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getFaces().size()));
        this.hair = MapleItemInformationProvider.getInstance().getHairs().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getHairs().size()));
        this.skin = MapleItemInformationProvider.getInstance().getSkins().get(Randomizer.nextInt(MapleItemInformationProvider.getInstance().getSkins().size()));
        this.changed = true;
        saveToDb();
    }

    public final void updatePosition(final List<LifeMovementFragment> movement) {
        for (final LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    setPos(((LifeMovement) move).getPosition());
                }
                setStance(((LifeMovement) move).getNewstate());
            }
        }
    }

    public AndroidTemplate getTemplate() {
        return template;
    }
}
