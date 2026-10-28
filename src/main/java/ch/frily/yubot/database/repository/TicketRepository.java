package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketStatus;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.util.EnvKey;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TicketRepository {

    /**
     * Fetch a Ticket from the database.
     * @param channelId the Ticket-Channel-ID
     * @return The instance of this Ticket
     */
    public static Ticket getTicketByChannelId(long channelId) throws SQLException, IllegalStateException {
        ResultSet resultSet = new DatabaseQuery(Table.TICKET)
                .select()
                .where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, channelId).executeDataQuery();

        if (!resultSet.next()) {
            throw new InvalidStateException("Ticket mit ID " + channelId + " nicht gefunden.");
        }

        int id = resultSet.getInt(Table.TicketColumn.ID.getColumn());
        long ownerId = resultSet.getLong(Table.TicketColumn.OWNER_ID.getColumn());
        long assigneeId = resultSet.getLong(Table.TicketColumn.ASSIGNEE_ID.getColumn());
        String typeName = resultSet.getString(Table.TicketColumn.TYPE.getColumn());
        Timestamp lastActivityAt = resultSet.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
        long welcomeMessageId = resultSet.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
        boolean isRequestPending = resultSet.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
        int closeRequestCount = resultSet.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());
        String statusName = resultSet.getString(Table.TicketColumn.STATUS.getColumn());
        Timestamp updatedAt = resultSet.getTimestamp(Table.TicketColumn.UPDATED_AT.getColumn());
        boolean isReminderSent = resultSet.getBoolean(Table.TicketColumn.IS_REMINDER_SENT.getColumn());

        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();

        Member owner = guild.getMemberById(ownerId);
        TicketType type = TicketType.valueOf(typeName);
        TicketStatus status = TicketStatus.valueOf(statusName);

        Ticket ticket = new Ticket(owner, type);
        ticket.setId(id);
        ticket.setAssignee(guild.getMemberById(assigneeId));
        ticket.setChannel(guild.getTextChannelById(channelId));
        ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
        ticket.setRequestPending(isRequestPending);
        ticket.setCloseRequestCount(closeRequestCount);
        ticket.setStatus(status);
        if (updatedAt != null) {
            ticket.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        ticket.setReminderSent(isReminderSent);

        ticket.setWelcomeMessageId(welcomeMessageId);
        return ticket;
    }

    /**
     * Get a ticket by the owner-user
     * <p>
     *     Here the {@link User} is used in order to be able to get tickets from users that left the server and therefore aren't {@link Member}s
     * </p>
     * @param user the user to find the tickets from
     * @return a list of tickets owner by the given user
     * @throws SQLException database failure
     */
    public static List<Ticket> getTicketsByUser(User user) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.select().where(Table.TicketColumn.OWNER_ID, DatabaseQuery.Operator.EQUALS, user.getIdLong());
        ResultSet resultSet = query.executeDataQuery();

        List<Ticket> tickets = new java.util.ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt(Table.TicketColumn.ID.getColumn());
            long ownerId = resultSet.getLong(Table.TicketColumn.OWNER_ID.getColumn());
            long assigneeId = resultSet.getLong(Table.TicketColumn.ASSIGNEE_ID.getColumn());
            long channelId = resultSet.getLong(Table.TicketColumn.CHANNEL_ID.getColumn());
            TicketType type = TicketType.valueOf(resultSet.getString(Table.TicketColumn.TYPE.getColumn()));
            Timestamp lastActivityAt = resultSet.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
            long welcomeMessageId = resultSet.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
            boolean isRequestPending = resultSet.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
            int closeRequestCount = resultSet.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());
            boolean isReminderSent = resultSet.getBoolean(Table.TicketColumn.IS_REMINDER_SENT.getColumn());
            TicketStatus status = TicketStatus.valueOf(resultSet.getString(Table.TicketColumn.STATUS.getColumn()));
            String summary =  resultSet.getString(Table.TicketColumn.SUMMARY.getColumn());
            Timestamp createdAt = resultSet.getTimestamp(Table.TicketColumn.CREATED_AT.getColumn());
            Timestamp updatedAt = resultSet.getTimestamp(Table.TicketColumn.UPDATED_AT.getColumn());
            Timestamp archivedAt = resultSet.getTimestamp(Table.TicketColumn.ARCHIVED_AT.getColumn());


            Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
            Member owner = guild.getMemberById(ownerId);

            Ticket ticket = new Ticket(owner, type);
            ticket.setId(id);
            ticket.setAssignee(guild.getMemberById(assigneeId));
            ticket.setChannel(guild.getTextChannelById(channelId));
            ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
            ticket.setRequestPending(isRequestPending);
            ticket.setCloseRequestCount(closeRequestCount);
            ticket.setStatus(status);
            ticket.setReminderSent(isReminderSent);
            ticket.setWelcomeMessageId(welcomeMessageId);
            ticket.setCreatedAt(createdAt.toLocalDateTime());

            if (Objects.nonNull(updatedAt)) {
                ticket.setUpdatedAt(updatedAt.toLocalDateTime());
            }
            if (Objects.nonNull(archivedAt)) {
                ticket.setArchivedAt(archivedAt.toLocalDateTime());
            }
            ticket.setSummary(summary);
            tickets.add(ticket);
        }
        return tickets;
    }

    public static List<Ticket> getTickets() throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.select();
        ResultSet resultSet = query.executeDataQuery();

        List<Ticket> tickets = new java.util.ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt(Table.TicketColumn.ID.getColumn());
            long ownerId = resultSet.getLong(Table.TicketColumn.OWNER_ID.getColumn());
            long assigneeId = resultSet.getLong(Table.TicketColumn.ASSIGNEE_ID.getColumn());
            long channelId = resultSet.getLong(Table.TicketColumn.CHANNEL_ID.getColumn());
            TicketType type = TicketType.valueOf(resultSet.getString(Table.TicketColumn.TYPE.getColumn()));
            Timestamp lastActivityAt = resultSet.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
            long welcomeMessageId = resultSet.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
            boolean isRequestPending = resultSet.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
            int closeRequestCount = resultSet.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());
            boolean isReminderSent = resultSet.getBoolean(Table.TicketColumn.IS_REMINDER_SENT.getColumn());
            TicketStatus status = TicketStatus.valueOf(resultSet.getString(Table.TicketColumn.STATUS.getColumn()));
            String summary =  resultSet.getString(Table.TicketColumn.SUMMARY.getColumn());
            Timestamp createdAt = resultSet.getTimestamp(Table.TicketColumn.CREATED_AT.getColumn());
            Timestamp updatedAt = resultSet.getTimestamp(Table.TicketColumn.UPDATED_AT.getColumn());
            Timestamp archivedAt = resultSet.getTimestamp(Table.TicketColumn.ARCHIVED_AT.getColumn());


            Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();
            Member owner = guild.getMemberById(ownerId);

            Ticket ticket = new Ticket(owner, type);
            ticket.setId(id);
            ticket.setAssignee(guild.getMemberById(assigneeId));
            ticket.setChannel(guild.getTextChannelById(channelId));
            ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
            ticket.setRequestPending(isRequestPending);
            ticket.setCloseRequestCount(closeRequestCount);
            ticket.setStatus(status);
            ticket.setReminderSent(isReminderSent);
            ticket.setWelcomeMessageId(welcomeMessageId);
            if (createdAt != null) {
                ticket.setCreatedAt(createdAt.toLocalDateTime());
            }
            if (Objects.nonNull(updatedAt)) {
                ticket.setUpdatedAt(updatedAt.toLocalDateTime());
            }
            if (Objects.nonNull(archivedAt)) {
                ticket.setArchivedAt(archivedAt.toLocalDateTime());
            }
            ticket.setSummary(summary);
            tickets.add(ticket);
        }
        return tickets;
    }

    public static void createTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.insert(Table.TicketColumn.OWNER_ID, ticket.getOwner().getIdLong());
        query.insert(Table.TicketColumn.CHANNEL_ID, ticket.getChannel().getIdLong());
        query.insert(Table.TicketColumn.TYPE, ticket.getType().name());
        query.insert(Table.TicketColumn.WELCOME_MESSAGE_ID, ticket.getWelcomeMessageId());
        query.insert(Table.TicketColumn.LAST_ACTIVITY_AT, ticket.getLastActivityAt());
        query.insert(Table.TicketColumn.CREATED_AT, LocalDateTime.now());
        query.insert(Table.TicketColumn.IS_REMINDER_SENT, ticket.isReminderSent());
        query.executeQuery();
    }

    /**
     * Update a ticket
     * <p>
     *     This does not update any archive-exclusive fields
     * </p>
     * @param ticket
     * @throws SQLException
     */
    public static void updateTicket(Ticket ticket) throws SQLException { // todo check silent update error - ticket update doesnt seem to work at all?
        log.info("updating ticket with id {}", ticket.getId());
        log.info("updating status to {}", ticket.getStatus());
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        if (ticket.getAssignee() != null) {
            query.update(Table.TicketColumn.ASSIGNEE_ID, ticket.getAssignee().getIdLong());
        }
        query.update(Table.TicketColumn.LAST_ACTIVITY_AT, ticket.getLastActivityAt());
        query.update(Table.TicketColumn.IS_REQUEST_PENDING, ticket.isRequestPending());
        query.update(Table.TicketColumn.CLOSE_REQUEST_COUNT, ticket.getCloseRequestCount());
        query.update(Table.TicketColumn.STATUS, ticket.getStatus().name());
        query.update(Table.TicketColumn.IS_REMINDER_SENT, ticket.isReminderSent());
        query.update(Table.TicketColumn.UPDATED_AT, LocalDateTime.now());
        query.where(Table.TicketColumn.ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.executeQuery();
    }

    /**
     * Update the activity timestamp of the ticket owner
     * @param ticket the ticket to update the last activity from
     */
    public static void updateTicketLastActivityAt(Ticket ticket) throws SQLException {
        ticket.setLastActivityAt(LocalDateTime.now());
        updateTicket(ticket);
    }

    /**
     *
     * @param ticket
     * @throws SQLException
     * @deprecated this method will be removed in the v2.2.0. Archiving the tickets instead of deleting them
     */
    @Deprecated
    public static void deleteTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        query.where(Table.TicketColumn.CHANNEL_ID, DatabaseQuery.Operator.EQUALS, ticket.getId()).delete();
        query.executeQuery();
    }

    /**
     * Archive a ticket
     * @param ticket
     * @throws SQLException
     */
    public static void archiveTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        query.update(Table.TicketColumn.STATUS, ticket.getStatus().name());
        query.update(Table.TicketColumn.SUMMARY, ticket.getSummary());
        query.update(Table.TicketColumn.ARCHIVE_MESSAGE_ID, ticket.getArchiveMessageId());
        query.update(Table.TicketColumn.ARCHIVED_AT, LocalDateTime.now());
        query.where(Table.TicketColumn.ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.executeQuery();
    }
}
