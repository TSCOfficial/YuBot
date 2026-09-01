package ch.frily.yubot.interaction.button.btn.activemod;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.activemod.ActiveMod;
import ch.frily.yubot.feature.profile.ProfileRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.interaction.modal.modal.SelectActiveModSendTypeModal;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
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
            // If the user does not have set the activeModSendInDm in Profile, request to set it
            event.replyModal(new SelectActiveModSendTypeModal().build()).queue();
            return;
        }

        event.deferReply(true).queue();

        ActiveMod.registerModerator(event.getMember()).thenAccept(response -> {
            event.getHook().sendMessage(response).setEphemeral(true).queue();
        }).exceptionally(throwable -> {
            return ExceptionHandler.fail(throwable);
        });
    }
}
