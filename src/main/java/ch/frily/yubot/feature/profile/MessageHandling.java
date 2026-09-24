package ch.frily.yubot.feature.profile;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.InvalidStateException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.IWebhookContainerUnion;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.api.utils.ImageProxy;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles messages according to the existing profiles
 */
@Slf4j
public class MessageHandling {

    public static String WEBHOOK_NAME = "YuBot Profile";

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
                var iconRef = new Object() {
                    Icon icon;
                };
                if (!profile.profilePicture().isBlank()) {
                    try {
                        iconRef.icon = new ImageProxy(profile.profilePicture()).downloadAsIcon().get();
                    } catch (InterruptedException | ExecutionException _) {
                        iconRef.icon = null;
                    }
                } else {
                    iconRef.icon = null;
                }

                fetchOrCreateWebhook(originalMessage.getChannel())
                    .thenAccept(webhook -> {
                        WebhookManager webhookManager = webhook.getManager();
                        webhookManager.setName(profile.name());
                        webhookManager.setAvatar(iconRef.icon);
                        webhookManager.queue(_ -> {
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
                            });
                        });
                    })
                    .exceptionally(exception -> {
                        throw new InvalidStateException(String.format("Webhook could not be fetched nor created: %s", exception.getMessage()));
                    });
            });
        }
    }

    public static void editMessage(Message originalMessage, String newContent) {
        fetchOrCreateWebhook(originalMessage.getChannel()).thenAccept(webhook -> {
            webhook.editMessageById(originalMessage.getId(), newContent).queue();
        });
    }


    public static CompletableFuture<Webhook> fetchOrCreateWebhook(MessageChannel channel) {
        IWebhookContainer webhookContainer;
        if (channel instanceof TextChannel) {
            webhookContainer = (TextChannel) channel;
        } else if (channel instanceof VoiceChannel) {
            webhookContainer = (VoiceChannel) channel;
        } else {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Channel-Typ unterstützt keine Webhooks: " + channel.getType()));
        }

        return getChannelWebhook(webhookContainer).thenCompose(optionalWebhook -> {
            if (optionalWebhook.isPresent()) {
                log.info("existing webhook: {}", optionalWebhook.get());
                return CompletableFuture.completedFuture(optionalWebhook.get());
            }

            return webhookContainer.createWebhook(WEBHOOK_NAME)
                    .submit()
                    .thenApply(newWebhook -> {
                        log.info("new webhook: {}", newWebhook);
                        return newWebhook;
                    });
        });
    }

    private static CompletableFuture<Optional<Webhook>> getChannelWebhook(IWebhookContainer webhookContainer) {
        if (webhookContainer == null) {
            throw new InvalidStateException("Webhook container is null");
        }

        return webhookContainer.retrieveWebhooks().submit().thenApply(webhooks -> {
                return webhooks.stream().filter(webhook -> webhook.getOwner().getId().equals(Client.getInstance().getClient().getSelfUser().getId())).findFirst();
            });
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
