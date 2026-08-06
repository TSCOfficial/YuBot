package ch.frily.yubot.interaction.button.btn;

import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.interaction.button.Button;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jspecify.annotations.NonNull;

import java.sql.SQLException;

public class AbsenceApproveDeleteBtn extends Button {
    @Override
    public String getId() {
        return "approve-detele-absence-btn";
    }

    @Override
    public String getLabel() {
        return "Ja, löschen";
    }

    @Override
    public ButtonStyle getStyle() {
        return ButtonStyle.DANGER;
    }

    @Override
    public void execute(@NonNull ButtonInteractionEvent event) throws SQLException, ClassNotFoundException, NoSuchMethodException {
        String absenceId = this.getArgument(event.getComponentId(), "absence_id");
        AbsenceRepository.deleteAbsenceById(Integer.parseInt(absenceId));
        event.getMessage().delete().queue();
        event.reply("Abwesenheit gelöscht").setEphemeral(true).queue();
    }
}
