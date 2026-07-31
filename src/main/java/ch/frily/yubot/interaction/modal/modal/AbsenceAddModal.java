package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.interaction.modal.IModal;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AbsenceAddModal implements IModal {
    @Override
    public String getTitle() {
        return "Abwesenheit anlegen";
    }

    @Override
    public String getId() {
        return "add-absence-modal";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        TextInput.Builder startTime = TextInput.create("start-time", TextInputStyle.SHORT);
        startTime.setRequiredRange(16, 16);
        startTime.setValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        startTime.setRequired(true);

        TextInput.Builder endTime = TextInput.create("end-time", TextInputStyle.SHORT);
        endTime.setRequiredRange(16, 16);
        endTime.setValue(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        endTime.setRequired(true);

        TextInput.Builder reason = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reason.setRequiredRange(10, 100);
        reason.setRequired(true);

        return List.of(
                Label.of("Startzeit (dd.MM.yyyy HH:mm)", startTime.build()),
                Label.of("Endzeit (dd.MM.yyyy HH:mm)", endTime.build()),
                Label.of("Begründung", reason.build()),
                Label.of("Anwesenheitsmeldung senden", Checkbox.of("absence-message", false)),
                TextDisplay.of("-# Die Abwesenheitsmeldung wird automatisch versendet, wenn dich jemand während deiner Abwesenheit erwähnt.")
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        event.reply("Modal executed").setEphemeral(true).queue();
    }
}
