package ch.frily.yubot.service.ticket.ctxmenu;

import ch.frily.yubot.interaction.contextmenu.IUserContextMenu;
import ch.frily.yubot.service.ticket.modal.ModTicketSelectorModal;
import ch.frily.yubot.util.EnvKey;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class ModticketUserCtxMenu implements IUserContextMenu {
    @Override
    public void execute(@NonNull UserContextInteractionEvent event) throws SQLException {
        ModTicketSelectorModal modal = new ModTicketSelectorModal();
        modal.addArgument("target", event.getTargetMember().getId());
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
                EnvKey.ROLE_SUPPORT,
                EnvKey.ROLE_MODERATOR
        ).map(EnvKey::getAsRole).toList();
    }
}
