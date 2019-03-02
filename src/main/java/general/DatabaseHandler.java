package main.java.general;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseHandler {

    private Connection conn = null;
    private Session session = null;

    private String strSshUser, strSshHost, strRemoteHost, strSshPassword;
    private String databaseUser, databasePassword;

    private boolean connectionFailed;

    /**
     * Creates and stores DatabaseHandler instance
     */
    private static class DBHolder {
        private static final DatabaseHandler INSTANCE = new DatabaseHandler();
    }

    /**
     * Private constructor, assigns user data and connects with database
     */
    private DatabaseHandler() {
        String dataPath = "@../../resources/inputData.txt";
        getUserData(dataPath);
        sshTunnelling();
        try {
            conn = databaseConn();
        } catch (Exception e) {
            e.printStackTrace();
            connectionFailed = true;
        }
    }

    /**
     * Reading user data required to ssh tunneling and database connection
     */
    private void getUserData(String path) {
        try (BufferedReader in  = new BufferedReader(new FileReader(path))) {
            strSshUser = in.readLine();
            strSshHost = in.readLine();
            strRemoteHost = in.readLine();
            strSshPassword = in.readLine();

            databaseUser = in.readLine();
            databasePassword = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            connectionFailed = true;
        }
    }

    /**
     * @return class instance
     */
    public static DatabaseHandler getInstance() {
        return DBHolder.INSTANCE;
    }

    /**
     * Making ssh tunneling
     */
    private void sshTunnelling() {
        int localPort = 3366;
        int remotePort = 5432;
        try {
            final JSch jsch = new JSch();
            session = jsch.getSession(strSshUser, strSshHost, 22);
            session.setPassword(strSshPassword);

            final Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            System.out.println("Establishing ssh connection...");

            session.connect();
            session.setPortForwardingL(localPort, strRemoteHost, remotePort);
        } catch (JSchException e) {
            e.printStackTrace();
            connectionFailed = true;
        }
    }

    /**
     * Making database connection
     * @return connection object
     * @throws Exception during database connection
     */
    private Connection databaseConn() throws Exception {
        String url = "jdbc:postgresql://localhost:3366/";

        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(url, databaseUser, databasePassword);
        System.out.println("Database connection established");
        return conn;
    }

    /**
     * Closing database connection. Triggering after closing stage.
     */
    void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
                System.out.println("Database connection terminated");
                session.disconnect();
                session = null;
                System.out.println("SSH disconnected");
            } catch (Exception e) {
                System.out.println("Close connection problem");
            }
        }
    }

    /**
     * @return database connection
     */
    public Connection getConnection() {
        return conn;
    }

    public boolean isConnectionFailed() {
        return connectionFailed;
    }
}
