package ch.frily.yubot.feature.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

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
                        String shortenedReplyMsg = shortenReplyMessage(originalMsgRef.getMessage().getContentRaw());
                        messageContentSB.append(String.format("> %s [[anzeigen]](%s)\n", shortenedReplyMsg, originalMsgRef.getMessage().getJumpUrl()));
                    }
                    messageContentSB.append(originalMessage.getContentRaw());
                    webhook.sendMessage(messageContentSB.toString()).setAllowedMentions(List.of()).queue( _ -> {
                        originalMessage.delete().queue();
                    });
                });

            });
        }
    }

    private static String shortenReplyMessage(String message) {
        log.info(message);
        message = message.replaceAll("\n", " ");
        if (message.length() > 100) {
            return message.substring(0, 100) + "...";
        }
        return message;
    }
}
