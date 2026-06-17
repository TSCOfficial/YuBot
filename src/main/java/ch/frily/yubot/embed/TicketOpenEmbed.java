package ch.frily.yubot.embed;

import ch.frily.yubot.feature.Ticket;
import lombok.Setter;

import java.awt.*;

public class TicketOpenEmbed implements IEmbed {

    @Setter
    private Ticket ticket;

    @Override
    public String getAuthorIconUrl() {
        return ticket.getOwner().getEffectiveAvatarUrl();
    }

    @Override
    public Color getColor() {
        return ticket.getOwner().getColors().getPrimary();
    }

    @Override
    public String getTitle() {
        return ticket.getType().getLabel();
    }

    @Override
    public String getDescription() {
        return "Willkommen **" + ticket.getOwner().getUser().getGlobalName() + "**!" +
                "\n" +
                ticket.getType().getEmbedDescription() +
                "\n" +
                "-# Mit </ticket add:1511995068846968875> kann das Team dem Ticket weitere Personen hinzugefügt.\n" +
                "-# Ticketinhalte werden zu Dokumentationszwecken sicher, und nur für die Serverleitung, hinterlegt.";
    }

    @Override
    public String getFooterText() {
        return ticket.getNameWithoutStatus();
    }
}
