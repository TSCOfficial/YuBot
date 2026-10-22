package ch.frily.yubot.util;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public enum EnvKey {
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

    ROLE_ERIHM("1252235086162825307"),
    ROLE_ERSIE("1557052814528749639"),
    ROLE_SIEER("1557052913942265966"),
    ROLE_ERDEY("1557052468486217759"),
    ROLE_SIEIHR("1252235133344419911"),
    ROLE_SIEDEY("1557052600862642316"),
    ROLE_DEYDEREN("1252235170774253640"),
    ROLE_DEYER("1557052687370031104"),
    ROLE_DEYSIE("1557052730797990079"),
    ROLE_ES("1520503495747178647"),
    ROLE_ERES("1557052986654593066"),
    ROLE_SIEES("1557053031294705716"),
    ROLE_DEYES("1557053068770676878"),
    ROLE_ESER("1557053125372936312"),
    ROLE_ESSIE("1557053213444931634"),
    ROLE_ESDEY("1557053255115345952"),
    ROLE_ALLE("1252235265963982979"),
    ROLE_KEINE("1520503428336324738"),
    ROLE_ANDEREFRAGEN("1252235303218053120"),

    ROLE_DMOFFEN("1515033490754765000"),
    ROLE_DMFRAGEN("1515033685982838916"),
    ROLE_DM_GESCHLOSSEN("1515033546660773918"),

    CHANNEL_ANKUENDIGUNGEN("1516862980707450932"),
    CHANNEL_DASTEAM("1511987896389996675"),
    CHANNEL_SERVERGESCHLOSSEN("1513791699170627625"),
    CHANNEL_LOBBY("1435272551759613983"),
    CHANNEL_GEGENSEITIGESPUSCHEN("1516878625524416512"),
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
    CHANNEL_YUMEMES("1532092235124969612"),
    CHANNEL_INTERNATIONAL("1528152637948559481"),
    CHANNEL_ZITAT("1522320495544897746"),
    CHANNEL_DEMOS("1522688395787108483"),
    CHANNEL_BOTCHAT("1535033271954571457"),
    CHANNEL_QUEERSPACE("1435273082796249108"),
    CHANNEL_FEEDBACK("1516812990194778162"),
    CHANNEL_KONZERTBEGLEITUNG("1516863664542716095"),
    CHANNEL_YUKONZERTFESTIVALS("1516863435122671958"),
    CHANNEL_DISKUSSIONEN("1533195474121658510"),
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

    EMOJI_PRONOMEN_ERIHM("1557055355404689418"),
    EMOJI_PRONOMEN_ERSIE("1557055458710126653"),
    EMOJI_PRONOMEN_SIEER("1557055460883046480"),
    EMOJI_PRONOMEN_ERDEY("1557055495045390458"),
    EMOJI_PRONOMEN_SIEIHR("1557055392226218024"),
    EMOJI_PRONOMEN_SIEDEY("1557055454809428099"),
    EMOJI_PRONOMEN_DEYDEREN("1557055492088397975"),
    EMOJI_PRONOMEN_DEYER("1557055476943028254"),
    EMOJI_PRONOMEN_DEYSIE("1557055456877215774"),
    EMOJI_PRONOMEN_ES("1557055481380348124"),
    EMOJI_PRONOMEN_ERES("1557055463923912754"),
    EMOJI_PRONOMEN_SIEES("1557055466201157732"),
    EMOJI_PRONOMEN_DEYES("1557055468394782781"),
    EMOJI_PRONOMEN_ESER("1557055470336737334"),
    EMOJI_PRONOMEN_ESSIE("1557055472807182449"),
    EMOJI_PRONOMEN_ESDEY("1557055474749276352"),
    EMOJI_PRONOMEN_ALLE("1557055482886099076"),
    EMOJI_PRONOMEN_KEINE("1557055712293691422"),
    EMOJI_PRONOMEN_ANDEREFRAGEN("1557055479648092281"),

    TIMEZONE("Europe/Berlin");

    @Getter
    private final String value;

    EnvKey(String value) {
        this.value = value;
    }

    public Integer getAsInt() {
        return Integer.valueOf(this.value);
    }

    public Long getAsLong() {
        return Long.valueOf(this.value);
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

    public <T extends Channel> T getAsChannel(Class<T> type, EnvKey guildKey) {
        return getAsChannel(type, guildKey.getAsLong());
    }
    /**
     * Get as {@link Channel} to be able to convert it further
     * @param type the channel type
     * @param guildId the guild to get the channel from
     * @return  object
     */
    public <T extends Channel> T getAsChannel(Class<T> type, long guildId) {
        Guild guild = Client.getInstance().getClient().getGuildById(guildId);

        Objects.requireNonNull(guild);

        if (type.equals(TextChannel.class)) {
                return (T) guild.getTextChannelById(this.value);
        } else if (type.equals(VoiceChannel.class)) {
            return (T) guild.getVoiceChannelById(this.value);
        } else if (type.equals(ThreadChannel.class)) {
            return (T) guild.getThreadChannelById(this.value);
        } else if (type.equals(StageChannel.class)) {
            return (T) guild.getStageChannelById(this.value);
        } else if (type.equals(ForumChannel.class)) {
            return (T) guild.getForumChannelById(this.value);
        } else {
            throw new InvalidStateException(String.format("Channeltype '%s' is not supported", type.getName()));
        }
    }


    public CompletableFuture<Message> getAsMessage(String guildId, String channelId ) {
        return getAsMessage(Long.parseLong(guildId), Long.parseLong(channelId));
    }

    public CompletableFuture<Message> getAsMessage(long guildId, long channelId) {
        MessageChannel channel = getAsChannel(TextChannel.class, guildId);
        if (channel == null) {
            log.error("Channel {} nicht gefunden!", channelId);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Channel not found"));
        }
        return channel.retrieveMessageById(this.value)
                .submit()
                .whenComplete((message, error) -> {
                    if (error != null) {
                        log.error("retrieveMessageById failed for message '{}' in channel '{}': {}", this.value, channelId, error.getMessage());
                    }
                });
    }

    public Category getAsCategory() {
        return Client.getInstance().getClient().getCategoryById(this.value);
    }

    /**
     * Get a guild emoji
     * @return
     */
    public Emoji getAsEmoji() {
        return EnvKey.GUILD_YUSERVER.getAsGuild().getEmojiById(this.value);
    }

    public ZoneId getAsZoneId(){
        return ZoneId.of(this.value);
    }
}
