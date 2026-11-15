package ch.frily.yubot.service.forummanager.container;

import ch.frily.yubot.container.Container;
import ch.frily.yubot.service.forummanager.button.RegisterNewInterestBtn;
import net.dv8tion.jda.api.components.actionrow.ActionRow;

/**
 * The container that displays the registration procedures and other information about the "special-interest"-forum.
 * @since v2.5.0
 * @author Aliz Rose Frily
 */
public class RegisterInterestContainer extends Container {

    public RegisterInterestContainer() {
        addTextDisplay("# Interest Forum - Neues Interest registrieren");

        addTextDisplay("In diesem Forum kann man in den verschiedenen Posts über die entsprechenden Themen quatschen. Falls es ein Thema nicht gibt, kannst du es hier registrieren.");

        addComponent(
                ActionRow.of(
                        new RegisterNewInterestBtn().build()
                )
        );

    }
}
