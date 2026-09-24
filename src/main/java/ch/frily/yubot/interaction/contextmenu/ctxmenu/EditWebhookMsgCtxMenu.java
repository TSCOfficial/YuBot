package ch.frily.yubot.interaction.contextmenu.ctxmenu;

import ch.frily.yubot.database.repository.ProfileHistoryRepository;
import ch.frily.yubot.database.repository.ProfileRepository;
import ch.frily.yubot.exception.NotFoundException;
import ch.frily.yubot.feature.profile.Profile;
import ch.frily.yubot.feature.profile.ProfileHistory;
import ch.frily.yubot.interaction.contextmenu.IMessageContextMenu;
import ch.frily.yubot.interaction.modal.modal.EditWebhookMessageModal;
import ch.frily.yubot.util.EnvKey;
import ch.frily.yubot.util.EnvResolver;
import ch.frily.yubot.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

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
    public void execute(@NonNull MessageContextInteractionEvent event) throws SQLException, ClassNotFoundException {
        EditWebhookMessageModal modal = new EditWebhookMessageModal();
        modal.setMessage(event.getTarget());
        event.replyModal(modal.build()).queue();
    }
}
