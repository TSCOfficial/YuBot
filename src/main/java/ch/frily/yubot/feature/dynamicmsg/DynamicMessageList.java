package ch.frily.yubot.feature.dynamicmsg;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.container.ticket.TicketPanelContainer;
import ch.frily.yubot.database.repository.DynamicMessageRepository;
import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.embed.teamlist.TeamlistEmbed;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.Getter;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Define an embed or container as a dynamic message to update itself.
 * <br>
 * <b>Usage:</b>
 * <pre><code>
 *     DynamicMessageList.<ENUM_ITEM>.update(<context>);
 *     // Example
 *     DynamicMessageList.ABSENCES.update(ContainerContext.defaults());
 * </code></pre>
 */
public enum DynamicMessageList {

    TICKET_PANEL(
            StaticContainerRegistry.TICKET_PANEL.name(),
            DynamicMessageType.CONTAINER,
            null,
            ctx -> {
                return new TicketPanelContainer().build();
            }
    ),
    TEAMLIST(
            StaticEmbedRegistry.TEAMLIST.name(),
            DynamicMessageType.EMBED,
            ctx -> {
                return new TeamlistEmbed().build();
            },
            null
    ),
    ABSENCES(
            StaticContainerRegistry.ABSENCE_OVERVIEW.name(),
            DynamicMessageType.CONTAINER,
            null,
            StaticContainerRegistry.ABSENCE_OVERVIEW::getContainer
    ),
    ACTIVE_MOD_CONTROL(
            StaticContainerRegistry.ACTIVE_MOD_CONTROL.name(),
            DynamicMessageType.CONTAINER,
            null,
            StaticContainerRegistry.ACTIVE_MOD_CONTROL::getContainer
    );

    @Getter
    private final String registryName;

    private final DynamicMessageType type;
    private final Function<ContainerContext, MessageEmbed> embedSupplier;
    private final Function<ContainerContext, List<Container>> containerSupplier;

    DynamicMessageList(
            String registryName,
            DynamicMessageType type,
            Function<ContainerContext, MessageEmbed> embedSupplier,
            Function<ContainerContext, List<Container>> containerSupplier
    ) {
        this.registryName = registryName;
        this.type = type;
        this.embedSupplier = embedSupplier;
        this.containerSupplier = containerSupplier;
    }

    /**
     * Check if a given embed or container is a dynamic message by its registry name
     * @param registryName
     * @return
     */
    public static boolean isDynamic(String registryName) {
        return Arrays.stream(values())
                .anyMatch(dynamicMessage -> dynamicMessage.registryName.equals(registryName));
    }

    /**
     * Get a dynamic message by its registry name
     * @param registryName
     * @return
     */
    public static DynamicMessageList fromRegistryName(String registryName) {
        return Arrays.stream(values())
                .filter(dynamicMessage -> dynamicMessage.registryName.equals(registryName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Remember a message as a dynamic message
     * @param message the message to remember
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void remember(Message message) throws SQLException, ClassNotFoundException {
        DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message));
    }

    /**
     * Update the dynamic message
     * <br>
     * If no dynamic message could be retrieved, the reference is used to send a new message to the original channel.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void update() throws SQLException, ClassNotFoundException {
        update(ContainerContext.defaults());
    }

    /**
     * Update the dynamic message
     * <br>
     * If no dynamic message could be retrieved, the reference is used to send a new message to the original channel.
     * @param context the arguments the embed/container gets built with
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void update(ContainerContext context) throws SQLException, ClassNotFoundException {
        DynamicMessageRepository.DynamicMessageReference reference =
                DynamicMessageRepository.getDynamicMessageReference(name());

        DynamicMessage.retrieve(reference.name(), reference.channelId(), reference.messageId())
                .thenAccept(dynamicMessage -> update(dynamicMessage, context))
                .exceptionally(exception -> {
                    sendNewMessage(reference, context);
                    return null;
                });
    }

    /**
     * Update the dynamic message
     * <br>
     * If a message could not be found, the reference is used to send a new message to the original channel.
     * @param dynamicMessage
     */
    public void update(DynamicMessage dynamicMessage, ContainerContext context) {
        if (type == DynamicMessageType.EMBED) {
            dynamicMessage.message()
                    .editMessageEmbeds(embedSupplier.apply(context))
                    .queue(
                            ThrowingConsumer.wrap(null, message -> DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message))),
                            exception -> {
                                ExceptionHandler.handle(exception);
                                sendNewMessage(
                                        new DynamicMessageRepository.DynamicMessageReference(
                                                name(),
                                                dynamicMessage.message().getChannel().getIdLong(),
                                                dynamicMessage.message().getIdLong()
                                        ),
                                        context
                                );
                            }
                    );
            return;
        }

        if (type == DynamicMessageType.CONTAINER) {
            dynamicMessage.message()
                    .editMessageComponents(containerSupplier.apply(context))
                    .useComponentsV2()
                    .setAllowedMentions(List.of())
                    .queue(
                            ThrowingConsumer.wrap(null, message -> DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message))),
                            exception -> {
                                ExceptionHandler.handle(exception);
                                sendNewMessage(
                                        new DynamicMessageRepository.DynamicMessageReference(
                                                name(),
                                                dynamicMessage.message().getChannel().getIdLong(),
                                                dynamicMessage.message().getIdLong()
                                        ),
                                        context
                                );
                            }
                    );
        }
    }

    /**
     * Send a new dynamic message to the stored channel when the old message is missing or unreachable.
     * @param reference stored dynamic message reference
     * @param context arguments passed to the embed/container builder
     */
    private void sendNewMessage(DynamicMessageRepository.DynamicMessageReference reference, ContainerContext context) {
        try {
            long guildId = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER).getIdLong();
            MessageChannel channel = EnvResolver.getChannelById(MessageChannel.class, guildId, reference.channelId());

            if (channel == null) {
                ExceptionHandler.handle(new IllegalStateException(
                        "Dynamic message channel " + reference.channelId() + " for " + name() + " not found."
                ));
                return;
            }

            if (type == DynamicMessageType.EMBED) {
                channel.sendMessageEmbeds(embedSupplier.apply(context))
                        .queue(
                                ThrowingConsumer.wrap(null, message -> DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message))),
                                ExceptionHandler::handle
                        );
                return;
            }

            if (type == DynamicMessageType.CONTAINER) {
                channel.sendMessageComponents(containerSupplier.apply(context))
                        .useComponentsV2()
                        .setAllowedMentions(List.of())
                        .queue(
                                ThrowingConsumer.wrap(null, message -> DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message))),
                                ExceptionHandler::handle
                        );
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    private enum DynamicMessageType {
        EMBED,
        CONTAINER
    }
}