package ch.frily.yubot.service.forummanager;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.service.forummanager.container.RegisterInterestRequestContainer;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the forum posts for special interest
 *
 * @author Aliz Rose Frily
 * @since 2.5.0
 */
@Slf4j
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

    /**
     * Request the approval of the interest registration by moderators
     * <p>
     *     The request handles the event reply itself
     * </p>
     * @param interest
     */
    public static void requestInterestApproval(Member initiator, SpecialInterest interest, Runnable onRequested) {
        RegisterInterestRequestContainer requestContainer = new RegisterInterestRequestContainer(initiator, interest);
        EnvKey.CHANNEL_SERVERCONFIG.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER)
                .sendMessageComponents(requestContainer.build()).useComponentsV2().setAllowedMentions(List.of()).queue(_ -> {
                    onRequested.run();
                });
    }
}
