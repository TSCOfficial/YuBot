package ch.frily.yubot.database;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Database {

    private static Database instance;

    // init database constants
    private static final String DATABASE_DRIVER = "org.postgresql.Driver";

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
            properties.setProperty("user", EnvResolver.getString(EnvKey.CRED_DB_USERNAME));
            properties.setProperty("password", EnvResolver.getString(EnvKey.CRED_DB_PASSWORD));
        }
        return properties;
    }

    /**
     * Connect to the database
     * @return The database connection
     */
    public Connection connect() {
        try {
            if (connection == null) {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(EnvResolver.getString(EnvKey.CRED_DB_URL), getProperties());
            }
            return connection;
        } catch (Exception exception) {
            return ExceptionHandler.fail(exception);
        }
    }

    /**
     * Disconnect the current database connection
     */
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }


}