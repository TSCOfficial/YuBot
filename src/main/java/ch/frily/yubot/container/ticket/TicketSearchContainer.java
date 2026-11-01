package ch.frily.yubot.container.ticket;

import ch.frily.yubot.container.*;
import ch.frily.yubot.database.repository.TicketParticipantRepository;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.slashcommand.TicketSearchSubCmdGroup;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.Util;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static kotlin.text.Typography.section;

@Slf4j
public class TicketSearchContainer extends PaginationContainer {

    private ContainerContext context;

    public TicketSearchContainer(ContainerContext context) {
        super(StaticContainerRegistry.TICKET_SEARCH_OVERVIEW, context);
        this.context = context;

        Member target = null;
        Optional<String> targetId = context.arg("target");
        if (targetId.isPresent()) {
            target = EnvKey.GUILD_YUSERVER.getAsGuild().getMemberById(targetId.get());
        }

        Optional<String> search = context.arg("q");

        TicketSearchSubCmdGroup.SearchType searchType = TicketSearchSubCmdGroup.SearchType.valueOf(context.requireArg("search"));
        try {
            List<Ticket> tickets = null;
            if (Objects.nonNull(target) && searchType == TicketSearchSubCmdGroup.SearchType.OWNER) {
                tickets = TicketRepository.getTicketsByUser(target.getUser());
            } else if (Objects.nonNull(target) && searchType == TicketSearchSubCmdGroup.SearchType.PARTICIPANT) {
                tickets = TicketParticipantRepository.getTicketsFromParticipant(target);
            } else if (search.isPresent() && searchType == TicketSearchSubCmdGroup.SearchType.NAME) {
                tickets = TicketRepository.getTicketsWithMatchingName(search.get().trim());
            }

            PaginationItem header = new PaginationItem();
            header.addChild(TextDisplay.ofFormat("# %s Tickets gefunden", tickets.size()));

            if (searchType == TicketSearchSubCmdGroup.SearchType.OWNER) {
                header.addChild(TextDisplay.ofFormat("**%s** ist besitzer\\*in von folgenden Tickets:", target.getEffectiveName()));
            } else if (searchType == TicketSearchSubCmdGroup.SearchType.PARTICIPANT) {
                header.addChild(TextDisplay.ofFormat("**%s** ist teilnehmer\\*in in folgenden Tickets:", target.getEffectiveName()));
            } else {
                header.addChild(TextDisplay.ofFormat("**%s** stimmt mit folgenden Ticketnamen überein:", search.get().trim()));
            }
            setHeader(header);

            tickets.sort(Comparator.comparing(Ticket::getCreatedAt).reversed());
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

        TicketSearchSubCmdGroup.SearchType searchType = TicketSearchSubCmdGroup.SearchType.valueOf(context.requireArg("search"));

        PaginationItem item = new PaginationItem();
        item.setTitle(String.format("### %s %s", ticket.getStatus().getIcon(), ticket.getName()));

        StringBuilder details = new StringBuilder();
        if (Objects.nonNull(ticket.getSummary())) {
            details.append(String.format("> Zusammenfassung: \"%s\"", Util.escapeMarkdown(ticket.getSummary())));
            details.append("\n");
        }

        if (Objects.nonNull(ticket.getAssignee())) {
            details.append(String.format("> Verantwortlich: %s", ticket.getAssignee().getAsMention()));
            details.append("\n");
        } else {
            details.append("> Verantwortlich: Keine\\*r");
            details.append("\n");
        }
        if (searchType == TicketSearchSubCmdGroup.SearchType.OWNER) {
            if (!ticket.getParticipants().isEmpty()) {
                details.append(String.format("> Teilnehmende: %s", String.join(", ", ticket.getParticipants().stream().map(Member::getAsMention).toList())));
                details.append("\n");
            } else {
                details.append("> Teilnehmende: Keine");
                details.append("\n");
            }
        } else {
            details.append(String.format("> Ticket Inhaber\\*in: %s", ticket.getOwner().getAsMention()));
            details.append("\n");
        }

        if (Objects.nonNull(ticket.getCreatedAt()) && Objects.nonNull(ticket.getArchivedAt())) {
            details.append(String.format("> Geöffnet <t:%s:f> ∙ Archiviert <t:%s:f>", Util.toEpochSeconds(ticket.getCreatedAt()), Util.toEpochSeconds(ticket.getArchivedAt())));
            details.append("\n");
        } else if (Objects.nonNull(ticket.getCreatedAt())) {
            details.append(String.format("> Geöffnet <t:%s:f>", Util.toEpochSeconds(ticket.getCreatedAt())));
            details.append("\n");
        }

        if (Util.isAdministrator(context.member()) && ticket.getArchiveMessageId() != null && !ticket.getArchiveMessageId().isBlank()) {
            String ticketArchiveJumpLink = EnvKey.CHANNEL_TICKETLOGS.getAsChannel(TextChannel.class, EnvKey.GUILD_YUSERVER).getJumpUrl() + "/" + ticket.getArchiveMessageId();

            Section section = Section.of(
                    Button.of(ButtonStyle.LINK, ticketArchiveJumpLink, "Archiv"),
                    TextDisplay.ofFormat(details.toString())
            );
            item.addChild(section);
            item.addChild(Separator.createInvisible(Separator.Spacing.SMALL));
        } else {
            item.addChild(TextDisplay.ofFormat(details.toString()));
        }

        addItem(item);
    }
}
