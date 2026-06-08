package ch.frily.yubot.database;

import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {

    private static Database instance;

    // init database constants
    private static final String DATABASE_DRIVER = "org.postgresql.Driver";
    private static final String DATABASE_URL = "EnvResolver.getString(EnvKey.CRED_DB_URL)";
    private static final String USERNAME = "EnvResolver.getString(EnvKey.CRED_DB_USERNAME)";
    private static final String PASSWORD = "EnvResolver.getString(EnvKey.CRED_DB_PASSWORD)";

    // init connection object
    private Connection connection;
    // init properties object
    private Properties properties;

    public static Database getInstance(){
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Get the connection properties
     * @return The connection properties
     */
    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
        }
        return properties;
    }

    /**
     * Connect to the database
     * @return The database connection
     */
    public Connection connect() {
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println(e);
            }
        }
        return connection;
    }

    /**
     * Disconnect the current database connection
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }


}