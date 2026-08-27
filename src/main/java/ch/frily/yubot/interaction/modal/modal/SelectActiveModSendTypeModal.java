package ch.frily.yubot.interaction.modal.modal;

import ch.frily.yubot.feature.ActiveMod;
import ch.frily.yubot.feature.ProfileRepository;
import ch.frily.yubot.interaction.modal.Modal;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.radiogroup.RadioGroup;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;
import java.util.List;

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
        builder.addOption("Via Server", "server","Sendet dir Nachricht in #active-moderation (standard)");
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
        ProfileRepository.setActiveModSendInDm(event.getMember(), sendInDM.equals("dm"));
        ActiveMod.registerModerator(event.getMember()).thenAccept(response -> {
            event.reply(response).setEphemeral(true).queue();
        });
    }
}
