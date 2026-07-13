package ch.frily.yubot.container;

import ch.frily.yubot.feature.Ticket;
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
            this.addComponent(TextDisplay.of(Util.format("**Kategorie**: `{}`", ticket.getType().getLabel())));
        } else {
            this.addComponent(TextDisplay.of(Util.format("**Kategorie**: `{} / {}`", ticket.getType().getGroup().getLabel(), ticket.getType().getLabel())));
        }

        if (ticket.getOwner() != null) {
            this.addComponent(TextDisplay.of(Util.format("**Erstellt von**: {} ({})", ticket.getOwner().getAsMention(), ticket.getOwner().getUser().getName())));

        } else {
            this.addComponent(TextDisplay.of(Util.format("**Erstellt von**: *Person hat den Server verlassen*")))   ;

        }

        String assignedMember = "**Verantwortlich**: -";
        if (ticket.getAssignee() != null) {
            assignedMember = Util.format("**Verantwortlich**: {} (@{})", ticket.getAssignee().getAsMention(), ticket.getAssignee().getUser().getName());
        }

        this.addComponent(TextDisplay.of(assignedMember));
        this.addComponent(TextDisplay.of(Util.format("**Gelöscht von**: {} (@{})", initiator.getAsMention(), initiator.getUser().getName())));

        long epochTimeCreated = ticket.getChannel().getTimeCreated().toEpochSecond();
        String opendTimeSinceCreation = Util.calcDuration(ticket.getChannel().getTimeCreated(), OffsetDateTime.now());
        this.addComponent(TextDisplay.of(Util.format("**Geöffnet am**: <t:{}:F> ({})", epochTimeCreated, opendTimeSinceCreation)));
        this.addComponent(Separator.createDivider(Separator.Spacing.LARGE));
        this.addComponent(FileDisplay.fromFile(transkript));
    }
}
