package ch.frily.yubot.database.repository;

import ch.frily.yubot.database.DatabaseQuery;
import ch.frily.yubot.database.Table;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketMemberRepository {

    public static List<Member> getMembersFromTicket(Ticket ticket) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
        query.select(Table.TicketParticipantColumn.MEMBER_ID);
        query.where(Table.TicketParticipantColumn.TICKET_ID, DatabaseQuery.Operator.EQUALS, ticket.getId());

        ResultSet rs = query.executeDataQuery();
        List<Member> members = new ArrayList<>();
        while (rs.next()) {
            String memberId = rs.getString(Table.TicketParticipantColumn.MEMBER_ID.getColumn());
            Member member = EnvKey.GUILD_YUSERVER.getAsGuild().getMemberById(memberId);
            members.add(member);
        }
        return members;
    }

    public static boolean memberIsParticipant(Ticket ticket, Member member) throws SQLException {
        DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
        query.select();
        query.where(Table.TicketParticipantColumn.TICKET_ID, DatabaseQuery.Operator.EQUALS, ticket.getId());
        query.where(Table.TicketParticipantColumn.MEMBER_ID, DatabaseQuery.Operator.EQUALS, member.getId());

        ResultSet rs = query.executeDataQuery();
        return rs.next();
    }

    public static void addMemberToTicket(Ticket ticket, Member member) throws SQLException {
        if (!memberIsParticipant(ticket, member)) {
            DatabaseQuery query = new DatabaseQuery(Table.TICKET_PARTICIPANT);
            query.insert(Table.TicketParticipantColumn.TICKET_ID, ticket.getId());
            query.insert(Table.TicketParticipantColumn.MEMBER_ID, member.getId());
            query.executeQuery();
        }

    }
}
