/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ec2box.manage.db;

import com.ec2box.manage.model.Auth;
import com.ec2box.manage.util.DBUtils;
import com.ec2box.manage.util.EncryptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * DAO to login administrative users
 */
public class AuthDB {

    private AuthDB() {
    }
    public static final String SELECT_FROM_USERS_WITH_AUTH_TOKEN = "select * from users where enabled=true and auth_token=?";

    /**
     * returns admin login object based on auth token
     *
     * @param authToken auth token string
     */
    public static Auth getAdminLogin(String authToken) {
        Auth auth = null;
        if (authToken != null && !authToken.trim().equals("")) {

            Connection con = null;
            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement(SELECT_FROM_USERS_WITH_AUTH_TOKEN);
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    auth = new Auth();
                    auth.setId(rs.getLong("id"));
                    auth.setAuthToken(rs.getString("auth_token"));
                    auth.setUsername(rs.getString("username"));

                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
            DBUtils.closeConn(con);
        }

        return auth;
    }

    /**
     * login user and return auth token if valid login
     *
     * @param auth username and password object
     * @return auth token if success
     */
    public static String loginAdmin(Auth auth) {
        String authToken = null;


        Connection con = null;
        try {
            con = DBUtils.getConn();
            //get salt for user
            String salt = getSaltByUsername(con, auth.getUsername());
            //login
            PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and username=? and password=?");
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, EncryptionUtil.hash(auth.getPassword() + salt));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                auth.setId(rs.getLong("id"));

                authToken = UUID.randomUUID().toString();
                auth.setAuthToken(authToken);

                //set auth token
                updateAdmin(con, auth);


            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return authToken;

    }


    /**
     * checks to see if user is an admin based on auth token
     *
     * @param authToken auth token string
     */
    public static boolean isAdmin(String authToken) {

        boolean isAdmin = false;

        Connection con = null;
        if (authToken != null && !authToken.trim().equals("")) {

            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement(SELECT_FROM_USERS_WITH_AUTH_TOKEN);
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    isAdmin = true;

                }
                DBUtils.closeRs(rs);

                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DBUtils.closeConn(con);
        return isAdmin;


    }

    /**
     * updates the users table based on login id
     *
     * @param con   DB connection
     * @param auth username and password object
     */
    private static void updateAdmin(Connection con, Auth auth) {


        try {
            String salt = EncryptionUtil.generateSalt();
            PreparedStatement stmt = con.prepareStatement("update users set username=?, password=?, auth_token=?, salt=? where id=?");
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, EncryptionUtil.hash(auth.getPassword() + salt));
            stmt.setString(3, auth.getAuthToken());
            stmt.setString(4, salt);
            stmt.setLong(5, auth.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates password for admin using auth token
     */
    public static boolean updatePassword(Auth auth) {
        boolean success = false;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            String prevSalt=getSaltByAuthToken(con, auth.getAuthToken());
            PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ? and password like ?");
            stmt.setString(1, auth.getAuthToken());
            stmt.setString(2, EncryptionUtil.hash(auth.getPrevPassword()+prevSalt));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String salt = EncryptionUtil.generateSalt();
                stmt = con.prepareStatement("update users set password=?, salt=? where auth_token like ?");
                stmt.setString(1, EncryptionUtil.hash(auth.getPassword()+salt));
                stmt.setString(2, salt);
                stmt.setString(3, auth.getAuthToken());
                stmt.execute();
                success = true;
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return success;
    }


    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @param con DB connection
     * @return user id
     */
    public static Long getUserIdByAuthToken(Connection con, String authToken) {


        Long userId=null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and auth_token like ?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userId=rs.getLong("id");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return userId;

    }

    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @return user id
     */
    public static Long getUserIdByAuthToken(String authToken) {

        Long userId=null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            userId=getUserIdByAuthToken(con, authToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return userId;

    }
    /**
     * checks to see if user is an admin based on auth token
     *
     * @param authToken auth token string
     * @return user type if authorized, null if not authorized
     */
    public static String isAuthorized(String authToken) {

        String authorized = null;

        Connection con = null;
        if (authToken != null && !authToken.trim().equals("")) {

            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement(SELECT_FROM_USERS_WITH_AUTH_TOKEN);
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    authorized = rs.getString("user_type");

                }
                DBUtils.closeRs(rs);

                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DBUtils.closeConn(con);
        return authorized;


    }

    /**
     * returns the shared secret based on user id
     *
     * @param userId user id
     * @return auth object
     */
    public static String getSharedSecret(Long userId) {

        String sharedSecret = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from users where id like ?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sharedSecret = EncryptionUtil.decrypt(rs.getString("otp_secret"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return sharedSecret;

    }

    /**
     * updates shared secret based on auth token
     *
     * @param secret    OTP shared secret
     * @param authToken auth token
     */
    public static void updateSharedSecret(String secret, String authToken) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set otp_secret=? where auth_token=?");
            stmt.setString(1, EncryptionUtil.encrypt(secret));
            stmt.setString(2, authToken);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


    /**
     * get salt by user name
     *
     * @param con DB connection
     * @param username username
     * @return salt
     */
    private static String getSaltByUsername(Connection con, String username) {

        String salt = "";
        try {
            PreparedStatement stmt = con.prepareStatement("select salt from users where enabled=true and username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("salt") != null) {
                salt = rs.getString("salt");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return salt;
    }



    /**
     * get salt by authentication token
     *
     * @param con DB connection
     * @param authToken auth token
     * @return salt
     */
    private static String getSaltByAuthToken(Connection con, String authToken) {

        String salt = "";
        try {
            PreparedStatement stmt = con.prepareStatement("select salt from users where enabled=true and auth_token=?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("salt") != null) {
                salt = rs.getString("salt");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return salt;
    }

}
