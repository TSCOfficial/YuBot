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
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class DenySubmissionReasonModal extends Modal {

    @Setter
    private SpecialInterest interest;

    @Override
    public String getId() {
        return "deny-interest-reason-modal";
    }

    @Override
    public String getTitle() {
        return "Interest ablehnen";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() throws SQLException {
        TextInput reasonInput = TextInput.create("reason", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Begründung eingeben...")
                .setRequiredRange(4, 200)
                .setRequired(true)
                .build();
        return List.of(
                TextDisplay.of("Gib eine Begründung für die Ablehnung vom Interest \"%s\" ein.".formatted(interest.getName())),
                Label.of(
                        "Begründung",
                        reasonInput
                )
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        ModalMapping reasonMapping = event.getValue("reason");

        event.deferReply(true).queue();
        String interestId = getArgument(event.getCustomId(), "interestId");
        SpecialInterest interest = SpecialInterestRepository.getInterestById(Integer.parseInt(interestId));

        if (Objects.nonNull(interest)) {
            InterestForumManager.denyInterestSubmission(interest, reasonMapping.getAsString());
            event.reply("✅ Interest \"%s\" wurde abgelehnt.".formatted(interest.getName())).setEphemeral(true).queue();
            event.getMessage().delete().queue();
        }else {
            throw new NotFoundException("Einsendung '%s' nicht gefunden.".formatted(interestId));
        }
    }
}
