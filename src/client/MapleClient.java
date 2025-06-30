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
 MERCHANTABILITY or FITESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import static client.LoginCrypto.makeSaltedSha512Hash;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ScriptableNPCConstants;
import constants.ServerConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.io.Serializable;

import javax.script.ScriptEngine;

import database.DatabaseConnection;
import database.DatabaseException;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.handler.GuildHandler;
import handling.login.LoginServer;
import handling.world.CharacterIdChannelPair;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.MapleRaid;
import handling.world.PartyOperation;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import handling.world.exped.MapleExpedition;
import handling.world.family.MapleFamilyCharacter;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import server.maps.MapleMap;
import tools.FileoutputUtil;
import tools.MapleAESOFB;
import tools.packet.LoginPacket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.script.Invocable;

import org.apache.mina.common.IoSession;
import scripting.AbstractPlayerInteraction;
import scripting.NPCConversationManager;
import scripting.NPCScriptManager;
import scripting.PortalPlayerInteraction;
import server.Randomizer;
import server.Timer.PingTimer;
import server.TimerManager;
import server.quest.MapleQuest;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MTSCSPacket;

public class MapleClient implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public static final byte LOGIN_NOTLOGGEDIN = 0, LOGIN_SERVER_TRANSITION = 1, LOGIN_LOGGEDIN = 2, CHANGE_CHANNEL = 3, POST_LOGGIN = 4;
    public static final int DEFAULT_CHARSLOT = 15;
    public static final String CLIENT_KEY = "CLIENT";
    public static final String CLIENT_HWID = "HWID";
    public static final String CLIENT_NIBBLEHWID = "HWID2";
    public static final String CLIENT_REMOTE_ADDRESS = "REMOTE_IP";
    public static final String CLIENT_TRANSITION = "TRANSITION";
    private final transient MapleAESOFB send, receive;
    private final transient IoSession session;
    private MapleCharacter player;
    private int channel = 1, accId = -1, world, birthday;
    private int charslots = DEFAULT_CHARSLOT;
    private boolean loggedIn = false, serverTransition = false;
    private transient Calendar tempban = null;
    private String accountName;
    private transient long lastPong = 0, lastPing = 0;
    private boolean monitored = false, receiving = true;
    private boolean gm;
    private byte greason = 1, gender = -1;
    public transient short loginAttempt = 0;
    private final transient List<Integer> allowedChar = new LinkedList<Integer>();
    private final transient Set<String> macs = new HashSet<String>();
    private final transient Map<String, ScriptEngine> engines = new HashMap<String, ScriptEngine>();
    private transient ScheduledFuture<?> idleTask = null;
    private transient String secondPassword, salt2, tempIP = ""; // To be used only on login
    private final transient Lock mutex = new ReentrantLock(true);
    private final transient Lock npc_mutex = new ReentrantLock();
    private long lastNpcClick = 0;
    private final static Lock login_mutex = new ReentrantLock(true);
    private String hwid = null;
    private final ReentrantReadWriteLock ALock = new ReentrantReadWriteLock();
    private final Lock readQueueLock = new ReentrantLock();
    public int numTimes = 0;
    public boolean online = false;
    private long sessionId;
    public boolean lock = false, chat = false;
    public long npcCoolDown = 0, ping = 0, delay = 0, spike = 0;
    private NPCConversationManager cms = null;
    private PortalPlayerInteraction pms = null;
    private Invocable NpcScripts = null;
    private final Lock announcerLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CLIENT_ANNOUNCER, true);
    public long CharCoolDown = 0;
    private volatile boolean fullLogging;
    public int tempid = 0;
    public int count = 0;
    public boolean pinging = false;
    public String hash = "";

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public final MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public final MapleAESOFB getSendCrypto() {
        return send;
    }

    public boolean isFullLogged() {
        return fullLogging;
    }

    public void setFullLogged(boolean toggle) {
        this.fullLogging = toggle;
    }

    /*
     public final void announce(final byte[] packet) {     // thanks GitGud for noticing an opportunity for improvement by overcoming "synchronized announce"
     announcerLock.lock();
     try {
     session.write(packet);
     } finally {
     announcerLock.unlock();
     }

     }
     */
    public final synchronized void announce(final byte[] packet) {     // thanks GitGud for noticing an opportunity for improvement by overcoming "synchronized announce"
        session.write(packet);

    }

    public final IoSession getSession() {
        //System.out.println("test");
        return session;
    }

    public final Lock getLock() {
        return mutex;
    }

    public final Lock getNPCLock() {
        return npc_mutex;
    }

    public final void setCMS(NPCConversationManager script) {
        cms = script;
    }

    public final NPCConversationManager getCMS() {
        return cms;
    }

    public final void setNpcScript(Invocable script) {
        NpcScripts = script;
    }

    public final Invocable getNpcScript() {
        return NpcScripts;
    }

    public final void setPMS(PortalPlayerInteraction script) {
        pms = script;
    }

    public final PortalPlayerInteraction getPMS() {
        return pms;
    }

    public MapleCharacter getPlayer() {
        if (this != null) {
            return player;
        } else {
            return null;
        }
    }

    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    public void setOnline(boolean toggle) {
        this.online = toggle;
    }

    public boolean getOnline() {
        return this.online;
    }

    public void setChat(boolean toggle) {
        this.chat = toggle;
    }

    public boolean getChat() {
        return this.chat;
    }

    public void setNpcCoolDown(long value) {
        this.npcCoolDown = value;
    }

    public long getNpcCoolDown() {
        return this.npcCoolDown;
    }

    public void setCharCoolDown(long value) {
        this.CharCoolDown = value;
    }

    public long getCharCoolDown() {
        return this.CharCoolDown;
    }

    public void createdChar(final int id) {
        allowedChar.add(id);
    }

    public final boolean login_Auth(final int id) {
        return allowedChar.contains(id);
    }

    public List<Integer> getAllChars(int id) {
        return allowedChar;
    }

    public final List<MapleCharacter> loadCharacters(final int serverId) { // TODO make this less costly zZz
        List<MapleCharacter> chars = new ArrayList<>(15);
        try {
            for (CharNameAndId cni : loadCharactersInternal(serverId)) {
                if (getPlayer() != null && getPlayer().getId() == cni.id) {
                    chars.add(getPlayer());
                } else {
                    final MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
                    chars.add(chr);
                    if (!login_Auth(chr.getId())) {
                        allowedChar.add(chr.getId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chars;
    }

    public final MapleCharacter loadCharacter(final int serverId, int id) { // TODO make this less costly zZz
        try {
            for (CharNameAndId cni : loadCharactersInternal(serverId)) {
                if (cni.id == id) {
                    return MapleCharacter.loadCharFromDB(cni.id, this, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final List<Integer> getCharacters(int accid) { // TODO make this less costly zZz
        List<Integer> chars = new ArrayList<>(15);
        try (Connection con = DatabaseConnection.getPlayerConnection()) {

            try (PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ?")) {
                ps.setInt(1, accid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        chars.add(rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chars;
    }

    public boolean canMakeCharacter(int serverId) {
        return loadCharactersSize(serverId) < getCharacterSlots();
    }

    public List<String> loadCharacterNames(int serverId) {
        List<String> chars = new LinkedList<String>();
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    public final void forceDisconnect() {
        disconnect(true, false);
    }

    private List<CharNameAndId> loadCharactersInternal(int serverId) {
        List<CharNameAndId> chars = new ArrayList<>(15);
        try (Connection con = DatabaseConnection.getPlayerConnection()) {

            try (PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ?")) {
                ps.setInt(1, getAccID());
                ps.setInt(2, serverId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chars;
    }

    private int loadCharactersSize(int serverId) {
        int chars = 0;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM characters WHERE accountid = ? AND world = ?");
            ps.setInt(1, accId);
            ps.setInt(2, serverId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                chars = rs.getInt(1);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("error loading characters internal");
            e.printStackTrace();
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn && accId >= 0;
    }

    private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
        Calendar lTempban = Calendar.getInstance();
        if (rs.getTimestamp("tempban") == null) { // basically if timestamp in db is 0000-00-00
            lTempban.setTimeInMillis(0);
            return lTempban;
        }
        Calendar today = Calendar.getInstance();
        lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
        if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
        }

        lTempban.setTimeInMillis(0);
        return lTempban;
    }

    public Calendar getTempBanCalendar() {
        return tempban;
    }

    public byte getBanReason() {
        return greason;
    }

    public boolean hasBannedIP() {
        boolean ret = false;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')");
            ps.setString(1, getSessionIPAddress());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                ret = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("Error checking ip bans" + ex);
        }
        return ret;
    }

    public String getCharNamebyID(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ?")) {
            ps.setInt(1, this.getAccID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (id == rs.getInt("id")) {
                        return rs.getString("name");
                    }
                }
            }
        }
        return "";
    }

    public String getMacIds() {
        String mac = "";
        for (String m : macs) {
            mac += m;
        }
        return mac;

    }

    public final Set<String> getMacs() {
        return Collections.unmodifiableSet(macs);
    }

    public boolean hasBannedMac() {
        if (macs.isEmpty()) {
            return false;
        }
        boolean ret = false;
        int i = 0;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
            for (i = 0; i < macs.size(); i++) {
                sql.append("?");
                if (i != macs.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                i = 0;
                for (String mac : macs) {
                    i++;
                    ps.setString(i, mac);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
            if (ret && getAccID() != -1) {
                try (PreparedStatement ps2 = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?")) {
                    ps2.setInt(1, 1);
                    ps2.setString(2, "macban");
                    ps2.setInt(3, getAccID());
                    ps2.execute();
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error checking mac bans" + ex);
        }

        return ret;
    }

    private void loadMacsIfNescessary() throws SQLException {
        if (macs.isEmpty()) {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?");
                ps.setInt(1, accId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("macs") != null) {
                        String[] macData = rs.getString("macs").split(", ");
                        for (String mac : macData) {
                            if (!mac.equals("")) {
                                macs.add(mac);
                            }
                        }
                    }
                } else {
                    rs.close();
                    ps.close();
                    throw new RuntimeException("No valid account associated with this client.");
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                System.err.println("Error getting character default" + e);
            }
        }
    }

    public void banMacs() {
        try {
            loadMacsIfNescessary();
            if (!this.macs.isEmpty()) {
                String[] macBans = new String[this.macs.size()];
                int z = 0;
                for (String mac : this.macs) {
                    if (!mac.equals("00-00-00-00-00-00")) {
                        macBans[z] = mac;
                        z++;
                    }
                }
                banMacs(macBans);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final void banMacs(String[] macs) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            List<String> filtered = new LinkedList<String>();
            PreparedStatement ps = con.prepareStatement("SELECT filter FROM macfilters");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                filtered.add(rs.getString("filter"));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
            for (String mac : macs) {
                boolean matched = false;
                for (String filter : filtered) {
                    if (mac.matches(filter)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    ps.setString(1, mac);
                    try {
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        // can fail because of UNIQUE key, we dont care
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error banning MACs" + e);
        }
    }

    /**
     * Returns 0 on success, a state to be used for
     * {@link CField#getLoginFailed(int)} otherwise.
     *
     * @param success
     * @return The state of the login.
     */
    public int finishLogin() {
        login_mutex.lock();
        try {
            updateLoginState(MapleClient.LOGIN_LOGGEDIN, getSessionIPAddress());
        } finally {
            login_mutex.unlock();
        }
        return 0;
    }

    public void clearInformation() {
        accountName = null;
        accId = -1;
        secondPassword = null;
        salt2 = null;
        gm = false;
        loggedIn = false;
        greason = (byte) 1;
        tempban = null;
        gender = (byte) -1;
    }

    public int login(String login, String pwd, boolean ipMacBanned) {
        int loginok = 5;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?")) {
                ps.setString(1, login);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        final int banned = rs.getInt("banned");
                        final String passhash = rs.getString("password");
                        final String salt = rs.getString("salt");
                        final String oldSession = rs.getString("SessionIP");
                        boolean isReal = false;

                        accountName = login;
                        accId = rs.getInt("id");
                        secondPassword = rs.getString("2ndpassword");
                        salt2 = rs.getString("salt2");
                        gm = rs.getInt("gm") > 0;
                        greason = rs.getByte("greason");
                        tempban = getTempBanCalendar(rs);
                        gender = rs.getByte("gender");

                        final boolean admin = rs.getInt("gm") > 1;

                        if (secondPassword != null && salt2 != null) {
                            secondPassword = LoginCrypto.rand_r(secondPassword);
                        }
                        ps.close();

                        if (banned > 0 && !gm) {
                            loginok = 3;
                        } else {
                            if (banned == -1) {
                                unban();
                            }
                            byte loginstate = getLoginState();
                            boolean updatePasswordHash = false;
                            // Check if the passwords are correct here. :B
                            if (passhash == null || passhash.isEmpty()) {
                                //match by sessionIP
                                if (oldSession != null && !oldSession.isEmpty()) {
                                    loggedIn = getSessionIPAddress().equals(oldSession);
                                    loginok = loggedIn ? 0 : 4;
                                    updatePasswordHash = loggedIn;
                                } else {
                                    loginok = 4;
                                    loggedIn = false;
                                }
                            } else if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
                                // Check if a password upgrade is needed.
                                loginok = 0;
                                updatePasswordHash = true;
                                isReal = true;
                            } else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
                                loginok = 0;
                                updatePasswordHash = true;
                                isReal = true;
                            } else if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
                                loginok = 0;
                                isReal = true;
                                hash = passhash;
                            } else {
                                loggedIn = false;
                                loginok = 4;
                            }
                            if (updatePasswordHash) {
                                PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?");
                                try {
                                    final String newSalt = LoginCrypto.makeSalt();
                                    pss.setString(1, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
                                    pss.setString(2, newSalt);
                                    pss.setInt(3, accId);
                                    pss.executeUpdate();
                                } finally {
                                    pss.close();
                                }
                            }
                            //hash = LoginCrypto.makeSaltedSha512Hash(pwd, newSalt);
                            if (!scanChar(accId)) {
                                loginok = 6;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR" + e);
        }
        return loginok;
    }

    public boolean CheckSecondPassword(String in) {
        boolean allow = false;
        boolean updatePasswordHash = false;

        // Check if the passwords are correct here. :B
        if (LoginCryptoLegacy.isLegacyPassword(secondPassword) && LoginCryptoLegacy.checkPassword(in, secondPassword)) {
            // Check if a password upgrade is needed.
            allow = true;
            updatePasswordHash = true;
        } else if (salt2 == null && LoginCrypto.checkSha1Hash(secondPassword, in)) {
            allow = true;
            updatePasswordHash = true;
        } else if (LoginCrypto.checkSaltedSha512Hash(secondPassword, in, salt2)) {
            allow = true;
        }
        if (updatePasswordHash) {
            if (updatePasswordHash) {
                try (Connection con = DatabaseConnection.getPlayerConnection()) {
                    PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?");
                    final String newSalt = LoginCrypto.makeSalt();
                    ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(in, newSalt)));
                    ps.setString(2, newSalt);
                    ps.setInt(3, accId);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    return false;
                }
            }
        }
        return allow;
    }

    private void unban() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?");
            ps.setInt(1, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
        }
    }

    public static final byte unban(String charname) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?");
            ps.setInt(1, accid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
            return -2;
        }
        return 0;
    }

    public void updateMacs(String macData) {
        for (String mac : macData.split(", ")) {
            macs.add(mac);
        }
        StringBuilder newMacData = new StringBuilder();
        Iterator<String> iter = macs.iterator();
        while (iter.hasNext()) {
            newMacData.append(iter.next());
            if (iter.hasNext()) {
                newMacData.append(", ");
            }
        }
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
            ps.setString(1, newMacData.toString());
            ps.setInt(2, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error saving MACs" + e);
        }
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public int getAccID() {
        return this.accId;
    }

    public final void updateLoginState(final int newstate, final String SessionID) { // TODO hide?
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
            ps.setInt(1, newstate);
            ps.setString(2, SessionID);
            ps.setInt(3, getAccID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("error updating login state" + e);
        }
        if (newstate == MapleClient.LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
        } else {
            serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION || newstate == MapleClient.CHANGE_CHANNEL);
            loggedIn = !serverTransition;
        }
    }

    public final void updateLoginState(final int newstate, final String SessionID, final int accid) { // TODO hide?
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
            ps.setInt(1, newstate);
            ps.setString(2, SessionID);
            ps.setInt(3, accid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("error updating login state" + e);
        }
        if (newstate == MapleClient.LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
        } else {
            serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION || newstate == MapleClient.CHANGE_CHANNEL);
            loggedIn = !serverTransition;
        }
    }

    public final void updateLoginState(final int newstate, final String SessionID, final int accid, boolean shutdown) { // TODO hide?
        if (!shutdown) {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
                ps.setInt(1, newstate);
                ps.setString(2, SessionID);
                ps.setInt(3, accid);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                System.err.println("error updating login state" + e);
            }
        }
        if (newstate == MapleClient.LOGIN_NOTLOGGEDIN) {
            loggedIn = false;
            serverTransition = false;
        } else {
            serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION || newstate == MapleClient.CHANGE_CHANNEL);
            loggedIn = !serverTransition;
        }
        lockAccount(0);
        engines.clear();
    }

    public final void updateSecondPassword() {
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?")) {
            final String newSalt = LoginCrypto.makeSalt();
            ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(secondPassword, newSalt)));
            ps.setString(2, newSalt);
            ps.setInt(3, accId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("error updating login state" + e);
        }
    }

    public final byte getLoginState() { // TODO hide?
        if (getAccID() < 0) {
            return -1;
        } else {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps;
                ps = con.prepareStatement("SELECT loggedin, lastlogin, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?");
                ps.setInt(1, getAccID());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    ps.close();
                    rs.close();
                    session.close();
                    throw new DatabaseException("Account doesn't exist");
                }
                birthday = rs.getInt("bday");
                byte state = rs.getByte("loggedin");

                if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
                    if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) { // connecting to chanserver timeout
                        state = MapleClient.LOGIN_NOTLOGGEDIN;
                        updateLoginState(state, getSessionIPAddress());
                    }
                }
                rs.close();
                ps.close();
                if (state == MapleClient.LOGIN_LOGGEDIN) {
                    loggedIn = true;
                } else {
                    loggedIn = false;
                }
                return state;
            } catch (SQLException e) {
                loggedIn = false;
                throw new DatabaseException("error getting login state", e);
            }
        }
    }

    public final byte getLoginState(int accid) { // TODO hide?
        if (accid < 0) {
            return -1;
        } else {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps;
                ps = con.prepareStatement("SELECT loggedin, lastlogin, banned, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?");
                ps.setInt(1, accid);
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || rs.getInt("banned") > 0) {
                    ps.close();
                    rs.close();
                    session.close();
                    throw new DatabaseException("Account doesn't exist or is banned");
                }
                birthday = rs.getInt("bday");
                byte state = rs.getByte("loggedin");

                if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
                    if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) { // connecting to chanserver timeout
                        state = MapleClient.LOGIN_NOTLOGGEDIN;
                        updateLoginState(state, getSessionIPAddress(), accid);
                    }
                }
                rs.close();
                ps.close();
                if (state == MapleClient.LOGIN_LOGGEDIN) {
                    loggedIn = true;
                } else {
                    loggedIn = false;
                }
                return state;
            } catch (SQLException e) {
                loggedIn = false;
                throw new DatabaseException("error getting login state", e);
            }
        }
    }

    public final boolean checkBirthDate(final int date) {
        return birthday == date;
    }

    public final void removalTask(boolean shutdown) {
        try {
            player.cancelAllBuffs_();
            player.cancelAllDebuffs();
            if (player.getMarriageId() > 0) {
                final MapleQuestStatus stat1 = player.getQuestNoAdd(MapleQuest.getInstance(160001));
                final MapleQuestStatus stat2 = player.getQuestNoAdd(MapleQuest.getInstance(160002));
                if (stat1 != null && stat1.getCustomData() != null && (stat1.getCustomData().equals("2_") || stat1.getCustomData().equals("2"))) {
                    //dc in process of marriage
                    if (stat2 != null && stat2.getCustomData() != null) {
                        stat2.setCustomData("0");
                    }
                    stat1.setCustomData("3");
                }
            }
            if (player.getMapId() == GameConstants.JAIL && !player.isIntern()) {
                final MapleQuestStatus stat1 = player.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_TIME));
                final MapleQuestStatus stat2 = player.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST));
                if (stat1.getCustomData() == null) {
                    stat1.setCustomData(String.valueOf(System.currentTimeMillis()));
                } else if (stat2.getCustomData() == null) {
                    stat2.setCustomData("0"); //seconds of jail
                } else { //previous seconds - elapsed seconds
                    int seconds = Integer.parseInt(stat2.getCustomData()) - (int) ((System.currentTimeMillis() - Long.parseLong(stat1.getCustomData())) / 1000);
                    if (seconds < 0) {
                        seconds = 0;
                    }
                    stat2.setCustomData(String.valueOf(seconds));
                }
            }
            player.changeRemoval(true);
            if (player.getEventInstance() != null) {
                player.getEventInstance().exitPlayer(player);
            }
            player.setMessenger(null);
            if (player.getMap() != null) {
                if (shutdown || (getChannelServer() != null && getChannelServer().isShutdown())) {
                    int questID = -1;
                    switch (player.getMapId()) {
                        case 240060200: //HT
                            questID = 160100;
                            break;
                        case 240060201: //ChaosHT
                            questID = 160103;
                            break;
                        case 280030000: //Zakum
                            questID = 160101;
                            break;
                        case 280030001: //ChaosZakum
                            questID = 160102;
                            break;
                        case 270050100: //PB
                            questID = 160101;
                            break;
                        case 105100300: //Balrog
                        case 105100400: //Balrog
                            questID = 160106;
                            break;
                        case 211070000: //VonLeon
                        case 211070100: //VonLeon
                        case 211070101: //VonLeon
                        case 211070110: //VonLeon
                            questID = 160107;
                            break;
                        case 551030200: //scartar
                            questID = 160108;
                            break;
                        case 271040100: //cygnus
                            questID = 160109;
                            break;
                    }
                    if (questID > 0) {
                        player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData("0"); //reset the time.
                    }
                } else if (player.isAlive()) {
                    switch (player.getMapId()) {
                        case 541010100: //latanica
                        case 541020800: //krexel
                        case 220080001: //pap
                            player.getMap().addDisconnected(player.getId());
                            break;
                    }
                }
                player.getMap().removePlayer(player, true);
            }
        } catch (final Throwable e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
        }
    }

    public void closeAllChars(int id) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr != null) {
                    if (id == chr.getAccountID()) {
                        System.out.println("Player:  " + chr.getName() + " has been logged off. Possible Dupe attempt.");
                        chr.getClient().forceDisconnect();
                        cserv.getPlayerStorage().deregisterPlayer(chr);
                    }
                }
            }
        }
    }

    public void kick() {
        disconnect(true, false, false);
        getSession().close();
    }

    public void close() {
        disconnect(true, false, true);
        getSession().close();
    }

    public void setShopClose() {
        if (getPlayer() != null) {
            long time = getPlayer().shoptime * 1000;
            getPlayer().offline = true;
            getPlayer().shopEvent = true;
            getPlayer().shoplock = true;
            TimerManager.getInstance().schedule(() -> {

                if (getPlayer() != null && getPlayer().getPlayerShop() != null) {
                    if (getPlayer().shopEvent) {
                        getPlayer().getPlayerShop().closeShop(true);
                    }
                }
            }, time);
        }
    }

    public void unloadChar(final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown) {
        unloadChar(RemoveInChannelServer, fromCS, shutdown, true);

    }

    public void unloadChar(final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown, boolean save) {
        if (this.player != null) {
            this.player.updateStat();
            //System.out.println("test");
            //System.out.println("Dc error from: " + this.getPlayer().getName() + " with following checks: " + RemoveInChannelServer + "-" + fromCS + "-" + shutdown);
            if (this.player.getPlayerShop() != null) {
                //System.out.println("test0a");
                if (this.player.getPlayerShop().isOpen()) {
                    //System.out.println("test1");
                    if (this.player.getPlayerShop().isOwner(this.player)) {
                        //System.out.println("test2");
                        if (!this.player.shopEvent) {
                            //System.out.println("test3");
                            if (!this.player.offline && this.player.shoptime > 0) {
                                //System.out.println("test4a");
                                this.setShopClose();
                                return;
                            } else {
                                //System.out.println("test4b");
                                getPlayer().offline = false;
                                this.player.getPlayerShop().closeShop(true);
                            }
                        } else {
                            //System.out.println("test2b");
                            getPlayer().offline = false;
                            this.player.getPlayerShop().closeShop(true);
                        }
                    } else {
                        if (this.player.getPlayerShop().isAvailable()) {
                            this.player.getPlayerShop().removeVisitor(this.player, 3);
                        }
                    }
                } else {
                    //System.out.println("test1b");
                    getPlayer().offline = false;
                    this.player.getPlayerShop().closeShop(true);
                }
            }
            MapleMap map = this.player.getMap();
            final MapleParty party = this.player.getParty();
            final MapleRaid raid = this.player.getRaid();
            final boolean clone = this.player.isClone();
            final String namez = this.player.getName();
            final int idz = this.player.getId(), messengerid = this.player.getMessenger() == null ? 0 : this.player.getMessenger().getId(), gid = this.player.getGuildId(), fid = this.player.getFamilyId();
            final BuddyList bl = this.player.getBuddylist();
            final MaplePartyCharacter chrp = new MaplePartyCharacter(this.player);
            final MapleMessengerCharacter chrm = new MapleMessengerCharacter(this.player);
            final MapleGuildCharacter chrg = this.player.getMGC();
            final MapleFamilyCharacter chrf = this.player.getMFC();
            //this.player.resetLevelOffline();
            if (!this.player.isBot()) {
                this.player.saveBlackList();
                this.player.saveLevelData();
                this.player.saveOverflow();
                this.player.saveVarData();
                this.player.saveVarAccData();
                this.player.saveDamageSkins();
                if (this.player.getAndroid() != null) {
                    this.player.getAndroid().saveToDb();
                }
            }
            if (this.player.isStorageOpened()) {
                this.player.storageOpen(false);
            }

            if (this.player.slotTask != null) {
                this.player.run = false;
                this.player.slotTask.cancel(true);
                this.player.slotTask = null;
            }

            if (this.player.getRaid() != null) {
                if (this.player.getRaid().getLeader() == this.player) {
                    this.player.getRaid().disbandRaid();
                } else {
                    this.player.getRaid().leaveRaid(this.player);
                }
            }
            if (party != null) {
                if (party.getLeader().getId() == this.player.getId()) {
                    World.Party.updateParty(party.getId(), PartyOperation.DISBAND, chrp);
                } else {
                    World.Party.updateParty(party.getId(), PartyOperation.LEAVE, chrp);
                }
            }
            if (raid != null) {
                if (this.player == raid.getLeader()) {
                    raid.disbandRaid();
                } else {
                    raid.leaveRaid(this.player);
                }
            }
            if (GuildHandler.hasGuildInvitation(this.player)) {
                GuildHandler.cancelInvite(this);
            }
            removalTask(shutdown);
            if (!this.player.isBot()) {
                this.player.saveQuestLocks();
                this.player.saveToDB();
                this.player.getStorage().saveToDB(true);
            }
            this.player.setOnline(false);
            this.player.dispose();
            LoginServer.getLoginAuth(this.player.getId());
            if (shutdown) {
                this.player = null;
                receiving = false;
                return;
            }

            if (!fromCS) {
                final ChannelServer ch = ChannelServer.getInstance(map == null ? channel : map.getChannel());
                final int chz = World.Find.findChannel(idz);
                if (chz < -1) {
                    disconnect(RemoveInChannelServer, true);//u lie
                    return;
                }
                try {
                    if (chz == -1 || ch == null || clone || ch.isShutdown()) {
                        this.player = null;
                        return;//no idea
                    }
                    if (messengerid > 0) {
                        World.Messenger.leaveMessenger(messengerid, chrm);
                    }
                    if (bl != null) {
                        if (!serverTransition) {
                            World.Buddy.loggedOff(namez, idz, channel, bl.getBuddyIds());
                        } else { // Change channel
                            World.Buddy.loggedOn(namez, idz, channel, bl.getBuddyIds());
                        }
                    }
                    if (gid > 0 && chrg != null) {
                        World.Guild.setGuildMemberOnline(chrg, false, -1);
                    }
                    if (fid > 0 && chrf != null) {
                        World.Family.setFamilyMemberOnline(chrf, false, -1);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
                    System.err.println(getLogMessage(this, "ERROR") + e);
                } finally {
                    if (RemoveInChannelServer && ch != null) {
                        ch.removePlayer(this.player);
                    }
                    this.player = null;
                }
            } else {
                final int ch = World.Find.findChannel(idz);
                if (ch > 0) {
                    disconnect(RemoveInChannelServer, false);//u lie
                    return;
                }
                try {
                    if (!serverTransition) {
                        World.Buddy.loggedOff(namez, idz, channel, bl.getBuddyIds());
                    } else { // Change channel
                        World.Buddy.loggedOn(namez, idz, channel, bl.getBuddyIds());
                    }
                    if (gid > 0 && chrg != null) {
                        World.Guild.setGuildMemberOnline(chrg, false, -1);
                    }
                    if (fid > 0 && chrf != null) {
                        World.Family.setFamilyMemberOnline(chrf, false, -1);
                    }
                    if (player != null) {
                        player.setMessenger(null);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
                    System.err.println(getLogMessage(this, "ERROR") + e);
                } finally {
                    if (RemoveInChannelServer && ch > 0) {
                        CashShopServer.getPlayerStorage().deregisterPlayer(idz, namez);
                    }
                }
            }
        }
    }

    public void unloadBot(MapleCharacter bot, final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown, boolean save) {
        if (bot != null) {
            //System.out.println("test");
            //System.out.println("Dc error from: " + this.getPlayer().getName() + " with following checks: " + RemoveInChannelServer + "-" + fromCS + "-" + shutdown);
            MapleMap map = bot.getMap();
            final MapleParty party = bot.getParty();
            final MapleRaid raid = bot.getRaid();
            final boolean clone = bot.isClone();
            final String namez = bot.getName();
            final int idz = bot.getId(), messengerid = bot.getMessenger() == null ? 0 : bot.getMessenger().getId(), gid = bot.getGuildId(), fid = bot.getFamilyId();
            final BuddyList bl = bot.getBuddylist();
            final MaplePartyCharacter chrp = new MaplePartyCharacter(bot);
            final MapleMessengerCharacter chrm = new MapleMessengerCharacter(bot);
            final MapleGuildCharacter chrg = bot.getMGC();
            final MapleFamilyCharacter chrf = bot.getMFC();
            //this.player.resetLevelOffline();
            if (bot.getRaid() != null) {
                if (bot.getRaid().getLeader() == bot) {
                    bot.getRaid().disbandRaid();
                } else {
                    bot.getRaid().leaveRaid(this.player);
                }
            }
            if (party != null) {
                if (party.getLeader().getId() == bot.getId()) {
                    World.Party.updateParty(party.getId(), PartyOperation.DISBAND, chrp);
                } else {
                    World.Party.updateParty(party.getId(), PartyOperation.LEAVE, chrp);
                }
            }
            if (raid != null) {
                if (bot == raid.getLeader()) {
                    raid.disbandRaid();
                } else {
                    raid.leaveRaid(bot);
                }
            }
            if (GuildHandler.hasGuildInvitation(bot)) {
                GuildHandler.cancelInvite(this);
            }
            removalTask(shutdown);
            bot.setOnline(false);
            bot.dispose();
            LoginServer.getLoginAuth(bot.getId());

            if (shutdown) {
                bot = null;
                receiving = false;
                return;
            }

            if (!fromCS) {
                final ChannelServer ch = ChannelServer.getInstance(map == null ? channel : map.getChannel());
                final int chz = World.Find.findChannel(idz);
                if (chz < -1) {
                    disconnect(RemoveInChannelServer, true);//u lie
                    return;
                }
                try {
                    if (chz == -1 || ch == null || clone || ch.isShutdown()) {
                        this.player = null;
                        return;//no idea
                    }
                    if (messengerid > 0) {
                        World.Messenger.leaveMessenger(messengerid, chrm);
                    }
                    if (bl != null) {
                        if (!serverTransition) {
                            World.Buddy.loggedOff(namez, idz, channel, bl.getBuddyIds());
                        } else { // Change channel
                            World.Buddy.loggedOn(namez, idz, channel, bl.getBuddyIds());
                        }
                    }
                    if (gid > 0 && chrg != null) {
                        World.Guild.setGuildMemberOnline(chrg, false, -1);
                    }
                    if (fid > 0 && chrf != null) {
                        World.Family.setFamilyMemberOnline(chrf, false, -1);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
                    System.err.println(getLogMessage(this, "ERROR") + e);
                } finally {
                    if (RemoveInChannelServer && ch != null) {
                        ch.removePlayer(this.player);
                    }
                    this.player = null;
                }
            } else {
                final int ch = World.Find.findChannel(idz);
                if (ch > 0) {
                    disconnect(RemoveInChannelServer, false);//u lie
                    return;
                }
                try {
                    if (!serverTransition) {
                        World.Buddy.loggedOff(namez, idz, channel, bl.getBuddyIds());
                    } else { // Change channel
                        World.Buddy.loggedOn(namez, idz, channel, bl.getBuddyIds());
                    }
                    if (gid > 0 && chrg != null) {
                        World.Guild.setGuildMemberOnline(chrg, false, -1);
                    }
                    if (fid > 0 && chrf != null) {
                        World.Family.setFamilyMemberOnline(chrf, false, -1);
                    }
                    if (player != null) {
                        player.setMessenger(null);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    FileoutputUtil.outputFileError(FileoutputUtil.Acc_Stuck, e);
                    System.err.println(getLogMessage(this, "ERROR") + e);
                } finally {
                    if (RemoveInChannelServer && ch > 0) {
                        CashShopServer.getPlayerStorage().deregisterPlayer(idz, namez);
                    }
                }
            }
        }
    }

    public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS) {
        disconnect(RemoveInChannelServer, fromCS, false);
    }

    public final void disconnect(final boolean RemoveInChannelServer, final boolean fromCS, final boolean shutdown) {
        if (getAccID() > 0) {
            if (this.getPlayer() != null) {
                unloadChar(RemoveInChannelServer, fromCS, shutdown);
            }
            if (getLoginState() != 1) {
                TimerManager.getInstance().schedule(() -> {

                    if (getLoginState() == 2 && getlockAccount() > 0) {
                        updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, getSessionIPAddress(), getAccID(), shutdown);
                    }
                    if (getLoginState() == 1 && getlockAccount() > 0) {
                        updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, getSessionIPAddress(), getAccID(), shutdown);
                    }
                    if (getLoginState() == 4 && getlockAccount() > 0 && scanChar(getAccID())) {
                        updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, getSessionIPAddress(), getAccID(), shutdown);
                    }
                }, 5000);
            }
        }
    }

    public final String getSessionIPAddress() {
        //System.out.println(session.getRemoteAddress().toString().split(":")[0]);
        return ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
    }

    public final boolean CheckIPAddress() {
        if (this.accId < 0) {
            return false;
        }
        boolean canlogin = false;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT SessionIP, banned FROM accounts WHERE id = ?");
            ps.setInt(1, this.accId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                final String sessionIP = rs.getString("SessionIP");

                if (sessionIP != null) { // Probably a login proced skipper?
                    canlogin = getSessionIPAddress().equals(sessionIP.split(":")[0]);
                }
                if (rs.getInt("banned") > 0) {
                    canlogin = false; //canlogin false = close client
                }
            }
            rs.close();
            ps.close();

        } catch (final SQLException e) {
            System.out.println("Failed in checking IP address for client.");
        }
        return canlogin;
    }

    public final void DebugMessage(final StringBuilder sb) {
        sb.append(getSession().getRemoteAddress());
        sb.append("Connected: ");
        sb.append(getSession().isConnected());
        sb.append(" Closing: ");
        sb.append(getSession().isClosing());
        sb.append(" ClientKeySet: ");
        sb.append(getSession().getAttribute(MapleClient.CLIENT_KEY) != null);
        sb.append(" loggedin: ");
        sb.append(isLoggedIn());
        sb.append(" has char: ");
        sb.append(getPlayer() != null);
    }

    public final int getChannel() {
        return channel;
    }

    public final ChannelServer getChannelServer() {
        return ChannelServer.getInstance(channel);
    }

    public final int deleteCharacter(final int cid) {
        if (getPlayer() != null && getPlayer().getOnline()) {
            getSession().close();
            return 10;
        }
        try {
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?");
                ps.setInt(1, cid);
                ps.setInt(2, accId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    return 9;
                }
                if (rs.getInt("guildid") > 0) { // is in a guild when deleted
                    if (rs.getInt("guildrank") == 1) { //cant delete when leader
                        rs.close();
                        ps.close();
                        return 22;
                    }
                    World.Guild.deleteGuildCharacter(rs.getInt("guildid"), cid);
                }
                if (rs.getInt("familyid") > 0 && World.Family.getFamily(rs.getInt("familyid")) != null) {
                    World.Family.getFamily(rs.getInt("familyid")).leaveFamily(cid);
                }
                rs.close();
                ps.close();
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mts_cart WHERE characterid = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mts_items WHERE characterid = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM cheatlog WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryequips WHERE characterid = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM dueypackages WHERE RecieverId = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE buddyid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hyperrocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM extendedSlots WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM event WHERE charid = ?", cid);
                return 0;
            } catch (Exception e) {
                FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 10;
    }

    public final void GMdeleteCharacter(final int cid, int accid, String accName) {
        try {
            updateGuildStuff(cid, accid);
            try (Connection con = DatabaseConnection.getPlayerConnection()) {
                System.out.println("Account: " + accName + " was banned and erased: " + getCharNamebyID(con, cid) + " by admin.");
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "UPDATE pokemon SET active = 0 WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mts_cart WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mts_items WHERE characterid = ?", cid);
                //MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM cheatlog WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM dueypackages WHERE RecieverId = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE buddyid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hyperrocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
                MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM extendedSlots WHERE characterid = ?", cid);
            } catch (Exception e) {
                FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateGuildStuff(int cid, int accid) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, accid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return;
            }
            if (rs.getInt("guildid") > 0) { // is in a guild when deleted
                if (rs.getInt("guildrank") == 1) { //cant delete when leader
                    rs.close();
                    ps.close();
                    return;
                }
                World.Guild.deleteGuildCharacter(rs.getInt("guildid"), cid);
            }
            if (rs.getInt("familyid") > 0 && World.Family.getFamily(rs.getInt("familyid")) != null) {
                World.Family.getFamily(rs.getInt("familyid")).leaveFamily(cid);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }
    }

    public final byte getGender() {
        return gender;
    }

    public final void setGender(final byte gender) {
        this.gender = gender;
    }

    public final String getSecondPassword() {
        return secondPassword;
    }

    public final void setSecondPassword(final String secondPassword) {
        this.secondPassword = secondPassword;
    }

    public final String getAccountName() {
        return accountName;
    }

    public final void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public final void setChannel(final int channel) {
        this.channel = channel;
    }

    public final int getWorld() {
        return world;
    }

    public final void setWorld(final int world) {
        this.world = world;
    }

    public final int getLatency() {
        return (int) (delay);
    }

    public final long getLastPong() {
        return lastPong;
    }

    public final long getLastPing() {
        return lastPing;
    }

    public final void pongReceived() {
        spike = 0;
        delay = System.currentTimeMillis() - ping;
    }

    public final void sendPing() {
        ping = System.currentTimeMillis();
        //session.write(LoginPacket.getPing());
    }

    public final void senderPing() {
        if (getPlayer() != null && !getPlayer().isChangingMaps()) {
            if (spike > 10) {
                getPlayer().kick();
            }
            spike++;
            ping = System.currentTimeMillis();
            session.write(LoginPacket.getPing());
        }
    }

    public static final String getLogMessage(final MapleClient cfor, final String message) {
        return getLogMessage(cfor, message, new Object[0]);
    }

    public static final String getLogMessage(final MapleCharacter cfor, final String message) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message);
    }

    public static final String getLogMessage(final MapleCharacter cfor, final String message, final Object... parms) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
    }

    public static final String getLogMessage(final MapleClient cfor, final String message, final Object... parms) {
        final StringBuilder builder = new StringBuilder();
        if (cfor != null) {
            if (cfor.getPlayer() != null) {
                builder.append("<");
                builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
                builder.append(" (cid: ");
                builder.append(cfor.getPlayer().getId());
                builder.append(")> ");
            }
            if (cfor.getAccountName() != null) {
                builder.append("(Account: ");
                builder.append(cfor.getAccountName());
                builder.append(") ");
            }
        }
        builder.append(message);
        int start;
        for (final Object parm : parms) {
            start = builder.indexOf("{}");
            builder.replace(start, start + 2, parm.toString());
        }
        return builder.toString();
    }

    public static final int findAccIdForCharacterName(final String charName) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, charName);
            ResultSet rs = ps.executeQuery();

            int ret = -1;
            if (rs.next()) {
                ret = rs.getInt("accountid");
            }
            rs.close();
            ps.close();

            return ret;
        } catch (final SQLException e) {
            System.err.println("findAccIdForCharacterName SQL error");
        }
        return -1;
    }

    public final boolean isGm() {
        return gm;
    }

    public final void setScriptEngine(final String name, final ScriptEngine e) {
        engines.put(name, e);
    }

    public final ScriptEngine getScriptEngine(final String name) {
        return engines.get(name);
    }

    public final void removeScriptEngine(final String name) {
        engines.remove(name);
    }

    public final ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public final void setIdleTask(final ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    protected static final class CharNameAndId {

        public final String name;
        public final int id;

        public CharNameAndId(final String name, final int id) {
            super();
            this.name = name;
            this.id = id;
        }
    }

    public int getCharacterSlots() {
        if (isGm()) {
            return 15;
        }
        if (charslots != DEFAULT_CHARSLOT) {
            return charslots; //save a sql
        }
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?");
            ps.setInt(1, accId);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                charslots = rs.getInt("charslots");
            } else {
                PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)");
                psu.setInt(1, accId);
                psu.setInt(2, world);
                psu.setInt(3, charslots);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }

        return charslots;
    }

    public boolean gainCharacterSlot() {
        if (getCharacterSlots() >= 15) {
            return false;
        }
        charslots++;
        if (getPlayer() != null) {
            getPlayer().dropMessage("You have gained an additional character slot. You now have " + charslots + " available.");
        }
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?");
            ps.setInt(1, charslots);
            ps.setInt(2, world);
            ps.setInt(3, accId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
            return false;
        }
        return true;
    }

    public static final byte unbanIPMacs(String charname) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final String sessionIP = rs.getString("sessionIP");
            final String macs = rs.getString("macs");
            rs.close();
            ps.close();
            byte ret = 0;
            if (sessionIP != null) {
                PreparedStatement psa = con.prepareStatement("DELETE FROM ipbans WHERE ip like ?");
                psa.setString(1, sessionIP);
                psa.execute();
                psa.close();
                ret++;
            }
            if (macs != null) {
                String[] macz = macs.split(", ");
                for (String mac : macz) {
                    if (!mac.equals("")) {
                        PreparedStatement psa = con.prepareStatement("DELETE FROM macbans WHERE mac = ?");
                        psa.setString(1, mac);
                        psa.execute();
                        psa.close();
                    }
                }
                ret++;
            }
            return ret;
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
            return -2;
        }
    }

    public static final byte unHellban(String charname) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid from characters where name = ?");
            ps.setString(1, charname);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final int accid = rs.getInt(1);
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            final String sessionIP = rs.getString("sessionIP");
            final String email = rs.getString("email");
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE email = ?" + (sessionIP == null ? "" : " OR sessionIP = ?"));
            ps.setString(1, email);
            if (sessionIP != null) {
                ps.setString(2, sessionIP);
            }
            ps.execute();
            ps.close();
            return 0;
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
            return -2;
        }
    }

    public final void lockAccount(int value) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            if (value == 0) {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET templogin = 0 where id = ?");
                ps.setInt(1, getAccID());
                ps.execute();
                ps.close();
            }
            if (value == 1) {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET templogin = 1 where id = ?");
                ps.setInt(1, getAccID());
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
        }
    }

    public final void setTempId(int value) {
        tempid = value;
    }

    public final int getlockAccount() {
        int value = 0;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, getAccID());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                value = rs.getInt("templogin");
            }
            rs.close();
            ps.close();
            return value;
        } catch (SQLException e) {
            System.err.println("Error while unbanning" + e);
            return 0;
        }
    }

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean m) {
        this.monitored = m;
    }

    public boolean isReceiving() {
        return receiving;
    }

    public void setReceiving(boolean m) {
        this.receiving = m;
    }

    public boolean canClickNPC() {
        return lastNpcClick + 100 < System.currentTimeMillis();
    }

    public void setClickedNPC() {
        lastNpcClick = System.currentTimeMillis();
    }

    public void removeClickedNPC() {
        lastNpcClick = 0;
    }

    public final Timestamp getCreated() { // TODO hide?
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT createdat FROM accounts WHERE id = ?");
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            Timestamp ret = rs.getTimestamp("createdat");
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException e) {
            throw new DatabaseException("error getting create", e);
        }
    }

    public String getTempIP() {
        return tempIP;
    }

    public void setTempIP(String s) {
        this.tempIP = s;
    }

    public boolean isLocalhost() {
        return ServerConstants.Use_Localhost;
    }

    public String getHWID() {
        return hwid;
    }

    public void setHWID(String hwid) {
        this.hwid = hwid;
    }

    public boolean checkChar(int accid) {//kaotic
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr != null && accid == chr.getAccountID()) {
                    System.out.println("Player:  " + chr.getName() + " has been removed from world. Possible Dupe attempt.");
                    chr.kick(); //give_buff with no data :D
                    return false;
                }
            }
        }
        return true;
    }

    public boolean scanChar(int accid) {//kaotic
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr != null && accid == chr.getAccountID()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void kickChar(int accid) {//kaotic
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr != null && accid == chr.getAccountID() && hash.equals(chr.getHash())) {
                    if (chr != null && chr.getOffline()) {
                        chr.kick();
                    }
                    break;
                }
            }
        }
    }

    public AbstractPlayerInteraction getAbstractPlayerInteraction() {
        return new AbstractPlayerInteraction(this);
    }

    public String getNibbleHWID() {
        return (String) session.getAttribute(MapleClient.CLIENT_NIBBLEHWID);
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public NPCConversationManager getNpcCM() {
        return NPCScriptManager.getInstance().getCM(this);
    }

    public void closePlayerScriptInteractions() {
        this.removeClickedNPC();
        NPCScriptManager.getInstance().dispose(this);
    }

    public void processCharBot(MapleCharacter player, ChannelServer channelServer, boolean first) {
        player.setOnline(true);
        player.setBot(true);
        player.loadVarAccData();
        player.loadVarData();
        player.loadLevelData();
        player.loadBonusStat();
        player.setReborns();
        player.getAchieveStat(false);
        player.getQuestStat(false);
        player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));
        player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));
        player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getId()));
        announce(CField.getCharInfo(player));
        //announce(CField.getCharInfo(player));//custom stat update
        //announce(MTSCSPacket.enableCSUse());
        //player.recalcLocalStats();
        /*if (player.isGM()) {
         SkillFactory.getSkill(GameConstants.GMS ? 9101004 : 9001004).getEffect(1).applyTo(player);
         }*/
        announce(CWvsContext.temporaryStats_Reset()); //
        try {
            // Start of buddylist
            final int buddyIds[] = player.getBuddylist().getBuddyIds();
            World.Buddy.loggedOn(player.getName(), player.getId(), getChannel(), buddyIds);
            if (player.getParty() != null) {
                final MapleParty party = player.getParty();
                if (party != null && party.getExpeditionId() > 0) {
                    World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
                    final MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                    if (me != null) {
                        announce(CWvsContext.ExpeditionPacket.expeditionStatus(me, false));
                    }
                }
            }
            final CharacterIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            announce(CWvsContext.BuddylistPacket.updateBuddylist(player.getBuddylist().getBuddies()));

            // Start of Messenger
            final MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(player));
                World.Messenger.updateMessenger(messenger.getId(), player.getName(), getChannel());
            }

            // Start of Guild and alliance
            if (player.getGuildId() > 0) {
                World.Guild.setGuildMemberOnline(player.getMGC(), true, getChannel());
                announce(CWvsContext.GuildPacket.showGuildInfo(player));
                final MapleGuild gs = World.Guild.getGuild(player.getGuildId());
                if (gs != null) {
                    final List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                    if (packetList != null) {
                        for (byte[] pack : packetList) {
                            if (pack != null) {
                                announce(pack);
                            }
                        }
                    }
                } else { //guild not found, change guild id
                    player.setGuildId(0);
                    player.setGuildRank((byte) 5);
                    player.setAllianceRank((byte) 5);
                    player.saveGuildStatus();
                }
            }

            if (player.getFamilyId() > 0) {
                World.Family.setFamilyMemberOnline(player.getMFC(), true, getChannel());
            }
            announce(CWvsContext.FamilyPacket.getFamilyData());
            announce(CWvsContext.FamilyPacket.getFamilyInfo(player));

        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
        }
        //player.getClient().announce(CWvsContext.serverMessage(channelServer.getServerMessage()));
        player.sendMacros();
        player.showNote();
        player.sendImp();
        player.updatePartyMemberHP();
        player.startFairySchedule(false);
        player.updatePetAuto();
        player.expirationTask(true, true);
        player.spawnSavedPets();
        if (player.getStat().equippedSummon > 0) {
            SkillFactory.getSkill(player.getStat().equippedSummon).getEffect(1).applyTo(player);
        }
        player.loadQuests(this);
        announce(CField.NPCPacket.setNPCScriptable(ScriptableNPCConstants.SCRIPTABLE_NPCS));
        if (player.getGuild() != null) {
            player.finishAchievement(35);
        }
        player.loadPals();
        player.loadMaps();
        player.checkQuest();
        player.checkLevels();
        player.loadKeys();
        player.loadMacros();
        player.loadEventScore();
        player.loadBuffs();
        player.loadQuestLocks();
        player.loadQuestPool();
        player.loadShopItems();
        player.loadOverflow();
        player.loadBlackList();
        player.loadStat();
        player.getLink();
        player.enableHelper();
        player.getExpLevel();
        player.loadItemsStorage();
        player.baseSkills(); //fix people who've lost skills.
        if (player.getVar("skin") > 0) {
            player.changeSkin((int) player.getVar("skin"));
        }
        if (!player.isGM() && player.getTotalLevel() > player.getMaxLevel()) {
            player.setTotalLevel(player.getMaxLevel());
        }
        //player.saveToDB(false, false);
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11) != null) {
            player.weaponType = GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11).getItemId()).getMaxDamageMultiplier();
        }
        //player.dropMessage(5, "Link stats have been applied.");
        player.getMap().addPlayer(player, true);
        //senderPing();
        player.canUseJob();
        player.updateJobTier();
        announce(CField.customMainStatUpdate(player));
        announce(CField.customStatDetail(player));
    }

    public void processChar(MapleCharacter player, ChannelServer channelServer, boolean first) {
        player.setOnline(true);
        player.loadVarAccData();
        player.loadVarData();
        player.loadLevelData();
        player.loadBonusStat();
        player.setReborns();
        player.getAchieveStat(false);
        player.getQuestStat(false);
        player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));
        player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));
        player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getId()));
        announce(CField.getCharInfo(player));
        //announce(CField.getCharInfo(player));//custom stat update
        announce(MTSCSPacket.enableCSUse());
        //player.recalcLocalStats();
        /*if (player.isGM()) {
         SkillFactory.getSkill(GameConstants.GMS ? 9101004 : 9001004).getEffect(1).applyTo(player);
         }*/
        announce(CWvsContext.temporaryStats_Reset()); //
        try {
            // Start of buddylist
            final int buddyIds[] = player.getBuddylist().getBuddyIds();
            World.Buddy.loggedOn(player.getName(), player.getId(), getChannel(), buddyIds);
            if (player.getParty() != null) {
                final MapleParty party = player.getParty();
                if (party != null && party.getExpeditionId() > 0) {
                    World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
                    final MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                    if (me != null) {
                        announce(CWvsContext.ExpeditionPacket.expeditionStatus(me, false));
                    }
                }
            }
            final CharacterIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            announce(CWvsContext.BuddylistPacket.updateBuddylist(player.getBuddylist().getBuddies()));

            // Start of Messenger
            final MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(player));
                World.Messenger.updateMessenger(messenger.getId(), player.getName(), getChannel());
            }

            // Start of Guild and alliance
            if (player.getGuildId() > 0) {
                World.Guild.setGuildMemberOnline(player.getMGC(), true, getChannel());
                announce(CWvsContext.GuildPacket.showGuildInfo(player));
                final MapleGuild gs = World.Guild.getGuild(player.getGuildId());
                if (gs != null) {
                    final List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                    if (packetList != null) {
                        for (byte[] pack : packetList) {
                            if (pack != null) {
                                announce(pack);
                            }
                        }
                    }
                } else { //guild not found, change guild id
                    player.setGuildId(0);
                    player.setGuildRank((byte) 5);
                    player.setAllianceRank((byte) 5);
                    player.saveGuildStatus();
                }
            }

            if (player.getFamilyId() > 0) {
                World.Family.setFamilyMemberOnline(player.getMFC(), true, getChannel());
            }
            announce(CWvsContext.FamilyPacket.getFamilyData());
            announce(CWvsContext.FamilyPacket.getFamilyInfo(player));

        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
        }
        //player.getClient().announce(CWvsContext.serverMessage(channelServer.getServerMessage()));
        player.sendMacros();
        player.showNote();
        player.sendImp();
        player.updatePartyMemberHP();
        player.startFairySchedule(false);
        player.updatePetAuto();
        player.expirationTask(true, true);
        player.spawnSavedPets();
        if (player.getStat().equippedSummon > 0) {
            SkillFactory.getSkill(player.getStat().equippedSummon).getEffect(1).applyTo(player);
        }
        player.loadQuests(this);
        announce(CField.NPCPacket.setNPCScriptable(ScriptableNPCConstants.SCRIPTABLE_NPCS));
        if (player.getGuild() != null) {
            player.finishAchievement(35);
        }
        player.loadPals();
        player.loadMaps();
        player.checkQuest();
        player.checkLevels();
        player.loadKeys();
        player.loadMacros();
        player.loadEventScore();
        player.loadBuffs();
        player.loadQuestLocks();
        player.loadQuestPool();
        player.loadShopItems();
        player.loadOverflow();
        player.loadBlackList();
        player.loadStat();
        player.getLink();
        player.enableHelper();
        player.getExpLevel();
        player.loadItemsStorage();
        player.baseSkills(); //fix people who've lost skills.
        if (player.getVar("skin") > 0) {
            player.changeSkin((int) player.getVar("skin"));
        }
        if (!player.isGM() && player.getTotalLevel() > player.getMaxLevel()) {
            player.setTotalLevel(player.getMaxLevel());
        }
        //player.saveToDB(false, false);
        if (!player.getShopItems().isEmpty()) {
            player.dropMessage(1, "You have " + player.getShopItems().size() + " unclaimed items left in shop storage.\r\nUse @claim to get them\r\nMake Sure you have enough room to claim.");
        }
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11) != null) {
            player.weaponType = GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11).getItemId()).getMaxDamageMultiplier();
        }
        player.dropMessage(5, "Link stats have been applied.");
        player.getMap().addPlayer(player, true);
        senderPing();
        player.canUseJob();
        player.updateJobTier();
        announce(CField.customMainStatUpdate(player));
        announce(CField.customStatDetail(player));
    }

    public void changeCharacter(int id, MapleMap map) {
        try {
            if (getPlayer() != null) {
                unloadChar(true, true, false, true);
            }
            MapleCharacter newChar = MapleCharacter.loadCharFromDB(id, this, true);
            setPlayer(newChar);
            processChar(newChar, getChannelServer(), false);
            //announce(CField.removePlayerFromMap(newChar.getObjectId()));
            newChar.fakeRelog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MapleCharacter> getAllCharacters() {
        Map<Byte, ArrayList<MapleCharacter>> worlds = new HashMap<Byte, ArrayList<MapleCharacter>>();
        List<MapleCharacter> chars = loadCharacters(0); //TODO multi world
        for (MapleCharacter chr : chars) {
            if (chr != null) {
                ArrayList<MapleCharacter> chrr;
                if (!worlds.containsKey(chr.getWorld())) {
                    chrr = new ArrayList<MapleCharacter>();
                    worlds.put(chr.getWorld(), chrr);
                } else {
                    chrr = worlds.get(chr.getWorld());
                }
                chrr.add(chr);
            }
        }
        return chars;
    }
}
