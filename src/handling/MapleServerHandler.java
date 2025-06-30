/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2012 Patrick Huy <patrick.huy@frz.cc> 
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
package handling;

import client.MapleCharacter;
import constants.ServerConstants;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import client.MapleClient;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory;
import client.maplepal.KaoticCraftHandler;
import client.maplepal.MaplePalBattlePacketHandler;
import client.maplepal.MaplePalHatchPacketHandler;
import client.maplepal.MaplePalWindowPacketHandler;
import constants.GameConstants;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.cashshop.handler.*;
import handling.channel.handler.*;
import handling.login.LoginServer;
import handling.login.handler.*;
import handling.mina.MaplePacketDecoder;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import server.Randomizer;
import tools.MapleAESOFB;
import tools.packet.LoginPacket;
import tools.data.ByteArrayByteStream;
import tools.data.LittleEndianAccessor;
import tools.Pair;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import scripting.NPCScriptManager;
import server.MTSStorage;
import tools.FilePrinter;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MTSCSPacket;

public class MapleServerHandler extends IoHandlerAdapter implements MapleServerHandlerMBean {

    public static boolean Log_Packets = true;
    private int channel = -1;
    private static int numDC = 0;
    private static long lastDC = System.currentTimeMillis();
    private boolean cs;
    private final List<String> BlockedIP = new ArrayList<String>();
    private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<String, Pair<Long, Byte>>();
    //Screw locking. Doesn't matter.
//    private static final ReentrantReadWriteLock IPLoggingLock = new ReentrantReadWriteLock();
    private static final String nl = System.getProperty("line.separator");
    private static final File loggedIPs = new File("LogIPs.txt");
    private static final HashMap<String, FileWriter> logIPMap = new HashMap<String, FileWriter>();
    //Note to Zero: Use an enumset. Don't iterate through an array.
    private static final EnumSet<RecvPacketOpcode> blocked = EnumSet.noneOf(RecvPacketOpcode.class), sBlocked = EnumSet.noneOf(RecvPacketOpcode.class);
    private static AtomicLong sessionId = new AtomicLong(7777);

    public static final Map<String, Integer> loggedAccounts = new ConcurrentHashMap<>();

    public static void reloadLoggedIPs() {
//        IPLoggingLock.writeLock().lock();
//        try {
        for (FileWriter fw : logIPMap.values()) {
            if (fw != null) {
                try {
                    fw.write("=== Closing Log ===");
                    fw.write(nl);
                    fw.flush(); //Just in case.
                    fw.close();
                } catch (IOException ex) {
                    System.out.println("Error closing Packet Log.");
                    System.out.println(ex);
                }
            }
        }
        logIPMap.clear();
        try {
            Scanner sc = new Scanner(loggedIPs);
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.length() > 0) {
                    addIP(line);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not reload packet logged IPs.");
            System.out.println(e);
        }
//        } finally {
//            IPLoggingLock.writeLock().unlock();
//        }
    }
    //Return the Filewriter if the IP is logged. Null otherwise.

    private static FileWriter isLoggedIP(IoSession sess) {
        String a = sess.getRemoteAddress().toString();
        String realIP = a.substring(a.indexOf('/') + 1, a.indexOf(':'));
        return logIPMap.get(realIP);
    }

    public static void addIP(String theIP) {
        try {
            FileWriter fw = new FileWriter(new File("PacketLog_" + theIP + ".txt"), true);
            fw.write("=== Creating Log ===");
            fw.write(nl);
            fw.flush();
            logIPMap.put(theIP, fw);
        } catch (IOException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }

    }
// <editor-fold defaultstate="collapsed" desc="Packet Log Implementation">
    private static final int Log_Size = 10000, Packet_Log_Size = 25;
    private static final ArrayList<LoggedPacket> Packet_Log = new ArrayList<LoggedPacket>(Log_Size);
    private static final ReentrantReadWriteLock Packet_Log_Lock = new ReentrantReadWriteLock();
    private static String Packet_Log_Output = "Packet/PacketLog";
    private static int Packet_Log_Index = 0;

    public static void log(String packet, String op, MapleClient c, IoSession io) {
        if (op == "CLOSED") {
            return;
        }
        if (c != null) {
            try {
                Packet_Log_Lock.writeLock().lock();
                LoggedPacket logged = null;
                if (Packet_Log.size() == Log_Size) {
                    logged = Packet_Log.remove(0);
                }
                //This way, we don't create new LoggedPacket objects, we reuse them =]
                if (logged == null) {
                    logged = new LoggedPacket(packet, op, io.getRemoteAddress().toString(),
                            c == null ? -1 : c.getAccID(), FileoutputUtil.CurrentReadable_Time(),
                            c == null || c.getAccountName() == null ? "[Null]" : c.getAccountName(),
                            c == null || c.getPlayer() == null || c.getPlayer().getName() == null ? "[Null]" : c.getPlayer().getName(),
                            c == null || c.getPlayer() == null || c.getPlayer().getMap() == null ? "[Null]" : String.valueOf(c.getPlayer().getMapId()),
                            c == null || NPCScriptManager.getInstance().getCM(c) == null ? "[Null]" : String.valueOf(NPCScriptManager.getInstance().getCM(c).getNpc()));
                } else {
                    logged.setInfo(packet, op, io.getRemoteAddress().toString(),
                            c == null ? -1 : c.getAccID(), FileoutputUtil.CurrentReadable_Time(),
                            c == null || c.getAccountName() == null ? "[Null]" : c.getAccountName(),
                            c == null || c.getPlayer() == null || c.getPlayer().getName() == null ? "[Null]" : c.getPlayer().getName(),
                            c == null || c.getPlayer() == null || c.getPlayer().getMap() == null ? "[Null]" : String.valueOf(c.getPlayer().getMapId()),
                            c == null || NPCScriptManager.getInstance().getCM(c) == null ? "[Null]" : String.valueOf(NPCScriptManager.getInstance().getCM(c).getNpc()));
                }
                Packet_Log.add(logged);
            } finally {
                Packet_Log_Lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void writeLog() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private static class LoggedPacket {

        private static final String nl = System.getProperty("line.separator");
        private String ip, accName, accId, chrName, packet, mapId, npcId, op, time;
        private long timestamp;

        public LoggedPacket(String p, String op, String ip, int id, String time, String accName, String chrName, String mapId, String npcId) {
            setInfo(p, op, ip, id, time, accName, chrName, mapId, npcId);
        }

        public final void setInfo(String p, String op, String ip, int id, String time, String accName, String chrName, String mapId, String npcId) {
            this.ip = ip;
            this.op = op;
            this.time = time;
            this.packet = p;
            this.accName = accName;
            this.chrName = chrName;
            this.mapId = mapId;
            this.npcId = npcId;
            timestamp = System.currentTimeMillis();
            this.accId = String.valueOf(id);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[IP: ").append(ip).append("] [").append(accId).append('|').append(accName).append('|').append(chrName).append("] [").append(npcId).append('|').append(mapId).append("] [Time: ").append(timestamp).append("] [").append(time).append(']');
            sb.append(nl);
            sb.append("[Op: ").append(op).append("] [").append(packet).append(']');
            return sb.toString();
        }
    }

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            MapleServerHandler mbean = new MapleServerHandler();
            //The log is a static object, so we can just use this hacky method.
            mBeanServer.registerMBean(mbean, new ObjectName("handling:type=MapleServerHandler"));
        } catch (Exception e) {
            System.out.println("Error registering PacketLog MBean");
            e.printStackTrace();
        }
    }

    public void writeLog(MapleClient client) {
        writeLog(client, false);
    }

    public void writeLog(MapleClient client, boolean crash) {
        if (client != null) {
            System.out.println("Client: " + client.getAccountName() + " Error logging");
            if (client.getPlayer() != null) {
                System.out.println("Char: " + client.getPlayer().getName() + " Error logging");
            } else {
                client.getSession().close();
            }
        }
        Packet_Log_Lock.readLock().lock();
        try {
            FileWriter fw = new FileWriter(new File(Packet_Log_Output + Packet_Log_Index + (crash ? "_DC.txt" : ".txt")), true);
            String nl = System.getProperty("line.separator");
            for (LoggedPacket loggedPacket : Packet_Log) {
                fw.write(loggedPacket.toString());
                fw.write(nl);
            }
            final String logString = "Log has been written at " + lastDC + " [" + FileoutputUtil.CurrentReadable_Time() + "] - " + numDC + " have disconnected, within " + (System.currentTimeMillis() - lastDC) + " milliseconds. (" + System.currentTimeMillis() + ")";
            System.out.println(logString);
            fw.write(logString);
            fw.write(nl);
            fw.flush();
            fw.close();
            Packet_Log.clear();
            Packet_Log_Index++;
            if (Packet_Log_Index > Packet_Log_Size) {
                Packet_Log_Index = 0;
                Log_Packets = false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error writing log to file.");

        } finally {
            Packet_Log_Lock.readLock().unlock();
        }
    }

    public static final void initiate() {
        reloadLoggedIPs();
        RecvPacketOpcode[] block = new RecvPacketOpcode[]{RecvPacketOpcode.AUTO_AGGRO, RecvPacketOpcode.NPC_ACTION, RecvPacketOpcode.MOVE_PLAYER, RecvPacketOpcode.PONG, RecvPacketOpcode.MOVE_PET, RecvPacketOpcode.MOVE_SUMMON, RecvPacketOpcode.MOVE_DRAGON, RecvPacketOpcode.MOVE_LIFE, RecvPacketOpcode.MOVE_ANDROID, RecvPacketOpcode.HEAL_OVER_TIME, RecvPacketOpcode.STRANGE_DATA, RecvPacketOpcode.CANCEL_DEBUFF, RecvPacketOpcode.MOVE_FAMILIAR};
        RecvPacketOpcode[] serverBlock = new RecvPacketOpcode[]{RecvPacketOpcode.AUTO_AGGRO, RecvPacketOpcode.CHANGE_KEYMAP, RecvPacketOpcode.ITEM_PICKUP, RecvPacketOpcode.PET_LOOT, RecvPacketOpcode.TAKE_DAMAGE, RecvPacketOpcode.FACE_EXPRESSION, RecvPacketOpcode.USE_ITEM, RecvPacketOpcode.CLOSE_RANGE_ATTACK, RecvPacketOpcode.MAGIC_ATTACK, RecvPacketOpcode.RANGED_ATTACK, RecvPacketOpcode.ARAN_COMBO, RecvPacketOpcode.SPECIAL_MOVE, RecvPacketOpcode.GENERAL_CHAT, RecvPacketOpcode.MONSTER_BOMB, RecvPacketOpcode.PASSIVE_ENERGY, RecvPacketOpcode.PET_AUTO_POT, RecvPacketOpcode.USE_CASH_ITEM, RecvPacketOpcode.PARTYCHAT, RecvPacketOpcode.CANCEL_BUFF, RecvPacketOpcode.SKILL_EFFECT, RecvPacketOpcode.CHAR_INFO_REQUEST, RecvPacketOpcode.ALLIANCE_OPERATION, RecvPacketOpcode.AUTO_ASSIGN_AP, RecvPacketOpcode.DISTRIBUTE_AP, RecvPacketOpcode.USE_MAGNIFY_GLASS, RecvPacketOpcode.SPAWN_PET, RecvPacketOpcode.SUMMON_ATTACK, RecvPacketOpcode.ITEM_MOVE, RecvPacketOpcode.PARTY_SEARCH_STOP};
        blocked.addAll(Arrays.asList(block));
        sBlocked.addAll(Arrays.asList(serverBlock));
        if (Log_Packets) {
            for (int i = 1; i <= Packet_Log_Size; i++) {
                if (!(new File(Packet_Log_Output + i + ".txt")).exists() && !(new File(Packet_Log_Output + i + "_DC.txt")).exists()) {
                    Packet_Log_Index = i;
                    break;
                }
            }
            if (Packet_Log_Index <= 0) { //25+ files, do not log
                Log_Packets = false;
            }
        }

        registerMBean();
    }

    public MapleServerHandler() {
        //ONLY FOR THE MBEAN
    }
    // </editor-fold>

    public MapleServerHandler(final int channel, final boolean cs) {
        this.channel = channel;
        this.cs = cs;
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {//kaotic packet debug
        if (cause instanceof IOException) {
            //System.out.println("Error in Session: " + session);
            //cause.printStackTrace();
            sessionClosed(session);
        }
        /*	MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
         log.error(MapleClient.getLogMessage(client, cause.getMessage()), cause);*/
        //cause.printStackTrace();
    }

    @Override
    public void sessionOpened(final IoSession session) throws Exception {
        // Start of IP checking
        String address = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        //System.out.print("ip: " + address);

        if (BlockedIP.contains(address)) {
            session.close();
            return;
        }
        String IP = address.substring(address.indexOf('/') + 1, address.length());
        if (channel > -1) {
            if (ChannelServer.getInstance(channel).isShutdown()) {
                session.close();
                return;
            }
        } else if (cs) {
            if (CashShopServer.isShutdown()) {
                session.close();
                return;
            }
        } else {
            if (LoginServer.isShutdown()) {
                session.close();
                return;
            }
        }
        //LoginServer.removeIPAuth(IP);

        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Randomizer.nextInt(255));
        ivSend[3] = (byte) (Randomizer.nextInt(255));
        MapleAESOFB sendCypher = new MapleAESOFB(ivSend, (short) (0xFFFF - ServerConstants.MAPLE_VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(ivRecv, (short) ServerConstants.MAPLE_VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, session);
        client.setSessionId(sessionId.getAndIncrement()); // Generates a reasonable session id.
        client.setChannel(channel);
        //MaplePacketDecoder.DecoderState decoderState = new MaplePacketDecoder.DecoderState();
        //session.setAttribute(MaplePacketDecoder.DECODER_STATE_KEY, decoderState);
        session.write(LoginPacket.getHello(ServerConstants.MAPLE_VERSION, ivSend, ivRecv));
        session.setAttribute(MapleClient.CLIENT_KEY, client);
    }

    private boolean isLoginServerHandler() {
        return channel == -1;
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        String address = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            byte state = MapleClient.CHANGE_CHANNEL;
            if (Log_Packets && !LoginServer.isShutdown() && !cs && channel > -1) {
                state = client.getLoginState();
            }
            if (state != MapleClient.CHANGE_CHANNEL) {
                log("Data: " + numDC, "CLOSED", client, session);
                if (System.currentTimeMillis() - lastDC < 60000) { //within the minute
                    numDC++;
                    if (numDC > 100) { //100+ people have dc'd in minute in channelserver
                        System.out.println("Writing log...");
                        writeLog(client);
                        numDC = 0;
                        lastDC = System.currentTimeMillis(); //intentionally place here
                    }
                } else {
                    numDC = 0;
                    lastDC = System.currentTimeMillis(); //intentionally place here
                }
            }
            try {
                if (session.containsAttribute(MapleClient.CLIENT_TRANSITION)) {
                    client.disconnect(false, false);
                } else {
                    client.disconnect(true, cs);
                }
            } finally {
                session.close();
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }

        }

        super.sessionClosed(session);
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        System.out.println("client went idle?");
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

        if (client != null && client.getPlayer() != null) {
            System.out.println("Player " + client.getPlayer().getName() + " went idle");
        }
        if (client != null) {
            client.sendPing();
        } else {
            if (session.isConnected()) {
                final String address = session.getRemoteAddress().toString().split(":")[0];
                if (LoginServer.containsIPAuth(address)) {
                    LoginServer.removeIPAuth(address);
                }
                session.close();
                session.removeAttribute(MapleClient.CLIENT_KEY);
            }
        }
        super.sessionIdle(session, status);
        System.out.println("client went idle part 2?");
    }

    public void closeAllChars(int id) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr != null) {
                    if (id == chr.getAccountID()) {
                        System.out.println("Player:  " + chr.getName() + " has been removed from world. Possible Dupe attempt.");
                        chr.getClient().forceDisconnect();
                        cserv.getPlayerStorage().deregisterPlayer(chr);
                    }
                }
            }
        }
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) {
        //System.out.println("mesg");
        if (message == null || session == null) {
            return;
        }
        byte[] content = (byte[]) message;
        //final SeekableLittleEndianAccessor slea = new LittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new tools.data.input.ByteArrayByteStream(content));
        if (slea.available() < 2) {
            return;
        }
        final MapleClient c = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (c == null || !c.isReceiving()) {
            return;
        }
        final short header_num = slea.readShort();

        //final StringBuilder sb = new StringBuilder("Received data :\n");
        //sb.append(HexTool.toString((byte[]) message)).append("\n").append(HexTool.toStringFromAscii((byte[]) message));
        //System.out.println(sb.toString());
        for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
            if (recv.getValue() == header_num) {
                if (recv.NeedsChecking()) {
                    if (!c.isLoggedIn()) {
                        return;
                    }
                }
                try {
                    //no login packets
                    if (Log_Packets && !blocked.contains(recv) && !sBlocked.contains(recv) && (cs || channel > -1)) {
                        log(slea.toString(), recv.toString(), c, session);
                    } else if (!blocked.contains(recv) && c.getAccountName() != null && loggedAccounts.containsKey(c.getAccountName().toLowerCase())) {
                        FilePrinter.print(FilePrinter.ACCOUNT_PACKET_LOG + c.getAccountName() + ".txt", recv.name() + " - " + slea.toString(), false, false);
                    }
                    handlePacket(recv, slea, c, cs);
                    //Log after the packet is handle. You'll see why =]
                } catch (NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
                    //swallow, no one cares
                    if (!ServerConstants.Use_Localhost) {
                        String txt = slea.toString(true);
                        FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                        FileoutputUtil.log(FileoutputUtil.PacketEx_Log, "Packet: " + header_num + "\n" + txt);
                        e.printStackTrace();
                        if (c.getPlayer() != null) {
                            c.getPlayer().kick();
                            System.out.println(c.getPlayer().getName() + " on Acoount: " + c.getAccountName() + " was kicked for bad packets: " + txt);
                        } else {
                            c.getSession().close();
                        }
                    }
                } catch (Exception e) {
                    String txt = slea.toString(true);
                    FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                    FileoutputUtil.log(FileoutputUtil.PacketEx_Log, "Packet: " + header_num + "\n" + txt);
                    e.printStackTrace();
                    if (c.getPlayer() != null) {
                        c.getPlayer().kick();
                        System.out.println(c.getPlayer().getName() + " on Acoount: " + c.getAccountName() + " was kicked for bad packets: " + txt);
                    } else {
                        c.getSession().close();
                    }
                }

                return;
            }
        }
    }

    //System.out.println("Received data : (Unhandled) " + HexTool.toString(((byte[]) message)));//kaotic packet
    public static final void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea, final MapleClient c, final boolean cs) throws Exception {
        if (cs) {
            return;
        }
        if (c == null) {
            return;
        }
        //System.out.println("test: " + header);
        switch (header) {
            case PONG:
                c.pongReceived();
                break;
            case STRANGE_DATA:
                // Does nothing for now, HackShield's heartbeat
                break;
            case LOGIN_PASSWORD:
                CharLoginHandler.login(slea, c);
                break;
            case SEND_ENCRYPTED:
                //if (c.isLocalhost()) {
                //    CharLoginHandler.login(slea, c);
                //} else {
                //    c.announce(LoginPacket.getCustomEncryption());
                //}
                break;
            case CLIENT_START:
            case CLIENT_FAILED:
                // c.announce(LoginPacket.getCustomEncryption());
                break;
            case VIEW_SERVERLIST:
                if (slea.readByte() == 0) {
                    CharLoginHandler.ServerListRequest(c);
                }
                break;
            case REDISPLAY_SERVERLIST:
            case SERVERLIST_REQUEST:
                CharLoginHandler.ServerListRequest(c);
                break;
            case CLIENT_HELLO:
                if (slea.readByte() != 8 || slea.readShort() != ServerConstants.MAPLE_VERSION || !String.valueOf(slea.readShort()).equals(ServerConstants.MAPLE_PATCH)) {
                    c.getSession().close();
                }
                break;
            case CHARLIST_REQUEST:
                CharLoginHandler.CharlistRequest(slea, c);
                break;
            case SERVERSTATUS_REQUEST:
                CharLoginHandler.ServerStatusRequest(c);
                break;
            case CHECK_CHAR_NAME:
                CharLoginHandler.CheckCharName(slea.readMapleAsciiString(), c);
                break;
            case CREATE_CHAR:
            case CREATE_SPECIAL_CHAR:
                CharLoginHandler.CreateChar(slea, c);
                break;
            case CREATE_ULTIMATE:
                CharLoginHandler.CreateUltimate(slea, c);
                break;
            case DELETE_CHAR:
                CharLoginHandler.DeleteChar(slea, c);
                break;
            case VIEW_ALL_CHAR:
                CharLoginHandler.ViewChar(slea, c);
                break;
            case PICK_ALL_CHAR:
                CharLoginHandler.Character_WithoutSecondPassword(slea, c, false, true);
                break;
            case CHAR_SELECT_NO_PIC:
                CharLoginHandler.Character_WithoutSecondPassword(slea, c, false, false);
                break;
            case VIEW_REGISTER_PIC:
                CharLoginHandler.Character_WithoutSecondPassword(slea, c, true, true);
                break;
            case CHAR_SELECT:
                CharLoginHandler.Character_WithoutSecondPassword(slea, c, true, false);
                break;
            case VIEW_SELECT_PIC:
                CharLoginHandler.Character_WithSecondPassword(slea, c, true);
                break;
            case AUTH_SECOND_PASSWORD:
                CharLoginHandler.Character_WithSecondPassword(slea, c, false);
                break;
            case CLIENT_ERROR:
                if (slea.available() < 8) {
                    return;
                }
                short type = slea.readShort();
                String type_str = "Unknown?!";
                if (type == 0x01) {
                    type_str = "SendBackupPacket";
                } else if (type == 0x02) {
                    type_str = "Crash Report";
                } else if (type == 0x03) {
                    type_str = "Exception";
                }
                int unk = slea.readInt();
                if (unk == 0) { // i don't wanna log error code 0 stuffs, (usually some bounceback to login)
                    return;
                }
                short data_length = slea.readShort();
                slea.skip(4); // ?				
                FileoutputUtil.log("ErrorCodes.rtf", "Client Acc: " + c.getAccountName() + " - Player: " + (c.getPlayer() != null ? c.getPlayer().getName() : "") + " - sent crashing packet: Type: " + type_str + "; Error code: " + unk + "; Length: " + data_length + "; Packet: " + slea.toString());
                System.out.println(c.getPlayer().getName() + " crashed from header: " + header);
                break;
            case ENABLE_SPECIAL_CREATION:
                c.announce(LoginPacket.enableSpecialCreation(c.getAccID(), true));
                break;
            case RSA_KEY: // Fix this somehow
                //c.announce(LoginPacket.getLoginAUTH());
                // c.announce(LoginPacket.StrangeDATA());
                break;
            // END OF LOGIN SERVER
            case CHANGE_CHANNEL:
            case CHANGE_ROOM_CHANNEL:
                InterServerHandler.ChangeChannel(slea, c, header == RecvPacketOpcode.CHANGE_ROOM_CHANNEL);
                break;
            case PLAYER_LOGGEDIN://player enters world
                InterServerHandler.Loggedin(slea, c);
                break;
            case ENTER_PVP:
            case ENTER_PVP_PARTY:
                PlayersHandler.EnterPVP(slea, c);
                break;
            case PVP_RESPAWN:
                PlayersHandler.RespawnPVP(slea, c);
                break;
            case LEAVE_PVP:
                PlayersHandler.LeavePVP(slea, c);
                break;
            case PVP_ATTACK:
                PlayersHandler.AttackPVP(slea, c);
                break;
            case PVP_SUMMON:
                SummonHandler.SummonPVP(slea, c);
                break;
            case ENTER_CASH_SHOP:
                InterServerHandler.EnterCS(c, false);
                break;
            case ENTER_MTS:
                InterServerHandler.EnterMTS(c);
                //InterServerHandler.EnterCS(c, c.getPlayer(), true);
                break;
            case MOVE_PLAYER:
                PlayerHandler.MovePlayer(slea, c);
                break;
            case NPC_SHOP:
                NPCHandler.NPCShop(slea, c);
                break;
            case CHAR_INFO_REQUEST:

                PlayerHandler.CharInfoRequest(slea, c);
                break;
            case CLOSE_RANGE_ATTACK:
                //System.out.println("Melee");
                PlayerHandler.closeRangeAttack(slea, c, false);
                break;
            case RANGED_ATTACK:
                //System.out.println("Range");
                PlayerHandler.rangedAttack(slea, c);
                break;
            case MAGIC_ATTACK:
                //System.out.println("Magic");
                PlayerHandler.MagicDamage(slea, c);
                break;
            case SPECIAL_MOVE:
                PlayerHandler.SpecialMove(slea, c);
                break;
            case PASSIVE_ENERGY:
                PlayerHandler.closeRangeAttack(slea, c, true);
                break;
            case GET_BOOK_INFO:
                PlayersHandler.MonsterBookInfoRequest(slea, c);
                break;
            case MONSTER_BOOK_DROPS:
                PlayersHandler.MonsterBookDropsRequest(slea, c, c.getPlayer());
                break;
            case CHANGE_SET:
                PlayersHandler.ChangeSet(slea, c, c.getPlayer());
                break;
            case PROFESSION_INFO:
                ItemMakerHandler.ProfessionInfo(slea, c);
                break;
            case CRAFT_DONE:
                ItemMakerHandler.CraftComplete(slea, c, c.getPlayer());
                break;
            case CRAFT_MAKE:
                ItemMakerHandler.CraftMake(slea, c, c.getPlayer());
                break;
            case CRAFT_EFFECT:
                ItemMakerHandler.CraftEffect(slea, c, c.getPlayer());
                break;
            case START_HARVEST:
                ItemMakerHandler.StartHarvest(slea, c, c.getPlayer());
                break;
            case STOP_HARVEST:
                ItemMakerHandler.StopHarvest(slea, c, c.getPlayer());
                break;
            case MAKE_EXTRACTOR:
                ItemMakerHandler.MakeExtractor(slea, c, c.getPlayer());
                break;
            case USE_BAG:
                ItemMakerHandler.UseBag(slea, c, c.getPlayer());
                break;
            case USE_FAMILIAR:
                MobHandler.UseFamiliar(slea, c, c.getPlayer());
                break;
            case SPAWN_FAMILIAR:
                //MobHandler.SpawnFamiliar(slea, c, c.getPlayer()); - needs to be fixed
                break;
            case RENAME_FAMILIAR:
                MobHandler.RenameFamiliar(slea, c, c.getPlayer());
                break;
            case MOVE_FAMILIAR:
                MobHandler.MoveFamiliar(slea, c, c.getPlayer());
                break;
            case ATTACK_FAMILIAR:
                MobHandler.AttackFamiliar(slea, c, c.getPlayer());
                break;
            case TOUCH_FAMILIAR:
                MobHandler.TouchFamiliar(slea, c, c.getPlayer());
                break;
            case USE_RECIPE:
                ItemMakerHandler.UseRecipe(slea, c, c.getPlayer());
                break;
            case MOVE_ANDROID:
                PlayerHandler.MoveAndroid(slea, c, c.getPlayer());
                break;
            case FACE_EXPRESSION:
                PlayerHandler.ChangeEmotion(slea.readInt(), c.getPlayer());
                break;
            case FACE_ANDROID:
                PlayerHandler.ChangeAndroidEmotion(slea.readInt(), c.getPlayer());
                break;
            case TAKE_DAMAGE:
                PlayerHandler.TakeDamage(slea, c, c.getPlayer());
                break;
            case HEAL_OVER_TIME:
                PlayerHandler.Heal(slea, c.getPlayer());
                break;
            case CANCEL_BUFF:
                PlayerHandler.CancelBuffHandler(slea.readInt(), c.getPlayer());
                break;
            case MECH_CANCEL:
                PlayerHandler.CancelMech(slea, c.getPlayer());
                break;
            case CANCEL_ITEM_EFFECT:
                PlayerHandler.CancelItemEffect(slea.readInt(), c.getPlayer());
                break;
            case USE_TITLE:
                PlayerHandler.UseTitle(slea.readInt(), c, c.getPlayer());
                break;
            case USE_CHAIR:
                PlayerHandler.UseChair(slea.readInt(), c, c.getPlayer());
                break;
            case CANCEL_CHAIR:
                PlayerHandler.CancelChair(slea.readShort(), c, c.getPlayer());
                break;
            case WHEEL_OF_FORTUNE:
                break; //whatever
            case USE_ITEMEFFECT:
                PlayerHandler.UseItemEffect(slea.readInt(), c, c.getPlayer());
                break;
            case SKILL_EFFECT:
                PlayerHandler.SkillEffect(slea, c.getPlayer());
                break;
            case QUICK_SLOT:
                PlayerHandler.QuickSlot(slea, c.getPlayer());
                break;
            case MESO_DROP:
                c.getPlayer().updateTick(slea.readInt());
                PlayerHandler.DropMeso(slea.readInt(), c.getPlayer());
                break;
            case CHANGE_KEYMAP:
                PlayerHandler.ChangeKeymap(slea, c.getPlayer());
                break;
            case UPDATE_ENV:
                // We handle this in MapleMap
                break;
            case CHANGE_MAP:
                //System.out.println("test");
                if (cs) {
                    CashShopOperation.LeaveCS(slea, c, c.getPlayer());
                } else {
                    PlayerHandler.ChangeMap(slea, c, c.getPlayer());
                }
                break;
            case CHANGE_MAP_SPECIAL:
                PlayerHandler.ChangeMapSpecial(slea, c, c.getPlayer());

                break;
            case PLAYER_MAP_TRANSFER:
                if (c.getPlayer() != null) {
                    c.getPlayer().setMapTransitionComplete();
                }
                break;
            case USE_INNER_PORTAL:
                slea.skip(1);
                PlayerHandler.InnerPortal(slea, c, c.getPlayer());
                break;
            case TROCK_ADD_MAP:
                PlayerHandler.TrockAddMap(slea, c, c.getPlayer());
                break;
            case ARAN_COMBO:
                PlayerHandler.AranCombo(c, c.getPlayer(), 1);
                break;
            case SKILL_MACRO:
                PlayerHandler.ChangeSkillMacro(slea, c.getPlayer());
                break;
            case GIVE_FAME:
                PlayersHandler.GiveFame(slea, c, c.getPlayer());
                break;
            case TRANSFORM_PLAYER:
                PlayersHandler.TransformPlayer(slea, c, c.getPlayer());
                break;
            case NOTE_ACTION:
                PlayersHandler.Note(slea, c.getPlayer());
                break;
            case USE_DOOR:
                PlayersHandler.UseDoor(slea, c.getPlayer());
                break;
            case USE_MECH_DOOR:
                PlayersHandler.UseMechDoor(slea, c.getPlayer());
                break;
            case DAMAGE_REACTOR:
                PlayersHandler.HitReactor(slea, c);
                break;
            case CLICK_REACTOR:
                PlayersHandler.ClickReactor(slea, c);
                break;
            case TOUCH_REACTOR:
                PlayersHandler.TouchReactor(slea, c);
                break;
            case CLOSE_CHALKBOARD:
                c.getPlayer().setChalkboard(null);
                break;
            case ITEM_SORT:
                InventoryHandler.ItemSort(slea, c);
                break;
            case ITEM_GATHER:
                InventoryHandler.ItemGather(slea, c);
                break;
            case ITEM_MOVE:
                InventoryHandler.ItemMove(slea, c);
                break;
            case MOVE_BAG:
                InventoryHandler.MoveBag(slea, c);
                break;
            case SWITCH_BAG:
                InventoryHandler.SwitchBag(slea, c);
                break;
            case ITEM_MAKER:
                ItemMakerHandler.ItemMaker(slea, c);
                break;
            case ITEM_PICKUP:
                InventoryHandler.Pickup_Player(slea, c);
                break;
            case USE_CASH_ITEM:
                InventoryHandler.UseCashItem(slea, c);
                break;
            case USE_ITEM:
                InventoryHandler.UseItem(slea, c);
                break;
            case USE_COSMETIC:
                InventoryHandler.UseCosmetic(slea, c, c.getPlayer());
                break;
            case USE_MAGNIFY_GLASS:
                InventoryHandler.UseMagnify(slea, c);
                break;
            case USE_SCRIPTED_NPC_ITEM:
                InventoryHandler.UseScriptedNPCItem(slea, c, c.getPlayer());
                break;
            case USE_RETURN_SCROLL:
                InventoryHandler.UseReturnScroll(slea, c, c.getPlayer());
                break;
            case USE_NEBULITE:
                InventoryHandler.UseNebulite(slea, c);
                break;
            case USE_ALIEN_SOCKET:
                InventoryHandler.UseAlienSocket(slea, c);
                break;
            case USE_ALIEN_SOCKET_RESPONSE:
                slea.skip(4); // all 0
                c.announce(MTSCSPacket.useAlienSocket(false));
                break;
            case VICIOUS_HAMMER:
                slea.skip(4); // 3F 00 00 00
                slea.skip(4); // all 0
                c.announce(MTSCSPacket.ViciousHammer(false, 0));
                break;
            case USE_NEBULITE_FUSION:
                InventoryHandler.UseNebuliteFusion(slea, c);
                break;
            case USE_UPGRADE_SCROLL:
                c.getPlayer().updateTick(slea.readInt());
                InventoryHandler.UseUpgradeScroll(slea.readShort(), slea.readShort(), slea.readShort(), c, c.getPlayer(), slea.readByte() > 0);
                break;
            case USE_FLAG_SCROLL:
            case USE_POTENTIAL_SCROLL:
            case USE_EQUIP_SCROLL:
                c.getPlayer().updateTick(slea.readInt());
                InventoryHandler.UseUpgradeScroll(slea.readShort(), slea.readShort(), (short) 0, c, c.getPlayer(), slea.readByte() > 0);
                break;
            case USE_SUMMON_BAG:
                InventoryHandler.UseSummonBag(slea, c, c.getPlayer());
                break;
            case USE_TREASUER_CHEST:
                InventoryHandler.UseTreasureChest(slea, c, c.getPlayer());
                break;
            case USE_SKILL_BOOK:
                c.getPlayer().updateTick(slea.readInt());
                InventoryHandler.UseSkillBook((byte) slea.readShort(), slea.readInt(), c, c.getPlayer());
                break;
            case USE_CATCH_ITEM:
                InventoryHandler.UseCatchItem(slea, c, c.getPlayer());
                break;
            case USE_MOUNT_FOOD:
                InventoryHandler.UseMountFood(slea, c, c.getPlayer());
                break;
            case REWARD_ITEM:
                InventoryHandler.UseRewardItem((byte) slea.readShort(), slea.readInt(), c, c.getPlayer());
                break;
            case HYPNOTIZE_DMG:
                MobHandler.HypnotizeDmg(slea, c.getPlayer());
                break;
            case MOB_NODE:
                MobHandler.MobNode(slea, c.getPlayer());
                break;
            case DISPLAY_NODE:
                MobHandler.DisplayNode(slea, c.getPlayer());
                break;
            case MOVE_LIFE:
                MobHandler.MoveMonster(slea, c, c.getPlayer());
                break;
            case AUTO_AGGRO:
                MobHandler.AutoAggro(slea.readInt(), c.getPlayer());
                break;
            case FRIENDLY_DAMAGE:
                MobHandler.FriendlyDamage(slea, c.getPlayer());
                break;
            case REISSUE_MEDAL:
                PlayerHandler.ReIssueMedal(slea, c, c.getPlayer());
                break;
            case MONSTER_BOMB:
                MobHandler.MonsterBomb(slea, c);
                break;
            case MOB_BOMB:
                MobHandler.MobBomb(slea, c.getPlayer());
                break;
            case NPC_TALK:
                NPCHandler.NPCTalk(slea, c);
                break;
            case NPC_TALK_MORE:
                NPCHandler.NPCMoreTalk(slea, c);
                break;
            case NPC_ACTION:
                NPCHandler.NPCAnimation(slea, c);
                break;
            case QUEST_ACTION:
                NPCHandler.QuestAction(slea, c);
                break;
            case STORAGE:
                NPCHandler.Storage(slea, c);
                break;
            case GENERAL_CHAT:
                if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
                    c.getPlayer().updateTick(slea.readInt());
                    ChatHandler.GeneralChat(slea.readMapleAsciiString(), slea.readBool(), c, c.getPlayer());
                }
                break;
            case PARTYCHAT:
                c.getPlayer().updateTick(slea.readInt());
                ChatHandler.Others(slea, c, c.getPlayer());
                break;
            case WHISPER:
                ChatHandler.Whisper_Find(slea, c);
                break;
            case MESSENGER:
                ChatHandler.Messenger(slea, c);
                break;
            case AUTO_ASSIGN_AP:
                StatsHandling.AutoAssignAP(slea, c, c.getPlayer());
                break;
            case DISTRIBUTE_AP:
                StatsHandling.DistributeAP(slea, c, c.getPlayer());
                break;
            case DISTRIBUTE_SP:
                c.getPlayer().updateTick(slea.readInt());
                StatsHandling.DistributeSP(slea.readInt(), c, c.getPlayer());
                break;
            case PLAYER_INTERACTION:
                PlayerInteractionHandler.PlayerInteraction(slea, c);
                break;
            case GUILD_OPERATION:
                GuildHandler.Guild(slea, c);
                break;
            case DENY_GUILD_REQUEST:
                slea.skip(1);
                GuildHandler.DenyGuildRequest(slea.readMapleAsciiString(), c);
                break;
            case ALLIANCE_OPERATION:
                AllianceHandler.HandleAlliance(slea, c, false);
                break;
            case DENY_ALLIANCE_REQUEST:
                AllianceHandler.HandleAlliance(slea, c, true);
                break;
            case PUBLIC_NPC:
                NPCHandler.OpenPublicNpc(slea, c);
                break;
            case BBS_OPERATION:
                BBSHandler.BBSOperation(slea, c);
                break;
            case PARTY_OPERATION:
                PartyHandler.PartyOperation(slea, c);
                break;
            case DENY_PARTY_REQUEST:
                PartyHandler.DenyPartyRequest(slea, c);
                break;
            case ALLOW_PARTY_INVITE:
                PartyHandler.AllowPartyInvite(slea, c);
                break;
            case BUDDYLIST_MODIFY:
                BuddyListHandler.BuddyOperation(slea, c);
                break;
            case CYGNUS_SUMMON:
                UserInterfaceHandler.CygnusSummon_NPCRequest(c);
                break;
            case SHIP_OBJECT:
                UserInterfaceHandler.ShipObjectRequest(slea.readInt(), c);
                break;
            case BUY_CS_ITEM:
                CashShopOperation.BuyCashItem(slea, c, c.getPlayer());
                break;
            case COUPON_CODE:
                //FileoutputUtil.log(FileoutputUtil.PacketEx_Log, "Coupon : \n" + slea.toString(true));
                //System.out.println(slea.toString());
                CashShopOperation.CouponCode(slea.readMapleAsciiString(), c);
                CashShopOperation.CouponCode(slea.readMapleAsciiString(), c);
                CashShopOperation.doCSPackets(c);
                break;
            case CS_UPDATE:
                CashShopOperation.CSUpdate(c);
                break;
            case TOUCHING_MTS:
                MTSOperation.MTSUpdate(MTSStorage.getInstance().getCart(c.getPlayer().getId()), c);
                break;
            case MTS_TAB:
                MTSOperation.MTSOperation(slea, c);
                break;
            case USE_POT:
                ItemMakerHandler.UsePot(slea, c);
                break;
            case CLEAR_POT:
                ItemMakerHandler.ClearPot(slea, c);
                break;
            case FEED_POT:
                ItemMakerHandler.FeedPot(slea, c);
                break;
            case CURE_POT:
                ItemMakerHandler.CurePot(slea, c);
                break;
            case REWARD_POT:
                ItemMakerHandler.RewardPot(slea, c);
                break;
            case DAMAGE_SUMMON:
                slea.skip(4);
                SummonHandler.DamageSummon(slea, c.getPlayer());
                break;
            case MOVE_SUMMON:
                SummonHandler.MoveSummon(slea, c.getPlayer());
                break;
            case SUMMON_ATTACK:
                SummonHandler.SummonAttack(slea, c, c.getPlayer());
                break;
            case MOVE_DRAGON:
                SummonHandler.MoveDragon(slea, c.getPlayer());
                break;
            case SUB_SUMMON:
                SummonHandler.SubSummon(slea, c.getPlayer());
                break;
            case REMOVE_SUMMON:
                SummonHandler.RemoveSummon(slea, c);
                break;
            case SPAWN_PET:
                PetHandler.SpawnPet(slea, c, c.getPlayer());
                break;
            case MOVE_PET:
                PetHandler.MovePet(slea, c.getPlayer());
                break;
            case PET_CHAT:
                //System.out.println("Pet chat: " + slea.toString());
                if (slea.available() < 12) {
                    break;
                }
                final int petid = GameConstants.GMS ? c.getPlayer().getPetIndex((int) slea.readLong()) : slea.readInt();
                c.getPlayer().updateTick(slea.readInt());
                PetHandler.PetChat(petid, slea.readShort(), slea.readMapleAsciiString(), c.getPlayer());
                break;
            case PET_COMMAND:
                MaplePet pet = null;
                if (GameConstants.GMS) {
                    pet = c.getPlayer().getPet(c.getPlayer().getPetIndex((int) slea.readLong()));
                } else {
                    pet = c.getPlayer().getPet((byte) slea.readInt());
                }
                slea.readByte(); //always 0?
                if (pet == null) {
                    return;
                }
                PetHandler.PetCommand(pet, PetDataFactory.getPetCommand(pet.getPetItemId(), slea.readByte()), c, c.getPlayer());
                break;
            case PET_FOOD:
                PetHandler.PetFood(slea, c, c.getPlayer());
                break;
            case PET_LOOT:
                InventoryHandler.Pickup_Pet(slea, c, c.getPlayer());
                break;
            case PET_AUTO_POT:
                PetHandler.Pet_AutoPotion(slea, c, c.getPlayer());
                break;
            case MONSTER_CARNIVAL:
                MonsterCarnivalHandler.MonsterCarnival(slea, c);
                break;
            case DUEY_ACTION:
                DueyHandler.DueyOperation(slea, c);
                break;
            case USE_HIRED_MERCHANT:
                HiredMerchantHandler.UseHiredMerchant(c, true);
                break;
            case MERCH_ITEM_STORE:
                HiredMerchantHandler.MerchantItemStore(slea, c);
                break;
            case CANCEL_DEBUFF:
                // Ignore for now
                break;
            case MAPLETV:
                break;
            case LEFT_KNOCK_BACK:
                PlayerHandler.leftKnockBack(slea, c);
                break;
            case SNOWBALL:
                PlayerHandler.snowBall(slea, c);
                break;
            case COCONUT:
                PlayersHandler.hitCoconut(slea, c);
                break;
            case REPAIR:
                NPCHandler.repair(slea, c);
                break;
            case REPAIR_ALL:
                NPCHandler.repairAll(c);
                break;
            case GAME_POLL:
                UserInterfaceHandler.InGame_Poll(slea, c);
                break;
            case OWL:
                InventoryHandler.Owl(slea, c);
                break;
            case OWL_WARP:
                InventoryHandler.OwlWarp(slea, c);
                break;
            case USE_OWL_MINERVA:
                InventoryHandler.OwlMinerva(slea, c);
                break;
            case RPS_GAME:
                NPCHandler.RPSGame(slea, c);
                break;
            case UPDATE_QUEST:
                NPCHandler.UpdateQuest(slea, c);
                break;
            case USE_ITEM_QUEST:
                NPCHandler.UseItemQuest(slea, c);
                break;
            case FOLLOW_REQUEST:
                PlayersHandler.FollowRequest(slea, c);
                break;
            case AUTO_FOLLOW_REPLY:
            case FOLLOW_REPLY:
                PlayersHandler.FollowReply(slea, c);
                break;
            case RING_ACTION:
                PlayersHandler.RingAction(slea, c);
                break;
            case REQUEST_FAMILY:
                FamilyHandler.RequestFamily(slea, c);
                break;
            case OPEN_FAMILY:
                FamilyHandler.OpenFamily(slea, c);
                break;
            case FAMILY_OPERATION:
                FamilyHandler.FamilyOperation(slea, c);
                break;
            case DELETE_JUNIOR:
                FamilyHandler.DeleteJunior(slea, c);
                break;
            case DELETE_SENIOR:
                FamilyHandler.DeleteSenior(slea, c);
                break;
            case USE_FAMILY:
                FamilyHandler.UseFamily(slea, c);
                break;
            case FAMILY_PRECEPT:
                FamilyHandler.FamilyPrecept(slea, c);
                break;
            case FAMILY_SUMMON:
                FamilyHandler.FamilySummon(slea, c);
                break;
            case ACCEPT_FAMILY:
                FamilyHandler.AcceptFamily(slea, c);
                break;
            case SOLOMON:
                PlayersHandler.Solomon(slea, c);
                break;
            case GACH_EXP:
                PlayersHandler.GachExp(slea, c);
                break;
            case PARTY_SEARCH_START:
                PartyHandler.MemberSearch(slea, c);
                break;
            case PARTY_SEARCH_STOP:
                PartyHandler.PartySearch(slea, c);
                break;
            case EXPEDITION_LISTING:
                PartyHandler.PartyListing(slea, c);
                break;
            case EXPEDITION_OPERATION:
                PartyHandler.Expedition(slea, c);
                break;
            case USE_TELE_ROCK:
                InventoryHandler.TeleRock(slea, c);
                break;
            case PAM_SONG:
                InventoryHandler.PamSong(slea, c);
                break;
            case REPORT:
                PlayersHandler.Report(slea, c);
                break;
            case KAOTIC_CRAFT_REQ_MAX_CRAFT:
            case KAOTIC_CRAFT_FINISH_CRAFT:
            case KAOTIC_CRAFT_ATTEMPT_CRAFT:
                KaoticCraftHandler.handlePacket(slea, c, header);
                break;
            case KAOTIC_PAL_WINDOW_REQ:
                MaplePalWindowPacketHandler.handlePacket(slea, c);
                break;
            case KAOTIC_PAL_HATCH_REQ:
                MaplePalHatchPacketHandler.handlePacket(slea, c);
                break;
            case KAOTIC_PAL_BATTLE_FINISHED:
                MaplePalBattlePacketHandler.handlePacket(slea, c);
                break;
            default:
                System.out.println("[UNHANDLED] Recv [" + header.toString() + "] found");
                break;
        }
    }
}
