package ch.frily.yubot.service.forummanager.container;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.service.forummanager.button.RegisterNewInterestBtn;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.separator.Separator;

/**
 * The container that displays the registration procedures and other information about the "special-interest"-forum.
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class RegisterInterestContainer extends Container {

    public RegisterInterestContainer() {
        addTextDisplay("# Interest Forum - Neues Interest registrieren");

        addTextDisplay("In diesem Forum kann man in den verschiedenen Posts über die entsprechenden Themen quatschen. Falls es ein Thema nicht gibt, kannst du es hier registrieren.");

        addTextDisplay("**Wie registriere ich ein neues Interest?**");
        addTextDisplay("""
                Du kannst jederzeit ein neues Interest Registrieren. Drücke dafür auf den darunterstehenden Button und folge den Anweisungen im Modal:
                1. Gib dem Interest einen Namen
                  -# Dieser Name wird ebenfalls als Post-Name im Interestforum verwendet.
                2. Gib dem Interest eine Beschreibung
                  -# Diese Beschreibung wird ebenfalls als erste Nachricht bzw. as Postbeschreibung verwendet.
                """);
        addTextDisplay("""
                Das Moderationsteam wird eine Registrierung schnellstmöglich prüfen und, wenn alles den Regeln entspricht, freigeben.
                -# Ob das Interest Freigegeben worden ist oder nicht, wird dir mitgeteilt.
                """);

        addInvisibleSeparator(Separator.Spacing.SMALL);

        addComponent(
                ActionRow.of(
                        new RegisterNewInterestBtn().build()
                )
        );

    }
}
