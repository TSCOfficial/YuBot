package ch.frily.yubot.service.ticket.modal;

import ch.frily.yubot.service.ticket.Ticket;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.service.ticket.TicketType;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class TicketSummaryModal extends Modal {

    private Ticket ticket;

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        addArgument("ticket_id", ticket.getChannel().getId());
    }

    @Override
    public String getId() {
        return "ticket-summary-modal";
    }

    @Override
    public String getTitle() {
        return "Zusammenfassung angeben";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        TextInput.Builder summaryInput = TextInput.create("summary", TextInputStyle.PARAGRAPH);
        summaryInput.setRequiredRange(10, 200);
        summaryInput.setPlaceholder("User1 hat User2 wegen xy gemeldet...");
        boolean isRequired = ticket.getType() == TicketType.MODTICKET;
        summaryInput.setRequired(isRequired);

        String summaryOptionalInfo = "Zusammenfassungen sind optional, werden aber für die Archivierung empfohlen.";
        if (isRequired) {
            summaryOptionalInfo = "Zusammenfassungen für ModTickets sind pflicht.";
        }

        return List.of(
                TextDisplay.of("""
                        Verfasse in ein paar Worten um was das Ticket ging.
                        -# Dies hilft Tickets im Ticket-log schneller wieder zu finden, wenn etwas geprüft werden muss.
                        """),
                Label.of("Zusammenfassung", summaryInput.build()),
                TextDisplay.of(String.format("-# %s", summaryOptionalInfo))
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, NullPointerException {
        long ticketId = Long.parseLong(this.getArgument(event.getModalId(), "ticket_id"));
        Ticket ticket = TicketRepository.getTicketByChannelId(ticketId);

        event.deferReply(true).queue();
        ModalMapping summary = event.getValue("summary");
        if (summary != null && !summary.getAsString().isBlank()) {
            ticket.setSummary(summary.getAsString().replace("\n", "\n> "));
        }

        ticket.archive(event.getMember());
    }
}
