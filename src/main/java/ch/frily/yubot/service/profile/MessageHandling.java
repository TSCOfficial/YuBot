package ch.frily.yubot.service.profile;

import ch.frily.yubot.Client;
import ch.frily.yubot.database.repository.ProfileMessageRepository;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.util.EnvKey;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.WebhookManager;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.ImageProxy;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Handles messages according to the existing profiles
 */
@Slf4j
public class MessageHandling {

    public static final String WEBHOOK_NAME = "YuBot Profile";

    public static void handleIncomingMessage(Message originalMessage) throws SQLException {
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
                if (Objects.nonNull(profile.profilePicture())) {
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
                                log.info(String.valueOf(originalMsgRef.getMessage().getMember()));
                                if (originalMsgRef.getMessage().getMember() != null) {
                                    repliedToMember = String.format("**%s** ", originalMsgRef.getMessage().getMember().getEffectiveName());
                                }

                                String attachmentIcon = "";
                                if (!originalMsgRef.getMessage().getAttachments().isEmpty()) {
                                    attachmentIcon = "<:image:1561701687302033418>";
                                }
                                messageContentSB.append(String.format("> -# %s%s %s[[anzeigen]](%s)\n",repliedToMember, shortenedReplyMsg, attachmentIcon, originalMsgRef.getMessage().getJumpUrl()));
                            }

                            String originalMsgString = originalMessage.getContentRaw();
                            if (usesProxy) {
                                originalMsgString = originalMsgString.replace(profile.proxy(), ""); // remove proxy chars from message
                            }
                            messageContentSB.append(originalMsgString);
                            WebhookMessageCreateAction<Message> messageCreateAction = webhook.sendMessage(messageContentSB.toString());
                            messageCreateAction.setAllowedMentions(List.of());
                            // collects and adds message attachmentsfile test
                            if (!originalMessage.getAttachments().isEmpty()) {
                                List<FileUpload> files = originalMessage.getAttachments().stream().map(attachment -> {
                                    return attachment.getProxy().downloadAsFileUpload(attachment.getFileName());
                                }).toList();
                                messageCreateAction.setFiles(files);
                            }
                            messageCreateAction.queue( msg -> {
                                ProfileMessage profileMsg = new ProfileMessage(msg, profile, originalMessage.getChannel(), msg.getMember());
                                try {
                                    ProfileMessageRepository.create(profileMsg);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
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
                return CompletableFuture.completedFuture(optionalWebhook.get());
            }

            return webhookContainer.createWebhook(WEBHOOK_NAME)
                    .submit()
                    .thenApply(newWebhook -> {
                        return newWebhook;
                    });
        });
    }

    private static CompletableFuture<Optional<Webhook>> getChannelWebhook(IWebhookContainer webhookContainer) {
        if (webhookContainer == null) {
            throw new InvalidStateException("Webhook container is null");
        }

        return webhookContainer.retrieveWebhooks().submit().thenApply(webhooks -> {
                return webhooks.stream().filter(webhook -> Objects.requireNonNull(webhook.getOwner()).getId().equals(Client.getInstance().getClient().getSelfUser().getId())).findFirst();
            });
    }

    /**
     * Checks for proxy usage
     * <p>
     *     Using the message and all of the member's profiles, it searches for a matching proxy and, if found, uses this profile to send the message instead of the current active profile
     * </p>
     * @param originalMessage the original message sent by the member
     * @return the profile assigned to the given proxy
     * @throws SQLException Database failure
     */
    private static Optional<Profile> handleProxy(Message originalMessage) throws SQLException {
        Member member = originalMessage.getMember();
        List<Profile> existingProfiles = ProfileRepository.getProfilesFromAccount(member);

        existingProfiles = existingProfiles.stream().filter(profile -> {
            if (Objects.isNull(profile.proxy())) {
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
     * @param message the raw message
     * @return a cleaned up version of the message, that prevents weird markdown-styling
     */
    private static String sanitizeMessage(String message) {
        if (message.length() > 70) {
            return message.substring(0, 70) + "...";
        }
        message = message.replaceAll("(?s)```(?:[a-zA-Z0-9_+-]*\\n)?(.*?)```", "`$1`");

        message = message.replaceAll("(?m)^>+\\h*", "");
        message = message.replaceAll("(?m)^-#+\\h*", "");
        message = message.replaceAll("(?m)^#{1,3}(?!#)\\h+(.*)$", "**$1**");

        message = message.replaceAll("\\s*\\n\\s*", "").trim();

        return message;
    }
}
