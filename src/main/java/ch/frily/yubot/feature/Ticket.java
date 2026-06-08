package ch.frily.yubot.feature;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.LocalDateTime;

public class Ticket {

    // Person who opened the Ticket
    @Getter
    @Setter
    private Member owner;

    // Team member that is assigned to this ticket
    @Getter
    @Setter
    private Member ticketAssignee;

    // The ticket channel itself
    @Getter
    @Setter
    private TextChannel channel;

    // Ticket type
    @Getter
    @Setter
    private TicketType type;

    // When the ticket was opened
    @Getter
    private LocalDateTime createdAt;

    // When the last message was sent
    @Getter
    private LocalDateTime lastActivityAt;
}
