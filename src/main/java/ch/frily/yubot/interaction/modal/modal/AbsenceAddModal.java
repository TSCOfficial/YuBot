package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.service.absence.Absence;
import ch.frily.yubot.database.repository.AbsenceRepository;
import ch.frily.yubot.service.absence.AbsenceType;
import ch.frily.yubot.service.dynamicmsg.DynamicMessageList;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceApproveDeleteBtn;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceCancelDeleteBtn;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.storage.SessionStorage;
import ch.frily.yubot.util.Util;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.checkbox.Checkbox;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * The modal for adding and editing absences
 */
@Slf4j
public class AbsenceAddModal extends Modal {

    private static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm";
    private static final int MINIMUM_ABSENCE_DURATION = 720; // 12 hours in minutes
    private static final int MAX_ABSENCES_AT_SAME_TIME = 1000;

    private boolean isEditing = false;
    private Absence absence;
    @Setter
    private Member member;

    public void setAbsence(Absence absence) {
        this.isEditing = true;
        this.absence = absence;
        this.addArgument("absence_id", absence.id().toString());

        if (isEditing && absence.fromDateTime().isBefore(LocalDateTime.now())) {
            addArgument("bypass", true);
        }
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

        ModalTopLevelComponent startTimeLabel;

        TextInput.Builder startTime = TextInput.create("start-time", TextInputStyle.SHORT);
        startTime.setRequiredRange(16, 16);
        startTime.setRequired(true);

        if (absenceModalDataRecord != null) {
            startTime.setValue(absenceModalDataRecord.startTime());
        } else if (absence != null && absence.fromDateTime() != null){
            startTime.setValue(absence.fromDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        } else {
            startTime.setValue(LocalDateTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        startTimeLabel = Label.of(String.format("Startzeit (%s)", DATE_TIME_FORMAT), startTime.build());


        TextInput.Builder endTime = TextInput.create("end-time", TextInputStyle.SHORT);
        endTime.setRequiredRange(16, 16);
        endTime.setValue(LocalDateTime.now().plusMinutes(MINIMUM_ABSENCE_DURATION + 1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        endTime.setRequired(true);
        if (isEditing) {
            endTime.setValue(absence.toDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        if (absenceModalDataRecord != null) {
            endTime.setValue(absenceModalDataRecord.endTime());
        }
        Label endTimeLabel = Label.of(String.format("Endzeit (%s)", DATE_TIME_FORMAT), endTime.build());

        TextInput.Builder reason = TextInput.create("reason", TextInputStyle.PARAGRAPH);
        reason.setRequiredRange(5, 100);
        reason.setRequired(true);
        if (isEditing) {
            reason.setValue(absence.reason());
        }
        if (absenceModalDataRecord != null) {
            reason.setValue(absenceModalDataRecord.reason());
        }
        Label reasonLabel = Label.of("Begründung", reason.build());

        StringSelectMenu.Builder absenceType = StringSelectMenu.create("absence-type-select");
        for (AbsenceType type : AbsenceType.values()) {
            absenceType.addOption(type.getLabel(), type.name(), type.getDescription(), type.getEmoji());
        }
        if (isEditing) {
            absenceType.setDefaultOptions(SelectOption.of(absence.type().getLabel(), absence.type().name()));
        }
        if (absenceModalDataRecord != null) {
            AbsenceType selectedType = AbsenceType.valueOf(absenceModalDataRecord.type());
            absenceType.setDefaultOptions(SelectOption.of(selectedType.getLabel(), selectedType.name()));
        }
        Label absenceTypeLabel = Label.of("Abwesenheitsart auswählen", absenceType.build());

        boolean absenceMessageChecked = false;
        if (isEditing) {
            absenceMessageChecked = absence.absenceMessage();
        }
        if (absenceModalDataRecord != null) {
            absenceMessageChecked = absenceModalDataRecord.sendNotice();
        }
        Label absenceNoticeLabel = Label.of("Anwesenheitsmeldung senden", Checkbox.of("absence-notice", absenceMessageChecked));

        Label absenceDeleteLabel = Label.of("🗑️ Abwesenheit löschen", Checkbox.of("absence-delete", false));

        if (isEditing) {
            return List.of(
                    startTimeLabel,
                    endTimeLabel,
                    absenceTypeLabel,
                    absenceNoticeLabel,
                    absenceDeleteLabel
            );
        }
        return List.of(
                startTimeLabel,
                endTimeLabel,
                absenceTypeLabel,
                reasonLabel,
                absenceNoticeLabel
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        ModalMapping startTimeValue = Objects.requireNonNull(event.getValue("start-time"));
        ModalMapping endTimeValue = Objects.requireNonNull(event.getValue("end-time"));
        ModalMapping absenceTypeValue = Objects.requireNonNull(event.getValue("absence-type-select"));
        ModalMapping noticeValue = Objects.requireNonNull(event.getValue("absence-notice"));

        Absence existingAbsence = null;
        if (hasArgument(event.getModalId(), "absence_id")) {
            int id = Integer.parseInt(getArgument(event.getModalId(), "absence_id"));
            existingAbsence = AbsenceRepository.getAbsenceById(id);
        }
        boolean bypassStartTimeCheck = false;
        if (hasArgument(event.getModalId(), "bypass")) {
            bypassStartTimeCheck = Boolean.parseBoolean(getArgument(event.getModalId(), "bypass"));
        }



        String startTimeString = startTimeValue.getAsString();
        String endTimeString = endTimeValue.getAsString();
        String absenceTypeString = absenceTypeValue.getAsStringList().getFirst();
        String reason = Objects.nonNull(event.getValue("reason")) ? event.getValue("reason").getAsString() : Objects.requireNonNull(existingAbsence).reason();
        boolean showNotice = noticeValue.getAsBoolean();
        boolean deleteAbsence = Objects.nonNull(event.getValue("absence-delete")) && event.getValue("absence-delete").getAsBoolean();

        // Delete Absence option
        if (deleteAbsence && existingAbsence != null) {
            String confirmationMsg = deleteConfirmationMessage(existingAbsence);
            AbsenceApproveDeleteBtn approveBtn = new AbsenceApproveDeleteBtn();
            approveBtn.addArgument("absence_id", existingAbsence.id());
            AbsenceCancelDeleteBtn cancelBtn = new AbsenceCancelDeleteBtn();
            cancelBtn.addArgument("absence_id", existingAbsence.id());
            event.reply(confirmationMsg).addComponents(ActionRow.of(approveBtn.build(), cancelBtn.build())).setEphemeral(true).queue();
            return;
        }

        // save data to session storage in case anything goes wrong
        AbsenceModalDataRecord absenceModalDataRecord = new AbsenceModalDataRecord(startTimeString, endTimeString, absenceTypeString, reason, showNotice);
        SessionStorage.getInstance().addStorage("invalid-absence-clipboard", event.getMember(), absenceModalDataRecord, 10);

        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            LocalDateTime endTime = LocalDateTime.parse(endTimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            AbsenceType absenceType = AbsenceType.valueOf(absenceTypeString);

            String responseText = "Deine Abwesenheit wurde erfolgreich angelegt.";
            if (Objects.nonNull(existingAbsence)) {
                absence = new Absence(existingAbsence.id(), event.getMember(), startTime, endTime, absenceType, reason, showNotice, existingAbsence.createdAt(), LocalDateTime.now());
                responseText = "Deine Abwesenheit wurde erfolgreich aktualisiert.";
            } else {
                absence = new Absence(null, event.getMember(), startTime, endTime, absenceType, reason, showNotice, LocalDateTime.now(), LocalDateTime.now());
            }

            validateAbsenceDisplayCap(absence, startTime, endTime);
            absenceTimeIsValid(event.getMember(), existingAbsence, absence, startTime, endTime, bypassStartTimeCheck);

            AbsenceRepository.upsertAbsence(absence);
            SessionStorage.getInstance().removeStorage("invalid-absence-clipboard", event.getMember()); // remove after successful upsert

            event.reply(responseText).setEphemeral(true).queue();

            DynamicMessageList.ABSENCES.update();

        } catch (DateTimeParseException dateTimeParseException) {
            throw new InvalidStateException("Ungltiges Zeitformat angegeben.", "Versuche es erneut und korrigiere das Format der Start- und Endzeit.");
        }
    }

    private String deleteConfirmationMessage(Absence absence) {
        StringBuilder sb = new StringBuilder();
        sb.append("### Bestätige die Absenzlöschung").append("\n");
        sb.append(String.format("> -# **Absenzzeitraum**: <t:%d:F> bis <t:%d:F>", Util.toEpochSeconds(absence.fromDateTime()), Util.toEpochSeconds(absence.toDateTime()))).append("\n");
        sb.append(String.format("> -# **Absenztyp**: %s %s", absence.type().getEmoji().getFormatted(), absence.type().getLabel())).append("\n");
        sb.append(String.format("> -# **Begründung**: %s", absence.reason())).append("\n");
        sb.append("Bist du dir sicher dass du diese Absenz löschen möchtest?").append("\n");
        return sb.toString();
    }

    private void absenceTimeIsValid(Member member, Absence originalAbsence, Absence absenceToValidate, LocalDateTime startDateTime, LocalDateTime endDateTime, boolean bypassStartTimeCheck) throws SQLException {
        List<Absence> otherAbsences = new java.util.ArrayList<>(AbsenceRepository.getAbsencesByMemberAndDateSpan(member, startDateTime, endDateTime));
        Objects.requireNonNull(originalAbsence);
        Objects.requireNonNull(absenceToValidate);

        otherAbsences.removeIf(currentAbsence -> currentAbsence.id().equals(absenceToValidate.id()));

        if (originalAbsence.fromDateTime().isAfter(absenceToValidate.fromDateTime())){
            throw new InvalidStateException("Ungültige Zeitangabe.", "Die Startzeit darf nicht weiter in die Vergangenheit gesetzt werden.");
        }

        if (!otherAbsences.isEmpty()) {
            throw new InvalidStateException("Zeitraum bereits vergeben.", "Es existiert bereits eine Absenz im Zeitraum.");
        }
        if (!bypassStartTimeCheck && startDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidStateException("Ungültige Zeitangabe.", "Die Startzeit muss in der Zukunft liegen.");
        }
        if (endDateTime.isBefore(LocalDateTime.now())){
            throw new InvalidStateException("Ungültige Zeitangabe.", "Die Endzeit muss in der Zukunft liegen.");
        }
        if (startDateTime.isAfter(endDateTime)) {
            throw new InvalidStateException("Ungültige Zeitangabe.", "Die Startzeit muss vor der Endzeit liegen.");
        }
        if (startDateTime.plusMinutes(MINIMUM_ABSENCE_DURATION).isAfter(endDateTime)) {
            throw new InvalidStateException("Ungültige Zeitangabe.", "Die Dauer der Abwesenheit muss mindestens " + MINIMUM_ABSENCE_DURATION / 60 + " Stunden betragen.");
        }
    }

    /**
     * Check if the absence would exceed the maximum number ({@link #MAX_ABSENCES_AT_SAME_TIME}) of items that can be displayed at the same time on the container
     * @param startTime start date-time of the absence
     * @param endTime end date-time of the absence
     * @throws SQLException Database failure
     * @throws InvalidStateException when an absence already exist in the given timespan
     */
    private void validateAbsenceDisplayCap(Absence absenceToValidate, LocalDateTime startTime, LocalDateTime endTime) throws SQLException, InvalidStateException {
        LocalDate currentDay = startTime.toLocalDate();
        LocalDate lastDay = endTime.toLocalDate();

        while (!currentDay.isAfter(lastDay)) {
            LocalDateTime dayStart = currentDay.atStartOfDay();
            LocalDateTime dayEnd = currentDay.atTime(LocalTime.MAX);

            List<Absence> existingAbsences = AbsenceRepository.getAbsencesByDateSpan(dayStart, dayEnd);
            existingAbsences.removeIf(absence -> absence.id().equals(absenceToValidate.id()));
            int existingOnThisDay = existingAbsences.size();

            log.info("{}: {} / {}", currentDay, existingOnThisDay, MAX_ABSENCES_AT_SAME_TIME);

            if (existingOnThisDay >= MAX_ABSENCES_AT_SAME_TIME) {
                throw new InvalidStateException(
                        "Es können maximal " + MAX_ABSENCES_AT_SAME_TIME + " Abwesenheiten gleichzeitig angelegt werden.",
                        "Kontaktiere bitte deine Bereichsleitung und informiere sie über deine Abwesenheit.");
            }

            currentDay = currentDay.plusDays(1);
        }
    }
}
