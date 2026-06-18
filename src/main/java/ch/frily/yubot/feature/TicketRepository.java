package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import javassist.NotFoundException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TicketRepository {

    /**
     * Fetch a Ticket from the database.
     * @param id Ticket id (represented by the Ticket-Channel-ID)
     * @return The instance of this Ticket
     */
    public static Ticket getTicketById(long id) throws SQLException, IllegalStateException {
        ResultSet resultSet = new DatabaseQuery(Table.TICKET)
                .select()
                .where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, id).executeDataQuery();

        if (!resultSet.next()) {
            throw new InvalidStateException("Ticket mit ID " + id + " nicht gefunden.");
        }

        long ownerId = resultSet.getLong(Table.TicketColumn.OWNER_ID.getColumn());
        long assigneeId = resultSet.getLong(Table.TicketColumn.ASSIGNEE_ID.getColumn());
        long channelId = resultSet.getLong(Table.TicketColumn.CHANNEL_ID.getColumn());
        String typeName = resultSet.getString(Table.TicketColumn.TYPE.getColumn());
        Timestamp lastActivityAt = resultSet.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
        long welcomeMessageId = resultSet.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
        boolean isRequestPending = resultSet.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
        int closeRequestCount = resultSet.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());
        String statusName = resultSet.getString(Table.TicketColumn.STATUS.getColumn());
        Timestamp updatedAt = resultSet.getTimestamp(Table.TicketColumn.UPDATED_AT.getColumn());

        Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);


        Member owner = guild.getMemberById(ownerId);
        TicketType type = TicketType.valueOf(typeName);
        TicketStatus status = TicketStatus.valueOf(statusName);

        Ticket ticket = new Ticket(owner, type);
        ticket.setAssignee(guild.getMemberById(assigneeId));
        ticket.setChannel(guild.getTextChannelById(channelId));
        ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
        ticket.setPendingRequest(isRequestPending);
        ticket.setCloseRequestCount(closeRequestCount);
        ticket.setStatus(status);
        ticket.setUpdatedAt(updatedAt.toLocalDateTime());

        ticket.setWelcomeMessageId(welcomeMessageId);
        return ticket;
    }

    public static void createTicket(Ticket ticket) {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.insert(Table.TicketColumn.OWNER_ID, ticket.getOwner().getIdLong());
        query.insert(Table.TicketColumn.CHANNEL_ID, ticket.getChannel().getIdLong());
        query.insert(Table.TicketColumn.TYPE, ticket.getType().name());
        query.insert(Table.TicketColumn.WELCOME_MESSAGE_ID, ticket.getWelcomeMessageId());
        query.insert(Table.TicketColumn.LAST_ACTIVITY_AT, ticket.getLastActivityAt());
        query.insert(Table.TicketColumn.UPDATED_AT, ticket.getUpdatedAt());
        query.executeQuery();
    }

    public static void updateTicket(Ticket ticket) {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        if (ticket.getAssignee() != null) {
            query.update(Table.TicketColumn.ASSIGNEE_ID, ticket.getAssignee().getIdLong());
        }
        query.update(Table.TicketColumn.LAST_ACTIVITY_AT, ticket.getLastActivityAt()); // todo check when to update activity
        query.update(Table.TicketColumn.IS_REQUEST_PENDING, ticket.isRequestPending());
        query.update(Table.TicketColumn.CLOSE_REQUEST_COUNT, ticket.getCloseRequestCount());
        query.update(Table.TicketColumn.STATUS, ticket.getStatus().name());
        query.update(Table.TicketColumn.UPDATED_AT, ticket.getUpdatedAt());
        query.where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.executeQuery();
    }

    public static void deleteTicket(Ticket ticket) {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        query.where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, ticket.getId()).delete();
        query.executeQuery();
    }
}
