package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.feature.ticket.*;
import ch.frily.yubot.interaction.button.btn.ticket.TicketCancelOpenBtn;
import ch.frily.yubot.interaction.button.btn.ticket.TicketConfirmOpenBtn;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TypeSelectorModal extends Modal {

    @Setter
    private TicketTypeGroup typeGroup;

    @Override
    public String getTitle() {
        return "Ticketsupport";
    }

    @Override
    public String getId() {
        return "modal:ticket-type-modal";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        List<ModalTopLevelComponent> components = new ArrayList<>();
        List<TicketType> types = Arrays.stream(TicketType.values()).filter(type -> type.getGroup() == typeGroup).toList();

        StringSelectMenu.Builder selectMenuBuilder = StringSelectMenu.create("select-menu:ticket-type-selector");
        types.forEach(type -> {
            if (type.getSelectDescription() != null) {
                selectMenuBuilder.addOption(type.getLabel(), type.getId(), type.getSelectDescription());
            }
        });
        selectMenuBuilder.setMinValues(1);
        selectMenuBuilder.setMaxValues(1);
        selectMenuBuilder.setRequired(true);
        selectMenuBuilder.setDefaultOptions(selectMenuBuilder.getOptions().getFirst());

        components.add(Label.of("Ticketart", selectMenuBuilder.build()));

        return components;
    }

    @Override
    public void execute(@NotNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        event.deferReply(true).queue();
        TicketType ticketType = Arrays.stream(TicketType.values()).filter(type ->
                Objects.equals(type.getId(), event.getValue("select-menu:ticket-type-selector").getAsStringList().getFirst())
        ).findFirst().orElseThrow(() -> new IllegalStateException("Tickettyp ist ungültig."));

        // check if person already has a ticket with the same type
        List<Ticket> openedTickets = TicketRepository.getTicketsByUser(event.getMember().getUser());
        openedTickets = openedTickets.stream().filter(ticket -> !ticket.getStatus().equals(TicketStatus.CLOSED) && ticket.getType().equals(ticketType)).toList();
        if (openedTickets.size() > 0) {
            String text = String.format("Du besitzt bereits ein Ticket mit dieser Art: %s", openedTickets.getFirst().getChannel().getAsMention());
            if (openedTickets.size() > 1) {
                text = String.format("Du besitzt bereits Tickets mit dieser Art: %s", openedTickets.stream().map(ticket -> ticket.getChannel().getAsMention()).collect(Collectors.joining(", ")));
            }
            TicketConfirmOpenBtn confirmOpenBtn = new TicketConfirmOpenBtn();
            confirmOpenBtn.addArgument("type", ticketType.name());
            TicketCancelOpenBtn cancelOpenBtn = new TicketCancelOpenBtn();

            event.getHook().editOriginal(String.format("%s\nBist du dir sicher dass du nochmal ein **%s-Ticket** öffnen möchtest?", text, ticketType.getLabel())).setComponents(ActionRow.of(confirmOpenBtn.build(), cancelOpenBtn.build())).queue();
            return;
        }

        TicketManager.getInstance().createTicket(ticketType, event.getMember(), channel -> {
            event.getHook().editOriginal("Dein Ticket wurde erstellt: " + channel.getAsMention()).queue();
        });
    }
}
