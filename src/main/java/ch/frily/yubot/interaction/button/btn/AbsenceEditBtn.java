package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.AbsenceAddModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

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
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        Absence absence = AbsenceRepository.getAbsenceById(Integer.parseInt(getArgument(event.getComponentId(), "absence_id")));
        AbsenceAddModal editModal = new AbsenceAddModal();
        editModal.setAbsence(absence);
        event.replyModal(editModal.build()).queue();
    }
}
