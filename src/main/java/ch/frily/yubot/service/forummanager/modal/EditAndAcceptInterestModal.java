package ch.frily.yubot.service.forummanager.modal;

import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.service.forummanager.InterestForumManager;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import lombok.Setter;
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
import java.util.Objects;

public class EditAndAcceptInterestModal extends Modal {

    @Setter
    public SpecialInterest interest;

    @Override
    public String getId() {
        return "edit-accept-interest-modal";
    }

    @Override
    public String getTitle() {
        return "Interest bearbeiten & freigeben";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        TextInput.Builder interestName = TextInput.create("interest-name", TextInputStyle.SHORT);
        interestName.setRequiredRange(4, ThreadChannel.MAX_NAME_LENGTH);
        interestName.setRequired(true);
        interestName.setPlaceholder("Name eingeben...");
        interestName.setValue(interest.getName());

        TextInput.Builder interestDescription = TextInput.create("interest-description", TextInputStyle.PARAGRAPH);
        interestDescription.setRequiredRange(25, 400);
        interestDescription.setRequired(true);
        interestDescription.setPlaceholder("Beschreibung eingeben...");
        interestDescription.setValue(interest.getDescription());

        return List.of(
                TextDisplay.of("Bearbeite das Interest bevor du es Freigibst. "),
                Label.of(
                        "Name",
                        "Dies wird, bei der Freigabe, als Post-Titel verwendet",
                        interestName.build()
                ),
                Label.of(
                        "Beschreibung",
                        "Dies wird, bei der Freigabe, als Post-Beschreibung (\"first message\") verwendet.",
                        interestDescription.build()
                ),
                TextDisplay.of("-# Original Poster (OP) wird von dieser Änderung informiert.")
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        event.deferReply(true).queue();
        String interestId = getArgument(event.getCustomId(), "interestId");
        SpecialInterest interest = SpecialInterestRepository.getInterestById(Integer.parseInt(interestId));

        if (Objects.nonNull(interest)) {
            interest.setRequestStatus(SpecialInterest.RequestStatus.APPROVED);
            SpecialInterestRepository.updateInterest(interest);
            InterestForumManager.acceptInterestRegistration(interest);

            event.getHook().editOriginal("✅ Interest wurde freigegeben.").queue();
            event.getMessage().delete().queue();
        } else {
            throw new NotFoundException("Es konnte keine Anfrage gefunden werden.");
        }
    }
}
