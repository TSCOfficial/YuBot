package ch.frily.yubot.embed;

import ch.frily.yubot.feature.Ticket;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

/**
 * Embed used after the Ticket was closed. Displays Ticket Informations
 */
public class TicketClosedOptionsEmbed implements IEmbed {

    @Setter
    private Ticket ticket;

    @Setter
    private boolean forceClosed;

    /**
     * The person who closed the ticket (useful when force-closed)
     */
    @Setter
    private Member initiator;

    @Override
    public String getAuthorName() {
        if (initiator == null) {
            return null;
        }
        return initiator.getEffectiveName();
    }

    @Override
    public String getAuthorIconUrl() {
        if (initiator == null) {
            return null;
        }
        return initiator.getEffectiveAvatarUrl();
    }

    @Override
    public String getTitle() {
        if (forceClosed) {
            return "🔒 Ticket schliessung erzwungen";
        }
        return "🔒 Ticket geschlossen";
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        if (initiator != null) {
            description.append(initiator.getAsMention()).append(" hat die schliessung erzwungen.\n\n");
        }
        description.append("Soll das Ticket gelöscht werden?\n-# Ein Transkript wird automatisch generiert und hinterlegt.");
        return description.toString();
    }

    @Override
    public Color getColor() {
        if (initiator != null) {
            return initiator.getColors().getPrimary();
        }
        return ticket.getOwner().getColors().getPrimary();
    }

    @Override
    public String getFooterText() {
        return ticket.getNameWithoutStatus();
    }
}
