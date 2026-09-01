package ch.frily.yubot.container.absence;

import ch.frily.yubot.container.ContainerContext;
import ch.frily.yubot.container.PaginationContainer;
import ch.frily.yubot.container.PaginationItem;
import ch.frily.yubot.container.StaticContainerRegistry;
import ch.frily.yubot.exception.ExceptionHandler;
import ch.frily.yubot.feature.absence.Absence;
import ch.frily.yubot.database.repository.AbsenceRepository;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceAddBtn;
import ch.frily.yubot.interaction.button.btn.absence.AbsenceEditBtn;
import ch.frily.yubot.util.Util;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class AbsenceEditOwnContainer extends PaginationContainer {

    public AbsenceEditOwnContainer(ContainerContext context) {
        super(StaticContainerRegistry.ABSENCE_EDITOWN, context);
        try {
            PaginationItem header = new PaginationItem();
            header.addChild(TextDisplay.of("# Deine Absenzen"));
            setHeader(header);

            // Derived from the interaction, so it is restored on every navigation click by itself
            Member member = context.member();

            if (member == null) {
                addItem(TextDisplay.of("Member not defined"));
                return;
            }

            List<Absence> absences = AbsenceRepository.getAbsences(member);
            absences.stream()
                    .forEach(this::addSectionText);

            if (absences.isEmpty()) {
                PaginationItem noAbsences = new PaginationItem();
                noAbsences.addChild(TextDisplay.of("Du hast keine kommenden Absenzen."));
                noAbsences.addChild(Separator.createDivider(Separator.Spacing.LARGE));
                addItem(noAbsences);
            }

            PaginationItem footer = new PaginationItem();
            footer.addChild(ActionRow.of(new AbsenceAddBtn().build()));
            setFooter(footer);

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

        private void addSectionText(Absence absence) {
        PaginationItem section = new PaginationItem();
        AbsenceEditBtn btn = new AbsenceEditBtn();
        btn.addArgument("absence_id", absence.id().toString());

        section.addChild(Section.of(btn.build(),
                TextDisplay.ofFormat("## <t:%d:D> - <t:%d:D>", Util.toEpochSeconds(absence.fromDateTime()), Util.toEpochSeconds(absence.toDateTime())),
                TextDisplay.ofFormat("""
                                -# %s %s
                                "%s"
                                Abwesenheitsmeldung: %s
                                -# Erstellt am: <t:%d:F>
                                %s
                                """,
                        absence.type().getEmoji().getFormatted(), absence.type().getLabel(),
                        absence.reason(),
                        absence.absenceMessage() ? "🟢 Aktiv" : "🔴 Deaktiviert",
                        Util.toEpochSeconds(absence.createdAt()),
                        absence.updatedAt() == null ? "" : String.format("-# Zuletzt aktualisiert am: <t:%d:F>", Util.toEpochSeconds(absence.updatedAt()))
                )));
        section.addChild(Separator.createDivider(Separator.Spacing.LARGE));
        addItem(section);
    }
}
