package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.profile.MessageHandling;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

/**
 * This modal allows a profile-user to edit a message sent from their profile-webhook
 */
public class EditWebhookMessageModal extends Modal {

    private Message message;

    public void setMessage(Message message) {
        this.message = message;
        addArgument("msg", message.getId());
        addArgument("channel", message.getChannel().getId());
    }

    @Override
    public String getId() {
        return "edit-webhook-msg";
    }

    @Override
    public String getTitle() {
        return "Nachricht bearbeiten";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {

        TextInput.Builder input = TextInput.create("message", TextInputStyle.PARAGRAPH);
        input.setRequiredRange(1, Message.MAX_CONTENT_LENGTH);
        input.setValue(message.getContentRaw());
        return List.of(
                Label.of("Nachricht", input.build())
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        event.deferReply(true).queue();
        String messageId = getArgument(event.getCustomId(), "msg");
        String channelId = getArgument(event.getCustomId(), "channel");

        String editedMessage = event.getValue("message").getAsString();

        EnvResolver.getMessageById(event.getGuild().getId(), channelId, messageId)
                .thenAccept(originalMessage -> {
                    MessageHandling.editMessage(originalMessage, editedMessage);
                    event.getHook().deleteOriginal().queue();
                })
                .exceptionally(exception -> {
                    return ExceptionHandler.fail(exception, event);
                });
    }
}
