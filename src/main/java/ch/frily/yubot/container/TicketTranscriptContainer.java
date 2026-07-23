package ch.frily.yubot.container;

import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.feature.TicketTypeGroup;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.filedisplay.FileDisplay;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;

import java.time.OffsetDateTime;

@Slf4j
public class TicketTranscriptContainer extends Container {

    public TicketTranscriptContainer(Member initiator, Ticket ticket, FileUpload transkript) {
        this.addComponent(TextDisplay.of(Util.format("### Ticket {} wurde geschlossen", ticket.getNameWithoutStatus())));
        this.addComponent(Separator.createInvisible(Separator.Spacing.SMALL));

        if (ticket.getType().getGroup() == null) {
            addFormatedText("**Kategorie**: `%s`", ticket.getType().getLabel());
        } else {
            addFormatedText("**Kategorie**: `%s / %s`", ticket.getType().getGroup().getLabel(), ticket.getType().getLabel());
        }

        String labelOwner = "Erstellt von";
        if (ticket.getType() == TicketType.MODTICKET) {
            labelOwner = "Erstellt für";
        }
        if (ticket.getOwner() != null) {
            addFormatedText("**%s**: %s (%s)", labelOwner, ticket.getOwner().getAsMention(), ticket.getOwner().getUser().getName());
        } else {
            addFormatedText("**%s**: *Person hat den Server verlassen*", labelOwner);
        }

        String assignedMember = "**Verantwortlich**: -";

        if (ticket.getAssignee() != null) {
            assignedMember = String.format("**Verantwortlich**: %s (@%s)", ticket.getAssignee().getAsMention(), ticket.getAssignee().getUser().getName());
        }

        this.addComponent(TextDisplay.of(assignedMember));
        this.addFormatedText("**Gelöscht von**: %s (@%s)", initiator.getAsMention(), initiator.getUser().getName());

        long epochTimeCreated = ticket.getChannel().getTimeCreated().toEpochSecond();
        String opendTimeSinceCreation = Util.calcDuration(ticket.getChannel().getTimeCreated(), OffsetDateTime.now());
        addFormatedText("**Geöffnet am**: <t:%d:F> ({})", epochTimeCreated, opendTimeSinceCreation);
        this.addComponent(Separator.createDivider(Separator.Spacing.LARGE));
        this.addComponent(FileDisplay.fromFile(transkript));
    }
}
