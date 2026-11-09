package ch.frily.yubot.service.ticket.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.IRepository;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.TicketStatus;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TicketParticipantRepository implements IRepository {

    @Override
    public Table getTable() {
        return Table.TICKET_PARTICIPANT;
    }

    public static List<Ticket> getTicketsFromParticipant(Member member) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
        query.select();
        query.join(Table.TICKET, Table.TicketColumn.ID, DatabaseQuery.Operator.EQUALS, Table.TicketParticipantColumn.TICKET_ID);
        query.where(Table.TicketParticipantColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());

        ResultSet rs = query.executeDataQuery();

        List<Ticket> tickets = new ArrayList<>();
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

    /**
     * Get every participant's ID from a given ticket
     * @param ticket
     * @return a list of member-Id that participated in the ticket, or empty list if none were found
     * @throws SQLException
     */
    public static List<String> getParticipantsFromTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
        query.select(Table.TicketParticipantColumn.MEMBER_ID);
        query.where(Table.TicketParticipantColumn.TICKET_ID, DatabaseQuery.Operator.EQUALS, ticket.getId());

        ResultSet rs = query.executeDataQuery();
        List<String> participants = new ArrayList<>();
        while (rs.next()) {
            String userId = rs.getString(Table.TicketParticipantColumn.MEMBER_ID.getColumn());
            participants.add(userId);
        }
        return participants;
    }

    public static boolean memberIsParticipant(Ticket ticket, Member member) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
        query.select();
        query.where(Table.TicketParticipantColumn.TICKET_ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.where(Table.TicketParticipantColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());

        ResultSet rs = query.executeDataQuery();
        return rs.next();
    }

    public static void addParticipantToTicket(Ticket ticket, Member member) throws SQLException {
        if (!memberIsParticipant(ticket, member)) {
            DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
            query.insert(Table.TicketParticipantColumn.TICKET_ID, ticket.getId());
            query.insert(Table.TicketParticipantColumn.MEMBER_ID, member.getId());
            query.executeQuery();
        }

    }
}
