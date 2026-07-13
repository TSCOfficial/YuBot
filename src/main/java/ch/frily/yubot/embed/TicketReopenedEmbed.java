package ch.frily.yubot.embed;

import ch.frily.yubot.feature.Ticket;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

/**
 * Embed used after the Ticket was closed. Displays Ticket Informations
 */
public class TicketReopenedEmbed implements IEmbed {

    @Setter
    private Ticket ticket;

    /** Person who reopened the ticket*/
    @Setter
    private Member initiator;

    @Override
    public String getAuthorName() {
        return initiator.getEffectiveName();
    }

    @Override
    public String getAuthorIconUrl() {
        return initiator.getEffectiveAvatarUrl();
    }

    @Override
    public String getTitle() {
        return "🔒 Ticket erneut geöffnet";
    }

    @Override
    public String getDescription() {
        return String.format("%s hat das Ticket erneut geöffnet.", initiator.getAsMention());
    }

    @Override
    public Color getColor() {
        return initiator.getColors().getPrimary();
    }

    @Override
    public String getFooterText() {
        return ticket.getNameWithoutStatus();
    }
}
