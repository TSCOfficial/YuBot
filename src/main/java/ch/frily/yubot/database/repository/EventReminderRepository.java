package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public class EventReminderRepository {

    /**
     * Get the event informations (as a map with)
     * @param eventId
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Map<String, Boolean> getEvent(String eventId) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.where(Table.EventReminderColumn.ID, DatabaseQuery.Operator.EQUALS, eventId);
        ResultSet rs = query.executeDataQuery();

        if (rs.next()) {
            return Map.of(eventId, rs.getBoolean(Table.EventReminderColumn.IS_REMINDER_SENT.getColumn()));
        }
        return null;
    }

    /**
     * Changes the "is reminder sent" status
     * @param eventId
     * @param status
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void setStatus(String eventId, boolean status) throws SQLException, ClassNotFoundException {
        createIfMissing(eventId);
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.where(Table.EventReminderColumn.ID, DatabaseQuery.Operator.EQUALS, eventId);
        query.update(Table.EventReminderColumn.IS_REMINDER_SENT, status);
        query.executeQuery();
    }

    /**
     * Creates the event-reminder entry
     * @param eventId
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void create(String eventId) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.insert(Table.EventReminderColumn.ID, eventId);
        query.insert(Table.EventReminderColumn.IS_REMINDER_SENT, false);
        query.executeQuery();
    }

    /**
     * Creates the event-reimder entry for the given event if it does not exist in the database
     * @param eventId
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void createIfMissing(String eventId) throws SQLException, ClassNotFoundException {
        if (getEvent(eventId) == null) {
            create(eventId);
        }
    }

    /**
     * Deletes the event-reimder entry
     * @param eventId
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void delete(String eventId) throws SQLException, ClassNotFoundException {
        log.info("Deleting event reminder entry for event: {}", eventId);
        DatabaseQuery query = new DatabaseQuery(Table.EVENT_REMINDER);
        query.where(Table.EventReminderColumn.ID, DatabaseQuery.Operator.EQUALS, eventId);
        query.delete();
        query.executeQuery();
    }
}
