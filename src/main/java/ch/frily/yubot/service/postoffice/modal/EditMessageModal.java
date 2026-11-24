package ch.frily.yubot.service.postoffice.modal;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.storage.SessionStorageService;
import lombok.Setter;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.attachmentupload.AttachmentUpload;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates a forum Post
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class EditMessageModal extends Modal {

    @Setter
    private Message message;

    @Override
    public String getId() {
        return "msg-edit-modal";
    }

    @Override
    public String getTitle() {
        return "Nachricht bearbeiten";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        String modalDescription = message.isUsingComponentsV2() ? """
                Überschreibe die Nachricht mit einem Container.
                -# Die Nachricht verwendet das ComponentsV2-Format, du kannst also ein container auswählen, aber kein Text definieren.
                """ : """
                Überschreibe die Nachricht mit einer TextNachricht und/oder einer Einbettung.
                -# Die Nachricht verwendet das standard MessageV1-Format, du kannst also die Nachricht und Einbettung bearbeiten/überschreiben.
                """;

        TextInput.Builder messageContent = TextInput.create("message", TextInputStyle.PARAGRAPH);
        messageContent.setRequiredRange(10, Message.MAX_CONTENT_LENGTH);
        messageContent.setRequired(false);
        messageContent.setPlaceholder("Nachricht eingeben...");
        if (!message.getContentRaw().isBlank()) {
            messageContent.setValue(message.getContentRaw());
        }

        String containerEmbedLabel = message.isUsingComponentsV2() ? "Container" : "Embed";
        StringSelectMenu.Builder containerEmbedSelect = StringSelectMenu.create("container-embed-select");
        containerEmbedSelect.setRequired(false);
        if (message.isUsingComponentsV2()) {
            StaticContainerRegistry.sendable().forEach(container -> {
                String option = buildOptionName(container);
                containerEmbedSelect.addOption(option, option);
            });
        } else {
            Arrays.stream(StaticEmbedRegistry.values()).forEach(embed -> {
                String option = buildOptionName(embed);
                containerEmbedSelect.addOption(option, option);
            });
        }

        List<ModalTopLevelComponent> modalChildren = new ArrayList<>();
        modalChildren.add(TextDisplay.of(modalDescription));
        if (!message.isUsingComponentsV2()) {
            modalChildren.add(Label.of(
                    "Textnachricht",
                    messageContent.build()
            ));
        }
        modalChildren.add(Label.of(
                containerEmbedLabel,
                containerEmbedSelect.build()
        ));

        return modalChildren;
    }

    private enum ComponentType {
        CONTAINER,
        EMBED
    }
    /**
     * composes the name for the container-embed select menu
     * <p>
     *     This is composed by "CONTAINER" & the enum constant name of the selected container:<br>
     *     <code>CONTAINER/MY_CONTAINER</code>
     * </p>
     */
    private String buildOptionName(StaticContainerRegistry containerRegistry) {
        return String.format("%s/%s", ComponentType.CONTAINER.name(), containerRegistry.name());
    }

    /**
     * composes the name for the container-embed select menu
     * <p>
     *     This is composed by "EMBED" & the enum constant name of the selected embed:<br>
     *     <code>CONTAINER/MY_CONTAINER</code>
     * </p>
     */
    private String buildOptionName(StaticEmbedRegistry embedRegistry) {
        return String.format("%s/%s", ComponentType.EMBED.name(), embedRegistry.name());
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        String messageId = getArgument(event.getCustomId(), "msgId");

        ModalMapping messageMapping = event.getValue("message");
        ModalMapping containerEmbeds = event.getValue("container-embed-select");

        String newMessageString = Objects.nonNull(messageMapping) ? messageMapping.getAsString() : null;
        String containerEmbedName = !containerEmbeds.getAsStringList().isEmpty() ? containerEmbeds.getAsStringList().getFirst() : null;

        event.deferReply(true).queue();
        event.getChannel().retrieveMessageById(messageId).queue(
                originalMessage -> {
                    try {
                        MessageEditData newMessageData = buildMessage(event, newMessageString, containerEmbedName);
                        originalMessage.editMessage(newMessageData).queue(
                                updatedMessage -> {
                                    event.getHook().editOriginalFormat("✅ %s wurde erfolgreich aktualisiert", updatedMessage.getJumpUrl()).queue();
                                },
                                exception -> {
                                    ExceptionHandler.handle(exception, event);
                                }
                        );
                    } catch (Exception exception) {
                        ExceptionHandler.handle(exception, event);
                    }
                },
                exception -> {
                    ExceptionHandler.handle(exception, event);
                });


    }

    /**
     * Build the new message
     * @param event the interaction event, to set the context of any container/embed
     * @param message the new message content
     * @param containerEmbedKey the new container/embed-key
     * @return the new message as {@link MessageCreateData}
     */
    private MessageEditData buildMessage(ModalInteractionEvent event, String message, String containerEmbedKey) {

        // sort the selected embeds/containers
        StaticContainerRegistry container = null;
        StaticEmbedRegistry embed = null;

        if (containerEmbedKey != null) {
            String type = containerEmbedKey.split("/")[0];
            String key = containerEmbedKey.split("/")[1];
            if (type.equals(ComponentType.CONTAINER.name())) {
                container = StaticContainerRegistry.valueOf(key);
            } else if (type.equals(ComponentType.EMBED.name())) {
                embed = StaticEmbedRegistry.valueOf(key);
            }
        }


        // Build the message
        List<String> clashingMessageComponents = new ArrayList<>(); // some components can't be used in the same message
        MessageEditBuilder messageBuilder = new MessageEditBuilder();
        AtomicBoolean hasContent = new AtomicBoolean(false);
        if (container != null) {
            ContainerContext ctx = ContainerContext.of(event);
            messageBuilder.setComponents(container.getContainer(ctx));
            messageBuilder.useComponentsV2();
            hasContent.set(true);

            if (message != null && !message.trim().isBlank()) {
                clashingMessageComponents.add("Textnachrichten sind nicht mit Containern kompatibel.");
            }
        } else {
            if (embed != null) {
                messageBuilder.setEmbeds(embed.getEmbed());
                hasContent.set(true);
            }
            if (message != null && !message.trim().isBlank()) {
                messageBuilder.setContent(message.trim());
                hasContent.set(true);
            } else {
                messageBuilder.setContent(null);
            }
        }

        if (!hasContent.get()) {
            clashingMessageComponents.add("Bitte gib ein Container, Embed oder eine Nachricht an. Leere Nachrichten können nicht gesendet werden.");
        }

        if (!clashingMessageComponents.isEmpty()) {
            throw new InvalidStateException("Die Nachricht konnte nicht überschriebnen werden.", String.join("\n", clashingMessageComponents));
        } else {
            return messageBuilder.build();
        }
    }
}
