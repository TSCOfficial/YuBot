package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.storage.SessionStorage;

/**
 * This record is used to temporrarly save the data of a failed {@link AbsenceAddModal}.
 * <p>
 *     When an exception occurs during the execution of the {@link AbsenceAddModal}, the data is saved in this record and passed to the {@link SessionStorage}.<br>
 *     The system retrieves the data when the same user opens the {@link AbsenceAddModal} again to display the previously entered data.
 * </p>
 */
public record AbsenceModalDataRecord(String startTime, String endTime, String reason, boolean sendNotice) {
}
