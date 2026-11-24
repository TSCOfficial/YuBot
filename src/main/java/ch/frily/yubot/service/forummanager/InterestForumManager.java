package ch.frily.yubot.service.forummanager;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.forummanager.container.NewInterestSubmittedContainer;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * Accept an interest submission
     * <p>
     *     This creates the forum post
     * </p>
     * @param interest
     */
    public static void acceptInterestSubmission(User moderator, SpecialInterest interest) {
        MessageCreateBuilder postMessage = new MessageCreateBuilder();
        postMessage.addContent(interest.getDescription());
        postMessage.addContent(String.format("\n-# Interest wurde von %s eingereicht", interest.getOriginalPoster().getAsMention()));
        postMessage.setAllowedMentions(List.of());
        forumChannel.createForumPost(interest.getName(), postMessage.build()).queue(post -> {
            post.getThreadChannel().addThreadMember(interest.getOriginalPoster()).queue();
            try {
                interest.setPost(post.getThreadChannel());
                interest.setRequestStatus(SpecialInterest.RequestStatus.APPROVED);
                interest.setModerator(moderator);
                SpecialInterestRepository.updateInterest(interest);

                if (interest.isNotifyOP()) {
                    StringBuilder acceptNotificationMessage = new StringBuilder();
                    acceptNotificationMessage.append("## Special-Interest freigegeben\n");
                    acceptNotificationMessage.append(String.format("Dein Special-Interest \"%s\" wurde freigegeben und ist nun unter %s auffindbar.", interest.getName(), interest.getPost().getJumpUrl()));
                    interest.notifyOP(acceptNotificationMessage);
                }
            } catch (SQLException e) {
                ExceptionHandler.handle(e);
            }
        });
    }

    public static void denyInterestSubmission(User moderator, SpecialInterest interest, String reason) throws SQLException {
        interest.setRequestStatus(SpecialInterest.RequestStatus.DENIED);
        interest.setDenyReason(reason);
        interest.setModerator(moderator);
        SpecialInterestRepository.updateInterest(interest);

        if (interest.isNotifyOP()) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("## Special-Interest abgelehnt\n");
            messageBuilder.append("Deine Anfrage für das Thema **%s** wurde leider abgelehnt.\n".formatted(interest.getName()));
            messageBuilder.append("Grund: %s".formatted(reason));
            interest.notifyOP(messageBuilder);
        }
    }

    /**
     * Request the approval of the interest submission by moderators
     * <p>
     *     The request handles the event reply itself
     * </p>
     * @param initiator the user who submitted the interest
     * @param interest the interest to be approved
     * @param onRequested method to execute when the submission request has been sent
     */
    public static void requestInterestApproval(Member initiator, SpecialInterest interest, Runnable onRequested) {
        NewInterestSubmittedContainer requestContainer = new NewInterestSubmittedContainer(initiator, interest);
        EnvKey.CHANNEL_SERVERCONFIG.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER)
                .sendMessageComponents(requestContainer.build()).useComponentsV2().setAllowedMentions(List.of()).queue(_ -> {
                    onRequested.run();
                });
    }

    /**
     * Search for similar interests by name, with a custom threshold
     * @return
     * @throws SQLException
     */
    public static List<SpecialInterest> getSimilarInterests(String term, double threshold) throws SQLException {
        List<SpecialInterest> approvedInterests = SpecialInterestRepository.getInterests(SpecialInterest.RequestStatus.APPROVED);
        Map<String, SpecialInterest> terms = approvedInterests.stream().collect(Collectors.toMap(SpecialInterest::getName, Function.identity()));
        List<String> similarTerms = Util.fuzzySearch(term, terms.keySet().stream().toList(), threshold);
        return approvedInterests.stream().filter(interest -> similarTerms.contains(Util.normalize(interest.getName()))).toList();
    }

    /**
     * Search for similar interests by name
     * <p>
     *     This uses the default threshold ({@link Util#FUZZY_SEARCH_DEFAULT_THRESHOLD})
     * </p>
     * @return
     * @throws SQLException
     */
    public static List<SpecialInterest> getSimilarInterests(String term) throws SQLException {
        return getSimilarInterests(term, 0);
    }
}
