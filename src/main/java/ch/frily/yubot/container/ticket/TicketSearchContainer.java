package ch.frily.yubot.container.ticket;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.service.ticket.Ticket;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class TicketSearchContainer extends Container {

    /**
     *
     * @param tickets The tickets to display the infos from
     * @param target The member that is related to these tickets
     * @param isOwnedByTarget whether the given tickets are owned by that person or if the person only participated in
     */
    public TicketSearchContainer(List<Ticket> tickets, Member target, boolean isOwnedByTarget) {
        addFormatedText("# %s Tickets gefunden", tickets.size());
        if (isOwnedByTarget) {
            addFormatedText("%s ist besitzer*in von folgenden Tickets:", target.getEffectiveName());
        }
        tickets.forEach(this::ticketDetail);
    }

    /**
     * Adds the details of a ticket to the container
     * @param ticket The ticket to display the infos from
     */
    private void ticketDetail(Ticket ticket) {
        addFormatedText("### %s", ticket.getName());
        String participants = ticket.getParticipants().stream().map(Member::getEffectiveName).toList().stream().reduce((a, b) -> a + ", " + b).orElse("");
        addFormatedText("""
                Geöffnet am %s
                Archiviert am %s
                Teilhabende: %s
                """, ticket.getCreatedAt(), ticket.getArchivedAt(), participants);
    }
}
