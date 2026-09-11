package ch.frily.yubot.feature.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Handles messages according to the existing profiles
 */
@Slf4j
public class MessageHandling {
    public static void handleIncomingMessage(Message originalMessage) throws SQLException, ClassNotFoundException {
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(originalMessage.getMember());
        if (existingProfiles.size() > 0) {
            existingProfiles.stream().filter(profile -> profile.isCurrentlyUsed()).findFirst().ifPresent(profile -> {
                originalMessage.getChannel().asTextChannel().createWebhook(profile.name()).queue( webhook -> {
                    // send message
                    StringBuilder messageContentSB = new StringBuilder();
                    MessageReference originalMsgRef = originalMessage.getMessageReference();
                    if (originalMsgRef != null &&originalMsgRef.getType() == MessageReference.MessageReferenceType.DEFAULT) {
                        String shortenedReplyMsg = sanitizeMessage(originalMsgRef.getMessage().getContentRaw());
                        messageContentSB.append(String.format("> -# %s [[anzeigen]](%s)\n", shortenedReplyMsg, originalMsgRef.getMessage().getJumpUrl()));
                    }
                    messageContentSB.append(originalMessage.getContentRaw());
                    webhook.sendMessage(messageContentSB.toString()).setAllowedMentions(List.of()).queue( _ -> {
                        originalMessage.delete().queue();
                        webhook.delete().queue();
                    });
                });

            });
        }
    }

    /**
     * Clear message from some of the markdown format, so that it fits with the reply-syntaxt
     * <p>
     *     This replaces code-blocks by inline-code, replaces header-markdown (#, ##, ###) by bold text, removes quote-markdown (> ), replaces linebreaks with spaces
     * </p>
     * @param message
     * @return
     */
    private static String sanitizeMessage(String message) {
        if (message.length() > 70) {
            return message.substring(0, 70) + "...";
        }
        log.info(message);
        message = message.replaceAll("(?s)```(?:[a-zA-Z0-9_+-]*\\n)?(.*?)```", "`$1`");
        log.info(message);

        message = message.replaceAll("(?m)^>+\\h*", "");
        log.info(message);
        message = message.replaceAll("(?m)^-#+\\h*", "");
        log.info(message);
        message = message.replaceAll("(?m)^#{1,3}(?!#)\\h+(.*)$", "**$1**");
        log.info(message);

        message = message.replaceAll("\\s*\\n\\s*", "").trim();
        log.info(message);

        return message;
    }
}
