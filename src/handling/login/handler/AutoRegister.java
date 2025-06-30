package handling.login.handler;

import client.LoginCrypto;
import client.MapleClient;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoRegister {

    private static final int ACCOUNTS_PER_IP = 10; //change the value to the amount of accounts you want allowed for each ip
    public static final boolean autoRegister = true; //enable = true or disable = false
    public static boolean success = false; // DONT CHANGE

    public static boolean getAccountExists(String login) {
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

    public static void createAccount(String login, String pwd, String eip) {
        String sockAddr = eip;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ipc = con.prepareStatement("SELECT SessionIP FROM accounts WHERE SessionIP = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ipc.setString(1, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
                ResultSet rs = ipc.executeQuery();
                if (rs.first() == false || rs.last() == true && rs.getRow() < ACCOUNTS_PER_IP) {
                    try {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, salt, email, birthday, macs, SessionIP) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                            ps.setString(1, login);
                            //ps.setString(2, LoginCrypto.hexSha1(pwd));
                            final String newSalt = LoginCrypto.makeSalt();
                            ps.setString(2, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
                            ps.setString(3, newSalt);
                            ps.setString(4, "no@email.provided");
                            ps.setString(5, "2008-04-07");
                            ps.setString(6, "00-00-00-00-00-00");
                            ///  ps.setInt(6, 123456);
                            ps.setString(7, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
                            ps.executeUpdate();
                        }

                        success = true;
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        return;
                    }
                } else {
                    success = false;
                }
                rs.close();
            }


            /*
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
             * 
             */
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
}
