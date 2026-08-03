package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.interaction.ArgumentComponent;
import ch.frily.yubot.interaction.modal.IModal;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

/**
 * The modal for adding and editing absences
 */
@Slf4j
public class AbsenceAddModal extends Modal {

    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";

    private boolean isEditing = false;
    private Absence absence;

    public void setAbsence(Absence absence) {
        this.isEditing = true;
        this.absence = absence;
        this.addArgument("absence_id", absence.id().toString());

        log.info("Modal identification: {}", getFullIdentification());
    }

    @Override
    public String getTitle() {
        if (isEditing)
            return "Abwesenheit bearbeiten";
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
        startTime.setValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        startTime.setRequired(true);
        if (isEditing) {
            startTime.setValue(absence.fromDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }

        TextInput.Builder endTime = TextInput.create("end-time", TextInputStyle.SHORT);
        endTime.setRequiredRange(16, 16);
        endTime.setValue(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        endTime.setRequired(true);
        if (isEditing) {
            endTime.setValue(absence.toDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }

        TextInput.Builder reason = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reason.setRequiredRange(10, 100);
        reason.setRequired(true);
        if (isEditing) {
            reason.setValue(absence.reason());
        }

        boolean absenceMessageChecked = false;
        if (isEditing) {
            absenceMessageChecked = absence.absenceMessage();
        }

        log.info("Is edit getComponents: " + isEditing);

        return List.of(
                Label.of(String.format("Startzeit (%s)", DATE_TIME_FORMAT), startTime.build()),
                Label.of(String.format("Endzeit (%s)", DATE_TIME_FORMAT), endTime.build()),
                Label.of("Begründung", reason.build()),
                Label.of("Anwesenheitsmeldung senden", Checkbox.of("absence-message", absenceMessageChecked)),
                TextDisplay.of("-# Die Abwesenheitsmeldung wird automatisch versendet, wenn dich jemand während deiner Abwesenheit erwähnt.")
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        String startTimeString = event.getValue("start-time").getAsString();
        LocalDateTime startTime = LocalDateTime.parse(startTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        String endTimeString = event.getValue("end-time").getAsString();
        LocalDateTime endTime = LocalDateTime.parse(endTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        String reason = event.getValue("reason").getAsString();
        boolean showNotice = event.getValue("absence-message").getAsBoolean();

        Absence absence = null;
        String responseText = "Deine Abwesenheit wurde erfolgreich angelegt.";
        if (hasArgument(event.getModalId(), "absence_id")) {
            int id = Integer.parseInt(getArgument(event.getModalId(), "absence_id"));
            Absence existingAbsence = AbsenceRepository.getAbsenceById(id);
            absence = new Absence(existingAbsence.id(), event.getMember(), startTime, endTime, reason, showNotice, existingAbsence.createdAt(), LocalDateTime.now());
            responseText = "Deine Abwesenheit wurde erfolgreich aktualisiert.";
        } else {
            absence = new Absence(null, event.getMember(), startTime, endTime, reason, showNotice, LocalDateTime.now(), LocalDateTime.now());
        }


        AbsenceRepository.upsertAbsence(absence);

        event.reply(responseText).setEphemeral(true).queue();

        DynamicMessageList.ABSENCES.update();
    }
}
