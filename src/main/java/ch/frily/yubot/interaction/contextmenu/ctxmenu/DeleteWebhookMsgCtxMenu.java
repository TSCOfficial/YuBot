package ch.frily.yubot.interaction.contextmenu.ctxmenu;

import ch.frily.yubot.database.repository.ProfileMessageRepository;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.PermissionDeniedException;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.interaction.modal.modal.EditWebhookMessageModal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * Look up whom a profile belongs to
 */
@Slf4j
public class DeleteWebhookMsgCtxMenu implements IMessageContextMenu {
    @Override
    public String getName() {
        return "Delete Message";
    }

    @Override
    public void execute(@NonNull MessageContextInteractionEvent event) throws SQLException, ClassNotFoundException {
        event.deferReply(true).queue();
        boolean isOwner = ProfileMessageRepository.isOwner(event.getMember(), event.getTarget());

        if (!event.getTarget().isWebhookMessage()) {
            throw new InvalidStateException("Keine Profil-Nachricht", "Es können nur Webhook-\"Profil\"-Nachrichten gelöscht werden.");
        }

        if (!isOwner) {
            throw new PermissionDeniedException("Nur das Konto welches diese Nachricht verfasst hat kann die Nachricht bearbeiten");
        }

        event.getTarget().delete().queue();
        event.getHook().deleteOriginal().queue();
    }
}
