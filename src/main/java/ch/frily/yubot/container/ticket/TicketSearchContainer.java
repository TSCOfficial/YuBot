package ch.frily.yubot.container.ticket;

import ch.frily.yubot.container.*;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.Util;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

import static kotlin.text.Typography.section;

@Slf4j
public class TicketSearchContainer extends PaginationContainer {

    public TicketSearchContainer(ContainerContext context) {
        super(StaticContainerRegistry.TICKET_SEARCH_OVERVIEW, context);
        String targetId = context.requireArg("target");
        Member target = EnvKey.GUILD_YUSERVER.getAsGuild().getMemberById(targetId);

        Boolean isOwnedByTarget = Boolean.valueOf(context.requireArg("owned"));
        try {
            List<Ticket> tickets = TicketRepository.getTicketsByUser(target.getUser());
            PaginationItem header = new PaginationItem();
            header.addChild(TextDisplay.ofFormat("# %s Tickets gefunden", tickets.size()));

            if (isOwnedByTarget) {
                header.addChild(TextDisplay.ofFormat("**%s** ist besitzer*in von folgenden Tickets:", target.getEffectiveName()));
            } else {
                header.addChild(TextDisplay.ofFormat("**%s** ist teilnehmer*in in folgenden Tickets:", target.getEffectiveName()));
            }
            setHeader(header);


            tickets.forEach(this::ticketDetail);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

    }

    /**
     * Adds the details of a ticket to the container
     * @param ticket The ticket to display the infos from
     */
    private void ticketDetail(Ticket ticket) {
        PaginationItem item = new PaginationItem();
        item.setTitle(String.format("### %s", ticket.getName()));

        StringBuilder details = new StringBuilder();
        details.append(String.format("> Geöffnet am <t:%s:f>", Util.toEpochSeconds(ticket.getCreatedAt())));
        details.append("\n");
        if (ticket.getArchivedAt() != null) {
            details.append(String.format("> Archiviert am <t:%s:f>", Util.toEpochSeconds(ticket.getArchivedAt())));
            details.append("\n");
        }
        if (!ticket.getParticipants().isEmpty()) {
            details.append(String.format("> Teilnehmende: %s", String.join(", ", ticket.getParticipants().stream().map(Member::getAsMention).toList())));
            details.append("\n");
        } else {
            details.append("> Teilnehmende: Keine");
            details.append("\n");
        }

        if (ticket.getArchiveMessageId() != null && !ticket.getArchiveMessageId().isBlank()) {
            String ticketArchiveJumpLink = EnvKey.CHANNEL_TICKETLOGS.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER).getJumpUrl() + "/" + ticket.getArchiveMessageId();

            Section section = Section.of(
                    Button.of(ButtonStyle.LINK, ticketArchiveJumpLink, "Archiv"),
                    TextDisplay.ofFormat(details.toString())
            );
            item.addChild(section);
        } else {
            item.addChild(TextDisplay.ofFormat(details.toString()));
        }

        addItem(item);
    }
}
