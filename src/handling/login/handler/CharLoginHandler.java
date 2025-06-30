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
package handling.login.handler;

import java.util.List;
import java.util.Calendar;

import client.inventory.Item;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginInformationProvider.JobType;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import handling.world.World;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.Timer.PingTimer;
import tools.packet.CField;
import tools.packet.LoginPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PacketHelper;

public class CharLoginHandler {

    private static boolean lock = false;
    private static Map<Integer, Integer> tempID = new ConcurrentHashMap<Integer, Integer>();

    private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        if (c.loginAttempt > 5) {
            return true;
        }
        return false;
    }

    private static boolean canLogin(final MapleClient c) {
        if (c.getAccID() < 0 || c.getAccountName() == null) {
            return false;
        }
        if (tempID.get(c.getAccID()) != c.tempid) {
            return false;
        }
        if (c.getLoginState() == 0 && c.getlockAccount() == 0) {
            return false;
        }
        if (c.getLoginState() == 1 && c.getlockAccount() == 1) {
            return false;
        }
        if (!c.scanChar(c.getAccID())) {
            return false;
        }
        if (LoginServer.isShutdown()) {
            return false;
        }
        return c.isLoggedIn();
    }

    public static boolean isValidUsername(String name) {

        // Regex to check valid username. 
        String regex = "^[a-zA-Z]\\w{4,13}$";

        // Compile the ReGex 
        Pattern p = Pattern.compile(regex);

        // If the username is empty 
        // return false 
        if (name == null) {
            return false;
        }

        // Pattern class contains matcher() method 
        // to find matching between given username 
        // and regular expression. 
        Matcher m = p.matcher(name);

        // Return if the username 
        // matched the ReGex 
        return m.matches();
    }

    public static boolean getAccountWhiteList(String login) {
        boolean accountExists = false;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountExists = true;
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return accountExists;
    }

    public static boolean checkBan(MapleClient c) {
        return c.hasBannedIP() || c.hasBannedMac();
    }

    public static void timeout(MapleClient c, int min) {
        c.setIdleTask(PingTimer.getInstance().schedule(() -> {
            if (c != null) {
                if (c.getlockAccount() > 0 && c.getLoginState() == MapleClient.LOGIN_LOGGEDIN) {
                    c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, c.getSessionIPAddress());
                    c.lockAccount(0);
                    c.getSession().close();
                }
            }
        }, min * 60 * 1000));
    }

    public static void login(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (LoginServer.isShutdown()) {
            c.getSession().close();
        }
        String login = slea.readMapleAsciiString().trim();
        String pwd = slea.readMapleAsciiString().trim();

        final boolean ipBan = c.hasBannedIP();
        final boolean macBan = c.hasBannedMac();

        int loginok = 5;
        final Calendar tempbannedTill = c.getTempBanCalendar();
        boolean isBanned = c.hasBannedIP() || c.hasBannedMac();
        if (AutoRegister.getAccountExists(login) == true) {
            loginok = c.login(login, pwd, isBanned);
        } else if (AutoRegister.autoRegister == true && !isBanned) {
            if (login.length() <= 13 && isValidUsername(login)) {
                AutoRegister.createAccount(login, pwd, c.getSession().getRemoteAddress().toString());
                if (AutoRegister.success) {
                    loginok = c.login(login, pwd, isBanned);
                } else {
                    c.clearInformation();
                    c.announce(LoginPacket.getLoginFailed(3));
                    return;
                }
            } else {
                c.clearInformation();
                c.announce(LoginPacket.getLoginFailed(3));
                return;
            }
        }

        //if (!c.scanChar(c.getAccID())) {
        //    c.clearInformation();
        //    c.announce(LoginPacket.getLoginFailed(7));
        // }
        if (loginok == 6) {
            //System.out.println("test");
            c.kickChar(c.getAccID());
            c.clearInformation();
            c.announce(LoginPacket.getLoginFailed(10));
            return;
        }

        if (loginok == 0 && (ipBan || macBan) && !c.isGm()) {
            loginok = 3;
            if (macBan) {
                // this is only an ipban o.O" - maybe we should refactor this a bit so it's more readable
                MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false, 4, false);
                c.clearInformation();
                c.announce(LoginPacket.getLoginFailed(7));
                return;
            }
        }

        if (c.getlockAccount() > 0) {
            if (c.getLoginState() > MapleClient.LOGIN_NOTLOGGEDIN) {
                c.clearInformation();
                c.announce(LoginPacket.getLoginFailed(7));
                return;
            }
        } else {
            if (c.getLoginState() > MapleClient.LOGIN_NOTLOGGEDIN) {
                c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, c.getSessionIPAddress());
                c.clearInformation();
                c.announce(LoginPacket.getLoginFailed(7));
                return;
            }
        }
        if (loginok != 0) {
            c.clearInformation();
            c.announce(LoginPacket.getLoginFailed(loginok));
        } else if (tempbannedTill != null && tempbannedTill.getTimeInMillis() != 0) {
            c.clearInformation();
            c.announce(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
        } else {
            if (c.getLoginState() > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
                c.clearInformation();
                c.announce(LoginPacket.getLoginFailed(7));
            } else {
                if (checkBan(c)) {
                    c.clearInformation();
                    c.announce(LoginPacket.getLoginFailed(3));
                } else {
                    c.lockAccount(1);
                    //c.setOnline(true);
                    //c.announce(LoginPacket.getAuthSuccessRequest(c));
                    tempID.put(c.getAccID(), Randomizer.random(1, Integer.MAX_VALUE));
                    c.setTempId(tempID.get(c.getAccID()));
                    LoginWorker.registerClient(c);
                    timeout(c, 10);
                }
            }
            //
            //System.out.println("login: " + c.finishLogin());
            //if (c.finishLogin() == 0) {
            //} else {
            //    c.announce(LoginPacket.getLoginFailed(7));
            //}
        }
    }

    public static void ServerListRequest(final MapleClient c) {
        if (!canLogin(c)) {
            c.getSession().close();
            return;
        }
        //c.announce(LoginPacket.getLoginWelcome());
        c.announce(LoginPacket.getServerList(0, LoginServer.getLoad()));
        //c.announce(CField.getServerList(1, "Scania", LoginServer.getInstance().getChannels(), 1200));
        //c.announce(CField.getServerList(2, "Scania", LoginServer.getInstance().getChannels(), 1200));
        //c.announce(CField.getServerList(3, "Scania", LoginServer.getInstance().getChannels(), 1200));
        c.announce(LoginPacket.getEndOfServerList());
        c.announce(LoginPacket.enableRecommended());
        c.announce(LoginPacket.sendRecommended(0, LoginServer.getEventMessage()));
    }

    public static void ServerStatusRequest(final MapleClient c) {
        if (!canLogin(c)) {
            c.getSession().close();
            return;
        }
        // 0 = Select world normally
        // 1 = "Since there are many users, you may encounter some..."
        // 2 = "The concurrent users in this world have reached the max"
        final int numPlayer = LoginServer.getUsersOn();
        final int userLimit = LoginServer.getUserLimit();
        if (numPlayer >= userLimit) {
            c.announce(LoginPacket.getServerStatus(2));
        } else if (numPlayer * 2 >= userLimit) {
            c.announce(LoginPacket.getServerStatus(1));
        } else {
            c.announce(LoginPacket.getServerStatus(0));
        }
    }

    public static void CharlistRequest(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (!canLogin(c)) {
            c.getSession().close();
            return;
        }
        if (GameConstants.GMS) {
            slea.readByte(); //2?
        }
        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;
        if (!World.isChannelAvailable(channel) || server != 0) { //TODOO: MULTI WORLDS
            c.announce(LoginPacket.getLoginFailed(10)); //cannot process so many
            return;
        }
        //System.out.println("Client " + c.getSession().getRemoteAddress().toString().split(":")[0] + " is connecting to server " + server + " channel " + channel + "");
        final List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorld(server);
            c.setChannel(channel);
            //c.announce(LoginPacket.getSecondAuthSuccess(c));
            c.announce(LoginPacket.getCharList(c.getSecondPassword(), chars, c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }
    }

    public static void CheckCharName(final String name, final MapleClient c) {
        c.announce(LoginPacket.charNameResponse(name, !(MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()))));
    }

    public static void CreateChar(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (!canLogin(c)) {
            c.getSession().close();
            return;
        }
        final String name = slea.readMapleAsciiString();
        //final JobType jobType = JobType.getByType(slea.readInt()); // BIGBANG: 0 = Resistance, 1 = Adventurer, 2 = Cygnus, 3 = Aran, 4 = Evan, 5 = mercedes
        final JobType jobType = JobType.getByType(slea.readInt()); // BIGBANG: 0 = Resistance, 1 = Adventurer, 2 = Cygnus, 3 = Aran, 4 = Evan, 5 = mercedes
        //System.out.println(c.getAccountName() + " created char: " + name + " with type: " + jobType);
        final short db = slea.readShort(); //whether dual blade = 1 or adventurer = 0need to swap 3
        final byte gender = slea.readByte(); //??idk corresponds with the thing in addCharStats
        byte skinColor = slea.readByte(); // 01
        int hairColor = 0;
        final byte unk2 = slea.readByte(); // 08
        final boolean mercedes = (jobType == JobType.Mercedes);
        final boolean demon = (jobType == JobType.Demon);
        final int face = slea.readInt();
        final int hair = slea.readInt();
        if (!mercedes && !demon) { //mercedes/demon dont need hair color since its already in the hair
            hairColor = slea.readInt();
            skinColor = (byte) slea.readInt();
        }
        final int demonMark = demon ? slea.readInt() : 0;
        final int top = slea.readInt();
        final int bottom = (mercedes || demon) ? 0 : slea.readInt();
        final int shoes = slea.readInt();
        final int weapon = slea.readInt();
        final int shield = demon ? slea.readInt() : (mercedes ? 1352000 : 0);
        if (jobType == JobType.Demon) {
            if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, jobType.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, jobType.type, hair)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, jobType.type, demonMark) || (skinColor != 0 && skinColor != 13)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, jobType.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, jobType.type, shoes)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, jobType.type, weapon) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 6, jobType.type, shield)) {
                c.getSession().close();
                return;
            }
        } else if (jobType == JobType.Mercedes) {
            if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, jobType.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, jobType.type, hair)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, jobType.type, top) || (skinColor != 0 && skinColor != 12)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, jobType.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, jobType.type, weapon)) {
                c.getSession().close();
                return;
            }
        } else {
            if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, jobType.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, jobType.type, hair)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, jobType.type, hairColor) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, jobType.type, skinColor)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, jobType.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, jobType.type, bottom)
                    || !LoginInformationProvider.getInstance().isEligibleItem(gender, 6, jobType.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 7, jobType.type, weapon)) {
                c.getSession().close();
                return;
            }
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c, jobType);//kaotic note - job type for later
        newchar.setWorld((byte) c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair + hairColor);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor(skinColor);
        newchar.setDemonMarking(demonMark);

        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);

        Item item = li.getEquipById(top);
        item.setPosition((byte) -5);
        equip.addFromDB(item);

        if (bottom > 0) { //resistance have overall
            item = li.getEquipById(bottom);
            item.setPosition((byte) -6);
            equip.addFromDB(item);
        }

        item = li.getEquipById(shoes);
        item.setPosition((byte) -7);
        equip.addFromDB(item);

        item = li.getEquipById(weapon);
        item.setPosition((byte) -11);
        equip.addFromDB(item);

        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000013, (byte) 0, (short) 100, (byte) 0));
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000014, (byte) 0, (short) 100, (byte) 0));
        //blue/red pots
        switch (jobType) {
            case Resistance: // Resistance
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1, (byte) 0));
                break;
            case Adventurer: // Adventurer
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1, (byte) 0));
                break;
            case Cygnus: // Cygnus
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1, (byte) 0));
                break;
            case Aran: // Aran
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1, (byte) 0));
                break;
            case Evan: //Evan
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161052, (byte) 0, (short) 1, (byte) 0));
                break;
            case Mercedes: // Mercedes
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161079, (byte) 0, (short) 1, (byte) 0));
                break;
            case Demon: //Demon
                newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161054, (byte) 0, (short) 1, (byte) 0));
                break;
        }

        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()) && (c.isGm() || c.canMakeCharacter(c.getWorld()))) {
            MapleCharacter.saveNewCharToDB(newchar, jobType, jobType.id == 0 ? db : 0);
            c.announce(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getId());
        } else {
            c.announce(LoginPacket.addNewCharEntry(newchar, false));
        }
    }

    public static void CreateUltimate(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        c.getPlayer().dropMessage(1, "System is disabled");
        //c.announce(CField.createUltimate(0));
    }

    public static void DeleteChar(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (!canLogin(c)) {
            c.getSession().close();
            return;
        }
        String Secondpw_Client = GameConstants.GMS ? slea.readMapleAsciiString() : null;
        if (Secondpw_Client == null) {
            if (slea.readByte() > 0) { // Specific if user have second password or not
                Secondpw_Client = slea.readMapleAsciiString();
            }
            slea.readMapleAsciiString();
        }

        final int Character_ID = slea.readInt();

        if (!c.login_Auth(Character_ID) || !c.isLoggedIn()) {
            c.getSession().close();
            return; // Attempting to delete other character
        }
        byte state = 0;

        if (c.getSecondPassword() != null) { // On the server, there's a second password
            if (Secondpw_Client == null) { // Client's hacking
                c.getSession().close();
                return;
            } else {
                if (!c.CheckSecondPassword(Secondpw_Client)) { // Wrong Password
                    c.getSession().close();
                    return;
                    //state = 20;
                }
            }
        }
        if (!c.scanChar(c.getAccID())) {
            c.getSession().close();
            return;
        }
        if (state == 0) {
            state = (byte) c.deleteCharacter(Character_ID);
        }
        c.announce(LoginPacket.deleteCharResponse(Character_ID, state));

    }

    public static void Character_WithoutSecondPassword(final SeekableLittleEndianAccessor slea, final MapleClient c, final boolean haspic, final boolean view) {
        slea.readByte(); // 1?
        slea.readByte(); // 1?
        final int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        final String currentpw = c.getSecondPassword();
        if (!c.isLoggedIn() || loginFailCount(c) || (currentpw != null && (!currentpw.equals("") || haspic)) || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || c.getWorld() != 0) { // TODOO: MULTI WORLDS
            c.getSession().close();
            return;
        }
        c.updateMacs(slea.readMapleAsciiString());
        slea.readMapleAsciiString();
        if (slea.available() != 0) {
            final String setpassword = slea.readMapleAsciiString();

            if (setpassword.length() >= 6 && setpassword.length() <= 16) {
                c.setSecondPassword(setpassword);
                c.updateSecondPassword();
            } else {
                c.announce(LoginPacket.secondPwError((byte) 0x14));
                return;
            }
        } else if (GameConstants.GMS && haspic) {
            return;
        }
        final String s = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        String[] socket = ChannelServer.getInstance(c.getChannel()).getIP().split(":");
        //InetAddress socket = InetAddress.getByName(ChannelServer.getInstance(c.getChannel()).getIP());
        try {
            c.announce(CField.getServerIP(c, InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void Character_WithSecondPassword(final SeekableLittleEndianAccessor slea, final MapleClient c, final boolean view) {
        if (!canLogin(c)) {
            c.getSession().close();
            return;
        }
        final String password = slea.readMapleAsciiString();
        final int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        if (c.getSecondPassword() == null) { // TODOO: MULTI WORLDS
            System.out.println("client: " + c.getAccountName() + " 2nd password is null.");
            c.getSession().close();
            return;
        }
        if (!c.login_Auth(charId)) { // TODOO: MULTI WORLDS
            System.out.println("client: " + c.getAccountName() + " login auth error.");
            c.getSession().close();
            return;
        }
        if (ChannelServer.getInstance(c.getChannel()) == null) { // TODOO: MULTI WORLDS
            System.out.println("client: " + c.getAccountName() + " channel is null.");
            c.getSession().close();
            return;
        }
        if (c.getWorld() != 0) { // TODOO: MULTI WORLDS
            System.out.println("client: " + c.getAccountName() + " world error.");
            c.getSession().close();
            return;
        }
        if (GameConstants.GMS) {
            c.updateMacs(slea.readMapleAsciiString());
        }
        if (c.CheckSecondPassword(password) && password.length() >= 6 && password.length() <= 16) {
            if (checkBan(c)) {
                c.getSession().close();
                return;
            }
            final String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            String[] socket = ChannelServer.getInstance(c.getChannel()).getIP().split(":");
            c.setIdleTask(PingTimer.getInstance().schedule(() -> {
                if (c.getLoginState() == 1) {
                    c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, c.getSessionIPAddress());
                    c.lockAccount(0);
                    c.getSession().close();
                }
            }, 5 * 1000));
            //String[] socket = LoginServer.getInstance().getIP(c.getWorld(), c.getChannel()).split(":");
            //InetAddress socket = InetAddress.getByName(ChannelServer.getInstance(c.getChannel()).getIP());
            try {
                c.announce(CField.getServerIP(c, InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } catch (UnknownHostException | NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            c.announce(LoginPacket.secondPwError((byte) 0x14));
        }
    }

    public static void ViewChar(SeekableLittleEndianAccessor slea, MapleClient c) {
        //c.getSession().close();
        /*
         Map<Byte, ArrayList<MapleCharacter>> worlds = new HashMap<Byte, ArrayList<MapleCharacter>>();
         List<MapleCharacter> chars = c.loadCharacters(0); //TODO multi world
         c.announce(LoginPacket.showAllCharacter(chars.size()));
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
         for (Entry<Byte, ArrayList<MapleCharacter>> w : worlds.entrySet()) {
         c.announce(LoginPacket.showAllCharacterInfo(w.getKey(), w.getValue(), c.getSecondPassword()));
         }
         */
    }
}
