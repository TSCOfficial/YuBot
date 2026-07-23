package ch.frily.yubot.feature;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class TicketRepository {

    /**
     * Fetch a Ticket from the database.
     * @param id Ticket id (represented by the Ticket-Channel-ID)
     * @return The instance of this Ticket
     */
    public static Ticket getTicketById(long id) throws SQLException, IllegalStateException, ClassNotFoundException {
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

    /**
     * Get a ticket by the owner-user
     * <p>
     *     Here the {@link User} is used in order to be able to get tickets from users that left the server and therefore aren't {@link Member}s
     * </p>
     * @param user
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static List<Ticket> getTicketsByUser(User user) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.select().where(Table.TicketColumn.OWNER_ID, DatabaseQuery.Operator.EQUALS, user.getIdLong());
        ResultSet resultSet = query.executeDataQuery();

        List<Ticket> tickets = new java.util.ArrayList<>();
        while (resultSet.next()) {
            long assigneeId = resultSet.getLong(Table.TicketColumn.ASSIGNEE_ID.getColumn());
            long channelId = resultSet.getLong(Table.TicketColumn.CHANNEL_ID.getColumn());
            String typeName = resultSet.getString(Table.TicketColumn.TYPE.getColumn());
            Timestamp lastActivityAt = resultSet.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
            long welcomeMessageId = resultSet.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
            boolean isRequestPending = resultSet.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
            int closeRequestCount = resultSet.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());

            TicketType type = TicketType.valueOf(typeName);
            TicketStatus status = TicketStatus.valueOf(resultSet.getString(Table.TicketColumn.STATUS.getColumn()));

            Guild guild = EnvResolver.getGuildById(EnvKey.GUILD_YUSERVER);

            Ticket ticket = new Ticket(null, type);
            ticket.setAssignee(guild.getMemberById(assigneeId));
            ticket.setChannel(guild.getTextChannelById(channelId));
            ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
            ticket.setPendingRequest(isRequestPending);
            ticket.setCloseRequestCount(closeRequestCount);
            ticket.setStatus(status);

            ticket.setWelcomeMessageId(welcomeMessageId);
            tickets.add(ticket);
        }
        return tickets;
    }

    public static void createTicket(Ticket ticket) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.insert(Table.TicketColumn.OWNER_ID, ticket.getOwner().getIdLong());
        query.insert(Table.TicketColumn.CHANNEL_ID, ticket.getChannel().getIdLong());
        query.insert(Table.TicketColumn.TYPE, ticket.getType().name());
        query.insert(Table.TicketColumn.WELCOME_MESSAGE_ID, ticket.getWelcomeMessageId());
        query.insert(Table.TicketColumn.LAST_ACTIVITY_AT, ticket.getLastActivityAt());
        query.insert(Table.TicketColumn.UPDATED_AT, ticket.getUpdatedAt());
        query.executeQuery();
    }

    public static void updateTicket(Ticket ticket) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        if (ticket.getAssignee() != null) {
            query.update(Table.TicketColumn.ASSIGNEE_ID, ticket.getAssignee().getIdLong());
        }
        query.update(Table.TicketColumn.LAST_ACTIVITY_AT, ticket.getLastActivityAt());
        query.update(Table.TicketColumn.IS_REQUEST_PENDING, ticket.isRequestPending());
        query.update(Table.TicketColumn.CLOSE_REQUEST_COUNT, ticket.getCloseRequestCount());
        query.update(Table.TicketColumn.STATUS, ticket.getStatus().name());
        query.update(Table.TicketColumn.UPDATED_AT, ticket.getUpdatedAt());
        query.where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.executeQuery();
    }

    /**
     * Update the activity timestamp of the ticket owner
     * @param ticket
     */
    public static void updateTicketLastActivityAt(Ticket ticket) throws SQLException, ClassNotFoundException {
        ticket.setLastActivityAt(LocalDateTime.now());
        updateTicket(ticket);
    }

    public static void deleteTicket(Ticket ticket) throws SQLException, ClassNotFoundException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        query.where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, ticket.getId()).delete();
        query.executeQuery();
    }
}
