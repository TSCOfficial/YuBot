package ch.frily.yubot.interaction.button.btn.activemod;

import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

/**
 * When the last active mod wants to opt-out via command, the bot askes to approve the opt-out before closing the server
 */
public class ActiveModCancelOptOutBtn extends Button {
    @Override
    public String getId() {
        return "approve-activemod-optout";
    }

    @Override
    public String getLabel() {
        return "Bestätige";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.PRIMARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        event.editMessage("Opt-out Prozess abgebrochen.").queue();
    }
}
