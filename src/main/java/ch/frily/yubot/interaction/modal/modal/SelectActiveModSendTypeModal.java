package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.Client;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.activemod.ActiveMod;
import ch.frily.yubot.feature.profile.ProfileRepository;
import ch.frily.yubot.feature.profile.Setting;
import ch.frily.yubot.feature.profile.SettingOption;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.radiogroup.RadioGroup;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * When the user executes the opt-in for the very first time, this modal is shown so that the moderator can select what option they would like to receive the active mod opt-in message.
 */
@Slf4j
public class SelectActiveModSendTypeModal extends Modal {


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
        Setting.ACTIVEMOD_SEND_IN_DM.getAutocompleteOptions().forEach(option -> builder.addOption(option.label(), String.valueOf(option.value()), option.description()));
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
        log.info("ActiveModSendTypeModal executed: {}", event.getValue("type-selector").getAsString());
        Boolean sendInDM = event.getValue("type-selector").getAsString().equals("true");
        AtomicBoolean couldntSetToDMs = new AtomicBoolean(false);
        if (sendInDM) {
            try {
                PrivateChannel privateChannel = event.getMember().getUser().openPrivateChannel().complete();
                privateChannel.sendMessage("ℹ️ Du erhälst absofort die ActiveMod-Nachrichten via DM.").complete();
                ProfileRepository.upsertSetting(event.getMember(), Setting.ACTIVEMOD_SEND_IN_DM, true);
            } catch (ErrorResponseException ere) {
                if (ere.getErrorResponse() == ErrorResponse.CANNOT_SEND_TO_USER || ere.getErrorCode() == Client.NO_MUTUAL_GUILD_EXCEPTION) {
                    ProfileRepository.upsertSetting(event.getMember(), Setting.ACTIVEMOD_SEND_IN_DM, false);
                    couldntSetToDMs.set(true);
                } else {
                    ExceptionHandler.handle(ere, event);
                }

            }
        } else {
            ProfileRepository.upsertSetting(event.getMember(), Setting.ACTIVEMOD_SEND_IN_DM, false);
        }
        SettingOption<Boolean> selectedSetting = Setting.ACTIVEMOD_SEND_IN_DM.getOptionByValue(couldntSetToDMs.get() ? false : sendInDM); // get the option if it could be sent, else get overwrite (via Server)
        ActiveMod.registerModerator(event.getMember()).thenAccept(responseText -> {
            StringBuilder replySB = new StringBuilder();
            replySB.append(couldntSetToDMs.get() ? "⚠️ " : "✅ ");
            replySB.append("Du erhälst deine ActiveMod-Nachrichten absofort ");
            replySB.append(selectedSetting.label()).append("\n");
            replySB.append(couldntSetToDMs.get() ? String.format("-# Deine Datenschutzeinstellungen erlauben keine DMs oder du hast den Bot blockiert, weshalb deine Einstellung auf %s überschrieben wurde.\n\n", selectedSetting.label()) : "\n");
            replySB.append(responseText);
            event.reply(replySB.toString()).setEphemeral(true).queue();
        }).exceptionally(throwable -> {
            return ExceptionHandler.fail(throwable);
        });
    }
}
