package ch.frily.yubot.embed.ticket;

import ch.frily.yubot.embed.IEmbed;
import ch.frily.yubot.feature.ticket.Ticket;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;

public class TicketCloseAcceptedEmbed implements IEmbed {

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
        return "🔒 Schliessanfrage angenommen";
    }

    @Override
    public String getDescription() {
        return member.getAsMention() + " hat die Schliessanfrage angenommen. Das Ticket wird nun geschlossen.";
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
