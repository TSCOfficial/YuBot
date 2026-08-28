package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.exception.InvalidStateException;
import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.radiogroup.RadioGroup;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

/**
 * When the user executes the opt-in for the very first time, this modal is shown so that the moderator can select what option they would like to receive the active mod opt-in message.
 */
@Slf4j
public class SelectActiveModSendTypeModal extends Modal {

    /**
     * When discord can't send a message to the user because they don't share a mutual guild or they blocked DMs from the server.
     * <p>
     *     This is the error code for discord's exception, that is not listed in the ErrorResponse enum.
     * </p>
     */
    private static final int NO_MUTUAL_GUILD_EXCEPTION = 50278;

    @Override
    public String getId() {
        return "select-activemod-send-type";
    }

    @Override
    public String getTitle() {
        return "ActiveMod Messages";
    }

    @Override
    public List<ModalTopLevelComponent> getComponents() {
        RadioGroup.Builder builder = RadioGroup.create("type-selector");
        builder.addOption("Via Server", "server", "Sendet dir Nachricht in #active-moderation (standard)");
        builder.addOption("Via Direct Message", "dm", "Sendet die Nachrichten per DM");
        return List.of(
                TextDisplay.of("""
                        Wähle aus wo du die ActiveMod-Nachrichten erhalten möchtest.
                        -# Dies musst du einmalig machen. Ändern kannst du es jederzeit via /settings.
                        """),
                Label.of("Typ auswählen", builder.build())
        );
    }

    @Override
    public void execute(@NonNull ModalInteractionEvent event) throws SQLException, ClassNotFoundException, NullPointerException {
        String sendInDM = event.getValue("type-selector").getAsString();
        log.info("ActiveMod send type selection for {} set to {}", event.getMember().getEffectiveName(), sendInDM);
        event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("ℹ️ Du erhälst absofort die ActiveMod-Nachrichten via DM.").queue(
                    success -> {
                        try {
                            ProfileRepository.setSetting(event.getMember(), ProfileRepository.Setting.ACTIVEMOD_SEND_IN_DM, sendInDM.equals("dm"));
                            ActiveMod.registerModerator(event.getMember()).thenAccept(response -> {
                                event.reply(response).setEphemeral(true).queue();
                            });
                        } catch (Exception e) {
                            ExceptionHandler.handle(e, event);
                        }
                    },
                    failure -> {
                        try {
                            if (failure instanceof ErrorResponseException ere
                                    && (ere.getErrorResponse() == ErrorResponse.CANNOT_SEND_TO_USER || ere.getErrorCode() == NO_MUTUAL_GUILD_EXCEPTION)) {
                                log.warn("DMs of {} are disabled or blocked.", event.getMember().getEffectiveName());
                                throw new InvalidStateException(String.format("`%s` kann nicht auf Direct Message umgestellt werden.", ProfileRepository.Setting.ACTIVEMOD_SEND_IN_DM.getLabel()), "Es scheint als hättest du deine DMs deaktiviert oder den Bot blockiert.");
                            } else {
                                ExceptionHandler.handle(failure, event);
                            }
                        } catch (InvalidStateException ise) {
                            ExceptionHandler.handle(ise, event);
                        }
                    }
            );
        });
    }
}
