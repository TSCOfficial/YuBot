package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.button.IButton;
import ch.frily.yubot.interaction.modal.modal.AbsenceAddModal;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class AbsenceAddBtn extends Button {

    @Override
    public String getId() {
        return "add-absence-btn";
    }

    @Override
    public String getLabel() {
        return "Abwesenheit anlegen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.replyModal(new AbsenceAddModal().build()).queue();
    }
}
