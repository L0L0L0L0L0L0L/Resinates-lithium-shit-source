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
package constants;

import client.MapleCharacter;
import client.MapleClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;
import server.ServerProperties;

public class ServerConstants {//73.35.242.13

    public static boolean TESPIA = false; // true = uses GMS test server, for MSEA it does nothing though
    //public static final byte[] Gateway_IP = getServerIP();
    public static final boolean USE_THREAD_TRACKER = false;//[SEVERE] This deadlock auditing thing will bloat the memory as fast as possible
    //Dangling Items/Locks Configuration
    public static final int ITEM_EXPIRE_TIME = 60 * 1000;  //Time before items start disappearing. Recommended to be set up to 3 minutes.
    public static final int KITE_EXPIRE_TIME = 60 * 60 * 1000; //Time before kites (cash item) disappears.
    public static final int ITEM_MONITOR_TIME = 5 * 60 * 1000;  //Interval between item monitoring tasks on maps, which checks for dangling (null) item objects on the map item history.
    public static final int LOCK_MONITOR_TIME = 30 * 1000;      //Waiting time for a lock to be released. If it reaches timeout, a critical server deadlock has made present.
    //public static final byte[] Gateway_IP = new byte[]{(byte) 73, (byte) 35, (byte) 242, (byte) 13};
    //public static final byte[] Gateway_IP = new byte[]{(byte) 5, (byte) 180, (byte) 9, (byte) 16};
    //public static final byte[] NEXON_IP = new byte[]{(byte) 8, (byte) 31, (byte) 98, (byte) 53};
    public static String HOST;
    public static boolean LOCALSERVER;
    public static String DB_URL = "";
    public static String DB_USER = "";
    public static String DB_PASS = "";
    public static final boolean DB_CONNECTION_POOL = true;      //Installs a connection pool to hub DB connections. Set false to default.
    //Inject a DLL that hooks SetupDiGetClassDevsExA and returns 0.
    public static final long UPDATE_INTERVAL = 777;
    public static final long PURGING_INTERVAL = 5 * 60 * 1000;
    public static List<MapleClient> clients = new ArrayList<>();
    public static long pool = 0;

    /*
     * Specifics which job gives an additional EXP to party
     * returns the percentage of EXP to increase
     */
    public static final byte Class_Bonus_EXP(final int job) {
        switch (job) {
            case 800:
            case 900:
            case 910:
                return 10;
        }
        return 1;
    }
    // Start of Poll
    public static final boolean PollEnabled = false;
    public static final String Poll_Question = "Are you mudkiz?";
    public static final String[] Poll_Answers = {"test1", "test2", "test3"};
    // End of Poll
    public static final short MAPLE_VERSION = (short) 111;
    public static final String MAPLE_PATCH = "1";
    public static boolean Use_Fixed_IV = false; // true = disable sniffing, false = server can connect to itself
    public static boolean Use_Localhost = false; // true = packets are logged, false = others can connect to server
    public static final int MIN_MTS = 100; //lowest amount an item can be, GMS = 110
    public static final int MTS_BASE = 0; //+amount to everything, GMS = 500, MSEA = 1000
    public static final int MTS_TAX = 5; //+% to everything, GMS = 10
    public static final int MTS_MESO = 10000; //mesos needed, GMS = 5000
    public static final String SQL_USER = "root", SQL_PASSWORD = "";
    //master login is only used in GMS: fake account for localhost only
    //master and master2 is to bypass all accounts passwords only if you are under the IPs below
    //Channel Mob Disease Monitor Configuration
    public static final int MOB_STATUS_MONITOR_PROC = 200;      //Frequency in milliseconds between each proc on the mob disease monitor schedule.
    public static final int MOB_STATUS_MONITOR_LIFE = 84;       //Idle proc count the mob disease monitor is allowed to be there before closing it due to inactivity.
    public static final int MOB_STATUS_AGGRO_PERSISTENCE = 2;   //Idle proc count on aggro update for a mob to keep following the current controller, given him/her is the leading damage dealer.
    public static final int MOB_STATUS_AGGRO_INTERVAL = 5000;   //Interval in milliseconds between aggro logistics update.

    public static enum PlayerGMRank {

        NORMAL('@', 0),
        DONATOR('#', 1),
        GM('!', 2),
        ADMIN('!', 3);
        private char commandPrefix;
        private int level;

        PlayerGMRank(char ch, int level) {
            commandPrefix = ch;
            this.level = level;
        }

        public char getCommandPrefix() {
            return commandPrefix;
        }

        public int getLevel() {
            return level;
        }
    }

    public static enum CommandType {

        NORMAL(0),
        TRADE(1),
        POKEMON(2);
        private int level;

        CommandType(int level) {
            this.level = level;
        }

        public int getType() {
            return level;
        }
    }

    //Properties
    static {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("configuration.ini"));
            ServerConstants.HOST = p.getProperty("HOST");
            ServerConstants.LOCALSERVER = ServerConstants.HOST.startsWith("127.") || ServerConstants.HOST.startsWith("localhost");
            //Sql Database
            ServerConstants.DB_URL = p.getProperty("URL");
            ServerConstants.DB_USER = p.getProperty("DB_USER");
            ServerConstants.DB_PASS = p.getProperty("DB_PASS");
            System.out.println("Loaded configuration.ini.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load configuration.ini.");
            System.exit(0);
        }
    }

    public static List<MapleClient> getClients() {
        List<MapleClient> chrz = new ArrayList<>(clients);
        return chrz;
    }

    public static void addClient(MapleClient client) {
        if (!clients.contains(client)) {
            clients.add(client);
        }
    }

    public static boolean getClient(MapleClient client) {
        return clients.contains(client);
    }

    public static void removeClient(MapleClient client) {
        if (clients.contains(client)) {
            clients.remove(client);
        }
    }

    public static void addPool(long value) {
        if ((pool + value) < Long.valueOf(999999999999999999L)) {
            pool += value;
        } else {
            if (pool < Long.valueOf(999999999999999999L)) {
                pool = Long.valueOf(999999999999999999L);
            }
        }
    }

    public static void setPool(long value) {
        pool = value;
    }

    public static long getPool() {
        return pool;
    }
}
