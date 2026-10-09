package ch.frily.yubot.service.absence.button;

import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.service.absence.Absence;
import ch.frily.yubot.database.repository.AbsenceRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.service.absence.modal.AbsenceAddModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.Objects;

public class AbsenceEditBtn extends Button {
    @Override
    public String getId() {
        return "edit-absence-btn";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public EmojiUnion getEmoji() {
        return Emoji.fromFormatted("✏️");
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, NullPointerException {
        Absence absence = AbsenceRepository.getAbsenceById(Integer.parseInt(getArgument(event.getComponentId(), "absence_id")));
        if (absence.member().getId().equals(Objects.requireNonNull(event.getMember()).getId())) {
            AbsenceAddModal editModal = new AbsenceAddModal();
            editModal.setAbsence(absence);
            editModal.setMember(event.getMember());
            event.replyModal(editModal.build()).queue();
        } else {
            throw new PermissionDeniedException("Du kannst nur deine Abwesenheiten bearbeiten.");
        }

    }
}
