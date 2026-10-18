package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class EventReminderRepository {

    /**
     * Get the event information
     * <p>
     *     The events are organized in a map covering their
     * </p>
     * @param eventId the ID of the event to check
     * @return True if the reminder was already sent, false if not.
     * @throws SQLException Database failure
     * @throws NotFoundException Event not found
     */
    public static Boolean reminderAlreadySent(String eventId) throws SQLException, NotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.where(Table.EventReminderColumn.ID, DatabaseQuery.Operator.EQUALS, eventId);
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            return rs.getBoolean(Table.EventReminderColumn.IS_REMINDER_SENT.getColumn());
        }
        throw new NotFoundException("Event not found");
    }

    /**
     * Changes the "is reminder sent" status
     * @param eventId the ID of the event to modify
     * @param status the new status to define
     * @throws SQLException Database failure
     */
    public static void setStatus(String eventId, boolean status) throws SQLException {
        createIfMissing(eventId);
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.where(Table.EventReminderColumn.ID, DatabaseQuery.Operator.EQUALS, eventId);
        query.update(Table.EventReminderColumn.IS_REMINDER_SENT, status);
        query.executeQuery();
    }

    /**
     * Creates the event-reminder entry
     * @param eventId the ID of the event to save
     * @throws SQLException Database failure
     */
    public static void create(String eventId) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.insert(Table.EventReminderColumn.ID, eventId);
        query.insert(Table.EventReminderColumn.IS_REMINDER_SENT, false);
        query.executeQuery();
    }

    /**
     * Creates the event-reimder entry for the given event if it does not exist in the database
     * @param eventId the ID of the event to save
     * @throws SQLException Database failure
     */
    public static void createIfMissing(String eventId) throws SQLException {
        try {
            reminderAlreadySent(eventId);
        } catch (NotFoundException e) {
            create(eventId);
        }
    }

    /**
     * Deletes the event-reimder entry
     * @param eventId the ID of the event to save
     * @throws SQLException Database failure
     */
    public static void delete(String eventId) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.where(Table.EventReminderColumn.ID, DatabaseQuery.Operator.EQUALS, eventId);
        query.delete();
        query.executeQuery();
    }
}
