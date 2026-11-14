package ch.frily.yubot.service.ticket.ctxmenu;

import ch.frily.yubot.Client;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.service.ticket.modal.ModTicketSelectorModal;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ModticketMsgCtxMenu implements IMessageContextMenu {
    @Override
    public void execute(@NonNull MessageContextInteractionEvent event) throws SQLException {
        ModTicketSelectorModal modal = new ModTicketSelectorModal();
        modal.addArgument("target", Objects.requireNonNull(event.getTarget().getMember()).getId());
        modal.setMessage(event.getTarget());
        modal.setInitiator(event.getMember());
        event.replyModal(modal.build()).queue();
    }

    @Override
    public String getName() {
        return "Open ModTicket";
    }

    @Override
    public List<Role> getAllowedRoles() {
        return Stream.of(
                Client.EnvKey.ROLE_SUPPORT,
                Client.EnvKey.ROLE_MODERATOR
        ).map(Client.EnvKey::getAsRole).toList();
    }
}
