package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.exception.ThrowingConsumer;
import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.ActiveModRepository;
import ch.frily.yubot.feature.Closure;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.SelectActiveModSendTypeModal;
import ch.frily.yubot.util.EnvResolver;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * When the last active mod wants to opt-out via command, the bot askes to approve the opt-out before closing the server
 */
public class ActiveModOptInBtn extends Button {
    @Override
    public String getId() {
        return "activemod-optin";
    }

    @Override
    public String getLabel() {
        return "Opt-in";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SUCCESS;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        if (ProfileRepository.getProfile(event.getMember()) == null || ProfileRepository.getProfile(event.getMember()).activeModSendInDm() == null) {
            // If the user does not have set the activeModSendInDm
            event.replyModal(new SelectActiveModSendTypeModal().build()).queue();
            return;
        }

        ActiveMod.registerModerator(event.getMember()).thenAccept(response -> {
            event.reply(response).setEphemeral(true).queue();
        });
    }
}
