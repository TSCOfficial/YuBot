package ch.frily.yubot;

import ch.frily.yubot.interaction.button.ButtonRegistry;
import ch.frily.yubot.interaction.modal.ModalRegistry;
import ch.frily.yubot.listeners.InteractionListener;
import ch.frily.yubot.listeners.OnReadyListener;
import ch.frily.yubot.listeners.GuildMemberUpdateListener;
import ch.frily.yubot.interaction.command.SlashCommandRegistry;
import ch.frily.yubot.util.EnvKey;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@Slf4j
public class Client {

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
            client = createClient();
            client.awaitReady();
            log.info("Application started successfully!");

            // Load/start stuff

            SlashCommandRegistry.getInstance().loadCommands();
            SlashCommandRegistry.getInstance().registerAll();
            ButtonRegistry.getInstance().loadButtons();
            ModalRegistry.getInstance().loadModals();

        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Creates the JDA client
     * @return New JDA client
     */
    private JDA createClient() {
        JDABuilder jdaBuilder = JDABuilder.createDefault(config.get(EnvKey.CRED_TOKEN.name()));
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES);
        jdaBuilder.setStatus(OnlineStatus.IDLE);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        jdaBuilder.setActivity(Activity.listening("Yu"));
        // Event listeners
        jdaBuilder.addEventListeners(InteractionListener.getInstance());
        jdaBuilder.addEventListeners(OnReadyListener.getInstance());
        jdaBuilder.addEventListeners(GuildMemberUpdateListener.getInstance());
        return jdaBuilder.build();
    }

    /**
     * Load .env-file configurations
     * @return {@link Dotenv} config object
     */
    private Dotenv loadConfig(){
        return Dotenv.configure().load();
    }
}
