package ch.frily.yubot.feature.profile;

import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Handles messages according to the existing profiles
 */
@Slf4j
public class MessageHandling {
    public static void handleIncomingMessage(Message originalMessage) throws SQLException, ClassNotFoundException {
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(originalMessage.getMember());
        if (!existingProfiles.isEmpty()) {
            Optional<Profile> useProfile = existingProfiles.stream().filter(Profile::isCurrentlyUsed).findFirst();
            Optional<Profile> proxyProfile = handleProxy(originalMessage);
            boolean usesProxy;
            if (proxyProfile.isPresent()) {
                useProfile = proxyProfile;
                usesProxy = true;
            } else {
                usesProxy = false;
            }

            useProfile.ifPresent(profile -> {
                Icon icon = null;
                try {
                    icon = new ImageProxy(profile.profilePicture()).downloadAsIcon().get();
                } catch (InterruptedException | ExecutionException e) {
                    ExceptionHandler.handle(e);
                }

                originalMessage.getChannel().asTextChannel().createWebhook(profile.name()).setAvatar(icon).queue(webhook -> {
                    // send message
                    StringBuilder messageContentSB = new StringBuilder();
                    MessageReference originalMsgRef = originalMessage.getMessageReference();
                    if (originalMsgRef != null &&originalMsgRef.getType() == MessageReference.MessageReferenceType.DEFAULT) {
                        String shortenedReplyMsg = sanitizeMessage(originalMsgRef.getMessage().getContentRaw());
                        String repliedToMember = "";
                        if (originalMsgRef.getMessage().getMember() != null) {
                            repliedToMember = String.format("**%s** ", originalMsgRef.getMessage().getMember().getEffectiveName());
                        }
                        messageContentSB.append(String.format("> -# %s%s [[anzeigen]](%s)\n",repliedToMember, shortenedReplyMsg, originalMsgRef.getMessage().getJumpUrl()));
                    }

                    String originalMsgString = originalMessage.getContentRaw();
                    if (usesProxy) {
                        originalMsgString = originalMsgString.replace(profile.proxy(), ""); // remove proxy chars from message
                    }
                    messageContentSB.append(originalMsgString);
                    webhook.sendMessage(messageContentSB.toString()).setAllowedMentions(List.of()).queue( _ -> {
                        originalMessage.delete().queue();
                        webhook.delete().queue();
                    });
                });
            });

        }
    }

    /**
     * Checks for proxy usage
     * <p>
     *     Using the message and all of the member's profiles, it searches for a matching proxy and, if found, uses this profile to send the message instead of the current active profile
     * </p>
     * @param originalMessage
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private static Optional<Profile> handleProxy(Message originalMessage) throws SQLException, ClassNotFoundException {
        Member member = originalMessage.getMember();
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(member);

        existingProfiles = existingProfiles.stream().filter(profile -> {
            if (profile.proxy().isBlank()) {
                return false;
            }
            return originalMessage.getContentRaw().startsWith(profile.proxy() + " ");
        }).toList();
        existingProfiles = existingProfiles.reversed();

        if (!existingProfiles.isEmpty()) {
            return Optional.ofNullable(existingProfiles.getFirst());
        } else {
            return Optional.empty();
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
