package ch.frily.yubot.embed;

import ch.frily.yubot.feature.Ticket;
import lombok.Setter;

/**
 * Embed used after the Ticket was closed. Displays Ticket Informations
 */
public class TicketClosedOptionsEmbed implements IEmbed {

    @Setter
    private Ticket ticket;

    @Setter
    private boolean isForcedClosed;


    @Override
    public String getAuthorName() {
        if (ticket.getAssignee() != null) {
            return ticket.getAssignee().getEffectiveName();
        }
        return null;
    }

    @Override
    public String getAuthorIconUrl() {
        if (ticket.getAssignee() != null) {
            return ticket.getAssignee().getAvatarUrl();
        }
        return null;
    }

    @Override
    public String getTitle() {
        return "🔒 Ticket geschlossen";
    }

    @Override
    public String getDescription() {
        return "Soll das Ticket gelöscht werden?\n-# Ein Transkript wird automatisch generiert und hinterlegt.";
    }

    @Override
    public String getFooterText() {
        return ticket.getNameWithoutStatus();
    }
}
