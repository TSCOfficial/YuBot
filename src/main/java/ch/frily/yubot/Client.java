package ch.frily.yubot;

import ch.frily.yubot.database.Database;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.contextmenu.ContextMenuRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import ch.frily.yubot.interaction.select.SelectRegistry;
import ch.frily.yubot.listeners.InteractionListener;
import ch.frily.yubot.listeners.OnMessageReceived;
import ch.frily.yubot.listeners.OnReadyListener;
import ch.frily.yubot.listeners.GuildMemberUpdateListener;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import ch.frily.yubot.scheduler.SchedulerRegistry;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            config = loadConfig();

            Connection conn = Database.getInstance().connect();
            if (conn != null) {
                log.info("Database connected!");
            } else {
                throw new SQLException("Database could not be reached!");
            }
            Database.getInstance().disconnect();

            client = createClient();
            client.awaitReady();
            log.info("Application started successfully!");

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);
            // Load actions
            ContextMenuRegistry.getInstance().loadContextMenus();
            SlashCommandRegistry.getInstance().loadCommands();
            ButtonRegistry.getInstance().loadButtons();
            ModalRegistry.getInstance().loadModals();
            SelectRegistry.getInstance().loadSelects();

            // Register actions
            SchedulerRegistry.registerAll();
            List<CommandData> slashcommand = SlashCommandRegistry.getInstance().prepareCommandsForRegistry();
            List<CommandData> ctxcommand = ContextMenuRegistry.getInstance().prepareForRegistry();

            ArrayList<CommandData> allCommands = new ArrayList<>();
            allCommands.addAll(slashcommand);
            allCommands.addAll(ctxcommand);

            guild.updateCommands()
                    .addCommands(allCommands)
                    .queue();


        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    /**
     * Creates the JDA client
     * @return New JDA client
     */
    private JDA createClient() {
        JDABuilder jdaBuilder = JDABuilder.createDefault(config.get(EnvKey.CRED_TOKEN.name()));
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.MESSAGE_CONTENT);
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.enableCache(CacheFlag.ACTIVITY);
        jdaBuilder.setActivity(Activity.listening("Yu"));

        // Event listeners
        jdaBuilder.addEventListeners(InteractionListener.getInstance());
        jdaBuilder.addEventListeners(OnReadyListener.getInstance());
        jdaBuilder.addEventListeners(GuildMemberUpdateListener.getInstance());
        jdaBuilder.addEventListeners(OnMessageReceived.getInstance());
        return jdaBuilder.build();
    }

    /**
     * Load ..env-file configurations
     * @return {@link Dotenv} config object
     */
    private Dotenv loadConfig(){
        Dotenv dotenv = Dotenv.configure().load();

        for (EnvKey key : EnvKey.values()) {
            if (dotenv.get(key.name()) == null) {
                throw new IllegalStateException(String.format("Missing key-configuration for '%s'", key.name()));
            }
        }
        return dotenv;
    }
}
