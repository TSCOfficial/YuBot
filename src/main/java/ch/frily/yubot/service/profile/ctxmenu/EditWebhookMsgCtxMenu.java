package ch.frily.yubot.service.profile.ctxmenu;

import ch.frily.yubot.database.repository.ProfileMessageRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.service.profile.modal.EditWebhookMessageModal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * Look up whom a profile belongs to
 */
@Slf4j
public class EditWebhookMsgCtxMenu implements IMessageContextMenu {
    @Override
    public String getName() {
        return "Edit Message";
    }

    @Override
    public void execute(@NonNull MessageContextInteractionEvent event) throws SQLException {

        boolean isOwner = ProfileMessageRepository.isOwner(event.getMember(), event.getTarget());

        if (!event.getTarget().isWebhookMessage()) {
            throw new InvalidStateException("Keine Profil-Nachricht", "Es können nur Webhook-\"Profil\"-Nachrichten bearbeitet werden.");
        }

        if (!isOwner) {
            throw new PermissionDeniedException("Nur das Konto welches diese Nachricht verfasst hat kann die Nachricht bearbeiten");
        }

        EditWebhookMessageModal modal = new EditWebhookMessageModal();
        modal.setMessage(event.getTarget());
        event.replyModal(modal.build()).queue();
    }
}
