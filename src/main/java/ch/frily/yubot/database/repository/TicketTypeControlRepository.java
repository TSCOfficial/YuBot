package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.feature.ticket.TicketType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketTypeControlRepository {

    public static void upsertType(TicketType type, boolean isLocked) throws SQLException, ClassNotFoundException {
        if (exists(type)) {
            updateType(type, isLocked);
        } else {
            createType(type, isLocked);
        }
    }

    private static boolean exists(TicketType type) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_TYPE_CONTROL);
        query.select().where(Table.TicketTypeControlColumn.TYPE, DatabaseQuery.Operator.EQUALS, type.name());

        ResultSet resultSet = query.executeDataQuery();
        return resultSet.next();
    }

    private static void createType(TicketType type, boolean isLocked) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_TYPE_CONTROL);
        query.insert(Table.TicketTypeControlColumn.TYPE, type.name());
        query.insert(Table.TicketTypeControlColumn.IS_LOCKED, isLocked);
        query.executeQuery();
    }

    private static void updateType(TicketType type, boolean isLocked) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_TYPE_CONTROL);
        query.update(Table.TicketTypeControlColumn.IS_LOCKED, isLocked);
        query.where(Table.TicketTypeControlColumn.TYPE, DatabaseQuery.Operator.EQUALS, type.name());
        query.executeQuery();
    }

    /**
     * Check if the ticket type is locked
     * @param type
     * @return True if the ticket type is locked, false if not
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static boolean isTypeLocked(TicketType type) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_TYPE_CONTROL);
        query.select().where(Table.TicketTypeControlColumn.TYPE, DatabaseQuery.Operator.EQUALS, type.name());

        ResultSet resultSet = query.executeDataQuery();
        return resultSet.next() && resultSet.getBoolean(Table.TicketTypeControlColumn.IS_LOCKED.getColumn());
    }
}
