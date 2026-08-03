package ch.frily.yubot.container;

import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.Absence;
import ch.frily.yubot.feature.AbsenceRepository;
import ch.frily.yubot.interaction.button.btn.AbsenceAddBtn;
import ch.frily.yubot.interaction.button.btn.AbsenceDetailBtn;
import ch.frily.yubot.interaction.button.btn.AbsenceEditBtn;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbsenceEditOwnContainer extends Container {

    public AbsenceEditOwnContainer(Member member) {
        try {
            List<Absence> absences = AbsenceRepository.getAbsences(member);

            addFormatedText("# Deine Absenzen");

            absences.stream()
                    .forEach(this::addSectionText);

            if (absences.isEmpty()) {
                addFormatedText("Du hast keine kommenden Absenzen.");
                addLineSeparator(Separator.Spacing.LARGE);
            }

            addComponent(ActionRow.of(
                    new AbsenceAddBtn().build()
            ));

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

        private void addSectionText(Absence absence) {
        AbsenceEditBtn btn = new AbsenceEditBtn();
        btn.addArgument("absence_id", absence.id().toString());

        addSection(btn.build(),
                TextDisplay.ofFormat("## <t:%d:D> - <t:%d:D>", Util.toEpochSeconds(absence.fromDateTime()), Util.toEpochSeconds(absence.toDateTime())),
                TextDisplay.ofFormat("""
                        "%s"
                        Abwesenheitsmeldung: %s
                        -# Erstellt am: <t:%d:F>
                        %s
                        """,
                        absence.reason(),
                        absence.absenceMessage() ? "🟢 Aktiv" : "🔴 Deaktiviert",
                        Util.toEpochSeconds(absence.createdAt()),
                        absence.updatedAt() == null ? "" : String.format("-# Zuletzt aktualisiert am: <t:%d:F>", Util.toEpochSeconds(absence.updatedAt()))
        ));
        addLineSeparator(Separator.Spacing.LARGE);
    }
}
