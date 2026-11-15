package ch.frily.yubot.service.forummanager.modal;

import ch.frily.yubot.interaction.modal.Modal;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

/**
 * Modal to request all infos for the new interest
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class RegisterNewInterestModal extends Modal {
    @Override
    public String getId() {
        return "request-interest-modal";
    }

    @Override
    public String getTitle() {
        return "Neues Interesse registrieren";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        TextInput.Builder interestName = TextInput.create("interest-name", TextInputStyle.SHORT);
        interestName.setRequiredRange(4, ThreadChannel.MAX_NAME_LENGTH);
        interestName.setRequired(true);
        interestName.setPlaceholder("Name eingeben...");

        TextInput.Builder interestDescription = TextInput.create("interest-description", TextInputStyle.PARAGRAPH);
        interestDescription.setRequiredRange(25, 400);
        interestDescription.setRequired(true);
        interestDescription.setPlaceholder("Beschreibung eingeben...");

        return List.of(
                TextDisplay.of("""
        Um ein neues Interest registrieren zu können musst ein Namen, sowie einer Beschreibung des Themas, angegeben werden.
        -# Das Serverteam wird die Anfrage anschliessend bearbeiten und annehmen, oder ablehnen. In beiden Fällen wirst du benachrichtigt.
        """),
                Label.of(
                        "Name",
                        interestName.build()
                ),
                Label.of(
                        "Beschreibung",
                        interestDescription.build()
                )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {

    }
}
