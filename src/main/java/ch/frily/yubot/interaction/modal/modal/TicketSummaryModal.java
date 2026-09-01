package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.container.ticket.TicketTranscriptContainer;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.feature.ticket.TicketRepository;
import ch.frily.yubot.feature.ticket.TicketType;
import ch.frily.yubot.interaction.modal.Modal;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
        summaryInput.setRequired(ticket.getType() == TicketType.MODTICKET);

        return List.of(
                TextDisplay.of("""
                        Verfasse in ein paar Worten oder Sätzen, um was das Ticket ging.
                        -# Dies hilft Tickets, im Ticket-log, schneller wieder zu finden, wenn etwas geprüft werden muss.
                        """),
                Label.of("Zusammenfassung", summaryInput.build()),
                TextDisplay.of("-# Zusammenfassungen sind Optional aber empfohlen.")
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        event.deferReply(true).queue();
        ModalMapping summary = event.getValue("summary");
        String summaryText;
        if (summary != null && !summary.getAsString().isBlank()) {
            summaryText = summary.getAsString().replace("\n", "\n> ");
        } else {
            summaryText = null;
        }

        long ticketId = Long.parseLong(this.getArgument(event.getModalId(), "ticket_id"));
        Ticket ticket = TicketRepository.getTicketById(ticketId);

        ticket.generateTranscript().thenAccept(ThrowingConsumer.wrap(event, fileUpload -> {
            fileUpload.setName("transkript-" + ticket.getNameWithoutStatus() + ".html");
            TextChannel logChannel = EnvResolver.getChannelById(TextChannel.class, EnvKey.GUILD_YUSERVER, EnvKey.CHANNEL_TICKETLOGS);
            List<Container> containers = new TicketTranscriptContainer(event.getMember(), ticket, fileUpload, summaryText).build();
            logChannel.sendMessageComponents(containers).useComponentsV2().addFiles(fileUpload).setAllowedMentions(List.of()).queue(ThrowingConsumer.wrap(event, message -> {
                event.getHook().editOriginal("Ticket wird gelöscht.").queue();
                ticket.delete();
            }));
        }));
    }
}
