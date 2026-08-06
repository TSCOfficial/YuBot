package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.interaction.button.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class AbsenceCancelDeleteBtn extends Button {
    @Override
    public String getId() {
        return "cancel-detele-absence-btn";
    }

    @Override
    public String getLabel() {
        return "Nein, abbrechen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.SECONDARY;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        event.reply("Absenzlöschung abgebrochen.").setEphemeral(true).queue();
        event.getMessage().delete().queue();
    }
}
