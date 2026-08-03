package ch.frily.yubot.container;

import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.interaction.button.btn.AbsenceEditBtn;
import ch.frily.yubot.util.Util;
import lombok.Setter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import java.time.format.DateTimeFormatter;

public class AbsenceDetailContainer extends Container{

    public AbsenceDetailContainer(Absence absence) {
        String fromDate = String.format("%d. %s", absence.fromDateTime().getDayOfMonth(), Util.translateMonth(absence.fromDateTime().getMonth()));
        long fromTimestamp = Util.toEpochSeconds(absence.fromDateTime());
        String toDate = String.format("%d. %s", absence.toDateTime().getDayOfMonth(), Util.translateMonth(absence.toDateTime().getMonth()));
        long toTimestamp = Util.toEpochSeconds(absence.toDateTime());
        this.addFormatedText("## Abwesenheit %s - %s", fromDate, toDate);
        this.addFormatedText("-# %s (%s)", absence.member().getAsMention(), absence.member().getEffectiveName());
        this.addFormatedText("Begründung: %s", absence.reason());
        this.addFormatedText("Abwesend vom <t:%d:F> bis <t:%d:F>", fromTimestamp, toTimestamp);




        this.addLineSeparator(Separator.Spacing.LARGE);

        AbsenceEditBtn btn = new AbsenceEditBtn();
        btn.addArgument("absence_id", absence.id().toString());

        String updatedAt = absence.updatedAt() == null ? "" : String.format(", zuletzt aktualisiert am: <t:%d:F>", Util.toEpochSeconds(absence.updatedAt()));
        this.addSection(btn.build(),
                TextDisplay.ofFormat("-# Erstellt am: <t:%d:F>%s", Util.toEpochSeconds(absence.createdAt()), updatedAt),
                absence.updatedAt() != null ? TextDisplay.ofFormat("-# Aktualisiert am: <t:%d:F>", Util.toEpochSeconds(absence.updatedAt())) : null
        );
    }
}
