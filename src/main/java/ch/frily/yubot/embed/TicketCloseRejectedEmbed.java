package ch.frily.yubot.embed;

import ch.frily.yubot.feature.Ticket;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

public class TicketCloseRejectedEmbed implements IEmbed {

    @Setter
    private Member member;

    @Setter
    private Ticket ticket;

    @Override
    public String getAuthorName() {
        return member.getEffectiveName();
    }

    @Override
    public String getAuthorIconUrl() {
        return member.getAvatarUrl();
    }

    @Override
    public String getTitle() {
        return "❌ Schliessanfrage abgelehnt";
    }

    @Override
    public String getDescription() {
        return member.getAsMention() + " hat eine Schliessanfrage abgelehnt.";
    }

    @Override
    public Color getColor() {
        return member.getColors().getPrimary();
    }

    @Override
    public String getFooterText() {
        return ticket.getNameWithoutStatus();
    }
}
