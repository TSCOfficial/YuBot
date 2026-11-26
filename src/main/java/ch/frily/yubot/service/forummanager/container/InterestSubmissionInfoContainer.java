package ch.frily.yubot.service.forummanager.container;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.service.forummanager.button.SubmitNewInterestBtn;
import ch.frily.yubot.service.forummanager.entity.SpecialInterest;
import ch.frily.yubot.service.forummanager.repository.SpecialInterestRepository;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.separator.Separator;

import java.sql.SQLException;
import java.util.List;

/**
 * The container that displays the registration procedures and other information about the "special-interest"-forum.
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class InterestSubmissionInfoContainer extends Container {

    public InterestSubmissionInfoContainer() {
        addTextDisplay("# Special-Interests Forum - Neues Interests einsenden");

        addTextDisplay("In diesem Forum kann man in den verschiedenen Posts über die entsprechenden Themen quatschen. Falls es ein Thema noch nicht gibt, kannst du es hier einsenden.");

        addTextDisplay("### Wie sende ich ein neues Special-Interest ein?");
        addTextDisplay("""
                Du kannst jederzeit ein neues Interest einsenden, indem du auf den darunterstehenden Button klickst. Du wirst durch die Einsendung geführt. Hier ist der Verlauf im Überblick:
                1. Namen angeben
                   -# Gib deinem Interest einen Namen. Dieser wird anschliessend auch als Post-Name im Interestforum verwendet.
                2. Beschreibung angeben
                   -# Gib deinem Interest eine Beschreibung. Diese wird ebenfalls als erste Nachricht bzw. als Postbeschreibung verwendet.
                3. Benachrichtigung erhalten?
                   -# Du kannst auswählen, ob du bei Änderungen deiner Einsendung, über DM, Benachrichtigt werden möchtest. (Standard: Aktiv)
                4. Freigabe durch Moderator:innen
                   -# Das Moderationsteam wird deine Einsendung anschliessend überprüfen und entsprechend Freigeben, oder ablehnen.
                """);
        addTextDisplay("Bitte beachte, dass deine Einsendung den Serverregeln entspricht und nicht bereits ein ähnliches Special-Interest existiert.");

        try { // todo awaiting forum channel processing
            displayPendingSubmissions();
        } catch (SQLException _) {}

        addInvisibleSeparator(Separator.Spacing.SMALL);

        addComponent(
                ActionRow.of(
                        new SubmitNewInterestBtn().build()
                )
        );
    }

    private void displayPendingSubmissions() throws SQLException {
        List<SpecialInterest> pendingInterests = SpecialInterestRepository.getInterests(SpecialInterest.RequestStatus.PENDING);
        List<String> preparedInterestNames = pendingInterests.stream().map(interest -> String.format("- \"%s\"", interest.getName())).toList();

        if (!pendingInterests.isEmpty()) {
            addLineSeparator(Separator.Spacing.LARGE);
            addFormatedText("### Ausstehende Interestfreigaben (%d)", pendingInterests.size());
            addFormatedText("""
                    Folgende Special-Interests wurden eingesendet, befinden sich aber noch in Überprüfung:
                    %s
                    """, String.join("\n", preparedInterestNames));
            addInvisibleSeparator(Separator.Spacing.SMALL);
        }
    }
}
