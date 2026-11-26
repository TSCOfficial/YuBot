package ch.frily.yubot.util;

import ch.frily.yubot.exception.InvalidStateException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.text.Normalizer;
import java.time.Month;
import java.util.regex.Pattern;

/**
 * Message Utilities, such as Channel & Message helpers and String formating
 *
 * @author Aliz Rose Frily
 * @since 2.5.1
 */
public class MessageUtil {

    /**
     * Get the {@link MessageChannel} from a generic {@link GuildChannel}, to be able to execute message actions
     * <ul>
     *     Messageing channels are:
     *     <li>{@link ChannelType#TEXT}</li>
     *     <li>{@link ChannelType#VOICE}</li>
     *     <li>{@link ChannelType#NEWS}</li>
     *     <li>{@link ChannelType#GUILD_NEWS_THREAD}, {@link ChannelType#GUILD_PUBLIC_THREAD}, {@link ChannelType#GUILD_PRIVATE_THREAD}</li>
     * </ul>
     * @param guildChannel
     * @return
     * @throws InvalidStateException when {@link GuildChannel#getType()} is not compatible with messageing channels, or if {@link GuildChannel#getType()} is {@link ChannelType#UNKNOWN}
     */
    public static MessageChannel getMessageChannelFromGuild(GuildChannel guildChannel) {
        Guild guild = guildChannel.getGuild();
        return switch (guildChannel.getType()) {
            case TEXT -> guild.getTextChannelById(guildChannel.getId());
            case VOICE -> guild.getVoiceChannelById(guildChannel.getId());
            case NEWS -> guild.getNewsChannelById(guildChannel.getId());
            case STAGE -> guild.getStageChannelById(guildChannel.getId());
            case GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> guild.getThreadChannelById(guildChannel.getId());
            case UNKNOWN -> throw new InvalidStateException("The channel type is unknown");
            default -> throw new InvalidStateException("The channel '%s' could not be cast to 'MessageChannel'");
        };
    }

    /**
     * Normalize strings
     * @param s
     * @return
     */
    public static String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // Akzente weg
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Escapes characters in a text that are interpreted as Markdown on Discord.
     * @param input The string to escape
     * @return The escaped string
     */
    public static String escapeMarkdown(String input) {
        if (input == null) return null;

        return input
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace("|", "\\|");
    }

    public static String translateMonth(Month month) {
        return switch (month) {
            case JANUARY -> "Januar";
            case FEBRUARY -> "Februar";
            case MARCH -> "März";
            case APRIL -> "April";
            case MAY -> "Mai";
            case JUNE -> "Juni";
            case JULY -> "Juli";
            case AUGUST -> "August";
            case SEPTEMBER -> "September";
            case OCTOBER -> "Oktober";
            case NOVEMBER -> "November";
            case DECEMBER -> "Dezember";
        };
    }

    /**
     * Extracts UTF-16 text from a given input string
     * <p>
     *     Keeps a-Z, 0-9 & punctuation<br>
     *     Discards emojis, non-UTF-16 characters etc
     * </p>
     * @param text The input string to extract text from.
     * @return The extracted text and Markdown escaped
     */
    public static String extractText(String text) {
        Pattern pattern = Pattern.compile("[^\\p{L}\\p{N}\\-\\s\\*]");
        if (text == null || text.isBlank()) return text;
        String roleName = pattern.matcher(text).replaceAll("").trim();
        return escapeMarkdown(roleName);
    }
}
