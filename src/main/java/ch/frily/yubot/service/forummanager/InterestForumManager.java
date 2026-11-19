package ch.frily.yubot.service.forummanager;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * Manages the forum posts for special interest
 *
 * @author Aliz Rose Frily
 * @since 2.5.0
 */
public class InterestForumManager {

    public static ForumChannel forumChannel = EnvKey.CHANNEL_SPECIALINTERESTS.getAsChannel(ForumChannel.class, EnvKey.GUILD_YUSERVER);

    /**
     * Accept an interest registration
     * <p>
     *     This creates the forum post
     * </p>
     * @param interest
     */
    public static void acceptInterestRegistration(SpecialInterest interest) {
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
        messageBuilder.addContent(interest.getDescription());
        messageBuilder.addContent(String.format("\n-# Interest wurde eingereicht von %s", interest.getOriginalPoster().getAsMention()));
        forumChannel.createForumPost(interest.getName(), messageBuilder.build()).queue();
    }
}
