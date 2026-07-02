package ch.frily.yubot.embed;

import ch.frily.yubot.feature.Ticket;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

public class TicketCloseRequestEmbed implements IEmbed {

    @Setter
    private Member initiator;

    @Setter
    private Ticket ticket;

    @Override
    public String getAuthorName() {
        return initiator.getEffectiveName();
    }

    @Override
    public String getAuthorIconUrl() {
        return initiator.getAvatarUrl();
    }

    @Override
    public String getTitle() {
        return "🔓 Schliessanfrage";
    }

    @Override
    public String getDescription() {
        return String.format("%s hat eine Schliessanfrage gestellt.\nMöchtest du, %s,  das Ticket schliessen?", initiator.getAsMention(), ticket.getOwner().getAsMention());
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
