package ch.frily.yubot.container.ticket;

import ch.frily.yubot.Client;
import ch.frily.yubot.container.*;
import ch.frily.yubot.service.ticket.repository.TicketParticipantRepository;
import ch.frily.yubot.service.ticket.repository.TicketRepository;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.service.ticket.slashcommand.TicketSearchSubCmdGroup;
import ch.frily.yubot.util.Util;
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
import java.util.stream.Collectors;

@Slf4j
public class TicketSearchContainer extends PaginationContainer {

    private ContainerContext context;

    public TicketSearchContainer(ContainerContext context) {
        super(StaticContainerRegistry.TICKET_SEARCH_OVERVIEW, context);
        this.context = context;

        User target = null;
        Optional<String> targetId = context.arg("target");
        if (targetId.isPresent()) {
            target = Client.getInstance().getClient().getUserById(targetId.get());
        }

        Optional<String> search = context.arg("q");

        TicketSearchSubCmdGroup.SearchType searchType = TicketSearchSubCmdGroup.SearchType.valueOf(context.requireArg("search"));
        try {
            List<Ticket> tickets = null;
            if (Objects.nonNull(target) && searchType == TicketSearchSubCmdGroup.SearchType.OWNER) {
                tickets = TicketRepository.getTicketsByUser(target);
            } else if (Objects.nonNull(target) && searchType == TicketSearchSubCmdGroup.SearchType.PARTICIPANT) {
                Member memberTarget = Client.EnvKey.GUILD_YUSERVER.getAsGuild().getMemberById(targetId.get());
                tickets = TicketParticipantRepository.getTicketsFromParticipant(memberTarget); // todo replace by User
            } else if (search.isPresent() && searchType == TicketSearchSubCmdGroup.SearchType.NAME) {
                tickets = TicketRepository.getTicketsWithMatchingName(search.get().trim());
            }

            int originalMatchedTicketListSize = tickets.size();
            tickets = tickets.stream().filter(ticket -> {
                if (Util.isAdministrator(context.member())) {
                    return true;
                }
                return ticket.getType().getResponsibleRoles().stream().anyMatch(role -> context.requireMember().getRoles().contains(role));
            }).collect(Collectors.toList());

            PaginationItem header = new PaginationItem();
            header.addChild(TextDisplay.ofFormat("# %s Tickets gefunden", tickets.size()));

            if (originalMatchedTicketListSize > tickets.size()) {
                header.addChild(TextDisplay.of("Du siehst nur die Tickets auf dennen du Zugriff hättest."));
            }

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
            details.append(String.format("> **Zusammenfassung**: \"%s\"", Util.escapeMarkdown(ticket.getSummary()).replace("\n\n", "\n").replace("\n", "\n> ")));
            details.append("\n");
        }

        if (Objects.nonNull(ticket.getAssigneeId())) {
            if (Objects.nonNull(ticket.getAssignee())) { // null if the assignee is no longer on the guild
                details.append(String.format("> **Verantwortlich**: %s (%s)", ticket.getAssignee().getAsMention(), ticket.getAssignee().getEffectiveName()));
                details.append("\n");
            } else {
                details.append(String.format("> **Verantwortlich**: %s *(Person hat den Server verlassen)*", ticket.getAssigneeId()));
                details.append("\n");
            }

        } else {
            details.append("> **Verantwortlich**: Keine\\*r");
            details.append("\n");
        }
        if (searchType == TicketSearchSubCmdGroup.SearchType.OWNER) {
            if (!ticket.getParticipantIds().isEmpty()) {
                StringBuilder participantsSB = new StringBuilder();
                ticket.getParticipantIds().forEach(participantId -> {
                    User participantUser = Client.getInstance().getClient().getUserById(participantId);
                    if (Objects.nonNull(participantUser)) {
                        String info = String.format("%s (%s)", participantUser.getAsMention(), participantUser.getEffectiveName());
                        participantsSB.append(info).append(", ");
                    } else {
                        participantsSB.append(participantId).append(", ");
                    }
                });
                details.append(String.format("> **Teilnehmende**: %s", participantsSB));
                details.append("\n");
            } else {
                details.append("> **Teilnehmende**: Keine");
                details.append("\n");
            }
        } else {
            if (Objects.nonNull(ticket.getUserOwner())) {
                details.append(String.format("> **Ticket Inhaber\\*in**: %s (%s)", ticket.getUserOwner().getAsMention(), ticket.getUserOwner().getEffectiveName()));
                details.append("\n");
            } else {
                details.append(String.format("> **Ticket Inhaber\\*in**: %s *(Person hat den Server verlassen)*", ticket.getOwnerId()));
                details.append("\n");
            }
        }

        if (Objects.nonNull(ticket.getCreatedAt()) && Objects.nonNull(ticket.getArchivedAt())) {
            details.append(String.format("> -# Geöffnet <t:%s:f> ∙ Archiviert <t:%s:f>", Util.toEpochSeconds(ticket.getCreatedAt()), Util.toEpochSeconds(ticket.getArchivedAt())));
            details.append("\n");
        } else if (Objects.nonNull(ticket.getCreatedAt())) {
            details.append(String.format("> -# Geöffnet <t:%s:f>", Util.toEpochSeconds(ticket.getCreatedAt())));
            details.append("\n");
        }

        if (Objects.nonNull(context.member()) && Util.isAdministrator(context.member()) && ticket.getArchiveMessageId() != null && !ticket.getArchiveMessageId().isBlank()) {
            String ticketArchiveJumpLink = Client.EnvKey.CHANNEL_TICKETLOGS.getAsChannel(TextChannel.class, Client.EnvKey.GUILD_YUSERVER).getJumpUrl() + "/" + ticket.getArchiveMessageId();

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
