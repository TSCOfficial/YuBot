package ch.frily.yubot.container;

import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.interaction.button.btn.AbsenceEditBtn;
import ch.frily.yubot.util.Util;
import lombok.Setter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;

import java.time.format.DateTimeFormatter;

public class AbsenceDetailContainer extends Container{

    public AbsenceDetailContainer(Absence absence) {
        String fromDate = absence.fromDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM."));
        long fromTimestamp = Util.toEpochSeconds(absence.fromDateTime());
        String toDate = absence.toDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM."));
        long toTimestamp = Util.toEpochSeconds(absence.toDateTime());
        this.addFormatedText("## Abwesenheit %s - %s", fromDate, toDate);
        this.addFormatedText("-# Id: %s", absence.id());
        this.addFormatedText("Person: %s", absence.member().getAsMention());
        this.addFormatedText("Begründung: %s", absence.reason());
        this.addFormatedText("Abwesenheit: <t:%d:F> - <t:%d:F>", fromTimestamp, toTimestamp);

        AbsenceEditBtn btn = new AbsenceEditBtn();
        btn.addArgument("absence_id", absence.id().toString());

        this.addComponent(ActionRow.of(btn.build()));
    }
}
