package ch.frily.yubot.util;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.InvalidStateException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public enum EnvKey {
    CRED_TOKEN(Client.getInstance().getConfig().get("CRED_TOKEN")),
    CRED_DB_URL(Client.getInstance().getConfig().get("CRED_DB_URL")),
    CRED_DB_USERNAME(Client.getInstance().getConfig().get("CRED_DB_USERNAME")),
    CRED_DB_PASSWORD(Client.getInstance().getConfig().get("CRED_DB_PASSWORD")),

    GUILD_YUSERVER("1045025807065698314"),

    ROLE_OWNER("1045025807065698320"),
    ROLE_SERVERLEITUNG("1433146444977803325"),
    ROLE_DEVLEITUNG("1433145883016564756"),
    ROLE_AWARENESSLEITUNG("1433144894184095856"),
    ROLE_STVAWARENESSLEITUNG("1544948247691272192"),
    ROLE_EVENTLEITUNG("1433145575607632044"),
    ROLE_STVEVENTLEITUNG("1544951707807785020"),
    ROLE_MODLEITUNG("1512005826888601710"),
    ROLE_STVMODLEITUNG("1544948580836446288"),
    ROLE_ORGA("1433164735167201524"),
    ROLE_MODERATOR("1045025807065698318"),
    ROLE_SUPPORTLEITUNG("1523729028245094610"),
    ROLE_STVSUPPORTLEITUNG("1544948474687000616"),
    ROLE_SUPPORT("1438622951963492482"),
    ROLE_AWARENESS("1163899965190459442"),
    ROLE_DEVELOPER("1433166243963080744"),
    ROLE_EVENT("1433165663446368407"),
    ROLE_DESIGN("1529824781195808868"),
    ROLE_ACTIVEMOD("1513639704870912130"),
    ROLE_EVERYONE("1045025807065698314"),
    ROLE_TWITCHMOD("1164290866605736026"),
    ROLE_ANTIFASHIST("1075430637646905424"),
    ROLE_YUTEAM("1249001079597699082"),
    ROLE_EROEFFNUNGSPING("1520720070802935929"),
    ROLE_EVENTPING("1299756366247755848"),
    ROLE_DIDSYSTEM("1526195902803480577"),

    CHANNEL_ANKUENDIGUNGEN("1516862980707450932"),
    CHANNEL_DASTEAM("1511987896389996675"),
    CHANNEL_SERVERGESCHLOSSEN("1513791699170627625"),
    CHANNEL_LOBBY("1435272551759613983"),
    CHANNEL_CLOSURELOGS("1513777649649193010"),
    CHANNEL_COMMUNITYEVENTS("1513887218056564806"),
    CHANNEL_TICKETLOGS("1435270898503647353"),
    CHANNEL_MODINTERN("1516042711273046087"),
    CHANNEL_SUPPORT("1435267124888338432"),
    CHANNEL_COUNTING("1512372143386792060"),
    CHANNEL_LIVESTAGE("1435268761476141160"),
    CHANNEL_STELLDICHVOR("1470270295985356911"),
    CHANNEL_FANART("1435272648157302954"),
    CHANNEL_MUSICDISCUSSION("1435272711449477255"),
    CHANNEL_LYRICLAB("1435272784769978432"),
    CHANNEL_QUEERSPACE("1435273082796249108"),
    CHANNEL_FEEDBACK("1516812990194778162"),
    CHANNEL_KONZERTBEGLEITUNG("1516863664542716095"),
    CHANNEL_YUKONZERTFESTIVALS("1516863435122671958"),
    CHANNEL_JOINTOCREATE("1516101652267008092"),
    CHANNEL_STARTHERE("1435266827864248412"),
    CHANNEL_KETTENBRIEF("1522310937284182132"),
    CHANNEL_ACTIVEMODERATION("1541846281528545370"),
    CHANNEL_VOICECHECK("1520419294540730448"),

    CATEGORY_TICKETS("1437589747634798672"),
    CATEGORY_ANSAGEN("1435264781828362330"),
    CATEGORY_LIVEVENTS("1435265311094870118"),
    CATEGORY_COMMUNITYFLOOR("1435265631183437895"),
    CATEGORY_VOICE("1470272362401235045"),
    CATEGORY_TEAMBEREICH("1435266533159866406"),

    TIMEZONE("Europe/Berlin");

    final String value;

    EnvKey(String value) {
        this.value = value;
    }

    /**
     * Get as {@link Guild}
     * @return Guild object
     */
    public Guild getAsGuild() {
        return Client.getInstance().getClient().getGuildById(this.value);
    }

    /**
     * Get as {@link Role}
     * @return Role object
     */
    public Role getAsRole() {
        return Client.getInstance().getClient().getRoleById(this.value);
    }

    /**
     * Get as {@link Channel} to be able to convert it further
     * @param type the channel type
     * @param guildId the guild to get the channel from
     * @return  object
     */
    public <T extends Channel> T getAsChannel(Class<T> type, long guildId) {
        Guild guild = Client.getInstance().getClient().getGuildById(guildId);

        return guild.getChannelById(type);
    }

    /**
     * Get a Role by its .env keyword
     * @param guildKeyword In the .env-file defined guild keyword
     * @param channelKeyword In the .env-file defined channel keyword
     * @return Role object
     */
    public static <T> T getChannelById(Class<T> type, EnvKey guildKeyword, EnvKey channelKeyword) {
        long guildId = checkAndResolve(guildKeyword, Long.class);
        long channelId = checkAndResolve(channelKeyword, Long.class);
        return getChannelById(type, guildId, channelId);
    }

    public static CompletableFuture<Message> getMessageById(String guildId, String channelId, String messageId ) {
        return getMessageById(Long.parseLong(guildId), Long.parseLong(channelId), Long.parseLong(messageId));
    }
    public static CompletableFuture<Message> getMessageById(long guildId, long channelId, long messageId) {
        MessageChannel channel = getChannelById(TextChannel.class, guildId, channelId);
        if (channel == null) {
            log.error("Channel {} nicht gefunden!", channelId);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Channel not found"));
        }
        return channel.retrieveMessageById(messageId)
                .submit()
                .whenComplete((message, error) -> {
                    if (error != null) {
                        log.error("retrieveMessageById failed for message '{}' in channel '{}': {}", messageId, channelId, error.getMessage());
                    }
                });
    }

    public static Category getCategoryById(EnvKey categoryKeyword) {
        long categoryId = checkAndResolve(categoryKeyword, Long.class);
        return Client.getInstance().getClient().getCategoryById(categoryId);
    }

    public static String getString(EnvKey keyword){
        return checkAndResolve(keyword, String.class);
    }

    public static ZoneId getZoneId(){
        return ZoneId.of(checkAndResolve(EnvKey.TIMEZONE, String.class));
    }

    /**
     * Checks the keyword for empty or null value, and resolves the key
     * @param keyword the keyword to check and resolve
     * @return Resolved value
     * @param <T> return type of the resolved value
     */
    private static <T> T checkAndResolve(EnvKey keyword, Class<T> type) {
        if (Objects.isNull(keyword.value) || Objects.equals(keyword.value, "")) {
            throw new InvalidStateException("Keyword '{}' is empty!");
        }


        if (type == String.class) return type.cast(keyword.value);
        if (type == Integer.class) return type.cast(Integer.parseInt(keyword.value));
        if (type == Long.class)    return type.cast(Long.parseLong(keyword.value));
        if (type == Boolean.class) return type.cast(Boolean.parseBoolean(keyword.value));
        if (type == Double.class)  return type.cast(Double.parseDouble(keyword.value));

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }
}
