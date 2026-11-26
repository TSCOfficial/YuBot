package ch.frily.yubot.service.forummanager.modal;

import ch.frily.yubot.EnvKey;
import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.embed.StaticEmbedRegistry;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.forummanager.entity.CreateCustomPostStorage;
import ch.frily.yubot.service.postoffice.DynamicMessageList;
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
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates a forum Post
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class CreateCustomPostModal extends Modal {

    @Setter
    private ForumChannel forumChannel;

    @Setter
    private Member initiator;

    @Override
    public String getId() {
        return "forum-create-modal";
    }

    @Override
    public String getTitle() {
        return "Post erstellen";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        // check for storage
        CreateCustomPostStorage storage = SessionStorageService.getInstance().getStorage(CreateCustomPostStorage.KEY, initiator, CreateCustomPostStorage.class);

        TextInput.Builder postTitle = TextInput.create("post-title", TextInputStyle.SHORT);
        postTitle.setRequiredRange(2, ThreadChannel.MAX_NAME_LENGTH);
        postTitle.setRequired(true);
        postTitle.setPlaceholder("Titel eingeben...");
        if (storage != null && !storage.title().isBlank()) {
            postTitle.setValue(storage.title());
        }

        TextInput.Builder postMessage = TextInput.create("post-msg", TextInputStyle.PARAGRAPH);
        postMessage.setRequiredRange(10, Message.MAX_CONTENT_LENGTH);
        postMessage.setRequired(false);
        postMessage.setPlaceholder("Nachricht eingeben...");
        if (storage != null && !storage.content().isBlank()) {
            postMessage.setValue(storage.content());
        }

        AttachmentUpload.Builder fileInput = AttachmentUpload.create("attachments");
        fileInput.setMaxValues(Message.MAX_FILE_AMOUNT);
        fileInput.setRequired(false);

        StringSelectMenu.Builder containerEmbedSelect = StringSelectMenu.create("container-embed-select");
        containerEmbedSelect.setRequired(false);
        StaticContainerRegistry.sendable().forEach(container -> {
            String option = buildOptionName(container);
            containerEmbedSelect.addOption(option, option);
        });
        Arrays.stream(StaticEmbedRegistry.values()).forEach(embed -> {
            String option = buildOptionName(embed);
            containerEmbedSelect.addOption(option, option);
        });
        if (storage != null && !storage.containerEmbedKey().isBlank()) {
            containerEmbedSelect.getOptions().stream().filter(option -> {
                return option.getValue().equals(storage.containerEmbedKey());
            }).findFirst().ifPresent(containerEmbedSelect::setDefaultOptions);
        }

        return List.of(
                TextDisplay.of("""
                Wähle aus was du senden möchtest.
                Du kannst eine Textnachricht, mit/ohne Anhängen, und/oder mit/ohne Embeds, senden. Wenn du Container auswählst, kannst du keine Textnachricht oder Einbettung senden.
                """),
                Label.of(
                        "Post Titel",
                        postTitle.build()
                ),
                Label.of(
                        "Textnachricht",
                        "Du must ein Text angeben, ausser du wählst Container an",
                        postMessage.build()
                ),
                Label.of(
                        "Anhänge / Dateien",
                        fileInput.build()
                ),
                Label.of(
                        "Container / Embeds",
                        containerEmbedSelect.build()
                )
        );
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
        String forumId = getArgument(event.getCustomId(), "forumId");

        ModalMapping titleMapping = event.getValue("post-title");
        ModalMapping messageMapping = event.getValue("post-msg");
        ModalMapping attachmentsMapping = event.getValue("attachments");
        ModalMapping containerEmbeds = event.getValue("container-embed-select");

        String title = titleMapping.getAsString();
        String message = messageMapping.getAsString();
        List<Message.Attachment> attachments = attachmentsMapping.getAsAttachmentList();
        String containerEmbedName = containerEmbeds.getAsStringList().getFirst();

        ForumChannel forumChannel = EnvKey.GUILD_YUSERVER.getAsGuild().getForumChannelById(forumId);

        event.deferReply(true).queue();

        // sort the selected embeds/containers
        StaticContainerRegistry container = null;
        StaticEmbedRegistry embed = null;

        String type = containerEmbedName.split("/")[0];
        String key = containerEmbedName.split("/")[1];
        if (type.equals(ComponentType.CONTAINER.name())) {
            container = StaticContainerRegistry.valueOf(key);
        } else if (type.equals(ComponentType.EMBED.name())) {
            embed = StaticEmbedRegistry.valueOf(key);
        }

        // Build the message
        List<String> clashingMessageComponents = new ArrayList<>(); // some components can't be used in the same message
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
        AtomicBoolean hasContent = new AtomicBoolean(false);
        if (container != null) {
            ContainerContext ctx = ContainerContext.of(event);
            messageBuilder.setComponents(container.getContainer(ctx));
            messageBuilder.useComponentsV2();
            hasContent.set(true);

            if (!message.trim().isBlank()) {
                clashingMessageComponents.add("Textnachrichten sind nicht mit Containern kompatibel.");
            }
        } else {
            if (embed != null) {
                messageBuilder.setEmbeds(embed.getEmbed());
                hasContent.set(true);
            }
            if (!message.trim().isBlank()) {
                messageBuilder.addContent(message.trim());
                hasContent.set(true);
            }
        }
        if (!attachments.isEmpty()) {
            List<FileUpload> files = attachments.stream().map(attachment -> {
                return attachment.getProxy().downloadAsFileUpload(attachment.getFileName());
            }).toList();
            messageBuilder.addFiles(files);
            hasContent.set(true);
        }
        if (!hasContent.get()) {
            clashingMessageComponents.add("Bitte gib ein Container, Embed oder eine Nachricht an. Leere Nachrichten können nicht gesendet werden.");
        }

        if (!clashingMessageComponents.isEmpty()) {
            // save data to storage
            CreateCustomPostStorage storage = new CreateCustomPostStorage(forumChannel, title, message, containerEmbedName);
            SessionStorageService.getInstance().addStorage(CreateCustomPostStorage.KEY, event.getMember(), storage, 10);

            event.getHook().editOriginal(String.join("\n", clashingMessageComponents)).queue();
        } else {
            forumChannel.createForumPost(title, messageBuilder.build()).queue(post -> {
                event.getHook().editOriginalFormat("✅ Der Post %s wurde erfolgreich erstellt.", post.getThreadChannel().getAsMention()).queue();
            });
        }
    }
}
