package ch.frily.yubot.interaction.contextmenu.ctxmenu;

import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.Ticket;
import ch.frily.yubot.feature.TicketManager;
import ch.frily.yubot.feature.TicketRepository;
import ch.frily.yubot.feature.TicketType;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.interaction.contextmenu.IUserContextMenu;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class ModticketCtxMenu implements IUserContextMenu {
    @Override
    public void execute(@NonNull UserContextInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.deferReply(true).queue();
        Member member = event.getTargetMember();
        TicketManager.getInstance().createTicket(TicketType.MODTICKET, member, ThrowingConsumer.wrap(event, channel -> {
            event.getHook().editOriginal(String.format("Modticket wurde erstellt: %s", channel.getAsMention())).queue();

            Ticket ticket = TicketRepository.getTicketById(channel.getIdLong());
            ticket.claim(event.getMember());
        }));
    }

    @Override
    public String getName() {
        return "open ModTicket";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                EnvKey.ROLE_SUPPORT,
                EnvKey.ROLE_MODERATOR
        ).map(EnvResolver::getRoleById).toList();
    }
}
