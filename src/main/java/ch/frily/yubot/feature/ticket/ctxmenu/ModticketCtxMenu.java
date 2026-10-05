package ch.frily.yubot.feature.ticket.ctxmenu;

import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.ticket.Ticket;
import ch.frily.yubot.feature.ticket.TicketManager;
import ch.frily.yubot.database.repository.TicketRepository;
import ch.frily.yubot.feature.ticket.TicketType;
import ch.frily.yubot.interaction.contextmenu.IUserContextMenu;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ModticketCtxMenu implements IUserContextMenu {
    @Override
    public void execute(@NonNull UserContextInteractionEvent event) throws SQLException {
        event.deferReply(true).queue();
        Member member = Objects.requireNonNull(event.getTargetMember());
        TicketManager.getInstance().createTicket(TicketType.MODTICKET, member, ThrowingConsumer.wrap(event, channel -> {
            event.getHook().editOriginal(String.format("Modticket wurde erstellt: %s", channel.getAsMention())).queue();

            Ticket ticket = TicketRepository.getTicketById(channel.getIdLong());
            ticket.claim(event.getMember());
        }));
    }

    @Override
    public String getName() {
        return "Open ModTicket";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SUPPORT,
                EnvKey.ROLE_MODERATOR
        ).map(EnvResolver::getRoleById).toList();
    }
}
