package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.feature.DynamicMessageList;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.storage.SessionStorage;
import ch.frily.yubot.storage.StorageData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The modal for adding and editing absences
 */
@Slf4j
public class AbsenceAddModal extends Modal {

    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
    private static final int MINIMUM_ABSENCE_DURATION = 1440; // 24 hours in minutes

    private boolean isEditing = false;
    private Absence absence;
    private Member member;

    public void setAbsence(Absence absence) {
        this.isEditing = true;
        this.absence = absence;
        this.addArgument("absence_id", absence.id().toString());

        log.info("Modal identification: {}", getFullIdentification());
    }

    public void setMember(Member member) {
        this.member = member;
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
        AbsenceModalDataRecord absenceModalDataRecord = SessionStorage.getInstance().getValue("invalid-absence-clipboard", member, AbsenceModalDataRecord.class);

        TextInput.Builder startTime = TextInput.create("start-time", TextInputStyle.SHORT);
        startTime.setRequiredRange(16, 16);
        startTime.setValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        startTime.setRequired(true);
        if (isEditing) {
            startTime.setValue(absence.fromDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        if (absenceModalDataRecord != null) {
            startTime.setValue(absenceModalDataRecord.startTime());
        }


        TextInput.Builder endTime = TextInput.create("end-time", TextInputStyle.SHORT);
        endTime.setRequiredRange(16, 16);
        endTime.setValue(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        endTime.setRequired(true);
        if (isEditing) {
            endTime.setValue(absence.toDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        if (absenceModalDataRecord != null) {
            endTime.setValue(absenceModalDataRecord.endTime());
        }

        TextInput.Builder reason = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reason.setRequiredRange(5, 100);
        reason.setRequired(true);
        if (isEditing) {
            reason.setValue(absence.reason());
        }
        if (absenceModalDataRecord != null) {
            reason.setValue(absenceModalDataRecord.reason());
        }

        boolean absenceMessageChecked = false;
        if (isEditing) {
            absenceMessageChecked = absence.absenceMessage();
        }
        if (absenceModalDataRecord != null) {
            absenceMessageChecked = absenceModalDataRecord.sendNotice();
        }

        return List.of(
                Label.of(String.format("Startzeit (%s)", DATE_TIME_FORMAT), startTime.build()),
                Label.of(String.format("Endzeit (%s)", DATE_TIME_FORMAT), endTime.build()),
                Label.of("Begründung", reason.build()),
                Label.of("Anwesenheitsmeldung senden", Checkbox.of("absence-notice", absenceMessageChecked)),
                TextDisplay.of("-# Die Abwesenheitsmeldung wird automatisch versendet, wenn dich jemand während deiner Abwesenheit erwähnt.") // todo replace by delete button if edit, else show this message
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        String startTimeString = event.getValue("start-time").getAsString();
        String endTimeString = event.getValue("end-time").getAsString();
        String reason = event.getValue("reason").getAsString();
        boolean showNotice = event.getValue("absence-notice").getAsBoolean();
        // save data to session storage in case anything goes wrong
        AbsenceModalDataRecord absenceModalDataRecord = new AbsenceModalDataRecord(startTimeString, endTimeString, reason, showNotice);
        SessionStorage.getInstance().addStorage("invalid-absence-clipboard", event.getMember(), absenceModalDataRecord, 10);

        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            LocalDateTime endTime = LocalDateTime.parse(endTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));

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
            SessionStorage.getInstance().removeStorage("invalid-absence-clipboard", event.getMember()); // remove after successful upsert

            event.reply(responseText).setEphemeral(true).queue();

            DynamicMessageList.ABSENCES.update();

        } catch (DateTimeParseException dateTimeParseException) {
            throw new InvalidStateException("Ungltiges Zeitformat angegeben.", "Versuche es erneut und korrigiere das Format der Start- und Endzeit.");
        }

    }

    private boolean absenceTimeIsValid(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime.isBefore(LocalDateTime.now()) || endDateTime.isBefore(LocalDateTime.now())) {
            return false;
        }
        if (startDateTime.isAfter(endDateTime)) {
            return false;
        }
        if (startDateTime.plusMinutes(MINIMUM_ABSENCE_DURATION).isAfter(endDateTime)) {
            return false;
        }
        return true;
    }
}
