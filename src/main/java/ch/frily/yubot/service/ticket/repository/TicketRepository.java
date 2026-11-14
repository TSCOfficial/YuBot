package ch.frily.yubot.service.ticket.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.IRepository;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketStatus;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.EnvKey;
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
public class TicketRepository implements IRepository {

    @Override
    public Table getTable() {
        return Table.TICKET;
    }

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
            throw new NotFoundException("Ticket mit ID " + channelId + " nicht gefunden.");
        }

        int id = resultSet.getInt(Table.TicketColumn.ID.getColumn());
        String name = resultSet.getString(Table.TicketColumn.NAME.getColumn());
        String ownerId = resultSet.getString(Table.TicketColumn.OWNER_ID.getColumn());
        String assigneeId = resultSet.getString(Table.TicketColumn.ASSIGNEE_ID.getColumn());
        String typeName = resultSet.getString(Table.TicketColumn.TYPE.getColumn());
        Timestamp lastActivityAt = resultSet.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
        long welcomeMessageId = resultSet.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
        boolean isRequestPending = resultSet.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
        int closeRequestCount = resultSet.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());
        String statusName = resultSet.getString(Table.TicketColumn.STATUS.getColumn());
        Timestamp updatedAt = resultSet.getTimestamp(Table.TicketColumn.UPDATED_AT.getColumn());
        boolean isReminderSent = resultSet.getBoolean(Table.TicketColumn.IS_REMINDER_SENT.getColumn());
        String archiveMessageId = resultSet.getString(Table.TicketColumn.ARCHIVE_MESSAGE_ID.getColumn());

        Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();

        TicketType type = TicketType.valueOf(typeName);

        TicketStatus status = TicketStatus.valueOf(statusName);

        Ticket ticket = new Ticket(ownerId, type);
        ticket.setId(id);
        ticket.setName(name);
        ticket.setAssigneeId(assigneeId);
        ticket.setChannel(guild.getTextChannelById(channelId));
        if (Objects.nonNull(lastActivityAt)) {
            ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
        }
        ticket.setRequestPending(isRequestPending);
        ticket.setCloseRequestCount(closeRequestCount);
        ticket.setStatus(status);
        if (Objects.nonNull(updatedAt)) {
            ticket.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        ticket.setReminderSent(isReminderSent);
        ticket.setWelcomeMessageId(welcomeMessageId);
        if (Objects.nonNull(archiveMessageId)) {
            ticket.setArchiveMessageId(archiveMessageId);
        }

        List<String> participants = TicketParticipantRepository.getParticipantsFromTicket(ticket);
        ticket.setParticipantIds(participants);
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
        query.select().where(Table.TicketColumn.OWNER_ID, DatabaseQuery.Operator.EQUALS, user.getId());
        ResultSet resultSet = query.executeDataQuery();

        List<Ticket> tickets = new java.util.ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt(Table.TicketColumn.ID.getColumn());
            String name = resultSet.getString(Table.TicketColumn.NAME.getColumn());
            String ownerId = resultSet.getString(Table.TicketColumn.OWNER_ID.getColumn());
            String assigneeId = resultSet.getString(Table.TicketColumn.ASSIGNEE_ID.getColumn());
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
            String archiveMessageId = resultSet.getString(Table.TicketColumn.ARCHIVE_MESSAGE_ID.getColumn());


            Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();

            Ticket ticket = new Ticket(ownerId, type);
            ticket.setId(id);
            ticket.setName(name);
            ticket.setAssigneeId(assigneeId);
            ticket.setChannel(guild.getTextChannelById(channelId));
            if (lastActivityAt != null) {
                ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
            }
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
            if (Objects.nonNull(archiveMessageId)) {
                ticket.setArchiveMessageId(archiveMessageId);
            }
            ticket.setSummary(summary);

            List<String> participants = TicketParticipantRepository.getParticipantsFromTicket(ticket);
            ticket.setParticipantIds(participants);

            tickets.add(ticket);
        }
        return tickets;
    }

    public static List<Ticket> getTicketsWithMatchingName(String searchQuery) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.select()
            .where(Table.TicketColumn.NAME, DatabaseQuery.Operator.LIKE, "%" + searchQuery + "%");

        ResultSet rs = query.executeDataQuery();
        List<Ticket> tickets = new java.util.ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt(Table.TicketColumn.ID.getColumn());
            String name = rs.getString(Table.TicketColumn.NAME.getColumn());
            String ownerId = rs.getString(Table.TicketColumn.OWNER_ID.getColumn());
            String assigneeId = rs.getString(Table.TicketColumn.ASSIGNEE_ID.getColumn());
            long channelId = rs.getLong(Table.TicketColumn.CHANNEL_ID.getColumn());
            TicketType type = TicketType.valueOf(rs.getString(Table.TicketColumn.TYPE.getColumn()));
            Timestamp lastActivityAt = rs.getTimestamp(Table.TicketColumn.LAST_ACTIVITY_AT.getColumn());
            long welcomeMessageId = rs.getLong(Table.TicketColumn.WELCOME_MESSAGE_ID.getColumn());
            boolean isRequestPending = rs.getBoolean(Table.TicketColumn.IS_REQUEST_PENDING.getColumn());
            int closeRequestCount = rs.getInt(Table.TicketColumn.CLOSE_REQUEST_COUNT.getColumn());
            boolean isReminderSent = rs.getBoolean(Table.TicketColumn.IS_REMINDER_SENT.getColumn());
            TicketStatus status = TicketStatus.valueOf(rs.getString(Table.TicketColumn.STATUS.getColumn()));
            String summary =  rs.getString(Table.TicketColumn.SUMMARY.getColumn());
            Timestamp createdAt = rs.getTimestamp(Table.TicketColumn.CREATED_AT.getColumn());
            Timestamp updatedAt = rs.getTimestamp(Table.TicketColumn.UPDATED_AT.getColumn());
            Timestamp archivedAt = rs.getTimestamp(Table.TicketColumn.ARCHIVED_AT.getColumn());
            String archiveMessageId = rs.getString(Table.TicketColumn.ARCHIVE_MESSAGE_ID.getColumn());


            Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();

            Ticket ticket = new Ticket(ownerId, type);
            ticket.setId(id);
            ticket.setName(name);
            ticket.setAssigneeId(assigneeId);
            ticket.setChannel(guild.getTextChannelById(channelId));
            if (lastActivityAt != null) {
                ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
            }
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
            if (Objects.nonNull(archiveMessageId)) {
                ticket.setArchiveMessageId(archiveMessageId);
            }
            ticket.setSummary(summary);

            List<String> participants = TicketParticipantRepository.getParticipantsFromTicket(ticket);
            ticket.setParticipantIds(participants);

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
            String name = resultSet.getString(Table.TicketColumn.NAME.getColumn());
            String ownerId = resultSet.getString(Table.TicketColumn.OWNER_ID.getColumn());
            String assigneeId = resultSet.getString(Table.TicketColumn.ASSIGNEE_ID.getColumn());
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

            String archiveMessageId = resultSet.getString(Table.TicketColumn.ARCHIVE_MESSAGE_ID.getColumn());


            Guild guild = EnvKey.GUILD_YUSERVER.getAsGuild();

            Ticket ticket = new Ticket(ownerId, type);
            ticket.setId(id);
            ticket.setName(name);
            ticket.setAssigneeId(assigneeId);
            ticket.setChannel(guild.getTextChannelById(channelId));
            if (lastActivityAt != null) {
                ticket.setLastActivityAt(lastActivityAt.toLocalDateTime());
            }

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
            if (Objects.nonNull(archiveMessageId)) {
                ticket.setArchiveMessageId(archiveMessageId);
            }
            ticket.setSummary(summary);

            List<String> participants = TicketParticipantRepository.getParticipantsFromTicket(ticket);
            ticket.setParticipantIds(participants);

            tickets.add(ticket);
        }
        return tickets;
    }

    public static void createTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);
        query.insert(Table.TicketColumn.OWNER_ID, ticket.getOwnerId());
        query.insert(Table.TicketColumn.NAME, ticket.getName());
        query.insert(Table.TicketColumn.CHANNEL_ID, ticket.getChannel().getIdLong());
        query.insert(Table.TicketColumn.TYPE, ticket.getType().name());
        query.insert(Table.TicketColumn.STATUS, TicketStatus.NEW.name());
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
    public static void updateTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        if (ticket.getAssigneeId() != null) {
            query.update(Table.TicketColumn.ASSIGNEE_ID, ticket.getAssigneeId());
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
     * Archive a ticket
     * @param ticket
     * @throws SQLException
     */
    public static void archiveTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET);

        query.update(Table.TicketColumn.STATUS, TicketStatus.ARCHIVED.name());
        query.update(Table.TicketColumn.CHANNEL_ID, null);
        query.update(Table.TicketColumn.WELCOME_MESSAGE_ID, null);
        query.update(Table.TicketColumn.IS_REQUEST_PENDING, false);
        query.update(Table.TicketColumn.SUMMARY, ticket.getSummary());
        query.update(Table.TicketColumn.ARCHIVE_MESSAGE_ID, ticket.getArchiveMessageId());
        query.update(Table.TicketColumn.ARCHIVED_AT, LocalDateTime.now());
        query.where(Table.TicketColumn.ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.executeQuery();
    }
}
