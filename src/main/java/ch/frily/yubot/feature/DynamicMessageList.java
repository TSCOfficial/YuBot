package ch.frily.yubot.feature;

import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.container.TicketPanelContainer;
import ch.frily.yubot.embed.StaticEmbedRegistry;
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
import java.util.function.Supplier;

/**
 * Define an embed or container as a dynamic message to update itself.
 * <br>
 * <b>Usage:</b>
 * <pre><code>
 *     DynamicMessageList.<ENUM_ITEM>.update(<arg1, arg2, ...>);
 *     // Example
 *     DynamicMessageList.TICKET_PANEL.update(true);
 * </code></pre>
 */
public enum DynamicMessageList {

    TICKET_PANEL(
            StaticContainerRegistry.TICKET_PANEL.name(),
            DynamicMessageType.CONTAINER,
            null,
            args -> {
                boolean isOpen = requireArg(args, 0, Boolean.class);
                return new TicketPanelContainer(isOpen).build();
            }
    );

    @Getter
    private final String registryName;

    private final DynamicMessageType type;
    private final Function<Object[], MessageEmbed> embedSupplier;
    private final Function<Object[], List<Container>> containerSupplier;

    DynamicMessageList(
            String registryName,
            DynamicMessageType type,
            Function<Object[], MessageEmbed> embedSupplier,
            Function<Object[], List<Container>> containerSupplier
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
    public void update(Object... args) throws SQLException, ClassNotFoundException {
        DynamicMessageRepository.DynamicMessageReference reference =
                DynamicMessageRepository.getDynamicMessageReference(name());

        DynamicMessage.retrieve(reference.name(), reference.channelId(), reference.messageId())
                .thenAccept(dynamicMessage -> update(dynamicMessage, args))
                .exceptionally(exception -> {
                    sendNewMessage(reference, args);
                    return null;
                });
    }

    /**
     * Update the dynamic message
     * <br>
     * If a message could not be found, the reference is used to send a new message to the original channel.
     * @param dynamicMessage
     */
    public void update(DynamicMessage dynamicMessage, Object... args) {
        if (type == DynamicMessageType.EMBED) {
            dynamicMessage.message()
                    .editMessageEmbeds(embedSupplier.apply(args))
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
                                        args
                                );
                            }
                    );
            return;
        }

        if (type == DynamicMessageType.CONTAINER) {
            dynamicMessage.message()
                    .editMessageComponents(containerSupplier.apply(args))
                    .useComponentsV2()
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
                                        args
                                );
                            }
                    );
        }
    }

    /**
     * Send a new dynamic message to the stored channel when the old message is missing or unreachable.
     * @param reference stored dynamic message reference
     * @param args arguments passed to the embed/container builder
     */
    private void sendNewMessage(DynamicMessageRepository.DynamicMessageReference reference, Object... args) {
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
                channel.sendMessageEmbeds(embedSupplier.apply(args))
                        .queue(
                                ThrowingConsumer.wrap(null, message -> DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message))),
                                ExceptionHandler::handle
                        );
                return;
            }

            if (type == DynamicMessageType.CONTAINER) {
                channel.sendMessageComponents(containerSupplier.apply(args))
                        .useComponentsV2()
                        .queue(
                                ThrowingConsumer.wrap(null, message -> DynamicMessageRepository.upsertDynamicMessage(new DynamicMessage(name(), message))),
                                ExceptionHandler::handle
                        );
            }
        } catch (Exception exception) {
            ExceptionHandler.handle(exception);
        }
    }

    /**
     * Check arguments for a dynamic message
     * @param args
     * @param index
     * @param type
     * @return
     * @param <T>
     */
    private static <T> T requireArg(Object[] args, int index, Class<T> type) {
        if (args.length <= index) {
            throw new IllegalArgumentException("Missing argument at index " + index + " of type " + type.getSimpleName());
        }

        Object value = args[index];

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Invalid argument at index " + index + ". Expected " + type.getSimpleName()
                            + ", got " + (value == null ? "null" : value.getClass().getSimpleName())
            );
        }

        return type.cast(value);
    }

    private enum DynamicMessageType {
        EMBED,
        CONTAINER
    }
}