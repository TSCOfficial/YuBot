package ch.frily.yubot.util;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.ClientException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Resolve Discord related IDs
 */
@Slf4j
public class EnvResolver {

    /**
     * Get a Guild by its integer ID
     * @param guildId
     * @return Guild object
     */
    public static Guild getGuildById(long guildId) {
        return Client.getInstance().getClient().getGuildById(guildId);
    }

    /**
     * Get a Guild by its .env keyword
     * @param keyword In the .env-file defined keyword
     * @return Guild object
     */
    public static Guild getGuildById(EnvKey keyword) {
        long guildId = checkAndResolve(keyword, Long.class);
        return getGuildById(guildId);
    }

    /**
     * Get a Role by its integer ID
     * @param roleId
     * @return Role object
     */
    public static Role getRoleById(long roleId) {
        return Client.getInstance().getClient().getRoleById(roleId);
    }

    /**
     * Get a Channel by its .env keyword
     * @param keyword In the .env-file defined keyword
     * @return Channel object
     */
    public static Role getRoleById(EnvKey keyword) {
        long roleId = checkAndResolve(keyword, Long.class);
        return getRoleById(roleId);
    }

    /**
     * Get a Channel by its integer ID
     * @param channelId
     * @return  object
     */
    public static <T> T getChannelById(Class<T> type, long guildId, long channelId) {
        Guild guild = getGuildById(guildId);

        if (type.equals(TextChannel.class)) {
            return (T) guild.getTextChannelById(channelId);
        } else if (type.equals(VoiceChannel.class)) {
            return (T) guild.getVoiceChannelById(channelId);
        } else {
            return (T) guild.getGuildChannelById(channelId);
        }
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

    public static CompletableFuture<Message> getMessageById(long guildId, long channelId, long messageId) {
        MessageChannel channel = getChannelById(TextChannel.class, guildId, channelId);
        if (channel == null) {
            log.error("Channel {} nicht gefunden!", channelId);
            return CompletableFuture.failedFuture(new IllegalArgumentException("Channel not found"));
        }
        return channel.retrieveMessageById(messageId)
                .submit()  // ✅ führt die Action aus und gibt CompletableFuture zurück
                .whenComplete((message, error) -> {
                    if (error != null) {
                        log.error("retrieveMessageById fehlgeschlagen: {}", error.getMessage());
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
     * @param keyword
     * @return Resolved value
     * @param <T> Returntype of the resolved value
     */
    private static <T> T checkAndResolve(EnvKey keyword, Class<T> type) {
        if (Objects.equals(keyword, "")) {
            throw new IllegalArgumentException("Illegal keyword");
        }

        String value = Client.getInstance().getConfig().get(keyword.name());


        if (Objects.equals(value, "") || value == null) throw new ClientException(String.format("Keyword '%s' is null", keyword.name()));
        if (type == String.class) return type.cast(value);
        if (type == Integer.class) return type.cast(Integer.parseInt(value));
        if (type == Long.class)    return type.cast(Long.parseLong(value));
        if (type == Boolean.class) return type.cast(Boolean.parseBoolean(value));
        if (type == Double.class)  return type.cast(Double.parseDouble(value));

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

}
