package ch.frily.yubot;

import ch.frily.yubot.database.Database;
import ch.frily.yubot.event.*;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.ServiceRegistry;
import ch.frily.yubot.util.EnvKey;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.reflections.Reflections.log;

@Slf4j
public class Client {

    // JDA seems to be missing this exception - gets thrown when the bot can't send a message to the user
    public static final int NO_MUTUAL_GUILD_EXCEPTION = 50278;

    private static Client instance;

    @Getter
    private JDA client;

    @Getter
    private Dotenv config;

    /**
     * Singleton
     * @return Get existing or create instance
     */
    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public static void main(String[] args) {
        getInstance().setup();
    }

    /**
     * Creates and connects every needed thing so that the bot can run normally
     */
    public void setup() {
        try {
            config = Dotenv.configure().load();

            Connection conn = Database.getInstance().connect();
            if (conn != null) {
                log.info("Database connected!");

                PreparedStatement stmt = conn.prepareStatement("""
                SELECT current_user, current_schemas(true), current_setting('search_path')
                """);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    log.info(rs.getString(1));
                    log.info(rs.getString(2));
                    log.info(rs.getString(3));
                }
            } else {
                throw new SQLException("Database could not be reached!");
            }
            Database.getInstance().disconnect();

            client = createClient();
            client.awaitReady();
            log.info("Application started successfully!");

            // Load actions
            ServiceRegistry.getInstance().load();

        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    /**
     * Creates the JDA client
     * @return New JDA client
     */
    private JDA createClient() {
        JDABuilder jdaBuilder = JDABuilder.createDefault(Client.getInstance().getConfig().get("CRED_TOKEN"));
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT);
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI);
        jdaBuilder.setActivity(Activity.customStatus(config.get("STATUS")));


        // Event listeners
        EventRegistry.getLISTENERS().forEach(jdaBuilder::addEventListeners);

        return jdaBuilder.build();
    }
}
