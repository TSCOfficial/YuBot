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
     * @param tickets
     * @param target
     * @param isOwned whether the given tickets are owned by that person or if the person only participated in
     */
    public TicketSearchContainer(List<Ticket> tickets, Member target, boolean isOwned) {
        addFormatedText("# %s Tickets gefunden", tickets.size());
        if (isOwned) {
            addFormatedText("%s ist besitzer*in von folgenden Tickets:", target.getEffectiveName());
        }
        tickets.forEach(this::ticketDetail);
    }

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
