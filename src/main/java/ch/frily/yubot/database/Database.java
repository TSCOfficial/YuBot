package ch.frily.yubot.database;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class Database {

    private static Database instance;

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
            properties.setProperty("user", Client.getInstance().getConfig().get("CRED_DB_USERNAME"));
            properties.setProperty("password", Client.getInstance().getConfig().get("CRED_DB_PASSWORD"));
        }
        return properties;
    }

    /**
     * Connect to the database
     * @return The database connection
     */
    public Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(Client.getInstance().getConfig().get("CRED_DB_URL"), getProperties());
            }
            return connection;
        } catch (SQLException e) {
            return ExceptionHandler.fail(e);
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